<template>
  <div class="maintenance-container">
    <div class="maintenance-card">
      <div class="icon">
        <svg viewBox="0 0 100 100" class="svg-icon">
          <circle cx="50" cy="50" r="40" fill="#f5f5f5" stroke="#409EFF" stroke-width="2"/>
          <path d="M50 25 L50 45" stroke="#409EFF" stroke-width="4" stroke-linecap="round"/>
          <circle cx="50" cy="55" r="8" fill="#409EFF"/>
          <path d="M35 70 L65 70" stroke="#409EFF" stroke-width="4" stroke-linecap="round"/>
        </svg>
      </div>
      <h1 class="title">{{ title }}</h1>
      <p class="description">{{ description }}</p>
      <div class="status-code">{{ statusCode }}</div>
      <button class="retry-btn" @click="handleRetry">
        <span class="retry-icon">🔄</span>
        重新加载
      </button>
      <p class="tip">我们正在紧急处理，请稍后再试</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const props = defineProps({
  title: {
    type: String,
    default: '服务维护中'
  },
  description: {
    type: String,
    default: '当前服务暂时不可用，请稍后重试'
  },
  statusCode: {
    type: String,
    default: '503'
  }
})

const handleRetry = () => {
  window.location.reload()
}

onMounted(() => {
  console.log('Maintenance page loaded')
})
</script>

<style scoped>
.maintenance-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.maintenance-card {
  background: #ffffff;
  border-radius: 20px;
  padding: 60px 40px;
  text-align: center;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
  max-width: 480px;
  width: 90%;
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.icon {
  width: 100px;
  height: 100px;
  margin: 0 auto 30px;
}

.svg-icon {
  width: 100%;
  height: 100%;
}

.title {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px;
}

.description {
  font-size: 15px;
  color: #606266;
  line-height: 1.6;
  margin: 0 0 20px;
}

.status-code {
  font-size: 64px;
  font-weight: 700;
  color: #409EFF;
  margin: 20px 0;
  text-shadow: 0 2px 10px rgba(64, 158, 255, 0.2);
}

.retry-btn {
  background: linear-gradient(135deg, #409EFF 0%, #667eea 100%);
  color: #ffffff;
  border: none;
  border-radius: 25px;
  padding: 12px 36px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s ease;
  margin: 20px 0;
}

.retry-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(64, 158, 255, 0.4);
}

.retry-btn:active {
  transform: translateY(0);
}

.retry-icon {
  font-size: 18px;
}

.tip {
  font-size: 13px;
  color: #909399;
  margin: 0;
}
</style>
