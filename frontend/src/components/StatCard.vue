<template>
  <div class="stat-card panel">
    <div class="stat-top">
      <div class="stat-icon">
        <el-icon><component :is="icon" /></el-icon>
      </div>
      <slot name="action" />
    </div>
    <span class="stat-label">{{ label }}</span>
    <strong class="stat-value">
      {{ value }}<small v-if="unit">{{ unit }}</small>
    </strong>
    <p v-if="hint" class="stat-hint">{{ hint }}</p>
  </div>
</template>

<script setup>
defineProps({
  label: {
    type: String,
    required: true
  },
  value: {
    type: [String, Number],
    default: '0'
  },
  unit: {
    type: String,
    default: ''
  },
  hint: {
    type: String,
    default: ''
  },
  icon: {
    type: String,
    default: 'DataLine'
  }
})
</script>

<style scoped>
.stat-card {
  padding: 18px 20px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-height: 132px;
}

.stat-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}

.stat-icon {
  width: 32px;
  height: 32px;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--app-primary-soft);
  color: #7cb2ff;
  font-size: 15px;
}

.stat-label {
  color: var(--app-muted);
  font-size: 12.5px;
}

.stat-value {
  font-size: 26px;
  font-weight: 700;
  letter-spacing: -0.02em;
  line-height: 1.15;
}

.stat-value small {
  margin-left: 3px;
  font-size: 12px;
  font-weight: 500;
  color: var(--app-muted);
}

.stat-hint {
  margin: 2px 0 0;
  color: var(--app-muted);
  font-size: 11.5px;
  line-height: 1.6;
}
</style>
