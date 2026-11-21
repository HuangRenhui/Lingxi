package com.lingxi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingxi.common.BusinessException;
import com.lingxi.common.ResultCode;
import com.lingxi.dto.ArtifactCreateRequest;
import com.lingxi.dto.ArtifactUpdateRequest;
import com.lingxi.entity.Artifact;
import com.lingxi.mapper.ArtifactMapper;
import com.lingxi.security.SecurityUtils;
import com.lingxi.service.ArtifactService;
import com.lingxi.service.SensitiveWordService;
import com.lingxi.vo.ArtifactVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 产物基础服务实现。
 */
@Service
@RequiredArgsConstructor
public class ArtifactServiceImpl implements ArtifactService {

    /** 单页最大条数，与全局分页上限保持一致 */
    private static final int MAX_PAGE_SIZE = 100;

    private final ArtifactMapper artifactMapper;
    private final SensitiveWordService sensitiveWordService;

    @Override
    public List<ArtifactVO> listMine(int page, int size, String type, String keyword, Integer trashed) {
        // 页码与页大小做边界收敛，避免异常参数打到数据库
        long current = Math.max(1, page);
        long pageSize = Math.min(Math.max(1, size), MAX_PAGE_SIZE);
        // 缺省返回全部作品；0 只看正常作品，1 只看回收站
        if (trashed != null && trashed != 0 && trashed != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        String typeFilter = StringUtils.hasText(type) ? type.trim().toUpperCase(Locale.ROOT) : null;
        String keywordFilter = StringUtils.hasText(keyword) ? keyword.trim() : null;
        // 关键字必须转义 LIKE 通配符，否则 "%" 会让条件退化为全表匹配
        String likeKeyword = keywordFilter == null ? null
                : keywordFilter.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        LambdaQueryWrapper<Artifact> wrapper = new LambdaQueryWrapper<Artifact>()
                .eq(Artifact::getUserId, SecurityUtils.getUserId())
                .eq(trashed != null, Artifact::getTrashed, trashed)
                .eq(typeFilter != null, Artifact::getType, typeFilter)
                .and(likeKeyword != null, w -> w
                        .like(Artifact::getTitle, likeKeyword)
                        .or()
                        .like(Artifact::getPrompt, likeKeyword))
                .orderByDesc(Artifact::getId);
        Page<Artifact> result = artifactMapper.selectPage(new Page<>(current, pageSize), wrapper);
        return result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public ArtifactVO get(Long id) {
        return toVO(getOwnedArtifact(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArtifactVO create(ArtifactCreateRequest request) {
        Artifact artifact = new Artifact();
        artifact.setUserId(SecurityUtils.getUserId());
        artifact.setTitle(request.getTitle().trim());
        artifact.setType(request.getType());
        artifact.setToolKey(trimToNull(request.getToolKey()));
        artifact.setToolName(trimToNull(request.getToolName()));
        artifact.setPrompt(trimToNull(request.getPrompt()));
        artifact.setUrl(request.getUrl().trim());
        artifact.setPosterUrl(trimToNull(request.getPosterUrl()));
        artifact.setSizeBytes(request.getSizeBytes() == null ? 0L : request.getSizeBytes());
        // 用户可自主编辑的文本进入敏感词校验，命中直接拒绝入库
        sensitiveWordService.checkContent(artifact.getTitle(), artifact.getPrompt());
        // 新建产物默认未收藏、不在回收站、待审核
        artifact.setFavorite(0);
        artifact.setTrashed(0);
        artifact.setStatus("PENDING");
        artifactMapper.insert(artifact);
        // 重新查询以拿到数据库生成的创建/更新时间
        return toVO(artifactMapper.selectById(artifact.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArtifactVO update(Long id, ArtifactUpdateRequest request) {
        Artifact artifact = getOwnedArtifact(id);
        // 可空字段需要用 UpdateWrapper 显式 set(null) 才能清空：
        // updateById 默认 NOT_NULL 策略会跳过 null 字段，导致「传入空串清空」静默失效。
        LambdaUpdateWrapper<Artifact> update = new LambdaUpdateWrapper<Artifact>().eq(Artifact::getId, id);
        boolean changed = false;

        if (request.getTitle() != null) {
            String title = request.getTitle().trim();
            if (title.isEmpty()) {
                throw new BusinessException(ResultCode.PARAM_ERROR);
            }
            artifact.setTitle(title);
            update.set(Artifact::getTitle, title);
            changed = true;
        }
        if (request.getType() != null) {
            artifact.setType(request.getType());
            update.set(Artifact::getType, artifact.getType());
            changed = true;
        }
        if (request.getToolKey() != null) {
            artifact.setToolKey(trimToNull(request.getToolKey()));
            update.set(Artifact::getToolKey, artifact.getToolKey());
            changed = true;
        }
        if (request.getToolName() != null) {
            artifact.setToolName(trimToNull(request.getToolName()));
            update.set(Artifact::getToolName, artifact.getToolName());
            changed = true;
        }
        if (request.getPrompt() != null) {
            artifact.setPrompt(trimToNull(request.getPrompt()));
            update.set(Artifact::getPrompt, artifact.getPrompt());
            changed = true;
        }
        if (request.getUrl() != null) {
            String url = request.getUrl().trim();
            if (url.isEmpty()) {
                throw new BusinessException(ResultCode.PARAM_ERROR);
            }
            artifact.setUrl(url);
            update.set(Artifact::getUrl, url);
            changed = true;
        }
        if (request.getPosterUrl() != null) {
            artifact.setPosterUrl(trimToNull(request.getPosterUrl()));
            update.set(Artifact::getPosterUrl, artifact.getPosterUrl());
            changed = true;
        }
        if (request.getSizeBytes() != null) {
            artifact.setSizeBytes(request.getSizeBytes());
            update.set(Artifact::getSizeBytes, artifact.getSizeBytes());
            changed = true;
        }
        if (request.getFavorite() != null) {
            artifact.setFavorite(request.getFavorite());
            update.set(Artifact::getFavorite, artifact.getFavorite());
            changed = true;
        }
        if (request.getTrashed() != null) {
            artifact.setTrashed(request.getTrashed());
            update.set(Artifact::getTrashed, artifact.getTrashed());
            changed = true;
        }

        // 只校验本次改动的文本，避免收藏/回收站等操作被历史内容误拦截
        sensitiveWordService.checkContent(
                request.getTitle() == null ? null : artifact.getTitle(),
                request.getPrompt() == null ? null : artifact.getPrompt());

        if (!changed) {
            return toVO(artifact);
        }
        artifactMapper.update(null, update);
        return toVO(artifactMapper.selectById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 先校验归属，避免越权删除
        getOwnedArtifact(id);
        artifactMapper.deleteById(id);
    }

    /**
     * 查询产物并校验归属。
     */
    private Artifact getOwnedArtifact(Long id) {
        Artifact artifact = artifactMapper.selectById(id);
        if (artifact == null) {
            throw new BusinessException(ResultCode.ARTIFACT_NOT_FOUND);
        }
        if (!SecurityUtils.getUserId().equals(artifact.getUserId())) {
            throw new BusinessException(ResultCode.ARTIFACT_NOT_OWNED);
        }
        return artifact;
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
    private ArtifactVO toVO(Artifact artifact) {
        ArtifactVO vo = new ArtifactVO();
        vo.setId(artifact.getId());
        vo.setTitle(artifact.getTitle());
        vo.setType(artifact.getType());
        vo.setToolKey(artifact.getToolKey());
        vo.setToolName(artifact.getToolName());
        vo.setPrompt(artifact.getPrompt());
        vo.setUrl(artifact.getUrl());
        vo.setPosterUrl(artifact.getPosterUrl());
        vo.setSizeBytes(artifact.getSizeBytes());
        vo.setFavorite(artifact.getFavorite());
        vo.setTrashed(artifact.getTrashed());
        vo.setStatus(artifact.getStatus());
        vo.setCreatedAt(artifact.getCreatedAt());
        vo.setUpdatedAt(artifact.getUpdatedAt());
        return vo;
    }
}
