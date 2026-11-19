<template>
  <span class="media-image" :class="{ 'is-ready': ready, 'is-failed': failed }">
    <img
      v-if="src && !failed"
      :src="src"
      :alt="alt"
      :loading="eager ? 'eager' : 'lazy'"
      @load="ready = true"
      @error="failed = true"
    />
    <span class="media-fallback">
      <slot>
        <el-icon><Picture /></el-icon>
      </slot>
    </span>
  </span>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  src: {
    type: String,
    default: ''
  },
  alt: {
    type: String,
    default: ''
  },
  eager: {
    type: Boolean,
    default: false
  }
})

const ready = ref(false)
const failed = ref(false)

// 图片地址变化时重新进入加载状态，避免复用旧图造成闪烁
watch(
  () => props.src,
  () => {
    ready.value = false
    failed.value = false
  }
)
</script>

<style scoped>
.media-image {
  position: relative;
  display: block;
  width: 100%;
  height: 100%;
  overflow: hidden;
  background:
    radial-gradient(120% 120% at 20% 10%, rgba(59, 130, 246, 0.28), transparent 55%),
    linear-gradient(135deg, #172033, #0b0f16 60%, #101a2c);
}

.media-image img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
  opacity: 0;
  transition: opacity 0.32s ease;
}

.media-image.is-ready img {
  opacity: 1;
}

.media-fallback {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(147, 197, 253, 0.55);
  font-size: 18px;
  transition: opacity 0.2s ease;
}

.media-image.is-ready .media-fallback {
  opacity: 0;
}
</style>
