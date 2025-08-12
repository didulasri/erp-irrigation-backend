package com.irrigation.erp.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
@Getter
@Setter

public class PurchaseRequestLineItemDTO {
    @NotNull(message = "Inventory request line item ID cannot be null")
    @Min(value = 1, message = "Inventory request line item ID must be a positive number")
    private Long inventoryRequestLineItemId;

    @NotBlank(message = "Item name cannot be empty")
    private String itemName;

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Quantity must be at least 1")
    private BigDecimal quantity;

    @NotNull(message = "Estimated price cannot be null")
    @Min(value = 0, message = "Estimated price cannot be negative")
    private BigDecimal estimatedPrice;
}
