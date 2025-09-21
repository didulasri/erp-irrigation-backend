package com.irrigation.erp.backend.service;

import com.irrigation.erp.backend.dto.*;
import com.irrigation.erp.backend.model.*;
import com.irrigation.erp.backend.repository.GoodsReceivingNoteRepository;
import com.irrigation.erp.backend.repository.InventoryItemRepository;
import com.irrigation.erp.backend.repository.InventoryRequestLineItemRepository;
import com.irrigation.erp.backend.repository.PurchaseRequestRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PurchaseRequestService {

    private final PurchaseRequestRepository purchaseRequestRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryRequestLineItemRepository inventoryRequestLineItemRepository;
    public static final String ITEM_NOT_FOUND = "' not found.";


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


        List<PurchaseRequestLineItem> items = requestDto.getItems().stream()
                .map(itemDto -> {

                    InventoryRequestLineItem reqLine = inventoryRequestLineItemRepository
                            .findById(itemDto.getInventoryRequestLineItemId())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Request line item with ID " + itemDto.getInventoryRequestLineItemId() + ITEM_NOT_FOUND));

                    PurchaseRequestLineItem li = new PurchaseRequestLineItem();
                    li.setPurchaseRequest(purchaseRequest);

                    li.setInventoryRequestLineItemId(reqLine.getId());


                    li.setItemName(itemDto.getItemName());

                    li.setQuantity(itemDto.getQuantity());
                    li.setEstimatedPrice(itemDto.getEstimatedPrice());
                    return li;
                })
                .collect(Collectors.toList());

        purchaseRequest.setItems(items);


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
                .orElseThrow(() -> new IllegalArgumentException("Purchase request with ID " + requestId + ITEM_NOT_FOUND));
    }

    public List<PurchaseResponseDTO> getAllPurchaseRequests() {
        return purchaseRequestRepository.findAllPurchaseRequestsWithItemNames();
    }

    public PurchaseResponseFormDTO getPurchaseRequestById(Long id) {
        PurchaseRequest pr = purchaseRequestRepository.findByIdWithItems(id)
                .orElseThrow(() -> new IllegalArgumentException("Purchase request with ID " + id + ITEM_NOT_FOUND));

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



}
