package com.irrigation.erp.backend.repository;


import com.irrigation.erp.backend.model.InventoryRequestLineItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryRequestLineItemRepository extends JpaRepository<InventoryRequestLineItem, Long> {

}
