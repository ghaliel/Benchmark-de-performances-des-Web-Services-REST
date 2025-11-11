package com.benchmark.springdata.projection;

import com.benchmark.entity.Category;
import org.springframework.data.rest.core.config.Projection;

import java.time.LocalDateTime;

@Projection(name = "summary", types = Category.class)
public interface CategoryProjection {
    Long getId();
    String getCode();
    String getName();
    LocalDateTime getUpdatedAt();

    // Pas d'items pour éviter les relations circulaires
}