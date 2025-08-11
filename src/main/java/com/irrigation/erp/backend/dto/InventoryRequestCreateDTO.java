package com.irrigation.erp.backend.dto;


import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class InventoryRequestCreateDTO {

    @NotNull(message = "Requester User ID cannot be null")
    private Long requesterUserId;
    private String purpose;

    @Valid
    @NotEmpty(message = "Request must contain at least one item.")
    private List<InventoryRequestLineItemCreateDTO> items;



}
