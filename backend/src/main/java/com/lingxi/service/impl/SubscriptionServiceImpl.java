package com.lingxi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingxi.common.BusinessException;
import com.lingxi.common.MoneyUtil;
import com.lingxi.common.OrderNoGenerator;
import com.lingxi.common.ResultCode;
import com.lingxi.dto.SubscriptionCreateRequest;
import com.lingxi.dto.SubscriptionUpdateRequest;
import com.lingxi.entity.BizOrder;
import com.lingxi.entity.SubscriptionPlan;
import com.lingxi.entity.UserSubscription;
import com.lingxi.mapper.BizOrderMapper;
import com.lingxi.mapper.SubscriptionPlanMapper;
import com.lingxi.mapper.UserSubscriptionMapper;
import com.lingxi.security.SecurityUtils;
import com.lingxi.service.SubscriptionService;
import com.lingxi.service.WalletService;
import com.lingxi.vo.SubscriptionPlanVO;
import com.lingxi.vo.UserSubscriptionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订阅服务实现。
 */
@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    /** 单页最大条数，与全局分页上限保持一致 */
    private static final int MAX_PAGE_SIZE = 100;

    private final SubscriptionPlanMapper subscriptionPlanMapper;
    private final UserSubscriptionMapper userSubscriptionMapper;
    private final BizOrderMapper orderMapper;
    private final WalletService walletService;

    @Override
    public List<SubscriptionPlanVO> listPlans() {
        List<SubscriptionPlan> plans = subscriptionPlanMapper.selectList(new LambdaQueryWrapper<SubscriptionPlan>()
                .eq(SubscriptionPlan::getEnabled, 1)
                .orderByAsc(SubscriptionPlan::getSortOrder)
                .orderByAsc(SubscriptionPlan::getId));
        return plans.stream().map(this::toPlanVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserSubscriptionVO getCurrent() {
        UserSubscription subscription = findActiveSubscription();
        if (subscription == null) {
            return null;
        }
        // 到期未续费的订阅在查询时惰性置为失效，避免依赖定时任务才能得到正确状态
        if (isExpired(subscription)) {
            deactivate(subscription.getId());
            return null;
        }
        return toVO(subscription);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserSubscriptionVO subscribe(SubscriptionCreateRequest request) {
        Long userId = SecurityUtils.getUserId();
        SubscriptionPlan plan = subscriptionPlanMapper.selectOne(new LambdaQueryWrapper<SubscriptionPlan>()
                .eq(SubscriptionPlan::getPlanKey, request.getPlanKey().trim())
                .last("LIMIT 1"));
        if (plan == null) {
            throw new BusinessException(ResultCode.SUBSCRIPTION_PLAN_NOT_FOUND);
        }
        if (plan.getEnabled() == null || plan.getEnabled() != 1) {
            throw new BusinessException(ResultCode.SUBSCRIPTION_PLAN_DISABLED);
        }

        String cycle = request.getCycle();
        BigDecimal price = resolvePrice(plan, cycle);

        // 以钱包行作为用户级互斥锁：并发双击时后一个请求会阻塞到前一个提交，
        // 随后能在下面看到「同套餐仍在有效期」并直接返回，不会重复扣费。
        walletService.ensureAccount(userId);
        walletService.lockAccount(userId);

        LocalDateTime now = LocalDateTime.now();
        UserSubscription current = findActiveSubscription();

        // 同一套餐仍在有效期内：视为重复提交（连点/重试），直接返回现有订阅
        if (current != null && !isExpired(current) && plan.getPlanKey().equals(current.getPlanKey())) {
            return toVO(current);
        }
        // 换套餐或已到期：旧记录统一失效，历史留痕
        if (current != null) {
            deactivate(current.getId());
        }

        // 付费套餐必须先扣费再开通；任何一步失败都随事务一起回滚，不会出现「零成本拿到权益」
        if (price.compareTo(BigDecimal.ZERO) > 0) {
            chargeForSubscription(userId, plan, cycle, price, now);
        }

        String planName = plan.getName();
        UserSubscription subscription = new UserSubscription();
        subscription.setUserId(userId);
        subscription.setPlanKey(plan.getPlanKey());
        subscription.setPlanName(planName);
        subscription.setCycle(cycle);
        subscription.setStatus("ACTIVE");
        subscription.setActiveFlag(1);
        subscription.setAutoRenew(1);
        subscription.setStartedAt(now);
        subscription.setExpiresAt("YEARLY".equals(cycle) ? now.plusYears(1) : now.plusMonths(1));
        subscription.setCreditsPerMonth(plan.getCreditsPerMonth() == null ? 0 : plan.getCreditsPerMonth());
        subscription.setCreditsUsed(0);
        userSubscriptionMapper.insert(subscription);
        return toVO(userSubscriptionMapper.selectById(subscription.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserSubscriptionVO updateCurrent(SubscriptionUpdateRequest request) {
        UserSubscription subscription = getActiveOrThrow();
        int affected = userSubscriptionMapper.update(null, new LambdaUpdateWrapper<UserSubscription>()
                .eq(UserSubscription::getId, subscription.getId())
                .eq(UserSubscription::getStatus, "ACTIVE")
                .set(UserSubscription::getAutoRenew, request.getAutoRenew()));
        if (affected == 0) {
            throw new BusinessException(ResultCode.SUBSCRIPTION_NOT_FOUND);
        }
        return toVO(userSubscriptionMapper.selectById(subscription.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelCurrent() {
        UserSubscription subscription = getActiveOrThrow();
        int affected = userSubscriptionMapper.update(null, new LambdaUpdateWrapper<UserSubscription>()
                .eq(UserSubscription::getId, subscription.getId())
                .eq(UserSubscription::getStatus, "ACTIVE")
                .set(UserSubscription::getStatus, "CANCELLED")
                .set(UserSubscription::getActiveFlag, null));
        if (affected == 0) {
            throw new BusinessException(ResultCode.SUBSCRIPTION_NOT_FOUND);
        }
    }

    @Override
    public List<UserSubscriptionVO> listHistory(int page, int size) {
        long current = Math.max(1, page);
        long pageSize = Math.min(Math.max(1, size), MAX_PAGE_SIZE);
        Page<UserSubscription> result = userSubscriptionMapper.selectPage(new Page<>(current, pageSize),
                new LambdaQueryWrapper<UserSubscription>()
                        .eq(UserSubscription::getUserId, SecurityUtils.getUserId())
                        .orderByDesc(UserSubscription::getId));
        return result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
    }

    /**
     * 计算开通价格：按计费周期取对应单价，负数与空值一律按 0 处理（配置错误不应放大成多扣费）。
     */
    private BigDecimal resolvePrice(SubscriptionPlan plan, String cycle) {
        BigDecimal price = "YEARLY".equals(cycle) ? plan.getYearlyPrice() : plan.getMonthlyPrice();
        return MoneyUtil.normalizeOrZero(price);
    }

    /**
     * 扣费并落订单：先建待支付订单，再按订单号幂等扣费，最后把订单推进到 PAID。
     */
    private void chargeForSubscription(Long userId, SubscriptionPlan plan, String cycle,
                                       BigDecimal price, LocalDateTime now) {
        String orderNo = OrderNoGenerator.next();
        BizOrder order = new BizOrder();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setDescription("订阅 " + plan.getName() + " · " + ("YEARLY".equals(cycle) ? "年付" : "月付"));
        order.setType("SUBSCRIPTION");
        order.setAmount(price);
        order.setPayMethod("CREDITS");
        order.setStatus("PENDING");
        orderMapper.insert(order);

        // bizNo 用订单号：同一订单重试只会入账一次
        walletService.debit(userId, price, "CONSUME", "订阅 " + plan.getName(), "ORDER-" + orderNo);

        orderMapper.update(null, new LambdaUpdateWrapper<BizOrder>()
                .eq(BizOrder::getId, order.getId())
                .eq(BizOrder::getStatus, "PENDING")
                .set(BizOrder::getStatus, "PAID")
                .set(BizOrder::getPaidAt, now));
    }

    /**
     * 把指定订阅置为失效。
     *
     * <p>active_flag 必须显式 set 为 null：MyBatis-Plus 的 updateById 会跳过 null 字段，
     * 直接把实体字段置空不会写库，唯一索引也就无法释放。</p>
     */
    private void deactivate(Long subscriptionId) {
        userSubscriptionMapper.update(null, new LambdaUpdateWrapper<UserSubscription>()
                .eq(UserSubscription::getId, subscriptionId)
                .eq(UserSubscription::getStatus, "ACTIVE")
                .set(UserSubscription::getStatus, "EXPIRED")
                .set(UserSubscription::getActiveFlag, null));
    }

    /**
     * 查询当前用户最近一条生效订阅，不考虑是否已到期。
     */
    private UserSubscription findActiveSubscription() {
        return userSubscriptionMapper.selectOne(new LambdaQueryWrapper<UserSubscription>()
                .eq(UserSubscription::getUserId, SecurityUtils.getUserId())
                .eq(UserSubscription::getStatus, "ACTIVE")
                .orderByDesc(UserSubscription::getId)
                .last("LIMIT 1"));
    }

    /**
     * 获取当前可用订阅，不存在或已到期时抛出业务异常。
     */
    private UserSubscription getActiveOrThrow() {
        UserSubscription subscription = findActiveSubscription();
        if (subscription == null || isExpired(subscription)) {
            throw new BusinessException(ResultCode.SUBSCRIPTION_NOT_FOUND);
        }
        return subscription;
    }

    private boolean isExpired(UserSubscription subscription) {
        return subscription.getExpiresAt() != null && !subscription.getExpiresAt().isAfter(LocalDateTime.now());
    }

    /**
     * 套餐实体转视图，权益文案按换行拆分。
     */
    private SubscriptionPlanVO toPlanVO(SubscriptionPlan plan) {
        SubscriptionPlanVO vo = new SubscriptionPlanVO();
        vo.setPlanKey(plan.getPlanKey());
        vo.setName(plan.getName());
        vo.setMonthlyPrice(plan.getMonthlyPrice());
        vo.setYearlyPrice(plan.getYearlyPrice());
        vo.setCreditsPerMonth(plan.getCreditsPerMonth());
        vo.setTag(plan.getTag());
        if (plan.getFeatures() == null || plan.getFeatures().trim().isEmpty()) {
            vo.setFeatures(Collections.emptyList());
        } else {
            vo.setFeatures(Arrays.stream(plan.getFeatures().split("\\n"))
                    .map(String::trim)
                    .filter(item -> !item.isEmpty())
                    .collect(Collectors.toList()));
        }
        return vo;
    }

    /**
     * 订阅实体转视图。
     */
    private UserSubscriptionVO toVO(UserSubscription subscription) {
        UserSubscriptionVO vo = new UserSubscriptionVO();
        vo.setId(subscription.getId());
        vo.setPlanKey(subscription.getPlanKey());
        vo.setPlanName(subscription.getPlanName());
        vo.setCycle(subscription.getCycle());
        vo.setStatus(subscription.getStatus());
        vo.setAutoRenew(subscription.getAutoRenew());
        vo.setStartedAt(subscription.getStartedAt());
        vo.setExpiresAt(subscription.getExpiresAt());
        vo.setCreditsPerMonth(subscription.getCreditsPerMonth());
        vo.setCreditsUsed(subscription.getCreditsUsed());
        vo.setCreatedAt(subscription.getCreatedAt());
        return vo;
    }
}
