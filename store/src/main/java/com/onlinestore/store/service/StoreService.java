package com.onlinestore.store.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlinestore.common.RabbitMQConfig;
import com.onlinestore.common.StockChangedEvent;
import com.onlinestore.store.dto.ProductQuantityRequest;
import com.onlinestore.store.exception.InsufficientStockException;
import com.onlinestore.store.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StoreService {
    private static final Logger log = LoggerFactory.getLogger(StoreService.class);

    private final ProductRepository productRepository;
    private final DemandRepository demandRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public StoreService(ProductRepository productRepository,
                        DemandRepository demandRepository,
                        OutboxEventRepository outboxEventRepository,
                        ObjectMapper objectMapper) {
        this.productRepository = productRepository;
        this.demandRepository = demandRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Demand> getAllDemand() {
        List<Demand> demands = demandRepository.findAll();
        List<Product> products = productRepository.findAll();
        Map<Long, Integer> quantityByProductId = products.stream().collect(Collectors.toMap(Product::getId, Product::getQuantity));

        List<Demand> result = new ArrayList<>();
        for (Demand demand : demands) {
            long productId = demand.getProductId();
            int demanded = demand.getQuantityInDemand();
            int available = quantityByProductId.getOrDefault(productId, 0);

            int deficit = demanded - available;
            if (deficit > 0) {
                result.add(new Demand(productId, deficit));
            }
        }
        return result;
    }

    @Transactional
    public void stockProduct(Long productId, String productName, Integer quantity) {
        if (!productRepository.existsById(productId)) {
            productRepository.save(new Product(productId, productName, 0));
            demandRepository.save(new Demand(productId, 0));
        }
        productRepository.incrementStock(productId, quantity);
        notifyStockChanged(List.of(productId));
    }

    @Transactional
    public void addDemand(Long productId, Integer quantity) {
        if (demandRepository.existsById(productId)) {
            demandRepository.incrementDemand(productId, quantity);
        } else {
            demandRepository.save(new Demand(productId, quantity));
        }
    }

    @Transactional
    public void takeProducts(List<ProductQuantityRequest> requests) {
        List<Long> productIds = new ArrayList<>();
        for (ProductQuantityRequest request : requests) {
            boolean taken = productRepository.decrementStock(request.productId(), request.quantity()) > 0;
            if (!taken) {
                // Throwing exception to trigger rollback of all decrements in this transaction
                throw new InsufficientStockException("Insufficient stock for product " + request.productId());
            }
            demandRepository.decrementDemand(request.productId(), request.quantity());
            productIds.add(request.productId());
        }
        notifyStockChanged(productIds);
    }

    public void notifyStockChanged(List<Long> productIds) {
        for (Long id : productIds) {
            StockChangedEvent event = new StockChangedEvent(id);
            try {
                String payload = objectMapper.writeValueAsString(event);
                outboxEventRepository.save(new OutboxEvent(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.STOCK_CHANGED_ROUTING_KEY, payload));
                log.info("Saved StockChangedEvent for product ID {} to outbox", id);
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize StockChangedEvent for product ID {}", id, e);
            }
        }
    }
}
