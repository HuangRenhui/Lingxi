package com.lingxi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingxi.common.BusinessException;
import com.lingxi.vo.PageResult;
import com.lingxi.common.ResultCode;
import com.lingxi.dto.BillingRuleCreateRequest;
import com.lingxi.dto.BillingRuleUpdateRequest;
import com.lingxi.entity.BillingRule;
import com.lingxi.mapper.BillingRuleMapper;
import com.lingxi.security.SecurityUtils;
import com.lingxi.service.BillingRuleService;
import com.lingxi.vo.BillingRuleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理端计费规则服务实现。
 */
@Service
@RequiredArgsConstructor
public class BillingRuleServiceImpl implements BillingRuleService {

    private static final int MAX_PAGE_SIZE = 100;

    private final BillingRuleMapper billingRuleMapper;

    @Override
    public PageResult<BillingRuleVO> list(int page, int size, String keyword, String category, Integer enabled) {
        requireAdmin();
        long current = Math.max(1, page);
        long pageSize = Math.min(Math.max(1, size), MAX_PAGE_SIZE);
        String likeKeyword = StringUtils.hasText(keyword)
                ? keyword.trim().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
                : null;
        LambdaQueryWrapper<BillingRule> wrapper = new LambdaQueryWrapper<BillingRule>()
                .and(likeKeyword != null, item -> item
                        .like(BillingRule::getTarget, likeKeyword)
                        .or()
                        .like(BillingRule::getModel, likeKeyword))
                .eq(StringUtils.hasText(category), BillingRule::getCategory, category)
                .eq(enabled != null, BillingRule::getEnabled, enabled)
                .orderByDesc(BillingRule::getId);
        Page<BillingRule> result = billingRuleMapper.selectPage(new Page<>(current, pageSize), wrapper);
        List<BillingRuleVO> records = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(records, result.getTotal(), current, pageSize);
    }

    @Override
    public BillingRuleVO get(Long id) {
        requireAdmin();
        return toVO(getRule(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BillingRuleVO create(BillingRuleCreateRequest request) {
        requireAdmin();
        String target = request.getTarget().trim();
        String model = request.getModel().trim();
        // 唯一索引为 (target, model, deleted)，删除后重建同键规则会撞索引：
        // 存在历史记录时直接复用该行，而不是插入新行。
        BillingRule existing = billingRuleMapper.selectByTargetAndModelIncludeDeleted(target, model);
        if (existing != null && !Integer.valueOf(1).equals(existing.getDeleted())) {
            throw new BusinessException(ResultCode.DATA_CONFLICT);
        }
        BillingRule rule = new BillingRule();
        rule.setCategory(request.getCategory().trim());
        rule.setTarget(target);
        rule.setModel(model);
        rule.setUnit(request.getUnit().trim());
        rule.setPriceIn(request.getPriceIn());
        rule.setPriceOut(request.getPriceOut());
        rule.setPriceCachedIn(request.getPriceCachedIn());
        rule.setCostIn(request.getCostIn());
        rule.setCostOut(request.getCostOut());
        rule.setMultiplier(request.getMultiplier());
        rule.setEnabled(request.getEnabled() == null ? 1 : request.getEnabled());
        if (existing != null) {
            rule.setId(existing.getId());
            billingRuleMapper.restoreById(existing.getId());
            billingRuleMapper.updateById(rule);
            return toVO(billingRuleMapper.selectById(existing.getId()));
        }
        billingRuleMapper.insert(rule);
        return toVO(billingRuleMapper.selectById(rule.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BillingRuleVO update(Long id, BillingRuleUpdateRequest request) {
        requireAdmin();
        BillingRule rule = getRule(id);

        String target = rule.getTarget();
        String model = rule.getModel();
        if (request.getTarget() != null) {
            target = trimmedOrThrow(request.getTarget());
        }
        if (request.getModel() != null) {
            model = trimmedOrThrow(request.getModel());
        }
        // 只写入本次传入的字段，避免整实体回写覆盖并发修改
        BillingRule patch = new BillingRule();
        patch.setId(id);
        boolean changed = false;
        if (!target.equals(rule.getTarget()) || !model.equals(rule.getModel())) {
            BillingRule other = billingRuleMapper.selectByTargetAndModelIncludeDeleted(target, model);
            // 唯一索引包含 deleted 列，已逻辑删除的行不构成冲突
            if (other != null && !other.getId().equals(id) && !Integer.valueOf(1).equals(other.getDeleted())) {
                throw new BusinessException(ResultCode.DATA_CONFLICT);
            }
            patch.setTarget(target);
            patch.setModel(model);
            changed = true;
        }

        if (request.getCategory() != null) {
            patch.setCategory(trimmedOrThrow(request.getCategory()));
            changed = true;
        }
        if (request.getUnit() != null) {
            patch.setUnit(trimmedOrThrow(request.getUnit()));
            changed = true;
        }
        if (request.getPriceIn() != null) {
            patch.setPriceIn(request.getPriceIn());
            changed = true;
        }
        if (request.getPriceOut() != null) {
            patch.setPriceOut(request.getPriceOut());
            changed = true;
        }
        if (request.getPriceCachedIn() != null) {
            patch.setPriceCachedIn(request.getPriceCachedIn());
            changed = true;
        }
        if (request.getCostIn() != null) {
            patch.setCostIn(request.getCostIn());
            changed = true;
        }
        if (request.getCostOut() != null) {
            patch.setCostOut(request.getCostOut());
            changed = true;
        }
        if (request.getMultiplier() != null) {
            patch.setMultiplier(request.getMultiplier());
            changed = true;
        }
        if (request.getEnabled() != null) {
            patch.setEnabled(request.getEnabled());
            changed = true;
        }

        if (!changed) {
            return toVO(rule);
        }
        billingRuleMapper.updateById(patch);
        BillingRule saved = billingRuleMapper.selectById(id);
        if (saved == null) {
            throw new BusinessException(ResultCode.BILLING_RULE_NOT_FOUND);
        }
        return toVO(saved);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        requireAdmin();
        getRule(id);
        billingRuleMapper.deleteById(id);
    }

    private void requireAdmin() {
        if (!"ADMIN".equals(SecurityUtils.getLoginUser().getRole())) {
            throw new BusinessException(ResultCode.ADMIN_REQUIRED);
        }
    }

    private BillingRule getRule(Long id) {
        BillingRule rule = billingRuleMapper.selectById(id);
        if (rule == null) {
            throw new BusinessException(ResultCode.BILLING_RULE_NOT_FOUND);
        }
        return rule;
    }

    private String trimmedOrThrow(String value) {
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return trimmed;
    }

    private BillingRuleVO toVO(BillingRule rule) {
        BillingRuleVO vo = new BillingRuleVO();
        vo.setId(rule.getId());
        vo.setCategory(rule.getCategory());
        vo.setTarget(rule.getTarget());
        vo.setModel(rule.getModel());
        vo.setUnit(rule.getUnit());
        vo.setPriceIn(rule.getPriceIn());
        vo.setPriceOut(rule.getPriceOut());
        vo.setPriceCachedIn(rule.getPriceCachedIn());
        vo.setCostIn(rule.getCostIn());
        vo.setCostOut(rule.getCostOut());
        vo.setMultiplier(rule.getMultiplier());
        vo.setEnabled(rule.getEnabled());
        vo.setCreatedAt(rule.getCreatedAt());
        vo.setUpdatedAt(rule.getUpdatedAt());
        return vo;
    }
}
