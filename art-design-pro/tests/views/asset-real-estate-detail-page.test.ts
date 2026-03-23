import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { reactive, ref } from 'vue'
import { flushPromises, mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import AssetRealEstateDetailPage from '@/views/asset/real-estate/detail/index.vue'
import * as realEstateApi from '@/api/asset/real-estate'

const mockPush = vi.fn()
const routeState = reactive({
  params: { assetId: '20001' },
  query: {} as Record<string, string>,
  meta: {},
  path: '/asset/real-estate/detail/20001',
  fullPath: '/asset/real-estate/detail/20001'
})

vi.mock('vue-router', async (importOriginal) => {
  const actual = await importOriginal<typeof import('vue-router')>()
  return {
    ...actual,
    useRouter: () => ({ push: mockPush }),
    useRoute: () => routeState
  }
})

vi.mock('@/utils/dict', () => {
  return {
    useDict: () => ({
      ast_asset_status: ref([{ value: 'IN_USE', label: '使用中' }]),
      ast_asset_source_type: ref([{ value: 'MANUAL', label: '手工新增' }]),
      ast_asset_acquire_type: ref([{ value: 'PURCHASE', label: '采购' }])
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
        ownerDeptName: '研发部门',
        useDeptName: '研发部门',
        responsibleUserName: '若依',
        locationName: '深圳南山科技园A座',
        ownershipCertNo: '粤(2024)深圳市不动产权第A0001号',
        landUseType: '研发办公',
        buildingArea: 18650.5,
        originalValue: 12500000,
        lastInventoryDate: '2026-03-01',
        sourceType: 'MANUAL',
        acquireType: 'PURCHASE',
        categoryName: '办公用房',
        enableDate: '2026-01-01',
        remark: '用于研发办公的不动产资产。'
      }
    }),
    getRealEstateLifecycle: vi.fn().mockResolvedValue({
      data: {
        occupancyRecords: [
          {
            occupancyId: 9101,
            occupancyNo: 'OCC-2026-9001',
            occupancyStatus: 'ACTIVE',
            useDeptId: 103,
            useDeptName: '研发部门',
            responsibleUserId: 1,
            responsibleUserName: '若依',
            locationName: '深圳南山科技园A座',
            startDate: '2026-03-22',
            changeReason: '用于占用前端闭环点测'
          },
          {
            occupancyId: 9100,
            occupancyNo: 'OCC-2026-8999',
            occupancyStatus: 'RELEASED',
            useDeptId: 101,
            useDeptName: '行政部门',
            responsibleUserId: 2,
            responsibleUserName: '王敏',
            locationName: '深圳南山科技园B座',
            startDate: '2026-03-01',
            endDate: '2026-03-10',
            changeReason: '历史占用',
            releaseReason: '部门搬离'
          }
        ],
        handoverRecords: [
          {
            handoverOrderId: 1,
            handoverItemId: 11,
            handoverNo: 'HO-2026-0001',
            handoverType: 'TRANSFER',
            handoverDate: '2026-02-18',
            beforeStatus: 'IN_USE',
            afterStatus: 'IN_USE',
            toDeptName: '研发部门',
            toUserName: '若依',
            toLocationName: '深圳南山科技园A座'
          }
        ],
        inventoryRecords: [
          {
            itemId: 66,
            taskId: 6,
            taskNo: 'INV-2026-0008',
            taskName: '第一季度不动产巡检',
            inventoryResult: 'LOCATION_DIFF',
            followUpAction: 'UPDATE_LEDGER',
            processStatus: 'PENDING',
            checkedBy: '资产管理员',
            checkedTime: '2026-03-01 09:30:00',
            resultDesc: '房间实际使用人与台账不一致'
          },
          {
            itemId: 67,
            taskId: 7,
            taskNo: 'INV-2026-0009',
            taskName: '消防设施专项巡检',
            inventoryResult: 'DAMAGED',
            followUpAction: 'UPDATE_LEDGER',
            followUpBizId: 9001,
            processStatus: 'PENDING',
            checkedBy: '资产管理员',
            checkedTime: '2026-03-05 10:00:00',
            resultDesc: '消防闭门器损坏'
          }
        ],
        rectificationOrders: [
          {
            rectificationId: 9001,
            rectificationNo: 'RC-2026-0001',
            assetId: 20001,
            taskId: 7,
            taskNo: 'INV-2026-0009',
            taskName: '消防设施专项巡检',
            issueType: '损坏',
            issueDesc: '消防闭门器损坏',
            responsibleDeptName: '研发部门',
            responsibleUserName: '若依',
            rectificationStatus: 'PENDING',
            deadlineDate: '2026-03-20'
          }
        ],
        disposalRecords: [
          {
            disposalId: 9,
            disposalNo: 'DIS-2026-0003',
            disposalType: '报废',
            disposalStatus: 'CONFIRMED',
            disposalDate: '2026-03-10',
            confirmedBy: '资产经理',
            confirmedTime: '2026-03-10 14:20:00'
          }
        ],
        changeLogs: [
          {
            logId: 100,
            bizType: 'LEDGER_CREATE',
            changeDesc: '完成资产建档',
            operateBy: 'admin',
            operateTime: '2026-03-01 10:00:00',
            beforeStatus: '-',
            afterStatus: 'IN_USE'
          },
          {
            logId: 101,
            bizType: 'LEDGER_UPDATE',
            changeDesc: '完成整改登记',
            operateBy: 'asset-admin',
            operateTime: '2026-03-02 15:30:00',
            beforeStatus: 'IN_USE',
            afterStatus: 'IN_USE'
          }
        ]
      }
    }),
    listRealEstateRectificationApprovalRecords: vi.fn().mockResolvedValue({ data: [] }),
    addRealEstateOccupancy: vi.fn().mockResolvedValue({ data: 9102 }),
    changeRealEstateOccupancy: vi.fn().mockResolvedValue({ data: 9103 }),
    releaseRealEstateOccupancy: vi.fn().mockResolvedValue({ code: 200 }),
    getRealEstateDeptTree: vi.fn().mockResolvedValue({
      data: [{ id: 103, label: '研发部门' }]
    }),
    listRealEstateResponsibleUsers: vi.fn().mockResolvedValue({
      data: [{ value: 1, label: '若依' }]
    }),
    submitRealEstateRectificationApproval: vi.fn().mockResolvedValue({ code: 200 }),
    approveRealEstateRectification: vi.fn().mockResolvedValue({ code: 200 }),
    rejectRealEstateRectificationApproval: vi.fn().mockResolvedValue({ code: 200 })
  }
})

describe('AssetRealEstateDetailPage 详情壳', () => {
  beforeEach(() => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2026-03-21T10:00:00+08:00'))
    vi.clearAllMocks()
    mockPush.mockReset()
    window.localStorage.clear()
    window.sessionStorage.clear()
    routeState.params = { assetId: '20001' }
    routeState.query = {}
    routeState.meta = {}
    routeState.path = '/asset/real-estate/detail/20001'
    routeState.fullPath = routeState.path
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('默认展示总览，并且页内切换页签不触发路由跳转', async () => {
    const wrapper = mount(AssetRealEstateDetailPage, {
      global: {
        plugins: [ElementPlus],
        stubs: { DictTag: true }
      }
    })

    await flushPromises()

    expect(wrapper.text()).toContain('不动产资产详情壳')
    expect(wrapper.text()).toContain('基础台账')
    expect(wrapper.text()).toContain('权属信息')

    const vm = wrapper.vm as any
    vm.handleTabChange('occupancy')
    await flushPromises()

    expect(wrapper.text()).toContain('当前有效占用')
    expect(wrapper.text()).toContain('OCC-2026-9001')
    expect(wrapper.text()).toContain('占用历史记录')
    expect(wrapper.text()).toContain('OCC-2026-8999')
    expect(mockPush).not.toHaveBeenCalled()
    expect(window.sessionStorage.getItem('asset-real-estate-detail-tab:20001')).toBe('occupancy')
  })

  it('详情壳顶部摘要和处置页签展示处置闭环摘要', async () => {
    const wrapper = mount(AssetRealEstateDetailPage, {
      global: {
        plugins: [ElementPlus],
        stubs: { DictTag: true }
      }
    })

    await flushPromises()

    expect(wrapper.text()).toContain('处置闭环')
    expect(wrapper.text()).toContain('已完成处置闭环')

    const vm = wrapper.vm as any
    vm.handleTabChange('disposal')
    await flushPromises()

    expect(wrapper.text()).toContain('处置闭环摘要')
    expect(wrapper.text()).toContain('已完成处置闭环')
    expect(wrapper.text()).toContain('最近处置动作')
    expect(wrapper.text()).toContain('已确认处置')
    expect(wrapper.text()).toContain('处置已闭环，可回看历史记录并归档留痕。')
  })

  it('顶部摘要、总览和处置页签统一展示处置闭环卡口径', async () => {
    const wrapper = mount(AssetRealEstateDetailPage, {
      global: {
        plugins: [ElementPlus],
        stubs: { DictTag: true }
      }
    })

    await flushPromises()

    const disposalSummary = wrapper.get('[data-testid="detail-summary-disposal"]')
    expect(disposalSummary.text()).toContain('已完成处置闭环')
    expect(disposalSummary.text()).toContain('资产管理员 / 归档回看')
    expect(disposalSummary.text()).toContain('最近动作：已确认处置')
    expect(disposalSummary.text()).toContain('最近责任人：资产经理')

    const overviewClosureCard = wrapper.get('[data-testid="overview-disposal-closure-card"]')
    expect(overviewClosureCard.text()).toContain('当前责任归口')
    expect(overviewClosureCard.text()).toContain('资产管理员')
    expect(overviewClosureCard.text()).toContain('责任动作')
    expect(overviewClosureCard.text()).toContain('归档回看')
    expect(overviewClosureCard.text()).toContain('下一步建议')

    const vm = wrapper.vm as any
    vm.handleTabChange('disposal')
    await flushPromises()

    const disposalClosureCard = wrapper.get('[data-testid="disposal-closure-card"]')
    expect(disposalClosureCard.text()).toContain('当前责任归口')
    expect(disposalClosureCard.text()).toContain('资产管理员')
    expect(disposalClosureCard.text()).toContain('责任动作')
    expect(disposalClosureCard.text()).toContain('归档回看')
    expect(disposalClosureCard.text()).toContain('下一步建议')
    expect(disposalClosureCard.text()).toContain('进入资产处置')
    expect(wrapper.text()).not.toContain('处置联动')
  })

  it('处置页签在未发起时提供发起入口并带来源上下文跳转', async () => {
    vi.mocked(realEstateApi.getRealEstateLifecycle).mockResolvedValueOnce({
      data: {
        occupancyRecords: [],
        handoverRecords: [],
        inventoryRecords: [],
        rectificationOrders: [],
        disposalRecords: [],
        changeLogs: []
      }
    } as any)

    const wrapper = mount(AssetRealEstateDetailPage, {
      global: {
        plugins: [ElementPlus],
        stubs: { DictTag: true }
      }
    })

    await flushPromises()

    const vm = wrapper.vm as any
    vm.handleTabChange('disposal')
    await flushPromises()

    expect(wrapper.text()).toContain('未发起处置')
    await wrapper.get('[data-testid="disposal-initiate-button"]').trigger('click')

    expect(mockPush).toHaveBeenLastCalledWith({
      path: '/asset/disposal',
      query: {
        tab: 'record',
        assetId: '20001',
        assetCode: 'RE-2026-0001',
        assetName: '深圳研发办公楼A座',
        source: 'real-estate-disposal-tab',
        intent: 'start'
      }
    })
  })

  it('未发起处置场景顶部摘要和处置页签统一展示发起提示', async () => {
    vi.mocked(realEstateApi.getRealEstateLifecycle).mockResolvedValueOnce({
      data: {
        occupancyRecords: [],
        handoverRecords: [],
        inventoryRecords: [],
        rectificationOrders: [],
        disposalRecords: [],
        changeLogs: []
      }
    } as any)

    const wrapper = mount(AssetRealEstateDetailPage, {
      global: {
        plugins: [ElementPlus],
        stubs: { DictTag: true }
      }
    })

    await flushPromises()

    const disposalSummary = wrapper.get('[data-testid="detail-summary-disposal"]')
    expect(disposalSummary.text()).toContain('未发起处置')
    expect(disposalSummary.text()).toContain('资产管理员 / 发起处置')
    expect(disposalSummary.text()).toContain('最近动作：未发起处置')

    const vm = wrapper.vm as any
    vm.handleTabChange('disposal')
    await flushPromises()

    const disposalClosureCard = wrapper.get('[data-testid="disposal-closure-card"]')
    expect(disposalClosureCard.text()).toContain('当前责任归口')
    expect(disposalClosureCard.text()).toContain('资产管理员')
    expect(disposalClosureCard.text()).toContain('责任动作')
    expect(disposalClosureCard.text()).toContain('发起处置')
    expect(disposalClosureCard.text()).toContain('下一步建议')
    expect(disposalClosureCard.text()).toContain('发起处置')
    expect(disposalClosureCard.text()).toContain('进入资产处置')
  })

  it('总览展示处置闭环摘要和最近处置动作', async () => {
    const wrapper = mount(AssetRealEstateDetailPage, {
      global: {
        plugins: [ElementPlus],
        stubs: { DictTag: true }
      }
    })

    await flushPromises()

    expect(wrapper.text()).toContain('处置闭环摘要')
    expect(wrapper.text()).toContain('已完成处置闭环')
    expect(wrapper.text()).toContain('最近处置动作')
    expect(wrapper.text()).toContain('已确认处置')
    expect(wrapper.text()).toContain('处置已闭环，可回看历史记录并归档留痕。')
  })

  it('总览和处置页签统一展示处置责任归口提示', async () => {
    const wrapper = mount(AssetRealEstateDetailPage, {
      global: {
        plugins: [ElementPlus],
        stubs: { DictTag: true }
      }
    })

    await flushPromises()

    expect(wrapper.text()).toContain('当前责任归口')
    expect(wrapper.text()).toContain('资产管理员')
    expect(wrapper.text()).toContain('归档回看')
    expect(wrapper.text()).toContain('处置已闭环，由资产管理员回看结果并完成留痕归档。')
    expect(wrapper.text()).toContain('最近责任人')
    expect(wrapper.text()).toContain('资产经理')

    const vm = wrapper.vm as any
    vm.handleTabChange('disposal')
    await flushPromises()

    expect(wrapper.text()).toContain('当前责任归口')
    expect(wrapper.text()).toContain('资产管理员')
    expect(wrapper.text()).toContain('归档回看')
    expect(wrapper.text()).toContain('处置已闭环，由资产管理员回看结果并完成留痕归档。')
    expect(wrapper.text()).toContain('最近责任人')
    expect(wrapper.text()).toContain('资产经理')
  })

  it('处置审批中场景统一提示当前责任归口和下一步动作', async () => {
    vi.mocked(realEstateApi.getRealEstateDetail).mockResolvedValueOnce({
      data: {
        assetId: 20001,
        assetCode: 'RE-2026-0001',
        assetName: '深圳研发办公楼A座',
        assetStatus: 'PENDING_DISPOSAL',
        ownerDeptName: '研发部门',
        useDeptName: '研发部门',
        responsibleUserName: '若依',
        locationName: '深圳南山科技园A座',
        ownershipCertNo: '粤(2024)深圳市不动产权第A0001号',
        landUseType: '研发办公',
        buildingArea: 18650.5,
        originalValue: 12500000,
        lastInventoryDate: '2026-03-01',
        sourceType: 'MANUAL',
        acquireType: 'PURCHASE',
        categoryName: '办公用房',
        enableDate: '2026-01-01',
        remark: '用于研发办公的不动产资产。'
      }
    } as any)

    vi.mocked(realEstateApi.getRealEstateLifecycle).mockResolvedValueOnce({
      data: {
        occupancyRecords: [],
        handoverRecords: [],
        inventoryRecords: [],
        rectificationOrders: [],
        disposalRecords: [
          {
            disposalId: 41,
            disposalNo: 'DIS-2026-0041',
            disposalType: '报废',
            disposalStatus: 'SUBMITTED',
            disposalDate: '2026-03-21',
            disposalReason: '设备老化报废'
          }
        ],
        changeLogs: [
          {
            logId: 501,
            bizType: 'LEDGER_UPDATE',
            changeDesc: '提交处置审批：DIS-2026-0041，意见：申请进入报废流程',
            operateBy: 'asset-admin',
            operateTime: '2026-03-21 11:00:00',
            beforeStatus: 'PENDING_DISPOSAL',
            afterStatus: 'PENDING_DISPOSAL'
          }
        ]
      }
    } as any)

    const wrapper = mount(AssetRealEstateDetailPage, {
      global: {
        plugins: [ElementPlus],
        stubs: { DictTag: true }
      }
    })

    await flushPromises()

    expect(wrapper.text()).toContain('处置审批中')
    expect(wrapper.text()).toContain('审批责任岗')
    expect(wrapper.text()).toContain('审批处理')
    expect(wrapper.text()).toContain('当前由审批责任岗处理，资产管理员需跟进审批反馈并准备补充材料。')
    expect(wrapper.text()).toContain('最近责任人')
    expect(wrapper.text()).toContain('asset-admin')
  })

  it('处置页签记录卡片展示最近责任归口视图', async () => {
    const wrapper = mount(AssetRealEstateDetailPage, {
      global: {
        plugins: [ElementPlus],
        stubs: { DictTag: true }
      }
    })

    await flushPromises()

    const vm = wrapper.vm as any
    vm.handleTabChange('disposal')
    await flushPromises()

    expect(wrapper.get('[data-testid="disposal-record-responsibility-9"]').text()).toContain(
      '资产管理员'
    )
    expect(wrapper.get('[data-testid="disposal-record-responsibility-9"]').text()).toContain(
      '归档回看'
    )
    expect(wrapper.get('[data-testid="disposal-record-responsibility-9"]').text()).toContain(
      '资产经理'
    )
  })

  it('总览展示整改闭环摘要和最近整改动作', async () => {
    vi.mocked(realEstateApi.getRealEstateLifecycle).mockResolvedValueOnce({
      data: {
        occupancyRecords: [],
        handoverRecords: [],
        inventoryRecords: [],
        rectificationOrders: [
          {
            rectificationId: 9101,
            rectificationNo: 'RC-2026-0010',
            assetId: 20001,
            taskId: 11,
            taskNo: 'INV-2026-0010',
            taskName: '门禁巡检',
            issueType: '损坏',
            issueDesc: '门禁控制器损坏',
            responsibleDeptName: '研发部门',
            responsibleUserName: '若依',
            rectificationStatus: 'PENDING',
            deadlineDate: '2026-03-25'
          },
          {
            rectificationId: 9102,
            rectificationNo: 'RC-2026-0011',
            assetId: 20001,
            taskId: 12,
            taskNo: 'INV-2026-0011',
            taskName: '消防巡检',
            issueType: '损坏',
            issueDesc: '消防闭门器损坏',
            responsibleDeptName: '研发部门',
            responsibleUserName: '若依',
            rectificationStatus: 'COMPLETED',
            approvalStatus: 'UNSUBMITTED',
            completedTime: '2026-03-21 09:30:00'
          },
          {
            rectificationId: 9103,
            rectificationNo: 'RC-2026-0012',
            assetId: 20001,
            taskId: 13,
            taskNo: 'INV-2026-0012',
            taskName: '设备巡检',
            issueType: '缺失',
            issueDesc: '监控补光灯缺失',
            responsibleDeptName: '研发部门',
            responsibleUserName: '若依',
            rectificationStatus: 'COMPLETED',
            approvalStatus: 'SUBMITTED',
            approvalSubmittedTime: '2026-03-21 10:30:00'
          },
          {
            rectificationId: 9104,
            rectificationNo: 'RC-2026-0013',
            assetId: 20001,
            taskId: 14,
            taskNo: 'INV-2026-0013',
            taskName: '门厅巡检',
            issueType: '损坏',
            issueDesc: '门厅玻璃门闭合异常',
            responsibleDeptName: '研发部门',
            responsibleUserName: '若依',
            rectificationStatus: 'COMPLETED',
            approvalStatus: 'REJECTED',
            approvalFinishedTime: '2026-03-21 13:00:00',
            latestApprovalOpinion: '请补充现场照片后重新提交审批'
          },
          {
            rectificationId: 9105,
            rectificationNo: 'RC-2026-0014',
            assetId: 20001,
            taskId: 15,
            taskNo: 'INV-2026-0014',
            taskName: '照明巡检',
            issueType: '损坏',
            issueDesc: '楼道照明恢复',
            responsibleDeptName: '研发部门',
            responsibleUserName: '若依',
            rectificationStatus: 'COMPLETED',
            approvalStatus: 'APPROVED',
            approvalFinishedTime: '2026-03-21 14:00:00'
          }
        ],
        disposalRecords: [],
        changeLogs: [
          {
            logId: 210,
            bizType: 'LEDGER_UPDATE',
            changeDesc: '发起整改单：RC-2026-0010，问题类型：损坏',
            operateBy: 'asset-admin',
            operateTime: '2026-03-21 08:00:00',
            beforeStatus: 'IN_USE',
            afterStatus: 'IN_USE'
          },
          {
            logId: 211,
            bizType: 'LEDGER_UPDATE',
            changeDesc: '完成整改单：RC-2026-0011，完成说明：已完成闭门器维修',
            operateBy: 'asset-admin',
            operateTime: '2026-03-21 09:30:00',
            beforeStatus: 'IN_USE',
            afterStatus: 'IN_USE'
          },
          {
            logId: 212,
            bizType: 'LEDGER_UPDATE',
            changeDesc: '提交整改审批：RC-2026-0012，意见：整改已完成，请审批',
            operateBy: 'asset-admin',
            operateTime: '2026-03-21 10:30:00',
            beforeStatus: 'IN_USE',
            afterStatus: 'IN_USE'
          },
          {
            logId: 213,
            bizType: 'LEDGER_UPDATE',
            changeDesc: '审批驳回：RC-2026-0013，意见：请补充现场照片后重新提交审批',
            operateBy: 'auditor',
            operateTime: '2026-03-21 13:00:00',
            beforeStatus: 'IN_USE',
            afterStatus: 'IN_USE'
          }
        ]
      }
    } as any)

    const wrapper = mount(AssetRealEstateDetailPage, {
      global: {
        plugins: [ElementPlus],
        stubs: { DictTag: true }
      }
    })

    await flushPromises()

    expect(wrapper.text()).toContain('整改闭环摘要')
    expect(wrapper.get('[data-testid="overview-rectification-count-pending-rectification"]').text()).toContain(
      '1'
    )
    expect(wrapper.get('[data-testid="overview-rectification-count-pending-submit"]').text()).toContain(
      '1'
    )
    expect(wrapper.get('[data-testid="overview-rectification-count-in-review"]').text()).toContain(
      '1'
    )
    expect(wrapper.get('[data-testid="overview-rectification-count-rejected-resubmit"]').text()).toContain(
      '1'
    )
    expect(wrapper.get('[data-testid="overview-rectification-count-approved-closed"]').text()).toContain(
      '1'
    )
    expect(wrapper.text()).toContain('整改闭环')
    expect(wrapper.text()).toContain('审批驳回待重提')
    expect(wrapper.text()).toContain('最近整改动作')
    expect(wrapper.text()).toContain('审批驳回')
    expect(wrapper.text()).toContain('请补充现场照片后重新提交审批')
  })

  it('整改页签复用总览闭环状态和下一步建议', async () => {
    vi.mocked(realEstateApi.getRealEstateLifecycle).mockResolvedValueOnce({
      data: {
        occupancyRecords: [],
        handoverRecords: [],
        inventoryRecords: [],
        rectificationOrders: [
          {
            rectificationId: 9201,
            rectificationNo: 'RC-2026-0020',
            assetId: 20001,
            taskId: 21,
            taskNo: 'INV-2026-0020',
            taskName: '门禁巡检',
            issueType: '损坏',
            issueDesc: '门禁控制器损坏',
            responsibleDeptName: '研发部门',
            responsibleUserName: '若依',
            rectificationStatus: 'PENDING',
            deadlineDate: '2026-03-28'
          },
          {
            rectificationId: 9202,
            rectificationNo: 'RC-2026-0021',
            assetId: 20001,
            taskId: 22,
            taskNo: 'INV-2026-0021',
            taskName: '消防巡检',
            issueType: '损坏',
            issueDesc: '消防闭门器损坏',
            responsibleDeptName: '研发部门',
            responsibleUserName: '若依',
            rectificationStatus: 'COMPLETED',
            approvalStatus: 'UNSUBMITTED',
            completedTime: '2026-03-21 09:30:00'
          }
        ],
        disposalRecords: [],
        changeLogs: [
          {
            logId: 220,
            bizType: 'LEDGER_UPDATE',
            changeDesc: '完成整改单：RC-2026-0021，完成说明：已完成闭门器维修',
            operateBy: 'asset-admin',
            operateTime: '2026-03-21 09:30:00',
            beforeStatus: 'IN_USE',
            afterStatus: 'IN_USE'
          }
        ]
      }
    } as any)

    const wrapper = mount(AssetRealEstateDetailPage, {
      global: {
        plugins: [ElementPlus],
        stubs: { DictTag: true }
      }
    })

    await flushPromises()

    expect(wrapper.text()).toContain('整改闭环')
    expect(wrapper.text()).toContain('待提交审批')

    const vm = wrapper.vm as any
    vm.handleTabChange('rectification')
    await flushPromises()

    expect(wrapper.text()).toContain('当前闭环状态')
    expect(wrapper.text()).toContain('待提交审批')
    expect(wrapper.text()).toContain('最近整改动作')
    expect(wrapper.text()).toContain('完成整改')
    expect(wrapper.text()).toContain('整改事实已经收口，但仍需尽快提交审批，避免闭环停在待提交阶段。')
  })

  it('总览生命周期轨迹对整改节点展示阶段标签', async () => {
    vi.mocked(realEstateApi.getRealEstateLifecycle).mockResolvedValueOnce({
      data: {
        occupancyRecords: [],
        handoverRecords: [],
        inventoryRecords: [],
        rectificationOrders: [],
        disposalRecords: [],
        changeLogs: [
          {
            logId: 301,
            bizType: 'LEDGER_UPDATE',
            changeDesc: '发起整改单：RC-2026-0020，问题类型：损坏',
            operateBy: 'asset-admin',
            operateTime: '2026-03-21 08:00:00',
            beforeStatus: 'IN_USE',
            afterStatus: 'IN_USE'
          },
          {
            logId: 302,
            bizType: 'LEDGER_UPDATE',
            changeDesc: '完成整改单：RC-2026-0020，完成说明：门禁检修已完成',
            operateBy: 'asset-admin',
            operateTime: '2026-03-21 09:00:00',
            beforeStatus: 'IN_USE',
            afterStatus: 'IN_USE'
          },
          {
            logId: 303,
            bizType: 'LEDGER_UPDATE',
            changeDesc: '提交整改审批：RC-2026-0020，意见：请审批',
            operateBy: 'asset-admin',
            operateTime: '2026-03-21 10:00:00',
            beforeStatus: 'IN_USE',
            afterStatus: 'IN_USE'
          },
          {
            logId: 304,
            bizType: 'LEDGER_UPDATE',
            changeDesc: '审批通过：RC-2026-0020，意见：同意闭环',
            operateBy: 'auditor',
            operateTime: '2026-03-21 11:00:00',
            beforeStatus: 'IN_USE',
            afterStatus: 'IN_USE'
          }
        ]
      }
    } as any)

    const wrapper = mount(AssetRealEstateDetailPage, {
      global: {
        plugins: [ElementPlus],
        stubs: { DictTag: true }
      }
    })

    await flushPromises()

    expect(wrapper.get('[data-testid="overview-rectification-event-301"]').text()).toContain('发起整改')
    expect(wrapper.get('[data-testid="overview-rectification-event-302"]').text()).toContain('完成整改')
    expect(wrapper.get('[data-testid="overview-rectification-event-303"]').text()).toContain('提交审批')
    expect(wrapper.get('[data-testid="overview-rectification-event-304"]').text()).toContain('审批通过')
    expect(wrapper.text()).toContain('整改已完成，下一步需要关注审批是否推进')
    expect(wrapper.text()).toContain('整改审批已通过，可归档回看')
  })

  it('总览生命周期轨迹对处置节点展示阶段标签', async () => {
    vi.mocked(realEstateApi.getRealEstateLifecycle).mockResolvedValueOnce({
      data: {
        occupancyRecords: [],
        handoverRecords: [],
        inventoryRecords: [],
        rectificationOrders: [],
        disposalRecords: [
          {
            disposalId: 31,
            disposalNo: 'DIS-2026-0031',
            disposalType: '报废',
            disposalStatus: 'CONFIRMED',
            disposalDate: '2026-03-21',
            confirmedBy: '资产经理',
            confirmedTime: '2026-03-21 13:00:00',
            disposalReason: '设备老化报废'
          }
        ],
        changeLogs: [
          {
            logId: 401,
            bizType: 'LEDGER_UPDATE',
            changeDesc: '发起处置：DIS-2026-0031，原因：设备老化报废',
            operateBy: 'asset-admin',
            operateTime: '2026-03-21 10:00:00',
            beforeStatus: 'IN_USE',
            afterStatus: 'PENDING_DISPOSAL'
          },
          {
            logId: 402,
            bizType: 'LEDGER_UPDATE',
            changeDesc: '提交处置审批：DIS-2026-0031，意见：申请进入报废流程',
            operateBy: 'asset-admin',
            operateTime: '2026-03-21 11:00:00',
            beforeStatus: 'PENDING_DISPOSAL',
            afterStatus: 'PENDING_DISPOSAL'
          },
          {
            logId: 403,
            bizType: 'LEDGER_UPDATE',
            changeDesc: '处置审批通过：DIS-2026-0031，意见：同意处置',
            operateBy: 'auditor',
            operateTime: '2026-03-21 12:00:00',
            beforeStatus: 'PENDING_DISPOSAL',
            afterStatus: 'PENDING_DISPOSAL'
          },
          {
            logId: 404,
            bizType: 'DISPOSAL_CONFIRM',
            changeDesc: '确认处置：DIS-2026-0031，结果：完成报废出清',
            operateBy: 'asset-admin',
            operateTime: '2026-03-21 13:00:00',
            beforeStatus: 'PENDING_DISPOSAL',
            afterStatus: 'DISPOSED'
          }
        ]
      }
    } as any)

    const wrapper = mount(AssetRealEstateDetailPage, {
      global: {
        plugins: [ElementPlus],
        stubs: { DictTag: true }
      }
    })

    await flushPromises()

    expect(wrapper.get('[data-testid="overview-disposal-event-401"]').text()).toContain('发起处置')
    expect(wrapper.get('[data-testid="overview-disposal-event-402"]').text()).toContain('提交审批')
    expect(wrapper.get('[data-testid="overview-disposal-event-403"]').text()).toContain('审批通过')
    expect(wrapper.get('[data-testid="overview-disposal-event-404"]').text()).toContain('确认处置')
    expect(wrapper.get('[data-testid="overview-disposal-responsibility-402"]').text()).toContain(
      '审批责任岗'
    )
    expect(wrapper.get('[data-testid="overview-disposal-responsibility-402"]').text()).toContain(
      'asset-admin'
    )
    expect(wrapper.get('[data-testid="overview-disposal-responsibility-404"]').text()).toContain(
      '资产管理员'
    )
    expect(wrapper.get('[data-testid="overview-disposal-responsibility-404"]').text()).toContain(
      '归档回看'
    )
    expect(wrapper.text()).toContain('处置已进入办理链路，下一步需要补齐申请并推进审批。')
    expect(wrapper.text()).toContain('处置审批已通过，下一步需要完成最终处置确认。')
    expect(wrapper.text()).toContain('处置已完成确认，可回看历史并归档留痕。')
  })

  it('占用页签支持打开变更占用抽屉', async () => {
    const wrapper = mount(AssetRealEstateDetailPage, {
      global: {
        plugins: [ElementPlus],
        stubs: { DictTag: true }
      }
    })

    await flushPromises()

    const vm = wrapper.vm as any
    vm.handleTabChange('occupancy')
    await flushPromises()

    await wrapper.get('[data-testid="occupancy-change-link-9101"]').trigger('click')
    expect(document.body.textContent || wrapper.text()).toContain('变更占用')
  })

  it('占用页签支持通过跨页签联动入口切到其他页签', async () => {
    const wrapper = mount(AssetRealEstateDetailPage, {
      global: {
        plugins: [ElementPlus],
        stubs: { DictTag: true }
      }
    })

    await flushPromises()

    const vm = wrapper.vm as any
    vm.handleTabChange('occupancy')
    await flushPromises()

    await wrapper.get('[data-testid="occupancy-tab-link-inspection"]').trigger('click')
    await flushPromises()
    expect(wrapper.get('[data-testid="inspection-reading-layout"]').exists()).toBe(true)

    vm.handleTabChange('occupancy')
    await flushPromises()

    await wrapper.get('[data-testid="occupancy-tab-link-rectification"]').trigger('click')
    await flushPromises()
    expect(wrapper.get('[data-testid="rectification-reading-layout"]').exists()).toBe(true)
  })

  it('跨页签联动后保留返回占用记忆，并支持回跳', async () => {
    const wrapper = mount(AssetRealEstateDetailPage, {
      global: {
        plugins: [ElementPlus],
        stubs: { DictTag: true }
      }
    })

    await flushPromises()

    const vm = wrapper.vm as any
    vm.handleTabChange('occupancy')
    await flushPromises()

    await wrapper.get('[data-testid="occupancy-tab-link-inspection"]').trigger('click')
    await flushPromises()

    expect(wrapper.get('[data-testid="detail-return-occupancy-link"]').exists()).toBe(true)
    expect(window.sessionStorage.getItem('asset-real-estate-detail-return-tab:20001')).toBe(
      'occupancy'
    )

    wrapper.unmount()

    const remountWrapper = mount(AssetRealEstateDetailPage, {
      global: {
        plugins: [ElementPlus],
        stubs: { DictTag: true }
      }
    })

    await flushPromises()

    expect(remountWrapper.get('[data-testid="detail-return-occupancy-link"]').exists()).toBe(true)

    await remountWrapper.get('[data-testid="detail-return-occupancy-link"]').trigger('click')
    await flushPromises()

    expect(remountWrapper.get('[data-testid="occupancy-reading-layout"]').exists()).toBe(true)
    expect(window.sessionStorage.getItem('asset-real-estate-detail-return-tab:20001')).toBeNull()
  })

  it('跨页签联动后展示回跳来源提示，并在刷新后恢复', async () => {
    const wrapper = mount(AssetRealEstateDetailPage, {
      global: {
        plugins: [ElementPlus],
        stubs: { DictTag: true }
      }
    })

    await flushPromises()

    const vm = wrapper.vm as any
    vm.handleTabChange('occupancy')
    await flushPromises()

    await wrapper.get('[data-testid="occupancy-tab-link-inspection"]').trigger('click')
    await flushPromises()

    expect(wrapper.get('[data-testid="detail-return-occupancy-source"]').text()).toContain(
      '来自：看巡检联动'
    )

    wrapper.unmount()

    const remountWrapper = mount(AssetRealEstateDetailPage, {
      global: {
        plugins: [ElementPlus],
        stubs: { DictTag: true }
      }
    })

    await flushPromises()

    expect(remountWrapper.get('[data-testid="detail-return-occupancy-source"]').text()).toContain(
      '来自：看巡检联动'
    )

    await remountWrapper.get('[data-testid="detail-return-occupancy-link"]').trigger('click')
    await flushPromises()

    expect(remountWrapper.find('[data-testid="detail-return-occupancy-source"]').exists()).toBe(false)
  })

  it('跨页签联动后立即卸载占用面板时仍会持久化来源链路统计', async () => {
    const wrapper = mount(AssetRealEstateDetailPage, {
      global: {
        plugins: [ElementPlus],
        stubs: { DictTag: true }
      }
    })

    await flushPromises()

    const vm = wrapper.vm as any
    vm.handleTabChange('occupancy')
    await flushPromises()

    await wrapper.get('[data-testid="occupancy-tab-link-inspection"]').trigger('click')
    await flushPromises()

    const storageKey = 'asset-real-estate-occupancy-link-stats:RE-2026-0001'
    const persistedRaw = window.localStorage.getItem(storageKey)
    expect(persistedRaw).not.toBeNull()

    const persisted = JSON.parse(String(persistedRaw))
    expect(persisted.counts.inspection).toBe(1)
    expect(persisted.lastTargetLabel).toBe('看巡检联动')

    wrapper.unmount()

    const remountWrapper = mount(AssetRealEstateDetailPage, {
      global: {
        plugins: [ElementPlus],
        stubs: { DictTag: true }
      }
    })

    await flushPromises()

    await remountWrapper.get('[data-testid="detail-return-occupancy-link"]').trigger('click')
    await flushPromises()
    await remountWrapper.get('[data-testid="occupancy-governance-toggle"]').trigger('click')
    await flushPromises()

    expect(remountWrapper.get('[data-testid="occupancy-link-stat-inspection"]').text()).toContain('1')
    expect(remountWrapper.get('[data-testid="occupancy-link-stat-last-target"]').text()).toContain(
      '看巡检联动'
    )
  })

  it('变更占用抽屉支持原因模板快捷填充', async () => {
    const wrapper = mount(AssetRealEstateDetailPage, {
      global: {
        plugins: [ElementPlus],
        stubs: { DictTag: true }
      }
    })

    await flushPromises()

    const vm = wrapper.vm as any
    vm.handleTabChange('occupancy')
    await flushPromises()

    await wrapper.get('[data-testid="occupancy-change-link-9101"]').trigger('click')
    await flushPromises()

    const changeTemplate = Array.from(
      document.body.querySelectorAll('[data-testid="occupancy-change-template-0"]')
    ).at(-1) as HTMLButtonElement | undefined
    changeTemplate?.dispatchEvent(new MouseEvent('click', { bubbles: true }))
    await flushPromises()

    expect(vm.occupancyForm.changeReason).toBe('部门调整交接')
  })

  it('释放占用抽屉支持释放原因模板快捷填充', async () => {
    const wrapper = mount(AssetRealEstateDetailPage, {
      global: {
        plugins: [ElementPlus],
        stubs: { DictTag: true }
      }
    })

    await flushPromises()

    const vm = wrapper.vm as any
    vm.handleTabChange('occupancy')
    await flushPromises()

    await wrapper.get('[data-testid="occupancy-release-link-9101"]').trigger('click')
    await flushPromises()

    const releaseTemplate = Array.from(
      document.body.querySelectorAll('[data-testid="occupancy-release-template-0"]')
    ).at(-1) as HTMLButtonElement | undefined
    releaseTemplate?.dispatchEvent(new MouseEvent('click', { bubbles: true }))
    await flushPromises()

    expect(vm.occupancyForm.releaseReason).toBe('部门搬离释放')
  })

  it('兼容旧巡检子路由入口，并支持任务明细和整改跳转', async () => {
    routeState.path = '/asset/real-estate/detail/20001/inspection'
    routeState.fullPath = routeState.path

    const wrapper = mount(AssetRealEstateDetailPage, {
      global: {
        plugins: [ElementPlus],
        stubs: { DictTag: true }
      }
    })

    await flushPromises()

    expect(wrapper.get('[data-testid="real-estate-detail-reading-page"]').exists()).toBe(true)
    expect(wrapper.get('[data-testid="inspection-reading-layout"]').exists()).toBe(true)
    expect(wrapper.get('[data-testid="inspection-record-list"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('巡检任务记录')
    expect(wrapper.text()).toContain('未发起整改')
    expect(wrapper.text()).toContain('待整改')
    expect(wrapper.text()).toContain('已闭环')
    expect(wrapper.get('[data-testid="inspection-status-not-created"]').text()).toContain('1')
    expect(wrapper.get('[data-testid="inspection-status-pending"]').text()).toContain('1')
    expect(wrapper.get('[data-testid="inspection-status-completed"]').text()).toContain('0')
    expect(wrapper.text()).toContain('发起整改')
    expect(wrapper.text()).toContain('查看整改')
    expect(wrapper.text()).toContain('整改联动：未发起整改')
    expect(wrapper.text()).toContain('请尽快发起整改并明确责任人和期限')
    expect(wrapper.text()).toContain('整改联动：待整改')
    expect(wrapper.text()).toContain('责任归口：研发部门 / 若依')
    expect(wrapper.text()).toContain('整改期限：2026-03-20')

    await wrapper.get('[data-testid="inspection-task-link-6"]').trigger('click')
    expect(mockPush).toHaveBeenLastCalledWith('/asset/real-estate/detail/20001/inspection-task/6')
    expect(window.sessionStorage.getItem('asset-real-estate-detail-tab:20001')).toBe('inspection')

    await wrapper.get('[data-testid="rectification-create-link-6"]').trigger('click')
    expect(mockPush).toHaveBeenLastCalledWith({
      path: '/asset/real-estate/detail/20001/rectification/create',
      query: {
        taskId: '6'
      }
    })
    expect(window.sessionStorage.getItem('asset-real-estate-detail-tab:20001')).toBe('rectification')
  })

  it('巡检页签对已闭环整改直接展示闭环摘要', async () => {
    routeState.path = '/asset/real-estate/detail/20001/inspection'
    routeState.fullPath = routeState.path
    vi.mocked(realEstateApi.getRealEstateLifecycle).mockResolvedValueOnce({
      data: {
        handoverRecords: [],
        inventoryRecords: [
          {
            itemId: 67,
            taskId: 7,
            taskNo: 'INV-2026-0009',
            taskName: '消防设施专项巡检',
            inventoryResult: 'DAMAGED',
            followUpAction: 'UPDATE_LEDGER',
            followUpBizId: 9001,
            processStatus: 'PROCESSED',
            checkedBy: '资产管理员',
            checkedTime: '2026-03-05 10:00:00',
            resultDesc: '消防闭门器损坏'
          }
        ],
        rectificationOrders: [
          {
            rectificationId: 9001,
            rectificationNo: 'RC-2026-0001',
            assetId: 20001,
            taskId: 7,
            taskNo: 'INV-2026-0009',
            taskName: '消防设施专项巡检',
            issueType: '损坏',
            issueDesc: '消防闭门器损坏',
            responsibleDeptName: '研发部门',
            responsibleUserName: '若依',
            rectificationStatus: 'COMPLETED',
            deadlineDate: '2026-03-20',
            completedTime: '2026-03-21 14:49:04',
            completionDesc: '已完成现场修复并复核，闭门器恢复正常使用。',
            acceptanceRemark: '资产管理员现场验收通过，允许关闭本次整改。'
          }
        ],
        disposalRecords: [],
        changeLogs: []
      }
    } as any)

    const wrapper = mount(AssetRealEstateDetailPage, {
      global: {
        plugins: [ElementPlus],
        stubs: { DictTag: true }
      }
    })

    await flushPromises()

    expect(wrapper.get('[data-testid="inspection-status-not-created"]').text()).toContain('0')
    expect(wrapper.get('[data-testid="inspection-status-pending"]').text()).toContain('0')
    expect(wrapper.get('[data-testid="inspection-status-completed"]').text()).toContain('1')
    expect(wrapper.text()).toContain('整改联动：已闭环')
    expect(wrapper.text()).toContain('已完成整改并通过验收，可归档留痕')
    expect(wrapper.text()).toContain('完成时间：2026-03-21 14:49:04')
    expect(wrapper.text()).toContain('验收备注：资产管理员现场验收通过，允许关闭本次整改。')
    expect(wrapper.find('[data-testid="rectification-edit-link-9001"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="rectification-complete-link-9001"]').exists()).toBe(false)
  })

  it('从缓存恢复整改页签，并支持跳转整改单和资产处置', async () => {
    window.sessionStorage.setItem('asset-real-estate-detail-tab:20001', 'rectification')

    const wrapper = mount(AssetRealEstateDetailPage, {
      global: {
        plugins: [ElementPlus],
        stubs: { DictTag: true }
      }
    })

    await flushPromises()

    expect(wrapper.get('[data-testid="real-estate-detail-reading-page"]').exists()).toBe(true)
    expect(wrapper.get('[data-testid="rectification-reading-layout"]').exists()).toBe(true)
    expect(wrapper.get('[data-testid="rectification-record-list"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('整改单列表')
    expect(wrapper.text()).toContain('RC-2026-0001')
    expect(wrapper.text()).toContain('当前动作')
    expect(wrapper.text()).toContain('已逾期')
    expect(wrapper.text()).toContain('尽快完成整改并提交完成信息')
    expect(wrapper.text()).toContain('完成整改')

    const vm = wrapper.vm as any
    vm.goToEditRectification(9001)
    expect(mockPush).toHaveBeenLastCalledWith('/asset/real-estate/detail/20001/rectification/edit/9001')
    expect(window.sessionStorage.getItem('asset-real-estate-detail-tab:20001')).toBe('rectification')

    await wrapper.get('[data-testid="rectification-complete-link-9001"]').trigger('click')
    expect(mockPush).toHaveBeenLastCalledWith('/asset/real-estate/detail/20001/rectification/complete/9001')
    expect(window.sessionStorage.getItem('asset-real-estate-detail-tab:20001')).toBe('rectification')

    vm.handleTabChange('disposal')
    await flushPromises()

    await wrapper.get('[data-testid="disposal-jump-button"]').trigger('click')
    expect(mockPush).toHaveBeenLastCalledWith({
      path: '/asset/disposal',
      query: {
        tab: 'record',
        assetId: '20001',
        assetCode: 'RE-2026-0001',
        assetName: '深圳研发办公楼A座',
        source: 'real-estate-disposal-tab',
        intent: 'view'
      }
    })
    expect(window.sessionStorage.getItem('asset-real-estate-detail-tab:20001')).toBe('disposal')
  })

  it('已完成整改单在整改页签直接展示完成说明与验收备注', async () => {
    window.sessionStorage.setItem('asset-real-estate-detail-tab:20001', 'rectification')
    vi.mocked(realEstateApi.getRealEstateLifecycle).mockResolvedValueOnce({
      data: {
        handoverRecords: [],
        inventoryRecords: [],
        rectificationOrders: [
          {
            rectificationId: 9001,
            rectificationNo: 'RC-2026-0001',
            assetId: 20001,
            taskId: 7,
            taskNo: 'INV-2026-0009',
            taskName: '消防设施专项巡检',
            issueType: '损坏',
            issueDesc: '消防闭门器损坏',
            responsibleDeptName: '研发部门',
            responsibleUserName: '若依',
            rectificationStatus: 'COMPLETED',
            deadlineDate: '2026-03-20',
            completedTime: '2026-03-21 14:49:04',
            completionDesc: '已完成现场修复并复核，闭门器恢复正常使用。',
            acceptanceRemark: '资产管理员现场验收通过，允许关闭本次整改。'
          }
        ],
        disposalRecords: [],
        changeLogs: []
      }
    } as any)

    const wrapper = mount(AssetRealEstateDetailPage, {
      global: {
        plugins: [ElementPlus],
        stubs: { DictTag: true }
      }
    })

    await flushPromises()

    expect(wrapper.text()).toContain('闭环结果')
    expect(wrapper.text()).toContain('已完成整改并通过验收，可归档留痕')
    expect(wrapper.text()).toContain('完成时间')
    expect(wrapper.text()).toContain('2026-03-21 14:49:04')
    expect(wrapper.text()).toContain('完成说明')
    expect(wrapper.text()).toContain('已完成现场修复并复核，闭门器恢复正常使用。')
    expect(wrapper.text()).toContain('验收备注')
    expect(wrapper.text()).toContain('资产管理员现场验收通过，允许关闭本次整改。')
    expect(wrapper.find('[data-testid="rectification-complete-link-9001"]').exists()).toBe(false)
  })
})
