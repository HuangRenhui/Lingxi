<template>
  <div class="admin-page">
    <PageHeader title="兑换码与优惠券" description="批量生成积分兑换码，配置充值满减与订阅折扣券。" flush>
      <el-button v-if="tab === 'code'" round type="primary" @click="openGenerate">
        <el-icon><Plus /></el-icon>
        批量生成兑换码
      </el-button>
      <el-button v-else round type="primary" @click="openCoupon">
        <el-icon><Plus /></el-icon>
        新建优惠券
      </el-button>
    </PageHeader>

    <section class="stat-grid">
      <StatCard label="可用兑换码" :value="stats.unused" icon="Ticket" hint="未使用且未作废" />
      <StatCard label="已兑换" :value="stats.used" icon="Select" hint="已绑定账号并发放积分" />
      <StatCard label="累计发放积分" :value="stats.grantedCredits" icon="Coin" hint="含兑换码与活动赠送" />
      <StatCard label="优惠券领取" :value="stats.claimed" unit="张" icon="Present" :hint="`启用中 ${stats.enabledCoupons} 个券`" />
    </section>

    <div class="panel tab-panel">
      <TabStrip v-model="tab" :items="tabs" />
    </div>

    <template v-if="tab === 'code'">
      <section class="filters panel">
        <el-input v-model="codeKeyword" class="filter-search" placeholder="搜索兑换码或使用人" clearable>
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="codeStatus" class="filter-item">
          <el-option label="全部状态" value="all" />
          <el-option label="未使用" value="UNUSED" />
          <el-option label="已使用" value="USED" />
          <el-option label="已作废" value="VOID" />
        </el-select>
        <el-button round @click="resetCodeFilters">重置筛选</el-button>
        <span class="filter-action muted">共 {{ filteredCodes.length }} 个兑换码</span>
      </section>

      <section class="panel table-panel">
        <el-table v-loading="loading" :data="codePaged" size="small">
          <el-table-column label="兑换码" min-width="210">
            <template #default="{ row }">
              <span class="mono code">{{ row.code }}</span>
            </template>
          </el-table-column>
          <el-table-column label="面额" width="100">
            <template #default="{ row }">
              <span class="mono">{{ row.amount }} 积分</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="codeStatusOf(row.status).type" size="small" effect="plain">
                {{ codeStatusOf(row.status).label }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="expiresAt" label="有效期至" width="130" />
          <el-table-column label="创建时间" width="150">
            <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="使用人" width="120">
            <template #default="{ row }">
              <span :class="row.usedBy ? 'mono' : 'muted'">{{ row.usedBy || '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="130">
            <template #default="{ row }">
              <div class="actions">
                <el-button link size="small" @click="copyCode(row)">复制</el-button>
                <el-button
                  v-if="row.status === 'UNUSED'"
                  link
                  type="danger"
                  size="small"
                  @click="voidCode(row)"
                >
                  作废
                </el-button>
              </div>
            </template>
          </el-table-column>
          <template #empty>
            <div class="table-empty">没有匹配的兑换码</div>
          </template>
        </el-table>
        <TablePager v-model:page="codePage" :total="filteredCodes.length" />
      </section>
    </template>

    <template v-else>
      <section class="panel table-panel">
        <div class="table-head">
          <h3>优惠券列表</h3>
          <span>共 {{ coupons.length }} 个券，关闭后不再发放</span>
        </div>
        <el-table v-loading="loading" :data="couponPaged" size="small">
          <el-table-column prop="name" label="名称" min-width="200" />
          <el-table-column prop="type" label="类型" width="90">
            <template #default="{ row }">
              <el-tag :type="isFullReduction(row.type) ? 'warning' : 'primary'" size="small" effect="plain">
                {{ couponTypeLabel(row.type) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="面额" width="120">
            <template #default="{ row }">
              <span class="mono">{{ isFullReduction(row.type) ? `¥${row.value}` : `${Number((row.value * 10).toFixed(1))} 折` }}</span>
            </template>
          </el-table-column>
          <el-table-column label="使用门槛" width="120">
            <template #default="{ row }">
              <span class="mono">{{ row.threshold ? `满 ¥${row.threshold}` : '无门槛' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="有效期" min-width="190">
            <template #default="{ row }">
              <span class="muted">{{ row.validFrom }} ~ {{ row.validTo }}</span>
            </template>
          </el-table-column>
          <el-table-column label="已领 / 总量" width="130">
            <template #default="{ row }">
              <span class="mono">{{ row.claimed }} / {{ row.total }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-switch v-model="row.enabled" @change="toggleCoupon(row)" />
            </template>
          </el-table-column>
          <template #empty>
            <div class="table-empty">还没有优惠券</div>
          </template>
        </el-table>
        <TablePager v-model:page="couponPage" :total="coupons.length" />
      </section>
    </template>

    <el-dialog v-model="generateVisible" title="批量生成兑换码" width="480px">
      <el-form :model="genForm" label-position="top">
        <div class="form-row">
          <el-form-item label="单个面额（积分）">
            <el-input-number v-model="genForm.amount" :min="1" :max="10000" :step="10" controls-position="right" />
          </el-form-item>
          <el-form-item label="生成数量">
            <el-input-number v-model="genForm.count" :min="1" :max="200" controls-position="right" />
          </el-form-item>
        </div>
        <div class="form-row">
          <el-form-item label="有效期至">
            <el-date-picker v-model="genForm.expiresAt" type="date" placeholder="选择日期" />
          </el-form-item>
          <el-form-item label="前缀">
            <el-input v-model="genForm.prefix" placeholder="LX2026" maxlength="12" />
          </el-form-item>
        </div>
      </el-form>
      <div class="gen-tip">
        <el-icon><InfoFilled /></el-icon>
        <span>将生成 {{ genForm.count }} 个面额为 {{ genForm.amount }} 积分的兑换码，可在生成后复制分发。</span>
      </div>
      <template #footer>
        <el-button round @click="generateVisible = false">取消</el-button>
        <el-button round type="primary" :loading="generating" @click="generateCodes">确认生成</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="couponVisible" title="新建优惠券" width="500px">
      <el-form :model="couponForm" label-position="top">
        <el-form-item label="优惠券名称">
          <el-input v-model="couponForm.name" maxlength="24" placeholder="例如：双十一充值满减" />
        </el-form-item>
        <div class="form-row">
          <el-form-item label="类型">
            <el-select v-model="couponForm.type">
              <el-option label="满减" value="FULL_REDUCTION" />
              <el-option label="折扣" value="DISCOUNT" />
            </el-select>
          </el-form-item>
          <el-form-item :label="isFullReduction(couponForm.type) ? '减免金额（元）' : '折扣（0-1）'">
            <el-input-number
              v-model="couponForm.value"
              :min="isFullReduction(couponForm.type) ? 1 : 0.1"
              :max="isFullReduction(couponForm.type) ? 1000 : 1"
              :step="isFullReduction(couponForm.type) ? 1 : 0.05"
              :precision="isFullReduction(couponForm.type) ? 0 : 2"
              controls-position="right"
            />
          </el-form-item>
        </div>
        <div class="form-row">
          <el-form-item label="使用门槛（元，0 为无门槛）">
            <el-input-number v-model="couponForm.threshold" :min="0" :max="10000" :step="10" controls-position="right" />
          </el-form-item>
          <el-form-item label="发放总量">
            <el-input-number v-model="couponForm.total" :min="1" :max="100000" :step="50" controls-position="right" />
          </el-form-item>
        </div>
        <el-form-item label="有效期">
          <el-date-picker
            v-model="couponForm.range"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button round @click="couponVisible = false">取消</el-button>
        <el-button round type="primary" :loading="creating" @click="createCoupon">确认创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import PageHeader from '@/components/PageHeader.vue'
import StatCard from '@/components/StatCard.vue'
import TabStrip from '@/components/TabStrip.vue'
import TablePager from '@/components/TablePager.vue'
import { loadCoupons, loadRedeemCodes, saveCoupons, saveRedeemCodes } from '@/mock/admin-extra'
import { createAdminCoupon, listAdminCoupons, updateAdminCoupon } from '@/api/adminCoupon'
import { fetchAllPages } from '@/utils/pagination'
import {
  batchCreateAdminRedeemCodes,
  listAdminRedeemCodes,
  updateAdminRedeemCode
} from '@/api/adminRedeemCode'

const tabs = [
  { key: 'code', label: '兑换码' },
  { key: 'coupon', label: '优惠券' }
]

const tab = ref('code')
const codes = ref([])
const coupons = ref([])
const demoMode = ref(false)
const loading = ref(false)
const generating = ref(false)
const creating = ref(false)
const codeKeyword = ref('')
const codeStatus = ref('all')
const generateVisible = ref(false)
const couponVisible = ref(false)
const codePage = ref(1)
const couponPage = ref(1)
const pageSize = 10

const genForm = reactive({
  amount: 100,
  count: 10,
  expiresAt: dayjs().add(90, 'day').toDate(),
  prefix: 'LX2026'
})

const couponForm = reactive({
  name: '',
  type: 'FULL_REDUCTION',
  value: 10,
  threshold: 50,
  total: 200,
  range: [dayjs().toDate(), dayjs().add(30, 'day').toDate()]
})

async function loadCodeList() {
  try {
    // 批量生成可能一次超过 100 条，逐页取完，避免超出的兑换码不可见
    const page = await fetchAllPages(params => listAdminRedeemCodes(params))
    if (page) {
      codes.value = page.list
      demoMode.value = false
      if (page.truncated) {
        ElMessage.warning('兑换码超过 1000 条，仅加载前 1000 条')
      }
    } else {
      codes.value = []
      demoMode.value = false
      if (import.meta.env.DEV) {
        codes.value = loadRedeemCodes()
        demoMode.value = true
      }
    }
  } catch (error) {
    codes.value = []
    demoMode.value = false
    if (import.meta.env.DEV) {
      codes.value = loadRedeemCodes()
      demoMode.value = true
    }
  }
}

async function loadCouponList() {
  // el-switch 只接受布尔值：后端返回 1/0 时必须归一化，
  // 否则 Element Plus 会把非 true/false 的 modelValue 强制改成 false 并触发 change，导致打开页面即批量停用优惠券
  const normalize = list => list.map(item => ({ ...item, enabled: item.enabled === 1 || item.enabled === true }))
  try {
    const page = await fetchAllPages(params => listAdminCoupons(params))
    if (page) {
      coupons.value = normalize(page.list)
      demoMode.value = false
      if (page.truncated) {
        ElMessage.warning('优惠券超过 1000 条，仅加载前 1000 条')
      }
    } else {
      coupons.value = []
      demoMode.value = false
      if (import.meta.env.DEV) {
        coupons.value = normalize(loadCoupons())
        demoMode.value = true
      }
    }
  } catch (error) {
    coupons.value = []
    demoMode.value = false
    if (import.meta.env.DEV) {
      coupons.value = normalize(loadCoupons())
      demoMode.value = true
    }
  }
}

async function loadAll() {
  loading.value = true
  try {
    await Promise.all([loadCodeList(), loadCouponList()])
  } finally {
    loading.value = false
  }
}

onMounted(loadAll)

function formatTime(value) {
  return value ? dayjs(value).format('YYYY-MM-DD HH:mm') : '—'
}

function isFullReduction(type) {
  return type === 'FULL_REDUCTION' || type === '满减'
}

function couponTypeLabel(type) {
  return isFullReduction(type) ? '满减' : '折扣'
}

const stats = computed(() => ({
  unused: codes.value.filter(item => item.status === 'UNUSED').length,
  used: codes.value.filter(item => item.status === 'USED').length,
  grantedCredits: codes.value
    .filter(item => item.status === 'USED')
    .reduce((sum, item) => sum + item.amount, 0),
  claimed: coupons.value.reduce((sum, item) => sum + item.claimed, 0),
  enabledCoupons: coupons.value.filter(item => item.enabled).length
}))

const filteredCodes = computed(() => {
  const text = codeKeyword.value.trim().toLowerCase()
  return codes.value.filter(item => {
    const matchKeyword =
      !text || String(item.code || '').toLowerCase().includes(text) || String(item.usedBy ?? '').toLowerCase().includes(text)
    const matchStatus = codeStatus.value === 'all' || item.status === codeStatus.value
    return matchKeyword && matchStatus
  })
})

const codePaged = computed(() =>
  filteredCodes.value.slice((codePage.value - 1) * pageSize, codePage.value * pageSize)
)

const couponPaged = computed(() =>
  coupons.value.slice((couponPage.value - 1) * pageSize, couponPage.value * pageSize)
)

watch([codeKeyword, codeStatus], () => {
  codePage.value = 1
})

function codeStatusOf(status) {
  return {
    UNUSED: { label: '未使用', type: 'success' },
    USED: { label: '已使用', type: 'info' },
    VOID: { label: '已作废', type: 'danger' }
  }[status] || { label: status, type: 'info' }
}

function resetCodeFilters() {
  codeKeyword.value = ''
  codeStatus.value = 'all'
}

function openGenerate() {
  generateVisible.value = true
}

async function generateCodes() {
  if (generating.value) return
  const { amount, count, prefix } = genForm
  if (!amount || !count) {
    ElMessage.warning('请填写面额与数量')
    return
  }
  // 有效期可被清空，必须校验非空且晚于今天，避免生成永久/已过期的兑换码
  const expires = genForm.expiresAt ? dayjs(genForm.expiresAt) : null
  if (!expires || !expires.isValid()) {
    ElMessage.warning('请选择兑换码有效期')
    return
  }
  if (!expires.startOf('day').isAfter(dayjs().startOf('day'))) {
    ElMessage.warning('有效期必须晚于今天')
    return
  }
  const expiresAt = expires.format('YYYY-MM-DD')

  if (demoMode.value) {
    const created = Array.from({ length: count }, (_, index) => {
      const token = Math.random().toString(36).slice(2, 7).toUpperCase()
      return {
        code: `${prefix || 'LX'}-${token}-${amount}-${index + 1}`,
        amount,
        status: 'UNUSED',
        expiresAt,
        createdAt: dayjs().format('YYYY-MM-DD HH:mm'),
        usedBy: ''
      }
    })
    codes.value = [...created, ...codes.value]
    saveRedeemCodes(codes.value)
    generateVisible.value = false
    ElMessage.success(`已生成 ${count} 个兑换码`)
    return
  }

  generating.value = true
  try {
    await batchCreateAdminRedeemCodes({ amount, count, expiresAt, prefix })
    await loadCodeList()
    generateVisible.value = false
    ElMessage.success(`已生成 ${count} 个兑换码`)
  } catch (error) {
    // 请求层已统一提示错误
  } finally {
    generating.value = false
  }
}

function copyCode(row) {
  if (!navigator.clipboard) {
    ElMessage.warning('当前浏览器不支持自动复制，请手动选择')
    return
  }
  navigator.clipboard
    .writeText(row.code)
    .then(() => ElMessage.success('兑换码已复制'))
    .catch(() => ElMessage.warning('复制失败，请手动选择'))
}

async function voidCode(row) {
  if (demoMode.value) {
    row.status = 'VOID'
    saveRedeemCodes(codes.value)
    ElMessage.success('兑换码已作废')
    return
  }
  try {
    await updateAdminRedeemCode(row.id, { status: 'VOID' })
    await loadCodeList()
    ElMessage.success('兑换码已作废')
  } catch (error) {
    // 请求层已统一提示错误
  }
}

async function toggleCoupon(row) {
  const enabled = row.enabled ? 1 : 0
  if (demoMode.value) {
    saveCoupons(coupons.value)
    ElMessage.success(row.enabled ? `「${row.name}」已启用` : `「${row.name}」已停用`)
    return
  }
  try {
    await updateAdminCoupon(row.id, { enabled })
    ElMessage.success(row.enabled ? `「${row.name}」已启用` : `「${row.name}」已停用`)
  } catch (error) {
    // 失败时回滚开关状态，避免界面与后端不一致
    row.enabled = enabled !== 1
  }
}

function openCoupon() {
  Object.assign(couponForm, {
    name: '',
    type: 'FULL_REDUCTION',
    value: 10,
    threshold: 50,
    total: 200,
    range: [dayjs().toDate(), dayjs().add(30, 'day').toDate()]
  })
  couponVisible.value = true
}

async function createCoupon() {
  if (creating.value) return
  if (!couponForm.name.trim()) {
    ElMessage.warning('请填写优惠券名称')
    return
  }
  if (!couponForm.range?.length) {
    ElMessage.warning('请选择有效期')
    return
  }
  const payload = {
    name: couponForm.name.trim(),
    type: couponForm.type,
    value: couponForm.value,
    threshold: couponForm.threshold,
    total: couponForm.total,
    validFrom: dayjs(couponForm.range[0]).format('YYYY-MM-DD'),
    validTo: dayjs(couponForm.range[1]).format('YYYY-MM-DD')
  }

  if (demoMode.value) {
    coupons.value = [{ id: `cp-${Date.now()}`, ...payload, claimed: 0, enabled: true }, ...coupons.value]
    saveCoupons(coupons.value)
    couponVisible.value = false
    ElMessage.success('优惠券已创建')
    return
  }

  creating.value = true
  try {
    await createAdminCoupon(payload)
    await loadCouponList()
    couponVisible.value = false
    ElMessage.success('优惠券已创建')
  } catch (error) {
    // 请求层已统一提示错误
  } finally {
    creating.value = false
  }
}
</script>

<style scoped>
.tab-panel {
  padding: 6px 20px 0;
}

.code {
  color: #9ecbff;
  font-size: 11.5px;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

.gen-tip {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px dashed var(--app-border-strong);
  color: var(--app-muted);
  font-size: 11.5px;
  line-height: 1.6;
}
</style>
