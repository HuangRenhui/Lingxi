package com.lingxi;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 灵犀后端启动类。
 *
 * <p>AI 创作平台后端：前端 Vue 3 + Element Plus，后端按 B1-B12 模块逐步建设。</p>
 */
@SpringBootApplication
@MapperScan("com.lingxi.mapper")
public class LingxiApplication {

    public static void main(String[] args) {
        SpringApplication.run(LingxiApplication.class, args);
    }
}
