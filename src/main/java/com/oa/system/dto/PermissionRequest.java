package com.oa.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PermissionRequest {
    private Long id;

    @NotBlank(message = "权限名称不能为空")
    private String permissionName;

    @NotBlank(message = "权限标识不能为空")
    private String permissionKey;

    private String resourceType;

    private String path;

    private String component;

    private Long parentId;

    private Integer orderNum = 0;

    private Integer status = 1;

    private String remark;
}
