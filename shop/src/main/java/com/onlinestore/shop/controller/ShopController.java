package com.onlinestore.shop.controller;

import com.onlinestore.shop.dto.OrderItemRequest;
import com.onlinestore.shop.service.ShopService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
public class ShopController {

    private final ShopService shopService;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    @PostMapping
    public ResponseEntity<Void> createOrder(@RequestBody List<OrderItemRequest> items) {
        shopService.createOrder(items);
        return ResponseEntity.accepted().build();
    }
}
