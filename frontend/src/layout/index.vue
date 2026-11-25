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
      <button v-if="isDev" type="button" class="credit-chip" title="积分余额" @click="router.push('/console/wallet')">
        <el-icon><Coin /></el-icon>
        <span class="amount">{{ creditBalance.toFixed(2) }}</span>
        <span class="unit">积分</span>
      </button>

      <div class="viewport">
        <div v-if="showDemoNotice" class="prod-demo-tip">
          该页面仍为演示数据，写操作不会同步到后端；真实功能需接入对应接口。
        </div>
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
import { consoleMenus, createRailGroups } from './menu'

const route = useRoute()
const router = useRouter()
const store = useStore()
const isDev = import.meta.env.DEV
// 这些页面仍直接使用本地 Mock 账本/资料，生产环境必须显式提示
const demoOnlyPrefixes = [
  '/console/wallet',
  '/console/subscription',
  '/console/coupons',
  '/console/overview',
  '/console/usage',
  '/console/earnings',
  '/console/browser-devices',
  '/account/profile',
  '/account/security',
  '/chat',
  '/create'
]
const showDemoNotice = computed(() => !isDev && demoOnlyPrefixes.some(path => route.path.startsWith(path)))
const settingsVisible = ref(false)
const creditBalance = ref(isDev ? loadWallet().balance : 0)
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

const railGroups = createRailGroups(tools, chatApps)

const activeGroup = computed(() => railGroups.find(group => group.key === activeGroupKey.value) || null)

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
  if (!isDev) return
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

<style scoped src="./index.scss"></style>

<style src="./index.global.scss"></style>
