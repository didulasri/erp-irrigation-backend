package com.irrigation.erp.backend.controller;


import com.irrigation.erp.backend.dto.CreateGrnRequest;
import com.irrigation.erp.backend.dto.GrnCheckResponseDTO;
import com.irrigation.erp.backend.model.GRN;
import com.irrigation.erp.backend.service.GrnService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/api/grn")
@CrossOrigin(origins = "http://localhost:5173")
public class GrnController {
    private final GrnService grnService;

    public GrnController(GrnService grnService) {
        this.grnService = grnService;
    }


    @PostMapping("/{purchaseRequestId}")
    public ResponseEntity<Long> createGrn(
            @PathVariable Long purchaseRequestId,
            @Valid @RequestBody CreateGrnRequest body,
            Authentication authentication
    ) {

        final Long userId = body.getCreatedBy();

        GRN saved = grnService.createGrn(purchaseRequestId, body, userId);
        return new ResponseEntity<>(saved.getId(), HttpStatus.CREATED);
    }


    @GetMapping("/check/{purchaseRequestId}")
    public ResponseEntity<GrnCheckResponseDTO> checkGrn(@PathVariable Long purchaseRequestId,@RequestParam Long userId


    ) {
        GrnCheckResponseDTO response = grnService.checkExistingGrn(purchaseRequestId, userId);
        return ResponseEntity.ok(response);
    }




}
