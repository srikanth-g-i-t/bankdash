package com.bankdash.transaction.kafka;

import com.bankdash.transaction.entity.TransactionStatus;
import com.bankdash.transaction.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEvent {
    private UUID transactionId;
    private String referenceNumber;
    private UUID userId;
    private TransactionType type;
    private TransactionStatus status;
    private BigDecimal amount;
    private String currency;
    private String fromAccountNumber;
    private String toAccountNumber;
    private String description;
    private LocalDateTime occurredAt;
}
