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
      <StatCard label="管理员" :value="stats.admins" icon="UserFilled" hint="拥有后台权限" />
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
      <div v-if="loadError" class="table-empty">{{ loadError }}</div>
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
        <el-table-column label="注册时间" width="170">
          <template #default="{ row }">
            <span>{{ row.createdAt }}</span>
          </template>
        </el-table-column>
        <el-table-column label="更新时间" width="170">
          <template #default="{ row }">
            <span class="cell-sub">{{ row.updatedAt }}</span>
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
            <span>状态</span>
            <strong>{{ activeUser.status === 'ACTIVE' ? '正常' : '已封禁' }}</strong>
          </div>
          <div class="detail-item full">
            <span>注册时间</span>
            <strong>{{ activeUser.createdAt }}</strong>
          </div>
          <div class="detail-item full">
            <span>更新时间</span>
            <strong>{{ activeUser.updatedAt }}</strong>
          </div>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import PageHeader from '@/components/PageHeader.vue'
import StatCard from '@/components/StatCard.vue'
import { listAdminUsers, updateAdminUser } from '@/api/adminUser'
import { fetchAllPages } from '@/utils/pagination'
import { downloadCsv } from '@/utils/csv'

const users = ref([])
const loadError = ref('')
const keyword = ref('')
const role = ref('all')
const status = ref('all')
const page = ref(1)
const pageSize = 10
const drawerVisible = ref(false)
const activeUser = ref(null)

const currentMonth = dayjs().format('YYYY 年 M 月')

function formatTime(value) {
  return value ? dayjs(value).format('YYYY-MM-DD HH:mm') : '—'
}

/** 后端 status 为 1/0，页面沿用 ACTIVE/BANNED 以复用既有筛选与标签 */
function normalizeUser(row) {
  return {
    id: row.id,
    username: row.username || '',
    nickname: row.nickname || row.username || '未命名用户',
    role: row.role || 'USER',
    status: Number(row.status) === 1 ? 'ACTIVE' : 'BANNED',
    createdAt: formatTime(row.createdAt),
    updatedAt: formatTime(row.updatedAt)
  }
}

async function loadUsers() {
  loadError.value = ''
  try {
    const result = await fetchAllPages(params => listAdminUsers(params))
    users.value = (result ? result.list : []).map(normalizeUser)
  } catch (error) {
    users.value = []
    loadError.value = '无法加载用户列表，请检查网络或稍后重试'
  }
}

onMounted(loadUsers)

const filtered = computed(() => {
  const text = keyword.value.trim().toLowerCase()
  return users.value.filter(user => {
    const matchKeyword =
      !text ||
      String(user.username || '').toLowerCase().includes(text) ||
      String(user.nickname || '').toLowerCase().includes(text)
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
    thisMonth: users.value.filter(user => user.createdAt.startsWith(month)).length,
    admins: users.value.filter(user => user.role === 'ADMIN').length,
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

async function onStatusChange(row) {
  const next = row.status === 'ACTIVE' ? 1 : 0
  try {
    await updateAdminUser(row.id, { status: next })
    ElMessage.success(`${row.nickname} 已${next === 1 ? '启用' : '停用'}`)
  } catch (error) {
    // 写入失败时把开关恢复原状态
    row.status = next === 1 ? 'BANNED' : 'ACTIVE'
  }
}

function openDetail(row) {
  activeUser.value = row
  drawerVisible.value = true
}

async function resetPassword(row) {
  try {
    await ElMessageBox.confirm(
      `将为 ${row.nickname} 生成新的临时密码，确定继续吗？`,
      '重置密码',
      {
        confirmButtonText: '重置',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
  } catch (error) {
    return
  }
  const tempPassword = `${Math.random().toString(36).slice(-8)}Aa1!`
  try {
    await updateAdminUser(row.id, { password: tempPassword })
    await ElMessageBox.alert(`临时密码：${tempPassword}`, '重置成功', {
      confirmButtonText: '我已记下'
    })
  } catch (error) {
    // 请求层已统一提示错误
  }
}

function exportCsv() {
  downloadCsv(
    `用户列表-${dayjs().format('YYYYMMDD')}.csv`,
    ['ID', '用户名', '昵称', '角色', '状态', '注册时间', '更新时间'],
    filtered.value.map(user => [
      user.id,
      user.username,
      user.nickname,
      user.role === 'ADMIN' ? '管理员' : '普通用户',
      user.status === 'ACTIVE' ? '正常' : '已封禁',
      user.createdAt,
      user.updatedAt
    ])
  )
  ElMessage.success(`已导出 ${filtered.value.length} 条用户数据`)
}
</script>

<style scoped>
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
