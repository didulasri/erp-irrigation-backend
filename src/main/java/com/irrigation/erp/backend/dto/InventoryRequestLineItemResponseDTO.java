package com.irrigation.erp.backend.dto;


import com.irrigation.erp.backend.enums.RequestLineItemStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class InventoryRequestLineItemResponseDTO {
    private Long id;
    private Long requestedItemId;
    private String requestedItemCode;
    private String requestedItemName;
    private BigDecimal requestedQuantity;
    private BigDecimal currentStockQuantity;

    private RequestLineItemStatus status;


}
