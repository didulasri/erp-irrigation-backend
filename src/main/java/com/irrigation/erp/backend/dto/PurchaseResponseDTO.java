package com.irrigation.erp.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class PurchaseResponseDTO {
    private Long id;
    private String requestedByName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDateTime requestedAt;
    private String itemName;

    public PurchaseResponseDTO(Long id, String requestedByName, LocalDateTime requestedAt, String itemName) {
        this.id = id;
        this.requestedByName = requestedByName;
        this.requestedAt = requestedAt;
        this.itemName = itemName;
    }

    public Long getId() { return id; }
    public String getRequestedByName() { return requestedByName; }
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public String getItemName() { return itemName; }
}
