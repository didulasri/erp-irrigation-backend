package com.irrigation.erp.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class StockAdjustmentRequestDTO {

    // Getters and Setters
    @NotNull(message = "Quantity change cannot be null")
    private Double quantityChange;

    @NotNull(message = "Adjusting user ID cannot be null")
    private Long adjustingUserId;

    @NotNull(message = "Reason for adjustment cannot be empty")
    private String reason;

}
