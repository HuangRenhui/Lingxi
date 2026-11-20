package com.lingxi.common;

/**
 * 业务状态码。
 *
 * <p>约定：业务结果以 code 为准，前端按 code 分支处理；HTTP 状态在认证（401）、
 * 授权（403）、路由（404/405）与系统错误（5xx）上与 code 保持一致，
 * 以便网关与监控无需解析响应体即可识别故障类型。</p>
 */
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    PARAM_ERROR(400, "参数校验失败"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无操作权限"),
    FAIL(500, "操作失败"),
    INTERNAL_ERROR(5000, "系统内部错误"),

    /* 请求解析：4001-4099 */
    REQUEST_BODY_ERROR(4001, "请求体格式错误"),
    PARAM_TYPE_MISMATCH(4002, "请求参数类型不匹配"),

    /* 路由与数据约束：4101-4199 */
    METHOD_NOT_ALLOWED(4101, "请求方法不支持"),
    API_NOT_FOUND(4102, "接口不存在"),
    DATA_CONFLICT(4103, "数据已存在或违反唯一约束"),

    /* 用户与认证：2001-2099 */
    INVALID_CREDENTIALS(2001, "用户名或密码错误"),
    ACCOUNT_DISABLED(2002, "账号已被禁用"),
    USER_NOT_FOUND(2003, "用户不存在"),
    LOGIN_LOCKED(2004, "登录失败次数过多，请 5 分钟后再试"),

    /* 无人值守 Agent：7001-7099 */
    TASK_NOT_FOUND(7001, "定时任务不存在"),
    TASK_NOT_OWNED(7002, "无权操作该任务"),
    TASK_STATUS_INVALID(7003, "任务状态仅支持 ENABLED / DISABLED"),
    TASK_SCHEDULE_INVALID(7004, "任务调度配置不合法"),
    TASK_CONCURRENT_MODIFIED(7005, "任务已被其他操作修改，请刷新后重试"),

    /* AI 应用：8001-8099 */
    APPLICATION_NOT_FOUND(8001, "应用不存在"),
    APPLICATION_NOT_OWNED(8002, "无权操作该应用"),

    /* API Key：8101-8199 */
    API_KEY_NOT_FOUND(8101, "密钥不存在"),
    API_KEY_NOT_OWNED(8102, "无权操作该密钥"),

    /* 通知：8201-8299 */
    NOTIFICATION_NOT_FOUND(8201, "通知不存在"),
    NOTIFICATION_NOT_OWNED(8202, "无权操作该通知"),

    /* 设备：8301-8399 */
    DEVICE_NOT_FOUND(8301, "设备不存在"),
    DEVICE_NOT_OWNED(8302, "无权操作该设备"),

    /* 技能：8401-8499 */
    SKILL_NOT_FOUND(8401, "技能不存在"),
    SKILL_NOT_OWNED(8402, "无权操作该技能"),

    /* 连接器：8501-8599 */
    CONNECTOR_NOT_FOUND(8501, "连接器不存在"),
    CONNECTOR_NOT_OWNED(8502, "无权操作该连接器"),

    /* 管理端用户：9001-9099 */
    USERNAME_TAKEN(9001, "用户名已存在"),
    CANNOT_DELETE_SELF(9002, "不能删除当前登录账号"),
    ADMIN_REQUIRED(9003, "需要管理员权限"),
    SELF_OPERATION_FORBIDDEN(9004, "不能停用或降级当前登录账号");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
