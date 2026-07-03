package com.oa.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryRequest {
    private Long id;

    @NotBlank(message = "分类名称不能为空")
    private String categoryName;

    @NotBlank(message = "分类编码不能为空")
    private String categoryCode;

    private Integer sort = 0;

    private Integer status = 1;

    private Long parentId;

    private String remark;
}
