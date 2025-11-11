package com.benchmark.springdata.repository;

import com.benchmark.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.Optional;

@RepositoryRestResource(path = "categories", collectionResourceRel = "categories")
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Exposé automatiquement : GET /api/categories
    // Exposé automatiquement : GET /api/categories/{id}

    // Recherche par code - exposé automatiquement : GET /api/categories/search/findByCode?code=...
    Optional<Category> findByCode(String code);

    // Recherche par nom - exposé automatiquement : GET /api/categories/search/findByNameContaining?name=...
    Page<Category> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Désactiver certaines opérations si besoin
    @Override
    @RestResource(exported = false)
    void deleteById(Long id);

    @Override
    @RestResource(exported = false)
    void delete(Category entity);
}