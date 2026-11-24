package com.lingxi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingxi.common.BusinessException;
import com.lingxi.vo.PageResult;
import com.lingxi.common.ResultCode;
import com.lingxi.dto.RedeemCodeBatchRequest;
import com.lingxi.dto.RedeemCodeCreateRequest;
import com.lingxi.dto.RedeemCodeUpdateRequest;
import com.lingxi.entity.RedeemCode;
import com.lingxi.mapper.RedeemCodeMapper;
import com.lingxi.security.SecurityUtils;
import com.lingxi.service.RedeemCodeService;
import com.lingxi.vo.RedeemCodeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理端兑换码服务实现。
 */
@Service
@RequiredArgsConstructor
public class RedeemCodeServiceImpl implements RedeemCodeService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final RedeemCodeMapper redeemCodeMapper;

    @Override
    public PageResult<RedeemCodeVO> list(int page, int size, String status, String keyword) {
        requireAdmin();
        long current = Math.max(1, page);
        long pageSize = Math.min(Math.max(1, size), MAX_PAGE_SIZE);
        String likeKeyword = StringUtils.hasText(keyword)
                ? keyword.trim().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
                : null;
        LambdaQueryWrapper<RedeemCode> wrapper = new LambdaQueryWrapper<RedeemCode>()
                .eq(StringUtils.hasText(status), RedeemCode::getStatus, status)
                .like(likeKeyword != null, RedeemCode::getCode, likeKeyword)
                .orderByDesc(RedeemCode::getId);
        Page<RedeemCode> result = redeemCodeMapper.selectPage(new Page<>(current, pageSize), wrapper);
        List<RedeemCodeVO> records = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(records, result.getTotal(), current, pageSize);
    }

    @Override
    public RedeemCodeVO get(Long id) {
        requireAdmin();
        return toVO(getCode(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RedeemCodeVO create(RedeemCodeCreateRequest request) {
        requireAdmin();
        String code = StringUtils.hasText(request.getCode()) ? request.getCode().trim() : generateUniqueCode("LX");
        // 唯一索引不区分逻辑删除，查重必须包含历史记录，否则会直接撞唯一键
        if (redeemCodeMapper.countByCodeIncludeDeleted(code) > 0) {
            throw new BusinessException(ResultCode.DATA_CONFLICT.getCode(), "兑换码已存在（含已删除记录），请更换");
        }
        RedeemCode redeemCode = buildCode(code, request.getAmount(), request.getExpiresAt());
        redeemCodeMapper.insert(redeemCode);
        return toVO(redeemCodeMapper.selectById(redeemCode.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<RedeemCodeVO> batchCreate(RedeemCodeBatchRequest request) {
        requireAdmin();
        String prefix = StringUtils.hasText(request.getPrefix()) ? request.getPrefix().trim() : "LX";
        List<RedeemCodeVO> created = new ArrayList<>();
        for (int i = 0; i < request.getCount(); i++) {
            RedeemCode redeemCode = buildCode(generateUniqueCode(prefix), request.getAmount(), request.getExpiresAt());
            redeemCodeMapper.insert(redeemCode);
            created.add(toVO(redeemCodeMapper.selectById(redeemCode.getId())));
        }
        return created;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RedeemCodeVO update(Long id, RedeemCodeUpdateRequest request) {
        requireAdmin();
        RedeemCode redeemCode = getCode(id);
        // 已使用的兑换码不允许再改面额、有效期或状态，避免篡改已发放记录
        if ("USED".equals(redeemCode.getStatus())) {
            throw new BusinessException(ResultCode.REDEEM_CODE_ALREADY_USED);
        }
        // 使用状态只能由兑换流程写入：手工置为 USED 会造成「已使用但无使用人/使用时间」的脏数据
        if ("USED".equals(request.getStatus())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(),
                    "使用状态由兑换流程维护，仅支持 UNUSED / VOID");
        }
        // 只写入本次传入的字段
        RedeemCode patch = new RedeemCode();
        patch.setId(id);
        boolean changed = false;
        if (request.getAmount() != null) {
            patch.setAmount(request.getAmount());
            changed = true;
        }
        if (request.getExpiresAt() != null) {
            patch.setExpiresAt(request.getExpiresAt());
            changed = true;
        }
        if (request.getStatus() != null) {
            patch.setStatus(request.getStatus());
            changed = true;
        }
        if (!changed) {
            return toVO(redeemCode);
        }
        redeemCodeMapper.updateById(patch);
        RedeemCode saved = redeemCodeMapper.selectById(id);
        if (saved == null) {
            throw new BusinessException(ResultCode.REDEEM_CODE_NOT_FOUND);
        }
        return toVO(saved);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        requireAdmin();
        getCode(id);
        redeemCodeMapper.deleteById(id);
    }

    private RedeemCode buildCode(String code, java.math.BigDecimal amount, java.time.LocalDate expiresAt) {
        RedeemCode redeemCode = new RedeemCode();
        redeemCode.setCode(code);
        redeemCode.setAmount(amount);
        redeemCode.setStatus("UNUSED");
        redeemCode.setExpiresAt(expiresAt);
        return redeemCode;
    }

    /**
     * 生成「前缀-随机串」形式的兑换码，并确保库中不存在同名记录。
     */
    private String generateUniqueCode(String prefix) {
        for (int attempt = 0; attempt < 10; attempt++) {
            StringBuilder builder = new StringBuilder(prefix).append('-');
            for (int i = 0; i < 6; i++) {
                builder.append(CODE_ALPHABET.charAt(RANDOM.nextInt(CODE_ALPHABET.length())));
            }
            String code = builder.toString();
            if (redeemCodeMapper.countByCodeIncludeDeleted(code) == 0) {
                return code;
            }
        }
        throw new BusinessException(ResultCode.DATA_CONFLICT.getCode(), "生成兑换码失败，请调整前缀后重试");
    }

    private void requireAdmin() {
        if (!"ADMIN".equals(SecurityUtils.getLoginUser().getRole())) {
            throw new BusinessException(ResultCode.ADMIN_REQUIRED);
        }
    }

    private RedeemCode getCode(Long id) {
        RedeemCode redeemCode = redeemCodeMapper.selectById(id);
        if (redeemCode == null) {
            throw new BusinessException(ResultCode.REDEEM_CODE_NOT_FOUND);
        }
        return redeemCode;
    }

    private RedeemCodeVO toVO(RedeemCode redeemCode) {
        RedeemCodeVO vo = new RedeemCodeVO();
        vo.setId(redeemCode.getId());
        vo.setCode(redeemCode.getCode());
        vo.setAmount(redeemCode.getAmount());
        vo.setStatus(redeemCode.getStatus());
        vo.setExpiresAt(redeemCode.getExpiresAt());
        vo.setUsedBy(redeemCode.getUsedBy());
        vo.setUsedAt(redeemCode.getUsedAt());
        vo.setCreatedAt(redeemCode.getCreatedAt());
        return vo;
    }
}
