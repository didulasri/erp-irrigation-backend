package com.irrigation.erp.backend.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_issues")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class InventoryIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_request_line_item_id", nullable = false)
    private InventoryRequestLineItem requestLineItem;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_request_id", nullable = false)
    private InventoryRequest inventoryRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issued_item_id", nullable = false)
    private InventoryItem issuedItem;

    @Column(nullable = false)
    private Double issuedQuantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issued_by_user_id", nullable = false)
    private User issuedByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issued_to_user_id", nullable = false)
    private User issuedToUser;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "item_value", nullable = false)
    private BigDecimal itemValue;

    @Column(columnDefinition = "TEXT")
    private String purpose;

    @Column(columnDefinition = "TEXT")
    private String notes;


}
