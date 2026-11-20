<template>
  <div class="admin-page">
    <PageHeader
      title="平台接入"
      description="管理上游通道：接口地址、密钥、限速与超时；模型条目的名称、图标与定价在「模型管理」中维护。"
      flush
    >
      <el-button round @click="goModels">
        <el-icon><Cpu /></el-icon>
        模型管理
      </el-button>
      <el-button type="primary" round @click="openCreate">
        <el-icon><Plus /></el-icon>
        接入新平台
      </el-button>
    </PageHeader>

    <section class="stat-grid">
      <StatCard label="接入平台" :value="providers.length" icon="Connection" :hint="`${stats.enabled} 个已启用`" />
      <StatCard label="今日调用" :value="formatNumber(stats.todayCalls)" icon="DataLine" hint="全部平台累计请求" />
      <StatCard label="平均延迟" :value="stats.avgLatency" unit="ms" icon="Timer" hint="启用平台的加权响应" />
      <StatCard label="降速中" :value="stats.throttled" icon="Warning" hint="已限制请求速率的平台" />
    </section>

    <section class="filters panel">
      <el-input v-model="keyword" class="filter-search" placeholder="搜索平台、厂商或模型" clearable>
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-select v-model="status" class="filter-item">
        <el-option label="全部状态" value="all" />
        <el-option label="已启用" value="enabled" />
        <el-option label="已停用" value="disabled" />
      </el-select>
      <el-select v-model="speedTier" class="filter-item">
        <el-option label="全部速率档位" value="all" />
        <el-option v-for="tier in providerSpeedTiers" :key="tier.value" :label="tier.label" :value="tier.value" />
      </el-select>
      <el-button round @click="resetFilters">重置筛选</el-button>
    </section>

    <section class="panel table-panel">
      <div class="table-head">
        <h3>模型接入平台</h3>
        <span>共 {{ filtered.length }} 个平台 · 调节档位立即生效</span>
      </div>
      <el-table :data="paged">
        <el-table-column label="平台" min-width="190">
          <template #default="{ row }">
            <div class="user-cell">
              <span
                class="platform-logo"
                :style="{ background: `${row.color}22`, borderColor: `${row.color}66`, color: row.color }"
              >
                {{ row.letter }}
              </span>
              <span class="user-cell-main">
                <strong>{{ row.name }}</strong>
                <span>{{ row.vendor }}</span>
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="接入模型" min-width="240">
          <template #default="{ row }">
            <div v-if="row.models.length" class="model-tags">
              <em v-for="model in row.models.slice(0, 2)" :key="model">{{ model }}</em>
              <em v-if="row.models.length > 2" class="more">+{{ row.models.length - 2 }}</em>
            </div>
            <span v-else class="cell-muted">待绑定模型</span>
          </template>
        </el-table-column>
        <el-table-column label="速率档位" width="272">
          <template #default="{ row }">
            <div class="speed-switch">
              <button
                v-for="tier in providerSpeedTiers"
                :key="tier.value"
                type="button"
                :class="[tier.value, { active: row.speedTier === tier.value }]"
                :title="`${tier.label}：${tier.rpm} RPM / ${tier.concurrency} 并发`"
                @click="changeTier(row, tier)"
              >
                {{ tier.label }}
              </button>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="限流参数" width="132">
          <template #default="{ row }">
            <span class="money">{{ row.rpm }} RPM</span>
            <span class="cell-sub">并发 {{ row.concurrency }}</span>
          </template>
        </el-table-column>
        <el-table-column label="今日调用" width="104">
          <template #default="{ row }"><span class="money">{{ formatNumber(row.todayCalls) }}</span></template>
        </el-table-column>
        <el-table-column label="平均延迟" width="100">
          <template #default="{ row }">
            <span :class="['latency', latencyLevel(row.latency)]">{{ row.latency ? `${row.latency} ms` : '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="成功率" width="92">
          <template #default="{ row }">{{ Number(row.successRate).toFixed(1) }}%</template>
        </el-table-column>
        <el-table-column label="状态" width="82">
          <template #default="{ row }">
            <el-switch v-model="row.enabled" @change="onToggle(row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openConfig(row)">配置</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="table-empty">没有匹配的接入平台</div>
        </template>
      </el-table>
      <TablePager v-model:page="page" :total="filtered.length" />
    </section>

    <el-drawer v-model="drawerVisible" title="接入配置" size="480px" append-to-body>
      <template v-if="activeProvider">
        <div class="platform-head">
          <span
            class="platform-logo big"
            :style="{ background: `${activeProvider.color}22`, borderColor: `${activeProvider.color}66`, color: activeProvider.color }"
          >
            {{ activeProvider.letter }}
          </span>
          <div class="platform-head-main">
            <strong>{{ activeProvider.name }}</strong>
            <span>{{ activeProvider.vendor }} · 最近更新 {{ activeProvider.updatedAt }}</span>
          </div>
          <el-tag :type="activeProvider.enabled ? 'success' : 'info'" size="small" effect="plain">
            {{ activeProvider.enabled ? '启用中' : '已停用' }}
          </el-tag>
        </div>

        <el-form label-position="top" class="provider-form">
          <el-form-item label="接口地址">
            <el-input v-model="form.apiBase" placeholder="https://api.example.com/v1" />
          </el-form-item>
          <el-form-item label="API Key">
            <el-input v-model="form.apiKey" type="password" show-password placeholder="请输入平台密钥" />
          </el-form-item>
          <el-form-item label="速率档位">
            <el-select v-model="form.speedTier" @change="onFormTierChange">
              <el-option
                v-for="tier in providerSpeedTiers"
                :key="tier.value"
                :label="`${tier.label} · ${tier.rpm} RPM / ${tier.concurrency} 并发`"
                :value="tier.value"
              />
            </el-select>
          </el-form-item>
          <div class="form-row">
            <el-form-item label="每分钟请求数（RPM）">
              <el-input-number v-model="form.rpm" :min="10" :max="10000" :step="10" controls-position="right" />
            </el-form-item>
            <el-form-item label="最大并发">
              <el-input-number v-model="form.concurrency" :min="1" :max="500" controls-position="right" />
            </el-form-item>
          </div>
          <div class="form-row">
            <el-form-item label="超时时间（秒）">
              <el-input-number v-model="form.timeout" :min="10" :max="300" :step="10" controls-position="right" />
            </el-form-item>
            <el-form-item label="失败重试次数">
              <el-input-number v-model="form.retries" :min="0" :max="5" controls-position="right" />
            </el-form-item>
          </div>
          <el-form-item label="接入模型">
            <el-select
              v-model="form.models"
              multiple
              filterable
              allow-create
              default-first-option
              placeholder="输入模型名称后回车添加"
            >
              <el-option v-for="model in activeProvider.models" :key="model" :label="model" :value="model" />
            </el-select>
          </el-form-item>
          <el-form-item label="平台状态">
            <el-switch v-model="form.enabled" active-text="启用" inactive-text="停用" />
          </el-form-item>
        </el-form>

        <div class="drawer-foot">
          <el-button round @click="drawerVisible = false">取消</el-button>
          <el-button type="primary" round @click="saveConfig">保存配置</el-button>
        </div>
      </template>
    </el-drawer>

    <el-dialog v-model="createVisible" title="接入新平台" width="480px" append-to-body>
      <el-form label-position="top" class="provider-form">
        <el-form-item label="平台名称" required>
          <el-input v-model="createForm.name" placeholder="例如：Mistral AI" />
        </el-form-item>
        <el-form-item label="厂商">
          <el-input v-model="createForm.vendor" placeholder="例如：Mistral" />
        </el-form-item>
        <el-form-item label="接口地址">
          <el-input v-model="createForm.apiBase" placeholder="https://api.example.com/v1" />
        </el-form-item>
        <el-form-item label="API Key">
          <el-input v-model="createForm.apiKey" type="password" show-password placeholder="请输入平台密钥" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button round @click="createVisible = false">取消</el-button>
        <el-button type="primary" round @click="submitCreate">确认接入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import PageHeader from '@/components/PageHeader.vue'
import StatCard from '@/components/StatCard.vue'
import TablePager from '@/components/TablePager.vue'
import { loadAdminProviders, providerSpeedTiers, saveAdminProviders } from '@/mock/admin'
import { matchKeyword } from '@/utils/text'

const router = useRouter()

function goModels() {
  const base = `/${import.meta.env.VITE_ADMIN_PATH || 'lingxi-console-7c3f'}`
  router.push(`${base}/models`)
}

const providers = ref(loadAdminProviders())
const keyword = ref('')
const status = ref('all')
const speedTier = ref('all')
const drawerVisible = ref(false)
const activeProvider = ref(null)
const createVisible = ref(false)
const page = ref(1)
const pageSize = 10

const form = reactive({
  apiBase: '',
  apiKey: '',
  speedTier: 'standard',
  rpm: 300,
  concurrency: 20,
  timeout: 90,
  retries: 2,
  models: [],
  enabled: true
})

const createForm = reactive({
  name: '',
  vendor: '',
  apiBase: '',
  apiKey: ''
})

const filtered = computed(() => {
  const text = keyword.value.trim().toLowerCase()
  return providers.value.filter(item => {
    // models 可能是 null 或含非字符串元素，交给 textOf 逐项归一后再匹配
    const hit = matchKeyword(text, item.name, item.vendor, ...(Array.isArray(item.models) ? item.models : []))
    const matchStatus =
      status.value === 'all' || (status.value === 'enabled' ? item.enabled : !item.enabled)
    const matchSpeed = speedTier.value === 'all' || item.speedTier === speedTier.value
    return hit && matchStatus && matchSpeed
  })
})

const paged = computed(() =>
  filtered.value.slice((page.value - 1) * pageSize, page.value * pageSize)
)

watch([keyword, status, speedTier], () => {
  page.value = 1
})

const stats = computed(() => {
  const enabled = providers.value.filter(item => item.enabled)
  const avgLatency = enabled.length
    ? Math.round(enabled.reduce((sum, item) => sum + Number(item.latency || 0), 0) / enabled.length)
    : 0
  return {
    enabled: enabled.length,
    todayCalls: providers.value.reduce((sum, item) => sum + Number(item.todayCalls || 0), 0),
    avgLatency,
    throttled: providers.value.filter(item => item.speedTier === 'slow').length
  }
})

function formatNumber(value) {
  return Number(value || 0).toLocaleString('zh-CN')
}

function latencyLevel(latency) {
  if (!latency) return ''
  if (latency <= 800) return 'good'
  if (latency <= 1400) return 'normal'
  return 'slow'
}

function resetFilters() {
  keyword.value = ''
  status.value = 'all'
  speedTier.value = 'all'
}

function onToggle(row) {
  row.updatedAt = dayjs().format('YYYY-MM-DD HH:mm')
  saveAdminProviders(providers.value)
  ElMessage.success(`${row.name} 已${row.enabled ? '启用' : '停用'}`)
}

function changeTier(row, tier) {
  if (row.speedTier === tier.value) return
  row.speedTier = tier.value
  row.rpm = tier.rpm
  row.concurrency = tier.concurrency
  row.updatedAt = dayjs().format('YYYY-MM-DD HH:mm')
  saveAdminProviders(providers.value)
  ElMessage.success(`${row.name} 已${tier.label}，限流调整为 ${tier.rpm} RPM / ${tier.concurrency} 并发`)
}

function openConfig(row) {
  activeProvider.value = row
  Object.assign(form, {
    apiBase: row.apiBase,
    apiKey: row.apiKey,
    speedTier: row.speedTier,
    rpm: row.rpm,
    concurrency: row.concurrency,
    timeout: row.timeout,
    retries: row.retries,
    models: [...row.models],
    enabled: row.enabled
  })
  drawerVisible.value = true
}

function onFormTierChange(value) {
  const tier = providerSpeedTiers.find(item => item.value === value)
  if (!tier) return
  form.rpm = tier.rpm
  form.concurrency = tier.concurrency
}

function saveConfig() {
  if (!activeProvider.value) return
  if (!form.apiBase.trim()) {
    ElMessage.warning('请填写接口地址')
    return
  }
  Object.assign(activeProvider.value, {
    apiBase: form.apiBase.trim(),
    apiKey: form.apiKey,
    speedTier: form.speedTier,
    rpm: form.rpm,
    concurrency: form.concurrency,
    timeout: form.timeout,
    retries: form.retries,
    models: [...form.models],
    enabled: form.enabled,
    updatedAt: dayjs().format('YYYY-MM-DD HH:mm')
  })
  saveAdminProviders(providers.value)
  drawerVisible.value = false
  ElMessage.success(`${activeProvider.value.name} 配置已保存`)
}

function openCreate() {
  Object.assign(createForm, { name: '', vendor: '', apiBase: '', apiKey: '' })
  createVisible.value = true
}

function submitCreate() {
  const name = createForm.name.trim()
  if (!name) {
    ElMessage.warning('请填写平台名称')
    return
  }
  providers.value.unshift({
    id: `provider-${Date.now()}`,
    name,
    vendor: createForm.vendor.trim() || '自定义',
    letter: name.slice(0, 1),
    color: '#3b82f6',
    enabled: false,
    speedTier: 'standard',
    rpm: 300,
    concurrency: 20,
    latency: 0,
    successRate: 100,
    apiBase: createForm.apiBase.trim(),
    apiKey: createForm.apiKey,
    timeout: 90,
    retries: 2,
    models: [],
    todayCalls: 0,
    updatedAt: dayjs().format('YYYY-MM-DD HH:mm')
  })
  saveAdminProviders(providers.value)
  createVisible.value = false
  ElMessage.success(`${name} 已接入，请补充配置后启用`)
}
</script>

<style scoped>
.platform-logo {
  width: 32px;
  height: 32px;
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--app-border);
  border-radius: 9px;
  font-size: 13px;
  font-weight: 700;
}

.platform-logo.big {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  font-size: 17px;
}

.model-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.model-tags em {
  font-style: normal;
  padding: 2px 7px;
  border-radius: 999px;
  border: 1px solid var(--app-border);
  background: rgba(255, 255, 255, 0.03);
  color: var(--app-muted);
  font-size: 10.5px;
  white-space: nowrap;
}

.model-tags em.more {
  color: #7cb2ff;
  border-color: rgba(59, 130, 246, 0.3);
  background: rgba(59, 130, 246, 0.1);
}

.cell-muted {
  color: var(--app-muted);
  font-size: 11.5px;
}

.speed-switch {
  display: inline-flex;
  gap: 2px;
  padding: 2px;
  border: 1px solid var(--app-border);
  border-radius: 9px;
  background: var(--app-surface-2);
}

.speed-switch button {
  height: 24px;
  padding: 0 9px;
  border: 0;
  border-radius: 6px;
  background: transparent;
  color: var(--app-muted);
  font-size: 11.5px;
  cursor: pointer;
  transition: all 0.16s ease;
}

.speed-switch button:hover {
  color: var(--app-text);
  background: rgba(255, 255, 255, 0.05);
}

.speed-switch button.active.slow {
  color: #fbbf24;
  background: rgba(245, 158, 11, 0.14);
  box-shadow: inset 0 0 0 1px rgba(245, 158, 11, 0.35);
}

.speed-switch button.active.standard {
  color: #93c5fd;
  background: rgba(59, 130, 246, 0.14);
  box-shadow: inset 0 0 0 1px rgba(59, 130, 246, 0.35);
}

.speed-switch button.active.fast {
  color: #34d399;
  background: rgba(16, 185, 129, 0.14);
  box-shadow: inset 0 0 0 1px rgba(16, 185, 129, 0.35);
}

.speed-switch button.active.turbo {
  color: #c084fc;
  background: rgba(168, 85, 247, 0.16);
  box-shadow: inset 0 0 0 1px rgba(168, 85, 247, 0.38);
}

.latency.good {
  color: #34d399;
}

.latency.normal {
  color: #fbbf24;
}

.latency.slow {
  color: #f87171;
}

.platform-head {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 4px 0 18px;
  border-bottom: 1px solid var(--app-border);
}

.platform-head-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.platform-head-main strong {
  font-size: 15px;
}

.platform-head-main span {
  color: var(--app-muted);
  font-size: 11.5px;
}

.provider-form {
  margin-top: 18px;
}

.provider-form :deep(.el-select),
.provider-form :deep(.el-input-number) {
  width: 100%;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.drawer-foot {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding-top: 8px;
}
</style>
