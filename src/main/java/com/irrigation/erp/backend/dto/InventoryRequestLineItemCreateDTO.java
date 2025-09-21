package com.irrigation.erp.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class InventoryRequestLineItemCreateDTO {
    @NotNull(message = "Item code cannot be null")
    private String itemCode;

    @NotNull(message = "Requested quantity cannot be null")
    private BigDecimal requestedQuantity;
}
