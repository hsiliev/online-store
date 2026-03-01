package com.onlinestore.store.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlinestore.store.persistence.OutboxEvent;
import com.onlinestore.store.persistence.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * The Outbox pattern ensures that database updates and event publishing are atomic, so if the database transaction rolls back,
 * no event is saved or sent, and if the transaction commits, the event is eventually sent by the OutboxProcessor.
 */
@Service
public class OutboxProcessor {
    private static final Logger log = LoggerFactory.getLogger(OutboxProcessor.class);

    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public OutboxProcessor(OutboxEventRepository outboxEventRepository,
                           RabbitTemplate rabbitTemplate,
                           ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processOutboxEvents() {
        List<OutboxEvent> events = outboxEventRepository.findByProcessedFalse();
        for (OutboxEvent event : events) {
            try {
                log.info("Processing outbox event {}: {}/{}", event.getId(), event.getExchange(), event.getRoutingKey());
                Object payload = objectMapper.readValue(event.getPayload(), Object.class);
                rabbitTemplate.convertAndSend(event.getExchange(), event.getRoutingKey(), payload);
                event.setProcessed(true);
                outboxEventRepository.save(event);
            } catch (JsonProcessingException e) {
                log.error("Failed to parse outbox event payload", e);
                // Mark as processed anyway to avoid infinite loop, or move to dead letter table
                event.setProcessed(true);
                outboxEventRepository.save(event);
            } catch (Exception e) {
                log.error("Failed to send outbox event", e);
                // Will retry on next schedule if not marked as processed
            }
        }
    }
}
