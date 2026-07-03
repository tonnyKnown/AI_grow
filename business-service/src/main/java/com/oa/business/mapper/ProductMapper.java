package com.oa.business.mapper;

import com.oa.business.entity.Product;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ProductMapper {
    @Select("SELECT id, product_name AS productName, product_code AS productCode, category, price, stock, description, image_url AS imageUrl, status, create_by AS createBy, create_time AS createTime, update_by AS updateBy, update_time AS updateTime, remark FROM sys_product")
    List<Product> selectAll();

    @Select("SELECT id, product_name AS productName, product_code AS productCode, category, price, stock, description, image_url AS imageUrl, status, create_by AS createBy, create_time AS createTime, update_by AS updateBy, update_time AS updateTime, remark FROM sys_product WHERE id = #{id}")
    Product selectById(@Param("id") Long id);

    @Insert("INSERT INTO sys_product (product_name, product_code, category, price, stock, description, image_url, status, create_by, create_time) " +
            "VALUES (#{productName}, #{productCode}, #{category}, #{price}, #{stock}, #{description}, #{imageUrl}, #{status}, #{createBy}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Product product);

    @Update("UPDATE sys_product SET product_name=#{productName}, product_code=#{productCode}, category=#{category}, price=#{price}, stock=#{stock}, " +
            "description=#{description}, image_url=#{imageUrl}, status=#{status}, update_by=#{updateBy}, update_time=NOW() WHERE id=#{id}")
    int update(Product product);

    @Delete("DELETE FROM sys_product WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM sys_product")
    long count();
}
