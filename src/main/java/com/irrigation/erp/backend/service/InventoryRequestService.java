package com.irrigation.erp.backend.service;

import com.irrigation.erp.backend.dto.*;
import com.irrigation.erp.backend.enums.RequestLineItemStatus;
import com.irrigation.erp.backend.enums.RequestStatus;
import com.irrigation.erp.backend.model.*;
import com.irrigation.erp.backend.repository.*;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Past;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class InventoryRequestService {

    private final InventoryRequestRepository inventoryRequestRepository;
    private final InventoryRequestLineItemRepository inventoryRequestLineItemRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final UserRepository userRepository;
    private final InventoryIssueRepository inventoryIssueRepository;

    public InventoryRequestService(InventoryRequestRepository inventoryRequestRepository,
                                   InventoryRequestLineItemRepository inventoryRequestLineItemRepository,
                                   InventoryItemRepository inventoryItemRepository,
                                   UserRepository userRepository,
                                   InventoryIssueRepository inventoryIssueRepository) {
        this.inventoryRequestRepository = inventoryRequestRepository;
        this.inventoryRequestLineItemRepository = inventoryRequestLineItemRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.userRepository = userRepository;
        this.inventoryIssueRepository = inventoryIssueRepository;
    }

    @Transactional
    public InventoryRequest createInventoryRequest(InventoryRequestCreateDTO requestDTO) {
        User requester = userRepository.findById(requestDTO.getRequesterUserId())
                .orElseThrow(() -> new IllegalArgumentException("Requester user with ID '" + requestDTO.getRequesterUserId() + "' not found."));

        if (requestDTO.getItems() == null || requestDTO.getItems().isEmpty()) {
            throw new IllegalArgumentException("Request must contain at least one item.");
        }

        InventoryRequest newRequest = new InventoryRequest();
        newRequest.setRequester(requester);
        newRequest.setPurpose(requestDTO.getPurpose());
        newRequest.setStatus(RequestStatus.PENDING);
        newRequest.setRequestedAt(LocalDateTime.now());

        for (InventoryRequestLineItemCreateDTO lineItemDTO : requestDTO.getItems()) {
            InventoryItem requestedItem = inventoryItemRepository.findByItemCode(lineItemDTO.getItemCode())
                    .orElseThrow(() -> new IllegalArgumentException("Requested item with code '" + lineItemDTO.getItemCode() + "' not found."));

            if (lineItemDTO.getRequestedQuantity() <= 0) {
                throw new IllegalArgumentException("Requested quantity must be positive for item '" + lineItemDTO.getItemCode() + "'.");
            }

            InventoryRequestLineItem lineItem = new InventoryRequestLineItem();
            lineItem.setRequestedItem(requestedItem);
            lineItem.setRequestedQuantity(lineItemDTO.getRequestedQuantity());
            lineItem.setStatus(RequestLineItemStatus.PENDING);
            newRequest.addLineItem(lineItem);
        }

        return inventoryRequestRepository.save(newRequest);
    }

    public List<InventoryRequest> getAllPendingInventoryRequestsWithLineItems() {
        return inventoryRequestRepository.findByStatusWithLineItems(RequestStatus.PENDING);
    }

    public Optional<InventoryRequest> getInventoryRequestById(Long requestId) {
        return inventoryRequestRepository.findById(requestId);
    }

    @Transactional
    public InventoryIssue issueInventoryItem(Long inventoryRequestLineItemId, IssueRequestDTO issueDTO) {
        InventoryRequestLineItem requestLineItem = inventoryRequestLineItemRepository.findById(inventoryRequestLineItemId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory Request Line Item with ID '" + inventoryRequestLineItemId + "' not found."));

        if (requestLineItem.getStatus() == RequestLineItemStatus.ISSUED || requestLineItem.getStatus() == RequestLineItemStatus.NO_STOCK) {
            throw new IllegalArgumentException("This line item has already been fully processed and cannot be issued again.");
        }

        User storeKeeper = userRepository.findById(issueDTO.getIssuedByUserId())
                .orElseThrow(() -> new IllegalArgumentException("Store keeper user with ID '" + issueDTO.getIssuedByUserId() + "' not found."));

        InventoryItem itemToIssue = requestLineItem.getRequestedItem();
        Double requestedQuantity = requestLineItem.getRequestedQuantity();
        Double quantityToIssue = issueDTO.getIssuedQuantity();

        Double alreadyIssuedQuantity = inventoryIssueRepository.sumIssuedQuantityByRequestLineItemId(requestLineItem.getId());
        Double remainingQuantity = requestedQuantity - alreadyIssuedQuantity;

        if (quantityToIssue <= 0 || quantityToIssue > remainingQuantity) {
            throw new IllegalArgumentException("Issued quantity must be positive and not exceed the remaining requested quantity (" + remainingQuantity + ").");
        }

        if (itemToIssue.getCurrentStockQuantity() < quantityToIssue) {
            throw new IllegalArgumentException("Insufficient stock for item '" + itemToIssue.getItemCode() + "'. Available: " + itemToIssue.getCurrentStockQuantity() + ", Requested: " + quantityToIssue);
        }

        itemToIssue.setCurrentStockQuantity(itemToIssue.getCurrentStockQuantity() - quantityToIssue);
        itemToIssue.setLastUpdatedByUser(storeKeeper);
        itemToIssue.setLastUpdatedAt(LocalDateTime.now());
        inventoryItemRepository.save(itemToIssue);

        InventoryIssue issue = new InventoryIssue();
        issue.setRequestLineItem(requestLineItem);
        issue.setIssuedItem(itemToIssue);
        issue.setIssuedQuantity(quantityToIssue);
        issue.setIssuedByUser(storeKeeper);
        issue.setIssuedToUser(requestLineItem.getRequest().getRequester());
        issue.setIssuedAt(LocalDateTime.now());

        BigDecimal unitPrice = itemToIssue.getUnitPrice();
        BigDecimal issuedQuantityBd = BigDecimal.valueOf(quantityToIssue);
        BigDecimal calculatedItemValue = unitPrice.multiply(issuedQuantityBd);
        issue.setItemValue(calculatedItemValue);

        issue.setPurpose(requestLineItem.getRequest().getPurpose());
        issue.setNotes(issueDTO.getIssueNotes());

        inventoryIssueRepository.save(issue);

        if ((alreadyIssuedQuantity + quantityToIssue) >= requestedQuantity) {
            requestLineItem.setStatus(RequestLineItemStatus.ISSUED);
        } else {
            requestLineItem.setStatus(RequestLineItemStatus.ISSUED_PARTIALLY);
        }
        inventoryRequestLineItemRepository.save(requestLineItem);

        updateOverallRequestStatus(requestLineItem.getRequest());

        return issue;
    }

    //Branch Issue Methods
    @Transactional
    public InventoryRequest issueBatchItems(BatchIssueRequestDTO issueDTO) {
        if (issueDTO.getItemsToIssue() == null || issueDTO.getItemsToIssue().isEmpty()) {
            throw new IllegalArgumentException("Batch issue must contain at least one item.");
        }

        User storeKeeper = userRepository.findById(issueDTO.getIssuedByUserId())
                .orElseThrow(() -> new IllegalArgumentException("Store keeper user with ID '" + issueDTO.getIssuedByUserId() + "' not found."));

        // Retrieve all line items for the batch
        List<Long> lineItemIds = issueDTO.getItemsToIssue().stream()
                .map(BatchIssueLineItemDTO::getInventoryRequestLineItemId)
                .collect(Collectors.toList());

        List<InventoryRequestLineItem> lineItems = inventoryRequestLineItemRepository.findAllById(lineItemIds);

        if (lineItems.size() != lineItemIds.size()) {
            throw new IllegalArgumentException("One or more line items not found.");
        }


        // Ensure all line items belong to the same parent request
        Set<Long> parentRequestIds = lineItems.stream()
                .map(item -> item.getRequest().getId())
                .collect(Collectors.toSet());
        if (parentRequestIds.size() > 1) {
            throw new IllegalArgumentException("All items in a batch must belong to the same inventory request.");
        }

        // Ensure all items are of the same type (e.g., Material, Stationary)
        Set<ItemType> itemTypes = lineItems.stream()
                .map(item -> item.getRequestedItem().getItemType())
                .collect(Collectors.toSet());
        if (itemTypes.size() > 1) {
            throw new IllegalArgumentException("All items in a batch must be of the same type.");
        }

        // Get the parent request
        InventoryRequest parentRequest = lineItems.get(0).getRequest();

        // Process each line item individually within the batch transaction
        for (BatchIssueLineItemDTO itemToIssue : issueDTO.getItemsToIssue()) {

            InventoryRequestLineItem requestLineItem = lineItems.stream()
                    .filter(li -> li.getId().equals(itemToIssue.getInventoryRequestLineItemId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Line item not found in batch list."));

            Double requestedQuantity = requestLineItem.getRequestedQuantity();
            Double quantityToIssue = itemToIssue.getIssuedQuantity();

            Double alreadyIssuedQuantity = inventoryIssueRepository.sumIssuedQuantityByRequestLineItemId(requestLineItem.getId());
            Double remainingQuantity = requestedQuantity - alreadyIssuedQuantity;

            if (quantityToIssue <= 0 || quantityToIssue > remainingQuantity) {
                throw new IllegalArgumentException("Issued quantity for item '" + requestLineItem.getRequestedItem().getItemCode() + "' is invalid. Remaining: " + remainingQuantity);
            }

            InventoryItem inventoryItem = requestLineItem.getRequestedItem();
            if (inventoryItem.getCurrentStockQuantity() < quantityToIssue) {
                throw new IllegalArgumentException("Insufficient stock for item '" + inventoryItem.getItemCode() + "'. Available: " + inventoryItem.getCurrentStockQuantity());
            }

            // Deduct stock and create issue record
            inventoryItem.setCurrentStockQuantity(inventoryItem.getCurrentStockQuantity() - quantityToIssue);
            inventoryItemRepository.save(inventoryItem);

            InventoryIssue issue = new InventoryIssue();
            issue.setRequestLineItem(requestLineItem);
            issue.setIssuedItem(inventoryItem);
            issue.setIssuedQuantity(quantityToIssue);
            issue.setIssuedByUser(storeKeeper);
            issue.setIssuedToUser(parentRequest.getRequester());
            issue.setIssuedAt(LocalDateTime.now());

            BigDecimal unitPrice = inventoryItem.getUnitPrice();
            BigDecimal issuedQuantityBd = BigDecimal.valueOf(quantityToIssue);
            BigDecimal calculatedItemValue = unitPrice.multiply(issuedQuantityBd);
            issue.setItemValue(calculatedItemValue);

            issue.setPurpose(parentRequest.getPurpose());
            issue.setNotes(issueDTO.getIssueNotes());
            inventoryIssueRepository.save(issue);

            // Update line item status
            if ((alreadyIssuedQuantity + quantityToIssue) >= requestedQuantity) {
                requestLineItem.setStatus(RequestLineItemStatus.ISSUED);
            } else {
                requestLineItem.setStatus(RequestLineItemStatus.ISSUED_PARTIALLY);
            }
            inventoryRequestLineItemRepository.save(requestLineItem);
        }

        // Update the overall request status once for the entire batch
        updateOverallRequestStatus(parentRequest);

        return parentRequest;
    }


    @Transactional
    public InventoryRequestLineItem markRequestLineItemNoStock(Long inventoryRequestLineItemId, NoStockRequestDTO noStockDTO) {
        InventoryRequestLineItem requestLineItem = inventoryRequestLineItemRepository.findById(inventoryRequestLineItemId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory Request Line Item with ID '" + inventoryRequestLineItemId + "' not found."));

        if (requestLineItem.getStatus() != RequestLineItemStatus.PENDING) {
            throw new IllegalArgumentException("This line item has already been processed and cannot be marked as no stock.");
        }

        User storeKeeper = userRepository.findById(noStockDTO.getStoreKeeperUserId())
                .orElseThrow(() -> new IllegalArgumentException("Store keeper user with ID '" + noStockDTO.getStoreKeeperUserId() + "' not found."));

        requestLineItem.setStatus(RequestLineItemStatus.NO_STOCK);
        inventoryRequestLineItemRepository.save(requestLineItem);

        updateOverallRequestStatus(requestLineItem.getRequest());

        return requestLineItem;
    }

    private void updateOverallRequestStatus(InventoryRequest request) {
        Set<RequestLineItemStatus> lineItemStatuses = request.getLineItems().stream()
                .map(InventoryRequestLineItem::getStatus)
                .collect(Collectors.toSet());

        if (lineItemStatuses.contains(RequestLineItemStatus.PENDING) || lineItemStatuses.contains(RequestLineItemStatus.ISSUED_PARTIALLY)) {
            request.setStatus(RequestStatus.ISSUED_PARTIALLY);
        } else if (lineItemStatuses.contains(RequestLineItemStatus.ISSUED) && lineItemStatuses.size() == 1) {
            request.setStatus(RequestStatus.ISSUED);
        } else {
            request.setStatus(RequestStatus.REJECTED);
        }

        inventoryRequestRepository.save(request);
    }
}
