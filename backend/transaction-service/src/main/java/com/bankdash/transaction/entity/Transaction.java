package com.bankdash.transaction.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions",
    indexes = {
        @Index(name = "idx_tx_user",    columnList = "userId"),
        @Index(name = "idx_tx_account", columnList = "fromAccountNumber"),
        @Index(name = "idx_tx_created", columnList = "createdAt")
    })
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String referenceNumber;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 20)
    private String fromAccountNumber;

    @Column(length = 20)
    private String toAccountNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(length = 3)
    @Builder.Default
    private String currency = "USD";

    @Column(precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal fee = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(length = 255)
    private String description;

    @Column(length = 50)
    private String category;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime processedAt;
}
