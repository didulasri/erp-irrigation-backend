package com.irrigation.erp.backend.repository;

import com.irrigation.erp.backend.dto.PurchaseResponseDTO;
import com.irrigation.erp.backend.model.PurchaseRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PurchaseRequestRepository extends JpaRepository<PurchaseRequest, Long> {

    @Query("""
                SELECT new com.irrigation.erp.backend.dto.PurchaseResponseDTO(
                    pr.id,
                    CONCAT(COALESCE(u.firstName, ''),
                           CASE WHEN u.firstName IS NOT NULL AND u.lastName IS NOT NULL THEN ' ' ELSE '' END,
                           COALESCE(u.lastName, '')),
                    pr.requestedAt,
                    CAST(function('string_agg', item.itemName, ', ') AS string)
                )
                FROM PurchaseRequest pr
                JOIN pr.items item
                JOIN User u ON u.id = pr.requestedByUserId
                GROUP BY pr.id, pr.requestedAt, u.firstName, u.lastName
            """)
    List<PurchaseResponseDTO> findAllPurchaseRequestsWithItemNames();
}
