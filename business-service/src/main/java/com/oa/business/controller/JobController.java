package com.oa.business.controller;

import com.oa.business.common.Result;
import com.oa.business.dto.JobTaskDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/business/api/jobs")
public class JobController {

    private static final Logger log = LoggerFactory.getLogger(JobController.class);

    @GetMapping("/list")
    public Result<List<JobTaskDto>> listJobs() {
        log.info("查询任务列表");
        List<JobTaskDto> jobs = new ArrayList<>();
        
        JobTaskDto job1 = new JobTaskDto();
        job1.setId(1L);
        job1.setJobName("demoJob");
        job1.setJobDesc("示例任务");
        job1.setJobHandler("demoJobHandler");
        job1.setCron("0/10 * * * * ?");
        job1.setStatus(1);
        job1.setShardingCount(1);
        job1.setCreator("admin");
        job1.setCreateTime(new Date());
        job1.setRunTimes(100);
        job1.setSuccessTimes(98);
        job1.setFailTimes(2);
        jobs.add(job1);

        JobTaskDto job2 = new JobTaskDto();
        job2.setId(2L);
        job2.setJobName("orderTimeoutJob");
        job2.setJobDesc("订单超时检查");
        job2.setJobHandler("orderTimeoutJob");
        job2.setCron("0 0 2 * * ?");
        job2.setStatus(1);
        job2.setShardingCount(1);
        job2.setCreator("admin");
        job2.setCreateTime(new Date());
        job2.setRunTimes(50);
        job2.setSuccessTimes(50);
        job2.setFailTimes(0);
        jobs.add(job2);

        JobTaskDto job3 = new JobTaskDto();
        job3.setId(3L);
        job3.setJobName("dataSyncJob");
        job3.setJobDesc("数据同步任务");
        job3.setJobHandler("dataSyncJob");
        job3.setCron("0 0/30 * * * ?");
        job3.setStatus(1);
        job3.setShardingCount(5);
        job3.setJobParam("北京,上海,广州,深圳,杭州");
        job3.setCreator("admin");
        job3.setCreateTime(new Date());
        job3.setRunTimes(200);
        job3.setSuccessTimes(195);
        job3.setFailTimes(5);
        jobs.add(job3);

        JobTaskDto job4 = new JobTaskDto();
        job4.setId(4L);
        job4.setJobName("statisticsJob");
        job4.setJobDesc("数据统计任务");
        job4.setJobHandler("statisticsJob");
        job4.setCron("0 0 1 * * ?");
        job4.setStatus(0);
        job4.setShardingCount(1);
        job4.setCreator("admin");
        job4.setCreateTime(new Date());
        job4.setRunTimes(0);
        job4.setSuccessTimes(0);
        job4.setFailTimes(0);
        jobs.add(job4);

        return Result.success(jobs);
    }

    @GetMapping("/{id}")
    public Result<JobTaskDto> getJob(@PathVariable Long id) {
        log.info("查询任务详情: {}", id);
        JobTaskDto job = new JobTaskDto();
        job.setId(id);
        job.setJobName("demoJob");
        job.setJobDesc("示例任务");
        job.setJobHandler("demoJobHandler");
        job.setCron("0/10 * * * * ?");
        job.setStatus(1);
        job.setShardingCount(1);
        job.setCreator("admin");
        job.setCreateTime(new Date());
        job.setRunTimes(100);
        job.setSuccessTimes(98);
        job.setFailTimes(2);
        return Result.success(job);
    }

    @PostMapping
    public Result<String> addJob(@RequestBody JobTaskDto job) {
        log.info("新增任务: {}", job.getJobName());
        return Result.success("任务创建成功");
    }

    @PutMapping("/{id}")
    public Result<String> updateJob(@PathVariable Long id, @RequestBody JobTaskDto job) {
        log.info("更新任务: {}", id);
        return Result.success("任务更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteJob(@PathVariable Long id) {
        log.info("删除任务: {}", id);
        return Result.success("任务删除成功");
    }

    @PostMapping("/{id}/start")
    public Result<String> startJob(@PathVariable Long id) {
        log.info("启动任务: {}", id);
        return Result.success("任务启动成功");
    }

    @PostMapping("/{id}/stop")
    public Result<String> stopJob(@PathVariable Long id) {
        log.info("停止任务: {}", id);
        return Result.success("任务停止成功");
    }

    @PostMapping("/{id}/trigger")
    public Result<String> triggerJob(@PathVariable Long id, @RequestParam(required = false) String params) {
        log.info("手动触发任务: {}, 参数: {}", id, params);
        return Result.success("任务触发成功");
    }

    @GetMapping("/handlers")
    public Result<List<String>> getJobHandlers() {
        log.info("获取所有任务处理器");
        List<String> handlers = new ArrayList<>();
        handlers.add("demoJobHandler");
        handlers.add("orderTimeoutJob");
        handlers.add("dataSyncJob");
        handlers.add("statisticsJob");
        handlers.add("shardingJob");
        handlers.add("asyncJob");
        handlers.add("failRetryJob");
        return Result.success(handlers);
    }

    @GetMapping("/logs/{jobId}")
    public Result<List<String>> getJobLogs(@PathVariable Long jobId,
                                           @RequestParam(required = false, defaultValue = "1") int pageNum,
                                           @RequestParam(required = false, defaultValue = "20") int pageSize) {
        log.info("获取任务 {} 的执行日志", jobId);
        List<String> logs = new ArrayList<>();
        logs.add("[2024-01-15 10:00:00] INFO - 任务开始执行");
        logs.add("[2024-01-15 10:00:01] INFO - 执行步骤 1/5");
        logs.add("[2024-01-15 10:00:02] INFO - 执行步骤 2/5");
        logs.add("[2024-01-15 10:00:03] INFO - 执行步骤 3/5");
        logs.add("[2024-01-15 10:00:04] INFO - 执行步骤 4/5");
        logs.add("[2024-01-15 10:00:05] INFO - 执行步骤 5/5");
        logs.add("[2024-01-15 10:00:05] INFO - 任务执行成功");
        return Result.success(logs);
    }
}