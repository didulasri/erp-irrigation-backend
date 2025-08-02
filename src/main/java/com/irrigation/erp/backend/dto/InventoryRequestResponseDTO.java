package com.irrigation.erp.backend.dto;


import com.irrigation.erp.backend.enums.RequestStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class InventoryRequestResponseDTO {

    private Long id;
    private Long requesterUserId;
    private String requesterUsername;
    private Long requestedItemId;
    private String requestedItemCode;
    private String requestedItemName;
    private Double requestedQuantity;
    private String purpose;
    private RequestStatus status;
    private LocalDateTime requestedAt;
    private Long processedByUserId;
    private String processedByUsername;
    private LocalDateTime processedAt;
    private BigDecimal itemValue;

}
