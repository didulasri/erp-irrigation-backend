package com.irrigation.erp.backend.controller;

import com.irrigation.erp.backend.dto.InventoryItemCreateRequestDTO;
import com.irrigation.erp.backend.dto.InventoryItemResponseDTO;
import com.irrigation.erp.backend.dto.InventoryItemUpdateRequestDTO;
import com.irrigation.erp.backend.dto.StockAdjustmentRequestDTO; // Assuming you have this DTO for adjustStock
import com.irrigation.erp.backend.model.InventoryItem;
import com.irrigation.erp.backend.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inventory/items")
@CrossOrigin(origins = "http://localhost:5173")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }
    public static final String ITEM_NOT_FOUND = "' not found.";
    public static final String INVENTORY_ITEM_WITH_CODE = "Inventory item with code";

    private InventoryItemResponseDTO convertToDto(InventoryItem inventoryItem) {
        InventoryItemResponseDTO dto = new InventoryItemResponseDTO();
        dto.setId(inventoryItem.getId());
        dto.setItemCode(inventoryItem.getItemCode());
        dto.setItemName(inventoryItem.getItemName());
        dto.setItemDescription(inventoryItem.getItemDescription());
        dto.setUnitOfMeasurement(inventoryItem.getUnitOfMeasurement());
        dto.setCurrentStockQuantity(inventoryItem.getCurrentStockQuantity());
        dto.setMinimumStockLevel(inventoryItem.getMinimumStockLevel());
        dto.setLocationInStore(inventoryItem.getLocationInStore());
        dto.setUnitPrice(inventoryItem.getUnitPrice());

        // Set related entity details (flattening the relationship)
        if (inventoryItem.getItemCategory() != null) {
            dto.setItemCategoryId(inventoryItem.getItemCategory().getId());
            dto.setItemCategoryName(inventoryItem.getItemCategory().getName());
        }
        if (inventoryItem.getItemType() != null) {
            dto.setItemTypeId(inventoryItem.getItemType().getId());
            dto.setItemTypeName(inventoryItem.getItemType().getName());
        }
        if (inventoryItem.getCreatingUser() != null) {
            dto.setCreatedByUserId(inventoryItem.getCreatingUser().getId());
            // Assuming User has getUsername()
            // Make sure your User model has @Data or at least a getUsername() method
            dto.setCreatedByUsername(inventoryItem.getCreatingUser().getUsername());
        }
        if (inventoryItem.getLastUpdatedByUser() != null) {
            dto.setLastUpdatedByUserId(inventoryItem.getLastUpdatedByUser().getId());
            // Assuming User has getUsername()
            dto.setLastUpdatedByUsername(inventoryItem.getLastUpdatedByUser().getUsername());
        }

        dto.setLastUpdatedAt(inventoryItem.getLastUpdatedAt());
        dto.setIsActive(inventoryItem.getIsActive());
        dto.setStockStatus(inventoryItem.getStockStatus());
        return dto;
    }

    //API Endpoints

    @PostMapping("/create")
    public ResponseEntity<InventoryItemResponseDTO> createInventoryItem(@Valid @RequestBody InventoryItemCreateRequestDTO requestDTO){
        try{

            InventoryItem createdItem = inventoryService.createInventoryItem(requestDTO);


            return new ResponseEntity<>(convertToDto(createdItem), HttpStatus.CREATED);
        }catch (IllegalArgumentException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/update/{itemCode}")
    public ResponseEntity<InventoryItemResponseDTO>updateInventoryItem(
            @PathVariable String itemCode,
            @Valid @RequestBody InventoryItemUpdateRequestDTO requestDTO){
        try {
            InventoryItem updatedItem = inventoryService.updateInventoryItem(
                    itemCode, // Use the path variable for item identification
                    requestDTO.getItemName(),
                    requestDTO.getItemDescription(),
                    requestDTO.getUnitOfMeasurement(),
                    requestDTO.getMinimumStockLevel(),
                    requestDTO.getLocationInStore(),
                    requestDTO.getUnitPrice(),
                    requestDTO.getItemCategoryName(),
                    requestDTO.getItemTypeName(),
                    requestDTO.getIsActiveStatus(),
                    requestDTO.getUpdatingUserId()
            );
            return ResponseEntity.ok(convertToDto(updatedItem));
        }catch(IllegalArgumentException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PatchMapping("/{itemCode}/adjust-stock")
    public ResponseEntity<InventoryItemResponseDTO> adjustStock(@PathVariable String itemCode,
                                                                @Valid @RequestBody StockAdjustmentRequestDTO requestDTO){
        try {
            InventoryItem adjustedItem = inventoryService.adjustStock(
                    inventoryService.getInventoryItemByItemCode(itemCode)
                            .orElseThrow(() -> new IllegalArgumentException(INVENTORY_ITEM_WITH_CODE  + itemCode + ITEM_NOT_FOUND))
                            .getId(),
                    requestDTO.getQuantityChange(),
                    requestDTO.getAdjustingUserId(),
                    requestDTO.getReason()
            );
            return ResponseEntity.ok(convertToDto(adjustedItem));
        }catch (IllegalArgumentException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<InventoryItemResponseDTO>> getAllInventoryItems(){
        List<InventoryItem> items = inventoryService.getAllInventoryItems();
        List<InventoryItemResponseDTO> dtos = items.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }


    @GetMapping("/category/{categoryName}")
    public ResponseEntity<List<InventoryItemResponseDTO>> getInventoryItemsByCategory(@PathVariable String categoryName) {
        try {
            List<InventoryItemResponseDTO> items = inventoryService.getInventoryItemsByCategory(categoryName);
            return ResponseEntity.ok(items);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    //.Get inventory items by category ID
    @GetMapping("/category-id/{categoryId}")
    public ResponseEntity<List<InventoryItemResponseDTO>> getInventoryItemsByCategoryId(@PathVariable Long categoryId) {
        try {
            List<InventoryItem> items = inventoryService.getInventoryItemsByCategoryId(categoryId);
            List<InventoryItemResponseDTO> dtos = items.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }


    @GetMapping("/category/{categoryName}/low-stock")
    public ResponseEntity<List<InventoryItemResponseDTO>> getLowStockItemsByCategory(@PathVariable String categoryName) {
        try {
            List<InventoryItemResponseDTO> items = inventoryService.getLowStockItemsByCategory(categoryName);

            return ResponseEntity.ok(items);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/{itemCode}")
    public ResponseEntity<InventoryItemResponseDTO> getInventoryItem(@PathVariable String itemCode){
        return inventoryService.getInventoryItemByItemCode(itemCode)
                .map(this::convertToDto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, INVENTORY_ITEM_WITH_CODE  + itemCode + ITEM_NOT_FOUND));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryItemResponseDTO>> getLowStockInventoryItems(){
        List<InventoryItem> items = inventoryService.getLowStockItems();
        List<InventoryItemResponseDTO> dtos = items.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }


    @DeleteMapping("/{itemCode}")
    public ResponseEntity<Void> deactivateInventoryItem(@PathVariable String itemCode, @RequestParam Long updatingUserId) {
        try {
            // Find the item by code

            InventoryItem itemToDeactivate = inventoryService.getInventoryItemByItemCode(itemCode)
                    .orElseThrow(() -> new IllegalArgumentException(INVENTORY_ITEM_WITH_CODE  + itemCode + ITEM_NOT_FOUND));

            System.out.println("Deactivating item: " + itemToDeactivate);



            // Call the update service method to set isActive to false
            inventoryService.updateInventoryItem(
                    itemCode, null, null, null, null, null, null, null, null, false, updatingUserId);

            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}