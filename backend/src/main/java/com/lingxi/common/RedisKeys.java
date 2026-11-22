package com.lingxi.common;

/**
 * Redis Key 统一定义，禁止在业务代码中散落字符串拼接。
 */
public final class RedisKeys {

    private static final String PREFIX = "lingxi:";

    /** 令牌白名单（预留：B1 退出登录/强制下线）。 */
    public static final String AUTH_TOKEN = PREFIX + "auth:token:";

    /** 登录失败计数（key 形如 auth:login:fail:{username}）。 */
    public static final String LOGIN_FAIL = PREFIX + "auth:login:fail:";

    /** 已注销的令牌黑名单，value 无意义，依赖 TTL 自动过期。 */
    public static final String TOKEN_BLACKLIST = PREFIX + "auth:token:blacklist:";

    /** 用户令牌失效时间点（秒）：改密/禁用/删除后，早于该时间签发的令牌一律失效。 */
    public static final String USER_TOKEN_INVALID_BEFORE = PREFIX + "auth:user:token-invalid-before:";

    /** 任务运行分布式锁（预留：B7 防止同一任务并发运行）。 */
    public static final String TASK_LOCK = PREFIX + "agent:task:lock:";

    /** 任务运行期 transcript（预留：B7 跨轮上下文）。 */
    public static final String TASK_TRANSCRIPT = PREFIX + "agent:transcript:";

    private RedisKeys() {
    }

    public static String authToken(Long userId) {
        return AUTH_TOKEN + userId;
    }

    public static String loginFail(String username) {
        return LOGIN_FAIL + username;
    }

    public static String tokenBlacklist(String token) {
        return TOKEN_BLACKLIST + token;
    }

    public static String userTokenInvalidBefore(Long userId) {
        return USER_TOKEN_INVALID_BEFORE + userId;
    }

    public static String taskLock(Long taskId) {
        return TASK_LOCK + taskId;
    }

    public static String taskTranscript(Long taskRunId) {
        return TASK_TRANSCRIPT + taskRunId;
    }
}
