package com.irrigation.erp.backend.repository;

import com.irrigation.erp.backend.model.InventoryPurchase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryPurchaseRepository extends JpaRepository<InventoryPurchase, Long> {
}