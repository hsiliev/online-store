package com.onlinestore.store.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlinestore.store.dto.ProductQuantityRequest;
import com.onlinestore.store.persistence.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class OutboxPatternTest {

    @Autowired
    private StoreService storeService;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OutboxProcessor outboxProcessor;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @BeforeEach
    void setUp() {
        outboxEventRepository.deleteAll();
        productRepository.deleteAll();
        productRepository.save(new Product(1L, "Test Product", 10));
    }

    @Test
    @Transactional
    void testTakeProductsSavesToOutbox() {
        storeService.takeProducts(List.of(new ProductQuantityRequest(1L, 2)));

        List<OutboxEvent> events = outboxEventRepository.findAll();
        assertEquals(1, events.size());
        assertFalse(events.get(0).isProcessed());
        assertTrue(events.get(0).getPayload().contains("\"productId\":1"));

        // Verify NO rabbit template call yet (before processing)
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), (Object) any());
    }

    @Test
    void testOutboxProcessorSendsMessage() {
        storeService.takeProducts(List.of(new ProductQuantityRequest(1L, 2)));

        outboxProcessor.processOutboxEvents();

        List<OutboxEvent> events = outboxEventRepository.findAll();
        assertEquals(1, events.size());
        assertTrue(events.get(0).isProcessed());

        // Verify rabbit template call
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), (Object) any());
    }
}
