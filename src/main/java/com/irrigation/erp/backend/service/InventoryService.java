package com.irrigation.erp.backend.service;

import com.irrigation.erp.backend.model.InventoryItem;
import com.irrigation.erp.backend.model.ItemCategory;
import com.irrigation.erp.backend.model.ItemType;
import com.irrigation.erp.backend.model.User;
import com.irrigation.erp.backend.repository.InventoryItemRepository;
import com.irrigation.erp.backend.repository.ItemCategoryRepository;
import com.irrigation.erp.backend.repository.ItemTypeRepository;
import com.irrigation.erp.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.hibernate.boot.model.naming.IllegalIdentifierException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    public InventoryItem createInventoryItem(String itemCode, String itemName, String itemDescription, String unitOfMeasurement, Double currentStockQuantity,
                                             Double minimumStockLevel, String locationInStore, BigDecimal unitPrice, String itemCategoryName,String itemTypeName, Long creatingUserId ){

        //check for unique item code
        if(inventoryItemRepository.findByItemCode(itemCode).isPresent()){
            throw new IllegalArgumentException("Item with code '" + itemCode + "' already exists.");
        }

      //fetch entities from repositories
        ItemCategory itemCategory = itemCategoryRepository.findByName(itemCategoryName)
                .orElseThrow(()-> new IllegalArgumentException("Item Category '" + itemCategoryName + "' not found."));

        ItemType itemType = itemTypeRepository.findByName(itemTypeName)
                .orElseThrow(()-> new IllegalArgumentException("Item Type '" + itemTypeName + "' not found."));

        User creatingUser = userRepository.findById(creatingUserId)
                .orElseThrow(()-> new IllegalIdentifierException("User with id '" + creatingUserId + "' not found."));


        //Create and Populate New InventoryItem Object
        InventoryItem newItem = new InventoryItem();
        newItem.setItemCode(itemCode);
        newItem.setItemName(itemName);
        newItem.setItemDescription(itemDescription);
        newItem.setUnitOfMeasurement(unitOfMeasurement);
        newItem.setCurrentStockQuantity(currentStockQuantity);
        newItem.setMinimumStockLevel(minimumStockLevel);
        newItem.setLocationInStore(locationInStore);
        newItem.setUnitPrice(unitPrice);
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
    public InventoryItem updateInventoryItem(String itemCode, String itemName, String itemDescription,String unitOfMeasurement,
                                             Double minimumStockLevel,
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

        if (itemCategoryName != null && !item.getItemCategory().getName().equals(itemCategoryName)) {
            ItemCategory newCategory = itemCategoryRepository.findByName(itemCategoryName)
                    .orElseThrow(() -> new IllegalArgumentException("Item Category '" + itemCategoryName + "' not found."));
            item.setItemCategory(newCategory);
        }
        if (itemTypeName != null && !item.getItemType().getName().equals(itemTypeName)) {
            ItemType newType = itemTypeRepository.findByName(itemTypeName)
                    .orElseThrow(() -> new IllegalArgumentException("Item Type '" + itemTypeName + "' not found."));
            item.setItemType(newType);
        }


        if(isActiveStatus != null) item.setIsActive(isActiveStatus);


        //update updatingUser fields
        User updatingUser = userRepository.findById(updatingUserId)
                .orElseThrow(()-> new IllegalIdentifierException("User with id '" + updatingUserId + "' not found."));
        item.setLastUpdatedByUser(updatingUser);
        item.setLastUpdatedAt(LocalDateTime.now());

        return inventoryItemRepository.save(item);


    }




}

