package com.bankdash.transaction.controller;

import com.bankdash.transaction.dto.TransactionDtos.*;
import com.bankdash.transaction.service.impl.TransactionServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Transaction processing — transfer, deposit, withdraw")
public class TransactionController {

    private final TransactionServiceImpl transactionService;

    @PostMapping("/transfer")
    @Operation(summary = "Transfer funds between accounts")
    public ResponseEntity<TransactionResponse> transfer(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody TransferRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(transactionService.transfer(UUID.fromString(userId), request));
    }

    @PostMapping("/deposit")
    @Operation(summary = "Deposit funds into an account")
    public ResponseEntity<TransactionResponse> deposit(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody DepositRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(transactionService.deposit(UUID.fromString(userId), request));
    }

    @PostMapping("/withdraw")
    @Operation(summary = "Withdraw funds from an account")
    public ResponseEntity<TransactionResponse> withdraw(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody WithdrawalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(transactionService.withdraw(UUID.fromString(userId), request));
    }

    @GetMapping
    @Operation(summary = "Get paginated transaction history")
    public ResponseEntity<TransactionPageResponse> getTransactions(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(transactionService.getTransactions(UUID.fromString(userId), page, size));
    }

    @GetMapping("/{transactionId}")
    @Operation(summary = "Get transaction by ID")
    public ResponseEntity<TransactionResponse> getTransaction(
            @PathVariable UUID transactionId,
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(transactionService.getTransaction(transactionId, UUID.fromString(userId)));
    }
}
