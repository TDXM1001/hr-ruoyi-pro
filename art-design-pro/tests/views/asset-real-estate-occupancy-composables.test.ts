import { afterEach, describe, expect, it, vi } from 'vitest'
import { computed, nextTick, ref } from 'vue'
import type { AssetRealEstateOccupancyRecord } from '@/api/asset/real-estate'
import { useOccupancyState } from '@/views/asset/real-estate/detail/components/occupancy/useOccupancyState'
import { useOccupancyGovernance } from '@/views/asset/real-estate/detail/components/occupancy/useOccupancyGovernance'

const createGovernanceOptions = (
  occupancyRecords: AssetRealEstateOccupancyRecord[],
  overrides: Partial<Parameters<typeof useOccupancyGovernance>[0]> = {}
) => {
  const statusFilter = ref<'ALL' | 'ACTIVE' | 'RELEASED'>('ALL')
  const timeFilter = ref<'ALL' | '7D' | '30D' | '90D' | 'CUSTOM'>('ALL')
  const sortDirection = ref<'DESC' | 'ASC'>('DESC')
  const groupViewMode = ref<'LIST' | 'GROUPED' | 'ANNOTATION'>('LIST')
  const keyword = ref('')
  const customRangeDraft = { start: '', end: '' }
  const customRangeApplied = { start: '', end: '' }
  const detailData = ref({
    assetCode: 'RE-2026-0001'
  })

  return {
    detailData,
    statusFilter,
    timeFilter,
    sortDirection,
    groupViewMode,
    keyword,
    customRangeDraft,
    customRangeApplied,
    filteredRecords: computed(() => occupancyRecords),
    activeRecord: computed(() => occupancyRecords[0]),
    sortedRecords: computed(() => occupancyRecords),
    buildFilterState: () => ({
      statusFilter: statusFilter.value,
      timeFilter: timeFilter.value,
      sortDirection: sortDirection.value,
      keyword: keyword.value,
      customRangeDraftStart: customRangeDraft.start,
      customRangeDraftEnd: customRangeDraft.end,
      customRangeAppliedStart: customRangeApplied.start,
      customRangeAppliedEnd: customRangeApplied.end
    }),
    applyFilterState: vi.fn(),
    resetFocusedRecord: vi.fn(),
    historyListRef: ref(),
    onSwitchTab: vi.fn(),
    ...overrides
  }
}

describe('occupancy composables', () => {
  afterEach(() => {
    vi.useRealTimers()
    vi.restoreAllMocks()
    window.localStorage.clear()
  })

  it('useOccupancyState derives active occupancy and applies released filter', async () => {
    const detailData = ref({
      assetCode: 'RE-2026-0001',
      useDeptName: 'R&D Department',
      responsibleUserName: 'Zhang San',
      locationName: 'Block B'
    })
    const occupancyRecords = ref<AssetRealEstateOccupancyRecord[]>([
      {
        occupancyId: 9101,
        occupancyNo: 'OCC-2026-9001',
        occupancyStatus: 'ACTIVE',
        useDeptName: 'R&D Department',
        responsibleUserName: 'Zhang San',
        locationName: 'Block B',
        startDate: '2026-03-22',
        changeReason: 'Initial assignment'
      },
      {
        occupancyId: 9100,
        occupancyNo: 'OCC-2026-8999',
        occupancyStatus: 'RELEASED',
        useDeptName: 'Admin Department',
        responsibleUserName: 'Li Si',
        locationName: 'Block A',
        startDate: '2026-03-01',
        endDate: '2026-03-20',
        changeReason: 'Historical occupancy',
        releaseReason: 'Department relocation'
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

    const occupancyRecords = ref<AssetRealEstateOccupancyRecord[]>([
      {
        occupancyId: 9101,
        occupancyNo: 'OCC-2026-9001',
        occupancyStatus: 'ACTIVE',
        useDeptName: 'R&D Department',
        responsibleUserName: 'Zhang San',
        locationName: 'Block B',
        startDate: '2026-03-22',
        changeReason: 'Initial assignment'
      }
    ])
    const onSwitchTab = vi.fn()

    const governance = useOccupancyGovernance(
      createGovernanceOptions(occupancyRecords.value, {
        onSwitchTab
      })
    )

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

  it('useOccupancyGovernance filters governance activities by type and keyword', () => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2026-03-23T11:00:00+08:00'))

    const occupancyRecords = ref<AssetRealEstateOccupancyRecord[]>([
      {
        occupancyId: 9101,
        occupancyNo: 'OCC-2026-9001',
        occupancyStatus: 'ACTIVE',
        useDeptName: 'R&D Department',
        responsibleUserName: 'Zhang San',
        locationName: 'Block B',
        startDate: '2026-03-22',
        changeReason: 'Initial assignment'
      }
    ])

    const governance = useOccupancyGovernance(createGovernanceOptions(occupancyRecords.value))

    governance.recordGovernanceActivity('SNAPSHOT', '保存趋势快照', '三月快照', '已保存趋势快照')
    governance.recordGovernanceActivity('RESET', '只重置趋势', '趋势数据', '已清空趋势事件 2 条')

    governance.governanceActivityFilter.value = 'SNAPSHOT'
    governance.governanceActivityKeyword.value = '快照'

    expect(governance.filteredGovernanceActivities.value).toHaveLength(1)
    expect(governance.filteredGovernanceActivities.value[0]?.type).toBe('SNAPSHOT')
    expect(governance.filteredGovernanceActivities.value[0]?.target).toBe('三月快照')
  })

  it('useOccupancyGovernance manages trend drilldown and snapshot restore', async () => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2026-03-23T14:00:00+08:00'))

    const occupancyRecords = [
      {
        occupancyId: 9101,
        occupancyNo: 'OCC-2026-9001',
        occupancyStatus: 'ACTIVE',
        useDeptName: 'R&D Department',
        responsibleUserName: 'Zhang San',
        locationName: 'Block B',
        startDate: '2026-03-22',
        changeReason: 'Initial assignment'
      }
    ] satisfies AssetRealEstateOccupancyRecord[]
    const applyFilterState = vi.fn()
    const options = createGovernanceOptions(occupancyRecords, {
      applyFilterState
    })
    const governance = useOccupancyGovernance(options)

    governance.emitTabSwitch('inspection')
    governance.setLinkStatsWindow('30D')

    const todayTrend = governance.linkTrendItems.value.find((item) => item.count > 0)
    expect(todayTrend).toBeTruthy()

    governance.toggleTrendDrilldown(todayTrend!)

    expect(governance.trendDrilldown.value?.date).toBe(todayTrend?.date)
    expect(options.timeFilter.value).toBe('CUSTOM')
    expect(options.customRangeApplied.start).toBe(todayTrend?.date)
    expect(options.customRangeApplied.end).toBe(todayTrend?.date)

    governance.trendSnapshotName.value = '三月趋势快照'
    governance.saveTrendSnapshot()

    expect(governance.savedTrendSnapshotHistory.value).toHaveLength(1)
    expect(governance.savedTrendSnapshot.value?.name).toBe('三月趋势快照')

    governance.setLinkStatsWindow('7D')
    governance.clearTrendDrilldown()
    await governance.applySavedTrendSnapshot(governance.savedTrendSnapshotHistory.value[0])

    expect(governance.linkStatsWindow.value).toBe('30D')
    expect(governance.trendDrilldown.value?.date).toBe(todayTrend?.date)
    expect(applyFilterState).toHaveBeenCalled()
  })
})
