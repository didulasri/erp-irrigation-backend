package com.irrigation.erp.backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter


public class BatchIssueItemDTO {

    private Long inventoryRequestLineItemId;


    private BigDecimal issuedQuantity;
}
