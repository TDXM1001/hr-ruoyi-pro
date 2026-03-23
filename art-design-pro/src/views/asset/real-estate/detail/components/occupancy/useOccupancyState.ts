import { computed, nextTick, reactive, ref, watch, type Ref } from 'vue'
import type { AssetRealEstateOccupancyRecord } from '@/api/asset/real-estate'
import {
  buildAnnotationChangeNote,
  buildAnnotationPreviewItems,
  buildAnnotationReleaseNote,
  buildAnnotationStatusNote,
  buildCompareItems,
  defaultFilterState,
  getRecordKey,
  parseDateValue,
  type AnnotationTemplateKey,
  type GroupViewMode,
  type OccupancyFilterState,
  type SortDirection,
  type StatusFilter,
  type TimeFilter
} from './occupancyShared'

interface UseOccupancyStateOptions {
  detailData: Ref<Record<string, any>>
  occupancyRecords: Ref<AssetRealEstateOccupancyRecord[]>
}

export const useOccupancyState = ({ detailData, occupancyRecords }: UseOccupancyStateOptions) => {
  const historyListRef = ref<HTMLElement>()
  const statusFilter = ref<StatusFilter>('ALL')
  const timeFilter = ref<TimeFilter>('ALL')
  const sortDirection = ref<SortDirection>('DESC')
  const groupViewMode = ref<GroupViewMode>('LIST')
  const annotationTemplate = ref<AnnotationTemplateKey>('standard')
  const focusedRecordKey = ref('')
  const filtersReady = ref(false)
  const keyword = ref('')
  const recordRefs = new Map<string, HTMLElement>()
  const customRangeDraft = reactive({
    start: '',
    end: ''
  })
  const customRangeApplied = reactive({
    start: '',
    end: ''
  })

  const storageKey = computed(() => {
    const assetKey = String(detailData.value.assetCode || detailData.value.assetId || '').trim()
    return assetKey ? `asset-real-estate-occupancy-filters:${assetKey}` : ''
  })

  const resolveTimelineDate = (record: AssetRealEstateOccupancyRecord) => {
    return parseDateValue(record.endDate || record.startDate)
  }

  const sortedRecords = computed(() => {
    return [...occupancyRecords.value].sort((left, right) => {
      const rightTime = resolveTimelineDate(right)?.getTime() || 0
      const leftTime = resolveTimelineDate(left)?.getTime() || 0
      return rightTime - leftTime
    })
  })

  const activeRecord = computed(() => {
    return sortedRecords.value.find(
      (record) => String(record.occupancyStatus || '').toUpperCase() === 'ACTIVE'
    )
  })

  const latestReleasedRecord = computed(() => {
    return sortedRecords.value.find(
      (record) => String(record.occupancyStatus || '').toUpperCase() === 'RELEASED'
    )
  })

  const isLedgerSynced = computed(() => {
    if (!activeRecord.value) {
      return false
    }
    return (
      String(detailData.value.useDeptName || '') === String(activeRecord.value.useDeptName || '') &&
      String(detailData.value.responsibleUserName || '') ===
        String(activeRecord.value.responsibleUserName || '') &&
      String(detailData.value.locationName || '') === String(activeRecord.value.locationName || '')
    )
  })

  const ledgerSyncTagType = computed(() => (isLedgerSynced.value ? 'success' : 'warning'))

  const ledgerSyncCompareItems = computed(() => {
    if (!activeRecord.value) {
      return []
    }
    return buildCompareItems(detailData.value || {}, activeRecord.value)
  })

  const lastChangeCompareItems = computed(() => {
    if (!activeRecord.value || !latestReleasedRecord.value) {
      return []
    }
    return buildCompareItems(latestReleasedRecord.value, activeRecord.value)
  })

  const matrixRules = computed(() => {
    return [
      {
        key: 'empty',
        title: '无有效占用',
        tagLabel: activeRecord.value ? '待切换' : '当前状态',
        tagType: activeRecord.value ? 'info' : 'primary',
        desc: '当前资产没有有效占用关系，需要先登记归口、责任人与位置。',
        actions: '发起占用',
        highlight: !activeRecord.value,
        shortcuts: [
          {
            key: 'all',
            label: '查看全部轨迹',
            status: 'ALL' as StatusFilter,
            testId: 'occupancy-shortcut-all'
          }
        ]
      },
      {
        key: 'active',
        title: '存在有效占用',
        tagLabel: activeRecord.value ? '当前状态' : '待触发',
        tagType: activeRecord.value ? 'success' : 'info',
        desc: '当前资产存在一条有效占用单，后续变更与释放都从当前有效单继续。',
        actions: '变更占用、释放占用',
        highlight: !!activeRecord.value,
        shortcuts: [
          {
            key: 'active',
            label: '只看有效占用',
            status: 'ACTIVE' as StatusFilter,
            testId: 'occupancy-shortcut-active'
          }
        ]
      },
      {
        key: 'released',
        title: '已释放历史',
        tagLabel: '历史状态',
        tagType: 'warning',
        desc: '已释放记录只保留轨迹，不允许直接对历史单再次执行变更或释放。',
        actions: '查看轨迹',
        highlight: false,
        shortcuts: [
          {
            key: 'released',
            label: '只看已释放',
            status: 'RELEASED' as StatusFilter,
            testId: 'occupancy-shortcut-released'
          }
        ]
      }
    ]
  })

  const filteredRecords = computed(() => {
    const normalizedKeyword = keyword.value.trim().toLowerCase()

    const matchedRecords = sortedRecords.value.filter((record) => {
      const matchesStatus =
        statusFilter.value === 'ALL' ||
        String(record.occupancyStatus || '').toUpperCase() === statusFilter.value
      if (!matchesStatus) {
        return false
      }

      if (timeFilter.value !== 'ALL') {
        const recordDate = resolveTimelineDate(record)
        if (!recordDate) {
          return false
        }
        if (timeFilter.value === 'CUSTOM') {
          const start = parseDateValue(customRangeApplied.start)
          const end = parseDateValue(customRangeApplied.end)
          if (start && recordDate < start) {
            return false
          }
          if (end) {
            const inclusiveEnd = new Date(end)
            inclusiveEnd.setHours(23, 59, 59, 999)
            if (recordDate > inclusiveEnd) {
              return false
            }
          }
        } else {
          const now = new Date()
          const diffDays = (now.getTime() - recordDate.getTime()) / (1000 * 60 * 60 * 24)
          const limitDays = { '7D': 7, '30D': 30, '90D': 90 }[timeFilter.value]
          if (typeof limitDays === 'number' && diffDays > limitDays) {
            return false
          }
        }
      }

      if (!normalizedKeyword) {
        return true
      }

      const searchableText = [
        record.occupancyNo,
        record.useDeptName,
        record.responsibleUserName,
        record.locationName,
        record.changeReason,
        record.releaseReason
      ]
        .filter(Boolean)
        .join(' ')
        .toLowerCase()

      return searchableText.includes(normalizedKeyword)
    })

    return matchedRecords.sort((left, right) => {
      const leftTime = resolveTimelineDate(left)?.getTime() || 0
      const rightTime = resolveTimelineDate(right)?.getTime() || 0
      return sortDirection.value === 'ASC' ? leftTime - rightTime : rightTime - leftTime
    })
  })

  const recordGroups = computed(() => {
    if (groupViewMode.value === 'LIST') {
      return [{ key: 'ALL', title: '全部轨迹', records: filteredRecords.value }]
    }

    return [
      {
        key: 'ACTIVE',
        title: '有效占用',
        records: filteredRecords.value.filter(
          (record) => String(record.occupancyStatus || '').toUpperCase() === 'ACTIVE'
        )
      },
      {
        key: 'RELEASED',
        title: '已释放',
        records: filteredRecords.value.filter(
          (record) => String(record.occupancyStatus || '').toUpperCase() === 'RELEASED'
        )
      }
    ].filter((group) => group.records.length)
  })

  const annotationPreviewRecord = computed(() => activeRecord.value || sortedRecords.value[0])

  const setRecordRef = (record: AssetRealEstateOccupancyRecord, element: Element | null) => {
    const key = getRecordKey(record)
    if (element instanceof HTMLElement) {
      recordRefs.set(key, element)
      return
    }
    recordRefs.delete(key)
  }

  const buildFilterState = (): OccupancyFilterState => {
    return {
      statusFilter: statusFilter.value,
      timeFilter: timeFilter.value,
      sortDirection: sortDirection.value,
      keyword: keyword.value,
      customRangeDraftStart: customRangeDraft.start,
      customRangeDraftEnd: customRangeDraft.end,
      customRangeAppliedStart: customRangeApplied.start,
      customRangeAppliedEnd: customRangeApplied.end
    }
  }

  const applyFilterState = (state: Partial<OccupancyFilterState>) => {
    statusFilter.value = ['ALL', 'ACTIVE', 'RELEASED'].includes(String(state.statusFilter))
      ? (state.statusFilter as StatusFilter)
      : defaultFilterState.statusFilter
    timeFilter.value = ['ALL', '7D', '30D', '90D', 'CUSTOM'].includes(String(state.timeFilter))
      ? (state.timeFilter as TimeFilter)
      : defaultFilterState.timeFilter
    sortDirection.value = ['DESC', 'ASC'].includes(String(state.sortDirection))
      ? (state.sortDirection as SortDirection)
      : defaultFilterState.sortDirection
    keyword.value = String(state.keyword || '')
    customRangeDraft.start = String(state.customRangeDraftStart || '')
    customRangeDraft.end = String(state.customRangeDraftEnd || '')
    customRangeApplied.start = String(state.customRangeAppliedStart || '')
    customRangeApplied.end = String(state.customRangeAppliedEnd || '')
  }

  const resetFocusedRecord = () => {
    focusedRecordKey.value = ''
  }

  const resetTimeFilters = () => {
    customRangeDraft.start = ''
    customRangeDraft.end = ''
    customRangeApplied.start = ''
    customRangeApplied.end = ''
    timeFilter.value = 'ALL'
  }

  const clearCustomRange = () => {
    resetTimeFilters()
  }

  const setQuickTimeFilter = (filter: Exclude<TimeFilter, 'CUSTOM'>) => {
    timeFilter.value = filter
    customRangeApplied.start = ''
    customRangeApplied.end = ''
  }

  const applyCustomRange = () => {
    if (!customRangeDraft.start || !customRangeDraft.end) {
      return
    }
    const start = parseDateValue(customRangeDraft.start)
    const end = parseDateValue(customRangeDraft.end)
    if (!start || !end) {
      return
    }
    if (start.getTime() <= end.getTime()) {
      customRangeApplied.start = customRangeDraft.start
      customRangeApplied.end = customRangeDraft.end
    } else {
      customRangeApplied.start = customRangeDraft.end
      customRangeApplied.end = customRangeDraft.start
    }
    timeFilter.value = 'CUSTOM'
  }

  const focusRecord = async (record: AssetRealEstateOccupancyRecord | undefined, filter: StatusFilter) => {
    if (!record) {
      return
    }
    statusFilter.value = filter
    sortDirection.value = 'DESC'
    keyword.value = ''
    resetTimeFilters()
    focusedRecordKey.value = getRecordKey(record)
    await nextTick()
    await nextTick()
    recordRefs.get(focusedRecordKey.value)?.scrollIntoView?.({ behavior: 'smooth', block: 'center' })
  }

  const focusActiveHistory = () => focusRecord(activeRecord.value, 'ACTIVE')
  const focusLatestReleasedHistory = () => focusRecord(latestReleasedRecord.value, 'RELEASED')

  const focusReleasedHistory = () => {
    statusFilter.value = 'RELEASED'
    resetTimeFilters()
    keyword.value = ''
    nextTick(() => historyListRef.value?.scrollIntoView?.({ behavior: 'smooth', block: 'start' }))
  }

  const applyLinkedFilter = (status: StatusFilter) => {
    statusFilter.value = status
    sortDirection.value = 'DESC'
    keyword.value = ''
    resetTimeFilters()
    resetFocusedRecord()
    nextTick(() => historyListRef.value?.scrollIntoView?.({ behavior: 'smooth', block: 'start' }))
  }

  const restorePersistedFilters = () => {
    filtersReady.value = false
    resetFocusedRecord()
    applyFilterState(defaultFilterState)

    if (!storageKey.value) {
      filtersReady.value = true
      return
    }

    const raw = window.localStorage.getItem(storageKey.value)
    if (!raw) {
      filtersReady.value = true
      return
    }

    try {
      applyFilterState(JSON.parse(raw) as Partial<OccupancyFilterState>)
    } catch {
      window.localStorage.removeItem(storageKey.value)
    }
    filtersReady.value = true
  }

  restorePersistedFilters()

  watch(storageKey, () => {
    restorePersistedFilters()
  })

  watch(
    () => [
      statusFilter.value,
      timeFilter.value,
      sortDirection.value,
      keyword.value,
      customRangeDraft.start,
      customRangeDraft.end,
      customRangeApplied.start,
      customRangeApplied.end
    ],
    () => {
      if (!filtersReady.value || !storageKey.value) {
        return
      }
      window.localStorage.setItem(storageKey.value, JSON.stringify(buildFilterState()))
    }
  )

  return {
    historyListRef,
    statusFilter,
    timeFilter,
    sortDirection,
    groupViewMode,
    annotationTemplate,
    focusedRecordKey,
    keyword,
    customRangeDraft,
    customRangeApplied,
    sortedRecords,
    activeRecord,
    latestReleasedRecord,
    isLedgerSynced,
    ledgerSyncTagType,
    ledgerSyncCompareItems,
    lastChangeCompareItems,
    matrixRules,
    filteredRecords,
    recordGroups,
    annotationPreviewRecord,
    buildFilterState,
    applyFilterState,
    resetFocusedRecord,
    resetTimeFilters,
    clearCustomRange,
    setQuickTimeFilter,
    applyCustomRange,
    focusActiveHistory,
    focusLatestReleasedHistory,
    focusReleasedHistory,
    applyLinkedFilter,
    setRecordRef,
    buildAnnotationStatusNote,
    buildAnnotationChangeNote,
    buildAnnotationReleaseNote,
    buildAnnotationPreviewItems
  }
}
