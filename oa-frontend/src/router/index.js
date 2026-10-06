import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/maintenance',
    name: 'Maintenance',
    component: () => import('@/views/MaintenancePage.vue')
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

/**
 * 不参与菜单管控的页面。
 *
 * - /login      登录页本身
 * - /maintenance 运维维护页，不挂在 Layout 菜单树上
 * - /dashboard  首页兜底：菜单中的重定向目标，必须始终可达，
 *               否则「用户菜单不含首页」时 next('/dashboard') 会形成死循环
 *               （Vue Router 会抛 "Detected an infinite redirection"）
 */
const MENU_EXEMPT_PATHS = ['/login', '/maintenance', '/dashboard']

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  const token = localStorage.getItem('token')

  if (to.path !== '/login' && !token) {
    next('/login')
    return
  }

  if (to.path === '/login' && token) {
    next('/')
    return
  }

  if (token && !userStore.userInfo) {
    try {
      await userStore.refresh()
    } catch (error) {
      console.error('获取用户信息失败:', error)
    }
  }

  // 菜单级访问控制：用户菜单已加载时，拦截其无权访问的页面
  if (token && userStore.menus.length > 0 && !MENU_EXEMPT_PATHS.includes(to.path)) {
    const accessiblePaths = []
    const collectPaths = (menus) => {
      menus.forEach(menu => {
        if (menu.path) {
          accessiblePaths.push(menu.path)
        }
        if (menu.children && menu.children.length > 0) {
          collectPaths(menu.children)
        }
      })
    }
    collectPaths(userStore.menus)

    // 菜单为空数组时不做拦截（避免后端菜单异常导致全站不可用）
    if (accessiblePaths.length > 0 && !accessiblePaths.includes(to.path)) {
      console.warn(`[router] 当前用户无权访问 ${to.path}，已重定向到首页`)
      next('/dashboard')
      return
    }
  }

  next()
})

export default router
