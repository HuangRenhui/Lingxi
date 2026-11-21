package com.lingxi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingxi.common.BusinessException;
import com.lingxi.common.ResultCode;
import com.lingxi.dto.ApplicationCreateRequest;
import com.lingxi.dto.ApplicationUpdateRequest;
import com.lingxi.entity.AiApplication;
import com.lingxi.mapper.AiApplicationMapper;
import com.lingxi.security.SecurityUtils;
import com.lingxi.service.ApplicationService;
import com.lingxi.service.SensitiveWordService;
import com.lingxi.vo.ApplicationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AI 应用基础服务实现。
 */
@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    /** 单页最大条数，与全局分页上限保持一致 */
    private static final int MAX_PAGE_SIZE = 100;

    private final AiApplicationMapper applicationMapper;
    private final SensitiveWordService sensitiveWordService;

    @Override
    public List<ApplicationVO> listMine(int page, int size) {
        // 页码与页大小做边界收敛，避免异常参数打到数据库
        long current = Math.max(1, page);
        long pageSize = Math.min(Math.max(1, size), MAX_PAGE_SIZE);
        LambdaQueryWrapper<AiApplication> wrapper = new LambdaQueryWrapper<AiApplication>()
                .eq(AiApplication::getUserId, SecurityUtils.getUserId())
                .orderByDesc(AiApplication::getId);
        Page<AiApplication> result = applicationMapper.selectPage(new Page<>(current, pageSize), wrapper);
        return result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public ApplicationVO get(Long id) {
        return toVO(getOwnedApplication(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApplicationVO create(ApplicationCreateRequest request) {
        AiApplication application = new AiApplication();
        application.setUserId(SecurityUtils.getUserId());
        application.setName(request.getName().trim());
        application.setDescription(trimToNull(request.getDescription()));
        // 未指定类型时按通用应用处理
        application.setAppType(StringUtils.hasText(request.getAppType()) ? request.getAppType() : "GENERAL");
        application.setStatus(1);
        sensitiveWordService.checkContent(application.getName(), application.getDescription());
        applicationMapper.insert(application);
        // 重新查询以拿到数据库生成的创建/更新时间；并发删除时给出业务错误而不是 NPE
        AiApplication saved = applicationMapper.selectById(application.getId());
        if (saved == null) {
            throw new BusinessException(ResultCode.APPLICATION_NOT_FOUND);
        }
        return toVO(saved);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApplicationVO update(Long id, ApplicationUpdateRequest request) {
        AiApplication application = getOwnedApplication(id);

        if (request.getName() != null) {
            String name = request.getName().trim();
            if (name.isEmpty()) {
                throw new BusinessException(ResultCode.PARAM_ERROR);
            }
            application.setName(name);
        }
        if (request.getDescription() != null) {
            application.setDescription(trimToNull(request.getDescription()));
        }
        if (request.getAppType() != null) {
            application.setAppType(request.getAppType());
        }
        if (request.getStatus() != null) {
            application.setStatus(request.getStatus());
        }

        // 只校验本次改动的文本，避免启用/停用被历史内容误拦截
        sensitiveWordService.checkContent(
                request.getName() == null ? null : application.getName(),
                request.getDescription() == null ? null : application.getDescription());

        // 用 UpdateWrapper 显式写入 null，保证「传入空串清空简介」真实生效
        LambdaUpdateWrapper<AiApplication> update =
                new LambdaUpdateWrapper<AiApplication>().eq(AiApplication::getId, id);
        boolean changed = false;
        if (request.getName() != null) {
            update.set(AiApplication::getName, application.getName());
            changed = true;
        }
        if (request.getDescription() != null) {
            update.set(AiApplication::getDescription, application.getDescription());
            changed = true;
        }
        if (request.getAppType() != null) {
            update.set(AiApplication::getAppType, application.getAppType());
            changed = true;
        }
        if (request.getStatus() != null) {
            update.set(AiApplication::getStatus, application.getStatus());
            changed = true;
        }
        if (!changed) {
            return toVO(application);
        }
        applicationMapper.update(null, update);
        return toVO(applicationMapper.selectById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 先校验归属，避免越权删除
        getOwnedApplication(id);
        applicationMapper.deleteById(id);
    }

    /**
     * 查询应用并校验归属。
     */
    private AiApplication getOwnedApplication(Long id) {
        AiApplication application = applicationMapper.selectById(id);
        if (application == null) {
            throw new BusinessException(ResultCode.APPLICATION_NOT_FOUND);
        }
        if (!SecurityUtils.getUserId().equals(application.getUserId())) {
            throw new BusinessException(ResultCode.APPLICATION_NOT_OWNED);
        }
        return application;
    }

    /**
     * 空字符串归一为 null，避免前端把可空字段传成空串。
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
    private ApplicationVO toVO(AiApplication application) {
        ApplicationVO vo = new ApplicationVO();
        vo.setId(application.getId());
        vo.setName(application.getName());
        vo.setDescription(application.getDescription());
        vo.setAppType(application.getAppType());
        vo.setStatus(application.getStatus());
        vo.setCreatedAt(application.getCreatedAt());
        vo.setUpdatedAt(application.getUpdatedAt());
        return vo;
    }
}
