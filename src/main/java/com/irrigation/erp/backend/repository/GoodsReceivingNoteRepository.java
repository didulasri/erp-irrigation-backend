package com.irrigation.erp.backend.repository;

import com.irrigation.erp.backend.model.GRN;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GoodsReceivingNoteRepository extends JpaRepository<GRN ,Long> {
    boolean existsByReceiptNo(String receiptNo);

    List<GRN> findByPurchaseRequestId(Long purchaseRequestId);
}
