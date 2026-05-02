package com.bankdash.account.repository;

import com.bankdash.account.entity.Account;
import com.bankdash.account.entity.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    List<Account> findByUserId(UUID userId);

    List<Account> findByUserIdAndStatus(UUID userId, AccountStatus status);

    Optional<Account> findByAccountNumber(String accountNumber);

    @Query("SELECT SUM(a.balance) FROM Account a WHERE a.userId = :userId AND a.status = 'ACTIVE'")
    java.math.BigDecimal sumBalanceByUserId(UUID userId);

    boolean existsByAccountNumber(String accountNumber);
}
