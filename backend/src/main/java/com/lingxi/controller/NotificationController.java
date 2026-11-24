package com.lingxi.controller;

import com.lingxi.common.Result;
import com.lingxi.dto.IdsRequest;
import com.lingxi.dto.NotificationCreateRequest;
import com.lingxi.dto.NotificationUpdateRequest;
import com.lingxi.service.NotificationService;
import com.lingxi.vo.NotificationVO;
import com.lingxi.vo.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

import java.util.List;
import java.util.Map;

/**
 * 通知中心接口。
 */
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "通知中心", description = "分页查询、标记已读与批量删除")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 分页查询通知。前端按 { list, total } 渲染列表与分页控件。
     */
    @GetMapping
    @Operation(summary = "分页查询我的通知")
    public Result<PageResult<NotificationVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(defaultValue = "false") boolean readOnly,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword) {
        return Result.success(notificationService.listMine(page, size, unreadOnly, readOnly, type, keyword));
    }

    /**
     * 通知详情。
     */
    @GetMapping("/{id}")
    @Operation(summary = "通知详情")
    public Result<NotificationVO> get(@PathVariable Long id) {
        return Result.success(notificationService.get(id));
    }

    /**
     * 新建通知（接收人为当前用户）。
     */
    @PostMapping
    @Operation(summary = "新建通知")
    public Result<NotificationVO> create(@Valid @RequestBody NotificationCreateRequest request) {
        return Result.success(notificationService.create(request));
    }

    /**
     * 更新通知内容或已读状态。
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新通知")
    public Result<NotificationVO> update(@PathVariable Long id,
                                         @Valid @RequestBody NotificationUpdateRequest request) {
        return Result.success(notificationService.update(id, request));
    }

    /**
     * 未读通知数量，供导航角标使用。
     */
    @GetMapping("/unread-count")
    @Operation(summary = "查询未读通知数量")
    public Result<Map<String, Long>> unreadCount() {
        return Result.success(java.util.Map.of("count", notificationService.unreadCount()));
    }

    /**
     * 标记单条通知已读。
     */
    @PutMapping("/{id}/read")
    @Operation(summary = "标记单条已读")
    public Result<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return Result.success();
    }

    /**
     * 全部标记已读。
     */
    @PutMapping("/read-all")
    @Operation(summary = "全部标记已读")
    public Result<Void> markAllRead() {
        notificationService.markAllRead();
        return Result.success();
    }

    /**
     * 批量删除通知：一次请求完成，替代前端逐条 DELETE。
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除通知")
    public Result<Void> deleteBatch(@Valid @RequestBody IdsRequest request) {
        notificationService.deleteBatch(request.getIds());
        return Result.success();
    }

    /**
     * 删除单条通知。
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除单条通知")
    public Result<Void> delete(@PathVariable Long id) {
        notificationService.delete(id);
        return Result.success();
    }
}
