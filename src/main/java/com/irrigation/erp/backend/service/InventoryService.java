package com.irrigation.erp.backend.service;

import com.irrigation.erp.backend.dto.InventoryItemCreateRequestDTO;
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
    public InventoryItem createInventoryItem(InventoryItemCreateRequestDTO requestDTO) { // Takes DTO directly

        //check for unique item code
        if(inventoryItemRepository.findByItemCode(requestDTO.getItemCode()).isPresent()){
            throw new IllegalArgumentException("Item with code '" + requestDTO.getItemCode() + "' already exists.");
        }

        //fetch entities from repositories
        ItemCategory itemCategory = itemCategoryRepository.findByName(requestDTO.getItemCategoryName())
                .orElseThrow(()-> new IllegalArgumentException("Item Category '" + requestDTO.getItemCategoryName() + "' not found."));

        ItemType itemType = itemTypeRepository.findByName(requestDTO.getItemTypeName())
                .orElseThrow(()-> new IllegalArgumentException("Item Type '" + requestDTO.getItemTypeName() + "' not found."));

        User creatingUser = userRepository.findById(requestDTO.getCreatingUserId())
                .orElseThrow(()-> new IllegalArgumentException("User with id '" + requestDTO.getCreatingUserId() + "' not found."));
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
                .orElseThrow(()-> new IllegalArgumentException("Item with code '" + itemCode + "' not found."));

        if (itemName != null) item.setItemName(itemName);
        if (itemDescription != null) item.setItemDescription(itemDescription);
        if (unitOfMeasurement != null) item.setUnitOfMeasurement(unitOfMeasurement);
        if (minimumStockLevel != null) item.setMinimumStockLevel(minimumStockLevel);
        if (locationInStore != null) item.setLocationInStore(locationInStore);
        if (unitPrice != null) item.setUnitPrice(unitPrice);

        if (itemCategoryName != null && item.getItemCategory() != null && !item.getItemCategory().getName().equals(itemCategoryName)) { // Added null check for getItemCategory()
            ItemCategory newCategory = itemCategoryRepository.findByName(itemCategoryName)
                    .orElseThrow(() -> new IllegalArgumentException("Item Category '" + itemCategoryName + "' not found."));
            item.setItemCategory(newCategory);
        }
        if (itemTypeName != null && item.getItemType() != null && !item.getItemType().getName().equals(itemTypeName)) { // Added null check for getItemType()
            ItemType newType = itemTypeRepository.findByName(itemTypeName)
                    .orElseThrow(() -> new IllegalArgumentException("Item Type '" + itemTypeName + "' not found."));
            item.setItemType(newType);
        }

        if(isActiveStatus != null) item.setIsActive(isActiveStatus);

        //update updatingUser fields
        User updatingUser = userRepository.findById(updatingUserId)
                .orElseThrow(()-> new IllegalArgumentException("User with id '" + updatingUserId + "' not found."));
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

    public List<InventoryItem> getLowStockItems() {
        return inventoryItemRepository.findAll().stream()
                .filter(item -> item.getStockStatus() == StockStatus.LOW || item.getStockStatus() == StockStatus.OUT_OF_STOCK)
                .collect(Collectors.toList());
    }

    @Transactional
    public InventoryItem adjustStock(Long itemId, @NotNull(message = "Quantity change cannot be null") BigDecimal quantityChange, Long adjustingUserId, String reason) {
        InventoryItem item = inventoryItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory item with ID " + itemId + " not found."));

        BigDecimal newQuantity = item.getCurrentStockQuantity().add(quantityChange);


        if (newQuantity.compareTo(BigDecimal.ZERO) < 0 && item.getItemType() != null && item.getItemType().getName().equals("Material")) {
            throw new IllegalArgumentException("Stock quantity cannot go below zero for " + item.getItemName() + " (Type: Material).");
        }

        item.setCurrentStockQuantity(newQuantity);

        User adjustingUser = userRepository.findById(adjustingUserId)
                .orElseThrow(() -> new IllegalArgumentException("Adjusting User with ID " + adjustingUserId + " not found."));
        item.setLastUpdatedByUser(adjustingUser);
        item.setLastUpdatedAt(LocalDateTime.now());

        return inventoryItemRepository.save(item);
    }
}