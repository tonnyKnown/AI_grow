package com.oa.business.mapper;

import com.oa.business.entity.Order;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OrderMapper {
    @Select("SELECT id, order_no AS orderNo, user_id AS userId, product_name AS productName, quantity, unit_price AS unitPrice, total_amount AS totalAmount, receiver_name AS receiverName, receiver_phone AS receiverPhone, shipping_address AS shippingAddress, status, create_by AS createBy, create_time AS createTime, update_by AS updateBy, update_time AS updateTime, remark FROM sys_order")
    List<Order> selectAll();

    @Select("SELECT id, order_no AS orderNo, user_id AS userId, product_name AS productName, quantity, unit_price AS unitPrice, total_amount AS totalAmount, receiver_name AS receiverName, receiver_phone AS receiverPhone, shipping_address AS shippingAddress, status, create_by AS createBy, create_time AS createTime, update_by AS updateBy, update_time AS updateTime, remark FROM sys_order WHERE id = #{id}")
    Order selectById(@Param("id") Long id);

    @Select("SELECT id, order_no AS orderNo, user_id AS userId, product_name AS productName, quantity, unit_price AS unitPrice, total_amount AS totalAmount, receiver_name AS receiverName, receiver_phone AS receiverPhone, shipping_address AS shippingAddress, status, create_by AS createBy, create_time AS createTime, update_by AS updateBy, update_time AS updateTime, remark FROM sys_order WHERE order_no = #{orderNo}")
    Order selectByOrderNo(@Param("orderNo") String orderNo);

    @Select("<script>" +
            "SELECT id, order_no AS orderNo, user_id AS userId, product_name AS productName, quantity, unit_price AS unitPrice, total_amount AS totalAmount, receiver_name AS receiverName, receiver_phone AS receiverPhone, shipping_address AS shippingAddress, status, create_by AS createBy, create_time AS createTime, update_by AS updateBy, update_time AS updateTime, remark FROM sys_order" +
            " WHERE 1=1" +
            "<if test='orderNo != null and orderNo != \"\"'>" +
            " AND order_no LIKE CONCAT('%', #{orderNo}, '%')" +
            "</if>" +
            "<if test='productName != null and productName != \"\"'>" +
            " AND product_name LIKE CONCAT('%', #{productName}, '%')" +
            "</if>" +
            "<if test='status != null'>" +
            " AND status = #{status}" +
            "</if>" +
            " ORDER BY create_time DESC" +
            "</script>")
    List<Order> selectByCondition(@Param("orderNo") String orderNo,
                                   @Param("productName") String productName,
                                   @Param("status") Integer status);

    @Select("<script>" +
            "SELECT id, order_no AS orderNo, user_id AS userId, product_name AS productName, quantity, unit_price AS unitPrice, total_amount AS totalAmount, receiver_name AS receiverName, receiver_phone AS receiverPhone, shipping_address AS shippingAddress, status, create_by AS createBy, create_time AS createTime, update_by AS updateBy, update_time AS updateTime, remark FROM sys_order" +
            " WHERE 1=1" +
            "<if test='userId != null'>" +
            " AND user_id = #{userId}" +
            "</if>" +
            "<if test='orderNo != null and orderNo != \"\"'>" +
            " AND order_no LIKE CONCAT('%', #{orderNo}, '%')" +
            "</if>" +
            "<if test='status != null'>" +
            " AND status = #{status}" +
            "</if>" +
            " ORDER BY create_time DESC" +
            "</script>")
    List<Order> selectByUserIdOrderNoStatus(@Param("userId") Long userId,
                                             @Param("orderNo") String orderNo,
                                             @Param("status") Integer status);

    @Select("<script>" +
            "SELECT id, order_no AS orderNo, user_id AS userId, product_name AS productName, quantity, unit_price AS unitPrice, total_amount AS totalAmount, receiver_name AS receiverName, receiver_phone AS receiverPhone, shipping_address AS shippingAddress, status, create_by AS createBy, create_time AS createTime, update_by AS updateBy, update_time AS updateTime, remark FROM sys_order" +
            " WHERE 1=1" +
            "<if test='userId != null'>" +
            " AND user_id = #{userId}" +
            "</if>" +
            "<if test='status != null'>" +
            " AND status = #{status}" +
            "</if>" +
            " ORDER BY create_time DESC" +
            "</script>")
    List<Order> selectByUserIdAndStatus(@Param("userId") Long userId,
                                          @Param("status") Integer status);

    @Insert("INSERT INTO sys_order (order_no, user_id, product_name, quantity, unit_price, total_amount, receiver_name, receiver_phone, shipping_address, status, create_by, create_time, remark) " +
            "VALUES (#{orderNo}, #{userId}, #{productName}, #{quantity}, #{unitPrice}, #{totalAmount}, #{receiverName}, #{receiverPhone}, #{shippingAddress}, #{status}, #{createBy}, NOW(), #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Order order);

    @Update("<script>" +
            "UPDATE sys_order SET update_by=#{updateBy}, update_time=NOW()" +
            "<if test='status != null'>" +
            ", status=#{status}" +
            "</if>" +
            "<if test='shippingAddress != null'>" +
            ", shipping_address=#{shippingAddress}" +
            "</if>" +
            "<if test='receiverName != null'>" +
            ", receiver_name=#{receiverName}" +
            "</if>" +
            "<if test='receiverPhone != null'>" +
            ", receiver_phone=#{receiverPhone}" +
            "</if>" +
            "<if test='remark != null'>" +
            ", remark=#{remark}" +
            "</if>" +
            " WHERE id=#{id}" +
            "</script>")
    int updateOrder(Order order);

    @Update("UPDATE sys_order SET status=#{status}, update_by=#{updateBy}, update_time=NOW() WHERE id=#{id}")
    int updateStatus(Order order);

    @Delete("DELETE FROM sys_order WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM sys_order")
    long count();
}
