package com.baitap03.repository;

import java.util.List;

import com.baitap03.model.Product;

public interface IProductRepository {

    void insert(Product product);

    void update(Product product);

    void delete(int productId);

    Product findById(int productId);

    Product findActiveById(int productId);

    List<Product> findAll();

    List<Product> findLatestActive(int limit);

    List<Product> findActivePage(int page, int pageSize);

    List<Product> findActivePageByCategory(int categoryId, int page, int pageSize);

    long countActive();

    long countActiveByCategory(int categoryId);
}
