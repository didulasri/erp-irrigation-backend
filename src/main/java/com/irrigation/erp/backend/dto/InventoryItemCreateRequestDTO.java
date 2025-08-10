package com.irrigation.erp.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class InventoryItemCreateRequestDTO {

    @NotBlank(message = "Item code cannot be empty")
    private String itemCode;

    @NotBlank(message = "Item name cannot be empty")
    private String itemName;

    private String itemDescription;

    @NotBlank(message = "Unit of measurement cannot be empty")
    private String unitOfMeasurement;

    @NotNull(message = "Current stock quantity cannot be null")
    @Min(value = 0, message = "Current stock quantity cannot be negative")
    private BigDecimal currentStockQuantity;

    @NotNull(message = "Minimum stock level cannot be null")
    @Min(value = 0, message = "Minimum stock level cannot be negative")
    private BigDecimal minimumStockLevel;

    @NotBlank(message = "Location in store cannot be empty")
    private String locationInStore;

    @NotNull(message = "Unit price cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Unit price must be positive")
    private BigDecimal unitPrice;

    @NotBlank(message = "Item category name cannot be empty")
    private String itemCategoryName;

    @NotBlank(message = "Item type name cannot be empty")
    private String itemTypeName;

    @NotNull(message = "Creating user ID cannot be null")
    private Long creatingUserId;


    //getters and setters


}
