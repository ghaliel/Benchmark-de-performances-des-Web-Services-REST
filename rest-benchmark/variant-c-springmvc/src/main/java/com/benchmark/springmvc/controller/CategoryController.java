package com.benchmark.springmvc.controller;

import com.benchmark.entity.Category;
import com.benchmark.entity.Item;
import com.benchmark.springmvc.dto.ItemResponse;
import com.benchmark.springmvc.repository.CategoryRepository;
import com.benchmark.springmvc.repository.ItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final ItemRepository itemRepository;

    public CategoryController(CategoryRepository categoryRepository, ItemRepository itemRepository) {
        this.categoryRepository = categoryRepository;
        this.itemRepository = itemRepository;
    }

    @GetMapping
    public ResponseEntity<Page<Category>> getCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Category> categories = categoryRepository.findAll(pageable);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategory(@PathVariable Long id) {
        return categoryRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Category> createCategory(@Valid @RequestBody Category category) {
        Category saved = categoryRepository.save(category);
        return ResponseEntity.status(201).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id,
                                                   @Valid @RequestBody Category category) {
        if (!categoryRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        category.setId(id);
        Category updated = categoryRepository.save(category);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        if (!categoryRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        categoryRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/items")
    @Transactional(readOnly = true)
    public ResponseEntity<Page<ItemResponse>> getCategoryItems(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        if (!categoryRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Item> items = itemRepository.findByCategoryId(id, pageable);
        return ResponseEntity.ok(items.map(ItemResponse::from));
    }
}