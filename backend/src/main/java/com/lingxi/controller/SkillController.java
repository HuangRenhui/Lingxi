package com.lingxi.controller;

import com.lingxi.common.Result;
import com.lingxi.vo.PageResult;
import com.lingxi.dto.SkillCreateRequest;
import com.lingxi.dto.SkillUpdateRequest;
import com.lingxi.service.SkillService;
import com.lingxi.vo.SkillVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 技能基础管理接口。
 *
 * <p>所有接口均按当前登录用户隔离，只能操作自己创建的技能。</p>
 */
@RestController
@RequestMapping("/skills")
@RequiredArgsConstructor
@Tag(name = "13. 技能管理")
public class SkillController {

    private final SkillService skillService;

    /**
     * 分页查询当前用户的技能。
     */
    @GetMapping
    @Operation(summary = "我的技能列表")
    public Result<PageResult<SkillVO>> list(@RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "20") int size) {
        return Result.success(skillService.listMine(page, size));
    }

    /**
     * 查询技能详情。
     */
    @GetMapping("/{id}")
    @Operation(summary = "技能详情")
    public Result<SkillVO> get(@PathVariable Long id) {
        return Result.success(skillService.get(id));
    }

    /**
     * 新建技能。
     */
    @PostMapping
    @Operation(summary = "新建技能")
    public Result<SkillVO> create(@Valid @RequestBody SkillCreateRequest request) {
        return Result.success(skillService.create(request));
    }

    /**
     * 更新技能，仅更新传入的字段。
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新技能")
    public Result<SkillVO> update(@PathVariable Long id,
                                  @Valid @RequestBody SkillUpdateRequest request) {
        return Result.success(skillService.update(id, request));
    }

    /**
     * 删除技能（逻辑删除）。
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除技能")
    public Result<Void> delete(@PathVariable Long id) {
        skillService.delete(id);
        return Result.success();
    }
}
