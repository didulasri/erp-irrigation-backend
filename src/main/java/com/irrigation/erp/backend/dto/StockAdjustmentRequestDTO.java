package com.irrigation.erp.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class StockAdjustmentRequestDTO {

    // Getters and Setters
    @NotNull(message = "Quantity change cannot be null")
    private BigDecimal quantityChange;

    @NotNull(message = "Adjusting user ID cannot be null")
    private Long adjustingUserId;

    @NotNull(message = "Reason for adjustment cannot be empty")
    private String reason;

}
