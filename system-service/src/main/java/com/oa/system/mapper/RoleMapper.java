package com.oa.system.mapper;

import com.oa.system.entity.Role;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RoleMapper {
    @Select("SELECT * FROM sys_role WHERE id = #{id}")
    Role selectById(@Param("id") Long id);

    @Select("SELECT * FROM sys_role")
    List<Role> selectAll();

    @Select("SELECT * FROM sys_role LIMIT #{offset}, #{limit}")
    List<Role> selectPage(@Param("offset") int offset, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM sys_role")
    long count();

    @Insert("INSERT INTO sys_role (role_name, role_key, description, status, create_by, create_time) " +
            "VALUES (#{roleName}, #{roleKey}, #{description}, #{status}, #{createBy}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Role role);

    @Update("UPDATE sys_role SET role_name=#{roleName}, role_key=#{roleKey}, description=#{description}, " +
            "status=#{status}, update_by=#{updateBy}, update_time=NOW() WHERE id=#{id}")
    int update(Role role);

    @Delete("DELETE FROM sys_role WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}
