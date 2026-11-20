package com.lingxi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingxi.common.BusinessException;
import com.lingxi.common.ResultCode;
import com.lingxi.dto.ArtifactCreateRequest;
import com.lingxi.dto.ArtifactUpdateRequest;
import com.lingxi.entity.Artifact;
import com.lingxi.mapper.ArtifactMapper;
import com.lingxi.security.SecurityUtils;
import com.lingxi.service.ArtifactService;
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

    @Override
    public List<ArtifactVO> listMine(int page, int size, String type, String keyword, Integer trashed) {
        // 页码与页大小做边界收敛，避免异常参数打到数据库
        long current = Math.max(1, page);
        long pageSize = Math.min(Math.max(1, size), MAX_PAGE_SIZE);
        // 缺省只查回收站之外的作品；1 表示只看回收站
        int trashedFlag = trashed == null ? 0 : trashed;
        if (trashedFlag != 0 && trashedFlag != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        String typeFilter = StringUtils.hasText(type) ? type.trim().toUpperCase(Locale.ROOT) : null;
        String keywordFilter = StringUtils.hasText(keyword) ? keyword.trim() : null;
        LambdaQueryWrapper<Artifact> wrapper = new LambdaQueryWrapper<Artifact>()
                .eq(Artifact::getUserId, SecurityUtils.getUserId())
                .eq(Artifact::getTrashed, trashedFlag)
                .eq(typeFilter != null, Artifact::getType, typeFilter)
                .and(keywordFilter != null, w -> w
                        .like(Artifact::getTitle, keywordFilter)
                        .or()
                        .like(Artifact::getPrompt, keywordFilter))
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
        // 新建产物默认未收藏、不在回收站、审核通过
        artifact.setFavorite(0);
        artifact.setTrashed(0);
        artifact.setStatus("APPROVED");
        artifactMapper.insert(artifact);
        // 重新查询以拿到数据库生成的创建/更新时间
        return toVO(artifactMapper.selectById(artifact.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArtifactVO update(Long id, ArtifactUpdateRequest request) {
        Artifact artifact = getOwnedArtifact(id);

        if (request.getTitle() != null) {
            String title = request.getTitle().trim();
            if (title.isEmpty()) {
                throw new BusinessException(ResultCode.PARAM_ERROR);
            }
            artifact.setTitle(title);
        }
        if (request.getType() != null) {
            artifact.setType(request.getType());
        }
        if (request.getToolKey() != null) {
            artifact.setToolKey(trimToNull(request.getToolKey()));
        }
        if (request.getToolName() != null) {
            artifact.setToolName(trimToNull(request.getToolName()));
        }
        if (request.getPrompt() != null) {
            artifact.setPrompt(trimToNull(request.getPrompt()));
        }
        if (request.getUrl() != null) {
            String url = request.getUrl().trim();
            if (url.isEmpty()) {
                throw new BusinessException(ResultCode.PARAM_ERROR);
            }
            artifact.setUrl(url);
        }
        if (request.getPosterUrl() != null) {
            artifact.setPosterUrl(trimToNull(request.getPosterUrl()));
        }
        if (request.getSizeBytes() != null) {
            artifact.setSizeBytes(request.getSizeBytes());
        }
        if (request.getFavorite() != null) {
            artifact.setFavorite(request.getFavorite());
        }
        if (request.getTrashed() != null) {
            artifact.setTrashed(request.getTrashed());
        }
        if (request.getStatus() != null) {
            artifact.setStatus(request.getStatus());
        }

        artifactMapper.updateById(artifact);
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
