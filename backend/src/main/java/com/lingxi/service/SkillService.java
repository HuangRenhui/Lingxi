package com.lingxi.service;

import com.lingxi.dto.SkillCreateRequest;
import com.lingxi.dto.SkillUpdateRequest;
import com.lingxi.vo.SkillVO;

import java.util.List;

/**
 * 技能基础服务。
 */
public interface SkillService {

    /**
     * 分页查询当前用户的技能。
     */
    List<SkillVO> listMine(int page, int size);

    /**
     * 查询技能详情。
     */
    SkillVO get(Long id);

    /**
     * 新建技能。
     */
    SkillVO create(SkillCreateRequest request);

    /**
     * 更新技能，仅更新传入的字段。
     */
    SkillVO update(Long id, SkillUpdateRequest request);

    /**
     * 删除技能（逻辑删除）。
     */
    void delete(Long id);
}
