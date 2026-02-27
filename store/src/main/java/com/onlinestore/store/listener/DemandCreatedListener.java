package com.onlinestore.store.listener;

import com.onlinestore.common.DemandCreatedEvent;
import com.onlinestore.common.RabbitMQConfig;
import com.onlinestore.store.service.StoreService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class DemandCreatedListener {

    private final StoreService storeService;

    public DemandCreatedListener(StoreService storeService) {
        this.storeService = storeService;
    }

    @RabbitListener(queues = RabbitMQConfig.DEMAND_CREATED_QUEUE)
    public void onDemandCreated(DemandCreatedEvent event) {
        storeService.addDemand(event.productId(), event.quantity());
    }
}
