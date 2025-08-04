package com.irrigation.erp.backend.repository;

import com.irrigation.erp.backend.enums.RequestStatus;
import com.irrigation.erp.backend.model.InventoryRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRequestRepository extends JpaRepository<InventoryRequest, Long> {
    @Query("SELECT r FROM InventoryRequest r JOIN FETCH r.lineItems WHERE r.status = :status")
    List<InventoryRequest> findByStatusWithLineItems(@Param("status") RequestStatus status);

    List<InventoryRequest> findByStatus(RequestStatus requestStatus);
}
