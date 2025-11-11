package com.benchmark.jersey.repository;

import com.benchmark.entity.Item;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class ItemRepository {

    private final EntityManagerFactory emf;
    private final boolean useJoinFetch;

    public ItemRepository(EntityManagerFactory emf) {
        this.emf = emf;
        this.useJoinFetch = Boolean.parseBoolean(
                System.getenv().getOrDefault("USE_JOIN_FETCH", "true"));
    }

    public List<Item> findAll(int page, int size) {
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = useJoinFetch
                    ? "SELECT i FROM Item i JOIN FETCH i.category ORDER BY i.id"
                    : "SELECT i FROM Item i ORDER BY i.id";

            TypedQuery<Item> query = em.createQuery(jpql, Item.class);
            query.setFirstResult(page * size);
            query.setMaxResults(size);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Item> findByCategoryId(Long categoryId, int page, int size) {
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = useJoinFetch
                    ? "SELECT i FROM Item i JOIN FETCH i.category WHERE i.category.id = :categoryId ORDER BY i.id"
                    : "SELECT i FROM Item i WHERE i.category.id = :categoryId ORDER BY i.id";

            TypedQuery<Item> query = em.createQuery(jpql, Item.class);
            query.setParameter("categoryId", categoryId);
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
            return em.createQuery("SELECT COUNT(i) FROM Item i", Long.class)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }

    public long countByCategoryId(Long categoryId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT COUNT(i) FROM Item i WHERE i.category.id = :categoryId", Long.class)
                    .setParameter("categoryId", categoryId)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }

    public Optional<Item> findById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = useJoinFetch
                    ? "SELECT i FROM Item i JOIN FETCH i.category WHERE i.id = :id"
                    : "SELECT i FROM Item i WHERE i.id = :id";

            List<Item> results = em.createQuery(jpql, Item.class)
                    .setParameter("id", id)
                    .getResultList();

            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            em.close();
        }
    }

    public Item save(Item item) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            item.setUpdatedAt(LocalDateTime.now());
            Item saved = em.merge(item);
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
            Item item = em.find(Item.class, id);
            if (item != null) {
                em.remove(item);
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