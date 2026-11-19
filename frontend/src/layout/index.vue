<template>
  <div class="app-layout">
    <aside class="rail">
      <router-link to="/home" class="rail-logo" aria-label="灵犀">
        <img :src="logo" alt="灵犀" />
      </router-link>

      <nav class="rail-nav">
        <button
          v-for="group in railGroups"
          :key="group.key"
          type="button"
          class="rail-item rail-group-btn"
          :class="{ active: isGroupActive(group) || activeGroupKey === group.key }"
          :aria-label="group.label"
          :aria-expanded="activeGroupKey === group.key"
          @click="toggleGroup(group.key)"
        >
          <el-icon><component :is="group.icon" /></el-icon>
          <b>{{ group.label }}</b>
        </button>

        <div class="rail-divider" />

        <el-tooltip content="我的作品" placement="right">
          <router-link to="/assets" class="rail-item" :class="{ active: isActive('/assets') }">
            <el-icon><FolderOpened /></el-icon>
          </router-link>
        </el-tooltip>

        <el-tooltip content="定时任务" placement="right">
          <router-link to="/tasks" class="rail-item" :class="{ active: isActive('/tasks') }">
            <el-icon><Timer /></el-icon>
          </router-link>
        </el-tooltip>

        <el-tooltip content="控制台" placement="right">
          <router-link to="/console/overview" class="rail-item" :class="{ active: isConsole }">
            <el-icon><Grid /></el-icon>
          </router-link>
        </el-tooltip>
      </nav>

      <div class="rail-bottom">
        <el-popover
          v-model:visible="accountMenuVisible"
          trigger="click"
          placement="right-end"
          :width="236"
          :show-arrow="false"
          :offset="14"
          popper-class="account-popper"
          @hide="switchVisible = false"
        >
          <template #reference>
            <button type="button" class="rail-avatar" title="账号">
              <img src="/images/avatar-default.jpg" alt="账号" />
            </button>
          </template>

          <div class="account-menu">
            <button
              type="button"
              class="account-head"
              :class="{ active: switchVisible }"
              @mouseenter="switchVisible = true"
              @click="switchVisible = !switchVisible"
            >
              <span class="account-mail">{{ accountEmail }}</span>
              <span class="account-switch-hint">切换账号</span>
              <el-icon class="account-caret"><ArrowRight /></el-icon>
            </button>

            <button type="button" class="account-item" @click="handleCommand('profile')">
              <el-icon><User /></el-icon>
              个人资料
            </button>
            <button type="button" class="account-item" @click="handleCommand('security')">
              <el-icon><Lock /></el-icon>
              安全设置
            </button>
            <button type="button" class="account-item" @click="handleCommand('notifications')">
              <el-icon><Bell /></el-icon>
              通知中心
            </button>
            <button type="button" class="account-item" @click="handleCommand('settings')">
              <el-icon><Setting /></el-icon>
              设置
            </button>
            <button type="button" class="account-item" @click="handleCommand('earnings')">
              <el-icon><Coin /></el-icon>
              推荐收益
            </button>
            <button type="button" class="account-item" @click="handleCommand('console')">
              <el-icon><Grid /></el-icon>
              控制台
            </button>
            <div class="account-divider" />
            <button type="button" class="account-item danger" @click="handleCommand('logout')">
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </button>

            <div v-if="switchVisible" class="account-switch">
              <span class="account-switch-title">切换账号</span>
              <span class="account-switch-label">当前账号</span>
              <button
                v-for="account in accounts"
                :key="account.key"
                type="button"
                class="account-row"
                :class="{ active: account.key === currentKey }"
                @click="handleSwitch(account)"
              >
                <span class="account-avatar">{{ initialOf(account) }}</span>
                <span class="account-row-copy">
                  <strong>{{ account.email || account.username }}</strong>
                  <small>{{ account.nickname }}</small>
                </span>
                <el-icon v-if="account.key === currentKey" class="account-check"><Check /></el-icon>
              </button>
              <button type="button" class="account-add" @click="addAccount">
                <el-icon><Plus /></el-icon>
                添加账号
              </button>
            </div>
          </div>
        </el-popover>
      </div>
    </aside>

    <aside v-if="isConsole" class="console-nav">
      <div class="console-title">控制台</div>
      <router-link
        v-for="item in consoleMenus"
        :key="item.path"
        :to="item.path"
        class="console-item"
        :class="{ active: isActive(item.path) }"
      >
        <el-icon><component :is="item.icon" /></el-icon>
        <span>{{ item.title }}</span>
      </router-link>
    </aside>

    <div class="main">
      <button type="button" class="credit-chip" title="积分余额" @click="router.push('/console/wallet')">
        <el-icon><Coin /></el-icon>
        <span class="amount">{{ creditBalance.toFixed(2) }}</span>
        <span class="unit">积分</span>
      </button>

      <div class="viewport">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <!--
              刻意不加 :key：工具路由与 chat/:app 都复用同一组件实例，
              加 key 会让对话切换应用时整体重建、丢失会话状态。
              工具切换时的定时器与预扣费由 create/index.vue 的 watch(setup) 主动收尾。
            -->
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </div>

    <transition name="flyout">
      <div v-if="activeGroup" class="group-layer" @click="closeGroup">
        <aside class="group-panel" @click.stop>
          <header class="group-head">
            <div class="group-head-copy">
              <span class="group-eyebrow">{{ activeGroup.label }} · {{ activeGroup.items.length }} 款应用</span>
              <h2>{{ activeGroup.title }}</h2>
              <p>{{ activeGroup.description }}</p>
            </div>
            <button type="button" class="group-close" aria-label="关闭" @click="closeGroup">
              <el-icon><Close /></el-icon>
            </button>
          </header>

          <div class="group-list">
            <button
              v-for="item in activeGroup.items"
              :key="item.key"
              type="button"
              class="group-app"
              :class="{ active: isItemActive(item) }"
              @click="openApp(item)"
            >
              <span class="group-app-icon">
                <AppIcon :name="item.key" :alt="item.title" />
              </span>
              <span class="group-app-main">
                <span class="group-app-title">{{ item.title }}</span>
                <span class="group-app-desc">{{ item.description }}</span>
                <span class="group-app-models">{{ modelSummary(item) }}</span>
              </span>
              <el-icon class="group-app-go"><Right /></el-icon>
            </button>
          </div>
        </aside>
      </div>
    </transition>

    <SettingsDialog v-model="settingsVisible" />
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import logo from '@/assets/logo.svg'
import AppIcon from '@/components/AppIcon.vue'
import SettingsDialog from '@/components/SettingsDialog.vue'
import { chatApps, modelOptionsOf, tools } from '@/config/tools'
import { getAccounts, rememberAccount, switchAccount } from '@/utils/auth'
import { loadWallet } from '@/mock/account'
import { optionNames } from '@/utils/model-catalog'

const route = useRoute()
const router = useRouter()
const store = useStore()
const settingsVisible = ref(false)
const creditBalance = ref(loadWallet().balance)
const activeGroupKey = ref('')
const accountMenuVisible = ref(false)
const switchVisible = ref(false)
const accounts = ref(getAccounts())

const accountEmail = computed(() => {
  const profile = store.state.user.profile || {}
  return profile.email || profile.username || '未登录'
})

const currentKey = computed(() => {
  const profile = store.state.user.profile || {}
  return String(profile.id ?? profile.username ?? profile.email ?? '')
})

// 左侧大类分片：对话、图片、视频、音乐。
// maestro（自动成片）本质是视频工具，归入视频组。
const railGroups = [
  {
    key: 'chat',
    label: '对话',
    title: '对话应用',
    description: '与主流大模型对话，写作、分析、推理都在这里。',
    icon: 'ChatDotRound',
    items: chatApps
  },
  {
    key: 'image',
    label: '图片',
    title: '图片生成',
    description: '文生图、改图、海报与商业视觉设计。',
    icon: 'Picture',
    items: tools.filter(item => item.category === 'image')
  },
  {
    key: 'video',
    label: '视频',
    title: '视频创作',
    description: '文生视频、图生视频、成片与数字人口播。',
    icon: 'VideoCamera',
    items: tools.filter(item => item.category === 'video' || item.category === 'maestro')
  },
  {
    key: 'music',
    label: '音乐',
    title: '音乐生成',
    description: '从风格、歌词到完整歌曲与人声。',
    icon: 'Headset',
    items: tools.filter(item => item.category === 'music')
  }
]

const activeGroup = computed(() => railGroups.find(group => group.key === activeGroupKey.value) || null)

const consoleMenus = [
  { path: '/console/overview', title: '概览', icon: 'Odometer' },
  { path: '/console/usages', title: '使用历史', icon: 'DataLine' },
  { path: '/console/wallet', title: '钱包与充值', icon: 'Wallet' },
  { path: '/console/subscription', title: '订阅套餐', icon: 'Medal' },
  { path: '/console/orders', title: '订单列表', icon: 'Tickets' },
  { path: '/console/coupons', title: '兑换与优惠券', icon: 'Present' },
  { path: '/console/connectors', title: '连接器', icon: 'Connection' },
  { path: '/console/skills', title: '技能', icon: 'MagicStick' },
  { path: '/console/browser-devices', title: '浏览器设备', icon: 'Monitor' },
  { path: '/console/earnings', title: '推荐收益', icon: 'Coin' }
]

const isConsole = computed(() => route.path.startsWith('/console'))

function isActive(path) {
  return route.path === path || route.path.startsWith(`${path}/`)
}

function isGroupActive(group) {
  if (group.key === 'chat') return route.path.startsWith('/chat/')
  return group.items.some(item => route.path === item.path)
}

function isItemActive(item) {
  if (item.key === route.params.app) return true
  return route.path === item.path
}

function modelSummary(item) {
  const fallback = modelOptionsOf(item).map(entry => entry.name)
  const list = optionNames(item.key, fallback)
  if (!list.length) return item.model || ''
  return `${list.length} 个模型 · ${list[0]}`
}

function toggleGroup(key) {
  activeGroupKey.value = activeGroupKey.value === key ? '' : key
}

function closeGroup() {
  activeGroupKey.value = ''
}

function openApp(item) {
  activeGroupKey.value = ''
  if (route.path !== item.path) router.push(item.path)
}

function handleKeydown(event) {
  if (event.key === 'Escape') closeGroup()
}

function syncCredit() {
  creditBalance.value = loadWallet().balance
}

watch(
  () => route.path,
  () => {
    closeGroup()
    syncCredit()
  }
)

onMounted(() => window.addEventListener('lingxi:wallet-changed', syncCredit))
onBeforeUnmount(() => window.removeEventListener('lingxi:wallet-changed', syncCredit))

watch(
  () => store.state.user.profile,
  profile => {
    if (profile) accounts.value = rememberAccount(profile)
  }
)

onMounted(() => {
  if (store.state.user.profile) {
    accounts.value = rememberAccount(store.state.user.profile)
  } else {
    store.dispatch('user/fetchProfile').catch(() => {
      // 静默失败会让头部一直显示「未登录」，这里明确提示一次
      ElMessage.warning('登录信息加载失败，请检查网络或重新登录')
    })
  }
  window.addEventListener('keydown', handleKeydown)
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleKeydown)
})

function initialOf(account) {
  return (account.nickname || account.email || account.username || '?').slice(0, 1)
}

function handleSwitch(account) {
  if (account.key === currentKey.value) return
  if (!switchAccount(account.key)) {
    ElMessage.warning('该账号登录状态已失效，请重新登录')
    return
  }
  window.location.reload()
}

function addAccount() {
  accountMenuVisible.value = false
  router.push({ path: '/login', query: { add: '1', redirect: route.fullPath } })
}

function handleCommand(command) {
  accountMenuVisible.value = false
  if (command === 'settings') {
    settingsVisible.value = true
    return
  }
  if (command === 'profile') {
    router.push('/account/profile')
    return
  }
  if (command === 'security') {
    router.push('/account/security')
    return
  }
  if (command === 'notifications') {
    router.push('/account/notifications')
    return
  }
  if (command === 'earnings') {
    router.push('/console/earnings')
    return
  }
  if (command === 'console') {
    router.push('/console/overview')
    return
  }
  if (command === 'logout') {
    store.dispatch('user/logout')
    router.push('/login')
  }
}
</script>

<style scoped>
.app-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
  background: var(--app-bg);
}

/* ---------- 左侧图标栏 ---------- */
.rail {
  width: var(--app-rail-width);
  flex-shrink: 0;
  border-right: 1px solid var(--app-border);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 14px 0 16px;
  background: #0a0c10;
  position: relative;
  z-index: 100;
}

.rail-logo {
  width: 38px;
  height: 38px;
  display: block;
  margin-bottom: 18px;
}

.rail-logo img {
  width: 100%;
  height: 100%;
  border-radius: 11px;
}

.rail-nav {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  width: 100%;
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  scrollbar-width: none;
}

.rail-nav::-webkit-scrollbar {
  display: none;
}

.rail-item {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6e7681;
  font-size: 18px;
  background: transparent;
  border: 0;
  cursor: pointer;
  transition: all 0.16s ease;
}

.rail-item:hover {
  color: var(--app-text);
  background: rgba(255, 255, 255, 0.06);
}

.rail-item.active {
  color: #dbeafe;
  background: var(--app-primary-soft);
  box-shadow: inset 0 0 0 1px rgba(59, 130, 246, 0.35);
}

.rail-group-btn {
  width: 46px;
  height: 50px;
  flex-direction: column;
  gap: 3px;
}

.rail-group-btn .el-icon {
  font-size: 19px;
}

.rail-group-btn b {
  font-size: 10px;
  font-weight: 500;
  letter-spacing: 0.02em;
}

.rail-divider {
  width: 26px;
  height: 1px;
  background: var(--app-border);
  margin: 8px 0;
}

.rail-bottom {
  margin-top: auto;
  flex-shrink: 0;
}

.rail-avatar {
  width: 34px;
  height: 34px;
  padding: 0;
  border-radius: 50%;
  border: 1px solid var(--app-border-strong);
  background: #0b1220;
  cursor: pointer;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.16s ease;
}

.rail-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transform: scale(1.55);
}

.rail-avatar:hover {
  transform: scale(1.06);
}

/* ---------- 大类应用浮层 ---------- */
.group-layer {
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  left: var(--app-rail-width);
  z-index: 90;
  background: rgba(4, 6, 10, 0.5);
  backdrop-filter: blur(2px);
}

.group-panel {
  position: absolute;
  top: 0;
  bottom: 0;
  left: 0;
  width: 400px;
  max-width: calc(100vw - var(--app-rail-width));
  display: flex;
  flex-direction: column;
  background:
    radial-gradient(420px 220px at 0% 0%, rgba(59, 130, 246, 0.16), transparent 70%),
    #0a0d12;
  border-right: 1px solid var(--app-border-strong);
  box-shadow: 24px 0 60px rgba(0, 0, 0, 0.45);
}

.group-head {
  flex-shrink: 0;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 26px 22px 18px;
  border-bottom: 1px solid var(--app-border);
}

.group-head-copy {
  min-width: 0;
}

.group-eyebrow {
  display: inline-block;
  margin-bottom: 9px;
  color: #7cb2ff;
  font-size: 11px;
  letter-spacing: 0.08em;
}

.group-head h2 {
  margin: 0 0 7px;
  font-size: 21px;
  letter-spacing: -0.02em;
}

.group-head p {
  margin: 0;
  color: var(--app-muted);
  font-size: 12px;
  line-height: 1.65;
}

.group-close {
  flex-shrink: 0;
  width: 28px;
  height: 28px;
  border-radius: 8px;
  border: 1px solid var(--app-border);
  background: transparent;
  color: var(--app-muted);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.16s ease;
}

.group-close:hover {
  color: var(--app-text);
  background: rgba(255, 255, 255, 0.06);
}

.group-list {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.group-app {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 13px;
  padding: 11px 12px;
  border: 1px solid transparent;
  border-radius: 12px;
  background: transparent;
  text-align: left;
  cursor: pointer;
  transition: all 0.16s ease;
}

.group-app:hover {
  background: rgba(255, 255, 255, 0.045);
  border-color: var(--app-border);
}

.group-app.active {
  background: var(--app-primary-soft);
  border-color: rgba(59, 130, 246, 0.32);
}

.group-app-icon {
  width: 46px;
  height: 46px;
  flex-shrink: 0;
  border-radius: 50%;
  overflow: hidden;
  border: 1px solid var(--app-border-strong);
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.35);
}

.group-app-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.group-app-title {
  color: var(--app-text);
  font-size: 13.5px;
  font-weight: 600;
}

.group-app-desc {
  color: var(--app-muted);
  font-size: 11.5px;
  line-height: 1.5;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.group-app-models {
  color: #6b7b93;
  font-size: 10.5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.group-app-go {
  flex-shrink: 0;
  color: #5b636e;
  font-size: 13px;
  transition: all 0.16s ease;
}

.group-app:hover .group-app-go,
.group-app.active .group-app-go {
  color: #7cb2ff;
  transform: translateX(2px);
}

.flyout-enter-active,
.flyout-leave-active {
  transition: opacity 0.18s ease;
}

.flyout-enter-from,
.flyout-leave-to {
  opacity: 0;
}

.flyout-enter-active .group-panel,
.flyout-leave-active .group-panel {
  transition: transform 0.22s ease;
}

.flyout-enter-from .group-panel,
.flyout-leave-to .group-panel {
  transform: translateX(-14px);
}

/* ---------- 控制台二级导航 ---------- */
.console-nav {
  width: var(--app-console-width);
  flex-shrink: 0;
  border-right: 1px solid var(--app-border);
  background: #0b0e12;
  padding: 24px 12px;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.console-title {
  padding: 0 12px 14px;
  color: #5b636e;
  font-size: 11px;
  letter-spacing: 0.12em;
}

.console-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 12px;
  border-radius: 8px;
  color: var(--app-muted);
  font-size: 13px;
  border-right: 2px solid transparent;
  transition: all 0.16s ease;
}

.console-item:hover {
  color: var(--app-text);
  background: rgba(255, 255, 255, 0.04);
}

.console-item.active {
  color: #dbeafe;
  background: var(--app-primary-soft);
  border-right-color: #60a5fa;
}

.console-item .el-icon {
  font-size: 15px;
}

/* ---------- 主区域 ---------- */
.main {
  flex: 1;
  min-width: 0;
  position: relative;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.viewport {
  flex: 1;
  overflow: auto;
}

.credit-chip {
  position: fixed;
  top: 20px;
  right: 26px;
  z-index: 40;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 32px;
  padding: 0 13px;
  border-radius: 999px;
  border: 1px solid var(--app-border-strong);
  background: rgba(13, 17, 23, 0.88);
  backdrop-filter: blur(10px);
  color: var(--app-text);
  cursor: pointer;
  font-size: 12px;
}

.credit-chip:hover {
  border-color: rgba(96, 165, 250, 0.55);
}

.credit-chip .el-icon {
  color: #7cb2ff;
  font-size: 14px;
}

.credit-chip .amount {
  font-family: "JetBrains Mono", Consolas, monospace;
  font-weight: 600;
}

.credit-chip .unit {
  color: var(--app-muted);
  font-size: 11px;
}

@media (max-width: 900px) {
  .console-nav {
    display: none;
  }

  .credit-chip {
    top: 14px;
    right: 14px;
  }

  .group-panel {
    width: 340px;
  }
}
</style>

<style>
/* 账号菜单（popover 挂载在 body，需全局样式） */
.account-popper.el-popover {
  padding: 6px;
  border-radius: 14px;
  border: 1px solid var(--app-border);
  background: #14161c;
  box-shadow: 0 18px 44px rgba(0, 0, 0, 0.5);
  overflow: visible;
}

.account-menu {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.account-head {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 9px 10px;
  border: 0;
  border-radius: 9px;
  background: transparent;
  color: var(--app-text);
  font-size: 12.5px;
  cursor: pointer;
  text-align: left;
  transition: background 0.14s ease;
}

.account-head:hover,
.account-head.active {
  background: rgba(255, 255, 255, 0.06);
}

.account-mail {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.account-switch-hint {
  color: var(--app-muted);
  font-size: 11px;
  white-space: nowrap;
}

.account-caret {
  color: var(--app-muted);
  font-size: 11px;
}

.account-item {
  display: flex;
  align-items: center;
  gap: 9px;
  width: 100%;
  height: 34px;
  padding: 0 10px;
  border: 0;
  border-radius: 8px;
  background: transparent;
  color: var(--app-text);
  font-size: 12.5px;
  cursor: pointer;
  text-align: left;
  transition: background 0.14s ease;
}

.account-item:hover {
  background: rgba(255, 255, 255, 0.06);
}

.account-item .el-icon {
  color: var(--app-muted);
  font-size: 15px;
}

.account-item.danger:hover,
.account-item.danger:hover .el-icon {
  color: #f87171;
}

.account-divider {
  height: 1px;
  margin: 4px 6px;
  background: var(--app-border);
}

.account-switch {
  position: absolute;
  top: 0;
  left: calc(100% + 10px);
  width: 232px;
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 10px;
  border: 1px solid var(--app-border);
  border-radius: 14px;
  background: #14161c;
  box-shadow: 0 18px 44px rgba(0, 0, 0, 0.5);
}

.account-switch-title {
  padding: 2px 6px 6px;
  color: var(--app-text);
  font-size: 12px;
  font-weight: 600;
}

.account-switch-label {
  padding: 0 6px 4px;
  color: var(--app-muted);
  font-size: 10.5px;
  letter-spacing: 0.06em;
}

.account-row {
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 7px 8px;
  border: 0;
  border-radius: 9px;
  background: transparent;
  color: var(--app-text);
  cursor: pointer;
  text-align: left;
  transition: background 0.14s ease;
}

.account-row:hover {
  background: rgba(255, 255, 255, 0.06);
}

.account-row.active {
  background: var(--app-primary-soft);
}

.account-avatar {
  display: grid;
  flex-shrink: 0;
  place-items: center;
  width: 26px;
  height: 26px;
  border: 1px solid rgba(96, 165, 250, 0.3);
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.45), rgba(29, 78, 216, 0.22));
  color: #dbeafe;
  font-size: 11px;
  font-weight: 600;
}

.account-row-copy {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 1px;
  line-height: 1.35;
}

.account-row-copy strong {
  overflow: hidden;
  font-size: 12px;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.account-row-copy small {
  color: var(--app-muted);
  font-size: 10.5px;
}

.account-check {
  flex-shrink: 0;
  color: #60a5fa;
  font-size: 13px;
}

.account-add {
  display: flex;
  align-items: center;
  gap: 7px;
  margin-top: 4px;
  padding: 8px;
  border: 1px dashed var(--app-border-strong);
  border-radius: 9px;
  background: transparent;
  color: var(--app-muted);
  font-size: 12px;
  cursor: pointer;
  transition: all 0.14s ease;
}

.account-add:hover {
  border-style: solid;
  background: rgba(255, 255, 255, 0.05);
  color: var(--app-text);
}
</style>
