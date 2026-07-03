<template>
  <div class="permission-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>权限管理</span>
          <el-button type="primary" @click="handleAdd">添加权限</el-button>
        </div>
      </template>
      
      <el-table :data="tableData" border stripe row-key="id">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="permissionName" label="权限名称" />
        <el-table-column prop="permissionKey" label="权限标识" />
        <el-table-column prop="resourceType" label="资源类型" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.resourceType === 'menu'">菜单</el-tag>
            <el-tag v-else-if="row.resourceType === 'button'" type="success">按钮</el-tag>
            <el-tag v-else type="warning">接口</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路径" />
        <el-table-column prop="component" label="组件" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="权限名称" prop="permissionName">
          <el-input v-model="form.permissionName" />
        </el-form-item>
        <el-form-item label="权限标识" prop="permissionKey">
          <el-input v-model="form.permissionKey" />
        </el-form-item>
        <el-form-item label="资源类型" prop="resourceType">
          <el-select v-model="form.resourceType" style="width: 100%">
            <el-option label="菜单" value="menu" />
            <el-option label="按钮" value="button" />
            <el-option label="接口" value="api" />
          </el-select>
        </el-form-item>
        <el-form-item label="路径" prop="path">
          <el-input v-model="form.path" />
        </el-form-item>
        <el-form-item label="组件" prop="component">
          <el-input v-model="form.component" />
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
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPermissionPage, createPermission, updatePermission, deletePermission } from '@/api/permission'

const tableData = ref([])
const dialogVisible = ref(false)
const formRef = ref()

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const form = reactive({
  id: null,
  permissionName: '',
  permissionKey: '',
  resourceType: 'menu',
  path: '',
  component: '',
  status: 1
})

const dialogTitle = computed(() => form.id ? '编辑权限' : '添加权限')

const rules = {
  permissionName: [{ required: true, message: '请输入权限名称', trigger: 'blur' }],
  permissionKey: [{ required: true, message: '请输入权限标识', trigger: 'blur' }]
}

const loadData = async () => {
  try {
    const res = await getPermissionPage(pagination)
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    ElMessage.error('加载数据失败')
  }
}

const handleAdd = () => {
  Object.assign(form, {
    id: null,
    permissionName: '',
    permissionKey: '',
    resourceType: 'menu',
    path: '',
    component: '',
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, {
    id: row.id,
    permissionName: row.permissionName,
    permissionKey: row.permissionKey,
    resourceType: row.resourceType,
    path: row.path,
    component: row.component,
    status: row.status
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (form.id) {
          await updatePermission(form)
          ElMessage.success('更新成功')
        } else {
          await createPermission(form)
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
    await ElMessageBox.confirm('确定要删除该权限吗？', '提示', {
      type: 'warning'
    })
    await deletePermission(id)
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
.permission-management {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
