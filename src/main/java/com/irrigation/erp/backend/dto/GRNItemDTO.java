package com.irrigation.erp.backend.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GRNItemDTO {
    @NotBlank
    private String description;

    @NotNull
    @Min(1)
    private Integer quantity;

    private String unit;
}
