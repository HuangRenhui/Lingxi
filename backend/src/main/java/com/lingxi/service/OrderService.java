package com.lingxi.service;

import com.lingxi.dto.OrderCreateRequest;
import com.lingxi.dto.OrderStatusRequest;
import com.lingxi.dto.OrderUpdateRequest;
import com.lingxi.vo.OrderVO;

import java.util.List;

/**
 * 订单服务。
 */
public interface OrderService {

    /**
     * 分页查询当前用户的订单，支持状态、类型与关键词筛选。
     */
    List<OrderVO> listMine(int page, int size, String status, String type, String keyword);

    /**
     * 查询订单详情。
     */
    OrderVO get(Long id);

    /**
     * 新建订单，初始状态为待支付。
     */
    OrderVO create(OrderCreateRequest request);

    /**
     * 更新订单描述与支付方式，仅待支付订单可修改。
     */
    OrderVO update(Long id, OrderUpdateRequest request);

    /**
     * 变更订单状态，用户侧仅支持取消待支付订单。
     */
    OrderVO changeStatus(Long id, OrderStatusRequest request);

    /**
     * 删除订单（逻辑删除），仅待支付或已取消订单可删除。
     */
    void delete(Long id);
}
