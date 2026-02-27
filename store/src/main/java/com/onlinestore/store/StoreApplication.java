package com.onlinestore.store;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.onlinestore.store", "com.onlinestore.common"})
@EnableJpaRepositories(basePackages = {"com.onlinestore.store.persistence"})
@EntityScan(basePackages = {"com.onlinestore.store.persistence"})
public class StoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(StoreApplication.class, args);
    }
}
