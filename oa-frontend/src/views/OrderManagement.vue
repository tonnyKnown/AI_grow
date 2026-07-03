<template>
  <div class="order-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>订单管理</span>
          <el-button type="primary" @click="handleAdd">创建订单</el-button>
        </div>
      </template>
      
      <el-input
        v-model="searchKeyword"
        placeholder="请输入订单号搜索"
        style="width: 300px; margin-bottom: 20px"
        @keyup.enter="loadData"
      >
        <template #append>
          <el-button @click="loadData"><el-icon><Search /></el-icon></el-button>
        </template>
      </el-input>
      
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="orderNo" label="订单号" />
        <el-table-column prop="productName" label="商品名称" />
        <el-table-column prop="quantity" label="数量" width="100" />
        <el-table-column prop="unitPrice" label="单价" width="120">
          <template #default="{ row }">¥{{ row.unitPrice }}</template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="总金额" width="120">
          <template #default="{ row }">¥{{ row.totalAmount }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="receiverName" label="收货人" />
        <el-table-column prop="receiverPhone" label="联系电话" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" size="small" v-if="row.status === 1" @click="handleUpdateStatus(row.id, 2)">确认发货</el-button>
            <el-button type="warning" size="small" v-if="row.status === 2" @click="handleUpdateStatus(row.id, 3)">确认收货</el-button>
            <el-button type="info" size="small" @click="handleView(row)">详情</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="empty-state">
            <el-icon class="empty-icon" :size="48">
              <Document />
            </el-icon>
            <p>暂无订单数据</p>
            <el-button type="primary" @click="handleAdd">立即创建</el-button>
          </div>
        </template>
      </el-table>
      
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
    
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="650px" class="order-dialog">
      <!-- 编辑模式 -->
      <template v-if="form.id">
        <div class="edit-header">
          <div class="order-no-label">订单号：{{ currentOrder?.orderNo }}</div>
          <el-tag :type="getStatusType(currentOrder?.status)" size="large">
            {{ getStatusText(currentOrder?.status) }}
          </el-tag>
        </div>
        <el-divider />
        <el-descriptions :column="2" :border="true" size="small" class="edit-descriptions">
          <el-descriptions-item label="商品名称" :span="2">{{ currentOrder?.productName }}</el-descriptions-item>
          <el-descriptions-item label="数量">{{ currentOrder?.quantity }}</el-descriptions-item>
          <el-descriptions-item label="单价">¥{{ currentOrder?.unitPrice }}</el-descriptions-item>
          <el-descriptions-item label="总金额">¥{{ currentOrder?.totalAmount }}</el-descriptions-item>
          <el-descriptions-item label="收货人">{{ currentOrder?.receiverName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentOrder?.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{ currentOrder?.shippingAddress }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentOrder?.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
        <el-divider />
        <el-form ref="formRef" :model="form" label-width="80px" class="edit-form">
          <el-form-item label="修改状态" prop="status">
            <el-select v-model="form.status" placeholder="请选择新状态" style="width: 100%">
              <el-option label="待发货" :value="1" />
              <el-option label="已发货" :value="2" />
              <el-option label="已完成" :value="3" />
              <el-option label="已取消" :value="4" />
            </el-select>
          </el-form-item>
        </el-form>
      </template>
      <!-- 创建模式 -->
      <template v-else>
        <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
          <el-form-item label="商品" prop="productId">
            <el-select v-model="form.productId" placeholder="请选择商品" style="width: 100%">
              <el-option v-for="product in products" :key="product.id" :label="product.productName" :value="product.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="数量" prop="quantity">
            <el-input-number v-model="form.quantity" :min="1" />
          </el-form-item>
          <el-form-item label="收货人" prop="receiverName">
            <el-input v-model="form.receiverName" />
          </el-form-item>
          <el-form-item label="联系电话" prop="receiverPhone">
            <el-input v-model="form.receiverPhone" />
          </el-form-item>
          <el-form-item label="收货地址" prop="shippingAddress">
            <el-input v-model="form.shippingAddress" type="textarea" />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="form.remark" type="textarea" />
          </el-form-item>
        </el-form>
      </template>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
    
    <el-dialog v-model="detailVisible" title="订单详情" width="600px">
      <el-descriptions :column="2" :border="true">
        <el-descriptions-item label="订单号">{{ selectedOrder?.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="商品名称">{{ selectedOrder?.productName }}</el-descriptions-item>
        <el-descriptions-item label="数量">{{ selectedOrder?.quantity }}</el-descriptions-item>
        <el-descriptions-item label="单价">¥{{ selectedOrder?.unitPrice }}</el-descriptions-item>
        <el-descriptions-item label="总金额">¥{{ selectedOrder?.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ getStatusText(selectedOrder?.status) }}</el-descriptions-item>
        <el-descriptions-item label="收货人">{{ selectedOrder?.receiverName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ selectedOrder?.receiverPhone }}</el-descriptions-item>
        <el-descriptions-item label="收货地址" :span="2">{{ selectedOrder?.shippingAddress }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ selectedOrder?.remark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ selectedOrder?.createTime }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Document } from '@element-plus/icons-vue'
import { getOrderPage, createOrder, updateOrder, updateOrderStatus, deleteOrder } from '@/api/order'
import { getProductPage } from '@/api/product'

const tableData = ref([])
const dialogVisible = ref(false)
const detailVisible = ref(false)
const formRef = ref()
const searchKeyword = ref('')
const products = ref([])
const selectedOrder = ref({})
const currentOrder = ref(null)
const loading = ref(false)

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const form = reactive({
  id: null,
  productId: null,
  quantity: 1,
  receiverName: '',
  receiverPhone: '',
  shippingAddress: '',
  remark: '',
  status: null
})

const dialogTitle = computed(() => form.id ? '编辑订单' : '创建订单')

const rules = computed(() => {
  if (form.id) {
    return {
      status: [{ required: true, message: '请选择新状态', trigger: 'change' }]
    }
  }
  return {
    productId: [{ required: true, message: '请选择商品', trigger: 'change' }],
    quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }],
    receiverName: [{ required: true, message: '请输入收货人', trigger: 'blur' }],
    receiverPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
    shippingAddress: [{ required: true, message: '请输入收货地址', trigger: 'blur' }]
  }
})

const getStatusType = (status) => {
  const types = {
    1: 'warning',
    2: 'primary',
    3: 'success',
    4: 'danger'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    1: '待发货',
    2: '已发货',
    3: '已完成',
    4: '已取消'
  }
  return texts[status] || '未知'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getOrderPage({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      orderNo: searchKeyword.value
    })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const loadProducts = async () => {
  try {
    const res = await getProductPage({ pageNum: 1, pageSize: 100 })
    products.value = res.data.records
  } catch (error) {
    ElMessage.error('加载商品列表失败')
  }
}

const handleAdd = () => {
  Object.assign(form, {
    id: null,
    productId: null,
    quantity: 1,
    receiverName: '',
    receiverPhone: '',
    shippingAddress: '',
    remark: '',
    status: null
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  currentOrder.value = row
  Object.assign(form, {
    id: row.id,
    productId: null,
    quantity: 1,
    receiverName: '',
    receiverPhone: '',
    shippingAddress: '',
    remark: '',
    status: row.status
  })
  dialogVisible.value = true
}

const handleView = (row) => {
  selectedOrder.value = row
  detailVisible.value = true
}

const handleUpdateStatus = async (id, status) => {
  try {
    await updateOrderStatus(id, status)
    ElMessage.success('状态更新成功')
    loadData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (form.id) {
          await updateOrder({ id: form.id, status: form.status })
          ElMessage.success('状态更新成功')
        } else {
          await createOrder(form)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        loadData()
      } catch (error) {
        ElMessage.error('操作失败')
      }
    }
  })
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该订单吗？', '提示', {
      type: 'warning'
    })
    await deleteOrder(id)
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
  loadProducts()
})
</script>

<style scoped>
.order-management {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

:deep(.order-dialog .el-dialog__body) {
  padding-top: 16px;
}

.edit-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
}

.order-no-label {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  letter-spacing: 1px;
}

.edit-descriptions {
  margin: 16px 0;
}

:deep(.edit-descriptions .el-descriptions__label) {
  width: 90px;
}

.edit-form {
  margin-top: 16px;
}

.edit-form :deep(.el-form-item:last-child) {
  margin-bottom: 0;
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
