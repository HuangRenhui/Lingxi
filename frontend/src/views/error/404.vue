<template>
  <div class="not-found">
    <h1>404</h1>
    <p>页面不存在或已被移动</p>
    <el-button type="primary" @click="router.push('/home')">返回首页</el-button>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<style scoped>
.not-found {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  background: var(--app-bg);
}

.not-found h1 {
  margin: 0;
  font-size: 76px;
  letter-spacing: -0.05em;
  background: linear-gradient(90deg, #60a5fa, #22d3ee);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.not-found p {
  margin: 0 0 12px;
  color: var(--app-muted);
  font-size: 13px;
}
</style>
