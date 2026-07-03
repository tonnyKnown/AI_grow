package com.oa.business.controller;

import com.oa.business.common.Result;
import com.oa.business.entity.Marketing;
import com.oa.business.service.MarketingService;
import com.oa.business.service.MarketingTypeService;
import com.oa.business.vo.MarketingTypeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/business/marketing")
public class MarketingController {

    @Autowired
    private MarketingService marketingService;

    @Autowired
    private MarketingTypeService marketingTypeService;

    @GetMapping
    public Result<Map<String, Object>> getAllMarketings(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "desc") String sortOrder) {
        List<Marketing> allMarketings = marketingService.getAllMarketings(sortOrder);
        int total = allMarketings.size();
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        List<Marketing> records = start < total ? allMarketings.subList(start, end) : List.of();
        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", total);
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);
        return Result.success(data);
    }

    @GetMapping("/{id}")
    public Result<Marketing> getMarketingById(@PathVariable Long id) {
        return Result.success(marketingService.getMarketingById(id));
    }

    @PostMapping
    public Result<Void> createMarketing(@RequestBody Marketing marketing,
                                        @RequestHeader(value = "userId") Long userId) {
        marketingService.createMarketing(marketing, userId);
        return Result.success("创建成功", null);
    }

    @PutMapping
    public Result<Void> updateMarketing(@RequestBody Marketing marketing,
                                        @RequestHeader(value = "userId") Long userId) {
        marketingService.updateMarketing(marketing, userId);
        return Result.success("更新成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteMarketing(@PathVariable Long id) {
        marketingService.deleteMarketing(id);
        return Result.success("删除成功", null);
    }

    @GetMapping("/types")
    public Result<List<MarketingTypeVO>> getMarketingTypes() {
        return Result.success(marketingTypeService.getAllTypes());
    }
}
