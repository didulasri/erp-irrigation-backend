package com.irrigation.erp.backend.dto;// MaterialDistributionTableDTO.java
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MaterialDistributionTableDTO {
   private final BigDecimal prevMonthQty;
   private final BigDecimal thisMonthQty;
   private final BigDecimal totalQty;
   private final String itemName;
   private final Long itemId;

   public MaterialDistributionTableDTO(BigDecimal prevMonthQty,
                                       BigDecimal thisMonthQty,
                                       BigDecimal totalQty,
                                       String itemName,
                                       Long itemId) {
      this.prevMonthQty = prevMonthQty;
      this.thisMonthQty = thisMonthQty;
      this.totalQty = totalQty;
      this.itemName = itemName;
      this.itemId = itemId;
   }
   // getters...
}
