package com.onlinestore.store.dto;

public record StockRequest(Long productId, String productName, Integer quantity) {}
