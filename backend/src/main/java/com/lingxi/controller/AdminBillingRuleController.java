package com.lingxi.controller;

import com.lingxi.common.Result;
import com.lingxi.vo.PageResult;
import com.lingxi.dto.BillingRuleCreateRequest;
import com.lingxi.dto.BillingRuleUpdateRequest;
import com.lingxi.service.BillingRuleService;
import com.lingxi.vo.BillingRuleVO;
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
 * 管理端计费规则配置接口。
 */
@RestController
@RequestMapping("/admin/billing-rules")
@RequiredArgsConstructor
@Tag(name = "22. 计费规则配置")
public class AdminBillingRuleController {

    private final BillingRuleService billingRuleService;

    @GetMapping
    @Operation(summary = "计费规则列表")
    public Result<PageResult<BillingRuleVO>> list(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "20") int size,
                                            @RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) String category,
                                            @RequestParam(required = false) Integer enabled) {
        return Result.success(billingRuleService.list(page, size, keyword, category, enabled));
    }

    @GetMapping("/{id}")
    @Operation(summary = "计费规则详情")
    public Result<BillingRuleVO> get(@PathVariable Long id) {
        return Result.success(billingRuleService.get(id));
    }

    @PostMapping
    @Operation(summary = "新建计费规则")
    public Result<BillingRuleVO> create(@Valid @RequestBody BillingRuleCreateRequest request) {
        return Result.success(billingRuleService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新计费规则")
    public Result<BillingRuleVO> update(@PathVariable Long id,
                                        @Valid @RequestBody BillingRuleUpdateRequest request) {
        return Result.success(billingRuleService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除计费规则")
    public Result<Void> delete(@PathVariable Long id) {
        billingRuleService.delete(id);
        return Result.success();
    }
}
