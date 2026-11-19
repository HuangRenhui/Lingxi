<template>
  <div class="home">
    <div class="home-scroll">
      <section class="hero" aria-label="精选创作推荐">
        <button type="button" class="hero-arrow prev" aria-label="上一张推荐" @click="prevSlide">
          <el-icon><ArrowLeft /></el-icon>
        </button>

        <div class="hero-card">
          <div class="hero-copy">
            <span class="eyebrow">精选创作推荐</span>
            <h1>{{ currentSlide.title }}</h1>
            <p>{{ currentSlide.subtitle }}</p>
            <router-link :to="currentSlide.to" class="hero-cta">
              立即创作
              <el-icon><Right /></el-icon>
            </router-link>
            <div class="hero-dots">
              <button
                v-for="(item, index) in slides"
                :key="item.to"
                type="button"
                :class="{ active: index === slideIndex }"
                :aria-label="`查看第 ${index + 1} 张：${item.title}`"
                @click="slideIndex = index"
              />
            </div>
          </div>
          <div class="hero-visual">
            <MediaImage :src="currentSlide.image" :alt="currentSlide.title" eager />
          </div>
        </div>

        <button type="button" class="hero-arrow next" aria-label="下一张推荐" @click="nextSlide">
          <el-icon><ArrowRight /></el-icon>
        </button>
      </section>

      <section class="section quick">
        <div class="section-head">
          <div>
            <span class="eyebrow">快速开始</span>
            <h2>今天想创作什么？</h2>
          </div>
          <p>无需先研究模型，直接从你的创作目标出发。</p>
        </div>

        <div class="category-grid">
          <div v-for="category in toolCategories" :key="category.key" class="category-item">
            <button
              type="button"
              class="category-card"
              :class="{ open: openCategory === category.key }"
              :aria-expanded="openCategory === category.key"
              @click="toggleCategory(category.key)"
            >
              <MediaImage class="category-media" :src="category.image" :alt="category.title" />
              <span class="shade" />
              <span class="category-copy">
                <strong>{{ category.title }}</strong>
                <span>{{ category.description }}</span>
                <b>
                  {{ categoryTools(category).length }} 款应用 ·
                  {{ openCategory === category.key ? '收起 ↑' : '查看模型 ↓' }}
                </b>
              </span>
            </button>

            <div v-if="openCategory === category.key" class="category-panel">
              <router-link
                v-for="item in categoryTools(category)"
                :key="item.path"
                :to="item.path"
                class="category-tool"
              >
                <span class="tool-logo">
                  <AppIcon :name="item.key" :alt="item.title" />
                </span>
                <span class="tool-main">
                  <strong>{{ item.title }}</strong>
                  <span>{{ item.description }}</span>
                </span>
                <span class="tool-go">立即创作 →</span>
              </router-link>
            </div>
          </div>
        </div>
      </section>

      <section class="section showcase">
        <div class="section-head">
          <div>
            <span class="eyebrow">精选作品</span>
            <h2>从好作品开始下一次创作</h2>
          </div>
          <p>预览精选生成结果，进入工作台后可继续调整同款参数。</p>
        </div>

        <div class="showcase-grid">
          <article v-for="item in visibleShowcases" :key="item.id" class="showcase-card">
            <div class="showcase-media">
              <MediaImage :src="item.image" :alt="item.title" />
            </div>
            <div class="showcase-body">
              <span class="showcase-tag">{{ toolTitle(item.toolKey) }}</span>
              <h3>{{ item.title }}</h3>
              <p>{{ item.subtitle }}</p>
              <router-link :to="`${toolOf(item.toolKey).path}?showcase=${item.id}`" class="showcase-link">
                创建同款
                <el-icon><Right /></el-icon>
              </router-link>
            </div>
          </article>
        </div>

        <div v-if="visibleCount < showcases.length" class="load-more">
          <el-button round size="large" @click="visibleCount += 6">加载更多</el-button>
        </div>
      </section>

      <footer class="home-foot">
        <span>灵犀 · 让灵感更快变成作品</span>
        <span>所有生成内容仅用于界面演示</span>
      </footer>
    </div>

    <button type="button" class="support-btn" @click="supportVisible = true">
      <el-icon><Service /></el-icon>
      客服
    </button>

    <el-dialog v-model="supportVisible" title="客服支持" width="380px" class="support-dialog">
      <div class="support-body">
        <div class="support-row">
          <span>在线时间</span>
          <strong>每天 09:00 - 22:00</strong>
        </div>
        <div class="support-row">
          <span>支持邮箱</span>
          <strong>support@lingxi.local</strong>
        </div>
        <div class="support-tip">遇到生成失败、积分异常或账号问题，请提供任务编号与截图，我们会尽快处理。</div>
        <div class="support-qr">
          <el-icon><Grid /></el-icon>
          <span>扫码联系客服</span>
        </div>
      </div>
      <template #footer>
        <el-button round @click="supportVisible = false">关闭</el-button>
        <el-button type="primary" round @click="copySupportEmail">复制客服邮箱</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import AppIcon from '@/components/AppIcon.vue'
import MediaImage from '@/components/MediaImage.vue'
import { chatApps, showcases, toolCategories, tools } from '@/config/tools'

const supportVisible = ref(false)

async function copySupportEmail() {
  try {
    await navigator.clipboard.writeText('support@lingxi.local')
    ElMessage.success('客服邮箱已复制')
  } catch (error) {
    ElMessage.warning('当前环境不支持剪贴板，请手动记录 support@lingxi.local')
  }
}
const slideIndex = ref(0)
const openCategory = ref('')
const visibleCount = ref(6)
let timer = null

const slides = [
  {
    title: '自动成片',
    subtitle: '把主题交给 AI，脚本、画面、配音、字幕和音乐自动完成，灵感快速变作品。',
    to: '/maestro',
    image: '/images/hero-1.jpg'
  },
  {
    title: 'AI 图片',
    subtitle: '生成、编辑、扩图和风格转换，把脑海中的画面准确呈现。',
    to: '/openai-image',
    image: '/images/hero-2.jpg'
  },
  {
    title: 'AI 视频',
    subtitle: '文字、图片和参考素材都能成为镜头，轻松完成电影感短片。',
    to: '/seedance',
    image: '/images/hero-3.jpg'
  }
]

const currentSlide = computed(() => slides[slideIndex.value])
const visibleShowcases = computed(() => showcases.slice(0, visibleCount.value))

function categoryTools(category) {
  if (category.key === 'chat') {
    return chatApps.filter(item => category.tools.includes(item.key))
  }
  return category.tools
    .map(key => tools.find(item => item.key === key))
    .filter(Boolean)
}

function toolOf(key) {
  return tools.find(item => item.key === key) || tools[0]
}

function toolTitle(key) {
  return toolOf(key).title
}

function toggleCategory(key) {
  openCategory.value = openCategory.value === key ? '' : key
}

function nextSlide() {
  slideIndex.value = (slideIndex.value + 1) % slides.length
}

function prevSlide() {
  slideIndex.value = (slideIndex.value - 1 + slides.length) % slides.length
}

onMounted(() => {
  timer = window.setInterval(nextSlide, 5000)
})

onBeforeUnmount(() => {
  if (timer) window.clearInterval(timer)
})
</script>

<style scoped>
.home {
  position: relative;
  min-height: 100vh;
  background:
    radial-gradient(900px 420px at 50% -12%, rgba(59, 130, 246, 0.09), transparent 62%),
    var(--app-bg);
}

.home-scroll {
  max-width: 1180px;
  margin: 0 auto;
  padding: 26px 30px 80px;
}

/* ---------- 轮播 ---------- */
.hero {
  position: relative;
  padding: 0 26px;
}

.hero-card {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1.05fr);
  min-height: 300px;
  border: 1px solid var(--app-border);
  border-radius: 18px;
  overflow: hidden;
  background: linear-gradient(120deg, rgba(18, 26, 40, 0.95), rgba(10, 14, 20, 0.9));
}

.hero-copy {
  padding: 44px 42px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 14px;
}

.eyebrow {
  display: inline-block;
  width: fit-content;
  padding: 5px 10px;
  border-radius: 999px;
  border: 1px solid rgba(96, 165, 250, 0.3);
  background: rgba(59, 130, 246, 0.1);
  color: #93c5fd;
  font-size: 11px;
  letter-spacing: 0.06em;
}

.hero-copy h1 {
  margin: 0;
  font-size: 42px;
  line-height: 1.1;
  letter-spacing: -0.04em;
}

.hero-copy p {
  margin: 0;
  max-width: 420px;
  color: var(--app-muted);
  font-size: 13.5px;
  line-height: 1.8;
}

.hero-cta {
  width: fit-content;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
  padding: 10px 18px;
  border-radius: 999px;
  background: var(--app-primary);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  transition: background 0.16s ease;
}

.hero-cta:hover {
  background: #2563eb;
}

.hero-dots {
  display: flex;
  gap: 6px;
  margin-top: 14px;
}

.hero-dots button {
  width: 18px;
  height: 4px;
  border: 0;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.16);
  cursor: pointer;
  transition: all 0.2s ease;
}

.hero-dots button.active {
  width: 30px;
  background: #60a5fa;
}

.hero-visual {
  position: relative;
  min-height: 300px;
  overflow: hidden;
}

.hero-visual :deep(.media-image) {
  position: absolute;
  inset: 0;
}

.category-media {
  position: absolute;
  inset: 0;
  transition: transform 0.3s ease;
}

.hero-arrow {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  z-index: 3;
  width: 38px;
  height: 38px;
  border-radius: 50%;
  border: 1px solid var(--app-border-strong);
  background: rgba(10, 14, 20, 0.82);
  backdrop-filter: blur(8px);
  color: var(--app-text);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.hero-arrow:hover {
  border-color: rgba(96, 165, 250, 0.6);
}

.hero-arrow.prev {
  left: 2px;
}

.hero-arrow.next {
  right: 2px;
}

/* ---------- 区块标题 ---------- */
.section {
  margin-top: 46px;
}

.section-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 20px;
}

.section-head h2 {
  margin: 10px 0 0;
  font-size: 24px;
  letter-spacing: -0.03em;
}

.section-head p {
  margin: 0;
  max-width: 360px;
  color: var(--app-muted);
  font-size: 12.5px;
  line-height: 1.7;
  text-align: right;
}

/* ---------- 快速开始 ---------- */
.category-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.category-card {
  position: relative;
  width: 100%;
  height: 168px;
  border: 1px solid var(--app-border);
  border-radius: 14px;
  overflow: hidden;
  padding: 0;
  cursor: pointer;
  display: block;
  text-align: left;
  background: #0d1117;
}

.category-card:hover .category-media {
  transform: scale(1.03);
}

.category-card .shade {
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, rgba(6, 9, 14, 0.92) 8%, rgba(6, 9, 14, 0.45) 70%, rgba(6, 9, 14, 0.2));
}

.category-copy {
  position: relative;
  z-index: 2;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 8px;
  padding: 0 24px;
}

.category-copy strong {
  font-size: 20px;
  letter-spacing: -0.02em;
}

.category-copy span {
  color: #9aa4af;
  font-size: 12px;
}

.category-copy b {
  margin-top: 8px;
  color: #7cb2ff;
  font-size: 11.5px;
  font-weight: 500;
}

.category-panel {
  margin-top: 8px;
  border: 1px solid var(--app-border);
  border-radius: 12px;
  background: #0d1117;
  overflow: hidden;
}

.category-tool {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 13px 16px;
  border-bottom: 1px solid var(--app-border);
  transition: background 0.16s ease;
}

.category-tool:last-child {
  border-bottom: 0;
}

.category-tool:hover {
  background: rgba(59, 130, 246, 0.08);
}

.tool-logo {
  width: 36px;
  height: 36px;
  flex-shrink: 0;
  border-radius: 50%;
  overflow: hidden;
  border: 1px solid var(--app-border);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.tool-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.tool-main strong {
  font-size: 13px;
  font-weight: 600;
}

.tool-main span {
  color: var(--app-muted);
  font-size: 11.5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.tool-go {
  flex-shrink: 0;
  color: #7cb2ff;
  font-size: 11.5px;
}

/* ---------- 精选作品 ---------- */
.showcase-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.showcase-card {
  border: 1px solid var(--app-border);
  border-radius: 14px;
  overflow: hidden;
  background: var(--app-surface);
  display: flex;
  flex-direction: column;
}

.showcase-media {
  aspect-ratio: 4 / 3;
  overflow: hidden;
  background: #0a0c10;
}

.showcase-media :deep(.media-image) {
  transition: transform 0.3s ease;
}

.showcase-card:hover .showcase-media :deep(.media-image) {
  transform: scale(1.03);
}

.showcase-body {
  padding: 14px 16px 16px;
  display: flex;
  flex-direction: column;
  gap: 7px;
  flex: 1;
}

.showcase-tag {
  width: fit-content;
  padding: 3px 8px;
  border-radius: 999px;
  background: rgba(59, 130, 246, 0.12);
  border: 1px solid rgba(59, 130, 246, 0.24);
  color: #93c5fd;
  font-size: 10.5px;
}

.showcase-body h3 {
  margin: 0;
  font-size: 14.5px;
  letter-spacing: -0.01em;
}

.showcase-body p {
  margin: 0;
  color: var(--app-muted);
  font-size: 11.5px;
  line-height: 1.7;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.showcase-link {
  margin-top: auto;
  padding-top: 6px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #7cb2ff;
  font-size: 11.5px;
  align-self: flex-end;
}

.load-more {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.home-foot {
  margin-top: 46px;
  padding-top: 18px;
  border-top: 1px solid var(--app-border);
  display: flex;
  justify-content: space-between;
  color: #5b636e;
  font-size: 11px;
}

/* ---------- 客服 ---------- */
.support-btn {
  position: fixed;
  right: 26px;
  bottom: 26px;
  z-index: 35;
  display: inline-flex;
  align-items: center;
  gap: 7px;
  height: 40px;
  padding: 0 16px;
  border-radius: 999px;
  border: 1px solid var(--app-border-strong);
  background: rgba(13, 17, 23, 0.92);
  backdrop-filter: blur(10px);
  color: var(--app-text);
  font-size: 12.5px;
  cursor: pointer;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.35);
}

.support-btn:hover {
  border-color: rgba(96, 165, 250, 0.55);
}

.support-btn .el-icon {
  color: #7cb2ff;
}

.support-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.support-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--app-border);
  color: var(--app-muted);
  font-size: 12.5px;
}

.support-row strong {
  color: var(--app-text);
  font-weight: 500;
}

.support-tip {
  color: var(--app-muted);
  font-size: 11.5px;
  line-height: 1.8;
}

.support-qr {
  height: 120px;
  border: 1px dashed var(--app-border-strong);
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #5b636e;
  font-size: 11.5px;
}

.support-qr .el-icon {
  font-size: 28px;
}

@media (max-width: 840px) {
  .home-scroll {
    padding: 20px 18px 70px;
  }

  .hero {
    padding: 0;
  }

  .hero-card {
    grid-template-columns: 1fr;
  }

  .hero-visual {
    min-height: 200px;
  }

  .hero-copy {
    padding: 28px 26px;
  }

  .hero-copy h1 {
    font-size: 32px;
  }

  .category-grid,
  .showcase-grid {
    grid-template-columns: 1fr;
  }

  .section-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .section-head p {
    text-align: left;
  }
}
</style>
