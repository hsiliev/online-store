package com.onlinestore.status;

import com.onlinestore.common.OrderCompletedEvent;
import com.onlinestore.common.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderCompletedListener {

    private final SimpMessagingTemplate messagingTemplate;

    public OrderCompletedListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @RabbitListener(queues = RabbitMQConfig.ORDER_COMPLETED_QUEUE)
    public void onOrderCompleted(OrderCompletedEvent event) {
        messagingTemplate.convertAndSend("/topic/orders", event);
    }
}
