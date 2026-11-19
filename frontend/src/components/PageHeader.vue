<template>
  <div class="page-header" :class="{ flush }">
    <div class="heading">
      <h1>{{ title }}</h1>
      <p v-if="description">{{ description }}</p>
    </div>
    <div v-if="$slots.default" class="actions">
      <slot />
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    required: true
  },
  description: {
    type: String,
    default: ''
  },
  // 管理后台等没有右上角积分浮层的页面，取消右侧预留间距
  flush: {
    type: Boolean,
    default: false
  }
})
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
  min-height: 56px;
  /* 为右上角固定的积分状态让出空间 */
  padding-right: 142px;
}

.page-header.flush {
  padding-right: 0;
}

h1 {
  margin: 0;
  font-size: 26px;
  font-weight: 700;
  letter-spacing: -0.02em;
  line-height: 1.25;
}

p {
  margin: 7px 0 0;
  color: var(--app-muted);
  font-size: 12.5px;
  line-height: 1.6;
  max-width: 760px;
}

.actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
  padding-top: 2px;
}

@media (max-width: 900px) {
  .page-header {
    padding-right: 0;
    flex-direction: column;
  }
}
</style>
