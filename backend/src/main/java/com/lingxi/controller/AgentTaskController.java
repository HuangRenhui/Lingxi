package com.lingxi.controller;

import com.lingxi.common.Result;
import com.lingxi.dto.TaskCreateRequest;
import com.lingxi.dto.TaskStatusRequest;
import com.lingxi.dto.TaskUpdateRequest;
import com.lingxi.service.AgentTaskService;
import com.lingxi.vo.TaskVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import javax.validation.Valid;
import java.util.List;

/**
 * 定时任务管理（B7）。
 */
@RestController
@RequestMapping("/agent/tasks")
@Tag(name = "07. 无人值守 Agent")
public class AgentTaskController {

    private final AgentTaskService agentTaskService;

    public AgentTaskController(AgentTaskService agentTaskService) {
        this.agentTaskService = agentTaskService;
    }

    @GetMapping
    @Operation(summary = "我的定时任务列表")
    public Result<List<TaskVO>> list(@RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "20") int size) {
        return Result.success(agentTaskService.listMine(page, size));
    }

    @PostMapping
    @Operation(summary = "新建定时任务")
    public Result<TaskVO> create(@Valid @RequestBody TaskCreateRequest request) {
        return Result.success(agentTaskService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新定时任务")
    public Result<TaskVO> update(@PathVariable Long id, @Valid @RequestBody TaskUpdateRequest request) {
        return Result.success(agentTaskService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "启用 / 停用任务")
    public Result<TaskVO> updateStatus(@PathVariable Long id, @Valid @RequestBody TaskStatusRequest request) {
        return Result.success(agentTaskService.updateStatus(id, request.getStatus()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除定时任务")
    public Result<Void> delete(@PathVariable Long id) {
        agentTaskService.delete(id);
        return Result.success();
    }
}
