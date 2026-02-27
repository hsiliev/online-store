package com.onlinestore.store.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "demand")
public class Demand {
    @Id
    private Long productId;
    private Integer quantityInDemand;

    public Demand() {}

    public Demand(Long productId, Integer quantityInDemand) {
        this.productId = productId;
        this.quantityInDemand = quantityInDemand;
    }

    // Getters and Setters
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Integer getQuantityInDemand() { return quantityInDemand; }
    public void setQuantityInDemand(Integer quantityInDemand) { this.quantityInDemand = quantityInDemand; }
}
