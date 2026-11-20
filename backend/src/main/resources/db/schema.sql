-- 灵犀初始化表结构（MySQL 8.0）
-- 说明：初始化阶段包含已落地模块（B1 用户、B7 任务、AI 应用、API Key、通知、设备、技能、连接器、B8 产物、B3 订单、B4 钱包、B5 订阅）所需表。
-- 其余 B2/B6/B9-B12 表随模块迭代补充，规划见 docs/数据库设计.md。

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS sys_user (
    id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username   VARCHAR(64)  NOT NULL COMMENT '用户名',
    password   VARCHAR(100) NOT NULL COMMENT 'BCrypt 密码',
    nickname   VARCHAR(64)           DEFAULT NULL COMMENT '昵称',
    role       VARCHAR(16)  NOT NULL DEFAULT 'USER' COMMENT '角色：ADMIN / USER',
    status     TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1 启用 / 0 禁用',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted    TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 未删除 / 1 已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_username (username)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '系统用户';

CREATE TABLE IF NOT EXISTS ai_task (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id          BIGINT       NOT NULL COMMENT '属主用户',
    name             VARCHAR(128) NOT NULL COMMENT '任务名称',
    prompt_template  TEXT         NOT NULL COMMENT '提示词模板',
    model            VARCHAR(64)  NOT NULL COMMENT '执行模型',
    run_location     VARCHAR(16)  NOT NULL DEFAULT 'CLOUD' COMMENT 'CLOUD / LOCAL',
    timezone         VARCHAR(64)  NOT NULL DEFAULT 'Asia/Shanghai' COMMENT 'IANA 时区',
    schedule_type    VARCHAR(16)  NOT NULL DEFAULT 'DAILY' COMMENT 'INTERVAL/HOURLY/DAILY/WEEKLY/CRON',
    cron_expr        VARCHAR(64)           DEFAULT NULL COMMENT 'cron 表达式',
    interval_minutes INT                   DEFAULT NULL COMMENT 'INTERVAL 间隔分钟数',
    execute_time     VARCHAR(5)            DEFAULT NULL COMMENT '执行时间 HH:mm',
    week_days        VARCHAR(16)           DEFAULT NULL COMMENT 'WEEKLY 星期集合 1-7',
    jitter_seconds   INT          NOT NULL DEFAULT 0 COMMENT '触发抖动 0-60 秒',
    max_rounds       INT          NOT NULL DEFAULT 50 COMMENT '单次运行最大轮次 1-50',
    status           VARCHAR(16)  NOT NULL DEFAULT 'ENABLED' COMMENT 'ENABLED / DISABLED',
    last_run_at      DATETIME              DEFAULT NULL COMMENT '最近一次运行时间',
    next_run_at      DATETIME              DEFAULT NULL COMMENT '下次计划运行时间',
    run_count        BIGINT       NOT NULL DEFAULT 0 COMMENT '累计运行次数',
    last_output      TEXT                  NULL COMMENT '上次运行结果摘要',
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted          TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    KEY idx_ai_task_user (user_id),
    KEY idx_ai_task_schedule (status, next_run_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '定时任务定义';

CREATE TABLE IF NOT EXISTS ai_task_run (
    id             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    task_id        BIGINT        NOT NULL COMMENT '任务 ID',
    user_id        BIGINT        NOT NULL COMMENT '属主用户',
    status         VARCHAR(16)   NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/RUNNING/SUCCESS/FAILED/ROUND_LIMIT/CANCELLED',
    channel        VARCHAR(16)   NOT NULL DEFAULT 'CLOUD_WORKER' COMMENT 'CLOUD_WORKER / LOCAL_RUNNER',
    rounds         INT           NOT NULL DEFAULT 0 COMMENT '实际消耗轮次',
    prompt_final   TEXT                   NULL COMMENT '渲染后的提示词',
    result_summary TEXT                   NULL COMMENT '结果摘要',
    error          VARCHAR(1024)          NULL COMMENT '失败原因',
    started_at     DATETIME               NULL COMMENT '开始时间',
    finished_at    DATETIME               NULL COMMENT '结束时间',
    created_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_ai_task_run_task (task_id, started_at),
    KEY idx_ai_task_run_user (user_id, status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '任务运行实例';

CREATE TABLE IF NOT EXISTS ai_application (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id     BIGINT       NOT NULL COMMENT '属主用户',
    name        VARCHAR(64)  NOT NULL COMMENT '应用名称',
    description VARCHAR(255)          DEFAULT NULL COMMENT '应用描述',
    app_type    VARCHAR(16)  NOT NULL DEFAULT 'GENERAL' COMMENT 'GENERAL / IMAGE / VIDEO / AUDIO',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1 启用 / 0 禁用',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    KEY idx_ai_application_user (user_id, deleted)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = 'AI 应用';

CREATE TABLE IF NOT EXISTS api_key (
    id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id    BIGINT       NOT NULL COMMENT '属主用户',
    name       VARCHAR(64)  NOT NULL COMMENT '密钥名称',
    key_prefix VARCHAR(16)  NOT NULL COMMENT '展示前缀',
    key_hash   CHAR(64)     NOT NULL COMMENT 'SHA-256 摘要',
    status     TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1 启用 / 0 禁用',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted    TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_api_key_hash (key_hash),
    KEY idx_api_key_user (user_id, deleted)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = 'API Key';

CREATE TABLE IF NOT EXISTS notification (
    id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id    BIGINT       NOT NULL COMMENT '接收用户',
    title      VARCHAR(128) NOT NULL COMMENT '标题',
    content    VARCHAR(500)          DEFAULT NULL COMMENT '正文',
    type       VARCHAR(16)  NOT NULL DEFAULT 'SYSTEM' COMMENT 'SYSTEM / TASK / BILLING',
    read_flag  TINYINT      NOT NULL DEFAULT 0 COMMENT '已读：1 是 / 0 否',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted    TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    KEY idx_notification_user (user_id, read_flag, deleted)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '站内通知';

CREATE TABLE IF NOT EXISTS user_device (
    id             BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id        BIGINT      NOT NULL COMMENT '属主用户',
    name           VARCHAR(64) NOT NULL COMMENT '设备名称',
    os             VARCHAR(32)          DEFAULT NULL COMMENT '操作系统',
    browser        VARCHAR(32)          DEFAULT NULL COMMENT '浏览器',
    ip             VARCHAR(45)          DEFAULT NULL COMMENT '登录 IP',
    location       VARCHAR(64)          DEFAULT NULL COMMENT '登录地',
    current_flag   TINYINT     NOT NULL DEFAULT 0 COMMENT '当前设备：1 是 / 0 否',
    last_active_at DATETIME             DEFAULT NULL COMMENT '最近活跃时间',
    created_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted        TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    KEY idx_user_device_user (user_id, deleted)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '登录设备';

CREATE TABLE IF NOT EXISTS ai_skill (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id     BIGINT       NOT NULL COMMENT '属主用户',
    name        VARCHAR(64)  NOT NULL COMMENT '技能名称，以 / 开头',
    description VARCHAR(255)          DEFAULT NULL COMMENT '技能简介',
    content     TEXT         NOT NULL COMMENT '技能内容（Markdown）',
    category    VARCHAR(16)  NOT NULL DEFAULT 'COMMUNITY' COMMENT 'OFFICIAL / COMMUNITY',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1 启用 / 0 停用',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    KEY idx_ai_skill_user (user_id, deleted)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '技能定义';

CREATE TABLE IF NOT EXISTS ai_mcp_server (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id     BIGINT       NOT NULL COMMENT '属主用户',
    name        VARCHAR(64)  NOT NULL COMMENT '连接器名称',
    description VARCHAR(255)          DEFAULT NULL COMMENT '连接器简介',
    server_type VARCHAR(16)  NOT NULL DEFAULT 'HTTP' COMMENT 'HTTP / SSE / STDIO',
    endpoint    VARCHAR(512)          DEFAULT NULL COMMENT '服务地址或启动命令',
    status      TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：1 已连接 / 0 未连接',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    KEY idx_ai_mcp_server_user (user_id, deleted)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '连接器（MCP Server）';

CREATE TABLE IF NOT EXISTS artifact (
    id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id    BIGINT       NOT NULL COMMENT '属主用户',
    title      VARCHAR(128) NOT NULL COMMENT '作品标题',
    type       VARCHAR(16)  NOT NULL COMMENT 'IMAGE / VIDEO / AUDIO',
    tool_key   VARCHAR(64)           DEFAULT NULL COMMENT '创作工具标识',
    tool_name  VARCHAR(64)           DEFAULT NULL COMMENT '创作工具名称',
    prompt     TEXT                  NULL COMMENT '生成提示词',
    url        VARCHAR(512) NOT NULL COMMENT '产物地址或对象存储 Key',
    poster_url VARCHAR(512)          DEFAULT NULL COMMENT '视频封面地址',
    size_bytes BIGINT       NOT NULL DEFAULT 0 COMMENT '文件大小（字节）',
    favorite   TINYINT      NOT NULL DEFAULT 0 COMMENT '收藏：1 是 / 0 否',
    trashed    TINYINT      NOT NULL DEFAULT 0 COMMENT '回收站：1 是 / 0 否',
    status     VARCHAR(16)  NOT NULL DEFAULT 'APPROVED' COMMENT 'PENDING / APPROVED / REJECTED',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted    TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    KEY idx_artifact_user (user_id, deleted, trashed),
    KEY idx_artifact_user_created (user_id, created_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '创作产物';

CREATE TABLE IF NOT EXISTS biz_order (
    id          BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    order_no    VARCHAR(32)    NOT NULL COMMENT '订单号',
    user_id     BIGINT         NOT NULL COMMENT '下单用户',
    description VARCHAR(128)   NOT NULL COMMENT '订单描述',
    type        VARCHAR(16)    NOT NULL DEFAULT 'CONSUME' COMMENT 'RECHARGE/SUBSCRIPTION/CONSUME/REDEEM',
    amount      DECIMAL(18, 6) NOT NULL DEFAULT 0 COMMENT '订单金额（积分）',
    pay_method  VARCHAR(16)             DEFAULT NULL COMMENT 'ALIPAY/WECHAT/REDEEM/CREDITS',
    status      VARCHAR(16)    NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/PAID/CANCELLED/REFUNDING/REFUNDED',
    paid_at     DATETIME                DEFAULT NULL COMMENT '支付完成时间',
    created_at  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_biz_order_no (order_no),
    KEY idx_biz_order_user (user_id, deleted, created_at),
    -- 资金类表必须能追溯到真实用户，避免脏数据产生孤儿订单
    CONSTRAINT fk_biz_order_user FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '业务订单';

CREATE TABLE IF NOT EXISTS wallet_account (
    id             BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id        BIGINT         NOT NULL COMMENT '属主用户',
    balance        DECIMAL(18, 6) NOT NULL DEFAULT 0 COMMENT '可用余额（积分）',
    frozen         DECIMAL(18, 6) NOT NULL DEFAULT 0 COMMENT '冻结金额（积分）',
    total_recharge DECIMAL(18, 6) NOT NULL DEFAULT 0 COMMENT '累计充值（积分）',
    total_spent    DECIMAL(18, 6) NOT NULL DEFAULT 0 COMMENT '累计消费（积分）',
    created_at     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted        TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_wallet_account_user (user_id),
    CONSTRAINT fk_wallet_account_user FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '钱包账户';

CREATE TABLE IF NOT EXISTS wallet_transaction (
    id         BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id    BIGINT         NOT NULL COMMENT '属主用户',
    type       VARCHAR(16)    NOT NULL COMMENT 'RECHARGE/CONSUME/REWARD/REFUND/FREEZE/UNFREEZE',
    amount     DECIMAL(18, 6) NOT NULL COMMENT '变动值，正增负减',
    balance    DECIMAL(18, 6) NOT NULL COMMENT '变动后余额',
    note       VARCHAR(255)            DEFAULT NULL COMMENT '备注',
    biz_no     VARCHAR(64)             DEFAULT NULL COMMENT '业务幂等键，如 ORDER-<订单号>',
    created_at DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    -- 幂等键唯一：支付回调/客户端重试只会入账一次（NULL 不受唯一约束限制）
    UNIQUE KEY uk_wallet_transaction_biz_no (biz_no),
    KEY idx_wallet_transaction_user (user_id, created_at),
    CONSTRAINT fk_wallet_transaction_user FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '钱包流水（只追加）';

CREATE TABLE IF NOT EXISTS subscription_plan (
    id                BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    plan_key          VARCHAR(32)    NOT NULL COMMENT '套餐标识',
    name              VARCHAR(64)    NOT NULL COMMENT '套餐名称',
    monthly_price     DECIMAL(18, 6) NOT NULL DEFAULT 0 COMMENT '月付价格（积分）',
    yearly_price      DECIMAL(18, 6) NOT NULL DEFAULT 0 COMMENT '年付价格（积分）',
    credits_per_month INT            NOT NULL DEFAULT 0 COMMENT '每周期积分',
    tag               VARCHAR(32)             DEFAULT NULL COMMENT '角标文案',
    features          TEXT                    NULL COMMENT '权益说明，按换行分隔',
    enabled           TINYINT        NOT NULL DEFAULT 1 COMMENT '上架：1 是 / 0 否',
    sort_order        INT            NOT NULL DEFAULT 0 COMMENT '排序值',
    created_at        DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at        DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted           TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_subscription_plan_key (plan_key)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '订阅套餐';

CREATE TABLE IF NOT EXISTS user_subscription (
    id                BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id           BIGINT      NOT NULL COMMENT '订阅用户',
    plan_key          VARCHAR(32) NOT NULL COMMENT '套餐标识快照',
    plan_name         VARCHAR(64) NOT NULL COMMENT '套餐名称快照',
    cycle             VARCHAR(16) NOT NULL DEFAULT 'MONTHLY' COMMENT 'MONTHLY / YEARLY',
    status            VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE / EXPIRED / CANCELLED',
    -- 生效标记：ACTIVE 时为 1，失效置 NULL；MySQL 唯一索引允许多个 NULL，
    -- 因此 (user_id, active_flag) 唯一键能在数据库层保证「每用户最多一条生效订阅」
    active_flag       TINYINT              DEFAULT NULL COMMENT '生效标记：1 生效 / NULL 失效',
    auto_renew        TINYINT     NOT NULL DEFAULT 1 COMMENT '自动续费：1 是 / 0 否',
    started_at        DATETIME    NOT NULL COMMENT '本周期开始时间',
    expires_at        DATETIME    NOT NULL COMMENT '本周期结束时间',
    credits_per_month INT         NOT NULL DEFAULT 0 COMMENT '周期内可用积分快照',
    credits_used      INT         NOT NULL DEFAULT 0 COMMENT '周期内已用积分',
    created_at        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted           TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_subscription_active (user_id, active_flag),
    KEY idx_user_subscription_user (user_id, status, deleted),
    CONSTRAINT fk_user_subscription_user FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '用户订阅与历史';
