package com.lingxi.controller;

import com.lingxi.common.Result;
import com.lingxi.vo.PageResult;
import com.lingxi.dto.AdminUserCreateRequest;
import com.lingxi.dto.AdminUserUpdateRequest;
import com.lingxi.service.AdminUserService;
import com.lingxi.vo.AdminUserVO;
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
 * 管理端用户管理接口。
 *
 * <p>接口内部会校验当前登录用户是否为 ADMIN。</p>
 */
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Tag(name = "12. 系统用户管理")
public class AdminUserController {

    private final AdminUserService adminUserService;

    /**
     * 用户列表。
     */
    @GetMapping
    @Operation(summary = "用户列表")
    public Result<PageResult<AdminUserVO>> list(@RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "20") int size,
                                          @RequestParam(required = false) String keyword) {
        return Result.success(adminUserService.list(page, size, keyword));
    }

    /**
     * 用户详情。
     */
    @GetMapping("/{id}")
    @Operation(summary = "用户详情")
    public Result<AdminUserVO> get(@PathVariable Long id) {
        return Result.success(adminUserService.get(id));
    }

    /**
     * 新建用户。
     */
    @PostMapping
    @Operation(summary = "新建用户")
    public Result<AdminUserVO> create(@Valid @RequestBody AdminUserCreateRequest request) {
        return Result.success(adminUserService.create(request));
    }

    /**
     * 更新用户。
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新用户")
    public Result<AdminUserVO> update(@PathVariable Long id,
                                      @Valid @RequestBody AdminUserUpdateRequest request) {
        return Result.success(adminUserService.update(id, request));
    }

    /**
     * 删除用户。
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    public Result<Void> delete(@PathVariable Long id) {
        adminUserService.delete(id);
        return Result.success();
    }
}
