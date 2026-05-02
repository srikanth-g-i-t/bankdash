package com.bankdash.account.service.impl;

import com.bankdash.account.dto.AccountDtos.*;
import com.bankdash.account.entity.Account;
import com.bankdash.account.entity.AccountStatus;
import com.bankdash.account.repository.AccountRepository;
import com.bankdash.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public AccountResponse createAccount(UUID userId, CreateAccountRequest request) {
        Account account = Account.builder()
            .userId(userId)
            .accountNumber(generateAccountNumber())
            .accountType(request.getAccountType())
            .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
            .nickname(request.getNickname())
            .balance(BigDecimal.ZERO)
            .availableBalance(BigDecimal.ZERO)
            .status(AccountStatus.ACTIVE)
            .build();

        Account saved = accountRepository.save(account);
        log.info("Account created: {} for user: {}", saved.getAccountNumber(), userId);
        return toResponse(saved);
    }

    @Override
    public List<AccountResponse> getAccountsByUser(UUID userId) {
        return accountRepository.findByUserId(userId).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    public AccountResponse getAccount(UUID accountId, UUID userId) {
        Account account = accountRepository.findById(accountId)
            .filter(a -> a.getUserId().equals(userId))
            .orElseThrow(() -> new RuntimeException("Account not found or access denied"));
        return toResponse(account);
    }

    @Override
    public AccountResponse getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
            .map(this::toResponse)
            .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));
    }

    @Override
    public AccountSummary getAccountSummary(UUID userId) {
        List<AccountResponse> accounts = getAccountsByUser(userId);
        BigDecimal total = accountRepository.sumBalanceByUserId(userId);

        AccountSummary summary = new AccountSummary();
        summary.setUserId(userId);
        summary.setTotalAccounts(accounts.size());
        summary.setTotalBalance(total != null ? total : BigDecimal.ZERO);
        summary.setAccounts(accounts);
        return summary;
    }

    @Override
    @Transactional
    public AccountResponse updateBalance(UpdateBalanceRequest request) {
        Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
            .orElseThrow(() -> new RuntimeException("Account not found"));

        BigDecimal newBalance = account.getBalance().add(request.getAmount());
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        account.setBalance(newBalance);
        account.setAvailableBalance(newBalance);
        return toResponse(accountRepository.save(account));
    }

    @Override
    @Transactional
    public void closeAccount(UUID accountId, UUID userId) {
        Account account = accountRepository.findById(accountId)
            .filter(a -> a.getUserId().equals(userId))
            .orElseThrow(() -> new RuntimeException("Account not found"));
        account.setStatus(AccountStatus.CLOSED);
        accountRepository.save(account);
    }

    private String generateAccountNumber() {
        String number;
        do {
            number = "BD" + String.format("%014d", new Random().nextLong(100_000_000_000_000L));
        } while (accountRepository.existsByAccountNumber(number));
        return number;
    }

    private AccountResponse toResponse(Account a) {
        AccountResponse r = new AccountResponse();
        r.setId(a.getId());
        r.setAccountNumber(a.getAccountNumber());
        r.setAccountType(a.getAccountType());
        r.setBalance(a.getBalance());
        r.setAvailableBalance(a.getAvailableBalance());
        r.setCurrency(a.getCurrency());
        r.setStatus(a.getStatus());
        r.setNickname(a.getNickname());
        r.setCreatedAt(a.getCreatedAt());
        return r;
    }
}
