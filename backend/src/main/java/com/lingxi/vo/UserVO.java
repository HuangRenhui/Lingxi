package com.lingxi.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户信息视图。
 */
@Data
@Schema(description = "用户信息")
public class UserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String username;

    private String nickname;

    private String role;
}
