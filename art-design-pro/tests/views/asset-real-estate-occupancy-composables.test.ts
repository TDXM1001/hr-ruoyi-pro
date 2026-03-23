import { afterEach, describe, expect, it, vi } from 'vitest'
import { computed, nextTick, ref } from 'vue'
import type { AssetRealEstateOccupancyRecord } from '@/api/asset/real-estate'
import { useOccupancyState } from '@/views/asset/real-estate/detail/components/occupancy/useOccupancyState'
import { useOccupancyGovernance } from '@/views/asset/real-estate/detail/components/occupancy/useOccupancyGovernance'

describe('occupancy composables', () => {
  afterEach(() => {
    vi.useRealTimers()
    vi.restoreAllMocks()
    window.localStorage.clear()
  })

  it('useOccupancyState derives active occupancy and applies released filter', async () => {
    const detailData = ref({
      assetCode: 'RE-2026-0001',
      useDeptName: '研发部门',
      responsibleUserName: '张三',
      locationName: '科技园A座'
    })
    const occupancyRecords = ref<AssetRealEstateOccupancyRecord[]>([
      {
        occupancyId: 9101,
        occupancyNo: 'OCC-2026-9001',
        occupancyStatus: 'ACTIVE',
        useDeptName: '研发部门',
        responsibleUserName: '张三',
        locationName: '科技园A座',
        startDate: '2026-03-22',
        changeReason: '首次发起'
      },
      {
        occupancyId: 9100,
        occupancyNo: 'OCC-2026-8999',
        occupancyStatus: 'RELEASED',
        useDeptName: '行政部门',
        responsibleUserName: '李四',
        locationName: '科技园B座',
        startDate: '2026-03-01',
        endDate: '2026-03-20',
        changeReason: '历史占用',
        releaseReason: '部门搬迁'
      }
    ])

    const state = useOccupancyState({
      detailData,
      occupancyRecords
    })

    expect(state.activeRecord.value?.occupancyNo).toBe('OCC-2026-9001')
    expect(state.latestReleasedRecord.value?.occupancyNo).toBe('OCC-2026-8999')
    expect(state.ledgerSyncTagType.value).toBe('success')

    state.applyLinkedFilter('RELEASED')
    await nextTick()

    expect(state.filteredRecords.value.map((item) => item.occupancyNo)).toEqual(['OCC-2026-8999'])
  })

  it('useOccupancyGovernance tracks link stats while governance panel stays script-driven', () => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2026-03-23T10:00:00+08:00'))

    const detailData = ref({
      assetCode: 'RE-2026-0001'
    })
    const occupancyRecords = ref<AssetRealEstateOccupancyRecord[]>([
      {
        occupancyId: 9101,
        occupancyNo: 'OCC-2026-9001',
        occupancyStatus: 'ACTIVE',
        useDeptName: '研发部门',
        responsibleUserName: '张三',
        locationName: '科技园A座',
        startDate: '2026-03-22',
        changeReason: '首次发起'
      }
    ])
    const onSwitchTab = vi.fn()

    const governance = useOccupancyGovernance({
      detailData,
      filteredRecords: computed(() => occupancyRecords.value),
      activeRecord: computed(() => occupancyRecords.value[0]),
      sortedRecords: computed(() => occupancyRecords.value),
      buildFilterState: () => ({
        statusFilter: 'ALL',
        timeFilter: 'ALL',
        sortDirection: 'DESC',
        keyword: '',
        customRangeDraftStart: '',
        customRangeDraftEnd: '',
        customRangeAppliedStart: '',
        customRangeAppliedEnd: ''
      }),
      applyFilterState: vi.fn(),
      resetFocusedRecord: vi.fn(),
      historyListRef: ref(),
      getStatusLabel: (status?: string) => (String(status).toUpperCase() === 'ACTIVE' ? '有效占用' : '已释放'),
      buildAnnotationStatusNote: (record) => `状态：${record.occupancyNo}`,
      buildAnnotationChangeNote: (record) => `变更：${record.changeReason || '-'}`,
      buildAnnotationReleaseNote: (record) => `释放：${record.releaseReason || '-'}`,
      onSwitchTab
    })

    expect(governance.governanceOpen.value).toBe(false)
    expect(governance.exportConfigOpen.value).toBe(false)

    governance.toggleGovernancePanel()
    governance.emitTabSwitch('inspection')

    expect(governance.governanceOpen.value).toBe(true)
    expect(governance.exportConfigOpen.value).toBe(true)
    expect(governance.linkStatItems.value.find((item) => item.key === 'inspection')?.count).toBe(1)
    expect(governance.displayedLinkLastTargetLabel.value).toBe('看巡检联动')
    expect(onSwitchTab).toHaveBeenCalledWith('inspection')
  })
})
