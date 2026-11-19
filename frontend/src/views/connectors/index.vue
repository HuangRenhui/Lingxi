<template>
  <div class="page">
    <PageHeader title="连接器" description="管理已连接的第三方服务，这些连接允许 Agent 调用工具完成任务。" />

    <div class="connector-shell panel">
      <aside class="connector-list">
        <div class="list-tools">
          <el-input v-model="keyword" placeholder="搜索连接器" clearable>
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-button circle><el-icon><Plus /></el-icon></el-button>
        </div>

        <div class="group-title">推荐</div>
        <button
          v-for="item in filteredConnectors"
          :key="item.name"
          type="button"
          class="connector-item"
          :class="{ active: current?.name === item.name }"
          @click="currentName = item.name"
        >
          <span class="connector-badge" :style="{ background: item.plain ? 'transparent' : item.color }">
            <img class="connector-logo" :class="{ plain: item.plain }" :src="connectorIcons[item.icon]" :alt="item.name" />
          </span>
          <span class="connector-name">{{ item.name }}</span>
          <span class="connector-status" :class="{ on: connected.includes(item.name) }">
            {{ connected.includes(item.name) ? '已连接' : '未连接' }}
          </span>
        </button>

        <el-button class="browse-all" round @click="allVisible = true">
          <el-icon><Grid /></el-icon>
          浏览全部连接器
        </el-button>
      </aside>

      <section v-if="current" class="connector-detail">
        <header class="detail-head">
          <div class="detail-brand">
            <span class="connector-badge large" :style="{ background: current.plain ? 'transparent' : current.color }">
              <img class="connector-logo" :class="{ plain: current.plain }" :src="connectorIcons[current.icon]" :alt="current.name" />
            </span>
            <div>
              <h2>{{ current.name }}</h2>
              <p>{{ current.summary }}</p>
            </div>
          </div>
          <el-button
            round
            :type="isConnected ? 'success' : 'primary'"
            :plain="isConnected"
            @click="toggleConnect(current)"
          >
            {{ isConnected ? '已连接' : '连接' }}
          </el-button>
        </header>

        <section class="detail-section">
          <h3>简介</h3>
          <p v-for="line in current.detail" :key="line" class="detail-line">{{ line }}</p>
        </section>

        <section class="detail-section">
          <h3>试一试 <span class="count">3</span></h3>
          <p class="section-hint">连接后即可使用这些示例词</p>
          <div class="examples">
            <div v-for="example in current.examples" :key="example" class="example-row">
              <el-icon><ChatDotRound /></el-icon>
              <span>{{ example }}</span>
              <el-icon class="lock"><Lock /></el-icon>
            </div>
          </div>
        </section>

        <section class="detail-section">
          <h3>工具权限</h3>
          <p class="section-hint">连接后即可查看该服务提供的工具。</p>
          <div class="tool-tags">
            <el-tag v-for="tool in current.tools" :key="tool" effect="plain" round>{{ tool }}</el-tag>
          </div>
        </section>
      </section>
    </div>

    <el-dialog v-model="allVisible" title="全部连接器" width="720px">
      <div class="all-grid">
        <article
          v-for="item in connectors"
          :key="item.name"
          class="all-card"
          :class="{ active: currentName === item.name }"
          @click="pickConnector(item)"
        >
          <span class="connector-badge" :style="{ background: item.plain ? 'transparent' : item.color }">
            <img class="connector-logo" :class="{ plain: item.plain }" :src="connectorIcons[item.icon]" :alt="item.name" />
          </span>
          <div class="all-copy">
            <strong>{{ item.name }}</strong>
            <span>{{ item.summary }}</span>
          </div>
          <el-button
            link
            size="small"
            :type="connected.includes(item.name) ? 'success' : 'primary'"
            @click.stop="toggleConnect(item)"
          >
            {{ connected.includes(item.name) ? '断开' : '连接' }}
          </el-button>
        </article>
      </div>
      <template #footer>
        <el-button round @click="allVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { readJson, writeJson } from '@/utils/storage'
import { matchKeyword } from '@/utils/text'
import PageHeader from '@/components/PageHeader.vue'
import openaiIcon from '@/assets/connector-icons/openai.svg'
import sunoIcon from '@/assets/connector-icons/suno.svg'
import midjourneyIcon from '@/assets/connector-icons/midjourney.svg'
import googleIcon from '@/assets/connector-icons/google.svg'
import tencentCloudIcon from '@/assets/connector-icons/tencent-cloud.svg'
import wechatIcon from '@/assets/connector-icons/wechat.svg'
import telegramIcon from '@/assets/connector-icons/telegram.svg'
import youtubeIcon from '@/assets/connector-icons/youtube.svg'
import tencentDocsIcon from '@/assets/connector-icons/tencent-docs.png'
import githubIcon from '@/assets/connector-icons/github.svg'
import googleDriveIcon from '@/assets/connector-icons/googledrive.svg'
import gmailIcon from '@/assets/connector-icons/gmail.svg'
import notionIcon from '@/assets/connector-icons/notion.svg'
import googleCalendarIcon from '@/assets/connector-icons/google-calendar.svg'
import oneDriveIcon from '@/assets/connector-icons/onedrive.svg'
import sentryIcon from '@/assets/connector-icons/sentry.svg'

const STORAGE_KEY = 'lingxi-connectors'

const connectorIcons = {
  openai: openaiIcon,
  suno: sunoIcon,
  midjourney: midjourneyIcon,
  google: googleIcon,
  'tencent-cloud': tencentCloudIcon,
  wechat: wechatIcon,
  telegram: telegramIcon,
  youtube: youtubeIcon,
  'tencent-docs': tencentDocsIcon,
  github: githubIcon,
  googledrive: googleDriveIcon,
  gmail: gmailIcon,
  notion: notionIcon,
  'google-calendar': googleCalendarIcon,
  onedrive: oneDriveIcon,
  sentry: sentryIcon
}

const connectors = [
  {
    name: 'OpenAI',
    icon: 'openai',
    color: '#10a37f',
    summary: 'OpenAI 全能力面板：GPT 系列对话、图像生成、向量嵌入与 Responses API。',
    detail: [
      '对话：openai chat_completion（GPT 系列 / o 系列推理模型）',
      'Responses API：openai create_response（含 search-preview 等扩展模型）',
      '文本图：openai generate_image（GPT-Image-1 / GPT-Image-2 / DALL·E 3 / nano-banana）',
      '改图：openai edit_image（局部编辑 / 风格迁移）',
      '向量：openai create_embedding（text-embedding-3-small / large）'
    ],
    examples: [
      '用 gpt-image-2 生成一张赛博朋克风格的电影海报，1024x1536',
      '用 gpt-image-2 局部编辑这张商品图：把乱背景替换成纯白影棚背景',
      '用 GPT-5 帮我把这段长文总结成三条要点'
    ],
    tools: ['chat_completion', 'create_response', 'generate_image', 'edit_image', 'create_embedding']
  },
  {
    name: 'Suno Music',
    icon: 'suno',
    color: '#f59e0b',
    summary: '根据提示词生成音乐、歌词、延长曲目与提取人声。',
    detail: ['生成音乐与歌词', '延长已有曲目', '翻唱与提取人声'],
    examples: ['生成一首 90 秒的轻快电子音乐', '为这段歌词谱曲并生成完整歌曲'],
    tools: ['generate_music', 'extend_track', 'extract_vocals']
  },
  {
    name: 'Midjourney',
    icon: 'midjourney',
    color: '#6366f1',
    summary: '通过提示词生成高质量插画与概念图，支持风格参考。',
    detail: ['文本生图', '风格参考与角色一致性', '放大与变体'],
    examples: ['生成一张极简主义的产品主视觉', '用同一角色生成四张不同场景插画'],
    tools: ['imagine', 'blend', 'upscale']
  },
  {
    name: 'SERP Search',
    icon: 'google',
    color: '#22c55e',
    summary: '通过搜索引擎检索公开网页，返回标题、摘要与来源链接。',
    detail: ['关键词搜索', '新闻与网页结果聚合', '结果去重与摘要'],
    examples: ['检索本周 AI 行业的重要融资事件', '查找这个产品的最新定价页'],
    tools: ['search', 'news_search']
  },
  {
    name: '腾讯云',
    icon: 'tencent-cloud',
    color: '#3b82f6',
    summary: '腾讯云资源管理、对象存储与短信服务。',
    detail: ['COS 对象存储', '短信发送', '云服务器查询'],
    examples: ['把这批产物上传到 COS 并返回访问地址', '查询当前账号下的实例状态'],
    tools: ['cos_upload', 'sms_send', 'describe_instances']
  },
  {
    name: '微信公众号',
    icon: 'wechat',
    color: '#16a34a',
    summary: '发布与检索公众号文章，读取阅读与互动数据。',
    detail: ['草稿创建与发布', '素材管理', '数据检索'],
    examples: ['把这份简报发布到公众号草稿箱', '统计最近三篇文章的阅读量'],
    tools: ['create_draft', 'publish', 'get_stats']
  },
  {
    name: 'Telegram',
    icon: 'telegram',
    color: '#0ea5e9',
    summary: '发送消息、管理频道与读取群组更新。',
    detail: ['发送文本与文件', '频道管理', '消息检索'],
    examples: ['把每日简报发送到运营频道', '检索群组里关于发布的讨论'],
    tools: ['send_message', 'send_file', 'search_messages']
  },
  {
    name: 'YouTube',
    icon: 'youtube',
    color: '#ef4444',
    summary: '检索视频、读取字幕与频道数据。',
    detail: ['视频检索', '字幕提取', '频道统计'],
    examples: ['提取这个视频的字幕并总结重点', '查找本周热门的 AI 产品评测'],
    tools: ['search_videos', 'get_captions', 'get_channel']
  },
  {
    name: '腾讯文档',
    icon: 'tencent-docs',
    plain: true,
    color: '#2563eb',
    summary: '读取与写入腾讯文档，支持表格与在线文档。',
    detail: ['读取文档内容', '写入表格', '创建文档'],
    examples: ['把这份数据写入在线表格', '读取需求文档并生成任务清单'],
    tools: ['read_doc', 'write_sheet', 'create_doc']
  },
  {
    name: 'GitHub',
    icon: 'github',
    color: '#6b7280',
    summary: '管理仓库、Issue、Pull Request 与 Actions 工作流。',
    detail: ['Issue 与 PR 管理', '代码检索', 'Actions 触发'],
    examples: ['列出仓库中未处理的 Issue', '为这个分支创建一个 PR'],
    tools: ['search_code', 'create_issue', 'create_pr', 'trigger_workflow']
  },
  {
    name: 'Google Drive',
    icon: 'googledrive',
    color: '#eab308',
    summary: '读取、导出、重命名、移动和删除云端文件。',
    detail: ['文件检索', '上传与导出', '权限查询'],
    examples: ['找到最近的销售报表并导出为 CSV', '把这个文件移动到归档目录'],
    tools: ['search_files', 'upload', 'export']
  },
  {
    name: 'Gmail',
    icon: 'gmail',
    color: '#dc2626',
    summary: '检索邮件、创建草稿与发送通知。',
    detail: ['邮件检索', '草稿创建', '发送与回复'],
    examples: ['整理今天未读的客户邮件并生成摘要', '把这份周报发送给团队'],
    tools: ['search_mail', 'create_draft', 'send_mail']
  },
  {
    name: 'Notion',
    icon: 'notion',
    color: '#374151',
    summary: '读写 Notion 页面与数据库，同步项目进度。',
    detail: ['页面读取', '数据库写入', '内容同步'],
    examples: ['把这份会议纪要写入项目页面', '同步数据库中的任务状态'],
    tools: ['read_page', 'append_blocks', 'query_database']
  },
  {
    name: 'Google Calendar',
    icon: 'google-calendar',
    color: '#3b82f6',
    summary: '查询日程、创建会议与检查空闲时间。',
    detail: ['日程查询', '创建与更新事件', '忙闲查询'],
    examples: ['查看我明天的日程安排', '约一个 30 分钟的空闲时段'],
    tools: ['list_events', 'create_event', 'find_free_time']
  },
  {
    name: 'Microsoft OneDrive',
    icon: 'onedrive',
    color: '#0284c7',
    summary: '管理 OneDrive 文件与共享链接。',
    detail: ['文件检索', '上传下载', '共享链接管理'],
    examples: ['找到最新上传的预算表', '为这个文件夹创建共享链接'],
    tools: ['search_files', 'upload', 'create_link']
  },
  {
    name: 'Sentry',
    icon: 'sentry',
    color: '#7c3aed',
    summary: '查询线上错误、告警与发布记录。',
    detail: ['错误聚合查询', '告警配置', '发布记录'],
    examples: ['汇总今天新增的错误类型', '查看最近一次发布后的错误趋势'],
    tools: ['list_issues', 'get_issue', 'list_releases']
  }
]

const connected = ref(loadConnected())
const keyword = ref('')
const currentName = ref(connectors[0].name)
const allVisible = ref(false)

const filteredConnectors = computed(() => {
  const key = keyword.value.trim().toLowerCase()
  return connectors.filter(item => matchKeyword(key, item.name))
})

const current = computed(() => connectors.find(item => item.name === currentName.value) || connectors[0])

function pickConnector(item) {
  currentName.value = item.name
  allVisible.value = false
}
const isConnected = computed(() => connected.value.includes(current.value.name))

function loadConnected() {
  const cached = readJson(STORAGE_KEY, [])
  return Array.isArray(cached) ? cached : []
}

function toggleConnect(item) {
  if (connected.value.includes(item.name)) {
    connected.value = connected.value.filter(name => name !== item.name)
    ElMessage.success(`已断开 ${item.name}`)
  } else {
    connected.value.push(item.name)
    ElMessage.success(`已连接 ${item.name}`)
  }
  if (!writeJson(STORAGE_KEY, connected.value)) {
    ElMessage.warning('本地存储不可用，连接状态仅保留到刷新前')
  }
}
</script>

<style scoped>
.connector-shell {
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr);
  min-height: 600px;
  overflow: hidden;
}

.connector-list {
  border-right: 1px solid var(--app-border);
  padding: 16px 12px;
  display: flex;
  flex-direction: column;
  gap: 3px;
  min-height: 0;
  max-height: calc(100vh - 190px);
  overflow: auto;
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
  margin: 4px 8px 6px;
  color: #5b636e;
  font-size: 11px;
}

.connector-item {
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 8px 9px;
  border: 0;
  border-radius: 8px;
  background: transparent;
  cursor: pointer;
  text-align: left;
}

.connector-item:hover {
  background: rgba(255, 255, 255, 0.04);
}

.connector-item.active {
  background: var(--app-primary-soft);
}

.connector-badge {
  width: 24px;
  height: 24px;
  border-radius: 7px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.connector-badge.large {
  width: 38px;
  height: 38px;
  border-radius: 10px;
}

.connector-logo {
  width: 15px;
  height: 15px;
  object-fit: contain;
  filter: brightness(0) invert(1);
}

.connector-logo.plain {
  width: 100%;
  height: 100%;
  border-radius: inherit;
  object-fit: cover;
  filter: none;
}

.connector-badge.large .connector-logo {
  width: 24px;
  height: 24px;
}

.connector-badge.large .connector-logo.plain {
  width: 100%;
  height: 100%;
}

.connector-name {
  flex: 1;
  min-width: 0;
  color: var(--app-text);
  font-size: 12.5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.connector-status {
  color: #5b636e;
  font-size: 10.5px;
}

.connector-status.on {
  color: #4ade80;
}

.browse-all {
  margin-top: auto;
  width: 100%;
}

.connector-detail {
  padding: 24px 30px 36px;
  min-width: 0;
  overflow: auto;
  max-height: calc(100vh - 190px);
}

.detail-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  padding-bottom: 18px;
  border-bottom: 1px solid var(--app-border);
}

.detail-brand {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.detail-brand h2 {
  margin: 0;
  font-size: 21px;
  letter-spacing: -0.02em;
}

.detail-brand p {
  margin: 6px 0 0;
  color: var(--app-muted);
  font-size: 12px;
  line-height: 1.6;
}

.detail-section {
  padding-top: 20px;
}

.detail-section h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 10px;
  font-size: 13.5px;
  font-weight: 600;
}

.count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.06);
  color: var(--app-muted);
  font-size: 10.5px;
  font-weight: 500;
}

.detail-line {
  margin: 0 0 6px;
  color: var(--app-muted);
  font-size: 12px;
  line-height: 1.7;
}

.section-hint {
  margin: 0 0 10px;
  color: #5b636e;
  font-size: 11.5px;
}

.examples {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.example-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border: 1px solid var(--app-border);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.015);
  color: var(--app-muted);
  font-size: 12px;
}

.example-row span {
  flex: 1;
}

.example-row .lock {
  color: #5b636e;
}

.tool-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

@media (max-width: 900px) {
  .connector-shell {
    grid-template-columns: 1fr;
  }

  .connector-list {
    border-right: 0;
    border-bottom: 1px solid var(--app-border);
    max-height: 320px;
  }

  .connector-detail {
    max-height: none;
  }
}

/* 全部连接器弹窗 */
.all-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  max-height: 60vh;
  overflow-y: auto;
}

.all-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid var(--app-border);
  background: var(--app-surface-2);
  cursor: pointer;
  transition: border-color 0.16s ease;
}

.all-card:hover,
.all-card.active {
  border-color: rgba(96, 165, 250, 0.45);
}

.all-copy {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.all-copy strong {
  font-size: 12.5px;
  font-weight: 600;
}

.all-copy span {
  color: var(--app-muted);
  font-size: 11px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

@media (max-width: 760px) {
  .all-grid {
    grid-template-columns: 1fr;
  }
}
</style>
