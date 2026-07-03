package com.oa.system.entity;

import lombok.Data;

import java.util.Date;

/**
 * 营销活动实体类
 */
@Data
public class Marketing {

    private Long id;
    private String name;
    private String type;
    private String description;
    private Date startTime;
    private Date endTime;
    private Integer status;
    private String rules;
    private String productIds;
    private String createBy;
    private String createByName;
    private Date createTime;
    private String updateBy;
    private Date updateTime;
    private String remark;
}