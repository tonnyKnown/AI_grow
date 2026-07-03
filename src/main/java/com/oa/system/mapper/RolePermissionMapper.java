package com.oa.system.mapper;

import com.oa.system.entity.RolePermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RolePermissionMapper {

    List<RolePermission> selectByRoleId(@Param("roleId") Long roleId);

    int insert(RolePermission rolePermission);

    int deleteByRoleId(@Param("roleId") Long roleId);

    int deleteByPermissionId(@Param("permissionId") Long permissionId);
}
