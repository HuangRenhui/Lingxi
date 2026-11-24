package com.lingxi.controller;

import com.lingxi.common.Result;
import com.lingxi.dto.ArtifactBatchUpdateRequest;
import com.lingxi.dto.ArtifactCreateRequest;
import com.lingxi.dto.ArtifactUpdateRequest;
import com.lingxi.dto.IdsRequest;
import com.lingxi.service.ArtifactService;
import com.lingxi.vo.ArtifactVO;
import com.lingxi.vo.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 产物管理接口。
 */
@RestController
@RequestMapping("/artifacts")
@RequiredArgsConstructor
@Tag(name = "产物管理", description = "分页查询、收藏、回收站与批量操作")
public class ArtifactController {

    private final ArtifactService artifactService;

    /**
     * 分页查询产物。所有筛选与排序均在下推到数据库执行，前端不再拉全量后本地过滤。
     */
    @GetMapping
    @Operation(summary = "分页查询我的产物")
    public Result<PageResult<ArtifactVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "0") Integer trashed,
            @RequestParam(required = false) Integer favorite,
            @RequestParam(required = false) String sort) {
        return Result.success(artifactService.listMine(page, size, type, keyword, trashed, favorite, sort));
    }

    /**
     * 查询单个产物详情。
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询产物详情")
    public Result<ArtifactVO> get(@PathVariable Long id) {
        return Result.success(artifactService.get(id));
    }

    /**
     * 新建产物（创作完成/本地任务回填）。
     */
    @PostMapping
    @Operation(summary = "新建产物")
    public Result<ArtifactVO> create(@Valid @RequestBody ArtifactCreateRequest request) {
        return Result.success(artifactService.create(request));
    }

    /**
     * 更新产物字段（收藏、移入回收站、重命名等）。
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新产物")
    public Result<ArtifactVO> update(@PathVariable Long id, @Valid @RequestBody ArtifactUpdateRequest request) {
        return Result.success(artifactService.update(id, request));
    }

    /**
     * 批量更新收藏/回收站状态。
     */
    @PatchMapping("/batch")
    @Operation(summary = "批量更新产物状态")
    public Result<Void> batchUpdate(@Valid @RequestBody ArtifactBatchUpdateRequest request) {
        artifactService.batchUpdate(request);
        return Result.success();
    }

    /**
     * 批量永久删除产物。
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除产物")
    public Result<Void> deleteBatch(@Valid @RequestBody IdsRequest request) {
        artifactService.deleteBatch(request.getIds());
        return Result.success();
    }

    /**
     * 删除单个产物（逻辑删除，进入回收站）。
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除产物")
    public Result<Void> delete(@PathVariable Long id) {
        artifactService.delete(id);
        return Result.success();
    }
}
