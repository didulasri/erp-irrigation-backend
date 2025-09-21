package com.irrigation.erp.backend.service;

import com.irrigation.erp.backend.dto.InventoryItemCreateRequestDTO;
import com.irrigation.erp.backend.dto.InventoryItemResponseDTO;
import com.irrigation.erp.backend.enums.StockStatus;
import com.irrigation.erp.backend.model.InventoryItem;
import com.irrigation.erp.backend.model.ItemCategory;
import com.irrigation.erp.backend.model.ItemType;
import com.irrigation.erp.backend.model.User;
import com.irrigation.erp.backend.repository.InventoryItemRepository;
import com.irrigation.erp.backend.repository.ItemCategoryRepository;
import com.irrigation.erp.backend.repository.ItemTypeRepository;
import com.irrigation.erp.backend.repository.UserRepository;
import jakarta.transaction.Transactional;

import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InventoryService {
    private final InventoryItemRepository inventoryItemRepository;
    private final ItemCategoryRepository itemCategoryRepository;
    private final ItemTypeRepository itemTypeRepository;
    private final UserRepository userRepository;
    public static final String NOT_FOUND = "' not found.";
    public static final String ITEM_CATEGORY = "Item Category '";

    public InventoryService(InventoryItemRepository inventoryItemRepository,
                            ItemCategoryRepository itemCategoryRepository,
                            ItemTypeRepository itemTypeRepository,
                            UserRepository userRepository) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.itemCategoryRepository = itemCategoryRepository;
        this.itemTypeRepository = itemTypeRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public InventoryItem createInventoryItem(InventoryItemCreateRequestDTO requestDTO) {

        //check for unique item code
        if(inventoryItemRepository.findByItemCode(requestDTO.getItemCode()).isPresent()){
            throw new IllegalArgumentException("Item with code '" + requestDTO.getItemCode() + "' already exists.");
        }

        //fetch entities from repositories
        ItemCategory itemCategory = itemCategoryRepository.findByName(requestDTO.getItemCategoryName())
                .orElseThrow(()-> new IllegalArgumentException(ITEM_CATEGORY + requestDTO.getItemCategoryName() + NOT_FOUND));

        ItemType itemType = itemTypeRepository.findByName(requestDTO.getItemTypeName())
                .orElseThrow(()-> new IllegalArgumentException("Item Type '" + requestDTO.getItemTypeName() + NOT_FOUND));

        User creatingUser = userRepository.findById(requestDTO.getCreatingUserId())
                .orElseThrow(()-> new IllegalArgumentException("User with id '" + requestDTO.getCreatingUserId() + NOT_FOUND));
        // Changed from IllegalIdentifierException for consistency

        //Create and Populate New InventoryItem Object
        InventoryItem newItem = new InventoryItem();
        newItem.setItemCode(requestDTO.getItemCode());
        newItem.setItemName(requestDTO.getItemName());
        newItem.setItemDescription(requestDTO.getItemDescription());
        newItem.setUnitOfMeasurement(requestDTO.getUnitOfMeasurement());
        newItem.setCurrentStockQuantity(requestDTO.getCurrentStockQuantity());
        newItem.setMinimumStockLevel(requestDTO.getMinimumStockLevel());
        newItem.setLocationInStore(requestDTO.getLocationInStore());
        newItem.setUnitPrice(requestDTO.getUnitPrice());
        newItem.setItemCategory(itemCategory);
        newItem.setItemType(itemType);

        newItem.setCreatingUser(creatingUser);
        newItem.setLastUpdatedByUser(creatingUser);
        newItem.setLastUpdatedAt(LocalDateTime.now());
        newItem.setIsActive(true);

        //save to database
        return inventoryItemRepository.save(newItem);
    }

    // service for updateInventoryItem
    @Transactional
    public InventoryItem updateInventoryItem(String itemCode, String itemName, String itemDescription, String unitOfMeasurement,
                                             BigDecimal minimumStockLevel,
                                             String locationInStore,
                                             BigDecimal unitPrice,
                                             String itemCategoryName,
                                             String itemTypeName,
                                             Boolean isActiveStatus,
                                             Long updatingUserId){

        InventoryItem item = inventoryItemRepository.findByItemCode(itemCode)
                .orElseThrow(()-> new IllegalArgumentException("Item with code '" + itemCode + NOT_FOUND));

        if (itemName != null) item.setItemName(itemName);
        if (itemDescription != null) item.setItemDescription(itemDescription);
        if (unitOfMeasurement != null) item.setUnitOfMeasurement(unitOfMeasurement);
        if (minimumStockLevel != null) item.setMinimumStockLevel(minimumStockLevel);
        if (locationInStore != null) item.setLocationInStore(locationInStore);
        if (unitPrice != null) item.setUnitPrice(unitPrice);

        if (itemCategoryName != null && item.getItemCategory() != null && !item.getItemCategory().getName().equals(itemCategoryName)) {
            ItemCategory newCategory = itemCategoryRepository.findByName(itemCategoryName)
                    .orElseThrow(() -> new IllegalArgumentException(ITEM_CATEGORY + itemCategoryName + NOT_FOUND));
            item.setItemCategory(newCategory);
        }
        if (itemTypeName != null && item.getItemType() != null && !item.getItemType().getName().equals(itemTypeName)) {
            ItemType newType = itemTypeRepository.findByName(itemTypeName)
                    .orElseThrow(() -> new IllegalArgumentException("Item Type '" + itemTypeName + NOT_FOUND));
            item.setItemType(newType);
        }

        if(isActiveStatus != null) item.setIsActive(isActiveStatus);

        //update updatingUser fields
        User updatingUser = userRepository.findById(updatingUserId)
                .orElseThrow(()-> new IllegalArgumentException("User with id '" + updatingUserId + NOT_FOUND));
        // Changed from IllegalIdentifierException for consistency
        item.setLastUpdatedByUser(updatingUser);
        item.setLastUpdatedAt(LocalDateTime.now());

        return inventoryItemRepository.save(item);
    }

    public Optional<InventoryItem> getInventoryItemById(Long id) {
        return inventoryItemRepository.findById(id);
    }

    public Optional<InventoryItem> getInventoryItemByItemCode(String itemCode) {
        return inventoryItemRepository.findByItemCode(itemCode);
    }

    public List<InventoryItem> getAllInventoryItems() {
        return inventoryItemRepository.findAll();
    }

    // NEW METHOD: Get inventory items by category name
    public List<InventoryItemResponseDTO> getInventoryItemsByCategory(String categoryName) {
        return inventoryItemRepository.findByItemCategory(
                        itemCategoryRepository.findByName(categoryName)
                                .orElseThrow(() -> new IllegalArgumentException(ITEM_CATEGORY + categoryName + NOT_FOUND))
                )
                .stream()
                .map(this::mapToDTO) // Map each entity to the DTO
                .collect(Collectors.toList());
    }

    // Helper method to map entity to DTO
    private InventoryItemResponseDTO mapToDTO(InventoryItem item) {
        InventoryItemResponseDTO dto = new InventoryItemResponseDTO();
        dto.setId(item.getId());
        dto.setItemCode(item.getItemCode());
        dto.setItemName(item.getItemName());
        dto.setItemDescription(item.getItemDescription());
        dto.setUnitOfMeasurement(item.getUnitOfMeasurement());
        dto.setCurrentStockQuantity(item.getCurrentStockQuantity());
        dto.setMinimumStockLevel(item.getMinimumStockLevel());
        dto.setLocationInStore(item.getLocationInStore());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setIsActive(item.getIsActive());
        dto.setStockStatus(item.getStockStatus());

        // Set the new field
        dto.setPendingPurchaseRequest(item.getPendingPurchaseRequest());

        // Map related entity fields
        if (item.getItemCategory() != null) {
            dto.setItemCategoryId(item.getItemCategory().getId());
            dto.setItemCategoryName(item.getItemCategory().getName());
        }
        if (item.getItemType() != null) {
            dto.setItemTypeId(item.getItemType().getId());
            dto.setItemTypeName(item.getItemType().getName());
        }
        if (item.getCreatingUser() != null) {
            dto.setCreatedByUserId(item.getCreatingUser().getId());
            dto.setCreatedByUsername(item.getCreatingUser().getUsername());
        }
        if (item.getLastUpdatedByUser() != null) {
            dto.setLastUpdatedByUserId(item.getLastUpdatedByUser().getId());
            dto.setLastUpdatedByUsername(item.getLastUpdatedByUser().getUsername());
        }
        dto.setLastUpdatedAt(item.getLastUpdatedAt());

        return dto;
    }

    // NEW METHOD: Get inventory items by category ID
    public List<InventoryItem> getInventoryItemsByCategoryId(Long categoryId) {
        ItemCategory category = itemCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Item Category with ID '" + categoryId + NOT_FOUND));
        return inventoryItemRepository.findByItemCategory(category);
    }

    // NEW METHOD: Get all categories with their inventory items count
    public List<ItemCategory> getAllCategoriesWithItemCount() {
        return itemCategoryRepository.findAll();
    }

    public List<InventoryItem> getLowStockItems() {
        return inventoryItemRepository.findAll().stream()
                .filter(item -> item.getStockStatus() == StockStatus.LOW || item.getStockStatus() == StockStatus.OUT_OF_STOCK)
                .collect(Collectors.toList());
    }

    // NEW METHOD: Get low stock items by category
    public List<InventoryItemResponseDTO> getLowStockItemsByCategory(String categoryName) {
        // Now calling the method that returns DTOs and filtering that result
        return getInventoryItemsByCategory(categoryName).stream()
                .filter(item -> item.getStockStatus() == StockStatus.LOW || item.getStockStatus() == StockStatus.OUT_OF_STOCK)
                .collect(Collectors.toList());
    }
    @Transactional
    public InventoryItem adjustStock(Long itemId, @NotNull(message = "Quantity change cannot be null") BigDecimal quantityChange, Long adjustingUserId, String reason) {
        InventoryItem item = inventoryItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory item with ID " + itemId + NOT_FOUND));

        BigDecimal newQuantity = item.getCurrentStockQuantity().add(quantityChange);


        if (newQuantity.compareTo(BigDecimal.ZERO) < 0 && item.getItemType() != null && item.getItemType().getName().equals("Material")) {
            throw new IllegalArgumentException("Stock quantity cannot go below zero for " + item.getItemName() + " (Type: Material).");
        }

        item.setCurrentStockQuantity(newQuantity);

        User adjustingUser = userRepository.findById(adjustingUserId)
                .orElseThrow(() -> new IllegalArgumentException("Adjusting User with ID " + adjustingUserId + NOT_FOUND));
        item.setLastUpdatedByUser(adjustingUser);
        item.setLastUpdatedAt(LocalDateTime.now());

        return inventoryItemRepository.save(item);
    }
}