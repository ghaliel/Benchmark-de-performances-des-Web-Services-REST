package com.benchmark.springmvc.repository;

import com.benchmark.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    @Override
    @EntityGraph(attributePaths = "category")
    Page<Item> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "category")
    Page<Item> findByCategoryId(Long categoryId, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "category")
    Optional<Item> findById(Long id);
}