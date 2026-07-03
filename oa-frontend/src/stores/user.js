import { defineStore } from 'pinia'
import { login as loginApi, logout as logoutApi, getCurrentUser, getMenu } from '@/api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: null,
    menus: []
  }),

  getters: {
    roles: (state) => {
      return state.userInfo?.roles || []
    },
    permissions: (state) => {
      return state.userInfo?.permissions || []
    },
    hasRole: (state) => (role) => {
      return state.userInfo?.roles?.includes(role) || false
    },
    hasAnyRole: (state) => (roles) => {
      if (!state.userInfo?.roles) return false
      return roles.some(role => state.userInfo.roles.includes(role))
    },
    hasPermission: (state) => (permission) => {
      return state.userInfo?.permissions?.includes(permission) || false
    },
    hasAnyPermission: (state) => (permissions) => {
      if (!state.userInfo?.permissions) return false
      return permissions.some(permission => state.userInfo.permissions.includes(permission))
    }
  },

  actions: {
    async login(loginForm) {
      const res = await loginApi(loginForm)
      this.token = res.data.token
      this.userInfo = res.data
      localStorage.setItem('token', res.data.token)
      await this.loadMenus()
      return res
    },

    async logout() {
      try {
        await logoutApi()
      } finally {
        this.token = ''
        this.userInfo = null
        this.menus = []
        localStorage.removeItem('token')
      }
    },

    async getUserInfo() {
      if (this.token) {
        const res = await getCurrentUser()
        this.userInfo = res.data
        return res
      }
    },

    async loadMenus() {
      if (this.token && this.userInfo) {
        try {
          const roles = this.userInfo.roles || []
          const res = await getMenu(roles)
          this.menus = res.data
        } catch (error) {
          console.error('加载菜单失败:', error)
          this.menus = []
        }
      }
    },

    async refresh() {
      await this.getUserInfo()
      await this.loadMenus()
    }
  }
})
