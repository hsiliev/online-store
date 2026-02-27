package com.onlinestore.shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import com.onlinestore.common.RabbitMQConfig;

@SpringBootApplication
@Import(RabbitMQConfig.class)
public class ShopApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShopApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
