<template>
  <div class="page">
    <PageHeader
      title="我的作品"
      description="统一管理图片、视频与音频产物，支持收藏、批量下载与回收站还原。"
    >
      <el-button round @click="selectAll">
        <el-icon><Select /></el-icon>
        全选当前
      </el-button>
      <el-button round type="primary" @click="goCreate">
        <el-icon><MagicStick /></el-icon>
        开始创作
      </el-button>
    </PageHeader>

    <section class="stat-grid">
      <StatCard label="作品总数" :value="activeAssets.length" icon="Picture" hint="不含回收站" />
      <StatCard label="已收藏" :value="favoriteCount" icon="Star" hint="点击卡片星标可取消" />
      <StatCard label="回收站" :value="trashedAssets.length" icon="Delete" hint="保留 30 天后自动清理" />
    </section>

    <section class="panel asset-panel">
      <div class="asset-toolbar">
        <TabStrip v-model="tab" :items="tabs" />
      </div>

      <div class="asset-filters">
        <el-input v-model="keyword" class="filter-search" placeholder="搜索标题或提示词" clearable>
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="type" class="filter-item">
          <el-option label="全部类型" value="all" />
          <el-option label="图片" value="image" />
          <el-option label="视频" value="video" />
          <el-option label="音频" value="music" />
        </el-select>
        <el-select v-model="sort" class="filter-item">
          <el-option label="最新创建" value="new" />
          <el-option label="最早创建" value="old" />
          <el-option label="占用空间" value="size" />
        </el-select>
        <span class="asset-count">{{ filtered.length }} 个作品</span>
      </div>

      <div v-if="filtered.length" class="asset-grid">
        <article
          v-for="item in filtered"
          :key="item.id"
          class="asset-card"
          :class="{ selected: isSelected(item.id) }"
        >
          <el-checkbox
            class="asset-check"
            :model-value="isSelected(item.id)"
            @change="toggleSelect(item.id)"
          />
          <div class="asset-thumb" @click="openPreview(item)">
            <img v-if="thumbOf(item)" :src="thumbOf(item)" :alt="item.title" />
            <div v-else class="asset-audio">
              <el-icon><Headset /></el-icon>
            </div>
            <span class="asset-type">{{ typeLabel(item.type) }}</span>
            <span v-if="item.status" class="asset-review" :class="classOf(item.status)">
              {{ reviewLabel(item.status) }}
            </span>
            <span v-if="item.type !== 'image'" class="asset-play">
              <el-icon><VideoPlay /></el-icon>
            </span>
            <span v-if="item.favorite" class="asset-star">
              <el-icon><StarFilled /></el-icon>
            </span>
          </div>
          <div class="asset-body">
            <strong class="asset-title" :title="item.title">{{ item.title }}</strong>
            <span class="asset-meta">{{ item.tool }} · {{ item.size }}</span>
            <span class="asset-time">{{ item.createdAt }}</span>
            <div class="asset-actions">
              <el-button link size="small" @click="toggleFavorite(item)">
                {{ item.favorite ? '取消收藏' : '收藏' }}
              </el-button>
              <el-button link size="small" @click="download(item)">下载</el-button>
              <el-button v-if="!item.trashed" link size="small" type="danger" @click="moveToTrash(item)">
                删除
              </el-button>
              <template v-else>
                <el-button link size="small" @click="restore(item)">还原</el-button>
                <el-button link size="small" type="danger" @click="destroy(item)">彻底删除</el-button>
              </template>
            </div>
          </div>
        </article>
      </div>

      <EmptyState v-else :text="emptyText">
        <el-button v-if="tab === 'trash'" round @click="tab = 'all'">返回作品列表</el-button>
        <el-button v-else round type="primary" @click="goCreate">去创作第一件作品</el-button>
      </EmptyState>
    </section>

    <transition name="bulk">
      <div v-if="selected.length" class="bulk-bar">
        <span>已选择 <strong>{{ selected.length }}</strong> 个作品</span>
        <div class="bulk-actions">
          <el-button size="small" round @click="favoriteSelected">批量收藏</el-button>
          <el-button size="small" round @click="downloadSelected">批量下载</el-button>
          <el-button v-if="tab !== 'trash'" size="small" round type="danger" plain @click="trashSelected">
            移入回收站
          </el-button>
          <el-button v-else size="small" round type="success" plain @click="restoreSelected">批量还原</el-button>
          <el-button size="small" round link @click="selected = []">取消</el-button>
        </div>
      </div>
    </transition>

    <el-dialog v-model="previewVisible" :title="previewItem?.title || '作品预览'" width="760px">
      <div v-if="previewItem" class="preview-body">
        <img v-if="previewItem.type === 'image'" :src="previewItem.url" :alt="previewItem.title" />
        <video v-else-if="previewItem.type === 'video'" :src="previewItem.url" controls autoplay />
        <audio v-else :src="previewItem.url" controls />
        <p class="preview-prompt">{{ previewItem.prompt }}</p>
        <div class="preview-meta">
          <span>{{ previewItem.tool }}</span>
          <span>{{ previewItem.size }}</span>
          <span>{{ previewItem.createdAt }}</span>
        </div>
      </div>
      <template #footer>
        <el-button round @click="previewVisible = false">关闭</el-button>
        <el-button round type="primary" @click="download(previewItem)">下载</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import StatCard from '@/components/StatCard.vue'
import TabStrip from '@/components/TabStrip.vue'
import EmptyState from '@/components/EmptyState.vue'
import { deleteArtifact, listArtifacts, updateArtifact } from '@/api/artifact'
import { classOf } from '@/utils/text'

const router = useRouter()
const assets = ref([])
const tab = ref('all')
const keyword = ref('')
const type = ref('all')
const sort = ref('new')
const selected = ref([])
const previewVisible = ref(false)
const previewItem = ref(null)

/** 字节数转可读大小，列表排序依赖字符串里的数字 */
function formatSize(bytes) {
  const value = Number(bytes || 0)
  if (!value) return '—'
  if (value >= 1024 * 1024 * 1024) return `${(value / 1024 / 1024 / 1024).toFixed(1)} GB`
  if (value >= 1024 * 1024) return `${(value / 1024 / 1024).toFixed(1)} MB`
  if (value >= 1024) return `${(value / 1024).toFixed(1)} KB`
  return `${value} B`
}

/** 后端用 Integer 0/1 表示收藏与回收站，页面模板按布尔使用 */
function normalizeArtifact(item) {
  return {
    id: item.id,
    title: item.title || '',
    type: item.type || '',
    toolKey: item.toolKey || '',
    toolName: item.toolName || '',
    prompt: item.prompt || '',
    url: item.url || '',
    posterUrl: item.posterUrl || '',
    size: formatSize(item.sizeBytes),
    sizeBytes: Number(item.sizeBytes || 0),
    favorite: Number(item.favorite) === 1,
    trashed: Number(item.trashed) === 1,
    status: item.status || '',
    createdAt: item.createdAt || ''
  }
}

async function loadAssetsFromApi() {
  try {
    // 不传 trashed，取全部由页面按标签过滤，避免切换标签重复请求
    const list = await listArtifacts({ page: 1, size: 200 })
    assets.value = (list || []).map(normalizeArtifact)
  } catch (error) {
    assets.value = []
  }
}

onMounted(loadAssetsFromApi)

const tabs = [
  { key: 'all', label: '全部作品' },
  { key: 'favorite', label: '收藏夹' },
  { key: 'trash', label: '回收站' }
]

const activeAssets = computed(() => assets.value.filter(item => !item.trashed))
const trashedAssets = computed(() => assets.value.filter(item => item.trashed))
const favoriteCount = computed(() => assets.value.filter(item => item.favorite && !item.trashed).length)

const emptyText = computed(() => {
  if (tab.value === 'trash') return '回收站是空的'
  if (tab.value === 'favorite') return '还没有收藏的作品'
  return '还没有作品，去创作页生成第一件吧'
})

const filtered = computed(() => {
  const text = keyword.value.trim().toLowerCase()
  let list = assets.value.filter(item => {
    if (tab.value === 'trash') return item.trashed
    if (tab.value === 'favorite') return item.favorite && !item.trashed
    return !item.trashed
  })
  if (text) {
    list = list.filter(
      item =>
        String(item.title || '').toLowerCase().includes(text) ||
        String(item.prompt || '').toLowerCase().includes(text)
    )
  }
  if (type.value !== 'all') {
    list = list.filter(item => item.type === type.value)
  }
  const sorted = [...list]
  if (sort.value === 'new') sorted.sort((a, b) => (a.createdAt < b.createdAt ? 1 : -1))
  if (sort.value === 'old') sorted.sort((a, b) => (a.createdAt > b.createdAt ? 1 : -1))
  if (sort.value === 'size') {
    // size 可能是 '—' 或带单位字符串，parseFloat 得到 NaN 会让排序结果不稳定
    const sizeOf = value => {
      const num = Number.parseFloat(String(value || '').replace(/[^\d.]/g, ''))
      return Number.isFinite(num) ? num : 0
    }
    sorted.sort((a, b) => sizeOf(b.size) - sizeOf(a.size))
  }
  return sorted
})

function typeLabel(value) {
  return { image: '图片', video: '视频', music: '音频' }[value] || value
}

function reviewLabel(status) {
  return { PENDING: '待审核', APPROVED: '已通过', REJECTED: '未通过' }[status] || status
}

function thumbOf(item) {
  if (item.type === 'image') return item.url
  if (item.type === 'video') return item.poster || ''
  return ''
}

function isSelected(id) {
  return selected.value.includes(id)
}

function toggleSelect(id) {
  selected.value = isSelected(id) ? selected.value.filter(item => item !== id) : [...selected.value, id]
}

function selectAll() {
  const ids = filtered.value.map(item => item.id)
  selected.value = ids.every(id => isSelected(id))
    ? selected.value.filter(id => !ids.includes(id))
    : Array.from(new Set([...selected.value, ...ids]))
}

async function toggleFavorite(item) {
  const next = !item.favorite
  try {
    await updateArtifact(item.id, { favorite: next ? 1 : 0 })
    item.favorite = next
    ElMessage.success(next ? '已加入收藏' : '已取消收藏')
  } catch (error) {
    // 请求层已提示
  }
}

async function moveToTrash(item) {
  try {
    await updateArtifact(item.id, { trashed: 1 })
    item.trashed = true
    selected.value = selected.value.filter(id => id !== item.id)
    ElMessage.success('已移入回收站')
  } catch (error) {
    // 请求层已提示
  }
}

async function restore(item) {
  try {
    await updateArtifact(item.id, { trashed: 0 })
    item.trashed = false
    ElMessage.success('已还原到作品列表')
  } catch (error) {
    // 请求层已提示
  }
}

async function destroy(item) {
  try {
    await ElMessageBox.confirm('彻底删除后不可恢复，确认继续？', '彻底删除', {
      type: 'warning',
      confirmButtonText: '彻底删除',
      cancelButtonText: '取消'
    })
  } catch (error) {
    return
  }
  try {
    await deleteArtifact(item.id)
    assets.value = assets.value.filter(entry => entry.id !== item.id)
    selected.value = selected.value.filter(id => id !== item.id)
    ElMessage.success('已彻底删除')
  } catch (error) {
    // 请求层已提示
  }
}

// 常见可下载媒体后缀：优先按源文件真实后缀命名，避免 mp4/webm/mov 都被写成 .wav
const MEDIA_EXTENSIONS = ['.mp4', '.webm', '.mov', '.m4v', '.mkv', '.mp3', '.wav', '.m4a', '.aac', '.flac', '.jpg', '.jpeg', '.png', '.webp', '.gif']

function extensionOf(item) {
  const url = String(item?.url || '').split('?')[0].toLowerCase()
  const matched = MEDIA_EXTENSIONS.find(ext => url.endsWith(ext))
  if (matched) return matched
  // 源地址没有可识别后缀时按作品类型兜底
  if (item?.type === 'image') return '.jpg'
  if (item?.type === 'music') return '.wav'
  return '.mp4'
}

function download(item) {
  if (!item?.url) {
    ElMessage.warning('该作品的源文件不可用')
    return
  }
  const url = String(item.url)
  // 标题来自用户提示词，直接拼进文件名可能包含操作系统非法字符
  const safeTitle = String(item.title || '作品')
    .replace(/[\\/:*?"<>|]/g, '_')
    .slice(0, 60)
  const link = document.createElement('a')
  link.href = url
  link.download = `${safeTitle}${extensionOf(item)}`
  link.target = '_blank'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

async function favoriteSelected() {
  const targets = assets.value.filter(item => isSelected(item.id) && !item.favorite)
  if (!targets.length) {
    ElMessage.info('所选作品都已收藏')
    return
  }
  try {
    await Promise.all(targets.map(item => updateArtifact(item.id, { favorite: 1 })))
    targets.forEach(item => {
      item.favorite = true
    })
    ElMessage.success(`已收藏 ${targets.length} 个作品`)
  } catch (error) {
    await loadAssetsFromApi()
  }
}

// 批量下载是「串行 + 间隔」的异步流程，页面卸载后必须停止，否则会继续操作已销毁的组件
let downloading = false
let assetsUnmounted = false

onBeforeUnmount(() => {
  assetsUnmounted = true
  downloading = false
})

async function downloadSelected() {
  if (downloading) {
    ElMessage.warning('上一批下载仍在进行中，请稍候')
    return
  }
  if (!selected.value.length) {
    ElMessage.warning('请先选择作品')
    return
  }
  const list = selected.value
    .map(id => assets.value.find(entry => entry.id === id))
    .filter(item => item?.url)
  if (!list.length) {
    ElMessage.warning('所选作品暂无可下载文件')
    return
  }
  if (list.length > 1) {
    ElMessage.info(`将依次下载 ${list.length} 个文件，浏览器可能要求允许多文件下载`)
  }
  // 连续同步触发下载会被浏览器限流（通常只放行第一个），改为逐个间隔触发
  downloading = true
  let triggered = 0
  try {
    for (const item of list) {
      if (assetsUnmounted) return
      download(item)
      triggered += 1
      await new Promise(resolve => window.setTimeout(resolve, 350))
    }
    if (assetsUnmounted) return
    ElMessage.success(triggered > 1 ? `已依次触发 ${triggered} 个下载` : '已开始下载')
  } finally {
    downloading = false
  }
}

async function trashSelected() {
  const targets = assets.value.filter(item => isSelected(item.id) && !item.trashed)
  if (!targets.length) {
    ElMessage.info('请先选择作品')
    return
  }
  try {
    await Promise.all(targets.map(item => updateArtifact(item.id, { trashed: 1 })))
    targets.forEach(item => {
      item.trashed = true
    })
    ElMessage.success('已移入回收站')
    selected.value = []
  } catch (error) {
    await loadAssetsFromApi()
  }
}

async function restoreSelected() {
  const targets = assets.value.filter(item => isSelected(item.id) && item.trashed)
  if (!targets.length) {
    ElMessage.info('请先选择回收站中的作品')
    return
  }
  try {
    await Promise.all(targets.map(item => updateArtifact(item.id, { trashed: 0 })))
    targets.forEach(item => {
      item.trashed = false
    })
    ElMessage.success('已批量还原')
    selected.value = []
  } catch (error) {
    await loadAssetsFromApi()
  }
}

function openPreview(item) {
  if (item.trashed) return
  previewItem.value = item
  previewVisible.value = true
}

function goCreate() {
  router.push('/nanobanana')
}
</script>

<style scoped>
.asset-panel {
  padding: 14px 20px 24px;
}

.asset-toolbar {
  margin-bottom: 4px;
}

.asset-filters {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  padding: 14px 0 16px;
}

.filter-search {
  width: 260px;
}

.filter-item {
  width: 142px;
}

.asset-count {
  margin-left: auto;
  color: var(--app-muted);
  font-size: 11.5px;
}

.asset-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(214px, 1fr));
  gap: 14px;
}

.asset-card {
  position: relative;
  display: flex;
  flex-direction: column;
  border: 1px solid var(--app-border);
  border-radius: 12px;
  background: var(--app-surface-2);
  overflow: hidden;
  transition: all 0.16s ease;
}

.asset-card:hover {
  border-color: var(--app-border-strong);
  transform: translateY(-2px);
}

.asset-card.selected {
  border-color: rgba(96, 165, 250, 0.6);
  box-shadow: 0 0 0 1px rgba(96, 165, 250, 0.35);
}

.asset-check {
  position: absolute;
  top: 8px;
  left: 10px;
  z-index: 2;
  padding: 2px 4px;
  border-radius: 6px;
  background: rgba(8, 9, 12, 0.66);
}

.asset-thumb {
  position: relative;
  aspect-ratio: 4 / 3;
  background: #0b0e13;
  cursor: pointer;
  overflow: hidden;
}

.asset-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.24s ease;
}

.asset-thumb:hover img {
  transform: scale(1.04);
}

.asset-audio {
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  font-size: 34px;
  color: #7cb2ff;
  background: radial-gradient(220px 120px at 50% 0%, rgba(59, 130, 246, 0.22), transparent 70%);
}

.asset-type {
  position: absolute;
  left: 8px;
  bottom: 8px;
  padding: 2px 7px;
  border-radius: 6px;
  background: rgba(8, 9, 12, 0.7);
  color: #c9d1d9;
  font-size: 10.5px;
}

.asset-review {
  position: absolute;
  right: 8px;
  bottom: 8px;
  padding: 2px 7px;
  border-radius: 6px;
  background: rgba(8, 9, 12, 0.72);
  color: #fbbf24;
  font-size: 10.5px;
}

.asset-review.approved {
  color: #34d399;
}

.asset-review.rejected {
  color: #f87171;
}

.asset-play {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  width: 42px;
  height: 42px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: rgba(8, 9, 12, 0.55);
  border: 1px solid rgba(255, 255, 255, 0.24);
  color: #fff;
  font-size: 16px;
}

.asset-star {
  position: absolute;
  top: 8px;
  right: 9px;
  color: #fbbf24;
  font-size: 15px;
}

.asset-body {
  display: flex;
  flex-direction: column;
  gap: 3px;
  padding: 11px 12px 12px;
}

.asset-title {
  font-size: 13px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.asset-meta {
  color: var(--app-muted);
  font-size: 11px;
}

.asset-time {
  color: #6b7b93;
  font-size: 10.5px;
}

.asset-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 6px;
  padding-top: 8px;
  border-top: 1px solid var(--app-border);
}

.asset-actions :deep(.el-button) {
  margin-left: 0;
  padding: 0;
  font-size: 11.5px;
}

.bulk-bar {
  position: sticky;
  bottom: 18px;
  z-index: 30;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 12px 18px;
  border-radius: 999px;
  border: 1px solid rgba(96, 165, 250, 0.35);
  background: rgba(13, 17, 23, 0.94);
  backdrop-filter: blur(12px);
  box-shadow: 0 18px 44px rgba(0, 0, 0, 0.5);
  color: var(--app-muted);
  font-size: 12.5px;
}

.bulk-bar strong {
  color: #7cb2ff;
}

.bulk-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.bulk-enter-active,
.bulk-leave-active {
  transition: all 0.2s ease;
}

.bulk-enter-from,
.bulk-leave-to {
  opacity: 0;
  transform: translateY(12px);
}

.preview-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.preview-body img,
.preview-body video {
  width: 100%;
  max-height: 460px;
  object-fit: contain;
  border-radius: 10px;
  background: #05070a;
}

.preview-body audio {
  width: 100%;
}

.preview-prompt {
  margin: 0;
  padding: 10px 12px;
  border-radius: 10px;
  background: var(--app-surface-2);
  border: 1px solid var(--app-border);
  color: var(--app-muted);
  font-size: 12px;
  line-height: 1.7;
}

.preview-meta {
  display: flex;
  gap: 16px;
  color: var(--app-muted);
  font-size: 11.5px;
}

@media (max-width: 720px) {
  .filter-search,
  .filter-item {
    width: 100%;
  }

  .bulk-bar {
    flex-direction: column;
    border-radius: 14px;
  }
}
</style>
