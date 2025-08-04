package com.irrigation.erp.backend.repository;

import com.irrigation.erp.backend.model.InventoryIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryIssueRepository extends JpaRepository<InventoryIssue, Long> {
    @Query("SELECT COALESCE(SUM(i.issuedQuantity), 0.0) FROM InventoryIssue i WHERE i.requestLineItem.id = :lineItemId")
    Double sumIssuedQuantityByRequestLineItemId(@Param("lineItemId") Long lineItemId);
}
