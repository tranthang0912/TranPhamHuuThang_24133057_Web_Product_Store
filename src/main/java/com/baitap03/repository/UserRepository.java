package com.baitap03.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import com.baitap03.config.JPAConfig;
import com.baitap03.model.User;

public class UserRepository
        implements IUserRepository {

    @Override
    public User findById(int userId) {
        EntityManager em = JPAConfig.getEntityManager();
        try {
            return em.find(User.class, userId);
        } finally {
            em.close();
        }
    }

    @Override
    public User updateProfile(int userId, String fullname, String phone, String image) {
        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            // Load the managed entity instead of merging a stale session account.
            User user = em.find(User.class, userId);
            if (user == null || !user.isActive()) {
                throw new IllegalArgumentException("Tài khoản không còn hoạt động.");
            }
            user.setFullname(fullname);
            user.setPhone(phone);
            if (image != null) {
                user.setImages(image);
            }
            transaction.commit();
            return user;
        } catch (RuntimeException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public void insert(User user) {

        EntityManager enma =
                JPAConfig.getEntityManager();

        EntityTransaction trans =
                enma.getTransaction();

        try {

            trans.begin();

            enma.persist(user);

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
    public void update(User user) {
        EntityManager enma = JPAConfig.getEntityManager();
        EntityTransaction trans = enma.getTransaction();
        try {
            trans.begin();
            enma.merge(user);
            trans.commit();
        } catch (Exception e) {
            if (trans.isActive()) {
                trans.rollback();
            }
            throw e;
        } finally {
            enma.close();
        }
    }


    @Override
    public User findByUsername(
            String username) {

        EntityManager enma =
                JPAConfig.getEntityManager();

        try {

            String jpql =
                    "SELECT u FROM User u "
                    + "WHERE u.username = :username";

            TypedQuery<User> query =
                    enma.createQuery(
                            jpql,
                            User.class
                    );

            query.setParameter(
                    "username",
                    username
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
    public User findByEmail(String email) {
        EntityManager enma = JPAConfig.getEntityManager();
        try {
            TypedQuery<User> query = enma.createQuery(
                    "SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email)",
                    User.class
            );
            query.setParameter("email", email);
            return query.getResultStream().findFirst().orElse(null);
        } finally {
            enma.close();
        }
    }

    @Override
    public User findByIdentifier(String identifier) {
        EntityManager enma = JPAConfig.getEntityManager();
        try {
            TypedQuery<User> query = enma.createQuery(
                    "SELECT u FROM User u "
                    + "WHERE LOWER(u.username) = LOWER(:identifier) "
                    + "OR LOWER(u.email) = LOWER(:identifier)",
                    User.class
            );
            query.setParameter("identifier", identifier);
            return query.getResultStream().findFirst().orElse(null);
        } finally {
            enma.close();
        }
    }
}
