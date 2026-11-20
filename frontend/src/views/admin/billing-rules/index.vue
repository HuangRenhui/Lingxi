<template>
  <div class="admin-page">
    <PageHeader title="计费规则" description="维护各应用与模型的计费单位、单价与倍率，保存后立即用于后续调用。" flush>
      <el-button type="primary" round @click="openCreate">
        <el-icon><Plus /></el-icon>
        新增规则
      </el-button>
      <el-button round @click="openAdjust">
        <el-icon><Sort /></el-icon>
        批量调价
      </el-button>
      <el-button round @click="exportCsv">
        <el-icon><Download /></el-icon>
        导出规则
      </el-button>
    </PageHeader>

    <section class="stat-grid">
      <StatCard label="计费项" :value="rules.length" icon="PriceTag" :hint="`启用中 ${stats.enabled} 项`" />
      <StatCard label="平均毛利率" :value="stats.avgMargin" icon="TrendCharts" hint="实际售价对比上游成本" />
      <StatCard label="亏损风险" :value="stats.risky" icon="Warning" hint="毛利率低于 15% 的启用项" />
      <StatCard label="最近调价" :value="stats.lastUpdated" icon="Clock" hint="最后一条规则更新时间" />
    </section>

    <section class="filters panel">
      <el-input v-model="keyword" class="filter-search" placeholder="搜索应用或模型" clearable>
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-select v-model="category" class="filter-item">
        <el-option label="全部类型" value="all" />
        <el-option label="对话" value="对话" />
        <el-option label="图片" value="图片" />
        <el-option label="视频" value="视频" />
        <el-option label="音乐" value="音乐" />
      </el-select>
      <el-select v-model="status" class="filter-item">
        <el-option label="全部状态" value="all" />
        <el-option label="已启用" value="on" />
        <el-option label="已停用" value="off" />
      </el-select>
      <el-button round @click="resetFilters">重置筛选</el-button>
      <span class="filter-action muted">共 {{ filtered.length }} 条规则</span>
    </section>

    <section class="panel table-panel">
      <el-table :data="paged" size="small">
        <el-table-column prop="category" label="类型" width="90">
          <template #default="{ row }">
            <el-tag size="small" effect="plain" :type="categoryType(row.category)">{{ row.category }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="target" label="应用" width="150" />
        <el-table-column label="模型" min-width="200">
          <template #default="{ row }">
            <span class="mono model">{{ row.model }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="unit" label="计费单位" width="100" />
        <el-table-column width="130">
          <template #header>
            <el-tooltip content="对话类依次为 输入 / 输出（积分 / 1K tokens），其余类型为单次单价" placement="top">
              <span class="header-tip">单价（积分）</span>
            </el-tooltip>
          </template>
          <template #default="{ row }">
            <span class="mono">{{ priceText(row) }}</span>
          </template>
        </el-table-column>
        <el-table-column width="130">
          <template #header>
            <el-tooltip content="上游成本折合积分，按同一口径与售价对比；对话类依次为 输入 / 输出" placement="top">
              <span class="header-tip">上游成本</span>
            </el-tooltip>
          </template>
          <template #default="{ row }">
            <span class="mono cost">{{ costText(row) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="倍率" width="80">
          <template #default="{ row }">
            <span class="mono">{{ row.multiplier }}x</span>
          </template>
        </el-table-column>
        <el-table-column label="实际单价" width="130">
          <template #default="{ row }">
            <span class="mono accent">{{ effectiveText(row) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="毛利率" width="95">
          <template #default="{ row }">
            <span class="mono margin" :class="marginClass(row)">{{ marginText(row) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-switch v-model="row.enabled" @change="toggleRule(row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <div class="actions">
              <el-button link size="small" @click="openEdit(row)">编辑</el-button>
              <el-button link size="small" type="danger" @click="removeRule(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
        <template #empty>
          <div class="table-empty">没有匹配的计费规则</div>
        </template>
      </el-table>
      <TablePager v-model:page="page" :total="filtered.length" />
    </section>

    <section class="panel note-panel">
      <div class="note-head">
        <el-icon><InfoFilled /></el-icon>
        <h3>计费口径</h3>
      </div>
      <ul class="note-list">
        <li>对话类按输入 / 输出分别计价，实际单价 = 单价 × 倍率；命中缓存的历史上下文按「缓存输入价」计费。</li>
        <li>上游成本以同一积分口径录入，毛利率 = 1 − 成本 ÷ 实际售价；低于 15% 会在列表中告警，为负即为亏损。</li>
        <li>请求前按最大轮次预扣积分，结束后以 usage 结算多退少补；用户中断时已产生的用量同样照实扣费。</li>
        <li>停用后的规则不再用于新调用，历史扣费与成本记录不受影响；批量调价只改售价，不改成本。</li>
      </ul>
    </section>

    <el-dialog v-model="editVisible" :title="isCreate ? '新增计费规则' : '编辑计费规则'" width="480px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-position="top">
        <template v-if="isCreate">
          <div class="form-row">
            <el-form-item label="类型" prop="category">
              <el-select v-model="form.category">
                <el-option v-for="item in categories" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
            <el-form-item label="应用" prop="target">
              <el-input v-model="form.target" placeholder="例如：ChatGPT" />
            </el-form-item>
          </div>
          <el-form-item label="模型" prop="model">
            <el-input v-model="form.model" placeholder="例如：GPT-6 Astra" />
          </el-form-item>
        </template>
        <el-form-item v-else label="计费对象">
          <el-input :model-value="`${form.target} · ${form.model}`" disabled />
        </el-form-item>
        <template v-if="isTokenForm">
          <div class="form-row">
            <el-form-item label="输入单价（积分 / 1K）">
              <el-input-number v-model="form.priceIn" :min="0" :max="10000" :step="0.01" :precision="4" controls-position="right" />
            </el-form-item>
            <el-form-item label="输出单价（积分 / 1K）">
              <el-input-number v-model="form.priceOut" :min="0" :max="10000" :step="0.01" :precision="4" controls-position="right" />
            </el-form-item>
          </div>
          <el-form-item label="缓存输入价（积分 / 1K）">
            <el-input-number v-model="form.priceCachedIn" :min="0" :max="10000" :step="0.001" :precision="4" controls-position="right" />
          </el-form-item>
          <div class="form-row">
            <el-form-item label="输入成本（积分 / 1K）">
              <el-input-number v-model="form.costIn" :min="0" :max="10000" :step="0.001" :precision="4" controls-position="right" />
            </el-form-item>
            <el-form-item label="输出成本（积分 / 1K）">
              <el-input-number v-model="form.costOut" :min="0" :max="10000" :step="0.001" :precision="4" controls-position="right" />
            </el-form-item>
          </div>
        </template>
        <div v-else class="form-row">
          <el-form-item label="单价（积分）">
            <el-input-number v-model="form.price" :min="0" :max="10000" :step="0.01" :precision="4" controls-position="right" />
          </el-form-item>
          <el-form-item label="上游成本（积分）">
            <el-input-number v-model="form.cost" :min="0" :max="10000" :step="0.001" :precision="4" controls-position="right" />
          </el-form-item>
        </div>
        <div class="form-row">
          <el-form-item label="倍率">
            <el-input-number v-model="form.multiplier" :min="0.1" :max="10" :step="0.1" :precision="1" controls-position="right" />
          </el-form-item>
          <el-form-item label="计费单位" prop="unit">
            <el-input v-model="form.unit" placeholder="例如：1K tokens / 张 / 5s / 首" />
          </el-form-item>
        </div>
        <div class="margin-preview" :class="{ danger: formMargin < 0 }">
          <el-icon><Warning /></el-icon>
          <span>
            预计毛利率
            <b>{{ formMargin === null ? '—' : `${(formMargin * 100).toFixed(1)}%` }}</b>
            {{ formMargin === null ? '（请填写售价）' : formMargin < 0 ? '· 实际售价低于上游成本，将亏损' : '· 按输入/输出/缓存三档汇总折算' }}
          </span>
        </div>
        <el-form-item label="状态">
          <el-switch v-model="form.enabled" active-text="启用" inactive-text="停用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button round @click="editVisible = false">取消</el-button>
        <el-button round type="primary" @click="saveRule">保存规则</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="adjustVisible" title="批量调价" width="440px">
      <el-form label-position="top">
        <el-form-item label="调整范围">
          <el-select v-model="adjustScope">
            <el-option label="当前筛选结果" value="filtered" />
            <el-option label="全部规则" value="all" />
          </el-select>
        </el-form-item>
        <el-form-item label="调整幅度（%）">
          <el-input-number v-model="adjustPercent" :min="-90" :max="300" :step="5" controls-position="right" />
        </el-form-item>
      </el-form>
      <div class="adjust-tip">
        <el-icon><Warning /></el-icon>
        <span>将影响 {{ adjustTargets.length }} 条规则的单价，正数为上调、负数为下调。</span>
      </div>
      <template #footer>
        <el-button round @click="adjustVisible = false">取消</el-button>
        <el-button round type="primary" @click="applyAdjust">确认调价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import PageHeader from '@/components/PageHeader.vue'
import StatCard from '@/components/StatCard.vue'
import TablePager from '@/components/TablePager.vue'
import { loadBillingRules, saveBillingRules } from '@/mock/admin-extra'
import { downloadCsv } from '@/utils/csv'

const categories = ['对话', '图片', '视频', '音乐']

const rules = ref(loadBillingRules())
const keyword = ref('')
const category = ref('all')
const status = ref('all')
const editVisible = ref(false)
const currentId = ref('')
const page = ref(1)
const pageSize = 10

const formRef = ref()
const dialogMode = ref('edit')
const isCreate = computed(() => dialogMode.value === 'create')

const form = reactive({
  category: '对话',
  target: '',
  model: '',
  price: 0,
  priceIn: 0,
  priceOut: 0,
  priceCachedIn: 0,
  cost: 0,
  costIn: 0,
  costOut: 0,
  multiplier: 1,
  unit: '',
  enabled: true
})

// 对话类按 token 分档计价，其余类型按次/秒/首单档计价
const isTokenForm = computed(() => form.category === '对话')

/**
 * 对话类毛利率按输入 / 输出 / 缓存输入三档汇总：
 * 缓存成本未单独录入时，按输入成本与缓存价的比例折算，保证三档同一口径参与。
 */
function tokenTiers(source) {
  const priceIn = Number(source.priceIn) || 0
  const priceOut = Number(source.priceOut) || 0
  const priceCachedIn = Number(source.priceCachedIn) || 0
  const costIn = Number(source.costIn) || 0
  const costOut = Number(source.costOut) || 0
  const costCachedIn =
    Number(source.costCachedIn) || (priceIn > 0 ? costIn * (priceCachedIn / priceIn) : 0)
  return {
    price: priceIn + priceOut + priceCachedIn,
    cost: costIn + costOut + costCachedIn
  }
}

function marginOfValues(price, cost, multiplier) {
  const effective = price * (Number(multiplier) || 1)
  if (!(effective > 0)) return null
  return 1 - cost / effective
}

const formMargin = computed(() => {
  if (isTokenForm.value) {
    const tiers = tokenTiers(form)
    return marginOfValues(tiers.price, tiers.cost, form.multiplier)
  }
  return marginOfValues(Number(form.price) || 0, Number(form.cost) || 0, form.multiplier)
})

const formRules = {
  category: [{ required: true, message: '请选择计费类型', trigger: 'change' }],
  target: [{ required: true, message: '请输入应用名称', trigger: 'blur' }],
  model: [{ required: true, message: '请输入模型名称', trigger: 'blur' }],
  unit: [{ required: true, message: '请输入计费单位', trigger: 'blur' }]
}

const stats = computed(() => {
  const enabled = rules.value.filter(item => item.enabled).length
  const margins = rules.value
    .filter(item => item.enabled)
    .map(item => marginOf(item))
    .filter(value => value !== null)
  const avg = margins.length ? margins.reduce((sum, value) => sum + value, 0) / margins.length : 0
  const last = [...rules.value].sort((a, b) => (a.updatedAt < b.updatedAt ? 1 : -1))[0]
  return {
    enabled,
    avgMargin: `${(avg * 100).toFixed(1)}%`,
    risky: margins.filter(value => value < 0.15).length,
    lastUpdated: last ? last.updatedAt.slice(5) : '—'
  }
})

const filtered = computed(() => {
  const text = keyword.value.trim().toLowerCase()
  return rules.value.filter(item => {
    const matchKeyword = !text || `${item.target}${item.model}`.toLowerCase().includes(text)
    const matchCategory = category.value === 'all' || item.category === category.value
    const matchStatus =
      status.value === 'all' || (status.value === 'on' ? item.enabled : !item.enabled)
    return matchKeyword && matchCategory && matchStatus
  })
})

const paged = computed(() =>
  filtered.value.slice((page.value - 1) * pageSize, page.value * pageSize)
)

watch([keyword, category, status], () => {
  page.value = 1
})

function categoryType(value) {
  return { 对话: 'primary', 图片: 'success', 视频: 'warning', 音乐: 'danger' }[value] || 'info'
}

function isToken(row) {
  return row.category === '对话'
}

function priceText(row) {
  if (!isToken(row)) return Number(row.price || 0).toFixed(3)
  return `${Number(row.priceIn || 0).toFixed(3)} / ${Number(row.priceOut || 0).toFixed(3)}`
}

function costText(row) {
  if (!isToken(row)) return Number(row.cost || 0).toFixed(3)
  return `${Number(row.costIn || 0).toFixed(3)} / ${Number(row.costOut || 0).toFixed(3)}`
}

function effectiveText(row) {
  const mult = Number(row.multiplier) || 1
  if (!isToken(row)) return (Number(row.price || 0) * mult).toFixed(3)
  return `${(Number(row.priceIn || 0) * mult).toFixed(3)} / ${(Number(row.priceOut || 0) * mult).toFixed(3)}`
}

function marginOf(row) {
  if (isToken(row)) {
    const tiers = tokenTiers(row)
    return marginOfValues(tiers.price, tiers.cost, row.multiplier)
  }
  return marginOfValues(Number(row.price) || 0, Number(row.cost) || 0, row.multiplier)
}

function marginText(row) {
  const value = marginOf(row)
  return value === null ? '—' : `${(value * 100).toFixed(1)}%`
}

function marginClass(row) {
  const value = marginOf(row)
  if (value === null) return ''
  if (value < 0) return 'loss'
  if (value < 0.15) return 'warn'
  return 'ok'
}

function resetFilters() {
  keyword.value = ''
  category.value = 'all'
  status.value = 'all'
}

function toggleRule(row) {
  saveBillingRules(rules.value)
  ElMessage.success(row.enabled ? '规则已启用' : '规则已停用')
}

function openCreate() {
  dialogMode.value = 'create'
  currentId.value = ''
  Object.assign(form, {
    category: '对话',
    target: '',
    model: '',
    price: 0,
    priceIn: 0,
    priceOut: 0,
    priceCachedIn: 0,
    cost: 0,
    costIn: 0,
    costOut: 0,
    multiplier: 1,
    unit: '',
    enabled: true
  })
  editVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

function openEdit(row) {
  dialogMode.value = 'edit'
  currentId.value = row.id
  Object.assign(form, {
    category: row.category,
    target: row.target,
    model: row.model,
    price: row.price,
    priceIn: row.priceIn,
    priceOut: row.priceOut,
    priceCachedIn: row.priceCachedIn,
    cost: row.cost,
    costIn: row.costIn,
    costOut: row.costOut,
    multiplier: row.multiplier,
    unit: row.unit,
    enabled: row.enabled
  })
  editVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

async function saveRule() {
  try {
    await formRef.value.validate()
  } catch (error) {
    return
  }

  if (formMargin.value !== null && formMargin.value < 0) {
    try {
      await ElMessageBox.confirm(
        `当前实际售价低于上游成本，保存后每笔调用都会亏损（毛利率 ${(formMargin.value * 100).toFixed(1)}%），仍要保存吗？`,
        '存在亏损风险',
        { confirmButtonText: '仍然保存', cancelButtonText: '返回修改', type: 'warning' }
      )
    } catch (error) {
      return
    }
  }

  const now = dayjs().format('YYYY-MM-DD HH:mm')
  const tokenPricing = isTokenForm.value
  const pricing = {
    price: tokenPricing ? 0 : Number(form.price),
    priceIn: tokenPricing ? Number(form.priceIn) : 0,
    priceOut: tokenPricing ? Number(form.priceOut) : 0,
    priceCachedIn: tokenPricing ? Number(form.priceCachedIn) : 0,
    cost: tokenPricing ? 0 : Number(form.cost),
    costIn: tokenPricing ? Number(form.costIn) : 0,
    costOut: tokenPricing ? Number(form.costOut) : 0,
    multiplier: Number(form.multiplier),
    unit: form.unit.trim(),
    enabled: form.enabled
  }

  if (isCreate.value) {
    const target = form.target.trim()
    const model = form.model.trim()
    const duplicated = rules.value.some(item => item.target === target && item.model === model)
    if (duplicated) {
      ElMessage.warning('该应用与模型的计费规则已存在，请直接编辑已有规则')
      return
    }
    rules.value.unshift({
      id: `br-${Date.now()}`,
      category: form.category,
      target,
      model,
      ...pricing,
      updatedAt: now
    })
    saveBillingRules(rules.value)
    editVisible.value = false
    ElMessage.success('计费规则已新增')
    return
  }

  const current = rules.value.find(item => item.id === currentId.value)
  if (!current) return
  Object.assign(current, {
    ...pricing,
    updatedAt: now
  })
  saveBillingRules(rules.value)
  editVisible.value = false
  ElMessage.success('计费规则已保存')
}

async function removeRule(row) {
  try {
    await ElMessageBox.confirm(
      `删除后「${row.target} · ${row.model}」将无法再用于新的调用计费，历史扣费不受影响，确定删除吗？`,
      '删除计费规则',
      { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' }
    )
  } catch (error) {
    return
  }
  rules.value = rules.value.filter(item => item.id !== row.id)
  saveBillingRules(rules.value)
  ElMessage.success('计费规则已删除')
}

const adjustVisible = ref(false)
const adjustScope = ref('filtered')
const adjustPercent = ref(10)

const adjustTargets = computed(() => (adjustScope.value === 'all' ? rules.value : filtered.value))

function openAdjust() {
  adjustVisible.value = true
}

function applyAdjust() {
  const targets = adjustTargets.value
  if (!targets.length) {
    ElMessage.warning('当前没有可调整的规则')
    return
  }
  const ratio = 1 + Number(adjustPercent.value) / 100
  targets.forEach(item => {
    item.price = Number((Number(item.price) * ratio).toFixed(4))
    item.priceIn = Number((Number(item.priceIn) * ratio).toFixed(4))
    item.priceOut = Number((Number(item.priceOut) * ratio).toFixed(4))
    item.priceCachedIn = Number((Number(item.priceCachedIn) * ratio).toFixed(4))
    // 成本保持不变，调价直接体现为毛利率变化
    item.updatedAt = dayjs().format('YYYY-MM-DD HH:mm')
  })
  saveBillingRules(rules.value)
  adjustVisible.value = false
  ElMessage.success(`已调整 ${targets.length} 条规则`)
}

function exportCsv() {
  downloadCsv(
    `计费规则-${dayjs().format('YYYYMMDD')}.csv`,
    ['类型', '应用', '模型', '计费单位', '输入单价', '输出单价', '缓存输入价', '单次单价', '输入成本', '输出成本', '单次成本', '倍率', '实际单价', '毛利率', '状态', '更新时间'],
    filtered.value.map(item => {
      const token = isToken(item)
      const margin = marginOf(item)
      return [
        item.category,
        item.target,
        item.model,
        item.unit,
        token ? item.priceIn : '—',
        token ? item.priceOut : '—',
        token ? item.priceCachedIn : '—',
        token ? '—' : item.price,
        token ? item.costIn : '—',
        token ? item.costOut : '—',
        token ? '—' : item.cost,
        item.multiplier,
        effectiveText(item),
        margin === null ? '—' : `${(margin * 100).toFixed(1)}%`,
        item.enabled ? '启用' : '停用',
        item.updatedAt
      ]
    })
  )
  ElMessage.success(`已导出 ${filtered.value.length} 条规则`)
}
</script>

<style scoped>
.model {
  color: #9ecbff;
  font-size: 11.5px;
}

.accent {
  color: #7cb2ff;
}

.cost {
  color: #f59e0b;
}

.header-tip {
  border-bottom: 1px dashed currentColor;
  cursor: help;
}

.margin.ok {
  color: #34d399;
}

.margin.warn {
  color: #fbbf24;
}

.margin.loss {
  color: #f87171;
  font-weight: 600;
}

.margin-preview {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin: 2px 0 14px;
  padding: 9px 12px;
  border-radius: 10px;
  border: 1px dashed rgba(52, 211, 153, 0.32);
  color: #34d399;
  font-size: 11.5px;
  line-height: 1.6;
}

.margin-preview b {
  color: inherit;
  font-size: 12.5px;
}

.margin-preview.danger {
  border-color: rgba(248, 113, 113, 0.4);
  color: #f87171;
}

.note-panel {
  padding: 16px 20px 18px;
}

.note-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  color: #7cb2ff;
}

.note-head h3 {
  margin: 0;
  color: var(--app-text);
  font-size: 13.5px;
  font-weight: 600;
}

.note-list {
  margin: 0;
  padding-left: 18px;
  display: flex;
  flex-direction: column;
  gap: 7px;
  color: var(--app-muted);
  font-size: 11.5px;
  line-height: 1.7;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

.adjust-tip {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px dashed rgba(251, 191, 36, 0.32);
  color: #fbbf24;
  font-size: 11.5px;
  line-height: 1.6;
}
</style>
