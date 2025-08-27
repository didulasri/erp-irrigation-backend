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
    @DecimalMin(value = "0", inclusive = true, message = "Current stock quantity cannot be negative")
    private BigDecimal currentStockQuantity;

     @NotNull(message = "Minimum stock level cannot be null")
    @DecimalMin(value = "0", inclusive = true, message = "Minimum stock level cannot be negative")
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

    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getItemDescription() { return itemDescription; }
    public void setItemDescription(String itemDescription) { this.itemDescription = itemDescription; }

    public String getUnitOfMeasurement() { return unitOfMeasurement; }
    public void setUnitOfMeasurement(String unitOfMeasurement) { this.unitOfMeasurement = unitOfMeasurement; }

    public BigDecimal getCurrentStockQuantity() { return currentStockQuantity; }
    public void setCurrentStockQuantity(BigDecimal currentStockQuantity) { this.currentStockQuantity = currentStockQuantity; }

    public BigDecimal getMinimumStockLevel() { return minimumStockLevel; }
    public void setMinimumStockLevel(BigDecimal minimumStockLevel) { this.minimumStockLevel = minimumStockLevel; }

    public String getLocationInStore() { return locationInStore; }
    public void setLocationInStore(String locationInStore) { this.locationInStore = locationInStore; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public String getItemCategoryName() { return itemCategoryName; }
    public void setItemCategoryName(String itemCategoryName) { this.itemCategoryName = itemCategoryName; }

    public String getItemTypeName() { return itemTypeName; }
    public void setItemTypeName(String itemTypeName) { this.itemTypeName = itemTypeName; }

    public Long getCreatingUserId() { return creatingUserId; }
    public void setCreatingUserId(Long creatingUserId) { this.creatingUserId = creatingUserId; }


}
