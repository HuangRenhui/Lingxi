package com.lingxi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingxi.common.BusinessException;
import com.lingxi.common.ResultCode;
import com.lingxi.dto.SkillCreateRequest;
import com.lingxi.dto.SkillUpdateRequest;
import com.lingxi.entity.AiSkill;
import com.lingxi.mapper.AiSkillMapper;
import com.lingxi.security.SecurityUtils;
import com.lingxi.service.SkillService;
import com.lingxi.vo.SkillVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 技能基础服务实现。
 */
@Service
@RequiredArgsConstructor
public class SkillServiceImpl implements SkillService {

    /** 单页最大条数，与全局分页上限保持一致 */
    private static final int MAX_PAGE_SIZE = 100;

    private final AiSkillMapper skillMapper;

    @Override
    public List<SkillVO> listMine(int page, int size) {
        // 页码与页大小做边界收敛，避免异常参数打到数据库
        long current = Math.max(1, page);
        long pageSize = Math.min(Math.max(1, size), MAX_PAGE_SIZE);
        LambdaQueryWrapper<AiSkill> wrapper = new LambdaQueryWrapper<AiSkill>()
                .eq(AiSkill::getUserId, SecurityUtils.getUserId())
                .orderByDesc(AiSkill::getId);
        Page<AiSkill> result = skillMapper.selectPage(new Page<>(current, pageSize), wrapper);
        return result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public SkillVO get(Long id) {
        return toVO(getOwnedSkill(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SkillVO create(SkillCreateRequest request) {
        AiSkill skill = new AiSkill();
        skill.setUserId(SecurityUtils.getUserId());
        // 技能名统一以 / 开头，与前端展示保持一致
        skill.setName(normalizeName(request.getName()));
        skill.setDescription(trimToNull(request.getDescription()));
        skill.setContent(request.getContent().trim());
        // 未指定分类时按社区技能处理
        skill.setCategory(StringUtils.hasText(request.getCategory()) ? request.getCategory() : "COMMUNITY");
        skill.setStatus(1);
        skillMapper.insert(skill);
        // 重新查询以拿到数据库生成的创建/更新时间
        return toVO(skillMapper.selectById(skill.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SkillVO update(Long id, SkillUpdateRequest request) {
        AiSkill skill = getOwnedSkill(id);

        if (request.getName() != null) {
            skill.setName(normalizeName(request.getName()));
        }
        if (request.getDescription() != null) {
            skill.setDescription(trimToNull(request.getDescription()));
        }
        if (request.getContent() != null) {
            String content = request.getContent().trim();
            if (content.isEmpty()) {
                throw new BusinessException(ResultCode.PARAM_ERROR);
            }
            skill.setContent(content);
        }
        if (request.getCategory() != null) {
            skill.setCategory(request.getCategory());
        }
        if (request.getStatus() != null) {
            skill.setStatus(request.getStatus());
        }

        skillMapper.updateById(skill);
        return toVO(skillMapper.selectById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 先校验归属，避免越权删除
        getOwnedSkill(id);
        skillMapper.deleteById(id);
    }

    /**
     * 查询技能并校验归属。
     */
    private AiSkill getOwnedSkill(Long id) {
        AiSkill skill = skillMapper.selectById(id);
        if (skill == null) {
            throw new BusinessException(ResultCode.SKILL_NOT_FOUND);
        }
        if (!SecurityUtils.getUserId().equals(skill.getUserId())) {
            throw new BusinessException(ResultCode.SKILL_NOT_OWNED);
        }
        return skill;
    }

    /**
     * 名称归一：去空白并补全开头的 /，空名称视为参数错误。
     */
    private String normalizeName(String name) {
        String trimmed = name == null ? "" : name.trim();
        if (trimmed.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return trimmed.startsWith("/") ? trimmed : "/" + trimmed;
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
    private SkillVO toVO(AiSkill skill) {
        SkillVO vo = new SkillVO();
        vo.setId(skill.getId());
        vo.setName(skill.getName());
        vo.setDescription(skill.getDescription());
        vo.setContent(skill.getContent());
        vo.setCategory(skill.getCategory());
        vo.setStatus(skill.getStatus());
        vo.setCreatedAt(skill.getCreatedAt());
        vo.setUpdatedAt(skill.getUpdatedAt());
        return vo;
    }
}
