package com.bankdash.account.service;

import com.bankdash.account.dto.AccountDtos.*;
import java.util.List;
import java.util.UUID;

public interface AccountService {
    AccountResponse createAccount(UUID userId, CreateAccountRequest request);
    List<AccountResponse> getAccountsByUser(UUID userId);
    AccountResponse getAccount(UUID accountId, UUID userId);
    AccountResponse getAccountByNumber(String accountNumber);
    AccountSummary getAccountSummary(UUID userId);
    AccountResponse updateBalance(UpdateBalanceRequest request);
    void closeAccount(UUID accountId, UUID userId);
}
