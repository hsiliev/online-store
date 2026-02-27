package com.onlinestore.shop;

import com.onlinestore.common.RabbitMQConfig;
import com.onlinestore.common.StockChangedEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class StockChangedListener {

    private final ShopService shopService;

    public StockChangedListener(ShopService shopService) {
        this.shopService = shopService;
    }

    @RabbitListener(queues = RabbitMQConfig.STOCK_CHANGED_QUEUE)
    public void onStockChanged(StockChangedEvent event) {
        shopService.tryCompletePendingOrders();
    }
}
