package com.lingxi.controller;

import com.lingxi.common.Result;
import com.lingxi.vo.PageResult;
import com.lingxi.dto.OrderCreateRequest;
import com.lingxi.dto.OrderStatusRequest;
import com.lingxi.dto.OrderUpdateRequest;
import com.lingxi.service.OrderService;
import com.lingxi.vo.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
 * 订单管理接口。
 *
 * <p>所有接口均按当前登录用户隔离；支付与退款状态由后续支付回调推进。</p>
 */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "16. 订单管理")
public class OrderController {

    private final OrderService orderService;

    /**
     * 分页查询当前用户的订单。
     */
    @GetMapping
    @Operation(summary = "我的订单列表")
    public Result<PageResult<OrderVO>> list(@RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "20") int size,
                                      @RequestParam(required = false) String status,
                                      @RequestParam(required = false) String type,
                                      @RequestParam(required = false) String keyword) {
        return Result.success(orderService.listMine(page, size, status, type, keyword));
    }

    /**
     * 查询订单详情。
     */
    @GetMapping("/{id}")
    @Operation(summary = "订单详情")
    public Result<OrderVO> get(@PathVariable Long id) {
        return Result.success(orderService.get(id));
    }

    /**
     * 新建订单。
     */
    @PostMapping
    @Operation(summary = "新建订单")
    public Result<OrderVO> create(@Valid @RequestBody OrderCreateRequest request) {
        return Result.success(orderService.create(request));
    }

    /**
     * 更新订单描述与支付方式。
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新订单")
    public Result<OrderVO> update(@PathVariable Long id,
                                  @Valid @RequestBody OrderUpdateRequest request) {
        return Result.success(orderService.update(id, request));
    }

    /**
     * 变更订单状态，当前仅支持取消。
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "变更订单状态")
    public Result<OrderVO> changeStatus(@PathVariable Long id,
                                        @Valid @RequestBody OrderStatusRequest request) {
        return Result.success(orderService.changeStatus(id, request));
    }

    /**
     * 删除订单（逻辑删除）。
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除订单")
    public Result<Void> delete(@PathVariable Long id) {
        orderService.delete(id);
        return Result.success();
    }
}
