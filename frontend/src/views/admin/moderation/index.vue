<template>
  <div class="admin-page">
    <PageHeader title="内容审核" description="审核待发布的图片、视频产物，维护提示词敏感词库。" flush>
      <el-button v-if="tab === 'queue'" round type="primary" @click="approveAllLow">
        <el-icon><Select /></el-icon>
        通过全部低风险
      </el-button>
      <el-button v-else round type="primary" @click="openWord">
        <el-icon><Plus /></el-icon>
        添加敏感词
      </el-button>
    </PageHeader>

    <section class="stat-grid">
      <StatCard label="待审核" :value="stats.pending" icon="Clock" hint="按创建时间倒序排队" />
      <StatCard label="今日已审" :value="stats.reviewedToday" icon="Select" hint="含通过与驳回" />
      <StatCard label="驳回率" :value="stats.rejectRate" unit="%" icon="CircleClose" hint="近 7 天驳回占比" />
      <StatCard label="高风险内容" :value="stats.highRisk" icon="Warning" hint="命中敏感词或人工标记" />
    </section>

    <div class="panel tab-panel">
      <TabStrip v-model="tab" :items="tabs" />
    </div>

    <template v-if="tab === 'queue'">
      <section class="filters panel">
        <el-input v-model="keyword" class="filter-search" placeholder="搜索标题、用户或提示词" clearable>
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="type" class="filter-item">
          <el-option label="全部类型" value="all" />
          <el-option label="图片" value="image" />
          <el-option label="视频" value="video" />
        </el-select>
        <el-select v-model="status" class="filter-item">
          <el-option label="全部状态" value="all" />
          <el-option label="待审核" value="PENDING" />
          <el-option label="已通过" value="APPROVED" />
          <el-option label="已驳回" value="REJECTED" />
        </el-select>
        <el-select v-model="risk" class="filter-item">
          <el-option label="全部风险" value="all" />
          <el-option label="低风险" value="低" />
          <el-option label="中风险" value="中" />
          <el-option label="高风险" value="高" />
        </el-select>
        <el-button round @click="resetFilters">重置筛选</el-button>
      </section>

      <div v-if="filteredQueue.length" class="review-grid">
        <article v-for="item in filteredQueue" :key="item.id" class="panel review-card">
          <div class="review-thumb">
            <img :src="item.url" :alt="item.title" />
            <span class="review-type">{{ item.type === 'image' ? '图片' : '视频' }}</span>
            <el-tag class="review-risk" :type="riskType(item.risk)" size="small" effect="dark">
              {{ item.risk }}风险
            </el-tag>
          </div>
          <div class="review-body">
            <strong class="review-title">{{ item.title }}</strong>
            <span class="review-meta">{{ item.user }} · {{ item.tool }}</span>
            <p class="review-prompt">{{ item.prompt }}</p>
            <span class="review-time">{{ item.createdAt }}</span>
            <div class="review-actions">
              <template v-if="item.status === 'PENDING'">
                <el-button size="small" round type="success" plain @click="approve(item)">通过</el-button>
                <el-button size="small" round type="danger" plain @click="openReject(item)">驳回</el-button>
              </template>
              <el-tag v-else :type="item.status === 'APPROVED' ? 'success' : 'danger'" size="small" effect="plain">
                {{ item.status === 'APPROVED' ? '已通过' : '已驳回' }}
              </el-tag>
            </div>
          </div>
        </article>
      </div>
      <EmptyState v-else text="没有匹配的待审产物" />
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
        <el-table :data="pagedWords" size="small">
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

    <el-dialog v-model="rejectVisible" title="驳回产物" width="460px">
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
        <el-button round type="primary" @click="addWords">确认添加</el-button>
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
        <el-button round type="primary" @click="doImport">开始导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import PageHeader from '@/components/PageHeader.vue'
import StatCard from '@/components/StatCard.vue'
import TabStrip from '@/components/TabStrip.vue'
import EmptyState from '@/components/EmptyState.vue'
import TablePager from '@/components/TablePager.vue'
import { loadSensitiveWords, saveSensitiveWords } from '@/mock/admin-extra'
import { listModeration, moderateAsset } from '@/mock/platform'

const tabs = [
  { key: 'queue', label: '审核队列' },
  { key: 'word', label: '敏感词库' }
]

const tab = ref('queue')
const queue = ref(listModeration())
const words = ref(loadSensitiveWords())
const keyword = ref('')
const type = ref('all')
const status = ref('all')
const risk = ref('all')
const wordKeyword = ref('')
const wordCategory = ref('all')
const rejectVisible = ref(false)
const rejectReason = ref('')
const rejectNote = ref('')
const current = ref(null)
const wordVisible = ref(false)
const importVisible = ref(false)
const importText = ref('')
const wordPage = ref(1)
const pageSize = 10

const wordForm = reactive({ words: '', category: '交易', level: '中' })
const categories = ['交易', '暴力', '政治', '违法', '隐私', '色情', '其他']

const stats = computed(() => {
  const pending = queue.value.filter(item => item.status === 'PENDING').length
  const rejected = queue.value.filter(item => item.status === 'REJECTED').length
  const reviewed = queue.value.filter(item => item.status !== 'PENDING').length
  return {
    pending,
    reviewedToday: reviewed,
    rejectRate: reviewed ? ((rejected / reviewed) * 100).toFixed(1) : '0.0',
    highRisk: queue.value.filter(item => item.risk === '高' || item.status === 'PENDING').length
  }
})

const filteredQueue = computed(() => {
  const text = keyword.value.trim().toLowerCase()
  return queue.value.filter(item => {
    const matchKeyword =
      !text || `${item.title}${item.user}${item.prompt}`.toLowerCase().includes(text)
    const matchType = type.value === 'all' || item.type === type.value
    const matchStatus = status.value === 'all' || item.status === status.value
    const matchRisk = risk.value === 'all' || item.risk === risk.value
    return matchKeyword && matchType && matchStatus && matchRisk
  })
})

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

watch([wordKeyword, wordCategory], () => {
  wordPage.value = 1
})

function riskType(value) {
  return { 高: 'danger', 中: 'warning', 低: 'success' }[value] || 'info'
}

function resetFilters() {
  keyword.value = ''
  type.value = 'all'
  status.value = 'all'
  risk.value = 'all'
}

function approve(item) {
  moderateAsset(item.id, 'APPROVED')
  queue.value = listModeration()
  ElMessage.success('已通过审核')
}

function approveAllLow() {
  const list = queue.value.filter(item => item.status === 'PENDING' && item.risk === '低')
  if (!list.length) {
    ElMessage.info('没有低风险待审产物')
    return
  }
  list.forEach(item => moderateAsset(item.id, 'APPROVED'))
  queue.value = listModeration()
  ElMessage.success(`已批量通过 ${list.length} 个产物`)
}

function openReject(item) {
  current.value = item
  rejectReason.value = ''
  rejectNote.value = ''
  rejectVisible.value = true
}

function confirmReject() {
  if (!rejectReason.value) {
    ElMessage.warning('请选择驳回原因')
    return
  }
  if (current.value) {
    const reason = rejectNote.value.trim()
      ? `${rejectReason.value}：${rejectNote.value.trim()}`
      : rejectReason.value
    moderateAsset(current.value.id, 'REJECTED', reason)
    queue.value = listModeration()
  }
  rejectVisible.value = false
  ElMessage.success('已驳回并通知用户')
}

function toggleWord(row) {
  saveSensitiveWords(words.value)
  ElMessage.success(row.enabled ? '敏感词已启用' : '敏感词已停用')
}

async function removeWord(row) {
  try {
    await ElMessageBox.confirm(`确认删除敏感词「${row.word}」？`, '删除敏感词', { type: 'warning' })
  } catch (error) {
    return
  }
  words.value = words.value.filter(item => item.id !== row.id)
  saveSensitiveWords(words.value)
  ElMessage.success('已删除')
}

function openWord() {
  wordForm.words = ''
  wordForm.category = '交易'
  wordForm.level = '中'
  wordVisible.value = true
}

function addWords() {
  const list = wordForm.words
    .split('\n')
    .map(item => item.trim())
    .filter(Boolean)
  if (!list.length) {
    ElMessage.warning('请至少输入一个敏感词')
    return
  }
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
}

function doImport() {
  const lines = importText.value
    .split('\n')
    .map(item => item.trim())
    .filter(Boolean)
  if (!lines.length) {
    ElMessage.warning('请粘贴需要导入的内容')
    return
  }
  const created = lines.map((line, index) => {
    const [word, category, level] = line.split(/[,，]/).map(item => item?.trim())
    return {
      id: `sw-${Date.now()}-${index}`,
      word,
      category: categories.includes(category) ? category : '其他',
      level: ['高', '中', '低'].includes(level) ? level : '中',
      hits: 0,
      enabled: true,
      createdAt: dayjs().format('YYYY-MM-DD HH:mm')
    }
  })
  words.value = [...created, ...words.value]
  saveSensitiveWords(words.value)
  importVisible.value = false
  importText.value = ''
  ElMessage.success(`已导入 ${created.length} 个敏感词`)
}
</script>

<style scoped>
.tab-panel {
  padding: 6px 20px 0;
}

.review-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(268px, 1fr));
  gap: 14px;
}

.review-card {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.review-thumb {
  position: relative;
  aspect-ratio: 16 / 10;
  background: #0b0e13;
}

.review-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.review-type {
  position: absolute;
  left: 9px;
  bottom: 9px;
  padding: 2px 7px;
  border-radius: 6px;
  background: rgba(8, 9, 12, 0.72);
  color: #c9d1d9;
  font-size: 10.5px;
}

.review-risk {
  position: absolute;
  top: 9px;
  right: 9px;
}

.review-body {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 12px 14px 14px;
}

.review-title {
  font-size: 13px;
  font-weight: 600;
}

.review-meta,
.review-time {
  color: var(--app-muted);
  font-size: 11px;
}

.review-prompt {
  margin: 2px 0;
  color: var(--app-muted);
  font-size: 11.5px;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.review-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 6px;
  padding-top: 10px;
  border-top: 1px solid var(--app-border);
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
