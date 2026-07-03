<template>
  <div class="user-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" @click="handleAdd">添加用户</el-button>
        </div>
      </template>
      
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="真实姓名" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="角色" min-width="150">
          <template #default="{ row }">
            <el-tag v-for="role in row.roleNames" :key="role" type="info" size="small" style="margin-right: 4px;">
              {{ role }}
            </el-tag>
            <span v-if="!row.roleNames || row.roleNames.length === 0" style="color: #999;">-</span>
          </template>
        </el-table-column>
        <el-table-column label="权限" min-width="200">
          <template #default="{ row }">
            <el-tag v-for="perm in (row.permissions || []).slice(0, 3)" :key="perm" type="warning" size="small" style="margin-right: 4px;">
              {{ perm }}
            </el-tag>
            <el-tag v-if="row.permissions && row.permissions.length > 3" type="warning" size="small">
              +{{ row.permissions.length - 3 }}
            </el-tag>
            <span v-if="!row.permissions || row.permissions.length === 0" style="color: #999;">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" size="small" @click="handleAssignRoles(row)">分配角色</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="empty-state">
            <el-icon class="empty-icon" :size="48">
              <User />
            </el-icon>
            <p>暂无用户数据</p>
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
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!form.id">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="角色" prop="roleIds" v-if="!form.id">
          <el-select v-model="form.roleIds" multiple placeholder="请选择角色（必选）" style="width: 100%">
            <el-option v-for="role in roles" :key="role.id" :label="role.roleName" :value="role.id" />
          </el-select>
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

    <el-dialog v-model="roleDialogVisible" title="分配角色" width="500px">
      <el-form ref="roleFormRef">
        <el-form-item label="用户名" style="margin-bottom: 20px;">
          <el-tag type="info">{{ selectedUser.username }}</el-tag>
        </el-form-item>
        <el-form-item label="选择角色">
          <el-select v-model="selectedRoleIds" multiple placeholder="请选择角色">
            <el-option v-for="role in roles" :key="role.id" :label="role.roleName" :value="role.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitRoles">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import { getUserPage, createUser, updateUser, deleteUser, assignRoles } from '@/api/user'
import { getRoleList } from '@/api/role'

const tableData = ref([])
const dialogVisible = ref(false)
const formRef = ref()
const loading = ref(false)

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const form = reactive({
  id: null,
  username: '',
  realName: '',
  email: '',
  phone: '',
  password: '',
  status: 1,
  roleIds: []
})

const roleDialogVisible = ref(false)
const roleFormRef = ref()
const roles = ref([])
const selectedUser = reactive({
  id: null,
  username: ''
})
const selectedRoleIds = ref([])

const dialogTitle = computed(() => form.id ? '编辑用户' : '添加用户')

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱', trigger: 'blur' }],
  roleIds: [{ required: true, message: '请至少选择一个角色', trigger: 'change', type: 'array', min: 1 }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getUserPage(pagination)
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const loadRoles = async () => {
  try {
    const res = await getRoleList()
    roles.value = res.data.records || res.data
  } catch (error) {
    ElMessage.error('加载角色失败')
  }
}

const handleAdd = () => {
  Object.assign(form, {
    id: null,
    username: '',
    realName: '',
    email: '',
    phone: '',
    password: '',
    status: 1,
    roleIds: []
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, {
    id: row.id,
    username: row.username,
    realName: row.realName,
    email: row.email,
    phone: row.phone,
    password: '',
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
          await updateUser(form)
          ElMessage.success('更新成功')
        } else {
          await createUser(form)
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
    await ElMessageBox.confirm('确定要删除该用户吗？', '提示', {
      type: 'warning'
    })
    await deleteUser(id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleAssignRoles = (row) => {
  selectedUser.id = row.id
  selectedUser.username = row.username
  selectedRoleIds.value = row.roleIds || []
  roleDialogVisible.value = true
}

const handleSubmitRoles = async () => {
  try {
    await assignRoles(selectedUser.id, selectedRoleIds.value)
    ElMessage.success('角色分配成功')
    roleDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  loadData()
  loadRoles()
})
</script>

<style scoped>
.user-management {
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
