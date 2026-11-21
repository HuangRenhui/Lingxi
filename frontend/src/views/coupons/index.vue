<template>
  <div class="page">
    <PageHeader title="兑换与优惠券" description="输入兑换码领取积分，领取充值优惠券并在充值时抵扣。">
      <el-button round @click="router.push('/console/wallet')">
        <el-icon><Wallet /></el-icon>
        去充值
      </el-button>
    </PageHeader>

    <el-alert
      v-if="!isDev"
      class="demo-note"
      style="margin-bottom: 16px"
      type="info"
      :closable="false"
      show-icon
      title="当前展示的是演示优惠券数据"
      description="真实兑换码与优惠券接口尚未接入；兑换、领取等写操作在生产环境已禁用。"
    />

    <section class="stat-grid">
      <StatCard label="可用优惠券" :value="available.length" unit="张" icon="Present" hint="可用于充值抵扣" />
      <StatCard label="累计兑换积分" :value="redeemedCredits" icon="Coin" hint="通过兑换码获得" />
      <StatCard label="即将过期" :value="expiring.length" unit="张" icon="AlarmClock" hint="7 天内到期" />
      <StatCard label="可领取" :value="claimable.length" unit="张" icon="Ticket" hint="来自领券中心" />
    </section>

    <section class="redeem-grid">
      <article class="panel panel-block">
        <div class="panel-head">
          <div>
            <h3>兑换码</h3>
            <span>输入兑换码领取积分，积分实时到账</span>
          </div>
        </div>

        <div class="redeem-form">
          <el-input
            v-model="code"
            size="large"
            placeholder="请输入兑换码，例如 LX2026-WELCOME-50"
            clearable
            @keyup.enter="submitRedeem"
          />
          <el-button type="primary" size="large" round :loading="redeeming" @click="submitRedeem">兑换</el-button>
        </div>
        <p class="redeem-tip">每个兑换码仅可使用一次，兑换成功后可在钱包流水中查看。</p>

        <div class="panel-divider" />

        <div class="panel-head">
          <div>
            <h3>兑换记录</h3>
            <span>最近 {{ records.length }} 条</span>
          </div>
        </div>
        <ul class="record-list">
          <li v-for="item in records" :key="item.id">
            <span class="record-code mono">{{ item.code }}</span>
            <span class="record-amount">+{{ item.amount }} 积分</span>
            <span class="record-time">{{ item.redeemedAt }}</span>
          </li>
          <li v-if="!records.length" class="empty">暂无兑换记录</li>
        </ul>
      </article>

      <article class="panel panel-block">
        <div class="panel-head">
          <div>
            <h3>我的优惠券</h3>
            <span>共 {{ coupons.length }} 张，按状态筛选</span>
          </div>
          <div class="coupon-tabs">
            <button
              v-for="item in filters"
              :key="item.key"
              type="button"
              :class="{ active: filter === item.key }"
              @click="filter = item.key"
            >
              {{ item.label }}
            </button>
          </div>
        </div>

        <div class="coupon-list">
          <article
            v-for="item in filteredCoupons"
            :key="item.id"
            class="coupon-card"
            :class="classOf(item.status)"
          >
            <div class="coupon-value">
              <strong v-if="item.type === '满减'">¥{{ item.value }}</strong>
              <strong v-else>{{ (item.value * 10).toFixed(1) }}<small>折</small></strong>
              <span>{{ item.threshold ? `满 ¥${item.threshold} 可用` : '无门槛' }}</span>
            </div>
            <div class="coupon-main">
              <strong>{{ item.name }}</strong>
              <span>有效期 {{ item.validFrom }} ~ {{ item.validTo }}</span>
            </div>
            <div class="coupon-action">
              <el-button v-if="item.status === 'AVAILABLE'" link type="primary" @click="useCoupon(item)">
                去使用
              </el-button>
              <span v-else class="coupon-status">{{ statusLabel(item.status) }}</span>
            </div>
          </article>
          <div v-if="!filteredCoupons.length" class="empty">该分类下暂无优惠券</div>
        </div>
      </article>
    </section>

    <section class="panel panel-block claim-panel">
      <div class="panel-head">
        <div>
          <h3>领券中心</h3>
          <span>领取后在充值弹窗中选择使用</span>
        </div>
      </div>

      <div class="claim-grid">
        <article v-for="item in claimable" :key="item.id" class="claim-card">
          <div class="claim-value">
            <strong v-if="item.type === '满减'">¥{{ item.value }}</strong>
            <strong v-else>{{ (item.value * 10).toFixed(1) }}<small>折</small></strong>
          </div>
          <div class="claim-main">
            <strong>{{ item.name }}</strong>
            <span>{{ item.threshold ? `满 ¥${item.threshold} 可用` : '无门槛' }} · 至 {{ item.validTo }}</span>
          </div>
          <el-button round size="small" @click="claim(item)">领取</el-button>
        </article>
        <div v-if="!claimable.length" class="empty">暂无可领取的优惠券</div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import PageHeader from '@/components/PageHeader.vue'
import StatCard from '@/components/StatCard.vue'
import { loadCoupons } from '@/mock/admin-extra'
import { classOf } from '@/utils/text'
import {
  claimCoupon,
  loadRedeemRecords,
  loadUserCoupons,
  redeemCode
} from '@/mock/coupons'

const router = useRouter()
const isDev = import.meta.env.DEV
const coupons = ref(loadUserCoupons())
const adminCoupons = ref(loadCoupons())
const records = ref(loadRedeemRecords())
const code = ref('')
const redeeming = ref(false)
const filter = ref('all')

// 兑换请求的模拟延时：切页后清理，避免卸载后仍写入兑换记录
let redeemTimer = null
let unmounted = false

onBeforeUnmount(() => {
  unmounted = true
  if (redeemTimer) {
    window.clearTimeout(redeemTimer)
    redeemTimer = null
  }
})

const filters = [
  { key: 'all', label: '全部' },
  { key: 'AVAILABLE', label: '可用' },
  { key: 'USED', label: '已使用' },
  { key: 'EXPIRED', label: '已过期' }
]

const available = computed(() => coupons.value.filter(item => item.status === 'AVAILABLE'))
const expiring = computed(() =>
  available.value.filter(item => dayjs(item.validTo).diff(dayjs(), 'day') <= 7)
)
const redeemedCredits = computed(() => records.value.reduce((sum, item) => sum + Number(item.amount), 0))

const filteredCoupons = computed(() => {
  if (filter.value === 'all') return coupons.value
  return coupons.value.filter(item => item.status === filter.value)
})

const claimable = computed(() =>
  adminCoupons.value.filter(item => {
    if (!item.enabled) return false
    if (Number(item.claimed) >= Number(item.total)) return false
    if (dayjs(item.validTo).endOf('day').isBefore(dayjs())) return false
    const owned = coupons.value.some(
      mine => mine.couponId === item.id && (mine.status === 'AVAILABLE' || mine.status === 'USED')
    )
    return !owned
  })
)

function statusLabel(status) {
  return { AVAILABLE: '可使用', USED: '已使用', EXPIRED: '已过期' }[status] || status
}

function submitRedeem() {
  if (redeeming.value) return
  redeeming.value = true
  redeemTimer = window.setTimeout(() => {
    redeemTimer = null
    if (unmounted) return
    try {
      const result = redeemCode(code.value)
      records.value = loadRedeemRecords()
      code.value = ''
      ElMessage.success(`兑换成功，${result.amount} 积分已到账`)
    } catch (error) {
      ElMessage.error(error?.message || '兑换失败，请稍后重试')
    } finally {
      redeeming.value = false
    }
  }, 500)
}

function claim(item) {
  if (!import.meta.env.DEV) {
    ElMessage.info('优惠券领取暂未接入后端')
    return
  }
  try {
    claimCoupon(item.id)
    coupons.value = loadUserCoupons()
    adminCoupons.value = loadCoupons()
    ElMessage.success(`已领取「${item.name}」`)
  } catch (error) {
    ElMessage.error(error?.message || '领取失败')
  }
}

function useCoupon(item) {
  if (!import.meta.env.DEV) {
    ElMessage.info('优惠券使用暂未接入后端')
    return
  }
  router.push({ path: '/console/wallet', query: { recharge: '1', coupon: item.id } })
}
</script>

<style scoped>
.redeem-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1.25fr);
  gap: 16px;
}

.panel-block {
  padding: 18px 20px;
}

.redeem-form {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 10px;
}

.redeem-tip {
  margin-top: 10px;
  color: var(--app-muted);
  font-size: 11.5px;
  line-height: 1.6;
}

.record-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
  margin-top: 4px;
}

.record-list li {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 90px 106px;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid var(--app-border);
  font-size: 12px;
}

.record-list li:last-child {
  border-bottom: 0;
}

.record-code {
  color: #9ecbff;
  font-size: 11.5px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.record-amount {
  color: #4ade80;
  font-weight: 600;
}

.record-time {
  color: var(--app-muted);
  font-size: 11px;
  text-align: right;
}

.coupon-tabs {
  display: inline-flex;
  gap: 2px;
  padding: 2px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.04);
}

.coupon-tabs button {
  padding: 4px 10px;
  border: 0;
  border-radius: 6px;
  background: transparent;
  color: var(--app-muted);
  font-size: 11.5px;
  cursor: pointer;
  transition: all 0.16s ease;
}

.coupon-tabs button.active {
  color: #dbeafe;
  background: var(--app-primary-soft);
}

.coupon-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 6px;
}

.coupon-card {
  display: grid;
  grid-template-columns: 96px minmax(0, 1fr) auto;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 12px;
  border: 1px solid var(--app-border);
  background: rgba(255, 255, 255, 0.02);
}

.coupon-value {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  padding: 8px 6px;
  border-radius: 9px;
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.22), rgba(239, 68, 68, 0.06));
  color: #fca5a5;
}

.coupon-value strong {
  font-size: 19px;
  line-height: 1.1;
}

.coupon-value small {
  font-size: 11px;
  margin-left: 1px;
}

.coupon-value span {
  color: #f87171;
  font-size: 10px;
  white-space: nowrap;
}

.coupon-card.used .coupon-value,
.coupon-card.expired .coupon-value {
  background: rgba(255, 255, 255, 0.04);
  color: #6e7681;
}

.coupon-card.used .coupon-value span,
.coupon-card.expired .coupon-value span {
  color: #6e7681;
}

.coupon-card.used .coupon-main strong,
.coupon-card.expired .coupon-main strong {
  color: var(--app-muted);
}

.coupon-main {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.coupon-main strong {
  font-size: 13px;
}

.coupon-main span {
  color: var(--app-muted);
  font-size: 11px;
}

.coupon-status {
  color: var(--app-muted);
  font-size: 11.5px;
}

.claim-panel {
  margin-top: 16px;
}

.claim-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 12px;
  margin-top: 4px;
}

.claim-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 12px;
  border: 1px dashed var(--app-border-strong);
  background: rgba(255, 255, 255, 0.02);
}

.claim-value {
  flex-shrink: 0;
  color: #fbbf24;
  font-size: 19px;
  font-weight: 700;
  min-width: 58px;
}

.claim-value small {
  font-size: 11px;
  margin-left: 1px;
}

.claim-main {
  display: flex;
  flex-direction: column;
  gap: 3px;
  min-width: 0;
  flex: 1;
}

.claim-main strong {
  font-size: 12.5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.claim-main span {
  color: var(--app-muted);
  font-size: 11px;
}

.empty {
  padding: 22px 0;
  color: var(--app-muted);
  font-size: 12px;
  text-align: center;
}

@media (max-width: 1080px) {
  .redeem-grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
