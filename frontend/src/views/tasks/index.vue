<template>
  <div class="page">
    <PageHeader title="定时任务" description="无人值守 Agent：按频率自动执行，支持模板变量与轮次护栏。">
      <el-dropdown split-button type="primary" @click="openCreate" @command="onCreateCommand">
        新建
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="template">从模板新建</el-dropdown-item>
            <el-dropdown-item command="custom">自定义创建</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </PageHeader>

    <TabStrip v-model="tab" :items="tabs" />

    <template v-if="tab === 'tasks'">
      <section class="hero panel">
        <div class="hero-copy">
          <h2>让 AI 按时替你完成工作</h2>
          <p>选择一个官方模板并连接所需账号。任务创建后可在卡片上立即运行。</p>
        </div>
        <div class="hero-actions">
          <el-button round @click="scrollToTemplates">浏览模板</el-button>
          <el-button round type="primary" @click="openCreate">自定义创建</el-button>
        </div>
      </section>

      <section ref="templateSection" class="section">
        <div class="section-head">
          <h3>官方模板</h3>
          <span>{{ templates.length }} 个</span>
        </div>
        <div class="template-grid">
          <button
            v-for="item in templates"
            :key="item.title"
            type="button"
            class="template-card panel"
            @click="useTemplate(item)"
          >
            <div class="tpl-icon">
              <el-icon><component :is="item.icon" /></el-icon>
            </div>
            <strong>{{ item.title }}</strong>
            <p>{{ item.description }}</p>
            <span class="tpl-action">使用模板 <el-icon><ArrowRight /></el-icon></span>
          </button>
        </div>
      </section>

      <section class="section">
        <div class="section-head">
          <h3>我的任务</h3>
          <span>{{ tasks.length }} 个</span>
        </div>

        <div v-if="loadError" class="load-hint">{{ loadError }}</div>

        <div v-if="demoMode" class="demo-strip">
          <el-icon><InfoFilled /></el-icon>
          <span>后端未连接，当前展示本地演示任务；新增与修改仅保存在本机。</span>
          <el-button link size="small" @click="retryConnect">重试连接</el-button>
        </div>

        <div v-if="!loading && !tasks.length" class="panel">
          <EmptyState text="还没有定时任务，先从模板创建一个吧" />
        </div>

        <div v-else v-loading="loading" class="task-grid">
          <article v-for="row in tasks" :key="row.id" class="task-card panel">
            <header class="task-head">
              <div class="task-icon">
                <el-icon><Timer /></el-icon>
              </div>
              <div class="task-title">
                <strong>{{ row.name }}</strong>
                <span>{{ row.model }}</span>
              </div>
              <el-switch
                v-model="row.status"
                active-value="ENABLED"
                inactive-value="DISABLED"
                @change="value => onStatusChange(row, value)"
              />
            </header>

            <p class="task-prompt">{{ row.promptTemplate }}</p>

            <div class="task-tags">
              <span><el-icon><Calendar /></el-icon>{{ scheduleText(row) }}</span>
              <span><el-icon><Monitor /></el-icon>{{ row.runLocation === 'CLOUD' ? '云端' : '这台设备' }}</span>
              <span><el-icon><RefreshRight /></el-icon>已运行 {{ row.runCount || 0 }} 次</span>
            </div>

            <footer class="task-foot">
              <span class="task-created">创建于 {{ formatTime(row.createdAt) }}</span>
              <div class="task-actions">
                <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
                <el-button link @click="runNow(row)">立即运行</el-button>
                <el-button link type="danger" @click="remove(row)">删除</el-button>
              </div>
            </footer>
          </article>
        </div>
      </section>
    </template>

    <div v-else class="run-wrap">
      <section class="filters panel">
        <el-select v-model="runStatus" class="filter-item">
          <el-option label="全部状态" value="all" />
          <el-option label="成功" value="SUCCESS" />
          <el-option label="失败" value="FAILED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
        <el-select v-model="runTrigger" class="filter-item">
          <el-option label="全部触发方式" value="all" />
          <el-option label="定时" value="定时" />
          <el-option label="手动" value="手动" />
        </el-select>
        <el-button round @click="resetRunFilters">重置筛选</el-button>
        <span class="filter-action muted">共 {{ filteredRuns.length }} 条运行记录</span>
      </section>

      <section class="panel table-panel">
        <el-table :data="paged" size="small">
          <el-table-column prop="taskName" label="任务" min-width="160" />
          <el-table-column prop="trigger" label="触发" width="80" />
          <el-table-column prop="startedAt" label="开始时间" width="150" />
          <el-table-column prop="duration" label="耗时" width="90" />
          <el-table-column prop="rounds" label="轮次" width="90" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="runStatusOf(row.status).type" size="small" effect="plain">
                {{ runStatusOf(row.status).label }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="90">
            <template #default="{ row }">
              <el-button link size="small" @click="showRun(row)">查看结果</el-button>
            </template>
          </el-table-column>
          <template #empty>
            <div class="run-empty">没有匹配的运行记录</div>
          </template>
        </el-table>
        <TablePager v-model:page="page" :total="filteredRuns.length" />
      </section>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑定时任务' : '新建任务'"
      width="640px"
      append-to-body
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="92px" class="task-form">
        <el-form-item label="任务名称" prop="name">
          <el-input v-model="form.name" maxlength="128" placeholder="例：每日资讯汇总" />
        </el-form-item>

        <el-form-item label="提示词" prop="promptTemplate">
          <el-input
            v-model="form.promptTemplate"
            type="textarea"
            :rows="4"
            placeholder="输入要定时执行的问题或指令，支持 {{date}}、{{run_count}}、{{last_output}} 变量"
          />
          <div class="field-hint">
            支持模板变量：<code v-pre>{{date}}</code> 今日日期，<code v-pre>{{run_count}}</code> 第几次运行，
            <code v-pre>{{last_output}}</code> 上次结果摘要
          </div>
        </el-form-item>

        <el-form-item label="模型" prop="model">
          <el-select v-model="form.model" style="width: 100%">
            <el-option v-for="item in modelOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>

        <el-form-item label="运行位置">
          <el-radio-group v-model="form.runLocation">
            <el-radio value="CLOUD">云端</el-radio>
            <el-radio value="LOCAL">这台设备</el-radio>
          </el-radio-group>
          <div class="field-hint">云端由平台按时触发，关掉电脑也会照常运行；本机执行需先在浏览器设备中完成配对。</div>
        </el-form-item>

        <el-form-item label="执行频率">
          <el-radio-group v-model="form.scheduleType" class="freq-group">
            <el-radio v-for="item in frequencyOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </el-radio>
          </el-radio-group>
          <div class="schedule-summary">
            <el-icon><Clock /></el-icon>
            执行频率：{{ formSummary }}
          </div>
        </el-form-item>

        <el-form-item v-if="form.scheduleType === 'INTERVAL'" label="间隔分钟">
          <el-input-number v-model="form.intervalMinutes" :min="1" :max="10080" controls-position="right" />
        </el-form-item>

        <el-form-item v-if="form.scheduleType === 'CRON'" label="Cron">
          <el-input v-model="form.cronExpr" placeholder="例如：0 9 * * * ?" />
        </el-form-item>

        <el-form-item v-if="needTime" label="执行时间">
          <el-time-picker
            v-model="form.executeTime"
            format="HH:mm"
            value-format="HH:mm"
            placeholder="09:00"
            style="width: 100%"
          />
          <span class="inline-hint">±{{ form.jitterSeconds }}s 抖动</span>
        </el-form-item>

        <el-form-item v-if="form.scheduleType === 'WEEKLY'" label="星期">
          <el-input v-model="form.weekDays" placeholder="1-7 逗号分隔，例如 1,3,5" />
        </el-form-item>

        <el-form-item label="随机抖动">
          <el-input-number v-model="form.jitterSeconds" :min="0" :max="60" controls-position="right" />
          <span class="inline-hint">秒，触发时随机延迟，避免整点同时执行</span>
        </el-form-item>

        <el-form-item label="时区">
          <el-select v-model="form.timezone" style="width: 100%">
            <el-option v-for="zone in timezoneOptions" :key="zone.value" :label="zone.label" :value="zone.value" />
          </el-select>
          <div class="field-hint">新任务默认使用当前设备时区，保存后固定到任务，不会随设备位置自动改变。</div>
        </el-form-item>

        <el-form-item label="授权 Skills">
          <el-select v-model="form.skills" multiple collapse-tags placeholder="选择允许此任务自动执行的 Skills" style="width: 100%">
            <el-option v-for="item in skillOptions" :key="item" :label="item" :value="item" />
          </el-select>
          <div class="field-hint">选中的 Skills 可跳过人工确认，在后台自动执行。</div>
        </el-form-item>

        <el-form-item label="授权 MCP">
          <el-select v-model="form.mcps" multiple collapse-tags placeholder="选择允许此任务自动加载的 MCP Servers" style="width: 100%">
            <el-option v-for="item in mcpOptions" :key="item" :label="item" :value="item" />
          </el-select>
          <div class="field-hint">未选中的 MCP Servers 不会在无人值守模式下加载。</div>
        </el-form-item>

        <el-form-item label="最大轮次">
          <el-input-number v-model="form.maxRounds" :min="1" :max="50" controls-position="right" />
          <span class="inline-hint">默认 50，过低会让多步骤任务提前停止</span>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import PageHeader from '@/components/PageHeader.vue'
import TabStrip from '@/components/TabStrip.vue'
import EmptyState from '@/components/EmptyState.vue'
import TablePager from '@/components/TablePager.vue'
import { createTask, deleteTask, listTasks, updateTask, updateTaskStatus } from '@/api/task'
import { fetchAllPages } from '@/utils/pagination'
import {
  loadDemoTasks,
  loadTaskRuns,
  saveDemoTasks,
  saveTaskRuns
} from '@/mock/account'
import { chargeCredits, refundCredits } from '@/mock/platform'
import {
  frequencyOptions,
  mcpOptions,
  modelOptions,
  rules,
  skillOptions,
  tabs,
  templates,
  timezoneOptions
} from './constants'

const tab = ref('tasks')
const tasks = ref([])
const loading = ref(false)
const loadError = ref('')
const runStatus = ref('all')
const runTrigger = ref('all')
const runRows = ref(loadTaskRuns())
const demoMode = ref(false)
const page = ref(1)
const pageSize = 10

const filteredRuns = computed(() =>
  runRows.value.filter(row => {
    const matchStatus = runStatus.value === 'all' || row.status === runStatus.value
    const matchTrigger = runTrigger.value === 'all' || row.trigger === runTrigger.value
    return matchStatus && matchTrigger
  })
)

const paged = computed(() =>
  filteredRuns.value.slice((page.value - 1) * pageSize, page.value * pageSize)
)

watch([runStatus, runTrigger], () => {
  page.value = 1
})

function runStatusOf(status) {
  return {
    SUCCESS: { label: '成功', type: 'success' },
    FAILED: { label: '失败', type: 'danger' },
    CANCELLED: { label: '已取消', type: 'info' },
    RUNNING: { label: '运行中', type: 'primary' }
  }[status] || { label: status, type: 'info' }
}

function resetRunFilters() {
  runStatus.value = 'all'
  runTrigger.value = 'all'
}

function showRun(row) {
  // alert 以 ESC / 关闭按钮退出时返回 rejected promise，必须 catch 否则控制台报未处理拒绝
  ElMessageBox.alert(row.result, `${row.taskName} · 运行结果`, {
    confirmButtonText: '知道了'
  }).catch(() => {})
}
const saving = ref(false)
const dialogVisible = ref(false)
const editingId = ref(null)
const formRef = ref()
const templateSection = ref()

const form = reactive(defaultForm())

const needTime = computed(() => ['DAILY', 'WEEKLY'].includes(form.scheduleType))

const formSummary = computed(() => {
  const time = form.executeTime || '09:00'
  if (form.scheduleType === 'INTERVAL') return `每隔 ${form.intervalMinutes} 分钟 · ${form.timezone}`
  if (form.scheduleType === 'CRON') return `${form.cronExpr || '未填写 Cron'} · ${form.timezone}`
  if (form.scheduleType === 'HOURLY') return `每小时整点 · ${form.timezone}`
  if (form.scheduleType === 'WEEKLY') return `每周 ${form.weekDays || '1'} ${time} · ${form.timezone}`
  return `每天 ${time} · ${form.timezone}`
})

function defaultForm() {
  return {
    name: '',
    promptTemplate: '',
    model: 'gpt-5.6-sol',
    runLocation: 'CLOUD',
    timezone: 'Asia/Shanghai',
    scheduleType: 'DAILY',
    cronExpr: '0 9 * * * ?',
    intervalMinutes: 60,
    executeTime: '09:00',
    weekDays: '1',
    jitterSeconds: 0,
    maxRounds: 50,
    skills: [],
    mcps: []
  }
}

function resetForm(source = {}) {
  Object.assign(form, defaultForm(), source)
}

async function loadTasks() {
  loading.value = true
  loadError.value = ''
  try {
    const result = await fetchAllPages(params => listTasks(params))
    // 接口异常时可能返回 null（未命中的 Mock 路由），直接赋值会让表格渲染报错
    if (!result) {
      demoMode.value = false
      tasks.value = []
      loadError.value = import.meta.env.DEV
        ? '任务数据格式异常，已切换到本地演示数据'
        : '任务数据格式异常，请稍后重试'
      if (import.meta.env.DEV) {
        demoMode.value = true
        tasks.value = loadDemoTasks()
      }
      return
    }
    tasks.value = result.list
    demoMode.value = false
  } catch (error) {
    // 只有开发环境允许回退演示数据；生产必须暴露错误，不能假装加载成功
    demoMode.value = false
    tasks.value = []
    loadError.value = import.meta.env.DEV
      ? '后端不可用，已切换到本地演示任务'
      : '无法加载任务，请检查网络或稍后重试'
    if (import.meta.env.DEV) {
      demoMode.value = true
      tasks.value = loadDemoTasks()
    }
  } finally {
    loading.value = false
  }
}

function retryConnect() {
  loadTasks()
}

function openCreate() {
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

function onCreateCommand(command) {
  if (command === 'template') {
    scrollToTemplates()
  } else {
    openCreate()
  }
}

function scrollToTemplates() {
  templateSection.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

function useTemplate(item) {
  editingId.value = null
  resetForm({
    name: item.title,
    promptTemplate: item.promptTemplate,
    model: item.model,
    scheduleType: item.scheduleType,
    executeTime: item.executeTime,
    weekDays: item.weekDays || '1'
  })
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  resetForm({
    name: row.name,
    promptTemplate: row.promptTemplate,
    model: row.model,
    runLocation: row.runLocation,
    timezone: row.timezone,
    scheduleType: row.scheduleType,
    cronExpr: row.cronExpr || '0 9 * * * ?',
    intervalMinutes: row.intervalMinutes || 60,
    executeTime: row.executeTime || '09:00',
    weekDays: row.weekDays || '1',
    jitterSeconds: row.jitterSeconds ?? 0,
    maxRounds: row.maxRounds ?? 50
  })
  dialogVisible.value = true
}

async function submit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const payload = {
      name: form.name,
      promptTemplate: form.promptTemplate,
      model: form.model,
      runLocation: form.runLocation,
      timezone: form.timezone,
      scheduleType: form.scheduleType,
      cronExpr: form.scheduleType === 'CRON' ? form.cronExpr : null,
      intervalMinutes: form.scheduleType === 'INTERVAL' ? form.intervalMinutes : null,
      executeTime: needTime.value ? form.executeTime : null,
      weekDays: form.scheduleType === 'WEEKLY' ? form.weekDays : null,
      jitterSeconds: form.jitterSeconds,
      maxRounds: form.maxRounds
    }
    if (demoMode.value) {
      if (editingId.value) {
        const target = tasks.value.find(item => item.id === editingId.value)
        if (target) Object.assign(target, payload)
      } else {
        tasks.value = [
          {
            id: `tk-${Date.now()}`,
            ...payload,
            status: 'ENABLED',
            runCount: 0,
            createdAt: dayjs().format('YYYY-MM-DD HH:mm')
          },
          ...tasks.value
        ]
      }
      saveDemoTasks(tasks.value)
      ElMessage.success(editingId.value ? '任务已更新' : '任务已创建')
      dialogVisible.value = false
      return
    }
    if (editingId.value) {
      await updateTask(editingId.value, payload)
      ElMessage.success('任务已更新')
    } else {
      await createTask(payload)
      ElMessage.success('任务已创建')
    }
    dialogVisible.value = false
    await loadTasks()
  } catch (error) {
    // 请求层已统一提示错误，这里只负责关闭 loading
  } finally {
    saving.value = false
  }
}

async function onStatusChange(row, value) {
  if (demoMode.value) {
    saveDemoTasks(tasks.value)
    ElMessage.success(value === 'ENABLED' ? '任务已启用' : '任务已停用')
    return
  }
  try {
    await updateTaskStatus(row.id, value)
    ElMessage.success(value === 'ENABLED' ? '任务已启用' : '任务已停用')
  } catch (error) {
    row.status = value === 'ENABLED' ? 'DISABLED' : 'ENABLED'
  }
}

// 运行模拟定时器：按运行记录登记，离开页面时清理并把未完成的任务置为失败
const runTimers = new Map()
let unmounted = false
// 同一任务同一时间只允许一次手动运行，避免连点重复扣费
const runningTaskIds = new Set()

/**
 * 收敛上一次会话遗留的「运行中」记录。
 * 定时器只存在于内存中，刷新/关页后不会再收敛，若不处理这些记录会永久停在 RUNNING，
 * 且当初预扣的积分不会退回。refunded 标记保证每条记录只退一次。
 */
function normalizeInterruptedRuns() {
  let dirty = false
  runRows.value.forEach(row => {
    if (row.status !== 'RUNNING' || row.refunded) return
    row.status = 'FAILED'
    row.duration = '—'
    row.rounds = '0 / 6'
    row.result = '页面刷新导致本次运行中断，预扣积分已退回。'
    row.refunded = true
    refundCredits({
      amount: 0.5,
      service: '定时任务',
      model: 'scheduled',
      reason: '页面刷新中断'
    })
    dirty = true
  })
  if (dirty) saveTaskRuns(runRows.value)
}

function runNow(row) {
  // 后端尚未提供手动触发/调度执行接口，生产环境不能只在本地模拟运行和扣费
  if (!import.meta.env.DEV) {
    ElMessage.info('定时任务执行器暂未接入后端')
    return
  }
  if (runningTaskIds.has(row.id)) {
    ElMessage.warning('该任务正在运行中，请等待本次运行结束')
    return
  }
  const charged = chargeCredits({
    amount: 0.5,
    service: '定时任务',
    operation: 'agent/tasks/run',
    model: row.model || 'scheduled',
    duration: '—'
  })
  if (!charged.ok) {
    ElMessage.warning(`积分不足，当前余额 ${charged.balance}，请先充值`)
    return
  }

  const record = {
    id: `tr-${Date.now()}`,
    taskName: row.name,
    trigger: '手动',
    startedAt: dayjs().format('YYYY-MM-DD HH:mm'),
    duration: '—',
    rounds: '0 / 6',
    status: 'RUNNING',
    result: '任务已加入执行队列，等待调度。'
  }
  runRows.value = [record, ...runRows.value]
  saveTaskRuns(runRows.value)
  tab.value = 'runs'
  runningTaskIds.add(row.id)
  ElMessage.success(`「${row.name}」已加入执行队列`)

  // 前端演示：2 秒后模拟一次完整运行（需更新响应式数组内的对象才能触发视图刷新）
  const timerId = window.setTimeout(() => {
    runTimers.delete(record.id)
    runningTaskIds.delete(row.id)
    if (unmounted) return
    const target = runRows.value.find(item => item.id === record.id)
    if (!target) return
    target.status = 'SUCCESS'
    target.duration = '0m 48s'
    target.rounds = '6 / 6'
    target.result = '手动触发的运行已完成，产物已保存到作品库。'
    saveTaskRuns(runRows.value)
    ElMessage.success(`「${row.name}」运行完成`)
  }, 2000)
  runTimers.set(record.id, { timerId, amount: 0.5 })
}

async function remove(row) {
  try {
    await ElMessageBox.confirm(`确认删除任务「${row.name}」？`, '删除确认', { type: 'warning' })
  } catch (error) {
    return
  }
  if (demoMode.value) {
    tasks.value = tasks.value.filter(item => item.id !== row.id)
    saveDemoTasks(tasks.value)
    ElMessage.success('任务已删除')
    return
  }
  try {
    await deleteTask(row.id)
    ElMessage.success('任务已删除')
    await loadTasks()
  } catch (error) {
    // 请求层已统一提示错误
  }
}

function scheduleText(row) {
  const type = row.scheduleType
  if (type === 'INTERVAL') return `每隔 ${row.intervalMinutes} 分钟`
  if (type === 'CRON') return row.cronExpr || 'Cron'
  const time = row.executeTime || '09:00'
  if (type === 'HOURLY') return `每小时 ${time}`
  if (type === 'DAILY') return `每天 ${time}`
  if (type === 'WEEKLY') return `每周 ${row.weekDays || '1'} ${time}`
  return type
}

function formatTime(value) {
  return value ? dayjs(value).format('YYYY-MM-DD HH:mm') : '-'
}

onMounted(() => {
  normalizeInterruptedRuns()
  loadTasks()
})

onBeforeUnmount(() => {
  unmounted = true
  if (!runTimers.size) return
  runTimers.forEach(({ timerId, amount }, id) => {
    window.clearTimeout(timerId)
    const target = runRows.value.find(item => item.id === id)
    if (target) {
      target.status = 'FAILED'
      target.duration = '—'
      target.rounds = '0 / 6'
      target.result = '页面已离开，本次手动运行被中断，预扣积分已退回。'
      // 标记已退费，避免下次进入页面时被 normalizeInterruptedRuns 重复退一次
      target.refunded = true
    }
    // 断开前端模拟后任务不会自行结束，需退回本次预扣积分，避免卡单与扣费丢失
    refundCredits({ amount, service: '定时任务', model: 'scheduled', reason: '页面卸载中断' })
  })
  saveTaskRuns(runRows.value)
  runTimers.clear()
  runningTaskIds.clear()
})
</script>

<style scoped src="./index.scss"></style>
