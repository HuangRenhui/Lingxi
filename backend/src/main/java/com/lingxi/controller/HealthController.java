package com.lingxi.controller;

import com.lingxi.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 健康检查。
 */
@RestController
@Tag(name = "01. 系统")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "健康检查")
    public Result<Map<String, Object>> health() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", "UP");
        data.put("service", "lingxi-backend");
        data.put("version", "0.1.0");
        data.put("time", LocalDateTime.now());
        return Result.success(data);
    }
}
