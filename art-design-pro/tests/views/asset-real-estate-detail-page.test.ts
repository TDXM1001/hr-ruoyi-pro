import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ref } from 'vue'
import { flushPromises, mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import AssetRealEstateDetailPage from '@/views/asset/real-estate/detail/index.vue'

const mockPush = vi.fn()

vi.mock('vue-router', async (importOriginal) => {
  const actual = await importOriginal<typeof import('vue-router')>()
  return {
    ...actual,
    useRouter: () => ({ push: mockPush }),
    useRoute: () => ({ params: { assetId: '20001' }, meta: {} })
  }
})

vi.mock('@/utils/dict', () => {
  return {
    useDict: () => ({
      ast_asset_status: ref([]),
      ast_asset_source_type: ref([]),
      ast_asset_acquire_type: ref([])
    })
  }
})

vi.mock('@/store/modules/user', () => {
  return {
    useUserStore: () => ({
      permissions: ['asset:realEstate:query', 'asset:realEstate:edit']
    })
  }
})

vi.mock('@/api/asset/real-estate', () => {
  return {
    getRealEstateDetail: vi.fn().mockResolvedValue({
      data: {
        assetId: 20001,
        assetCode: 'RE-2026-0001',
        assetName: '深圳研发办公楼A座',
        assetStatus: 'IN_USE',
        ownershipCertNo: '粤(2024)深圳市不动产权第A0001号'
      }
    }),
    getRealEstateLifecycle: vi.fn().mockResolvedValue({
      data: {
        ledger: { assetId: 20001 },
        handoverRecords: [],
        inventoryRecords: [],
        disposalRecords: [],
        changeLogs: [{ bizType: 'LEDGER_CREATE' }]
      }
    })
  }
})

describe('AssetRealEstateDetailPage 点测', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('应展示不动产详情与生命周期区域', async () => {
    const wrapper = mount(AssetRealEstateDetailPage, {
      global: {
        plugins: [ElementPlus],
        stubs: {
          DictTag: true
        }
      }
    })

    await flushPromises()

    expect(wrapper.text()).toContain('不动产档案详情')
    expect(wrapper.text()).toContain('权属信息')
    expect(wrapper.text()).toContain('生命周期轨迹')
  })
})
