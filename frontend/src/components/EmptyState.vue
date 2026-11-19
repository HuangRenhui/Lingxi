<template>
  <div class="empty-state" :class="{ compact }">
    <div class="empty-icon">
      <el-icon><Box /></el-icon>
    </div>
    <p>{{ text }}</p>
    <slot />
  </div>
</template>

<script setup>
defineProps({
  text: {
    type: String,
    default: '暂无数据'
  },
  compact: {
    type: Boolean,
    default: false
  }
})
</script>

<style scoped>
.empty-state {
  min-height: 260px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 32px;
  color: var(--app-muted);
}

.empty-state.compact {
  min-height: 160px;
}

.empty-icon {
  width: 46px;
  height: 46px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid var(--app-border);
  font-size: 20px;
  color: #6e7681;
}

p {
  margin: 0;
  font-size: 12.5px;
}
</style>
