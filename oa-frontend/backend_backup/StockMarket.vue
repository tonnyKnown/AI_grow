<template>
  <div class="stock-market">
    <el-card>
      <template #header>
        <div class="card-header">
          <div style="display: flex; align-items: center; gap: 10px;">
            <span>A股市场</span>
            <el-tag :type="autoRefresh ? 'success' : 'info'">
              <el-icon><Stopwatch /></el-icon>
              {{ autoRefresh ? '实时刷新中' : '已暂停' }}
            </el-tag>
          </div>
          <div class="header-controls">
            <el-select 
              v-model="refreshInterval" 
              placeholder="刷新间隔" 
              style="width: 120px; margin-right: 10px"
              @change="restartTimer"
            >
              <el-option label="1秒" :value="1000" />
              <el-option label="2秒" :value="2000" />
              <el-option label="5秒" :value="5000" />
              <el-option label="10秒" :value="10000" />
            </el-select>
            <el-button :type="autoRefresh ? 'warning' : 'primary'" @click="toggleAutoRefresh">
              <el-icon><Stopwatch /></el-icon>
              {{ autoRefresh ? '暂停' : '开启' }}
            </el-button>
            <el-button type="primary" @click="refreshData">
              <el-icon><Refresh /></el-icon> 刷新
            </el-button>
          </div>
        </div>
      </template>

      <!-- 大盘指数 -->
      <div class="market-index">
        <div 
          v-for="index in marketIndex" 
          :key="index.code" 
          class="index-card"
          :class="getIndexClass(index.changePercent)"
        >
          <div class="index-name">{{ index.name }}</div>
          <div class="index-price">{{ index.price.toFixed(2) }}</div>
          <div class="index-change">
            <span :class="index.changePercent >= 0 ? 'up' : 'down'">
              {{ index.changePercent >= 0 ? '+' : '' }}{{ index.change.toFixed(2) }}
              ({{ index.changePercent >= 0 ? '+' : '' }}{{ index.changePercent.toFixed(2) }}%)
            </span>
          </div>
        </div>
      </div>

      <!-- 搜索和筛选 -->
      <div class="filter-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索股票名称或代码"
          style="width: 300px; margin-right: 10px"
          clearable
          @keyup.enter="loadStockList"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select
          v-model="selectedCategory"
          placeholder="选择行业"
          clearable
          style="width: 150px; margin-right: 10px"
          @change="loadStockList"
        >
          <el-option label="全部" value="all" />
          <el-option
            v-for="cat in categories"
            :key="cat"
            :label="cat"
            :value="cat"
          />
        </el-select>
        <el-button type="primary" @click="loadStockList">搜索</el-button>
      </div>

      <!-- 股票列表 -->
      <el-table :data="stockList" border stripe style="margin-top: 20px" @row-click="handleRowClick">
        <el-table-column prop="code" label="代码" width="100" />
        <el-table-column prop="name" label="名称" width="120" />
        <el-table-column prop="category" label="行业" width="100" />
        <el-table-column prop="price" label="最新价" width="100">
          <template #default="{ row }">
            <span :class="getPriceClass(row.changePercent)">
              {{ row.price.toFixed(2) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="涨跌额" width="100">
          <template #default="{ row }">
            <span :class="getPriceClass(row.changePercent)">
              {{ row.changePercent >= 0 ? '+' : '' }}{{ row.change.toFixed(2) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="涨跌幅" width="100">
          <template #default="{ row }">
            <span :class="getPriceClass(row.changePercent)">
              {{ row.changePercent >= 0 ? '+' : '' }}{{ row.changePercent.toFixed(2) }}%
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="volume" label="成交量" width="120">
          <template #default="{ row }">
            {{ formatVolume(row.volume) }}
          </template>
        </el-table-column>
        <el-table-column prop="turnover" label="成交额" width="120">
          <template #default="{ row }">
            {{ formatTurnover(row.turnover) }}
          </template>
        </el-table-column>
        <el-table-column prop="high" label="最高" width="90">
          <template #default="{ row }">
            <span class="up">{{ row.high.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="low" label="最低" width="90">
          <template #default="{ row }">
            <span class="down">{{ row.low.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click.stop="handleViewDetail(row)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-if="pagination.total > 0"
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 20px; justify-content: flex-end"
        @size-change="loadStockList"
        @current-change="loadStockList"
      />
    </el-card>

    <!-- 股票详情弹窗 -->
    <el-dialog
      v-model="detailVisible"
      :title="`${currentStock?.name} (${currentStock?.code})`"
      width="900px"
    >
      <div v-if="currentStock" class="stock-detail">
        <div class="detail-header">
          <div class="price-section">
            <div class="current-price" :class="getPriceClass(currentStock.changePercent)">
              {{ currentStock.price.toFixed(2) }}
            </div>
            <div class="change-info">
              <div :class="getPriceClass(currentStock.changePercent)">
                <span>{{ currentStock.changePercent >= 0 ? '+' : '' }}{{ currentStock.change.toFixed(2) }}</span>
                <span style="margin-left: 10px">
                  ({{ currentStock.changePercent >= 0 ? '+' : '' }}{{ currentStock.changePercent.toFixed(2) }}%)
                </span>
              </div>
            </div>
          </div>
          <div class="info-grid">
            <div class="info-item">
              <span class="label">今开：</span>
              <span>{{ currentStock.open.toFixed(2) }}</span>
            </div>
            <div class="info-item">
              <span class="label">昨收：</span>
              <span>{{ currentStock.previousClose.toFixed(2) }}</span>
            </div>
            <div class="info-item">
              <span class="label">最高：</span>
              <span class="up">{{ currentStock.high.toFixed(2) }}</span>
            </div>
            <div class="info-item">
              <span class="label">最低：</span>
              <span class="down">{{ currentStock.low.toFixed(2) }}</span>
            </div>
            <div class="info-item">
              <span class="label">成交量：</span>
              <span>{{ formatVolume(currentStock.volume) }}</span>
            </div>
            <div class="info-item">
              <span class="label">成交额：</span>
              <span>{{ formatTurnover(currentStock.turnover) }}</span>
            </div>
          </div>
        </div>

        <el-tabs v-model="activeTab" type="border-card">
          <el-tab-pane label="K线图" name="kline">
            <div class="kline-container">
              <div class="kline-chart">
                <svg v-if="currentStock.kline" :width="chartWidth" :height="chartHeight" class="kline-svg">
                  <g v-for="(item, index) in currentStock.kline" :key="index">
                    <rect
                      :x="getKlineX(index)"
                      :y="getKlineY(item)"
                      :width="klineWidth"
                      :height="getKlineHeight(item)"
                      :fill="item.close >= item.open ? '#ef4444' : '#22c55e'"
                      stroke="rgba(0,0,0,0.2)"
                    />
                    <line
                      :x1="getKlineX(index) + klineWidth / 2"
                      :y1="getPriceY(item.high)"
                      :x2="getKlineX(index) + klineWidth / 2"
                      :y2="getPriceY(item.low)"
                      :stroke="item.close >= item.open ? '#ef4444' : '#22c55e'"
                      :stroke-width="1"
                    />
                  </g>
                </svg>
                <div class="kline-legend">
                  <span class="up">● 上涨</span>
                  <span class="down">● 下跌</span>
                </div>
              </div>
            </div>
          </el-tab-pane>
          <el-tab-pane label="基本信息" name="info">
            <el-descriptions :column="2" :border="true">
              <el-descriptions-item label="股票代码">{{ currentStock.code }}</el-descriptions-item>
              <el-descriptions-item label="股票名称">{{ currentStock.name }}</el-descriptions-item>
              <el-descriptions-item label="所属行业">{{ currentStock.category }}</el-descriptions-item>
              <el-descriptions-item label="最新价">{{ currentStock.price.toFixed(2) }}</el-descriptions-item>
              <el-descriptions-item label="涨跌幅">
                <span :class="getPriceClass(currentStock.changePercent)">
                  {{ currentStock.changePercent >= 0 ? '+' : '' }}{{ currentStock.changePercent.toFixed(2) }}%
                </span>
              </el-descriptions-item>
              <el-descriptions-item label="涨跌额">
                <span :class="getPriceClass(currentStock.changePercent)">
                  {{ currentStock.changePercent >= 0 ? '+' : '' }}{{ currentStock.change.toFixed(2) }}
                </span>
              </el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
        </el-tabs>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Stopwatch } from '@element-plus/icons-vue'
import { getStockList, getStockDetail, getStockCategories, getMarketIndex } from '@/api/stock'

const searchKeyword = ref('')
const selectedCategory = ref('all')
const categories = ref([])
const stockList = ref([])
const marketIndex = ref([])

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const detailVisible = ref(false)
const currentStock = ref(null)
const activeTab = ref('kline')

// 实时刷新相关
const autoRefresh = ref(true)
const refreshInterval = ref(1000) // 默认1秒刷新
let timer = null

const chartWidth = 800
const chartHeight = 300
const klineWidth = 15

const loadMarketIndex = async () => {
  try {
    const res = await getMarketIndex()
    marketIndex.value = res.data
  } catch (error) {
    ElMessage.error('加载大盘指数失败')
  }
}

const loadCategories = async () => {
  try {
    const res = await getStockCategories()
    categories.value = res.data
  } catch (error) {
    ElMessage.error('加载分类失败')
  }
}

const loadStockList = async () => {
  try {
    const res = await getStockList({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: searchKeyword.value,
      category: selectedCategory.value,
      sortField: 'changePercent',
      sortOrder: 'desc'
    })
    stockList.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    ElMessage.error('加载股票列表失败')
  }
}

const handleViewDetail = async (stock) => {
  try {
    const res = await getStockDetail(stock.code)
    currentStock.value = res.data
    detailVisible.value = true
  } catch (error) {
    ElMessage.error('加载股票详情失败')
  }
}

const handleRowClick = (row) => {
  handleViewDetail(row)
}

const refreshData = () => {
  loadMarketIndex()
  loadStockList()
  // 如果有打开的详情，也更新详情
  if (detailVisible.value && currentStock.value) {
    handleViewDetail(currentStock.value)
  }
}

const startTimer = () => {
  if (timer) return
  timer = setInterval(() => {
    refreshData()
  }, refreshInterval.value)
}

const stopTimer = () => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

const restartTimer = () => {
  stopTimer()
  if (autoRefresh.value) {
    startTimer()
  }
}

const toggleAutoRefresh = () => {
  autoRefresh.value = !autoRefresh.value
  if (autoRefresh.value) {
    startTimer()
    ElMessage.success('已开启实时刷新')
  } else {
    stopTimer()
    ElMessage.warning('已暂停实时刷新')
  }
}

const getPriceClass = (changePercent) => {
  return changePercent >= 0 ? 'up' : 'down'
}

const getIndexClass = (changePercent) => {
  return changePercent >= 0 ? 'index-up' : 'index-down'
}

const formatVolume = (volume) => {
  if (volume >= 100000000) {
    return (volume / 100000000).toFixed(2) + '亿'
  } else if (volume >= 10000) {
    return (volume / 10000).toFixed(2) + '万'
  }
  return volume
}

const formatTurnover = (turnover) => {
  if (turnover >= 100000000) {
    return (turnover / 100000000).toFixed(2) + '亿'
  } else if (turnover >= 10000) {
    return (turnover / 10000).toFixed(2) + '万'
  }
  return turnover
}

const priceRange = computed(() => {
  if (!currentStock.value?.kline) return { min: 0, max: 100 }
  const prices = currentStock.value.kline.flatMap(k => [k.high, k.low])
  const min = Math.min(...prices)
  const max = Math.max(...prices)
  const padding = (max - min) * 0.1
  return { min: min - padding, max: max + padding }
})

const getKlineX = (index) => {
  return 50 + index * (klineWidth + 3)
}

const getPriceY = (price) => {
  const range = priceRange.value
  const ratio = (price - range.min) / (range.max - range.min)
  return 30 + (1 - ratio) * (chartHeight - 60)
}

const getKlineY = (item) => {
  return getPriceY(Math.max(item.open, item.close))
}

const getKlineHeight = (item) => {
  const top = getPriceY(Math.max(item.open, item.close))
  const bottom = getPriceY(Math.min(item.open, item.close))
  return Math.max(bottom - top, 1)
}

onMounted(() => {
  loadMarketIndex()
  loadCategories()
  loadStockList()
  // 启动自动刷新
  if (autoRefresh.value) {
    startTimer()
  }
})

onUnmounted(() => {
  // 组件卸载时清理定时器
  stopTimer()
})
</script>

<style scoped>
.stock-market {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-controls {
  display: flex;
  align-items: center;
}

.market-index {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.index-card {
  flex: 1;
  min-width: 250px;
  padding: 20px;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  text-align: center;
}

.index-up {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.index-down {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.index-name {
  font-size: 16px;
  margin-bottom: 10px;
  opacity: 0.9;
}

.index-price {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 8px;
}

.index-change {
  font-size: 14px;
}

.filter-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.up {
  color: #ef4444;
}

.down {
  color: #22c55e;
}

.stock-detail {
  padding: 10px;
}

.detail-header {
  display: flex;
  gap: 30px;
  margin-bottom: 20px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

.price-section {
  flex-shrink: 0;
}

.current-price {
  font-size: 48px;
  font-weight: bold;
  line-height: 1.2;
}

.change-info {
  margin-top: 10px;
  font-size: 18px;
}

.info-grid {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
}

.info-item {
  display: flex;
  flex-direction: column;
}

.info-item .label {
  color: #909399;
  font-size: 12px;
  margin-bottom: 5px;
}

.kline-container {
  padding: 20px 0;
}

.kline-chart {
  position: relative;
  overflow-x: auto;
  padding-bottom: 10px;
}

.kline-svg {
  display: block;
}

.kline-legend {
  display: flex;
  gap: 20px;
  justify-content: center;
  margin-top: 10px;
}
</style>
