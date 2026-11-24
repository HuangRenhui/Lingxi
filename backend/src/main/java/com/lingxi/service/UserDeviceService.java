package com.lingxi.service;

import com.lingxi.dto.DeviceCreateRequest;
import com.lingxi.dto.DeviceUpdateRequest;
import com.lingxi.vo.DeviceVO;

import java.util.List;

/**
 * 登录设备服务。
 */
public interface UserDeviceService {

    /**
     * 分页查询当前用户的登录设备。
     */
    List<DeviceVO> listMine(int page, int size);

    /**
     * 查询设备详情，仅限本人。
     */
    DeviceVO get(Long id);

    /**
     * 新增一条设备记录。
     */
    DeviceVO create(DeviceCreateRequest request);

    /**
     * 重命名设备。
     */
    DeviceVO update(Long id, DeviceUpdateRequest request);

    /**
     * 删除设备记录（等同于下线）。
     */
    void delete(Long id);

    /**
     * 批量删除设备记录（一次请求下线多台设备）。
     */
    void deleteBatch(List<Long> ids);
}
