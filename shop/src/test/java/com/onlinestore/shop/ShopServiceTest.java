package com.onlinestore.shop;

import com.onlinestore.common.DemandCreatedEvent;
import com.onlinestore.common.OrderCompletedEvent;
import com.onlinestore.common.RabbitMQConfig;
import com.onlinestore.shop.dto.OrderItemRequest;
import com.onlinestore.shop.persistence.Order;
import com.onlinestore.shop.persistence.OrderRepository;
import com.onlinestore.shop.service.ShopService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class ShopServiceTest {

    @Autowired
    private ShopService shopService;

    @Autowired
    private OrderRepository orderRepository;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @MockitoBean
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
    }

    @Test
    void testCreateOrderAndProcessImmediately() {
        Long productId = 1L;
        Integer quantity = 2;

        // Mock store stock success for take
        when(restTemplate.postForObject(endsWith("/stock/take"), any(), eq(Void.class)))
                .thenReturn(null);

        shopService.createOrder(List.of(new OrderItemRequest(productId, quantity)));

        List<Order> orders = orderRepository.findAll();
        assertEquals(1, orders.size());
        assertTrue(orders.get(0).isCompleted());

        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.EXCHANGE_NAME), eq(RabbitMQConfig.ORDER_COMPLETED_ROUTING_KEY), any(OrderCompletedEvent.class));
        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.EXCHANGE_NAME), eq(RabbitMQConfig.DEMAND_CREATED_ROUTING_KEY), any(DemandCreatedEvent.class));
        verify(restTemplate, times(1)).postForObject(endsWith("/stock/take"), any(), eq(Void.class));
    }

    @Test
    void testCreateOrderButNoStock() {
        Long productId = 1L;
        Integer quantity = 2;

        // Mock stock take failure (404)
        when(restTemplate.postForObject(endsWith("/stock/take"), any(), eq(Void.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        shopService.createOrder(List.of(new OrderItemRequest(productId, quantity)));

        List<Order> orders = orderRepository.findAll();
        assertEquals(1, orders.size());
        assertFalse(orders.get(0).isCompleted());

        verify(rabbitTemplate, never()).convertAndSend(eq(RabbitMQConfig.EXCHANGE_NAME), eq(RabbitMQConfig.ORDER_COMPLETED_ROUTING_KEY), any(OrderCompletedEvent.class));
        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.EXCHANGE_NAME), eq(RabbitMQConfig.DEMAND_CREATED_ROUTING_KEY), any(DemandCreatedEvent.class));
        verify(restTemplate, times(1)).postForObject(endsWith("/stock/take"), any(), eq(Void.class));
    }

    @Test
    void testCompletePendingOrderOnStockChanged() {
        Long productId = 1L;
        Integer quantity = 2;

        // Create a pending order - first attempt fails
        when(restTemplate.postForObject(endsWith("/stock/take"), any(), eq(Void.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        shopService.createOrder(List.of(new OrderItemRequest(productId, quantity)));

        List<Order> orders = orderRepository.findAll();
        assertFalse(orders.get(0).isCompleted());

        // Now mock stock added - second attempt succeeds
        reset(restTemplate);
        when(restTemplate.postForObject(endsWith("/stock/take"), any(), eq(Void.class)))
                .thenReturn(null);

        shopService.tryCompletePendingOrders();

        orders = orderRepository.findAll();
        assertTrue(orders.get(0).isCompleted());
        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.EXCHANGE_NAME), eq(RabbitMQConfig.ORDER_COMPLETED_ROUTING_KEY), any(OrderCompletedEvent.class));
    }

    @Test
    void testRaceConditionOnStockTake() {
        Long productId = 1L;
        Integer quantity = 2;

        // Stock take fails with 404
        when(restTemplate.postForObject(endsWith("/stock/take"), any(), eq(Void.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        shopService.createOrder(List.of(new OrderItemRequest(productId, quantity)));

        List<Order> orders = orderRepository.findAll();
        assertFalse(orders.get(0).isCompleted());
    }
}
