import { useUserStore } from '@/stores/user'

export const hasPermission = {
  mounted(el, binding) {
    const userStore = useUserStore()
    const permission = binding.value

    if (!userStore.hasPermission(permission)) {
      el.style.display = 'none'
    }
  }
}

export const hasAnyPermission = {
  mounted(el, binding) {
    const userStore = useUserStore()
    const permissions = binding.value

    if (!userStore.hasAnyPermission(permissions)) {
      el.style.display = 'none'
    }
  }
}

export const hasRole = {
  mounted(el, binding) {
    const userStore = useUserStore()
    const role = binding.value

    if (!userStore.hasRole(role)) {
      el.style.display = 'none'
    }
  }
}

export const hasAnyRole = {
  mounted(el, binding) {
    const userStore = useUserStore()
    const roles = binding.value

    if (!userStore.hasAnyRole(roles)) {
      el.style.display = 'none'
    }
  }
}