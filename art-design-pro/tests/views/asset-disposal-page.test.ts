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
    listAssetDisposal: vi.fn().mockResolvedValue({ rows: [], total: 0 }),
    addAssetDisposal: vi.fn().mockResolvedValue({ code: 200 }),
    approveAssetDisposal: vi.fn().mockResolvedValue({ code: 200 }),
    rejectAssetDisposal: vi.fn().mockResolvedValue({ code: 200 }),
    listAssetDisposalApprovals: vi.fn().mockResolvedValue({ data: [] })
  }
})

vi.mock('@/api/asset/ledger', () => {
  return {
    listAssetLedger: vi.fn().mockResolvedValue({ rows: [], total: 0 })
  }
})

const mountPage = () => {
  return mount(AssetDisposalPage, {
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
}

describe('AssetDisposalPage 来源上下文接入', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockPush.mockReset()
    routeState.query = {
      tab: 'record',
      assetId: '20001',
      assetCode: 'RE-2026-0001'
    }
  })

  it('首次加载时会按资产上下文过滤处置记录', async () => {
    mountPage()

    await flushPromises()

    expect(disposalApi.listAssetDisposal).toHaveBeenCalledWith(
      expect.objectContaining({
        pageNum: 1,
        pageSize: 10,
        assetId: 20001
      })
    )
  })

  it('待处置资产池首屏会带入资产编码过滤', async () => {
    routeState.query = {
      tab: 'pool',
      assetCode: 'RE-2026-0001'
    }

    mountPage()

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

    mountPage()

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

  it('intent=view 时展示来源横幅、上下文和返回入口', async () => {
    routeState.query = {
      source: 'real-estate-disposal-tab',
      intent: 'view',
      assetId: '20002',
      assetCode: 'RE-2026-0002',
      assetName: '深圳测试不动产B座'
    }

    const wrapper = mountPage()

    await flushPromises()

    expect(disposalApi.listAssetDisposal).toHaveBeenCalledWith(
      expect.objectContaining({
        pageNum: 1,
        pageSize: 10,
        assetId: 20002
      })
    )
    expect(wrapper.get('[data-testid="disposal-source-banner"]').text()).toContain('来自不动产档案处置联动')
    expect(wrapper.get('[data-testid="disposal-source-banner"]').text()).toContain('当前进入的是处置记录视图')
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

    const wrapper = mountPage()

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
    expect(wrapper.get('[data-testid="disposal-source-banner"]').text()).toContain('来自不动产档案处置联动')
    expect(wrapper.get('[data-testid="disposal-source-banner"]').text()).toContain('请继续发起或补齐处置流程')
    expect(wrapper.get('[data-testid="disposal-source-scope"]').text()).toContain('当前锁定范围')
    expect(wrapper.get('[data-testid="disposal-source-scope"]').text()).toContain('待处置资产池')
    expect(wrapper.get('[data-testid="disposal-source-next-step"]').text()).toContain('下一步建议')
    expect(wrapper.find('[data-testid="disposal-pool-scope-alert"]').exists()).toBe(false)
  })

  it('intent=view 时应展示处置闭环入口卡并可继续查看记录', async () => {
    routeState.query = {
      source: 'real-estate-disposal-tab',
      intent: 'view',
      assetId: '20002',
      assetCode: 'RE-2026-0002',
      assetName: '深圳测试不动产B座'
    }

    const wrapper = mountPage()

    await flushPromises()

    const beforeCalls = vi.mocked(disposalApi.listAssetDisposal).mock.calls.length

    expect(wrapper.get('[data-testid="disposal-entry-card"]').text()).toContain('RE-2026-0002')
    expect(wrapper.get('[data-testid="disposal-entry-primary-action"]').text()).toContain('查看处置记录')

    await wrapper.get('[data-testid="disposal-entry-primary-action"]').trigger('click')
    await flushPromises()

    const afterCalls = vi.mocked(disposalApi.listAssetDisposal).mock.calls.length
    expect(afterCalls).toBeGreaterThan(beforeCalls)
  })

  it('intent=start 时应展示处置闭环入口卡并可继续办理资产池', async () => {
    routeState.query = {
      source: 'real-estate-disposal-tab',
      intent: 'start',
      assetId: '20002',
      assetCode: 'RE-2026-0002',
      assetName: '深圳测试不动产B座'
    }

    const wrapper = mountPage()

    await flushPromises()

    const beforeCalls = vi.mocked(ledgerApi.listAssetLedger).mock.calls.length

    expect(wrapper.get('[data-testid="disposal-entry-card"]').text()).toContain('RE-2026-0002')
    expect(wrapper.get('[data-testid="disposal-entry-primary-action"]').text()).toContain('进入待处置资产池')

    await wrapper.get('[data-testid="disposal-entry-primary-action"]').trigger('click')
    await flushPromises()

    const afterCalls = vi.mocked(ledgerApi.listAssetLedger).mock.calls.length
    expect(afterCalls).toBeGreaterThan(beforeCalls)
  })

  it('intent=view 时应展示办理视图提示，并可切到待处置资产池', async () => {
    routeState.query = {
      source: 'real-estate-disposal-tab',
      intent: 'view',
      assetId: '20002',
      assetCode: 'RE-2026-0002',
      assetName: '深圳测试不动产B座'
    }

    const wrapper = mountPage()

    await flushPromises()

    const beforeCalls = vi.mocked(ledgerApi.listAssetLedger).mock.calls.length

    expect(wrapper.get('[data-testid="disposal-entry-workflow"]').text()).toContain('当前办理视图')
    expect(wrapper.get('[data-testid="disposal-entry-workflow"]').text()).toContain('处置记录回看')
    expect(wrapper.get('[data-testid="disposal-entry-secondary-action"]').text()).toContain('去待处置资产池')

    await wrapper.get('[data-testid="disposal-entry-secondary-action"]').trigger('click')
    await flushPromises()

    const afterCalls = vi.mocked(ledgerApi.listAssetLedger).mock.calls.length
    expect(afterCalls).toBeGreaterThan(beforeCalls)
  })

  it('intent=start 时应展示办理视图提示，并可切回处置记录', async () => {
    routeState.query = {
      source: 'real-estate-disposal-tab',
      intent: 'start',
      assetId: '20002',
      assetCode: 'RE-2026-0002',
      assetName: '深圳测试不动产B座'
    }

    const wrapper = mountPage()

    await flushPromises()

    const beforeCalls = vi.mocked(disposalApi.listAssetDisposal).mock.calls.length

    expect(wrapper.get('[data-testid="disposal-entry-workflow"]').text()).toContain('当前办理视图')
    expect(wrapper.get('[data-testid="disposal-entry-workflow"]').text()).toContain('待处置资产池办理')
    expect(wrapper.get('[data-testid="disposal-entry-secondary-action"]').text()).toContain('查看处置记录')

    await wrapper.get('[data-testid="disposal-entry-secondary-action"]').trigger('click')
    await flushPromises()

    const afterCalls = vi.mocked(disposalApi.listAssetDisposal).mock.calls.length
    expect(afterCalls).toBeGreaterThan(beforeCalls)
  })

  it('无来源参数时不展示来源横幅和返回入口', async () => {
    routeState.query = {
      tab: 'record',
      assetId: '20001',
      assetCode: 'RE-2026-0001'
    }

    const wrapper = mountPage()

    await flushPromises()

    expect(wrapper.find('[data-testid="disposal-source-banner"]').exists()).toBe(false)
    expect(wrapper.find('[data-testid="disposal-return-real-estate"]').exists()).toBe(false)
    expect(wrapper.find('[data-testid="disposal-record-scope-alert"]').exists()).toBe(true)
  })
})
