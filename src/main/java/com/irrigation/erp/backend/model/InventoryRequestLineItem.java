package com.irrigation.erp.backend.model;

import com.irrigation.erp.backend.enums.RequestLineItemStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "inventory_request_line_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryRequestLineItem {
    //for requests of more than one item
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_request_id", nullable = false)
    private InventoryRequest request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_item_id", nullable = false)
    private InventoryItem requestedItem;

    @Column(nullable = false)
    private BigDecimal requestedQuantity;

    //Status for specific line item
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestLineItemStatus status;


}
