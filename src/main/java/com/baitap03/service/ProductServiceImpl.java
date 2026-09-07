package com.baitap03.service;

import java.math.BigDecimal;
import java.util.List;

import com.baitap03.model.Product;
import com.baitap03.repository.IProductRepository;
import com.baitap03.repository.ProductRepository;

public class ProductServiceImpl implements IProductService {

    private final IProductRepository productRepository = new ProductRepository();

    @Override
    public void insert(Product product) {
        validate(product);
        productRepository.insert(product);
    }

    @Override
    public void update(Product product) {
        validate(product);
        productRepository.update(product);
    }

    @Override
    public void delete(int productId) {
        productRepository.delete(productId);
    }

    @Override
    public Product findById(int productId) {
        return productRepository.findById(productId);
    }

    @Override
    public Product findActiveById(int productId) {
        return productRepository.findActiveById(productId);
    }

    @Override
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Override
    public List<Product> findLatestActive(int limit) {
        return productRepository.findLatestActive(limit);
    }

    @Override
    public List<Product> findActivePage(int page, int pageSize) {
        return productRepository.findActivePage(page, pageSize);
    }

    @Override
    public List<Product> findActivePageByCategory(
            int categoryId,
            int page,
            int pageSize) {
        return productRepository.findActivePageByCategory(categoryId, page, pageSize);
    }

    @Override
    public long countActive() {
        return productRepository.countActive();
    }

    @Override
    public long countActiveByCategory(int categoryId) {
        return productRepository.countActiveByCategory(categoryId);
    }

    private void validate(Product product) {
        if (product.getProductname() == null || product.getProductname().isBlank()) {
            throw new IllegalArgumentException("Tên sản phẩm không được để trống");
        }
        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Giá sản phẩm không hợp lệ");
        }
        if (product.getQuantity() < 0) {
            throw new IllegalArgumentException("Số lượng không hợp lệ");
        }
        if (product.getCategory() == null) {
            throw new IllegalArgumentException("Vui lòng chọn danh mục");
        }
        product.setProductname(product.getProductname().trim());
    }
}
