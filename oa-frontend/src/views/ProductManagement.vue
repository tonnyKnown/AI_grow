<template>
  <div class="product-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>商品管理</span>
          <el-button type="primary" @click="handleAdd">添加商品</el-button>
        </div>
      </template>
      
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="productName" label="商品名称" />
        <el-table-column prop="productCode" label="商品编码" />
        <el-table-column prop="category" label="分类" />
        <el-table-column prop="price" label="价格" width="120">
          <template #default="{ row }">
            ¥{{ row.price }}
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="250">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="warning" size="small" @click="handleStock(row)">库存</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty v-if="!loading">
          <div class="empty-state">
            <el-icon class="empty-icon" :size="48">
              <Box />
            </el-icon>
            <p>暂无商品数据</p>
            <el-button type="primary" @click="handleAdd">立即添加</el-button>
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
    
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="商品名称" prop="productName">
          <el-input v-model="form.productName" />
        </el-form-item>
        <el-form-item label="商品编码" prop="productCode">
          <el-input v-model="form.productCode" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-input v-model="form.category" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="form.stock" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" rows="3" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
    
    <el-dialog v-model="stockDialogVisible" title="调整库存" width="400px">
      <el-form :model="stockForm" label-width="80px">
        <el-form-item label="商品名称">
          {{ stockForm.productName }}
        </el-form-item>
        <el-form-item label="当前库存">
          {{ stockForm.currentStock }}
        </el-form-item>
        <el-form-item label="调整数量">
          <el-input-number v-model="stockForm.quantity" :min="-stockForm.currentStock" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stockDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleStockSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Box } from '@element-plus/icons-vue'
import { getProductPage, createProduct, updateProduct, deleteProduct, updateStock } from '@/api/product'

const tableData = ref([])
const dialogVisible = ref(false)
const stockDialogVisible = ref(false)
const formRef = ref()
const loading = ref(false)

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const form = reactive({
  id: null,
  productName: '',
  productCode: '',
  category: '',
  price: 0,
  stock: 0,
  description: '',
  status: 1
})

const stockForm = reactive({
  id: null,
  productName: '',
  currentStock: 0,
  quantity: 0
})

const dialogTitle = computed(() => form.id ? '编辑商品' : '添加商品')

const rules = {
  productName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getProductPage(pagination)
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  Object.assign(form, {
    id: null,
    productName: '',
    productCode: '',
    category: '',
    price: 0,
    stock: 0,
    description: '',
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, {
    id: row.id,
    productName: row.productName,
    productCode: row.productCode,
    category: row.category,
    price: row.price,
    stock: row.stock,
    description: row.description,
    status: row.status
  })
  dialogVisible.value = true
}

const handleStock = (row) => {
  Object.assign(stockForm, {
    id: row.id,
    productName: row.productName,
    currentStock: row.stock,
    quantity: 0
  })
  stockDialogVisible.value = true
}

const handleStockSubmit = async () => {
  try {
    await updateStock(stockForm.id, stockForm.quantity)
    ElMessage.success('库存更新成功')
    stockDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('库存更新失败')
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (form.id) {
          await updateProduct(form)
          ElMessage.success('更新成功')
        } else {
          await createProduct(form)
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
    await ElMessageBox.confirm('确定要删除该商品吗？', '提示', {
      type: 'warning'
    })
    await deleteProduct(id)
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
.product-management {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
