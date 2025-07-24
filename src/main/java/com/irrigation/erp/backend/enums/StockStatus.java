package com.irrigation.erp.backend.enums;

import lombok.Getter;

@Getter
public enum StockStatus {
    GOOD("Good Stock Level"),
    LOW("Low Stock Level - Reorder Soon"),
    OUT_OF_STOCK("Out of Stock");

    private final String description;

    StockStatus(String description) {
        this.description = description;
    }

}
