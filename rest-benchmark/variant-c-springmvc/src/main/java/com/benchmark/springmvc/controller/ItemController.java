package com.benchmark.springmvc.controller;

import com.benchmark.springmvc.repository.ItemRepository;
import com.benchmark.springmvc.dto.ItemResponse;
import com.benchmark.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemRepository itemRepository;

    public ItemController(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<Page<ItemResponse>> getItems(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Item> items;

        if (categoryId != null) {
            items = itemRepository.findByCategoryId(categoryId, pageable);
        } else {
            items = itemRepository.findAll(pageable);
        }

        return ResponseEntity.ok(items.map(ItemResponse::from));
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<ItemResponse> getItem(@PathVariable Long id) {
        return itemRepository.findById(id)
                .map(ItemResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Item> createItem(@Valid @RequestBody Item item) {
        Item saved = itemRepository.save(item);
        return ResponseEntity.status(201).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(@PathVariable Long id,
                                           @Valid @RequestBody Item item) {
        if (!itemRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        item.setId(id);
        Item updated = itemRepository.save(item);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        if (!itemRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        itemRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}