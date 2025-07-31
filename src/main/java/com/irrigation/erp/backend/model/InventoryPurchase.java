package com.irrigation.erp.backend.model;

import jakarta.persistence.*;
import lombok.*;

//import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inventory_purchases")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryPurchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "purchase_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_request_id", nullable = false)
    private InventoryRequest inventoryRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accepted_by_user_id", nullable = false)
    private User acceptedByUser;

    @Column(nullable = false)
    private String division;

    @Column(name = "sub_division", nullable = false)
    private String subDivision;

    @Column(nullable = false)
    private String programme;

    @Column(nullable = false)
    private String project;

    @Column(nullable = false)
    private String object;

    @Column(name = "ref_no", nullable = false, unique = true)
    private String refNo; // Format: 2025.11.1.2.4.5

    @Column(nullable = false)
    private LocalDate date;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "purchase", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseItem> items = new ArrayList<>();

    @Column(nullable = false)
    private String payee;

    @Column(name = "prepared_by", nullable = false)
    private String preparedBy;

    @Column(name = "good_receiving_note_path")
    private String goodReceivingNotePath; // Path to the attachment file

    @Column(name = "shop_bill_path")
    private String shopBillPath; // Path to the attachment file

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper method to add items
    public void addItem(InventoryItem item, Double quantity) {
        PurchaseItem purchaseItem = new PurchaseItem(this, item, quantity);
        items.add(purchaseItem);
    }

    // Helper method to remove items
    public void removeItem(PurchaseItem purchaseItem) {
        items.remove(purchaseItem);
        purchaseItem.setPurchase(null);
    }
}

