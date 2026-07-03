package com.oa.system.service;

import com.oa.system.dto.*;
import com.oa.system.entity.Category;

import java.util.List;

public interface CategoryService {
    PageResponse<Category> getCategoryPage(SPageRequest request);
    Category getCategoryById(Long id);
    void createCategory(CategoryRequest request);
    void updateCategory(CategoryRequest request);
    void deleteCategory(Long id);
    List<Category> getAllCategories();
}
