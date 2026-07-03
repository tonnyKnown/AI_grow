package com.oa.system.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

@Mapper
public interface DashboardMapper {
    Map<String, Object> getDashboardStats();
}
