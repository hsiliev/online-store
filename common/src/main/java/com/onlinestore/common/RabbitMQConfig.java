package com.onlinestore.common;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "online-store-exchange";
    public static final String STOCK_CHANGED_QUEUE = "stock-changed-queue";
    public static final String ORDER_COMPLETED_QUEUE = "order-completed-queue";
    public static final String DEMAND_CREATED_QUEUE = "demand-created-queue";
    public static final String STOCK_CHANGED_ROUTING_KEY = "stock.changed";
    public static final String ORDER_COMPLETED_ROUTING_KEY = "order.completed";
    public static final String DEMAND_CREATED_ROUTING_KEY = "demand.created";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue stockChangedQueue() {
        return new Queue(STOCK_CHANGED_QUEUE);
    }

    @Bean
    public Queue orderCompletedQueue() {
        return new Queue(ORDER_COMPLETED_QUEUE);
    }

    @Bean
    public Queue demandCreatedQueue() {
        return new Queue(DEMAND_CREATED_QUEUE);
    }

    @Bean
    public Binding stockChangedBinding(Queue stockChangedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(stockChangedQueue).to(exchange).with(STOCK_CHANGED_ROUTING_KEY);
    }

    @Bean
    public Binding orderCompletedBinding(Queue orderCompletedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(orderCompletedQueue).to(exchange).with(ORDER_COMPLETED_ROUTING_KEY);
    }

    @Bean
    public Binding demandCreatedBinding(Queue demandCreatedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(demandCreatedQueue).to(exchange).with(DEMAND_CREATED_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
