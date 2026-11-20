<template>
  <div class="admin-page">
    <PageHeader title="积分管理" description="查看用户积分账户与全部流水，支持手动增加或扣减积分。" flush>
      <el-button round @click="exportCsv">
        <el-icon><Download /></el-icon>
        导出流水
      </el-button>
    </PageHeader>

    <section class="stat-grid">
      <StatCard label="流通积分" :value="stats.totalCredits.toFixed(2)" icon="Coin" hint="全部用户当前余额之和" />
      <StatCard label="本月发放" :value="stats.monthGained.toFixed(2)" icon="TopRight" hint="充值、奖励与手动增加" />
      <StatCard label="本月消耗" :value="stats.monthConsumed.toFixed(2)" icon="BottomLeft" hint="调用消费与手动扣减" />
      <StatCard label="参与用户" :value="stats.participants" icon="User" hint="积分余额大于 0 的用户" />
    </section>

    <section class="filters panel">
      <TabStrip v-model="tab" :items="tabs" class="credit-tabs" />
      <el-input v-model="keyword" class="filter-search" placeholder="搜索用户或备注" clearable>
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-select v-if="tab === 'records'" v-model="type" class="filter-item">
        <el-option label="全部类型" value="all" />
        <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-button round @click="resetFilters">重置筛选</el-button>
    </section>

    <section v-if="tab === 'accounts'" class="panel table-panel">
      <div class="table-head">
        <h3>积分账户</h3>
        <span>共 {{ filteredAccounts.length }} 个账户</span>
      </div>
      <el-table :data="pagedAccounts">
        <el-table-column label="用户" min-width="190">
          <template #default="{ row }">
            <div class="user-cell">
              <span class="avatar">{{ row.nickname.slice(0, 1) }}</span>
              <span class="user-cell-main">
                <strong>{{ row.nickname }}</strong>
                <span>@{{ row.username }}</span>
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="plan" label="套餐" width="100" />
        <el-table-column label="当前余额" width="120">
          <template #default="{ row }">
            <span class="money balance">{{ Number(row.credits).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="本月获得" width="110">
          <template #default="{ row }"><span class="gain">+{{ row.gained.toFixed(2) }}</span></template>
        </el-table-column>
        <el-table-column label="本月消耗" width="110">
          <template #default="{ row }"><span class="loss">-{{ row.consumed.toFixed(2) }}</span></template>
        </el-table-column>
        <el-table-column prop="lastChange" label="最近变动" width="160" />
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openAdjust(row)">调整积分</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="table-empty">没有匹配的积分账户</div>
        </template>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="page"
          layout="prev, pager, next"
          :page-size="pageSize"
          :total="filteredAccounts.length"
          background
        />
      </div>
    </section>

    <section v-else class="panel table-panel">
      <div class="table-head">
        <h3>积分流水</h3>
        <span>共 {{ filteredRecords.length }} 条记录</span>
      </div>
      <el-table :data="pagedRecords">
        <el-table-column prop="time" label="时间" width="160" />
        <el-table-column label="用户" min-width="150">
          <template #default="{ row }">
            <div class="user-cell-main">
              <strong>{{ row.user }}</strong>
              <span>@{{ row.username }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="104">
          <template #default="{ row }">
            <el-tag :type="typeTag[row.type] || 'info'" size="small" effect="plain">
              {{ typeLabel[row.type] || row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="变动" width="110">
          <template #default="{ row }">
            <span :class="row.amount >= 0 ? 'gain' : 'loss'">
              {{ row.amount >= 0 ? '+' : '' }}{{ Number(row.amount).toFixed(2) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="变动后余额" width="120">
          <template #default="{ row }">
            <span class="money">{{ Number(row.balance).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="220" show-overflow-tooltip />
        <el-table-column prop="operator" label="操作人" width="110" />
        <template #empty>
          <div class="table-empty">没有匹配的积分流水</div>
        </template>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="page"
          layout="prev, pager, next"
          :page-size="pageSize"
          :total="filteredRecords.length"
          background
        />
      </div>
    </section>

    <el-dialog v-model="adjustVisible" title="调整积分" width="440px" append-to-body>
      <template v-if="activeUser">
        <div class="adjust-user">
          <span class="avatar">{{ activeUser.nickname.slice(0, 1) }}</span>
          <div class="adjust-user-main">
            <strong>{{ activeUser.nickname }}</strong>
            <span>@{{ activeUser.username }} · {{ activeUser.plan }}</span>
          </div>
          <span class="adjust-balance">余额 {{ Number(activeUser.credits).toFixed(2) }}</span>
        </div>

        <el-form label-position="top" class="adjust-form">
          <el-form-item label="调整方式">
            <el-radio-group v-model="adjustForm.direction">
              <el-radio-button value="add">增加积分</el-radio-button>
              <el-radio-button value="minus">扣减积分</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="积分数量">
            <el-input-number
              v-model="adjustForm.amount"
              :min="0.01"
              :max="100000"
              :step="10"
              :precision="2"
              controls-position="right"
            />
          </el-form-item>
          <el-form-item label="备注">
            <el-input
              v-model="adjustForm.remark"
              maxlength="60"
              show-word-limit
              placeholder="例如：活动补发、客服补偿"
            />
          </el-form-item>
        </el-form>

        <div class="adjust-tip">
          调整后余额：<strong :class="{ danger: previewBalance < 0 }">{{ previewBalance.toFixed(2) }}</strong> 积分
        </div>
      </template>
      <template #footer>
        <el-button round @click="adjustVisible = false">取消</el-button>
        <el-button type="primary" round @click="submitAdjust">确认调整</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import PageHeader from '@/components/PageHeader.vue'
import StatCard from '@/components/StatCard.vue'
import TabStrip from '@/components/TabStrip.vue'
import { loadCreditTransactions, saveCreditTransactions } from '@/mock/admin'
import { adjustCredits, listAdminUsers } from '@/mock/platform'
import { downloadCsv } from '@/utils/csv'
import { matchKeyword } from '@/utils/text'

const users = ref(listAdminUsers())
const records = ref(loadCreditTransactions())
const tab = ref('accounts')
const keyword = ref('')
const type = ref('all')
const page = ref(1)
const pageSize = 10
const adjustVisible = ref(false)
const activeUser = ref(null)

const tabs = [
  { key: 'accounts', label: '账户余额' },
  { key: 'records', label: '积分流水' }
]

const typeOptions = [
  { value: 'RECHARGE', label: '充值' },
  { value: 'CONSUME', label: '消费' },
  { value: 'REWARD', label: '奖励' },
  { value: 'ADJUST', label: '管理调整' },
  { value: 'REFUND', label: '退款' }
]

const typeLabel = Object.fromEntries(typeOptions.map(item => [item.value, item.label]))
const typeTag = {
  RECHARGE: 'success',
  CONSUME: 'info',
  REWARD: 'warning',
  ADJUST: 'primary',
  REFUND: 'danger'
}

const monthKey = dayjs().format('YYYY-MM')

const adjustForm = reactive({
  direction: 'add',
  amount: 10,
  remark: ''
})

const stats = computed(() => {
  const monthRecords = records.value.filter(item => item.time.startsWith(monthKey))
  return {
    totalCredits: users.value.reduce((sum, user) => sum + Number(user.credits || 0), 0),
    monthGained: monthRecords
      .filter(item => item.amount > 0)
      .reduce((sum, item) => sum + Number(item.amount), 0),
    monthConsumed: Math.abs(
      monthRecords.filter(item => item.amount < 0).reduce((sum, item) => sum + Number(item.amount), 0)
    ),
    participants: users.value.filter(user => Number(user.credits) > 0).length
  }
})

const filteredAccounts = computed(() => {
  const text = keyword.value.trim().toLowerCase()
  return users.value
    .filter(user => user.role !== 'ADMIN')
    .map(user => {
      const own = records.value.filter(item => item.username === user.username)
      const monthOwn = own.filter(item => item.time.startsWith(monthKey))
      return {
        ...user,
        gained: monthOwn.filter(item => item.amount > 0).reduce((sum, item) => sum + Number(item.amount), 0),
        consumed: Math.abs(
          monthOwn.filter(item => item.amount < 0).reduce((sum, item) => sum + Number(item.amount), 0)
        ),
        lastChange: own[0] ? own[0].time : '—'
      }
    })
    .filter(user => matchKeyword(text, user.username, user.nickname, user.email))
})

const filteredRecords = computed(() => {
  const text = keyword.value.trim().toLowerCase()
  return records.value.filter(item => {
    const hit = matchKeyword(text, item.user, item.username, item.remark)
    const matchType = type.value === 'all' || item.type === type.value
    return hit && matchType
  })
})

const pagedAccounts = computed(() =>
  filteredAccounts.value.slice((page.value - 1) * pageSize, page.value * pageSize)
)

const pagedRecords = computed(() =>
  filteredRecords.value.slice((page.value - 1) * pageSize, page.value * pageSize)
)

const previewBalance = computed(() => {
  if (!activeUser.value) return 0
  const base = Number(activeUser.value.credits)
  const amount = Number(adjustForm.amount || 0)
  return adjustForm.direction === 'add' ? base + amount : base - amount
})

watch([keyword, type, tab], () => {
  page.value = 1
})

function resetFilters() {
  keyword.value = ''
  type.value = 'all'
}

function openAdjust(row) {
  activeUser.value = row
  adjustForm.direction = 'add'
  adjustForm.amount = 10
  adjustForm.remark = ''
  adjustVisible.value = true
}

function submitAdjust() {
  if (!activeUser.value) return
  const amount = Number(adjustForm.amount)
  if (!amount || amount <= 0) {
    ElMessage.warning('请输入大于 0 的积分数量')
    return
  }
  const target = users.value.find(user => user.username === activeUser.value.username)
  if (!target) {
    ElMessage.error('用户不存在，请刷新后重试')
    return
  }
  const delta = adjustForm.direction === 'add' ? amount : -amount
  const nextBalance = Number((Number(target.credits) + delta).toFixed(2))
  if (nextBalance < 0) {
    ElMessage.warning('扣减后余额不能为负数')
    return
  }

  // 同步写入用户端钱包（当前账号）或其他用户的积分余额
  const applied = adjustCredits(target.username, delta, adjustForm.remark.trim() || '管理员调整积分')
  const finalBalance = applied === null ? nextBalance : applied
  target.credits = finalBalance

  records.value.unshift({
    id: `ctx-${Date.now()}`,
    time: dayjs().format('YYYY-MM-DD HH:mm'),
    user: target.nickname,
    username: target.username,
    type: 'ADJUST',
    amount: delta,
    balance: finalBalance,
    remark: adjustForm.remark.trim() || (delta > 0 ? '管理员手动增加积分' : '管理员手动扣减积分'),
    operator: '演示管理员'
  })
  saveCreditTransactions(records.value)

  activeUser.value = { ...activeUser.value, credits: finalBalance }
  adjustVisible.value = false
  ElMessage.success(`${target.nickname} 积分已${delta > 0 ? '增加' : '扣减'} ${amount.toFixed(2)}，当前余额 ${Number(finalBalance).toFixed(2)}`)
}

function exportCsv() {
  downloadCsv(
    `积分流水-${dayjs().format('YYYYMMDD')}.csv`,
    ['时间', '用户', '用户名', '类型', '变动', '变动后余额', '备注', '操作人'],
    filteredRecords.value.map(item => [
      item.time,
      item.user,
      item.username,
      typeLabel[item.type] || item.type,
      item.amount,
      item.balance,
      item.remark,
      item.operator
    ])
  )
  ElMessage.success(`已导出 ${filteredRecords.value.length} 条积分流水`)
}
</script>

<style scoped>
.credit-tabs {
  flex-shrink: 0;
}

.gain {
  color: #34d399;
  font-family: "JetBrains Mono", Consolas, Monaco, monospace;
  font-size: 12.5px;
}

.loss {
  color: #f87171;
  font-family: "JetBrains Mono", Consolas, Monaco, monospace;
  font-size: 12.5px;
}

.balance {
  color: #dbeafe;
  font-weight: 600;
}

.adjust-user {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 4px 0 16px;
  border-bottom: 1px solid var(--app-border);
}

.adjust-user-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.adjust-user-main strong {
  font-size: 14px;
}

.adjust-user-main span {
  color: var(--app-muted);
  font-size: 11.5px;
}

.adjust-balance {
  color: var(--app-muted);
  font-size: 11.5px;
}

.adjust-form {
  margin-top: 18px;
}

.adjust-form :deep(.el-input-number) {
  width: 100%;
}

.adjust-tip {
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid var(--app-border);
  background: var(--app-surface-2);
  color: var(--app-muted);
  font-size: 12px;
}

.adjust-tip strong {
  color: #34d399;
  font-family: "JetBrains Mono", Consolas, Monaco, monospace;
  font-size: 13px;
}

.adjust-tip strong.danger {
  color: #f87171;
}
</style>
