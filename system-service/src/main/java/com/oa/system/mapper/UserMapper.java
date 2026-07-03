package com.oa.system.mapper;

import com.oa.system.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

@Mapper
public interface UserMapper {
    @Select("SELECT * FROM sys_user WHERE id = #{id}")
    User selectById(@Param("id") Long id);

    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    Optional<User> selectByUsername(@Param("username") String username);

    @Select("SELECT * FROM sys_user WHERE email = #{email}")
    Optional<User> selectByEmail(@Param("email") String email);

    @Select("SELECT * FROM sys_user")
    List<User> selectAll();

    @Select("SELECT * FROM sys_user WHERE real_name LIKE CONCAT('%', #{realName}, '%')")
    List<User> selectByRealName(@Param("realName") String realName);

    @Insert("INSERT INTO sys_user (username, password, email, phone, real_name, status, create_by, create_time) " +
            "VALUES (#{username}, #{password}, #{email}, #{phone}, #{realName}, #{status}, #{createBy}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Update("UPDATE sys_user SET username=#{username}, email=#{email}, phone=#{phone}, " +
            "real_name=#{realName}, status=#{status}, update_by=#{updateBy}, update_time=NOW() WHERE id=#{id}")
    int update(User user);

    @Update("UPDATE sys_user SET password=#{password}, update_by=#{updateBy}, update_time=NOW() WHERE id=#{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password, @Param("updateBy") String updateBy);

    @Delete("DELETE FROM sys_user WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM sys_user")
    long count();

    @Select("SELECT COUNT(*) > 0 FROM sys_user WHERE username = #{username}")
    boolean existsByUsername(@Param("username") String username);

    @Select("SELECT COUNT(*) > 0 FROM sys_user WHERE email = #{email}")
    boolean existsByEmail(@Param("email") String email);
}
