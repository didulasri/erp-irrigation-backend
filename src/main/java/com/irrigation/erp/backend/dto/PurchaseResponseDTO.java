package com.irrigation.erp.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.irrigation.erp.backend.model.PurchaseRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@ToString
public class PurchaseResponseDTO {
    private Long id;
    private String requestedByName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDateTime requestedAt;

    private String itemName;
    private PurchaseRequest.Status status;
}
