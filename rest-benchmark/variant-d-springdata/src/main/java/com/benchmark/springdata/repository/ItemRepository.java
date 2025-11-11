package com.benchmark.springdata.repository;

import com.benchmark.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.Optional;

@RepositoryRestResource(path = "items", collectionResourceRel = "items")
public interface ItemRepository extends JpaRepository<Item, Long> {

    // Exposé automatiquement : GET /api/items
    // Exposé automatiquement : GET /api/items/{id}

    // Filtrage par catégorie - exposé automatiquement : GET /api/items/search/findByCategoryId?categoryId=...
    @RestResource(path = "byCategoryId", rel = "byCategoryId")
    Page<Item> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    // Recherche par SKU
    Optional<Item> findBySku(String sku);

    // Recherche par nom
    Page<Item> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Méthodes avec JOIN FETCH pour éviter N+1
    @Query("SELECT i FROM Item i JOIN FETCH i.category")
    Page<Item> findAllWithCategory(Pageable pageable);

    @Query("SELECT i FROM Item i JOIN FETCH i.category WHERE i.category.id = :categoryId")
    Page<Item> findByCategoryIdWithCategory(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query("SELECT i FROM Item i JOIN FETCH i.category WHERE i.id = :id")
    Optional<Item> findByIdWithCategory(@Param("id") Long id);

    // Désactiver DELETE si besoin pour le benchmark
    @Override
    @RestResource(exported = false)
    void deleteById(Long id);
}