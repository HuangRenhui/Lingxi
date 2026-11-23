import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import App from './App.vue'
import './mock'
import router from './router'
import store from './store'
import './styles/index.scss'

const app = createApp(App)
for (const [name, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(name, component)
}
// 组件渲染/生命周期异常不应只停留在控制台，至少统一记录，便于接入监控平台
app.config.errorHandler = (error, instance, info) => {
  console.error('[Vue error]', info, error)
}
app.use(store)
app.use(router)
app.use(ElementPlus)
app.mount('#app')
