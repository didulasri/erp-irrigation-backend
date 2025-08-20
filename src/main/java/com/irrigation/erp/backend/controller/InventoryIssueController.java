package com.irrigation.erp.backend.controller;

import com.irrigation.erp.backend.dto.InventoryIssueResponseDTO;
import com.irrigation.erp.backend.dto.OtherDistributionsResponseDTO;
import com.irrigation.erp.backend.model.InventoryIssue;
import com.irrigation.erp.backend.service.InventoryIssueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inventory/issues")
@CrossOrigin(origins = "http://localhost:5173")
public class InventoryIssueController {

    private final InventoryIssueService inventoryIssueService;

    public InventoryIssueController(InventoryIssueService inventoryIssueService) {
        this.inventoryIssueService = inventoryIssueService;
    }

    private InventoryIssueResponseDTO convertToDto(InventoryIssue inventoryIssue) {
        InventoryIssueResponseDTO dto = new InventoryIssueResponseDTO();
        dto.setId(inventoryIssue.getId());
        dto.setIssuedQuantity(inventoryIssue.getIssuedQuantity());
        dto.setIssuedAt(inventoryIssue.getIssuedAt());
        dto.setItemValue(inventoryIssue.getItemValue());
        dto.setPurpose(inventoryIssue.getPurpose());
        dto.setNotes(inventoryIssue.getNotes());

        // Set issued item details
        if (inventoryIssue.getIssuedItem() != null) {
            dto.setIssuedItemId(inventoryIssue.getIssuedItem().getId());
            dto.setIssuedItemCode(inventoryIssue.getIssuedItem().getItemCode());
            dto.setIssuedItemName(inventoryIssue.getIssuedItem().getItemName());
        }

        // Set user details
        if (inventoryIssue.getIssuedByUser() != null) {
            dto.setIssuedByUserId(inventoryIssue.getIssuedByUser().getId());
            dto.setIssuedByUsername(inventoryIssue.getIssuedByUser().getUsername());
        }

        if (inventoryIssue.getIssuedToUser() != null) {
            dto.setIssuedToUserId(inventoryIssue.getIssuedToUser().getId());
            dto.setIssuedToUsername(inventoryIssue.getIssuedToUser().getUsername());
        }

        // Set request details
        if (inventoryIssue.getInventoryRequest() != null) {
            dto.setInventoryRequestId(inventoryIssue.getInventoryRequest().getId());
            // Use the ID as request code for now, or check if your InventoryRequest has a different field
            dto.setRequestCode("REQ-" + inventoryIssue.getInventoryRequest().getId());
        }

        if (inventoryIssue.getRequestLineItem() != null) {
            dto.setRequestLineItemId(inventoryIssue.getRequestLineItem().getId());
            dto.setRequestedQuantity(inventoryIssue.getRequestLineItem().getRequestedQuantity());
        }

        return dto;
    }

    @GetMapping("/all")
    public ResponseEntity<List<InventoryIssueResponseDTO>> getAllIssues() {
        try {
            List<InventoryIssue> issues = inventoryIssueService.getAllIssues();
            List<InventoryIssueResponseDTO> dtos = issues.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching all issues: " + e.getMessage());
        }
    }

    @GetMapping("/item/{itemId}")
    public ResponseEntity<List<InventoryIssueResponseDTO>> getIssueHistoryByItemId(@PathVariable Long itemId) {
        try {
            List<InventoryIssue> issues = inventoryIssueService.getIssueHistoryByItemId(itemId);
            List<InventoryIssueResponseDTO> dtos = issues.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching issues by item ID: " + e.getMessage());
        }
    }

    @GetMapping("/item/code/{itemCode}")
    public ResponseEntity<List<InventoryIssueResponseDTO>> getIssueHistoryByItemCode(@PathVariable String itemCode) {
        try {
            List<InventoryIssue> issues = inventoryIssueService.getIssueHistoryByItemCode(itemCode);
            List<InventoryIssueResponseDTO> dtos = issues.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching issues by item code: " + e.getMessage());
        }
    }

    @GetMapping("/request/{requestId}")
    public ResponseEntity<List<InventoryIssueResponseDTO>> getIssuesByRequestId(@PathVariable Long requestId) {
        try {
            List<InventoryIssue> issues = inventoryIssueService.getIssuesByRequestId(requestId);
            List<InventoryIssueResponseDTO> dtos = issues.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching issues by request ID: " + e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
public ResponseEntity<List<InventoryIssueResponseDTO>> getIssuesByUserId(@PathVariable Long userId) {
    try {
        List<InventoryIssue> issues = inventoryIssueService.getIssuesByUserId(userId);
        List<InventoryIssueResponseDTO> dtos = issues.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    } catch (IllegalArgumentException e) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
    } catch (Exception e) {
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching issues by user ID: " + e.getMessage());
    }
}


    @GetMapping("/user/{userId}/other-distributions")
    public ResponseEntity<OtherDistributionsResponseDTO> getOtherDistributionsByUserId(@PathVariable Long userId) {
        try {
            OtherDistributionsResponseDTO distributions = inventoryIssueService.getOtherDistributionsByUserId(userId);
            return ResponseEntity.ok(distributions);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching distributions: " + e.getMessage());
        }
    }
}