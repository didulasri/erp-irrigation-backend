package com.irrigation.erp.backend.repository;

import com.irrigation.erp.backend.model.InventoryIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryIssueRepository extends JpaRepository<InventoryIssue, Long> {
    List<InventoryIssue> findByRequestId(Long requestId);
}
