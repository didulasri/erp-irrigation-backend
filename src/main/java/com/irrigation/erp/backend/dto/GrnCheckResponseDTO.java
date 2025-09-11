package com.irrigation.erp.backend.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrnCheckResponseDTO {
    private boolean exists;
    private CreateGrnRequest grn;
}
