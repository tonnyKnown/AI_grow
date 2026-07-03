package com.oa.business.mapper;

import com.oa.business.entity.Category;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CategoryMapper {
    @Select("SELECT * FROM sys_category")
    List<Category> selectAll();

    @Select("SELECT * FROM sys_category WHERE id = #{id}")
    Category selectById(@Param("id") Long id);

    @Insert("INSERT INTO sys_category (name, description, sort, status, create_by, create_time) " +
            "VALUES (#{name}, #{description}, #{sort}, #{status}, #{createBy}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Category category);

    @Update("UPDATE sys_category SET name=#{name}, description=#{description}, sort=#{sort}, status=#{status}, " +
            "update_by=#{updateBy}, update_time=NOW() WHERE id=#{id}")
    int update(Category category);

    @Delete("DELETE FROM sys_category WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}
