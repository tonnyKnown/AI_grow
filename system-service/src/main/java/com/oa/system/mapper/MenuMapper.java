package com.oa.system.mapper;

import com.oa.system.entity.Menu;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MenuMapper {
    @Select("SELECT * FROM sys_menu")
    List<Menu> selectAll();

    @Select("SELECT * FROM sys_menu WHERE id = #{id}")
    Menu selectById(@Param("id") Long id);

    @Select("SELECT * FROM sys_menu WHERE parent_id = #{parentId}")
    List<Menu> selectByParentId(@Param("parentId") Long parentId);

    @Select("SELECT * FROM sys_menu WHERE FIND_IN_SET(#{roleKey}, role_keys)")
    List<Menu> selectByRoleKey(@Param("roleKey") String roleKey);

    @Insert("INSERT INTO sys_menu (menu_name, path, component, icon, parent_id, order_num, role_keys, status, create_by, create_time) " +
            "VALUES (#{menuName}, #{path}, #{component}, #{icon}, #{parentId}, #{orderNum}, #{roleKeys}, #{status}, #{createBy}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Menu menu);

    @Update("UPDATE sys_menu SET menu_name=#{menuName}, path=#{path}, component=#{component}, icon=#{icon}, " +
            "parent_id=#{parentId}, order_num=#{orderNum}, role_keys=#{roleKeys}, status=#{status}, " +
            "update_by=#{updateBy}, update_time=NOW() WHERE id=#{id}")
    int update(Menu menu);

    @Delete("DELETE FROM sys_menu WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}
