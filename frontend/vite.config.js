import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
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
    // element-plus 已独立成 chunk（约 1MB），按需引入前先放宽告警阈值，避免每次构建刷噪音
    chunkSizeWarningLimit: 1200,
    rollupOptions: {
      output: {
        // 框架与组件库单独成 chunk，避免主包过大并提升长期缓存命中率
        manualChunks: {
          vue: ['vue', 'vue-router', 'vuex'],
          'element-plus': ['element-plus']
        }
      }
    }
  }
})
