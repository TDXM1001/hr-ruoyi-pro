import { ElMessageBox } from 'element-plus'
import { computed, nextTick, reactive, ref, watch, type ComputedRef, type Ref } from 'vue'
import type { AssetRealEstateOccupancyRecord } from '@/api/asset/real-estate'
import {
  buildGovernanceActivityTypeLabel,
  buildResetScopeLabel,
  formatLocalDateKey,
  formatLocalDateTime,
  linkStatsResetScopeOptions,
  linkStatsWindowOptions,
  parseDateValue,
  tabLinkOptions,
  type GovernanceActivityState,
  type GovernanceActivityType,
  type GroupViewMode,
  type LinkResetLogState,
  type LinkStatsResetScope,
  type LinkStatsWindow,
  type LinkTrendItem,
  type LinkedTabName,
  type OccupancyFilterState,
  type OccupancyLinkStatEvent,
  type OccupancyLinkStatsState,
  type ResetLogFilterScope,
  type SavedTrendSnapshotState,
  type SortDirection,
  type StatusFilter,
  type TimeFilter,
  type TrendDrilldownState,
  type TrendFilterSnapshot,
  type GovernanceExportMetaState
} from './occupancyShared'

interface UseOccupancyGovernanceOptions {
  detailData: Ref<Record<string, any>>
  statusFilter: Ref<StatusFilter>
  timeFilter: Ref<TimeFilter>
  sortDirection: Ref<SortDirection>
  groupViewMode: Ref<GroupViewMode>
  keyword: Ref<string>
  customRangeDraft: { start: string; end: string }
  customRangeApplied: { start: string; end: string }
  historyListRef: Ref<HTMLElement | undefined>
  buildFilterState: () => OccupancyFilterState
  applyFilterState: (state: Partial<OccupancyFilterState>) => void
  resetFocusedRecord: () => void
  onSwitchTab: (tab: LinkedTabName) => void
  activeRecord: ComputedRef<AssetRealEstateOccupancyRecord | undefined>
  sortedRecords: ComputedRef<AssetRealEstateOccupancyRecord[]>
  filteredRecords: ComputedRef<AssetRealEstateOccupancyRecord[]>
}

export const useOccupancyGovernance = ({
  detailData,
  statusFilter,
  timeFilter,
  sortDirection,
  groupViewMode,
  keyword,
  customRangeDraft,
  customRangeApplied,
  historyListRef,
  buildFilterState,
  applyFilterState,
  resetFocusedRecord,
  onSwitchTab
}: UseOccupancyGovernanceOptions) => {
  const exportConfigOpen = ref(false)
  const governanceOpen = ref(false)
  const governanceActivityFilter = ref<'ALL' | GovernanceActivityType>('ALL')
  const governanceActivityKeyword = ref('')
  const linkStatsWindow = ref<LinkStatsWindow>('7D')
  const linkStatsResetScope = ref<LinkStatsResetScope>('ALL')
  const trendSnapshotName = ref('')
  const resetLogFilterScope = ref<ResetLogFilterScope>('ALL_RECORDS')
  const resetLogKeyword = ref('')
  const trendDrilldown = ref<TrendDrilldownState | null>(null)
  const trendFilterSnapshot = ref<TrendFilterSnapshot | null>(null)
  const savedTrendSnapshot = ref<SavedTrendSnapshotState | null>(null)
  const savedTrendSnapshotHistory = ref<SavedTrendSnapshotState[]>([])
  const linkResetLogs = ref<LinkResetLogState[]>([])
  const governanceActivities = ref<GovernanceActivityState[]>([])
  const governanceExportMeta = ref<GovernanceExportMetaState | null>(null)
  const linkStats = reactive<OccupancyLinkStatsState>({
    counts: {
      overview: 0,
      inspection: 0,
      rectification: 0,
      disposal: 0
    },
    lastTargetKey: '',
    lastTargetLabel: '',
    events: []
  })

  const linkStatsStorageKey = computed(() => {
    const assetKey = String(detailData.value.assetCode || detailData.value.assetId || '').trim()
    return assetKey ? `asset-real-estate-occupancy-link-stats:${assetKey}` : ''
  })
  const trendSnapshotStorageKey = computed(() => {
    const assetKey = String(detailData.value.assetCode || detailData.value.assetId || '').trim()
    return assetKey ? `asset-real-estate-occupancy-trend-snapshot:${assetKey}` : ''
  })
  const linkResetLogStorageKey = computed(() => {
    const assetKey = String(detailData.value.assetCode || detailData.value.assetId || '').trim()
    return assetKey ? `asset-real-estate-occupancy-reset-logs:${assetKey}` : ''
  })
  const governanceActivitiesStorageKey = computed(() => {
    const assetKey = String(detailData.value.assetCode || detailData.value.assetId || '').trim()
    return assetKey ? `asset-real-estate-occupancy-governance-activities:${assetKey}` : ''
  })
  const governanceExportMetaStorageKey = computed(() => {
    const assetKey = String(detailData.value.assetCode || detailData.value.assetId || '').trim()
    return assetKey ? `asset-real-estate-occupancy-governance-export-meta:${assetKey}` : ''
  })

  const visibleLinkEvents = computed(() => {
    if (!linkStats.events.length) {
      return [] as OccupancyLinkStatEvent[]
    }
    const limitDays = linkStatsWindow.value === '30D' ? 30 : 7
    const start = new Date()
    start.setHours(0, 0, 0, 0)
    start.setDate(start.getDate() - (limitDays - 1))
    return linkStats.events.filter((event) => {
      const eventDate = parseDateValue(event.occurredAt)
      return !!eventDate && eventDate.getTime() >= start.getTime()
    })
  })

  const displayedLinkLastTargetLabel = computed(() => {
    const latestEvent = visibleLinkEvents.value[visibleLinkEvents.value.length - 1]
    if (latestEvent) {
      return latestEvent.targetLabel
    }
    if (!linkStats.events.length) {
      return linkStats.lastTargetLabel
    }
    return ''
  })

  const linkStatItems = computed(() => {
    if (visibleLinkEvents.value.length) {
      const counter = visibleLinkEvents.value.reduce<Record<LinkedTabName, number>>(
        (accumulator, event) => {
          accumulator[event.targetKey] += 1
          return accumulator
        },
        { overview: 0, inspection: 0, rectification: 0, disposal: 0 }
      )
      return tabLinkOptions.map((item) => ({
        key: item.key,
        label: item.label,
        count: counter[item.key]
      }))
    }

    return tabLinkOptions.map((item) => ({
      key: item.key,
      label: item.label,
      count: linkStats.counts[item.key]
    }))
  })

  const linkTrendTitle = computed(() => (linkStatsWindow.value === '30D' ? '近 30 天联动趋势' : '近 7 天联动趋势'))
  const historyDrilldownTip = computed(() => (trendDrilldown.value ? `当前来自趋势钻取：${trendDrilldown.value.date}` : ''))
  const visibleSavedTrendSnapshots = computed(() => savedTrendSnapshotHistory.value.slice(0, 5))
  const latestGovernanceActivity = computed(() => governanceActivities.value[0] || null)
  const filteredGovernanceActivities = computed(() => {
    return governanceActivities.value.filter((item) => {
      if (governanceActivityFilter.value !== 'ALL' && item.type !== governanceActivityFilter.value) {
        return false
      }
      const keywordValue = governanceActivityKeyword.value.trim().toLowerCase()
      if (!keywordValue) {
        return true
      }
      return (
        item.label.toLowerCase().includes(keywordValue) ||
        item.target.toLowerCase().includes(keywordValue) ||
        item.summary.toLowerCase().includes(keywordValue) ||
        buildGovernanceActivityTypeLabel(item.type).toLowerCase().includes(keywordValue)
      )
    })
  })

  const filteredLinkResetLogs = computed(() => {
    return linkResetLogs.value.filter((item) => {
      if (resetLogFilterScope.value !== 'ALL_RECORDS' && item.scope !== resetLogFilterScope.value) {
        return false
      }
      const keywordValue = resetLogKeyword.value.trim().toLowerCase()
      if (!keywordValue) {
        return true
      }
      return (
        buildResetScopeLabel(item.scope).toLowerCase().includes(keywordValue) ||
        item.summary.toLowerCase().includes(keywordValue) ||
        item.executedAt.toLowerCase().includes(keywordValue)
      )
    })
  })

  const linkTrendItems = computed<LinkTrendItem[]>(() => {
    const days = linkStatsWindow.value === '30D' ? 30 : 7
    const dayKeys = Array.from({ length: days }).map((_, index) => {
      const current = new Date()
      current.setHours(0, 0, 0, 0)
      current.setDate(current.getDate() - ((days - 1) - index))
      return current
    })
    const buckets = dayKeys.map((date) => {
      const dateKey = formatLocalDateKey(date)
      const sameDayEvents = visibleLinkEvents.value.filter((event) => {
        const eventDate = parseDateValue(event.occurredAt)
        return eventDate && formatLocalDateKey(eventDate) === dateKey
      })
      const targetCounter = sameDayEvents.reduce<Record<string, number>>((accumulator, event) => {
        accumulator[event.targetLabel] = (accumulator[event.targetLabel] || 0) + 1
        return accumulator
      }, {})
      const topEntry = Object.entries(targetCounter).sort((left, right) => right[1] - left[1])[0] || undefined
      return {
        date: dateKey,
        label: dateKey.slice(5),
        count: sameDayEvents.length,
        topLabel: topEntry?.[0] || '暂无联动'
      }
    })
    const maxCount = Math.max(...buckets.map((item) => item.count), 1)
    return buckets.map((item) => ({
      ...item,
      barHeight: item.count ? Math.max((item.count / maxCount) * 100, 12) : 0
    }))
  })

  const persistLinkStats = () => {
    if (!linkStatsStorageKey.value) {
      return
    }
    window.localStorage.setItem(
      linkStatsStorageKey.value,
      JSON.stringify({
        counts: { ...linkStats.counts },
        lastTargetKey: linkStats.lastTargetKey,
        lastTargetLabel: linkStats.lastTargetLabel,
        events: [...linkStats.events]
      })
    )
  }

  const persistSavedTrendSnapshot = () => {
    if (!trendSnapshotStorageKey.value) {
      return
    }
    if (!savedTrendSnapshot.value && !savedTrendSnapshotHistory.value.length) {
      window.localStorage.removeItem(trendSnapshotStorageKey.value)
      return
    }
    window.localStorage.setItem(
      trendSnapshotStorageKey.value,
      JSON.stringify({
        current: savedTrendSnapshot.value,
        items: savedTrendSnapshotHistory.value
      })
    )
  }

  const persistLinkResetLogs = () => {
    if (!linkResetLogStorageKey.value) {
      return
    }
    if (!linkResetLogs.value.length) {
      window.localStorage.removeItem(linkResetLogStorageKey.value)
      return
    }
    window.localStorage.setItem(linkResetLogStorageKey.value, JSON.stringify(linkResetLogs.value))
  }

  const persistGovernanceActivities = () => {
    if (!governanceActivitiesStorageKey.value) {
      return
    }
    if (!governanceActivities.value.length) {
      window.localStorage.removeItem(governanceActivitiesStorageKey.value)
      return
    }
    window.localStorage.setItem(governanceActivitiesStorageKey.value, JSON.stringify(governanceActivities.value))
  }

  const persistGovernanceExportMeta = () => {
    if (!governanceExportMetaStorageKey.value) {
      return
    }
    if (!governanceExportMeta.value) {
      window.localStorage.removeItem(governanceExportMetaStorageKey.value)
      return
    }
    window.localStorage.setItem(governanceExportMetaStorageKey.value, JSON.stringify(governanceExportMeta.value))
  }

  const recordGovernanceActivity = (
    type: GovernanceActivityType,
    label: string,
    target: string,
    summary: string,
    executedAt = new Date().toISOString()
  ) => {
    governanceActivities.value = [
      {
        key: `governance-${Date.now()}-${type.toLowerCase()}`,
        type,
        label,
        target,
        summary,
        executedAt
      },
      ...governanceActivities.value
    ].slice(0, 20)
  }

  const emitTabSwitch = (tab: LinkedTabName) => {
    const linkOption = tabLinkOptions.find((item) => item.key === tab)
    const occurredAt = new Date().toISOString()
    linkStats.counts[tab] += 1
    linkStats.lastTargetKey = tab
    linkStats.lastTargetLabel = linkOption?.label || ''
    linkStats.events = [
      ...linkStats.events,
      {
        targetKey: tab,
        targetLabel: linkOption?.label || '',
        occurredAt
      }
    ].slice(-200)
    persistLinkStats()
    onSwitchTab(tab)
  }

  const setLinkStatsWindow = (windowValue: LinkStatsWindow) => {
    linkStatsWindow.value = windowValue
    if (trendDrilldown.value) {
      clearTrendDrilldown()
    }
  }

  const applyTrendDrilldownFilters = (date: string) => {
    if (!trendFilterSnapshot.value) {
      trendFilterSnapshot.value = buildFilterState()
    }
    statusFilter.value = 'ALL'
    sortDirection.value = 'DESC'
    keyword.value = ''
    customRangeDraft.start = date
    customRangeDraft.end = date
    customRangeApplied.start = date
    customRangeApplied.end = date
    timeFilter.value = 'CUSTOM'
  }

  const clearTrendDrilldown = () => {
    trendDrilldown.value = null
    if (trendFilterSnapshot.value) {
      applyFilterState(trendFilterSnapshot.value)
      trendFilterSnapshot.value = null
    }
  }

  const toggleTrendDrilldown = (item: LinkTrendItem) => {
    if (!item.count) {
      return
    }
    if (trendDrilldown.value?.date === item.date) {
      clearTrendDrilldown()
      return
    }
    trendDrilldown.value = {
      date: item.date,
      label: item.topLabel,
      count: item.count
    }
    applyTrendDrilldownFilters(item.date)
    groupViewMode.value = 'LIST'
    resetFocusedRecord()
    nextTick(() => historyListRef.value?.scrollIntoView?.({ behavior: 'smooth', block: 'start' }))
  }

  const saveTrendSnapshot = () => {
    const snapshotIndex = savedTrendSnapshotHistory.value.length + 1
    const snapshot: SavedTrendSnapshotState = {
      key: `snapshot-${Date.now()}-${snapshotIndex}`,
      name: trendSnapshotName.value.trim() || `趋势快照 ${snapshotIndex}`,
      savedAt: new Date().toISOString(),
      linkStatsWindow: linkStatsWindow.value,
      drilldown: trendDrilldown.value ? { ...trendDrilldown.value } : null,
      filterState: buildFilterState()
    }
    savedTrendSnapshot.value = snapshot
    savedTrendSnapshotHistory.value = [snapshot, ...savedTrendSnapshotHistory.value].slice(0, 10)
    trendSnapshotName.value = ''
    recordGovernanceActivity(
      'SNAPSHOT',
      '保存趋势快照',
      snapshot.name,
      `已保存${snapshot.linkStatsWindow === '30D' ? '近 30 天' : '近 7 天'}趋势快照`,
      snapshot.savedAt
    )
  }

  const toggleGovernancePanel = () => {
    if (governanceOpen.value) {
      governanceOpen.value = false
      return
    }
    governanceOpen.value = true
    exportConfigOpen.value = true
  }

  const toggleExportConfigPanel = () => {
    if (!governanceOpen.value) {
      governanceOpen.value = true
    }
    exportConfigOpen.value = !exportConfigOpen.value
  }

  const applySavedTrendSnapshot = async (snapshot?: SavedTrendSnapshotState | Event) => {
    const targetSnapshot =
      snapshot && typeof snapshot === 'object' && 'filterState' in snapshot
        ? (snapshot as SavedTrendSnapshotState)
        : savedTrendSnapshot.value
    if (!targetSnapshot?.filterState) {
      return
    }
    savedTrendSnapshot.value = targetSnapshot
    linkStatsWindow.value = targetSnapshot.linkStatsWindow
    trendFilterSnapshot.value = null
    applyFilterState(targetSnapshot.filterState)
    trendDrilldown.value = targetSnapshot.drilldown ? { ...targetSnapshot.drilldown } : null
    resetFocusedRecord()
    await nextTick()
    historyListRef.value?.scrollIntoView?.({ behavior: 'smooth', block: 'start' })
    recordGovernanceActivity(
      'SNAPSHOT',
      '恢复趋势快照',
      targetSnapshot.name,
      `已恢复${targetSnapshot.name}`,
      new Date().toISOString()
    )
  }

  const clearSavedTrendSnapshot = (snapshotKey?: string | Event) => {
    const targetSnapshot =
      typeof snapshotKey === 'string'
        ? savedTrendSnapshotHistory.value.find((item) => item.key === snapshotKey)
        : savedTrendSnapshot.value || undefined
    let resolvedSnapshotKey = typeof snapshotKey === 'string' ? snapshotKey : undefined
    if (!resolvedSnapshotKey && savedTrendSnapshot.value) {
      resolvedSnapshotKey = savedTrendSnapshot.value.key
    }
    if (!resolvedSnapshotKey) {
      savedTrendSnapshot.value = null
      return
    }
    savedTrendSnapshotHistory.value = savedTrendSnapshotHistory.value.filter((item) => item.key !== resolvedSnapshotKey)
    if (savedTrendSnapshot.value?.key === resolvedSnapshotKey) {
      savedTrendSnapshot.value = savedTrendSnapshotHistory.value[0] || null
    }
    if (targetSnapshot) {
      recordGovernanceActivity(
        'SNAPSHOT',
        '删除趋势快照',
        targetSnapshot.name,
        `已删除${targetSnapshot.name}`,
        new Date().toISOString()
      )
    }
  }

  const resetLinkStatsView = async () => {
    const scopeLabelMap: Record<LinkStatsResetScope, string> = {
      EVENTS: '趋势数据',
      COUNTS: '来源计数',
      ALL: '来源链路统计与趋势数据'
    }
    try {
      await ElMessageBox.confirm(`确认清空${scopeLabelMap[linkStatsResetScope.value]}吗？`, '重置统计', {
        type: 'warning',
        confirmButtonText: '确认重置',
        cancelButtonText: '取消'
      })
    } catch {
      return
    }

    const clearedEvents = linkStats.events.length
    const clearedCount =
      linkStats.counts.overview +
      linkStats.counts.inspection +
      linkStats.counts.rectification +
      linkStats.counts.disposal

    if (linkStatsResetScope.value === 'EVENTS' || linkStatsResetScope.value === 'ALL') {
      linkStats.events = []
      trendFilterSnapshot.value = null
      savedTrendSnapshot.value = null
      savedTrendSnapshotHistory.value = []
      clearTrendDrilldown()
      linkStatsWindow.value = '7D'
    }

    if (linkStatsResetScope.value === 'COUNTS' || linkStatsResetScope.value === 'ALL') {
      linkStats.counts.overview = 0
      linkStats.counts.inspection = 0
      linkStats.counts.rectification = 0
      linkStats.counts.disposal = 0
      linkStats.lastTargetKey = ''
      linkStats.lastTargetLabel = ''
    }

    const summaryMap: Record<LinkStatsResetScope, string> = {
      EVENTS: `已清空趋势事件 ${clearedEvents} 条`,
      COUNTS: `已清空来源计数 ${clearedCount} 次`,
      ALL: `已清空趋势事件 ${clearedEvents} 条，来源计数 ${clearedCount} 次`
    }
    const executedAt = new Date().toISOString()
    linkResetLogs.value = [
      {
        key: `reset-${Date.now()}-${linkStatsResetScope.value.toLowerCase()}`,
        executedAt,
        scope: linkStatsResetScope.value,
        summary: summaryMap[linkStatsResetScope.value]
      },
      ...linkResetLogs.value
    ].slice(0, 10)
    recordGovernanceActivity(
      'RESET',
      buildResetScopeLabel(linkStatsResetScope.value),
      buildResetScopeLabel(linkStatsResetScope.value),
      summaryMap[linkStatsResetScope.value],
      executedAt
    )
  }

  const exportFilteredResetLogs = () => {
    if (!filteredLinkResetLogs.value.length) {
      return
    }
    const content = JSON.stringify(
      {
        version: 1,
        filters: {
          scope: resetLogFilterScope.value,
          keyword: resetLogKeyword.value.trim()
        },
        records: filteredLinkResetLogs.value
      },
      null,
      2
    )
    const blob = new Blob([content], { type: 'application/json;charset=utf-8;' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${detailData.value.assetCode || 'asset'}-occupancy-reset-log-audit.json`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
  }

  const exportGovernanceAuditPackage = () => {
    const exportedAt = formatLocalDateTime(new Date())
    const fileName = `${detailData.value.assetCode || 'asset'}-occupancy-governance-audit.json`
    governanceExportMeta.value = { exportedAt, fileName }
    recordGovernanceActivity('EXPORT', '导出治理审计包', '治理审计包', '已导出当前资产治理状态', exportedAt)
  }

  const restoreLinkStats = () => {
    linkStats.counts.overview = 0
    linkStats.counts.inspection = 0
    linkStats.counts.rectification = 0
    linkStats.counts.disposal = 0
    linkStats.lastTargetKey = ''
    linkStats.lastTargetLabel = ''
    linkStats.events = []
    if (!linkStatsStorageKey.value) {
      return
    }
    const raw = window.localStorage.getItem(linkStatsStorageKey.value)
    if (!raw) {
      return
    }
    try {
      const parsed = JSON.parse(raw) as Partial<OccupancyLinkStatsState>
      ;(['overview', 'inspection', 'rectification', 'disposal'] as LinkedTabName[]).forEach((key) => {
        const nextValue = Number(parsed?.counts?.[key] || 0)
        linkStats.counts[key] = Number.isFinite(nextValue) ? nextValue : 0
      })
      linkStats.lastTargetKey = ['overview', 'inspection', 'rectification', 'disposal'].includes(String(parsed.lastTargetKey || ''))
        ? (parsed.lastTargetKey as LinkedTabName)
        : ''
      linkStats.lastTargetLabel = String(parsed.lastTargetLabel || '')
      linkStats.events = Array.isArray(parsed.events)
        ? parsed.events
            .map((item) => {
              const targetKey = String(item?.targetKey || '') as LinkedTabName
              const targetLabel = String(item?.targetLabel || '').trim()
              const occurredAt = String(item?.occurredAt || '').trim()
              if (
                !['overview', 'inspection', 'rectification', 'disposal'].includes(targetKey) ||
                !targetLabel ||
                !parseDateValue(occurredAt)
              ) {
                return undefined
              }
              return { targetKey, targetLabel, occurredAt }
            })
            .filter((item): item is OccupancyLinkStatEvent => !!item)
        : []
    } catch {
      window.localStorage.removeItem(linkStatsStorageKey.value)
    }
  }

  const restoreSavedTrendSnapshot = () => {
    savedTrendSnapshot.value = null
    savedTrendSnapshotHistory.value = []
    if (!trendSnapshotStorageKey.value) {
      return
    }
    const raw = window.localStorage.getItem(trendSnapshotStorageKey.value)
    if (!raw) {
      return
    }
    try {
      const parsed = JSON.parse(raw) as Partial<SavedTrendSnapshotState> | { current?: Partial<SavedTrendSnapshotState>; items?: Partial<SavedTrendSnapshotState>[] }
      const normalizeSnapshot = (item?: Partial<SavedTrendSnapshotState>) => {
        if (!item) {
          return undefined
        }
        const drilldown = item.drilldown
          ? {
              date: String(item.drilldown.date || ''),
              label: String(item.drilldown.label || ''),
              count: Number(item.drilldown.count || 0)
            }
          : null
        const normalized: SavedTrendSnapshotState = {
          key: String(item.key || `snapshot-${Date.now()}`),
          name: String(item.name || '未命名快照'),
          savedAt: String(item.savedAt || ''),
          linkStatsWindow: item.linkStatsWindow === '30D' ? '30D' : '7D',
          drilldown: drilldown?.date ? drilldown : null,
          filterState: {
            statusFilter: ['ALL', 'ACTIVE', 'RELEASED'].includes(String(item.filterState?.statusFilter || ''))
              ? (item.filterState?.statusFilter as StatusFilter)
              : 'ALL',
            timeFilter: ['ALL', '7D', '30D', '90D', 'CUSTOM'].includes(String(item.filterState?.timeFilter || ''))
              ? (item.filterState?.timeFilter as TimeFilter)
              : 'ALL',
            sortDirection: item.filterState?.sortDirection === 'ASC' ? 'ASC' : 'DESC',
            keyword: String(item.filterState?.keyword || ''),
            customRangeDraftStart: String(item.filterState?.customRangeDraftStart || ''),
            customRangeDraftEnd: String(item.filterState?.customRangeDraftEnd || ''),
            customRangeAppliedStart: String(item.filterState?.customRangeAppliedStart || ''),
            customRangeAppliedEnd: String(item.filterState?.customRangeAppliedEnd || '')
          }
        }
        return normalized.savedAt ? normalized : undefined
      }

      if ('items' in parsed || 'current' in parsed) {
        savedTrendSnapshotHistory.value = Array.isArray(parsed.items)
          ? parsed.items.map((item) => normalizeSnapshot(item)).filter((item): item is SavedTrendSnapshotState => !!item)
          : []
        savedTrendSnapshot.value = normalizeSnapshot(parsed.current) || savedTrendSnapshotHistory.value[0] || null
        return
      }

      const legacySnapshot = normalizeSnapshot(parsed as Partial<SavedTrendSnapshotState>)
      savedTrendSnapshot.value = legacySnapshot || null
      savedTrendSnapshotHistory.value = legacySnapshot ? [legacySnapshot] : []
    } catch {
      window.localStorage.removeItem(trendSnapshotStorageKey.value)
    }
  }

  const restoreLinkResetLogs = () => {
    linkResetLogs.value = []
    if (!linkResetLogStorageKey.value) {
      return
    }
    const raw = window.localStorage.getItem(linkResetLogStorageKey.value)
    if (!raw) {
      return
    }
    try {
      const parsed = JSON.parse(raw)
      linkResetLogs.value = Array.isArray(parsed)
        ? parsed
            .map((item) => {
              const key = String(item?.key || '').trim()
              const executedAt = String(item?.executedAt || '').trim()
              const scope = String(item?.scope || '') as LinkStatsResetScope
              const summary = String(item?.summary || '').trim()
              if (!key || !executedAt || !['EVENTS', 'COUNTS', 'ALL'].includes(scope) || !summary) {
                return undefined
              }
              return { key, executedAt, scope, summary }
            })
            .filter((item): item is LinkResetLogState => !!item)
        : []
    } catch {
      window.localStorage.removeItem(linkResetLogStorageKey.value)
    }
  }

  const restoreGovernanceActivities = () => {
    governanceActivities.value = []
    if (!governanceActivitiesStorageKey.value) {
      return
    }
    const raw = window.localStorage.getItem(governanceActivitiesStorageKey.value)
    if (!raw) {
      return
    }
    try {
      const parsed = JSON.parse(raw)
      governanceActivities.value = Array.isArray(parsed)
        ? parsed
            .map((item) => {
              const key = String(item?.key || '').trim()
              const type = String(item?.type || '').trim() as GovernanceActivityType
              const label = String(item?.label || '').trim()
              const target = String(item?.target || '').trim()
              const summary = String(item?.summary || '').trim()
              const executedAt = String(item?.executedAt || '').trim()
              if (
                !key ||
                !['TEMPLATE', 'SNAPSHOT', 'RESET', 'EXPORT'].includes(type) ||
                !label ||
                !target ||
                !summary ||
                !executedAt
              ) {
                return undefined
              }
              return { key, type, label, target, summary, executedAt }
            })
            .filter((item): item is GovernanceActivityState => !!item)
        : []
    } catch {
      window.localStorage.removeItem(governanceActivitiesStorageKey.value)
    }
  }

  const restoreGovernanceExportMeta = () => {
    governanceExportMeta.value = null
    if (!governanceExportMetaStorageKey.value) {
      return
    }
    const raw = window.localStorage.getItem(governanceExportMetaStorageKey.value)
    if (!raw) {
      return
    }
    try {
      const parsed = JSON.parse(raw)
      const exportedAt = String(parsed?.exportedAt || '').trim()
      const fileName = String(parsed?.fileName || '').trim()
      if (!exportedAt) {
        return
      }
      governanceExportMeta.value = { exportedAt, fileName }
    } catch {
      window.localStorage.removeItem(governanceExportMetaStorageKey.value)
    }
  }

  restoreLinkStats()
  restoreSavedTrendSnapshot()
  restoreLinkResetLogs()
  restoreGovernanceActivities()
  restoreGovernanceExportMeta()

  watch(linkStatsStorageKey, restoreLinkStats)
  watch(trendSnapshotStorageKey, restoreSavedTrendSnapshot)
  watch(linkResetLogStorageKey, restoreLinkResetLogs)
  watch(governanceActivitiesStorageKey, restoreGovernanceActivities)
  watch(governanceExportMetaStorageKey, restoreGovernanceExportMeta)

  watch(
    () => ({
      counts: { ...linkStats.counts },
      lastTargetKey: linkStats.lastTargetKey,
      lastTargetLabel: linkStats.lastTargetLabel,
      events: [...linkStats.events]
    }),
    persistLinkStats,
    { deep: true }
  )
  watch(savedTrendSnapshot, persistSavedTrendSnapshot, { deep: true })
  watch(savedTrendSnapshotHistory, persistSavedTrendSnapshot, { deep: true })
  watch(linkResetLogs, persistLinkResetLogs, { deep: true })
  watch(governanceActivities, persistGovernanceActivities, { deep: true })
  watch(governanceExportMeta, persistGovernanceExportMeta, { deep: true })

  return {
    tabLinkOptions,
    linkStatsWindowOptions,
    linkStatsResetScopeOptions,
    exportConfigOpen,
    governanceOpen,
    governanceActivityFilter,
    governanceActivityKeyword,
    linkStatsWindow,
    linkStatsResetScope,
    trendSnapshotName,
    resetLogFilterScope,
    resetLogKeyword,
    trendDrilldown,
    savedTrendSnapshot,
    savedTrendSnapshotHistory,
    linkResetLogs,
    governanceActivities,
    governanceExportMeta,
    linkStats,
    displayedLinkLastTargetLabel,
    linkStatItems,
    linkTrendTitle,
    historyDrilldownTip,
    visibleSavedTrendSnapshots,
    latestGovernanceActivity,
    filteredGovernanceActivities,
    filteredLinkResetLogs,
    linkTrendItems,
    emitTabSwitch,
    setLinkStatsWindow,
    clearTrendDrilldown,
    toggleTrendDrilldown,
    saveTrendSnapshot,
    toggleGovernancePanel,
    toggleExportConfigPanel,
    applySavedTrendSnapshot,
    clearSavedTrendSnapshot,
    resetLinkStatsView,
    exportFilteredResetLogs,
    exportGovernanceAuditPackage,
    recordGovernanceActivity,
    buildResetScopeLabel,
    buildGovernanceActivityTypeLabel
  }
}
