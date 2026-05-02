package com.bankdash.notification.service;

import com.bankdash.notification.kafka.TransactionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {

    public void handleTransactionEvent(TransactionEvent event) {
        String message = buildMessage(event);
        // In production: send via email (SES), SMS (SNS), or push notification
        log.info("📧 NOTIFICATION to user {}: {}", event.getUserId(), message);
    }

    private String buildMessage(TransactionEvent event) {
        return switch (event.getType()) {
            case "TRANSFER"    -> String.format("Transfer of %s %s from %s to %s — %s",
                event.getAmount(), event.getCurrency(),
                event.getFromAccountNumber(), event.getToAccountNumber(), event.getStatus());
            case "DEPOSIT"     -> String.format("Deposit of %s %s to %s — %s",
                event.getAmount(), event.getCurrency(),
                event.getToAccountNumber(), event.getStatus());
            case "WITHDRAWAL"  -> String.format("Withdrawal of %s %s from %s — %s",
                event.getAmount(), event.getCurrency(),
                event.getFromAccountNumber(), event.getStatus());
            default            -> String.format("Transaction %s: %s %s — %s",
                event.getReferenceNumber(), event.getAmount(), event.getCurrency(), event.getStatus());
        };
    }
}
