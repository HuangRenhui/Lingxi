package com.lingxi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingxi.common.BusinessException;
import com.lingxi.common.ResultCode;
import com.lingxi.dto.ModerationReportCreateRequest;
import com.lingxi.dto.ModerationReportUpdateRequest;
import com.lingxi.entity.ModerationReport;
import com.lingxi.mapper.ModerationReportMapper;
import com.lingxi.security.SecurityUtils;
import com.lingxi.service.ModerationReportService;
import com.lingxi.vo.ModerationReportVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理端内容举报服务实现。
 */
@Service
@RequiredArgsConstructor
public class ModerationReportServiceImpl implements ModerationReportService {

    private static final int MAX_PAGE_SIZE = 100;

    private final ModerationReportMapper moderationReportMapper;

    @Override
    public List<ModerationReportVO> list(int page, int size, String status, String keyword) {
        requireAdmin();
        long current = Math.max(1, page);
        long pageSize = Math.min(Math.max(1, size), MAX_PAGE_SIZE);
        String likeKeyword = StringUtils.hasText(keyword)
                ? keyword.trim().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
                : null;
        LambdaQueryWrapper<ModerationReport> wrapper = new LambdaQueryWrapper<ModerationReport>()
                .eq(StringUtils.hasText(status), ModerationReport::getStatus, status)
                .like(likeKeyword != null, ModerationReport::getReason, likeKeyword)
                .orderByDesc(ModerationReport::getId);
        Page<ModerationReport> result = moderationReportMapper.selectPage(new Page<>(current, pageSize), wrapper);
        return result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public ModerationReportVO get(Long id) {
        requireAdmin();
        return toVO(getReport(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ModerationReportVO create(ModerationReportCreateRequest request) {
        requireAdmin();
        ModerationReport report = new ModerationReport();
        report.setTargetType(request.getTargetType().trim());
        report.setTargetId(request.getTargetId());
        report.setReason(request.getReason().trim());
        report.setReporterId(SecurityUtils.getUserId());
        report.setStatus("PENDING");
        moderationReportMapper.insert(report);
        return toVO(moderationReportMapper.selectById(report.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ModerationReportVO update(Long id, ModerationReportUpdateRequest request) {
        requireAdmin();
        ModerationReport report = getReport(id);

        if (request.getTargetType() != null) {
            report.setTargetType(request.getTargetType().trim());
        }
        if (request.getReason() != null) {
            String reason = request.getReason().trim();
            if (reason.isEmpty()) {
                throw new BusinessException(ResultCode.PARAM_ERROR);
            }
            report.setReason(reason);
        }
        if (request.getStatus() != null) {
            report.setStatus(request.getStatus());
        }
        if (request.getResult() != null) {
            report.setResult(request.getResult());
        }

        moderationReportMapper.updateById(report);
        return toVO(moderationReportMapper.selectById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        requireAdmin();
        getReport(id);
        moderationReportMapper.deleteById(id);
    }

    private void requireAdmin() {
        if (!"ADMIN".equals(SecurityUtils.getLoginUser().getRole())) {
            throw new BusinessException(ResultCode.ADMIN_REQUIRED);
        }
    }

    private ModerationReport getReport(Long id) {
        ModerationReport report = moderationReportMapper.selectById(id);
        if (report == null) {
            throw new BusinessException(ResultCode.MODERATION_REPORT_NOT_FOUND);
        }
        return report;
    }

    private ModerationReportVO toVO(ModerationReport report) {
        ModerationReportVO vo = new ModerationReportVO();
        vo.setId(report.getId());
        vo.setTargetType(report.getTargetType());
        vo.setTargetId(report.getTargetId());
        vo.setReason(report.getReason());
        vo.setReporterId(report.getReporterId());
        vo.setStatus(report.getStatus());
        vo.setResult(report.getResult());
        vo.setCreatedAt(report.getCreatedAt());
        vo.setUpdatedAt(report.getUpdatedAt());
        return vo;
    }
}
