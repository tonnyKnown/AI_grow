package com.oa.system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.lang.NonNull;

import java.util.Arrays;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.oa.system.mapper")
public class SystemServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SystemServiceApplication.class, args);


    }



}
