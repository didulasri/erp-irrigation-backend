package com.irrigation.erp.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "purchase_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor


public class PurchaseRequest {
    public enum Status {
        PENDING,
        APPROVED,
        DIRECT_PURCHASE,
        REJECTED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String division;
    private String subDivision;
    private String programme;
    private String project;
    private String object;


    @Column(name = "ref_no", unique = true)
    private String refNo;

    @Column(name = "requested_by_user_id", nullable = false, updatable = false)
    private Long requestedByUserId;

    @Column(name = "requested_at", nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    @Column(name = "total_value", nullable = false)
    private BigDecimal totalValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @OneToMany(mappedBy = "purchaseRequest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PurchaseRequestLineItem> items;




}
