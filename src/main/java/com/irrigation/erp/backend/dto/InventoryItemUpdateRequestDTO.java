package com.irrigation.erp.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class InventoryItemUpdateRequestDTO {
    // Getters and Setters
    private String itemName;
    private String itemDescription;
    private String unitOfMeasurement;

    @Min(value = 0, message = "Minimum stock level cannot be negative")
    private BigDecimal minimumStockLevel;

    private String locationInStore;

    @DecimalMin(value = "0.0", inclusive = false, message = "Unit price must be positive")
    private BigDecimal unitPrice;

    private String itemCategoryName;
    private String itemTypeName;
    private Boolean isActiveStatus;

    @NotNull(message = "Updating user ID cannot be null")
    private Long updatingUserId;


}
