package com.oa.business.controller;

import com.oa.business.common.Result;
import com.oa.business.entity.Product;
import com.oa.business.dto.ProductRequest;
import com.oa.business.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/business/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public Result<Map<String, Object>> getAllProducts(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        List<Product> allProducts = productService.getAllProducts();
        int total = allProducts.size();
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        List<Product> records = start < total ? allProducts.subList(start, end) : List.of();
        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", total);
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);
        return Result.success(data);
    }

    @GetMapping("/{id}")
    public Result<Product> getProductById(@PathVariable Long id) {
        return Result.success(productService.getProductById(id));
    }

    @PostMapping
    public Result<Void> createProduct(@RequestBody ProductRequest request, @RequestHeader(value = "userId") Long userId) {
        productService.createProduct(request, userId);
        return Result.success("创建成功", null);
    }

    @PutMapping
    public Result<Void> updateProduct(@RequestBody ProductRequest request, @RequestHeader(value = "userId") Long userId) {
        productService.updateProduct(request, userId);
        return Result.success("更新成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return Result.success("删除成功", null);
    }
}
