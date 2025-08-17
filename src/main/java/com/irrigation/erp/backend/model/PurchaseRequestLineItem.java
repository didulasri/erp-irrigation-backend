package com.irrigation.erp.backend.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "purchase_request_line_items")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class PurchaseRequestLineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_request_id", nullable = false)
    private PurchaseRequest purchaseRequest;


    @Column(name = "inventory_request_line_item_id", nullable = false)
    private Long inventoryRequestLineItemId;


    @Column(name = "item_name", nullable = false)
    private String itemName;


    @Column(name = "quantity", nullable = false)
    private BigDecimal quantity;


    @Column(name = "estimated_price", nullable = false)
    private BigDecimal estimatedPrice;


}
