package com.example.javachain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Javachain Agent Application - Spring Boot 启动类
 *
 * 作为 AI Agent，专注于：
 * - 输入信息的清洗和语义匹配
 * - 任务调度和 ReAct 推理
 * - MCP 服务发现与调用（通过 Nacos MCP Registry）
 *
 * MCP 工具由独立的 MCP 插件项目提供，通过 Nacos MCP Registry 注册
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
public class JavachainApplication {

    public static void main(String[] args) {

        SpringApplication.run(JavachainApplication.class, args);
    }
}