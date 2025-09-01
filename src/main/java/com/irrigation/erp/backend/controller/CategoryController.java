package com.irrigation.erp.backend.controller;

import com.irrigation.erp.backend.dto.CategoryWithCountDTO;
import com.irrigation.erp.backend.model.ItemCategory;
import com.irrigation.erp.backend.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "http://localhost:5173")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<ItemCategory>> getAllCategories() {
        List<ItemCategory> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/with-counts")
    public ResponseEntity<List<CategoryWithCountDTO>> getAllCategoriesWithCounts() {
        List<CategoryWithCountDTO> categoriesWithCounts = categoryService.getAllCategoriesWithCounts();
        return ResponseEntity.ok(categoriesWithCounts);
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<ItemCategory> getCategoryById(@PathVariable Long categoryId) {
        ItemCategory category = categoryService.getCategoryById(categoryId);
        return ResponseEntity.ok(category);
    }

    @GetMapping("/name/{categoryName}")
    public ResponseEntity<ItemCategory> getCategoryByName(@PathVariable String categoryName) {
        ItemCategory category = categoryService.getCategoryByName(categoryName);
        return ResponseEntity.ok(category);
    }

    @PostMapping("/add")
    public ResponseEntity<ItemCategory> addCategory(@RequestBody ItemCategory category) {
        ItemCategory savedCategory = categoryService.addCategory(category);
        return ResponseEntity.ok(savedCategory);
    }
}