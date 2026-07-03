package com.oa.system.mapper;

import com.oa.system.entity.RolePermission;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RolePermissionMapper {
    @Select("SELECT * FROM sys_role_permission WHERE role_id = #{roleId}")
    List<RolePermission> selectByRoleId(@Param("roleId") Long roleId);

    @Select("SELECT permission_id FROM sys_role_permission WHERE role_id = #{roleId}")
    List<Long> selectPermissionIdsByRoleId(@Param("roleId") Long roleId);

    @Insert("<script>" +
            "INSERT INTO sys_role_permission (role_id, permission_id) VALUES " +
            "<foreach collection='permissionIds' item='pid' separator=','>" +
            "(#{roleId}, #{pid})" +
            "</foreach>" +
            "</script>")
    int insertBatch(@Param("roleId") Long roleId, @Param("permissionIds") List<Long> permissionIds);

    @Delete("DELETE FROM sys_role_permission WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") Long roleId);
}
