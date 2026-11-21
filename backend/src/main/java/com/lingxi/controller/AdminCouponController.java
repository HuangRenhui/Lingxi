package com.lingxi.controller;

import com.lingxi.common.Result;
import com.lingxi.dto.CouponCreateRequest;
import com.lingxi.dto.CouponUpdateRequest;
import com.lingxi.service.CouponService;
import com.lingxi.vo.CouponVO;
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
 * 管理端优惠券配置接口。
 */
@RestController
@RequestMapping("/admin/coupons")
@RequiredArgsConstructor
@Tag(name = "20. 优惠券配置")
public class AdminCouponController {

    private final CouponService couponService;

    @GetMapping
    @Operation(summary = "优惠券列表")
    public Result<List<CouponVO>> list(@RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "20") int size,
                                       @RequestParam(required = false) String keyword,
                                       @RequestParam(required = false) Integer enabled) {
        return Result.success(couponService.list(page, size, keyword, enabled));
    }

    @GetMapping("/{id}")
    @Operation(summary = "优惠券详情")
    public Result<CouponVO> get(@PathVariable Long id) {
        return Result.success(couponService.get(id));
    }

    @PostMapping
    @Operation(summary = "新建优惠券")
    public Result<CouponVO> create(@Valid @RequestBody CouponCreateRequest request) {
        return Result.success(couponService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新优惠券")
    public Result<CouponVO> update(@PathVariable Long id,
                                   @Valid @RequestBody CouponUpdateRequest request) {
        return Result.success(couponService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除优惠券")
    public Result<Void> delete(@PathVariable Long id) {
        couponService.delete(id);
        return Result.success();
    }
}
