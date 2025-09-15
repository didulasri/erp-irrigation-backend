package com.irrigation.erp.backend.service;

import com.irrigation.erp.backend.dto.CreateGrnRequest;
import com.irrigation.erp.backend.dto.GRNItemDTO;
import com.irrigation.erp.backend.dto.GrnCheckResponseDTO;
import com.irrigation.erp.backend.model.GRN;
import com.irrigation.erp.backend.model.GoodsReceivingItem;
import com.irrigation.erp.backend.model.PurchaseRequest;
import com.irrigation.erp.backend.model.User;
import com.irrigation.erp.backend.repository.GoodsReceivingNoteRepository;
import com.irrigation.erp.backend.repository.PurchaseRequestRepository;
import com.irrigation.erp.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

import java.util.List;

@Service
public class GrnService {
    private final GoodsReceivingNoteRepository grnRepository;
    private final PurchaseRequestRepository purchaseRequestRepository;
    private final UserRepository userRepository; // <-- adapt to your project

    public GrnService(GoodsReceivingNoteRepository grnRepository,
                      PurchaseRequestRepository purchaseRequestRepository,
                      UserRepository userRepository) {
        this.grnRepository = grnRepository;
        this.purchaseRequestRepository = purchaseRequestRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public GRN createGrn(Long purchaseRequestId, CreateGrnRequest dto, Long userId) {
        // Get the User by userId
        User createdBy = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // Get the PurchaseRequest by purchaseRequestId
        PurchaseRequest pr = purchaseRequestRepository.findById(purchaseRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Purchase request not found: " + purchaseRequestId));

        // Create the GRN
        GRN grn = new GRN();
        grn.setReceiptNo(dto.getReceiptNo());
        grn.setReceivingStation(dto.getReceivingStation());
        grn.setReferenceOrderNo(dto.getReferenceOrderNo());

        // Handle reference date
        if (dto.getReferenceOrderDate() != null) {
            grn.setReferenceOrderDate(LocalDate.from(dto.getReferenceOrderDate()));
        }

        grn.setIssuingOfficer(dto.getIssuingOfficer());
        grn.setStation(dto.getStation());
        grn.setPurchaseRequest(pr); // Associate the GRN with the PurchaseRequest
        grn.setCreatedBy(createdBy); // Associate the GRN with the user

        // Add items to GRN using addItem method
        for (GRNItemDTO row : dto.getItems()) {
            GoodsReceivingItem item = new GoodsReceivingItem();
            item.setDescription(row.getDescription());
            item.setQuantity(row.getQuantity());
            item.setUnit(row.getUnit());
            grn.addItem(item); // Using addItem ensures the proper association
        }

        // Save the GRN
        return grnRepository.save(grn);
    }



    public GrnCheckResponseDTO checkExistingGrn(Long purchaseRequestId, Long userId) {
        // Find GRNs by purchaseRequestId using the repository
        List<GRN> grns = grnRepository.findByPurchaseRequestId(purchaseRequestId);

        // Filter by the userId (only GRNs created by this user)
        grns = grns.stream()
                .filter(grn -> grn.getCreatedBy() != null && grn.getCreatedBy().getId().equals(userId))
                .toList();

        if (!grns.isEmpty()) {
            GRN grn = grns.get(0);

            // Prepare the CreateGrnRequest details
            CreateGrnRequest dto = new CreateGrnRequest();
            dto.setReceiptNo(grn.getReceiptNo());
            dto.setReceivingStation(grn.getReceivingStation());
            dto.setReferenceOrderNo(grn.getReferenceOrderNo());
            dto.setReferenceOrderDate(grn.getReferenceOrderDate());
            dto.setIssuingOfficer(grn.getIssuingOfficer());
            dto.setStation(grn.getStation());


            return new GrnCheckResponseDTO(true, dto);
        }

        // If no GRN found, return response indicating GRN doesn't exist
        return new GrnCheckResponseDTO(false, null);
    }








}