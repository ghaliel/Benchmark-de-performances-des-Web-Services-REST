package com.benchmark.springmvc.dto;

import com.benchmark.entity.Category;
import com.benchmark.entity.Item;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ItemResponse(
        Long id,
        String sku,
        String name,
        BigDecimal price,
        Integer stock,
        String description,
        LocalDateTime updatedAt,
        CategoryResponse category
) {

    public static ItemResponse from(Item item) {
        Category category = item.getCategory();
        CategoryResponse categoryResponse = null;
        if (category != null) {
            categoryResponse = new CategoryResponse(
                    category.getId(),
                    category.getCode(),
                    category.getName()
            );
        }

        return new ItemResponse(
                item.getId(),
                item.getSku(),
                item.getName(),
                item.getPrice(),
                item.getStock(),
                item.getDescription(),
                item.getUpdatedAt(),
                categoryResponse
        );
    }

    public record CategoryResponse(Long id, String code, String name) {
    }
}

