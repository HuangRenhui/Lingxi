<template>
  <div class="admin-page">
    <PageHeader title="操作日志" description="后台关键操作的审计记录，便于追溯配置变更与账号操作。" flush>
      <el-button round @click="exportCsv">
        <el-icon><Download /></el-icon>
        导出 CSV
      </el-button>
    </PageHeader>

    <section class="stat-grid">
      <StatCard label="今日操作" :value="stats.today" icon="Document" hint="含系统自动任务" />
      <StatCard label="失败操作" :value="stats.failed" icon="Warning" hint="校验未通过或被拒绝" />
      <StatCard label="活跃操作人" :value="stats.operators" icon="User" hint="近 7 天有操作记录" />
      <StatCard label="覆盖模块" :value="stats.modules" icon="Grid" hint="已产生日志的后台模块" />
    </section>

    <section class="filters panel">
      <el-input v-model="keyword" class="filter-search" placeholder="搜索操作人、对象或详情" clearable>
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-select v-model="module" class="filter-item">
        <el-option label="全部模块" value="all" />
        <el-option v-for="item in modules" :key="item" :label="item" :value="item" />
      </el-select>
      <el-select v-model="result" class="filter-item">
        <el-option label="全部结果" value="all" />
        <el-option label="成功" value="SUCCESS" />
        <el-option label="失败" value="FAILED" />
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
    </section>

    <section class="panel table-panel">
      <div class="table-head">
        <h3>日志列表</h3>
        <span>共 {{ filtered.length }} 条记录</span>
      </div>

      <el-table :data="paged" size="small">
        <el-table-column prop="createdAt" label="时间" width="150" />
        <el-table-column label="操作人" width="120">
          <template #default="{ row }">
            <span class="mono">{{ row.operator }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="module" label="模块" width="120" />
        <el-table-column prop="action" label="动作" width="140" />
        <el-table-column label="对象" min-width="150">
          <template #default="{ row }">
            <span class="mono target">{{ row.target }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="IP" width="140" />
        <el-table-column label="结果" width="90">
          <template #default="{ row }">
            <el-tag :type="row.result === 'SUCCESS' ? 'success' : 'danger'" size="small" effect="plain">
              {{ row.result === 'SUCCESS' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button link size="small" @click="openDetail(row)">详情</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="table-empty">没有匹配的日志</div>
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

    <el-drawer v-model="detailVisible" title="日志详情" size="420px">
      <div v-if="detail" class="detail-list">
        <div class="detail-item">
          <span>时间</span>
          <strong>{{ detail.createdAt }}</strong>
        </div>
        <div class="detail-item">
          <span>操作人</span>
          <strong>{{ detail.operator }}</strong>
        </div>
        <div class="detail-item">
          <span>模块</span>
          <strong>{{ detail.module }}</strong>
        </div>
        <div class="detail-item">
          <span>动作</span>
          <strong>{{ detail.action }}</strong>
        </div>
        <div class="detail-item">
          <span>操作对象</span>
          <strong>{{ detail.target }}</strong>
        </div>
        <div class="detail-item">
          <span>来源 IP</span>
          <strong>{{ detail.ip }}</strong>
        </div>
        <div class="detail-item">
          <span>执行结果</span>
          <strong>{{ detail.result === 'SUCCESS' ? '成功' : '失败' }}</strong>
        </div>
        <div class="detail-item full">
          <span>详情</span>
          <strong>{{ detail.detail }}</strong>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import PageHeader from '@/components/PageHeader.vue'
import StatCard from '@/components/StatCard.vue'
import { loadOperationLogs } from '@/mock/admin-extra'
import { downloadCsv } from '@/utils/csv'

const logs = ref(loadOperationLogs())
const keyword = ref('')
const module = ref('all')
const result = ref('all')
const range = ref([])
const page = ref(1)
const pageSize = 10
const detailVisible = ref(false)
const detail = ref(null)

const modules = computed(() => Array.from(new Set(logs.value.map(item => item.module))))

const stats = computed(() => {
  const today = dayjs().format('YYYY-MM-DD')
  const weekAgo = dayjs().subtract(7, 'day')
  return {
    today: logs.value.filter(item => item.createdAt.startsWith(today)).length,
    failed: logs.value.filter(item => item.result === 'FAILED').length,
    operators: new Set(
      logs.value.filter(item => dayjs(item.createdAt).isAfter(weekAgo)).map(item => item.operator)
    ).size,
    modules: modules.value.length
  }
})

const filtered = computed(() => {
  const text = keyword.value.trim().toLowerCase()
  return logs.value.filter(item => {
    const matchKeyword =
      !text ||
      `${item.operator}${item.target}${item.detail}${item.action}`.toLowerCase().includes(text)
    const matchModule = module.value === 'all' || item.module === module.value
    const matchResult = result.value === 'all' || item.result === result.value
    let matchRange = true
    if (range.value?.length === 2) {
      const day = item.createdAt.slice(0, 10)
      matchRange = day >= dayjs(range.value[0]).format('YYYY-MM-DD') && day <= dayjs(range.value[1]).format('YYYY-MM-DD')
    }
    return matchKeyword && matchModule && matchResult && matchRange
  })
})

const paged = computed(() => filtered.value.slice((page.value - 1) * pageSize, page.value * pageSize))

watch([keyword, module, result, range], () => {
  page.value = 1
})

function resetFilters() {
  keyword.value = ''
  module.value = 'all'
  result.value = 'all'
  range.value = []
}

function openDetail(row) {
  detail.value = row
  detailVisible.value = true
}

function exportCsv() {
  downloadCsv(
    `操作日志-${dayjs().format('YYYYMMDD')}.csv`,
    ['时间', '操作人', '模块', '动作', '对象', 'IP', '结果', '详情'],
    filtered.value.map(item => [
      item.createdAt,
      item.operator,
      item.module,
      item.action,
      item.target,
      item.ip,
      item.result === 'SUCCESS' ? '成功' : '失败',
      item.detail
    ])
  )
  ElMessage.success(`已导出 ${filtered.value.length} 条日志`)
}
</script>

<style scoped>
.range-picker {
  width: 260px;
}

.target {
  color: #9ecbff;
  font-size: 11.5px;
}
</style>
