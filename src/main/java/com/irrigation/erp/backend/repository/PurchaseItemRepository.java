package com.irrigation.erp.backend.repository;

import com.irrigation.erp.backend.model.PurchaseItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface PurchaseItemRepository extends JpaRepository<PurchaseItem, Long> {
    @Transactional
    @Modifying
    @Query("DELETE FROM PurchaseItem pi WHERE pi.purchase.id = :purchaseId")
    void deleteByPurchaseId(Long purchaseId);
}