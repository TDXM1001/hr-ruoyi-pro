import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ref } from 'vue'
import { flushPromises, mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import AssetDisposalPage from '@/views/asset/disposal/index.vue'
import * as disposalApi from '@/api/asset/disposal'
import * as ledgerApi from '@/api/asset/ledger'

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
    useRoute: () => routeState
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
    addAssetDisposal: vi.fn().mockResolvedValue({ code: 200 })
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
})
