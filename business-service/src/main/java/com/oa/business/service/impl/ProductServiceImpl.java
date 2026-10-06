package com.oa.business.service.impl;

import com.oa.business.common.PageUtils;
import com.oa.business.dto.PageResponse;
import com.oa.business.dto.ProductRequest;
import com.oa.business.entity.Product;
import com.oa.business.mapper.ProductMapper;
import com.oa.business.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Override
    public Product getProductById(Long id) {
        return productMapper.selectById(id);
    }

    @Override
    public List<Product> getAllProducts() {
        return productMapper.selectAll();
    }

    @Override
    public PageResponse<Product> getProductsPage(int pageNum, int pageSize) {
        List<Product> all = productMapper.selectAll();
        int safePageNum = PageUtils.normalizePageNum(pageNum);
        int safePageSize = PageUtils.normalizePageSize(pageSize);
        List<Product> records = PageUtils.slice(all, safePageNum, safePageSize);
        return PageResponse.of(all.size(), safePageNum, safePageSize, records);
    }

    @Override
    public void createProduct(ProductRequest request, Long userId) {
        Product product = new Product();
        product.setProductName(request.getProductName());
        product.setProductCode(request.getProductCode());
        product.setCategory(request.getCategory());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setDescription(request.getDescription());
        product.setImageUrl(request.getImageUrl());
        product.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        product.setCreateBy(userId);
        product.setCreateTime(LocalDateTime.now());
        productMapper.insert(product);
    }

    @Override
    public void updateProduct(ProductRequest request,Long userId) {
        Product product = productMapper.selectById(request.getId());
        if (product != null) {
            product.setProductName(request.getProductName());
            product.setProductCode(request.getProductCode());
            product.setCategory(request.getCategory());
            product.setPrice(request.getPrice());
            product.setStock(request.getStock());
            product.setDescription(request.getDescription());
            product.setImageUrl(request.getImageUrl());
            product.setStatus(request.getStatus());
            product.setUpdateBy(userId);
            product.setUpdateTime(LocalDateTime.now());
            productMapper.update(product);
        }
    }

    @Override
    public void deleteProduct(Long id) {
        productMapper.deleteById(id);
    }
}
