<template>
  <div class="admin-page">
    <PageHeader title="推荐管理" description="管理用户邀请记录、奖励发放与推荐活动规则。" flush>
      <el-button round @click="exportCsv">
        <el-icon><Download /></el-icon>
        导出记录
      </el-button>
    </PageHeader>

    <section class="stat-grid">
      <StatCard label="累计邀请" :value="stats.total" icon="Share" hint="全部推荐关系" />
      <StatCard label="已发放" :value="stats.granted" icon="CircleCheck" hint="奖励已到账" />
      <StatCard label="待发放" :value="stats.pending" icon="Clock" hint="等待满足发放条件" />
      <StatCard label="累计奖励" :value="stats.rewardPaid.toFixed(2)" icon="Coin" hint="已发放积分总额" />
    </section>

    <section class="filters panel">
      <TabStrip v-model="tab" :items="tabs" class="referral-tabs" />
      <el-input v-model="keyword" class="filter-search" placeholder="搜索邀请人、被邀请人或备注" clearable>
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-select v-if="tab === 'records'" v-model="status" class="filter-item">
        <el-option label="全部状态" value="all" />
        <el-option label="待发放" value="PENDING" />
        <el-option label="已发放" value="GRANTED" />
        <el-option label="已作废" value="VOID" />
      </el-select>
      <el-button round @click="resetFilters">重置筛选</el-button>
    </section>

    <section v-if="tab === 'records'" class="panel table-panel">
      <div class="table-head">
        <h3>推荐记录</h3>
        <span v-if="!rules.enabled">推荐计划已停用 · 共 {{ filtered.length }} 条记录</span>
        <span v-else>共 {{ filtered.length }} 条记录</span>
      </div>
      <el-table :data="paged">
        <el-table-column label="邀请人" min-width="160">
          <template #default="{ row }">
            <div class="user-cell">
              <span class="avatar">{{ row.inviter.slice(0, 1) }}</span>
              <span class="user-cell-main">
                <strong>{{ row.inviter }}</strong>
                <span>@{{ row.inviterUsername }}</span>
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="被邀请人" min-width="160">
          <template #default="{ row }">
            <div class="user-cell-main">
              <strong>{{ row.invitee }}</strong>
              <span>@{{ row.inviteeUsername }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="channel" label="渠道" width="100" />
        <el-table-column prop="registeredAt" label="注册时间" width="160" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTag[row.status]" size="small" effect="plain">
              {{ statusLabel[row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="奖励积分" width="100">
          <template #default="{ row }">
            <span class="money reward">{{ Number(row.reward).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="发放时间" width="160">
          <template #default="{ row }">{{ row.grantedAt || '—' }}</template>
        </el-table-column>
        <el-table-column label="备注" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="cell-sub">{{ row.note || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <div v-if="row.status === 'PENDING'" class="actions">
              <el-button link type="primary" @click="grant(row)">发放</el-button>
              <el-button link type="danger" @click="voidRecord(row)">作废</el-button>
            </div>
            <span v-else class="cell-sub">—</span>
          </template>
        </el-table-column>
        <template #empty>
          <div class="table-empty">没有匹配的推荐记录</div>
        </template>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="page"
          layout="prev, pager, next"
          :page-size="pageSize"
          :total="filtered.length"
          background
        />
      </div>
    </section>

    <section v-else class="panel rules-panel">
      <div class="table-head">
        <h3>奖励规则</h3>
        <span>保存后对新的推荐关系生效</span>
      </div>

      <el-form label-position="top" class="rules-form">
        <el-form-item label="推荐计划" class="full">
          <div class="rule-switch">
            <el-switch v-model="rules.enabled" active-text="启用" inactive-text="停用" />
            <span>停用后不再产生新的邀请关系与奖励</span>
          </div>
        </el-form-item>
        <el-form-item label="邀请人奖励积分">
          <el-input-number v-model="rules.inviterReward" :min="0" :max="10000" :precision="2" controls-position="right" />
        </el-form-item>
        <el-form-item label="被邀请人奖励积分">
          <el-input-number v-model="rules.inviteeReward" :min="0" :max="10000" :precision="2" controls-position="right" />
        </el-form-item>
        <el-form-item label="发放条件">
          <el-select v-model="rules.condition">
            <el-option label="注册成功即发放" value="REGISTER" />
            <el-option label="完成首次付费后发放" value="FIRST_PAY" />
          </el-select>
        </el-form-item>
        <el-form-item label="奖励有效期（天）">
          <el-input-number v-model="rules.validDays" :min="1" :max="365" controls-position="right" />
        </el-form-item>
        <el-form-item label="每人最多邀请人数">
          <el-input-number v-model="rules.maxInvites" :min="1" :max="1000" controls-position="right" />
        </el-form-item>
        <el-form-item label="邀请海报">
          <div class="rule-switch">
            <el-switch v-model="rules.posterEnabled" active-text="开启" inactive-text="关闭" />
            <span>允许用户生成专属邀请海报</span>
          </div>
        </el-form-item>
        <el-form-item label="邀请链接域名" class="full">
          <el-input v-model="rules.linkDomain" placeholder="https://lingxi.ai/invite" />
        </el-form-item>
        <el-form-item label="规则说明" class="full">
          <el-input
            v-model="rules.remark"
            type="textarea"
            :rows="3"
            maxlength="120"
            show-word-limit
            placeholder="说明奖励发放时机、有效条件等"
          />
        </el-form-item>
      </el-form>

      <div class="rules-foot">
        <el-button round @click="resetRules">恢复默认</el-button>
        <el-button type="primary" round @click="saveRules">保存规则</el-button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import PageHeader from '@/components/PageHeader.vue'
import StatCard from '@/components/StatCard.vue'
import TabStrip from '@/components/TabStrip.vue'
import {
  defaultReferralRules,
  loadAdminReferrals,
  loadAdminUsers,
  loadCreditTransactions,
  loadReferralRules,
  saveAdminReferrals,
  saveAdminUsers,
  saveCreditTransactions,
  saveReferralRules
} from '@/mock/admin'
import { downloadCsv } from '@/utils/csv'
import { matchKeyword } from '@/utils/text'

const referrals = ref(loadAdminReferrals())
const tab = ref('records')
const keyword = ref('')
const status = ref('all')
const page = ref(1)
const pageSize = 10

const tabs = [
  { key: 'records', label: '推荐记录' },
  { key: 'rules', label: '奖励规则' }
]

const statusLabel = {
  PENDING: '待发放',
  GRANTED: '已发放',
  VOID: '已作废'
}

const statusTag = {
  PENDING: 'warning',
  GRANTED: 'success',
  VOID: 'info'
}

const rules = reactive(loadReferralRules())

const filtered = computed(() => {
  const text = keyword.value.trim().toLowerCase()
  return referrals.value.filter(item => {
    // 演示数据可能缺字段，统一用 matchKeyword 做空值安全的下匹配
    const matchKeywordText = matchKeyword(
      text,
      item.inviter,
      item.inviterUsername,
      item.invitee,
      item.inviteeUsername,
      item.note
    )
    const matchStatus = status.value === 'all' || item.status === status.value
    return matchKeywordText && matchStatus
  })
})

const paged = computed(() => filtered.value.slice((page.value - 1) * pageSize, page.value * pageSize))

const stats = computed(() => {
  const granted = referrals.value.filter(item => item.status === 'GRANTED')
  return {
    total: referrals.value.length,
    granted: granted.length,
    pending: referrals.value.filter(item => item.status === 'PENDING').length,
    rewardPaid: granted.reduce((sum, item) => sum + Number(item.reward), 0)
  }
})

watch([keyword, status, tab], () => {
  page.value = 1
})

function resetFilters() {
  keyword.value = ''
  status.value = 'all'
}

async function grant(row) {
  if (!rules.enabled) {
    ElMessage.warning('推荐计划已停用，请先在「奖励规则」中启用后再发放奖励')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确认向 ${row.inviter} 发放 ${Number(row.reward).toFixed(2)} 积分推荐奖励吗？`,
      '发放奖励',
      { confirmButtonText: '发放', cancelButtonText: '取消', type: 'warning' }
    )
  } catch (error) {
    return
  }

  const now = dayjs().format('YYYY-MM-DD HH:mm')
  const users = loadAdminUsers()
  const target = users.find(user => user.username === row.inviterUsername)
  if (!target) {
    ElMessage.error('未找到邀请人账户，无法发放奖励')
    return
  }

  const nextBalance = Number(((Number(target.credits) || 0) + Number(row.reward)).toFixed(2))
  target.credits = nextBalance
  saveAdminUsers(users)

  const transactions = loadCreditTransactions()
  transactions.unshift({
    id: `ctx-${Date.now()}`,
    time: now,
    user: target.nickname,
    username: target.username,
    type: 'REWARD',
    amount: Number(row.reward),
    balance: nextBalance,
    remark: `推荐奖励 · 邀请「${row.invitee}」`,
    operator: '演示管理员'
  })
  saveCreditTransactions(transactions)

  row.status = 'GRANTED'
  row.grantedAt = now
  saveAdminReferrals(referrals.value)
  ElMessage.success(`已向 ${row.inviter} 发放 ${Number(row.reward).toFixed(2)} 积分`)
}

async function voidRecord(row) {
  try {
    await ElMessageBox.confirm(
      `作废后 ${row.inviter} 将无法获得该笔推荐奖励，确定继续吗？`,
      '作废推荐奖励',
      { confirmButtonText: '作废', cancelButtonText: '取消', type: 'warning' }
    )
  } catch (error) {
    return
  }
  row.status = 'VOID'
  row.grantedAt = ''
  saveAdminReferrals(referrals.value)
  ElMessage.success('该笔推荐奖励已作废')
}

function resetRules() {
  Object.assign(rules, defaultReferralRules)
  ElMessage.info('已恢复默认规则，保存后生效')
}

function saveRules() {
  const linkDomain = String(rules.linkDomain || '').trim()
  if (!linkDomain) {
    ElMessage.warning('请填写邀请链接域名')
    return
  }
  if (!/^https?:\/\/.+/i.test(linkDomain)) {
    ElMessage.warning('邀请链接域名需以 http:// 或 https:// 开头')
    return
  }
  if (Number(rules.inviterReward) <= 0 && Number(rules.inviteeReward) <= 0) {
    ElMessage.warning('邀请人与被邀请人奖励不能同时为 0')
    return
  }

  saveReferralRules({ ...rules, linkDomain })
  rules.linkDomain = linkDomain
  ElMessage.success('推荐规则已保存')
}

function exportCsv() {
  downloadCsv(
    `推荐记录-${dayjs().format('YYYYMMDD')}.csv`,
    ['邀请人', '邀请人账号', '被邀请人', '被邀请人账号', '渠道', '注册时间', '状态', '奖励积分', '发放时间', '备注'],
    filtered.value.map(item => [
      item.inviter,
      item.inviterUsername,
      item.invitee,
      item.inviteeUsername,
      item.channel,
      item.registeredAt,
      statusLabel[item.status],
      item.reward,
      item.grantedAt || '—',
      item.note
    ])
  )
  ElMessage.success(`已导出 ${filtered.value.length} 条推荐记录`)
}
</script>

<style scoped>
.referral-tabs {
  flex-shrink: 0;
}

.reward {
  color: #fbbf24;
  font-weight: 600;
}

.rules-panel {
  padding: 4px 24px 22px;
}

.rules-form {
  margin-top: 8px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 18px;
}

.rules-form .full {
  grid-column: 1 / -1;
}

.rules-form :deep(.el-select),
.rules-form :deep(.el-input-number) {
  width: 100%;
}

.rule-switch {
  display: flex;
  align-items: center;
  gap: 12px;
}

.rule-switch span {
  color: var(--app-muted);
  font-size: 11.5px;
}

.rules-foot {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding-top: 6px;
}

@media (max-width: 900px) {
  .rules-form {
    grid-template-columns: 1fr;
  }
}
</style>
