<template>
  <div class="page">
    <PageHeader title="个人资料" description="维护头像、昵称与联系方式，资料保存在本地浏览器。">
      <el-button round @click="resetForm">撤销修改</el-button>
      <el-button round type="primary" :loading="saving" @click="save">保存修改</el-button>
    </PageHeader>

    <section class="profile-grid">
      <article class="panel account-card">
        <div class="avatar-wrap">
          <img :src="form.avatar || defaultAvatar" alt="头像" />
          <button type="button" class="avatar-edit" @click="pickAvatar">
            <el-icon><Camera /></el-icon>
          </button>
          <input ref="fileInput" type="file" accept="image/*" hidden @change="onAvatarChange" />
        </div>
        <h2>{{ form.nickname || '未设置昵称' }}</h2>
        <span class="account-mail">{{ form.email || '未绑定邮箱' }}</span>
        <el-tag type="primary" effect="plain" size="small">个人账号</el-tag>

        <div class="account-meta">
          <div class="meta-row">
            <span>用户 ID</span>
            <strong class="mono">{{ form.username || '-' }}</strong>
          </div>
          <div class="meta-row">
            <span>当前套餐</span>
            <strong>{{ planName }}</strong>
          </div>
          <div class="meta-row">
            <span>注册时间</span>
            <strong>{{ registeredAt }}</strong>
          </div>
          <div class="meta-row">
            <span>最近登录</span>
            <strong>{{ lastActiveAt }}</strong>
          </div>
        </div>

        <el-button class="logout-btn" round @click="logout">
          <el-icon><SwitchButton /></el-icon>
          退出登录
        </el-button>
      </article>

      <article class="panel form-card">
        <div class="form-head">
          <h3>基本信息</h3>
          <span>昵称与头像会显示在任务与作品署名中</span>
        </div>

        <el-form :model="form" label-position="top" class="profile-form">
          <div class="form-row">
            <el-form-item label="昵称">
              <el-input v-model="form.nickname" maxlength="20" show-word-limit placeholder="请输入昵称" />
            </el-form-item>
            <el-form-item label="用户名">
              <el-input v-model="form.username" placeholder="登录用用户名" disabled />
            </el-form-item>
          </div>

          <div class="form-row">
            <el-form-item label="邮箱">
              <el-input v-model="form.email" placeholder="name@example.com" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="form.phone" placeholder="选填" />
            </el-form-item>
          </div>

          <el-form-item label="个人简介">
            <el-input
              v-model="form.bio"
              type="textarea"
              :rows="3"
              maxlength="120"
              show-word-limit
              placeholder="介绍一下你的创作方向"
            />
          </el-form-item>

          <div class="form-row">
            <el-form-item label="时区">
              <el-select v-model="form.timezone">
                <el-option label="Asia/Shanghai（UTC+8）" value="Asia/Shanghai" />
                <el-option label="Asia/Tokyo（UTC+9）" value="Asia/Tokyo" />
                <el-option label="Europe/London（UTC+0）" value="Europe/London" />
                <el-option label="America/Los_Angeles（UTC-8）" value="America/Los_Angeles" />
              </el-select>
            </el-form-item>
            <el-form-item label="界面语言">
              <el-select v-model="form.language">
                <el-option label="简体中文" value="简体中文" />
                <el-option label="English" value="English" />
              </el-select>
            </el-form-item>
          </div>
        </el-form>

        <div class="form-foot">
          <span>资料仅保存在本机浏览器，清除浏览器数据后会恢复演示默认值。</span>
        </div>
      </article>
    </section>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import PageHeader from '@/components/PageHeader.vue'
import { loadProfile, loadSubscription, saveProfile } from '@/mock/account'
import { loadPlans } from '@/mock/platform'
import { setUser } from '@/utils/auth'

const router = useRouter()
const store = useStore()
const fileInput = ref(null)
const saving = ref(false)
const defaultAvatar = '/images/avatar-default.jpg'

const source = loadProfile()
const form = reactive({
  avatar: source.avatar || '',
  nickname: source.nickname || '',
  username: source.username || '',
  email: source.email || '',
  phone: source.phone || '',
  bio: source.bio || '',
  timezone: source.timezone || 'Asia/Shanghai',
  language: source.language || '简体中文'
})

const subscription = loadSubscription()
const planName = computed(() => loadPlans().find(plan => plan.key === subscription.planKey)?.name || '体验版')
const registeredAt = dayjs().subtract(96, 'day').format('YYYY-MM-DD')
const lastActiveAt = dayjs().format('YYYY-MM-DD HH:mm')

function pickAvatar() {
  fileInput.value?.click()
}

function onAvatarChange(event) {
  const file = event.target.files?.[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    event.target.value = ''
    return
  }
  if (file.size > 1024 * 1024) {
    ElMessage.warning('头像图片请控制在 1MB 以内')
    event.target.value = ''
    return
  }
  const reader = new FileReader()
  reader.onload = () => {
    form.avatar = String(reader.result)
    ElMessage.success('头像已更新，记得保存')
  }
  reader.onerror = () => ElMessage.error('头像读取失败，请重试')
  reader.readAsDataURL(file)
  event.target.value = ''
}

function save() {
  if (!import.meta.env.DEV) {
    ElMessage.info('资料修改暂未接入后端，请联系管理员更新')
    return
  }
  if (!form.nickname.trim()) {
    ElMessage.warning('昵称不能为空')
    return
  }
  if (form.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
    ElMessage.error('邮箱格式不正确')
    return
  }
  saving.value = true
  window.setTimeout(() => {
    try {
      saveProfile({ ...form })
      const nextProfile = { ...(store.state.user.profile || {}), nickname: form.nickname, email: form.email }
      store.commit('user/SET_PROFILE', nextProfile)
      setUser(nextProfile)
      ElMessage.success('资料已保存')
    } catch (error) {
      ElMessage.error('资料保存失败，请重试')
    } finally {
      saving.value = false
    }
  }, 400)
}

function resetForm() {
  const data = loadProfile()
  Object.assign(form, {
    avatar: data.avatar || '',
    nickname: data.nickname || '',
    username: data.username || '',
    email: data.email || '',
    phone: data.phone || '',
    bio: data.bio || '',
    timezone: data.timezone || 'Asia/Shanghai',
    language: data.language || '简体中文'
  })
  ElMessage.info('已恢复为上次保存的内容')
}

function logout() {
  store.dispatch('user/logout')
  router.push('/login')
}
</script>

<style scoped>
.profile-grid {
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr);
  gap: 14px;
  align-items: start;
}

.account-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 28px 22px 22px;
}

.avatar-wrap {
  position: relative;
  width: 84px;
  height: 84px;
  margin-bottom: 6px;
}

.avatar-wrap img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(96, 165, 250, 0.35);
  background: #0b1220;
}

.avatar-edit {
  position: absolute;
  right: -2px;
  bottom: -2px;
  width: 26px;
  height: 26px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  border: 1px solid var(--app-border-strong);
  background: #161b22;
  color: #9ecbff;
  cursor: pointer;
  font-size: 12px;
}

.avatar-edit:hover {
  background: #1f2937;
}

.account-card h2 {
  margin: 2px 0 0;
  font-size: 18px;
  font-weight: 600;
}

.account-mail {
  color: var(--app-muted);
  font-size: 12px;
}

.account-meta {
  width: 100%;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--app-border);
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.meta-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  font-size: 12px;
}

.meta-row span {
  color: var(--app-muted);
}

.meta-row strong {
  font-weight: 500;
}

.logout-btn {
  width: 100%;
  margin-top: 18px;
}

.form-card {
  padding: 20px 24px 18px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.form-head h3 {
  margin: 0 0 3px;
  font-size: 14px;
  font-weight: 600;
}

.form-head span {
  color: var(--app-muted);
  font-size: 11.5px;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 18px;
}

.profile-form :deep(.el-form-item) {
  margin-bottom: 16px;
}

.profile-form :deep(.el-form-item__label) {
  padding-bottom: 4px;
  color: var(--app-muted);
  font-size: 12px;
}

.form-foot {
  padding-top: 12px;
  border-top: 1px solid var(--app-border);
  color: var(--app-muted);
  font-size: 11.5px;
}

@media (max-width: 1000px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }

  .form-row {
    grid-template-columns: 1fr;
  }
}
</style>
