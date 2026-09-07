package com.baitap03.repository;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import com.baitap03.config.JPAConfig;
import com.baitap03.model.Product;

public class ProductRepository implements IProductRepository {

    @Override
    public void insert(Product product) {
        executeWrite(enma -> enma.persist(product));
    }

    @Override
    public void update(Product product) {
        executeWrite(enma -> enma.merge(product));
    }

    @Override
    public void delete(int productId) {
        executeWrite(enma -> {
            Product product = enma.find(Product.class, productId);
            if (product != null) {
                enma.remove(product);
            }
        });
    }

    @Override
    public Product findById(int productId) {
        EntityManager enma = JPAConfig.getEntityManager();
        try {
            return enma.find(Product.class, productId);
        } finally {
            enma.close();
        }
    }

    @Override
    public Product findActiveById(int productId) {
        EntityManager enma = JPAConfig.getEntityManager();
        try {
            TypedQuery<Product> query = enma.createQuery(
                    "SELECT p FROM Product p JOIN FETCH p.category "
                    + "WHERE p.productid = :id AND p.status = 1",
                    Product.class
            );
            query.setParameter("id", productId);
            return query.getResultStream().findFirst().orElse(null);
        } finally {
            enma.close();
        }
    }

    @Override
    public List<Product> findAll() {
        EntityManager enma = JPAConfig.getEntityManager();
        try {
            return enma.createQuery(
                    "SELECT p FROM Product p JOIN FETCH p.category ORDER BY p.createdAt DESC",
                    Product.class
            ).getResultList();
        } finally {
            enma.close();
        }
    }

    @Override
    public List<Product> findLatestActive(int limit) {
        EntityManager enma = JPAConfig.getEntityManager();
        try {
            TypedQuery<Product> query = enma.createQuery(
                    "SELECT p FROM Product p JOIN FETCH p.category "
                    + "WHERE p.status = 1 AND p.category.status = 1 "
                    + "ORDER BY p.createdAt DESC, p.productid DESC",
                    Product.class
            );
            query.setMaxResults(limit);
            return query.getResultList();
        } finally {
            enma.close();
        }
    }

    @Override
    public List<Product> findActivePage(int page, int pageSize) {
        EntityManager enma = JPAConfig.getEntityManager();
        try {
            TypedQuery<Product> query = enma.createQuery(
                    "SELECT p FROM Product p JOIN FETCH p.category "
                    + "WHERE p.status = 1 AND p.category.status = 1 "
                    + "ORDER BY p.createdAt DESC, p.productid DESC",
                    Product.class
            );
            query.setFirstResult(page * pageSize);
            query.setMaxResults(pageSize);
            return query.getResultList();
        } finally {
            enma.close();
        }
    }

    @Override
    public List<Product> findActivePageByCategory(
            int categoryId,
            int page,
            int pageSize) {
        EntityManager enma = JPAConfig.getEntityManager();
        try {
            TypedQuery<Product> query = enma.createQuery(
                    "SELECT p FROM Product p JOIN FETCH p.category "
                    + "WHERE p.status = 1 AND p.category.status = 1 "
                    + "AND p.category.categoryid = :categoryId "
                    + "ORDER BY p.createdAt DESC, p.productid DESC",
                    Product.class
            );
            query.setParameter("categoryId", categoryId);
            query.setFirstResult(page * pageSize);
            query.setMaxResults(pageSize);
            return query.getResultList();
        } finally {
            enma.close();
        }
    }

    @Override
    public long countActive() {
        EntityManager enma = JPAConfig.getEntityManager();
        try {
            return enma.createQuery(
                    "SELECT COUNT(p) FROM Product p "
                    + "WHERE p.status = 1 AND p.category.status = 1",
                    Long.class
            ).getSingleResult();
        } finally {
            enma.close();
        }
    }

    @Override
    public long countActiveByCategory(int categoryId) {
        EntityManager enma = JPAConfig.getEntityManager();
        try {
            return enma.createQuery(
                    "SELECT COUNT(p) FROM Product p "
                    + "WHERE p.status = 1 AND p.category.status = 1 "
                    + "AND p.category.categoryid = :categoryId",
                    Long.class
            ).setParameter("categoryId", categoryId)
                    .getSingleResult();
        } finally {
            enma.close();
        }
    }

    private void executeWrite(EntityAction action) {
        EntityManager enma = JPAConfig.getEntityManager();
        EntityTransaction trans = enma.getTransaction();
        try {
            trans.begin();
            action.run(enma);
            trans.commit();
        } catch (RuntimeException e) {
            if (trans.isActive()) {
                trans.rollback();
            }
            throw e;
        } finally {
            enma.close();
        }
    }

    @FunctionalInterface
    private interface EntityAction {
        void run(EntityManager entityManager);
    }
}
