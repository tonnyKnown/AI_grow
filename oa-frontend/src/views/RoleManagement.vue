<template>
  <div class="role-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>角色管理</span>
          <el-button type="primary" @click="handleAdd">添加角色</el-button>
        </div>
      </template>
      
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="roleName" label="角色名称" />
        <el-table-column prop="roleKey" label="角色标识" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="300">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" size="small" @click="handleAssignPermissions(row)">分配权限</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="empty-state">
            <el-icon class="empty-icon" :size="48">
              <UserFilled />
            </el-icon>
            <p>暂无角色数据</p>
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
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" />
        </el-form-item>
        <el-form-item label="角色标识" prop="roleKey">
          <el-input v-model="form.roleKey" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" rows="3" />
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

    <el-dialog v-model="permissionDialogVisible" title="分配权限" width="700px">
      <el-tree
        ref="permissionTreeRef"
        :data="permissionTree"
        :props="treeProps"
        show-checkbox
        node-key="id"
        default-expand-all
      />
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSavePermissions">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'
import { getRolePage, createRole, updateRole, deleteRole, getPermissionTree, getRolePermissionIds, assignPermissions } from '@/api/role'

const tableData = ref([])
const dialogVisible = ref(false)
const permissionDialogVisible = ref(false)
const formRef = ref()
const permissionTreeRef = ref()
const loading = ref(false)

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const form = reactive({
  id: null,
  roleName: '',
  roleKey: '',
  description: '',
  status: 1
})

const permissionTree = ref([])
const currentRoleId = ref(null)

const treeProps = {
  label: 'permissionName',
  children: 'children'
}

const dialogTitle = computed(() => form.id ? '编辑角色' : '添加角色')

const rules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleKey: [{ required: true, message: '请输入角色标识', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getRolePage(pagination)
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const loadPermissionTree = async () => {
  try {
    const res = await getPermissionTree()
    permissionTree.value = res.data
  } catch (error) {
    ElMessage.error('加载权限树失败')
  }
}

const handleAdd = () => {
  Object.assign(form, {
    id: null,
    roleName: '',
    roleKey: '',
    description: '',
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, {
    id: row.id,
    roleName: row.roleName,
    roleKey: row.roleKey,
    description: row.description,
    status: row.status
  })
  dialogVisible.value = true
}

const handleAssignPermissions = async (row) => {
  currentRoleId.value = row.id
  permissionDialogVisible.value = true
  
  await loadPermissionTree()
  
  try {
    const res = await getRolePermissionIds(row.id)
    const permissionIds = res.data || []
    await nextTick()
    permissionTreeRef.value.setCheckedKeys(permissionIds)
  } catch (error) {
    ElMessage.error('加载角色权限失败')
  }
}

const handleSavePermissions = async () => {
  if (!currentRoleId.value) {
    ElMessage.error('请先选择角色')
    return
  }
  
  const checkedKeys = permissionTreeRef.value.getCheckedKeys()
  try {
    await assignPermissions(currentRoleId.value, checkedKeys)
    ElMessage.success('权限分配成功')
    permissionDialogVisible.value = false
  } catch (error) {
    ElMessage.error('权限分配失败')
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (form.id) {
          await updateRole(form)
          ElMessage.success('更新成功')
        } else {
          await createRole(form)
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
    await ElMessageBox.confirm('确定要删除该角色吗？', '提示', {
      type: 'warning'
    })
    await deleteRole(id)
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
.role-management {
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
