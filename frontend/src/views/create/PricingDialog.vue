<template>
  <el-dialog :model-value="modelValue" title="定价说明" width="400px" @update:model-value="$emit('update:modelValue', $event)">
    <div class="pricing">
      <div class="pricing-head">
        <span class="tool-name">{{ tool.title }}</span>
        <span class="tool-price">
          <el-icon><Coin /></el-icon>
          {{ displayPrice }} 积分 / 次
        </span>
      </div>
      <p v-if="modelName" class="pricing-model">当前模型：{{ modelName }}</p>
      <ul class="pricing-list">
        <li>按次扣减积分，提交任务时冻结，生成完成后结算。</li>
        <li>生成失败或任务取消会自动退回本次积分。</li>
        <li>当前账户可用余额：<strong>{{ balance.toFixed(2) }} 积分</strong>。</li>
        <li>余额不足时可在控制台「钱包与充值」中补充。</li>
      </ul>
      <div class="pricing-note">演示环境不产生真实扣费，积分与任务记录仅保存在本地。</div>
    </div>
    <template #footer>
      <el-button round @click="$emit('update:modelValue', false)">关闭</el-button>
      <el-button type="primary" round @click="goWallet">去充值</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { loadWallet } from '@/mock/account'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  tool: {
    type: Object,
    required: true
  },
  price: {
    type: Number,
    default: null
  },
  modelName: {
    type: String,
    default: ''
  }
})

const displayPrice = computed(() => {
  const value = props.price === null ? Number(props.tool.price) : Number(props.price)
  return Number.isFinite(value) ? value.toFixed(2) : props.tool.price
})

const emit = defineEmits(['update:modelValue'])
const router = useRouter()
const balance = ref(loadWallet().balance)

watch(
  () => props.modelValue,
  visible => {
    if (visible) balance.value = loadWallet().balance
  }
)

function goWallet() {
  emit('update:modelValue', false)
  router.push('/console/wallet?recharge=1')
}
</script>

<style scoped>
.pricing {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.pricing-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 10px;
  border: 1px solid rgba(59, 130, 246, 0.28);
  background: rgba(59, 130, 246, 0.08);
}

.tool-name {
  font-size: 13px;
  font-weight: 600;
}

.tool-price {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  color: #93c5fd;
  font-size: 12.5px;
}

.pricing-model {
  margin: -6px 0 0;
  color: var(--app-muted);
  font-size: 11.5px;
}

.pricing-list {
  margin: 0;
  padding-left: 18px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  color: var(--app-muted);
  font-size: 12px;
  line-height: 1.7;
}

.pricing-list strong {
  color: var(--app-text);
  font-weight: 600;
}

.pricing-note {
  padding: 10px 12px;
  border-radius: 8px;
  border: 1px dashed var(--app-border-strong);
  color: #5b636e;
  font-size: 11.5px;
  line-height: 1.7;
}
</style>
