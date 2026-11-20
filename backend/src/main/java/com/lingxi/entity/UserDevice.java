package com.lingxi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户登录设备实体，对应表 user_device。
 */
@Data
@TableName("user_device")
public class UserDevice {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 属主用户 */
    private Long userId;

    /** 设备名称，可由用户重命名 */
    private String name;

    /** 操作系统 */
    private String os;

    /** 浏览器 */
    private String browser;

    /** 登录 IP */
    private String ip;

    /** 登录地 */
    private String location;

    /** 是否当前设备：1 是，0 否 */
    private Integer currentFlag;

    /** 最近活跃时间 */
    private LocalDateTime lastActiveAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
