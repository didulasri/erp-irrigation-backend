package com.irrigation.erp.backend.dto;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @NotNull(message = "Batch issue must contain at least one item")
    @Size(min = 1, message = "Batch issue must contain at least one item")
    @Valid
    private List<BatchIssueLineItemDTO> itemsToIssue;

}
