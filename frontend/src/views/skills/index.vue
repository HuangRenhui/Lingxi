<template>
  <div class="page">
    <PageHeader
      title="技能"
      description="技能（Skill）是一段告诉 AI「该用哪些工具、按什么步骤完成任务」的 Markdown 说明书，你可以上传自己的技能，平台也内置了一些公共技能。"
    >
      <el-button round @click="browseVisible = true">
        <el-icon><Compass /></el-icon>
        浏览技能
      </el-button>
      <el-button round type="primary" @click="createVisible = true">
        <el-icon><Plus /></el-icon>
        创建技能
      </el-button>
    </PageHeader>

    <div class="skill-shell panel">
      <aside class="skill-list">
        <div class="list-tools">
          <el-input v-model="keyword" placeholder="搜索技能" clearable>
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-button circle @click="createVisible = true">
            <el-icon><Plus /></el-icon>
          </el-button>
        </div>

        <div class="group-title">个人技能</div>
        <div v-if="!filteredSkills.length" class="group-empty">你还没有任何技能。</div>
        <button
          v-for="item in filteredSkills"
          :key="skillKey(item)"
          type="button"
          class="skill-item"
          :class="{ active: currentKey === skillKey(item) }"
          @click="currentKey = skillKey(item)"
        >
          <el-icon><Document /></el-icon>
          <span>{{ item.name }}</span>
        </button>
      </aside>

      <section class="skill-detail">
        <template v-if="current">
          <header class="detail-head">
            <div>
              <h2>{{ current.name }}</h2>
              <div class="detail-meta">
                <span>{{ publisherOf(current) }}</span>
                <span>·</span>
                <span>{{ statusText(current) }}</span>
              </div>
            </div>
            <div class="detail-actions">
              <el-switch
                :model-value="current.status !== 0"
                size="small"
                @change="value => toggleStatus(current, value)"
              />
              <el-button link type="danger" @click="removeSkill(current)">删除</el-button>
            </div>
          </header>
          <p class="detail-desc">{{ current.description }}</p>
          <pre class="detail-content">{{ current.content }}</pre>
        </template>

        <div v-else class="detail-empty">
          <div class="empty-illustration">
            <el-icon><Reading /></el-icon>
          </div>
          <p>从左侧列表选择一个技能查看详情</p>
          <div class="empty-actions">
            <el-button round @click="browseVisible = true">
              <el-icon><Compass /></el-icon>
              浏览技能
            </el-button>
            <el-button round @click="createVisible = true">
              <el-icon><Plus /></el-icon>
              创建技能
            </el-button>
          </div>
        </div>
      </section>
    </div>

    <el-dialog v-model="browseVisible" title="浏览技能" width="820px" append-to-body>
      <div class="browse-tools">
        <el-input v-model="browseKeyword" placeholder="搜索技能..." clearable>
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select v-model="browsePublisher" style="width: 150px">
          <el-option label="全部发布者" value="all" />
          <el-option label="官方" value="official" />
          <el-option label="社区" value="community" />
        </el-select>
        <el-select v-model="browseSort" style="width: 120px">
          <el-option label="热门" value="hot" />
          <el-option label="最新" value="new" />
        </el-select>
      </div>

      <div class="browse-grid">
        <article v-for="item in pagedSkills" :key="item.name" class="browse-card">
          <header>
            <strong>{{ item.name }}</strong>
            <button type="button" :disabled="isAdded(item.name)" @click="addSkill(item)">
              {{ isAdded(item.name) ? '已添加' : '添加' }}
            </button>
          </header>
          <div class="browse-meta">{{ item.publisher }} · {{ item.downloads }}</div>
          <p>{{ item.description }}</p>
        </article>
      </div>

      <div class="pager">
        <el-pagination
          v-model:current-page="page"
          layout="prev, pager, next"
          :page-size="6"
          :total="filteredCatalog.length"
          background
        />
      </div>
    </el-dialog>

    <el-dialog v-model="createVisible" title="创建技能" width="620px" append-to-body>
      <el-form label-position="top">
        <el-form-item label="技能名称">
          <el-input v-model="newSkill.name" placeholder="例如：/weekly-report" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="newSkill.description" placeholder="一句话描述该技能的用途" />
        </el-form-item>
        <el-form-item label="技能内容（Markdown）">
          <el-input
            v-model="newSkill.content"
            type="textarea"
            :rows="8"
            placeholder="# 角色&#10;你是一名……&#10;&#10;## 步骤&#10;1. ……"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" @click="createSkill">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createSkill as createSkillApi, deleteSkill, listSkills, updateSkill } from '@/api/skill'
import { readJson, writeJson } from '@/utils/storage'
import { matchKeyword } from '@/utils/text'
import { fetchAllPages } from '@/utils/pagination'
import PageHeader from '@/components/PageHeader.vue'

const STORAGE_KEY = 'lingxi-skills'

const catalog = [
  {
    name: '/github',
    publisher: '官方',
    type: 'official',
    downloads: 102,
    description: '管理 GitHub issues、pull requests、仓库检索、发布与 Actions 工作流。',
    content: '# GitHub\n\n当用户提到仓库、Issue、PR 或发布时使用本技能。\n\n1. 确认仓库与分支\n2. 拉取最新状态\n3. 执行对应操作并回写结果'
  },
  {
    name: '/zhihu',
    publisher: '官方',
    type: 'official',
    downloads: 31,
    description: '检索知乎内容与热榜，获取话题、回答与创作者数据。',
    content: '# 知乎\n\n用于检索知乎话题与热榜内容，输出结构化摘要。'
  },
  {
    name: '/seedance-video',
    publisher: '官方',
    type: 'official',
    downloads: 29,
    description: '使用 Seedance 生成和编辑 AI 视频，支持文本、图片与参考视频输入。',
    content: '# Seedance Video\n\n根据提示词生成视频，支持 2x 多模态参考与音效。'
  },
  {
    name: '/suno-music',
    publisher: '官方',
    type: 'official',
    downloads: 28,
    description: '使用 Suno 根据提示词生成音乐、歌词、延长曲目与提取人声。',
    content: '# Suno Music\n\n生成音乐、歌词与封面，支持扩展与翻唱。'
  },
  {
    name: '/google-drive',
    publisher: '官方',
    type: 'official',
    downloads: 27,
    description: '读取、导出、重命名、移动和删除 Google Drive 文件。',
    content: '# Google Drive\n\n基于 Drive v3 REST API 管理文件与目录。'
  },
  {
    name: '/skill-creator',
    publisher: '社区',
    type: 'community',
    downloads: 22,
    description: '指导你创建有效的新技能，扩展 AI 的专长能力。',
    content: '# Skill Creator\n\n帮助用户编写结构化、可复用的技能说明。'
  },
  {
    name: '/web-search',
    publisher: '官方',
    type: 'official',
    downloads: 64,
    description: '联网检索并提取网页正文，返回带来源的摘要。',
    content: '# Web Search\n\n搜索公开网页，抓取正文并给出引用。'
  },
  {
    name: '/image-gen',
    publisher: '官方',
    type: 'official',
    downloads: 47,
    description: '调用图像模型生成商品图、插画与海报，并保存到产物库。',
    content: '# Image Gen\n\n根据提示词与宽高比生成图片，支持参考图。'
  },
  {
    name: '/data-report',
    publisher: '社区',
    type: 'community',
    downloads: 19,
    description: '将表格数据整理为指标卡、趋势图与结论文本。',
    content: '# Data Report\n\n读取数据源，输出关键指标与异常说明。'
  }
]

const skills = ref(loadDemoSkills())
const demoMode = ref(false)
const currentKey = ref('')
const keyword = ref('')
const browseVisible = ref(false)
const createVisible = ref(false)
const browseKeyword = ref('')
const browsePublisher = ref('all')
const browseSort = ref('hot')
const page = ref(1)

const newSkill = reactive({ name: '', description: '', content: '' })

const filteredSkills = computed(() => {
  const key = keyword.value.trim().toLowerCase()
  return skills.value.filter(item => matchKeyword(key, item.name))
})

const current = computed(
  () => skills.value.find(item => skillKey(item) === currentKey.value) || null
)

const filteredCatalog = computed(() => {
  const key = browseKeyword.value.trim().toLowerCase()
  return catalog.filter(item => {
    const matchKey = matchKeyword(key, item.name, item.description)
    const matchPublisher = browsePublisher.value === 'all' || item.type === browsePublisher.value
    return matchKey && matchPublisher
  })
})

const pagedSkills = computed(() => {
  const start = (page.value - 1) * 6
  return filteredCatalog.value.slice(start, start + 6)
})

watch([browseKeyword, browsePublisher], () => {
  page.value = 1
})

function skillKey(item) {
  return item.id ?? item.name
}

function publisherOf(item) {
  if (item.publisher) return item.publisher
  return item.category === 'OFFICIAL' ? '官方' : '社区'
}

function statusText(item) {
  return item.status === 0 ? '已停用' : '已启用'
}

function loadDemoSkills() {
  const cached = readJson(STORAGE_KEY, [])
  return Array.isArray(cached) ? cached : []
}

function persistDemo() {
  if (!writeJson(STORAGE_KEY, skills.value)) {
    ElMessage.warning('本地存储不可用，本次改动仅保留到刷新前')
  }
}

async function loadSkills() {
  try {
    const result = await fetchAllPages(params => listSkills(params))
    if (!result) {
      demoMode.value = false
      skills.value = []
      if (import.meta.env.DEV) {
        demoMode.value = true
        skills.value = loadDemoSkills()
      }
    } else {
      demoMode.value = false
      skills.value = result.list
    }
  } catch (error) {
    demoMode.value = false
    skills.value = []
    if (import.meta.env.DEV) {
      demoMode.value = true
      skills.value = loadDemoSkills()
    }
  }
  if (!skills.value.some(item => skillKey(item) === currentKey.value)) {
    currentKey.value = skills.value.length ? skillKey(skills.value[0]) : ''
  }
}

function isAdded(name) {
  return skills.value.some(item => item.name === name)
}

function toPayload(item) {
  return {
    name: item.name,
    description: item.description || '',
    content: item.content,
    category: item.category || (item.type === 'official' ? 'OFFICIAL' : 'COMMUNITY')
  }
}

async function addSkill(item) {
  if (isAdded(item.name)) return
  if (demoMode.value) {
    skills.value.push({ ...item })
    currentKey.value = skillKey(item)
    persistDemo()
    ElMessage.success(`已添加 ${item.name}`)
    return
  }
  try {
    const created = await createSkillApi(toPayload(item))
    await loadSkills()
    currentKey.value = skillKey(created)
    ElMessage.success(`已添加 ${item.name}`)
  } catch (error) {
    // 请求层已统一提示错误
  }
}

async function createSkill() {
  if (!newSkill.name.trim() || !newSkill.content.trim()) {
    ElMessage.warning('请填写技能名称与内容')
    return
  }
  const name = newSkill.name.startsWith('/') ? newSkill.name.trim() : `/${newSkill.name.trim()}`
  if (isAdded(name)) {
    ElMessage.warning('同名技能已存在')
    return
  }
  const payload = {
    name,
    description: newSkill.description,
    content: newSkill.content,
    category: 'COMMUNITY'
  }
  if (demoMode.value) {
    const item = { ...payload, publisher: '我', type: 'community', downloads: 0, status: 1 }
    skills.value.push(item)
    currentKey.value = skillKey(item)
    persistDemo()
  } else {
    try {
      const created = await createSkillApi(payload)
      await loadSkills()
      currentKey.value = skillKey(created)
    } catch (error) {
      return
    }
  }
  newSkill.name = ''
  newSkill.description = ''
  newSkill.content = ''
  createVisible.value = false
  ElMessage.success('技能已创建')
}

async function toggleStatus(item, enabled) {
  const status = enabled ? 1 : 0
  if (demoMode.value) {
    item.status = status
    persistDemo()
    ElMessage.success(enabled ? '技能已启用' : '技能已停用')
    return
  }
  try {
    await updateSkill(item.id, { status })
    item.status = status
    ElMessage.success(enabled ? '技能已启用' : '技能已停用')
  } catch (error) {
    // 请求层已统一提示错误
  }
}

async function removeSkill(item) {
  try {
    await ElMessageBox.confirm(`确认删除技能「${item.name}」？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除'
    })
  } catch (error) {
    return
  }
  if (demoMode.value) {
    skills.value = skills.value.filter(entry => skillKey(entry) !== skillKey(item))
    persistDemo()
  } else {
    try {
      await deleteSkill(item.id)
      await loadSkills()
    } catch (error) {
      return
    }
  }
  if (currentKey.value === skillKey(item)) {
    currentKey.value = skills.value.length ? skillKey(skills.value[0]) : ''
  }
  ElMessage.success('技能已删除')
}

onMounted(loadSkills)
</script>

<style scoped>
.skill-shell {
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr);
  min-height: 560px;
  overflow: hidden;
}

.skill-list {
  border-right: 1px solid var(--app-border);
  padding: 16px 14px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.list-tools {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}

.list-tools .el-input {
  flex: 1;
}

.group-title {
  margin: 6px 4px;
  color: #5b636e;
  font-size: 11px;
  letter-spacing: 0.06em;
}

.group-empty {
  padding: 10px 6px;
  color: #5b636e;
  font-size: 12px;
}

.skill-item {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 9px 10px;
  border: 0;
  border-radius: 8px;
  background: transparent;
  color: var(--app-muted);
  font-size: 12.5px;
  text-align: left;
  cursor: pointer;
}

.skill-item:hover {
  background: rgba(255, 255, 255, 0.04);
  color: var(--app-text);
}

.skill-item.active {
  background: var(--app-primary-soft);
  color: #dbeafe;
}

.skill-detail {
  padding: 26px 30px;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.detail-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--app-border);
}

.detail-head h2 {
  margin: 0;
  font-size: 22px;
  letter-spacing: -0.02em;
}

.detail-meta {
  display: flex;
  gap: 6px;
  margin-top: 6px;
  color: #5b636e;
  font-size: 11.5px;
}

.detail-actions {
  display: flex;
  align-items: center;
  gap: 14px;
}

.detail-desc {
  margin: 16px 0;
  color: var(--app-muted);
  font-size: 13px;
  line-height: 1.75;
}

.detail-content {
  margin: 0;
  padding: 16px;
  border-radius: 10px;
  background: #0a0c10;
  border: 1px solid var(--app-border);
  color: #c9d1d9;
  font-size: 12px;
  line-height: 1.8;
  white-space: pre-wrap;
  font-family: "JetBrains Mono", Consolas, monospace;
  overflow: auto;
}

.detail-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--app-muted);
}

.empty-illustration {
  width: 58px;
  height: 58px;
  border-radius: 14px;
  border: 1px solid var(--app-border);
  background: rgba(255, 255, 255, 0.03);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  color: #6e7681;
}

.detail-empty p {
  margin: 0;
  font-size: 12.5px;
}

.empty-actions {
  display: flex;
  gap: 10px;
  margin-top: 6px;
}

.browse-tools {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}

.browse-tools .el-input {
  flex: 1;
}

.browse-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.browse-card {
  border: 1px solid var(--app-border);
  border-radius: 10px;
  padding: 14px 16px;
  background: rgba(255, 255, 255, 0.015);
}

.browse-card header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.browse-card strong {
  font-size: 13.5px;
}

.browse-card header button {
  border: 0;
  background: transparent;
  color: #7cb2ff;
  font-size: 12px;
  cursor: pointer;
}

.browse-card header button:disabled {
  color: #5b636e;
  cursor: default;
}

.browse-meta {
  margin: 4px 0 8px;
  color: #5b636e;
  font-size: 11px;
}

.browse-card p {
  margin: 0;
  color: var(--app-muted);
  font-size: 11.5px;
  line-height: 1.65;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: 18px;
}

@media (max-width: 900px) {
  .skill-shell {
    grid-template-columns: 1fr;
  }

  .skill-list {
    border-right: 0;
    border-bottom: 1px solid var(--app-border);
  }

  .browse-grid {
    grid-template-columns: 1fr;
  }
}
</style>
