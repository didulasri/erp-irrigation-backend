package com.irrigation.erp.backend.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PurchaseItemDTO {
    private Long id;
    private Long itemId;
    private String itemCode;
    private String itemName;
    private Double quantity;
    private String unitOfMeasurement;
    private BigDecimal unitPrice;
}