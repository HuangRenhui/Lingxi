<template>
  <div class="page">
    <PageHeader title="订阅套餐" description="查看当前套餐、积分额度与续费状态，随时切换方案。">
      <el-button round @click="router.push('/console/wallet?recharge=1')">
        <el-icon><Wallet /></el-icon>
        充值积分
      </el-button>
    </PageHeader>

    <section class="current-grid">
      <article class="panel current-card">
        <div class="current-top">
          <div>
            <span class="eyebrow">当前套餐</span>
            <h2>{{ currentPlan.name }}</h2>
            <p>{{ cycleLabel(currentPlan.cycle) }} · {{ currentPlan.expiresAt }} 到期</p>
          </div>
          <el-tag :type="daysLeft > 7 ? 'success' : 'warning'" effect="plain" round>
            剩余 {{ daysLeft }} 天
          </el-tag>
        </div>

        <div class="credit-block">
          <div class="credit-head">
            <span>本月积分额度</span>
            <strong class="mono">{{ subscription.creditsUsed }} / {{ subscription.creditsPerMonth }}</strong>
          </div>
          <el-progress
            :percentage="creditPercent"
            :stroke-width="10"
            :show-text="false"
            :color="creditPercent > 85 ? '#f87171' : '#3b82f6'"
          />
          <span class="credit-hint">已使用 {{ creditPercent }}%，剩余 {{ creditsLeft }} 积分</span>
        </div>

        <div class="current-foot">
          <div class="renew">
            <span>到期自动续费</span>
            <el-switch v-model="subscription.autoRenew" @change="toggleAutoRenew" />
          </div>
          <el-button round @click="renew">立即续费</el-button>
        </div>
      </article>

      <article class="panel perks-card">
        <h3>套餐权益</h3>
        <ul class="perk-list">
          <li v-for="item in currentPlan.features" :key="item">
            <el-icon><CircleCheckFilled /></el-icon>
            {{ item }}
          </li>
        </ul>
        <div class="perk-note">
          <el-icon><InfoFilled /></el-icon>
          <span>切换套餐后，剩余积分按新套餐额度重新计算，已消耗部分不退还。</span>
        </div>
      </article>
    </section>

    <section class="plan-section">
      <div class="plan-head">
        <div>
          <h3>选择套餐</h3>
          <span>年付可享 2 个月免费，切换立即生效</span>
        </div>
        <el-radio-group v-model="cycle" size="small">
          <el-radio-button label="monthly">月付</el-radio-button>
          <el-radio-button label="yearly">年付</el-radio-button>
        </el-radio-group>
      </div>

      <div class="plan-grid">
        <article
          v-for="plan in plans"
          :key="plan.key"
          class="plan-card panel"
          :class="{ featured: plan.tag, current: plan.key === subscription.planKey }"
        >
          <span v-if="plan.tag" class="plan-tag">{{ plan.tag }}</span>
          <div class="plan-title">
            <h3>{{ plan.name }}</h3>
            <span v-if="plan.key === subscription.planKey" class="current-badge">使用中</span>
          </div>
          <div class="plan-price">
            <!-- 价格单位是平台积分，不是法币：订阅直接从积分余额扣减 -->
            <strong>{{ priceOf(plan) }} 积分</strong>
            <span>/ {{ cycle === 'monthly' ? '月' : '年' }}</span>
          </div>
          <p class="plan-credits">每月 {{ plan.credits }} 积分</p>
          <ul class="plan-features">
            <li v-for="item in plan.features" :key="item">
              <el-icon><Check /></el-icon>
              {{ item }}
            </li>
          </ul>
          <el-button
            class="plan-btn"
            round
            :type="plan.key === subscription.planKey ? 'default' : 'primary'"
            :disabled="plan.key === subscription.planKey"
            @click="choosePlan(plan)"
          >
            {{ plan.key === subscription.planKey ? '当前套餐' : plan.key === 'free' ? '切换到免费版' : '立即订阅' }}
          </el-button>
        </article>
      </div>
    </section>

    <section class="panel table-panel">
      <div class="table-head">
        <h3>套餐变更记录</h3>
        <span>共 {{ history.length }} 条</span>
      </div>
      <el-table :data="paged" size="small">
        <el-table-column prop="createdAt" label="时间" width="150" />
        <el-table-column prop="planName" label="套餐" width="120" />
        <el-table-column prop="cycle" label="周期" width="90" />
        <el-table-column label="金额" width="110">
          <template #default="{ row }">
            <span class="mono">{{ row.amount ? `${row.amount.toFixed(2)} 积分` : '免费' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="operator" label="操作人" width="110" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag type="success" size="small" effect="plain">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <template #empty>
          <div class="table-empty">暂无变更记录</div>
        </template>
      </el-table>
      <TablePager v-model:page="page" :total="history.length" />
    </section>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import PageHeader from '@/components/PageHeader.vue'
import TablePager from '@/components/TablePager.vue'
import {
  appendOrder,
  appendPlanHistory,
  loadPlanHistory,
  loadSubscription,
  saveSubscription
} from '@/mock/account'
import { chargeCredits, loadPlans, refundCredits } from '@/mock/platform'

const router = useRouter()
const subscription = ref(loadSubscription())
const history = ref(loadPlanHistory())
const plans = ref(loadPlans())
const cycle = ref(subscription.value.cycle || 'monthly')
const page = ref(1)
const pageSize = 10

const paged = computed(() =>
  history.value.slice((page.value - 1) * pageSize, page.value * pageSize)
)

const currentPlan = computed(
  () => plans.value.find(plan => plan.key === subscription.value.planKey) || plans.value[0]
)
const daysLeft = computed(() => Math.max(0, dayjs(subscription.value.expiresAt).diff(dayjs(), 'day')))
const creditPercent = computed(() =>
  Math.min(100, Math.round((subscription.value.creditsUsed / (subscription.value.creditsPerMonth || 1)) * 100))
)
const creditsLeft = computed(() =>
  Math.max(0, subscription.value.creditsPerMonth - subscription.value.creditsUsed)
)

function cycleLabel(value) {
  return value === 'yearly' ? '年付' : '月付'
}

function priceOf(plan) {
  return cycle.value === 'yearly' ? plan.yearly : plan.monthly
}

function persist() {
  saveSubscription(subscription.value)
}

/**
 * 扣减订阅费用。余额不足时弹确认框引导充值（与后端 8802 的业务语义一致）。
 *
 * @returns 扣费成功返回扣费结果对象（免费套餐返回 true），失败返回 false
 */
async function payForSubscription(price, purpose) {
  if (!price || price <= 0) return true

  const charge = chargeCredits({
    amount: price,
    service: '订阅',
    operation: 'subscription/subscribe',
    model: purpose
  })
  if (charge.ok) return charge

  const balance = Number(charge.balance) || 0
  try {
    await ElMessageBox.confirm(
      `当前积分余额 ${balance.toFixed(2)}，本次需要 ${price.toFixed(2)}，还差 ${(price - balance).toFixed(2)}。是否前往充值？`,
      '积分余额不足',
      { confirmButtonText: '去充值', cancelButtonText: '稍后再说', type: 'warning' }
    )
    router.push('/console/wallet?recharge=1')
  } catch (error) {
    // 用户选择稍后再说：保持当前套餐不变
  }
  return false
}

function toggleAutoRenew(value) {
  subscription.value.autoRenew = value
  persist()
  ElMessage.success(value ? '已开启自动续费' : '已关闭自动续费')
}

async function renew() {
  const plan = currentPlan.value
  const price = plan[cycle.value] || plan.monthly
  try {
    await ElMessageBox.confirm(
      `确认续费「${plan.name}」${cycleLabel(cycle.value)}，将从积分余额扣除 ${price} 积分？`,
      '续费确认',
      { confirmButtonText: '确认续费', cancelButtonText: '取消' }
    )
  } catch (error) {
    // 用户取消，不做任何变更
    return
  }

  // 先扣费：余额不足时直接引导充值，不会出现「先延期再欠费」
  const charged = await payForSubscription(price, `${plan.name}续费`)
  if (!charged) return

  try {
    // 订单先落盘再更新订阅：中途失败不会出现「有效期已延长但没有订单」
    if (price > 0) {
      appendOrder({
        id: `LX${dayjs().format('YYYYMMDDHHmmss')}${Math.floor(Math.random() * 900 + 100)}`,
        amount: price,
        description: `${plan.name}订阅续费 · ${cycleLabel(cycle.value)}`,
        status: 'paid',
        payMethod: '积分',
        createdAt: dayjs().format('YYYY-MM-DD HH:mm')
      })
    }
    const unit = cycle.value === 'yearly' ? 'year' : 'month'
    subscription.value.expiresAt = dayjs(subscription.value.expiresAt)
      .add(1, unit)
      .format('YYYY-MM-DD')
    subscription.value.cycle = cycle.value
    persist()
    ElMessage.success('续费成功，有效期已延长')
  } catch (error) {
    // 落库失败必须把刚扣的积分退回，否则账实不符；按扣费分摊原路退回
    if (price > 0) {
      refundCredits({
        amount: price,
        service: '订阅',
        model: plan.name,
        reason: '续费落库失败自动退款',
        allocations: charged.allocations || null
      })
    }
    ElMessage.error('续费失败，积分已退回')
  }
}

async function choosePlan(plan) {
  const price = plan[cycle.value] || plan.monthly
  try {
    await ElMessageBox.confirm(
      `确认切换到「${plan.name}」${cycleLabel(cycle.value)}${price ? `，将从积分余额扣除 ${price} 积分` : '（免费）'}？`,
      '切换套餐',
      { confirmButtonText: '确认切换', cancelButtonText: '取消' }
    )
  } catch (error) {
    return
  }

  // 切换/开通前先扣费，余额不足时引导充值（免费套餐不扣）
  const charged = await payForSubscription(price, plan.name)
  if (!charged) return

  try {
    if (price > 0) {
      appendOrder({
        id: `LX${dayjs().format('YYYYMMDDHHmmss')}${Math.floor(Math.random() * 900 + 100)}`,
        amount: price,
        description: `${plan.name}订阅 · ${cycleLabel(cycle.value)}`,
        status: 'paid',
        payMethod: '积分',
        createdAt: dayjs().format('YYYY-MM-DD HH:mm')
      })
    }
    appendPlanHistory({
      id: `ph-${Date.now()}`,
      planName: plan.name,
      cycle: cycleLabel(cycle.value),
      amount: price,
      operator: '本人',
      status: '已完成',
      createdAt: dayjs().format('YYYY-MM-DD HH:mm')
    })
    subscription.value.planKey = plan.key
    subscription.value.cycle = cycle.value
    subscription.value.status = 'ACTIVE'
    subscription.value.creditsPerMonth = plan.credits
    subscription.value.creditsUsed = 0
    subscription.value.startedAt = dayjs().format('YYYY-MM-DD HH:mm')
    subscription.value.expiresAt = dayjs()
      .add(1, cycle.value === 'yearly' ? 'year' : 'month')
      .format('YYYY-MM-DD')
    persist()
    history.value = loadPlanHistory()
    ElMessage.success(`已切换到${plan.name}`)
  } catch (error) {
    // 扣费已发生但订阅未落盘：必须退回，否则用户白扣；按扣费分摊原路退回
    if (price > 0) {
      refundCredits({
        amount: price,
        service: '订阅',
        model: plan.name,
        reason: '套餐切换落库失败自动退款',
        allocations: charged.allocations || null
      })
    }
    ElMessage.error('切换套餐失败，积分已退回')
  }
}
</script>

<style scoped>
.current-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(0, 1fr);
  gap: 14px;
}

.current-card {
  padding: 22px 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  background:
    radial-gradient(420px 180px at 0% 0%, rgba(59, 130, 246, 0.16), transparent 70%),
    var(--app-surface);
}

.current-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.eyebrow {
  color: #7cb2ff;
  font-size: 11px;
  letter-spacing: 0.08em;
}

.current-top h2 {
  margin: 6px 0 4px;
  font-size: 26px;
  letter-spacing: -0.02em;
}

.current-top p {
  margin: 0;
  color: var(--app-muted);
  font-size: 12px;
}

.credit-block {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.credit-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: var(--app-muted);
  font-size: 12px;
}

.credit-head strong {
  color: var(--app-text);
  font-size: 13px;
}

.credit-hint {
  color: var(--app-muted);
  font-size: 11.5px;
}

.current-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--app-border);
}

.renew {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--app-muted);
  font-size: 12px;
}

.perks-card {
  padding: 22px 24px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.perks-card h3 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
}

.perk-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.perk-list li {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--app-text);
  font-size: 12.5px;
}

.perk-list :deep(.el-icon) {
  color: #34d399;
  font-size: 14px;
}

.perk-note {
  margin-top: auto;
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

.plan-section {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.plan-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.plan-head h3 {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
}

.plan-head span {
  color: var(--app-muted);
  font-size: 11.5px;
}

.plan-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.plan-card {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 22px 20px 20px;
  transition: all 0.16s ease;
}

.plan-card:hover {
  border-color: var(--app-border-strong);
  transform: translateY(-2px);
}

.plan-card.featured {
  border-color: rgba(96, 165, 250, 0.5);
  box-shadow: 0 0 0 1px rgba(96, 165, 250, 0.2);
}

.plan-card.current {
  background: rgba(59, 130, 246, 0.08);
}

.plan-tag {
  position: absolute;
  top: -9px;
  left: 20px;
  padding: 2px 10px;
  border-radius: 999px;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: #fff;
  font-size: 10.5px;
}

.plan-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.plan-title h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.current-badge {
  padding: 1px 7px;
  border-radius: 6px;
  background: rgba(59, 130, 246, 0.2);
  color: #9ecbff;
  font-size: 10px;
}

.plan-price {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.plan-price strong {
  font-size: 28px;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.plan-price span {
  color: var(--app-muted);
  font-size: 11.5px;
}

.plan-credits {
  margin: 0;
  color: #7cb2ff;
  font-size: 12px;
}

.plan-features {
  list-style: none;
  margin: 4px 0 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
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

.plan-btn {
  width: 100%;
  margin-top: 6px;
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

@media (max-width: 1080px) {
  .current-grid {
    grid-template-columns: 1fr;
  }

  .plan-grid {
    grid-template-columns: 1fr;
  }
}
</style>
