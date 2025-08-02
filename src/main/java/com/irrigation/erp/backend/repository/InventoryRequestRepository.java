package com.irrigation.erp.backend.repository;

import com.irrigation.erp.backend.enums.RequestStatus;
import com.irrigation.erp.backend.model.InventoryRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRequestRepository extends JpaRepository<InventoryRequest, Long> {
    List<InventoryRequest>findByStatus(RequestStatus status);
}
