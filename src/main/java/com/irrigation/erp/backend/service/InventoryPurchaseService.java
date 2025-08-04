package com.irrigation.erp.backend.service;

import com.irrigation.erp.backend.dto.*;
import com.irrigation.erp.backend.model.InventoryPurchase;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InventoryPurchaseService {
    InventoryPurchase createPurchase(InventoryPurchaseCreateRequestDTO requestDTO);
    InventoryPurchase updatePurchase(Long purchaseId, String refNo, LocalDate date,
                                     String division, String subDivision, String programme,
                                     String project, String object, String description,
                                     String payee, String preparedBy, String goodReceivingNotePath,
                                     String shopBillPath, Long acceptedByUserId,
                                     Long inventoryRequestId, List<PurchaseItemRequestDTO> items);
    List<InventoryPurchase> getAllPurchases();
    Optional<InventoryPurchase> getPurchaseById(Long purchaseId);
    void deletePurchase(Long purchaseId);
}