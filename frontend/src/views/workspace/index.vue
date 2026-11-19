<template>
  <div class="chat-page">
    <aside class="history">
      <button type="button" class="new-chat" @click="newChat">
        <el-icon><Plus /></el-icon>
        新对话
      </button>

      <div class="history-label">最近对话</div>
      <div v-if="!conversations.length" class="history-empty">暂无对话记录</div>
      <div class="history-list">
        <div
          v-for="conv in conversations"
          :key="conv.id"
          class="history-item"
          :class="{ active: conv.id === currentId }"
          @click="currentId = conv.id"
        >
          <div class="conv-main">
            <span class="conv-title">{{ conv.title }}</span>
            <span class="conv-meta">{{ conv.model }} · {{ conv.updatedAt }}</span>
          </div>
          <button type="button" class="conv-delete" title="删除" @click.stop="removeConversation(conv.id)">
            <el-icon><Close /></el-icon>
          </button>
        </div>
      </div>
    </aside>

    <section class="chat">
      <header class="chat-head">
        <div class="chat-head-left">
          <span class="app-badge">
            <AppIcon :name="appConfig.key" :alt="appConfig.title" />
          </span>
          <span class="app-name">{{ appConfig.title }}</span>
          <span class="head-divider">·</span>
          <span class="chat-name">{{ current ? current.title : '新对话' }}</span>
          <el-tag v-if="sending" size="small" effect="plain" round>生成中</el-tag>
        </div>
        <div class="chat-head-right">
          <el-select v-model="model" size="default" class="model-select" popper-class="model-select-popper">
            <el-option v-for="item in models" :key="item.name" :label="item.name" :value="item.name">
              <span class="model-option">
                <span class="model-option-icon">
                  <AppIcon :name="appConfig.key" :alt="item.name" />
                </span>
                <span class="model-option-main">
                  <strong>{{ item.name }}</strong>
                  <span v-if="item.description">{{ item.description }}</span>
                </span>
                <span v-if="item.badge" class="model-badge">{{ item.badge }}</span>
              </span>
            </el-option>
          </el-select>
          <el-dropdown trigger="click" placement="bottom-end" @command="handleCommand">
            <button type="button" class="more-btn" title="更多">
              <el-icon><MoreFilled /></el-icon>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="new">
                  <el-icon><Plus /></el-icon>
                  新建对话
                </el-dropdown-item>
                <el-dropdown-item command="clear">
                  <el-icon><Delete /></el-icon>
                  清空当前应用会话
                </el-dropdown-item>
                <el-dropdown-item command="export">
                  <el-icon><Download /></el-icon>
                  导出会话
                </el-dropdown-item>
                <el-dropdown-item command="help" divided>
                  <el-icon><QuestionFilled /></el-icon>
                  帮助与反馈
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <div ref="scrollRef" class="messages">
        <div v-if="!current" class="welcome">
          <span class="welcome-logo">
            <AppIcon :name="appConfig.key" :alt="appConfig.title" />
          </span>
          <div class="welcome-app">{{ appConfig.title }}</div>
          <h1>今天想聊点什么？</h1>
          <p>{{ appConfig.description }}</p>
          <div class="suggestions">
            <button v-for="item in suggestions" :key="item" type="button" @click="useSuggestion(item)">
              {{ item }}
            </button>
          </div>
        </div>

        <template v-else>
          <div v-for="(msg, index) in current.messages" :key="index" class="message" :class="msg.role">
            <div class="msg-avatar">{{ msg.role === 'user' ? '我' : 'AI' }}</div>
            <div class="msg-body">
              <div v-if="msg.attachments && msg.attachments.length" class="msg-files">
                <span v-for="(name, fileIndex) in msg.attachments" :key="`${name}-${fileIndex}`" class="msg-file">
                  <el-icon><Document /></el-icon>
                  {{ name }}
                </span>
              </div>
              <div class="bubble">{{ msg.content }}</div>
            </div>
          </div>
          <div v-if="sending" class="message assistant">
            <div class="msg-avatar">AI</div>
            <div class="bubble typing">
              <span /><span /><span />
            </div>
          </div>
        </template>
      </div>

      <div class="composer-wrap">
        <div class="composer">
          <div v-if="attachments.length" class="attachment-row">
            <span v-for="(name, index) in attachments" :key="`${name}-${index}`" class="file-chip">
              <el-icon><Document /></el-icon>
              <span class="file-name">{{ name }}</span>
              <button type="button" class="file-remove" title="移除" @click="removeAttachment(index)">
                <el-icon><Close /></el-icon>
              </button>
            </span>
          </div>

          <textarea
            v-model="draft"
            rows="2"
            placeholder="输入消息，Enter 发送，Shift + Enter 换行"
            @keydown.enter.exact.prevent="send"
          />

          <div class="composer-foot">
            <div class="composer-tools">
              <el-upload
                class="upload-trigger"
                action="#"
                :auto-upload="false"
                :show-file-list="false"
                :on-change="onFileChange"
              >
                <button type="button" class="tool-btn" title="添加附件">
                  <el-icon><Paperclip /></el-icon>
                </button>
              </el-upload>
              <button type="button" class="tool-btn" title="语音通话" @click="voiceVisible = true">
                <el-icon><Microphone /></el-icon>
              </button>
              <button type="button" class="tool-btn" title="帮助与反馈" @click="helpVisible = true">
                <el-icon><QuestionFilled /></el-icon>
              </button>
              <span class="hint">消息仅保存在本地，接入模型网关后返回真实结果</span>
            </div>
            <button
              type="button"
              class="send-btn"
              :disabled="(!draft.trim() && !attachments.length) || sending"
              @click="send"
            >
              <el-icon><Promotion /></el-icon>
            </button>
          </div>
        </div>
      </div>
    </section>

    <el-dialog v-model="voiceVisible" title="语音通话" width="420px" align-center>
      <div class="voice-panel">
        <div class="voice-icon">
          <el-icon><Microphone /></el-icon>
        </div>
        <h3>语音通话（界面预览）</h3>
        <p>
          当前版本仅展示通话界面，不会申请麦克风权限，也不会发起任何网络请求。接入实时语音能力后可在此直接与 {{ appConfig.title }} 对话。
        </p>
      </div>
      <template #footer>
        <el-button type="primary" @click="voiceVisible = false">知道了</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="helpVisible" title="帮助与反馈" width="520px" align-center>
      <div class="help-panel">
        <div class="help-title">常见问题</div>
        <el-collapse v-model="activeFaq">
          <el-collapse-item title="对话记录保存在哪里？" name="1">
            <p>对话按应用分别保存在浏览器本地存储中，同一浏览器下切换应用不会互相覆盖，清理浏览器数据会一并删除。</p>
          </el-collapse-item>
          <el-collapse-item title="模型之间有什么区别？" name="2">
            <p>不同模型在推理能力、响应速度和上下文长度上各有侧重，可以在顶部下拉中随时切换，切换后仅影响新消息。</p>
          </el-collapse-item>
          <el-collapse-item title="附件会被上传吗？" name="3">
            <p>当前为演示环境，附件只记录文件名并展示在输入框上方，不会上传到服务器。</p>
          </el-collapse-item>
        </el-collapse>

        <div class="help-title">意见反馈</div>
        <el-input
          v-model="feedback"
          type="textarea"
          :rows="3"
          maxlength="300"
          show-word-limit
          placeholder="描述遇到的问题，或希望增加的功能"
        />
      </div>
      <template #footer>
        <el-button @click="helpVisible = false">取消</el-button>
        <el-button type="primary" @click="submitFeedback">提交反馈</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import AppIcon from '@/components/AppIcon.vue'
import { chatApps, getChatApp } from '@/config/tools'
import { resolveModelOptions } from '@/utils/model-catalog'
import { chargeCredits, refundCredits } from '@/mock/platform'

const route = useRoute()

/** 每个应用的会话单独存放在 lingxi-chats-<appKey> 下 */
const STORAGE_PREFIX = 'lingxi-chats-'

const genericSuggestions = [
  '帮我总结今天 AI 行业的三条重要动态',
  '为新品写一段产品介绍',
  '梳理一份竞品分析提纲',
  '把这段需求拆成开发任务'
]

const appSuggestions = {
  chatgpt: [
    '帮我分析这份产品需求里的核心风险',
    '写一封专业但不生硬的英文邮件',
    '把这段会议记录整理成行动清单',
    '用类比解释一下 Transformer 的工作原理'
  ],
  deepseek: [
    '证明这道数列题，并给出完整推理步骤',
    '检查这段 Python 代码的性能问题',
    '推导贝叶斯公式并解释每个符号的含义',
    '把这篇论文摘要压缩成 5 条结论'
  ],
  qwen: [
    '把这段中文产品文案改写得更有画面感',
    '用 Python 写一个带重试的接口请求封装',
    '整理一份大模型选型对比表',
    '根据需求文档输出接口设计草案'
  ],
  doubao: [
    '帮我写一条 30 秒短视频口播脚本',
    '把这份会议纪要整理成待办清单',
    '给这张商品图写 5 条卖点文案',
    '用通俗的话解释一下向量数据库'
  ]
}

function resolveAppKey(raw) {
  return chatApps.some(item => item.key === raw) ? raw : chatApps[0].key
}

const appKey = ref(resolveAppKey(route.params.app))
const appConfig = computed(() => getChatApp(appKey.value))
const catalogTick = ref(0)

// 模型下拉优先读后台「模型管理」的目录，目录缺失时回退到静态配置
const models = computed(() => {
  catalogTick.value
  return resolveModelOptions(appKey.value, appConfig.value.models)
})
const defaultModelName = computed(() => models.value[0]?.name || appConfig.value.model)
const suggestions = computed(() => appSuggestions[appKey.value] || genericSuggestions)

const conversations = ref(loadConversations(appKey.value))
const currentId = ref(null)
const model = ref(defaultModelName.value)
const draft = ref('')
const attachments = ref([])
const sending = ref(false)
const scrollRef = ref()

const voiceVisible = ref(false)
const helpVisible = ref(false)
const activeFaq = ref([])
const feedback = ref('')

let replyTimer = null
// 已扣费但回复尚未产生：卸载或切换应用时需要冲正
let pendingCharge = null

const current = computed(() => conversations.value.find(item => item.id === currentId.value) || null)

function loadConversations(key) {
  try {
    const raw = localStorage.getItem(STORAGE_PREFIX + key)
    return raw ? JSON.parse(raw) : []
  } catch (error) {
    return []
  }
}

function persist() {
  // 只写入当前应用自己的 key，切换应用时不会把旧数据写到新应用下
  try {
    localStorage.setItem(STORAGE_PREFIX + appKey.value, JSON.stringify(conversations.value))
    return true
  } catch (error) {
    // 隐私模式或配额耗尽时不能让调用方以为已保存
    return false
  }
}

function clearReplyTimer() {
  const interrupted = Boolean(replyTimer)
  if (replyTimer) {
    window.clearTimeout(replyTimer)
    replyTimer = null
  }
  // 回复尚未产生就中断（切换应用/离开页面）：退回本次扣费，避免只扣费无产出
  if (interrupted && pendingCharge) {
    refundCredits({ ...pendingCharge, service: '对话', reason: '回复中断，自动退回' })
    pendingCharge = null
  }
}

/**
 * 切换数据源：先重置自身状态，再读取对应应用的本地会话。
 * 调用前旧应用的变更已经通过 persist 落盘，因此这里直接切换不会串数据。
 */
function activateApp(nextKey) {
  clearReplyTimer()
  sending.value = false
  appKey.value = nextKey
  conversations.value = loadConversations(nextKey)
  currentId.value = null
  draft.value = ''
  attachments.value = []
  model.value = models.value[0]?.name || appConfig.value.model
  scrollToBottom()
}

function newChat() {
  currentId.value = null
  draft.value = ''
  attachments.value = []
}

function removeConversation(id) {
  conversations.value = conversations.value.filter(item => item.id !== id)
  if (currentId.value === id) currentId.value = null
  persist()
}

function useSuggestion(text) {
  draft.value = text
  send()
}

function onFileChange(file) {
  // 仅记录文件名，不实际上传
  if (file && file.name) attachments.value.push(file.name)
}

function removeAttachment(index) {
  attachments.value.splice(index, 1)
}

function scrollToBottom() {
  nextTick(() => {
    if (scrollRef.value) scrollRef.value.scrollTop = scrollRef.value.scrollHeight
  })
}

function send() {
  const content = draft.value.trim()
  if ((!content && !attachments.value.length) || sending.value) return

  const files = [...attachments.value]

  // 按消息长度估算 token 费用
  const cost = Math.min(0.2, 0.02 + content.length * 0.00002)

  let createdNew = false
  if (!current.value) {
    const conv = {
      id: Date.now().toString(36),
      title: (content || files[0] || '新对话').slice(0, 18),
      model: model.value,
      updatedAt: now(),
      messages: []
    }
    conversations.value.unshift(conv)
    currentId.value = conv.id
    createdNew = true
  }

  const conv = current.value
  conv.model = model.value
  conv.updatedAt = now()
  conv.messages.push({
    role: 'user',
    content: content || '（已发送附件）',
    attachments: files,
    time: now()
  })
  draft.value = ''
  attachments.value = []
  sending.value = true

  const rollbackMessage = () => {
    const index = conversations.value.findIndex(item => item.id === conv.id)
    if (index < 0) return
    if (createdNew) {
      conversations.value.splice(index, 1)
      currentId.value = null
    } else {
      conv.messages.pop()
    }
  }

  // 先持久化再扣费：落盘失败时不会产生消费流水与用量记录，账本始终一致
  if (!persist()) {
    sending.value = false
    rollbackMessage()
    ElMessage.error('本地存储不可用，本次消息未保存')
    return
  }

  const charged = chargeCredits({
    amount: cost,
    service: '对话',
    operation: `${appKey.value}/chat/completions`,
    model: model.value,
    duration: '—'
  })
  if (!charged.ok) {
    rollbackMessage()
    persist()
    sending.value = false
    ElMessage.warning(`积分不足，当前余额 ${charged.balance}，请先充值`)
    return
  }

  // 记录本次扣费：回复产生前若中断，需要冲正
  pendingCharge = { amount: cost, model: model.value }

  scrollToBottom()

  // 前端演示：接入模型网关后替换为真实的流式响应
  replyTimer = window.setTimeout(() => {
    replyTimer = null
    try {
      const target = conversations.value.find(item => item.id === conv.id)
      if (!target) return
      target.messages.push({
        role: 'assistant',
        content: `已收到你的消息：「${content || files[0] || '附件'}」。\n\n当前为 ${appConfig.value.title} 的界面演示环境，消息已保存在本地。接入模型网关后，这里会返回真实的流式结果。`,
        time: now()
      })
      target.updatedAt = now()
      persist()
      scrollToBottom()
    } catch (error) {
      ElMessage.error('回复渲染失败，请重试')
    } finally {
      // 回复已产生，本次扣费有效
      pendingCharge = null
      // 无论成功失败都要解锁输入框，否则页面会卡在「发送中」
      sending.value = false
    }
  }, 900)
}

function handleCommand(command) {
  if (command === 'new') {
    newChat()
    return
  }
  if (command === 'clear') {
    clearAppConversations()
    return
  }
  if (command === 'export') {
    exportConversations()
    return
  }
  if (command === 'help') helpVisible.value = true
}

async function clearAppConversations() {
  try {
    await ElMessageBox.confirm(`确定清空 ${appConfig.value.title} 的全部会话？`, '清空会话', {
      confirmButtonText: '清空',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch (error) {
    return
  }
  clearReplyTimer()
  sending.value = false
  conversations.value = []
  currentId.value = null
  persist()
  ElMessage.success('已清空当前应用会话')
}

function exportConversations() {
  const payload = {
    app: appKey.value,
    title: appConfig.value.title,
    exportedAt: new Date().toISOString(),
    conversations: conversations.value
  }
  const blob = new Blob([JSON.stringify(payload, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `${appKey.value}-chats.json`
  link.click()
  URL.revokeObjectURL(url)
  ElMessage.success('会话已导出')
}

function submitFeedback() {
  if (!feedback.value.trim()) {
    ElMessage.warning('请先填写反馈内容')
    return
  }
  feedback.value = ''
  helpVisible.value = false
  ElMessage.success('反馈已提交')
}

function now() {
  const date = new Date()
  return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

watch(
  () => route.params.app,
  value => {
    const nextKey = resolveAppKey(value)
    if (nextKey === appKey.value) return
    activateApp(nextKey)
  }
)

watch(currentId, () => {
  draft.value = ''
  scrollToBottom()
})

// 目录变化后当前选中模型可能已被下架，自动切到第一个可用模型
watch(models, list => {
  if (list.length && !list.some(item => item.name === model.value)) {
    model.value = list[0].name
  }
})

function onCatalogChange() {
  catalogTick.value += 1
}

onMounted(() => window.addEventListener('lingxi:model-catalog-changed', onCatalogChange))

onBeforeUnmount(() => {
  clearReplyTimer()
  window.removeEventListener('lingxi:model-catalog-changed', onCatalogChange)
})
</script>

<style scoped>
.chat-page {
  height: 100vh;
  display: grid;
  grid-template-columns: 236px minmax(0, 1fr);
  overflow: hidden;
}

/* ---------- 会话列表 ---------- */
.history {
  border-right: 1px solid var(--app-border);
  background: #0a0c10;
  padding: 18px 12px;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.new-chat {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  height: 36px;
  border-radius: 9px;
  border: 1px solid var(--app-border-strong);
  background: rgba(255, 255, 255, 0.03);
  color: var(--app-text);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.16s ease;
}

.new-chat:hover {
  border-color: rgba(96, 165, 250, 0.5);
  background: var(--app-primary-soft);
}

.history-label {
  margin: 20px 8px 8px;
  font-size: 11px;
  color: #5b636e;
  letter-spacing: 0.08em;
}

.history-empty {
  padding: 8px;
  color: #5b636e;
  font-size: 12px;
}

.history-list {
  flex: 1;
  overflow: auto;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.history-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 9px 10px;
  border-radius: 8px;
  cursor: pointer;
}

.history-item:hover {
  background: rgba(255, 255, 255, 0.04);
}

.history-item.active {
  background: rgba(59, 130, 246, 0.12);
}

.conv-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.conv-title {
  font-size: 12.5px;
  color: var(--app-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.conv-meta {
  font-size: 10.5px;
  color: #5b636e;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.conv-delete {
  display: none;
  width: 22px;
  height: 22px;
  border: 0;
  border-radius: 6px;
  background: transparent;
  color: var(--app-muted);
  cursor: pointer;
  align-items: center;
  justify-content: center;
}

.history-item:hover .conv-delete {
  display: flex;
}

.conv-delete:hover {
  color: #f87171;
  background: rgba(248, 113, 113, 0.1);
}

/* ---------- 对话区 ---------- */
.chat {
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 0;
  background:
    radial-gradient(700px 360px at 70% -10%, rgba(59, 130, 246, 0.08), transparent 60%),
    var(--app-bg);
}

.chat-head {
  height: 60px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 0 146px 0 26px;
  border-bottom: 1px solid var(--app-border);
}

.chat-head-left {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.app-badge {
  width: 26px;
  height: 26px;
  flex-shrink: 0;
  border-radius: 50%;
  overflow: hidden;
  border: 1px solid var(--app-border-strong);
}

.app-name {
  font-size: 13.5px;
  font-weight: 650;
  color: var(--app-text);
  white-space: nowrap;
}

.head-divider {
  color: #3a4450;
  font-size: 12px;
}

.chat-name {
  font-size: 13px;
  color: var(--app-muted);
  max-width: 420px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chat-head-right {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.model-select {
  width: 230px;
}

.more-btn {
  width: 32px;
  height: 32px;
  border-radius: 9px;
  border: 1px solid var(--app-border-strong);
  background: transparent;
  color: var(--app-muted);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.16s ease;
}

.more-btn:hover {
  color: var(--app-text);
  background: rgba(255, 255, 255, 0.06);
}

.messages {
  flex: 1;
  overflow: auto;
  padding: 30px max(28px, calc((100% - 780px) / 2)) 20px;
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.welcome {
  margin: auto;
  text-align: center;
  max-width: 620px;
  padding: 40px 0;
}

.welcome-logo {
  display: block;
  width: 64px;
  height: 64px;
  border-radius: 50%;
  overflow: hidden;
  margin: 0 auto 16px;
  border: 1px solid var(--app-border-strong);
  box-shadow: 0 14px 34px rgba(0, 0, 0, 0.4);
}

.welcome-app {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 3px 10px;
  margin-bottom: 14px;
  border-radius: 999px;
  border: 1px solid rgba(59, 130, 246, 0.28);
  background: var(--app-primary-soft);
  color: #bfdbfe;
  font-size: 11.5px;
  letter-spacing: 0.02em;
}

.welcome h1 {
  margin: 0 0 10px;
  font-size: 30px;
  letter-spacing: -0.03em;
}

.welcome p {
  margin: 0;
  color: var(--app-muted);
  font-size: 13px;
  line-height: 1.7;
}

.suggestions {
  margin-top: 30px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.suggestions button {
  padding: 12px 14px;
  border-radius: 10px;
  border: 1px solid var(--app-border);
  background: rgba(255, 255, 255, 0.02);
  color: #c9d1d9;
  font-size: 12px;
  text-align: left;
  cursor: pointer;
  transition: all 0.16s ease;
}

.suggestions button:hover {
  border-color: rgba(96, 165, 250, 0.45);
  background: var(--app-primary-soft);
}

.message {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.message.user {
  flex-direction: row-reverse;
}

.msg-avatar {
  width: 30px;
  height: 30px;
  flex-shrink: 0;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 700;
  background: rgba(255, 255, 255, 0.06);
  color: #c9d1d9;
}

.message.assistant .msg-avatar {
  background: linear-gradient(135deg, #1f6feb, #0e7490);
  color: #fff;
}

.msg-body {
  display: flex;
  flex-direction: column;
  gap: 6px;
  max-width: 76%;
  min-width: 0;
}

.message.user .msg-body {
  align-items: flex-end;
}

.msg-files {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.msg-file {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  max-width: 240px;
  padding: 3px 9px;
  border-radius: 7px;
  border: 1px solid var(--app-border-strong);
  background: rgba(255, 255, 255, 0.04);
  color: #c9d1d9;
  font-size: 11px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.bubble {
  max-width: 100%;
  padding: 11px 14px;
  border-radius: 12px;
  background: var(--app-surface);
  border: 1px solid var(--app-border);
  color: var(--app-text);
  font-size: 13px;
  line-height: 1.75;
  white-space: pre-wrap;
  word-break: break-word;
}

.message.user .bubble {
  background: rgba(59, 130, 246, 0.14);
  border-color: rgba(59, 130, 246, 0.28);
}

.typing {
  display: flex;
  gap: 4px;
  align-items: center;
  height: 40px;
}

.typing span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #60a5fa;
  animation: blink 1.2s infinite ease-in-out;
}

.typing span:nth-child(2) {
  animation-delay: 0.15s;
}

.typing span:nth-child(3) {
  animation-delay: 0.3s;
}

@keyframes blink {
  0%,
  80%,
  100% {
    opacity: 0.25;
    transform: translateY(0);
  }

  40% {
    opacity: 1;
    transform: translateY(-3px);
  }
}

.composer-wrap {
  flex-shrink: 0;
  padding: 6px max(28px, calc((100% - 780px) / 2)) 24px;
}

.composer {
  border: 1px solid var(--app-border-strong);
  border-radius: 14px;
  background: var(--app-surface);
  padding: 12px 12px 8px;
}

.composer:focus-within {
  border-color: rgba(96, 165, 250, 0.6);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.08);
}

.attachment-row {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 8px;
}

.file-chip {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  max-width: 240px;
  padding: 4px 8px;
  border-radius: 8px;
  border: 1px solid rgba(59, 130, 246, 0.28);
  background: var(--app-primary-soft);
  color: #bfdbfe;
  font-size: 11.5px;
}

.file-name {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.file-remove {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
  border: 0;
  border-radius: 4px;
  background: transparent;
  color: #93c5fd;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 10px;
}

.file-remove:hover {
  color: #fff;
  background: rgba(255, 255, 255, 0.12);
}

.composer textarea {
  width: 100%;
  border: 0;
  outline: 0;
  resize: none;
  background: transparent;
  color: var(--app-text);
  font-size: 13px;
  line-height: 1.7;
  font-family: inherit;
}

.composer textarea::placeholder {
  color: #5b636e;
}

.composer-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 6px;
}

.composer-tools {
  display: flex;
  align-items: center;
  gap: 4px;
  min-width: 0;
}

.upload-trigger {
  display: inline-flex;
}

.tool-btn {
  width: 28px;
  height: 28px;
  border: 0;
  border-radius: 8px;
  background: transparent;
  color: var(--app-muted);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.16s ease;
}

.tool-btn:hover {
  color: var(--app-text);
  background: rgba(255, 255, 255, 0.06);
}

.hint {
  margin-left: 6px;
  font-size: 11px;
  color: #5b636e;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.send-btn {
  width: 32px;
  height: 32px;
  flex-shrink: 0;
  border: 0;
  border-radius: 9px;
  background: #2f6fd0;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background 0.16s ease;
}

.send-btn:hover:not(:disabled) {
  background: #3b82f6;
}

.send-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

/* ---------- 语音与帮助弹窗 ---------- */
.voice-panel {
  text-align: center;
  padding: 8px 4px 4px;
}

.voice-icon {
  width: 54px;
  height: 54px;
  margin: 0 auto 14px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: #bfdbfe;
  background: var(--app-primary-soft);
  border: 1px solid rgba(59, 130, 246, 0.3);
}

.voice-panel h3 {
  margin: 0 0 8px;
  font-size: 16px;
  color: var(--app-text);
}

.voice-panel p {
  margin: 0;
  font-size: 12.5px;
  line-height: 1.8;
  color: var(--app-muted);
}

.help-panel {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.help-title {
  font-size: 12.5px;
  font-weight: 600;
  color: var(--app-text);
}

.help-panel p {
  margin: 0;
  font-size: 12.5px;
  line-height: 1.75;
  color: var(--app-muted);
}

.help-panel :deep(.el-collapse) {
  border-color: var(--app-border);
}

.help-panel :deep(.el-collapse-item__header) {
  background: transparent;
  border-bottom-color: var(--app-border);
  color: var(--app-text);
  font-size: 13px;
  height: 42px;
}

.help-panel :deep(.el-collapse-item__wrap) {
  background: transparent;
  border-bottom-color: var(--app-border);
}

.help-panel :deep(.el-collapse-item__content) {
  padding: 10px 0 14px;
}

@media (max-width: 820px) {
  .chat-page {
    grid-template-columns: 1fr;
  }

  .history {
    display: none;
  }

  .suggestions {
    grid-template-columns: 1fr;
  }
}
</style>
