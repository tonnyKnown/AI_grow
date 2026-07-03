package com.oa.system.mapper;

import com.oa.system.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ProductMapper {

    List<Product> selectProductPage(@Param("offset") int offset, @Param("limit") int limit, @Param("category") String category, @Param("startTime") String startTime, @Param("endTime") String endTime);

    long countProductPage(@Param("category") String category, @Param("startTime") String startTime, @Param("endTime") String endTime);

    Optional<Product> selectById(@Param("id") Long id);

    Optional<Product> selectByProductCode(@Param("productCode") String productCode);

    List<Product> selectByCategory(@Param("category") String category);

    List<Product> selectByStatus(@Param("status") Integer status);

    boolean existsByProductCode(@Param("productCode") String productCode);

    int insert(Product product);

    int update(Product product);

    int deleteById(@Param("id") Long id);
}
