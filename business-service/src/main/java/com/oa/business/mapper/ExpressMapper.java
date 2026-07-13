package com.oa.business.mapper;

import com.oa.business.entity.Express;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ExpressMapper {

    @Select("SELECT id, order_id AS orderId, order_no AS orderNo, express_company AS expressCompany, express_no AS expressNo, status, tracking_nodes AS trackingNodes, sender_name AS senderName, sender_phone AS senderPhone, sender_address AS senderAddress, remark, create_by AS createBy, create_time AS createTime, update_by AS updateBy, update_time AS updateTime FROM sys_express WHERE order_id = #{orderId}")
    Express selectByOrderId(@Param("orderId") Long orderId);

    @Select("SELECT id, order_id AS orderId, order_no AS orderNo, express_company AS expressCompany, express_no AS expressNo, status, tracking_nodes AS trackingNodes, sender_name AS senderName, sender_phone AS senderPhone, sender_address AS senderAddress, remark, create_by AS createBy, create_time AS createTime, update_by AS updateBy, update_time AS updateTime FROM sys_express WHERE id = #{id}")
    Express selectById(@Param("id") Long id);

    @Select("SELECT id, order_id AS orderId, order_no AS orderNo, express_company AS expressCompany, express_no AS expressNo, status, tracking_nodes AS trackingNodes, sender_name AS senderName, sender_phone AS senderPhone, sender_address AS senderAddress, remark, create_by AS createBy, create_time AS createTime, update_by AS updateBy, update_time AS updateTime FROM sys_express WHERE express_no = #{expressNo} LIMIT 1")
    Express selectByOrderNo(@Param("expressNo") String expressNo);

    @Select("<script>" +
            "SELECT e.id, e.order_id AS orderId, e.order_no AS orderNo, e.express_company AS expressCompany, e.express_no AS expressNo, e.status, e.tracking_nodes AS trackingNodes, e.sender_name AS senderName, e.sender_phone AS senderPhone, e.sender_address AS senderAddress, e.remark, e.create_by AS createBy, e.create_time AS createTime, e.update_by AS updateBy, e.update_time AS updateTime" +
            " FROM sys_express e" +
            " WHERE 1=1" +
            "<if test='orderNo != null and orderNo != \"\"'>" +
            " AND e.order_no LIKE CONCAT('%', #{orderNo}, '%')" +
            "</if>" +
            "<if test='expressCompany != null and expressCompany != \"\"'>" +
            " AND e.express_company = #{expressCompany}" +
            "</if>" +
            "<if test='status != null'>" +
            " AND e.status = #{status}" +
            "</if>" +
            " ORDER BY e.create_time DESC" +
            "</script>")
    List<Express> selectByCondition(@Param("orderNo") String orderNo,
                                    @Param("expressCompany") String expressCompany,
                                    @Param("status") Integer status);

    @Select("SELECT COUNT(*) FROM sys_express")
    long count();

    @Select("SELECT id, order_id AS orderId, order_no AS orderNo, express_company AS expressCompany, express_no AS expressNo, status, tracking_nodes AS trackingNodes, sender_name AS senderName, sender_phone AS senderPhone, sender_address AS senderAddress, remark, create_by AS createBy, create_time AS createTime, update_by AS updateBy, update_time AS updateTime FROM sys_express WHERE status &lt; 3 ORDER BY create_time ASC")
    List<Express> selectActive();

    @Insert("INSERT INTO sys_express (order_id, order_no, express_company, express_no, status, tracking_nodes, sender_name, sender_phone, sender_address, remark, create_by, create_time) " +
            "VALUES (#{orderId}, #{orderNo}, #{expressCompany}, #{expressNo}, #{status}, #{trackingNodes}, #{senderName}, #{senderPhone}, #{senderAddress}, #{remark}, #{createBy}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Express express);

    @Update("<script>" +
            "UPDATE sys_express SET update_by=#{updateBy}, update_time=NOW()" +
            "<if test='expressCompany != null'>, express_company=#{expressCompany}</if>" +
            "<if test='expressNo != null'>, express_no=#{expressNo}</if>" +
            "<if test='status != null'>, status=#{status}</if>" +
            "<if test='trackingNodes != null'>, tracking_nodes=#{trackingNodes}</if>" +
            "<if test='senderName != null'>, sender_name=#{senderName}</if>" +
            "<if test='senderPhone != null'>, sender_phone=#{senderPhone}</if>" +
            "<if test='senderAddress != null'>, sender_address=#{senderAddress}</if>" +
            "<if test='remark != null'>, remark=#{remark}</if>" +
            " WHERE id=#{id}" +
            "</script>")
    int update(Express express);

    @Delete("DELETE FROM sys_express WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Delete("DELETE FROM sys_express WHERE order_id = #{orderId}")
    int deleteByOrderId(@Param("orderId") Long orderId);
}
