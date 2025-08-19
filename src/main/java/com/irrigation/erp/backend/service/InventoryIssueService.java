package com.irrigation.erp.backend.service;

import com.irrigation.erp.backend.dto.OtherDistributionsResponseDTO;
import com.irrigation.erp.backend.model.InventoryIssue;
import com.irrigation.erp.backend.model.InventoryItem;
import com.irrigation.erp.backend.repository.InventoryIssueRepository;
import com.irrigation.erp.backend.repository.InventoryItemRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InventoryIssueService {

    private final InventoryIssueRepository inventoryIssueRepository;
    private final InventoryItemRepository inventoryItemRepository;

    public InventoryIssueService(InventoryIssueRepository inventoryIssueRepository,
                                 InventoryItemRepository inventoryItemRepository) {
        this.inventoryIssueRepository = inventoryIssueRepository;
        this.inventoryItemRepository = inventoryItemRepository;
    }

    // Existing methods
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

    // NEW METHOD: Get issues by user ID (issued to user)
    public List<InventoryIssue> getIssuesByUserId(Long userId) {
        return inventoryIssueRepository.findByIssuedToUserIdOrderByIssuedAtDesc(userId);
    }

    // ALTERNATIVE METHOD: Get issues by user ID (issued by user)
    public List<InventoryIssue> getIssuesByIssuedByUserId(Long userId) {
        return inventoryIssueRepository.findByIssuedByUserIdOrderByIssuedAtDesc(userId);
    }

    // COMBINED METHOD: Get all issues related to a user (both issued to and issued by)
    public List<InventoryIssue> getAllIssuesRelatedToUser(Long userId) {
        List<InventoryIssue> issuedToUser = inventoryIssueRepository.findByIssuedToUserIdOrderByIssuedAtDesc(userId);
        List<InventoryIssue> issuedByUser = inventoryIssueRepository.findByIssuedByUserIdOrderByIssuedAtDesc(userId);

        // Combine and remove duplicates
        Set<InventoryIssue> combinedIssues = new HashSet<>(issuedToUser);
        combinedIssues.addAll(issuedByUser);

        // Convert back to list and sort by issued date (descending)
        return combinedIssues.stream()
                .sorted((a, b) -> b.getIssuedAt().compareTo(a.getIssuedAt()))
                .collect(Collectors.toList());
    }

    // NEW METHOD: Get other distributions for a specific user
    public OtherDistributionsResponseDTO getOtherDistributionsByUserId(Long userId) {
        // Get all Non-Material issues for the user
        List<InventoryIssue> issues = inventoryIssueRepository.findNonMaterialIssuesByIssuedToUserId(userId);

        // Get all distinct Non-Material item names
        List<String> itemHeaders = inventoryIssueRepository.findDistinctNonMaterialItemNames();

        // Group issues by date and request ID to create distribution records
        Map<String, List<InventoryIssue>> groupedByDateAndRequest = issues.stream()
                .collect(Collectors.groupingBy(issue ->
                        issue.getIssuedAt().toLocalDate().toString() + "_" + issue.getInventoryRequest().getId()
                ));

        // Convert to distribution records
        List<OtherDistributionsResponseDTO.DistributionRecord> distributions = new ArrayList<>();

        for (Map.Entry<String, List<InventoryIssue>> entry : groupedByDateAndRequest.entrySet()) {
            List<InventoryIssue> issuesGroup = entry.getValue();
            if (!issuesGroup.isEmpty()) {
                InventoryIssue firstIssue = issuesGroup.get(0);

                // Create distribution record
                OtherDistributionsResponseDTO.DistributionRecord record =
                        new OtherDistributionsResponseDTO.DistributionRecord();

                record.setDate(firstIssue.getIssuedAt().toLocalDate());
                record.setIssueNumber("REQ-" + firstIssue.getInventoryRequest().getId());

                // Create item quantities map
                Map<String, Integer> itemQuantities = new HashMap<>();

                // Initialize all items with 0
                for (String itemName : itemHeaders) {
                    itemQuantities.put(itemName, 0);
                }

                // Set actual quantities
                for (InventoryIssue issue : issuesGroup) {
                    String itemName = issue.getIssuedItem().getItemName();
                    int currentQuantity = itemQuantities.getOrDefault(itemName, 0);
                    int issuedQuantity = issue.getIssuedQuantity().intValue();
                    itemQuantities.put(itemName, currentQuantity + issuedQuantity);
                }

                record.setItemQuantities(itemQuantities);
                distributions.add(record);
            }
        }

        // Sort distributions by date (most recent first)
        distributions.sort((a, b) -> b.getDate().compareTo(a.getDate()));

        return new OtherDistributionsResponseDTO(itemHeaders, distributions);
    }
}