package com.irrigation.erp.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoStockRequestDTO {
    @NotNull(message = "Store keeper User ID cannot be null")
    private Long storeKeeperUserId;

    private String notes;



}
