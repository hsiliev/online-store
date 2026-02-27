package com.onlinestore.store.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface DemandRepository extends JpaRepository<Demand, Long> {

    @Transactional
    @Modifying
    @Query("UPDATE Demand d SET d.quantityInDemand = d.quantityInDemand + :quantity WHERE d.productId = :productId")
    int incrementDemand(Long productId, Integer quantity);

    @Transactional
    @Modifying
    @Query("UPDATE Demand d SET d.quantityInDemand = GREATEST(0, d.quantityInDemand - :quantity) WHERE d.productId = :productId")
    int decrementDemand(Long productId, Integer quantity);
}
