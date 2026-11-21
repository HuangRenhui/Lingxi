package com.lingxi.controller;

import com.lingxi.common.Result;
import com.lingxi.dto.ModerationReportCreateRequest;
import com.lingxi.dto.ModerationReportUpdateRequest;
import com.lingxi.service.ModerationReportService;
import com.lingxi.vo.ModerationReportVO;
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
 * 管理端内容举报接口。
 */
@RestController
@RequestMapping("/admin/moderation-reports")
@RequiredArgsConstructor
@Tag(name = "23. 内容举报管理")
public class AdminModerationReportController {

    private final ModerationReportService moderationReportService;

    @GetMapping
    @Operation(summary = "举报列表")
    public Result<List<ModerationReportVO>> list(@RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "20") int size,
                                                 @RequestParam(required = false) String status,
                                                 @RequestParam(required = false) String keyword) {
        return Result.success(moderationReportService.list(page, size, status, keyword));
    }

    @GetMapping("/{id}")
    @Operation(summary = "举报详情")
    public Result<ModerationReportVO> get(@PathVariable Long id) {
        return Result.success(moderationReportService.get(id));
    }

    @PostMapping
    @Operation(summary = "新建举报")
    public Result<ModerationReportVO> create(@Valid @RequestBody ModerationReportCreateRequest request) {
        return Result.success(moderationReportService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "处理举报")
    public Result<ModerationReportVO> update(@PathVariable Long id,
                                             @Valid @RequestBody ModerationReportUpdateRequest request) {
        return Result.success(moderationReportService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除举报")
    public Result<Void> delete(@PathVariable Long id) {
        moderationReportService.delete(id);
        return Result.success();
    }
}
