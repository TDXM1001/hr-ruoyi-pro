import { beforeEach, describe, expect, it, vi } from 'vitest'
import { reactive } from 'vue'
import { flushPromises, mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import AssetRealEstateInspectionTaskPage from '@/views/asset/real-estate/inspection-task/index.vue'

const mockPush = vi.fn()
const routeState = reactive({
  params: { assetId: '20001', taskId: '6' },
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

vi.mock('@/store/modules/user', () => {
  return {
    useUserStore: () => ({
      permissions: ['asset:realEstate:edit']
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
        lastInventoryDate: '2026-03-01'
      }
    })
  }
})

vi.mock('@/api/asset/inventory', () => {
  return {
    getInventoryTask: vi.fn().mockResolvedValue({
      data: {
        taskId: 6,
        taskNo: 'INV-2026-0008',
        taskName: '一季度不动产巡检',
        plannedDate: '2026-03-01',
        taskStatus: 'COMPLETED'
      }
    }),
    listInventoryTaskAssets: vi.fn().mockResolvedValue({
      rows: [
        {
          itemId: 66,
          assetId: 20001,
          assetCode: 'RE-2026-0001',
          assetName: '深圳研发办公楼A座',
          inventoryResult: 'LOCATION_DIFF',
          followUpAction: 'UPDATE_LEDGER',
          processStatus: 'PENDING',
          checkedBy: '资产管理员',
          checkedTime: '2026-03-01 09:30:00',
          resultDesc: '房间实际使用人与台账不一致'
        }
      ],
      total: 1
    })
  }
})

describe('AssetRealEstateInspectionTaskPage 点测', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockPush.mockReset()
    routeState.params = { assetId: '20001', taskId: '6' }
    routeState.query = {}
    routeState.meta = {}
  })

  it('应渲染巡检任务明细和整改入口', async () => {
    const wrapper = mount(AssetRealEstateInspectionTaskPage, {
      global: {
        plugins: [ElementPlus]
      }
    })

    await flushPromises()

    expect(wrapper.text()).toContain('巡检任务明细')
    expect(wrapper.text()).toContain('一季度不动产巡检')
    expect(wrapper.text()).toContain('发起整改')

    await wrapper.get('[data-testid="inspection-task-create-rectification"]').trigger('click')

    expect(mockPush).toHaveBeenCalledWith({
      path: '/asset/real-estate/detail/20001/rectification/create',
      query: {
        taskId: '6'
      }
    })
  })
})
