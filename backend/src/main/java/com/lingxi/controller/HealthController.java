package com.lingxi.controller;

import com.lingxi.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 健康检查：必须同时探测数据库与 Redis，否则依赖故障时容器仍会被判定为健康。
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "01. 系统")
public class HealthController {

    private final DataSource dataSource;
    private final StringRedisTemplate redisTemplate;

    @GetMapping("/health")
    @Operation(summary = "健康检查")
    public ResponseEntity<Result<Map<String, Object>>> health() {
        boolean dbUp;
        try (Connection connection = dataSource.getConnection()) {
            dbUp = connection.isValid(1);
        } catch (Exception ex) {
            dbUp = false;
        }

        boolean redisUp;
        RedisConnection redisConnection = null;
        try {
            redisConnection = redisTemplate.getConnectionFactory().getConnection();
            redisUp = "PONG".equalsIgnoreCase(redisConnection.ping());
        } catch (Exception ex) {
            redisUp = false;
        } finally {
            if (redisConnection != null) {
                redisConnection.close();
            }
        }

        boolean up = dbUp && redisUp;
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", up ? "UP" : "DOWN");
        data.put("service", "lingxi-backend");
        data.put("version", "0.1.0");
        data.put("database", dbUp ? "UP" : "DOWN");
        data.put("redis", redisUp ? "UP" : "DOWN");
        data.put("time", LocalDateTime.now());
        // 依赖不可用时返回 503，供 Docker/K8s 正确摘除实例
        return up
                ? ResponseEntity.ok(Result.success(data))
                : ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Result.success(data));
    }
}
