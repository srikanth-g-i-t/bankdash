package com.bankdash.transaction.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventProducer {

    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;

    @Value("${kafka.topics.transaction-events}")
    private String topic;

    public void publishTransactionEvent(TransactionEvent event) {
        CompletableFuture<SendResult<String, TransactionEvent>> future =
            kafkaTemplate.send(topic, event.getTransactionId().toString(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Transaction event published: {} offset={}",
                    event.getReferenceNumber(),
                    result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish transaction event: {}", ex.getMessage());
            }
        });
    }
}
