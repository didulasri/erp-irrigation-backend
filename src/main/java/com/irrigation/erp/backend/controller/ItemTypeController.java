package com.irrigation.erp.backend.controller;

import com.irrigation.erp.backend.model.ItemType;
import com.irrigation.erp.backend.repository.ItemTypeRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/item-types")
@CrossOrigin(origins = "http://localhost:5173")
public class ItemTypeController {

    private final ItemTypeRepository itemTypeRepository;

    public ItemTypeController(ItemTypeRepository itemTypeRepository) {
        this.itemTypeRepository = itemTypeRepository;
    }

    @GetMapping
    public List<ItemType> getAllItemTypes() {
        return itemTypeRepository.findAll();
    }
}
