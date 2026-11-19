<template>
  <div class="usage-trend">
    <div v-if="!points.length" class="trend-empty">暂无数据</div>
    <template v-else>
      <div class="trend-meta">
        <span class="trend-total">{{ total.toLocaleString() }} <small>{{ unit }}</small></span>
        <span class="trend-range">{{ points[0]?.date }} - {{ points[points.length - 1]?.date }}</span>
      </div>
      <div class="trend-chart">
        <div v-for="(point, index) in points" :key="point.date" class="trend-col">
          <div class="trend-bar-wrap">
            <div
              class="trend-bar"
              :style="{ height: heightOf(point.requests) }"
              :title="`${point.date} · ${point.requests} ${unit} · ${point.credits} 积分`"
            />
          </div>
          <span class="trend-label">{{ index % 2 === 0 ? point.date : '' }}</span>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  points: {
    type: Array,
    default: () => []
  },
  unit: {
    type: String,
    default: '次'
  }
})

const max = computed(() => Math.max(1, ...props.points.map(point => point.requests)))
const total = computed(() => props.points.reduce((sum, point) => sum + point.requests, 0))

function heightOf(value) {
  return `${Math.max(4, Math.round((value / max.value) * 100))}%`
}
</script>

<style scoped>
.usage-trend {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.trend-meta {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
}

.trend-total {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.trend-total small {
  margin-left: 4px;
  font-size: 11px;
  font-weight: 500;
  color: var(--app-muted);
}

.trend-range {
  color: var(--app-muted);
  font-size: 11px;
}

.trend-empty {
  padding: 56px 0;
  text-align: center;
  color: var(--app-muted);
  font-size: 12px;
}

.trend-chart {
  display: flex;
  align-items: flex-end;
  gap: 6px;
  height: 190px;
  padding-top: 6px;
}

.trend-col {
  flex: 1;
  min-width: 0;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  gap: 6px;
}

.trend-bar-wrap {
  flex: 1;
  min-height: 0;
  display: flex;
  align-items: flex-end;
}

.trend-bar {
  width: 100%;
  border-radius: 5px 5px 2px 2px;
  background: linear-gradient(180deg, #60a5fa, rgba(37, 99, 235, 0.55));
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.18);
  transition: filter 0.16s ease;
}

.trend-bar:hover {
  filter: brightness(1.2);
}

.trend-label {
  height: 14px;
  color: var(--app-muted);
  font-size: 9.5px;
  text-align: center;
  white-space: nowrap;
}
</style>
