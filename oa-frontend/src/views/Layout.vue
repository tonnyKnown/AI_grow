<template>
  <div class="layout-container">
    <el-container class="main-container">
      <el-aside width="200px">
        <div class="logo">OA系统</div>
        <el-menu
          :default-active="activeMenu"
          router
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
        >
          <template v-if="userStore.menus.length > 0">
            <template v-for="menu in userStore.menus" :key="menu.id">
              <el-menu-item v-if="!menu.children || menu.children.length === 0" :index="menu.path">
                <span>{{ menu.menuName }}</span>
              </el-menu-item>
              <el-sub-menu v-else :index="menu.path">
                <template #title>
                  <span>{{ menu.menuName }}</span>
                </template>
                <el-menu-item v-for="child in menu.children" :key="child.id" :index="child.path">
                  {{ child.menuName }}
                </el-menu-item>
              </el-sub-menu>
            </template>
          </template>
          <template v-else>
            <el-menu-item index="/dashboard">
              <span>首页</span>
            </el-menu-item>
            <el-menu-item index="/users">
              <span>用户管理</span>
            </el-menu-item>
            <el-menu-item index="/roles">
              <span>角色管理</span>
            </el-menu-item>
            <el-menu-item index="/permissions">
              <span>权限管理</span>
            </el-menu-item>
            <el-menu-item index="/products">
              <span>商品管理</span>
            </el-menu-item>
            <el-menu-item index="/marketing">
              <span>营销推广</span>
            </el-menu-item>
            <el-menu-item index="/orders">
              <span>订单管理</span>
            </el-menu-item>
            <el-menu-item index="/menus">
              <span>菜单管理</span>
            </el-menu-item>
          </template>
        </el-menu>
      </el-aside>
      <el-container class="right-container">
        <el-header>
          <div class="header-content">
            <span class="username">{{ userStore.userInfo?.username || '未登录' }}</span>
            <el-tag size="small" v-if="userStore.roles.length > 0">
              {{ userStore.roles.join(', ') }}
            </el-tag>
            <el-button type="danger" size="small" @click="handleLogout">退出</el-button>
          </div>
        </el-header>
        <el-main class="content-main">
          <router-view></router-view>
        </el-main>
        <el-footer class="footer">
          <span>OA系统 © 2026</span>
        </el-footer>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

onMounted(async () => {
  if (userStore.token && !userStore.userInfo) {
    try {
      await userStore.refresh()
    } catch (error) {
      console.error('加载用户信息失败:', error)
    }
  } else if (userStore.token && userStore.menus.length === 0) {
    await userStore.loadMenus()
  }
})

const handleLogout = async () => {
  await userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout-container {
  width: 100%;
  height: 100vh;
  display: flex;
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: row;
}

.el-aside {
  background-color: #304156;
  flex-shrink: 0;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  background-color: #2b3a4a;
}

.right-container {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.el-header {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  padding: 0 20px;
}

.header-content {
  width: 100%;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 15px;
}

.username {
  color: #333;
  font-size: 14px;
}

.content-main {
  background-color: #f0f2f5;
  padding: 20px;
  flex: 1;
  overflow-y: auto;
}

.footer {
  background-color: #fff;
  padding: 12px 20px;
  text-align: center;
  color: #999;
  font-size: 12px;
  border-top: 1px solid #e8e8e8;
  flex-shrink: 0;
}
</style>
