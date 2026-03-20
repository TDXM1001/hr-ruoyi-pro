import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import AssetLedgerDetailPage from '@/views/asset/ledger/detail/index.vue'

const mockPush = vi.fn()

vi.mock('vue-router', async (importOriginal) => {
  const actual = await importOriginal<typeof import('vue-router')>()
  return {
    ...actual,
    useRouter: () => ({ push: mockPush }),
    useRoute: () => ({ params: { assetId: '8' }, meta: {} })
  }
})

vi.mock('@/utils/dict', () => {
  return {
    useDict: () => ({
      ast_asset_status: [],
      ast_asset_source_type: [],
      ast_asset_acquire_type: []
    })
  }
})

vi.mock('@/store/modules/user', () => {
  return {
    useUserStore: () => ({
      permissions: ['asset:ledger:query', 'asset:ledger:edit']
    })
  }
})

vi.mock('@/api/asset/ledger', () => {
  return {
    getAssetLedger: vi.fn().mockResolvedValue({
      data: {
        assetId: 8,
        assetCode: 'FA-2026-0008',
        assetName: '测试资产',
        assetStatus: 'IN_USE'
      }
    }),
    getAssetLedgerLifecycle: vi.fn().mockResolvedValue({
      data: {
        ledger: { assetId: 8 },
        handoverRecords: [{ handoverNo: 'HD-2026-0001' }],
        inventoryRecords: [{ taskNo: 'IV-2026-0001' }],
        disposalRecords: [{ disposalNo: 'DP-2026-0001' }],
        changeLogs: [{ bizType: 'LEDGER_CREATE' }]
      }
    })
  }
})

describe('AssetLedgerDetailPage 生命周期区块', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('应展示生命周期轨迹与业务记录区块', async () => {
    const wrapper = mount(AssetLedgerDetailPage, {
      global: {
        plugins: [ElementPlus],
        stubs: {
          DictTag: true
        }
      }
    })

    await flushPromises()

    expect(wrapper.text()).toContain('生命周期轨迹')
    expect(wrapper.text()).toContain('交接记录')
    expect(wrapper.text()).toContain('盘点记录')
    expect(wrapper.text()).toContain('处置记录')
  })
})
