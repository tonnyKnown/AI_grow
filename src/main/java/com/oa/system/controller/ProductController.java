package com.oa.system.controller;

import com.oa.system.common.Result;
import com.oa.system.dto.PageResponse;
import com.oa.system.dto.ProductRequest;
import com.oa.system.dto.SPageRequest;
import com.oa.system.entity.Product;
import com.oa.system.service.ProductService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    @Resource
    private  ProductService productService;
    
    @GetMapping
    public Result<PageResponse<Product>> getProductPage(SPageRequest request) {
        PageResponse<Product> response = productService.getProductPage(request);
        return Result.success(response);
    }
    
    @GetMapping("/{id}")
    public Result<Product> getProductById(@PathVariable Long id) {
        Product response = productService.getProductById(id);
        return Result.success(response);
    }
    
    @PostMapping
    public Result<String> createProduct(@Valid @RequestBody ProductRequest request) {
        productService.createProduct(request);
        return Result.success("商品创建成功");
    }
    
    @PutMapping
    public Result<String> updateProduct(@Valid @RequestBody ProductRequest request) {
        productService.updateProduct(request);
        return Result.success("商品更新成功");
    }
    
    @DeleteMapping("/{id}")
    public Result<String> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return Result.success("商品删除成功");
    }
    
    @PutMapping("/{id}/stock")
    public Result<String> updateStock(@PathVariable Long id, @RequestParam Integer quantity) {
        productService.updateStock(id, quantity);
        return Result.success("库存更新成功");
    }
}
