package com.irrigation.erp.backend.service;

import com.irrigation.erp.backend.dto.CategoryWithCountDTO;
import com.irrigation.erp.backend.enums.StockStatus;
import com.irrigation.erp.backend.model.InventoryItem;
import com.irrigation.erp.backend.model.ItemCategory;
import com.irrigation.erp.backend.repository.InventoryItemRepository;
import com.irrigation.erp.backend.repository.ItemCategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final ItemCategoryRepository itemCategoryRepository;
    private final InventoryItemRepository inventoryItemRepository;

    public CategoryService(ItemCategoryRepository itemCategoryRepository,
                           InventoryItemRepository inventoryItemRepository) {
        this.itemCategoryRepository = itemCategoryRepository;
        this.inventoryItemRepository = inventoryItemRepository;
    }

    public List<ItemCategory> getAllCategories() {
        return itemCategoryRepository.findAll();
    }

    public ItemCategory getCategoryById(Long categoryId) {
        return itemCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category with ID " + categoryId + " not found"));
    }

    public ItemCategory getCategoryByName(String categoryName) {
        return itemCategoryRepository.findByName(categoryName)
                .orElseThrow(() -> new IllegalArgumentException("Category with name '" + categoryName + "' not found"));
    }

    public List<CategoryWithCountDTO> getAllCategoriesWithCounts() {
        List<ItemCategory> categories = itemCategoryRepository.findAll();

        return categories.stream().map(category -> {
            List<InventoryItem> items = inventoryItemRepository.findByItemCategory(category);
            Long itemCount = (long) items.size();

            // Count low stock items
            Long lowStockCount = items.stream()
                    .mapToLong(item -> {
                        StockStatus status = item.getStockStatus();
                        return (status == StockStatus.LOW || status == StockStatus.OUT_OF_STOCK) ? 1 : 0;
                    })
                    .sum();

            return new CategoryWithCountDTO(
                    category.getId(),
                    category.getName(),
                    category.getDescription(),
                    itemCount,
                    lowStockCount
            );
        }).collect(Collectors.toList());
    }
}