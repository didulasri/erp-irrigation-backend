package com.irrigation.erp.backend.model;


import com.irrigation.erp.backend.enums.StockStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.configurationprocessor.metadata.ItemMetadata;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String itemCode;

    @Column(nullable = false)
    private String itemName;

    @Column(columnDefinition = "TEXT")
    private String itemDescription;

    @Column(name = "unit_of_measurement" ,nullable = false)
    private String unitOfMeasurement;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_category_id", nullable = false)
    private ItemCategory itemCategory;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_type_id",nullable = false)
    private ItemType itemType;

    @Column(name = "current_stock_quantity", nullable = false)
    private Double currentStockQuantity;

    @Column(name = "minimum_stock_level", nullable = false)
    private Double minimumStockLevel;

    @Column(name = "location_in_store")
    private String locationInStore;

    @Column(name = "unit_price",nullable = false,precision = 10,scale = 2)
    private BigDecimal unitPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_updated_by_user_id")
    private User lastUpdatedByUser;

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;


    @Transient
    private StockStatus stockStatus;

    public StockStatus getStockStatus() {
        if(this.currentStockQuantity <= 0){
            return StockStatus.OUT_OF_STOCK;
        } else if (this.currentStockQuantity <= this.minimumStockLevel) {
            return StockStatus.LOW;
        }else{
            return StockStatus.GOOD;

        }
    }
}
