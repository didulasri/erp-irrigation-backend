package com.irrigation.erp.backend.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InventoryRequestCreateDTO {

    @NotNull(message = "Requester User ID cannot be null")
    private Long requesterUserId;


    @NotBlank(message = "Item Code cannot be empty")
    private String itemCode;

    @NotNull(message = "Requested quantity cannot be null")
    @Min(value = 0, message = "Requested quantity must be positive")
    private Double requestedQuantity;

    @NotBlank(message = "Purpose cannot be empty")
    private String purpose;
}
