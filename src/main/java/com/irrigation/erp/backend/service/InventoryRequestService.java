package com.irrigation.erp.backend.service;

import com.irrigation.erp.backend.dto.InventoryItemCreateRequestDTO;
import com.irrigation.erp.backend.dto.InventoryRequestCreateDTO;
import com.irrigation.erp.backend.dto.IssueRequestDTO;
import com.irrigation.erp.backend.dto.NoStockRequestDTO;
import com.irrigation.erp.backend.enums.RequestStatus;
import com.irrigation.erp.backend.model.InventoryIssue;
import com.irrigation.erp.backend.model.InventoryItem;
import com.irrigation.erp.backend.model.InventoryRequest;
import com.irrigation.erp.backend.model.User;
import com.irrigation.erp.backend.repository.InventoryIssueRepository;
import com.irrigation.erp.backend.repository.InventoryItemRepository;
import com.irrigation.erp.backend.repository.InventoryRequestRepository;
import com.irrigation.erp.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Past;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class InventoryRequestService {


    private final InventoryRequestRepository inventoryRequestRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final UserRepository userRepository;
    private final InventoryIssueRepository inventoryIssueRepository;

    public InventoryRequestService(InventoryRequestRepository inventoryRequestRepository,
                                   InventoryItemRepository inventoryItemRepository,
                                   UserRepository userRepository, InventoryIssueRepository inventoryIssueRepository) {
        this.inventoryRequestRepository = inventoryRequestRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.userRepository = userRepository;
        this.inventoryIssueRepository = inventoryIssueRepository;
    }


    // --- Request Creation
    @Transactional
    public InventoryRequest createInventoryRequest(InventoryRequestCreateDTO requestDTO){
        User requester = userRepository.findById(requestDTO.getRequesterUserId())
                .orElseThrow(() -> new IllegalArgumentException("Requester user with ID '" + requestDTO.getRequesterUserId() + "' not found."));

        InventoryItem requestedItem = inventoryItemRepository.findByItemCode(requestDTO.getItemCode())
                .orElseThrow(() -> new IllegalArgumentException("Requested item with code '" + requestDTO.getItemCode() + "' not found."));

        if (requestDTO.getRequestedQuantity() <= 0) {
            throw new IllegalArgumentException("Requested quantity must be positive.");
        }

        InventoryRequest newRequest = new InventoryRequest();
        newRequest.setRequester(requester);
        newRequest.setRequestedItem(requestedItem);
        newRequest.setRequestedQuantity(requestDTO.getRequestedQuantity());
        newRequest.setPurpose(requestDTO.getPurpose());
        newRequest.setStatus(RequestStatus.PENDING); // Initial status
        newRequest.setRequestedAt(LocalDateTime.now());

        return inventoryRequestRepository.save(newRequest);

    }


    // --- View Requests (by Store Keeper) ---
    public List<InventoryRequest> getAllPendingInventoryRequests() {
        return inventoryRequestRepository.findByStatus(RequestStatus.PENDING);
    }

    public Optional<InventoryRequest> getInventoryRequestById(Long requestId) {
        return inventoryRequestRepository.findById(requestId);
    }



    // --- Issue Item (by Store Keeper) ---
    @Transactional
    public InventoryIssue issueInventoryItem(Long requestId, IssueRequestDTO issueDTO) {
        InventoryRequest request = inventoryRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory request with ID '" + requestId + "' not found."));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalArgumentException("Request is not in PENDING status and cannot be issued.");
        }

        User storeKeeper = userRepository.findById(issueDTO.getIssuedByUserId()) // Using issuedByUserId from updated DTO
                .orElseThrow(() -> new IllegalArgumentException("Store keeper user with ID '" + issueDTO.getIssuedByUserId() + "' not found."));

        InventoryItem itemToIssue = request.getRequestedItem();
        Double requestedQuantity = request.getRequestedQuantity();
        Double quantityToIssue = issueDTO.getIssuedQuantity();

        if (quantityToIssue <= 0 || quantityToIssue > requestedQuantity) {
            throw new IllegalArgumentException("Issued quantity must be positive and not exceed requested quantity.");
        }

        // Check stock availability
        if (itemToIssue.getCurrentStockQuantity() < quantityToIssue) {
            throw new IllegalArgumentException("Insufficient stock for item '" + itemToIssue.getItemCode() + "'. Available: " + itemToIssue.getCurrentStockQuantity() + ", Requested: " + quantityToIssue);
        }

        // Deduct stock
        itemToIssue.setCurrentStockQuantity(itemToIssue.getCurrentStockQuantity() - quantityToIssue);

        if (itemToIssue.getLastUpdatedByUser() != null) {
            itemToIssue.setLastUpdatedByUser(storeKeeper);
        }
        itemToIssue.setLastUpdatedAt(LocalDateTime.now());
        inventoryItemRepository.save(itemToIssue); // Save updated item stock

        // Create Issue record
        InventoryIssue issue = new InventoryIssue();
        issue.setRequest(request);
        issue.setIssuedItem(itemToIssue);
        issue.setIssuedQuantity(quantityToIssue);
        issue.setIssuedByUser(storeKeeper);
        issue.setIssuedToUser(request.getRequester());
        issue.setIssuedAt(LocalDateTime.now());

        // Calculate itemValue
        BigDecimal unitPrice = itemToIssue.getUnitPrice();
        BigDecimal issuedQuantityBd = BigDecimal.valueOf(quantityToIssue);
        BigDecimal calculatedItemValue = unitPrice.multiply(issuedQuantityBd);
        issue.setItemValue(calculatedItemValue);

        // Set purpose from the request
        issue.setPurpose(request.getPurpose());


        inventoryIssueRepository.save(issue);

        // Update Request status
        if (quantityToIssue.equals(requestedQuantity)) {
            request.setStatus(RequestStatus.ISSUED); // Fully issued
        } else {
            // If partial issue is allowed, you might need to adjust requestedQuantity on the original request
            // or create a new request for the remaining amount.
            // For now, marking as ISSUED even if partially fulfilled
            request.setStatus(RequestStatus.ISSUED);
        }
        request.setProcessedBy(storeKeeper);
        request.setProcessedAt(LocalDateTime.now());
        inventoryRequestRepository.save(request);
        return issue;
    }


    // --- Mark Request as No Stock (by Store Keeper) ---
    @Transactional
    public InventoryRequest markRequestNoStock(Long requestId, NoStockRequestDTO noStockDTO) {
        InventoryRequest request = inventoryRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory request with ID '" + requestId + "' not found."));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalArgumentException("Request is not in PENDING status and cannot be marked as no stock.");
        }

        User storeKeeper = userRepository.findById(noStockDTO.getStoreKeeperUserId())
                .orElseThrow(() -> new IllegalArgumentException("Store keeper user with ID '" + noStockDTO.getStoreKeeperUserId() + "' not found."));

        request.setStatus(RequestStatus.NO_STOCK);
        request.setProcessedBy(storeKeeper);
        request.setProcessedAt(LocalDateTime.now());
        return inventoryRequestRepository.save(request);
    }









}
