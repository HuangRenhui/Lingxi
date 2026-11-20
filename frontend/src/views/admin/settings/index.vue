<template>
  <div class="admin-page">
    <PageHeader title="系统设置" description="站点基础信息与平台运行策略。" flush>
      <el-button round @click="restoreDemo">恢复演示数据</el-button>
      <el-button round type="primary" :loading="saving" @click="save">保存设置</el-button>
    </PageHeader>

    <el-form :model="form" label-position="top" class="settings-form">
      <section class="panel settings-panel">
        <header class="settings-head">
          <h3>基础信息</h3>
          <span>展示在登录页与站内通知中</span>
        </header>
        <div class="form-grid">
          <el-form-item label="站点名称">
            <el-input v-model="form.siteName" maxlength="32" />
          </el-form-item>
          <el-form-item label="客服邮箱">
            <el-input v-model="form.supportEmail" maxlength="64" />
          </el-form-item>
          <el-form-item label="站点公告" class="full">
            <el-input v-model="form.announcement" type="textarea" :rows="3" maxlength="200" show-word-limit />
          </el-form-item>
        </div>
      </section>

      <section class="panel settings-panel">
        <header class="settings-head">
          <h3>注册与额度</h3>
          <span>控制新用户能否注册以及初始额度</span>
        </header>
        <div class="form-grid">
          <el-form-item label="允许新用户注册">
            <div class="switch-line">
              <el-switch v-model="form.allowRegister" />
              <span>关闭后登录页将隐藏注册入口</span>
            </div>
          </el-form-item>
          <el-form-item label="新用户赠送积分">
            <el-input-number v-model="form.defaultCredits" :min="0" :max="1000" controls-position="right" />
          </el-form-item>
          <el-form-item label="默认任务最大轮次">
            <el-input-number v-model="form.maxRounds" :min="1" :max="100" controls-position="right" />
          </el-form-item>
          <el-form-item label="默认时区">
            <el-select v-model="form.defaultTimezone">
              <el-option v-for="zone in timezones" :key="zone.value" :label="zone.label" :value="zone.value" />
            </el-select>
          </el-form-item>
        </div>
      </section>

      <section class="panel settings-panel">
        <header class="settings-head">
          <h3>运行策略</h3>
          <span>影响任务调度与平台可用性</span>
        </header>
        <div class="form-grid">
          <el-form-item label="单用户并发任务上限">
            <el-input-number v-model="form.maxConcurrent" :min="1" :max="20" controls-position="right" />
          </el-form-item>
          <el-form-item label="维护模式">
            <div class="switch-line">
              <el-switch v-model="form.maintenance" />
              <span>开启后除管理员外暂停访问</span>
            </div>
          </el-form-item>
        </div>
        <p v-if="form.maintenance" class="maintenance-hint">
          <el-icon><Warning /></el-icon>
          维护模式已开启（演示数据，不会真正影响创作空间）。
        </p>
      </section>
    </el-form>

    <section class="panel settings-foot">
      <span>设置保存在本地浏览器，仅用于前端演示。</span>
      <el-button round type="primary" :loading="saving" @click="save">保存设置</el-button>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { loadAdminSettings, resetAdminDemoData, saveAdminSettings } from '@/mock/admin'
import { resetDemoState } from '@/mock/account'

const form = reactive(loadAdminSettings())
const saving = ref(false)

const timezones = [
  { label: '(UTC+8) 亚洲/上海', value: 'Asia/Shanghai' },
  { label: '(UTC+0) 协调世界时', value: 'UTC' },
  { label: '(UTC-8) 美洲/洛杉矶', value: 'America/Los_Angeles' },
  { label: '(UTC+0) 欧洲/伦敦', value: 'Europe/London' }
]

function save() {
  saving.value = true
  window.setTimeout(() => {
    try {
      saveAdminSettings({ ...form })
      ElMessage.success('设置已保存')
    } catch (error) {
      ElMessage.error('设置保存失败，请重试')
    } finally {
      saving.value = false
    }
  }, 300)
}

async function restoreDemo() {
  try {
    await ElMessageBox.confirm('将清空本地修改并恢复全部演示数据，确定继续吗？', '恢复演示数据', {
      confirmButtonText: '恢复',
      cancelButtonText: '取消',
      type: 'warning'
    })
    resetAdminDemoData()
    resetDemoState()
    ElMessage.success('演示数据已恢复，页面即将刷新')
    setTimeout(() => window.location.reload(), 600)
  } catch (error) {
    // 用户取消操作
  }
}
</script>

<style scoped>
.settings-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.settings-panel {
  padding: 18px 20px 8px;
}

.settings-head {
  display: flex;
  align-items: baseline;
  gap: 10px;
  padding-bottom: 14px;
  margin-bottom: 14px;
  border-bottom: 1px solid var(--app-border);
}

.settings-head h3 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
}

.settings-head span {
  color: var(--app-muted);
  font-size: 11.5px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 2px 20px;
}

.form-grid .full {
  grid-column: 1 / -1;
}

:deep(.el-form-item) {
  margin-bottom: 16px;
}

.switch-line {
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 32px;
}

.switch-line span {
  color: var(--app-muted);
  font-size: 11.5px;
}

.maintenance-hint {
  display: flex;
  align-items: center;
  gap: 7px;
  margin: 0 0 14px;
  padding: 9px 12px;
  border-radius: 9px;
  border: 1px solid rgba(245, 158, 11, 0.26);
  background: rgba(245, 158, 11, 0.08);
  color: #fbbf24;
  font-size: 12px;
}

.settings-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 20px;
}

.settings-foot span {
  color: var(--app-muted);
  font-size: 11.5px;
}

@media (max-width: 720px) {
  .form-grid {
    grid-template-columns: 1fr;
  }

  .settings-foot {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
