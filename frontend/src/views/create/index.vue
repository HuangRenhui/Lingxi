<template>
  <div class="studio-page">
    <aside class="params">
      <div class="params-scroll">
        <div class="tool-hero">
          <span class="tool-hero-icon">
            <AppIcon :name="tool.key" :alt="tool.title" />
          </span>
          <div class="tool-hero-copy">
            <strong>{{ tool.title }}</strong>
            <span>{{ tool.description }}</span>
            <em>{{ currentModelName }} · {{ currentPriceLabel }} 积分 / 次</em>
          </div>
        </div>

        <div v-for="item in renderItems" :key="item.key" class="item">
          <div v-if="item.kind === 'prompt'" class="field">
            <div class="label-row">
              <label>
                {{ tool.promptLabel || '提示词' }}
                <el-icon class="info"><InfoFilled /></el-icon>
              </label>
              <button v-if="prompt" type="button" class="ghost-icon" title="清空" @click="prompt = ''">
                <el-icon><Delete /></el-icon>
              </button>
            </div>
            <el-input
              v-model="prompt"
              type="textarea"
              :rows="4"
              resize="none"
              :placeholder="tool.promptPlaceholder || '输入提示词'"
            />
          </div>
          <FieldRenderer v-else v-model="form[item.field.key]" :field="item.field" />
        </div>

        <div v-if="tool.actions" class="action-row">
          <button v-for="action in tool.actions" :key="action" type="button" @click="runAction(action)">
            <el-icon><MagicStick /></el-icon>
            {{ action }}
          </button>
        </div>
      </div>

      <div class="params-footer">
        <div class="price">
          <el-icon><Coin /></el-icon>
          {{ currentPriceLabel }} 积分 ·
          <span @click="pricingVisible = true">定价</span>
        </div>
        <button v-if="tool.layout === 'music'" type="button" class="clear-all" @click="resetForm">清空所有</button>
        <el-button type="primary" class="generate-btn" :loading="generating" @click="generate">
          <el-icon><MagicStick /></el-icon>
          {{ generating ? '生成中' : tool.generateLabel || '生成' }}
        </el-button>
      </div>
    </aside>

    <section class="stage">
      <div class="stage-head">
        <TabStrip v-model="tab" :items="tabs" />
        <div class="stage-tools">
          <span class="stage-meta">{{ tasks.length }} 个任务</span>
          <el-dropdown trigger="click" placement="bottom-end" @command="handleHistory">
            <button type="button" class="more-btn" title="历史记录选项">
              <el-icon><MoreFilled /></el-icon>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="clearCurrent">
                  <el-icon><Delete /></el-icon>
                  清空当前任务
                </el-dropdown-item>
                <el-dropdown-item command="clearGallery">
                  <el-icon><RefreshLeft /></el-icon>
                  清空画廊
                </el-dropdown-item>
                <el-dropdown-item command="download" divided>
                  <el-icon><Download /></el-icon>
                  导出任务记录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <div v-if="tab === 'current'" class="stage-body">
        <div v-if="!tasks.length" class="stage-empty">
          <EmptyState text="没有历史任务，请开始新的任务吧～" />
        </div>
        <div v-else class="task-list">
          <article v-for="task in tasks" :key="task.id" class="task-card panel">
            <div v-if="task.kind === 'music'" class="music-preview" :class="{ loading: task.status === '生成中' }">
              <div class="music-disc">
                <el-icon><Headset /></el-icon>
              </div>
              <div class="music-copy">
                <strong>{{ task.title }}</strong>
                <span>{{ task.meta }}</span>
              </div>
              <button
                type="button"
                class="play-btn"
                :disabled="task.status !== '已完成'"
                title="播放音乐"
                @click="openPreview(task)"
              >
                <el-icon><VideoPlay /></el-icon>
              </button>
              <a
                v-if="task.status === '已完成'"
                class="play-btn"
                :href="task.file"
                :download="downloadName(task)"
                title="下载音乐"
                @click.stop
              >
                <el-icon><Download /></el-icon>
              </a>
            </div>

            <div
              v-else
              class="task-preview"
              :class="{ loading: task.status === '生成中' }"
              :title="task.status === '已完成' ? (task.kind === 'video' ? '点击预览视频' : '点击下载图片') : undefined"
              @click="handleMediaClick(task)"
            >
              <img v-if="task.image" :src="task.image" :alt="task.prompt" />
              <div v-else class="placeholder">
                <el-icon><Picture /></el-icon>
                <span>{{ task.status === '生成中' ? '正在生成…' : '暂无预览' }}</span>
              </div>
              <span v-if="task.kind === 'video' && task.status === '已完成'" class="preview-play">
                <el-icon><VideoPlay /></el-icon>
              </span>
              <a
                v-if="task.status === '已完成'"
                class="media-download"
                :href="task.file"
                :download="downloadName(task)"
                :title="`下载${task.kind === 'video' ? '视频' : '图片'}`"
                @click.stop
              >
                <el-icon><Download /></el-icon>
              </a>
              <span v-if="task.kind === 'video' && task.status === '已完成'" class="preview-badge">
                <el-icon><VideoCamera /></el-icon>
                预览
              </span>
            </div>

            <div class="task-info">
              <div class="task-prompt">{{ task.prompt }}</div>
              <div class="task-meta">
                <span>{{ task.meta }}</span>
                <span class="task-time">{{ task.time }}</span>
              </div>
              <el-tag :type="task.status === '已完成' ? 'success' : 'warning'" effect="plain" size="small">
                {{ task.status }}
              </el-tag>
            </div>
          </article>
        </div>
      </div>

      <div v-else class="stage-body">
        <div v-if="!gallery.length" class="stage-empty">
          <EmptyState text="画廊里还没有作品" />
        </div>
        <div v-else class="gallery">
          <figure v-for="item in gallery" :key="item.id" class="gallery-item">
            <div class="gallery-media" @click="handleMediaClick(item)">
              <img v-if="item.image" :src="item.image" :alt="item.prompt" />
              <div v-else class="gallery-music">
                <el-icon><VideoPlay /></el-icon>
              </div>
              <span v-if="item.kind === 'video'" class="preview-play is-small">
                <el-icon><VideoPlay /></el-icon>
              </span>
              <a
                class="media-download"
                :href="item.file"
                :download="downloadName(item)"
                title="下载"
                @click.stop
              >
                <el-icon><Download /></el-icon>
              </a>
            </div>
            <figcaption>{{ item.title || item.prompt }}</figcaption>
          </figure>
        </div>
      </div>
    </section>

    <el-dialog
      v-model="previewVisible"
      :title="previewTask ? previewTask.title || '预览' : '预览'"
      width="760px"
      append-to-body
      destroy-on-close
    >
      <div v-if="previewTask" class="media-player">
        <video
          v-if="previewTask.kind === 'video'"
          :src="previewTask.file"
          :poster="previewTask.image || undefined"
          controls
          autoplay
          playsinline
        />
        <div v-else class="audio-player">
          <div class="music-disc">
            <el-icon><Headset /></el-icon>
          </div>
          <div class="audio-copy">
            <strong>{{ previewTask.title }}</strong>
            <span>{{ previewTask.meta }}</span>
          </div>
          <audio :src="previewTask.file" controls autoplay />
        </div>
      </div>
    </el-dialog>

    <PricingDialog v-model="pricingVisible" :tool="tool" :price="currentPrice" :model-name="currentModelName" />
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import AppIcon from '@/components/AppIcon.vue'
import EmptyState from '@/components/EmptyState.vue'
import TabStrip from '@/components/TabStrip.vue'
import { getTool, modelOptionsOf, showcases } from '@/config/tools'
import { MODEL_FIELD_KEYS, optionNames, unitPrice } from '@/utils/model-catalog'
import { chargeCredits, refundCredits } from '@/mock/platform'
import { createArtifact } from '@/api/artifact'
import FieldRenderer from './fields/FieldRenderer.vue'
import PricingDialog from './PricingDialog.vue'

const route = useRoute()
const tabs = [
  { key: 'current', label: '当前任务' },
  { key: 'gallery', label: '画廊' }
]

const tool = computed(() => getTool(route.meta.toolKey))
const form = ref({})
const prompt = ref('')
const tab = ref('current')
const tasks = ref([])
const generating = ref(false)
const pricingVisible = ref(false)
const previewVisible = ref(false)
const previewTask = ref(null)

// 模型字段的选项优先读后台「模型管理」的目录，目录缺失时回退到静态配置
const effectiveFields = computed(() =>
  (tool.value.fields || []).map(field =>
    MODEL_FIELD_KEYS.includes(field.key)
      ? { ...field, options: optionNames(tool.value.key, field.options || []) }
      : field
  )
)

// 编辑页顶部展示当前工具与默认模型，切换到其他模型时同步更新
const currentModelName = computed(() => {
  const options = optionNames(tool.value.key, modelOptionsOf(tool.value).map(item => item.name))
  const selected = form.value.model || form.value.version
  return selected || tool.value.model || options[0] || ''
})

const currentPrice = computed(() =>
  unitPrice(tool.value.key, currentModelName.value, tool.value.price)
)

const currentPriceLabel = computed(() => Number(currentPrice.value || 0).toFixed(2))

const renderItems = computed(() => {
  const items = []
  const fields = effectiveFields.value
  const promptIndex = tool.value.promptIndex ?? 0
  fields.forEach((field, index) => {
    if (index === promptIndex) items.push({ kind: 'prompt', key: 'prompt' })
    if (isFieldVisible(field)) items.push({ kind: 'field', key: field.key, field })
  })
  if (promptIndex >= fields.length) items.push({ kind: 'prompt', key: 'prompt' })
  return items
})

const gallery = computed(() => tasks.value.filter(item => item.status === '已完成'))

function isFieldVisible(field) {
  if (!field.visibleWhen) return true
  return form.value[field.visibleWhen.key] === field.visibleWhen.equals
}

function storageKey() {
  return `lingxi-tool-tasks-${tool.value.key}`
}

function buildInitialForm(currentTool) {
  const next = {}
  ;(currentTool.fields || []).forEach(field => {
    const isModelField = MODEL_FIELD_KEYS.includes(field.key)
    const options = isModelField
      ? optionNames(currentTool.key, field.options || [])
      : field.options
    if (field.type === 'tags') next[field.key] = Array.isArray(field.value) ? [...field.value] : []
    else if (field.type === 'switch') next[field.key] = Boolean(field.value)
    else if (field.type === 'number') next[field.key] = field.value ?? 0
    else {
      const fallback = field.value ?? (options ? options[0] : '')
      // 默认模型可能已被后台下架，回退到第一个可用模型
      next[field.key] =
        isModelField && options?.length && !options.includes(fallback) ? options[0] : fallback
    }
  })
  return next
}

function loadTasks() {
  try {
    const list = JSON.parse(localStorage.getItem(storageKey()) || '[]')
    if (!Array.isArray(list)) {
      tasks.value = []
      return
    }
    // 页面刷新后没有进行中的生成定时器，遗留的「生成中」按中断处理，不伪造产物
    tasks.value = list.map(item => {
      if (item.status === '生成中') return { ...item, status: '已中断' }
      return item.file ? item : { ...item, file: fileFor(item) }
    })
  } catch (error) {
    tasks.value = []
  }
}

// 落盘键可按工具显式指定：切换工具时 handle 里的任务属于旧工具，必须写回旧工具的键
function persist(key = storageKey()) {
  try {
    localStorage.setItem(key, JSON.stringify(tasks.value.slice(0, 20)))
    return true
  } catch (error) {
    // 隐私模式或配额耗尽：调用方需要回滚扣费与任务状态
    return false
  }
}

// 生成演示定时器：离开页面时清理，避免卸载后仍写库并弹提示
let generateTimer = null
// 已扣费但产物尚未生成：卸载、清空或切换工具时需要冲正
let pendingCharge = null

/**
 * 中断当前生成：清定时器、退回本次扣费、标记任务中断，并写回该任务所属工具。
 * 各工具路由共用本组件且 router-view 未加 key，切换工具时组件被复用、
 * onBeforeUnmount 不会触发，因此必须由 watch(setup) 主动收尾。
 */
function interruptPending(reason) {
  if (generateTimer) {
    window.clearTimeout(generateTimer)
    generateTimer = null
  }
  if (!pendingCharge) return
  const charge = pendingCharge
  pendingCharge = null
  // 生成被中断：退回本次扣费并标记任务中断，不留下「凭扣费凭空出图」的记录
  refundCredits({
    amount: charge.amount,
    service: charge.service,
    model: charge.model,
    reason,
    allocations: charge.allocations
  })
  const task = tasks.value.find(item => item.id === charge.taskId)
  if (task) task.status = '已中断'
  // 用 charge.key 落盘：此刻 storageKey() 已指向新工具，直接写会覆盖新工具的任务列表
  persist(charge.key || storageKey())
}

onBeforeUnmount(() => {
  interruptPending('页面关闭导致生成中断')
})

// 前端演示使用本地示例图，避免依赖在线渲染服务
const demoCovers = {
  image: [
    '/images/showcase-sc-nano-lodge.jpg',
    '/images/showcase-sc-gpt-lamp.jpg',
    '/images/showcase-sc-nano-rain.jpg',
    '/images/showcase-sc-gpt-poster.jpg',
    '/images/category-image.jpg'
  ],
  video: [
    '/images/showcase-sc-seedance-ink.jpg',
    '/images/showcase-sc-seedream-desert.jpg',
    '/images/showcase-sc-maestro-china.jpg',
    '/images/category-video.jpg'
  ]
}

const demoFiles = {
  video: ['/media/demo-video-ink.mp4', '/media/demo-video-desert.mp4'],
  music: ['/media/demo-music.wav']
}

function hashOf(text) {
  let hash = 0
  for (let i = 0; i < text.length; i += 1) hash = (hash * 31 + text.charCodeAt(i)) % 997
  return hash
}

function coverFor(task) {
  if (task.kind === 'music') return ''
  const pool = demoCovers[task.kind] || demoCovers.image
  return pool[hashOf(task.prompt || task.title || '') % pool.length]
}

function fileFor(task) {
  if (task.kind === 'music') return demoFiles.music[0]
  if (task.kind === 'video') {
    return demoFiles.video[hashOf(task.prompt || task.title || '') % demoFiles.video.length]
  }
  return task.image || coverFor(task)
}

function downloadName(task) {
  const base = (task.title || task.prompt || task.kind).replace(/[\\/:*?"<>|\s]+/g, '-').slice(0, 24)
  const ext = { image: 'jpg', video: 'mp4', music: 'wav' }[task.kind] || 'bin'
  return `lingxi-${base || task.kind}.${ext}`
}

// 视频、音乐点击后弹窗预览播放；图片保持点击下载
function openPreview(task) {
  if (!task || task.status !== '已完成' || !task.file) return
  previewTask.value = task
  previewVisible.value = true
}

function handleMediaClick(task) {
  if (task.status !== '已完成' || !task.file) return
  if (task.kind === 'image') {
    const link = document.createElement('a')
    link.href = task.file
    link.download = downloadName(task)
    link.click()
    return
  }
  openPreview(task)
}

function buildMeta() {
  const parts = []
  const skipKeys = ['lyrics', 'style', 'title', 'negativePrompt', 'singerStyle', 'reference', 'referenceAudio', 'firstFrame', 'lastFrame', 'refImage', 'refAudio', 'refVideo', 'image', 'images', 'audio']
  Object.entries(form.value).forEach(([key, value]) => {
    if (skipKeys.includes(key)) return
    if (Array.isArray(value)) {
      if (value.length) parts.push(value.join(' / '))
      return
    }
    if (value === '' || value === null || value === undefined || typeof value === 'boolean') return
    parts.push(String(value))
  })
  return parts.slice(0, 4).join(' · ')
}

function buildTask() {
  const currentTool = tool.value
  const title = form.value.title || prompt.value.trim().slice(0, 18) || currentTool.title
  const meta = buildMeta() || currentTool.model
  return {
    id: Date.now().toString(36),
    kind: currentTool.resultType,
    title,
    prompt: prompt.value.trim(),
    meta,
    status: '生成中',
    image: '',
    file: '',
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
}

const SERVICE_LABEL = {
  chat: '对话',
  image: '图像生成',
  video: '视频生成',
  music: '音乐生成',
  maestro: '视频生成'
}

function generate() {
  if (!prompt.value.trim()) {
    ElMessage.warning(`请先输入${tool.value.promptLabel || '提示词'}`)
    return
  }

  // 先落盘再扣费：持久化失败时不会产生消费流水与用量记录，账本始终一致
  const serviceLabel = SERVICE_LABEL[tool.value.category] || '内容生成'
  const task = buildTask()
  tasks.value.unshift(task)
  generating.value = true

  if (!persist()) {
    tasks.value = tasks.value.filter(item => item.id !== task.id)
    generating.value = false
    ElMessage.error('本地存储不可用，任务未创建')
    return
  }

  const charge = chargeCredits({
    amount: currentPrice.value,
    service: serviceLabel,
    operation: `${tool.value.key}/generations`,
    model: currentModelName.value,
    duration: '—'
  })
  if (!charge.ok) {
    // 扣费失败时撤销刚创建的任务，避免留下未计费的成功记录
    tasks.value = tasks.value.filter(item => item.id !== task.id)
    persist()
    generating.value = false
    ElMessage.error(`积分不足：当前 ${charge.balance}，本次需要 ${currentPriceLabel.value}`)
    return
  }

  // 记录本次扣费：产物生成前若中断（卸载、清空、切换工具），需要冲正
  pendingCharge = {
    amount: currentPrice.value,
    service: serviceLabel,
    model: currentModelName.value,
    taskId: task.id,
    // 记住扣费时所属工具：切工具后 storageKey() 会变，冲正时必须写回旧键
    key: tool.value.key,
    // 扣费分摊明细：退款时原路退回，避免「奖励积分扣掉、退款进了购买池」
    allocations: charge.allocations || null
  }

  // 前端演示：图片/视频使用本地示例图，音乐生成曲目卡
  generateTimer = window.setTimeout(async () => {
    generateTimer = null
    const charge = pendingCharge
    try {
      if (task.kind !== 'music') {
        task.image = coverFor(task)
      }
      task.file = fileFor(task)
      task.status = '已完成'
      // 产物未落盘等于生成失败：保留扣费会让用户白扣积分
      if (!persist()) throw new Error('persist failed')

      // 产物入作品库（后端 artifacts），作品库页面读取同一份数据
      await createArtifact({
        type: task.kind === 'music' ? 'music' : task.kind === 'video' ? 'video' : 'image',
        toolKey: tool.value.key,
        toolName: tool.value.title,
        title: task.title,
        prompt: task.prompt,
        url: task.file || task.image,
        posterUrl: task.image,
        sizeBytes: 0
      })

      // 产物已写入，本次扣费有效
      pendingCharge = null
      ElMessage.success('生成完成，已保存到作品库')
    } catch (error) {
      task.status = '已中断'
      if (charge) {
        refundCredits({
          amount: charge.amount,
          service: charge.service,
          model: charge.model,
          reason: '生成结果保存失败',
          allocations: charge.allocations
        })
      }
      pendingCharge = null
      ElMessage.error('生成结果保存失败，积分已退回')
    } finally {
      // 无论成功失败都解除按钮锁定
      generating.value = false
    }
  }, 1200)
}

function runAction(action) {
  if (action === 'AI 生成歌词') {
    form.value.lyrics = `[主歌]\n城市的灯慢慢亮起\n风把旧故事吹回这里\n\n[副歌]\n让我们沿着星光前行\n把每个瞬间唱成风景\n\n（根据「${prompt.value.trim() || '当前描述'}」生成）`
  } else if (action === '混搭歌词') {
    form.value.lyrics = '[主歌]\n霓虹落在安静的街\n心跳跟着鼓点重叠\n\n[副歌]\n此刻把世界调成蓝色\n让旋律带我们穿过夜色'
  } else if (action === '优化风格') {
    const base = form.value.style ? `${form.value.style} · ` : ''
    form.value.style = `${base}温暖 · 电影感 · 层次分明`
  }
}

async function handleHistory(command) {
  if (command === 'clearCurrent') {
    const hasPending = Boolean(pendingCharge)
    if (!tasks.value.length && !hasPending) return
    try {
      await ElMessageBox.confirm(
        hasPending
          ? '当前任务仍在生成中，清空将中断生成并退回本次扣费，是否继续？'
          : `确认清空当前 ${tasks.value.length} 条任务记录？`,
        '清空任务',
        { confirmButtonText: '清空', cancelButtonText: '取消', type: 'warning' }
      )
    } catch (error) {
      return
    }
    if (hasPending) {
      interruptPending('清空任务导致生成中断')
      generating.value = false
    }
    tasks.value = []
    persist()
    ElMessage.success('已清空当前任务')
    return
  }
  if (command === 'clearGallery') {
    tasks.value = tasks.value.filter(item => item.status !== '已完成')
    persist()
    ElMessage.success('已清空画廊')
    return
  }
  if (command === 'download') {
    const blob = new Blob([JSON.stringify(tasks.value, null, 2)], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${tool.value.key}-tasks.json`
    link.click()
    URL.revokeObjectURL(url)
  }
}

function resetForm() {
  form.value = buildInitialForm(tool.value)
  prompt.value = ''
}

function applyShowcase() {
  const id = route.query.showcase
  if (!id) return
  const item = showcases.find(entry => entry.id === id && entry.toolKey === tool.value.key)
  if (!item) return
  prompt.value = item.prompt
  Object.assign(form.value, item.params)
}

// 记录当前渲染的工具：只有工具真正变化时才需要中断在途生成
let currentToolKey = null

function setup() {
  const nextKey = tool.value.key
  // 各工具路由共用本组件、router-view 未加 key，切换工具不会卸载组件；
  // 不在这里收尾会导致定时器继续跑、扣费留下而产物写进新工具的任务列表。
  if (currentToolKey && currentToolKey !== nextKey) {
    interruptPending('切换工具导致生成中断')
  }
  currentToolKey = nextKey
  form.value = buildInitialForm(tool.value)
  prompt.value = ''
  tab.value = 'current'
  loadTasks()
  applyShowcase()
}

watch(() => [route.meta.toolKey, route.query.showcase], setup, { immediate: true })
</script>

<style scoped>
.studio-page {
  height: 100vh;
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  overflow: hidden;
}

/* ---------- 左侧参数面板 ---------- */
.params {
  border-right: 1px solid var(--app-border);
  background: #0a0c10;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.params-scroll {
  flex: 1;
  overflow: auto;
  padding: 20px 18px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.item {
  min-width: 0;
}

.tool-hero {
  display: flex;
  align-items: center;
  gap: 13px;
  padding: 14px;
  border: 1px solid var(--app-border);
  border-radius: 12px;
  background:
    radial-gradient(220px 120px at 0% 0%, rgba(59, 130, 246, 0.18), transparent 70%),
    rgba(255, 255, 255, 0.02);
}

.tool-hero-icon {
  width: 50px;
  height: 50px;
  flex-shrink: 0;
  border-radius: 50%;
  overflow: hidden;
  border: 1px solid var(--app-border-strong);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.4);
}

.tool-hero-copy {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.tool-hero-copy strong {
  font-size: 14.5px;
  font-weight: 650;
  letter-spacing: -0.01em;
}

.tool-hero-copy span {
  color: var(--app-muted);
  font-size: 11.5px;
  line-height: 1.55;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.tool-hero-copy em {
  color: #7cb2ff;
  font-size: 10.5px;
  font-style: normal;
}

.field label {
  display: flex;
  align-items: center;
  gap: 5px;
  margin-bottom: 8px;
  color: var(--app-text);
  font-size: 12.5px;
  font-weight: 500;
}

.info {
  color: #5b636e;
  font-size: 12px;
}

.label-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.ghost-icon {
  border: 0;
  background: transparent;
  color: #5b636e;
  cursor: pointer;
  font-size: 14px;
  padding: 0 0 8px;
}

.ghost-icon:hover {
  color: var(--app-text);
}

.action-row {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
}

.action-row button {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 6px 10px;
  border-radius: 8px;
  border: 1px solid var(--app-border-strong);
  background: var(--app-surface);
  color: var(--app-muted);
  font-size: 11.5px;
  cursor: pointer;
  transition: all 0.16s ease;
}

.action-row button:hover {
  border-color: rgba(96, 165, 250, 0.45);
  color: var(--app-text);
}

.params-footer {
  flex-shrink: 0;
  border-top: 1px solid var(--app-border);
  padding: 12px 18px 18px;
  display: flex;
  flex-direction: column;
  gap: 9px;
}

.price {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  color: var(--app-muted);
  font-size: 11.5px;
}

.price .el-icon {
  color: #7cb2ff;
}

.price span {
  text-decoration: underline;
  text-underline-offset: 3px;
  cursor: pointer;
}

.clear-all {
  border: 0;
  background: transparent;
  color: var(--app-muted);
  font-size: 11.5px;
  cursor: pointer;
  padding: 0;
}

.clear-all:hover {
  color: #f87171;
}

.generate-btn {
  width: 100%;
  height: 38px;
  font-weight: 600;
}

/* ---------- 右侧舞台 ---------- */
.stage {
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 0;
  background:
    radial-gradient(700px 360px at 65% -10%, rgba(59, 130, 246, 0.06), transparent 60%),
    var(--app-bg);
}

.stage-head {
  height: 58px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 146px 0 24px;
  border-bottom: 1px solid var(--app-border);
}

.stage-tools {
  display: flex;
  align-items: center;
  gap: 10px;
}

.stage-meta {
  color: #5b636e;
  font-size: 11.5px;
}

.more-btn {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  border: 1px solid var(--app-border);
  background: var(--app-surface);
  color: var(--app-muted);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.more-btn:hover {
  color: var(--app-text);
  border-color: var(--app-border-strong);
}

.stage-body {
  flex: 1;
  overflow: auto;
  padding: 24px;
}

.stage-empty {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.task-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.task-card {
  overflow: hidden;
}

.task-preview {
  position: relative;
  height: 220px;
  background: #0a0c10;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  text-decoration: none;
  color: inherit;
  cursor: pointer;
}

.media-download {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 2;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(8, 12, 18, 0.72);
  border: 1px solid rgba(148, 163, 184, 0.28);
  backdrop-filter: blur(6px);
  color: #dbeafe;
  font-size: 14px;
  transition: background 0.16s ease;
}

.task-preview:hover .media-download,
.gallery-media:hover .media-download {
  background: rgba(37, 99, 235, 0.85);
}

.task-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-badge {
  position: absolute;
  right: 10px;
  bottom: 10px;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 5px 10px;
  border-radius: 999px;
  background: rgba(8, 12, 18, 0.78);
  backdrop-filter: blur(6px);
  color: #dbeafe;
  font-size: 11px;
}

.preview-play {
  position: absolute;
  width: 54px;
  height: 54px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(8, 12, 18, 0.62);
  border: 1px solid rgba(148, 163, 184, 0.4);
  backdrop-filter: blur(6px);
  color: #ffffff;
  font-size: 22px;
  pointer-events: none;
  transition: transform 0.16s ease;
}

.preview-play.is-small {
  width: 42px;
  height: 42px;
  font-size: 18px;
}

.task-preview:hover .preview-play,
.gallery-media:hover .preview-play {
  transform: scale(1.08);
}

.media-player video {
  display: block;
  width: 100%;
  max-height: 60vh;
  border-radius: 10px;
  background: #000000;
}

.audio-player {
  display: grid;
  grid-template-columns: auto 1fr;
  align-items: center;
  gap: 14px 16px;
  padding: 8px 4px 4px;
}

.audio-player .music-disc {
  width: 64px;
  height: 64px;
  font-size: 28px;
}

.audio-copy {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}

.audio-copy strong {
  font-size: 15px;
}

.audio-copy span {
  color: var(--app-muted);
  font-size: 12px;
}

.audio-player audio {
  grid-column: 1 / -1;
  width: 100%;
}

.placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: #5b636e;
  font-size: 12px;
}

.placeholder .el-icon {
  font-size: 26px;
}

.loading .placeholder {
  animation: pulse 1.4s infinite ease-in-out;
}

@keyframes pulse {
  0%,
  100% {
    opacity: 0.55;
  }

  50% {
    opacity: 1;
  }
}

.music-preview {
  height: 120px;
  padding: 18px;
  display: flex;
  align-items: center;
  gap: 14px;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.16), rgba(14, 116, 144, 0.12));
}

.music-preview.loading {
  animation: pulse 1.4s infinite ease-in-out;
}

.music-disc {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(8, 12, 18, 0.5);
  border: 1px solid rgba(96, 165, 250, 0.35);
  color: #93c5fd;
  font-size: 24px;
}

.music-copy {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.music-copy strong {
  font-size: 13.5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.music-copy span {
  color: var(--app-muted);
  font-size: 11.5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.play-btn {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  border: 1px solid rgba(96, 165, 250, 0.4);
  background: rgba(8, 12, 18, 0.5);
  color: #dbeafe;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  text-decoration: none;
  flex-shrink: 0;
}

.play-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.task-info {
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.task-prompt {
  font-size: 13px;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.task-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  color: #5b636e;
  font-size: 11px;
}

.task-time {
  margin-left: auto;
}

.gallery {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 14px;
}

.gallery-item {
  margin: 0;
  border: 1px solid var(--app-border);
  border-radius: 12px;
  overflow: hidden;
  background: var(--app-surface);
}

.gallery-media {
  position: relative;
  display: block;
  aspect-ratio: 1 / 1;
  overflow: hidden;
  background: #0a0c10;
  cursor: pointer;
}

.gallery-media .media-download {
  top: 8px;
  right: 8px;
  width: 28px;
  height: 28px;
}

.gallery-media img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.gallery-music {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 34px;
  color: #93c5fd;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.16), rgba(14, 116, 144, 0.12));
}

.gallery-item figcaption {
  padding: 10px 12px;
  color: var(--app-muted);
  font-size: 11.5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

@media (max-width: 900px) {
  .studio-page {
    grid-template-columns: 1fr;
    height: auto;
  }

  .params {
    border-right: 0;
    border-bottom: 1px solid var(--app-border);
  }
}
</style>
