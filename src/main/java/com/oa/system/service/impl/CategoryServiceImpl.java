package com.oa.system.service.impl;

import com.oa.system.dto.CategoryRequest;
import com.oa.system.dto.PageResponse;
import com.oa.system.dto.SPageRequest;
import com.oa.system.entity.Category;
import com.oa.system.mapper.CategoryMapper;
import com.oa.system.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public PageResponse<Category> getCategoryPage(SPageRequest request) {
        int offset = (request.getPageNum() - 1) * request.getPageSize();
        var page = categoryMapper.selectCategoryPage(offset, request.getPageSize());
        long total = categoryMapper.countCategoryPage();

        return PageResponse.of(total, request.getPageNum(), request.getPageSize(), page);
    }

    @Override
    public Category getCategoryById(Long id) {
        return categoryMapper.selectById(id)
                .orElseThrow(() -> new RuntimeException("分类不存在"));
    }

    @Override
    @Transactional
    public void createCategory(CategoryRequest request) {
        if (categoryMapper.existsByCategoryCode(request.getCategoryCode())) {
            throw new RuntimeException("分类编码已存在");
        }

        Category category = new Category();
        category.setCategoryName(request.getCategoryName());
        category.setCategoryCode(request.getCategoryCode());
        category.setSort(request.getSort() != null ? request.getSort() : 0);
        category.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        category.setParentId(request.getParentId());
        category.setRemark(request.getRemark());
        category.setCreateBy(1L);
        category.setCreateTime(LocalDateTime.now());

        categoryMapper.insert(category);
    }

    @Override
    @Transactional
    public void updateCategory(CategoryRequest request) {
        Category category = categoryMapper.selectById(request.getId())
                .orElseThrow(() -> new RuntimeException("分类不存在"));

        category.setCategoryName(request.getCategoryName());
        category.setCategoryCode(request.getCategoryCode());
        category.setSort(request.getSort());
        category.setStatus(request.getStatus());
        category.setParentId(request.getParentId());
        category.setRemark(request.getRemark());
        category.setUpdateBy(1L);
        category.setUpdateTime(LocalDateTime.now());

        categoryMapper.update(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        categoryMapper.deleteById(id);
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryMapper.selectAll();
    }
}
