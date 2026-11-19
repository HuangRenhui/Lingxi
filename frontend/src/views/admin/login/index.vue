<template>
  <div class="admin-login">
    <div class="glow glow-a" />
    <div class="glow glow-b" />

    <div class="login-card">
      <div class="brand">
        <img :src="logo" alt="灵犀" />
        <span class="brand-name">灵犀</span>
        <span class="brand-tag">管理后台</span>
      </div>

      <h1>管理员登录</h1>
      <p class="sub">仅限平台管理员访问，登录后将返回管理控制台。</p>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="submit">
        <el-form-item label="管理员账号" prop="username">
          <el-input v-model="form.username" placeholder="admin" size="large" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            placeholder="请输入管理员密码"
            size="large"
            autocomplete="current-password"
          />
        </el-form-item>
        <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="submit">
          进入管理后台
        </el-button>
      </el-form>

      <div class="footer">独立管理后台 · 与用户端会话隔离</div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import logo from '@/assets/logo.svg'
import { adminLogin, login as loginApi } from '@/api/auth'
import { MOCK_ENABLED } from '@/mock'
import { setAdminSession } from '@/utils/auth'
import { ADMIN_BASE, ADMIN_HOME_PATH, ADMIN_LOGIN_PATH } from '@/config/admin'

const route = useRoute()
const router = useRouter()
const formRef = ref()
const loading = ref(false)
const form = reactive({ username: 'admin', password: '' })
const rules = {
  username: [{ required: true, message: '请输入管理员账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

function resolveRedirect() {
  const target = String(route.query.redirect || '')
  if (target.startsWith(`${ADMIN_BASE}/`) && target !== ADMIN_LOGIN_PATH) {
    return target
  }
  return ADMIN_HOME_PATH
}

async function submit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const username = form.username.trim()

    // 凭证校验放在服务端（Mock 模式下由本地演示适配器模拟），视图层不再持有明文口令
    // 直接调用登录接口，不经过用户端 store，避免覆盖普通用户的 token 与资料
    const data = MOCK_ENABLED
      ? await adminLogin({ username, password: form.password })
      : await loginApi({ username, password: form.password })
    if (data?.user?.role !== 'ADMIN') {
      ElMessage.error('该账号没有管理员权限')
      return
    }
    setAdminSession({ ...data.user, token: data.token, loginAt: dayjs().format('YYYY-MM-DD HH:mm') })

    ElMessage.success('登录成功')
    router.replace(resolveRedirect())
  } catch (error) {
    ElMessage.error(error?.message || error?.response?.data?.message || '登录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.admin-login {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px;
  overflow: hidden;
  background: #07080b;
}

.glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(120px);
  pointer-events: none;
}

.glow-a {
  width: 520px;
  height: 520px;
  top: -180px;
  left: 12%;
  background: rgba(59, 130, 246, 0.18);
}

.glow-b {
  width: 460px;
  height: 460px;
  right: 8%;
  bottom: -180px;
  background: rgba(14, 116, 144, 0.16);
}

.login-card {
  position: relative;
  width: min(420px, 100%);
  padding: 40px 38px 30px;
  border-radius: 18px;
  border: 1px solid var(--app-border-strong);
  background: rgba(13, 17, 23, 0.92);
  box-shadow: 0 30px 80px rgba(0, 0, 0, 0.55);
  backdrop-filter: blur(14px);
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  font-weight: 650;
  letter-spacing: 0.01em;
}

.brand img {
  width: 34px;
  height: 34px;
  border-radius: 10px;
}

.brand-tag {
  margin-left: 2px;
  padding: 2px 8px;
  border-radius: 999px;
  border: 1px solid rgba(96, 165, 250, 0.32);
  background: rgba(59, 130, 246, 0.12);
  color: #93c5fd;
  font-size: 10.5px;
  font-weight: 500;
  letter-spacing: 0.04em;
}

h1 {
  margin: 34px 0 6px;
  font-size: 28px;
  letter-spacing: -0.03em;
}

.sub {
  margin: 0 0 28px;
  color: var(--app-muted);
  font-size: 12.5px;
  line-height: 1.6;
}

.submit-btn {
  width: 100%;
  height: 44px;
  margin-top: 6px;
  font-weight: 600;
}

.demo-hint {
  margin-top: 22px;
  padding-top: 16px;
  border-top: 1px solid var(--app-border);
  font-size: 11.5px;
  color: var(--app-muted);
  text-align: center;
}

.footer {
  margin-top: 10px;
  font-size: 11px;
  color: #4d5560;
  text-align: center;
}
</style>
