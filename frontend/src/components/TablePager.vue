<template>
  <div class="table-pager">
    <el-pagination
      :current-page="page"
      :page-size="pageSize"
      :total="total"
      :layout="layout"
      background
      @current-change="onChange"
    />
  </div>
</template>

<script setup>
import { watch } from 'vue'

const props = defineProps({
  page: { type: Number, default: 1 },
  pageSize: { type: Number, default: 10 },
  total: { type: Number, default: 0 },
  layout: { type: String, default: 'prev, pager, next' }
})

const emit = defineEmits(['update:page'])

function onChange(value) {
  emit('update:page', value)
}

// 数据量变少或当前页越界时自动回退，避免停留在空白页（total 不变但条目被删也要回退）
watch(
  () => [props.total, props.page, props.pageSize],
  () => {
    const max = Math.max(1, Math.ceil(props.total / props.pageSize))
    if (props.page > max) emit('update:page', max)
  }
)
</script>

<style scoped>
.table-pager {
  display: flex;
  justify-content: flex-end;
  padding: 12px 0 4px;
}
</style>
