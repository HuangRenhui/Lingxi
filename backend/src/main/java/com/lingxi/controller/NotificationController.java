package com.lingxi.controller;

import com.lingxi.common.Result;
import com.lingxi.service.NotificationService;
import com.lingxi.vo.NotificationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 通知中心接口。
 *
 * <p>通知由系统产生，用户侧提供查询、已读与删除。</p>
 */
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "10. 通知中心")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 当前用户的通知列表。
     */
    @GetMapping
    @Operation(summary = "通知列表")
    public Result<List<NotificationVO>> list(@RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "20") int size,
                                             @RequestParam(defaultValue = "false") boolean unreadOnly) {
        return Result.success(notificationService.listMine(page, size, unreadOnly));
    }

    /**
     * 未读通知数量。
     */
    @GetMapping("/unread-count")
    @Operation(summary = "未读数量")
    public Result<Long> unreadCount() {
        return Result.success(notificationService.unreadCount());
    }

    /**
     * 标记单条通知已读。
     */
    @PutMapping("/{id}/read")
    @Operation(summary = "标记已读")
    public Result<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return Result.success();
    }

    /**
     * 全部标记已读。
     */
    @PutMapping("/read-all")
    @Operation(summary = "全部已读")
    public Result<Void> markAllRead() {
        notificationService.markAllRead();
        return Result.success();
    }

    /**
     * 删除通知。
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除通知")
    public Result<Void> delete(@PathVariable Long id) {
        notificationService.delete(id);
        return Result.success();
    }
}
