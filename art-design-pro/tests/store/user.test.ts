import { setActivePinia, createPinia } from 'pinia'
import { describe, it, expect, beforeEach } from 'vitest'
import { useUserStore } from '../../src/store/modules/user'

describe('User Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('should process and store roles and permissions', async () => {
    const store = useUserStore()
    // 此处可以 mock api，验证 actions 是否将 permissions 赋值
    await store.getInfo()
    expect(store.roles).toContain('admin')
    expect(store.permissions).toContain('*:*:*')
  })
})
