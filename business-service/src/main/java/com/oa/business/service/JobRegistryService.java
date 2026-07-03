package com.oa.business.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class JobRegistryService {

    private static final Logger log = LoggerFactory.getLogger(JobRegistryService.class);

    private static final String JOB_REGISTRY_KEY = "xxl:job:registry:";
    private static final String JOB_INFO_KEY = "xxl:job:info:";

    @Autowired
    private StringRedisTemplate redisTemplate;

    public void registerExecutor(String appName, String executorAddress, String port) {
        String key = JOB_REGISTRY_KEY + appName;
        Map<String, String> registryInfo = new HashMap<>();
        registryInfo.put("address", executorAddress);
        registryInfo.put("port", port);
        registryInfo.put("timestamp", String.valueOf(System.currentTimeMillis()));

        redisTemplate.opsForHash().putAll(key, registryInfo);
        redisTemplate.expire(key, Duration.ofSeconds(60));

        log.info("执行器注册成功: appName={}, address={}:{}", appName, executorAddress, port);
    }

    public void unregisterExecutor(String appName, String executorAddress) {
        String key = JOB_REGISTRY_KEY + appName;
        redisTemplate.opsForHash().delete(key, executorAddress);
        log.info("执行器注销成功: appName={}, address={}", appName, executorAddress);
    }

    public Map<Object, Object> getExecutorList(String appName) {
        String key = JOB_REGISTRY_KEY + appName;
        Map<Object, Object> result = redisTemplate.opsForHash().entries(key);
        log.info("获取执行器列表: appName={}, count={}", appName, result.size());
        return result;
    }

    public boolean isExecutorAlive(String appName, String executorAddress) {
        String key = JOB_REGISTRY_KEY + appName;
        Object lastUpdate = redisTemplate.opsForHash().get(key, executorAddress + ":timestamp");
        if (lastUpdate == null) {
            return false;
        }
        long timestamp = Long.parseLong(lastUpdate.toString());
        return System.currentTimeMillis() - timestamp < 60000;
    }

    public void saveJobInfo(Long jobId, Map<String, Object> jobInfo) {
        String key = JOB_INFO_KEY + jobId;
        redisTemplate.opsForHash().putAll(key, jobInfo);
        log.info("保存任务信息: jobId={}", jobId);
    }

    public Map<Object, Object> getJobInfo(Long jobId) {
        String key = JOB_INFO_KEY + jobId;
        return redisTemplate.opsForHash().entries(key);
    }

    public void updateJobLastRunTime(Long jobId) {
        String key = JOB_INFO_KEY + jobId;
        redisTemplate.opsForHash().put(key, "lastRunTime", String.valueOf(System.currentTimeMillis()));
    }

    public void incrementJobRunCount(Long jobId, boolean success) {
        String key = JOB_INFO_KEY + jobId;
        redisTemplate.opsForHash().increment(key, "runTimes", 1);
        if (success) {
            redisTemplate.opsForHash().increment(key, "successTimes", 1);
        } else {
            redisTemplate.opsForHash().increment(key, "failTimes", 1);
        }
    }
}