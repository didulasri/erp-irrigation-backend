package com.irrigation.erp.backend.controller;


import com.irrigation.erp.backend.dto.*;
import com.irrigation.erp.backend.model.InventoryIssue;
import com.irrigation.erp.backend.model.InventoryRequest;
import com.irrigation.erp.backend.model.InventoryRequestLineItem;
import com.irrigation.erp.backend.service.InventoryRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/requests")
@CrossOrigin(origins = "http://localhost:5173")
public class InventoryRequestController {

    private final InventoryRequestService inventoryRequestService;

    public InventoryRequestController(InventoryRequestService inventoryRequestService) {
        this.inventoryRequestService = inventoryRequestService;
    }

    // --- DTO Conversion Helper Methods ---
    private InventoryRequestResponseDTO convertToRequestDto(InventoryRequest request) {
        InventoryRequestResponseDTO dto = new InventoryRequestResponseDTO();
        dto.setId(request.getId());
        dto.setPurpose(request.getPurpose());
        dto.setStatus(request.getStatus());
        dto.setRequestedAt(request.getRequestedAt());
        dto.setNotes(request.getNotes());

        if (request.getRequester() != null) {
            dto.setRequesterUserId(request.getRequester().getId());
            dto.setRequesterUsername(request.getRequester().getUsername());
        }
        if (request.getProcessedBy() != null) {
            dto.setProcessedByUserId(request.getProcessedBy().getId());
            dto.setProcessedByUsername(request.getProcessedBy().getUsername());
            dto.setProcessedAt(request.getProcessedAt());
        }

        if (request.getLineItems() != null) {
            dto.setItems(request.getLineItems().stream()
                    .map(this::convertToLineItemDto)
                    .collect(Collectors.toList()));
        } else {
            dto.setItems(List.of());
        }

        return dto;
    }

    private InventoryRequestLineItemResponseDTO convertToLineItemDto(InventoryRequestLineItem lineItem) {
        InventoryRequestLineItemResponseDTO dto = new InventoryRequestLineItemResponseDTO();
        dto.setId(lineItem.getId());
        dto.setRequestedQuantity(lineItem.getRequestedQuantity());
        dto.setStatus(lineItem.getStatus());

        if (lineItem.getRequestedItem() != null) {
            dto.setRequestedItemId(lineItem.getRequestedItem().getId());
            dto.setRequestedItemCode(lineItem.getRequestedItem().getItemCode());
            dto.setRequestedItemName(lineItem.getRequestedItem().getItemName());
            dto.setCurrentStockQuantity(lineItem.getRequestedItem().getCurrentStockQuantity());
        }
        return dto;
    }

    // --- Inventory Request Endpoints ---

    @PostMapping("/create")
    public ResponseEntity<InventoryRequestResponseDTO> createInventoryRequest(@Valid @RequestBody InventoryRequestCreateDTO requestDTO) {
        try {
            InventoryRequest newRequest = inventoryRequestService.createInventoryRequest(requestDTO);
            return new ResponseEntity<>(convertToRequestDto(newRequest), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<List<InventoryRequestResponseDTO>> getPendingInventoryRequests() {
        List<InventoryRequest> pendingRequests = inventoryRequestService.getAllPendingInventoryRequestsWithLineItems();
        List<InventoryRequestResponseDTO> dtos = pendingRequests.stream()
                .map(this::convertToRequestDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/issued")
    public ResponseEntity<List<InventoryRequestResponseDTO>> getIssuedInventoryRequests() {
        List<InventoryRequest> issuedRequests = inventoryRequestService.getAllIssuedInventoryRequestsWithLineItems();
        List<InventoryRequestResponseDTO> dtos = issuedRequests.stream()
                .map(this::convertToRequestDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }



    @PatchMapping("/line-items/{lineItemId}/issue")
    public ResponseEntity<InventoryRequestResponseDTO> issueRequestedItem(@PathVariable("lineItemId") Long inventoryRequestLineItemId, @Valid @RequestBody IssueRequestDTO issueDTO) {
        try {
            InventoryIssue issuedRecord = inventoryRequestService.issueInventoryItem(inventoryRequestLineItemId, issueDTO);
            return ResponseEntity.ok(convertToRequestDto(issuedRecord.getRequestLineItem().getRequest()));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PatchMapping("/line-items/{lineItemId}/no-stock")
    public ResponseEntity<InventoryRequestResponseDTO> markRequestLineItemNoStock(@PathVariable("lineItemId") Long inventoryRequestLineItemId, @Valid @RequestBody NoStockRequestDTO noStockDTO) {
        try {
            InventoryRequestLineItem updatedLineItem = inventoryRequestService.markRequestLineItemNoStock(inventoryRequestLineItemId, noStockDTO);
            return ResponseEntity.ok(convertToRequestDto(updatedLineItem.getRequest()));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<InventoryRequestResponseDTO> getInventoryRequest(@PathVariable Long requestId) {
        return inventoryRequestService.getInventoryRequestById(requestId)
                .map(this::convertToRequestDto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory request with ID '" + requestId + "' not found."));
    }

    @PostMapping("/issue-batch")
    public ResponseEntity<InventoryRequestResponseDTO> issueBatchRequestedItems(@Valid @RequestBody BatchIssueRequestDTO batchIssueDTO) {
        try {
            InventoryRequest updatedRequest = inventoryRequestService.issueBatchItems(batchIssueDTO);
            return ResponseEntity.ok(convertToRequestDto(updatedRequest));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/material-distribution")
    public ResponseEntity<List<MaterialDistributionTableDTO>> getMaterialDistributionTable(@RequestParam Long userId) {
        List<MaterialDistributionTableDTO> distribution = inventoryRequestService.getMaterialDistributionTable(userId);
        return ResponseEntity.ok(distribution);
    }


}
