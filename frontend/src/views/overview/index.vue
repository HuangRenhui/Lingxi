<template>
  <div class="page">
    <PageHeader title="控制台" description="余额、用量、任务与订阅状态一览，快速回到常用创作入口。">
      <el-button round type="primary" @click="router.push('/home')">
        <el-icon><MagicStick /></el-icon>
        去创作
      </el-button>
    </PageHeader>

    <section class="stat-grid">
      <StatCard label="积分余额" :value="wallet.balance.toFixed(2)" icon="Coin" :hint="`冻结 ${wallet.frozen.toFixed(2)} 积分`" />
      <StatCard label="本月消费" :value="monthSpent.toFixed(2)" unit="积分" icon="DataLine" :hint="`累计消耗 ${wallet.totalSpent.toFixed(2)} 积分`" />
      <StatCard label="运行中任务" :value="runningCount" icon="Timer" :hint="`共 ${runningTasks.length} 个定时任务`" />
      <StatCard :label="`当前${planName}`" :value="daysLeft" unit="天" icon="Medal" :hint="`额度 ${subscription.creditsUsed} / ${subscription.creditsPerMonth}`" />
    </section>

    <section class="quick-grid">
      <button v-for="item in quickLinks" :key="item.path" type="button" class="quick-card panel" @click="router.push(item.path)">
        <span class="quick-icon">
          <el-icon><component :is="item.icon" /></el-icon>
        </span>
        <span class="quick-copy">
          <strong>{{ item.title }}</strong>
          <small>{{ item.desc }}</small>
        </span>
        <el-icon class="quick-go"><Right /></el-icon>
      </button>
    </section>

    <section class="split-grid">
      <article class="panel panel-block">
        <div class="panel-head">
          <div>
            <h3>最近使用</h3>
            <span>最近 6 次调用与扣费</span>
          </div>
          <router-link to="/console/usages" class="panel-more">全部记录</router-link>
        </div>
        <el-table :data="paged" size="small">
          <el-table-column prop="app" label="应用" min-width="110" />
          <el-table-column prop="model" label="模型" min-width="150" show-overflow-tooltip />
          <el-table-column label="积分" width="80">
            <template #default="{ row }">
              <span class="mono">{{ row.cost.toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === '成功' ? 'success' : 'danger'" size="small" effect="plain">
                {{ row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="时间" width="140" />
        </el-table>
        <TablePager v-model:page="page" :total="recentUsages.length" />
      </article>

      <article class="panel panel-block">
        <div class="panel-head">
          <div>
            <h3>近 14 天用量</h3>
            <span>调用次数与积分消耗</span>
          </div>
          <router-link to="/console/usages" class="panel-more">用量分析</router-link>
        </div>
        <UsageTrend :points="usagePoints" unit="次" />
      </article>
    </section>

    <section class="split-grid">
      <article class="panel panel-block">
        <div class="panel-head">
          <div>
            <h3>定时任务</h3>
            <span>无人值守运行状态</span>
          </div>
          <router-link to="/tasks" class="panel-more">管理任务</router-link>
        </div>
        <ul class="task-list">
          <li v-for="task in runningTasks" :key="task.id">
            <span class="task-dot" :class="classOf(task.status)" />
            <span class="task-main">
              <strong>{{ task.name }}</strong>
              <span>{{ task.schedule }} · {{ task.location }}</span>
            </span>
            <span class="task-status">{{ statusLabel(task.status) }}</span>
          </li>
        </ul>
      </article>

      <article class="panel panel-block">
        <div class="panel-head">
          <div>
            <h3>未读通知</h3>
            <span>{{ unread.length }} 条待查看</span>
          </div>
          <router-link to="/account/notifications" class="panel-more">通知中心</router-link>
        </div>
        <ul v-if="unread.length" class="notice-list">
          <li v-for="item in unread.slice(0, 4)" :key="item.id">
            <el-icon class="notice-icon"><Bell /></el-icon>
            <span class="notice-main">
              <strong>{{ item.title }}</strong>
              <span>{{ item.createdAt }}</span>
            </span>
          </li>
        </ul>
        <EmptyState v-else compact text="没有未读通知" />
      </article>
    </section>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import PageHeader from '@/components/PageHeader.vue'
import StatCard from '@/components/StatCard.vue'
import UsageTrend from '@/components/UsageTrend.vue'
import EmptyState from '@/components/EmptyState.vue'
import TablePager from '@/components/TablePager.vue'
import {
  loadNotifications,
  loadSubscription,
  loadWallet,
  loadWalletLogs,
  recentUsages,
  runningTasks,
  usagePoints
} from '@/mock/account'
import { loadPlans } from '@/mock/platform'
import { classOf } from '@/utils/text'

const router = useRouter()
const wallet = ref(loadWallet())
const subscription = ref(loadSubscription())
const walletLogs = ref(loadWalletLogs())
const notifications = ref(loadNotifications())
const page = ref(1)
const pageSize = 10

const paged = computed(() =>
  recentUsages.slice((page.value - 1) * pageSize, page.value * pageSize)
)

const quickLinks = [
  { path: '/nanobanana', title: '图片创作', desc: '海报、商品图与视觉设计', icon: 'Picture' },
  { path: '/seedance', title: '视频创作', desc: '文生视频与参考素材生成', icon: 'VideoCamera' },
  { path: '/suno', title: '音乐生成', desc: '从歌词到完整歌曲', icon: 'Headset' },
  { path: '/tasks', title: '定时任务', desc: '无人值守自动运行', icon: 'Timer' },
  { path: '/assets', title: '我的作品', desc: '统一管理与批量下载', icon: 'FolderOpened' },
  { path: '/console/wallet', title: '钱包充值', desc: '余额、充值与兑换码', icon: 'Wallet' }
]

const monthSpent = computed(() => {
  const month = dayjs().format('YYYY-MM')
  return Math.abs(
    walletLogs.value
      .filter(log => log.type === '消费' && log.createdAt.startsWith(month))
      .reduce((sum, log) => sum + log.amount, 0)
  )
})

const runningCount = computed(() => runningTasks.filter(task => task.status === 'RUNNING').length)
const planName = computed(
  () => loadPlans().find(plan => plan.key === subscription.value.planKey)?.name || '套餐'
)
const daysLeft = computed(() => Math.max(0, dayjs(subscription.value.expiresAt).diff(dayjs(), 'day')))
const unread = computed(() => notifications.value.filter(item => !item.read))

function statusLabel(status) {
  return {
    RUNNING: '运行中',
    PENDING: '排队中',
    SUCCESS: '已完成',
    FAILED: '失败'
  }[status] || status
}
</script>

<style scoped>
.quick-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.quick-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 15px 16px;
  text-align: left;
  cursor: pointer;
  color: var(--app-text);
  transition: all 0.16s ease;
}

.quick-card:hover {
  border-color: var(--app-border-strong);
  background: var(--app-surface-2);
}

.quick-icon {
  width: 36px;
  height: 36px;
  flex-shrink: 0;
  display: grid;
  place-items: center;
  border-radius: 10px;
  background: var(--app-primary-soft);
  color: #7cb2ff;
  font-size: 17px;
}

.quick-copy {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.quick-copy strong {
  font-size: 13px;
  font-weight: 600;
}

.quick-copy small {
  color: var(--app-muted);
  font-size: 11px;
}

.quick-go {
  color: #5b636e;
  font-size: 12px;
}

.split-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(0, 1fr);
  gap: 14px;
}

.panel-block {
  padding: 16px 20px 18px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.panel-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
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

.panel-more {
  color: #7cb2ff;
  font-size: 12px;
  white-space: nowrap;
}

.task-list,
.notice-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.task-list li,
.notice-list li {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 10px;
  border-radius: 10px;
  background: var(--app-surface-2);
  border: 1px solid var(--app-border);
}

.task-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  flex-shrink: 0;
  background: #6e7681;
}

.task-dot.running {
  background: #34d399;
  box-shadow: 0 0 0 4px rgba(52, 211, 153, 0.15);
}

.task-dot.pending {
  background: #fbbf24;
}

.task-dot.success {
  background: #60a5fa;
}

.task-dot.failed {
  background: #f87171;
}

.task-main,
.notice-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.task-main strong,
.notice-main strong {
  font-size: 12.5px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.task-main span,
.notice-main span {
  color: var(--app-muted);
  font-size: 11px;
}

.task-status {
  color: var(--app-muted);
  font-size: 11px;
  white-space: nowrap;
}

.notice-icon {
  color: #7cb2ff;
  font-size: 15px;
  flex-shrink: 0;
}

@media (max-width: 1080px) {
  .quick-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .split-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .quick-grid {
    grid-template-columns: 1fr;
  }
}
</style>
