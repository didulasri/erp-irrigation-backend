package com.irrigation.erp.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IssueRequestDTO {

    @NotNull(message = "Store keeper User ID cannot be null")
    private Long issuedByUserId;

    @NotNull(message = "Issued quantity cannot be null")
    @Min(value = 0, message = "Issued quantity must be positive")
    private Double issuedQuantity;

    private String issueNotes;





}
