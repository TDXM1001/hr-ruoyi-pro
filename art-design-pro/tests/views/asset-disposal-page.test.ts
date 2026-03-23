import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ref } from 'vue'
import { flushPromises, mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import AssetDisposalPage from '@/views/asset/disposal/index.vue'
import * as disposalApi from '@/api/asset/disposal'
import * as ledgerApi from '@/api/asset/ledger'

const mockPush = vi.fn()

const routeState = {
  query: {
    tab: 'record',
    assetId: '20001',
    assetCode: 'RE-2026-0001'
  }
}

vi.mock('vue-router', async (importOriginal) => {
  const actual = await importOriginal<typeof import('vue-router')>()
  return {
    ...actual,
    useRoute: () => routeState,
    useRouter: () => ({
      push: mockPush
    })
  }
})

vi.mock('@/utils/dict', () => {
  return {
    useDict: () => ({
      ast_asset_status: ref([{ value: 'IN_USE', label: '使用中' }])
    })
  }
})

vi.mock('@/store/modules/user', () => {
  return {
    useUserStore: () => ({
      permissions: ['asset:disposal:add']
    })
  }
})

vi.mock('@/api/asset/disposal', () => {
  return {
    listAssetDisposal: vi.fn().mockResolvedValue({
      rows: [],
      total: 0
    }),
    addAssetDisposal: vi.fn().mockResolvedValue({ code: 200 }),
    approveAssetDisposal: vi.fn().mockResolvedValue({ code: 200 }),
    rejectAssetDisposal: vi.fn().mockResolvedValue({ code: 200 }),
    listAssetDisposalApprovals: vi.fn().mockResolvedValue({
      data: []
    })
  }
})

vi.mock('@/api/asset/ledger', () => {
  return {
    listAssetLedger: vi.fn().mockResolvedValue({
      rows: [],
      total: 0
    })
  }
})

describe('AssetDisposalPage 上下文点测', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockPush.mockReset()
    routeState.query = {
      tab: 'record',
      assetId: '20001',
      assetCode: 'RE-2026-0001'
    }
  })

  it('应在首次加载时携带资产上下文过滤处置记录', async () => {
    mount(AssetDisposalPage, {
      global: {
        plugins: [ElementPlus],
        stubs: {
          DictTag: true,
          ArtSearchBar: {
            template: '<div class="art-search-bar-stub"></div>',
            props: ['modelValue', 'items', 'showExpand']
          },
          ArtTable: {
            template: '<div class="art-table-stub"></div>',
            props: ['data', 'columns', 'loading', 'pagination']
          }
        }
      }
    })

    await flushPromises()

    expect(disposalApi.listAssetDisposal).toHaveBeenCalledWith(
      expect.objectContaining({
        pageNum: 1,
        pageSize: 10,
        assetId: 20001
      })
    )
  })

  it('应在待处置资产池页签首屏带入资产编码上下文', async () => {
    routeState.query = {
      tab: 'pool',
      assetCode: 'RE-2026-0001'
    }

    mount(AssetDisposalPage, {
      global: {
        plugins: [ElementPlus],
        stubs: {
          DictTag: true,
          ArtSearchBar: {
            template: '<div class="art-search-bar-stub"></div>',
            props: ['modelValue', 'items', 'showExpand']
          },
          ArtTable: {
            template: '<div class="art-table-stub"></div>',
            props: ['data', 'columns', 'loading', 'pagination']
          }
        }
      }
    })

    await flushPromises()

    expect(ledgerApi.listAssetLedger).toHaveBeenCalledWith(
      expect.objectContaining({
        pageNum: 1,
        pageSize: 10,
        assetType: 'FIXED',
        assetStatus: 'PENDING_DISPOSAL',
        assetCode: 'RE-2026-0001'
      })
    )
  })

  it('挂载时不应出现审批弹窗相关未定义属性警告', async () => {
    const warnSpy = vi.spyOn(console, 'warn').mockImplementation(() => {})
    const errorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

    mount(AssetDisposalPage, {
      global: {
        plugins: [ElementPlus],
        stubs: {
          DictTag: true,
          ArtSearchBar: {
            template: '<div class="art-search-bar-stub"></div>',
            props: ['modelValue', 'items', 'showExpand']
          },
          ArtTable: {
            template: '<div class="art-table-stub"></div>',
            props: ['data', 'columns', 'loading', 'pagination']
          }
        }
      }
    })

    await flushPromises()

    const warnOutput = warnSpy.mock.calls.flat().join(' ')
    const errorOutput = errorSpy.mock.calls.flat().join(' ')

    expect(warnOutput).not.toContain('approvalDialogVisible')
    expect(warnOutput).not.toContain('approvalDialogTitle')
    expect(warnOutput).not.toContain('handleApprovalDialogClosed')
    expect(warnOutput).not.toContain('approvalDrawerVisible')
    expect(errorOutput).not.toContain('approvalDialogVisible')
    expect(errorOutput).not.toContain('approvalDialogTitle')
    expect(errorOutput).not.toContain('handleApprovalDialogClosed')
    expect(errorOutput).not.toContain('approvalDrawerVisible')

    warnSpy.mockRestore()
    errorSpy.mockRestore()
  })

  it('intent=view 时展示不动产来源横幅、资产上下文和返回入口', async () => {
    routeState.query = {
      source: 'real-estate-disposal-tab',
      intent: 'view',
      assetId: '20002',
      assetCode: 'RE-2026-0002',
      assetName: '深圳测试不动产B座'
    }

    const wrapper = mount(AssetDisposalPage, {
      global: {
        plugins: [ElementPlus],
        stubs: {
          DictTag: true,
          ArtSearchBar: {
            template: '<div class="art-search-bar-stub"></div>',
            props: ['modelValue', 'items', 'showExpand']
          },
          ArtTable: {
            template: '<div class="art-table-stub"></div>',
            props: ['data', 'columns', 'loading', 'pagination']
          }
        }
      }
    })

    await flushPromises()

    expect(disposalApi.listAssetDisposal).toHaveBeenCalledWith(
      expect.objectContaining({
        pageNum: 1,
        pageSize: 10,
        assetId: 20002
      })
    )
    expect(wrapper.get('[data-testid="disposal-source-banner"]').text()).toContain(
      '来自不动产档案处置联动'
    )
    expect(wrapper.get('[data-testid="disposal-source-banner"]').text()).toContain(
      '当前进入的是处置记录视图'
    )
    expect(wrapper.get('[data-testid="disposal-source-context"]').text()).toContain('RE-2026-0002')
    expect(wrapper.get('[data-testid="disposal-source-context"]').text()).toContain('深圳测试不动产B座')
    expect(wrapper.get('[data-testid="disposal-source-scope"]').text()).toContain('当前锁定范围')
    expect(wrapper.get('[data-testid="disposal-source-scope"]').text()).toContain('处置记录')
    expect(wrapper.get('[data-testid="disposal-source-next-step"]').text()).toContain('下一步建议')
    expect(wrapper.find('[data-testid="disposal-record-scope-alert"]').exists()).toBe(false)

    await wrapper.get('[data-testid="disposal-return-real-estate"]').trigger('click')

    expect(mockPush).toHaveBeenCalledWith({
      path: '/asset/real-estate/detail/20002',
      query: {
        tab: 'disposal',
        from: 'asset-disposal'
      }
    })
  })

  it('intent=start 时默认切到待处置资产池并展示继续办理提示', async () => {
    routeState.query = {
      source: 'real-estate-disposal-tab',
      intent: 'start',
      assetId: '20002',
      assetCode: 'RE-2026-0002',
      assetName: '深圳测试不动产B座'
    }

    const wrapper = mount(AssetDisposalPage, {
      global: {
        plugins: [ElementPlus],
        stubs: {
          DictTag: true,
          ArtSearchBar: {
            template: '<div class="art-search-bar-stub"></div>',
            props: ['modelValue', 'items', 'showExpand']
          },
          ArtTable: {
            template: '<div class="art-table-stub"></div>',
            props: ['data', 'columns', 'loading', 'pagination']
          }
        }
      }
    })

    await flushPromises()

    expect(ledgerApi.listAssetLedger).toHaveBeenCalledWith(
      expect.objectContaining({
        pageNum: 1,
        pageSize: 10,
        assetType: 'FIXED',
        assetStatus: 'PENDING_DISPOSAL',
        assetCode: 'RE-2026-0002'
      })
    )
    expect(wrapper.get('[data-testid="disposal-source-banner"]').text()).toContain(
      '来自不动产档案处置联动'
    )
    expect(wrapper.get('[data-testid="disposal-source-banner"]').text()).toContain(
      '请继续发起或补齐处置流程'
    )
    expect(wrapper.get('[data-testid="disposal-source-scope"]').text()).toContain('当前锁定范围')
    expect(wrapper.get('[data-testid="disposal-source-scope"]').text()).toContain('待处置资产池')
    expect(wrapper.get('[data-testid="disposal-source-next-step"]').text()).toContain('下一步建议')
    expect(wrapper.find('[data-testid="disposal-pool-scope-alert"]').exists()).toBe(false)
  })

  it('无来源参数时不展示来源横幅和返回入口', async () => {
    routeState.query = {
      tab: 'record',
      assetId: '20001',
      assetCode: 'RE-2026-0001'
    }

    const wrapper = mount(AssetDisposalPage, {
      global: {
        plugins: [ElementPlus],
        stubs: {
          DictTag: true,
          ArtSearchBar: {
            template: '<div class="art-search-bar-stub"></div>',
            props: ['modelValue', 'items', 'showExpand']
          },
          ArtTable: {
            template: '<div class="art-table-stub"></div>',
            props: ['data', 'columns', 'loading', 'pagination']
          }
        }
      }
    })

    await flushPromises()

    expect(wrapper.find('[data-testid="disposal-source-banner"]').exists()).toBe(false)
    expect(wrapper.find('[data-testid="disposal-return-real-estate"]').exists()).toBe(false)
    expect(wrapper.find('[data-testid="disposal-record-scope-alert"]').exists()).toBe(true)
  })
})
