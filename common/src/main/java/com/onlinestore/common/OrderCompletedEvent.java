package com.onlinestore.common;

public record OrderCompletedEvent(Long orderId, String message) {}
