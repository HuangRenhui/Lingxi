package com.lingxi.config;

import org.springframework.context.annotation.Configuration;

/**
 * Redis 配置。
 *
 * <p>业务只使用 Spring Boot 自动装配的 {@code StringRedisTemplate}，不再暴露
 * 带默认类型信息的 {@code RedisTemplate<String, Object>}：默认类型 + 宽松校验器
 * 在缓存外部数据时会形成反序列化攻击面。</p>
 */
@Configuration
public class RedisConfig {
}
