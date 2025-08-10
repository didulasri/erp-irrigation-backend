package com.irrigation.erp.backend.repository;

import com.irrigation.erp.backend.enums.RequestStatus;
import com.irrigation.erp.backend.model.InventoryRequest;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRequestRepository extends JpaRepository<InventoryRequest, Long> {
    @EntityGraph(attributePaths = {"lineItems", "lineItems.inventoryItem", "lineItems.inventoryItem.createdBy", "lineItems.inventoryItem.createdBy.role", "lineItems.inventoryItem.itemCategory", "lineItems.inventoryItem.itemType"})
    @Query("SELECT r FROM InventoryRequest r JOIN FETCH r.lineItems WHERE r.status = :status")
    List<InventoryRequest> findByStatusWithLineItems(@Param("status") RequestStatus status);



    @EntityGraph(attributePaths = "lineItems")
    List<InventoryRequest> findByStatus(RequestStatus requestStatus);
}
