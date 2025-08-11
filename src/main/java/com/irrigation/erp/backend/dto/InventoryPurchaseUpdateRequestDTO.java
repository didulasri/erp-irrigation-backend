package com.irrigation.erp.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class InventoryPurchaseUpdateRequestDTO {
    @NotBlank
    private String refNo;

    @NotNull
    private LocalDate date;

    @NotBlank
    private String division;

    @NotBlank
    private String subDivision;

    @NotBlank
    private String programme;

    @NotBlank
    private String project;

    @NotBlank
    private String object;

    private String description;

    @NotBlank
    private String payee;

    @NotBlank
    private String preparedBy;

    private String goodReceivingNotePath;
    private String shopBillPath;

    @NotNull
    private Long acceptedByUserId;

    @NotNull
    private Long inventoryRequestId;

    @NotNull
    private List<PurchaseItemRequestDTO> items;
}