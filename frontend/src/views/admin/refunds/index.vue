<template>
  <div class="admin-page">
    <PageHeader title="退款与对账" description="处理退款申请，核对各支付渠道的日终账单差异。" flush>
      <el-button round @click="exportCsv">
        <el-icon><Download /></el-icon>
        {{ tab === 'refund' ? '导出退款单' : '导出对账单' }}
      </el-button>
    </PageHeader>

    <section class="stat-grid">
      <StatCard label="待处理退款" :value="stats.pending" icon="Clock" hint="需在 24 小时内响应" />
      <StatCard label="本月退款额" :value="`¥${stats.refundedAmount.toFixed(2)}`" icon="RefreshLeft" hint="已通过的退款合计" />
      <StatCard label="退款率" :value="stats.rate" unit="%" icon="DataLine" hint="退款金额 / 交易总额" />
      <StatCard label="对账差异" :value="stats.diffCount" icon="Warning" hint="近 7 天渠道账单不一致" />
    </section>

    <div class="panel tab-panel">
      <TabStrip v-model="tab" :items="tabs" />
    </div>

    <section v-if="tab === 'refund'" class="panel table-panel">
      <div class="table-head">
        <h3>退款申请</h3>
        <span>共 {{ refunds.length }} 条，待处理 {{ stats.pending }} 条</span>
      </div>
      <el-table :data="refundPaged" size="small">
        <el-table-column prop="appliedAt" label="申请时间" width="150" />
        <el-table-column label="订单号" min-width="170">
          <template #default="{ row }">
            <span class="mono order-no">{{ row.orderId }}</span>
          </template>
        </el-table-column>
        <el-table-column label="用户" width="140">
          <template #default="{ row }">
            <div class="user-cell">
              <span class="avatar">{{ row.user.slice(0, 1) }}</span>
              <span class="user-cell-main">
                <strong>{{ row.user }}</strong>
                <span>@{{ row.username }}</span>
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="90" />
        <el-table-column label="金额" width="110">
          <template #default="{ row }">
            <span class="money">¥{{ row.amount.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="退款原因" min-width="220" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusOf(row.status).type" size="small" effect="plain">
              {{ statusOf(row.status).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <div v-if="row.status === 'PENDING'" class="actions">
              <el-button link type="primary" size="small" @click="openHandle(row, 'APPROVED')">
                同意
              </el-button>
              <el-button link type="danger" size="small" @click="openHandle(row, 'REJECTED')">
                拒绝
              </el-button>
            </div>
            <el-button v-else link size="small" @click="showNote(row)">查看备注</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="table-empty">没有退款申请</div>
        </template>
      </el-table>
      <TablePager v-model:page="refundPage" :total="refunds.length" />
    </section>

    <section v-else class="panel table-panel">
      <div class="table-head">
        <h3>日终对账</h3>
        <span>应收为系统金额，实收为渠道账单金额</span>
      </div>
      <el-table :data="reconciliationPaged" size="small">
        <el-table-column prop="date" label="账单日期" width="130" />
        <el-table-column prop="channel" label="支付渠道" width="130" />
        <el-table-column label="应收" width="130">
          <template #default="{ row }">
            <span class="money">¥{{ money(row.receivable) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="实收" width="130">
          <template #default="{ row }">
            <span class="money">¥{{ money(row.received) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="差异" width="120">
          <template #default="{ row }">
            <span class="money" :class="row.diff === 0 ? 'muted' : row.diff > 0 ? 'income' : 'expense'">
              {{ row.diff === 0 ? '0.00' : `${row.diff > 0 ? '+' : ''}${row.diff.toFixed(2)}` }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.status === 'BALANCED' ? 'success' : 'warning'" size="small" effect="plain">
              {{ row.status === 'BALANCED' ? '已平账' : '有差异' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button v-if="row.status !== 'BALANCED'" link size="small" @click="markBalanced(row)">
              标记已核销
            </el-button>
            <span v-else class="muted">—</span>
          </template>
        </el-table-column>
        <template #empty>
          <div class="table-empty">没有对账记录</div>
        </template>
      </el-table>
      <TablePager v-model:page="reconciliationPage" :total="reconciliations.length" />
    </section>

    <el-dialog v-model="handleVisible" :title="handleStatus === 'APPROVED' ? '同意退款' : '拒绝退款'" width="460px">
      <div v-if="current" class="handle-summary">
        <span>订单 <b class="mono">{{ current.orderId }}</b></span>
        <span>金额 <b class="mono">¥{{ current.amount.toFixed(2) }}</b></span>
      </div>
      <el-form label-position="top">
        <el-form-item :label="handleStatus === 'APPROVED' ? '退款说明' : '拒绝原因'">
          <el-input
            v-model="note"
            type="textarea"
            :rows="3"
            maxlength="120"
            show-word-limit
            :placeholder="handleStatus === 'APPROVED' ? '填写退款说明，将记录在订单备注中' : '请说明拒绝原因，会同步给用户'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button round @click="handleVisible = false">取消</el-button>
        <el-button round :type="handleStatus === 'APPROVED' ? 'primary' : 'danger'" @click="confirmHandle">
          确认{{ handleStatus === 'APPROVED' ? '退款' : '拒绝' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import PageHeader from '@/components/PageHeader.vue'
import StatCard from '@/components/StatCard.vue'
import TabStrip from '@/components/TabStrip.vue'
import TablePager from '@/components/TablePager.vue'
import { loadOrders } from '@/mock/account'
import { loadReconciliations, saveReconciliations } from '@/mock/admin-extra'
import { handleRefund, listRefunds } from '@/mock/platform'
import { downloadCsv } from '@/utils/csv'

const tabs = [
  { key: 'refund', label: '退款申请' },
  { key: 'reconcile', label: '日终对账' }
]

const tab = ref('refund')
const refunds = ref(listRefunds())
const reconciliations = ref(loadReconciliations())
const handleVisible = ref(false)
const handleStatus = ref('APPROVED')
const current = ref(null)
const note = ref('')
const refundPage = ref(1)
const reconciliationPage = ref(1)
const pageSize = 10

const refundPaged = computed(() =>
  refunds.value.slice((refundPage.value - 1) * pageSize, refundPage.value * pageSize)
)

const reconciliationPaged = computed(() =>
  reconciliations.value.slice(
    (reconciliationPage.value - 1) * pageSize,
    reconciliationPage.value * pageSize
  )
)

const stats = computed(() => {
  const pending = refunds.value.filter(item => item.status === 'PENDING').length
  const approved = refunds.value.filter(item => item.status === 'APPROVED')
  const refundedAmount = approved.reduce((sum, item) => sum + (Number(item.amount) || 0), 0)
  const diffCount = reconciliations.value.filter(item => item.status !== 'BALANCED').length
  // 退款率分母取已支付订单总额，缺失时退回退款金额本身，避免硬编码与除零
  const paidTotal = loadOrders()
    .filter(order => order.status === 'paid')
    .reduce((sum, order) => sum + (Number(order.amount) || 0), 0)
  const base = Math.max(paidTotal, refundedAmount)
  return {
    pending,
    refundedAmount,
    rate: base > 0 ? ((refundedAmount / base) * 100).toFixed(1) : '0.0',
    diffCount
  }
})

function statusOf(status) {
  return {
    PENDING: { label: '待处理', type: 'warning' },
    APPROVED: { label: '已退款', type: 'success' },
    REJECTED: { label: '已拒绝', type: 'info' }
  }[status] || { label: status, type: 'info' }
}

function money(value) {
  return Number(value).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function openHandle(row, status) {
  current.value = row
  handleStatus.value = status
  note.value = ''
  handleVisible.value = true
}

function confirmHandle() {
  if (!current.value) return
  if (handleStatus.value === 'REJECTED' && !note.value.trim()) {
    ElMessage.warning('请填写拒绝原因')
    return
  }
  handleRefund(current.value.id, handleStatus.value, note.value.trim())
  refunds.value = listRefunds()
  handleVisible.value = false
  ElMessage.success(handleStatus.value === 'APPROVED' ? '已同意退款' : '已拒绝退款')
}

function showNote(row) {
  ElMessage.info(row.note || row.handledAt || '暂无备注')
}

function markBalanced(row) {
  row.status = 'BALANCED'
  row.diff = 0
  row.received = row.receivable
  saveReconciliations(reconciliations.value)
  ElMessage.success('已标记核销，差异归零')
}

function exportCsv() {
  if (tab.value === 'refund') {
    downloadCsv(
      `退款单-${dayjs().format('YYYYMMDD')}.csv`,
      ['申请时间', '订单号', '用户', '类型', '金额', '原因', '状态'],
      refunds.value.map(item => [
        item.appliedAt,
        item.orderId,
        item.user,
        item.type,
        item.amount,
        item.reason,
        statusOf(item.status).label
      ])
    )
    ElMessage.success('退款单已导出')
    return
  }
  downloadCsv(
    `对账单-${dayjs().format('YYYYMMDD')}.csv`,
    ['账单日期', '渠道', '应收', '实收', '差异', '状态'],
    reconciliations.value.map(item => [
      item.date,
      item.channel,
      item.receivable,
      item.received,
      item.diff,
      item.status === 'BALANCED' ? '已平账' : '有差异'
    ])
  )
  ElMessage.success('对账单已导出')
}
</script>

<style scoped>
.tab-panel {
  padding: 6px 20px 0;
}

.order-no {
  color: #9ecbff;
  font-size: 11.5px;
}

.income {
  color: #34d399;
}

.expense {
  color: #f87171;
}

.handle-summary {
  display: flex;
  gap: 20px;
  margin-bottom: 14px;
  padding: 10px 12px;
  border-radius: 10px;
  background: var(--app-surface-2);
  border: 1px solid var(--app-border);
  color: var(--app-muted);
  font-size: 12px;
}

.handle-summary b {
  color: var(--app-text);
  font-weight: 600;
}
</style>
