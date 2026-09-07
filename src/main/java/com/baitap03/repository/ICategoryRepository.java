package com.baitap03.repository;

import java.util.List;

import com.baitap03.model.Category;

public interface ICategoryRepository {

    void insert(Category category);

    void update(Category category);

    void delete(int categoryid) throws Exception;

    Category findById(int categoryid);

    Category findByCategoryname(String name);

    List<Category> findAll();

    List<Category> searchByName(String categoryname);

    List<Category> findAll(int page, int pagesize);

    int count();
}