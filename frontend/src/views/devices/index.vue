<template>
  <div class="page">
    <PageHeader title="浏览器设备" description="配对一个本地浏览器，供 Agent 执行需要登录态的本机任务。">
      <el-button round type="primary" @click="openPairing">
        <el-icon><Plus /></el-icon>
        配对设备
      </el-button>
    </PageHeader>

    <section class="stat-grid">
      <StatCard label="已配对设备" :value="devices.length" icon="Monitor" hint="仅本机可信浏览器" />
      <StatCard label="在线设备" :value="onlineCount" icon="Connection" hint="最近 5 分钟有心跳" />
      <StatCard label="本机任务" :value="localTaskCount" icon="Timer" hint="运行位置为本机的任务" />
      <StatCard label="沙箱模式" :value="sandboxCount" icon="Lock" hint="不读取 Cookie 与登录态" />
    </section>

    <section v-if="devices.length" class="panel table-panel">
      <div class="table-head">
        <h3>设备列表</h3>
        <span>解除绑定后，本机任务将无法执行</span>
      </div>
      <el-table :data="paged" size="small">
        <el-table-column label="设备" min-width="200">
          <template #default="{ row }">
            <div class="device-cell">
              <span class="device-icon"><el-icon><Monitor /></el-icon></span>
              <span class="device-main">
                <strong>{{ row.name }}</strong>
                <span>{{ row.os }} · {{ row.browser }}</span>
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ONLINE' ? 'success' : 'info'" size="small" effect="plain">
              {{ row.status === 'ONLINE' ? '在线' : '离线' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastActiveAt" label="最近心跳" width="160" />
        <el-table-column prop="pairedAt" label="配对时间" width="160" />
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <div class="actions">
              <el-button link size="small" @click="renew(row)">重新配对</el-button>
              <el-button link size="small" type="danger" @click="unbind(row)">解除绑定</el-button>
            </div>
          </template>
        </el-table-column>
        <template #empty>
          <div class="table-empty">暂无已配对的浏览器设备</div>
        </template>
      </el-table>
      <TablePager v-model:page="page" :total="devices.length" />
    </section>

    <div v-else class="panel">
      <EmptyState text="暂无已配对的浏览器设备">
        <el-button round type="primary" @click="openPairing">配对当前浏览器</el-button>
      </EmptyState>
    </div>

    <section class="panel note-panel">
      <div class="note-head">
        <el-icon><InfoFilled /></el-icon>
        <h3>配对说明</h3>
      </div>
      <ul class="note-list">
        <li>配对码一次性有效，10 分钟内未提交设备身份自动失效。</li>
        <li>本机通道只执行你明确授权的任务，不会浏览其他页面。</li>
        <li>关闭浏览器或扩展后设备会变为离线，任务将跳过本轮。</li>
      </ul>
    </section>

    <el-dialog v-model="pairingVisible" title="配对浏览器" width="560px" append-to-body @closed="stopCountdown">
      <div class="notice">
        <el-icon><Warning /></el-icon>
        <span>配对仅信任这台本地浏览器设备，不会批准任何浏览器操作，也不会把 Cookie 或登录态上传到云端。</span>
      </div>

      <p class="pair-hint">打开灵犀浏览器扩展并输入此一次性配对码。扩展提交设备身份前请保持此窗口打开。</p>

      <div class="code-row">
        <code>{{ code }}</code>
        <el-button @click="copyCode">
          <el-icon><CopyDocument /></el-icon>
          复制
        </el-button>
      </div>
      <div class="countdown">配对码将在 {{ seconds }} 秒后过期</div>

      <div class="waiting">
        <el-icon class="spin"><Loading /></el-icon>
        正在等待浏览器扩展提交设备身份…
      </div>

      <template #footer>
        <el-button @click="pairingVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmPairing">已在扩展中输入，完成配对</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { readJson, writeJson } from '@/utils/storage'
import dayjs from 'dayjs'
import PageHeader from '@/components/PageHeader.vue'
import StatCard from '@/components/StatCard.vue'
import EmptyState from '@/components/EmptyState.vue'
import TablePager from '@/components/TablePager.vue'
import { runningTasks } from '@/mock/account'

const STORAGE_KEY = 'lingxi-browser-devices'

const devices = ref(loadDevices())
const page = ref(1)
const pageSize = 10
const pairingVisible = ref(false)
const code = ref('')
const seconds = ref(600)
let timer = null

const paged = computed(() =>
  devices.value.slice((page.value - 1) * pageSize, page.value * pageSize)
)
const onlineCount = computed(() => devices.value.filter(item => item.status === 'ONLINE').length)
const localTaskCount = computed(() => runningTasks.filter(item => item.location === '本机').length)
const sandboxCount = computed(() => devices.value.filter(item => item.sandbox).length)

function loadDevices() {
  const cached = readJson(STORAGE_KEY, null)
  if (Array.isArray(cached)) return cached
  const seed = [
    {
      id: 'bd-1',
      name: 'Windows · Chrome',
      os: 'Windows 11',
      browser: 'Chrome 141',
      status: 'ONLINE',
      sandbox: true,
      lastActiveAt: dayjs().subtract(2, 'minute').format('YYYY-MM-DD HH:mm'),
      pairedAt: dayjs().subtract(9, 'day').format('YYYY-MM-DD HH:mm')
    }
  ]
  writeJson(STORAGE_KEY, seed)
  return seed
}

function saveDevices() {
  if (!writeJson(STORAGE_KEY, devices.value)) {
    ElMessage.warning('本地存储不可用，设备状态仅保留到刷新前')
  }
}

function randomCode() {
  const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789'
  const build = count =>
    Array.from({ length: count }, () => chars[Math.floor(Math.random() * chars.length)]).join('')
  return `${build(4)}-${build(4)}`
}

function openPairing() {
  if (!import.meta.env.DEV) {
    ElMessage.info('浏览器设备配对暂未接入后端')
    return
  }
  code.value = randomCode()
  seconds.value = 600
  pairingVisible.value = true
  timer = window.setInterval(() => {
    seconds.value = Math.max(0, seconds.value - 1)
    if (seconds.value === 0) stopCountdown()
  }, 1000)
}

function stopCountdown() {
  if (timer) {
    window.clearInterval(timer)
    timer = null
  }
}

async function copyCode() {
  try {
    await navigator.clipboard.writeText(code.value)
    ElMessage.success('配对码已复制')
  } catch (error) {
    ElMessage.warning('当前环境不支持剪贴板，请手动输入配对码')
  }
}

function confirmPairing() {
  if (!import.meta.env.DEV) {
    ElMessage.info('浏览器设备配对暂未接入后端')
    pairingVisible.value = false
    stopCountdown()
    return
  }
  const exists = devices.value.some(item => item.name === 'Windows · Chrome')
  if (exists) {
    const target = devices.value.find(item => item.name === 'Windows · Chrome')
    target.status = 'ONLINE'
    target.sandbox = true
    target.lastActiveAt = dayjs().format('YYYY-MM-DD HH:mm')
  } else {
    devices.value = [
      {
        id: `bd-${Date.now()}`,
        name: 'Windows · Chrome',
        os: 'Windows 11',
        browser: 'Chrome 141',
        status: 'ONLINE',
        sandbox: true,
        lastActiveAt: dayjs().format('YYYY-MM-DD HH:mm'),
        pairedAt: dayjs().format('YYYY-MM-DD HH:mm')
      },
      ...devices.value
    ]
  }
  saveDevices()
  pairingVisible.value = false
  stopCountdown()
  ElMessage.success('设备配对成功，本机任务可以开始执行')
}

function renew(row) {
  if (!import.meta.env.DEV) {
    ElMessage.info('设备状态刷新暂未接入后端')
    return
  }
  row.lastActiveAt = dayjs().format('YYYY-MM-DD HH:mm')
  row.status = 'ONLINE'
  saveDevices()
  ElMessage.success(`已刷新 ${row.name} 的配对状态`)
}

async function unbind(row) {
  if (!import.meta.env.DEV) {
    ElMessage.info('设备解绑暂未接入后端')
    return
  }
  try {
    await ElMessageBox.confirm(`解除绑定后 ${row.name} 将无法执行本机任务，确认继续？`, '解除绑定', {
      type: 'warning',
      confirmButtonText: '解除绑定'
    })
  } catch (error) {
    return
  }
  devices.value = devices.value.filter(item => item.id !== row.id)
  saveDevices()
  ElMessage.success('已解除绑定')
}

onBeforeUnmount(stopCountdown)
</script>

<style scoped>
.table-panel {
  padding: 16px 20px 10px;
}

.table-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.table-head h3 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
}

.table-head span {
  color: var(--app-muted);
  font-size: 11.5px;
}

.device-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.device-icon {
  width: 30px;
  height: 30px;
  flex-shrink: 0;
  display: grid;
  place-items: center;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.05);
  color: #9aa4af;
  font-size: 14px;
}

.device-main {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.device-main strong {
  font-size: 12.5px;
  font-weight: 600;
}

.device-main span {
  color: var(--app-muted);
  font-size: 11px;
}

.actions {
  display: flex;
  align-items: center;
  gap: 2px;
}

.table-empty {
  padding: 30px 0;
  color: var(--app-muted);
  font-size: 12px;
}

.note-panel {
  padding: 16px 20px 18px;
}

.note-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  color: #7cb2ff;
}

.note-head h3 {
  margin: 0;
  color: var(--app-text);
  font-size: 13.5px;
  font-weight: 600;
}

.note-list {
  margin: 0;
  padding-left: 18px;
  display: flex;
  flex-direction: column;
  gap: 7px;
  color: var(--app-muted);
  font-size: 11.5px;
  line-height: 1.7;
}

.notice {
  display: flex;
  gap: 9px;
  padding: 11px 14px;
  border-radius: 9px;
  border: 1px solid rgba(245, 158, 11, 0.28);
  background: rgba(245, 158, 11, 0.08);
  color: #fbbf24;
  font-size: 11.5px;
  line-height: 1.65;
}

.pair-hint {
  margin: 18px 0 12px;
  color: var(--app-muted);
  font-size: 12px;
  line-height: 1.7;
}

.code-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.code-row code {
  flex: 1;
  padding: 15px 18px;
  border-radius: 9px;
  background: #0a0c10;
  border: 1px solid var(--app-border-strong);
  font-size: 24px;
  font-weight: 700;
  letter-spacing: 0.22em;
  text-align: center;
  color: #dbeafe;
}

.countdown {
  margin-top: 10px;
  text-align: center;
  color: #5b636e;
  font-size: 11.5px;
}

.waiting {
  margin-top: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: var(--app-muted);
  font-size: 12px;
}

.spin {
  animation: spin 1.4s linear infinite;
  color: #7cb2ff;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
