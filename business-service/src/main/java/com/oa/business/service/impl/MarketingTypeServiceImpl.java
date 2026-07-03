package com.oa.business.service.impl;

import com.oa.business.enums.MarketingTypeEnum;
import com.oa.business.service.MarketingTypeService;
import com.oa.business.vo.MarketingTypeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class MarketingTypeServiceImpl implements MarketingTypeService {

    private static final String MARKETING_TYPE_CACHE_KEY = "marketing:types";
    private static final long CACHE_EXPIRE_HOURS = 24;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    @SuppressWarnings("unchecked")
    public List<MarketingTypeVO> getAllTypes() {
        // 先从 Redis 缓存获取
        List<MarketingTypeVO> cachedTypes = (List<MarketingTypeVO>) redisTemplate.opsForValue().get(MARKETING_TYPE_CACHE_KEY);
        if (cachedTypes != null && !cachedTypes.isEmpty()) {
            return cachedTypes;
        }

        // 缓存不存在，从枚举构建
        List<MarketingTypeVO> types = new ArrayList<>();
        Arrays.stream(MarketingTypeEnum.values()).forEach(enumItem -> {
            types.add(new MarketingTypeVO(enumItem.getCode(), enumItem.getName()));
        });

        // 存入 Redis，设置过期时间
        redisTemplate.opsForValue().set(MARKETING_TYPE_CACHE_KEY, types, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);

        return types;
    }
}
