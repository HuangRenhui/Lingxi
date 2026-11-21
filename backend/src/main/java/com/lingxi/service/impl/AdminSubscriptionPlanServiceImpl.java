package com.lingxi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingxi.common.BusinessException;
import com.lingxi.common.ResultCode;
import com.lingxi.dto.SubscriptionPlanCreateRequest;
import com.lingxi.dto.SubscriptionPlanUpdateRequest;
import com.lingxi.entity.SubscriptionPlan;
import com.lingxi.mapper.SubscriptionPlanMapper;
import com.lingxi.security.SecurityUtils;
import com.lingxi.service.AdminSubscriptionPlanService;
import com.lingxi.vo.SubscriptionPlanVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理端订阅套餐服务实现。
 */
@Service
@RequiredArgsConstructor
public class AdminSubscriptionPlanServiceImpl implements AdminSubscriptionPlanService {

    private static final int MAX_PAGE_SIZE = 100;

    private final SubscriptionPlanMapper subscriptionPlanMapper;

    @Override
    public List<SubscriptionPlanVO> list(int page, int size, String keyword) {
        requireAdmin();
        long current = Math.max(1, page);
        long pageSize = Math.min(Math.max(1, size), MAX_PAGE_SIZE);
        String likeKeyword = StringUtils.hasText(keyword)
                ? keyword.trim().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
                : null;
        LambdaQueryWrapper<SubscriptionPlan> wrapper = new LambdaQueryWrapper<SubscriptionPlan>()
                .and(likeKeyword != null, item -> item
                        .like(SubscriptionPlan::getPlanKey, likeKeyword)
                        .or()
                        .like(SubscriptionPlan::getName, likeKeyword))
                .orderByAsc(SubscriptionPlan::getSortOrder)
                .orderByAsc(SubscriptionPlan::getId);
        Page<SubscriptionPlan> result = subscriptionPlanMapper.selectPage(new Page<>(current, pageSize), wrapper);
        return result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public SubscriptionPlanVO get(Long id) {
        requireAdmin();
        return toVO(getPlan(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubscriptionPlanVO create(SubscriptionPlanCreateRequest request) {
        requireAdmin();
        String planKey = request.getPlanKey().trim();
        // plan_key 是唯一索引且不区分逻辑删除，必须先查历史记录：
        // 已存在则复用该行，否则删除后重建同一标识会直接撞唯一键。
        SubscriptionPlan existing = subscriptionPlanMapper.selectByPlanKeyIncludeDeleted(planKey);
        if (existing != null && !Integer.valueOf(1).equals(existing.getDeleted())) {
            throw new BusinessException(ResultCode.SUBSCRIPTION_PLAN_KEY_TAKEN);
        }

        SubscriptionPlan plan = new SubscriptionPlan();
        plan.setPlanKey(planKey);
        plan.setName(request.getName().trim());
        plan.setMonthlyPrice(request.getMonthlyPrice());
        plan.setYearlyPrice(request.getYearlyPrice());
        plan.setCreditsPerMonth(request.getCreditsPerMonth());
        plan.setTag(StringUtils.hasText(request.getTag()) ? request.getTag().trim() : null);
        plan.setFeatures(joinFeatures(request.getFeatures()));
        plan.setEnabled(request.getEnabled() == null ? 1 : request.getEnabled());
        plan.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        if (existing != null) {
            plan.setId(existing.getId());
            subscriptionPlanMapper.restoreById(existing.getId());
            subscriptionPlanMapper.updateById(plan);
            return toVO(subscriptionPlanMapper.selectById(existing.getId()));
        }
        subscriptionPlanMapper.insert(plan);
        return toVO(subscriptionPlanMapper.selectById(plan.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubscriptionPlanVO update(Long id, SubscriptionPlanUpdateRequest request) {
        requireAdmin();
        SubscriptionPlan plan = getPlan(id);
        // 只写入本次传入的字段；可空字段用 UpdateWrapper 显式 set(null) 才能清空
        LambdaUpdateWrapper<SubscriptionPlan> update =
                new LambdaUpdateWrapper<SubscriptionPlan>().eq(SubscriptionPlan::getId, id);
        boolean changed = false;

        if (request.getPlanKey() != null) {
            String planKey = request.getPlanKey().trim();
            if (planKey.isEmpty()) {
                throw new BusinessException(ResultCode.PARAM_ERROR);
            }
            if (!planKey.equals(plan.getPlanKey())) {
                // plan_key 唯一索引不区分逻辑删除，已删除行同样占用该标识
                SubscriptionPlan other = subscriptionPlanMapper.selectByPlanKeyIncludeDeleted(planKey);
                if (other != null && !other.getId().equals(id)) {
                    throw new BusinessException(ResultCode.SUBSCRIPTION_PLAN_KEY_TAKEN);
                }
            }
            update.set(SubscriptionPlan::getPlanKey, planKey);
            changed = true;
        }
        if (request.getName() != null) {
            String name = request.getName().trim();
            if (name.isEmpty()) {
                throw new BusinessException(ResultCode.PARAM_ERROR);
            }
            update.set(SubscriptionPlan::getName, name);
            changed = true;
        }
        if (request.getMonthlyPrice() != null) {
            update.set(SubscriptionPlan::getMonthlyPrice, request.getMonthlyPrice());
            changed = true;
        }
        if (request.getYearlyPrice() != null) {
            update.set(SubscriptionPlan::getYearlyPrice, request.getYearlyPrice());
            changed = true;
        }
        if (request.getCreditsPerMonth() != null) {
            update.set(SubscriptionPlan::getCreditsPerMonth, request.getCreditsPerMonth());
            changed = true;
        }
        if (request.getTag() != null) {
            update.set(SubscriptionPlan::getTag,
                    StringUtils.hasText(request.getTag()) ? request.getTag().trim() : null);
            changed = true;
        }
        if (request.getFeatures() != null) {
            update.set(SubscriptionPlan::getFeatures, joinFeatures(request.getFeatures()));
            changed = true;
        }
        if (request.getEnabled() != null) {
            update.set(SubscriptionPlan::getEnabled, request.getEnabled());
            changed = true;
        }
        if (request.getSortOrder() != null) {
            update.set(SubscriptionPlan::getSortOrder, request.getSortOrder());
            changed = true;
        }

        if (!changed) {
            return toVO(plan);
        }
        subscriptionPlanMapper.update(null, update);
        SubscriptionPlan saved = subscriptionPlanMapper.selectById(id);
        if (saved == null) {
            throw new BusinessException(ResultCode.SUBSCRIPTION_PLAN_NOT_FOUND);
        }
        return toVO(saved);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        requireAdmin();
        getPlan(id);
        subscriptionPlanMapper.deleteById(id);
    }

    private void requireAdmin() {
        if (!"ADMIN".equals(SecurityUtils.getLoginUser().getRole())) {
            throw new BusinessException(ResultCode.ADMIN_REQUIRED);
        }
    }

    private SubscriptionPlan getPlan(Long id) {
        SubscriptionPlan plan = subscriptionPlanMapper.selectById(id);
        if (plan == null) {
            throw new BusinessException(ResultCode.SUBSCRIPTION_PLAN_NOT_FOUND);
        }
        return plan;
    }

    private String joinFeatures(List<String> features) {
        if (features == null || features.isEmpty()) {
            return null;
        }
        String text = features.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.joining("\n"));
        return text.isEmpty() ? null : text;
    }

    private SubscriptionPlanVO toVO(SubscriptionPlan plan) {
        SubscriptionPlanVO vo = new SubscriptionPlanVO();
        vo.setId(plan.getId());
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
        vo.setEnabled(plan.getEnabled());
        vo.setSortOrder(plan.getSortOrder());
        vo.setCreatedAt(plan.getCreatedAt());
        vo.setUpdatedAt(plan.getUpdatedAt());
        return vo;
    }
}
