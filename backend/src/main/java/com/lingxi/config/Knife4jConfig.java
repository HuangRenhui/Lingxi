package com.lingxi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j / OpenAPI 文档配置。
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI lingxiOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("灵犀 API")
                .description("灵犀 AI 创作平台后端接口")
                .version("0.1.0"));
    }
}
