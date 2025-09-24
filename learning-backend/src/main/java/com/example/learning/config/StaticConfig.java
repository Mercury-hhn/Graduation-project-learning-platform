package com.example.learning.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * 静态资源映射：将本地上传目录映射为 /files/** 可访问。
 */
@Configuration
public class StaticConfig implements WebMvcConfigurer {

    @Value("${storage.local-path}")
    private String root;

    @Value("${storage.public-prefix}")
    private String prefix;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(prefix + "**")
            .addResourceLocations(Paths.get(root).toUri().toString());
    }
}