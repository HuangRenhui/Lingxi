package com.lingxi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingxi.common.BusinessException;
import com.lingxi.common.OrderNoGenerator;
import com.lingxi.common.ResultCode;
import com.lingxi.dto.OrderCreateRequest;
import com.lingxi.dto.OrderStatusRequest;
import com.lingxi.dto.OrderUpdateRequest;
import com.lingxi.entity.BizOrder;
import com.lingxi.mapper.BizOrderMapper;
import com.lingxi.security.SecurityUtils;
import com.lingxi.service.OrderService;
import com.lingxi.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 订单服务实现。
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    /** 单页最大条数，与全局分页上限保持一致 */
    private static final int MAX_PAGE_SIZE = 100;

    /** 订单状态全集，用于筛选参数校验 */
    private static final Set<String> STATUSES = new HashSet<>(Arrays.asList(
            "PENDING", "PAID", "CANCELLED", "REFUNDING", "REFUNDED"));

    /** 订单类型全集，用于筛选参数校验 */
    private static final Set<String> TYPES = new HashSet<>(Arrays.asList(
            "RECHARGE", "SUBSCRIPTION", "CONSUME", "REDEEM"));

    private final BizOrderMapper orderMapper;

    @Override
    public List<OrderVO> listMine(int page, int size, String status, String type, String keyword) {
        long current = Math.max(1, page);
        long pageSize = Math.min(Math.max(1, size), MAX_PAGE_SIZE);

        String statusFilter = normalizeFilter(status, STATUSES);
        String typeFilter = normalizeFilter(type, TYPES);
        String keywordFilter = StringUtils.hasText(keyword) ? keyword.trim() : null;

        // 关键字必须转义 LIKE 通配符：用户输入 "%" 或 "_" 会让条件退化为全表匹配
        String likeKeyword = keywordFilter == null ? null : escapeLike(keywordFilter);
        LambdaQueryWrapper<BizOrder> wrapper = new LambdaQueryWrapper<BizOrder>()
                .eq(BizOrder::getUserId, SecurityUtils.getUserId())
                .eq(statusFilter != null, BizOrder::getStatus, statusFilter)
                .eq(typeFilter != null, BizOrder::getType, typeFilter)
                .and(likeKeyword != null, w -> w
                        .like(BizOrder::getDescription, likeKeyword)
                        .or()
                        .like(BizOrder::getOrderNo, likeKeyword))
                .orderByDesc(BizOrder::getId);
        Page<BizOrder> result = orderMapper.selectPage(new Page<>(current, pageSize), wrapper);
        return result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public OrderVO get(Long id) {
        return toVO(getOwnedOrder(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO create(OrderCreateRequest request) {
        BizOrder order = new BizOrder();
        order.setOrderNo(OrderNoGenerator.next());
        order.setUserId(SecurityUtils.getUserId());
        order.setDescription(request.getDescription().trim());
        order.setType(StringUtils.hasText(request.getType()) ? request.getType() : "CONSUME");
        // 金额统一保留 6 位小数，与 DECIMAL(18,6) 对齐，避免尾差
        order.setAmount(request.getAmount().setScale(6, RoundingMode.HALF_UP));
        order.setPayMethod(trimToNull(request.getPayMethod()));
        // 新建订单一律待支付，支付状态只能由支付流程推进
        order.setStatus("PENDING");
        orderMapper.insert(order);
        return toVO(orderMapper.selectById(order.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO update(Long id, OrderUpdateRequest request) {
        BizOrder order = getOwnedOrder(id);
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException(ResultCode.ORDER_NOT_EDITABLE);
        }
        // 只更新显式传入的字段，并把 status='PENDING' 写进 where：
        // 整实体 updateById 会把读到的旧状态一并写回，与支付回调并发时会覆盖 PAID
        LambdaUpdateWrapper<BizOrder> wrapper = new LambdaUpdateWrapper<BizOrder>()
                .eq(BizOrder::getId, id)
                .eq(BizOrder::getStatus, "PENDING");
        boolean changed = false;
        if (request.getDescription() != null) {
            String description = request.getDescription().trim();
            if (description.isEmpty()) {
                throw new BusinessException(ResultCode.PARAM_ERROR);
            }
            wrapper.set(BizOrder::getDescription, description);
            changed = true;
        }
        if (request.getPayMethod() != null) {
            wrapper.set(BizOrder::getPayMethod, trimToNull(request.getPayMethod()));
            changed = true;
        }
        if (changed && orderMapper.update(null, wrapper) == 0) {
            throw new BusinessException(ResultCode.ORDER_NOT_EDITABLE);
        }
        return toVO(orderMapper.selectById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO changeStatus(Long id, OrderStatusRequest request) {
        // 归属校验与状态校验都必须落到 SQL 条件里：先查后改会让「取消」与「支付回调」互相覆盖
        getOwnedOrder(id);
        int affected = orderMapper.update(null, new LambdaUpdateWrapper<BizOrder>()
                .eq(BizOrder::getId, id)
                .eq(BizOrder::getStatus, "PENDING")
                .set(BizOrder::getStatus, request.getStatus()));
        if (affected == 0) {
            throw new BusinessException(ResultCode.ORDER_STATUS_INVALID);
        }
        return toVO(orderMapper.selectById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 已支付或退款中的订单不允许删除，避免资金凭证丢失；
        // 状态条件必须写进 SQL，否则「检查后被支付」仍会被删掉
        getOwnedOrder(id);
        int affected = orderMapper.delete(new LambdaQueryWrapper<BizOrder>()
                .eq(BizOrder::getId, id)
                .in(BizOrder::getStatus, "PENDING", "CANCELLED"));
        if (affected == 0) {
            throw new BusinessException(ResultCode.ORDER_STATUS_INVALID);
        }
    }

    /**
     * 查询订单并校验归属。
     */
    private BizOrder getOwnedOrder(Long id) {
        BizOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (!SecurityUtils.getUserId().equals(order.getUserId())) {
            throw new BusinessException(ResultCode.ORDER_NOT_OWNED);
        }
        return order;
    }

    /**
     * 转义 LIKE 通配符。MySQL 默认转义符为反斜杠，需同时转义自身。
     */
    private String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    /**
     * 筛选值归一化：空值返回 null，非法枚举值返回参数错误。
     */
    private String normalizeFilter(String value, Set<String> allowed) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (!allowed.contains(normalized)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return normalized;
    }

    /**
     * 空字符串归一为 null。
     */
    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * 实体转视图。
     */
    private OrderVO toVO(BizOrder order) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setDescription(order.getDescription());
        vo.setType(order.getType());
        vo.setAmount(order.getAmount());
        vo.setPayMethod(order.getPayMethod());
        vo.setStatus(order.getStatus());
        vo.setPaidAt(order.getPaidAt());
        vo.setCreatedAt(order.getCreatedAt());
        vo.setUpdatedAt(order.getUpdatedAt());
        return vo;
    }
}
