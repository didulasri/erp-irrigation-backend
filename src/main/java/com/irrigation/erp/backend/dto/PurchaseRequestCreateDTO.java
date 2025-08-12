package com.irrigation.erp.backend.dto;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PurchaseRequestCreateDTO {
    @NotBlank(message = "Division cannot be empty")
    private String division;

    @NotBlank(message = "Sub-division cannot be empty")
    private String subDivision;

    @NotBlank(message = "Programme cannot be empty")
    private String programme;

    @NotBlank(message = "Project cannot be empty")
    private String project;

    @NotBlank(message = "Object cannot be empty")
    private String object;

    private String refNo;

    @NotNull(message = "Items list cannot be null")
    @Size(min = 1, message = "At least one item must be included")
    @Valid
    private List<PurchaseRequestLineItemDTO> items;
}
