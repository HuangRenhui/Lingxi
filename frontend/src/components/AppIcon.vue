<template>
  <span class="app-icon" :style="{ background: theme.bg }">
    <span
      v-if="theme.textGradient"
      class="app-icon-text is-gradient"
      :style="{ backgroundImage: theme.textGradient }"
    >
      {{ theme.glyph || initial }}
    </span>
    <span
      v-else-if="!src"
      class="app-icon-text"
      :class="{ 'is-emoji': theme.emoji }"
      :style="{ color: theme.fg || '#ffffff' }"
    >
      {{ theme.glyph || initial }}
    </span>
    <img v-else :src="src" :alt="alt" class="app-icon-img" :class="{ 'is-fill': theme.fill }" />
  </span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  name: {
    type: String,
    required: true
  },
  alt: {
    type: String,
    default: ''
  }
})

// 本地品牌图标：随构建打包，打开面板瞬间可用，不依赖图片生成接口
const iconModules = import.meta.glob('../assets/app-icons/*.{svg,png}', {
  eager: true,
  query: '?url',
  import: 'default'
})

const iconUrls = {}
Object.entries(iconModules).forEach(([path, url]) => {
  const key = path.split('/').pop().replace(/\.(svg|png)$/, '')
  iconUrls[key] = url
})

/**
 * 每个应用一套图标主题：
 * - 品牌图标全部来自官方 Logo（LobeHub / Simple Icons / Twemoji / 官方应用商店）
 * - 深色底上的单色 Logo 已在 SVG 内直接改为白色
 * - 平台型工具（自动成片、QR Art、数字人、Producer）没有官方品牌，保留通用图形
 */
const themes = {
  chatgpt: { bg: '#f4efe4' },
  deepseek: { bg: '#e8f0ff' },
  grok: { bg: '#0b0b0c' },
  gemini: { bg: '#ffffff' },
  claude: { bg: '#fbf2ec' },
  kimi: { bg: '#ffffff' },
  qwen: { bg: '#f2edff' },
  doubao: { bg: '#dbeafe', fill: true },
  maestro: { bg: 'linear-gradient(135deg, #f59e0b, #92400e)' },
  'openai-image': { icon: 'chatgpt', bg: 'linear-gradient(135deg, #fdf3e1, #fbcfe8)' },
  nanobanana: { bg: '#111827' },
  seedream: { bg: '#ffffff' },
  flux: { bg: '#f5f5f5' },
  ideogram: { bg: '#ffffff' },
  midjourney: { bg: '#f8fafc' },
  qrart: { bg: '#ffffff' },
  seedance: { bg: '#ffffff' },
  kling: { bg: '#0b0b0c' },
  veo: { bg: '#ffffff' },
  'grok-video': { icon: 'grok', bg: '#0b0b0c' },
  pika: { bg: 'linear-gradient(135deg, #ec4899, #111827)' },
  minimax: { bg: '#1b1024' },
  sora: { bg: '#0b0b0c' },
  luma: { bg: '#ffffff' },
  runway: { bg: '#ffffff' },
  wan: { bg: '#ffffff' },
  'digital-human': { bg: 'linear-gradient(135deg, #22d3ee, #0f766e)' },
  suno: { bg: '#0b0b0c' },
  producer: { bg: 'linear-gradient(135deg, #f97316, #7c2d12)' },
  udio: { bg: '#ffffff' }
}

const theme = computed(() => themes[props.name] || {
  bg: 'linear-gradient(135deg, #3b82f6, #1e3a8a)',
  glyph: props.name.slice(0, 1).toUpperCase()
})

const src = computed(() => iconUrls[theme.value.icon || props.name] || '')

const initial = computed(() => (props.alt || props.name).slice(0, 1).toUpperCase())
</script>

<style scoped>
.app-icon {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  overflow: hidden;
  border-radius: 50%;
  container-type: size;
}

.app-icon-img {
  width: 64%;
  height: 64%;
  object-fit: contain;
  display: block;
}

/* 应用商店图标本身就是完整圆形画面，直接铺满 */
.app-icon-img.is-fill {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.app-icon-text {
  font-size: 40cqw;
  font-weight: 700;
  line-height: 1;
  letter-spacing: -0.03em;
}

.app-icon-text.is-emoji {
  font-size: 38cqw;
  font-weight: 400;
}

.app-icon-text.is-gradient {
  background-clip: text;
  -webkit-background-clip: text;
  color: transparent;
  font-family: Georgia, "Times New Roman", serif;
  font-weight: 700;
}
</style>
