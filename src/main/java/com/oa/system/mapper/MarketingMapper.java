package com.oa.system.mapper;

import com.oa.system.entity.Marketing;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 营销活动数据访问接口
 */
@Mapper
public interface MarketingMapper {

    /**
     * 查询所有活动
     */
    List<Marketing> selectAll();

    /**
     * 根据ID查询活动
     */
    Marketing selectById(Long id);

    /**
     * 根据名称查询活动
     */
    List<Marketing> selectByName(@Param("name") String name);

    /**
     * 根据类型查询活动
     */
    List<Marketing> selectByType(@Param("type") String type);

    /**
     * 根据状态查询活动
     */
    List<Marketing> selectByStatus(@Param("status") Integer status);

    /**
     * 插入活动
     */
    int insert(Marketing marketing);

    /**
     * 更新活动
     */
    int update(Marketing marketing);

    /**
     * 删除活动
     */
    int deleteById(Long id);

    /**
     * 分页查询活动
     */
    List<Marketing> selectPage(
            @Param("name") String name,
            @Param("type") String type,
            @Param("status") Integer status,
            @Param("startRow") Integer startRow,
            @Param("pageSize") Integer pageSize
    );

    /**
     * 查询总记录数
     */
    int count(
            @Param("name") String name,
            @Param("type") String type,
            @Param("status") Integer status
    );
}