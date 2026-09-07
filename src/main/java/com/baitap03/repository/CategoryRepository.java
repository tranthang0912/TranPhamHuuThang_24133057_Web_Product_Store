package com.baitap03.repository;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import com.baitap03.config.JPAConfig;
import com.baitap03.model.Category;

public class CategoryRepository
        implements ICategoryRepository {

    @Override
    public void insert(Category category) {

        EntityManager enma =
                JPAConfig.getEntityManager();

        EntityTransaction trans =
                enma.getTransaction();

        try {

            trans.begin();

            enma.persist(category);

            trans.commit();

        } catch (Exception e) {

            e.printStackTrace();

            if (trans.isActive()) {
                trans.rollback();
            }

            throw e;

        } finally {

            enma.close();
        }
    }


    @Override
    public void update(Category category) {

        EntityManager enma =
                JPAConfig.getEntityManager();

        EntityTransaction trans =
                enma.getTransaction();

        try {

            trans.begin();

            enma.merge(category);

            trans.commit();

        } catch (Exception e) {

            e.printStackTrace();

            if (trans.isActive()) {
                trans.rollback();
            }

            throw e;

        } finally {

            enma.close();
        }
    }


    @Override
    public void delete(int categoryid)
            throws Exception {

        EntityManager enma =
                JPAConfig.getEntityManager();

        EntityTransaction trans =
                enma.getTransaction();

        try {

            trans.begin();

            Category category =
                    enma.find(
                            Category.class,
                            categoryid
                    );

            if (category != null) {

                enma.remove(category);

            } else {

                throw new Exception(
                        "Không tìm thấy Category"
                );
            }

            trans.commit();

        } catch (Exception e) {

            e.printStackTrace();

            if (trans.isActive()) {
                trans.rollback();
            }

            throw e;

        } finally {

            enma.close();
        }
    }


    @Override
    public Category findById(int categoryid) {

        EntityManager enma =
                JPAConfig.getEntityManager();

        try {

            return enma.find(
                    Category.class,
                    categoryid
            );

        } finally {

            enma.close();
        }
    }


    @Override
    public Category findByCategoryname(
            String name) {

        EntityManager enma =
                JPAConfig.getEntityManager();

        try {

            String jpql =
                    "SELECT c FROM Category c "
                    + "WHERE c.categoryname = :catename";

            TypedQuery<Category> query =
                    enma.createQuery(
                            jpql,
                            Category.class
                    );

            query.setParameter(
                    "catename",
                    name
            );

            return query
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

        } finally {

            enma.close();
        }
    }


    @Override
    public List<Category> findAll() {

        EntityManager enma =
                JPAConfig.getEntityManager();

        try {

            TypedQuery<Category> query =
                    enma.createNamedQuery(
                            "Category.findAll",
                            Category.class
                    );

            return query.getResultList();

        } finally {

            enma.close();
        }
    }


    @Override
    public List<Category> searchByName(
            String categoryname) {

        EntityManager enma =
                JPAConfig.getEntityManager();

        try {

            String jpql =
                    "SELECT c FROM Category c "
                    + "WHERE c.categoryname LIKE :catname";

            TypedQuery<Category> query =
                    enma.createQuery(
                            jpql,
                            Category.class
                    );

            query.setParameter(
                    "catname",
                    "%" + categoryname + "%"
            );

            return query.getResultList();

        } finally {

            enma.close();
        }
    }


    @Override
    public List<Category> findAll(
            int page,
            int pagesize) {

        EntityManager enma =
                JPAConfig.getEntityManager();

        try {

            TypedQuery<Category> query =
                    enma.createNamedQuery(
                            "Category.findAll",
                            Category.class
                    );

            query.setFirstResult(
                    page * pagesize
            );

            query.setMaxResults(
                    pagesize
            );

            return query.getResultList();

        } finally {

            enma.close();
        }
    }


    @Override
    public int count() {

        EntityManager enma =
                JPAConfig.getEntityManager();

        try {

            String jpql =
                    "SELECT COUNT(c) FROM Category c";

            Query query =
                    enma.createQuery(jpql);

            return ((Long)
                    query.getSingleResult())
                    .intValue();

        } finally {

            enma.close();
        }
    }
}