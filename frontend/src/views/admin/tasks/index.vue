<template>
  <div class="admin-page">
    <PageHeader title="任务管理" description="集中查看与调度所有用户的定时任务。" flush>
      <el-button round @click="exportCsv">
        <el-icon><Download /></el-icon>
        导出 CSV
      </el-button>
    </PageHeader>

    <section class="stat-grid">
      <StatCard label="任务总数" :value="stats.total" icon="Timer" hint="全部用户创建" />
      <StatCard label="启用中" :value="stats.enabled" icon="CircleCheck" hint="按计划自动执行" />
      <StatCard label="累计运行" :value="stats.runs" icon="RefreshRight" hint="含所有历史执行" />
      <StatCard label="异常任务" :value="stats.failing" icon="Warning" hint="最近一次运行失败" />
    </section>

    <section class="filters panel">
      <el-input v-model="keyword" class="filter-search" placeholder="搜索任务名称、用户或模型" clearable>
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-select v-model="status" class="filter-item">
        <el-option label="全部状态" value="all" />
        <el-option label="启用中" value="ENABLED" />
        <el-option label="已停用" value="DISABLED" />
      </el-select>
      <el-select v-model="location" class="filter-item">
        <el-option label="全部运行位置" value="all" />
        <el-option label="云端" value="云端" />
        <el-option label="本机" value="本机" />
      </el-select>
      <el-select v-model="model" class="filter-item" filterable>
        <el-option label="全部模型" value="all" />
        <el-option v-for="item in modelOptions" :key="item" :label="item" :value="item" />
      </el-select>
      <el-button round @click="resetFilters">重置筛选</el-button>
    </section>

    <section class="panel table-panel">
      <div class="table-head">
        <h3>任务列表</h3>
        <span>共 {{ filtered.length }} 个任务</span>
      </div>
      <el-table :data="paged">
        <el-table-column label="任务" min-width="230">
          <template #default="{ row }">
            <div class="task-cell">
              <strong>{{ row.name }}</strong>
              <span>{{ row.promptTemplate }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="所属用户" width="120">
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
        <el-table-column prop="model" label="模型" min-width="130" />
        <el-table-column prop="frequency" label="执行频率" width="130" />
        <el-table-column label="运行位置" width="100">
          <template #default="{ row }">
            <el-tag :type="row.location === '云端' ? 'primary' : 'info'" size="small" effect="plain">
              {{ row.location }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              active-value="ENABLED"
              inactive-value="DISABLED"
              @change="onStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="runCount" label="运行次数" width="100" />
        <el-table-column prop="lastRunAt" label="最近运行" width="160" />
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="table-empty">没有匹配的任务</div>
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

    <el-drawer v-model="drawerVisible" title="任务详情" size="520px" append-to-body>
      <template v-if="activeTask">
        <div class="drawer-head">
          <div>
            <strong>{{ activeTask.name }}</strong>
            <span>@{{ activeTask.username }} · {{ activeTask.user }}</span>
          </div>
          <el-tag :type="activeTask.status === 'ENABLED' ? 'success' : 'info'" size="small" effect="plain">
            {{ activeTask.status === 'ENABLED' ? '启用中' : '已停用' }}
          </el-tag>
        </div>

        <div class="detail-section">任务配置</div>
        <div class="detail-list">
          <div class="detail-item">
            <span>模型</span>
            <strong>{{ activeTask.model }}</strong>
          </div>
          <div class="detail-item">
            <span>执行频率</span>
            <strong>{{ activeTask.frequency }}</strong>
          </div>
          <div class="detail-item">
            <span>运行位置</span>
            <strong>{{ activeTask.location }}</strong>
          </div>
          <div class="detail-item">
            <span>时区</span>
            <strong>{{ activeTask.timezone }}</strong>
          </div>
          <div class="detail-item">
            <span>最大轮次</span>
            <strong>{{ activeTask.maxRounds }}</strong>
          </div>
          <div class="detail-item">
            <span>最近状态</span>
            <strong>{{ activeTask.lastStatus }}</strong>
          </div>
          <div class="detail-item">
            <span>累计运行</span>
            <strong>{{ activeTask.runCount }} 次</strong>
          </div>
          <div class="detail-item">
            <span>最近运行</span>
            <strong>{{ activeTask.lastRunAt }}</strong>
          </div>
          <div class="detail-item full">
            <span>创建时间</span>
            <strong>{{ activeTask.createdAt }}</strong>
          </div>
        </div>

        <div class="detail-section">提示词</div>
        <pre class="prompt-block">{{ activeTask.promptTemplate }}</pre>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import PageHeader from '@/components/PageHeader.vue'
import StatCard from '@/components/StatCard.vue'
import { saveAdminTasks } from '@/mock/admin'
import { listAdminTasks } from '@/mock/platform'
import { downloadCsv } from '@/utils/csv'

const tasks = ref(listAdminTasks())
const keyword = ref('')
const status = ref('all')
const location = ref('all')
const model = ref('all')
const page = ref(1)
const pageSize = 10
const drawerVisible = ref(false)
const activeTask = ref(null)

const modelOptions = [...new Set(tasks.value.map(task => task.model))]

const filtered = computed(() => {
  const text = keyword.value.trim().toLowerCase()
  return tasks.value.filter(task => {
    const matchKeyword =
      !text ||
      String(task.name || '').toLowerCase().includes(text) ||
      String(task.user || '').toLowerCase().includes(text) ||
      String(task.model || '').toLowerCase().includes(text)
    const matchStatus = status.value === 'all' || task.status === status.value
    const matchLocation = location.value === 'all' || task.location === location.value
    const matchModel = model.value === 'all' || task.model === model.value
    return matchKeyword && matchStatus && matchLocation && matchModel
  })
})

const paged = computed(() => filtered.value.slice((page.value - 1) * pageSize, page.value * pageSize))

const stats = computed(() => ({
  total: tasks.value.length,
  enabled: tasks.value.filter(task => task.status === 'ENABLED').length,
  runs: tasks.value.reduce((sum, task) => sum + task.runCount, 0),
  failing: tasks.value.filter(task => task.lastStatus === '失败').length
}))

watch([keyword, status, location, model], () => {
  page.value = 1
})

function resetFilters() {
  keyword.value = ''
  status.value = 'all'
  location.value = 'all'
  model.value = 'all'
}

function onStatusChange(row) {
  saveAdminTasks(tasks.value)
  ElMessage.success(`${row.name} 已${row.status === 'ENABLED' ? '启用' : '停用'}`)
}

function openDetail(row) {
  activeTask.value = row
  drawerVisible.value = true
}

function exportCsv() {
  downloadCsv(
    `任务列表-${dayjs().format('YYYYMMDD')}.csv`,
    ['任务ID', '任务名称', '所属用户', '模型', '执行频率', '运行位置', '状态', '运行次数', '最近状态', '最近运行', '创建时间'],
    filtered.value.map(task => [
      task.id,
      task.name,
      task.user,
      task.model,
      task.frequency,
      task.location,
      task.status === 'ENABLED' ? '启用中' : '已停用',
      task.runCount,
      task.lastStatus,
      task.lastRunAt,
      task.createdAt
    ])
  )
  ElMessage.success(`已导出 ${filtered.value.length} 条任务数据`)
}
</script>

<style scoped>
.task-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.task-cell strong {
  font-size: 12.5px;
  font-weight: 600;
}

.task-cell span {
  max-width: 320px;
  color: var(--app-muted);
  font-size: 11px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.drawer-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 4px 0 18px;
  border-bottom: 1px solid var(--app-border);
}

.drawer-head strong {
  display: block;
  font-size: 15px;
  margin-bottom: 3px;
}

.drawer-head span {
  color: var(--app-muted);
  font-size: 11.5px;
}
</style>
