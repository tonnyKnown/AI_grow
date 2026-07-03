package com.oa.business.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class XxlJobConfig {

    private static final Logger log = LoggerFactory.getLogger(XxlJobConfig.class);

    @Value("${xxl.job.admin.addresses:http://127.0.0.1:8080/xxl-job-admin}")
    private String adminAddresses;

    @Value("${xxl.job.access-token:}")
    private String accessToken;

    @Value("${xxl.job.executor.app-name:business-service}")
    private String appname;

    @Value("${xxl.job.executor.port:9999}")
    private int port;

    @Value("${xxl.job.executor.log-path:./logs/xxl-job/jobhandler}")
    private String logPath;

    @Value("${xxl.job.executor.log-retention-days:30}")
    private int logRetentionDays;

    @Bean
    public XxlJobSpringExecutor xxlJobExecutor() {
        log.info("Initializing XXL-Job Executor...");
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(adminAddresses);
        xxlJobSpringExecutor.setAccessToken(accessToken);
        xxlJobSpringExecutor.setAppname(appname);
        xxlJobSpringExecutor.setPort(port);
        xxlJobSpringExecutor.setLogPath(logPath);
        xxlJobSpringExecutor.setLogRetentionDays(logRetentionDays);
        log.info("XXL-Job Executor initialized with appname: {}, port: {}", appname, port);
        return xxlJobSpringExecutor;
    }
}