<template>
  <div class="admin-page">
    <PageHeader title="用量记录" description="全平台调用流水与积分消耗。" flush>
      <el-button round @click="exportCsv">
        <el-icon><Download /></el-icon>
        导出 CSV
      </el-button>
    </PageHeader>

    <section class="stat-grid">
      <StatCard label="近 14 天消耗" :value="trendTotals.credits" unit="积分" icon="Coin" hint="按实际扣费统计" />
      <StatCard label="近 14 天调用" :value="trendTotals.requests" unit="次" icon="DataLine" hint="全部服务合计" />
      <StatCard label="成功率" :value="`${successRate}%`" icon="CircleCheck" :hint="`样本 ${records.length} 条调用`" />
      <StatCard label="平均耗时" :value="`${avgDuration}s`" icon="Timer" hint="按最近调用流水计算" />
    </section>

    <section class="panel trend-panel">
      <div class="table-head">
        <h3>近 14 天调用趋势</h3>
        <span>柱状高度按每日调用次数绘制</span>
      </div>
      <UsageTrend :points="usageTrend" />
    </section>

    <section class="filters panel">
      <el-input v-model="keyword" class="filter-search" placeholder="搜索用户或接口" clearable>
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-select v-model="service" class="filter-item">
        <el-option label="全部服务" value="all" />
        <el-option label="对话" value="对话" />
        <el-option label="图像生成" value="图像生成" />
        <el-option label="视频生成" value="视频生成" />
        <el-option label="音乐生成" value="音乐生成" />
      </el-select>
      <el-select v-model="status" class="filter-item">
        <el-option label="全部状态码" value="all" />
        <el-option label="200" :value="200" />
        <el-option label="400" :value="400" />
        <el-option label="500" :value="500" />
      </el-select>
      <el-button round @click="resetFilters">重置筛选</el-button>
    </section>

    <section class="panel table-panel">
      <div class="table-head">
        <h3>调用明细</h3>
        <span>共 {{ filtered.length }} 条记录</span>
      </div>
      <el-table :data="paged">
        <el-table-column prop="time" label="时间" width="160" />
        <el-table-column label="用户" width="130">
          <template #default="{ row }">
            <div class="user-cell">
              <span class="avatar">{{ row.user.slice(0, 1) }}</span>
              <span class="user-cell-main">
                <strong>{{ row.user }}</strong>
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="service" label="服务" width="110" />
        <el-table-column prop="operation" label="接口操作" min-width="260" />
        <el-table-column label="状态码" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.statusCode)" size="small" effect="plain">{{ row.statusCode }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="耗时" width="90" />
        <el-table-column label="扣除积分" width="110">
          <template #default="{ row }">
            <span class="money">{{ Number(row.credits).toFixed(2) }}</span>
          </template>
        </el-table-column>
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
import UsageTrend from '@/components/UsageTrend.vue'
import { usageTrend } from '@/mock/admin'
import { listAdminUsageRecords } from '@/mock/platform'
import { downloadCsv } from '@/utils/csv'

const records = listAdminUsageRecords()
const keyword = ref('')
const service = ref('all')
const status = ref('all')
const page = ref(1)
const pageSize = 10

const filtered = computed(() => {
  const text = keyword.value.trim().toLowerCase()
  return records.filter(record => {
    const matchKeyword =
      !text ||
      String(record.user || '').toLowerCase().includes(text) ||
      String(record.operation || '').toLowerCase().includes(text)
    const matchService = service.value === 'all' || record.service === service.value
    const matchStatus = status.value === 'all' || record.statusCode === status.value
    return matchKeyword && matchService && matchStatus
  })
})

const paged = computed(() => filtered.value.slice((page.value - 1) * pageSize, page.value * pageSize))

const trendTotals = usageTrend.reduce(
  (acc, point) => ({
    requests: acc.requests + point.requests,
    credits: acc.credits + point.credits
  }),
  { requests: 0, credits: 0 }
)

const successCount = records.filter(record => record.statusCode === 200).length
// 记录为空或时长为「—」时不能产生 NaN/Infinity
const successRate = records.length ? ((successCount / records.length) * 100).toFixed(1) : '0.0'
const avgDuration = records.length
  ? (
      records.reduce((sum, record) => {
        const value = Number.parseFloat(record.duration)
        return sum + (Number.isFinite(value) ? value : 0)
      }, 0) / records.length
    ).toFixed(2)
  : '0.00'

watch([keyword, service, status], () => {
  page.value = 1
})

function resetFilters() {
  keyword.value = ''
  service.value = 'all'
  status.value = 'all'
}

function statusTag(code) {
  if (code === 200) return 'success'
  if (code === 400) return 'warning'
  return 'danger'
}

function exportCsv() {
  downloadCsv(
    `用量记录-${dayjs().format('YYYYMMDD')}.csv`,
    ['时间', '用户', '服务', '接口操作', '状态码', '耗时', '扣除积分'],
    filtered.value.map(record => [
      record.time,
      record.user,
      record.service,
      record.operation,
      record.statusCode,
      record.duration,
      record.credits
    ])
  )
  ElMessage.success(`已导出 ${filtered.value.length} 条调用记录`)
}
</script>

<style scoped>
.trend-panel {
  padding: 4px 20px 18px;
}
</style>
