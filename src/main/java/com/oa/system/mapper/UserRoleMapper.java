package com.oa.system.mapper;

import com.oa.system.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserRoleMapper {

    List<UserRole> selectByUserId(@Param("userId") Long userId);
    
    List<UserRole> selectByUserIdWithRole(@Param("userId") Long userId);

    int insert(UserRole userRole);

    int deleteByUserId(@Param("userId") Long userId);
}
