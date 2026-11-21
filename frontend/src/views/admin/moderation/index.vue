<template>
  <div class="admin-page">
    <PageHeader title="内容审核" description="处理用户对图片、视频与文本产物的举报，维护提示词敏感词库。" flush>
      <el-button v-if="tab === 'queue'" round type="primary" :loading="bulkReviewing" @click="approveAllPending">
        <el-icon><Select /></el-icon>
        处理全部待办
      </el-button>
      <el-button v-else round type="primary" @click="openWord">
        <el-icon><Plus /></el-icon>
        添加敏感词
      </el-button>
    </PageHeader>

    <section class="stat-grid">
      <StatCard label="待处理" :value="stats.pending" icon="Clock" hint="按提交时间倒序排队" />
      <StatCard label="已处理" :value="stats.reviewed" icon="Select" hint="含已处理与已驳回" />
      <StatCard label="驳回率" :value="stats.rejectRate" unit="%" icon="CircleClose" hint="已处理举报中的驳回占比" />
      <StatCard label="举报总数" :value="stats.total" icon="Warning" hint="全部举报记录" />
    </section>

    <div class="panel tab-panel">
      <TabStrip v-model="tab" :items="tabs" />
    </div>

    <template v-if="tab === 'queue'">
      <section class="filters panel">
        <el-input v-model="keyword" class="filter-search" placeholder="搜索举报原因或对象 ID" clearable>
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="type" class="filter-item">
          <el-option label="全部类型" value="all" />
          <el-option label="图片" value="IMAGE" />
          <el-option label="视频" value="VIDEO" />
          <el-option label="产物" value="ARTIFACT" />
          <el-option label="文本" value="TEXT" />
        </el-select>
        <el-select v-model="status" class="filter-item">
          <el-option label="全部状态" value="all" />
          <el-option label="待处理" value="PENDING" />
          <el-option label="已处理" value="RESOLVED" />
          <el-option label="已驳回" value="REJECTED" />
        </el-select>
        <el-button round @click="resetFilters">重置筛选</el-button>
        <span class="filter-action muted">共 {{ filteredQueue.length }} 条举报</span>
      </section>

      <section class="panel table-panel">
        <el-table v-loading="loading" :data="queuePaged" size="small">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column label="举报对象" width="150">
            <template #default="{ row }">
              <el-tag size="small" effect="plain">{{ targetTypeLabel(row.targetType) }}</el-tag>
              <span class="mono report-target">#{{ row.targetId }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="reason" label="举报原因" min-width="220" />
          <el-table-column label="举报人" width="110">
            <template #default="{ row }">
              <span :class="row.reporterId ? 'mono' : 'muted'">{{ row.reporterId || '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="reportStatusOf(row.status).type" size="small" effect="plain">
                {{ reportStatusOf(row.status).label }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="处理结果" min-width="160">
            <template #default="{ row }">
              <span :class="row.result ? '' : 'muted'">{{ row.result || '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="提交时间" width="150">
            <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <div class="actions">
                <el-button v-if="row.status === 'PENDING'" link size="small" type="success" @click="approve(row)">
                  处理
                </el-button>
                <el-button v-if="row.status === 'PENDING'" link size="small" type="danger" @click="openReject(row)">
                  驳回
                </el-button>
                <el-button link size="small" @click="removeReport(row)">删除</el-button>
              </div>
            </template>
          </el-table-column>
          <template #empty>
            <div class="table-empty">没有匹配的举报记录</div>
          </template>
        </el-table>
        <TablePager v-model:page="page" :total="filteredQueue.length" />
      </section>
    </template>

    <template v-else>
      <section class="filters panel">
        <el-input v-model="wordKeyword" class="filter-search" placeholder="搜索敏感词或分类" clearable>
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="wordCategory" class="filter-item">
          <el-option label="全部分类" value="all" />
          <el-option v-for="item in categories" :key="item" :label="item" :value="item" />
        </el-select>
        <el-button round @click="importVisible = true">批量导入</el-button>
        <span class="filter-action muted">共 {{ filteredWords.length }} 条</span>
      </section>

      <section class="panel table-panel">
        <el-table v-loading="wordLoading" :data="pagedWords" size="small">
          <el-table-column prop="word" label="敏感词" min-width="180">
            <template #default="{ row }">
              <span class="word">{{ row.word }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="category" label="分类" width="110" />
          <el-table-column label="级别" width="100">
            <template #default="{ row }">
              <el-tag :type="row.level === '高' ? 'danger' : 'warning'" size="small" effect="plain">
                {{ row.level }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="hits" label="命中次数" width="110" />
          <el-table-column prop="createdAt" label="添加时间" width="150" />
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-switch v-model="row.enabled" @change="toggleWord(row)" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="90">
            <template #default="{ row }">
              <el-button link type="danger" size="small" @click="removeWord(row)">删除</el-button>
            </template>
          </el-table-column>
          <template #empty>
            <div class="table-empty">没有匹配的敏感词</div>
          </template>
        </el-table>
        <TablePager v-model:page="wordPage" :total="filteredWords.length" />
      </section>
    </template>

    <el-dialog v-model="rejectVisible" title="驳回举报" width="460px">
      <el-form label-position="top">
        <el-form-item label="驳回原因">
          <el-select v-model="rejectReason" placeholder="选择常见原因">
            <el-option label="含违规或敏感内容" value="含违规或敏感内容" />
            <el-option label="含未授权品牌标识" value="含未授权品牌标识" />
            <el-option label="人物肖像未经授权" value="人物肖像未经授权" />
            <el-option label="画面质量不达标" value="画面质量不达标" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="补充说明">
          <el-input v-model="rejectNote" type="textarea" :rows="3" maxlength="120" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button round @click="rejectVisible = false">取消</el-button>
        <el-button round type="danger" @click="confirmReject">确认驳回</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="wordVisible" title="添加敏感词" width="460px">
      <el-form label-position="top">
        <el-form-item label="敏感词（每行一个）">
          <el-input v-model="wordForm.words" type="textarea" :rows="4" placeholder="支持一次添加多个，换行分隔" />
        </el-form-item>
        <div class="form-row">
          <el-form-item label="分类">
            <el-select v-model="wordForm.category">
              <el-option v-for="item in categories" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="级别">
            <el-select v-model="wordForm.level">
              <el-option label="高" value="高" />
              <el-option label="中" value="中" />
              <el-option label="低" value="低" />
            </el-select>
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button round @click="wordVisible = false">取消</el-button>
        <el-button round type="primary" :loading="wordSubmitting" @click="addWords">确认添加</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="importVisible" title="批量导入敏感词" width="460px">
      <el-input
        v-model="importText"
        type="textarea"
        :rows="8"
        placeholder="每行一条，格式：敏感词,分类,级别（逗号分隔，分类与级别可省略）"
      />
      <template #footer>
        <el-button round @click="importVisible = false">取消</el-button>
        <el-button round type="primary" :loading="wordSubmitting" @click="doImport">开始导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import PageHeader from '@/components/PageHeader.vue'
import StatCard from '@/components/StatCard.vue'
import TabStrip from '@/components/TabStrip.vue'
import TablePager from '@/components/TablePager.vue'
import { loadSensitiveWords, saveSensitiveWords } from '@/mock/admin-extra'
import { listModeration } from '@/mock/platform'
import {
  deleteAdminModerationReport,
  listAdminModerationReports,
  updateAdminModerationReport
} from '@/api/adminModerationReport'
import {
  createAdminSensitiveWords,
  deleteAdminSensitiveWord,
  listAdminSensitiveWords,
  updateAdminSensitiveWord
} from '@/api/adminSensitiveWord'
import { fetchAllPages } from '@/utils/pagination'

const tabs = [
  { key: 'queue', label: '举报队列' },
  { key: 'word', label: '敏感词库' }
]

const tab = ref('queue')
const queue = ref([])
const demoMode = ref(false)
const loading = ref(false)
const words = ref([])
const wordDemoMode = ref(false)
const wordLoading = ref(false)
const wordSubmitting = ref(false)
const keyword = ref('')
const type = ref('all')
const status = ref('all')
const wordKeyword = ref('')
const wordCategory = ref('all')
const rejectVisible = ref(false)
const rejectReason = ref('')
const rejectNote = ref('')
const current = ref(null)
const wordVisible = ref(false)
const importVisible = ref(false)
const importText = ref('')
const page = ref(1)
const wordPage = ref(1)
const pageSize = 10

const wordForm = reactive({ words: '', category: '交易', level: '中' })
const categories = ['交易', '暴力', '政治', '违法', '隐私', '色情', '其他']

/** 后端状态与页面展示的映射 */
function reportStatusOf(value) {
  return (
    {
      PENDING: { label: '待处理', type: 'warning' },
      RESOLVED: { label: '已处理', type: 'success' },
      REJECTED: { label: '已驳回', type: 'danger' }
    }[value] || { label: value || '—', type: 'info' }
  )
}

function targetTypeLabel(value) {
  return { IMAGE: '图片', VIDEO: '视频', ARTIFACT: '产物', TEXT: '文本' }[value] || value || '—'
}

function formatTime(value) {
  return value ? dayjs(value).format('YYYY-MM-DD HH:mm') : '—'
}

/** 后端不可用时，把演示用的产物审核队列转换为举报记录结构 */
function fromDemoReport(item, index) {
  return {
    id: item.id,
    targetType: item.type === 'video' ? 'VIDEO' : 'IMAGE',
    targetId: index + 1,
    reason: item.prompt || item.title,
    reporterId: null,
    status: item.status === 'APPROVED' ? 'RESOLVED' : item.status,
    result: item.status === 'APPROVED' ? '审核通过' : item.status === 'REJECTED' ? '含违规或敏感内容' : '',
    createdAt: item.createdAt,
    updatedAt: item.createdAt
  }
}

async function loadQueue() {
  loading.value = true
  try {
    // 逐页取完，避免举报总数超过单页 100 条时后面的记录不可见
    const page = await fetchAllPages(params => listAdminModerationReports(params))
    if (page) {
      queue.value = page.list
      demoMode.value = false
      if (page.truncated) {
        ElMessage.warning('举报记录超过 1000 条，仅加载前 1000 条')
      }
    } else if (import.meta.env.DEV) {
      queue.value = listModeration().map(fromDemoReport)
      demoMode.value = true
    } else {
      queue.value = []
      demoMode.value = false
    }
  } catch (error) {
    queue.value = []
    demoMode.value = false
    if (import.meta.env.DEV) {
      queue.value = listModeration().map(fromDemoReport)
      demoMode.value = true
    }
  } finally {
    loading.value = false
  }
}

/**
 * 加载敏感词库：优先服务端，后端不可用时在开发环境下回退本地演示数据。
 * 词库必须落在服务端，否则换设备/清缓存后会丢失。
 */
async function loadWords() {
  wordLoading.value = true
  try {
    const page = await fetchAllPages(params => listAdminSensitiveWords(params))
    if (page) {
      words.value = page.list.map(item => ({ ...item, enabled: item.enabled === true }))
      wordDemoMode.value = false
      if (page.truncated) {
        ElMessage.warning('敏感词超过 1000 条，仅加载前 1000 条')
      }
    } else if (import.meta.env.DEV) {
      words.value = loadSensitiveWords()
      wordDemoMode.value = true
    } else {
      words.value = []
      wordDemoMode.value = false
    }
  } catch (error) {
    words.value = import.meta.env.DEV ? loadSensitiveWords() : []
    wordDemoMode.value = import.meta.env.DEV
  } finally {
    wordLoading.value = false
  }
}

onMounted(() => {
  loadQueue()
  loadWords()
})

const stats = computed(() => {
  const pending = queue.value.filter(item => item.status === 'PENDING').length
  const rejected = queue.value.filter(item => item.status === 'REJECTED').length
  const reviewed = queue.value.filter(item => item.status !== 'PENDING').length
  return {
    pending,
    reviewed,
    rejectRate: reviewed ? ((rejected / reviewed) * 100).toFixed(1) : '0.0',
    total: queue.value.length
  }
})

const filteredQueue = computed(() => {
  const text = keyword.value.trim().toLowerCase()
  return queue.value.filter(item => {
    const haystack = `${item.targetType || ''}${item.reason || ''}${item.targetId || ''}${item.reporterId || ''}`
    const matchKeyword = !text || haystack.toLowerCase().includes(text)
    const matchType = type.value === 'all' || item.targetType === type.value
    const matchStatus = status.value === 'all' || item.status === status.value
    return matchKeyword && matchType && matchStatus
  })
})

const queuePaged = computed(() =>
  filteredQueue.value.slice((page.value - 1) * pageSize, page.value * pageSize)
)

const filteredWords = computed(() => {
  const text = wordKeyword.value.trim().toLowerCase()
  return words.value.filter(item => {
    const matchKeyword = !text || `${item.word}${item.category}`.toLowerCase().includes(text)
    const matchCategory = wordCategory.value === 'all' || item.category === wordCategory.value
    return matchKeyword && matchCategory
  })
})

const pagedWords = computed(() =>
  filteredWords.value.slice((wordPage.value - 1) * pageSize, wordPage.value * pageSize)
)

watch([keyword, type, status], () => {
  page.value = 1
})

watch([wordKeyword, wordCategory], () => {
  wordPage.value = 1
})

function resetFilters() {
  keyword.value = ''
  type.value = 'all'
  status.value = 'all'
}

async function approve(row) {
  if (demoMode.value) {
    row.status = 'RESOLVED'
    row.result = '审核通过'
    ElMessage.success('已处理举报')
    return
  }
  try {
    await updateAdminModerationReport(row.id, { status: 'RESOLVED', result: '审核通过' })
    await loadQueue()
    ElMessage.success('已处理举报')
  } catch (error) {
    // 请求层已统一提示错误
  }
}

const bulkReviewing = ref(false)

async function approveAllPending() {
  const list = queue.value.filter(item => item.status === 'PENDING')
  if (!list.length) {
    ElMessage.info('没有待处理的举报')
    return
  }
  try {
    await ElMessageBox.confirm(
      `将把 ${list.length} 条待处理举报全部标记为已处理，该操作不可逐条撤销，确定继续吗？`,
      '处理全部待办',
      { confirmButtonText: '全部通过', cancelButtonText: '取消', type: 'warning' }
    )
  } catch (error) {
    return
  }
  if (demoMode.value) {
    list.forEach(item => {
      item.status = 'RESOLVED'
      item.result = '审核通过'
    })
    ElMessage.success(`已处理 ${list.length} 条举报`)
    return
  }
  if (bulkReviewing.value) {
    return
  }
  bulkReviewing.value = true
  try {
    // 逐条提交并汇总结果：部分失败时刷新列表，让管理员准确看到剩余待办，
    // 而不是笼统失败后重复点击导致重复处理。
    const results = await Promise.allSettled(
      list.map(item => updateAdminModerationReport(item.id, { status: 'RESOLVED', result: '审核通过' }))
    )
    const failed = results.filter(result => result.status === 'rejected').length
    await loadQueue()
    if (failed > 0) {
      ElMessage.error(`${list.length - failed} 条成功，${failed} 条失败，请重试剩余待办`)
      return
    }
    ElMessage.success(`已处理 ${list.length} 条举报`)
  } finally {
    bulkReviewing.value = false
  }
}

function openReject(row) {
  current.value = row
  rejectReason.value = ''
  rejectNote.value = ''
  rejectVisible.value = true
}

async function confirmReject() {
  if (!rejectReason.value) {
    ElMessage.warning('请选择驳回原因')
    return
  }
  if (current.value) {
    const result = rejectNote.value.trim()
      ? `${rejectReason.value}：${rejectNote.value.trim()}`
      : rejectReason.value
    if (demoMode.value) {
      current.value.status = 'REJECTED'
      current.value.result = result
    } else {
      try {
        await updateAdminModerationReport(current.value.id, { status: 'REJECTED', result })
        await loadQueue()
      } catch (error) {
        rejectVisible.value = false
        return
      }
    }
  }
  rejectVisible.value = false
  ElMessage.success('已驳回举报')
}

async function removeReport(row) {
  try {
    await ElMessageBox.confirm(`确认删除举报 #${row.id}？`, '删除举报', { type: 'warning' })
  } catch (error) {
    return
  }
  if (demoMode.value) {
    queue.value = queue.value.filter(item => item.id !== row.id)
    ElMessage.success('已删除')
    return
  }
  try {
    await deleteAdminModerationReport(row.id)
    await loadQueue()
    ElMessage.success('已删除')
  } catch (error) {
    // 请求层已统一提示错误
  }
}

async function toggleWord(row) {
  const next = row.enabled === true
  if (wordDemoMode.value) {
    saveSensitiveWords(words.value)
    ElMessage.success(next ? '敏感词已启用' : '敏感词已停用')
    return
  }
  try {
    await updateAdminSensitiveWord(row.id, { enabled: next })
    ElMessage.success(next ? '敏感词已启用' : '敏感词已停用')
  } catch (error) {
    // 失败时回滚开关，避免界面状态与后端不一致
    row.enabled = !next
  }
}

async function removeWord(row) {
  try {
    await ElMessageBox.confirm(`确认删除敏感词「${row.word}」？`, '删除敏感词', { type: 'warning' })
  } catch (error) {
    return
  }
  if (wordDemoMode.value) {
    words.value = words.value.filter(item => item.id !== row.id)
    saveSensitiveWords(words.value)
    ElMessage.success('已删除')
    return
  }
  try {
    await deleteAdminSensitiveWord(row.id)
    await loadWords()
    ElMessage.success('已删除')
  } catch (error) {
    // 请求层已统一提示错误
  }
}

function openWord() {
  wordForm.words = ''
  wordForm.category = '交易'
  wordForm.level = '中'
  wordVisible.value = true
}

async function addWords() {
  if (wordSubmitting.value) return
  const list = wordForm.words
    .split('\n')
    .map(item => item.trim())
    .filter(Boolean)
  if (!list.length) {
    ElMessage.warning('请至少输入一个敏感词')
    return
  }
  if (wordDemoMode.value) {
    const created = list.map((word, index) => ({
      id: `sw-${Date.now()}-${index}`,
      word,
      category: wordForm.category,
      level: wordForm.level,
      hits: 0,
      enabled: true,
      createdAt: dayjs().format('YYYY-MM-DD HH:mm')
    }))
    words.value = [...created, ...words.value]
    saveSensitiveWords(words.value)
    wordVisible.value = false
    ElMessage.success(`已添加 ${created.length} 个敏感词`)
    return
  }
  wordSubmitting.value = true
  try {
    const created = await createAdminSensitiveWords({
      words: list,
      category: wordForm.category,
      level: wordForm.level
    })
    await loadWords()
    wordVisible.value = false
    ElMessage.success(`已添加 ${Array.isArray(created) ? created.length : list.length} 个敏感词`)
  } catch (error) {
    // 请求层已统一提示错误
  } finally {
    wordSubmitting.value = false
  }
}

async function doImport() {
  if (wordSubmitting.value) return
  const lines = importText.value
    .split('\n')
    .map(item => item.trim())
    .filter(Boolean)
  if (!lines.length) {
    ElMessage.warning('请粘贴需要导入的内容')
    return
  }
  // 后端按「分类 + 级别」批量写入，这里先按这两个维度分组
  const groups = new Map()
  for (const line of lines) {
    const [word, category, level] = line.split(/[,，]/).map(item => item?.trim())
    if (!word) continue
    const resolvedCategory = categories.includes(category) ? category : '其他'
    const resolvedLevel = ['高', '中', '低'].includes(level) ? level : '中'
    const key = `${resolvedCategory}|${resolvedLevel}`
    if (!groups.has(key)) {
      groups.set(key, { category: resolvedCategory, level: resolvedLevel, words: [] })
    }
    groups.get(key).words.push(word)
  }
  if (!groups.size) {
    ElMessage.warning('没有解析到有效的敏感词')
    return
  }

  if (wordDemoMode.value) {
    const created = []
    let index = 0
    for (const group of groups.values()) {
      for (const word of group.words) {
        created.push({
          id: `sw-${Date.now()}-${index++}`,
          word,
          category: group.category,
          level: group.level,
          hits: 0,
          enabled: true,
          createdAt: dayjs().format('YYYY-MM-DD HH:mm')
        })
      }
    }
    words.value = [...created, ...words.value]
    saveSensitiveWords(words.value)
    importVisible.value = false
    importText.value = ''
    ElMessage.success(`已导入 ${created.length} 个敏感词`)
    return
  }

  wordSubmitting.value = true
  try {
    let total = 0
    for (const group of groups.values()) {
      const created = await createAdminSensitiveWords(group)
      total += Array.isArray(created) ? created.length : group.words.length
    }
    await loadWords()
    importVisible.value = false
    importText.value = ''
    ElMessage.success(`已导入 ${total} 个敏感词`)
  } catch (error) {
    // 请求层已统一提示错误；已成功的分组会在列表中体现，可重新导入剩余部分
  } finally {
    wordSubmitting.value = false
  }
}
</script>

<style scoped>
.tab-panel {
  padding: 6px 20px 0;
}

.report-target {
  margin-left: 6px;
  color: var(--app-muted);
  font-size: 11.5px;
}

.actions {
  display: flex;
  align-items: center;
  gap: 4px;
}

.word {
  font-family: "JetBrains Mono", Consolas, monospace;
  font-size: 12px;
  color: #f0b3b3;
}

.table-panel {
  padding: 8px 20px 10px;
}

.table-empty {
  padding: 30px 0;
  color: var(--app-muted);
  font-size: 12px;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}
</style>
