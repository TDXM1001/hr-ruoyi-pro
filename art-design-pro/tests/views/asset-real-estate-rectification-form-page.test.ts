import { beforeEach, describe, expect, it, vi } from 'vitest'
import { reactive } from 'vue'
import { flushPromises, mount } from '@vue/test-utils'
import ElementPlus, { ElDatePicker, ElSelect, ElTreeSelect } from 'element-plus'
import AssetRealEstateRectificationFormPage from '@/views/asset/real-estate/rectification/form/index.vue'
import * as realEstateApi from '@/api/asset/real-estate'

const mockPush = vi.fn()
const routeState = reactive({
  params: { assetId: '20001' },
  query: { taskId: '6' },
  meta: {}
})

vi.mock('vue-router', async (importOriginal) => {
  const actual = await importOriginal<typeof import('vue-router')>()
  return {
    ...actual,
    useRouter: () => ({ push: mockPush }),
    useRoute: () => routeState
  }
})

vi.mock('@/api/asset/real-estate', () => {
  return {
    getRealEstateDetail: vi.fn().mockResolvedValue({
      data: {
        assetId: 20001,
        assetCode: 'RE-2026-0001',
        assetName: '深圳研发办公楼A座'
      }
    }),
    getRealEstateDeptTree: vi.fn().mockResolvedValue([{ id: 103, label: '研发部门' }]),
    listRealEstateResponsibleUsers: vi.fn().mockResolvedValue([{ value: 1, label: '若依（研发部门）' }]),
    getRealEstateRectification: vi.fn().mockResolvedValue({ data: {} }),
    addRealEstateRectification: vi.fn().mockResolvedValue({ data: 9002 }),
    updateRealEstateRectification: vi.fn().mockResolvedValue({ code: 200 })
  }
})

vi.mock('@/api/asset/inventory', () => {
  return {
    getInventoryTask: vi.fn().mockResolvedValue({
      data: {
        taskId: 6,
        taskNo: 'INV-2026-0008',
        taskName: '第一季度不动产巡检'
      }
    }),
    listInventoryTaskAssets: vi.fn().mockResolvedValue({
      rows: [
        {
          itemId: 66,
          taskId: 6,
          inventoryResult: 'LOCATION_DIFF',
          resultDesc: '房间实际使用人与台账不一致'
        }
      ],
      total: 1
    })
  }
})

describe('AssetRealEstateRectificationFormPage 点测', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockPush.mockReset()
    window.sessionStorage.clear()
    routeState.params = { assetId: '20001' }
    routeState.query = { taskId: '6' }
    routeState.meta = {}
  })

  it('展示整改上下文与表单信息', async () => {
    const wrapper = mount(AssetRealEstateRectificationFormPage, {
      global: {
        plugins: [ElementPlus]
      }
    })

    await flushPromises()

    expect(wrapper.get('[data-testid="rectification-processing-page"]').exists()).toBe(true)
    expect(wrapper.get('[data-testid="rectification-processing-layout"]').exists()).toBe(true)
    expect(wrapper.get('[data-testid="rectification-processing-main"]').exists()).toBe(true)
    expect(wrapper.get('[data-testid="rectification-processing-side"]').exists()).toBe(true)
    expect(wrapper.classes()).not.toContain('art-full-height')
    expect(wrapper.classes()).not.toContain('overflow-auto')
    expect(wrapper.text()).toContain('新增整改单')
    expect(wrapper.text()).toContain('INV-2026-0008')
    expect(wrapper.text()).toContain('房间实际使用人与台账不一致')
  })

  it('保存整改后回到详情壳整改页签，并带正确提交参数', async () => {
    const wrapper = mount(AssetRealEstateRectificationFormPage, {
      global: {
        plugins: [ElementPlus]
      }
    })

    await flushPromises()

    const vm = wrapper.vm as any
    vm.formData.responsibleDeptId = 103
    vm.formData.responsibleUserId = 1
    vm.formData.deadlineDate = '2026-03-25'
    vm.formRef = {
      validate: vi.fn().mockResolvedValue(true)
    }

    await vm.handleSubmit()

    expect(realEstateApi.addRealEstateRectification).toHaveBeenCalledWith(
      20001,
      expect.objectContaining({
        assetId: 20001,
        taskId: 6,
        inventoryItemId: 66,
        responsibleDeptId: 103,
        responsibleUserId: 1
      })
    )
    expect(mockPush).toHaveBeenCalledWith('/asset/real-estate/detail/20001')
    expect(window.sessionStorage.getItem('asset-real-estate-detail-tab:20001')).toBe('rectification')
  })

  it('责任部门树继续使用资产域树节点协议', async () => {
    const wrapper = mount(AssetRealEstateRectificationFormPage, {
      global: {
        plugins: [ElementPlus]
      }
    })

    await flushPromises()

    const treeSelect = wrapper.findComponent(ElTreeSelect)
    expect(treeSelect.exists()).toBe(true)
    expect(treeSelect.props('props')).toMatchObject({
      value: 'id',
      label: 'label',
      children: 'children',
      disabled: 'disabled'
    })
    expect(treeSelect.props('valueKey')).toBe('id')
  })

  it('查看已完成整改单时展示完成信息只读分区', async () => {
    routeState.params = { assetId: '20001', rectificationId: '9001' } as any
    routeState.query = {}
    vi.mocked(realEstateApi.getRealEstateRectification).mockResolvedValue({
      data: {
        rectificationId: 9001,
        rectificationNo: 'RC-2026-0001',
        assetId: 20001,
        taskId: 6,
        taskNo: 'INV-2026-0008',
        taskName: '第一季度不动产巡检',
        inventoryItemId: 66,
        rectificationStatus: 'COMPLETED',
        issueType: '位置不符',
        issueDesc: '房间实际使用人与台账不一致',
        responsibleDeptId: 103,
        responsibleUserId: 1,
        deadlineDate: '2026-03-25',
        completedTime: '2026-03-21 14:49:04',
        completionDesc: '已完成现场复核并修正责任人信息。',
        acceptanceRemark: '资产管理员复核通过。'
      }
    } as any)

    const wrapper = mount(AssetRealEstateRectificationFormPage, {
      global: {
        plugins: [ElementPlus]
      }
    })

    await flushPromises()

    expect(wrapper.get('[data-testid="rectification-processing-side"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('整改完成信息')
    expect(wrapper.text()).toContain('完成时间')
    expect(wrapper.text()).toContain('2026-03-21 14:49:04')
    expect(wrapper.text()).toContain('完成说明')
    expect(wrapper.text()).toContain('已完成现场复核并修正责任人信息。')
    expect(wrapper.text()).toContain('验收备注')
    expect(wrapper.text()).toContain('资产管理员复核通过。')
    expect(wrapper.text()).not.toContain('保存整改单')

    expect(wrapper.find('input[placeholder="请输入问题类型"]').attributes('readonly')).toBeDefined()
    expect(wrapper.find('textarea[placeholder="请输入整改问题描述"]').attributes('readonly')).toBeDefined()
    expect(wrapper.find('textarea[placeholder="请输入备注"]').attributes('readonly')).toBeDefined()
    expect(wrapper.findComponent(ElTreeSelect).props('disabled')).toBe(true)
    expect(wrapper.findComponent(ElSelect).props('disabled')).toBe(true)
    expect(wrapper.findComponent(ElDatePicker).props('disabled')).toBe(true)
  })
})
