package com.bankdash.transaction.repository;

import com.bankdash.transaction.entity.Transaction;
import com.bankdash.transaction.entity.TransactionStatus;
import com.bankdash.transaction.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Page<Transaction> findByUserId(UUID userId, Pageable pageable);

    Page<Transaction> findByUserIdAndType(UUID userId, TransactionType type, Pageable pageable);

    Page<Transaction> findByUserIdAndStatus(UUID userId, TransactionStatus status, Pageable pageable);

    List<Transaction> findByFromAccountNumber(String accountNumber);

    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId AND t.createdAt BETWEEN :from AND :to")
    Page<Transaction> findByUserIdAndDateRange(UUID userId, LocalDateTime from, LocalDateTime to, Pageable pageable);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.userId = :userId AND t.type = :type AND t.status = 'COMPLETED'")
    BigDecimal sumByUserIdAndType(UUID userId, TransactionType type);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.userId = :userId AND t.createdAt >= :since")
    long countRecentByUserId(UUID userId, LocalDateTime since);
}
