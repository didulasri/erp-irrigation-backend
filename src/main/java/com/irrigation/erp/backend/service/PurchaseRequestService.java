package com.irrigation.erp.backend.service;


import com.irrigation.erp.backend.dto.PurchaseRequestCreateDTO;
import com.irrigation.erp.backend.dto.PurchaseResponseDTO;
import com.irrigation.erp.backend.dto.PurchaseResponseFormDTO;
import com.irrigation.erp.backend.model.InventoryItem;
import com.irrigation.erp.backend.model.PurchaseRequest;
import com.irrigation.erp.backend.model.PurchaseRequestLineItem;
import com.irrigation.erp.backend.repository.InventoryItemRepository;
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
    private static final BigDecimal DIRECT_PURCHASE_LIMIT = new BigDecimal("5000");

    @Autowired
    public PurchaseRequestService(PurchaseRequestRepository purchaseRequestRepository,InventoryItemRepository inventoryItemRepository) {
        this.purchaseRequestRepository = purchaseRequestRepository;
        this.inventoryItemRepository = inventoryItemRepository;
    }

    @Transactional
    public PurchaseRequest createPurchaseRequest(PurchaseRequestCreateDTO requestDto) {
        PurchaseRequest purchaseRequest = new PurchaseRequest();

        // Map DTO fields to entity fields
        purchaseRequest.setDivision(requestDto.getDivision());
        purchaseRequest.setSubDivision(requestDto.getSubDivision());
        purchaseRequest.setProgramme(requestDto.getProgramme());
        purchaseRequest.setProject(requestDto.getProject());
        purchaseRequest.setObject(requestDto.getObject());
        purchaseRequest.setRefNo(requestDto.getRefNo());

        // Get the user ID from the DTO
        purchaseRequest.setRequestedByUserId(requestDto.getRequestedByUserId());
        purchaseRequest.setRequestedAt(LocalDateTime.now());

        // Map DTO line items to entity line items and fetch the InventoryItem
        List<PurchaseRequestLineItem> items = requestDto.getItems().stream()
                .map(itemDto -> {
                    // Fetch the InventoryItem entity to establish the relationship
                    InventoryItem inventoryItem = inventoryItemRepository.findById(itemDto.getInventoryRequestLineItemId())
                            .orElseThrow(() -> new IllegalArgumentException("Inventory item with ID " + itemDto.getInventoryRequestLineItemId() + " not found."));

                    PurchaseRequestLineItem item = new PurchaseRequestLineItem();
                    item.setInventoryRequestLineItemId(inventoryItem.getId()); // <-- Use the fetched entity
                    item.setItemName(itemDto.getItemName());
                    item.setQuantity(itemDto.getQuantity());
                    item.setEstimatedPrice(itemDto.getEstimatedPrice());
                    item.setPurchaseRequest(purchaseRequest);
                    return item;
                }).collect(Collectors.toList());

        purchaseRequest.setItems(items);

        // Update the pending status of the inventory items ---
        for (PurchaseRequestLineItem lineItem : items) {
            // Fetch the InventoryItem using the ID from the line item
            inventoryItemRepository.findById(lineItem.getInventoryRequestLineItemId()).ifPresent(inventoryItem -> {
                inventoryItem.setPendingPurchaseRequest(true);
                inventoryItemRepository.save(inventoryItem);
            });
        }


        // Calculate total value
        BigDecimal totalValue = items.stream()
                .map(PurchaseRequestLineItem::getEstimatedPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        purchaseRequest.setTotalValue(totalValue);

        // Apply business logic for status
        if (totalValue.compareTo(DIRECT_PURCHASE_LIMIT) <= 0) {
            purchaseRequest.setStatus(PurchaseRequest.Status.DIRECT_PURCHASE);
        } else {
            purchaseRequest.setStatus(PurchaseRequest.Status.PENDING);
        }

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
                    // You can add an approval date/user here if needed
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
                pr.getId(),                 // id
                pr.getRefNo(),              // refNo
                requestedByName,            // requestedByName
                pr.getRequestedAt(),        // requestedAt
                pr.getTotalValue(),         // totalValue
                pr.getDivision(),           // division
                pr.getSubDivision(),        // subDivision
                pr.getProgramme(),          // programme
                pr.getProject(),            // project
                pr.getObject(),             // object
                pr.getStatus(),             // status
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
