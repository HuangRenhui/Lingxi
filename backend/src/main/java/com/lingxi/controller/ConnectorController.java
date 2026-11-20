package com.lingxi.controller;

import com.lingxi.common.Result;
import com.lingxi.dto.ConnectorCreateRequest;
import com.lingxi.dto.ConnectorUpdateRequest;
import com.lingxi.service.ConnectorService;
import com.lingxi.vo.ConnectorVO;
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
 * 连接器基础管理接口。
 *
 * <p>所有接口均按当前登录用户隔离，只能操作自己创建的连接器。</p>
 */
@RestController
@RequestMapping("/connectors")
@RequiredArgsConstructor
@Tag(name = "14. 连接器管理")
public class ConnectorController {

    private final ConnectorService connectorService;

    /**
     * 分页查询当前用户的连接器。
     */
    @GetMapping
    @Operation(summary = "我的连接器列表")
    public Result<List<ConnectorVO>> list(@RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "20") int size) {
        return Result.success(connectorService.listMine(page, size));
    }

    /**
     * 查询连接器详情。
     */
    @GetMapping("/{id}")
    @Operation(summary = "连接器详情")
    public Result<ConnectorVO> get(@PathVariable Long id) {
        return Result.success(connectorService.get(id));
    }

    /**
     * 新建连接器。
     */
    @PostMapping
    @Operation(summary = "新建连接器")
    public Result<ConnectorVO> create(@Valid @RequestBody ConnectorCreateRequest request) {
        return Result.success(connectorService.create(request));
    }

    /**
     * 更新连接器，仅更新传入的字段。
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新连接器")
    public Result<ConnectorVO> update(@PathVariable Long id,
                                      @Valid @RequestBody ConnectorUpdateRequest request) {
        return Result.success(connectorService.update(id, request));
    }

    /**
     * 删除连接器（逻辑删除）。
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除连接器")
    public Result<Void> delete(@PathVariable Long id) {
        connectorService.delete(id);
        return Result.success();
    }
}
