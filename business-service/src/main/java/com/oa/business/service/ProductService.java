package com.oa.business.service;

import com.oa.business.dto.ProductRequest;
import com.oa.business.entity.Product;
import com.oa.business.dto.PageResponse;

import java.util.List;

public interface ProductService {
    Product getProductById(Long id);
    List<Product> getAllProducts();
    PageResponse<Product> getProductsPage(int pageNum, int pageSize);
    void createProduct(ProductRequest request, Long userId);
    void updateProduct(ProductRequest request, Long userId);
    void deleteProduct(Long id);
}
