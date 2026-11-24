package com.lingxi.controller;

import com.lingxi.common.Result;
import com.lingxi.vo.PageResult;
import com.lingxi.dto.SensitiveWordBatchCreateRequest;
import com.lingxi.dto.SensitiveWordUpdateRequest;
import com.lingxi.service.SensitiveWordService;
import com.lingxi.vo.SensitiveWordVO;
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
 * 管理端敏感词库接口。
 */
@RestController
@RequestMapping("/admin/moderation-words")
@RequiredArgsConstructor
@Tag(name = "24. 敏感词管理")
public class AdminSensitiveWordController {

    private final SensitiveWordService sensitiveWordService;

    @GetMapping
    @Operation(summary = "敏感词列表")
    public Result<PageResult<SensitiveWordVO>> list(@RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "20") int size,
                                              @RequestParam(required = false) String keyword,
                                              @RequestParam(required = false) String category) {
        return Result.success(sensitiveWordService.list(page, size, keyword, category));
    }

    @PostMapping
    @Operation(summary = "批量新增敏感词")
    public Result<List<SensitiveWordVO>> batchCreate(@Valid @RequestBody SensitiveWordBatchCreateRequest request) {
        return Result.success(sensitiveWordService.batchCreate(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "启用/停用敏感词")
    public Result<SensitiveWordVO> update(@PathVariable Long id,
                                          @Valid @RequestBody SensitiveWordUpdateRequest request) {
        return Result.success(sensitiveWordService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除敏感词")
    public Result<Void> delete(@PathVariable Long id) {
        sensitiveWordService.delete(id);
        return Result.success();
    }
}
