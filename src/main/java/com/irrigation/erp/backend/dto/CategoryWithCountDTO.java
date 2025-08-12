package com.irrigation.erp.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryWithCountDTO {
    private Long id;
    private String name;
    private String description;
    private Long itemCount;
    private Long lowStockCount;
}