<template>
  <div class="dashboard">
    <h2>欢迎使用OA管理系统</h2>
    <el-row :gutter="20" v-loading="loading">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-icon blue">
              <el-icon size="30"><User /></el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-label">用户总数</p>
              <p class="stat-value">{{ stats.totalUsers }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-icon green">
              <el-icon size="30"><Goods /></el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-label">商品总数</p>
              <p class="stat-value">{{ stats.totalProducts }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-icon orange">
              <el-icon size="30"><Key /></el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-label">订单总数</p>
              <p class="stat-value">{{ stats.totalOrders }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-icon purple">
              <el-icon size="30"><Lock /></el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-label">营销总数</p>
              <p class="stat-value">{{ stats.totalMarketing }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Goods, Key, Lock } from '@element-plus/icons-vue'
import { getDashboardStats } from '@/api/dashboard'

const stats = ref({
  totalUsers: 0,
  totalProducts: 0,
  totalOrders: 0,
  totalMarketing: 0
})
const loading = ref(false)

const loadStats = async () => {
  loading.value = true
  try {
    const res = await getDashboardStats()
    stats.value = res.data
  } catch (error) {
    ElMessage.error('加载统计数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.dashboard h2 {
  margin-bottom: 20px;
  color: #333;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 10px;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #fff;
}

.stat-icon.blue { background: linear-gradient(135deg, #667eea, #764ba2); }
.stat-icon.green { background: linear-gradient(135deg, #11998e, #38ef7d); }
.stat-icon.orange { background: linear-gradient(135deg, #f093fb, #f5576c); }
.stat-icon.purple { background: linear-gradient(135deg, #4facfe, #00f2fe); }

.stat-info {
  flex: 1;
}

.stat-label {
  color: #999;
  font-size: 14px;
  margin-bottom: 5px;
}

.stat-value {
  color: #333;
  font-size: 28px;
  font-weight: bold;
}
</style>
