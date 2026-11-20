<template>
  <div class="admin-page">
    <PageHeader title="订阅套餐" description="维护套餐价格、积分额度与权益，修改后对用户端立即生效。" flush>
      <el-button round @click="loadAll">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </PageHeader>

    <section class="stat-grid">
      <StatCard label="套餐数量" :value="plans.length" icon="Medal" :hint="`启用中 ${stats.enabled} 个`" />
      <StatCard label="付费用户" :value="stats.paidUsers" icon="User" hint="当前持有付费套餐" />
      <StatCard label="本月订阅收入" :value="`¥${stats.monthlyIncome.toLocaleString()}`" icon="Money" hint="含年付摊销" />
      <StatCard label="续费率" :value="stats.renewRate" unit="%" icon="RefreshRight" hint="近 30 天到期续费占比" />
    </section>

    <section class="plan-grid">
      <article v-for="plan in plans" :key="plan.key" class="panel plan-card" :class="{ disabled: !plan.enabled }">
        <div class="plan-head">
          <div class="plan-title">
            <h3>{{ plan.name }}</h3>
            <el-tag v-if="plan.tag" type="warning" size="small" effect="plain">{{ plan.tag }}</el-tag>
            <el-tag v-if="!plan.enabled" type="info" size="small" effect="plain">已下线</el-tag>
          </div>
          <el-switch v-model="plan.enabled" @change="togglePlan(plan)" />
        </div>

        <div class="plan-price">
          <strong>¥{{ plan.monthly }}</strong>
          <span>/ 月</span>
          <small>年付 ¥{{ plan.yearly }}</small>
        </div>

        <div class="plan-metrics">
          <div class="metric">
            <span>每月积分</span>
            <strong class="mono">{{ plan.credits }}</strong>
          </div>
          <div class="metric">
            <span>并行任务</span>
            <strong class="mono">{{ plan.parallelTasks }}</strong>
          </div>
          <div class="metric">
            <span>更新于</span>
            <strong>{{ plan.updatedAt.slice(5) }}</strong>
          </div>
        </div>

        <ul class="plan-features">
          <li v-for="item in plan.features" :key="item">
            <el-icon><Check /></el-icon>
            {{ item }}
          </li>
        </ul>

        <el-button class="plan-edit" round @click="openEdit(plan)">编辑套餐</el-button>
      </article>
    </section>

    <section class="panel table-panel">
      <div class="table-head">
        <h3>变更记录</h3>
        <span>最近 20 条套餐调整</span>
      </div>
      <el-table :data="paged" size="small">
        <el-table-column prop="createdAt" label="时间" width="150" />
        <el-table-column prop="planName" label="套餐" width="120" />
        <el-table-column prop="action" label="变更内容" min-width="260" />
        <el-table-column prop="operator" label="操作人" width="110" />
        <template #empty>
          <div class="table-empty">暂无变更记录</div>
        </template>
      </el-table>
      <TablePager v-model:page="page" :total="history.length" />
    </section>

    <el-dialog v-model="editVisible" title="编辑套餐" width="520px">
      <el-form :model="form" label-position="top">
        <div class="form-row">
          <el-form-item label="套餐名称">
            <el-input v-model="form.name" maxlength="12" />
          </el-form-item>
          <el-form-item label="推荐标签">
            <el-input v-model="form.tag" maxlength="10" placeholder="留空不展示" />
          </el-form-item>
        </div>
        <div class="form-row">
          <el-form-item label="月付价格（元）">
            <el-input-number v-model="form.monthly" :min="0" :max="9999" :step="10" controls-position="right" />
          </el-form-item>
          <el-form-item label="年付价格（元）">
            <el-input-number v-model="form.yearly" :min="0" :max="99999" :step="100" controls-position="right" />
          </el-form-item>
        </div>
        <div class="form-row">
          <el-form-item label="每月积分额度">
            <el-input-number v-model="form.credits" :min="0" :max="1000000" :step="100" controls-position="right" />
          </el-form-item>
          <el-form-item label="并行任务上限">
            <el-input-number v-model="form.parallelTasks" :min="1" :max="100" controls-position="right" />
          </el-form-item>
        </div>
        <el-form-item label="套餐权益（每行一条）">
          <el-input v-model="featuresText" type="textarea" :rows="5" placeholder="每行一条权益说明" />
        </el-form-item>
        <el-form-item label="套餐状态">
          <el-switch v-model="form.enabled" active-text="启用" inactive-text="停用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button round @click="editVisible = false">取消</el-button>
        <el-button round type="primary" @click="savePlan">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { readJson, writeJson } from '@/utils/storage'
import dayjs from 'dayjs'
import PageHeader from '@/components/PageHeader.vue'
import StatCard from '@/components/StatCard.vue'
import TablePager from '@/components/TablePager.vue'
import { loadAdminPlans, saveAdminPlans } from '@/mock/admin-extra'

const HISTORY_KEY = 'lingxi-admin-plan-logs'

const plans = ref(loadAdminPlans())
const history = ref(loadHistory())
const editVisible = ref(false)
const featuresText = ref('')
const currentKey = ref('')
const page = ref(1)
const pageSize = 10

const form = reactive({
  name: '',
  tag: '',
  monthly: 0,
  yearly: 0,
  credits: 0,
  parallelTasks: 1,
  enabled: true
})

const stats = computed(() => ({
  enabled: plans.value.filter(plan => plan.enabled).length,
  paidUsers: 186,
  monthlyIncome: 6820,
  renewRate: 78.4
}))

const paged = computed(() =>
  history.value.slice((page.value - 1) * pageSize, page.value * pageSize)
)

function loadHistory() {
  const cached = readJson(HISTORY_KEY, null)
  if (Array.isArray(cached)) return cached
  const seed = [
    { id: 'pl-1', createdAt: dayjs().subtract(2, 'day').format('YYYY-MM-DD HH:mm'), planName: '专业版', action: '月付 ¥78 → ¥68，积分额度不变', operator: 'admin' },
    { id: 'pl-2', createdAt: dayjs().subtract(12, 'day').format('YYYY-MM-DD HH:mm'), planName: '旗舰版', action: '新增权益「自定义 Skills 与连接器」', operator: 'admin' },
    { id: 'pl-3', createdAt: dayjs().subtract(30, 'day').format('YYYY-MM-DD HH:mm'), planName: '体验版', action: '积分额度 100 → 200', operator: 'admin' }
  ]
  writeJson(HISTORY_KEY, seed)
  return seed
}

function loadAll() {
  plans.value = loadAdminPlans()
  history.value = loadHistory()
  ElMessage.success('已刷新套餐配置')
}

function togglePlan(plan) {
  saveAdminPlans(plans.value)
  ElMessage.success(plan.enabled ? `「${plan.name}」已上线` : `「${plan.name}」已下线`)
}

function openEdit(plan) {
  currentKey.value = plan.key
  Object.assign(form, {
    name: plan.name,
    tag: plan.tag || '',
    monthly: plan.monthly,
    yearly: plan.yearly,
    credits: plan.credits,
    parallelTasks: plan.parallelTasks,
    enabled: plan.enabled
  })
  featuresText.value = (plan.features || []).join('\n')
  editVisible.value = true
}

function savePlan() {
  if (!form.name.trim()) {
    ElMessage.warning('请填写套餐名称')
    return
  }
  const target = plans.value.find(plan => plan.key === currentKey.value)
  if (!target) return
  const before = `月付 ¥${target.monthly} / 积分 ${target.credits}`
  Object.assign(target, {
    name: form.name.trim(),
    tag: form.tag.trim(),
    monthly: form.monthly,
    yearly: form.yearly,
    credits: form.credits,
    parallelTasks: form.parallelTasks,
    enabled: form.enabled,
    features: featuresText.value
      .split('\n')
      .map(item => item.trim())
      .filter(Boolean),
    updatedAt: dayjs().format('YYYY-MM-DD HH:mm')
  })
  saveAdminPlans(plans.value)
  const after = `月付 ¥${target.monthly} / 积分 ${target.credits}`
  const record = {
    id: `pl-${Date.now()}`,
    createdAt: dayjs().format('YYYY-MM-DD HH:mm'),
    planName: target.name,
    action: `${before} → ${after}`,
    operator: 'admin'
  }
  history.value = [record, ...history.value].slice(0, 20)
  if (!writeJson(HISTORY_KEY, history.value)) {
    ElMessage.warning('本地存储不可用，套餐变更记录仅保留到刷新前')
  }
  editVisible.value = false
  ElMessage.success('套餐已保存')
}
</script>

<style scoped>
.plan-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.plan-card {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 18px 20px 20px;
  transition: all 0.16s ease;
}

.plan-card:hover {
  border-color: var(--app-border-strong);
}

.plan-card.disabled {
  opacity: 0.62;
}

.plan-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.plan-title {
  display: flex;
  align-items: center;
  gap: 7px;
}

.plan-title h3 {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
}

.plan-price {
  display: flex;
  align-items: baseline;
  gap: 5px;
}

.plan-price strong {
  font-size: 27px;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.plan-price span {
  color: var(--app-muted);
  font-size: 11.5px;
}

.plan-price small {
  margin-left: auto;
  color: var(--app-muted);
  font-size: 11px;
}

.plan-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
  padding: 10px 0;
  border-top: 1px solid var(--app-border);
  border-bottom: 1px solid var(--app-border);
}

.metric {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.metric span {
  color: var(--app-muted);
  font-size: 10.5px;
}

.metric strong {
  font-size: 12.5px;
  font-weight: 600;
}

.plan-features {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 7px;
  flex: 1;
}

.plan-features li {
  display: flex;
  align-items: flex-start;
  gap: 7px;
  color: var(--app-muted);
  font-size: 11.5px;
  line-height: 1.55;
}

.plan-features :deep(.el-icon) {
  margin-top: 2px;
  color: #60a5fa;
  font-size: 12px;
}

.plan-edit {
  width: 100%;
  margin-top: 4px;
}

.table-panel {
  padding: 16px 20px 10px;
}

.table-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.table-head h3 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
}

.table-head span {
  color: var(--app-muted);
  font-size: 11.5px;
}

.table-empty {
  padding: 30px 0;
  color: var(--app-muted);
  font-size: 12px;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

@media (max-width: 1100px) {
  .plan-grid {
    grid-template-columns: 1fr;
  }
}
</style>
