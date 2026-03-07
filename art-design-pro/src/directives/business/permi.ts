import { useUserStore } from '@/store/modules/user'

export const hasPermi = {
  mounted(el: HTMLElement, binding: any) {
    const { value } = binding
    const userStore = useUserStore()
    const all_permission = '*:*:*'
    const permissions = userStore.permissions || []

    if (value && value instanceof Array && value.length > 0) {
      const perms = value
      const hasPermissions = permissions.some((v) => {
        return all_permission === v || perms.includes(v)
      })
      if (!hasPermissions) {
        el.parentNode && el.parentNode.removeChild(el)
      }
    } else {
      throw new Error(`请设置操作权限标签值`)
    }
  }
}
