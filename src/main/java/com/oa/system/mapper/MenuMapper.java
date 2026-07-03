package com.oa.system.mapper;

import com.oa.system.entity.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MenuMapper {

    List<Menu> selectByRoleKeys(@Param("roleKeys") List<String> roleKeys);

    List<Menu> selectAll();

    Menu selectById(Long id);

    int insert(Menu menu);

    int update(Menu menu);

    int deleteById(Long id);

    List<Menu> selectByParentId(Long parentId);
}