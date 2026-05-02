package com.bankdash.notification.kafka;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TransactionEvent {
    private UUID transactionId;
    private String referenceNumber;
    private UUID userId;
    private String type;
    private String status;
    private BigDecimal amount;
    private String currency;
    private String fromAccountNumber;
    private String toAccountNumber;
    private String description;
    private LocalDateTime occurredAt;
}
