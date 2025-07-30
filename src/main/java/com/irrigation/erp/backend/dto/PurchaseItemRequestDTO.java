package com.irrigation.erp.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PurchaseItemRequestDTO {
    @NotNull
    private Long itemId;

    @NotNull
    private Double quantity;
}