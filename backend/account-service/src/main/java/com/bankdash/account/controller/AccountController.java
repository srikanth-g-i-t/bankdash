package com.bankdash.account.controller;

import com.bankdash.account.dto.AccountDtos.*;
import com.bankdash.account.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Bank account management")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @Operation(summary = "Create a new bank account")
    public ResponseEntity<AccountResponse> createAccount(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CreateAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(accountService.createAccount(UUID.fromString(userId), request));
    }

    @GetMapping
    @Operation(summary = "Get all accounts for authenticated user")
    public ResponseEntity<List<AccountResponse>> getMyAccounts(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(accountService.getAccountsByUser(UUID.fromString(userId)));
    }

    @GetMapping("/summary")
    @Operation(summary = "Get account summary with total balance")
    public ResponseEntity<AccountSummary> getSummary(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(accountService.getAccountSummary(UUID.fromString(userId)));
    }

    @GetMapping("/{accountId}")
    @Operation(summary = "Get account by ID")
    public ResponseEntity<AccountResponse> getAccount(
            @PathVariable UUID accountId,
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(accountService.getAccount(accountId, UUID.fromString(userId)));
    }

    @GetMapping("/number/{accountNumber}")
    @Operation(summary = "Get account by account number (internal use)")
    public ResponseEntity<AccountResponse> getByNumber(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getAccountByNumber(accountNumber));
    }

    @PatchMapping("/balance")
    @Operation(summary = "Update account balance (internal — called by transaction-service)")
    public ResponseEntity<AccountResponse> updateBalance(@Valid @RequestBody UpdateBalanceRequest request) {
        return ResponseEntity.ok(accountService.updateBalance(request));
    }

    @DeleteMapping("/{accountId}")
    @Operation(summary = "Close account")
    public ResponseEntity<Void> closeAccount(
            @PathVariable UUID accountId,
            @RequestHeader("X-User-Id") String userId) {
        accountService.closeAccount(accountId, UUID.fromString(userId));
        return ResponseEntity.noContent().build();
    }
}
