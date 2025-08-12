// Add these methods to your InventoryItemRepository interface

package com.irrigation.erp.backend.repository;

import com.irrigation.erp.backend.model.InventoryItem;
import com.irrigation.erp.backend.model.ItemCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    // Existing methods
    Optional<InventoryItem> findByItemCode(String itemCode);

    // NEW METHOD: Find inventory items by category
    List<InventoryItem> findByItemCategory(ItemCategory itemCategory);

    // NEW METHOD: Find active inventory items by category
    List<InventoryItem> findByItemCategoryAndIsActive(ItemCategory itemCategory, Boolean isActive);

    // NEW METHOD: Find inventory items by category name (using JPQL)
    @Query("SELECT i FROM InventoryItem i WHERE i.itemCategory.name = :categoryName")
    List<InventoryItem> findByItemCategoryName(@Param("categoryName") String categoryName);

    // NEW METHOD: Find active inventory items by category name
    @Query("SELECT i FROM InventoryItem i WHERE i.itemCategory.name = :categoryName AND i.isActive = :isActive")
    List<InventoryItem> findByItemCategoryNameAndIsActive(@Param("categoryName") String categoryName, @Param("isActive") Boolean isActive);

    // NEW METHOD: Count inventory items by category
    @Query("SELECT COUNT(i) FROM InventoryItem i WHERE i.itemCategory = :category")
    Long countByItemCategory(@Param("category") ItemCategory category);

    // NEW METHOD: Get inventory items with low stock by category
    @Query("SELECT i FROM InventoryItem i WHERE i.itemCategory = :category AND i.currentStockQuantity <= i.minimumStockLevel")
    List<InventoryItem> findLowStockItemsByCategory(@Param("category") ItemCategory category);
}