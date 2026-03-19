import { describe, it, expect, vi, beforeEach } from 'vitest'
import { ref } from 'vue'
import { mount, flushPromises } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import AssetUseFormPage from '@/views/asset/use/form/index.vue'
import { addAssetHandoverOrder } from '@/api/asset/handover'

const mockPush = vi.fn()

vi.mock('vue-router', async (importOriginal) => {
  const actual = await importOriginal<typeof import('vue-router')>()
  return {
    ...actual,
    useRouter: () => ({ push: mockPush }),
    useRoute: () => ({ query: { type: 'ASSIGN' }, meta: {} })
  }
})

vi.mock('@/utils/dict', () => {
  return {
    useDict: () => ({
      ast_asset_status: ref([
        { value: 'IN_LEDGER', label: '在册' },
        { value: 'IDLE', label: '闲置中' },
        { value: 'IN_USE', label: '使用中' }
      ])
    })
  }
})

vi.mock('@/api/asset/ledger', () => {
  return {
    listAssetLedger: vi.fn().mockResolvedValue({ rows: [], total: 0 }),
    getAssetDeptTree: vi.fn().mockResolvedValue({ data: [{ id: 100, label: '研发部' }] }),
    listAssetResponsibleUsers: vi.fn().mockResolvedValue({ data: [{ value: 1, label: '若依' }] })
  }
})

vi.mock('@/api/asset/handover', () => {
  return {
    addAssetHandoverOrder: vi.fn().mockResolvedValue(1)
  }
})

vi.mock('@/hooks/core/useTable', () => {
  return {
    useTable: () => ({
      columns: ref([
        { type: 'selection', width: 48, align: 'center' },
        { prop: 'assetCode', label: '资产编码' }
      ]),
      data: ref([{ assetId: 1, assetCode: 'FA-2026-0001', assetStatus: 'IN_LEDGER' }]),
      loading: ref(false),
      pagination: ref({ current: 1, size: 10, total: 1 }),
      getData: vi.fn(),
      searchParams: {},
      resetSearchParams: vi.fn(),
      handleSizeChange: vi.fn(),
      handleCurrentChange: vi.fn()
    })
  }
})

describe('AssetUseFormPage 点测烟测', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('应能正常渲染关键流程区域', async () => {
    const wrapper = mount(AssetUseFormPage, {
      global: {
        plugins: [ElementPlus],
        stubs: {
          ArtSearchBar: true,
          ArtTable: true,
          DictTag: true
        }
      }
    })

    await flushPromises()

    expect(wrapper.text()).toContain('发起资产交接单')
    expect(wrapper.text()).toContain('交接信息')
    expect(wrapper.text()).toContain('选择交接资产')
    expect(wrapper.text()).toContain('提交交接单')
  })

  it('切换为退还后应显示必填 1/1', async () => {
    const wrapper = mount(AssetUseFormPage, {
      global: {
        plugins: [ElementPlus],
        stubs: {
          ArtSearchBar: true,
          ArtTable: true,
          DictTag: true
        }
      }
    })

    await flushPromises()

    const returnRadio = wrapper.find('input[type="radio"][value="RETURN"]')
    await returnRadio.setValue(true)
    await flushPromises()

    expect(wrapper.text()).toContain('必填完成：1/1')
  })

  it('未勾选资产直接提交应提示校验信息', async () => {
    const wrapper = mount(AssetUseFormPage, {
      global: {
        plugins: [ElementPlus],
        stubs: {
          ArtSearchBar: true,
          ArtTable: true,
          DictTag: true
        }
      }
    })

    await flushPromises()
    const submitButton = wrapper
      .findAll('button')
      .find((buttonWrapper) => buttonWrapper.text().includes('提交交接单'))
    expect(submitButton).toBeTruthy()
    await submitButton!.trigger('click')

    expect(vi.mocked(addAssetHandoverOrder)).not.toHaveBeenCalled()
    expect(mockPush).not.toHaveBeenCalled()
  })

  it('选中资产后提交应调用接口并回到资产使用列表', async () => {
    const wrapper = mount(AssetUseFormPage, {
      global: {
        plugins: [ElementPlus],
        stubs: {
          ArtSearchBar: true,
          ArtTable: true,
          DictTag: true
        }
      }
    })

    await flushPromises()

    const returnRadio = wrapper.find('input[type="radio"][value="RETURN"]')
    await returnRadio.setValue(true)

    const vm = wrapper.vm as any
    vm.handleSelectionChange([
      {
        assetId: 1,
        assetCode: 'FA-2026-0001',
        assetName: '研发笔记本电脑',
        assetStatus: 'IN_USE'
      }
    ])

    const submitButton = wrapper
      .findAll('button')
      .find((buttonWrapper) => buttonWrapper.text().includes('提交交接单'))
    expect(submitButton).toBeTruthy()
    await submitButton!.trigger('click')
    await flushPromises()

    expect(vi.mocked(addAssetHandoverOrder)).toHaveBeenCalledTimes(1)
    expect(vi.mocked(addAssetHandoverOrder)).toHaveBeenCalledWith(
      expect.objectContaining({
        handoverType: 'RETURN',
        assetIds: [1]
      })
    )
    expect(mockPush).toHaveBeenCalledWith({
      path: '/asset/use',
      query: { refresh: '1' }
    })
  })
})
