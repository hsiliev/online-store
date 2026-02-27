package com.onlinestore.store.controller;

import com.onlinestore.store.dto.ProductQuantityRequest;
import com.onlinestore.store.dto.StockRequest;
import com.onlinestore.store.exception.InsufficientStockException;
import com.onlinestore.store.persistence.Demand;
import com.onlinestore.store.persistence.Product;
import com.onlinestore.store.service.StoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/")
public class StoreController {

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @GetMapping("/stock")
    public List<Product> getStock() {
        return storeService.getAllProducts();
    }

    @GetMapping("/demand")
    public List<Demand> getDemand() {
        return storeService.getAllDemand();
    }

    @PostMapping("/stock")
    public void postStock(@RequestBody StockRequest request) {
        storeService.stockProduct(request.productId(), request.productName(), request.quantity());
    }

    @PostMapping("/stock/take-all")
    public void takeStockAll(@RequestBody List<ProductQuantityRequest> requests) {
        storeService.takeProducts(requests);
        storeService.notifyStockChanged(requests.stream().map(ProductQuantityRequest::productId).toList());
    }
}
