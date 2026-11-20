package com.lingxi.controller;

import com.lingxi.common.Result;
import com.lingxi.dto.ApplicationCreateRequest;
import com.lingxi.dto.ApplicationUpdateRequest;
import com.lingxi.service.ApplicationService;
import com.lingxi.vo.ApplicationVO;
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
 * AI 应用基础管理接口。
 *
 * <p>所有接口均按当前登录用户隔离，只能操作自己创建的应用。</p>
 */
@RestController
@RequestMapping("/applications")
@RequiredArgsConstructor
@Tag(name = "08. AI 应用管理")
public class ApplicationController {

    private final ApplicationService applicationService;

    /**
     * 分页查询当前用户的应用。
     */
    @GetMapping
    @Operation(summary = "我的应用列表")
    public Result<List<ApplicationVO>> list(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        return Result.success(applicationService.listMine(page, size));
    }

    /**
     * 查询应用详情。
     */
    @GetMapping("/{id}")
    @Operation(summary = "应用详情")
    public Result<ApplicationVO> get(@PathVariable Long id) {
        return Result.success(applicationService.get(id));
    }

    /**
     * 新建应用。
     */
    @PostMapping
    @Operation(summary = "新建应用")
    public Result<ApplicationVO> create(@Valid @RequestBody ApplicationCreateRequest request) {
        return Result.success(applicationService.create(request));
    }

    /**
     * 更新应用，仅更新传入的字段。
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新应用")
    public Result<ApplicationVO> update(@PathVariable Long id,
                                        @Valid @RequestBody ApplicationUpdateRequest request) {
        return Result.success(applicationService.update(id, request));
    }

    /**
     * 删除应用（逻辑删除）。
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除应用")
    public Result<Void> delete(@PathVariable Long id) {
        applicationService.delete(id);
        return Result.success();
    }
}
