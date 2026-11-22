<template>
  <div class="page">
    <PageHeader title="所有订单" description="查看充值、订阅与消费订单。">
      <el-button round @click="exportCsv">
        <el-icon><Download /></el-icon>
        导出 CSV
      </el-button>
      <el-button round type="primary" @click="router.push('/console/wallet?recharge=1')">
        <el-icon><Wallet /></el-icon>
        去充值
      </el-button>
    </PageHeader>

    <section class="stat-grid">
      <StatCard label="订单总数" :value="stats.total" icon="Tickets" hint="含已取消订单" />
      <StatCard label="总消费" :value="`¥${stats.amount.toFixed(2)}`" icon="Money" hint="仅统计已完成订单" />
      <StatCard label="已完成订单" :value="stats.paid" icon="CircleCheck" hint="充值、订阅与兑换码" />
      <StatCard label="待支付订单" :value="stats.pending" icon="Clock" hint="24 小时后自动关闭" />
    </section>

    <section class="filters">
      <el-select v-model="status" class="filter-item">
        <el-option label="全部状态" value="all" />
        <el-option label="已完成" value="paid" />
        <el-option label="待支付" value="pending" />
        <el-option label="已取消" value="cancelled" />
      </el-select>
      <el-select v-model="payMethod" class="filter-item">
        <el-option label="全部支付方式" value="all" />
        <el-option label="支付宝" value="支付宝" />
        <el-option label="微信支付" value="微信支付" />
        <el-option label="兑换码" value="兑换码" />
      </el-select>
      <el-date-picker
        v-model="range"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        class="range-picker"
      />
      <el-button round @click="resetFilters">重置筛选</el-button>
      <span class="filter-action muted">共 {{ filtered.length }} 笔订单</span>
    </section>

    <section class="panel table-panel">
      <el-table :data="paged" size="small">
        <el-table-column label="订单ID" min-width="180">
          <template #default="{ row }">
            <span class="mono order-no">{{ row.orderNo || row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="120">
          <template #default="{ row }">
            <span class="mono">¥{{ row.amount.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="订单描述" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusOf(row.status).type" size="small" effect="plain">
              {{ statusOf(row.status).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="payMethod" label="支付方式" width="120" />
        <el-table-column prop="createdAt" label="创建时间" width="160" />
        <el-table-column label="操作" width="170">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'pending' && demoMode"
              link
              size="small"
              @click="payNow(row)"
            >
              去支付
            </el-button>
            <el-button
              v-else-if="row.status === 'pending'"
              link
              size="small"
              @click="cancelOrder(row)"
            >
              取消订单
            </el-button>
            <el-button
              v-if="row.status === 'paid' && row.amount > 0 && demoMode"
              link
              size="small"
              type="danger"
              @click="applyRefund(row)"
            >
              申请退款
            </el-button>
            <el-button
              v-if="row.status === 'cancelled'"
              link
              size="small"
              type="danger"
              @click="removeOrder(row)"
            >
              删除
            </el-button>
            <el-button link size="small" @click="showDetail(row)">详情</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="table-empty">没有匹配的订单</div>
        </template>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="page"
          layout="prev, pager, next"
          :page-size="pageSize"
          :total="filtered.length"
          background
        />
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { changeOrderStatus, deleteOrder, listOrders } from '@/api/order'
import PageHeader from '@/components/PageHeader.vue'
import StatCard from '@/components/StatCard.vue'
import {
  appendWalletLog,
  loadOrders,
  loadWallet,
  saveOrders,
  saveWallet
} from '@/mock/account'
import { requestRefund } from '@/mock/platform'
import { rechargeToPurchased } from '@/mock/credits'
import { downloadCsv } from '@/utils/csv'
import { fetchAllPages } from '@/utils/pagination'

const router = useRouter()
const orders = ref([])
const demoMode = ref(false)
const status = ref('all')
const payMethod = ref('all')
const range = ref([])
const page = ref(1)
const pageSize = 8

/** 后端订单状态到页面状态值 */
const STATUS_MAP = {
  PAID: 'paid',
  PENDING: 'pending',
  CANCELLED: 'cancelled',
  REFUNDING: 'refunding',
  REFUNDED: 'refunded'
}

/** 后端支付方式到页面展示文案 */
const PAY_METHOD_MAP = {
  ALIPAY: '支付宝',
  WECHAT: '微信支付',
  REDEEM: '兑换码',
  BALANCE: '余额'
}

function formatTime(value) {
  return value ? dayjs(value).format('YYYY-MM-DD HH:mm') : '—'
}

function normalizeOrder(row) {
  return {
    id: row.id,
    orderNo: row.orderNo,
    amount: Number(row.amount) || 0,
    description: row.description || '—',
    status: STATUS_MAP[row.status] || row.status,
    payMethod: PAY_METHOD_MAP[row.payMethod] || row.payMethod || '—',
    createdAt: formatTime(row.createdAt),
    paidAt: formatTime(row.paidAt)
  }
}

async function fetchOrders() {
  try {
    const result = await fetchAllPages(params => listOrders(params))
    if (!result) {
      demoMode.value = false
      orders.value = []
      if (import.meta.env.DEV) {
        demoMode.value = true
        orders.value = loadOrders()
      }
      return
    }
    demoMode.value = false
    orders.value = result.list.map(normalizeOrder)
  } catch (error) {
    // 生产环境不能把接口失败伪装成本地演示订单
    demoMode.value = false
    orders.value = []
    if (import.meta.env.DEV) {
      demoMode.value = true
      orders.value = loadOrders()
    }
  }
}

onMounted(fetchOrders)

const filtered = computed(() =>
  orders.value.filter(row => {
    const matchStatus = status.value === 'all' || row.status === status.value
    const matchPay = payMethod.value === 'all' || row.payMethod === payMethod.value
    let matchRange = true
    if (range.value?.length === 2) {
      const day = String(row.createdAt).slice(0, 10)
      matchRange =
        day >= dayjs(range.value[0]).format('YYYY-MM-DD') && day <= dayjs(range.value[1]).format('YYYY-MM-DD')
    }
    return matchStatus && matchPay && matchRange
  })
)

const paged = computed(() =>
  filtered.value.slice((page.value - 1) * pageSize, page.value * pageSize)
)

const stats = computed(() => {
  const paid = orders.value.filter(row => row.status === 'paid')
  return {
    total: orders.value.length,
    amount: paid.reduce((sum, row) => sum + row.amount, 0),
    paid: paid.length,
    pending: orders.value.filter(row => row.status === 'pending').length
  }
})

watch([status, payMethod, range], () => {
  page.value = 1
})

function statusOf(value) {
  return {
    paid: { label: '已完成', type: 'success' },
    pending: { label: '待支付', type: 'warning' },
    cancelled: { label: '已取消', type: 'info' },
    refunding: { label: '退款中', type: 'warning' },
    refunded: { label: '已退款', type: 'danger' }
  }[value] || { label: value, type: 'info' }
}

function resetFilters() {
  status.value = 'all'
  payMethod.value = 'all'
  range.value = []
}

async function payNow(row) {
  if (!demoMode.value) return
  try {
    await ElMessageBox.confirm(`确认支付订单 ${row.orderNo || row.id}，金额 ¥${row.amount.toFixed(2)}？`, '支付确认', {
      confirmButtonText: '确认支付',
      cancelButtonText: '取消'
    })
  } catch (error) {
    return
  }
  row.status = 'paid'
  saveOrders(orders.value)

  if (row.description.includes('充值')) {
    const creditValue = Number(row.amount) || 0
    // 充值到账进入购买积分池，再累加累计充值
    const credited = rechargeToPurchased(creditValue)
    const wallet = loadWallet()
    wallet.totalRecharge = Number((wallet.totalRecharge + creditValue).toFixed(2))
    saveWallet(wallet)
    appendWalletLog({
      id: `wl-${Date.now()}`,
      createdAt: dayjs().format('YYYY-MM-DD HH:mm'),
      type: '充值',
      amount: creditValue,
      balance: credited.balance,
      note: `${row.payMethod}充值 · ¥${creditValue.toFixed(2)}`
    })
  }
  ElMessage.success('支付成功')
}

async function cancelOrder(row) {
  try {
    await ElMessageBox.confirm(`确认取消订单 ${row.orderNo || row.id}？`, '取消订单', {
      type: 'warning',
      confirmButtonText: '取消订单',
      cancelButtonText: '保留'
    })
  } catch (error) {
    return
  }
  if (demoMode.value) {
    row.status = 'cancelled'
    saveOrders(orders.value)
    ElMessage.success('订单已取消')
    return
  }
  try {
    await changeOrderStatus(row.id, 'CANCELLED')
    row.status = 'cancelled'
    ElMessage.success('订单已取消')
  } catch (error) {
    // 请求层已统一提示错误
  }
}

async function removeOrder(row) {
  try {
    await ElMessageBox.confirm(`删除后订单 ${row.orderNo || row.id} 不可恢复，确认继续？`, '删除订单', {
      type: 'warning',
      confirmButtonText: '删除'
    })
  } catch (error) {
    return
  }
  if (demoMode.value) {
    orders.value = orders.value.filter(entry => entry.id !== row.id)
    saveOrders(orders.value)
    ElMessage.success('订单已删除')
    return
  }
  try {
    await deleteOrder(row.id)
    orders.value = orders.value.filter(entry => entry.id !== row.id)
    ElMessage.success('订单已删除')
  } catch (error) {
    // 请求层已统一提示错误
  }
}

async function applyRefund(row) {
  if (!demoMode.value) return
  let reason = ''
  try {
    const result = await ElMessageBox.prompt('请填写退款原因，提交后由管理员审核', '申请退款', {
      confirmButtonText: '提交申请',
      cancelButtonText: '取消',
      inputPlaceholder: '例如：重复购买 / 未产生消费',
      inputValidator: value => (value && value.trim() ? true : '请填写退款原因')
    })
    reason = result.value.trim()
  } catch (error) {
    return
  }
  if (requestRefund(row.id, reason)) {
    orders.value = loadOrders()
    ElMessage.success('退款申请已提交，可在后台退款与对账中处理')
  } else {
    // 幂等拒绝（已申请过 / 订单状态已变化）时不能静默返回，否则用户以为提交成功
    ElMessage.warning('该订单当前状态不支持退款申请，请刷新后重试')
  }
}

function showDetail(row) {
  ElMessageBox.alert(
    `订单号：${row.orderNo || row.id}\n金额：¥${row.amount.toFixed(2)}\n描述：${row.description}\n支付方式：${row.payMethod}\n创建时间：${row.createdAt}\n状态：${statusOf(row.status).label}`,
    '订单详情',
    { confirmButtonText: '知道了' }
  )
}

function exportCsv() {
  if (!filtered.value.length) {
    ElMessage.info('当前筛选条件下没有可导出的订单')
    return
  }
  downloadCsv(
    `订单列表-${dayjs().format('YYYYMMDD')}.csv`,
    ['订单ID', '金额', '订单描述', '状态', '支付方式', '创建时间'],
    filtered.value.map(row => [
      row.orderNo || row.id,
      row.amount,
      row.description,
      statusOf(row.status).label,
      row.payMethod,
      row.createdAt
    ])
  )
  ElMessage.success(`已导出 ${filtered.value.length} 笔订单`)
}
</script>

<style scoped>
.filters {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}

.filter-item {
  width: 156px;
}

.range-picker {
  width: 280px;
}

.filter-action {
  margin-left: auto;
  font-size: 11.5px;
}

.table-panel {
  padding: 14px 18px 10px;
}

.order-no {
  color: #9ecbff;
  font-size: 11.5px;
}

.table-empty {
  padding: 40px 0;
  color: var(--app-muted);
  font-size: 12px;
}

.pager {
  display: flex;
  justify-content: flex-end;
  padding: 10px 0;
}
</style>
