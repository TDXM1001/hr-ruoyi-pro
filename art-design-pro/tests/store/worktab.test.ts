import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { ref } from 'vue'
import { useWorktabStore } from '@/store/modules/worktab'

vi.mock('@/router', () => {
  return {
    router: {
      push: vi.fn()
    }
  }
})

vi.mock('@/hooks/core/useCommon', () => {
  return {
    useCommon: () => ({
      homePath: ref('/')
    })
  }
})

describe('worktabStore 旧不动产详情页签清理', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('校验工作签时会移除已废弃的不动产详情子路由签，并把当前签切回有效页面', () => {
    const store = useWorktabStore()
    store.opened = [
      {
        title: '不动产档案占用信息页',
        name: 'AssetRealEstateDetailOccupancy',
        path: '/asset/real-estate/detail/:assetId/occupancy',
        keepAlive: true
      },
      {
        title: '不动产档案详情页',
        name: 'AssetRealEstateDetail',
        path: '/asset/real-estate/detail/20001',
        keepAlive: true
      }
    ]
    store.current = store.opened[0]

    const routerStub = {
      getRoutes: () => [{ name: 'AssetRealEstateDetail' }],
      resolve: (target: { path?: string }) => ({
        matched: target.path === '/asset/real-estate/detail/20001' ? [{}] : []
      })
    } as any

    store.validateWorktabs(routerStub)

    expect(store.opened).toHaveLength(1)
    expect(store.opened[0].name).toBe('AssetRealEstateDetail')
    expect(store.current.name).toBe('AssetRealEstateDetail')
    expect(store.keepAliveExclude).toContain('AssetRealEstateDetailOccupancy')
  })
})
