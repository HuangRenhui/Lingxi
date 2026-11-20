-- 灵犀初始化数据（幂等，可重复执行）
-- 警告：本脚本包含演示管理员账号，仅用于本地开发环境。
-- 生产环境请勿执行本脚本，或执行后立即通过环境变量/管理接口重置管理员口令；口令不以明文写入仓库。

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

INSERT IGNORE INTO ai_skill (id, user_id, name, description, content, category, status, deleted)
VALUES (1, 1, '/github', '管理 GitHub issues、pull requests、仓库检索与 Actions 工作流。',
        '# GitHub\n\n当用户提到仓库、Issue、PR 或发布时使用本技能。', 'OFFICIAL', 1, 0),
       (2, 1, '/web-search', '联网检索并提取网页正文，返回带来源的摘要。',
        '# Web Search\n\n搜索公开网页，抓取正文并给出引用。', 'OFFICIAL', 1, 0),
       (3, 1, '/weekly-report', '整理一周数据并生成结构化周报。',
        '# Weekly Report\n\n汇总本周数据，输出要点、图表说明与结论。', 'COMMUNITY', 1, 0);

INSERT IGNORE INTO ai_mcp_server (id, user_id, name, description, server_type, endpoint, status, deleted)
VALUES (1, 1, 'GitHub', '管理仓库、Issue、Pull Request 与 Actions 工作流。',
        'HTTP', 'https://mcp.example.com/github', 1, 0),
       (2, 1, 'Notion', '读写 Notion 页面与数据库，同步项目进度。',
        'SSE', 'https://mcp.example.com/notion/sse', 0, 0);

INSERT IGNORE INTO artifact (id, user_id, title, type, tool_key, tool_name, prompt, url, poster_url,
                             size_bytes, favorite, trashed, status, deleted)
VALUES (1, 1, '蓝调雪后山地旅舍', 'IMAGE', 'nanobanana', 'Nano Banana',
        '覆雪木石旅舍坐落冷蓝山林，窗内暖光与入口灯笼，安静归宿感，电影感构图。',
        '/images/showcase-sc-nano-lodge.jpg', NULL, 2516582, 1, 0, 'APPROVED', 0),
       (2, 1, '墨河山径初醒', 'VIDEO', 'seedance', 'SeeDance',
        '黑色河流笔触向下流动，纸层山峦徐徐展开，朱红小径从雾中浮现，东方水墨动画。',
        '/media/demo-video-ink.mp4', '/images/showcase-sc-seedance-ink.jpg', 19503514, 0, 0, 'APPROVED', 0);

INSERT IGNORE INTO wallet_account (id, user_id, balance, frozen, total_recharge, total_spent, deleted)
VALUES (1, 1, 128.600000, 6.400000, 500.000000, 371.400000, 0);

INSERT IGNORE INTO wallet_transaction (id, user_id, type, amount, balance, note, created_at)
VALUES (1, 1, 'CONSUME', -2.100000, 128.600000, 'Kling · 视频生成', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
       (2, 1, 'CONSUME', -0.370000, 130.700000, 'SeeDream · 图片生成', DATE_SUB(NOW(), INTERVAL 4 HOUR)),
       (3, 1, 'RECHARGE', 30.000000, 139.470000, '支付宝充值 · ¥30.00', DATE_SUB(NOW(), INTERVAL 2 DAY)),
       (4, 1, 'REWARD', 20.000000, 109.470000, '邀请好友注册奖励', DATE_SUB(NOW(), INTERVAL 2 DAY)),
       (5, 1, 'REFUND', 4.200000, 90.030000, 'Sora 任务失败自动退回', DATE_SUB(NOW(), INTERVAL 5 DAY));

INSERT IGNORE INTO subscription_plan (id, plan_key, name, monthly_price, yearly_price, credits_per_month,
                                      tag, features, enabled, sort_order, deleted)
VALUES (1, 'free', '体验版', 0, 0, 200, NULL,
        '每月 200 积分\n全部对话应用\n基础图片生成\n任务最多保留 7 天\n社区支持', 1, 1, 0),
       (2, 'pro', '专业版', 68, 680, 3000, '最受欢迎',
        '每月 3000 积分\n全部对话与创作工具\n视频生成 4K 输出\n并行任务 5 个\n定时任务保留 90 天\n优先客服支持', 1, 2, 0),
       (3, 'ultra', '旗舰版', 168, 1680, 12000, NULL,
        '每月 12000 积分\n全部模型与最高档位\n并行任务 20 个\n任务与产物永久保留\n自定义 Skills 与连接器\n专属技术支持', 1, 3, 0);

INSERT IGNORE INTO user_subscription (id, user_id, plan_key, plan_name, cycle, status, active_flag, auto_renew,
                                      started_at, expires_at, credits_per_month, credits_used, deleted)
VALUES (1, 1, 'pro', '专业版', 'MONTHLY', 'ACTIVE', 1, 1,
        DATE_SUB(NOW(), INTERVAL 12 DAY), DATE_ADD(NOW(), INTERVAL 18 DAY), 3000, 1284, 0);

INSERT IGNORE INTO biz_order (id, order_no, user_id, description, type, amount, pay_method, status, paid_at, deleted)
VALUES (1, 'LX202611180006', 1, '专业版订阅 · 月付', 'SUBSCRIPTION', 68.000000, 'ALIPAY', 'PAID',
        DATE_SUB(NOW(), INTERVAL 1 DAY), 0),
       (2, 'LX202611170012', 1, '积分充值', 'RECHARGE', 200.000000, 'WECHAT', 'PAID',
        DATE_SUB(NOW(), INTERVAL 2 DAY), 0),
       (3, 'LX202611150003', 1, 'SeeDance · 视频生成', 'CONSUME', 8.400000, 'ALIPAY', 'PAID',
        DATE_SUB(NOW(), INTERVAL 4 DAY), 0),
       (4, 'LX202611130009', 1, '积分充值', 'RECHARGE', 50.000000, 'WECHAT', 'PENDING',
        NULL, 0);
