package com.irrigation.erp.backend.dto;


import com.irrigation.erp.backend.model.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class CreateGrnRequest {
    @NotBlank
    private String receiptNo;

    @NotBlank
    private String receivingStation;

    private String referenceOrderNo;

    private LocalDate referenceOrderDate;


    private String issuingOfficer;


    private String station;

    private Long createdBy;


    @Valid
    @NotEmpty
    private List<GRNItemDTO> items;
}
