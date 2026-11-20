<template>
  <div class="admin-page">
    <PageHeader title="订单管理" description="充值、订阅与兑换码订单明细。" flush>
      <el-button round @click="exportCsv">
        <el-icon><Download /></el-icon>
        导出 CSV
      </el-button>
    </PageHeader>

    <section class="stat-grid">
      <StatCard label="订单总数" :value="stats.total" icon="Tickets" hint="全部订单记录" />
      <StatCard label="交易总额" :value="`¥${formatMoney(stats.paidAmount)}`" icon="Money" hint="仅统计已完成订单" />
      <StatCard label="待支付" :value="stats.pending" icon="Clock" hint="等待用户完成付款" />
      <StatCard label="已退款" :value="stats.refunded" icon="RefreshLeft" hint="退款订单金额不计入收入" />
    </section>

    <section class="filters panel">
      <el-input v-model="keyword" class="filter-search" placeholder="搜索订单号或用户" clearable>
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-select v-model="type" class="filter-item">
        <el-option label="全部类型" value="all" />
        <el-option label="充值" value="充值" />
        <el-option label="订阅" value="订阅" />
        <el-option label="兑换码" value="兑换码" />
      </el-select>
      <el-select v-model="status" class="filter-item">
        <el-option label="全部状态" value="all" />
        <el-option label="已完成" value="PAID" />
        <el-option label="待支付" value="PENDING" />
        <el-option label="已退款" value="REFUNDED" />
      </el-select>
      <el-select v-model="payMethod" class="filter-item">
        <el-option label="全部支付方式" value="all" />
        <el-option label="支付宝" value="支付宝" />
        <el-option label="微信支付" value="微信支付" />
        <el-option label="兑换码" value="兑换码" />
      </el-select>
      <el-button round @click="resetFilters">重置筛选</el-button>
    </section>

    <section class="panel table-panel">
      <div class="table-head">
        <h3>订单列表</h3>
        <span>共 {{ filtered.length }} 笔订单</span>
      </div>
      <el-table :data="paged">
        <el-table-column label="订单号" min-width="190">
          <template #default="{ row }">
            <span class="mono order-no">{{ row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column label="用户" min-width="150">
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
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="typeOf(row.type).type" size="small" effect="plain">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="120">
          <template #default="{ row }">
            <span class="money">¥{{ formatMoney(row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="payMethod" label="支付方式" width="120" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusOf(row.status).type" size="small" effect="plain">
              {{ statusOf(row.status).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
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
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import PageHeader from '@/components/PageHeader.vue'
import StatCard from '@/components/StatCard.vue'
import { listAdminOrders } from '@/mock/platform'
import { downloadCsv } from '@/utils/csv'
import { matchKeyword } from '@/utils/text'

const orders = listAdminOrders()
const keyword = ref('')
const type = ref('all')
const status = ref('all')
const payMethod = ref('all')
const page = ref(1)
const pageSize = 10

const filtered = computed(() => {
  const text = keyword.value.trim().toLowerCase()
  return orders.filter(order => {
    const hit = matchKeyword(text, order.id, order.user)
    const matchType = type.value === 'all' || order.type === type.value
    const matchStatus = status.value === 'all' || order.status === status.value
    const matchPay = payMethod.value === 'all' || order.payMethod === payMethod.value
    return hit && matchType && matchStatus && matchPay
  })
})

const paged = computed(() => filtered.value.slice((page.value - 1) * pageSize, page.value * pageSize))

const stats = computed(() => ({
  total: orders.length,
  paidAmount: orders.filter(order => order.status === 'PAID').reduce((sum, order) => sum + order.amount, 0),
  pending: orders.filter(order => order.status === 'PENDING').length,
  refunded: orders.filter(order => order.status === 'REFUNDED').length
}))

watch([keyword, type, status, payMethod], () => {
  page.value = 1
})

function resetFilters() {
  keyword.value = ''
  type.value = 'all'
  status.value = 'all'
  payMethod.value = 'all'
}

function typeOf(value) {
  const map = {
    订阅: 'warning',
    充值: 'primary',
    兑换码: 'info'
  }
  return { type: map[value] || 'info' }
}

function statusOf(value) {
  const map = {
    PAID: { label: '已完成', type: 'success' },
    PENDING: { label: '待支付', type: 'warning' },
    REFUNDED: { label: '已退款', type: 'info' }
  }
  return map[value] || { label: value, type: 'info' }
}

function formatMoney(value) {
  return Number(value).toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

function exportCsv() {
  downloadCsv(
    `订单列表-${dayjs().format('YYYYMMDD')}.csv`,
    ['订单号', '用户', '类型', '金额', '支付方式', '状态', '创建时间'],
    filtered.value.map(order => [
      order.id,
      order.user,
      order.type,
      order.amount,
      order.payMethod,
      statusOf(order.status).label,
      order.createdAt
    ])
  )
  ElMessage.success(`已导出 ${filtered.value.length} 笔订单`)
}
</script>

<style scoped>
.order-no {
  color: #9ecbff;
  font-size: 11.5px;
}
</style>
