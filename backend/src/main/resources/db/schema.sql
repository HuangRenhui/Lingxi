-- 灵犀初始化表结构（MySQL 8.0）
-- 说明：初始化阶段包含已落地模块（B1 用户、B7 任务、AI 应用、API Key）所需表。
-- 其余 B2-B12 表随模块迭代补充，规划见 docs/数据库设计.md。

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
