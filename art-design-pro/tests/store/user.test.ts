import { setActivePinia, createPinia } from 'pinia'
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { useUserStore } from '../../src/store/modules/user'

vi.mock('../../src/api/auth', () => ({
  getInfo: vi.fn().mockResolvedValue({
    data: {
      roles: ['admin'],
      permissions: ['*:*:*'],
      user: { userId: 1, userName: 'admin' }
    }
  })
}))

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
