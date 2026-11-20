package com.lingxi.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录设备视图对象。
 */
@Data
@Schema(description = "登录设备")
public class DeviceVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String os;

    private String browser;

    private String ip;

    private String location;

    /** 是否当前设备：1 是，0 否 */
    private Integer currentFlag;

    private LocalDateTime lastActiveAt;

    private LocalDateTime createdAt;
}
