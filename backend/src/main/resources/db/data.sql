-- 灵犀初始化数据（幂等，可重复执行）
-- 默认账号：admin / Admin@123（仅用于本地与演示环境，上线前必须修改）

INSERT IGNORE INTO sys_user (id, username, password, nickname, role, status, deleted)
VALUES (1, 'admin', '$2b$10$TyGTQFtbQpWK9kseDlLHC.YyE/kMhpNgdvTJ7b5YsqRL.5Rnw09NC', '演示管理员', 'ADMIN', 1, 0);

INSERT IGNORE INTO ai_task (id, user_id, name, prompt_template, model, run_location, timezone,
                            schedule_type, execute_time, week_days, jitter_seconds, max_rounds,
                            status, run_count, deleted)
VALUES (1, 1, '每日 AI 资讯汇总',
        '总结 {{date}} 的 AI 行业资讯，这是第 {{run_count}} 次运行。上次结果摘要：{{last_output}}',
        'gpt-4o', 'CLOUD', 'Asia/Shanghai', 'DAILY', '09:00', NULL, 30, 50, 'ENABLED', 0, 0),
       (2, 1, '每周竞品观察',
        '整理本周竞品动态并输出要点，重点关注产品更新与定价变化。',
        'claude-3-5-sonnet', 'LOCAL', 'Asia/Shanghai', 'WEEKLY', '18:00', '5', 0, 30, 'DISABLED', 0, 0);

INSERT IGNORE INTO ai_application (id, user_id, name, description, app_type, status, deleted)
VALUES (1, 1, '商品海报工厂', '上传商品图，批量生成营销海报与详情页配图。', 'IMAGE', 1, 0),
       (2, 1, '短视频脚本助手', '根据主题生成分镜、口播文案与发布标题。', 'VIDEO', 1, 0);

INSERT IGNORE INTO notification (id, user_id, title, content, type, read_flag, deleted)
VALUES (1, 1, '每日资讯任务已完成', '「每日 AI 资讯汇总」第 12 次运行完成，可在任务中心查看结果。', 'TASK', 0, 0),
       (2, 1, '本月积分即将用完', '当前额度剩余 18%，可在钱包页面充值或升级套餐。', 'BILLING', 0, 0),
       (3, 1, '新设备登录提醒', '检测到 Chrome / macOS 设备登录，如非本人操作请及时修改密码。', 'SYSTEM', 1, 0);

INSERT IGNORE INTO user_device (id, user_id, name, os, browser, ip, location, current_flag, last_active_at, deleted)
VALUES (1, 1, '工作台 MacBook', 'macOS 15', 'Chrome 131', '127.0.0.1', '本地', 1, NOW(), 0),
       (2, 1, '备用 Windows', 'Windows 11', 'Edge 131', '192.168.1.20', '局域网', 0, NOW(), 0);
