<template>
  <div class="admin-page">
    <PageHeader title="支付平台" description="接入并管理平台支付渠道，统一维护商户信息、费率与回调地址。" flush>
      <el-button type="primary" round @click="openCreate">
        <el-icon><Plus /></el-icon>
        接入新渠道
      </el-button>
    </PageHeader>

    <section class="stat-grid">
      <StatCard label="今日交易额" :value="`¥${formatMoney(stats.todayAmount)}`" icon="Money" :hint="`共 ${stats.todayOrders} 笔支付`" />
      <StatCard label="接入渠道" :value="payments.length" icon="CreditCard" :hint="`${stats.enabled} 个已启用`" />
      <StatCard label="平均成功率" :value="`${stats.successRate.toFixed(1)}%`" icon="CircleCheck" hint="近 24 小时支付结果" />
      <StatCard label="累计交易额" :value="`¥${formatMoney(stats.totalAmount)}`" icon="TrendCharts" hint="全部渠道历史累计" />
    </section>

    <section class="filters panel">
      <el-input v-model="keyword" class="filter-search" placeholder="搜索渠道名称、标识或商户号" clearable>
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-select v-model="status" class="filter-item">
        <el-option label="全部状态" value="all" />
        <el-option label="已启用" value="enabled" />
        <el-option label="已停用" value="disabled" />
      </el-select>
      <el-select v-model="mode" class="filter-item">
        <el-option label="全部环境" value="all" />
        <el-option label="正式环境" value="PROD" />
        <el-option label="沙箱环境" value="SANDBOX" />
      </el-select>
      <el-button round @click="resetFilters">重置筛选</el-button>
    </section>

    <section class="panel table-panel">
      <div class="table-head">
        <h3>支付渠道</h3>
        <span>共 {{ filtered.length }} 个渠道 · {{ stats.enabled }} 个已启用</span>
      </div>
      <el-table :data="paged">
        <el-table-column label="渠道" min-width="190">
          <template #default="{ row }">
            <div class="user-cell">
              <span
                class="channel-logo"
                :style="{ background: `${row.color}22`, borderColor: `${row.color}66`, color: row.color }"
              >
                {{ row.letter || row.name.slice(0, 1) }}
              </span>
              <span class="user-cell-main">
                <strong>{{ row.name }}</strong>
                <span>{{ row.code }}</span>
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="环境" width="92">
          <template #default="{ row }">
            <el-tag :type="row.mode === 'PROD' ? 'success' : 'warning'" size="small" effect="plain">
              {{ row.mode === 'PROD' ? '正式' : '沙箱' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="merchantId" label="商户号" min-width="180" show-overflow-tooltip />
        <el-table-column label="费率" width="88">
          <template #default="{ row }">{{ Number(row.feeRate).toFixed(1) }}%</template>
        </el-table-column>
        <el-table-column prop="settleCycle" label="结算周期" width="100" />
        <el-table-column label="今日交易" width="150">
          <template #default="{ row }">
            <span class="money">¥{{ formatMoney(row.todayAmount) }}</span>
            <span class="cell-sub">{{ row.todayOrders }} 笔</span>
          </template>
        </el-table-column>
        <el-table-column label="成功率" width="94">
          <template #default="{ row }">{{ Number(row.successRate).toFixed(1) }}%</template>
        </el-table-column>
        <el-table-column label="状态" width="86">
          <template #default="{ row }">
            <el-switch v-model="row.enabled" @change="onToggle(row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="128" fixed="right">
          <template #default="{ row }">
            <div class="actions">
              <el-button link type="primary" @click="openConfig(row)">配置</el-button>
              <el-button link @click="testChannel(row)">测试</el-button>
            </div>
          </template>
        </el-table-column>
        <template #empty>
          <div class="table-empty">没有匹配的支付渠道</div>
        </template>
      </el-table>
      <TablePager v-model:page="page" :total="filtered.length" />
    </section>

    <el-drawer v-model="drawerVisible" title="渠道配置" size="460px" append-to-body>
      <template v-if="activePayment">
        <div class="channel-head">
          <span
            class="channel-logo big"
            :style="{ background: `${activePayment.color}22`, borderColor: `${activePayment.color}66`, color: activePayment.color }"
          >
            {{ activePayment.letter || activePayment.name.slice(0, 1) }}
          </span>
          <div class="channel-head-main">
            <strong>{{ activePayment.name }}</strong>
            <span>{{ activePayment.code }} · 最近更新 {{ activePayment.updatedAt }}</span>
          </div>
          <el-tag :type="activePayment.enabled ? 'success' : 'info'" size="small" effect="plain">
            {{ activePayment.enabled ? '启用中' : '已停用' }}
          </el-tag>
        </div>

        <el-form label-position="top" class="channel-form">
          <el-form-item label="商户号">
            <el-input v-model="form.merchantId" placeholder="请输入商户号" />
          </el-form-item>
          <el-form-item label="费率（%）">
            <el-input-number v-model="form.feeRate" :min="0" :max="10" :step="0.1" :precision="2" controls-position="right" />
          </el-form-item>
          <el-form-item label="结算周期">
            <el-select v-model="form.settleCycle">
              <el-option label="即时到账" value="即时到账" />
              <el-option label="T+1" value="T+1" />
              <el-option label="T+3" value="T+3" />
              <el-option label="T+7" value="T+7" />
            </el-select>
          </el-form-item>
          <el-form-item label="支付环境">
            <el-radio-group v-model="form.mode">
              <el-radio-button value="PROD">正式环境</el-radio-button>
              <el-radio-button value="SANDBOX">沙箱环境</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="回调地址">
            <el-input v-model="form.callbackUrl" placeholder="https://api.example.com/pay/notify" />
          </el-form-item>
          <el-form-item label="渠道状态">
            <el-switch v-model="form.enabled" active-text="启用" inactive-text="停用" />
          </el-form-item>
        </el-form>

        <div class="drawer-foot">
          <el-button round @click="drawerVisible = false">取消</el-button>
          <el-button type="primary" round @click="saveConfig">保存配置</el-button>
        </div>
      </template>
    </el-drawer>

    <el-dialog v-model="createVisible" title="接入新渠道" width="460px" append-to-body>
      <el-form label-position="top" class="channel-form">
        <el-form-item label="渠道名称" required>
          <el-input v-model="createForm.name" placeholder="例如：支付宝 HK" />
        </el-form-item>
        <el-form-item label="渠道标识" required>
          <el-input v-model="createForm.code" placeholder="例如：ALIPAY_HK" />
        </el-form-item>
        <el-form-item label="商户号">
          <el-input v-model="createForm.merchantId" placeholder="请输入商户号" />
        </el-form-item>
        <el-form-item label="费率（%）">
          <el-input-number v-model="createForm.feeRate" :min="0" :max="10" :step="0.1" :precision="2" controls-position="right" />
        </el-form-item>
        <el-form-item label="支付环境">
          <el-radio-group v-model="createForm.mode">
            <el-radio-button value="PROD">正式环境</el-radio-button>
            <el-radio-button value="SANDBOX">沙箱环境</el-radio-button>
          </el-radio-group>
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
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import PageHeader from '@/components/PageHeader.vue'
import StatCard from '@/components/StatCard.vue'
import TablePager from '@/components/TablePager.vue'
import { loadAdminPayments, saveAdminPayments } from '@/mock/admin'
import { matchKeyword } from '@/utils/text'

const payments = ref(loadAdminPayments())
const keyword = ref('')
const status = ref('all')
const mode = ref('all')
const drawerVisible = ref(false)
const activePayment = ref(null)
const createVisible = ref(false)
const page = ref(1)
const pageSize = 10

const form = reactive({
  merchantId: '',
  feeRate: 0,
  settleCycle: 'T+1',
  callbackUrl: '',
  mode: 'PROD',
  enabled: true
})

const createForm = reactive({
  name: '',
  code: '',
  merchantId: '',
  feeRate: 0.6,
  mode: 'PROD'
})

const filtered = computed(() => {
  const text = keyword.value.trim().toLowerCase()
  return payments.value.filter(item => {
    const hit = matchKeyword(text, item.name, item.code, item.merchantId)
    const matchStatus =
      status.value === 'all' || (status.value === 'enabled' ? item.enabled : !item.enabled)
    const matchMode = mode.value === 'all' || item.mode === mode.value
    return hit && matchStatus && matchMode
  })
})

const paged = computed(() =>
  filtered.value.slice((page.value - 1) * pageSize, page.value * pageSize)
)

watch([keyword, status, mode], () => {
  page.value = 1
})

const stats = computed(() => {
  const enabled = payments.value.filter(item => item.enabled)
  const successRate = enabled.length
    ? enabled.reduce((sum, item) => sum + Number(item.successRate), 0) / enabled.length
    : 0
  return {
    todayAmount: payments.value.reduce((sum, item) => sum + Number(item.todayAmount), 0),
    todayOrders: payments.value.reduce((sum, item) => sum + Number(item.todayOrders), 0),
    totalAmount: payments.value.reduce((sum, item) => sum + Number(item.totalAmount), 0),
    successRate,
    enabled: enabled.length
  }
})

function formatMoney(value) {
  return Number(value || 0).toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

function resetFilters() {
  keyword.value = ''
  status.value = 'all'
  mode.value = 'all'
}

function onToggle(row) {
  row.updatedAt = dayjs().format('YYYY-MM-DD HH:mm')
  saveAdminPayments(payments.value)
  ElMessage.success(`${row.name} 已${row.enabled ? '启用' : '停用'}`)
}

function openConfig(row) {
  activePayment.value = row
  Object.assign(form, {
    merchantId: row.merchantId,
    feeRate: row.feeRate,
    settleCycle: row.settleCycle,
    callbackUrl: row.callbackUrl,
    mode: row.mode,
    enabled: row.enabled
  })
  drawerVisible.value = true
}

function saveConfig() {
  if (!activePayment.value) return
  if (!form.merchantId.trim()) {
    ElMessage.warning('请填写商户号')
    return
  }
  Object.assign(activePayment.value, form, {
    updatedAt: dayjs().format('YYYY-MM-DD HH:mm')
  })
  saveAdminPayments(payments.value)
  drawerVisible.value = false
  ElMessage.success(`${activePayment.value.name} 配置已保存`)
}

function testChannel(row) {
  if (!row.enabled) {
    ElMessage.warning(`${row.name} 已停用，请先启用后再测试`)
    return
  }
  ElMessage.success(`${row.name} 支付链路正常，平均响应 ${120 + Math.floor(Math.random() * 180)}ms`)
}

function openCreate() {
  Object.assign(createForm, {
    name: '',
    code: '',
    merchantId: '',
    feeRate: 0.6,
    mode: 'PROD'
  })
  createVisible.value = true
}

function submitCreate() {
  const name = createForm.name.trim()
  const code = createForm.code.trim().toUpperCase()
  if (!name || !code) {
    ElMessage.warning('请填写渠道名称与渠道标识')
    return
  }
  if (payments.value.some(item => item.code === code)) {
    ElMessage.warning('渠道标识已存在，请更换')
    return
  }
  payments.value.unshift({
    id: `pay-${Date.now()}`,
    name,
    code,
    letter: name.slice(0, 1),
    color: '#3b82f6',
    enabled: false,
    mode: createForm.mode,
    merchantId: createForm.merchantId.trim() || '—',
    feeRate: createForm.feeRate,
    settleCycle: 'T+1',
    callbackUrl: '',
    todayAmount: 0,
    todayOrders: 0,
    successRate: 100,
    totalAmount: 0,
    updatedAt: dayjs().format('YYYY-MM-DD HH:mm')
  })
  saveAdminPayments(payments.value)
  createVisible.value = false
  ElMessage.success(`${name} 已接入，请补充配置后启用`)
}
</script>

<style scoped>
.channel-logo {
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

.channel-logo.big {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  font-size: 17px;
}

.channel-head {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 4px 0 18px;
  border-bottom: 1px solid var(--app-border);
}

.channel-head-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.channel-head-main strong {
  font-size: 15px;
}

.channel-head-main span {
  color: var(--app-muted);
  font-size: 11.5px;
}

.channel-form {
  margin-top: 18px;
}

.channel-form :deep(.el-select),
.channel-form :deep(.el-input-number) {
  width: 100%;
}

.drawer-foot {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding-top: 8px;
}
</style>
