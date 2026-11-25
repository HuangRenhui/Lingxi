<template>
  <div class="page">
    <PageHeader title="通知中心" description="任务运行、账单变动与系统更新通知，未读消息会显示红点。">
      <el-button round :disabled="!unreadCount" @click="readAll">
        <el-icon><Select /></el-icon>
        全部标为已读
      </el-button>
      <el-button round @click="clearRead">
        <el-icon><Delete /></el-icon>
        清除已读
      </el-button>
    </PageHeader>

    <section class="notice-grid">
      <div class="panel notice-panel">
        <div class="notice-toolbar">
          <TabStrip v-model="tab" :items="tabs" />
          <el-input v-model="keyword" class="filter-search" placeholder="搜索通知内容" clearable>
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>

        <ul v-if="notifications.length" class="notice-list">
          <li
            v-for="item in notifications"
            :key="item.id"
            class="notice-item"
            :class="{ unread: !item.read }"
          >
            <span class="notice-icon" :class="item.category">
              <el-icon><component :is="iconOf(item.category)" /></el-icon>
            </span>
            <div class="notice-main">
              <div class="notice-title">
                <strong>{{ item.title }}</strong>
                <el-tag v-if="!item.read" type="primary" size="small" effect="plain" round>未读</el-tag>
                <el-tag size="small" effect="plain" round>{{ categoryLabel(item.category) }}</el-tag>
              </div>
              <p>{{ item.content }}</p>
              <span class="notice-time">{{ item.createdAt }}</span>
            </div>
            <div class="notice-actions">
              <el-button v-if="!item.read" link size="small" @click="markRead(item)">标为已读</el-button>
              <el-button link size="small" type="danger" @click="remove(item)">删除</el-button>
            </div>
          </li>
        </ul>

        <EmptyState v-else :text="emptyText" />
        <TablePager v-model:page="page" :page-size="pageSize" :total="total" />
      </div>

      <aside class="panel setting-panel">
        <div class="card-head">
          <h3>通知设置</h3>
          <span>关闭后将不再接收对应类型的站内通知</span>
        </div>

        <ul class="switch-list">
          <li v-for="item in prefItems" :key="item.key">
            <span class="switch-main">
              <strong>{{ item.title }}</strong>
              <span>{{ item.desc }}</span>
            </span>
            <el-switch v-model="prefs[item.key]" @change="savePrefs" />
          </li>
        </ul>

        <div class="summary">
          <div class="summary-row">
            <span>未读</span>
            <strong>{{ unreadCount }}</strong>
          </div>
          <div class="summary-row">
            <span>全部</span>
            <strong>{{ total }}</strong>
          </div>
        </div>
      </aside>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { writeJson } from '@/utils/storage'
import PageHeader from '@/components/PageHeader.vue'
import TabStrip from '@/components/TabStrip.vue'
import EmptyState from '@/components/EmptyState.vue'
import TablePager from '@/components/TablePager.vue'
import {
  deleteNotification,
  deleteNotifications,
  getUnreadCount,
  listNotifications,
  markAllNotificationsRead,
  markNotificationRead
} from '@/api/notification'

const PREF_KEY = 'lingxi-notification-prefs'
const DEFAULT_PREFS = { task: true, billing: true, system: true, marketing: false }

const notifications = ref([])
const loading = ref(false)
const tab = ref('all')
const keyword = ref('')
const page = ref(1)
const pageSize = 20
const total = ref(0)
const unreadCount = ref(0)
let filterTimer = null

/** 后端字段是 type/readFlag，页面沿用 category/read 的既有模板 */
function normalizeNotification(item) {
  // 后端返回大写 type + readFlag，Mock 旧数据是 category + read，两种都兼容
  const rawCategory = item.category || item.type || 'system'
  return {
    id: item.id,
    title: item.title || '',
    content: item.content || '',
    category: String(rawCategory).toLowerCase(),
    // 后端返回 readFlag(0/1)，Mock 返回 read(boolean)
    read: item.readFlag === undefined ? Boolean(item.read) : Number(item.readFlag) === 1,
    createdAt: item.createdAt || ''
  }
}

async function reload() {
  loading.value = true
  try {
    const result = await listNotifications({
      page: page.value,
      size: pageSize,
      unreadOnly: tab.value === 'unread',
      type: ['all', 'unread'].includes(tab.value) ? undefined : tab.value,
      keyword: keyword.value.trim() || undefined
    })
    notifications.value = (result?.list || []).map(normalizeNotification)
    total.value = Number(result?.total || 0)
  } catch (error) {
    notifications.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

async function reloadUnreadCount() {
  try {
    const result = await getUnreadCount()
    unreadCount.value = Number(result?.count ?? result ?? 0)
  } catch (error) {
    // 角标失败不影响列表
  }
}

function refresh() {
  reload()
  reloadUnreadCount()
}

onMounted(refresh)

watch([tab], () => {
  page.value = 1
  reload()
})
watch(keyword, () => {
  window.clearTimeout(filterTimer)
  filterTimer = window.setTimeout(() => {
    page.value = 1
    reload()
  }, 300)
})
watch(page, reload)

const tabs = [
  { key: 'all', label: '全部' },
  { key: 'unread', label: '未读' },
  { key: 'system', label: '系统' },
  { key: 'task', label: '任务' },
  { key: 'billing', label: '账单' }
]

const prefItems = [
  { key: 'task', title: '任务通知', desc: '定时任务开始、完成或失败时提醒' },
  { key: 'billing', title: '账单通知', desc: '充值到账、扣费失败与订阅到期' },
  { key: 'system', title: '系统通知', desc: '新模型上线、功能更新与安全提醒' },
  { key: 'marketing', title: '活动与推荐', desc: '优惠活动与运营推广消息' }
]

const prefs = reactive(loadPrefs())

const emptyText = computed(() => {
  if (tab.value === 'unread') return '没有未读通知'
  if (keyword.value.trim()) return '没有匹配的通知'
  return '还没有通知'
})

function loadPrefs() {
  try {
    const raw = localStorage.getItem(PREF_KEY)
    return raw ? { ...DEFAULT_PREFS, ...JSON.parse(raw) } : { ...DEFAULT_PREFS }
  } catch (error) {
    return { ...DEFAULT_PREFS }
  }
}

function savePrefs() {
  if (!writeJson(PREF_KEY, prefs)) {
    ElMessage.warning('本地存储不可用，通知设置仅保留到刷新前')
    return
  }
  ElMessage.success('通知设置已保存')
}

function iconOf(category) {
  return { task: 'Timer', billing: 'Wallet', system: 'Bell' }[category] || 'Bell'
}

function categoryLabel(category) {
  return { task: '任务', billing: '账单', system: '系统' }[category] || category
}

async function markRead(item) {
  if (item.read) return
  try {
    await markNotificationRead(item.id)
    refresh()
  } catch (error) {
    // 拦截器已提示，保持原状态即可
  }
}

async function readAll() {
  try {
    await markAllNotificationsRead()
    ElMessage.success('已全部标为已读')
    refresh()
  } catch (error) {
    // 拦截器已提示
  }
}

async function remove(item) {
  try {
    await deleteNotification(item.id)
    ElMessage.success('通知已删除')
    refresh()
  } catch (error) {
    // 拦截器已提示
  }
}

async function clearRead() {
  try {
    // 先按页取全部已读通知的 ID（每页最多 100 条），再一次性批量删除
    const ids = []
    let current = 1
    let pages = 1
    do {
      const result = await listNotifications({ page: current, size: 100, readOnly: true })
      ids.push(...(result?.list || []).map(item => item.id))
      pages = Math.max(1, Math.ceil(Number(result?.total || 0) / 100))
      current += 1
    } while (current <= pages)
    if (!ids.length) {
      ElMessage.info('没有已读通知需要清除')
      return
    }
    await deleteNotifications(ids)
    ElMessage.success(`已清除 ${ids.length} 条已读通知`)
    refresh()
  } catch (error) {
    // 请求层已提示
  }
}
</script>

<style scoped>
.notice-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.5fr) minmax(0, 1fr);
  gap: 14px;
  align-items: start;
}

.notice-panel {
  padding: 14px 20px 18px;
}

.notice-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.notice-toolbar :deep(.tab-strip) {
  border-bottom: 0;
}

.filter-search {
  width: 220px;
}

.notice-list {
  list-style: none;
  margin: 8px 0 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.notice-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 14px 13px;
  border-radius: 12px;
  border: 1px solid var(--app-border);
  background: var(--app-surface-2);
  transition: border-color 0.16s ease;
}

.notice-item:hover {
  border-color: var(--app-border-strong);
}

.notice-item.unread {
  border-color: rgba(96, 165, 250, 0.28);
  background: rgba(59, 130, 246, 0.07);
}

.notice-icon {
  width: 34px;
  height: 34px;
  flex-shrink: 0;
  display: grid;
  place-items: center;
  border-radius: 10px;
  font-size: 15px;
  background: rgba(255, 255, 255, 0.05);
  color: #9aa4af;
}

.notice-icon.task {
  background: rgba(52, 211, 153, 0.14);
  color: #34d399;
}

.notice-icon.billing {
  background: rgba(251, 191, 36, 0.14);
  color: #fbbf24;
}

.notice-icon.system {
  background: var(--app-primary-soft);
  color: #7cb2ff;
}

.notice-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.notice-title {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.notice-title strong {
  font-size: 13px;
  font-weight: 600;
}

.notice-main p {
  margin: 0;
  color: var(--app-muted);
  font-size: 12px;
  line-height: 1.65;
}

.notice-time {
  color: #6b7b93;
  font-size: 10.5px;
}

.notice-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
  flex-shrink: 0;
}

.notice-actions :deep(.el-button) {
  margin-left: 0;
  font-size: 11.5px;
}

.setting-panel {
  padding: 20px 22px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.card-head h3 {
  margin: 0 0 3px;
  font-size: 14px;
  font-weight: 600;
}

.card-head span {
  color: var(--app-muted);
  font-size: 11.5px;
}

.switch-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.switch-list li {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 11px 12px;
  border-radius: 11px;
  border: 1px solid var(--app-border);
  background: var(--app-surface-2);
}

.switch-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.switch-main strong {
  font-size: 12.5px;
  font-weight: 600;
}

.switch-main span {
  color: var(--app-muted);
  font-size: 11px;
}

.summary {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  padding-top: 14px;
  border-top: 1px solid var(--app-border);
}

.summary-row {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 10px 12px;
  border-radius: 10px;
  background: var(--app-surface-2);
  border: 1px solid var(--app-border);
}

.summary-row span {
  color: var(--app-muted);
  font-size: 11px;
}

.summary-row strong {
  font-size: 18px;
  font-weight: 700;
}

@media (max-width: 1000px) {
  .notice-grid {
    grid-template-columns: 1fr;
  }

  .filter-search {
    width: 100%;
  }
}
</style>
