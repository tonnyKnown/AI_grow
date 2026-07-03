package com.oa.system.mapper;

import com.oa.system.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface OrderMapper {
    List<Order> selectOrderPage(@Param("userId") Long userId, @Param("orderNo") String orderNo, @Param("offset") int offset, @Param("limit") int limit);
    long countOrderPage(@Param("userId") Long userId, @Param("orderNo") String orderNo);
    Optional<Order> selectById(@Param("id") Long id);
    Optional<Order> selectByOrderNo(@Param("orderNo") String orderNo);
    int insert(Order order);
    int update(Order order);
    int deleteById(@Param("id") Long id);
}
