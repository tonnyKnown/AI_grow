package com.oa.business.mapper;

import com.oa.business.entity.Marketing;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MarketingMapper {
    @Select("<script>" +
            "SELECT id, name, type, description, start_time AS startTime, end_time AS endTime, status, create_by AS createBy, create_time AS createTime, update_by AS updateBy, update_time AS updateTime, remark FROM marketing" +
            " ORDER BY start_time" +
            "<if test='sortOrder != null and sortOrder == \"desc\"'>" +
            " DESC" +
            "</if>" +
            "<if test='sortOrder == null or sortOrder == \"asc\"'>" +
            " ASC" +
            "</if>" +
            "</script>")
    List<Marketing> selectAll(@Param("sortOrder") String sortOrder);

    @Select("SELECT id, name, type, description, start_time AS startTime, end_time AS endTime, status, create_by AS createBy, create_time AS createTime, update_by AS updateBy, update_time AS updateTime, remark FROM marketing WHERE id = #{id}")
    Marketing selectById(@Param("id") Long id);

    @Insert("INSERT INTO marketing (name, type, description, start_time, end_time, status, create_by, create_time, remark) " +
            "VALUES (#{name}, #{type}, #{description}, #{startTime}, #{endTime}, #{status}, #{createBy}, NOW(), #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Marketing marketing);

    @Update("UPDATE marketing SET name=#{name}, type=#{type}, description=#{description}, start_time=#{startTime}, end_time=#{endTime}, status=#{status}, update_by=#{updateBy}, update_time=NOW(), remark=#{remark} WHERE id=#{id}")
    int update(Marketing marketing);

    @Delete("DELETE FROM marketing WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM marketing")
    long count();
}
