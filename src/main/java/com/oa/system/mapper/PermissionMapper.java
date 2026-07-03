package com.oa.system.mapper;

import com.oa.system.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface PermissionMapper {

    List<Permission> selectPermissionPage(@Param("offset") int offset, @Param("limit") int limit);

    long countPermissionPage();

    Optional<Permission> selectById(@Param("id") Long id);

    List<Permission> selectByStatus(@Param("status") Integer status);

    List<Permission> selectByParentId(@Param("parentId") Long parentId);

    List<Permission> selectByRoleId(@Param("roleId") Long roleId);

    List<Permission> selectByRoleIds(@Param("roleIds") List<Long> roleIds);

    int insert(Permission permission);

    int update(Permission permission);

    int deleteById(@Param("id") Long id);

    long countAll();
}
