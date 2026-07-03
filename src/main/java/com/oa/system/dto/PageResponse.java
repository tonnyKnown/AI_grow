package com.oa.system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    private Long total;
    private Integer pageNum;
    private Integer pageSize;
    private Integer totalPages;
    private List<T> records;
    
    public static <T> PageResponse<T> of(Long total, Integer pageNum, Integer pageSize, List<T> records) {
        int totalPages = (int) Math.ceil((double) total / pageSize);
        return new PageResponse<>(total, pageNum, pageSize, totalPages, records);
    }
}
