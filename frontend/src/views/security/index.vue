<template>
  <div class="page">
    <PageHeader title="安全设置" description="管理登录密码、绑定邮箱与已登录设备。演示环境的改动仅保存在本机。" />

    <section class="security-grid">
      <article class="panel form-card">
        <div class="card-head">
          <h3>修改密码</h3>
          <span>建议使用 12 位以上、包含大小写与符号的密码</span>
        </div>

        <el-form :model="pwd" label-position="top" class="pwd-form">
          <el-form-item label="当前密码">
            <el-input v-model="pwd.current" type="password" show-password placeholder="请输入当前密码" />
          </el-form-item>
          <el-form-item label="新密码">
            <el-input v-model="pwd.next" type="password" show-password placeholder="至少 12 位，不能是纯数字" />
          </el-form-item>
          <el-form-item label="确认新密码">
            <el-input v-model="pwd.confirm" type="password" show-password placeholder="再次输入新密码" />
          </el-form-item>
        </el-form>

        <div class="strength">
          <div class="strength-bars">
            <span v-for="index in 4" :key="index" :class="{ on: strength >= index, weak: strength <= 1 }" />
          </div>
          <span class="strength-text">{{ strengthText }}</span>
        </div>

        <div class="card-foot">
          <span class="muted">上次修改：{{ security.passwordUpdatedAt }}</span>
          <el-button round type="primary" :loading="savingPwd" @click="changePassword">保存新密码</el-button>
        </div>
      </article>

      <article class="panel status-card">
        <div class="card-head">
          <h3>安全状态</h3>
          <span>按风险从高到低排列</span>
        </div>

        <ul class="status-list">
          <li>
            <span class="status-icon"><el-icon><Lock /></el-icon></span>
            <span class="status-main">
              <strong>登录密码</strong>
              <span>上次修改 {{ security.passwordUpdatedAt }}</span>
            </span>
            <el-tag type="success" size="small" effect="plain">已设置</el-tag>
          </li>
          <li>
            <span class="status-icon"><el-icon><Message /></el-icon></span>
            <span class="status-main">
              <strong>绑定邮箱</strong>
              <span>{{ profile.email }}</span>
            </span>
            <el-button link size="small" @click="rebindVisible = true">换绑</el-button>
          </li>
          <li>
            <span class="status-icon"><el-icon><Key /></el-icon></span>
            <span class="status-main">
              <strong>两步验证</strong>
              <span>{{ security.twoFactor ? '已开启，登录需输入动态验证码' : '未开启，建议开启以提升安全等级' }}</span>
            </span>
            <el-switch v-model="security.twoFactor" @change="toggleTwoFactor" />
          </li>
          <li>
            <span class="status-icon"><el-icon><Bell /></el-icon></span>
            <span class="status-main">
              <strong>异地登录提醒</strong>
              <span>新设备登录时通过邮件通知</span>
            </span>
            <el-switch v-model="security.loginAlert" @change="toggleLoginAlert" />
          </li>
        </ul>
      </article>
    </section>

    <section class="panel table-panel">
      <div class="table-head">
        <div>
          <h3>登录设备</h3>
          <span>共 {{ devices.length }} 台设备，可下线非当前设备</span>
        </div>
        <el-button round size="small" @click="offlineOthers">
          <el-icon><SwitchButton /></el-icon>
          下线其他设备
        </el-button>
      </div>

      <el-table :data="pagedDevices" size="small">
        <el-table-column label="设备" min-width="190">
          <template #default="{ row }">
            <div class="device-cell">
              <span class="device-icon"><el-icon><Monitor /></el-icon></span>
              <span class="device-main">
                <strong>
                  {{ row.name }}
                  <el-tag v-if="row.current" type="primary" size="small" effect="plain">当前设备</el-tag>
                </strong>
                <span>{{ row.os }} · {{ row.browser }}</span>
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="IP 地址" width="150" />
        <el-table-column prop="location" label="登录地" width="110" />
        <el-table-column prop="lastActiveAt" label="最近活跃" width="160" />
        <el-table-column label="操作" width="110">
          <template #default="{ row }">
            <el-button
              v-if="!row.current"
              link
              type="danger"
              size="small"
              @click="offlineDevice(row)"
            >
              下线
            </el-button>
            <span v-else class="muted">—</span>
          </template>
        </el-table-column>
        <template #empty>
          <div class="table-empty">暂无登录设备</div>
        </template>
      </el-table>
      <TablePager v-model:page="page" :total="devices.length" />
    </section>

    <section class="panel danger-card">
      <div class="danger-copy">
        <h3>危险操作</h3>
        <p>以下操作不可撤销，请确认已备份需要保留的作品与任务记录。</p>
      </div>
      <div class="danger-actions">
        <el-button round @click="clearDemoData">清空本机演示数据</el-button>
        <el-button round type="danger" plain @click="deleteAccount">注销账号</el-button>
      </div>
    </section>

    <el-dialog v-model="rebindVisible" title="换绑邮箱" width="460px">
      <el-form label-position="top">
        <el-form-item label="新邮箱">
          <el-input v-model="rebind.email" placeholder="name@example.com" />
        </el-form-item>
        <el-form-item label="验证码">
          <div class="code-row">
            <el-input v-model="rebind.code" maxlength="6" placeholder="6 位验证码" />
            <el-button round :disabled="rebind.countdown > 0" @click="sendCode">
              {{ rebind.countdown > 0 ? `${rebind.countdown}s` : '发送验证码' }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button round @click="rebindVisible = false">取消</el-button>
        <el-button round type="primary" @click="confirmRebind">确认换绑</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { deleteDevice, listDevices } from '@/api/device'
import PageHeader from '@/components/PageHeader.vue'
import TablePager from '@/components/TablePager.vue'
import {
  loadDevices,
  loadProfile,
  loadSecurity,
  resetDemoState,
  saveDevices,
  saveProfile,
  saveSecurity
} from '@/mock/account'

const router = useRouter()
const store = useStore()
const profile = reactive(loadProfile())
const security = reactive(loadSecurity())
const devices = ref([])
const deviceDemoMode = ref(false)
const page = ref(1)
const pageSize = 10
const pagedDevices = computed(() =>
  devices.value.slice((page.value - 1) * pageSize, page.value * pageSize)
)

function normalizeDevice(row) {
  return {
    id: row.id,
    name: row.name,
    os: row.os,
    browser: row.browser,
    ip: row.ip,
    location: row.location,
    current: row.currentFlag === 1,
    lastActiveAt: row.lastActiveAt ? dayjs(row.lastActiveAt).format('YYYY-MM-DD HH:mm') : '—'
  }
}

async function fetchDevices() {
  try {
    const list = await listDevices()
    if (!Array.isArray(list)) {
      deviceDemoMode.value = false
      devices.value = []
      if (import.meta.env.DEV) {
        deviceDemoMode.value = true
        devices.value = loadDevices()
      }
      return
    }
    deviceDemoMode.value = false
    devices.value = list.map(normalizeDevice)
  } catch (error) {
    deviceDemoMode.value = false
    devices.value = []
    if (import.meta.env.DEV) {
      deviceDemoMode.value = true
      devices.value = loadDevices()
    }
  }
}

onMounted(fetchDevices)
const savingPwd = ref(false)
const rebindVisible = ref(false)

const pwd = reactive({ current: '', next: '', confirm: '' })
const rebind = reactive({ email: '', code: '', countdown: 0 })

const strength = computed(() => {
  const value = pwd.next
  if (!value) return 0
  let score = 0
  if (value.length >= 12) score += 1
  if (/[a-z]/.test(value) && /[A-Z]/.test(value)) score += 1
  if (/\d/.test(value)) score += 1
  if (/[^A-Za-z0-9]/.test(value)) score += 1
  return Math.min(4, Math.max(1, score))
})

const strengthText = computed(() => ['未输入', '弱', '一般', '较强', '很强'][strength.value])

function changePassword() {
  // 真实后端尚未提供自助改密接口，生产环境不能提示“密码已更新”
  if (!import.meta.env.DEV) {
    ElMessage.info('自助改密暂未接入，请联系管理员重置密码')
    return
  }
  if (!pwd.current) {
    ElMessage.warning('请输入当前密码')
    return
  }
  if (pwd.next.length < 12 || /^\d+$/.test(pwd.next)) {
    ElMessage.warning('新密码至少 12 位，且不能是纯数字')
    return
  }
  if (pwd.next !== pwd.confirm) {
    ElMessage.error('两次输入的新密码不一致')
    return
  }
  savingPwd.value = true
  setTimeout(() => {
    security.passwordUpdatedAt = dayjs().format('YYYY-MM-DD HH:mm')
    saveSecurity({ ...security })
    savingPwd.value = false
    pwd.current = ''
    pwd.next = ''
    pwd.confirm = ''
    ElMessage.success('密码已更新')
  }, 500)
}

function toggleTwoFactor(value) {
  if (!import.meta.env.DEV) {
    security.twoFactor = !value
    ElMessage.info('两步验证暂未接入')
    return
  }
  saveSecurity({ ...security })
  ElMessage.success(value ? '已开启两步验证' : '已关闭两步验证')
}

function toggleLoginAlert(value) {
  if (!import.meta.env.DEV) {
    security.loginAlert = !value
    ElMessage.info('登录提醒暂未接入')
    return
  }
  saveSecurity({ ...security })
  ElMessage.success(value ? '已开启异地登录提醒' : '已关闭异地登录提醒')
}

// 倒计时定时器：重复点击只保留一个，卸载时统一清理
let countdownTimer = null

function stopCountdown() {
  if (countdownTimer) {
    window.clearInterval(countdownTimer)
    countdownTimer = null
  }
}

function sendCode() {
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(rebind.email)) {
    ElMessage.warning('请输入正确的邮箱地址')
    return
  }
  stopCountdown()
  rebind.countdown = 60
  ElMessage.success(`验证码已发送至 ${rebind.email}`)
  countdownTimer = window.setInterval(() => {
    rebind.countdown -= 1
    if (rebind.countdown <= 0) stopCountdown()
  }, 1000)
}

onBeforeUnmount(stopCountdown)

function confirmRebind() {
  if (!import.meta.env.DEV) {
    ElMessage.info('邮箱换绑暂未接入')
    return
  }
  if (!rebind.email || !rebind.code) {
    ElMessage.warning('请填写新邮箱与验证码')
    return
  }
  profile.email = rebind.email
  saveProfile({ ...profile })
  rebindVisible.value = false
  rebind.email = ''
  rebind.code = ''
  ElMessage.success('邮箱已换绑')
}

async function offlineDevice(row) {
  if (deviceDemoMode.value) {
    devices.value = devices.value.filter(item => item.id !== row.id)
    saveDevices(devices.value)
    ElMessage.success(`已下线 ${row.name}`)
    return
  }
  try {
    await deleteDevice(row.id)
    devices.value = devices.value.filter(item => item.id !== row.id)
    ElMessage.success(`已下线 ${row.name}`)
  } catch (error) {
    // 请求层已统一提示错误
  }
}

async function offlineOthers() {
  try {
    await ElMessageBox.confirm('将下线除当前设备外的全部登录状态，确认继续？', '下线其他设备', {
      type: 'warning',
      confirmButtonText: '全部下线'
    })
  } catch (error) {
    return
  }
  if (deviceDemoMode.value) {
    devices.value = devices.value.filter(item => item.current)
    saveDevices(devices.value)
    ElMessage.success('其他设备已全部下线')
    return
  }
  const others = devices.value.filter(item => !item.current)
  if (!others.length) {
    ElMessage.info('没有需要下线的其他设备')
    return
  }
  try {
    await Promise.all(others.map(item => deleteDevice(item.id)))
    devices.value = devices.value.filter(item => item.current)
    ElMessage.success('其他设备已全部下线')
  } catch (error) {
    await fetchDevices()
  }
}

async function clearDemoData() {
  try {
    await ElMessageBox.confirm('将清空作品、流水、订阅与通知等本机演示数据，确认继续？', '清空演示数据', {
      type: 'warning',
      confirmButtonText: '清空'
    })
  } catch (error) {
    return
  }
  resetDemoState()
  ElMessage.success('已清空，正在刷新页面')
  setTimeout(() => window.location.reload(), 600)
}

async function deleteAccount() {
  if (!import.meta.env.DEV) {
    ElMessage.info('注销账号暂未接入，请联系管理员处理')
    return
  }
  try {
    await ElMessageBox.confirm(
      '注销后本地账号将立即失效，所有演示数据会被清除。该操作不可恢复。',
      '注销账号',
      { type: 'error', confirmButtonText: '确认注销', cancelButtonText: '取消' }
    )
  } catch (error) {
    return
  }
  store.dispatch('user/logout')
  router.push('/login')
}
</script>

<style scoped>
.security-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 14px;
  align-items: start;
}

.form-card,
.status-card {
  padding: 20px 22px 18px;
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

.pwd-form :deep(.el-form-item) {
  margin-bottom: 14px;
}

.pwd-form :deep(.el-form-item__label) {
  padding-bottom: 4px;
  color: var(--app-muted);
  font-size: 12px;
}

.strength {
  display: flex;
  align-items: center;
  gap: 10px;
}

.strength-bars {
  flex: 1;
  display: flex;
  gap: 5px;
}

.strength-bars span {
  flex: 1;
  height: 5px;
  border-radius: 3px;
  background: rgba(255, 255, 255, 0.08);
  transition: background 0.16s ease;
}

.strength-bars span.on {
  background: #34d399;
}

.strength-bars span.on.weak {
  background: #f87171;
}

.strength-text {
  width: 42px;
  color: var(--app-muted);
  font-size: 11.5px;
  text-align: right;
}

.card-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--app-border);
  font-size: 11.5px;
}

.status-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.status-list li {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 11px 12px;
  border-radius: 11px;
  border: 1px solid var(--app-border);
  background: var(--app-surface-2);
}

.status-icon {
  width: 32px;
  height: 32px;
  flex-shrink: 0;
  display: grid;
  place-items: center;
  border-radius: 9px;
  background: var(--app-primary-soft);
  color: #7cb2ff;
  font-size: 14px;
}

.status-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.status-main strong {
  font-size: 12.5px;
  font-weight: 600;
}

.status-main span {
  color: var(--app-muted);
  font-size: 11px;
}

.table-panel {
  padding: 16px 20px 10px;
}

.table-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 10px;
}

.table-head h3 {
  margin: 0 0 2px;
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
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12.5px;
  font-weight: 600;
}

.device-main span {
  color: var(--app-muted);
  font-size: 11px;
}

.table-empty {
  padding: 30px 0;
  color: var(--app-muted);
  font-size: 12px;
}

.danger-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 18px 22px;
  border-color: rgba(248, 113, 113, 0.24);
}

.danger-copy h3 {
  margin: 0 0 4px;
  color: #f87171;
  font-size: 14px;
  font-weight: 600;
}

.danger-copy p {
  margin: 0;
  color: var(--app-muted);
  font-size: 11.5px;
}

.danger-actions {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}

.code-row {
  display: flex;
  gap: 10px;
  width: 100%;
}

@media (max-width: 1000px) {
  .security-grid {
    grid-template-columns: 1fr;
  }

  .danger-card {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
