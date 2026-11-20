package com.lingxi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingxi.common.BusinessException;
import com.lingxi.common.ResultCode;
import com.lingxi.dto.DeviceCreateRequest;
import com.lingxi.dto.DeviceUpdateRequest;
import com.lingxi.entity.UserDevice;
import com.lingxi.mapper.UserDeviceMapper;
import com.lingxi.security.SecurityUtils;
import com.lingxi.service.UserDeviceService;
import com.lingxi.vo.DeviceVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 登录设备服务实现。
 */
@Service
@RequiredArgsConstructor
public class UserDeviceServiceImpl implements UserDeviceService {

    private static final int MAX_PAGE_SIZE = 100;

    private final UserDeviceMapper userDeviceMapper;

    @Override
    public List<DeviceVO> listMine(int page, int size) {
        long current = Math.max(1, page);
        long pageSize = Math.min(Math.max(1, size), MAX_PAGE_SIZE);
        LambdaQueryWrapper<UserDevice> wrapper = new LambdaQueryWrapper<UserDevice>()
                .eq(UserDevice::getUserId, SecurityUtils.getUserId())
                .orderByDesc(UserDevice::getCurrentFlag)
                .orderByDesc(UserDevice::getId);
        Page<UserDevice> result = userDeviceMapper.selectPage(new Page<>(current, pageSize), wrapper);
        return result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeviceVO create(DeviceCreateRequest request) {
        UserDevice device = new UserDevice();
        device.setUserId(SecurityUtils.getUserId());
        device.setName(request.getName().trim());
        device.setOs(request.getOs());
        device.setBrowser(request.getBrowser());
        device.setIp(request.getIp());
        device.setLocation(request.getLocation());
        device.setCurrentFlag(0);
        device.setLastActiveAt(LocalDateTime.now());
        userDeviceMapper.insert(device);
        return toVO(userDeviceMapper.selectById(device.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeviceVO update(Long id, DeviceUpdateRequest request) {
        UserDevice device = getOwnedDevice(id);
        if (request.getName() != null) {
            String name = request.getName().trim();
            if (name.isEmpty()) {
                throw new BusinessException(ResultCode.PARAM_ERROR);
            }
            device.setName(name);
        }
        userDeviceMapper.updateById(device);
        return toVO(userDeviceMapper.selectById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        getOwnedDevice(id);
        userDeviceMapper.deleteById(id);
    }

    /**
     * 查询设备并校验归属。
     */
    private UserDevice getOwnedDevice(Long id) {
        UserDevice device = userDeviceMapper.selectById(id);
        if (device == null) {
            throw new BusinessException(ResultCode.DEVICE_NOT_FOUND);
        }
        if (!SecurityUtils.getUserId().equals(device.getUserId())) {
            throw new BusinessException(ResultCode.DEVICE_NOT_OWNED);
        }
        return device;
    }

    private DeviceVO toVO(UserDevice device) {
        DeviceVO vo = new DeviceVO();
        vo.setId(device.getId());
        vo.setName(device.getName());
        vo.setOs(device.getOs());
        vo.setBrowser(device.getBrowser());
        vo.setIp(device.getIp());
        vo.setLocation(device.getLocation());
        vo.setCurrentFlag(device.getCurrentFlag());
        vo.setLastActiveAt(device.getLastActiveAt());
        vo.setCreatedAt(device.getCreatedAt());
        return vo;
    }
}
