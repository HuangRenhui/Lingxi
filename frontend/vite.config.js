import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// 模板里 <Plus /> 这类静态图标不带 ElIcon 前缀，需要单独解析；
// 这里只在构建期读取全部图标名，实际打包仍只包含被引用的图标。
const iconNames = new Set(Object.keys(ElementPlusIconsVue))
const IconResolver = (name) => (iconNames.has(name) ? { name, from: '@element-plus/icons-vue' } : undefined)

export default defineConfig({
  plugins: [
    vue(),
    // Element Plus 的 ElMessage/ElMessageBox 等命令式 API 由源码显式 import，
    // 这里只处理组件与指令；dts 关闭避免生成无用的类型声明文件。
    AutoImport({ resolvers: [ElementPlusResolver()], dts: false }),
    Components({ resolvers: [ElementPlusResolver({ importStyle: 'css' }), IconResolver], dts: false })
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  css: {
    preprocessorOptions: {
      // Vite 5.4 支持 Sass 现代 API，避免 legacy JS API 弃用告警
      scss: { api: 'modern' }
    }
  },
  build: {
    outDir: 'dist',
    sourcemap: false,
    rollupOptions: {
      output: {
        // 仅保留框架长期缓存 chunk；Element Plus 由按需插件自动分包
        manualChunks: {
          vue: ['vue', 'vue-router', 'vuex']
        }
      }
    }
  }
})
