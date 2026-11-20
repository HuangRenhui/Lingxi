package com.lingxi.controller;

import com.lingxi.common.Result;
import com.lingxi.dto.ArtifactCreateRequest;
import com.lingxi.dto.ArtifactUpdateRequest;
import com.lingxi.service.ArtifactService;
import com.lingxi.vo.ArtifactVO;
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

import javax.validation.Valid;
import java.util.List;

/**
 * 产物基础管理接口。
 *
 * <p>所有接口均按当前登录用户隔离，只能操作自己创建的作品。</p>
 */
@RestController
@RequestMapping("/artifacts")
@RequiredArgsConstructor
@Tag(name = "15. 产物管理")
public class ArtifactController {

    private final ArtifactService artifactService;

    /**
     * 分页查询当前用户的产物，可按类型、关键词与回收站状态筛选。
     */
    @GetMapping
    @Operation(summary = "我的产物列表")
    public Result<List<ArtifactVO>> list(@RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "20") int size,
                                         @RequestParam(required = false) String type,
                                         @RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) Integer trashed) {
        return Result.success(artifactService.listMine(page, size, type, keyword, trashed));
    }

    /**
     * 查询产物详情。
     */
    @GetMapping("/{id}")
    @Operation(summary = "产物详情")
    public Result<ArtifactVO> get(@PathVariable Long id) {
        return Result.success(artifactService.get(id));
    }

    /**
     * 新建产物。
     */
    @PostMapping
    @Operation(summary = "新建产物")
    public Result<ArtifactVO> create(@Valid @RequestBody ArtifactCreateRequest request) {
        return Result.success(artifactService.create(request));
    }

    /**
     * 更新产物，仅更新传入的字段。
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新产物")
    public Result<ArtifactVO> update(@PathVariable Long id,
                                     @Valid @RequestBody ArtifactUpdateRequest request) {
        return Result.success(artifactService.update(id, request));
    }

    /**
     * 删除产物（逻辑删除）。
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除产物")
    public Result<Void> delete(@PathVariable Long id) {
        artifactService.delete(id);
        return Result.success();
    }
}
