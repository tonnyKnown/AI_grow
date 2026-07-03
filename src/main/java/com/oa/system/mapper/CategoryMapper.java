package com.oa.system.mapper;

import com.oa.system.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface CategoryMapper {

    List<Category> selectCategoryPage(@Param("offset") int offset, @Param("limit") int limit);

    long countCategoryPage();

    Optional<Category> selectById(@Param("id") Long id);

    Optional<Category> selectByCategoryCode(@Param("categoryCode") String categoryCode);

    List<Category> selectAll();

    List<Category> selectByParentId(@Param("parentId") Long parentId);

    boolean existsByCategoryCode(@Param("categoryCode") String categoryCode);

    int insert(Category category);

    int update(Category category);

    int deleteById(@Param("id") Long id);
}
