package com.irrigation.erp.backend.dto;


import com.irrigation.erp.backend.enums.RequestStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class InventoryRequestResponseDTO {

    private Long id;
    private Long requesterUserId;
    private String requesterUsername;
    private String purpose;
    private RequestStatus status;
    private LocalDateTime requestedAt;
    private Long processedByUserId;
    private String processedByUsername;
    private LocalDateTime processedAt;
    private String notes;
    private BigDecimal itemValue;

    private List<InventoryRequestLineItemResponseDTO> items;

}
