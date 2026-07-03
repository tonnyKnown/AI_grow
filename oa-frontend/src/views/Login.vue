<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-left">
        <div class="left-content">
          <div class="logo-section">
            <div class="logo-icon">OA</div>
            <h1>OA 管理系统</h1>
            <p class="subtitle">高效 · 便捷 · 智能</p>
          </div>
          <div class="features">
            <div class="feature-item">
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </div>
            <div class="feature-item">
              <el-icon><Key /></el-icon>
              <span>权限控制</span>
            </div>
            <div class="feature-item">
              <el-icon><DataAnalysis /></el-icon>
              <span>数据可视化</span>
            </div>
          </div>
          <div class="footer-text">
            <p>© 2026 OA System</p>
          </div>
        </div>
      </div>
      <div class="login-right">
        <div class="form-container">
          <div class="form-header">
            <h2>欢迎登录</h2>
            <p class="welcome-text">请输入您的账号信息</p>
          </div>
          <el-form ref="loginFormRef" :model="loginForm" :rules="rules" label-width="0">
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名"
                size="large"
                :prefix-icon="User"
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                :prefix-icon="Lock"
                @keyup.enter="handleLogin"
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                :loading="loading"
                style="width: 100%"
                @click="handleLogin"
              >
                登 录
              </el-button>
            </el-form-item>
          </el-form>
          <div class="form-footer">
            <span class="forgot-link">忘记密码？</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { User, Lock, Key, DataAnalysis } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref()
const loading = ref(false)

const loginForm = reactive({
  username: 'admin',
  password: '123456'
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await userStore.login(loginForm)
        ElMessage.success('登录成功')
        router.push('/')
      } catch (error) {
        ElMessage.error('登录失败')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  display: flex;
  width: 900px;
  height: 550px;
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  overflow: hidden;
}

.login-left,
.login-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 50px;
}

.login-left {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.left-content {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 40px;
}

.logo-section {
  text-align: center;
}

.logo-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 20px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
}

.logo-section h1 {
  font-size: 32px;
  margin-bottom: 12px;
  font-weight: 600;
}

.subtitle {
  font-size: 16px;
  opacity: 0.85;
}

.features {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 14px;
  font-size: 15px;
}

.feature-item .el-icon {
  width: 28px;
  height: 28px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.footer-text {
  text-align: center;
  opacity: 0.7;
  font-size: 13px;
}

.login-right {
  background: #fafafa;
}

.form-container {
  width: 100%;
  max-width: 300px;
  display: flex;
  flex-direction: column;
  height: 100%;
  justify-content: center;
  gap: 30px;
}

.form-header {
  text-align: center;
}

.form-header h2 {
  font-size: 26px;
  color: #333;
  margin-bottom: 8px;
  font-weight: 600;
}

.welcome-text {
  color: #888;
  font-size: 13px;
}

:deep(.el-form-item) {
  margin-bottom: 22px;
}

:deep(.el-input__wrapper) {
  padding: 12px 15px;
  border-radius: 10px;
  background: white;
  border: 1px solid #e8e8e8;
}

:deep(.el-input__inner) {
  font-size: 14px;
}

:deep(.el-button--primary) {
  border-radius: 10px;
  font-size: 15px;
  padding: 13px 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.form-footer {
  text-align: center;
}

.forgot-link {
  font-size: 13px;
  color: #667eea;
  cursor: pointer;
}

.forgot-link:hover {
  text-decoration: underline;
}
</style>
