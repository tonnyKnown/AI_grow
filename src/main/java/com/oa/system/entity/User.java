package com.oa.system.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户实体类
 * 
 * 用于存储系统用户的基本信息，包括用户名、密码、联系方式、状态等
 */
@Data
public class User {
    /**
     * 用户ID，主键，自增
     */
    private Long id;
    
    /**
     * 用户名，登录账号，唯一
     */
    private String username;
    
    /**
     * 密码，加密存储
     */
    private String password;
    
    /**
     * 电子邮箱
     */
    private String email;
    
    /**
     * 手机号码
     */
    private String phone;
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 用户头像URL
     */
    private String avatar;
    
    /**
     * 用户状态 0禁用 1启用
     */
    private Integer status = 1;
    
    /**
     * 创建人ID
     */
    private Long createBy;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新人ID
     */
    private Long updateBy;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 备注信息
     */
    private String remark;
}
