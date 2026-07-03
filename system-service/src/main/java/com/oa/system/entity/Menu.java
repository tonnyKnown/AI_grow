package com.oa.system.entity;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class Menu {
    private Long id;
    private String menuName;
    private String path;
    private String component;
    private String icon;
    private Long parentId;
    private Integer orderNum;
    private String roleKeys;
    private Integer status;
    private Long createBy;
    private Date createTime;
    private Long updateBy;
    private Date updateTime;
    private String remark;
    private List<Menu> children;
}
