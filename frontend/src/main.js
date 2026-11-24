import { createApp } from 'vue'
import 'element-plus/theme-chalk/dark/css-vars.css'
// 命令式 API 不在组件模板里，按需插件扫描不到样式，这里显式引入
import 'element-plus/theme-chalk/el-message.css'
import 'element-plus/theme-chalk/el-message-box.css'
import 'element-plus/theme-chalk/el-notification.css'
import App from './App.vue'
import router from './router'
import store from './store'
import { registerIcons } from './plugins/icons'
import './styles/index.scss'

async function bootstrap() {
  // Mock 只在开发环境或显式打开时加载；生产包不再包含演示数据与演示令牌逻辑。
  if (import.meta.env.DEV || import.meta.env.VITE_MOCK === 'true') {
    await import('./mock')
  }
  const app = createApp(App)
  // 组件渲染/生命周期异常不应只停留在控制台，至少统一记录，便于接入监控平台
  app.config.errorHandler = (error, instance, info) => {
    console.error('[Vue error]', info, error)
  }
  // 只注册源码中实际用到的图标（含动态 :is 场景）
  registerIcons(app)
  app.use(store)
  app.use(router)
  app.mount('#app')
}

bootstrap()
