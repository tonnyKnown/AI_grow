<template>
  <div class="logistics-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>物流管理</span>
        </div>
      </template>

      <!-- 搜索 -->
      <el-form :model="searchForm" inline size="small" style="margin-bottom: 16px">
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable @keyup.enter="loadData" />
        </el-form-item>
        <el-form-item label="快递公司">
          <el-select v-model="searchForm.expressCompany" placeholder="请选择" clearable style="width: 140px">
            <el-option v-for="c in expressCompanies" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="物流状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="已揽收" :value="0" />
            <el-option label="运输中" :value="1" />
            <el-option label="派送中" :value="2" />
            <el-option label="已签收" :value="3" />
            <el-option label="异常" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 物流列表 -->
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="orderNo" label="订单号" width="200" />
        <el-table-column prop="expressCompany" label="快递公司" width="120" />
        <el-table-column prop="expressNo" label="运单号" width="180" />
        <el-table-column prop="status" label="物流状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="收件人" width="120">
          <template #default="{ row }">
            <span>{{ row.senderName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发货时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleViewTracking(row)">查看轨迹</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="empty-state">
            <el-icon class="empty-icon" :size="48"><Van /></el-icon>
            <p>暂无物流数据</p>
          </div>
        </template>
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
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>

    <!-- 物流轨迹弹窗（使用共享组件） -->
    <ExpressTrackingDialog v-model="trackingVisible" :expressData="currentTracking" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Van } from '@element-plus/icons-vue'
import { getExpressList, deleteExpress } from '@/api/express'
import ExpressTrackingDialog from '@/views/ExpressTrackingDialog.vue'

const expressCompanies = ['顺丰速运', '圆通速递', '中通快递', '韵达快递', '京东快递', '中国邮政', '德邦快递', '极兔速递', '申通快递']

const tableData = ref([])
const loading = ref(false)
const trackingVisible = ref(false)
const currentTracking = ref(null)

const searchForm = reactive({
  orderNo: '',
  expressCompany: '',
  status: null
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const getStatusType = (status) => {
  const types = { 0: 'warning', 1: 'primary', 2: '', 3: 'success', 4: 'danger' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 0: '已揽收', 1: '运输中', 2: '派送中', 3: '已签收', 4: '异常' }
  return texts[status] || '未知'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getExpressList({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      orderNo: searchForm.orderNo || undefined,
      expressCompany: searchForm.expressCompany || undefined,
      status: searchForm.status != null ? searchForm.status : undefined
    })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    ElMessage.error('加载物流数据失败')
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchForm.orderNo = ''
  searchForm.expressCompany = ''
  searchForm.status = null
  pagination.pageNum = 1
  loadData()
}

const handleViewTracking = (row) => {
  currentTracking.value = row
  trackingVisible.value = true
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该物流记录吗？', '提示', { type: 'warning' })
    await deleteExpress(id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.logistics-management {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}


.no-tracking {
  padding: 40px 0;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  color: #999;
}

.empty-icon {
  margin-bottom: 16px;
  color: #ccc;
}

.empty-state p {
  margin-bottom: 16px;
}
</style>
