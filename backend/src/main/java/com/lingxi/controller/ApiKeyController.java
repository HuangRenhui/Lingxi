package com.lingxi.controller;

import com.lingxi.common.Result;
import com.lingxi.dto.ApiKeyCreateRequest;
import com.lingxi.dto.ApiKeyUpdateRequest;
import com.lingxi.service.ApiKeyService;
import com.lingxi.vo.ApiKeyVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.List;

/**
 * API Key 基础管理接口。
 */
@RestController
@RequestMapping("/api-keys")
@RequiredArgsConstructor
@Tag(name = "09. API Key 管理")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    /**
     * 当前用户的密钥列表，仅返回前缀，不含明文。
     */
    @GetMapping
    @Operation(summary = "我的密钥列表")
    public Result<List<ApiKeyVO>> list(@RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "20") int size) {
        return Result.success(apiKeyService.listMine(page, size));
    }

    /**
     * 密钥详情，仅返回前缀。
     */
    @GetMapping("/{id}")
    @Operation(summary = "密钥详情")
    public Result<ApiKeyVO> get(@PathVariable Long id) {
        return Result.success(apiKeyService.get(id));
    }

    /**
     * 创建密钥，明文仅在本次响应中返回。
     */
    @PostMapping
    @Operation(summary = "创建密钥")
    public Result<ApiKeyVO> create(@Valid @RequestBody ApiKeyCreateRequest request) {
        return Result.success(apiKeyService.create(request));
    }

    /**
     * 修改密钥名称或启停状态。
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新密钥")
    public Result<ApiKeyVO> update(@PathVariable Long id,
                                   @Valid @RequestBody ApiKeyUpdateRequest request) {
        return Result.success(apiKeyService.update(id, request));
    }

    /**
     * 删除密钥。
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除密钥")
    public Result<Void> delete(@PathVariable Long id) {
        apiKeyService.delete(id);
        return Result.success();
    }
}
