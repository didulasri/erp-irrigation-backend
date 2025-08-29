package com.irrigation.erp.backend.repository;

import com.irrigation.erp.backend.dto.MaterialDistributionTableDTO;
import com.irrigation.erp.backend.model.InventoryIssue;
import com.irrigation.erp.backend.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryIssueRepository extends JpaRepository<InventoryIssue, Long> {

    @Query("SELECT COALESCE(SUM(i.issuedQuantity), 0.0) FROM InventoryIssue i WHERE i.requestLineItem.id = :lineItemId")
    Double sumIssuedQuantityByRequestLineItemId(@Param("lineItemId") Long lineItemId);

    // Existing methods
    List<InventoryIssue> findByIssuedItemOrderByIssuedAtDesc(InventoryItem issuedItem);
    List<InventoryIssue> findAllByOrderByIssuedAtDesc();
    List<InventoryIssue> findByInventoryRequestIdOrderByIssuedAtDesc(Long requestId);
    List<InventoryIssue> findByIssuedByUserIdOrderByIssuedAtDesc(Long userId);
    List<InventoryIssue> findByIssuedToUserIdOrderByIssuedAtDesc(Long userId);

    // NEW METHOD: Get issues for a specific user with Non-Material items only
    @Query("SELECT ii FROM InventoryIssue ii " +
            "JOIN ii.issuedItem item " +
            "JOIN item.itemType type " +
            "WHERE ii.issuedToUser.id = :userId " +
            "AND type.name = 'Non Materials' " +
            "ORDER BY ii.issuedAt DESC")
    List<InventoryIssue> findNonMaterialIssuesByIssuedToUserId(@Param("userId") Long userId);

    // NEW METHOD: Get all Non-Material item names that have been issued
    @Query("SELECT DISTINCT item.itemName FROM InventoryIssue ii " +
            "JOIN ii.issuedItem item " +
            "JOIN item.itemType type " +
            "WHERE type.name = 'Non Materials' " +
            "ORDER BY item.itemName")
    List<String> findDistinctNonMaterialItemNames();


    @Query("""
    SELECT new com.irrigation.erp.backend.dto.MaterialDistributionTableDTO(
        COALESCE(SUM(CASE WHEN MONTH(i.issuedAt) = :previousMonth AND YEAR(i.issuedAt) = :previousMonthYear THEN i.issuedQuantity END), 0),
        COALESCE(SUM(CASE WHEN MONTH(i.issuedAt) = :currentMonth AND YEAR(i.issuedAt) = :currentYear THEN i.issuedQuantity END), 0),
        COALESCE(SUM(i.issuedQuantity), 0),
        i.issuedItem.itemName,
        i.issuedItem.id
    )
    FROM InventoryIssue i
    JOIN i.issuedItem item
    JOIN item.itemType t
    WHERE LOWER(t.name) = 'material'
      AND (i.issuedByUser.id = :userId OR i.issuedToUser.id = :userId)
    GROUP BY i.issuedItem.itemName, i.issuedItem.id
""")
    List<MaterialDistributionTableDTO> getMaterialDistributionTable(
            @Param("userId") Long userId,
            @Param("currentMonth") int currentMonth,
            @Param("currentYear") int currentYear,
            @Param("previousMonth") int previousMonth,
            @Param("previousMonthYear") int previousMonthYear);

}