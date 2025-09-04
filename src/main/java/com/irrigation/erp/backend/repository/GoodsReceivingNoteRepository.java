package com.irrigation.erp.backend.repository;

import com.irrigation.erp.backend.model.GRN;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoodsReceivingNoteRepository extends JpaRepository<GRN ,Long> {
    boolean existsByReceiptNo(String receiptNo);
}
