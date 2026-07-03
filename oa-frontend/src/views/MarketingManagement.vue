<template>
  <div class="marketing-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>营销推广</span>
          <el-button type="primary" @click="handleAdd">添加活动</el-button>
        </div>
      </template>

      <el-table :data="tableData" border v-loading="loading">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="name" label="活动名称" min-width="150" />
        <el-table-column prop="type" label="活动类型" width="120">
          <template #default="scope">
            <el-tag :type="getTypeTag(getTypeName(scope.row.type))">{{ getTypeName(scope.row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="活动描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="startTime" label="开始时间" width="160" />
        <el-table-column prop="endTime" label="结束时间" width="160" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createBy" label="创建人" width="100" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleView(scope.row)">查看</el-button>
            <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="empty-state">
            <el-icon class="empty-icon" :size="48">
              <Present />
            </el-icon>
            <p>暂无活动数据</p>
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
        <el-form-item label="活动名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="活动类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择活动类型">
            <el-option label="折扣" value="discount" />
            <el-option label="秒杀" value="flash" />
            <el-option label="团购" value="group" />
            <el-option label="优惠券" value="coupon" />
          </el-select>
        </el-form-item>
        <el-form-item label="活动描述" prop="description">
          <el-input v-model="form.description" type="textarea" rows="3" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择结束时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="活动详情" width="600px">
      <el-descriptions :column="2" :border="true">
        <el-descriptions-item label="活动名称">{{ selectedMarketing?.name }}</el-descriptions-item>
        <el-descriptions-item label="活动类型">{{ getTypeName(selectedMarketing?.type) }}</el-descriptions-item>
        <el-descriptions-item label="活动描述" :span="2">{{ selectedMarketing?.description }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ selectedMarketing?.startTime }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ selectedMarketing?.endTime }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="selectedMarketing?.status === 1 ? 'success' : 'info'">
            {{ selectedMarketing?.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建人">{{ selectedMarketing?.createBy }}</el-descriptions-item>
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
import { Present } from '@element-plus/icons-vue'
import { getMarketingList, createMarketing, updateMarketing, deleteMarketing } from '@/api/marketing'

const tableData = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const formRef = ref()
const selectedMarketing = ref({})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const form = reactive({
  id: null,
  name: '',
  type: '',
  description: '',
  startTime: '',
  endTime: '',
  status: 1
})

const dialogTitle = computed(() => form.id ? '编辑活动' : '添加活动')

const rules = {
  name: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择活动类型', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

const typeMap = {
  discount: '折扣',
  flash: '秒杀',
  group: '团购',
  coupon: '优惠券'
}

const getTypeName = (type) => {
  return typeMap[type] || type
}

const getTypeTag = (type) => {
  const tags = { '折扣': 'warning', '秒杀': 'success', '团购': 'info', '优惠券': 'danger' }
  return tags[type] || 'info'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getMarketingList({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    })
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
    name: '',
    type: '',
    description: '',
    startTime: '',
    endTime: '',
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, {
    id: row.id,
    name: row.name,
    type: row.type,
    description: row.description,
    startTime: row.startTime,
    endTime: row.endTime,
    status: row.status
  })
  dialogVisible.value = true
}

const handleView = (row) => {
  selectedMarketing.value = row
  detailVisible.value = true
}

const handleSubmit = async () => {
  try {
    if (form.id) {
      await updateMarketing(form)
      ElMessage.success('更新成功')
    } else {
      await createMarketing(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除该活动吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteMarketing(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.marketing-management {
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
