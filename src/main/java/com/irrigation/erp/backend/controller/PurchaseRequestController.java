package com.irrigation.erp.backend.controller;


import com.irrigation.erp.backend.dto.PurchaseRequestCreateDTO;
import com.irrigation.erp.backend.dto.PurchaseResponseDTO;
import com.irrigation.erp.backend.dto.PurchaseResponseFormDTO;
import com.irrigation.erp.backend.model.PurchaseRequest;
import com.irrigation.erp.backend.service.PurchaseRequestService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchase-requests")
@CrossOrigin(origins = "http://localhost:5173")
public class PurchaseRequestController {

    private final PurchaseRequestService purchaseRequestService;

    @Autowired
    public PurchaseRequestController(PurchaseRequestService purchaseRequestService) {
        this.purchaseRequestService = purchaseRequestService;
    }

    @PostMapping
    public ResponseEntity<PurchaseRequest> createPurchaseRequest(@Valid @RequestBody PurchaseRequestCreateDTO requestDto) {
        try {
            PurchaseRequest newRequest = purchaseRequestService.createPurchaseRequest(requestDto);
            return new ResponseEntity<>(newRequest, HttpStatus.CREATED);
        } catch (Exception e) {
            // A more robust error handling mechanism is recommended for a production app
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<PurchaseRequest> approvePurchaseRequest(@PathVariable Long id) {
        try {
            PurchaseRequest approvedRequest = purchaseRequestService.approvePurchaseRequest(id);
            return ResponseEntity.ok(approvedRequest);
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Catch specific exceptions to return more meaningful error codes
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping("/get-purchase-requests")
    public ResponseEntity<List<PurchaseResponseDTO>> getAllPurchaseRequests() {
        try {
            List<PurchaseResponseDTO> requests = purchaseRequestService.getAllPurchaseRequests();
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/purchase-requests-Form/{id}")
    public ResponseEntity<PurchaseResponseFormDTO> getFormPurchaseRequests(@PathVariable Long id) {
        try {
            PurchaseResponseFormDTO request = purchaseRequestService.getPurchaseRequestById(id);
            return ResponseEntity.ok(request);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}

