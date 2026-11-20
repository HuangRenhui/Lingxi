# Lingxi

English | [简体中文](./README.md)

> A runnable, deployable AI creation platform: Vue 3 + Element Plus frontend (all user and admin pages, runnable on mock data alone), Spring Boot + B1-B12 backend. The backend currently ships login, token refresh/logout, B7 task-definition management, and base APIs for applications, API keys, notifications, devices, skills, connectors, artifacts, orders, and wallet; the **B7 unattended agent orchestration engine is the core direction of the upcoming milestones**.

![Java](https://img.shields.io/badge/Java-8-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-brightgreen)
![Vue](https://img.shields.io/badge/Vue-3.5-42b883)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479a1)
![Redis](https://img.shields.io/badge/Redis-7.x-dc382d)

## Introduction

Lingxi provides four creation tracks — chat, image, video, and music — with 8 chat apps (ChatGPT, Claude, Gemini, Grok, DeepSeek, Kimi, Qwen, Doubao) and 22 creation tools, plus scheduled tasks, a console, account management, and an admin console. It runs locally or deploys as a full stack:

- **Frontend**: Vue 3 + Element Plus + Vite + Vue Router + Vuex, with a dark theme and config-driven pages, organized as home + creation area + console + admin.
- **Backend**: Spring Boot 2.7.18 + MyBatis-Plus + Spring Security + JWT, with boundaries for modules B1-B12.
- **Infrastructure**: MySQL 8 + Redis 7, runnable with Docker Compose.
- **Docs**: Chinese and English READMEs, plus architecture, feature, API, database, development, and page-spec docs.

The frontend ships a built-in mock layer, so every page works locally without the backend; Docker Compose starts the complete production stack.

## Current Status

| Capability | Status |
| --- | --- |
| Project skeleton (frontend + backend + Docker + docs) | ✅ |
| Login / JWT authentication / token refresh and logout / current user | ✅ |
| Task list, create, update, enable/disable, delete | ✅ |
| User-facing pages (home, 8 chat apps, 22 tools, tasks, assets, 10 console pages, 3 account pages) | ✅ Frontend done (mock data) |
| Admin console (19 pages, admin login, hidden entry, grouped navigation) | ✅ Frontend done (mock data) |
| Billing-rule cost accounting (input/output/cached tiers, margin alerts) | ✅ Frontend done (mock data) |
| Table pagination, fixed columns, and dark theme | ✅ |
| Model gateway (B2) | ⬜ Planned |
| Scheduler and multi-round agent loop (B7) | ⬜ Planned |
| Billing rules / payment callbacks (B3 billing, B6) | ⬜ Backend planned; frontend demo pages done |
| Artifacts (B8) | 🟡 List / detail / create / update / delete APIs done |
| Orders (B3), wallet (B4) | 🟡 Base APIs done; payment callbacks and balance changes pending |
| Usage / admin / reports / local channel (B9-B12) | ⬜ Backend planned; frontend demo pages done |

Demo account: `admin / Admin@123` (initialization only; change before deployment).

> Except for login, tasks and base management APIs, business pages currently read demo data from `frontend/src/mock`, stored in the browser's `localStorage` and independent of the backend.

## Core Design

- **Module boundaries**: B1 users, B2 model gateway, B3 billing & orders, B4 wallet, B5 subscriptions, B6 payments & top-ups, B7 unattended agent, B8 artifacts, B9 usage, B10 admin, B11 reports, B12 local browser channel.
- **B7 dual channel**: `CLOUD_WORKER` runs in the cloud even when the user's device is offline; `LOCAL_RUNNER` runs on a paired browser/device for local sessions and intranet resources.
- **B7 state machine**: `PENDING → RUNNING → SUCCESS / FAILED / ROUND_LIMIT / CANCELLED`.
- **Unified response**: HTTP 200 with a business `code`; the user-side frontend interceptor handles 401 (refresh and replay once) and error messages centrally.
- **Billing by design**: all model calls go through the B2 gateway so usage can be metered and channels can be swapped.
- **Billing basis**: chat models price input, output, and cached input separately; upstream cost is entered in the same unit as the sale price, and the margin is recomputed live — below 15% warns, below zero means a loss.
- **Cost reconciliation**: `usage_record` aggregates cost and charge, reconciled against upstream invoices monthly; streaming calls must return usage, and cancelled runs are still charged for the usage already produced.
- **Dual frontend**: the user app and admin console share one Vue project; the console lives on a configurable hidden path (default `/lingxi-console-7c3f`) with its own session.
- **Demo data layer**: `frontend/src/mock` installs a custom Axios adapter, so every page works without the backend; data is persisted per module in `localStorage` and can be cleared from Security settings.
- **Consistent pagination**: every table defaults to 10 rows per page via the shared `TablePager`; filters reset to page 1 and the pager falls back when data shrinks.

## Tech Stack

### Backend

| Component | Choice |
| --- | --- |
| JDK | 1.8+ (target 1.8; a JDK, not a JRE, is required; JDK 21 verified) |
| Spring Boot | 2.7.18 |
| Security | Spring Security + JWT (jjwt 0.11.5) + BCrypt |
| Persistence | MyBatis-Plus 3.5.7 + MySQL 8 |
| Cache | Redis + Spring Data Redis |
| API docs | Knife4j / OpenAPI 3 |
| Build | Maven |

### Frontend

| Component | Choice |
| --- | --- |
| Framework | Vue 3.5 |
| UI | Element Plus 2.9 |
| Router / state | Vue Router 4 / Vuex 4 |
| HTTP | Axios |
| Build | Vite 5 |

## Architecture

```text
Browser (Vue 3 + Element Plus)
        │  /api (JSON + JWT)
        ▼
Spring Boot 2.7
  controller → service → mapper → MySQL
  security / common / config / dto / vo / entity
        │
        ├── MySQL 8: users, tasks, orders, wallet...
        ├── Redis: login failure counters, token blacklist, task locks, runtime context
        └── B7 engine (planned): scheduler → agent loop → Skills/MCP → model gateway
```

## Quick Start

### Option 1: Docker Compose

```powershell
Copy-Item .env.example .env
# Edit .env: MYSQL_ROOT_PASSWORD / REDIS_PASSWORD / JWT_SECRET (at least 32 bytes)
docker compose up -d --build
```

| Service | URL |
| --- | --- |
| Frontend | <http://localhost> |
| Backend API | <http://localhost:8080/api> |
| Health check | <http://localhost:8080/api/health> |
| Knife4j | <http://localhost:8080/api/doc.html> |
| MySQL | localhost:3306 |
| Redis | localhost:6379 |

MySQL applies `schema.sql` and `data.sql` on first startup.

`JWT_SECRET` is required and must be at least 32 bytes; outside the development profile, a missing secret aborts startup (the development default exists only in `application-dev.yml`). CORS is controlled by `CORS_ALLOWED_ORIGINS` as a comma-separated list, defaulting to `http://localhost:5173,http://127.0.0.1:5173`; `*` must not be combined with credentials. `.env.example` and `docker-compose.yml` currently share the same three required variables: `MYSQL_ROOT_PASSWORD`, `REDIS_PASSWORD`, and `JWT_SECRET`; `CORS_ALLOWED_ORIGINS` is optional, and extra origins can be added to the backend environment in Compose.

### Option 2: Local Development

1. Create the `lingxi` database and import SQL:

```powershell
mysql -uroot -p lingxi -e "source backend/src/main/resources/db/schema.sql"
mysql -uroot -p lingxi -e "source backend/src/main/resources/db/data.sql"
```

2. Start Redis (optional but recommended):

```powershell
docker run -d --name lingxi-redis -p 6379:6379 redis:7-alpine
```

3. Start the backend (`JAVA_HOME` must point to a JDK, not a JRE; JDK 21 is verified):

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\latest\jdk-21"
mvn -f backend/pom.xml spring-boot:run
```

Outside the `dev` profile, `JWT_SECRET` (at least 32 bytes) must be set or startup fails; the development default is provided only in `application-dev.yml`. Set `CORS_ALLOWED_ORIGINS` when calling the backend directly (comma-separated; defaults to the two local port-5173 origins).

4. Start the frontend:

```powershell
cd frontend
npm install
npm run dev
```

Open <http://localhost:5173>. The dev server enables the mock layer by default (no login or backend required); set `VITE_MOCK=false` to use the real backend.

The admin console is at <http://localhost:5173/lingxi-console-7c3f/login> (demo admin `admin / Admin@123`); change the hidden path with `VITE_ADMIN_PATH`. The console currently uses a frontend-only demo session and must be wired to backend authentication before launch.

See the [development guide](./docs/开发指南.md) for configuration and troubleshooting (Chinese).

### Option 3: Build and publish the frontend

```powershell
cd frontend
npm ci
npm run build
```

The output lives in `frontend/dist` and can be served by any static host or Nginx. The bundled `frontend/nginx.conf` already provides SPA fallback and a `/api` reverse proxy; set `VITE_API_BASE_URL` at build time to point at a separate backend. Deploy the backend with MySQL and Redis for the complete product experience. For a frontend-only demo, build with `VITE_MOCK=true`; all data is then kept in the browser.

## Project Structure

```text
lingxi/
├── backend/                        # Spring Boot backend
│   ├── src/main/java/com/lingxi/
│   │   ├── common/                 # Result, exceptions, Redis keys
│   │   ├── config/                 # Security, MyBatis-Plus, Redis, OpenAPI
│   │   ├── controller/             # Health, auth, task endpoints
│   │   ├── dto/                    # Request objects
│   │   ├── entity/                 # Database entities
│   │   ├── mapper/                 # MyBatis-Plus mappers
│   │   ├── security/               # JWT filter and login context
│   │   ├── service/                # Business interfaces and implementations
│   │   └── vo/                     # Response objects
│   ├── src/main/resources/
│   │   ├── db/                     # schema.sql / data.sql
│   │   ├── application.yml
│   │   ├── application-dev.yml
│   │   ├── application-prod.yml
│   │   └── logback-spring.xml
│   ├── Dockerfile
│   └── pom.xml
├── frontend/                       # Vue 3 frontend
│   ├── src/
│   │   ├── api/                    # API clients
│   │   ├── assets/                 # Logo and model/connector icons
│   │   ├── components/             # Shared components (PageHeader, StatCard, TablePager...)
│   │   ├── config/                 # tools.js registry; admin.js hidden admin path
│   │   ├── layout/                 # index.vue user layout; admin.vue admin layout
│   │   ├── mock/                   # Demo data layer (account, platform, admin, coupons)
│   │   ├── router/                 # Routes and auth guards
│   │   ├── store/                  # Vuex user state
│   │   ├── styles/                 # Global styles and theme variables
│   │   ├── utils/                  # Axios, auth, model catalog, CSV export
│   │   └── views/                  # Home, chat, creation, tasks, assets, console, account, admin/
│   ├── Dockerfile
│   ├── nginx.conf
│   ├── vite.config.js
│   └── package.json
├── docs/                           # Architecture, feature, API, DB, dev docs, specs
├── docker-compose.yml
├── .env.example
├── README.md
└── README.en.md
```

## Frontend Routes

| Route | Page | Notes |
| --- | --- | --- |
| `/` → `/home` | Home | Carousel, quick-start categories, showcases, support dialog |
| `/chat/:app` | Chat | 8 apps; conversations are isolated per app |
| 22 tool routes | Creation | `/maestro`, `/nanobanana`, `/seedance`, `/kling`, `/suno`, ... rendered by one config-driven page |
| `/tasks` | Scheduled tasks | List, create, edit, enable/disable, delete |
| `/assets` | My assets | Asset grid, type filter, batch download |
| `/console/*` | Console | Overview, usage, wallet, subscription, orders, coupons, connectors, skills, browser devices, earnings |
| `/account/*` | Account | Profile, security, notifications |
| `/lingxi-console-7c3f/*` | Admin console | Hidden path with its own admin session; 19 pages in 6 navigation groups |
| `/:pathMatch(.*)*` | 404 | Fallback |

Admin pages are grouped as Overview (dashboard, monitor), Users & Operations (users, tasks, logs), Transactions (orders, refunds, payments, credits, coupons, plans, referrals), Content & Compliance (moderation), Platform Config (models, providers, billing rules, settings), and Data (usage, reports).

## Backend Modules

| ID | Module | Description |
| --- | --- | --- |
| B1 | Users | Login, roles, account status, JWT |
| B2 | Model gateway | Upstream models, routing, key custody, public API |
| B3 | Billing & orders | Orders API done; pricing rules, payment callbacks, refunds pending |
| B4 | Wallet | Account and transaction queries done |
| B5 | Subscriptions | Plans, entitlements, cycles |
| B6 | Payments & top-ups | Payment channels, top-ups, redeem codes |
| B7 | Unattended agent | Scheduled tasks, multi-round loop, authorization, dual channel |
| B8 | Artifacts | Image/video/document outputs and object storage |
| B9 | Usage | Model calls, tokens, cost aggregation |
| B10 | Admin | Users, orders, content management |
| B11 | Reports | Revenue, retention, usage dashboards |
| B12 | Local browser channel | Device pairing, reverse WebSocket, local execution |

## API

- Knife4j: `/api/doc.html`
- OpenAPI JSON: `/api/v3/api-docs`
- Unified response: `{ code, message, data, timestamp, success }`
- Auth header: `Authorization: Bearer <token>`

| Method | Path | Description |
| --- | --- | --- |
| GET | `/api/health` | Health check |
| POST | `/api/auth/login` | Login |
| GET | `/api/auth/profile` | Current user |
| POST | `/api/auth/logout` | Logout (idempotent; the old token is blacklisted) |
| POST | `/api/auth/refresh` | Refresh token (the old token is invalidated immediately) |
| GET | `/api/agent/tasks` | List tasks (`page` / `size` paging; response remains an array) |
| POST | `/api/agent/tasks` | Create task |
| PUT | `/api/agent/tasks/{id}` | Update task |
| PATCH | `/api/agent/tasks/{id}/status` | Enable/disable task |
| DELETE | `/api/agent/tasks/{id}` | Delete task |
| GET / POST | `/api/orders` | List orders (`page` / `size` / `status` / `type` / `keyword`) / create |
| GET / PUT / PATCH / DELETE | `/api/orders/{id}` | Order detail / update / cancel / delete |
| GET | `/api/wallet` | Wallet account |
| GET | `/api/wallet/transactions` | Wallet transactions (`type` filter) |

See the [API documentation](./docs/接口文档.md) for fields and examples (Chinese).

> In mock mode, the frontend additionally handles `/auth/email-code` and `/auth/register` locally in the browser; they are not backend contracts.

## Documentation

| Document | Content |
| --- | --- |
| [Architecture](./docs/架构设计.md) | Overall architecture, module boundaries, B7 design |
| [Feature status](./docs/功能实现说明.md) | Completion status, matrix, milestones, limitations |
| [API](./docs/接口文档.md) | Conventions, error codes, endpoints |
| [Database](./docs/数据库设计.md) | Existing tables, planned tables, billing fields, relationships |
| [Model access & billing plan](./docs/模型接入与计费方案.md) | Channel consolidation, three-stage billing, dual pricing, reconciliation and risk control (Chinese) |
| [Development guide](./docs/开发指南.md) | Environment, startup, configuration, conventions, FAQ |
| [Docs index](./docs/文档索引.md) | Entry point and reading paths (Chinese) |

## Roadmap

1. **M1**: Minimal B2 model gateway; workspace chat works.
2. **M2**: B7 scheduler and single-round execution; tasks run automatically.
3. **M3**: B7 multi-round loop, Skills/MCP authorization, run details.
4. **M4**: B3/B4/B6 billing, wallet, and payment loop.
5. **M5**: B12 local browser channel and device management.

## Compliance Notes

- Before launch, complete payment verification, content moderation, privacy assessments, and any required licenses.
- Never use demo accounts, default JWT secrets, or default database passwords in production.

## Contributing

1. Fork the repository and create a feature branch.
2. Follow the layering and commit conventions in the development guide.
3. Verify with `mvn -f backend/pom.xml -DskipTests compile` and `npm run build`.
4. Open a pull request describing the module, motivation, and verification.

## License

No license file is included yet. Add one before distribution.
