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
import { SERVICE_LABEL, demoCovers, demoFiles, tabs } from './constants'

const route = useRoute()

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
        // 后端枚举是 IMAGE / VIDEO / AUDIO，必须与 ArtifactCreateRequest 保持一致
        type: task.kind === 'music' ? 'AUDIO' : task.kind === 'video' ? 'VIDEO' : 'IMAGE',
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

<style scoped src="./index.scss"></style>
