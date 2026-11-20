package com.lingxi.controller;

import com.lingxi.common.Result;
import com.lingxi.dto.SubscriptionCreateRequest;
import com.lingxi.dto.SubscriptionUpdateRequest;
import com.lingxi.service.SubscriptionService;
import com.lingxi.vo.SubscriptionPlanVO;
import com.lingxi.vo.UserSubscriptionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 订阅套餐与用户订阅接口。
 */
@RestController
@RequestMapping("/subscriptions")
@RequiredArgsConstructor
@Tag(name = "18. 订阅管理")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    /**
     * 查询已上架的套餐目录。
     */
    @GetMapping("/plans")
    @Operation(summary = "套餐列表")
    public Result<List<SubscriptionPlanVO>> listPlans() {
        return Result.success(subscriptionService.listPlans());
    }

    /**
     * 查询当前生效的订阅。
     */
    @GetMapping("/current")
    @Operation(summary = "当前订阅")
    public Result<UserSubscriptionVO> getCurrent() {
        return Result.success(subscriptionService.getCurrent());
    }

    /**
     * 开通、续费或变更套餐。
     */
    @PostMapping
    @Operation(summary = "开通订阅")
    public Result<UserSubscriptionVO> subscribe(@Valid @RequestBody SubscriptionCreateRequest request) {
        return Result.success(subscriptionService.subscribe(request));
    }

    /**
     * 更新当前订阅（自动续费开关）。
     */
    @PutMapping("/current")
    @Operation(summary = "更新当前订阅")
    public Result<UserSubscriptionVO> updateCurrent(@Valid @RequestBody SubscriptionUpdateRequest request) {
        return Result.success(subscriptionService.updateCurrent(request));
    }

    /**
     * 取消当前订阅。
     */
    @DeleteMapping("/current")
    @Operation(summary = "取消当前订阅")
    public Result<Void> cancelCurrent() {
        subscriptionService.cancelCurrent();
        return Result.success();
    }

    /**
     * 分页查询订阅历史。
     */
    @GetMapping("/history")
    @Operation(summary = "订阅历史")
    public Result<List<UserSubscriptionVO>> listHistory(@RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "20") int size) {
        return Result.success(subscriptionService.listHistory(page, size));
    }
}
