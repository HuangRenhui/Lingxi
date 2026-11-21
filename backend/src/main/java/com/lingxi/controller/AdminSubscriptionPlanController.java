package com.lingxi.controller;

import com.lingxi.common.Result;
import com.lingxi.dto.SubscriptionPlanCreateRequest;
import com.lingxi.dto.SubscriptionPlanUpdateRequest;
import com.lingxi.service.AdminSubscriptionPlanService;
import com.lingxi.vo.SubscriptionPlanVO;
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

import javax.validation.Valid;
import java.util.List;

/**
 * 管理端订阅套餐配置接口。
 */
@RestController
@RequestMapping("/admin/subscription-plans")
@RequiredArgsConstructor
@Tag(name = "19. 订阅套餐配置")
public class AdminSubscriptionPlanController {

    private final AdminSubscriptionPlanService adminSubscriptionPlanService;

    @GetMapping
    @Operation(summary = "套餐列表（含已下架）")
    public Result<List<SubscriptionPlanVO>> list(@RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "20") int size,
                                                 @RequestParam(required = false) String keyword) {
        return Result.success(adminSubscriptionPlanService.list(page, size, keyword));
    }

    @GetMapping("/{id}")
    @Operation(summary = "套餐详情")
    public Result<SubscriptionPlanVO> get(@PathVariable Long id) {
        return Result.success(adminSubscriptionPlanService.get(id));
    }

    @PostMapping
    @Operation(summary = "新建套餐")
    public Result<SubscriptionPlanVO> create(@Valid @RequestBody SubscriptionPlanCreateRequest request) {
        return Result.success(adminSubscriptionPlanService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新套餐")
    public Result<SubscriptionPlanVO> update(@PathVariable Long id,
                                             @Valid @RequestBody SubscriptionPlanUpdateRequest request) {
        return Result.success(adminSubscriptionPlanService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除套餐")
    public Result<Void> delete(@PathVariable Long id) {
        adminSubscriptionPlanService.delete(id);
        return Result.success();
    }
}
