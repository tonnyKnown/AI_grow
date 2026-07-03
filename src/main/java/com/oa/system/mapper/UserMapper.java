package com.oa.system.mapper;

import com.oa.system.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface UserMapper {

    List<User> selectUserPage(@Param("keyword") String keyword, @Param("offset") int offset, @Param("limit") int limit);

    long countUserPage(@Param("keyword") String keyword);

    Optional<User> selectById(@Param("id") Long id);

    Optional<User> selectByUsername(@Param("username") String username);

    boolean existsByUsername(@Param("username") String username);

    boolean existsByEmail(@Param("email") String email);

    List<User> selectByRealName(@Param("realName") String realName);

    int insert(User user);

    int update(User user);

    int deleteById(@Param("id") Long id);

    long countAll();
}
