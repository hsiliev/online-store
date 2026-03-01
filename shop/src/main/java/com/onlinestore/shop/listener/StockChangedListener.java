package com.onlinestore.shop.listener;

import com.onlinestore.common.RabbitMQConfig;
import com.onlinestore.common.StockChangedEvent;
import com.onlinestore.shop.service.ShopService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class StockChangedListener {
    private final Logger log = LoggerFactory.getLogger(StockChangedListener.class);

    private final ShopService shopService;

    public StockChangedListener(ShopService shopService) {
        this.shopService = shopService;
    }

    @RabbitListener(queues = RabbitMQConfig.STOCK_CHANGED_QUEUE, ackMode = "NONE")
    public void onStockChanged(StockChangedEvent event) {
        log.info("Received stock changed event for product ID: {}", event.productId());
        shopService.tryCompletePendingOrders();
    }
}
