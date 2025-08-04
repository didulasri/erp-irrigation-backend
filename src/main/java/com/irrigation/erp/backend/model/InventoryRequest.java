package com.irrigation.erp.backend.model;


import com.irrigation.erp.backend.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="inventory_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_user_id", nullable = false)
    private User requester;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InventoryRequestLineItem> lineItems = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String purpose;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;

    @Column(name = "requested_at", nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by_user_id")
    private User processedBy;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // Helper method to add a line item
    public void addLineItem(InventoryRequestLineItem lineItem) {
        lineItems.add(lineItem);
        lineItem.setRequest(this);
    }



}
