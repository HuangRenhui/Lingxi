package com.lingxi.controller;

import com.lingxi.common.Result;
import com.lingxi.vo.PageResult;
import com.lingxi.dto.RedeemCodeBatchRequest;
import com.lingxi.dto.RedeemCodeCreateRequest;
import com.lingxi.dto.RedeemCodeUpdateRequest;
import com.lingxi.service.RedeemCodeService;
import com.lingxi.vo.RedeemCodeVO;
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
 * 管理端兑换码配置接口。
 */
@RestController
@RequestMapping("/admin/redeem-codes")
@RequiredArgsConstructor
@Tag(name = "21. 兑换码配置")
public class AdminRedeemCodeController {

    private final RedeemCodeService redeemCodeService;

    @GetMapping
    @Operation(summary = "兑换码列表")
    public Result<PageResult<RedeemCodeVO>> list(@RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "20") int size,
                                           @RequestParam(required = false) String status,
                                           @RequestParam(required = false) String keyword) {
        return Result.success(redeemCodeService.list(page, size, status, keyword));
    }

    @GetMapping("/{id}")
    @Operation(summary = "兑换码详情")
    public Result<RedeemCodeVO> get(@PathVariable Long id) {
        return Result.success(redeemCodeService.get(id));
    }

    @PostMapping
    @Operation(summary = "新建兑换码")
    public Result<RedeemCodeVO> create(@Valid @RequestBody RedeemCodeCreateRequest request) {
        return Result.success(redeemCodeService.create(request));
    }

    @PostMapping("/batch")
    @Operation(summary = "批量生成兑换码")
    public Result<List<RedeemCodeVO>> batchCreate(@Valid @RequestBody RedeemCodeBatchRequest request) {
        return Result.success(redeemCodeService.batchCreate(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新兑换码")
    public Result<RedeemCodeVO> update(@PathVariable Long id,
                                       @Valid @RequestBody RedeemCodeUpdateRequest request) {
        return Result.success(redeemCodeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除兑换码")
    public Result<Void> delete(@PathVariable Long id) {
        redeemCodeService.delete(id);
        return Result.success();
    }
}
