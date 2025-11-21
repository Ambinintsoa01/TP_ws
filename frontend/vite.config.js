// frontend/vite.config.js
import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import devtools from 'vite-plugin-vue-devtools'

export default defineConfig({
  plugins: [vue(), devtools()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    proxy: {
      '/api': {
        // proxy API calls to the PHP backend running on :8000
        target: 'http://localhost:8000',
        changeOrigin: true,
        // keep the /api prefix so backend routes like /api/personnels are preserved
        // no rewrite
      }
    }
  }
})