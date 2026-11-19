<template>
  <div class="admin-shell">
    <aside class="admin-side">
      <router-link :to="ADMIN_HOME_PATH" class="admin-brand">
        <img :src="logo" alt="灵犀" />
        <span class="brand-copy">
          <strong>灵犀</strong>
          <small>管理后台</small>
        </span>
      </router-link>

      <nav class="admin-nav">
        <div v-for="group in navGroups" :key="group.title" class="nav-group">
          <span class="nav-title">{{ group.title }}</span>
          <router-link
            v-for="item in group.items"
            :key="item.path"
            :to="item.path"
            class="nav-item"
            :class="{ active: isActive(item.path) }"
          >
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.title }}</span>
          </router-link>
        </div>
      </nav>
    </aside>

    <div class="admin-main">
      <header class="admin-topbar">
        <div class="crumb">
          <span>管理后台</span>
          <el-icon><ArrowRight /></el-icon>
          <strong>{{ currentTitle }}</strong>
        </div>

        <div class="topbar-right">
          <span class="demo-chip"><i class="dot" />演示数据</span>
          <el-dropdown trigger="click" placement="bottom-end" @command="handleCommand">
            <button type="button" class="admin-user">
              <span class="avatar">{{ initial }}</span>
              <span class="user-copy">
                <strong>{{ displayName }}</strong>
                <span>平台管理员</span>
              </span>
              <el-icon class="caret"><ArrowDown /></el-icon>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="admin-viewport">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import logo from '@/assets/logo.svg'
import { getAdminSession, removeAdminSession } from '@/utils/auth'
import { ADMIN_HOME_PATH, ADMIN_LOGIN_PATH, adminPath } from '@/config/admin'

const route = useRoute()
const router = useRouter()
const adminSession = getAdminSession() || {}

const navGroups = [
  {
    title: '总览',
    items: [
      { path: adminPath('dashboard'), title: '仪表盘', icon: 'Odometer' },
      { path: adminPath('monitor'), title: '实时监控', icon: 'Watch' }
    ]
  },
  {
    title: '用户与运营',
    items: [
      { path: adminPath('users'), title: '用户管理', icon: 'User' },
      { path: adminPath('tasks'), title: '任务管理', icon: 'Timer' },
      { path: adminPath('logs'), title: '操作日志', icon: 'Document' }
    ]
  },
  {
    title: '交易',
    items: [
      { path: adminPath('orders'), title: '订单管理', icon: 'Tickets' },
      { path: adminPath('refunds'), title: '退款与对账', icon: 'RefreshLeft' },
      { path: adminPath('payments'), title: '支付平台', icon: 'CreditCard' },
      { path: adminPath('credits'), title: '积分管理', icon: 'Coin' },
      { path: adminPath('coupons'), title: '兑换码与优惠券', icon: 'Ticket' },
      { path: adminPath('plans'), title: '订阅套餐', icon: 'Medal' },
      { path: adminPath('referrals'), title: '推荐管理', icon: 'Share' }
    ]
  },
  {
    title: '内容与合规',
    items: [{ path: adminPath('moderation'), title: '内容审核', icon: 'View' }]
  },
  {
    title: '平台配置',
    items: [
      { path: adminPath('models'), title: '模型管理', icon: 'Cpu' },
      { path: adminPath('providers'), title: '平台接入', icon: 'Connection' },
      { path: adminPath('billing-rules'), title: '计费规则', icon: 'PriceTag' },
      { path: adminPath('settings'), title: '系统设置', icon: 'Setting' }
    ]
  },
  {
    title: '数据',
    items: [
      { path: adminPath('usage'), title: '用量记录', icon: 'DataLine' },
      { path: adminPath('reports'), title: '报表分析', icon: 'PieChart' }
    ]
  }
]

const currentTitle = computed(() => route.meta.title || '管理后台')
const displayName = computed(() => adminSession.nickname || adminSession.username || '平台管理员')
const initial = computed(() => displayName.value.slice(0, 1))

function isActive(path) {
  return route.path === path
}

function handleCommand(command) {
  if (command === 'logout') {
    removeAdminSession()
    router.push(ADMIN_LOGIN_PATH)
  }
}
</script>

<style scoped>
.admin-shell {
  display: flex;
  height: 100vh;
  overflow: hidden;
  background: var(--app-bg);
}

/* ---------- 左侧导航 ---------- */
.admin-side {
  width: 232px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  background: #0a0c10;
  border-right: 1px solid var(--app-border);
}

.admin-brand {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 20px 18px 18px;
  flex-shrink: 0;
}

.admin-brand img {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  flex-shrink: 0;
}

.brand-copy {
  display: flex;
  flex-direction: column;
  gap: 1px;
  line-height: 1.3;
}

.brand-copy strong {
  font-size: 15px;
  font-weight: 700;
  letter-spacing: -0.01em;
}

.brand-copy small {
  color: var(--app-muted);
  font-size: 10.5px;
  letter-spacing: 0.08em;
}

.admin-nav {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 4px 12px 16px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  scrollbar-width: none;
}

.admin-nav::-webkit-scrollbar {
  display: none;
}

.nav-group {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.nav-title {
  display: block;
  padding: 0 11px 7px;
  color: #9aa4af;
  font-size: 11.5px;
  font-weight: 500;
  letter-spacing: 0.08em;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 38px;
  padding: 0 11px;
  border-radius: 9px;
  color: #9aa4af;
  font-size: 13px;
  transition: all 0.16s ease;
}

.nav-item .el-icon {
  font-size: 16px;
  flex-shrink: 0;
}

.nav-item:hover {
  color: var(--app-text);
  background: rgba(255, 255, 255, 0.05);
}

.nav-item.active {
  color: #dbeafe;
  background: var(--app-primary-soft);
  box-shadow: inset 0 0 0 1px rgba(59, 130, 246, 0.3);
}

/* ---------- 右侧内容 ---------- */
.admin-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.admin-topbar {
  height: 58px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 0 26px;
  border-bottom: 1px solid var(--app-border);
  background: rgba(10, 13, 18, 0.78);
  backdrop-filter: blur(10px);
}

.crumb {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  color: var(--app-muted);
  font-size: 12.5px;
}

.crumb .el-icon {
  font-size: 11px;
}

.crumb strong {
  color: var(--app-text);
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-shrink: 0;
}

.demo-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid rgba(245, 158, 11, 0.28);
  background: rgba(245, 158, 11, 0.1);
  color: #fbbf24;
  font-size: 11px;
  white-space: nowrap;
}

.demo-chip .dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #f59e0b;
}

.admin-user {
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 4px 10px 4px 4px;
  border-radius: 999px;
  border: 1px solid var(--app-border);
  background: rgba(255, 255, 255, 0.02);
  color: var(--app-text);
  cursor: pointer;
  transition: all 0.16s ease;
}

.admin-user:hover {
  border-color: var(--app-border-strong);
  background: rgba(255, 255, 255, 0.05);
}

.admin-user .avatar {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.45), rgba(29, 78, 216, 0.22));
  border: 1px solid rgba(96, 165, 250, 0.3);
  color: #dbeafe;
  font-size: 11px;
  font-weight: 600;
  flex-shrink: 0;
}

.user-copy {
  display: flex;
  flex-direction: column;
  gap: 1px;
  line-height: 1.25;
  text-align: left;
}

.user-copy strong {
  font-size: 12px;
  font-weight: 600;
}

.user-copy span {
  color: var(--app-muted);
  font-size: 10px;
}

.caret {
  color: var(--app-muted);
  font-size: 11px;
}

.admin-viewport {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
}

@media (max-width: 1080px) {
  .admin-side {
    width: 68px;
  }

  .brand-copy,
  .nav-title,
  .nav-item span {
    display: none;
  }

  .admin-brand {
    justify-content: center;
    padding: 18px 0;
  }

  .nav-item {
    justify-content: center;
    padding: 0;
  }

  .admin-topbar {
    padding: 0 18px;
  }
}

@media (max-width: 720px) {
  .crumb span:first-child,
  .crumb .el-icon,
  .demo-chip,
  .user-copy {
    display: none;
  }

  .admin-user {
    padding: 4px;
    border: 0;
    background: transparent;
  }

  .caret {
    display: none;
  }
}
</style>

<style lang="scss" src="../styles/admin.scss"></style>
