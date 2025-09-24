package com.example.learning;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * 启动类：用于启动在线学习平台后端服务。
 */
@EnableCaching
@MapperScan("com.example.learning.**.mapper")
@SpringBootApplication
public class LearningApplication {

    /**
     * 应用入口。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(LearningApplication.class, args);
    }
}

