package com.irrigation.erp.backend.dto;

import com.irrigation.erp.backend.model.PurchaseRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@ToString
public class PurchaseResponseFormDTO {

    private Long id;
    private String refNo;
    private String requestedByName;
    private LocalDateTime requestedAt;
    private BigDecimal totalValue;
    private String division;
    private String subDivision;
    private String programme;
    private String project;
    private String object;
    private PurchaseRequest.Status status;
    private List<PurchaseLineItemDTO> items;

    @Getter
    @AllArgsConstructor
    @ToString
    public static class PurchaseLineItemDTO {
        private Long id;
        private Long inventoryRequestLineItemId;
        private String itemName;
        private BigDecimal quantity;
        private BigDecimal estimatedPrice;
    }
}
