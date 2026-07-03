package com.oa.system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.oa.system.mapper")
@EnableAsync
public class OaSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(OaSystemApplication.class, args);
    }
}
