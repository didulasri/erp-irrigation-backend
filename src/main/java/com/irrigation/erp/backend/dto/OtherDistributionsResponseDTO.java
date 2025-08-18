package com.irrigation.erp.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtherDistributionsResponseDTO {
    private List<String> itemHeaders;
    private List<DistributionRecord> distributions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DistributionRecord {
        private LocalDate date;
        private String issueNumber;
        private Map<String, Integer> itemQuantities; // Item name -> quantity
    }
}