package com.oa.system.mapper;

import com.oa.system.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface RoleMapper {

    List<Role> selectRolePage(@Param("offset") int offset, @Param("limit") int limit);

    long countRolePage();

    Optional<Role> selectById(@Param("id") Long id);

    Optional<Role> selectByRoleKey(@Param("roleKey") String roleKey);

    List<Role> selectByStatus(@Param("status") Integer status);

    List<Role> selectByIds(@Param("roleIds") List<Long> roleIds);

    boolean existsByRoleKey(@Param("roleKey") String roleKey);

    int insert(Role role);

    int update(Role role);

    int deleteById(@Param("id") Long id);

    long countAll();
}
