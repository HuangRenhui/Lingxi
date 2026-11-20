<template>
  <div class="page">
    <PageHeader title="使用历史" description="查看调用流水、扣费记录与用量趋势。">
      <el-button round @click="exportCsv">
        <el-icon><Download /></el-icon>
        导出 CSV
      </el-button>
    </PageHeader>

    <section class="filters panel">
      <el-select v-model="service" placeholder="全部服务" class="filter-item">
        <el-option label="全部服务" value="all" />
        <el-option label="对话" value="对话" />
        <el-option label="图像生成" value="图像生成" />
        <el-option label="视频生成" value="视频生成" />
        <el-option label="音乐生成" value="音乐生成" />
      </el-select>
      <el-select v-model="operation" placeholder="全部接口" class="filter-item">
        <el-option label="全部接口" value="all" />
        <el-option label="chat/completions" value="chat/completions" />
        <el-option label="images/generations" value="images/generations" />
        <el-option label="videos/generations" value="videos/generations" />
        <el-option label="music/generations" value="music/generations" />
      </el-select>
      <el-date-picker
        v-model="range"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        class="range-picker"
      />
      <el-select v-model="status" placeholder="状态码" class="filter-item small">
        <el-option label="全部状态" value="all" />
        <el-option label="200" value="200" />
        <el-option label="400" value="400" />
        <el-option label="500" value="500" />
      </el-select>
      <el-button round @click="resetFilters">重置筛选</el-button>
    </section>

    <section class="stat-grid">
      <StatCard label="使用总量（积分）" :value="stats.cost" icon="Coin" hint="仅统计成功调用" />
      <StatCard label="调用次数" :value="filtered.length" icon="DataLine" :hint="`成功 ${stats.success} 次`" />
      <StatCard label="成功率" :value="stats.successRate" unit="%" icon="CircleCheck" hint="失败任务不计费" />
      <StatCard label="平均耗时" :value="stats.avgDuration" unit="s" icon="Timer" hint="视频任务耗时较长" />
    </section>

    <section class="analysis panel">
      <div class="analysis-head">
        <h3>使用分析</h3>
        <TabStrip v-model="chartTab" :items="chartTabs" />
      </div>

      <UsageTrend v-if="chartTab === 'trend'" :points="points" />

      <ul v-else class="share-list">
        <li v-for="item in share" :key="item.name">
          <span class="share-name">
            <i :style="{ background: item.color }" />
            {{ item.name }}
          </span>
          <div class="share-track">
            <span :style="{ width: `${item.percent}%`, background: item.color }" />
          </div>
          <span class="share-value">{{ item.count }} 次 · {{ item.percent }}%</span>
        </li>
      </ul>
    </section>

    <section class="panel table-panel">
      <div class="table-head">
        <h3>调用明细</h3>
        <span>共 {{ filtered.length }} 条记录</span>
      </div>
      <el-table :data="paged" size="small">
        <el-table-column prop="createdAt" label="时间" width="150" />
        <el-table-column prop="service" label="服务" min-width="110" />
        <el-table-column label="接口操作" min-width="180">
          <template #default="{ row }">
            <span class="mono op">{{ row.operation }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态码" width="90">
          <template #default="{ row }">
            <el-tag :type="row.statusCode === 200 ? 'success' : 'danger'" size="small" effect="plain">
              {{ row.statusCode }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="耗时" width="90" />
        <el-table-column label="已扣除" width="100">
          <template #default="{ row }">
            <span class="mono">{{ row.cost.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="扣后余额" width="110">
          <template #default="{ row }">
            <span class="mono muted">{{ row.balance.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="metadata" label="模型" min-width="170" show-overflow-tooltip />
        <template #empty>
          <div class="table-empty">没有匹配的调用记录</div>
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
import TabStrip from '@/components/TabStrip.vue'
import UsageTrend from '@/components/UsageTrend.vue'
import { loadUsages } from '@/mock/account'
import { downloadCsv } from '@/utils/csv'

const chartTabs = [
  { key: 'trend', label: '趋势' },
  { key: 'share', label: '占比' }
]

const rows = ref(loadUsages())
const service = ref('all')
const operation = ref('all')
const status = ref('all')
const range = ref([])
const chartTab = ref('trend')
const page = ref(1)
const pageSize = 8

const filtered = computed(() => {
  return rows.value.filter(row => {
    const matchService = service.value === 'all' || row.service === service.value
    const matchOperation = operation.value === 'all' || row.operation === operation.value
    const matchStatus = status.value === 'all' || String(row.statusCode) === String(status.value)
    let matchRange = true
    if (range.value?.length === 2) {
      const day = row.createdAt.slice(0, 10)
      matchRange =
        day >= dayjs(range.value[0]).format('YYYY-MM-DD') && day <= dayjs(range.value[1]).format('YYYY-MM-DD')
    }
    return matchService && matchOperation && matchStatus && matchRange
  })
})

const paged = computed(() =>
  filtered.value.slice((page.value - 1) * pageSize, page.value * pageSize)
)

const stats = computed(() => {
  const list = filtered.value
  const success = list.filter(row => row.statusCode === 200)
  const costs = success.reduce((sum, row) => sum + row.cost, 0)
  const durations = success
    .map(row => Number(String(row.duration).replace(/\D/g, '')))
    .filter(value => Number.isFinite(value) && value > 0)
  return {
    cost: costs.toFixed(2),
    success: success.length,
    successRate: list.length ? ((success.length / list.length) * 100).toFixed(1) : '0.0',
    avgDuration: durations.length
      ? Math.round(durations.reduce((sum, value) => sum + value, 0) / durations.length)
      : 0
  }
})

const points = computed(() => {
  const map = new Map()
  filtered.value.forEach(row => {
    const date = row.createdAt.slice(5, 10)
    const current = map.get(date) || { date, requests: 0, credits: 0 }
    current.requests += 1
    current.credits = Number((current.credits + row.cost).toFixed(2))
    map.set(date, current)
  })
  return [...map.values()].sort((a, b) => (a.date > b.date ? 1 : -1))
})

const share = computed(() => {
  const colors = { 对话: '#3b82f6', 图像生成: '#34d399', 视频生成: '#f59e0b', 音乐生成: '#a78bfa' }
  const total = filtered.value.length || 1
  return Object.keys(colors).map(name => {
    const count = filtered.value.filter(row => row.service === name).length
    return { name, count, percent: Math.round((count / total) * 100), color: colors[name] }
  })
})

watch([service, operation, status, range], () => {
  page.value = 1
})

function resetFilters() {
  service.value = 'all'
  operation.value = 'all'
  status.value = 'all'
  range.value = []
}

function exportCsv() {
  if (!filtered.value.length) {
    ElMessage.info('当前筛选条件下没有可导出的使用记录')
    return
  }
  downloadCsv(
    `使用记录-${dayjs().format('YYYYMMDD')}.csv`,
    ['时间', '服务', '接口操作', '状态码', '耗时', '已扣除', '扣后余额', '模型'],
    filtered.value.map(row => [
      row.createdAt,
      row.service,
      row.operation,
      row.statusCode,
      row.duration,
      row.cost,
      row.balance,
      row.metadata
    ])
  )
  ElMessage.success(`已导出 ${filtered.value.length} 条记录`)
}
</script>

<style scoped>
.filters {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
}

.filter-item {
  width: 168px;
}

.filter-item.small {
  width: 130px;
}

.range-picker {
  width: 280px;
}

.analysis {
  padding: 16px 20px 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.analysis-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  border-bottom: 1px solid var(--app-border);
}

.analysis-head h3 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
}

.analysis-head :deep(.tab-strip) {
  border-bottom: 0;
  gap: 18px;
}

.analysis-head :deep(.tab-strip button) {
  padding: 8px 0;
  font-size: 12px;
}

.share-list {
  list-style: none;
  margin: 0;
  padding: 4px 0 0;
  display: flex;
  flex-direction: column;
  gap: 13px;
}

.share-list li {
  display: flex;
  align-items: center;
  gap: 12px;
}

.share-name {
  display: flex;
  align-items: center;
  gap: 7px;
  width: 92px;
  flex-shrink: 0;
  font-size: 12.5px;
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
  width: 108px;
  color: var(--app-muted);
  font-size: 11.5px;
  text-align: right;
}

.table-panel {
  padding: 16px 20px 8px;
}

.table-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
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

.op {
  color: #9ecbff;
  font-size: 11.5px;
}

.table-empty {
  padding: 34px 0;
  color: var(--app-muted);
  font-size: 12px;
}

.pager {
  display: flex;
  justify-content: flex-end;
  padding: 10px 0;
}

@media (max-width: 1000px) {
  .range-picker {
    width: 100%;
  }
}
</style>
