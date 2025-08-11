package com.irrigation.erp.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class IssueRequestDTO {

    @NotNull(message = "Store keeper User ID cannot be null")
    private Long issuedByUserId;

    @NotNull(message = "Issued quantity cannot be null")

    private BigDecimal issuedQuantity;

    private String issueNotes;





}
