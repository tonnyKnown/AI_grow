import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue')
      },
      {
        path: '/system/users',
        name: 'UserManagement',
        component: () => import('@/views/UserManagement.vue')
      },
      {
        path: '/system/roles',
        name: 'RoleManagement',
        component: () => import('@/views/RoleManagement.vue')
      },
      {
        path: '/system/permissions',
        name: 'PermissionManagement',
        component: () => import('@/views/PermissionManagement.vue')
      },
      {
        path: '/system/menus',
        name: 'MenuManagement',
        component: () => import('@/views/MenuManagement.vue')
      },
      {
        path: '/business/products',
        name: 'ProductManagement',
        component: () => import('@/views/ProductManagement.vue')
      },
      {
        path: '/business/orders',
        name: 'OrderManagement',
        component: () => import('@/views/OrderManagement.vue')
      },
      {
        path: '/business/logistics',
        name: 'LogisticsManagement',
        component: () => import('@/views/LogisticsManagement.vue')
      },
      {
        path: '/business/marketing',
        name: 'MarketingManagement',
        component: () => import('@/views/MarketingManagement.vue')
      },
      {
        path: '/business/chat',
        name: 'ChatBot',
        component: () => import('@/views/ChatBot.vue')
      },
      {
        path: '/business/knowledge',
        name: 'KnowledgeBot',
        component: () => import('@/views/KnowledgeBot.vue')
      },
      {
        path: '/business/javachain',
        name: 'JavaChain',
        component: () => import('@/views/JavaChain.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  const token = localStorage.getItem('token')

  if (to.path !== '/login' && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/')
  } else {
    if (token && !userStore.userInfo) {
      try {
        await userStore.refresh()
      } catch (error) {
        console.error('获取用户信息失败:', error)
      }
    }

    if (token && userStore.menus.length > 0 && to.path !== '/login') {
      const accessiblePaths = []
      const collectPaths = (menus) => {
        menus.forEach(menu => {
          if (menu.path) {
            accessiblePaths.push(menu.path)
          }
          if (menu.children) {
            collectPaths(menu.children)
          }
        })
      }
      collectPaths(userStore.menus)

      if (!accessiblePaths.includes(to.path) && !to.path.startsWith('/')) {
        next('/dashboard')
        return
      }
    }
    next()
  }
})

export default router
