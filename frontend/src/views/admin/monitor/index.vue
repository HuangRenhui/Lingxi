<template>
  <div class="admin-page">
    <PageHeader title="实时监控" description="任务队列、渠道健康与近 24 小时调用情况。数据每 30 秒自动刷新。" flush>
      <el-button round :loading="refreshing" @click="refresh">
        <el-icon><Refresh /></el-icon>
        刷新数据
      </el-button>
    </PageHeader>

    <section class="stat-grid">
      <StatCard label="在线用户" :value="stats.online" icon="User" hint="近 5 分钟有请求的账号" />
      <StatCard label="队列积压" :value="queue.length" icon="Timer" :hint="`其中运行中 ${stats.running} 个`" />
      <StatCard label="平均延迟" :value="stats.latency" unit="ms" icon="Odometer" hint="近 1 小时全部渠道" />
      <StatCard label="错误率" :value="stats.errorRate" unit="%" icon="Warning" hint="含上游超时与限流" />
    </section>

    <section class="monitor-grid">
      <article class="panel panel-block">
        <div class="panel-head">
          <div>
            <h3>近 24 小时调用</h3>
            <span>按小时聚合，柱高为请求次数</span>
          </div>
          <span class="muted">最后刷新 {{ lastRefresh }}</span>
        </div>
        <UsageTrend :points="hourlyPoints" />
      </article>

      <article class="panel panel-block">
        <div class="panel-head">
          <div>
            <h3>渠道健康</h3>
            <span>按成功率与延迟综合判定</span>
          </div>
        </div>
        <ul class="channel-list">
          <li v-for="item in channelHealth" :key="item.name">
            <span class="channel-dot" :class="classOf(item.status)" />
            <span class="channel-main">
              <strong>{{ item.name }}</strong>
              <span>{{ item.note }} · {{ item.requests.toLocaleString() }} 次</span>
            </span>
            <span class="channel-metric">
              <strong>{{ item.successRate }}%</strong>
              <span>{{ item.latency }}ms</span>
            </span>
          </li>
        </ul>
      </article>
    </section>

    <section class="panel table-panel">
      <div class="table-head">
        <h3>任务队列</h3>
        <span>运行中的任务可在详情中终止</span>
      </div>
      <el-table :data="pagedQueue" size="small">
        <el-table-column label="任务" min-width="180">
          <template #default="{ row }">
            <div class="task-cell">
              <strong>{{ row.name }}</strong>
              <span class="cell-sub">{{ row.user }} · {{ row.location }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="round" label="轮次" width="90" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusOf(row.status).type" size="small" effect="plain">
              {{ statusOf(row.status).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startedAt" label="开始时间" width="150" />
        <el-table-column prop="duration" label="耗时" width="100" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button
              v-if="['RUNNING', 'PENDING'].includes(row.status)"
              link
              type="danger"
              size="small"
              @click="terminate(row)"
            >
              终止
            </el-button>
            <span v-else class="muted">—</span>
          </template>
        </el-table-column>
        <template #empty>
          <div class="table-empty">队列空闲</div>
        </template>
      </el-table>
      <TablePager v-model:page="page" :total="queue.length" />
    </section>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import PageHeader from '@/components/PageHeader.vue'
import StatCard from '@/components/StatCard.vue'
import UsageTrend from '@/components/UsageTrend.vue'
import TablePager from '@/components/TablePager.vue'
import { channelHealth, hourlyPoints, loadMonitorQueue, saveMonitorQueue } from '@/mock/admin-extra'
import { classOf } from '@/utils/text'

const queue = ref(loadMonitorQueue())
const refreshing = ref(false)
const lastRefresh = ref(dayjs().format('HH:mm:ss'))
const page = ref(1)
const pageSize = 10
let timer = null
let refreshTimer = null

const pagedQueue = computed(() =>
  queue.value.slice((page.value - 1) * pageSize, page.value * pageSize)
)

const stats = computed(() => {
  const running = queue.value.filter(item => item.status === 'RUNNING').length
  return {
    online: 42 + (queue.value.length % 5),
    running,
    latency: 1180,
    errorRate: '1.8'
  }
})

function statusOf(status) {
  return {
    RUNNING: { label: '运行中', type: 'primary' },
    PENDING: { label: '排队中', type: 'warning' },
    SUCCESS: { label: '已完成', type: 'success' },
    FAILED: { label: '失败', type: 'danger' }
  }[status] || { label: status, type: 'info' }
}

function refresh() {
  if (refreshing.value) return
  refreshing.value = true
  refreshTimer = window.setTimeout(() => {
    refreshTimer = null
    // 重新读取持久化的队列，而不是只更新时间戳造成「假刷新」
    queue.value = loadMonitorQueue()
    lastRefresh.value = dayjs().format('HH:mm:ss')
    refreshing.value = false
    ElMessage.success('已刷新监控数据')
  }, 500)
}

async function terminate(row) {
  try {
    await ElMessageBox.confirm(`确认终止任务「${row.name}」？已产生的积分将按实际轮次结算。`, '终止任务', {
      type: 'warning',
      confirmButtonText: '终止'
    })
  } catch (error) {
    return
  }
  queue.value = queue.value.filter(item => item.id !== row.id)
  saveMonitorQueue(queue.value)
  ElMessage.success('任务已终止')
}

onMounted(() => {
  timer = window.setInterval(() => {
    // 定时刷新同样重新读取队列，与手动刷新保持一致，避免只更新时间戳的「假刷新」
    // 轮询回调里的异常不会冒泡到组件，必须自行兜住，否则一次读取失败就静默中断
    try {
      queue.value = loadMonitorQueue()
      lastRefresh.value = dayjs().format('HH:mm:ss')
    } catch (error) {
      ElMessage.warning('监控数据刷新失败，将在下次轮询重试')
    }
  }, 30000)
})

onBeforeUnmount(() => {
  if (timer) {
    window.clearInterval(timer)
    timer = null
  }
  if (refreshTimer) {
    window.clearTimeout(refreshTimer)
    refreshTimer = null
  }
})
</script>

<style scoped>
.monitor-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.5fr) minmax(0, 1fr);
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

.channel-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.channel-list li {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid var(--app-border);
  background: var(--app-surface-2);
}

.channel-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  flex-shrink: 0;
  background: #34d399;
}

.channel-dot.slow {
  background: #fbbf24;
}

.channel-dot.degraded {
  background: #f87171;
}

.channel-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.channel-main strong {
  font-size: 12.5px;
  font-weight: 600;
}

.channel-main span {
  color: var(--app-muted);
  font-size: 11px;
}

.channel-metric {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
}

.channel-metric strong {
  font-size: 12.5px;
  font-weight: 600;
}

.channel-metric span {
  color: var(--app-muted);
  font-size: 10.5px;
}

.task-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.task-cell strong {
  font-size: 12.5px;
  font-weight: 600;
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

.table-empty {
  padding: 30px 0;
  color: var(--app-muted);
  font-size: 12px;
}

@media (max-width: 1080px) {
  .monitor-grid {
    grid-template-columns: 1fr;
  }
}
</style>
