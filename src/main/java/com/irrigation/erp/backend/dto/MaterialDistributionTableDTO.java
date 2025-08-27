package com.irrigation.erp.backend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MaterialDistributionTableDTO {
   private final BigDecimal prevMonthQty;
   private final BigDecimal thisMonthQty;
   private final BigDecimal totalQty;
   private final String itemName;
   private final Long itemId;
   private final LocalDate referenceDate;

   // Constructor for your query (without date)
   public MaterialDistributionTableDTO(BigDecimal prevMonthQty,
                                       BigDecimal thisMonthQty,
                                       BigDecimal totalQty,
                                       String itemName,
                                       Long itemId) {
      this(prevMonthQty, thisMonthQty, totalQty, itemName, itemId, null);
   }

   // Constructor with date
   public MaterialDistributionTableDTO(BigDecimal prevMonthQty,
                                       BigDecimal thisMonthQty,
                                       BigDecimal totalQty,
                                       String itemName,
                                       Long itemId,
                                       LocalDate referenceDate) {
      this.prevMonthQty = prevMonthQty;
      this.thisMonthQty = thisMonthQty;
      this.totalQty = totalQty;
      this.itemName = itemName;
      this.itemId = itemId;
      this.referenceDate = referenceDate;
   }
}