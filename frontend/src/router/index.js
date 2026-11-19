import { createRouter, createWebHistory } from 'vue-router'
import { clearInvalidSession, getAdminSession, getToken, isTokenValid, removeAdminSession } from '@/utils/auth'
import Layout from '@/layout/index.vue'
import { tools } from '@/config/tools'
import { ADMIN_BASE, ADMIN_HOME_PATH, ADMIN_LOGIN_PATH } from '@/config/admin'

const toolRoutes = tools.map(tool => ({
  path: tool.path.replace(/^\//, ''),
  name: `Tool-${tool.key}`,
  component: () => import('@/views/create/index.vue'),
  meta: { title: tool.title, toolKey: tool.key }
}))

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { public: true, title: '登录' }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/home/index.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'chat/:app',
        name: 'Chat',
        component: () => import('@/views/workspace/index.vue'),
        meta: { title: '对话' }
      },
      ...toolRoutes,
      {
        path: 'workspace',
        redirect: '/chat/chatgpt'
      },
      {
        path: 'image',
        redirect: '/nanobanana'
      },
      {
        path: 'video',
        redirect: '/seedance'
      },
      {
        path: 'tasks',
        name: 'Tasks',
        component: () => import('@/views/tasks/index.vue'),
        meta: { title: '定时任务' }
      },
      {
        path: 'assets',
        name: 'Assets',
        component: () => import('@/views/assets/index.vue'),
        meta: { title: '我的作品' }
      },
      {
        path: 'console/overview',
        name: 'Overview',
        component: () => import('@/views/overview/index.vue'),
        meta: { title: '控制台概览' }
      },
      {
        path: 'console/usages',
        name: 'Usages',
        component: () => import('@/views/usage/index.vue'),
        meta: { title: '使用历史' }
      },
      {
        path: 'console/wallet',
        name: 'Wallet',
        component: () => import('@/views/wallet/index.vue'),
        meta: { title: '钱包与充值' }
      },
      {
        path: 'console/subscription',
        name: 'Subscription',
        component: () => import('@/views/subscription/index.vue'),
        meta: { title: '订阅套餐' }
      },
      {
        path: 'console/skills',
        name: 'Skills',
        component: () => import('@/views/skills/index.vue'),
        meta: { title: '技能' }
      },
      {
        path: 'console/connectors',
        name: 'Connectors',
        component: () => import('@/views/connectors/index.vue'),
        meta: { title: '连接器' }
      },
      {
        path: 'console/browser-devices',
        name: 'BrowserDevices',
        component: () => import('@/views/devices/index.vue'),
        meta: { title: '浏览器设备' }
      },
      {
        path: 'console/orders',
        name: 'Orders',
        component: () => import('@/views/orders/index.vue'),
        meta: { title: '订单列表' }
      },
      {
        path: 'console/earnings',
        name: 'Earnings',
        component: () => import('@/views/earnings/index.vue'),
        meta: { title: '推荐收益' }
      },
      {
        path: 'console/coupons',
        name: 'Coupons',
        component: () => import('@/views/coupons/index.vue'),
        meta: { title: '兑换与优惠券' }
      },
      {
        path: 'account/profile',
        name: 'AccountProfile',
        component: () => import('@/views/profile/index.vue'),
        meta: { title: '个人资料' }
      },
      {
        path: 'account/security',
        name: 'AccountSecurity',
        component: () => import('@/views/security/index.vue'),
        meta: { title: '安全设置' }
      },
      {
        path: 'account/notifications',
        name: 'AccountNotifications',
        component: () => import('@/views/notifications/index.vue'),
        meta: { title: '通知中心' }
      }
    ]
  },
  {
    path: ADMIN_LOGIN_PATH,
    name: 'AdminLogin',
    component: () => import('@/views/admin/login/index.vue'),
    meta: { public: true, title: '管理员登录' }
  },
  {
    path: ADMIN_BASE,
    component: () => import('@/layout/admin.vue'),
    redirect: ADMIN_HOME_PATH,
    meta: { admin: true },
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/dashboard/index.vue'),
        meta: { title: '仪表盘' }
      },
      {
        path: 'monitor',
        name: 'AdminMonitor',
        component: () => import('@/views/admin/monitor/index.vue'),
        meta: { title: '实时监控' }
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/users/index.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'logs',
        name: 'AdminLogs',
        component: () => import('@/views/admin/logs/index.vue'),
        meta: { title: '操作日志' }
      },
      {
        path: 'tasks',
        name: 'AdminTasks',
        component: () => import('@/views/admin/tasks/index.vue'),
        meta: { title: '任务管理' }
      },
      {
        path: 'orders',
        name: 'AdminOrders',
        component: () => import('@/views/admin/orders/index.vue'),
        meta: { title: '订单管理' }
      },
      {
        path: 'refunds',
        name: 'AdminRefunds',
        component: () => import('@/views/admin/refunds/index.vue'),
        meta: { title: '退款与对账' }
      },
      {
        path: 'payments',
        name: 'AdminPayments',
        component: () => import('@/views/admin/payments/index.vue'),
        meta: { title: '支付平台' }
      },
      {
        path: 'credits',
        name: 'AdminCredits',
        component: () => import('@/views/admin/credits/index.vue'),
        meta: { title: '积分管理' }
      },
      {
        path: 'referrals',
        name: 'AdminReferrals',
        component: () => import('@/views/admin/referrals/index.vue'),
        meta: { title: '推荐管理' }
      },
      {
        path: 'coupons',
        name: 'AdminCoupons',
        component: () => import('@/views/admin/coupons/index.vue'),
        meta: { title: '兑换码与优惠券' }
      },
      {
        path: 'plans',
        name: 'AdminPlans',
        component: () => import('@/views/admin/plans/index.vue'),
        meta: { title: '订阅套餐' }
      },
      {
        path: 'moderation',
        name: 'AdminModeration',
        component: () => import('@/views/admin/moderation/index.vue'),
        meta: { title: '内容审核' }
      },
      {
        path: 'usage',
        name: 'AdminUsage',
        component: () => import('@/views/admin/usage/index.vue'),
        meta: { title: '用量记录' }
      },
      {
        path: 'reports',
        name: 'AdminReports',
        component: () => import('@/views/admin/reports/index.vue'),
        meta: { title: '报表分析' }
      },
      {
        path: 'models',
        name: 'AdminModels',
        component: () => import('@/views/admin/models/index.vue'),
        meta: { title: '模型管理' }
      },
      {
        path: 'providers',
        name: 'AdminProviders',
        component: () => import('@/views/admin/providers/index.vue'),
        meta: { title: '模型接入' }
      },
      {
        path: 'billing-rules',
        name: 'AdminBillingRules',
        component: () => import('@/views/admin/billing-rules/index.vue'),
        meta: { title: '计费规则' }
      },
      {
        path: 'settings',
        name: 'AdminSettings',
        component: () => import('@/views/admin/settings/index.vue'),
        meta: { title: '系统设置' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { public: true, title: '页面不存在' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

/**
 * 登录后回跳地址只允许站内路径，禁止 // 或协议开头的开放重定向；
 * 同时避免回跳到登录页本身导致循环。
 */
function resolveInternalRedirect(value, fallback = '/home') {
  const target = String(value || '')
  if (!target.startsWith('/') || target.startsWith('//')) return fallback
  if (target === '/login' || target.startsWith('/login?') || target === ADMIN_LOGIN_PATH) return fallback
  return target
}

router.beforeEach(to => {
  document.title = to.meta.title ? `${to.meta.title} · 灵犀` : '灵犀'
  if (to.path === ADMIN_LOGIN_PATH) {
    return getAdminSession() ? ADMIN_HOME_PATH : true
  }
  if (to.meta.admin) {
    // 会话需带管理员标识且未过期，空对象或过期会话一律回到登录页
    if (!getAdminSession()) {
      // 失效会话在此显式清理，getAdminSession 本身保持只读
      removeAdminSession()
      return { path: ADMIN_LOGIN_PATH, query: { redirect: to.fullPath } }
    }
    return true
  }
  if (to.path === '/login') {
    const token = getToken()
    if (token && isTokenValid(token)) {
      return resolveInternalRedirect(to.query.redirect)
    }
    return true
  }
  if (to.meta.public) return true

  const token = getToken()
  if (!token) {
    return { path: '/login', query: { redirect: resolveInternalRedirect(to.fullPath) } }
  }
  // 令牌过期时先清理登录态再跳转，避免带着失效令牌反复触发 401
  if (!isTokenValid(token)) {
    clearInvalidSession()
    return { path: '/login', query: { redirect: resolveInternalRedirect(to.fullPath) } }
  }
  return true
})

export default router
