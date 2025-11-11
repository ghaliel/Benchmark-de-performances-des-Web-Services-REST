package com.benchmark.jersey.repository;

import com.benchmark.entity.Category;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class CategoryRepository {

    private final EntityManagerFactory emf;

    public CategoryRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public List<Category> findAll(int page, int size) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Category> query = em.createQuery(
                    "SELECT c FROM Category c ORDER BY c.id", Category.class);
            query.setFirstResult(page * size);
            query.setMaxResults(size);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public long count() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT COUNT(c) FROM Category c", Long.class)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }

    public Optional<Category> findById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            Category category = em.find(Category.class, id);
            return Optional.ofNullable(category);
        } finally {
            em.close();
        }
    }

    public boolean existsById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            Long count = em.createQuery("SELECT COUNT(c) FROM Category c WHERE c.id = :id", Long.class)
                    .setParameter("id", id)
                    .getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }

    public Category save(Category category) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            category.setUpdatedAt(LocalDateTime.now());
            Category saved = em.merge(category);
            em.getTransaction().commit();
            return saved;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void delete(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Category category = em.find(Category.class, id);
            if (category != null) {
                em.remove(category);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}