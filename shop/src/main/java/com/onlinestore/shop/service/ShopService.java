package com.onlinestore.shop.service;

import com.onlinestore.common.OrderCompletedEvent;
import com.onlinestore.common.RabbitMQConfig;
import com.onlinestore.shop.dto.DemandRequest;
import com.onlinestore.shop.dto.OrderItemRequest;
import com.onlinestore.shop.dto.StockTakeRequest;
import com.onlinestore.shop.persistence.Order;
import com.onlinestore.shop.persistence.OrderItem;
import com.onlinestore.shop.persistence.OrderRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShopService {

    private final OrderRepository orderRepository;
    private final RabbitTemplate rabbitTemplate;
    private final RestTemplate restTemplate;

    @Value("${store.service.url:http://store:8081}")
    private String storeServiceUrl;

    public ShopService(OrderRepository orderRepository,
                       RabbitTemplate rabbitTemplate,
                       RestTemplate restTemplate) {
        this.orderRepository = orderRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.restTemplate = restTemplate;
    }

    @Transactional
    public void createOrder(List<OrderItemRequest> items) {
        Order order = new Order();
        order.setItems(items.stream()
                .map(it -> new OrderItem(it.productId(), it.quantity()))
                .collect(Collectors.toList()));
        order.setCompleted(false);
        order = orderRepository.save(order);

        // Notify store about demand
        for (OrderItemRequest item : items) {
            restTemplate.postForObject(storeServiceUrl + "/demand", new DemandRequest(item.productId(), item.quantity()), Void.class);
        }

        tryCompleteOrder(order);
    }

    @Transactional
    public void tryCompletePendingOrders() {
        List<Order> pendingOrders = orderRepository.findByCompletedFalse();
        for (Order order : pendingOrders) {
            tryCompleteOrder(order);
        }
    }

    @Transactional
    public void tryCompleteOrder(Order order) {
        if (order.isCompleted()) return;

        // Take products from store
        for (OrderItem item : order.getItems()) {
            try {
                restTemplate.postForObject(
                        storeServiceUrl + "/stock/take",
                        new StockTakeRequest(item.getProductId(), item.getQuantity()), Void.class
                );
            } catch (HttpClientErrorException e) {
                // This could happen due to race conditions.
                return;
            }
        }

        // Mark as completed
        order.setCompleted(true);
        orderRepository.save(order);

        // Send notification
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ORDER_COMPLETED_ROUTING_KEY,
            new OrderCompletedEvent(order.getId(), "Order " + order.getId() + " completed"));
    }
}
