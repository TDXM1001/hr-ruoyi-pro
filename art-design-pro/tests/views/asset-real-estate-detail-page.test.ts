import { beforeEach, describe, expect, it, vi } from 'vitest'
import { reactive, ref } from 'vue'
import { flushPromises, mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import AssetRealEstateDetailPage from '@/views/asset/real-estate/detail/index.vue'

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
    })
  }
})

describe('AssetRealEstateDetailPage 详情壳', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockPush.mockReset()
    window.sessionStorage.clear()
    routeState.params = { assetId: '20001' }
    routeState.query = {}
    routeState.meta = {}
    routeState.path = '/asset/real-estate/detail/20001'
    routeState.fullPath = routeState.path
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

    expect(wrapper.text()).toContain('当前占用关系')
    expect(wrapper.text()).toContain('交接轨迹')
    expect(mockPush).not.toHaveBeenCalled()
    expect(window.sessionStorage.getItem('asset-real-estate-detail-tab:20001')).toBe('occupancy')
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

    expect(wrapper.text()).toContain('巡检任务记录')
    expect(wrapper.text()).toContain('发起整改')
    expect(wrapper.text()).toContain('查看整改')

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

  it('从缓存恢复整改页签，并支持跳转整改单和资产处置', async () => {
    window.sessionStorage.setItem('asset-real-estate-detail-tab:20001', 'rectification')

    const wrapper = mount(AssetRealEstateDetailPage, {
      global: {
        plugins: [ElementPlus],
        stubs: { DictTag: true }
      }
    })

    await flushPromises()

    expect(wrapper.text()).toContain('整改单列表')
    expect(wrapper.text()).toContain('RC-2026-0001')

    const vm = wrapper.vm as any
    vm.goToEditRectification(9001)
    expect(mockPush).toHaveBeenLastCalledWith('/asset/real-estate/detail/20001/rectification/edit/9001')
    expect(window.sessionStorage.getItem('asset-real-estate-detail-tab:20001')).toBe('rectification')

    vm.handleTabChange('disposal')
    await flushPromises()

    await wrapper.get('[data-testid="disposal-jump-button"]').trigger('click')
    expect(mockPush).toHaveBeenLastCalledWith({
      path: '/asset/disposal',
      query: {
        tab: 'record',
        assetId: '20001',
        assetCode: 'RE-2026-0001'
      }
    })
    expect(window.sessionStorage.getItem('asset-real-estate-detail-tab:20001')).toBe('disposal')
  })
})
