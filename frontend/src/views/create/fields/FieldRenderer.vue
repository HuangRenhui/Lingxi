<template>
  <div class="field">
    <div v-if="field.type === 'modeTabs'" class="mode-tabs">
      <button
        v-for="option in field.options"
        :key="option"
        type="button"
        :class="{ active: modelValue === option }"
        @click="$emit('update:modelValue', option)"
      >
        {{ option }}
      </button>
    </div>

    <template v-else>
      <div class="label-row">
        <label>
          {{ field.label }}
          <el-icon class="info"><InfoFilled /></el-icon>
        </label>
        <button
          v-if="field.type === 'textarea' && modelValue"
          type="button"
          class="ghost-icon"
          title="清空"
          @click="$emit('update:modelValue', '')"
        >
          <el-icon><Delete /></el-icon>
        </button>
      </div>

      <el-select
        v-if="field.type === 'select'"
        :model-value="modelValue"
        size="default"
        style="width: 100%"
        @update:model-value="$emit('update:modelValue', $event)"
      >
        <el-option v-for="option in field.options" :key="option" :label="option" :value="option" />
      </el-select>

      <div v-else-if="field.type === 'segmented'" class="segmented">
        <button
          v-for="option in field.options"
          :key="option"
          type="button"
          :class="{ active: modelValue === option }"
          @click="$emit('update:modelValue', option)"
        >
          {{ option }}
        </button>
      </div>

      <div v-else-if="field.type === 'ratio'" class="ratio-grid">
        <button
          v-for="option in field.options"
          :key="option.value"
          type="button"
          class="ratio-btn"
          :class="{ active: modelValue === option.value }"
          @click="$emit('update:modelValue', option.value)"
        >
          <span class="ratio-shape" :style="{ width: `${option.w}px`, height: `${option.h}px` }" />
          <span class="ratio-label">{{ option.value }}</span>
        </button>
      </div>

      <el-input-number
        v-else-if="field.type === 'number'"
        :model-value="modelValue"
        :min="field.min ?? 0"
        :max="field.max ?? 999999"
        :step="field.step ?? 1"
        controls-position="right"
        style="width: 100%"
        @update:model-value="$emit('update:modelValue', $event)"
      />

      <el-switch
        v-else-if="field.type === 'switch'"
        :model-value="modelValue"
        @update:model-value="$emit('update:modelValue', $event)"
      />

      <el-input
        v-else-if="field.type === 'textarea'"
        :model-value="modelValue"
        type="textarea"
        :rows="field.rows || 4"
        :maxlength="field.maxlength"
        :placeholder="field.placeholder"
        resize="none"
        show-word-limit
        @update:model-value="$emit('update:modelValue', $event)"
      />

      <el-input
        v-else-if="field.type === 'text'"
        :model-value="modelValue"
        :placeholder="field.placeholder"
        @update:model-value="$emit('update:modelValue', $event)"
      />

      <div v-else-if="field.type === 'tags'" class="tag-list">
        <button
          v-for="option in field.options"
          :key="option"
          type="button"
          :class="{ active: hasTag(option) }"
          @click="toggleTag(option)"
        >
          {{ option }}
        </button>
      </div>

      <div v-else-if="field.type === 'upload'" class="upload-block">
        <el-upload
          :auto-upload="false"
          :show-file-list="false"
          :accept="field.accept"
          :multiple="field.multiple"
          :on-change="onFileChange"
        >
          <el-button size="small" type="primary" plain>
            <el-icon><Upload /></el-icon>
            {{ field.text || '上传' }}
          </el-button>
        </el-upload>
        <div v-if="files.length" class="file-list">
          <div v-for="file in files" :key="file" class="file-chip">
            <el-icon><Document /></el-icon>
            <span>{{ file }}</span>
            <button type="button" @click="removeFile(file)">
              <el-icon><Close /></el-icon>
            </button>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  field: {
    type: Object,
    required: true
  },
  modelValue: {
    type: [String, Number, Boolean, Array],
    default: ''
  }
})

const emit = defineEmits(['update:modelValue'])

const files = computed(() => {
  if (Array.isArray(props.modelValue)) return props.modelValue
  return props.modelValue ? [props.modelValue] : []
})

function hasTag(tag) {
  return Array.isArray(props.modelValue) && props.modelValue.includes(tag)
}

function toggleTag(tag) {
  const list = Array.isArray(props.modelValue) ? [...props.modelValue] : []
  const index = list.indexOf(tag)
  if (index >= 0) list.splice(index, 1)
  else list.push(tag)
  emit('update:modelValue', list)
}

function onFileChange(file) {
  if (props.field.multiple) {
    const list = Array.isArray(props.modelValue) ? [...props.modelValue] : []
    if (!list.includes(file.name)) list.push(file.name)
    emit('update:modelValue', list)
    return
  }
  emit('update:modelValue', file.name)
}

function removeFile(name) {
  if (props.field.multiple) {
    emit('update:modelValue', files.value.filter(item => item !== name))
    return
  }
  emit('update:modelValue', '')
}
</script>

<style scoped>
.field label {
  display: flex;
  align-items: center;
  gap: 5px;
  margin-bottom: 8px;
  color: var(--app-text);
  font-size: 12.5px;
  font-weight: 500;
}

.info {
  color: #5b636e;
  font-size: 12px;
}

.label-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.ghost-icon {
  border: 0;
  background: transparent;
  color: #5b636e;
  cursor: pointer;
  font-size: 14px;
  padding: 0 0 8px;
}

.ghost-icon:hover {
  color: var(--app-text);
}

.mode-tabs {
  display: flex;
  gap: 4px;
  padding: 4px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid var(--app-border);
  margin-bottom: 4px;
}

.mode-tabs button {
  flex: 1;
  min-width: 0;
  height: 30px;
  padding: 0 8px;
  border: 0;
  border-radius: 7px;
  background: transparent;
  color: var(--app-muted);
  font-size: 12px;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.16s ease;
}

.mode-tabs button.active {
  background: var(--app-primary-soft);
  color: #dbeafe;
  box-shadow: inset 0 0 0 1px rgba(59, 130, 246, 0.35);
}

.segmented {
  display: flex;
  gap: 6px;
}

.segmented button {
  flex: 1;
  height: 32px;
  border-radius: 8px;
  border: 1px solid var(--app-border-strong);
  background: var(--app-surface);
  color: var(--app-muted);
  font-size: 12px;
  cursor: pointer;
  transition: all 0.16s ease;
}

.segmented button.active {
  border-color: var(--app-primary);
  background: var(--app-primary-soft);
  color: #bfdbfe;
}

.ratio-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 7px;
}

.ratio-btn {
  height: 54px;
  border: 1px solid var(--app-border-strong);
  border-radius: 8px;
  background: var(--app-surface);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 5px;
  cursor: pointer;
  color: var(--app-muted);
  transition: all 0.16s ease;
}

.ratio-btn:hover {
  border-color: rgba(96, 165, 250, 0.45);
}

.ratio-btn.active {
  border-color: var(--app-primary);
  background: var(--app-primary-soft);
  color: #bfdbfe;
}

.ratio-shape {
  display: block;
  border: 1px solid currentColor;
  border-radius: 2px;
  opacity: 0.8;
}

.ratio-label {
  font-size: 10.5px;
  line-height: 1;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tag-list button {
  padding: 5px 11px;
  border-radius: 999px;
  border: 1px solid var(--app-border-strong);
  background: var(--app-surface);
  color: var(--app-muted);
  font-size: 11.5px;
  cursor: pointer;
  transition: all 0.16s ease;
}

.tag-list button:hover {
  border-color: rgba(96, 165, 250, 0.45);
}

.tag-list button.active {
  border-color: var(--app-primary);
  background: var(--app-primary-soft);
  color: #bfdbfe;
}

.file-list {
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.file-chip {
  display: flex;
  align-items: center;
  gap: 7px;
  padding: 7px 10px;
  border-radius: 8px;
  border: 1px solid var(--app-border);
  background: var(--app-surface);
  color: var(--app-muted);
  font-size: 11.5px;
}

.file-chip span {
  flex: 1;
  min-width: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.file-chip button {
  border: 0;
  background: transparent;
  color: var(--app-muted);
  cursor: pointer;
  display: flex;
}

.file-chip button:hover {
  color: #f87171;
}
</style>
