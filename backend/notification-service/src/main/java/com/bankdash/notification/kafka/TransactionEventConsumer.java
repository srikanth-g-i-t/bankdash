package com.bankdash.notification.kafka;

import com.bankdash.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
        topics = "transaction-events",
        groupId = "notification-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(TransactionEvent event) {
        log.info("Received transaction event: {} type={} status={}",
            event.getReferenceNumber(), event.getType(), event.getStatus());
        notificationService.handleTransactionEvent(event);
    }
}
