package com.onlinestore.shop;

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
    public ResponseEntity<Void> createOrder(@RequestBody List<ShopService.OrderItemRequest> items) {
        shopService.createOrder(items);
        return ResponseEntity.accepted().build();
    }
}
