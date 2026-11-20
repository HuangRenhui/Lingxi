<template>
  <div class="admin-page">
    <PageHeader title="用户管理" description="查看全部注册用户、账号状态与积分余额。" flush>
      <el-button round @click="exportCsv">
        <el-icon><Download /></el-icon>
        导出 CSV
      </el-button>
    </PageHeader>

    <section class="stat-grid">
      <StatCard label="用户总数" :value="stats.total" icon="User" hint="包含管理员账号" />
      <StatCard label="本月新增" :value="stats.thisMonth" icon="UserFilled" :hint="`${currentMonth} 注册`" />
      <StatCard label="付费用户" :value="stats.paid" icon="Wallet" hint="专业版与团队版" />
      <StatCard label="已封禁" :value="stats.banned" icon="CircleClose" hint="无法登录与调用接口" />
    </section>

    <section class="filters panel">
      <el-input v-model="keyword" class="filter-search" placeholder="搜索用户名、昵称或邮箱" clearable>
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-select v-model="role" class="filter-item">
        <el-option label="全部角色" value="all" />
        <el-option label="管理员" value="ADMIN" />
        <el-option label="普通用户" value="USER" />
      </el-select>
      <el-select v-model="status" class="filter-item">
        <el-option label="全部状态" value="all" />
        <el-option label="正常" value="ACTIVE" />
        <el-option label="已封禁" value="BANNED" />
      </el-select>
      <el-button round @click="resetFilters">重置筛选</el-button>
    </section>

    <section class="panel table-panel">
      <div class="table-head">
        <h3>用户列表</h3>
        <span>共 {{ filtered.length }} 位用户</span>
      </div>
      <el-table :data="paged">
        <el-table-column label="用户" min-width="180">
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
        <el-table-column label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'warning' : 'info'" size="small" effect="plain">
              {{ row.role === 'ADMIN' ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="plan" label="套餐" width="90" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              active-value="ACTIVE"
              inactive-value="BANNED"
              @change="onStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="积分余额" width="100">
          <template #default="{ row }">
            <span class="money">{{ Number(row.credits).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="注册 / 活跃" width="190">
          <template #default="{ row }">
            <div class="time-cell">
              <span>{{ row.registeredAt }}</span>
              <span class="cell-sub">活跃 {{ row.lastActiveAt }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="130">
          <template #default="{ row }">
            <div class="actions">
              <el-button link type="primary" @click="openDetail(row)">详情</el-button>
              <el-button link @click="resetPassword(row)">重置密码</el-button>
            </div>
          </template>
        </el-table-column>
        <template #empty>
          <div class="table-empty">没有匹配的用户</div>
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

    <el-drawer v-model="drawerVisible" title="用户详情" size="440px" append-to-body>
      <template v-if="activeUser">
        <div class="drawer-user">
          <span class="avatar big">{{ activeUser.nickname.slice(0, 1) }}</span>
          <div class="drawer-user-main">
            <strong>{{ activeUser.nickname }}</strong>
            <span>@{{ activeUser.username }}</span>
          </div>
          <el-tag :type="activeUser.status === 'ACTIVE' ? 'success' : 'danger'" size="small" effect="plain">
            {{ activeUser.status === 'ACTIVE' ? '正常' : '已封禁' }}
          </el-tag>
        </div>

        <div class="detail-section">基础信息</div>
        <div class="detail-list">
          <div class="detail-item">
            <span>用户 ID</span>
            <strong>{{ activeUser.id }}</strong>
          </div>
          <div class="detail-item">
            <span>角色</span>
            <strong>{{ activeUser.role === 'ADMIN' ? '管理员' : '普通用户' }}</strong>
          </div>
          <div class="detail-item">
            <span>套餐</span>
            <strong>{{ activeUser.plan }}</strong>
          </div>
          <div class="detail-item">
            <span>邮箱</span>
            <strong>{{ activeUser.email }}</strong>
          </div>
          <div class="detail-item full">
            <span>注册时间</span>
            <strong>{{ activeUser.registeredAt }}</strong>
          </div>
          <div class="detail-item full">
            <span>最近活跃</span>
            <strong>{{ activeUser.lastActiveAt }}</strong>
          </div>
        </div>

        <div class="detail-section">账户概况</div>
        <div class="detail-list">
          <div class="detail-item">
            <span>积分余额</span>
            <strong>{{ Number(activeUser.credits).toFixed(2) }}</strong>
          </div>
          <div class="detail-item">
            <span>定时任务</span>
            <strong>{{ activeUser.tasks }} 个</strong>
          </div>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import PageHeader from '@/components/PageHeader.vue'
import StatCard from '@/components/StatCard.vue'
import { listAdminUsers, saveUsers } from '@/mock/platform'
import { downloadCsv } from '@/utils/csv'

const users = ref(listAdminUsers())
const keyword = ref('')
const role = ref('all')
const status = ref('all')
const page = ref(1)
const pageSize = 10
const drawerVisible = ref(false)
const activeUser = ref(null)

const currentMonth = dayjs().format('YYYY 年 M 月')

const filtered = computed(() => {
  const text = keyword.value.trim().toLowerCase()
  return users.value.filter(user => {
    const matchKeyword =
      !text ||
      String(user.username || '').toLowerCase().includes(text) ||
      String(user.nickname || '').toLowerCase().includes(text) ||
      String(user.email || '').toLowerCase().includes(text)
    const matchRole = role.value === 'all' || user.role === role.value
    const matchStatus = status.value === 'all' || user.status === status.value
    return matchKeyword && matchRole && matchStatus
  })
})

const paged = computed(() => filtered.value.slice((page.value - 1) * pageSize, page.value * pageSize))

const stats = computed(() => {
  const month = dayjs().format('YYYY-MM')
  return {
    total: users.value.length,
    thisMonth: users.value.filter(user => user.registeredAt.startsWith(month)).length,
    paid: users.value.filter(user => user.plan === '专业版' || user.plan === '团队版').length,
    banned: users.value.filter(user => user.status === 'BANNED').length
  }
})

watch([keyword, role, status], () => {
  page.value = 1
})

function resetFilters() {
  keyword.value = ''
  role.value = 'all'
  status.value = 'all'
}

function onStatusChange(row) {
  saveUsers(users.value)
  ElMessage.success(`${row.nickname} 已${row.status === 'ACTIVE' ? '启用' : '停用'}`)
}

function openDetail(row) {
  activeUser.value = row
  drawerVisible.value = true
}

async function resetPassword(row) {
  try {
    await ElMessageBox.confirm(
      `将为 ${row.nickname} 生成临时密码并发送到 ${row.email}，确定继续吗？`,
      '重置密码',
      {
        confirmButtonText: '重置',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    ElMessage.success('临时密码已发送到用户邮箱')
  } catch (error) {
    // 用户取消操作
  }
}

function exportCsv() {
  downloadCsv(
    `用户列表-${dayjs().format('YYYYMMDD')}.csv`,
    ['ID', '用户名', '昵称', '邮箱', '角色', '套餐', '状态', '积分余额', '任务数', '注册时间', '最近活跃'],
    filtered.value.map(user => [
      user.id,
      user.username,
      user.nickname,
      user.email,
      user.role === 'ADMIN' ? '管理员' : '普通用户',
      user.plan,
      user.status === 'ACTIVE' ? '正常' : '已封禁',
      user.credits,
      user.tasks,
      user.registeredAt,
      user.lastActiveAt
    ])
  )
  ElMessage.success(`已导出 ${filtered.value.length} 条用户数据`)
}
</script>

<style scoped>
.time-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
  line-height: 1.45;
}

.drawer-user {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 4px 0 18px;
  border-bottom: 1px solid var(--app-border);
}

.drawer-user .avatar.big {
  width: 46px;
  height: 46px;
  font-size: 17px;
}

.drawer-user-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.drawer-user-main strong {
  font-size: 15px;
}

.drawer-user-main span {
  color: var(--app-muted);
  font-size: 11.5px;
}
</style>
