package com.baitap03.service;

import java.util.List;

import com.baitap03.model.Category;
import com.baitap03.repository.CategoryRepository;
import com.baitap03.repository.ICategoryRepository;

public class CategoryServiceImpl
        implements ICategoryService {

    public ICategoryRepository categoryRepository =
            new CategoryRepository();


    @Override
    public void insert(Category category) {

        Category old =
                findByCategoryname(
                        category.getCategoryname()
                );

        if (old == null) {

            categoryRepository.insert(
                    category
            );

        } else {

            throw new IllegalArgumentException(
                    "Tên Category đã tồn tại"
            );
        }
    }


    @Override
    public void update(Category category) {

        Category duplicate = findByCategoryname(category.getCategoryname());
        if (duplicate != null && duplicate.getCategoryid() != category.getCategoryid()) {
            throw new IllegalArgumentException("Tên danh mục đã tồn tại.");
        }

        Category old =
                findById(
                        category.getCategoryid()
                );

        if (old != null) {

            categoryRepository.update(
                    category
            );
        }
    }


    @Override
    public void delete(int categoryid)
            throws Exception {

        categoryRepository.delete(
                categoryid
        );
    }


    @Override
    public Category findById(
            int categoryid) {

        return categoryRepository
                .findById(categoryid);
    }


    @Override
    public Category findByCategoryname(
            String name) {

        return categoryRepository
                .findByCategoryname(name);
    }


    @Override
    public List<Category> findAll() {

        return categoryRepository
                .findAll();
    }


    @Override
    public List<Category> searchByName(
            String categoryname) {

        return categoryRepository
                .searchByName(categoryname);
    }


    @Override
    public List<Category> findAll(
            int page,
            int pagesize) {

        return categoryRepository
                .findAll(page, pagesize);
    }


    @Override
    public int count() {

        return categoryRepository.count();
    }
}
