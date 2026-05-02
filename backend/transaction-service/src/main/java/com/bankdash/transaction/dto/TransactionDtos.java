package com.bankdash.transaction.dto;

import com.bankdash.transaction.entity.TransactionStatus;
import com.bankdash.transaction.entity.TransactionType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class TransactionDtos {

    @Data
    public static class TransferRequest {
        @NotBlank  private String fromAccountNumber;
        @NotBlank  private String toAccountNumber;
        @NotNull @DecimalMin("0.01") private BigDecimal amount;
        private String description;
        private String currency = "USD";
    }

    @Data
    public static class DepositRequest {
        @NotBlank  private String accountNumber;
        @NotNull @DecimalMin("0.01") private BigDecimal amount;
        private String description;
    }

    @Data
    public static class WithdrawalRequest {
        @NotBlank  private String accountNumber;
        @NotNull @DecimalMin("0.01") private BigDecimal amount;
        private String description;
    }

    @Data
    public static class TransactionResponse {
        private UUID id;
        private String referenceNumber;
        private TransactionType type;
        private BigDecimal amount;
        private String currency;
        private BigDecimal fee;
        private TransactionStatus status;
        private String fromAccountNumber;
        private String toAccountNumber;
        private String description;
        private String category;
        private LocalDateTime createdAt;
        private LocalDateTime processedAt;
    }

    @Data
    public static class TransactionPageResponse {
        private java.util.List<TransactionResponse> content;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
    }
}
