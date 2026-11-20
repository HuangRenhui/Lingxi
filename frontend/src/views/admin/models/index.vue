<template>
  <div class="admin-page">
    <PageHeader title="模型管理" description="维护对用户暴露的模型条目：名称、图标、厂商、能力、定价与上下架。" flush>
      <el-button round @click="goProviders">
        <el-icon><Connection /></el-icon>
        平台接入
      </el-button>
      <el-button round type="primary" @click="openCreate">
        <el-icon><Plus /></el-icon>
        新增模型
      </el-button>
    </PageHeader>

    <div class="layer-note panel">
      <el-icon><InfoFilled /></el-icon>
      <span>
        这里管理「卖什么」——模型名称、图标、能力标签与价格；上游通道的地址、密钥与限速在
        <b>平台接入</b> 中配置，两者是依赖关系，不重复。
      </span>
    </div>

    <section class="stat-grid">
      <StatCard label="模型总数" :value="catalog.length" icon="Cpu" hint="覆盖对话、图片、视频与音乐" />
      <StatCard label="已上架" :value="stats.enabled" icon="CircleCheck" hint="用户可在工作台选择" />
      <StatCard label="推荐位" :value="stats.recommended" icon="Star" hint="在工作台优先展示" />
      <StatCard label="绑定平台" :value="stats.providers" icon="Connection" hint="来自平台接入的通道数" />
    </section>

    <div class="panel tab-panel">
      <TabStrip v-model="tab" :items="tabs" />
    </div>

    <template v-if="tab === 'models'">
      <section class="filters panel">
        <el-input v-model="keyword" class="filter-search" placeholder="搜索模型名称、标识或厂商" clearable>
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="category" class="filter-item">
          <el-option label="全部类型" value="all" />
          <el-option v-for="item in modelCategories" :key="item.key" :label="item.label" :value="item.key" />
        </el-select>
        <el-select v-model="status" class="filter-item">
          <el-option label="全部状态" value="all" />
          <el-option label="已上架" value="on" />
          <el-option label="已下架" value="off" />
        </el-select>
        <el-button round @click="resetFilters">重置筛选</el-button>
        <span class="filter-action muted">共 {{ filteredModels.length }} 个模型</span>
      </section>

      <section class="panel table-panel">
        <el-table :data="filteredModels" size="small">
          <el-table-column label="模型" min-width="260">
            <template #default="{ row }">
              <div class="model-cell">
                <span class="model-icon">
                  <AppIcon :name="row.appKey" :alt="row.name" />
                </span>
                <span class="model-copy">
                  <strong>
                    {{ row.name }}
                    <el-tag v-if="row.recommended" type="warning" size="small" effect="plain">推荐</el-tag>
                  </strong>
                  <span>{{ row.key }}{{ row.description ? ` · ${row.description}` : '' }}</span>
                </span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="vendor" label="厂商" width="130" />
          <el-table-column label="类型" width="90">
            <template #default="{ row }">
              <el-tag size="small" effect="plain" :type="categoryType(row.category)">
                {{ categoryLabel[row.category] }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="能力" min-width="190">
            <template #default="{ row }">
              <span class="caps">
                <em v-for="cap in row.capabilities.slice(0, 3)" :key="cap">{{ cap }}</em>
                <em v-if="row.capabilities.length > 3" class="more">+{{ row.capabilities.length - 3 }}</em>
              </span>
            </template>
          </el-table-column>
          <el-table-column label="定价" width="150">
            <template #default="{ row }">
              <div class="price-cell">
                <strong class="mono">{{ effective(row) }}</strong>
                <span>{{ row.unit }} · {{ row.multiplier }}x</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="上下文" width="90">
            <template #default="{ row }">
              <span class="mono muted">{{ row.context }}</span>
            </template>
          </el-table-column>
          <el-table-column label="上架" width="80">
            <template #default="{ row }">
              <el-switch v-model="row.enabled" @change="persist" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" fixed="right">
            <template #default="{ row }">
              <el-button link size="small" type="primary" @click="openEdit(row)">编辑</el-button>
            </template>
          </el-table-column>
          <template #empty>
            <div class="table-empty">没有匹配的模型</div>
          </template>
        </el-table>
      </section>
    </template>

    <template v-else>
      <section class="filters panel">
        <el-input v-model="appKeyword" class="filter-search" placeholder="搜索应用或模型名称" clearable>
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <TabStrip v-model="appCategory" :items="categories" class="category-tabs" />
      </section>

      <section v-if="filteredApps.length" class="model-grid">
        <article
          v-for="app in filteredApps"
          :key="app.key"
          class="model-card panel"
          :class="{ offline: !isEnabled(app.key) }"
        >
          <header class="model-head">
            <span class="model-icon">
              <AppIcon :name="app.key" :alt="app.title" />
            </span>
            <span class="model-title">
              <strong>{{ app.title }}</strong>
              <span>{{ categoryLabel[app.category] }} · {{ app.models.length }} 个模型</span>
            </span>
            <el-switch :model-value="isEnabled(app.key)" @change="toggleApp(app)" />
          </header>

          <p class="model-desc">{{ app.description }}</p>

          <footer class="model-foot">
            <span class="model-tags">
              <em v-for="model in app.models.slice(0, 2)" :key="model.name">{{ model.name }}</em>
              <em v-if="app.models.length > 2">+{{ app.models.length - 2 }}</em>
            </span>
            <el-button link type="primary" @click="openModels(app)">查看模型</el-button>
          </footer>
        </article>
      </section>

      <div v-else class="panel empty-panel">没有匹配的应用</div>
    </template>

    <el-drawer v-model="drawerVisible" :title="editingId ? '编辑模型' : '新增模型'" size="520px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="model-form">
        <div class="icon-row">
          <span class="icon-preview">
            <AppIcon :name="form.appKey || 'model'" :alt="form.name || '模型'" />
          </span>
          <div class="icon-picker">
            <el-form-item label="模型图标" prop="appKey">
              <el-select v-model="form.appKey" filterable placeholder="选择品牌图标">
                <el-option v-for="item in iconChoices" :key="item.key" :label="item.label" :value="item.key">
                  <span class="option-row">
                    <span class="option-icon"><AppIcon :name="item.key" :alt="item.label" /></span>
                    {{ item.label }}
                  </span>
                </el-option>
              </el-select>
            </el-form-item>
          </div>
        </div>

        <div class="form-row">
          <el-form-item label="显示名称" prop="name">
            <el-input v-model="form.name" maxlength="40" placeholder="例如：GPT-6 Astra" />
          </el-form-item>
          <el-form-item label="模型标识" prop="key">
            <el-input v-model="form.key" maxlength="48" placeholder="例如：gpt-6-astra" />
          </el-form-item>
        </div>

        <div class="form-row">
          <el-form-item label="厂商">
            <el-input v-model="form.vendor" maxlength="24" placeholder="例如：OpenAI" />
          </el-form-item>
          <el-form-item label="上游平台">
            <el-select v-model="form.provider" filterable allow-create default-first-option placeholder="选择或输入平台">
              <el-option v-for="item in providerNames" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
        </div>

        <div class="form-row">
          <el-form-item label="类型">
            <el-select v-model="form.category">
              <el-option v-for="item in modelCategories" :key="item.key" :label="item.label" :value="item.key" />
            </el-select>
          </el-form-item>
          <el-form-item label="上下文长度">
            <el-input v-model="form.context" placeholder="例如：200K，图片与视频填 —" />
          </el-form-item>
        </div>

        <el-form-item label="能力标签">
          <el-select
            v-model="form.capabilities"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="输入后回车添加，例如：推理、视觉、长上下文"
          />
        </el-form-item>

        <div class="form-row three">
          <el-form-item label="计费单位">
            <el-select v-model="form.unit" filterable allow-create default-first-option>
              <el-option label="1K tokens" value="1K tokens" />
              <el-option label="张" value="张" />
              <el-option label="5s" value="5s" />
              <el-option label="10s" value="10s" />
              <el-option label="首" value="首" />
            </el-select>
          </el-form-item>
          <el-form-item label="单价（积分）">
            <el-input-number v-model="form.price" :min="0" :max="10000" :step="0.01" :precision="4" controls-position="right" />
          </el-form-item>
          <el-form-item label="倍率">
            <el-input-number v-model="form.multiplier" :min="0.1" :max="10" :step="0.1" :precision="1" controls-position="right" />
          </el-form-item>
        </div>

        <div class="price-preview">
          实际单价：<b class="mono">{{ (Number(form.price || 0) * Number(form.multiplier || 1)).toFixed(4) }}</b>
          积分 / {{ form.unit || '单位' }}
        </div>

        <div class="form-row three">
          <el-form-item label="默认温度">
            <el-input-number v-model="form.defaultTemp" :min="0" :max="2" :step="0.1" :precision="1" controls-position="right" />
          </el-form-item>
          <el-form-item label="最大输出">
            <el-input-number v-model="form.maxOutput" :min="256" :max="131072" :step="1024" controls-position="right" />
          </el-form-item>
          <el-form-item label="排序">
            <el-input-number v-model="form.sort" :min="0" :max="999" controls-position="right" />
          </el-form-item>
        </div>

        <el-form-item label="模型说明">
          <el-input v-model="form.description" type="textarea" :rows="2" maxlength="80" show-word-limit />
        </el-form-item>

        <div class="switch-row">
          <div class="switch-item">
            <span>推荐位</span>
            <el-switch v-model="form.recommended" />
          </div>
          <div class="switch-item">
            <span>上架状态</span>
            <el-switch v-model="form.enabled" inline-prompt active-text="上架" inactive-text="下架" />
          </div>
        </div>
      </el-form>

      <template #footer>
        <el-button round @click="drawerVisible = false">取消</el-button>
        <el-button round type="primary" @click="saveModel">保存模型</el-button>
      </template>
    </el-drawer>

    <el-drawer v-model="appDrawerVisible" title="应用模型" size="460px" append-to-body>
      <template v-if="activeApp">
        <div class="drawer-app">
          <span class="model-icon big">
            <AppIcon :name="activeApp.key" :alt="activeApp.title" />
          </span>
          <div class="drawer-app-main">
            <strong>{{ activeApp.title }}</strong>
            <span>{{ categoryLabel[activeApp.category] }}应用 · {{ activeApp.models.length }} 个模型</span>
          </div>
        </div>

        <p class="drawer-desc">{{ activeApp.description }}</p>

        <div class="detail-section">可用模型</div>
        <div class="model-list">
          <div v-for="model in activeApp.models" :key="model.name" class="model-row">
            <strong>{{ model.name }}</strong>
            <span>{{ model.description || '暂无说明' }}</span>
          </div>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import PageHeader from '@/components/PageHeader.vue'
import StatCard from '@/components/StatCard.vue'
import TabStrip from '@/components/TabStrip.vue'
import AppIcon from '@/components/AppIcon.vue'
import { chatApps, modelOptionsOf, tools } from '@/config/tools'
import { loadAdminProviders, loadModelStatus, saveModelStatus } from '@/mock/admin'
import {
  iconChoices,
  loadModelCatalog,
  modelCategories,
  saveModelCatalog
} from '@/mock/model-catalog'

const router = useRouter()
const categoryLabel = { chat: '对话', image: '图片', video: '视频', music: '音乐' }

const tabs = [
  { key: 'models', label: '模型目录' },
  { key: 'apps', label: '应用与工具' }
]

const tab = ref('models')
const catalog = ref(loadModelCatalog())
const providers = loadAdminProviders()
const providerNames = providers.map(item => item.name)

const keyword = ref('')
const category = ref('all')
const status = ref('all')

const appKeyword = ref('')
const appCategory = ref('all')

const drawerVisible = ref(false)
const editingId = ref(null)
const formRef = ref()

const appDrawerVisible = ref(false)
const activeApp = ref(null)

const form = reactive(defaultModelForm())

const rules = {
  name: [{ required: true, message: '请输入显示名称', trigger: 'blur' }],
  key: [
    { required: true, message: '请输入模型标识', trigger: 'blur' },
    { pattern: /^[a-z0-9-._]+$/, message: '标识只能包含小写字母、数字与 - . _', trigger: 'blur' }
  ],
  appKey: [{ required: true, message: '请选择模型图标', trigger: 'change' }]
}

const apps = [
  ...chatApps.map(item => ({
    key: item.key,
    title: item.title,
    category: 'chat',
    description: item.description,
    models: modelOptionsOf(item)
  })),
  ...tools.map(item => ({
    key: item.key,
    title: item.title,
    category: item.category === 'maestro' ? 'video' : item.category,
    description: item.description,
    models: modelOptionsOf(item)
  }))
]

const categories = [
  { key: 'all', label: '全部应用' },
  { key: 'chat', label: '对话' },
  { key: 'image', label: '图片' },
  { key: 'video', label: '视频' },
  { key: 'music', label: '音乐' }
]

const statusMap = ref(loadModelStatus())

const stats = computed(() => ({
  enabled: catalog.value.filter(item => item.enabled).length,
  recommended: catalog.value.filter(item => item.recommended && item.enabled).length,
  providers: providers.length
}))

const filteredModels = computed(() => {
  const text = keyword.value.trim().toLowerCase()
  return [...catalog.value]
    .filter(item => {
      const matchKeyword =
        !text || `${item.name}${item.key}${item.vendor}`.toLowerCase().includes(text)
      const matchCategory = category.value === 'all' || item.category === category.value
      const matchStatus =
        status.value === 'all' || (status.value === 'on' ? item.enabled : !item.enabled)
      return matchKeyword && matchCategory && matchStatus
    })
    .sort((a, b) => a.sort - b.sort)
})

const filteredApps = computed(() => {
  const text = appKeyword.value.trim().toLowerCase()
  return apps.filter(app => {
    const matchKeyword =
      !text ||
      app.title.toLowerCase().includes(text) ||
      app.models.some(model => model.name.toLowerCase().includes(text))
    const matchCategory = appCategory.value === 'all' || app.category === appCategory.value
    return matchKeyword && matchCategory
  })
})

function defaultModelForm() {
  return {
    appKey: 'chatgpt',
    name: '',
    key: '',
    vendor: '',
    provider: providerNames[0] || '',
    category: 'chat',
    capabilities: [],
    unit: '1K tokens',
    price: 0,
    multiplier: 1,
    context: '128K',
    defaultTemp: 0.7,
    maxOutput: 8192,
    sort: catalog.value.length + 1,
    description: '',
    recommended: false,
    enabled: true
  }
}

function categoryType(value) {
  return { chat: 'primary', image: 'success', video: 'warning', music: 'danger' }[value] || 'info'
}

function effective(row, digits = 4) {
  return (Number(row.price) * Number(row.multiplier)).toFixed(digits)
}

function persist() {
  saveModelCatalog(catalog.value)
  ElMessage.success('已更新')
}

function resetFilters() {
  keyword.value = ''
  category.value = 'all'
  status.value = 'all'
}

function openCreate() {
  editingId.value = null
  Object.assign(form, defaultModelForm())
  drawerVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, {
    appKey: row.appKey,
    name: row.name,
    key: row.key,
    vendor: row.vendor,
    provider: row.provider,
    category: row.category,
    capabilities: [...row.capabilities],
    unit: row.unit,
    price: row.price,
    multiplier: row.multiplier,
    context: row.context,
    defaultTemp: row.defaultTemp ?? 0.7,
    maxOutput: row.maxOutput ?? 8192,
    sort: row.sort,
    description: row.description || '',
    recommended: row.recommended,
    enabled: row.enabled
  })
  drawerVisible.value = true
}

async function saveModel() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  const duplicated = catalog.value.some(
    item => item.key === form.key && item.id !== editingId.value
  )
  if (duplicated) {
    ElMessage.error('模型标识已存在，请更换')
    return
  }

  const payload = {
    appKey: form.appKey,
    name: form.name.trim(),
    key: form.key.trim(),
    vendor: form.vendor.trim(),
    provider: form.provider,
    category: form.category,
    capabilities: [...form.capabilities],
    unit: form.unit,
    price: form.price,
    multiplier: form.multiplier,
    context: form.context.trim() || '—',
    defaultTemp: form.defaultTemp,
    maxOutput: form.maxOutput,
    sort: form.sort,
    description: form.description.trim(),
    recommended: form.recommended,
    enabled: form.enabled,
    updatedAt: dayjs().format('YYYY-MM-DD HH:mm')
  }

  if (editingId.value) {
    const target = catalog.value.find(item => item.id === editingId.value)
    if (target) Object.assign(target, payload)
    ElMessage.success('模型已更新')
  } else {
    catalog.value = [
      { id: `mc-${Date.now()}`, ...payload },
      ...catalog.value
    ]
    ElMessage.success('模型已创建')
  }

  saveModelCatalog(catalog.value)
  drawerVisible.value = false
}

function isEnabled(key) {
  return statusMap.value[key] !== false
}

function toggleApp(app) {
  statusMap.value = { ...statusMap.value, [app.key]: !isEnabled(app.key) }
  saveModelStatus(statusMap.value)
  ElMessage.success(isEnabled(app.key) ? `「${app.title}」已上线` : `「${app.title}」已下线`)
}

function openModels(app) {
  activeApp.value = app
  appDrawerVisible.value = true
}

function goProviders() {
  const base = `/${import.meta.env.VITE_ADMIN_PATH || 'lingxi-console-7c3f'}`
  router.push(`${base}/providers`)
}
</script>

<style scoped>
.layer-note {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 11px 16px;
  color: var(--app-muted);
  font-size: 11.5px;
  line-height: 1.7;
  border-color: rgba(96, 165, 250, 0.24);
  background: rgba(59, 130, 246, 0.06);
}

.layer-note b {
  color: #9ecbff;
  font-weight: 600;
}

.tab-panel {
  padding: 6px 20px 0;
}

.category-tabs {
  border-bottom: 0;
}

.model-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(276px, 1fr));
  gap: 12px;
}

.model-card {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 16px 18px 14px;
  transition: border-color 0.16s ease;
}

.model-card:hover {
  border-color: var(--app-border-strong);
}

.model-card.offline {
  opacity: 0.62;
}

.model-head {
  display: flex;
  align-items: center;
  gap: 10px;
}

.model-icon {
  width: 34px;
  height: 34px;
  flex-shrink: 0;
  border-radius: 50%;
  overflow: hidden;
  display: inline-flex;
}

.model-icon.big {
  width: 44px;
  height: 44px;
}

.model-title {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.model-title strong {
  font-size: 13px;
  font-weight: 600;
}

.model-title span {
  color: var(--app-muted);
  font-size: 11px;
}

.model-desc {
  margin: 0;
  color: var(--app-muted);
  font-size: 11.5px;
  line-height: 1.65;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.model-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding-top: 10px;
  border-top: 1px solid var(--app-border);
}

.model-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  min-width: 0;
}

.model-tags em {
  font-style: normal;
  padding: 2px 7px;
  border-radius: 6px;
  border: 1px solid var(--app-border);
  color: var(--app-muted);
  font-size: 10.5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 110px;
}

.model-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.model-copy {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.model-copy strong {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12.5px;
  font-weight: 600;
}

.model-copy span {
  color: var(--app-muted);
  font-size: 11px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.caps {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.caps em {
  font-style: normal;
  padding: 1px 7px;
  border-radius: 6px;
  border: 1px solid var(--app-border);
  color: var(--app-muted);
  font-size: 10.5px;
}

.caps em.more {
  color: #9ecbff;
  border-color: rgba(59, 130, 246, 0.3);
}

.price-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.price-cell strong {
  font-size: 12px;
  color: #9ecbff;
}

.price-cell span {
  color: var(--app-muted);
  font-size: 10.5px;
}

.table-empty {
  padding: 30px 0;
  color: var(--app-muted);
  font-size: 12px;
}

.empty-panel {
  padding: 40px 0;
  text-align: center;
  color: var(--app-muted);
  font-size: 12px;
}

.icon-row {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 6px;
}

.icon-preview {
  width: 56px;
  height: 56px;
  flex-shrink: 0;
  display: inline-flex;
  border-radius: 50%;
  overflow: hidden;
  border: 1px solid var(--app-border);
}

.icon-picker {
  flex: 1;
  min-width: 0;
}

.option-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.option-icon {
  width: 18px;
  height: 18px;
  display: inline-flex;
  border-radius: 50%;
  overflow: hidden;
}

.model-form :deep(.el-form-item) {
  margin-bottom: 14px;
}

.model-form :deep(.el-form-item__label) {
  padding-bottom: 4px;
  color: var(--app-muted);
  font-size: 12px;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 14px;
}

.form-row.three {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.price-preview {
  margin: -4px 0 14px;
  padding: 8px 12px;
  border-radius: 8px;
  border: 1px dashed rgba(96, 165, 250, 0.28);
  color: var(--app-muted);
  font-size: 11.5px;
}

.price-preview b {
  color: #9ecbff;
  font-size: 13px;
}

.switch-row {
  display: flex;
  align-items: center;
  gap: 32px;
  padding-top: 12px;
  border-top: 1px solid var(--app-border);
}

.switch-item {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--app-muted);
  font-size: 12px;
}

.drawer-app {
  display: flex;
  align-items: center;
  gap: 12px;
}

.drawer-app-main {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.drawer-app-main strong {
  font-size: 15px;
}

.drawer-app-main span {
  color: var(--app-muted);
  font-size: 11.5px;
}

.drawer-desc {
  margin: 14px 0 18px;
  color: var(--app-muted);
  font-size: 12px;
  line-height: 1.7;
}

.detail-section {
  margin-bottom: 10px;
  font-size: 13px;
  font-weight: 600;
}

.model-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.model-row {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid var(--app-border);
  background: var(--app-surface-2);
}

.model-row strong {
  font-size: 12.5px;
  font-weight: 600;
}

.model-row span {
  color: var(--app-muted);
  font-size: 11px;
}

@media (max-width: 900px) {
  .form-row,
  .form-row.three {
    grid-template-columns: 1fr;
  }
}
</style>
