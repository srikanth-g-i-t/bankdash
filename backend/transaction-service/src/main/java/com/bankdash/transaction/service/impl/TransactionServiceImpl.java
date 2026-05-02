package com.bankdash.transaction.service.impl;

import com.bankdash.transaction.dto.TransactionDtos.*;
import com.bankdash.transaction.entity.*;
import com.bankdash.transaction.kafka.TransactionEvent;
import com.bankdash.transaction.kafka.TransactionEventProducer;
import com.bankdash.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl {

    private final TransactionRepository transactionRepository;
    private final TransactionEventProducer eventProducer;
    private final RestTemplate restTemplate;

    @Value("${services.account-service.url}")
    private String accountServiceUrl;

    @Transactional
    public TransactionResponse transfer(UUID userId, TransferRequest request) {
        // Debit source account
        updateBalance(request.getFromAccountNumber(), request.getAmount().negate());
        // Credit destination account
        updateBalance(request.getToAccountNumber(), request.getAmount());

        Transaction tx = Transaction.builder()
            .referenceNumber(generateRef())
            .userId(userId)
            .fromAccountNumber(request.getFromAccountNumber())
            .toAccountNumber(request.getToAccountNumber())
            .type(TransactionType.TRANSFER)
            .amount(request.getAmount())
            .currency(request.getCurrency())
            .description(request.getDescription())
            .status(TransactionStatus.COMPLETED)
            .processedAt(LocalDateTime.now())
            .build();

        Transaction saved = transactionRepository.save(tx);
        publishEvent(saved);
        return toResponse(saved);
    }

    @Transactional
    public TransactionResponse deposit(UUID userId, DepositRequest request) {
        updateBalance(request.getAccountNumber(), request.getAmount());

        Transaction tx = Transaction.builder()
            .referenceNumber(generateRef())
            .userId(userId)
            .fromAccountNumber("EXTERNAL")
            .toAccountNumber(request.getAccountNumber())
            .type(TransactionType.DEPOSIT)
            .amount(request.getAmount())
            .description(request.getDescription() != null ? request.getDescription() : "Deposit")
            .status(TransactionStatus.COMPLETED)
            .processedAt(LocalDateTime.now())
            .build();

        Transaction saved = transactionRepository.save(tx);
        publishEvent(saved);
        return toResponse(saved);
    }

    @Transactional
    public TransactionResponse withdraw(UUID userId, WithdrawalRequest request) {
        updateBalance(request.getAccountNumber(), request.getAmount().negate());

        Transaction tx = Transaction.builder()
            .referenceNumber(generateRef())
            .userId(userId)
            .fromAccountNumber(request.getAccountNumber())
            .toAccountNumber("EXTERNAL")
            .type(TransactionType.WITHDRAWAL)
            .amount(request.getAmount())
            .description(request.getDescription() != null ? request.getDescription() : "Withdrawal")
            .status(TransactionStatus.COMPLETED)
            .processedAt(LocalDateTime.now())
            .build();

        Transaction saved = transactionRepository.save(tx);
        publishEvent(saved);
        return toResponse(saved);
    }

    public TransactionPageResponse getTransactions(UUID userId, int page, int size) {
        Page<Transaction> txPage = transactionRepository.findByUserId(
            userId, PageRequest.of(page, size, Sort.by("createdAt").descending())
        );
        return toPageResponse(txPage);
    }

    public TransactionResponse getTransaction(UUID txId, UUID userId) {
        Transaction tx = transactionRepository.findById(txId)
            .filter(t -> t.getUserId().equals(userId))
            .orElseThrow(() -> new RuntimeException("Transaction not found"));
        return toResponse(tx);
    }

    // ── helpers ──────────────────────────────────────────────────

    private void updateBalance(String accountNumber, BigDecimal amount) {
        Map<String, Object> body = new HashMap<>();
        body.put("accountNumber", accountNumber);
        body.put("amount", amount);
        restTemplate.patchForObject(
            accountServiceUrl + "/api/v1/accounts/balance", body, Object.class
        );
    }

    private void publishEvent(Transaction tx) {
        TransactionEvent event = TransactionEvent.builder()
            .transactionId(tx.getId())
            .referenceNumber(tx.getReferenceNumber())
            .userId(tx.getUserId())
            .type(tx.getType())
            .status(tx.getStatus())
            .amount(tx.getAmount())
            .currency(tx.getCurrency())
            .fromAccountNumber(tx.getFromAccountNumber())
            .toAccountNumber(tx.getToAccountNumber())
            .description(tx.getDescription())
            .occurredAt(tx.getCreatedAt())
            .build();
        eventProducer.publishTransactionEvent(event);
    }

    private String generateRef() {
        return "TXN" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private TransactionResponse toResponse(Transaction t) {
        TransactionResponse r = new TransactionResponse();
        r.setId(t.getId());
        r.setReferenceNumber(t.getReferenceNumber());
        r.setType(t.getType());
        r.setAmount(t.getAmount());
        r.setCurrency(t.getCurrency());
        r.setFee(t.getFee());
        r.setStatus(t.getStatus());
        r.setFromAccountNumber(t.getFromAccountNumber());
        r.setToAccountNumber(t.getToAccountNumber());
        r.setDescription(t.getDescription());
        r.setCategory(t.getCategory());
        r.setCreatedAt(t.getCreatedAt());
        r.setProcessedAt(t.getProcessedAt());
        return r;
    }

    private TransactionPageResponse toPageResponse(Page<Transaction> page) {
        TransactionPageResponse r = new TransactionPageResponse();
        r.setContent(page.getContent().stream().map(this::toResponse).collect(Collectors.toList()));
        r.setPage(page.getNumber());
        r.setSize(page.getSize());
        r.setTotalElements(page.getTotalElements());
        r.setTotalPages(page.getTotalPages());
        return r;
    }
}
