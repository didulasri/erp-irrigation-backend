package com.irrigation.erp.backend.controller;

import com.irrigation.erp.backend.dto.*;
import com.irrigation.erp.backend.model.InventoryPurchase;
import com.irrigation.erp.backend.model.PurchaseItem;
import com.irrigation.erp.backend.service.InventoryPurchaseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inventory/purchases")
public class InventoryPurchaseController {

    private final InventoryPurchaseService inventoryPurchaseService;

    public InventoryPurchaseController(InventoryPurchaseService inventoryPurchaseService) {
        this.inventoryPurchaseService = inventoryPurchaseService;
    }

    private InventoryPurchaseResponseDTO convertToDto(InventoryPurchase purchase) {
        InventoryPurchaseResponseDTO dto = new InventoryPurchaseResponseDTO();
        dto.setPurchaseId(purchase.getId());
        dto.setRefNo(purchase.getRefNo());
        dto.setDate(purchase.getDate());
        dto.setDivision(purchase.getDivision());
        dto.setSubDivision(purchase.getSubDivision());
        dto.setProgramme(purchase.getProgramme());
        dto.setProject(purchase.getProject());
        dto.setObject(purchase.getObject());
        dto.setDescription(purchase.getDescription());
        dto.setPayee(purchase.getPayee());
        dto.setPreparedBy(purchase.getPreparedBy());
        dto.setGoodReceivingNotePath(purchase.getGoodReceivingNotePath());
        dto.setShopBillPath(purchase.getShopBillPath());
        dto.setCreatedAt(purchase.getCreatedAt());
        dto.setUpdatedAt(purchase.getUpdatedAt());

        // Set related entity details
        if (purchase.getInventoryRequest() != null) {
            dto.setInventoryRequestId(purchase.getInventoryRequest().getId());
        }
        if (purchase.getAcceptedByUser() != null) {
            dto.setAcceptedByUserId(purchase.getAcceptedByUser().getId());
            dto.setAcceptedByUsername(purchase.getAcceptedByUser().getUsername());
        }

        // Convert purchase items
        List<PurchaseItemDTO> itemDTOs = purchase.getItems().stream()
                .map(this::convertPurchaseItemToDto)
                .collect(Collectors.toList());
        dto.setItems(itemDTOs);

        return dto;
    }

    private PurchaseItemDTO convertPurchaseItemToDto(PurchaseItem purchaseItem) {
        PurchaseItemDTO dto = new PurchaseItemDTO();
        dto.setId(purchaseItem.getId());
        dto.setItemId(purchaseItem.getItem().getId());
        dto.setItemCode(purchaseItem.getItem().getItemCode());
        dto.setItemName(purchaseItem.getItem().getItemName());
        dto.setQuantity(purchaseItem.getQuantity());
        dto.setUnitOfMeasurement(purchaseItem.getItem().getUnitOfMeasurement());
        dto.setUnitPrice(purchaseItem.getItem().getUnitPrice());
        return dto;
    }

    // API Endpoints

    @PostMapping("/create")
    public ResponseEntity<InventoryPurchaseResponseDTO> createPurchase(
            @Valid @RequestBody InventoryPurchaseCreateRequestDTO requestDTO) {
        try {
            InventoryPurchase createdPurchase = inventoryPurchaseService.createPurchase(requestDTO);
            return new ResponseEntity<>(convertToDto(createdPurchase), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/update/{purchaseId}")
    public ResponseEntity<InventoryPurchaseResponseDTO> updatePurchase(
            @PathVariable Long purchaseId,
            @Valid @RequestBody InventoryPurchaseUpdateRequestDTO requestDTO) {
        try {
            InventoryPurchase updatedPurchase = inventoryPurchaseService.updatePurchase(
                    purchaseId,
                    requestDTO.getRefNo(),
                    requestDTO.getDate(),
                    requestDTO.getDivision(),
                    requestDTO.getSubDivision(),
                    requestDTO.getProgramme(),
                    requestDTO.getProject(),
                    requestDTO.getObject(),
                    requestDTO.getDescription(),
                    requestDTO.getPayee(),
                    requestDTO.getPreparedBy(),
                    requestDTO.getGoodReceivingNotePath(),
                    requestDTO.getShopBillPath(),
                    requestDTO.getAcceptedByUserId(),
                    requestDTO.getInventoryRequestId(),
                    requestDTO.getItems()
            );
            return ResponseEntity.ok(convertToDto(updatedPurchase));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<InventoryPurchaseResponseDTO>> getAllPurchases() {
        List<InventoryPurchase> purchases = inventoryPurchaseService.getAllPurchases();
        List<InventoryPurchaseResponseDTO> dtos = purchases.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{purchaseId}")
    public ResponseEntity<InventoryPurchaseResponseDTO> getPurchaseById(
            @PathVariable Long purchaseId) {
        return inventoryPurchaseService.getPurchaseById(purchaseId)
                .map(this::convertToDto)
                .map(dto -> ResponseEntity.ok(dto))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Purchase with id '" + purchaseId + "' not found."));
    }

    @DeleteMapping("/{purchaseId}")
    public ResponseEntity<Void> deletePurchase(@PathVariable Long purchaseId) {
        try {
            inventoryPurchaseService.deletePurchase(purchaseId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}