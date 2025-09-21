package com.irrigation.erp.backend.dto;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BatchIssueRequestDTO {
    @NotNull(message = "Issued by user ID cannot be null")
    private Long issuedByUserId;

    @NotNull(message = "Issue notes cannot be null")
    private String issueNotes;


    @Valid
    @NotEmpty(message = "Items to issue cannot be empty")
    private List<BatchIssueItemDTO> itemsToIssue;

}
