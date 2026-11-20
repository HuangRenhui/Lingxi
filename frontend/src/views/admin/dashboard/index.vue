<template>
  <div class="admin-page">
    <PageHeader title="仪表盘" description="平台运行概况、用户增长与收入趋势。" flush>
      <el-button round :loading="refreshing" @click="refresh">
        <el-icon><Refresh /></el-icon>
        刷新数据
      </el-button>
    </PageHeader>

    <section class="stat-grid">
      <StatCard label="用户总数" :value="stats.totalUsers" icon="User" :hint="`其中已封禁 ${stats.banned} 个账号`" />
      <StatCard label="今日活跃" :value="stats.activeToday" icon="Sunny" hint="近 24 小时有操作的用户" />
      <StatCard label="运行中任务" :value="stats.runningTasks" icon="Timer" :hint="`共 ${tasks.length} 个定时任务`" />
      <StatCard
        label="本月收入"
        :value="`¥${formatMoney(stats.monthlyIncome)}`"
        icon="Money"
        :hint="`已完成 ${stats.paidCount} 笔订单`"
      />
    </section>

    <section class="dash-grid">
      <article class="panel panel-block">
        <div class="panel-head">
          <div>
            <h3>近 14 天调用趋势</h3>
            <span>全平台 API 调用次数与积分消耗</span>
          </div>
        </div>
        <UsageTrend :points="usageTrend" />
      </article>

      <article class="panel panel-block">
        <div class="panel-head">
          <div>
            <h3>服务调用占比</h3>
            <span>近 14 天按调用次数排序</span>
          </div>
        </div>
        <div class="rank-list">
          <div v-for="item in serviceShare" :key="item.name" class="rank-row">
            <span class="rank-name">
              <i :style="{ background: item.color }" />
              {{ item.name }}
            </span>
            <div class="rank-track">
              <span :style="{ width: rankWidth(item), background: item.color }" />
            </div>
            <span class="rank-value">{{ item.requests.toLocaleString() }}</span>
            <span class="rank-percent">{{ rankShare(item) }}</span>
          </div>
        </div>
      </article>
    </section>

    <section class="dash-grid-2">
      <article class="panel panel-block">
        <div class="panel-head">
          <div>
            <h3>最近注册用户</h3>
            <span>按注册时间倒序</span>
          </div>
          <router-link :to="adminPath('users')" class="panel-more">全部用户</router-link>
        </div>
        <ul class="recent-list">
          <li v-for="user in recentUsers" :key="user.id">
            <span class="avatar">{{ user.nickname.slice(0, 1) }}</span>
            <span class="recent-main">
              <strong>{{ user.nickname }}</strong>
              <span>@{{ user.username }} · {{ user.plan }}</span>
            </span>
            <span class="recent-time">{{ user.registeredAt.slice(5) }}</span>
          </li>
        </ul>
      </article>

      <article class="panel panel-block">
        <div class="panel-head">
          <div>
            <h3>最近订单</h3>
            <span>最新 5 笔交易记录</span>
          </div>
          <router-link :to="adminPath('orders')" class="panel-more">全部订单</router-link>
        </div>
        <ul class="recent-list">
          <li v-for="order in recentOrders" :key="order.id">
            <span class="order-icon">
              <el-icon><Tickets /></el-icon>
            </span>
            <span class="recent-main">
              <strong>{{ order.user }} · ¥{{ formatMoney(order.amount) }}</strong>
              <span>{{ order.type }} · {{ order.payMethod }}</span>
            </span>
            <el-tag :type="statusOf(order.status).type" size="small" effect="plain">
              {{ statusOf(order.status).label }}
            </el-tag>
          </li>
        </ul>
      </article>
    </section>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import dayjs from 'dayjs'
import PageHeader from '@/components/PageHeader.vue'
import StatCard from '@/components/StatCard.vue'
import UsageTrend from '@/components/UsageTrend.vue'
import { loadAdminTasks, serviceShare, usageTrend } from '@/mock/admin'
import { listAdminOrders, listAdminUsers } from '@/mock/platform'
import { adminPath } from '@/config/admin'

const users = ref(listAdminUsers())
const orders = listAdminOrders()
const tasks = ref(loadAdminTasks())
const refreshing = ref(false)

const stats = computed(() => {
  const today = dayjs().format('YYYY-MM-DD')
  const month = dayjs().format('YYYY-MM')
  const paidThisMonth = orders.filter(order => order.status === 'PAID' && order.createdAt.startsWith(month))
  return {
    totalUsers: users.value.length,
    activeToday: users.value.filter(user => user.lastActiveAt.startsWith(today)).length,
    runningTasks: tasks.value.filter(task => task.status === 'ENABLED').length,
    monthlyIncome: paidThisMonth.reduce((sum, order) => sum + order.amount, 0),
    paidCount: paidThisMonth.length,
    banned: users.value.filter(user => user.status === 'BANNED').length
  }
})

const recentUsers = computed(() =>
  [...users.value].sort((a, b) => b.registeredAt.localeCompare(a.registeredAt)).slice(0, 5)
)

const recentOrders = computed(() => orders.slice(0, 5))

const shareTotal = serviceShare.reduce((sum, item) => sum + item.requests, 0)
const shareMax = Math.max(...serviceShare.map(item => item.requests))

function rankWidth(item) {
  return `${Math.round((item.requests / shareMax) * 100)}%`
}

function rankShare(item) {
  return `${((item.requests / shareTotal) * 100).toFixed(1)}%`
}

function formatMoney(value) {
  return Number(value).toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

function statusOf(status) {
  const map = {
    PAID: { label: '已完成', type: 'success' },
    PENDING: { label: '待支付', type: 'warning' },
    REFUNDED: { label: '已退款', type: 'info' }
  }
  return map[status] || { label: status, type: 'info' }
}

function refresh() {
  refreshing.value = true
  window.setTimeout(() => {
    users.value = loadAdminUsers()
    tasks.value = loadAdminTasks()
    refreshing.value = false
  }, 360)
}
</script>

<style scoped>
.dash-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.7fr) minmax(0, 1fr);
  gap: 12px;
}

.dash-grid-2 {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.panel-block {
  padding: 18px 20px 16px;
}

.panel-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.panel-head h3 {
  margin: 0 0 3px;
  font-size: 14px;
  font-weight: 600;
}

.panel-head span {
  color: var(--app-muted);
  font-size: 11.5px;
}

.panel-more {
  color: #7cb2ff;
  font-size: 12px;
  flex-shrink: 0;
}

.panel-more:hover {
  color: #bfdbfe;
}

.rank-list {
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding-top: 4px;
}

.rank-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.rank-name {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  width: 76px;
  flex-shrink: 0;
  color: var(--app-text);
  font-size: 12px;
}

.rank-name i {
  width: 7px;
  height: 7px;
  border-radius: 50%;
}

.rank-track {
  flex: 1;
  height: 6px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.06);
  overflow: hidden;
}

.rank-track span {
  display: block;
  height: 100%;
  border-radius: 999px;
  opacity: 0.85;
}

.rank-value {
  width: 54px;
  flex-shrink: 0;
  text-align: right;
  font-size: 11.5px;
  font-family: "JetBrains Mono", Consolas, monospace;
}

.rank-percent {
  width: 46px;
  flex-shrink: 0;
  text-align: right;
  color: var(--app-muted);
  font-size: 11px;
}

.recent-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
}

.recent-list li {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 0;
  border-bottom: 1px dashed var(--app-border);
}

.recent-list li:last-child {
  border-bottom: 0;
  padding-bottom: 2px;
}

.recent-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 1px;
  line-height: 1.4;
}

.recent-main strong {
  font-size: 12.5px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.recent-main span {
  color: var(--app-muted);
  font-size: 11px;
}

.recent-time {
  flex-shrink: 0;
  color: var(--app-muted);
  font-size: 11px;
}

.order-icon {
  width: 30px;
  height: 30px;
  flex-shrink: 0;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--app-primary-soft);
  color: #7cb2ff;
  font-size: 14px;
}

@media (max-width: 1180px) {
  .dash-grid,
  .dash-grid-2 {
    grid-template-columns: 1fr;
  }
}
</style>
