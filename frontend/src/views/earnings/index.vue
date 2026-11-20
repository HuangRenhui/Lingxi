<template>
  <div class="page">
    <PageHeader title="推荐收益" description="邀请好友使用，获得持续的推荐奖励。">
      <el-button round @click="detailVisible = true">
        <el-icon><List /></el-icon>
        推荐明细
      </el-button>
    </PageHeader>

    <section class="stat-grid">
      <StatCard label="推荐订单总金额" :value="`¥${money(state.totalAmount)}`" icon="Money" hint="被邀请者累计消费" />
      <StatCard label="总推荐奖励" :value="`¥${money(state.totalReward)}`" icon="Coin" :hint="`当前可提现`">
        <template #action>
          <el-button link type="primary" @click="withdraw">提现</el-button>
        </template>
      </StatCard>
      <StatCard label="推荐收益百分比" :value="summary.rate" unit="%" icon="Discount" hint="按被邀请者消费金额持续计算" />
      <StatCard label="邀请者总数" :value="state.invitees" unit="人" icon="User" :hint="`已展示 ${records.length} 条明细`" />
    </section>

    <section class="promo panel">
      <div class="promo-icon">
        <el-icon><Promotion /></el-icon>
      </div>
      <div class="promo-copy">
        <strong>自动推广</strong>
        <p>把推荐链接分享到公众号、社群或邮件签名，长期获得推荐奖励。</p>
      </div>
      <el-button round @click="templateVisible = true">浏览推广模板</el-button>
    </section>

    <section class="bottom-grid">
      <div class="panel level-panel">
        <div class="panel-head">
          <h3>级别信息</h3>
          <span class="muted">了解推荐等级与对应奖励</span>
        </div>
        <div class="level-now">
          <div>
            <span class="muted">当前级别</span>
            <strong>{{ summary.level }} 会员</strong>
          </div>
          <div>
            <span class="muted">下一级别</span>
            <strong>{{ summary.nextLevel }} 会员</strong>
          </div>
        </div>
        <el-progress :percentage="summary.progress" :stroke-width="6" :show-text="false" color="#3b82f6" />
        <div class="progress-label">距下一级别还需 {{ summary.needMore }} 位有效邀请</div>

        <el-table :data="levels" class="level-table" size="small">
          <el-table-column prop="level" label="级别" width="128" />
          <el-table-column prop="member" label="会员奖励" />
          <el-table-column prop="referral" label="推荐奖励" />
        </el-table>
      </div>

      <div class="panel link-panel">
        <div class="panel-head">
          <h3>推荐链接</h3>
        </div>
        <p class="muted link-hint">好友通过此链接注册后，将自动绑定推荐关系。</p>
        <div class="link-row">
          <el-input :model-value="referralLink" readonly />
          <el-button @click="copyLink">复制</el-button>
        </div>

        <div class="qr-wrap">
          <div class="qr">
            <span v-for="(cell, index) in qrCells" :key="index" :class="{ on: cell }" />
          </div>
          <p class="muted">扫码注册，自动绑定推荐关系</p>
        </div>
      </div>
    </section>

    <el-drawer v-model="detailVisible" title="推荐明细" size="560px">
      <el-table :data="paged" size="small">
        <el-table-column label="被邀请人" min-width="140">
          <template #default="{ row }">
            <div class="invitee">
              <span class="avatar">{{ row.name.slice(0, 1) }}</span>
              <span class="invitee-main">
                <strong>{{ row.name }}</strong>
                <span>@{{ row.username }}</span>
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="registeredAt" label="注册时间" width="140" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusOf(row.status).type" size="small" effect="plain">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="贡献消费" width="100">
          <template #default="{ row }">
            <span class="mono">¥{{ money(row.spent) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="我的奖励" width="100">
          <template #default="{ row }">
            <span class="mono reward">¥{{ money(row.reward) }}</span>
          </template>
        </el-table-column>
      </el-table>
      <TablePager v-model:page="page" :total="records.length" />
      <p class="drawer-tip">奖励按被邀请者实际消费金额的 {{ summary.rate }}% 计算，按月结算到钱包。</p>
    </el-drawer>

    <el-dialog v-model="templateVisible" title="推广模板" width="600px">
      <div class="template-list">
        <article v-for="item in templates" :key="item.title" class="template-item">
          <div class="template-head">
            <strong>{{ item.title }}</strong>
            <el-button link size="small" @click="copyTemplate(item)">复制文案</el-button>
          </div>
          <p>{{ item.content }}</p>
        </article>
      </div>
      <template #footer>
        <el-button round @click="templateVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { writeJson } from '@/utils/storage'
import dayjs from 'dayjs'
import PageHeader from '@/components/PageHeader.vue'
import StatCard from '@/components/StatCard.vue'
import TablePager from '@/components/TablePager.vue'
import {
  appendWalletLog,
  referralRecords,
  referralSummary,
  referralTemplates
} from '@/mock/account'
import { creditSummary, grantReward } from '@/mock/credits'

const STATE_KEY = 'lingxi-referral-state'

const referralLink = 'https://lingxi.app/register?ref=AI-STUDIO'
const summary = referralSummary
const records = referralRecords
const templates = referralTemplates
const detailVisible = ref(false)
const templateVisible = ref(false)
const page = ref(1)
const pageSize = 10

const state = reactive(loadState())

const paged = computed(() =>
  records.slice((page.value - 1) * pageSize, page.value * pageSize)
)

const activeCount = computed(() => records.filter(item => item.status === '有效').length)

const levels = [
  { level: 'L1 一级会员', member: '5%', referral: '1%' },
  { level: 'L2 二级会员', member: '8%', referral: '2%' },
  { level: 'L3 三级会员', member: '10%', referral: '3%' },
  { level: 'L4 四级会员', member: '12%', referral: '4%' },
  { level: 'L5 五级会员', member: '15%', referral: '5%' },
  { level: 'L6 六级会员', member: '18%', referral: '6%' },
  { level: 'L7 七级会员', member: '20%', referral: '7%' },
  { level: 'L8 八级会员', member: '22%', referral: '8%' },
  { level: 'L9 九级会员', member: '25%', referral: '9%' },
  { level: 'L10 十级会员', member: '30%', referral: '10%' }
]

// 生成 21x21 的二维码视觉占位（含三个定位角），仅用于界面展示
const qrCells = buildQrCells(21)

function buildQrCells(size) {
  const cells = []
  for (let y = 0; y < size; y++) {
    for (let x = 0; x < size; x++) {
      const inFinder = (x < 7 && y < 7) || (x >= size - 7 && y < 7) || (x < 7 && y >= size - 7)
      if (inFinder) {
        const localX = x >= size - 7 ? x - (size - 7) : x
        const localY = y >= size - 7 ? y - (size - 7) : y
        const ring = Math.max(Math.abs(localX - 3), Math.abs(localY - 3))
        cells.push(ring === 3 || ring <= 1)
      } else {
        cells.push((x * 7 + y * 13 + (x * y) % 11) % 5 < 2)
      }
    }
  }
  return cells
}

function loadState() {
  const fallback = {
    totalAmount: referralSummary.totalAmount,
    totalReward: referralSummary.totalReward,
    invitees: referralSummary.invitees
  }
  try {
    const raw = localStorage.getItem(STATE_KEY)
    return raw ? { ...fallback, ...JSON.parse(raw) } : fallback
  } catch (error) {
    return fallback
  }
}

function persistState() {
  if (!writeJson(STATE_KEY, state)) {
    ElMessage.warning('本地存储不可用，推荐收益仅保留到刷新前')
  }
}

function money(value) {
  return Number(value).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function statusOf(status) {
  return {
    有效: { type: 'success' },
    待激活: { type: 'warning' },
    已失效: { type: 'info' }
  }[status] || { type: 'info' }
}

async function copyLink() {
  try {
    await navigator.clipboard.writeText(referralLink)
    ElMessage.success('推荐链接已复制')
  } catch (error) {
    ElMessage.warning('当前环境不支持剪贴板，请手动复制')
  }
}

async function copyTemplate(item) {
  try {
    await navigator.clipboard.writeText(item.content)
    ElMessage.success(`「${item.title}」文案已复制`)
  } catch (error) {
    ElMessage.warning('当前环境不支持剪贴板，请手动复制')
  }
}

async function withdraw() {
  if (state.totalReward <= 0) {
    ElMessage.info('当前没有可提现的推荐奖励')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确认将 ¥${money(state.totalReward)} 推荐奖励转入钱包余额？`,
      '提现到钱包',
      { confirmButtonText: '确认转入', cancelButtonText: '取消' }
    )
  } catch (error) {
    return
  }
  const amount = Number(Number(state.totalReward || 0).toFixed(2))
  if (!(amount > 0)) {
    ElMessage.info('当前没有可提现的推荐奖励')
    return
  }
  // 推荐奖励属于「奖励积分」：独立批次、带到期时间，可在钱包页查看明细
  grantReward({
    name: '推荐奖励',
    source: 'referral',
    amount,
    expiresAt: dayjs().add(1, 'year').endOf('day').format('YYYY-MM-DD HH:mm'),
    note: '推荐奖励提现到钱包'
  })
  const summary = creditSummary()
  appendWalletLog({
    id: `wl-${Date.now()}`,
    createdAt: dayjs().format('YYYY-MM-DD HH:mm'),
    type: '奖励',
    amount,
    balance: summary.total,
    note: '推荐奖励提现到钱包'
  })
  state.totalReward = 0
  persistState()
  ElMessage.success(`已转入钱包 ¥${money(amount)}`)
}
</script>

<style scoped>
.promo {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 22px;
}

.promo-icon {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  background: var(--app-primary-soft);
  color: #7cb2ff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}

.promo-copy {
  flex: 1;
}

.promo-copy strong {
  font-size: 14px;
}

.promo-copy p {
  margin: 4px 0 0;
  color: var(--app-muted);
  font-size: 11.5px;
}

.bottom-grid {
  display: grid;
  grid-template-columns: 1.25fr 1fr;
  gap: 12px;
}

.level-panel,
.link-panel {
  padding: 20px 22px;
}

.panel-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.panel-head h3 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
}

.panel-head span {
  font-size: 11.5px;
}

.level-now {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 12px;
}

.level-now > div {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.level-now .muted {
  font-size: 11px;
}

.level-now strong {
  font-size: 13.5px;
  font-weight: 600;
}

.progress-label {
  margin: 8px 0 18px;
  color: #5b636e;
  font-size: 11px;
}

.level-table {
  margin-top: 4px;
}

.link-hint {
  margin: 0 0 10px;
  font-size: 11.5px;
}

.link-row {
  display: flex;
  gap: 8px;
}

.qr-wrap {
  margin-top: 26px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.qr {
  width: 148px;
  height: 148px;
  padding: 8px;
  border-radius: 10px;
  background: #e6edf3;
  display: grid;
  grid-template-columns: repeat(21, 1fr);
  grid-template-rows: repeat(21, 1fr);
  gap: 0;
}

.qr span {
  background: transparent;
}

.qr span.on {
  background: #0a0c10;
}

.qr-wrap p {
  margin: 0;
  font-size: 11px;
}

.invitee {
  display: flex;
  align-items: center;
  gap: 9px;
}

.invitee .avatar {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.38), rgba(29, 78, 216, 0.2));
  border: 1px solid rgba(96, 165, 250, 0.3);
  color: #dbeafe;
  font-size: 11px;
  flex-shrink: 0;
}

.invitee-main {
  display: flex;
  flex-direction: column;
  line-height: 1.35;
}

.invitee-main strong {
  font-size: 12.5px;
  font-weight: 600;
}

.invitee-main span {
  color: var(--app-muted);
  font-size: 10.5px;
}

.reward {
  color: #34d399;
}

.drawer-tip {
  margin: 14px 0 0;
  color: var(--app-muted);
  font-size: 11.5px;
  line-height: 1.7;
}

.template-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.template-item {
  padding: 12px 14px;
  border-radius: 10px;
  border: 1px solid var(--app-border);
  background: var(--app-surface-2);
}

.template-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}

.template-head strong {
  font-size: 12.5px;
  font-weight: 600;
}

.template-item p {
  margin: 0;
  color: var(--app-muted);
  font-size: 11.5px;
  line-height: 1.7;
}

@media (max-width: 1100px) {
  .bottom-grid {
    grid-template-columns: 1fr;
  }
}
</style>
