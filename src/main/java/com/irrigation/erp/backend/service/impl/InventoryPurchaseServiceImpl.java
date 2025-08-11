package com.irrigation.erp.backend.service.impl;

import com.irrigation.erp.backend.dto.*;
import com.irrigation.erp.backend.model.*;
import com.irrigation.erp.backend.repository.*;
import com.irrigation.erp.backend.service.InventoryPurchaseService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryPurchaseServiceImpl implements InventoryPurchaseService {

    private final InventoryPurchaseRepository purchaseRepository;
    private final InventoryRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final InventoryItemRepository itemRepository;
    private final PurchaseItemRepository purchaseItemRepository;

    @Override
    @Transactional
    public InventoryPurchase createPurchase(InventoryPurchaseCreateRequestDTO requestDTO) {
        InventoryRequest request = requestRepository.findById(requestDTO.getInventoryRequestId())
                .orElseThrow(() -> new IllegalArgumentException("Inventory request not found"));

        User acceptedBy = userRepository.findById(requestDTO.getAcceptedByUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        InventoryPurchase purchase = new InventoryPurchase();
        purchase.setRefNo(requestDTO.getRefNo());
        purchase.setDate(requestDTO.getDate());
        purchase.setDivision(requestDTO.getDivision());
        purchase.setSubDivision(requestDTO.getSubDivision());
        purchase.setProgramme(requestDTO.getProgramme());
        purchase.setProject(requestDTO.getProject());
        purchase.setObject(requestDTO.getObject());
        purchase.setDescription(requestDTO.getDescription());
        purchase.setPayee(requestDTO.getPayee());
        purchase.setPreparedBy(requestDTO.getPreparedBy());
        purchase.setGoodReceivingNotePath(requestDTO.getGoodReceivingNotePath());
        purchase.setShopBillPath(requestDTO.getShopBillPath());
        purchase.setInventoryRequest(request);
        purchase.setAcceptedByUser(acceptedBy);
        purchase.setCreatedAt(LocalDateTime.now());

        InventoryPurchase savedPurchase = purchaseRepository.save(purchase);

        // Save purchase items
        List<PurchaseItem> purchaseItems = requestDTO.getItems().stream()
                .map(itemDTO -> {
                    InventoryItem item = itemRepository.findById(itemDTO.getItemId())
                            .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemDTO.getItemId()));

                    PurchaseItem purchaseItem = new PurchaseItem();
                    purchaseItem.setPurchase(savedPurchase);
                    purchaseItem.setItem(item);
                    purchaseItem.setQuantity(itemDTO.getQuantity());
                    return purchaseItem;
                })
                .collect(Collectors.toList());

        purchaseItemRepository.saveAll(purchaseItems);
        savedPurchase.setItems(purchaseItems);

        return savedPurchase;
    }

    @Override
    @Transactional
    public InventoryPurchase updatePurchase(Long purchaseId, String refNo, LocalDate date,
                                            String division, String subDivision, String programme,
                                            String project, String object, String description,
                                            String payee, String preparedBy, String goodReceivingNotePath,
                                            String shopBillPath, Long acceptedByUserId,
                                            Long inventoryRequestId, List<PurchaseItemRequestDTO> items) {

        InventoryPurchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new IllegalArgumentException("Purchase not found"));

        if (inventoryRequestId != null) {
            InventoryRequest request = requestRepository.findById(inventoryRequestId)
                    .orElseThrow(() -> new IllegalArgumentException("Inventory request not found"));
            purchase.setInventoryRequest(request);
        }

        if (acceptedByUserId != null) {
            User acceptedBy = userRepository.findById(acceptedByUserId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            purchase.setAcceptedByUser(acceptedBy);
        }

        // Update basic fields
        if (refNo != null) purchase.setRefNo(refNo);
        if (date != null) purchase.setDate(date);
        if (division != null) purchase.setDivision(division);
        if (subDivision != null) purchase.setSubDivision(subDivision);
        if (programme != null) purchase.setProgramme(programme);
        if (project != null) purchase.setProject(project);
        if (object != null) purchase.setObject(object);
        if (description != null) purchase.setDescription(description);
        if (payee != null) purchase.setPayee(payee);
        if (preparedBy != null) purchase.setPreparedBy(preparedBy);
        if (goodReceivingNotePath != null) purchase.setGoodReceivingNotePath(goodReceivingNotePath);
        if (shopBillPath != null) purchase.setShopBillPath(shopBillPath);

        purchase.setUpdatedAt(LocalDateTime.now());

        // Update items if provided
        if (items != null && !items.isEmpty()) {
            // First remove existing items
            purchaseItemRepository.deleteByPurchaseId(purchaseId);

            // Add new items
            List<PurchaseItem> purchaseItems = items.stream()
                    .map(itemDTO -> {
                        InventoryItem item = itemRepository.findById(itemDTO.getItemId())
                                .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemDTO.getItemId()));

                        PurchaseItem purchaseItem = new PurchaseItem();
                        purchaseItem.setPurchase(purchase);
                        purchaseItem.setItem(item);
                        purchaseItem.setQuantity(itemDTO.getQuantity());
                        return purchaseItem;
                    })
                    .collect(Collectors.toList());

            purchaseItemRepository.saveAll(purchaseItems);
            purchase.setItems(purchaseItems);
        }

        return purchaseRepository.save(purchase);
    }

    @Override
    public List<InventoryPurchase> getAllPurchases() {
        return purchaseRepository.findAll();
    }

    @Override
    public Optional<InventoryPurchase> getPurchaseById(Long purchaseId) {
        return purchaseRepository.findById(purchaseId);
    }

    @Override
    @Transactional
    public void deletePurchase(Long purchaseId) {
        // First delete all associated items
        purchaseItemRepository.deleteByPurchaseId(purchaseId);
        // Then delete the purchase
        purchaseRepository.deleteById(purchaseId);
    }
}