package com.benchmark.springmvc.repository;

import com.benchmark.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Méthodes de base fournies par JpaRepository :
    // findAll(Pageable), findById, save, deleteById, etc.

    @Query("SELECT c FROM Category c WHERE c.code = :code")
    Optional<Category> findByCode(@Param("code") String code);

    Page<Category> findByNameContainingIgnoreCase(String name, Pageable pageable);
}