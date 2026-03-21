import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ref } from 'vue'
import { flushPromises, mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import AssetRealEstateFormPage from '@/views/asset/real-estate/form/index.vue'
import * as realEstateApi from '@/api/asset/real-estate'

const mockPush = vi.fn()

vi.mock('vue-router', async (importOriginal) => {
  const actual = await importOriginal<typeof import('vue-router')>()
  return {
    ...actual,
    useRouter: () => ({ push: mockPush }),
    useRoute: () => ({ params: {}, meta: {} })
  }
})

vi.mock('@/utils/dict', () => {
  return {
    useDict: () => ({
      ast_asset_status: ref([{ value: 'IN_USE', label: '使用中' }]),
      ast_asset_source_type: ref([{ value: 'MANUAL', label: '手工录入' }]),
      ast_asset_acquire_type: ref([{ value: 'PURCHASE', label: '采购' }])
    })
  }
})

vi.mock('@/api/asset/real-estate', () => {
  return {
    addRealEstate: vi.fn().mockResolvedValue({ data: 20002 }),
    updateRealEstate: vi.fn().mockResolvedValue({ code: 200 }),
    getRealEstateDetail: vi.fn().mockResolvedValue({ data: {} }),
    getRealEstateCategoryTree: vi.fn().mockResolvedValue({ data: [{ id: 1101, label: '办公用房' }] }),
    getRealEstateDeptTree: vi.fn().mockResolvedValue({ data: [{ id: 103, label: '研发部门' }] }),
    listRealEstateResponsibleUsers: vi.fn().mockResolvedValue({
      data: [{ value: 1, label: '若依（研发部门）' }]
    }),
    getNextRealEstateCode: vi.fn().mockResolvedValue('RE-2026-0002')
  }
})

describe('AssetRealEstateFormPage 点测', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockPush.mockReset()
  })

  it('应渲染不动产建档表单关键区域', async () => {
    const wrapper = mount(AssetRealEstateFormPage, {
      global: {
        plugins: [ElementPlus]
      }
    })

    await flushPromises()

    expect(wrapper.text()).toContain('新增不动产档案')
    expect(wrapper.text()).toContain('基础信息')
    expect(wrapper.text()).toContain('权属信息')
  })

  it('应提交后端所需的不动产类型', async () => {
    const wrapper = mount(AssetRealEstateFormPage, {
      global: {
        plugins: [ElementPlus]
      }
    })

    await flushPromises()

    const vm = wrapper.vm as any
    vm.formData.assetName = '深圳测试不动产B座'
    vm.formData.categoryId = 1101
    vm.formData.ownerDeptId = 103
    vm.formData.ownershipCertNo = '粤(2026)深圳市不动产权第B0002号'
    vm.formData.landUseType = '研发办公'
    vm.formData.buildingArea = 12580.25
    vm.formRef = {
      validate: vi.fn().mockResolvedValue(true)
    }

    await vm.handleSubmit()

    expect(realEstateApi.addRealEstate).toHaveBeenCalledWith(
      expect.objectContaining({
        assetType: 'REAL_ESTATE'
      })
    )
    expect(mockPush).toHaveBeenCalledWith('/asset/real-estate/detail/20002')
  })
})
