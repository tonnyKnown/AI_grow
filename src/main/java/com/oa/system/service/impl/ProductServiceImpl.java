package com.oa.system.service.impl;

import com.oa.system.dto.PageResponse;
import com.oa.system.dto.ProductRequest;
import com.oa.system.dto.SPageRequest;
import com.oa.system.entity.Product;
import com.oa.system.mapper.ProductMapper;
import com.oa.system.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Override
    public PageResponse<Product> getProductPage(SPageRequest request) {
        int offset = (request.getPageNum() - 1) * request.getPageSize();
        String category = request.getKeyword();
        var page = productMapper.selectProductPage(offset, request.getPageSize(), category, request.getStartTime(), request.getEndTime());
        long total = productMapper.countProductPage(category, request.getStartTime(), request.getEndTime());

        return PageResponse.of(total, request.getPageNum(), request.getPageSize(), page);
    }

    @Override
    public Product getProductById(Long id) {
        return productMapper.selectById(id)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
    }

    @Override
    @Transactional
    public void createProduct(ProductRequest request) {
        if (request.getProductCode() != null && productMapper.existsByProductCode(request.getProductCode())) {
            throw new RuntimeException("商品编码已存在");
        }

        Product product = new Product();
        product.setProductName(request.getProductName());
        product.setProductCode(request.getProductCode());
        product.setCategory(request.getCategory());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock() != null ? request.getStock() : 0);
        product.setDescription(request.getDescription());
        product.setImageUrl(request.getImageUrl());
        product.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        product.setCreateBy(1L);
        product.setRemark(request.getRemark());

        productMapper.insert(product);
    }

    @Override
    @Transactional
    public void updateProduct(ProductRequest request) {
        Product product = productMapper.selectById(request.getId())
                .orElseThrow(() -> new RuntimeException("商品不存在"));

        product.setProductName(request.getProductName());
        product.setProductCode(request.getProductCode());
        product.setCategory(request.getCategory());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setDescription(request.getDescription());
        product.setImageUrl(request.getImageUrl());
        product.setStatus(request.getStatus());
        product.setUpdateBy(1L);
        product.setUpdateTime(LocalDateTime.now());
        product.setRemark(request.getRemark());

        productMapper.update(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        productMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void updateStock(Long id, Integer quantity) {
        Product product = productMapper.selectById(id)
                .orElseThrow(() -> new RuntimeException("商品不存在"));

        product.setStock(product.getStock() + quantity);
        product.setUpdateBy(1L);
        product.setUpdateTime(LocalDateTime.now());

        productMapper.update(product);
    }
}
