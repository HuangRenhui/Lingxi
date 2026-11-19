<template>
  <el-dialog
    :model-value="modelValue"
    width="680px"
    class="settings-dialog"
    append-to-body
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <template #header>
      <span>设置</span>
    </template>

    <div class="settings-shell">
      <aside class="settings-nav">
        <button
          v-for="item in navItems"
          :key="item.key"
          type="button"
          :class="{ active: active === item.key }"
          @click="active = item.key"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </button>
      </aside>

      <section class="settings-body">
        <template v-if="active === 'general'">
          <h3>通用设置</h3>
          <div class="field">
            <label>显示语言</label>
            <el-select v-model="settings.language" style="width: 100%">
              <el-option label="简体中文" value="zh-CN" />
              <el-option label="English" value="en-US" />
            </el-select>
          </div>
          <div class="field">
            <label>界面主题</label>
            <el-select v-model="settings.theme" style="width: 100%">
              <el-option label="深色（默认）" value="dark" />
              <el-option label="跟随系统" value="system" />
            </el-select>
          </div>
          <div class="field">
            <label>默认模型</label>
            <el-select v-model="settings.model" style="width: 100%">
              <el-option v-for="item in models" :key="item" :label="item" :value="item" />
            </el-select>
          </div>
          <p class="tip">新会话将默认使用该模型，可在对话页随时切换。</p>
        </template>

        <template v-else-if="active === 'memory'">
          <h3>记忆</h3>
          <div class="switch-row">
            <div>
              <strong>允许 Agent 记住上下文</strong>
              <p class="tip">开启后，跨会话保留偏好与常用信息。</p>
            </div>
            <el-switch v-model="settings.memoryEnabled" />
          </div>
          <div class="field">
            <label>长期记忆</label>
            <el-input
              v-model="settings.memory"
              type="textarea"
              :rows="5"
              placeholder="例如：默认使用简体中文，报告输出 Markdown 格式"
            />
          </div>
        </template>

        <template v-else>
          <h3>关于</h3>
          <div class="about-brand">
            <img :src="logo" alt="灵犀" />
            <div>
              <strong>灵犀</strong>
              <p>版本 0.1.0 · AI 创作平台</p>
            </div>
          </div>
          <p class="tip">
            AI 创作与无人值守任务平台。前端基于 Vue 3 + Element Plus，后端按 B1-B12 模块逐步建设。
          </p>
          <div class="tag-row">
            <el-tag effect="plain" round>Vue 3</el-tag>
            <el-tag effect="plain" round>Element Plus</el-tag>
            <el-tag effect="plain" round>Spring Boot 2.7</el-tag>
          </div>
        </template>
      </section>
    </div>

    <template #footer>
      <el-button @click="$emit('update:modelValue', false)">关闭</el-button>
      <el-button type="primary" @click="save">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { writeJson } from '@/utils/storage'
import logo from '@/assets/logo.svg'
import { loadModelCatalog } from '@/mock/model-catalog'

defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue'])

const SETTINGS_KEY = 'lingxi-settings'

const navItems = [
  { key: 'general', label: '通用设置', icon: 'Setting' },
  { key: 'memory', label: '记忆', icon: 'Collection' },
  { key: 'about', label: '关于', icon: 'InfoFilled' }
]

// 默认模型候选来自后台「模型管理」中已上架的对话模型
const models = computed(() => {
  const list = loadModelCatalog()
    .filter(item => item.enabled && item.category === 'chat')
    .sort((a, b) => a.sort - b.sort)
    .map(item => item.name)
  return list.length
    ? list
    : ['gpt-5.6-sol', 'deepseek-4.0', 'grok-4', 'gemini-3.1-pro', 'claude-opus-5', 'kimi-k3']
})

const active = ref('general')
const settings = reactive(loadSettings())

// 上次保存的默认模型可能已下架，回退到第一个可用模型
if (!models.value.includes(settings.model)) {
  settings.model = models.value[0]
}

function loadSettings() {
  const fallback = {
    language: 'zh-CN',
    theme: 'dark',
    model: 'gpt-5.6-sol',
    memoryEnabled: true,
    memory: '默认使用简体中文回答，输出结构化的 Markdown。'
  }
  try {
    return { ...fallback, ...JSON.parse(localStorage.getItem(SETTINGS_KEY) || '{}') }
  } catch (error) {
    return fallback
  }
}

function save() {
  if (!writeJson(SETTINGS_KEY, { ...settings })) {
    ElMessage.warning('本地存储不可用，设置仅保留到刷新前')
    return
  }
  ElMessage.success('设置已保存')
  emit('update:modelValue', false)
}
</script>

<style scoped>
.settings-shell {
  display: grid;
  grid-template-columns: 170px 1fr;
  min-height: 360px;
}

.settings-nav {
  border-right: 1px solid var(--app-border);
  padding: 18px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.settings-nav button {
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 9px 12px;
  border: 0;
  border-radius: 8px;
  background: transparent;
  color: var(--app-muted);
  font-size: 13px;
  cursor: pointer;
  text-align: left;
}

.settings-nav button:hover {
  background: rgba(255, 255, 255, 0.04);
  color: var(--app-text);
}

.settings-nav button.active {
  background: var(--app-primary-soft);
  color: #bfdbfe;
}

.settings-body {
  padding: 22px 26px;
}

h3 {
  margin: 0 0 18px;
  font-size: 15px;
  font-weight: 650;
}

.field {
  margin-bottom: 16px;
}

.field label {
  display: block;
  margin-bottom: 7px;
  color: var(--app-muted);
  font-size: 12.5px;
}

.tip {
  margin: 4px 0 0;
  color: var(--app-muted);
  font-size: 11.5px;
  line-height: 1.7;
}

.key-box {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 14px 16px;
  border: 1px solid var(--app-border);
  border-radius: 10px;
  background: var(--app-surface);
  margin: 14px 0;
}

.key-box code {
  color: #bfdbfe;
  font-size: 12px;
  word-break: break-all;
}

.key-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.switch-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.switch-row strong {
  font-size: 13px;
  font-weight: 600;
}

.about-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}

.about-brand img {
  width: 42px;
  height: 42px;
  border-radius: 12px;
}

.about-brand strong {
  font-size: 15px;
}

.about-brand p {
  margin: 3px 0 0;
  color: var(--app-muted);
  font-size: 12px;
}

.tag-row {
  display: flex;
  gap: 8px;
  margin-top: 18px;
}

@media (max-width: 720px) {
  .settings-shell {
    grid-template-columns: 1fr;
  }

  .settings-nav {
    flex-direction: row;
    border-right: 0;
    border-bottom: 1px solid var(--app-border);
    overflow: auto;
  }
}
</style>
