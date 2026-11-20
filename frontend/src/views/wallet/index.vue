<template>
  <div class="page">
    <PageHeader title="钱包与充值" description="查看积分余额、充值记录与消费流水。演示环境不会产生真实支付。">
      <el-button type="primary" round @click="openRecharge">
        <el-icon><Wallet /></el-icon>
        充值
      </el-button>
      <el-button round @click="exportCsv">
        <el-icon><Download /></el-icon>
        导出流水
      </el-button>
    </PageHeader>

    <section class="stat-grid">
      <StatCard label="可用余额" :value="wallet.balance.toFixed(2)" unit="积分" icon="Coin" hint="可直接用于全部模型调用" />
      <StatCard label="冻结中" :value="wallet.frozen.toFixed(2)" unit="积分" icon="Lock" hint="运行中任务预扣，结束后结算" />
      <StatCard label="累计充值" :value="wallet.totalRecharge.toFixed(2)" unit="积分" icon="Wallet" hint="含活动赠送与兑换码" />
      <StatCard label="累计消耗" :value="wallet.totalSpent.toFixed(2)" unit="积分" icon="DataLine" hint="全部应用与任务的扣费合计" />
    </section>

    <section class="panel credit-overview">
      <div class="overview-main">
        <div class="overview-label">
          当前总可用积分
          <el-tooltip
            placement="top"
            content="总可用 = 购买积分 + 奖励积分；扣费时优先消耗最早到期的奖励积分，过期奖励不再参与抵扣"
          >
            <el-icon class="overview-info"><InfoFilled /></el-icon>
          </el-tooltip>
        </div>
        <div class="overview-value">{{ credit.total.toFixed(2) }}</div>
        <div class="overview-split">
          <span>购买积分 <strong>{{ credit.purchased.toFixed(2) }}</strong></span>
          <i class="split-dot" />
          <span>奖励积分 <strong>{{ credit.reward.toFixed(2) }}</strong></span>
        </div>
      </div>
      <div class="overview-side">
        <el-button round @click="router.push('/console/subscription')">升级权益</el-button>
        <div class="overview-redeem">
          有积分兑换码？
          <el-button link type="primary" @click="focusRedeem">兑换积分</el-button>
        </div>
      </div>
    </section>

    <section class="panel reward-panel">
      <div class="panel-head">
        <div>
          <h3>推荐奖励积分</h3>
          <span>按批次管理，先过期先用；过期批次不再参与抵扣</span>
        </div>
        <div class="reward-tabs">
          <button
            type="button"
            :class="{ active: rewardTab === 'available' }"
            @click="rewardTab = 'available'"
          >
            可用积分
          </button>
          <button
            type="button"
            :class="{ active: rewardTab === 'earned' }"
            @click="rewardTab = 'earned'"
          >
            获得积分
          </button>
        </div>
      </div>

      <ul class="reward-list">
        <li
          v-for="batch in rewardBatches"
          :key="batch.id"
          class="reward-card"
          :class="{ 'is-expired': batch.expired }"
        >
          <div class="reward-main">
            <div class="reward-name">
              <strong>{{ batch.name }}</strong>
              <span v-if="batch.expired" class="reward-tag muted">已过期</span>
              <span v-else-if="isExpiringSoon(batch)" class="reward-tag warn">即将过期</span>
            </div>
            <div class="reward-meta">
              <span>{{ batch.expiresAt ? `${batch.expiresAt} 到期` : '长期有效' }}</span>
              <i class="split-dot" />
              <span>共 {{ batch.entries.length }} 笔</span>
            </div>
          </div>
          <div class="reward-amount">
            <template v-if="rewardTab === 'available'">
              <strong>{{ batch.remaining.toFixed(2) }}</strong>
              <span class="reward-total">/ {{ batch.total.toFixed(2) }}</span>
            </template>
            <template v-else>
              <strong>{{ batch.total.toFixed(2) }}</strong>
              <span class="reward-total">累计获得</span>
            </template>
            <el-button link type="primary" @click="openRewardDetail(batch)">明细</el-button>
          </div>
        </li>
        <li v-if="!rewardBatches.length" class="reward-empty">
          暂无奖励积分，邀请好友或每日签到即可获得
        </li>
      </ul>

      <div class="reward-foot">
        <span>购买积分 {{ credit.purchased.toFixed(2) }}，不过期，可直接用于全部模型</span>
        <span v-if="credit.expiringSoon" class="reward-foot-warn">
          {{ credit.expiringSoon.name }} 将于 {{ credit.expiringSoon.expiresAt }} 过期，剩余
          {{ credit.expiringSoon.remaining.toFixed(2) }}
        </span>
      </div>
    </section>

    <section class="wallet-grid">
      <article class="panel panel-block recharge-panel">
        <div class="panel-head">
          <div>
            <h3>充值积分</h3>
            <span>1 元 = 1 积分，充值后立即到账</span>
          </div>
          <span class="bonus-badge">最高送 400 积分</span>
        </div>

        <div class="recharge-balance">
          <span class="balance-label">当前可用</span>
          <strong>{{ wallet.balance.toFixed(2) }}</strong>
          <span class="balance-unit">积分</span>
        </div>

        <ul class="recharge-tips">
          <li>500 / 1000 / 2000 积分档位分别赠送 50 / 150 / 400 积分。</li>
          <li>支持支付宝与微信支付，支付通道对接完成后跳转对应平台收银台。</li>
        </ul>

        <el-button type="primary" round class="recharge-cta" @click="openRecharge">
          <el-icon><Wallet /></el-icon>
          立即充值
        </el-button>
      </article>

      <article class="panel panel-block">
        <div class="panel-head">
          <div>
            <h3>兑换码</h3>
            <span>输入兑换码领取积分，示例：LX2026-WELCOME-50</span>
          </div>
          <el-button link type="primary" @click="router.push('/console/coupons')">兑换与优惠券</el-button>
        </div>
        <div class="redeem-form">
          <el-input v-model="redeemCode" placeholder="请输入兑换码" clearable @keyup.enter="redeem" />
          <el-button round :loading="redeeming" @click="redeem">兑换</el-button>
        </div>
        <div class="redeem-tip">
          <el-icon><InfoFilled /></el-icon>
          <span>每个兑换码仅可使用一次，兑换后积分实时到账并记入流水。</span>
        </div>

        <div class="panel-divider" />

        <div class="panel-head">
          <div>
            <h3>计费说明</h3>
            <span>按实际调用量扣除，失败自动退回</span>
          </div>
        </div>
        <ul class="rule-list">
          <li v-for="rule in rules" :key="rule">{{ rule }}</li>
        </ul>
      </article>
    </section>

    <el-dialog
      v-model="rechargeVisible"
      width="560px"
      align-center
      class="recharge-dialog"
      :show-close="!recharging"
      :close-on-click-modal="!recharging"
      :close-on-press-escape="!recharging"
    >
      <template #header>
        <div class="rc-head">
          <h3>充值积分</h3>
          <span>1 元 = 1 积分，充值后立即到账</span>
        </div>
      </template>

      <div class="rc-body">
        <div class="rc-amount-grid">
          <button
            v-for="item in amounts"
            :key="item.value"
            type="button"
            class="rc-amount-card"
            :class="{ active: Number(amount) === item.value }"
            @click="selectAmount(item.value)"
          >
            <strong>{{ item.value }}</strong>
            <span>积分</span>
            <small v-if="item.bonus">{{ item.bonus }}</small>
          </button>
        </div>

        <div class="rc-custom">
          <span class="rc-label">自定义金额</span>
          <el-input-number v-model="amount" :min="1" :max="20000" :step="10" controls-position="right" />
          <span class="rc-preview">实付 ¥{{ payable.toFixed(2) }}</span>
        </div>

        <div class="rc-coupon-row">
          <span class="rc-label">优惠券</span>
          <el-select v-model="chosenCouponId" placeholder="不使用优惠券" clearable class="rc-coupon-select">
            <el-option
              v-for="item in applicableCoupons"
              :key="item.id"
              :label="couponLabel(item)"
              :value="item.id"
            />
          </el-select>
          <span v-if="chosenCoupon" class="rc-preview rc-discount">
            已优惠 ¥{{ (Number(amount || 0) - payable).toFixed(2) }}
          </span>
        </div>

        <div class="rc-pay-row">
          <span class="rc-label">支付方式</span>
          <div class="rc-pay-switch">
            <button
              v-for="item in payMethods"
              :key="item"
              type="button"
              :class="{ active: payMethod === item }"
              @click="payMethod = item"
            >
              {{ item }}
            </button>
          </div>
          <button type="button" class="rc-pay-btn" :disabled="recharging" @click="recharge">
            {{ recharging ? '处理中…' : `立即充值 ¥${payable.toFixed(2)}` }}
          </button>
        </div>

        <p class="rc-note">
          <el-icon><InfoFilled /></el-icon>
          支付通道尚未接入，当前为演示到账；后续将对接支付宝、微信支付等平台。
        </p>
      </div>
    </el-dialog>

    <el-drawer v-model="detailVisible" :title="detailTitle" size="440px" class="reward-drawer">
      <div v-if="detailBatch" class="detail-head">
        <div class="detail-amount">
          <strong>{{ detailBatch.remaining.toFixed(2) }}</strong>
          <span>/ {{ detailBatch.total.toFixed(2) }} 剩余</span>
        </div>
        <span class="detail-expire">
          {{ detailBatch.expiresAt ? `${detailBatch.expiresAt} 到期` : '长期有效' }}
        </span>
      </div>
      <el-table :data="detailEntries" size="small">
        <el-table-column prop="createdAt" label="时间" width="124" />
        <el-table-column label="类型" width="72">
          <template #default="{ row }">
            <el-tag size="small" effect="plain" :type="row.amount >= 0 ? 'success' : 'warning'">
              {{ row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="变动" width="88">
          <template #default="{ row }">
            <span class="mono" :class="row.amount >= 0 ? 'income' : 'expense'">
              {{ row.amount >= 0 ? '+' : '' }}{{ row.amount.toFixed(2) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="note" label="说明" min-width="120" show-overflow-tooltip />
        <template #empty>
          <div class="table-empty">该批次暂无明细</div>
        </template>
      </el-table>
      <div class="detail-note">
        <el-icon><InfoFilled /></el-icon>
        <span>奖励积分按批次管理，消费时优先消耗最早到期的批次。</span>
      </div>
    </el-drawer>

    <section class="panel table-panel">
      <div class="table-head">
        <h3>积分流水</h3>
        <div class="table-tools">
          <el-select v-model="type" size="small" class="type-select">
            <el-option label="全部类型" value="all" />
            <el-option label="充值" value="充值" />
            <el-option label="消费" value="消费" />
            <el-option label="奖励" value="奖励" />
            <el-option label="退款" value="退款" />
          </el-select>
          <span>共 {{ filteredLogs.length }} 条记录</span>
        </div>
      </div>
      <el-table :data="pagedLogs" size="small">
        <el-table-column prop="createdAt" label="时间" width="150" />
        <el-table-column label="类型" width="90">
          <template #default="{ row }">
            <el-tag :type="typeOf(row.type)" size="small" effect="plain">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="变动" width="110">
          <template #default="{ row }">
            <span class="mono" :class="row.amount >= 0 ? 'income' : 'expense'">
              {{ row.amount >= 0 ? '+' : '' }}{{ row.amount.toFixed(2) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="变动后余额" width="120">
          <template #default="{ row }">
            <span class="mono muted">{{ row.balance.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="note" label="备注" min-width="220" show-overflow-tooltip />
        <template #empty>
          <div class="table-empty">没有匹配的流水</div>
        </template>
      </el-table>
      <div class="pager">
        <el-pagination
          v-model:current-page="page"
          layout="prev, pager, next"
          :page-size="pageSize"
          :total="filteredLogs.length"
          background
        />
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import PageHeader from '@/components/PageHeader.vue'
import StatCard from '@/components/StatCard.vue'
import { appendOrder, appendWalletLog, loadWallet, loadWalletLogs, saveWallet } from '@/mock/account'
import {
  couponPayable,
  isCouponUsable,
  loadUserCoupons,
  markCouponUsed,
  redeemCode as submitRedeemCode
} from '@/mock/coupons'
import { creditSummary, loadBatchEntries, loadRewardBatches } from '@/mock/credits'
import { downloadCsv } from '@/utils/csv'

const route = useRoute()
const router = useRouter()
const wallet = ref(loadWallet())
const logs = ref(loadWalletLogs())
// 积分构成：购买积分 + 奖励积分；奖励按批次展示并可查明细
const credit = ref(creditSummary())
const rewardBatches = ref(loadRewardBatches())
const rewardTab = ref('available')
const detailVisible = ref(false)
const detailBatch = ref(null)
const detailEntries = ref([])

const detailTitle = computed(() => (detailBatch.value ? `${detailBatch.value.name} · 积分明细` : '积分明细'))
const amount = ref(100)
const payMethod = ref('支付宝')
const payMethods = ['支付宝', '微信支付']
const rechargeVisible = ref(false)
const recharging = ref(false)
const redeemCode = ref('')
const redeeming = ref(false)
const userCoupons = ref(loadUserCoupons())
const chosenCouponId = ref('')
const type = ref('all')
const page = ref(1)
const pageSize = 8

// 模拟支付的延时任务：组件卸载后必须清理，避免仍在后台写钱包数据
let rechargeTimer = null
let redeemTimer = null
let unmounted = false

const amounts = [
  { value: 50, bonus: '' },
  { value: 100, bonus: '' },
  { value: 300, bonus: '送 20 积分' },
  { value: 500, bonus: '送 50 积分' },
  { value: 1000, bonus: '送 150 积分' },
  { value: 2000, bonus: '送 400 积分' }
]

const rules = [
  '对话按 token 计费，图片按张、视频按秒、音乐按首计费。',
  '任务开始前预扣积分，失败或取消自动退回。',
  '充值余额用于全部应用，不支持提现。',
  '如需对账，可在本页导出积分流水。'
]

const filteredLogs = computed(() =>
  logs.value.filter(log => type.value === 'all' || log.type === type.value)
)

const pagedLogs = computed(() =>
  filteredLogs.value.slice((page.value - 1) * pageSize, page.value * pageSize)
)

const applicableCoupons = computed(() =>
  userCoupons.value.filter(item => isCouponUsable(item, amount.value))
)

const chosenCoupon = computed(
  () => userCoupons.value.find(item => item.id === chosenCouponId.value) || null
)

const payable = computed(() => couponPayable(chosenCoupon.value, amount.value))

function couponLabel(item) {
  const rule = item.type === '满减' ? `减 ¥${item.value}` : `${(item.value * 10).toFixed(1)} 折`
  const threshold = item.threshold ? `满 ¥${item.threshold} 可用` : '无门槛'
  return `${item.name} · ${rule}（${threshold}）`
}

watch(amount, () => {
  if (chosenCouponId.value && !applicableCoupons.value.some(item => item.id === chosenCouponId.value)) {
    chosenCouponId.value = ''
  }
})

watch(type, () => {
  page.value = 1
})

onBeforeUnmount(() => {
  unmounted = true
  if (rechargeTimer) {
    window.clearTimeout(rechargeTimer)
    rechargeTimer = null
  }
  if (redeemTimer) {
    window.clearTimeout(redeemTimer)
    redeemTimer = null
  }
})

function selectAmount(value) {
  amount.value = value
}

function openRecharge() {
  userCoupons.value = loadUserCoupons()
  if (chosenCouponId.value && !applicableCoupons.value.some(item => item.id === chosenCouponId.value)) {
    chosenCouponId.value = ''
  }
  rechargeVisible.value = true
}

onMounted(() => {
  userCoupons.value = loadUserCoupons()
  const couponId = String(route.query.coupon || '')
  if (couponId) {
    const target = userCoupons.value.find(item => item.id === couponId && item.status === 'AVAILABLE')
    if (target) {
      // 从优惠券页“去使用”进入时，把金额抬到券的使用门槛
      if (target.threshold && Number(amount.value) < Number(target.threshold)) {
        amount.value = Number(target.threshold)
      }
      chosenCouponId.value = target.id
    }
  }
  // 从订单、订阅、优惠券等页面带参数进入时直接弹出充值框
  if (route.query.recharge === '1' || couponId) {
    rechargeVisible.value = true
    router.replace({ path: route.path })
  }
  refreshCredits()
})

/** 重新读取积分账户：充值、兑换、退款后都必须刷新，否则拆分展示会滞后 */
function refreshCredits() {
  credit.value = creditSummary()
  rewardBatches.value = loadRewardBatches()
  wallet.value = loadWallet()
}

/** 30 天内到期视为「即将过期」，用于卡片角标与顶部提醒 */
function isExpiringSoon(batch) {
  if (!batch.expiresAt || batch.expired) return false
  return dayjs(batch.expiresAt).diff(dayjs(), 'day') <= 30
}

function openRewardDetail(batch) {
  detailBatch.value = batch
  detailEntries.value = loadBatchEntries(batch.id)
  detailVisible.value = true
}

/** 「有积分兑换码？兑换积分」：滚动到兑换卡并聚焦输入框 */
function focusRedeem() {
  const input = document.querySelector('.redeem-form input')
  if (!input) return
  input.scrollIntoView({ behavior: 'smooth', block: 'center' })
  input.focus()
}

function recharge() {
  if (!amount.value || amount.value < 1) {
    ElMessage.warning('请输入有效的充值金额')
    return
  }
  if (recharging.value) return
  recharging.value = true
  rechargeTimer = window.setTimeout(() => {
    rechargeTimer = null
    if (unmounted) return
    try {
      const value = Number(amount.value)
      const coupon = chosenCoupon.value
      const paid = payable.value
      const bonus = amounts.find(item => item.value === value)?.bonus
      const bonusValue = bonus ? Number(bonus.replace(/\D/g, '')) : 0
      const total = value + bonusValue

      // 充值进入购买积分池：先写积分账户，再回读钱包，避免页面快照覆盖期间发生的扣费
      const credited = rechargeToPurchased(total)
      const current = loadWallet()
      const baseRecharge = Number(current.totalRecharge) || 0
      current.totalRecharge = Number((baseRecharge + total).toFixed(2))
      saveWallet(current)
      wallet.value = loadWallet()

      appendWalletLog({
        id: `wl-${Date.now()}`,
        createdAt: dayjs().format('YYYY-MM-DD HH:mm'),
        type: '充值',
        amount: total,
        balance: credited.balance,
        note: `${payMethod.value}充值 · ¥${paid.toFixed(2)}${coupon ? ` · 优惠券 ${coupon.name}` : ''}${bonusValue ? ` · 含赠送 ${bonusValue} 积分` : ''}`
      })
      appendOrder({
        id: `LX${dayjs().format('YYYYMMDDHHmmss')}${Math.floor(Math.random() * 900 + 100)}`,
        amount: paid,
        description: `积分充值${coupon ? ` · ${coupon.name}` : ''}${bonusValue ? ` · 赠 ${bonusValue} 积分` : ''}`,
        status: 'paid',
        payMethod: payMethod.value,
        createdAt: dayjs().format('YYYY-MM-DD HH:mm')
      })
      if (coupon) {
        markCouponUsed(coupon.id)
        userCoupons.value = loadUserCoupons()
        chosenCouponId.value = ''
      }
      logs.value = loadWalletLogs()
      refreshCredits()
      rechargeVisible.value = false
      page.value = 1
      ElMessage.success(`充值成功，到账 ${total} 积分`)
    } catch (error) {
      ElMessage.error('充值失败，请稍后重试')
    } finally {
      recharging.value = false
    }
  }, 600)
}

function redeem() {
  if (redeeming.value) return
  redeeming.value = true
  redeemTimer = window.setTimeout(() => {
    redeemTimer = null
    if (unmounted) return
    try {
      const result = submitRedeemCode(redeemCode.value)
      appendOrder({
        id: `LX${dayjs().format('YYYYMMDDHHmmss')}${Math.floor(Math.random() * 900 + 100)}`,
        amount: 0,
        description: `兑换码兑换 · ${result.amount} 积分`,
        status: 'paid',
        payMethod: '兑换码',
        createdAt: dayjs().format('YYYY-MM-DD HH:mm')
      })
      wallet.value = loadWallet()
      logs.value = loadWalletLogs()
      refreshCredits()
      redeemCode.value = ''
      page.value = 1
      ElMessage.success(`兑换成功，获得 ${result.amount} 积分`)
    } catch (error) {
      ElMessage.error(error?.message || '兑换失败，请稍后重试')
    } finally {
      redeeming.value = false
    }
  }, 500)
}

function typeOf(value) {
  return { 充值: 'success', 消费: 'warning', 奖励: 'primary', 退款: 'info', 冻结: 'danger' }[value] || 'info'
}

function exportCsv() {
  downloadCsv(
    `积分流水-${dayjs().format('YYYYMMDD')}.csv`,
    ['时间', '类型', '变动', '变动后余额', '备注'],
    filteredLogs.value.map(log => [log.createdAt, log.type, log.amount, log.balance, log.note])
  )
  ElMessage.success(`已导出 ${filteredLogs.value.length} 条流水`)
}
</script>

<style scoped>
.wallet-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) minmax(0, 1fr);
  gap: 14px;
}

.panel-block {
  padding: 18px 20px 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.panel-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.panel-head h3 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
}

.panel-head span {
  color: var(--app-muted);
  font-size: 11.5px;
}

.recharge-panel {
  gap: 16px;
}

.bonus-badge {
  flex-shrink: 0;
  padding: 3px 9px;
  border-radius: 999px;
  border: 1px solid rgba(251, 191, 36, 0.28);
  background: rgba(251, 191, 36, 0.1);
  color: #fbbf24;
  font-size: 10.5px;
  white-space: nowrap;
}

.recharge-balance {
  display: flex;
  align-items: baseline;
  gap: 8px;
  padding: 16px 18px;
  border: 1px solid var(--app-border);
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(37, 99, 235, 0.16), rgba(15, 23, 42, 0.18));
}

.balance-label {
  color: var(--app-muted);
  font-size: 12px;
}

.recharge-balance strong {
  font-size: 28px;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.balance-unit {
  color: var(--app-muted);
  font-size: 12px;
}

.recharge-tips {
  margin: 0;
  padding-left: 16px;
  display: flex;
  flex-direction: column;
  gap: 7px;
  color: var(--app-muted);
  font-size: 11.5px;
  line-height: 1.7;
}

.recharge-cta {
  width: 100%;
  height: 40px;
  margin-top: auto;
}

.redeem-form {
  display: flex;
  gap: 10px;
}

.redeem-tip {
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

.panel-divider {
  height: 1px;
  background: var(--app-border);
}

.rule-list {
  margin: 0;
  padding-left: 16px;
  display: flex;
  flex-direction: column;
  gap: 7px;
  color: var(--app-muted);
  font-size: 11.5px;
  line-height: 1.65;
}

.table-panel {
  padding: 16px 20px 10px;
}

.table-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
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

.table-tools {
  display: flex;
  align-items: center;
  gap: 12px;
}

.type-select {
  width: 130px;
}

.income {
  color: #34d399;
}

.expense {
  color: #f87171;
}

.table-empty {
  padding: 32px 0;
  color: var(--app-muted);
  font-size: 12px;
}

.pager {
  display: flex;
  justify-content: flex-end;
  padding: 10px 0 2px;
}

@media (max-width: 1080px) {
  .wallet-grid {
    grid-template-columns: 1fr;
  }
}
/* ---------- 积分总览 ---------- */
.credit-overview {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 20px 24px;
  border: 1px solid var(--app-border);
  border-radius: 14px;
  background: linear-gradient(135deg, rgba(37, 99, 235, 0.18), rgba(15, 23, 42, 0.24));
}

.overview-label {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--app-muted);
  font-size: 12.5px;
}

.overview-info {
  font-size: 13px;
  cursor: help;
}

.overview-value {
  margin-top: 6px;
  font-size: 34px;
  font-weight: 700;
  letter-spacing: -0.03em;
  line-height: 1.1;
}

.overview-split {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 8px;
  color: var(--app-muted);
  font-size: 12px;
}

.overview-split strong {
  color: inherit;
  font-weight: 600;
}

.split-dot {
  width: 3px;
  height: 3px;
  border-radius: 50%;
  background: currentColor;
  opacity: 0.45;
}

.overview-side {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
}

.overview-redeem {
  color: var(--app-muted);
  font-size: 12px;
}

/* ---------- 奖励积分 ---------- */
.reward-panel {
  padding: 18px 20px 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.reward-tabs {
  display: inline-flex;
  padding: 3px;
  gap: 2px;
  border: 1px solid var(--app-border);
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.35);
}

.reward-tabs button {
  padding: 5px 14px;
  border: none;
  border-radius: 999px;
  background: transparent;
  color: var(--app-muted);
  font-size: 12px;
  cursor: pointer;
  transition: all 0.18s ease;
}

.reward-tabs button.active {
  background: rgba(37, 99, 235, 0.9);
  color: #fff;
}

.reward-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.reward-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 16px;
  border: 1px solid var(--app-border);
  border-radius: 12px;
  background: rgba(15, 23, 42, 0.28);
}

.reward-card.is-expired {
  opacity: 0.55;
}

.reward-name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13.5px;
}

.reward-tag {
  padding: 1px 7px;
  border-radius: 999px;
  font-size: 10.5px;
  line-height: 1.6;
}

.reward-tag.warn {
  border: 1px solid rgba(251, 191, 36, 0.32);
  background: rgba(251, 191, 36, 0.12);
  color: #fbbf24;
}

.reward-tag.muted {
  border: 1px solid var(--app-border);
  color: var(--app-muted);
}

.reward-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 5px;
  color: var(--app-muted);
  font-size: 11.5px;
}

.reward-amount {
  display: flex;
  align-items: baseline;
  gap: 6px;
  white-space: nowrap;
}

.reward-amount strong {
  font-size: 16px;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}

.reward-total {
  color: var(--app-muted);
  font-size: 11.5px;
}

.reward-empty {
  padding: 22px 0;
  text-align: center;
  color: var(--app-muted);
  font-size: 12.5px;
  border: 1px dashed var(--app-border);
  border-radius: 12px;
}

.reward-foot {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  justify-content: space-between;
  color: var(--app-muted);
  font-size: 11.5px;
}

.reward-foot-warn {
  color: #fbbf24;
}

/* ---------- 奖励明细抽屉 ---------- */
.detail-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  margin-bottom: 12px;
  border: 1px solid var(--app-border);
  border-radius: 10px;
  background: rgba(15, 23, 42, 0.28);
}

.detail-amount strong {
  font-size: 22px;
  font-weight: 700;
}

.detail-amount span {
  margin-left: 6px;
  color: var(--app-muted);
  font-size: 12px;
}

.detail-expire {
  color: var(--app-muted);
  font-size: 11.5px;
}

.detail-note {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 14px;
  color: var(--app-muted);
  font-size: 11.5px;
}
</style>

<style>
/* 充值弹窗（el-dialog 渲染在组件之外，使用全局样式） */
.recharge-dialog.el-dialog {
  padding: 0;
  border-radius: 18px;
  border: 1px solid var(--app-border);
  background: #12141a;
  box-shadow: 0 30px 80px rgba(0, 0, 0, 0.55);
}

.recharge-dialog .el-dialog__header {
  padding: 20px 24px 8px;
  margin: 0;
}

.recharge-dialog .el-dialog__body {
  padding: 6px 24px 22px;
}

.rc-head h3 {
  margin: 0;
  color: var(--app-text);
  font-size: 16px;
  font-weight: 700;
}

.rc-head span {
  display: block;
  margin-top: 4px;
  color: var(--app-muted);
  font-size: 11.5px;
}

.rc-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.rc-amount-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.rc-amount-card {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: 15px 8px 13px;
  border: 1px solid var(--app-border);
  border-radius: 12px;
  background: #171a21;
  color: var(--app-text);
  cursor: pointer;
  transition: all 0.16s ease;
}

.rc-amount-card:hover {
  border-color: var(--app-border-strong);
}

.rc-amount-card.active {
  border-color: rgba(96, 165, 250, 0.7);
  background: rgba(37, 99, 235, 0.18);
  box-shadow: inset 0 0 0 1px rgba(96, 165, 250, 0.35);
}

.rc-amount-card strong {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: -0.01em;
}

.rc-amount-card span {
  color: var(--app-muted);
  font-size: 11px;
}

.rc-amount-card small {
  position: absolute;
  top: 7px;
  right: 8px;
  color: #fbbf24;
  font-size: 9.5px;
}

.rc-custom {
  display: flex;
  align-items: center;
  gap: 12px;
  color: var(--app-muted);
  font-size: 12px;
}

.rc-custom .el-input-number {
  width: 150px;
}

.rc-coupon-row {
  display: flex;
  align-items: center;
  gap: 12px;
  color: var(--app-muted);
  font-size: 12px;
}

.rc-coupon-select {
  width: 270px;
}

.rc-discount {
  color: #4ade80;
}

.rc-label {
  color: var(--app-muted);
  font-size: 12px;
}

.rc-preview {
  margin-left: auto;
  color: var(--app-text);
  font-size: 13px;
  font-weight: 500;
}

.rc-pay-row {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
  padding-top: 16px;
  border-top: 1px solid var(--app-border);
}

.rc-pay-switch {
  display: inline-flex;
  gap: 2px;
  padding: 3px;
  border: 1px solid var(--app-border);
  border-radius: 10px;
  background: #0d0f14;
}

.rc-pay-switch button {
  height: 30px;
  padding: 0 16px;
  border: 0;
  border-radius: 8px;
  background: transparent;
  color: var(--app-muted);
  font-size: 12.5px;
  cursor: pointer;
  transition: all 0.16s ease;
}

.rc-pay-switch button:hover {
  color: var(--app-text);
}

.rc-pay-switch button.active {
  background: #2563eb;
  color: #ffffff;
}

.rc-pay-btn {
  margin-left: auto;
  height: 38px;
  padding: 0 22px;
  border: 0;
  border-radius: 999px;
  background: #2563eb;
  color: #ffffff;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.16s ease;
}

.rc-pay-btn:hover:not(:disabled) {
  background: #1d4ed8;
}

.rc-pay-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.rc-note {
  display: flex;
  align-items: flex-start;
  gap: 7px;
  margin: 0;
  color: var(--app-muted);
  font-size: 11.5px;
  line-height: 1.6;
}

.rc-note .el-icon {
  margin-top: 2px;
  flex-shrink: 0;
}

@media (max-width: 640px) {
  .rc-amount-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .rc-custom {
    flex-wrap: wrap;
  }

  .rc-pay-btn {
    width: 100%;
    margin-left: 0;
  }
}
</style>
