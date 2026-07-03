package com.oa.system.controller;

import com.oa.system.common.Result;
import com.oa.system.dto.CategoryRequest;
import com.oa.system.dto.PageResponse;
import com.oa.system.dto.SPageRequest;
import com.oa.system.entity.Category;
import com.oa.system.service.CategoryService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    @Resource
    private CategoryService categoryService;
    
    @GetMapping
    public Result<PageResponse<Category>> getCategoryPage(SPageRequest request) {
        PageResponse<Category> response = categoryService.getCategoryPage(request);
        return Result.success(response);
    }
    
    @GetMapping("/all")
    public Result<List<Category>> getAllCategories() {
        List<Category> response = categoryService.getAllCategories();
        return Result.success(response);
    }
    
    @GetMapping("/{id}")
    public Result<Category> getCategoryById(@PathVariable Long id) {
        Category response = categoryService.getCategoryById(id);
        return Result.success(response);
    }
    
    @PostMapping
    public Result<String> createCategory(@Valid @RequestBody CategoryRequest request) {
        categoryService.createCategory(request);
        return Result.success("分类创建成功");
    }
    
    @PutMapping
    public Result<String> updateCategory(@Valid @RequestBody CategoryRequest request) {
        categoryService.updateCategory(request);
        return Result.success("分类更新成功");
    }
    
    @DeleteMapping("/{id}")
    public Result<String> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success("分类删除成功");
    }
}
