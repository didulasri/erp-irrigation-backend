package com.irrigation.erp.backend.dto;

import com.irrigation.erp.backend.enums.StockStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
public class InventoryItemResponseDTO {

    private Long id;
    private String itemCode;
    private String itemName;
    private String itemDescription;
    private String unitOfMeasurement;
    private BigDecimal currentStockQuantity;
    private BigDecimal minimumStockLevel;
    private String locationInStore;
    private BigDecimal unitPrice;


    private Long itemCategoryId;
    private String itemCategoryName;
    private Long itemTypeId;
    private String itemTypeName;

    private Long createdByUserId;
    private String createdByUsername;
    private Long lastUpdatedByUserId;
    private String lastUpdatedByUsername;
    private LocalDateTime lastUpdatedAt;
    private Boolean isActive;
    private StockStatus stockStatus;
    private Boolean pendingPurchaseRequest;




    // Getters and Setters


}
