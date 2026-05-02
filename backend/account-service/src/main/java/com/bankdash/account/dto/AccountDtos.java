package com.bankdash.account.dto;

import com.bankdash.account.entity.AccountStatus;
import com.bankdash.account.entity.AccountType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class AccountDtos {

    @Data
    public static class CreateAccountRequest {
        @NotNull private AccountType accountType;
        private String nickname;
        private String currency = "USD";
    }

    @Data
    public static class AccountResponse {
        private UUID id;
        private String accountNumber;
        private AccountType accountType;
        private BigDecimal balance;
        private BigDecimal availableBalance;
        private String currency;
        private AccountStatus status;
        private String nickname;
        private LocalDateTime createdAt;
    }

    @Data
    public static class AccountSummary {
        private UUID userId;
        private int totalAccounts;
        private BigDecimal totalBalance;
        private java.util.List<AccountResponse> accounts;
    }

    @Data
    public static class UpdateBalanceRequest {
        @NotNull private BigDecimal amount;      // positive = credit, negative = debit
        @NotNull private String accountNumber;
    }
}
