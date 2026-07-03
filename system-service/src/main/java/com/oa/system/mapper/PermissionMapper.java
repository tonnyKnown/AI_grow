package com.oa.system.mapper;

import com.oa.system.entity.Permission;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PermissionMapper {
    @Select("SELECT * FROM sys_permission")
    List<Permission> selectAll();

    @Select("SELECT * FROM sys_permission WHERE parent_id = #{parentId}")
    List<Permission> selectByParentId(@Param("parentId") Long parentId);

    @Select("SELECT * FROM sys_permission WHERE id = #{id}")
    Permission selectById(@Param("id") Long id);

    @Select("SELECT * FROM sys_permission LIMIT #{offset}, #{limit}")
    List<Permission> selectPage(@Param("offset") int offset, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM sys_permission")
    long count();

    @Insert("INSERT INTO sys_permission (permission_name, permission_key, resource_type, path, component, parent_id, order_num, status, create_by, create_time) " +
            "VALUES (#{permissionName}, #{permissionKey}, #{resourceType}, #{path}, #{component}, #{parentId}, #{orderNum}, #{status}, #{createBy}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Permission permission);

    @Update("UPDATE sys_permission SET permission_name=#{permissionName}, permission_key=#{permissionKey}, " +
            "resource_type=#{resourceType}, path=#{path}, component=#{component}, parent_id=#{parentId}, " +
            "order_num=#{orderNum}, status=#{status}, update_by=#{updateBy}, update_time=NOW() WHERE id=#{id}")
    int update(Permission permission);

    @Delete("DELETE FROM sys_permission WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}
