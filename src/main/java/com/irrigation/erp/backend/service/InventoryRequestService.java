package com.irrigation.erp.backend.service;

import com.irrigation.erp.backend.dto.*;
import com.irrigation.erp.backend.enums.RequestLineItemStatus;
import com.irrigation.erp.backend.enums.RequestStatus;
import com.irrigation.erp.backend.model.*;
import com.irrigation.erp.backend.repository.*;
import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    public static final String NOT_FOUND = "' not found.";
    public static final String INVENTORY_REQUEST_LINE_ITEM_WITH_ID = "Inventory Request Line Item with ID ";
    public static final String STORE_KEEPER_USER_WITH_ID = "Store keeper user with ID '";

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
                .orElseThrow(() -> new IllegalArgumentException("Requester user with ID '" + requestDTO.getRequesterUserId() + NOT_FOUND));

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
                    .orElseThrow(() -> new IllegalArgumentException("Requested item with code '" + lineItemDTO.getItemCode() + NOT_FOUND));

            if (lineItemDTO.getRequestedQuantity().compareTo(BigDecimal.ZERO) <= 0) {
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
        return inventoryRequestRepository.findByStatus(RequestStatus.PENDING);
    }

    public List<InventoryRequest> getAllIssuedInventoryRequestsWithLineItems() {
        return inventoryRequestRepository.findByStatus(RequestStatus.ISSUED);
    }

    public Optional<InventoryRequest> getInventoryRequestById(Long requestId) {
        return inventoryRequestRepository.findById(requestId);
    }

    @Transactional
    public InventoryIssue issueInventoryItem(Long inventoryRequestLineItemId, IssueRequestDTO issueDTO) {
        InventoryRequestLineItem requestLineItem = inventoryRequestLineItemRepository.findById(inventoryRequestLineItemId)
                .orElseThrow(() -> new IllegalArgumentException(INVENTORY_REQUEST_LINE_ITEM_WITH_ID + inventoryRequestLineItemId + NOT_FOUND));

        if (requestLineItem.getStatus() == RequestLineItemStatus.ISSUED || requestLineItem.getStatus() == RequestLineItemStatus.NO_STOCK) {
            throw new IllegalArgumentException("This line item has already been fully processed and cannot be issued again.");
        }

        User storeKeeper = userRepository.findById(issueDTO.getIssuedByUserId())
                .orElseThrow(() -> new IllegalArgumentException(STORE_KEEPER_USER_WITH_ID + issueDTO.getIssuedByUserId() + NOT_FOUND));

        InventoryItem itemToIssue = requestLineItem.getRequestedItem();
        BigDecimal requestedQuantity = requestLineItem.getRequestedQuantity();
        BigDecimal quantityToIssue = issueDTO.getIssuedQuantity();


        BigDecimal alreadyIssuedQuantity = BigDecimal.valueOf(inventoryIssueRepository.sumIssuedQuantityByRequestLineItemId(requestLineItem.getId()));
        BigDecimal remainingQuantity = requestedQuantity.subtract(alreadyIssuedQuantity);


        if (quantityToIssue.compareTo(BigDecimal.ZERO) <= 0 || quantityToIssue.compareTo(remainingQuantity) > 0) {
            throw new IllegalArgumentException("Issued quantity must be positive and not exceed the remaining requested quantity (" + remainingQuantity + ").");
        }

        if (itemToIssue.getCurrentStockQuantity().compareTo(quantityToIssue) < 0) {
            throw new IllegalArgumentException("Insufficient stock for item '" + itemToIssue.getItemCode() + "'. Available: " + itemToIssue.getCurrentStockQuantity() + ", Requested: " + quantityToIssue);
        }

        itemToIssue.setCurrentStockQuantity(itemToIssue.getCurrentStockQuantity().subtract(quantityToIssue));
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
        BigDecimal calculatedItemValue = unitPrice.multiply(quantityToIssue);
        issue.setItemValue(calculatedItemValue);

        issue.setPurpose(requestLineItem.getRequest().getPurpose());
        issue.setNotes(issueDTO.getIssueNotes());

        inventoryIssueRepository.save(issue);


        if ((alreadyIssuedQuantity.add(quantityToIssue)).compareTo(requestedQuantity) >= 0) {
            requestLineItem.setStatus(RequestLineItemStatus.ISSUED);
        } else {
            requestLineItem.setStatus(RequestLineItemStatus.ISSUED_PARTIALLY);
        }
        inventoryRequestLineItemRepository.save(requestLineItem);

        updateOverallRequestStatus(requestLineItem.getRequest(), storeKeeper);

        return issue;
    }
    //Branch Issue Methods
    @Transactional
    public InventoryRequest issueBatchItems(BatchIssueRequestDTO issueDTO) {

        if (issueDTO.getItemsToIssue() == null || issueDTO.getItemsToIssue().isEmpty()) {
            throw new IllegalArgumentException("Batch of items to issue cannot be empty.");
        }


        // This is now safe as we've checked for an empty list
        Long parentRequestId = issueDTO.getItemsToIssue().get(0).getInventoryRequestLineItemId();
        InventoryRequest parentRequest = inventoryRequestLineItemRepository.findById(parentRequestId)
                .map(InventoryRequestLineItem::getRequest)
                .orElseThrow(() -> new IllegalArgumentException("Parent Inventory Request not found for batch."));


        Set<Long> parentRequestIds = issueDTO.getItemsToIssue().stream()
                .map(item -> inventoryRequestLineItemRepository.findById(item.getInventoryRequestLineItemId()).get().getRequest().getId())
                .collect(Collectors.toSet());
        if (parentRequestIds.size() > 1) {
            throw new IllegalArgumentException("All items in a batch must belong to the same inventory request.");
        }


        User storeKeeper = userRepository.findById(issueDTO.getIssuedByUserId())
                .orElseThrow(() -> new IllegalArgumentException(STORE_KEEPER_USER_WITH_ID + issueDTO.getIssuedByUserId() + NOT_FOUND));


        for (BatchIssueItemDTO itemDTO : issueDTO.getItemsToIssue()) {
            InventoryRequestLineItem requestLineItem = inventoryRequestLineItemRepository.findById(itemDTO.getInventoryRequestLineItemId())
                    .orElseThrow(() -> new IllegalArgumentException(INVENTORY_REQUEST_LINE_ITEM_WITH_ID + itemDTO.getInventoryRequestLineItemId() + NOT_FOUND));

            // Deduct stock and create issue record
            InventoryItem inventoryItem = requestLineItem.getRequestedItem();
            if (inventoryItem.getCurrentStockQuantity().compareTo(itemDTO.getIssuedQuantity()) < 0) {
                throw new IllegalArgumentException("Insufficient stock for item: " + inventoryItem.getItemName());
            }
            inventoryItem.setCurrentStockQuantity(inventoryItem.getCurrentStockQuantity().subtract(itemDTO.getIssuedQuantity()));
            inventoryItemRepository.save(inventoryItem);

            InventoryIssue issue = new InventoryIssue();
            issue.setRequestLineItem(requestLineItem);
            issue.setIssuedItem(inventoryItem);
            issue.setIssuedQuantity(itemDTO.getIssuedQuantity());
            issue.setIssuedByUser(storeKeeper);
            issue.setIssuedToUser(parentRequest.getRequester());
            issue.setIssuedAt(LocalDateTime.now());
            issue.setInventoryRequest(parentRequest);
            issue.setNotes(issueDTO.getIssueNotes());

            BigDecimal unitPrice = inventoryItem.getUnitPrice();
            BigDecimal issuedQuantityBd = itemDTO.getIssuedQuantity();
            BigDecimal calculatedItemValue = unitPrice.multiply(issuedQuantityBd);
            issue.setItemValue(calculatedItemValue);

            inventoryIssueRepository.save(issue);

            requestLineItem.setStatus(RequestLineItemStatus.ISSUED);
            inventoryRequestLineItemRepository.save(requestLineItem);
        }


        updateOverallRequestStatus(parentRequest, storeKeeper);

        return parentRequest;
    }

    @Transactional
    public InventoryRequestLineItem markRequestLineItemNoStock(Long inventoryRequestLineItemId, NoStockRequestDTO noStockDTO) {
        InventoryRequestLineItem requestLineItem = inventoryRequestLineItemRepository.findById(inventoryRequestLineItemId)
                .orElseThrow(() -> new IllegalArgumentException(INVENTORY_REQUEST_LINE_ITEM_WITH_ID + inventoryRequestLineItemId + NOT_FOUND));

        if (requestLineItem.getStatus() != RequestLineItemStatus.PENDING) {
            throw new IllegalArgumentException("This line item has already been processed and cannot be marked as no stock.");
        }

        User storeKeeper = userRepository.findById(noStockDTO.getStoreKeeperUserId())
                .orElseThrow(() -> new IllegalArgumentException(STORE_KEEPER_USER_WITH_ID + noStockDTO.getStoreKeeperUserId() + NOT_FOUND));

        requestLineItem.setStatus(RequestLineItemStatus.NO_STOCK);
        inventoryRequestLineItemRepository.save(requestLineItem);

        updateOverallRequestStatus(requestLineItem.getRequest(),storeKeeper);

        return requestLineItem;
    }

    public void updateOverallRequestStatus(InventoryRequest parentRequest, User processedByUser) {
        // Reload the request to get the latest status of all line items
        parentRequest = inventoryRequestRepository.findById(parentRequest.getId()).orElse(null);
        if (parentRequest == null) {
            return;
        }


        boolean hasUnprocessedItems = parentRequest.getLineItems().stream()
                .anyMatch(lineItem -> lineItem.getStatus() == RequestLineItemStatus.PENDING ||
                        lineItem.getStatus() == RequestLineItemStatus.NO_STOCK);
//


        if (hasUnprocessedItems) {
            parentRequest.setStatus(RequestStatus.PENDING);
        } else {

            parentRequest.setStatus(RequestStatus.ISSUED);
            parentRequest.setProcessedBy(processedByUser);
            parentRequest.setProcessedAt(LocalDateTime.now());
        }

        inventoryRequestRepository.save(parentRequest);

    }

    public List<MaterialDistributionTableDTO> getMaterialDistributionTable(Long userId) {
        LocalDate now = LocalDate.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();

        // Calculate previous month
        int previousMonth = currentMonth == 1 ? 12 : currentMonth - 1;
        int previousMonthYear = currentMonth == 1 ? currentYear - 1 : currentYear;

        return inventoryIssueRepository.getMaterialDistributionTable(
                userId,
                currentMonth,
                currentYear,
                previousMonth,
                previousMonthYear
        );
    }
}
