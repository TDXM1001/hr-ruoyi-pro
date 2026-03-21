import { beforeEach, describe, expect, it, vi } from 'vitest'
import { reactive } from 'vue'
import { flushPromises, mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import AssetRealEstateRectificationCompletePage from '@/views/asset/real-estate/rectification/complete/index.vue'
import * as realEstateApi from '@/api/asset/real-estate'

const mockPush = vi.fn()
const routeState = reactive({
  params: { assetId: '20001', rectificationId: '9001' },
  query: {},
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
    getRealEstateRectification: vi.fn().mockResolvedValue({
      data: {
        rectificationId: 9001,
        rectificationNo: 'RC-2026-0001',
        assetId: 20001,
        taskId: 6,
        taskNo: 'INV-2026-0008',
        taskName: '第一季度不动产巡检',
        inventoryItemId: 66,
        rectificationStatus: 'PENDING',
        issueType: '位置不符',
        issueDesc: '房间实际使用人与台账不一致',
        responsibleDeptName: '研发部门',
        responsibleUserName: '若依（研发部门）',
        deadlineDate: '2026-03-25',
        remark: '等待完成'
      }
    }),
    completeRealEstateRectification: vi.fn().mockResolvedValue({ code: 200 }),
    listRealEstateRectificationApprovalRecords: vi.fn().mockResolvedValue({ data: [] }),
    submitRealEstateRectificationApproval: vi.fn().mockResolvedValue({ code: 200 }),
    approveRealEstateRectification: vi.fn().mockResolvedValue({ code: 200 }),
    rejectRealEstateRectificationApproval: vi.fn().mockResolvedValue({ code: 200 })
  }
})

describe('AssetRealEstateRectificationCompletePage 点测', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockPush.mockReset()
    window.sessionStorage.clear()
    routeState.params = { assetId: '20001', rectificationId: '9001' }
    routeState.query = {}
    routeState.meta = {}
  })

  it('展示整改完成上下文与处理型页面布局', async () => {
    const wrapper = mount(AssetRealEstateRectificationCompletePage, {
      global: {
        plugins: [ElementPlus]
      }
    })

    await flushPromises()

    expect(wrapper.get('[data-testid="rectification-complete-processing-page"]').exists()).toBe(
      true
    )
    expect(wrapper.get('[data-testid="rectification-complete-layout"]').exists()).toBe(true)
    expect(wrapper.get('[data-testid="rectification-complete-main"]').exists()).toBe(true)
    expect(wrapper.get('[data-testid="rectification-complete-side"]').exists()).toBe(true)
    expect(wrapper.classes()).not.toContain('art-full-height')
    expect(wrapper.classes()).not.toContain('overflow-auto')
    expect(wrapper.text()).toContain('整改完成页')
    expect(wrapper.text()).toContain('RC-2026-0001')
    expect(wrapper.text()).toContain('房间实际使用人与台账不一致')
    expect(wrapper.text()).toContain('完成说明')
  })

  it('提交完成后回到详情壳整改页签，并调用独立完成接口', async () => {
    const wrapper = mount(AssetRealEstateRectificationCompletePage, {
      global: {
        plugins: [ElementPlus]
      }
    })

    await flushPromises()

    const vm = wrapper.vm as any
    vm.formData.completionDesc = '已重新调整房间责任人并完成现场复核'
    vm.formData.acceptanceRemark = '资产管理员已复核通过'
    vm.formRef = {
      validate: vi.fn().mockResolvedValue(true)
    }

    await vm.handleSubmit()

    expect(realEstateApi.completeRealEstateRectification).toHaveBeenCalledWith(
      20001,
      9001,
      expect.objectContaining({
        completionDesc: '已重新调整房间责任人并完成现场复核',
        acceptanceRemark: '资产管理员已复核通过'
      })
    )
    expect(mockPush).toHaveBeenCalledWith('/asset/real-estate/detail/20001')
    expect(window.sessionStorage.getItem('asset-real-estate-detail-tab:20001')).toBe(
      'rectification'
    )
  })

  it('已完成整改单进入完成页时只展示结果，不允许重复提交', async () => {
    vi.mocked(realEstateApi.getRealEstateRectification).mockResolvedValueOnce({
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
        responsibleDeptName: '研发部门',
        responsibleUserName: '若依（研发部门）',
        deadlineDate: '2026-03-25',
        completedTime: '2026-03-21 14:49:04',
        completionDesc: '已完成现场复核并修正责任人信息。',
        acceptanceRemark: '资产管理员现场验收通过。'
      }
    } as any)

    const wrapper = mount(AssetRealEstateRectificationCompletePage, {
      global: {
        plugins: [ElementPlus]
      }
    })

    await flushPromises()

    expect(wrapper.get('[data-testid="rectification-complete-side"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('整改完成结果')
    expect(wrapper.text()).toContain('完成时间')
    expect(wrapper.text()).toContain('2026-03-21 14:49:04')
    expect(wrapper.text()).toContain('已完成现场复核并修正责任人信息。')
    expect(wrapper.text()).toContain('资产管理员现场验收通过。')
    expect(wrapper.text()).not.toContain('确认完成整改')

    const vm = wrapper.vm as any
    await vm.handleSubmit()

    expect(realEstateApi.completeRealEstateRectification).not.toHaveBeenCalled()
  })
})
