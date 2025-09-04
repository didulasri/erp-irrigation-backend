package com.irrigation.erp.backend.service;

import com.irrigation.erp.backend.dto.*;
import com.irrigation.erp.backend.model.*;
import com.irrigation.erp.backend.repository.GoodsReceivingNoteRepository;
import com.irrigation.erp.backend.repository.InventoryItemRepository;
import com.irrigation.erp.backend.repository.InventoryRequestLineItemRepository; // ⬅ add this repo
import com.irrigation.erp.backend.repository.PurchaseRequestRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PurchaseRequestService {

    private final PurchaseRequestRepository purchaseRequestRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryRequestLineItemRepository inventoryRequestLineItemRepository;
    private final GoodsReceivingNoteRepository goodsReceivingNoteRepository;

    private static final BigDecimal DIRECT_PURCHASE_LIMIT = new BigDecimal("5000");

    @Autowired
    public PurchaseRequestService(
            PurchaseRequestRepository purchaseRequestRepository,
            InventoryItemRepository inventoryItemRepository,
            InventoryRequestLineItemRepository inventoryRequestLineItemRepository,
            GoodsReceivingNoteRepository goodsReceivingNoteRepository
    ) {
        this.purchaseRequestRepository = purchaseRequestRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.inventoryRequestLineItemRepository = inventoryRequestLineItemRepository;
        this.goodsReceivingNoteRepository = goodsReceivingNoteRepository;
    }

    @Transactional
    public PurchaseRequest createPurchaseRequest(PurchaseRequestCreateDTO requestDto) {
        PurchaseRequest purchaseRequest = new PurchaseRequest();

        // Map header
        purchaseRequest.setDivision(requestDto.getDivision());
        purchaseRequest.setSubDivision(requestDto.getSubDivision());
        purchaseRequest.setProgramme(requestDto.getProgramme());
        purchaseRequest.setProject(requestDto.getProject());
        purchaseRequest.setObject(requestDto.getObject());
        purchaseRequest.setRefNo(requestDto.getRefNo());
        purchaseRequest.setRequestedByUserId(requestDto.getRequestedByUserId());
        purchaseRequest.setRequestedAt(LocalDateTime.now());

        // Build line items from **REQUEST LINE ITEM ID** (not inventory item id)
        List<PurchaseRequestLineItem> items = requestDto.getItems().stream()
                .map(itemDto -> {

                    InventoryRequestLineItem reqLine = inventoryRequestLineItemRepository
                            .findById(itemDto.getInventoryRequestLineItemId())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Request line item with ID " + itemDto.getInventoryRequestLineItemId() + " not found."));

                    PurchaseRequestLineItem li = new PurchaseRequestLineItem();
                    li.setPurchaseRequest(purchaseRequest);
                    // keep the *request line item* id in your PR line item for traceability
                    li.setInventoryRequestLineItemId(reqLine.getId());

                    // trust client name or derive from request line item if you prefer
                    li.setItemName(itemDto.getItemName()); // or reqLine.getRequestedItemName()

                    li.setQuantity(itemDto.getQuantity());           // BigDecimal from DTO
                    li.setEstimatedPrice(itemDto.getEstimatedPrice());// BigDecimal from DTO
                    return li;
                })
                .collect(Collectors.toList());

        purchaseRequest.setItems(items);

        // Optionally flag the underlying inventory item as pending
        // Flag the underlying **request line item** as pending purchase
        for (PurchaseRequestLineItem li : items) {
            inventoryItemRepository.findById(li.getInventoryRequestLineItemId())
                    .ifPresent(inv -> {
                        if (!Boolean.TRUE.equals(inv.getPendingPurchaseRequest())) {
                            inv.setPendingPurchaseRequest(true);
                            inv.setLastUpdatedAt(LocalDateTime.now());
                            inventoryItemRepository.save(inv);
                        }
                    });
        }

        // Total value: sum of line estimatedPrice (if this is per-line total).
        // If estimatedPrice is a *unit* price, change to quantity.multiply(estimatedPrice).
        BigDecimal totalValue = items.stream()
                .map(PurchaseRequestLineItem::getEstimatedPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        purchaseRequest.setTotalValue(totalValue);

        purchaseRequest.setStatus(
                totalValue.compareTo(DIRECT_PURCHASE_LIMIT) <= 0
                        ? PurchaseRequest.Status.DIRECT_PURCHASE
                        : PurchaseRequest.Status.PENDING
        );

        return purchaseRequestRepository.save(purchaseRequest);
    }

    @Transactional
    public PurchaseRequest approvePurchaseRequest(Long requestId) {
        return purchaseRequestRepository.findById(requestId)
                .map(request -> {
                    if (request.getStatus() != PurchaseRequest.Status.PENDING) {
                        throw new IllegalStateException("Cannot approve a request that is not in PENDING status.");
                    }
                    request.setStatus(PurchaseRequest.Status.APPROVED);
                    return purchaseRequestRepository.save(request);
                })
                .orElseThrow(() -> new IllegalArgumentException("Purchase request with ID " + requestId + " not found."));
    }

    public List<PurchaseResponseDTO> getAllPurchaseRequests() {
        return purchaseRequestRepository.findAllPurchaseRequestsWithItemNames();
    }

    public PurchaseResponseFormDTO getPurchaseRequestById(Long id) {
        PurchaseRequest pr = purchaseRequestRepository.findByIdWithItems(id)
                .orElseThrow(() -> new IllegalArgumentException("Purchase request with ID " + id + " not found."));

        String requestedByName = purchaseRequestRepository.findUserFullNameById(pr.getRequestedByUserId());

        return new PurchaseResponseFormDTO(
                pr.getId(),
                pr.getRefNo(),
                requestedByName,
                pr.getRequestedAt(),
                pr.getTotalValue(),
                pr.getDivision(),
                pr.getSubDivision(),
                pr.getProgramme(),
                pr.getProject(),
                pr.getObject(),
                pr.getStatus(),
                pr.getItems().stream()
                        .map(li -> new PurchaseResponseFormDTO.PurchaseLineItemDTO(
                                li.getId(),
                                li.getInventoryRequestLineItemId(),
                                li.getItemName(),
                                li.getQuantity(),
                                li.getEstimatedPrice()
                        ))
                        .collect(Collectors.toList())
        );
    }

    @Transactional
    public GRN createGoodsReceivingNote(Long purchaseRequestId,
                                        CreateGrnRequest dto) {

        PurchaseRequest pr = purchaseRequestRepository.findById(purchaseRequestId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Purchase request with ID " + purchaseRequestId + " not found."));

        if (goodsReceivingNoteRepository.existsByReceiptNo(dto.getReceiptNo())) {
            throw new IllegalArgumentException("Receipt No already exists: " + dto.getReceiptNo());
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User must be authenticated to create GRN");
        }
        User currentUser = (User) authentication.getPrincipal();

        GRN grn = new GRN();
        grn.setReceiptNo(dto.getReceiptNo());
        grn.setReceivingStation(dto.getReceivingStation());
        grn.setReferenceOrderNo(dto.getReferenceOrderNo());
        grn.setReferenceOrderDate(dto.getReferenceOrderDate());
        grn.setIssuingOfficer(dto.getIssuingOfficer());
        grn.setStation(dto.getStation());
        grn.setCreatedBy(currentUser);
        grn.setPurchaseRequest(pr);
        grn.setItems(new ArrayList<>());


        for (GRNItemDTO row : dto.getItems()) {
            GoodsReceivingItem item = new GoodsReceivingItem();
            item.setDescription(row.getDescription());
            item.setQuantity(row.getQuantity());
            item.setUnit(row.getUnit());
            item.setGrn(grn);
            grn.addItem(item);
        }


        return goodsReceivingNoteRepository.save(grn);
    }

}
