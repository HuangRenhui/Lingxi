-- 灵犀数据库升级脚本（MySQL 8.0）
-- 适用场景：已有数据库，从旧版本升级到当前版本。
--
-- 执行顺序（必须）：
--   1) 先执行 schema.sql：用 CREATE TABLE IF NOT EXISTS 补齐新增表；
--   2) 再执行本脚本：修改已有表的默认值、补唯一索引、补 user_coupon 表。
--
-- 本脚本可重复执行；不会删除业务数据。

SET NAMES utf8mb4;

-- ---------------------------------------------------------------------------
-- 0. 升级前检查：billing_rule 如果已有重复的 (target, model, deleted)，
--    唯一索引会创建失败。先查询，人工确认后再决定保留哪一条。
-- ---------------------------------------------------------------------------
SELECT target, model, deleted, COUNT(*) AS duplicate_count, MIN(id) AS min_id, MAX(id) AS max_id
FROM billing_rule
GROUP BY target, model, deleted
HAVING COUNT(*) > 1;

-- ---------------------------------------------------------------------------
-- 1. 作品默认状态改为待审核
--    旧版本创建作品时默认 APPROVED，升级后新作品必须经过审核。
-- ---------------------------------------------------------------------------
ALTER TABLE artifact
    MODIFY COLUMN status VARCHAR(16) NOT NULL DEFAULT 'PENDING'
    COMMENT 'PENDING / APPROVED / REJECTED';

-- ---------------------------------------------------------------------------
-- 2. 计费规则补唯一索引（旧库可能只有普通索引）
-- ---------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS lingxi_add_index_if_missing;

DELIMITER $$
CREATE PROCEDURE lingxi_add_index_if_missing(
    IN p_table VARCHAR(64),
    IN p_index VARCHAR(64),
    IN p_ddl   TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = p_table
          AND index_name = p_index
    ) THEN
        SET @ddl = p_ddl;
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

CALL lingxi_add_index_if_missing(
    'billing_rule',
    'uk_billing_rule_target',
    'ALTER TABLE billing_rule ADD UNIQUE KEY uk_billing_rule_target (target, model, deleted)'
);

DROP PROCEDURE IF EXISTS lingxi_add_index_if_missing;

-- ---------------------------------------------------------------------------
-- 3. 用户优惠券领取表
--    同一用户同一张券只能领取一次；核销时用状态条件更新防并发重复使用。
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_coupon (
    id         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id    BIGINT      NOT NULL COMMENT '领取用户',
    coupon_id  BIGINT      NOT NULL COMMENT '优惠券模板 ID',
    status     VARCHAR(16) NOT NULL DEFAULT 'UNUSED' COMMENT 'UNUSED / USED / EXPIRED',
    claimed_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
    used_at    DATETIME             DEFAULT NULL COMMENT '核销时间',
    order_no   VARCHAR(32)          DEFAULT NULL COMMENT '核销订单号',
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_coupon_user_coupon (user_id, coupon_id),
    KEY idx_user_coupon_user_status (user_id, status),
    KEY idx_user_coupon_coupon (coupon_id, status),
    CONSTRAINT fk_user_coupon_user FOREIGN KEY (user_id) REFERENCES sys_user (id),
    CONSTRAINT fk_user_coupon_coupon FOREIGN KEY (coupon_id) REFERENCES marketing_coupon (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '用户优惠券领取与核销';

-- ---------------------------------------------------------------------------
-- 4. 已有 MySQL 数据卷补应用账号（新卷由 docker-compose 自动创建）
--    把下面的密码替换为 .env 中的 MYSQL_PASSWORD 后再执行。
-- ---------------------------------------------------------------------------
-- CREATE USER IF NOT EXISTS 'lingxi'@'%' IDENTIFIED BY '替换为 MYSQL_PASSWORD';
-- GRANT ALL PRIVILEGES ON lingxi.* TO 'lingxi'@'%';
-- FLUSH PRIVILEGES;

-- ---------------------------------------------------------------------------
-- 4.1 如果旧库仍在使用 data.sql 的演示管理员，轮换密码：
--     先用 BCryptPasswordEncoder.encode("新口令") 生成哈希，再取消注释执行。
-- ---------------------------------------------------------------------------
-- UPDATE sys_user SET password = '替换为新的 BCrypt 哈希' WHERE username = 'admin';

-- ---------------------------------------------------------------------------
-- 5. 升级后校验
-- ---------------------------------------------------------------------------
SELECT table_name
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN ('user_coupon', 'marketing_coupon', 'billing_rule', 'artifact');
