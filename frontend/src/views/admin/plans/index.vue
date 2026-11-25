<template>
  <div class="admin-page">
    <PageHeader title="订阅套餐" description="维护套餐价格、积分额度与权益，修改后对用户端立即生效。" flush>
      <el-button round @click="loadAll()">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </PageHeader>

    <section class="stat-grid">
      <StatCard label="套餐数量" :value="plans.length" icon="Medal" :hint="`启用中 ${stats.enabled} 个`" />
      <StatCard label="启用套餐" :value="stats.enabled" icon="Present" hint="当前对用户可见" />
      <StatCard label="最低月付" :value="`¥${stats.minMonthly}`" icon="Money" hint="启用套餐中的最低月价" />
      <StatCard label="每月积分合计" :value="stats.credits" icon="Coin" hint="启用套餐额度之和" />
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
            <span>更新于</span>
            <strong>{{ plan.updatedAt ? plan.updatedAt.slice(5) : '—' }}</strong>
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
          <el-form-item label="套餐标识">
            <el-input v-model="form.planKey" maxlength="32" placeholder="字母、数字、下划线或中划线" />
          </el-form-item>
          <el-form-item label="排序值">
            <el-input-number v-model="form.sortOrder" :min="0" :max="9999" :step="1" controls-position="right" />
          </el-form-item>
        </div>
        <div class="form-tip">标识修改后新订阅按新标识查询，历史订阅仍保留原快照；排序值越小越靠前。</div>
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
        <el-button round type="primary" :loading="saving" @click="savePlan">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { readJson, writeJson } from '@/utils/storage'
import dayjs from 'dayjs'
import PageHeader from '@/components/PageHeader.vue'
import StatCard from '@/components/StatCard.vue'
import TablePager from '@/components/TablePager.vue'
import { loadAdminPlans, saveAdminPlans } from '@/mock/admin-extra'
import { listAdminPlans, updateAdminPlan } from '@/api/adminPlan'

const HISTORY_KEY = 'lingxi-admin-plan-logs'

const plans = ref([])
const demoMode = ref(false)
const history = ref(loadHistory())
const editVisible = ref(false)
const featuresText = ref('')
const currentId = ref('')
const saving = ref(false)
const page = ref(1)
const pageSize = 10

const form = reactive({
  planKey: '',
  name: '',
  tag: '',
  monthly: 0,
  yearly: 0,
  credits: 0,
  sortOrder: 0,
  enabled: true
})

/** 后端字段（planKey/monthlyPrice/creditsPerMonth）映射为页面结构 */
function fromApi(plan) {
  return {
    id: plan.id,
    key: plan.planKey,
    name: plan.name,
    monthly: Number(plan.monthlyPrice || 0),
    yearly: Number(plan.yearlyPrice || 0),
    credits: Number(plan.creditsPerMonth || 0),
    tag: plan.tag || '',
    features: plan.features || [],
    enabled: Boolean(plan.enabled),
    sortOrder: Number(plan.sortOrder ?? 0),
    updatedAt: plan.updatedAt ? dayjs(plan.updatedAt).format('YYYY-MM-DD HH:mm') : ''
  }
}

async function loadAll(silent = false) {
  try {
    const result = await listAdminPlans({ page: 1, size: 100 })
    // 后端已返回 PageResult；同时兼容未接后端时 Mock 返回的数组
    const list = Array.isArray(result) ? result : result?.list
    if (Array.isArray(list)) {
      plans.value = list.map(fromApi)
      demoMode.value = false
    } else if (import.meta.env.DEV) {
      plans.value = loadAdminPlans()
      demoMode.value = true
    } else {
      plans.value = []
      demoMode.value = false
    }
  } catch (error) {
    plans.value = []
    demoMode.value = false
    if (import.meta.env.DEV) {
      plans.value = loadAdminPlans()
      demoMode.value = true
    }
  }
  history.value = import.meta.env.DEV ? loadHistory() : []
  if (!silent) {
    ElMessage.success('已刷新套餐配置')
  }
}

onMounted(() => loadAll(true))

// 仅展示可由套餐数据直接推导的指标；付费用户数、订阅收入等需要后端统计接口，不能写死假数字
const stats = computed(() => {
  const enabledPlans = plans.value.filter(plan => plan.enabled)
  return {
    enabled: enabledPlans.length,
    minMonthly: enabledPlans.length ? Math.min(...enabledPlans.map(plan => Number(plan.monthly) || 0)) : 0,
    credits: enabledPlans.reduce((sum, plan) => sum + (Number(plan.credits) || 0), 0)
  }
})

const paged = computed(() =>
  history.value.slice((page.value - 1) * pageSize, page.value * pageSize)
)

function loadHistory() {
  // 只展示本机真实发生过的调整记录，不再预置虚构的变更历史
  const cached = readJson(HISTORY_KEY, null)
  return Array.isArray(cached) ? cached : []
}

async function togglePlan(plan) {
  if (demoMode.value) {
    saveAdminPlans(plans.value)
    ElMessage.success(plan.enabled ? `「${plan.name}」已上线` : `「${plan.name}」已下线`)
    return
  }
  try {
    await updateAdminPlan(plan.id, { enabled: plan.enabled ? 1 : 0 })
    ElMessage.success(plan.enabled ? `「${plan.name}」已上线` : `「${plan.name}」已下线`)
  } catch (error) {
    // 失败时回滚开关状态
    plan.enabled = !plan.enabled
  }
}

function openEdit(plan) {
  currentId.value = plan.id ?? plan.key
  Object.assign(form, {
    planKey: plan.key || '',
    name: plan.name,
    tag: plan.tag || '',
    monthly: plan.monthly,
    yearly: plan.yearly,
    credits: plan.credits,
    sortOrder: Number(plan.sortOrder ?? 0),
    enabled: plan.enabled
  })
  featuresText.value = (plan.features || []).join('\n')
  editVisible.value = true
}

async function savePlan() {
  if (saving.value) return
  if (!form.name.trim()) {
    ElMessage.warning('请填写套餐名称')
    return
  }
  const planKey = form.planKey.trim()
  if (!/^[A-Za-z0-9_-]+$/.test(planKey)) {
    ElMessage.warning('套餐标识仅支持字母、数字、下划线和中划线')
    return
  }
  if (!Number.isInteger(form.sortOrder) || form.sortOrder < 0) {
    ElMessage.warning('排序值需为不小于 0 的整数')
    return
  }
  const target = plans.value.find(plan => (plan.id ?? plan.key) === currentId.value)
  if (!target) return
  const before = `标识 ${target.key || '—'} / 排序 ${target.sortOrder ?? 0} / 月付 ¥${target.monthly} / 积分 ${target.credits}`
  const payload = {
    planKey,
    name: form.name.trim(),
    tag: form.tag.trim(),
    monthlyPrice: form.monthly,
    yearlyPrice: form.yearly,
    creditsPerMonth: form.credits,
    features: featuresText.value
      .split('\n')
      .map(item => item.trim())
      .filter(Boolean),
    sortOrder: form.sortOrder,
    enabled: form.enabled ? 1 : 0
  }

  if (demoMode.value) {
    Object.assign(target, {
      key: payload.planKey,
      name: payload.name,
      tag: payload.tag,
      monthly: payload.monthlyPrice,
      yearly: payload.yearlyPrice,
      credits: payload.creditsPerMonth,
      sortOrder: payload.sortOrder,
      enabled: form.enabled,
      features: payload.features,
      updatedAt: dayjs().format('YYYY-MM-DD HH:mm')
    })
    saveAdminPlans(plans.value)
  } else {
    saving.value = true
    try {
      await updateAdminPlan(currentId.value, payload)
      await loadAll(true)
    } catch (error) {
      return
    } finally {
      saving.value = false
    }
  }

  const after = `标识 ${payload.planKey} / 排序 ${payload.sortOrder} / 月付 ¥${form.monthly} / 积分 ${form.credits}`
  const record = {
    id: `pl-${Date.now()}`,
    createdAt: dayjs().format('YYYY-MM-DD HH:mm'),
    planName: payload.name,
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
  grid-template-columns: repeat(2, minmax(0, 1fr));
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

.form-tip {
  margin: -6px 0 12px;
  color: var(--app-muted);
  font-size: 11.5px;
  line-height: 1.5;
}

@media (max-width: 1100px) {
  .plan-grid {
    grid-template-columns: 1fr;
  }
}
</style>
