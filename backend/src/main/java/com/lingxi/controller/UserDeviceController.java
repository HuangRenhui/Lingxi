package com.lingxi.controller;

import com.lingxi.common.Result;
import com.lingxi.dto.DeviceCreateRequest;
import com.lingxi.dto.DeviceUpdateRequest;
import com.lingxi.dto.IdsRequest;
import com.lingxi.service.UserDeviceService;
import com.lingxi.vo.DeviceVO;
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
 * 登录设备接口。
 */
@RestController
@RequestMapping("/devices")
@RequiredArgsConstructor
@Tag(name = "11. 用户设备")
public class UserDeviceController {

    private final UserDeviceService userDeviceService;

    /**
     * 当前用户的登录设备列表。
     */
    @GetMapping
    @Operation(summary = "设备列表")
    public Result<List<DeviceVO>> list(@RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "20") int size) {
        return Result.success(userDeviceService.listMine(page, size));
    }

    /**
     * 设备详情。
     */
    @GetMapping("/{id}")
    @Operation(summary = "设备详情")
    public Result<DeviceVO> get(@PathVariable Long id) {
        return Result.success(userDeviceService.get(id));
    }

    /**
     * 新增设备记录。
     */
    @PostMapping
    @Operation(summary = "新增设备")
    public Result<DeviceVO> create(@Valid @RequestBody DeviceCreateRequest request) {
        return Result.success(userDeviceService.create(request));
    }

    /**
     * 重命名设备。
     */
    @PutMapping("/{id}")
    @Operation(summary = "重命名设备")
    public Result<DeviceVO> update(@PathVariable Long id,
                                   @Valid @RequestBody DeviceUpdateRequest request) {
        return Result.success(userDeviceService.update(id, request));
    }

    /**
     * 批量删除设备：一次请求下线多台设备。
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除设备")
    public Result<Void> deleteBatch(@Valid @RequestBody IdsRequest request) {
        userDeviceService.deleteBatch(request.getIds());
        return Result.success();
    }

    /**
     * 删除设备记录。
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除设备")
    public Result<Void> delete(@PathVariable Long id) {
        userDeviceService.delete(id);
        return Result.success();
    }
}
