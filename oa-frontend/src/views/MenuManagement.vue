<template>
  <div class="menu-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>菜单管理</span>
          <el-button type="primary" @click="handleAdd">添加菜单</el-button>
        </div>
      </template>

      <el-table :data="treeData" border stripe row-key="id" default-expand-all v-loading="loading">
        <el-table-column prop="menuName" label="菜单名称" />
        <el-table-column prop="path" label="路径" />
        <el-table-column prop="component" label="组件" />
        <el-table-column prop="icon" label="图标" width="100">
          <template #default="{ row }">
            <el-icon v-if="row.icon">
              <component :is="row.icon" />
            </el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="orderNum" label="排序" width="80" />
        <el-table-column prop="roleKeys" label="角色标识" />
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
        <template #empty>
          <div class="empty-state">
            <el-icon class="empty-icon" :size="48">
              <Menu />
            </el-icon>
            <p>暂无菜单数据</p>
            <el-button type="primary" @click="handleAdd">立即添加</el-button>
          </div>
        </template>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="form.menuName" />
        </el-form-item>
        <el-form-item label="路径" prop="path">
          <el-input v-model="form.path" />
        </el-form-item>
        <el-form-item label="组件" prop="component">
          <el-input v-model="form.component" placeholder="如: Layout.vue 或 @/views/xxx.vue" />
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <el-input v-model="form.icon" placeholder="如: User, Setting" />
        </el-form-item>
        <el-form-item label="父级菜单" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="menuTreeData"
            :props="{ label: 'menuName', value: 'id', children: 'children' }"
            check-strictly
            placeholder="选择父级菜单（默认顶级）"
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="排序" prop="orderNum">
          <el-input-number v-model="form.orderNum" :min="0" />
        </el-form-item>
        <el-form-item label="角色标识" prop="roleKeys">
          <el-input v-model="form.roleKeys" placeholder="多个角色用逗号分隔，如: admin,user" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" rows="3" />
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
import { Menu } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { createMenu, updateMenu, deleteMenu } from '@/api/menu'

const userStore = useUserStore()
const dialogVisible = ref(false)
const formRef = ref()
const loading = ref(false)

const treeData = computed(() => userStore.menus || [])
const menuTreeData = computed(() => userStore.menus || [])

const form = reactive({
  id: null,
  menuName: '',
  path: '',
  component: '',
  icon: '',
  parentId: null,
  orderNum: 0,
  roleKeys: '',
  status: 1,
  remark: ''
})

const dialogTitle = computed(() => form.id ? '编辑菜单' : '添加菜单')

const rules = {
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  path: [{ required: true, message: '请输入路径', trigger: 'blur' }]
}

const handleAdd = () => {
  Object.assign(form, {
    id: null,
    menuName: '',
    path: '',
    component: '',
    icon: '',
    parentId: null,
    orderNum: 0,
    roleKeys: '',
    status: 1,
    remark: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, {
    id: row.id,
    menuName: row.menuName,
    path: row.path,
    component: row.component || '',
    icon: row.icon || '',
    parentId: row.parentId || null,
    orderNum: row.orderNum || 0,
    roleKeys: row.roleKeys || '',
    status: row.status,
    remark: row.remark || ''
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const submitData = { ...form }
        if (form.id) {
          await updateMenu(form.id, submitData)
          ElMessage.success('更新成功')
        } else {
          await createMenu(submitData)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        await loadMenus()
      } catch (error) {
        ElMessage.error('操作失败')
      }
    }
  })
}

const loadMenus = async () => {
  loading.value = true
  try {
    await userStore.loadMenus()
  } catch (error) {
    ElMessage.error('加载菜单失败')
  } finally {
    loading.value = false
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该菜单吗？', '提示', {
      type: 'warning'
    })
    await deleteMenu(id)
    ElMessage.success('删除成功')
    await loadMenus()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadMenus()
})
</script>

<style scoped>
.menu-management {
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
