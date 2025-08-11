package com.irrigation.erp.backend.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class InventoryPurchaseResponseDTO {
    private Long purchaseId;
    private String refNo;
    private LocalDate date;
    private String division;
    private String subDivision;
    private String programme;
    private String project;
    private String object;
    private String description;
    private String payee;
    private String preparedBy;
    private String goodReceivingNotePath;
    private String shopBillPath;
    private Long inventoryRequestId;
    private Long acceptedByUserId;
    private String acceptedByUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PurchaseItemDTO> items;
}