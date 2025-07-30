package com.irrigation.erp.backend.controller;


import com.irrigation.erp.backend.dto.InventoryRequestCreateDTO;
import com.irrigation.erp.backend.dto.InventoryRequestResponseDTO;
import com.irrigation.erp.backend.dto.IssueRequestDTO;
import com.irrigation.erp.backend.dto.NoStockRequestDTO;
import com.irrigation.erp.backend.model.InventoryIssue;
import com.irrigation.erp.backend.model.InventoryRequest;
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
public class InventoryRequestController {

    private final InventoryRequestService inventoryRequestService;

    public InventoryRequestController(InventoryRequestService inventoryRequestService) {
        this.inventoryRequestService = inventoryRequestService;
    }

    //convert to request Dto
    private InventoryRequestResponseDTO convertToRequestDto(InventoryRequest request) {
        InventoryRequestResponseDTO dto = new InventoryRequestResponseDTO();
        dto.setId(request.getId());
        dto.setRequestedQuantity(request.getRequestedQuantity());
        dto.setPurpose(request.getPurpose());
        dto.setStatus(request.getStatus());
        dto.setRequestedAt(request.getRequestedAt());


        if (request.getRequester() != null) {
            dto.setRequesterUserId(request.getRequester().getId());
            dto.setRequesterUsername(request.getRequester().getUsername());
        }
        if (request.getRequestedItem() != null) {
            dto.setRequestedItemId(request.getRequestedItem().getId());
            dto.setRequestedItemCode(request.getRequestedItem().getItemCode());
            dto.setRequestedItemName(request.getRequestedItem().getItemName());
        }
        if (request.getProcessedBy() != null) {
            dto.setProcessedByUserId(request.getProcessedBy().getId());
            dto.setProcessedByUsername(request.getProcessedBy().getUsername());
            dto.setProcessedAt(request.getProcessedAt());
        }
        return dto;
    }

    // --- Inventory Request Endpoints ---

    @PostMapping("/create")
    public ResponseEntity<InventoryRequestResponseDTO>createInventoryRequest (@Valid @RequestBody InventoryRequestCreateDTO requestDTO){
        try {
            InventoryRequest newRequest = inventoryRequestService.createInventoryRequest(requestDTO);
            return new ResponseEntity<>(convertToRequestDto(newRequest), HttpStatus.CREATED);
        }catch(IllegalArgumentException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());

        }
    }

    @GetMapping("/pending")
    public ResponseEntity<List<InventoryRequestResponseDTO>> getPendingInventoryRequests() {
        List<InventoryRequest> pendingRequests = inventoryRequestService.getAllPendingInventoryRequests();
        List<InventoryRequestResponseDTO> dtos = pendingRequests.stream()
                .map(this::convertToRequestDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }


    @PatchMapping("/{requestId}/issue")
    public ResponseEntity<InventoryRequestResponseDTO> issueRequestedItem(@PathVariable Long requestId, @Valid @RequestBody IssueRequestDTO issueDTO) {
        try {
            InventoryIssue issuedRecord = inventoryRequestService.issueInventoryItem(requestId, issueDTO);
            return ResponseEntity.ok(convertToRequestDto(issuedRecord.getRequest()));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PatchMapping("/{requestId}/no-stock")
    public ResponseEntity<InventoryRequestResponseDTO> markRequestNoStock(@PathVariable Long requestId, @Valid @RequestBody NoStockRequestDTO noStockDTO) {
        try {
            InventoryRequest updatedRequest = inventoryRequestService.markRequestNoStock(requestId, noStockDTO);
            return ResponseEntity.ok(convertToRequestDto(updatedRequest));
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



}
