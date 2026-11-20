<template>
  <div class="admin-page">
    <PageHeader title="报表分析" description="营收、用量与用户的趋势与结构分析，支持导出当前视图。" flush>
      <el-button round @click="exportCsv">
        <el-icon><Download /></el-icon>
        导出报表
      </el-button>
    </PageHeader>

    <section class="filters panel">
      <el-date-picker
        v-model="range"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        class="range-picker"
      />
      <el-select v-model="granularity" class="filter-item">
        <el-option label="按月" value="month" />
        <el-option label="按周" value="week" />
        <el-option label="按日" value="day" />
      </el-select>
      <el-select v-model="channel" class="filter-item">
        <el-option label="全部渠道" value="all" />
        <el-option label="支付宝" value="支付宝" />
        <el-option label="微信支付" value="微信支付" />
      </el-select>
      <span class="filter-action muted">统计口径：{{ rangeLabel }}</span>
    </section>

    <div class="panel tab-panel">
      <TabStrip v-model="tab" :items="tabs" />
    </div>

    <section class="stat-grid">
      <StatCard
        v-for="item in kpis"
        :key="item.label"
        :label="item.label"
        :value="item.value"
        :unit="item.unit"
        :icon="item.icon"
        :hint="item.hint"
      />
    </section>

    <section class="report-grid">
      <article class="panel panel-block">
        <div class="panel-head">
          <div>
            <h3>{{ trendTitle }}</h3>
            <span>{{ granularityLabel }}聚合，柱高为业务量</span>
          </div>
        </div>
        <UsageTrend :points="points" :unit="tab === 'revenue' ? '百元' : '次'" />
      </article>

      <article class="panel panel-block">
        <div class="panel-head">
          <div>
            <h3>结构占比</h3>
            <span>按{{ tab === 'user' ? '套餐' : '应用类型' }}拆分</span>
          </div>
        </div>
        <ul class="share-list">
          <li v-for="item in share" :key="item.name">
            <span class="share-name">
              <i :style="{ background: item.color }" />
              {{ item.name }}
            </span>
            <div class="share-track">
              <span :style="{ width: `${item.value}%`, background: item.color }" />
            </div>
            <span class="share-value">{{ item.value }}%</span>
          </li>
        </ul>
      </article>
    </section>

    <section class="report-grid">
      <article class="panel table-panel">
        <div class="table-head">
          <h3>Top 模型</h3>
          <span>按调用次数排序</span>
        </div>
        <el-table :data="modelPaged" size="small">
          <el-table-column type="index" label="#" width="50" />
          <el-table-column prop="name" label="模型" min-width="190">
            <template #default="{ row }">
              <span class="mono model">{{ row.name }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="category" label="类型" width="80" />
          <el-table-column prop="requests" label="调用" width="100">
            <template #default="{ row }">{{ row.requests.toLocaleString() }}</template>
          </el-table-column>
          <el-table-column prop="credits" label="积分" width="100" />
          <el-table-column label="占比" width="90">
            <template #default="{ row }">{{ row.share }}%</template>
          </el-table-column>
        </el-table>
        <TablePager v-model:page="modelPage" :total="topModels.length" />
      </article>

      <article class="panel table-panel">
        <div class="table-head">
          <h3>Top 用户</h3>
          <span>按消耗积分排序</span>
        </div>
        <el-table :data="userPaged" size="small">
          <el-table-column label="用户" min-width="150">
            <template #default="{ row }">
              <div class="user-cell">
                <span class="avatar">{{ row.name.slice(0, 1) }}</span>
                <span class="user-cell-main">
                  <strong>{{ row.name }}</strong>
                  <span>@{{ row.username }}</span>
                </span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="plan" label="套餐" width="90" />
          <el-table-column prop="requests" label="调用" width="90">
            <template #default="{ row }">{{ row.requests.toLocaleString() }}</template>
          </el-table-column>
          <el-table-column prop="credits" label="积分" width="80" />
          <el-table-column label="消费" width="100">
            <template #default="{ row }">
              <span class="money">¥{{ row.amount }}</span>
            </template>
          </el-table-column>
        </el-table>
        <TablePager v-model:page="userPage" :total="topUsers.length" />
      </article>
    </section>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import PageHeader from '@/components/PageHeader.vue'
import StatCard from '@/components/StatCard.vue'
import TabStrip from '@/components/TabStrip.vue'
import UsageTrend from '@/components/UsageTrend.vue'
import TablePager from '@/components/TablePager.vue'
import { reportKpis, revenuePoints, topModels, topUsers } from '@/mock/admin-extra'
import { listAdminOrders } from '@/mock/platform'
import { downloadCsv } from '@/utils/csv'

const tabs = [
  { key: 'revenue', label: '营收' },
  { key: 'usage', label: '用量' },
  { key: 'user', label: '用户' }
]

const tab = ref('revenue')
const range = ref([dayjs().subtract(30, 'day').toDate(), dayjs().toDate()])
const granularity = ref('month')
const channel = ref('all')
const modelPage = ref(1)
const userPage = ref(1)
const pageSize = 10

const modelPaged = computed(() =>
  topModels.slice((modelPage.value - 1) * pageSize, modelPage.value * pageSize)
)

const userPaged = computed(() =>
  topUsers.slice((userPage.value - 1) * pageSize, userPage.value * pageSize)
)

const granularityLabel = computed(() => ({ month: '按月', week: '按周', day: '按日' }[granularity.value]))
const rangeLabel = computed(() =>
  range.value?.length === 2
    ? `${dayjs(range.value[0]).format('YYYY-MM-DD')} 至 ${dayjs(range.value[1]).format('YYYY-MM-DD')}`
    : '全部时间'
)

const trendTitle = computed(() =>
  ({ revenue: '营收趋势', usage: '调用趋势', user: '新增用户趋势' }[tab.value])
)

const points = computed(() => {
  if (tab.value === 'revenue') return revenuePoints
  if (tab.value === 'usage') {
    return revenuePoints.map(item => ({ ...item, requests: item.credits * 3 }))
  }
  return revenuePoints.map((item, index) => ({
    date: item.date,
    requests: [52, 61, 58, 72, 66, 84, 78, 96, 88, 102, 96, 118][index],
    credits: 0
  }))
})

// 与仪表盘共用同一份订单数据，避免两处金额不一致
const orders = listAdminOrders()

const revenueStats = computed(() => {
  const paid = orders.filter(order => order.status === 'PAID')
  const month = dayjs().format('YYYY-MM')
  const monthPaid = paid.filter(order => (order.createdAt || '').startsWith(month))
  const refunded = orders.filter(order => order.status === 'REFUNDED')
  const total = paid.reduce((sum, order) => sum + order.amount, 0)
  const refund = refunded.reduce((sum, order) => sum + order.amount, 0)
  return {
    total,
    month: monthPaid.reduce((sum, order) => sum + order.amount, 0),
    avg: paid.length ? total / paid.length : 0,
    refund,
    refundRate: total ? ((refund / total) * 100).toFixed(1) : '0.0'
  }
})

const kpis = computed(() => {
  if (tab.value === 'revenue') {
    return [
      { label: '累计营收', value: `¥${money(revenueStats.value.total)}`, icon: 'Money', hint: `本月 ¥${money(revenueStats.value.month)}` },
      { label: '环比增长', value: reportKpis.revenue.growth, unit: '%', icon: 'TrendCharts', hint: '较上月同期' },
      { label: '客单价', value: `¥${revenueStats.value.avg.toFixed(1)}`, icon: 'Tickets', hint: '付费订单平均金额' },
      { label: '退款金额', value: `¥${money(revenueStats.value.refund)}`, icon: 'RefreshLeft', hint: `占营收 ${revenueStats.value.refundRate}%` }
    ]
  }
  if (tab.value === 'usage') {
    return [
      { label: '累计调用', value: reportKpis.usage.requests.toLocaleString(), icon: 'DataLine', hint: '全部应用合计' },
      { label: '积分消耗', value: reportKpis.usage.credits.toLocaleString(), icon: 'Coin', hint: '含失败退回后净额' },
      { label: '环比增长', value: reportKpis.usage.growth, unit: '%', icon: 'TrendCharts', hint: '较上月同期' },
      { label: '失败调用', value: '2,146', icon: 'Warning', hint: '失败率 1.7%' }
    ]
  }
  return [
    { label: '用户总数', value: reportKpis.user.total.toLocaleString(), icon: 'User', hint: '含免费与付费用户' },
    { label: '本月新增', value: reportKpis.user.newThisMonth, icon: 'Plus', hint: '较上月 +12.8%' },
    { label: '次月留存', value: reportKpis.user.retention, unit: '%', icon: 'RefreshRight', hint: '注册后次月仍活跃' },
    { label: '付费转化', value: '14.5', unit: '%', icon: 'Medal', hint: '付费用户 / 总用户' }
  ]
})

const share = computed(() => {
  if (tab.value === 'user') {
    return [
      { name: '体验版', value: 68, color: '#64748b' },
      { name: '专业版', value: 24, color: '#3b82f6' },
      { name: '旗舰版', value: 8, color: '#f59e0b' }
    ]
  }
  return [
    { name: '对话', value: 46, color: '#3b82f6' },
    { name: '图片', value: 28, color: '#34d399' },
    { name: '视频', value: 19, color: '#f59e0b' },
    { name: '音乐', value: 7, color: '#a78bfa' }
  ]
})

function money(value) {
  return Number(value).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function exportCsv() {
  if (tab.value === 'user') {
    downloadCsv(
      `用户报表-${dayjs().format('YYYYMMDD')}.csv`,
      ['用户', '用户名', '套餐', '调用次数', '消耗积分', '消费金额'],
      topUsers.map(item => [item.name, item.username, item.plan, item.requests, item.credits, item.amount])
    )
  } else {
    downloadCsv(
      `模型报表-${dayjs().format('YYYYMMDD')}.csv`,
      ['模型', '类型', '调用次数', '消耗积分', '占比'],
      topModels.map(item => [item.name, item.category, item.requests, item.credits, `${item.share}%`])
    )
  }
  ElMessage.success('报表已导出')
}
</script>

<style scoped>
.tab-panel {
  padding: 6px 20px 0;
}

.range-picker {
  width: 280px;
}

.report-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(0, 1fr);
  gap: 14px;
}

.panel-block {
  padding: 16px 20px 18px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.panel-head h3 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
}

.panel-head span {
  color: var(--app-muted);
  font-size: 11.5px;
}

.share-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.share-list li {
  display: flex;
  align-items: center;
  gap: 10px;
}

.share-name {
  display: flex;
  align-items: center;
  gap: 7px;
  width: 84px;
  flex-shrink: 0;
  color: var(--app-text);
  font-size: 12px;
}

.share-name i {
  width: 7px;
  height: 7px;
  border-radius: 50%;
}

.share-track {
  flex: 1;
  height: 7px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.06);
  overflow: hidden;
}

.share-track span {
  display: block;
  height: 100%;
  border-radius: 4px;
}

.share-value {
  width: 42px;
  color: var(--app-muted);
  font-size: 11.5px;
  text-align: right;
}

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

.model {
  color: #9ecbff;
  font-size: 11.5px;
}

@media (max-width: 1100px) {
  .report-grid {
    grid-template-columns: 1fr;
  }

  .range-picker {
    width: 100%;
  }
}
</style>
