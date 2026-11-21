package com.lingxi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingxi.common.BusinessException;
import com.lingxi.common.ResultCode;
import com.lingxi.dto.CouponCreateRequest;
import com.lingxi.dto.CouponUpdateRequest;
import com.lingxi.entity.MarketingCoupon;
import com.lingxi.mapper.MarketingCouponMapper;
import com.lingxi.security.SecurityUtils;
import com.lingxi.service.CouponService;
import com.lingxi.vo.CouponVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理端优惠券服务实现。
 */
@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private static final int MAX_PAGE_SIZE = 100;

    private final MarketingCouponMapper marketingCouponMapper;

    @Override
    public List<CouponVO> list(int page, int size, String keyword, Integer enabled) {
        requireAdmin();
        long current = Math.max(1, page);
        long pageSize = Math.min(Math.max(1, size), MAX_PAGE_SIZE);
        String likeKeyword = StringUtils.hasText(keyword)
                ? keyword.trim().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
                : null;
        LambdaQueryWrapper<MarketingCoupon> wrapper = new LambdaQueryWrapper<MarketingCoupon>()
                .like(likeKeyword != null, MarketingCoupon::getName, likeKeyword)
                .eq(enabled != null, MarketingCoupon::getEnabled, enabled)
                .orderByDesc(MarketingCoupon::getId);
        Page<MarketingCoupon> result = marketingCouponMapper.selectPage(new Page<>(current, pageSize), wrapper);
        return result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public CouponVO get(Long id) {
        requireAdmin();
        return toVO(getCoupon(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CouponVO create(CouponCreateRequest request) {
        requireAdmin();
        if ("DISCOUNT".equals(request.getType()) && request.getValue().compareTo(BigDecimal.ONE) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "折扣率不能大于 1");
        }
        if (request.getValidFrom() != null && request.getValidTo() != null
                && request.getValidFrom().isAfter(request.getValidTo())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "有效期开始不能晚于结束");
        }
        MarketingCoupon coupon = new MarketingCoupon();
        coupon.setName(request.getName().trim());
        coupon.setType(request.getType());
        coupon.setValue(request.getValue());
        coupon.setThreshold(request.getThreshold());
        coupon.setTotal(request.getTotal());
        coupon.setClaimed(0);
        coupon.setValidFrom(request.getValidFrom());
        coupon.setValidTo(request.getValidTo());
        coupon.setEnabled(request.getEnabled() == null ? 1 : request.getEnabled());
        marketingCouponMapper.insert(coupon);
        return toVO(marketingCouponMapper.selectById(coupon.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CouponVO update(Long id, CouponUpdateRequest request) {
        requireAdmin();
        MarketingCoupon coupon = getCoupon(id);

        // 校验使用「合并后的值」，写入只提交本次传入的字段：
        // 避免整实体回写把并发领取累加的 claimed 覆盖回旧值。
        MarketingCoupon patch = new MarketingCoupon();
        patch.setId(id);
        boolean changed = false;
        if (request.getName() != null) {
            String name = request.getName().trim();
            if (name.isEmpty()) {
                throw new BusinessException(ResultCode.PARAM_ERROR);
            }
            patch.setName(name);
            coupon.setName(name);
            changed = true;
        }
        if (request.getType() != null) {
            patch.setType(request.getType());
            coupon.setType(request.getType());
            changed = true;
        }
        if (request.getValue() != null) {
            patch.setValue(request.getValue());
            coupon.setValue(request.getValue());
            changed = true;
        }
        if (request.getThreshold() != null) {
            patch.setThreshold(request.getThreshold());
            coupon.setThreshold(request.getThreshold());
            changed = true;
        }
        if (request.getTotal() != null) {
            patch.setTotal(request.getTotal());
            coupon.setTotal(request.getTotal());
            changed = true;
        }
        if (request.getValidFrom() != null) {
            patch.setValidFrom(request.getValidFrom());
            coupon.setValidFrom(request.getValidFrom());
            changed = true;
        }
        if (request.getValidTo() != null) {
            patch.setValidTo(request.getValidTo());
            coupon.setValidTo(request.getValidTo());
            changed = true;
        }
        if (request.getEnabled() != null) {
            patch.setEnabled(request.getEnabled());
            coupon.setEnabled(request.getEnabled());
            changed = true;
        }
        if ("DISCOUNT".equals(coupon.getType()) && coupon.getValue().compareTo(BigDecimal.ONE) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "折扣率不能大于 1");
        }
        if (coupon.getValidFrom() != null && coupon.getValidTo() != null
                && coupon.getValidFrom().isAfter(coupon.getValidTo())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "有效期开始不能晚于结束");
        }
        if (coupon.getTotal() < coupon.getClaimed()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "发放总量不能小于已领取数量");
        }

        if (!changed) {
            return toVO(coupon);
        }
        marketingCouponMapper.updateById(patch);
        MarketingCoupon saved = marketingCouponMapper.selectById(id);
        if (saved == null) {
            throw new BusinessException(ResultCode.COUPON_NOT_FOUND);
        }
        return toVO(saved);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        requireAdmin();
        getCoupon(id);
        marketingCouponMapper.deleteById(id);
    }

    private void requireAdmin() {
        if (!"ADMIN".equals(SecurityUtils.getLoginUser().getRole())) {
            throw new BusinessException(ResultCode.ADMIN_REQUIRED);
        }
    }

    private MarketingCoupon getCoupon(Long id) {
        MarketingCoupon coupon = marketingCouponMapper.selectById(id);
        if (coupon == null) {
            throw new BusinessException(ResultCode.COUPON_NOT_FOUND);
        }
        return coupon;
    }

    private CouponVO toVO(MarketingCoupon coupon) {
        CouponVO vo = new CouponVO();
        vo.setId(coupon.getId());
        vo.setName(coupon.getName());
        vo.setType(coupon.getType());
        vo.setValue(coupon.getValue());
        vo.setThreshold(coupon.getThreshold());
        vo.setTotal(coupon.getTotal());
        vo.setClaimed(coupon.getClaimed());
        vo.setValidFrom(coupon.getValidFrom());
        vo.setValidTo(coupon.getValidTo());
        vo.setEnabled(coupon.getEnabled());
        vo.setCreatedAt(coupon.getCreatedAt());
        vo.setUpdatedAt(coupon.getUpdatedAt());
        return vo;
    }
}
