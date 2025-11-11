package com.benchmark.springdata.projection;

import com.benchmark.entity.Item;
import com.benchmark.entity.Category;
import org.springframework.data.rest.core.config.Projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Projection(name = "summary", types = Item.class)
public interface ItemProjection {
    Long getId();
    String getSku();
    String getName();
    BigDecimal getPrice();
    Integer getStock();
    LocalDateTime getUpdatedAt();

    // Inclure seulement l'ID de la catégorie pour éviter les relations profondes
    CategorySummary getCategory();

    interface CategorySummary {
        Long getId();
        String getCode();
        String getName();
    }
}