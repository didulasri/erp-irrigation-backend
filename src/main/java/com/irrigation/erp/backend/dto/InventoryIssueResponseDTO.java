package com.irrigation.erp.backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class InventoryIssueResponseDTO {

    private Long id;

    // Issue details
    private BigDecimal issuedQuantity;
    private LocalDateTime issuedAt;
    private BigDecimal itemValue;
    private String purpose;
    private String notes;

    // Issued item details
    private Long issuedItemId;
    private String issuedItemCode;
    private String issuedItemName;

    // User details
    private Long issuedByUserId;
    private String issuedByUsername;
    private Long issuedToUserId;
    private String issuedToUsername;

    // Request details
    private Long inventoryRequestId;
    private String requestCode;
    private Long requestLineItemId;
    private BigDecimal requestedQuantity;
}