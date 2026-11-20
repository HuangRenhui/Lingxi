# 灵犀 Lingxi

[English](./README.en.md) | 简体中文

> 可运行、可部署的 AI 创作平台：前端 Vue 3 + Element Plus（用户端与管理后台全量页面，内置演示数据可独立运行），后端 Spring Boot + B1-B12 业务模块；当前后端已落地登录、令牌刷新/注销、B7 任务定义与 AI 应用 / API Key / 通知 / 设备 / 技能 / 连接器 / 产物 / 订单 / 钱包 / 订阅等基础管理接口，**B7 无人值守 Agent 编排引擎是后续里程碑的核心方向**。

![Java](https://img.shields.io/badge/Java-8-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-brightgreen)
![Vue](https://img.shields.io/badge/Vue-3.5-42b883)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479a1)
![Redis](https://img.shields.io/badge/Redis-7.x-dc382d)
![License](https://img.shields.io/badge/License-TBD-lightgrey)

## 项目简介

灵犀提供对话、图片、视频、音乐四类创作入口，内置 ChatGPT、Claude、Gemini、Grok、DeepSeek、Kimi、通义千问、豆包 8 个对话应用与 22 个创作工具，并配套定时任务、控制台、账号体系与管理后台。项目按开箱即用组织，既可本地开发，也可直接构建发布：

- **前端**：Vue 3 + Element Plus + Vite + Vue Router + Vuex，深色主题，按「首页 + 创作区 + 控制台 + 管理后台」组织，页面由统一配置注册表驱动；
- **后端**：Spring Boot 2.7.18 + MyBatis-Plus + Spring Security + JWT，按 B1-B12 模块边界逐步建设；
- **基础设施**：MySQL 8 + Redis 7，Docker Compose 一键启动；
- **文档**：中文 / English README，以及架构、功能、接口、数据库、开发指南与页面补充 Spec。

前端内置 Mock 数据层，本地开发无需后端即可完整体验全部页面；生产环境通过 Docker Compose 启动前端、后端、MySQL 与 Redis 全套服务。

## 当前完成度

| 能力 | 状态 |
| --- | --- |
| 项目骨架（前后端 + Docker + 文档） | ✅ |
| 登录 / JWT 鉴权 / 令牌刷新与注销 / 当前用户 | ✅ |
| 定时任务列表、新建、编辑、启停、删除 | ✅ |
| AI 应用 / API Key / 通知 / 设备 / 技能 / 连接器管理 | ✅ 后端接口 |
| 产物管理（分页筛选、收藏/回收站状态） | ✅ 后端接口 |
| 订单 / 钱包 / 订阅管理 | ✅ 后端接口 |
| 用户端页面（首页、8 个对话应用、22 个创作工具、任务、作品、控制台 10 页、账号 3 页） | ✅ 前端完成（演示数据） |
| 管理后台（19 个页面、管理员登录、隐藏入口、分组导航） | ✅ 前端完成（演示数据） |
| 计费规则成本核算（输入/输出/缓存分档、毛利预警） | ✅ 前端完成（演示数据） |
| 列表分页、表格固定列与深色主题适配 | ✅ |
| 模型网关（B2） | ⬜ 规划 |
| 任务调度器与多轮编排（B7 后半） | ⬜ 规划 |
| 计费规则与支付回调（B3 计费 / B6） | ⬜ 后端规划，前端演示页已完成 |
| 用量 / 后台 / 报表 / 本机通道（B9-B12） | ⬜ 后端规划，前端演示页已完成 |

> 登录、定时任务以及上述基础管理能力已提供后端接口；其余业务页面当前仍使用 `frontend/src/mock` 下的演示数据，数据保存在浏览器 `localStorage`，不依赖后端。

## 核心设计

- **模块边界**：B1 用户、B2 模型网关、B3 计费订单、B4 钱包、B5 订阅、B6 支付充值、B7 无人值守 Agent、B8 产物、B9 用量、B10 后台、B11 报表、B12 本机浏览器通道。
- **B7 双通道**：`CLOUD_WORKER` 云端执行（关掉设备也继续）、`LOCAL_RUNNER` 本机执行（依赖本机登录态/内网资源）。
- **B7 状态机**：`PENDING → RUNNING → SUCCESS / FAILED / ROUND_LIMIT / CANCELLED`。
- **统一响应**：HTTP 统一 200，业务结果由 `code` 表达；用户端前端拦截器统一处理 401（自动刷新并重放一次）与错误提示。
- **计费前置**：后续所有模型调用只经过 B2 网关，便于计量、计费与渠道切换。
- **计费口径**：对话类区分输入 / 输出 / 缓存输入三档单价，上游成本与售价同单位录入并实时折算毛利率，低于 15% 预警、为负即为亏损。
- **成本对账**：按 `usage_record` 汇总成本与收费，月度与上游账单核对；流式请求必须取回 usage，用户中断也按已产生用量扣费。
- **双端前端**：用户端与管理后台共用一套 Vue 工程；管理后台使用可配置的隐藏路径（默认 `/lingxi-console-7c3f`），独立会话，普通用户不会从界面进入。
- **演示数据层**：`frontend/src/mock` 通过 Axios 自定义 adapter 接管请求，未启动后端时全部页面可用；数据按模块写入 `localStorage`，可在「安全设置」中一键清空。
- **列表统一分页**：所有表格默认每页 10 条，使用通用组件 `TablePager`；筛选条件变化时回到第一页，数据变少时自动回退最后一页。

## 技术栈

### 后端

| 组件 | 版本 / 选型 |
| --- | --- |
| JDK | 1.8+（target 1.8；构建需 JDK，已验证 JDK 21） |
| Spring Boot | 2.7.18 |
| 安全 | Spring Security + JWT（jjwt 0.11.5）+ BCrypt |
| 持久层 | MyBatis-Plus 3.5.7 + MySQL 8 |
| 缓存 | Redis + Spring Data Redis |
| 接口文档 | Knife4j / OpenAPI 3 |
| 构建 | Maven |

### 前端

| 组件 | 版本 / 选型 |
| --- | --- |
| 框架 | Vue 3.5 |
| UI | Element Plus 2.9 |
| 路由 / 状态 | Vue Router 4 / Vuex 4 |
| 请求 | Axios |
| 构建 | Vite 5 |

## 系统架构

```text
浏览器（Vue 3 + Element Plus）
        │  /api（JSON + JWT）
        ▼
Spring Boot 2.7
  controller → service → mapper → MySQL
  security / common / config / dto / vo / entity
        │
        ├── MySQL 8：用户、任务、订单、钱包…
        ├── Redis：登录失败计数、令牌黑名单、任务锁与运行期上下文
        └── B7 编排引擎（规划）：调度 → 多轮 Loop → Skills/MCP → 模型网关
```

## 快速开始

### 方式一：Docker Compose

```powershell
Copy-Item .env.example .env
# 修改 .env：MYSQL_ROOT_PASSWORD / REDIS_PASSWORD / JWT_SECRET（至少 32 字节）
docker compose up -d --build
```

| 服务 | 地址 |
| --- | --- |
| 前端 | <http://localhost> |
| 后端 API | <http://localhost:8080/api> |
| 健康检查 | <http://localhost:8080/api/health> |
| Knife4j | <http://localhost:8080/api/doc.html> |
| MySQL | localhost:3306 |
| Redis | localhost:6379 |

MySQL 首次启动会自动执行 `schema.sql` 与 `data.sql`。

`JWT_SECRET` 为必填项且不少于 32 字节，非开发环境未配置会启动失败（开发默认密钥仅存在于 `application-dev.yml`）。跨域白名单由 `CORS_ALLOWED_ORIGINS` 控制，逗号分隔，默认 `http://localhost:5173,http://127.0.0.1:5173`，禁止使用 `*` 与凭据组合。`.env.example` 与 `docker-compose.yml` 当前共同使用 `MYSQL_ROOT_PASSWORD`、`REDIS_PASSWORD`、`JWT_SECRET` 三个必填项；`CORS_ALLOWED_ORIGINS` 为可选后端变量，如需额外来源，在 Compose 的 backend 环境变量中追加。

### 方式二：本地开发

1. 创建数据库 `lingxi` 并导入 SQL：

```powershell
mysql -uroot -p lingxi -e "source backend/src/main/resources/db/schema.sql"
mysql -uroot -p lingxi -e "source backend/src/main/resources/db/data.sql"
```

2. 启动 Redis（可选但推荐）：

```powershell
docker run -d --name lingxi-redis -p 6379:6379 redis:7-alpine
```

3. 启动后端（`JAVA_HOME` 必须指向 JDK 而不是 JRE；已验证 JDK 21 可构建）：

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\latest\jdk-21"
mvn -f backend/pom.xml spring-boot:run
```

非 `dev` profile 必须设置 `JWT_SECRET`（至少 32 字节），否则启动失败；开发环境的默认密钥只在 `application-dev.yml` 中提供。需要直连后端调试时，按需设置 `CORS_ALLOWED_ORIGINS`（逗号分隔，默认本地 5173 两个来源）。

4. 启动前端：

```powershell
cd frontend
npm install
npm run dev
```

访问 <http://localhost:5173>，开发环境默认启用前端 Mock（自动跳过登录与后端校验），直接进入首页；需要连接真实后端时设置 `VITE_MOCK=false` 后重启。

管理后台入口为 <http://localhost:5173/lingxi-console-7c3f/login>，可通过 `VITE_ADMIN_PATH` 修改隐藏路径。演示管理员口令为默认种子数据 `db/data.sql` 中记录的演示账号（仅用于本地演示，**生产环境必须在首次部署后立即重置**，否则等同于公开凭据）；管理后台为纯前端演示会话，上线前需接入后端鉴权。

更完整的配置与排错说明见 [开发指南](./docs/开发指南.md)。

### 方式三：构建前端产物发布

```powershell
cd frontend
npm ci
npm run build
```

构建结果位于 `frontend/dist`，可直接部署到静态托管或 Nginx。仓库内的 `frontend/nginx.conf` 已包含 SPA 路由回退与 `/api` 反向代理配置；前端默认请求 `/api`，如需直连独立后端，可在构建时设置 `VITE_API_BASE_URL`。若要发布完整业务能力，请同时部署后端与 MySQL / Redis；若要发布纯前端演示版，可在构建时设置 `VITE_MOCK=true`，无需后端即可完整体验，数据保存在浏览器本地。

## 目录结构

```text
lingxi/
├── backend/                        # Spring Boot 后端
│   ├── src/main/java/com/lingxi/
│   │   ├── common/                 # Result / 异常 / Redis Key
│   │   ├── config/                 # Security / MyBatis-Plus / Redis / OpenAPI
│   │   ├── controller/             # 健康检查、认证、任务接口
│   │   ├── dto/                    # 入参对象
│   │   ├── entity/                 # 数据库实体
│   │   ├── mapper/                 # MyBatis-Plus Mapper
│   │   ├── security/               # JWT 过滤器与登录上下文
│   │   ├── service/                # 业务接口与实现
│   │   └── vo/                     # 出参对象
│   ├── src/main/resources/
│   │   ├── db/                     # schema.sql / data.sql
│   │   ├── application.yml         # 通用配置
│   │   ├── application-dev.yml     # 本地配置
│   │   ├── application-prod.yml    # 生产配置
│   │   └── logback-spring.xml
│   ├── Dockerfile
│   └── pom.xml
├── frontend/                       # Vue 3 前端
│   ├── src/
│   │   ├── api/                    # 接口封装
│   │   ├── assets/                 # Logo 与模型/连接器图标
│   │   ├── components/             # 通用组件（PageHeader、StatCard、TablePager 等）
│   │   ├── config/                 # tools.js 内容注册表；admin.js 管理后台路径
│   │   ├── layout/                 # index.vue 用户端布局；admin.vue 管理后台布局
│   │   ├── mock/                   # 演示数据层（账号、平台、管理后台、优惠券）
│   │   ├── router/                 # 路由与登录守卫（工具路由由配置生成）
│   │   ├── store/                  # Vuex 用户态
│   │   ├── styles/                 # 全局样式、主题变量与管理后台样式
│   │   ├── utils/                  # axios / 鉴权 / 模型目录 / CSV 导出
│   │   └── views/                  # 首页、对话、创作、任务、作品、控制台、账号、admin/
│   ├── Dockerfile
│   ├── nginx.conf
│   ├── vite.config.js
│   └── package.json
├── docs/                           # 架构、功能、接口、数据库、开发文档、specs
├── docker-compose.yml
├── .env.example
├── README.md
└── README.en.md
```

## 前端页面与路由

| 路由 | 页面 | 说明 |
| --- | --- | --- |
| `/` → `/home` | 首页 | 轮播推荐、快速开始分类、精选作品、客服弹窗 |
| `/home` | 首页 | 同上，登录后的默认入口 |
| `/chat/:app` | 多应用对话 | `chatgpt`、`claude`、`gemini`、`grok`、`deepseek`、`kimi`、`qwen`、`doubao`，会话按应用隔离存储 |
| 22 条工具路由 | 通用创作页 | `/maestro`、`/openai-image`、`/nanobanana`、`/seedream`、`/flux`、`/ideogram`、`/midjourney`、`/qrart`、`/seedance`、`/kling`、`/veo`、`/grok-video`、`/pika`、`/minimax`、`/sora`、`/luma`、`/runway`、`/wan`、`/digital-human`、`/suno`、`/producer`、`/udio` |
| `/workspace` → `/chat/chatgpt` | 重定向 | 兼容旧对话入口 |
| `/image` → `/nanobanana`、`/video` → `/seedance` | 重定向 | 兼容旧创作入口 |
| `/tasks` | 定时任务 | 任务列表、新建、编辑、启停、删除 |
| `/assets` | 我的作品 | 作品网格、类型筛选、批量下载 |
| `/console/overview` | 控制台概览 | 余额、本月消费、运行任务与订阅状态 |
| `/console/*` | 控制台 | `usages` 使用历史、`wallet` 钱包与充值、`subscription` 订阅套餐、`orders` 订单列表、`coupons` 兑换与优惠券、`connectors` 连接器、`skills` 技能、`browser-devices` 浏览器设备、`earnings` 推荐收益 |
| `/account/*` | 账号 | `profile` 个人资料、`security` 安全设置、`notifications` 通知中心 |
| `/lingxi-console-7c3f/*` | 管理后台 | 隐藏路径，独立管理员登录与会话；19 个页面，见下方列表 |
| `/:pathMatch(.*)*` | 404 | 未匹配路由 |

布局为左侧 60px 图标栏（首页 + 按对话 / 图片 / 视频 / 音乐分组的应用图标 + 定时任务 + 控制台），分组间以分隔线区分，悬浮提示显示「大类 · 应用名」；控制台页额外显示 210px 二级导航，右上角固定积分胶囊。

对话应用、创作工具、首页分类与精选作品统一登记在 `frontend/src/config/tools.js`；22 个工具共用 `frontend/src/views/create/index.vue`，按路由 `meta.toolKey` 渲染参数面板，任务与画廊按工具持久化到 `localStorage`。

管理后台入口由 `frontend/src/config/admin.js` 定义（默认 `/lingxi-console-7c3f`，可用 `VITE_ADMIN_PATH` 覆盖），导航分为 6 组：

| 分组 | 页面 |
| --- | --- |
| 总览 | 仪表盘、实时监控 |
| 用户与运营 | 用户管理、任务管理、操作日志 |
| 交易 | 订单管理、退款与对账、支付平台、积分管理、兑换码与优惠券、订阅套餐、推荐管理 |
| 内容与合规 | 内容审核 |
| 平台配置 | 模型管理、平台接入、计费规则、系统设置 |
| 数据 | 用量记录、报表分析 |

## 后端模块规划

| 编号 | 模块 | 说明 |
| --- | --- | --- |
| B1 | 用户体系 | 登录、角色、账号状态、JWT |
| B2 | 模型网关 | 上游模型接入、路由、Key 托管、对外 API |
| B3 | 计费与订单 | 订单管理已落地基础接口；计费规则、支付回调与退款待实现 |
| B4 | 钱包资产 | 账户与流水查询已落地；充值/消费等余额变动由 B6/B9 驱动 |
| B5 | 订阅套餐 | 套餐目录、订阅开通/续费/变更、自动续费与历史已落地 |
| B6 | 支付与充值 | 支付渠道、充值、兑换码 |
| B7 | 无人值守 Agent | 定时任务、多轮编排、授权、双通道 |
| B8 | 产物管理 | 图片/视频/文档产物与对象存储 |
| B9 | 用量统计 | 模型调用、Token、成本聚合 |
| B10 | 运营后台 | 用户、订单、内容管理 |
| B11 | 数据报表 | 营收、留存、用量看板 |
| B12 | 本机浏览器通道 | 设备配对、反向 WebSocket、本机执行 |

## 接口与文档

- Knife4j 在线调试：`/api/doc.html`
- OpenAPI JSON：`/api/v3/api-docs`
- 统一响应：`{ code, message, data, timestamp, success }`
- 认证头：`Authorization: Bearer <token>`

已实现接口：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/health` | 健康检查 |
| POST | `/api/auth/login` | 登录 |
| GET | `/api/auth/profile` | 当前用户 |
| POST | `/api/auth/logout` | 退出登录（幂等，旧令牌加入黑名单） |
| POST | `/api/auth/refresh` | 刷新令牌（旧令牌立即失效） |
| GET | `/api/agent/tasks` | 任务列表（支持 `page` / `size` 分页，响应仍为数组） |
| POST | `/api/agent/tasks` | 新建任务 |
| PUT | `/api/agent/tasks/{id}` | 更新任务 |
| PATCH | `/api/agent/tasks/{id}/status` | 启停任务 |
| DELETE | `/api/agent/tasks/{id}` | 删除任务 |
| GET / POST | `/api/applications` | AI 应用列表 / 新建 |
| GET / PUT / DELETE | `/api/applications/{id}` | 应用详情 / 更新 / 删除 |
| GET / POST | `/api/api-keys` | API Key 列表 / 创建（明文仅创建时返回） |
| PUT / DELETE | `/api/api-keys/{id}` | 重命名/启停 / 删除 |
| GET | `/api/notifications` | 通知列表（支持 `unreadOnly`） |
| GET | `/api/notifications/unread-count` | 未读数量 |
| PUT | `/api/notifications/{id}/read` | 标记单条已读 |
| PUT | `/api/notifications/read-all` | 全部已读 |
| DELETE | `/api/notifications/{id}` | 删除通知 |
| GET / POST | `/api/devices` | 登录设备列表 / 新增 |
| PUT / DELETE | `/api/devices/{id}` | 重命名 / 删除设备 |
| GET | `/api/admin/users` | 用户列表（ADMIN） |
| GET | `/api/admin/users/{id}` | 用户详情（ADMIN） |
| POST | `/api/admin/users` | 新建用户（ADMIN） |
| PUT / DELETE | `/api/admin/users/{id}` | 更新 / 删除用户（ADMIN） |
| GET / POST | `/api/skills` | 技能列表 / 新建 |
| GET / PUT / DELETE | `/api/skills/{id}` | 技能详情 / 更新 / 删除 |
| GET / POST | `/api/connectors` | 连接器列表 / 新建 |
| GET / PUT / DELETE | `/api/connectors/{id}` | 连接器详情 / 更新 / 删除 |
| GET / POST | `/api/artifacts` | 产物列表（支持 `type` / `keyword` / `trashed`）/ 新建 |
| GET / PUT / DELETE | `/api/artifacts/{id}` | 产物详情 / 更新 / 删除 |
| GET / POST | `/api/orders` | 订单列表（支持 `status` / `type` / `keyword`）/ 新建 |
| GET / PUT / PATCH / DELETE | `/api/orders/{id}` | 订单详情 / 更新 / 取消 / 删除 |
| GET | `/api/wallet` | 钱包账户 |
| GET | `/api/wallet/transactions` | 钱包流水（支持 `type`） |
| GET | `/api/subscriptions/plans` | 套餐目录 |
| GET / POST | `/api/subscriptions` | 当前订阅 / 开通、续费或变更套餐 |
| PUT / DELETE | `/api/subscriptions/current` | 自动续费开关 / 取消订阅 |
| GET | `/api/subscriptions/history` | 订阅历史 |

完整字段与示例见 [接口文档](./docs/接口文档.md)。

> 前端 Mock 模式还提供 `/auth/email-code`（发送邮箱验证码）与 `/auth/register`（邮箱注册）两个演示接口，仅在浏览器本地生效，不属于后端契约。

## 文档

| 文档 | 内容 |
| --- | --- |
| [架构设计](./docs/架构设计.md) | 总体架构、前端路由与后台、演示数据层、模块边界、B7 引擎设计 |
| [功能实现说明](./docs/功能实现说明.md) | 完成度、页面清单、实现矩阵、里程碑、已知限制 |
| [接口文档](./docs/接口文档.md) | 响应约定、错误码、Mock 模式、接口明细 |
| [数据库设计](./docs/数据库设计.md) | 已落地表、规划表、运营后台表、计费字段约定、与演示数据的关系 |
| [模型接入与计费方案](./docs/模型接入与计费方案.md) | 渠道收敛、三段计费、双价定价、对账与风控 |
| [开发指南](./docs/开发指南.md) | 环境、启动、配置、规范、FAQ |
| [文档索引](./docs/文档索引.md) | 全部文档入口与阅读路径 |

## 路线图

1. **M1**：B2 模型网关最小闭环，工作台可对话；
2. **M2**：B7 调度器 + 单轮执行，任务自动运行；
3. **M3**：B7 多轮编排、Skills/MCP 授权与运行详情；
4. **M4**：B3/B4/B6 计费与支付闭环（B3 订单、B4 钱包、B5 订阅基础管理已落地，待支付回调与余额变动）；
5. **M5**：B12 本机浏览器通道与设备管理。

## 合规提示

- 支付、订阅、内容生成与数据存储上线前，需完成相应资质、支付回调验签、内容审核与隐私合规评估；
- 严禁将演示账号、默认 JWT 密钥、数据库口令用于生产环境。

## 贡献

1. Fork 并创建特性分支；
2. 遵循 [开发指南](./docs/开发指南.md) 的分层与提交规范；
3. 提交前执行 `mvn -f backend/pom.xml -DskipTests compile` 与 `npm run build`；
4. 提交 Pull Request 并说明模块、动机与验证方式。

## License

本项目暂未附带许可证文件。确定分发方式后补充 LICENSE。
