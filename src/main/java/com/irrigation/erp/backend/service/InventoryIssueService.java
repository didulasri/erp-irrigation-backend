package com.irrigation.erp.backend.service;

import com.irrigation.erp.backend.model.InventoryIssue;
import com.irrigation.erp.backend.model.InventoryItem;
import com.irrigation.erp.backend.repository.InventoryIssueRepository;
import com.irrigation.erp.backend.repository.InventoryItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryIssueService {

    private final InventoryIssueRepository inventoryIssueRepository;
    private final InventoryItemRepository inventoryItemRepository;

    public InventoryIssueService(InventoryIssueRepository inventoryIssueRepository,
                                 InventoryItemRepository inventoryItemRepository) {
        this.inventoryIssueRepository = inventoryIssueRepository;
        this.inventoryItemRepository = inventoryItemRepository;
    }

    public List<InventoryIssue> getIssueHistoryByItemId(Long itemId) {
        InventoryItem item = inventoryItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory item with ID " + itemId + " not found."));
        return inventoryIssueRepository.findByIssuedItemOrderByIssuedAtDesc(item);
    }

    public List<InventoryIssue> getIssueHistoryByItemCode(String itemCode) {
        InventoryItem item = inventoryItemRepository.findByItemCode(itemCode)
                .orElseThrow(() -> new IllegalArgumentException("Inventory item with code '" + itemCode + "' not found."));
        return inventoryIssueRepository.findByIssuedItemOrderByIssuedAtDesc(item);
    }

    public List<InventoryIssue> getAllIssues() {
        return inventoryIssueRepository.findAllByOrderByIssuedAtDesc();
    }

    public List<InventoryIssue> getIssuesByRequestId(Long requestId) {
        return inventoryIssueRepository.findByInventoryRequestIdOrderByIssuedAtDesc(requestId);
    }
}