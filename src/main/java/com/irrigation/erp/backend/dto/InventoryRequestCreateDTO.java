package com.irrigation.erp.backend.dto;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class InventoryRequestCreateDTO {

    @NotNull(message = "Requester User ID cannot be null")
    private Long requesterUserId;


    @NotNull(message = "Request must contain at least one item")
    @Size(min = 1, message = "Request must contain at least one item")
    @Valid
    private List<InventoryRequestLineItemCreateDTO> items;

    @NotBlank(message = "Purpose cannot be empty")
    private String purpose;
}
