package com.oa.system.service;

import com.oa.system.dto.*;
import com.oa.system.entity.Product;

public interface ProductService {
    PageResponse<Product> getProductPage(SPageRequest request);
    Product getProductById(Long id);
    void createProduct(ProductRequest request);
    void updateProduct(ProductRequest request);
    void deleteProduct(Long id);
    void updateStock(Long id, Integer quantity);
}
