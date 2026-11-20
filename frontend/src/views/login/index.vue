<template>
  <div class="auth-page">
    <div class="glow glow-a" />
    <div class="glow glow-b" />

    <div class="auth-card">
      <div class="brand">
        <img :src="logo" alt="灵犀" />
        <span>灵犀</span>
      </div>

      <template v-if="mode === 'login'">
        <h1>使用电子邮件登录</h1>

        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="auth-form"
          @keyup.enter="submitLogin"
        >
          <el-form-item prop="email">
            <el-input v-model="loginForm.email" placeholder="电子邮件" size="large" autocomplete="email" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="密码"
              size="large"
              show-password
              autocomplete="current-password"
            />
          </el-form-item>
        </el-form>

        <button type="button" class="primary-btn" :disabled="loading" @click="submitLogin">
          {{ loading ? '登录中…' : '登录' }}
        </button>
        <button type="button" class="text-link forgot" @click="onForgot">忘记密码？</button>

        <div class="divider"><span>其他登录方式</span></div>

        <button type="button" class="oauth-btn" @click="unsupported('手机号')">
          <el-icon><Iphone /></el-icon>
          使用手机号登录
        </button>
        <button type="button" class="oauth-btn" @click="unsupported('GitHub')">
          <svg class="oauth-icon" viewBox="0 0 16 16" aria-hidden="true">
            <path
              fill="currentColor"
              d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82a7.4 7.4 0 0 1 2-.27c.68 0 1.36.09 2 .27 1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.01 8.01 0 0 0 16 8c0-4.42-3.58-8-8-8Z"
            />
          </svg>
          使用GitHub登录
        </button>
        <button type="button" class="oauth-btn" @click="unsupported('谷歌')">
          <svg class="oauth-icon" viewBox="0 0 24 24" aria-hidden="true">
            <path
              fill="currentColor"
              d="M21.6 12.23c0-.68-.06-1.33-.17-1.96H12v3.71h5.38a4.6 4.6 0 0 1-2 3.02v2.51h3.24c1.9-1.75 2.98-4.32 2.98-7.28Z"
            />
            <path
              fill="currentColor"
              opacity=".72"
              d="M12 22c2.7 0 4.96-.9 6.62-2.44l-3.24-2.51c-.9.6-2.05.96-3.38.96-2.6 0-4.8-1.76-5.59-4.12H3.06v2.59A10 10 0 0 0 12 22Z"
            />
            <path
              fill="currentColor"
              opacity=".5"
              d="M6.41 13.89A6 6 0 0 1 6.1 12c0-.66.11-1.3.31-1.89V7.52H3.06A10 10 0 0 0 2 12c0 1.61.39 3.14 1.06 4.48l3.35-2.59Z"
            />
            <path
              fill="currentColor"
              opacity=".85"
              d="M12 5.99c1.47 0 2.79.51 3.83 1.5l2.87-2.87A9.63 9.63 0 0 0 12 2a10 10 0 0 0-8.94 5.52l3.35 2.59C7.2 7.75 9.4 5.99 12 5.99Z"
            />
          </svg>
          使用谷歌登录
        </button>
        <button type="button" class="oauth-btn" @click="unsupported('Apple')">
          <svg class="oauth-icon" viewBox="0 0 24 24" aria-hidden="true">
            <path
              fill="currentColor"
              d="M16.36 12.63c.02-2.02 1.65-2.99 1.72-3.04-.94-1.37-2.4-1.56-2.92-1.58-1.24-.13-2.42.73-3.05.73-.63 0-1.6-.71-2.63-.69-1.35.02-2.6.79-3.3 2-1.4 2.43-.36 6.03 1 8 .66.96 1.45 2.04 2.48 2 1-.04 1.38-.65 2.59-.65s1.55.65 2.6.63c1.08-.02 1.76-.98 2.42-1.94.76-1.11 1.07-2.18 1.09-2.24-.02-.01-2.09-.8-2.1-3.22ZM14.4 6.6c.55-.67.92-1.6.82-2.53-.79.03-1.75.53-2.32 1.2-.51.59-.96 1.54-.84 2.45.88.07 1.79-.45 2.34-1.12Z"
            />
          </svg>
          使用 Apple 登录
        </button>

        <div class="switch-line">
          还没有账户？
          <button type="button" class="text-link" @click="toRegister">注册</button>
        </div>
      </template>

      <template v-else>
        <h1>使用电子邮件注册</h1>

        <el-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="registerRules"
          class="auth-form"
          @keyup.enter="submitRegister"
        >
          <el-form-item prop="email">
            <el-input v-model="registerForm.email" placeholder="电子邮件" size="large" autocomplete="email" />
          </el-form-item>
          <el-form-item prop="code">
            <div class="code-row">
              <el-input
                v-model="registerForm.code"
                placeholder="电子邮件验证码"
                size="large"
                maxlength="6"
                autocomplete="one-time-code"
              />
              <button type="button" class="code-btn" :disabled="countdown > 0 || sending" @click="sendCode">
                {{ countdown > 0 ? `${countdown}s 后重发` : sending ? '发送中…' : '发送邮件验证码' }}
              </button>
            </div>
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="密码"
              size="large"
              show-password
              autocomplete="new-password"
            />
          </el-form-item>
        </el-form>

        <p class="password-hint">
          密码需为 12–1024 个字符，不能使用常见密码、纯数字或与账号过于相似的内容。
        </p>

        <button type="button" class="primary-btn" :disabled="loading" @click="submitRegister">
          {{ loading ? '注册中…' : '注册' }}
        </button>

        <div class="switch-line">
          已经有账户了？
          <button type="button" class="text-link" @click="toLogin">登录</button>
        </div>
      </template>

      <p v-if="isMock" class="demo-tip">演示账号：demo@lingxi.local / lingxi-demo-2026</p>
    </div>
  </div>
</template>

<script setup>
import { onBeforeUnmount, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import logo from '@/assets/logo.svg'
import { MOCK_ENABLED } from '@/mock'
import { sendEmailCode } from '@/api/auth'

const route = useRoute()
const router = useRouter()
const store = useStore()

const isMock = MOCK_ENABLED
const mode = ref('login')
const loading = ref(false)
const sending = ref(false)
const countdown = ref(0)
const loginFormRef = ref()
const registerFormRef = ref()

const loginForm = reactive({ email: '', password: '' })
const registerForm = reactive({ email: '', code: '', password: '' })

let countdownTimer = null

const emailRules = [
  { required: true, message: '请输入电子邮件', trigger: 'blur' },
  { type: 'email', message: '请输入有效的电子邮件', trigger: 'blur' }
]

const loginRules = {
  email: emailRules,
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

function validatePassword(rule, value, callback) {
  if (!value) return callback()
  if (value.length < 12) return callback(new Error('密码至少 12 个字符'))
  if (/^\d+$/.test(value)) return callback(new Error('密码不能是纯数字'))
  const name = registerForm.email.split('@')[0].trim()
  if (name.length >= 4 && value.toLowerCase().includes(name.toLowerCase())) {
    return callback(new Error('密码不能与邮箱账号过于相似'))
  }
  callback()
}

const registerRules = {
  email: emailRules,
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码为 6 位数字', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { validator: validatePassword, trigger: 'blur' }
  ]
}

function startCountdown() {
  countdown.value = 60
  countdownTimer = window.setInterval(() => {
    countdown.value -= 1
    if (countdown.value <= 0) {
      window.clearInterval(countdownTimer)
      countdownTimer = null
    }
  }, 1000)
}

function goNext() {
  const raw = typeof route.query.redirect === 'string' ? route.query.redirect : ''
  // 只接受站内相对路径，拒绝 //evil.com 之类的开放重定向
  const safe = raw.startsWith('/') && !raw.startsWith('//') ? raw : '/home'
  router.replace(safe)
}

async function submitLogin() {
  const valid = await loginFormRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await store.dispatch('user/login', {
      email: loginForm.email.trim(),
      password: loginForm.password
    })
    ElMessage.success('登录成功')
    goNext()
  } catch (error) {
    // 错误提示由请求拦截器统一处理
  } finally {
    loading.value = false
  }
}

async function sendCode() {
  const email = registerForm.email.trim()
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    ElMessage.warning('请先输入有效的电子邮件')
    return
  }
  sending.value = true
  try {
    const data = await sendEmailCode({ email })
    if (data?.code) {
      ElMessage.success(`验证码已发送（演示环境，验证码：${data.code}，仅在本机生效）`)
    } else {
      ElMessage.success('验证码已发送，请查收邮件')
    }
    startCountdown()
  } catch (error) {
    // 错误提示由请求拦截器统一处理
  } finally {
    sending.value = false
  }
}

async function submitRegister() {
  const valid = await registerFormRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await store.dispatch('user/register', {
      email: registerForm.email.trim(),
      code: registerForm.code.trim(),
      password: registerForm.password
    })
    ElMessage.success('注册成功，已自动登录')
    goNext()
  } catch (error) {
    // 错误提示由请求拦截器统一处理
  } finally {
    loading.value = false
  }
}

function toRegister() {
  registerForm.email = loginForm.email
  registerForm.code = ''
  registerForm.password = ''
  mode.value = 'register'
}

function toLogin() {
  loginForm.email = registerForm.email
  loginForm.password = ''
  mode.value = 'login'
}

function onForgot() {
  ElMessage.info('演示环境暂不支持找回密码，请使用演示账号或重新注册')
}

function unsupported(name) {
  ElMessage.info(`${name}登录暂未接入，请使用电子邮件登录`)
}

onBeforeUnmount(() => {
  if (countdownTimer) window.clearInterval(countdownTimer)
})
</script>

<style scoped>
.auth-page {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 28px 20px;
  overflow: hidden;
  background:
    radial-gradient(900px 520px at 18% 0%, rgba(37, 99, 235, 0.16), transparent 62%),
    radial-gradient(760px 480px at 86% 100%, rgba(14, 116, 144, 0.14), transparent 60%),
    #07080b;
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
  background: rgba(59, 130, 246, 0.16);
}

.glow-b {
  width: 460px;
  height: 460px;
  right: 8%;
  bottom: -180px;
  background: rgba(14, 116, 144, 0.14);
}

.auth-card {
  position: relative;
  width: min(420px, 100%);
  max-height: calc(100vh - 56px);
  overflow-y: auto;
  padding: 28px 30px 24px;
  border-radius: 20px;
  background: #ffffff;
  color: #1f2937;
  box-shadow: 0 30px 80px rgba(0, 0, 0, 0.55);
  scrollbar-width: thin;
}

.brand {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #0f172a;
  font-size: 19px;
  font-weight: 700;
  letter-spacing: -0.01em;
}

.brand img {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  box-shadow: 0 8px 20px rgba(37, 99, 235, 0.22);
}

h1 {
  margin: 18px 0 20px;
  color: #111827;
  font-size: 19px;
  font-weight: 600;
  text-align: center;
  letter-spacing: -0.01em;
}

.auth-form :deep(.el-form-item) {
  margin-bottom: 16px;
}

.auth-form :deep(.el-input__wrapper) {
  min-height: 46px;
  padding: 0 14px;
  border-radius: 10px;
  background: #f7f8fa;
  box-shadow: inset 0 0 0 1px #e3e8ee;
}

.auth-form :deep(.el-input__wrapper.is-focus) {
  background: #ffffff;
  box-shadow: inset 0 0 0 1px #1f7a8c;
}

.auth-form :deep(.el-input__inner) {
  color: #111827;
  -webkit-text-fill-color: #111827;
  font-size: 14px;
}

.auth-form :deep(.el-input__inner::placeholder) {
  color: #9aa4b2;
}

.auth-form :deep(.el-input__password) {
  color: #9aa4b2;
}

.auth-form :deep(.el-form-item__error) {
  padding-top: 4px;
  font-size: 11.5px;
}

.code-row {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}

.code-row :deep(.el-input) {
  flex: 1;
  min-width: 0;
}

.code-btn {
  flex-shrink: 0;
  height: 46px;
  padding: 0 14px;
  border: 0;
  border-radius: 10px;
  background: #1f7a8c;
  color: #ffffff;
  font-size: 12.5px;
  white-space: nowrap;
  cursor: pointer;
  transition: background 0.16s ease;
}

.code-btn:hover:not(:disabled) {
  background: #186b7b;
}

.code-btn:disabled {
  background: #a9c6cc;
  cursor: not-allowed;
}

.primary-btn {
  width: 100%;
  height: 46px;
  margin-top: 4px;
  border: 0;
  border-radius: 12px;
  background: #1f7a8c;
  color: #ffffff;
  font-size: 14.5px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.16s ease;
}

.primary-btn:hover:not(:disabled) {
  background: #186b7b;
}

.primary-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.text-link {
  padding: 0;
  border: 0;
  background: transparent;
  color: #1f7a8c;
  font-size: inherit;
  cursor: pointer;
}

.text-link:hover {
  text-decoration: underline;
}

.forgot {
  display: block;
  margin-top: 12px;
  font-size: 12.5px;
}

.divider {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 22px 0 14px;
  color: #8b95a5;
  font-size: 12px;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: #e5e9ef;
}

.oauth-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  height: 42px;
  margin-bottom: 10px;
  border: 1px solid #e2e8f0;
  border-radius: 999px;
  background: #ffffff;
  color: #4b5563;
  font-size: 13.5px;
  cursor: pointer;
  transition: all 0.16s ease;
}

.oauth-btn:hover {
  border-color: #cbd5e1;
  background: #f8fafc;
  color: #1f2937;
}

.oauth-btn .el-icon {
  font-size: 16px;
}

.oauth-icon {
  width: 16px;
  height: 16px;
}

.password-hint {
  margin: 2px 0 18px;
  color: #8b95a5;
  font-size: 12px;
  line-height: 1.7;
}

.switch-line {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 18px;
  padding-top: 16px;
  border-top: 1px solid #eef2f6;
  color: #6b7280;
  font-size: 13px;
}

.demo-tip {
  margin: 14px 0 0;
  color: #9aa4b2;
  font-size: 11px;
  text-align: center;
  line-height: 1.6;
}

@media (max-height: 760px) {
  .auth-card {
    padding: 24px 26px 20px;
  }

  h1 {
    margin: 18px 0 16px;
  }
}
</style>
