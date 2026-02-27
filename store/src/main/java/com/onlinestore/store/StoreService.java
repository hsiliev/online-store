package com.onlinestore.store;

import com.onlinestore.common.RabbitMQConfig;
import com.onlinestore.common.StockChangedEvent;
import com.onlinestore.store.persistence.Demand;
import com.onlinestore.store.persistence.DemandRepository;
import com.onlinestore.store.persistence.Product;
import com.onlinestore.store.persistence.ProductRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StoreService {

    private final ProductRepository productRepository;
    private final DemandRepository demandRepository;
    private final RabbitTemplate rabbitTemplate;

    public StoreService(ProductRepository productRepository,
                        DemandRepository demandRepository,
                        RabbitTemplate rabbitTemplate) {
        this.productRepository = productRepository;
        this.demandRepository = demandRepository;
        this.rabbitTemplate = rabbitTemplate;
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
            var qty = Math.max(0, demand.getQuantityInDemand() - quantityByProductId.getOrDefault(demand.getProductId(), 0));
            result.add(new Demand(demand.getProductId(), qty));
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
        demandRepository.decrementDemand(productId, quantity);
        notifyStockChanged(productId);
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
    public boolean takeProduct(Long productId, Integer quantity) {
        boolean takenFromStock = productRepository.decrementStock(productId, quantity) > 0;
        if (takenFromStock) {
            demandRepository.decrementDemand(productId, quantity);
            notifyStockChanged(productId);
        }
        return takenFromStock;
    }

    private void notifyStockChanged(Long productId) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.STOCK_CHANGED_ROUTING_KEY, new StockChangedEvent(productId));
    }
}
