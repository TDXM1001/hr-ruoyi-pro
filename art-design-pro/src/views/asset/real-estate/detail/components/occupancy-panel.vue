<template>
  <div class="section-stack" data-testid="occupancy-reading-layout">
    <ElAlert
      class="section-alert"
      type="info"
      show-icon
      :closable="false"
      title="占用页签聚焦当前实际使用归口、责任人和释放轨迹，资产管理员可以在当前详情页内连续完成发起、变更和释放。"
    />

    <div class="occupancy-overview-grid">
      <ElCard class="section-card" shadow="never">
        <template #header>
          <div class="card-title">当前有效占用</div>
        </template>

        <div v-if="activeRecord" class="current-occupancy-card">
          <div class="current-occupancy-card__header">
            <div>
              <div class="current-occupancy-card__title">
                {{ activeRecord.occupancyNo || '待生成占用单号' }}
              </div>
              <div class="current-occupancy-card__subtitle">
                当前资产已被占用，后续变更和释放都从这条有效占用继续推进。
              </div>
            </div>

            <div class="current-occupancy-card__tags">
              <ElTag type="success" effect="dark">当前有效占用</ElTag>
              <ElTag effect="light">{{ getStatusLabel(activeRecord.occupancyStatus) }}</ElTag>
            </div>
          </div>

          <div class="current-occupancy-grid">
            <div class="summary-card">
              <div class="summary-card__label">使用部门</div>
              <div class="summary-card__value">{{ activeRecord.useDeptName || '-' }}</div>
            </div>
            <div class="summary-card">
              <div class="summary-card__label">责任人</div>
              <div class="summary-card__value">{{ activeRecord.responsibleUserName || '-' }}</div>
            </div>
            <div class="summary-card">
              <div class="summary-card__label">占用起始</div>
              <div class="summary-card__value">{{ activeRecord.startDate || '-' }}</div>
            </div>
          </div>

          <div class="detail-card-grid">
            <div class="detail-card detail-card--wide">
              <div class="detail-card__label">使用位置</div>
              <div class="detail-card__value">{{ activeRecord.locationName || '-' }}</div>
            </div>
            <div class="detail-card detail-card--wide">
              <div class="detail-card__label">发起/变更原因</div>
              <div class="detail-card__value">{{ activeRecord.changeReason || '-' }}</div>
            </div>
          </div>

          <div class="insight-card-grid">
            <div class="insight-card" data-testid="occupancy-ledger-sync-summary">
              <div class="insight-card__header">
                <div class="insight-card__title">主档联动摘要</div>
                <ElTag :type="ledgerSyncTagType" effect="light">
                  {{ isLedgerSynced ? '主档已同步' : '主档待校正' }}
                </ElTag>
              </div>
              <div class="insight-card__desc">
                {{
                  isLedgerSynced
                    ? '当前有效占用与资产主档快照一致，可直接回总览核对最新主档口径。'
                    : '当前有效占用与主档快照存在差异，建议回到总览核对使用部门、责任人和位置。'
                }}
              </div>
            <div class="insight-card__grid">
                <div
                  v-for="item in ledgerSyncCompareItems"
                  :key="item.key"
                  :data-testid="`occupancy-ledger-sync-item-${item.key}`"
                  class="compare-item"
                  :class="item.changed ? 'compare-item--changed' : 'compare-item--stable'"
                >
                  <div class="compare-item__header">
                    <div class="detail-card__label">{{ item.label }}</div>
                    <ElTag :type="item.changed ? 'warning' : 'success'" effect="light" size="small">
                      {{ item.changed ? '待校正' : '已同步' }}
                    </ElTag>
                  </div>
                  <div class="compare-item__values">
                    <div class="compare-item__row">
                      <span>主档</span>
                      <strong>{{ item.baseValue }}</strong>
                    </div>
                    <div class="compare-item__row">
                      <span>当前占用</span>
                      <strong>{{ item.compareValue }}</strong>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="insight-card" data-testid="occupancy-last-change-summary">
              <div class="insight-card__header">
                <div class="insight-card__title">最近一次变更摘要</div>
                <ElTag effect="light">
                  {{ latestReleasedRecord ? '存在上一次轨迹' : '当前为首条有效占用' }}
                </ElTag>
              </div>
              <template v-if="latestReleasedRecord">
                <div class="insight-card__desc">
                  基于最近一条已释放记录，快速确认本次变更前后责任归属和位置变化。
                </div>
                <div class="insight-card__grid">
                  <div class="detail-card">
                    <div class="detail-card__label">上一条占用单</div>
                    <div class="detail-card__value">
                      {{ latestReleasedRecord.occupancyNo || '-' }}
                    </div>
                  </div>
                  <div
                    v-for="item in lastChangeCompareItems"
                    :key="item.key"
                    :data-testid="`occupancy-last-change-item-${item.key}`"
                    class="compare-item"
                    :class="item.changed ? 'compare-item--changed' : 'compare-item--stable'"
                  >
                    <div class="compare-item__header">
                      <div class="detail-card__label">{{ item.label }}</div>
                      <ElTag :type="item.changed ? 'warning' : 'info'" effect="light" size="small">
                        {{ item.changed ? '已变更' : '未变更' }}
                      </ElTag>
                    </div>
                    <div class="compare-item__values">
                      <div class="compare-item__row">
                        <span>上一条</span>
                        <strong>{{ item.baseValue }}</strong>
                      </div>
                      <div class="compare-item__row">
                        <span>当前</span>
                        <strong>{{ item.compareValue }}</strong>
                      </div>
                    </div>
                  </div>
                </div>
              </template>
              <div v-else class="insight-card__desc">
                当前有效占用是该资产的首条占用记录，后续如发生变更或释放，会在这里展示前后摘要。
              </div>
            </div>
          </div>

          <div v-if="props.canEdit" class="current-occupancy-card__actions">
            <ElButton
              :data-testid="`occupancy-change-link-${activeRecord.occupancyId}`"
              link
              type="primary"
              @click="$emit('change-occupancy', activeRecord)"
            >
              变更占用
            </ElButton>
            <ElButton
              :data-testid="`occupancy-release-link-${activeRecord.occupancyId}`"
              link
              type="warning"
              @click="$emit('release-occupancy', activeRecord)"
            >
              释放占用
            </ElButton>
          </div>
        </div>

        <div v-else class="empty-occupancy-card">
          <div>
            <div class="empty-occupancy-card__title">暂无有效占用</div>
            <div class="empty-occupancy-card__desc">
              当前资产没有有效占用关系。可以先登记使用部门、责任人和实际位置，再进入后续变更和释放流程。
            </div>
          </div>

          <div class="empty-occupancy-card__meta">
            <div class="empty-occupancy-card__meta-item">
              <span>资产编码</span>
              <strong>{{ props.detailData.assetCode || '-' }}</strong>
            </div>
            <div class="empty-occupancy-card__meta-item">
              <span>权属部门</span>
              <strong>{{ props.detailData.ownerDeptName || '-' }}</strong>
            </div>
          </div>

          <div
            v-if="latestReleasedRecord"
            class="empty-occupancy-card__release-summary"
            data-testid="occupancy-empty-released-summary"
          >
            <div class="insight-card__title">最近释放信息</div>
            <div class="insight-card__desc">
              最近一次释放后，当前资产处于无有效占用状态。可以直接重新发起占用，或先回看已释放轨迹确认释放原因。
            </div>
            <div class="insight-card__grid">
              <div class="detail-card">
                <div class="detail-card__label">最近释放单号</div>
                <div class="detail-card__value">{{ latestReleasedRecord.occupancyNo || '-' }}</div>
              </div>
              <div class="detail-card">
                <div class="detail-card__label">最近释放日期</div>
                <div class="detail-card__value">{{ latestReleasedRecord.endDate || '-' }}</div>
              </div>
              <div class="detail-card detail-card--wide">
                <div class="detail-card__label">最近释放原因</div>
                <div class="detail-card__value">
                  {{ latestReleasedRecord.releaseReason || '-' }}
                </div>
              </div>
            </div>
          </div>

          <div class="empty-occupancy-card__actions">
            <ElButton
              v-if="props.canEdit"
              data-testid="occupancy-create-link"
              type="primary"
              plain
              @click="$emit('create-occupancy')"
            >
              发起占用
            </ElButton>
            <ElButton
              v-if="latestReleasedRecord"
              data-testid="occupancy-focus-released-link"
              link
              type="primary"
              @click="focusReleasedHistory"
            >
              查看已释放轨迹
            </ElButton>
          </div>
        </div>
      </ElCard>

      <ElCard class="section-card" shadow="never">
        <template #header>
          <div class="card-title">状态矩阵</div>
        </template>

        <div class="matrix-panel">
          <div
            v-for="rule in matrixRules"
            :key="rule.key"
            class="matrix-item"
            :class="rule.highlight ? 'matrix-item--highlight' : ''"
          >
            <div class="matrix-item__header">
              <div class="matrix-item__title">{{ rule.title }}</div>
              <ElTag :type="rule.tagType" effect="light">{{ rule.tagLabel }}</ElTag>
            </div>
            <div class="matrix-item__desc">{{ rule.desc }}</div>
            <div class="matrix-item__actions">可执行动作：{{ rule.actions }}</div>
          </div>
        </div>
      </ElCard>
    </div>

    <ElCard class="section-card" shadow="never">
      <template #header>
        <div class="card-title">占用历史记录</div>
      </template>

      <div class="history-toolbar">
        <div class="history-toolbar__groups">
          <div class="history-toolbar__group">
            <span class="history-toolbar__label">状态</span>
            <div class="history-toolbar__filters">
              <ElButton
                data-testid="occupancy-filter-all"
                size="small"
                :type="statusFilter === 'ALL' ? 'primary' : 'default'"
                @click="statusFilter = 'ALL'"
              >
                全部
              </ElButton>
              <ElButton
                data-testid="occupancy-filter-active"
                size="small"
                :type="statusFilter === 'ACTIVE' ? 'primary' : 'default'"
                @click="statusFilter = 'ACTIVE'"
              >
                有效占用
              </ElButton>
              <ElButton
                data-testid="occupancy-filter-released"
                size="small"
                :type="statusFilter === 'RELEASED' ? 'primary' : 'default'"
                @click="statusFilter = 'RELEASED'"
              >
                已释放
              </ElButton>
            </div>
          </div>

          <div class="history-toolbar__group">
            <span class="history-toolbar__label">时间</span>
            <div class="history-toolbar__filters">
              <ElButton
                data-testid="occupancy-time-all"
                size="small"
                :type="timeFilter === 'ALL' ? 'primary' : 'default'"
                @click="setQuickTimeFilter('ALL')"
              >
                全部时间
              </ElButton>
              <ElButton
                data-testid="occupancy-time-7d"
                size="small"
                :type="timeFilter === '7D' ? 'primary' : 'default'"
                @click="setQuickTimeFilter('7D')"
              >
                近 7 天
              </ElButton>
              <ElButton
                data-testid="occupancy-time-30d"
                size="small"
                :type="timeFilter === '30D' ? 'primary' : 'default'"
                @click="setQuickTimeFilter('30D')"
              >
                近 30 天
              </ElButton>
              <ElButton
                data-testid="occupancy-time-90d"
                size="small"
                :type="timeFilter === '90D' ? 'primary' : 'default'"
                @click="setQuickTimeFilter('90D')"
              >
                近 90 天
              </ElButton>
            </div>
          </div>

          <div class="history-toolbar__group history-toolbar__group--range">
            <span class="history-toolbar__label">自定义范围</span>
            <div class="history-toolbar__range">
              <input
                v-model="customRangeDraft.start"
                data-testid="occupancy-custom-start"
                type="date"
                class="history-toolbar__date-input"
              />
              <span class="history-toolbar__range-divider">至</span>
              <input
                v-model="customRangeDraft.end"
                data-testid="occupancy-custom-end"
                type="date"
                class="history-toolbar__date-input"
              />
              <ElButton
                data-testid="occupancy-custom-apply"
                size="small"
                type="primary"
                plain
                @click="applyCustomRange"
              >
                应用范围
              </ElButton>
              <ElButton
                v-if="timeFilter === 'CUSTOM' || customRangeDraft.start || customRangeDraft.end"
                data-testid="occupancy-custom-clear"
                size="small"
                @click="clearCustomRange"
              >
                清除
              </ElButton>
            </div>
          </div>

          <div class="history-toolbar__group">
            <span class="history-toolbar__label">排序</span>
            <div class="history-toolbar__filters">
              <ElButton
                data-testid="occupancy-sort-desc"
                size="small"
                :type="sortDirection === 'DESC' ? 'primary' : 'default'"
                @click="sortDirection = 'DESC'"
              >
                最新在前
              </ElButton>
              <ElButton
                data-testid="occupancy-sort-asc"
                size="small"
                :type="sortDirection === 'ASC' ? 'primary' : 'default'"
                @click="sortDirection = 'ASC'"
              >
                最早在前
              </ElButton>
            </div>
          </div>
        </div>

        <ElInput
          v-model="keyword"
          data-testid="occupancy-keyword-input"
          clearable
          placeholder="搜索占用单号/部门/责任人/位置/原因"
          class="history-toolbar__search"
        />
      </div>

      <div ref="historyListRef" class="record-wrapper" data-testid="occupancy-history-list">
        <div v-if="filteredRecords.length" class="record-list">
          <div
            v-for="record in filteredRecords"
            :key="record.occupancyId || record.occupancyNo || record.startDate"
            class="record-item"
            :class="record.occupancyStatus === 'ACTIVE' ? 'record-item--active' : 'record-item--released'"
          >
            <div class="record-item__header">
              <div>
                <div class="record-item__title">
                  {{ record.occupancyNo || '待生成占用单号' }}
                </div>
                <div class="record-item__subtitle">
                  {{ record.useDeptName || '-' }} / {{ record.responsibleUserName || '-' }} /
                  {{ record.locationName || '-' }}
                </div>
              </div>

              <div class="record-item__tags">
                <ElTag :type="record.occupancyStatus === 'ACTIVE' ? 'success' : 'info'" effect="light">
                  {{ getStatusLabel(record.occupancyStatus) }}
                </ElTag>
                <ElTag v-if="record.occupancyStatus === 'RELEASED'" type="warning" effect="light">
                  已释放
                </ElTag>
              </div>
            </div>

            <div class="record-detail-grid">
              <div class="detail-card">
                <div class="detail-card__label">占用起始</div>
                <div class="detail-card__value">{{ record.startDate || '-' }}</div>
              </div>
              <div class="detail-card">
                <div class="detail-card__label">释放时间</div>
                <div class="detail-card__value">{{ record.endDate || '-' }}</div>
              </div>
              <div class="detail-card detail-card--wide">
                <div class="detail-card__label">发起/变更原因</div>
                <div class="detail-card__value">{{ record.changeReason || '-' }}</div>
              </div>
              <div class="detail-card detail-card--wide">
                <div class="detail-card__label">释放原因</div>
                <div class="detail-card__value">{{ record.releaseReason || '-' }}</div>
              </div>
            </div>
          </div>
        </div>

        <ElEmpty v-else description="当前筛选条件下暂无占用轨迹" :image-size="68" />
      </div>
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import type { AssetRealEstateOccupancyRecord } from '@/api/asset/real-estate'

  type TimeFilter = 'ALL' | '7D' | '30D' | '90D' | 'CUSTOM'
  type SortDirection = 'DESC' | 'ASC'
  type CompareFieldKey = 'useDeptName' | 'responsibleUserName' | 'locationName'

  const props = defineProps<{
    detailData: Record<string, any>
    occupancyRecords: AssetRealEstateOccupancyRecord[]
    canEdit?: boolean
  }>()

  defineEmits<{
    'create-occupancy': []
    'change-occupancy': [record: AssetRealEstateOccupancyRecord]
    'release-occupancy': [record: AssetRealEstateOccupancyRecord]
  }>()

  const historyListRef = ref<HTMLElement>()
  const statusFilter = ref<'ALL' | 'ACTIVE' | 'RELEASED'>('ALL')
  const timeFilter = ref<TimeFilter>('ALL')
  const sortDirection = ref<SortDirection>('DESC')
  const keyword = ref('')
  const customRangeDraft = reactive({
    start: '',
    end: ''
  })
  const customRangeApplied = reactive({
    start: '',
    end: ''
  })

  const compareFieldLabels: Record<CompareFieldKey, string> = {
    useDeptName: '使用部门',
    responsibleUserName: '责任人',
    locationName: '使用位置'
  }

  const parseDateValue = (value?: string) => {
    if (!value) {
      return undefined
    }
    const normalized = /^\d{4}-\d{2}-\d{2}$/.test(value) ? `${value}T00:00:00` : value
    const parsed = new Date(normalized)
    return Number.isNaN(parsed.getTime()) ? undefined : parsed
  }

  const resolveTimelineDate = (record: AssetRealEstateOccupancyRecord) => {
    return parseDateValue(record.endDate || record.startDate)
  }

  const getDisplayValue = (value?: string) => {
    const text = String(value || '').trim()
    return text || '-'
  }

  const buildCompareItems = (
    baseSource: Record<string, any>,
    compareSource: Record<string, any>
  ) => {
    return (Object.keys(compareFieldLabels) as CompareFieldKey[]).map((key) => {
      const baseValue = getDisplayValue(baseSource?.[key])
      const compareValue = getDisplayValue(compareSource?.[key])
      return {
        key,
        label: compareFieldLabels[key],
        baseValue,
        compareValue,
        changed: baseValue !== compareValue
      }
    })
  }

  const sortedRecords = computed(() => {
    return [...props.occupancyRecords].sort((left, right) => {
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
      String(props.detailData.useDeptName || '') === String(activeRecord.value.useDeptName || '') &&
      String(props.detailData.responsibleUserName || '') ===
        String(activeRecord.value.responsibleUserName || '') &&
      String(props.detailData.locationName || '') === String(activeRecord.value.locationName || '')
    )
  })

  const ledgerSyncTagType = computed(() => {
    return isLedgerSynced.value ? 'success' : 'warning'
  })

  const ledgerSyncCompareItems = computed(() => {
    if (!activeRecord.value) {
      return []
    }
    return buildCompareItems(props.detailData || {}, activeRecord.value)
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
        highlight: !activeRecord.value
      },
      {
        key: 'active',
        title: '存在有效占用',
        tagLabel: activeRecord.value ? '当前状态' : '待触发',
        tagType: activeRecord.value ? 'success' : 'info',
        desc: '当前资产存在一条有效占用单，后续变更与释放都从当前有效单继续。',
        actions: '变更占用、释放占用',
        highlight: !!activeRecord.value
      },
      {
        key: 'released',
        title: '已释放历史',
        tagLabel: '历史状态',
        tagType: 'warning',
        desc: '已释放记录只保留轨迹，不允许直接对历史单再次执行变更或释放。',
        actions: '查看轨迹',
        highlight: false
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
          const limitDays = {
            '7D': 7,
            '30D': 30,
            '90D': 90
          }[timeFilter.value]

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

  const getStatusLabel = (status?: string) => {
    const mapper: Record<string, string> = {
      ACTIVE: '有效占用',
      RELEASED: '已释放'
    }
    return mapper[String(status || '').toUpperCase()] || status || '-'
  }

  const focusReleasedHistory = () => {
    statusFilter.value = 'RELEASED'
    setQuickTimeFilter('ALL')
    keyword.value = ''
    nextTick(() => historyListRef.value?.scrollIntoView?.({ behavior: 'smooth', block: 'start' }))
  }

  const clearCustomRange = () => {
    customRangeDraft.start = ''
    customRangeDraft.end = ''
    customRangeApplied.start = ''
    customRangeApplied.end = ''
    timeFilter.value = 'ALL'
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
</script>

<style scoped lang="scss">
  .occupancy-overview-grid {
    display: grid;
    grid-template-columns: minmax(0, 1.4fr) minmax(320px, 0.9fr);
    gap: 12px;
  }

  .current-occupancy-card,
  .empty-occupancy-card,
  .matrix-panel,
  .record-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
    padding: 16px;
  }

  .current-occupancy-card__header,
  .record-item__header,
  .matrix-item__header,
  .history-toolbar {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 16px;
  }

  .current-occupancy-card__title,
  .record-item__title,
  .matrix-item__title {
    font-size: 18px;
    font-weight: 700;
    line-height: 1.5;
    color: #18233a;
  }

  .current-occupancy-card__subtitle,
  .record-item__subtitle,
  .matrix-item__desc,
  .matrix-item__actions,
  .insight-card__desc,
  .empty-occupancy-card__desc {
    font-size: 13px;
    line-height: 1.8;
    color: #5d6b86;
    word-break: break-word;
    white-space: normal;
  }

  .current-occupancy-card__tags,
  .record-item__tags,
  .current-occupancy-card__actions,
  .empty-occupancy-card__actions,
  .history-toolbar__filters {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
  }

  .current-occupancy-grid,
  .detail-card-grid,
  .record-detail-grid,
  .empty-occupancy-card__meta,
  .insight-card-grid,
  .insight-card__grid {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 12px;
  }

  .summary-card,
  .detail-card,
  .record-item,
  .matrix-item,
  .insight-card,
  .empty-occupancy-card__release-summary {
    border: 1px solid #e7edf6;
    border-radius: 14px;
    background: rgb(255 255 255 / 90%);
  }

  .summary-card,
  .detail-card {
    padding: 14px 16px;
  }

  .summary-card__label,
  .detail-card__label,
  .empty-occupancy-card__meta-item span {
    font-size: 12px;
    font-weight: 600;
    color: #6f7f99;
  }

  .summary-card__value,
  .detail-card__value,
  .empty-occupancy-card__meta-item strong {
    margin-top: 10px;
    font-size: 14px;
    line-height: 1.8;
    color: #18233a;
    word-break: break-word;
    white-space: pre-wrap;
  }

  .detail-card--wide {
    grid-column: span 2;
  }

  .current-occupancy-card {
    background: linear-gradient(180deg, rgb(236 253 245 / 92%), #fff 100%);
  }

  .empty-occupancy-card {
    align-items: flex-start;
    background: linear-gradient(180deg, rgb(248 250 252 / 96%), #fff 100%);
  }

  .empty-occupancy-card__title {
    font-size: 18px;
    font-weight: 700;
    color: #18233a;
  }

  .empty-occupancy-card__meta {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    width: 100%;
  }

  .empty-occupancy-card__release-summary,
  .insight-card {
    padding: 14px 16px;
  }

  .insight-card-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .insight-card__header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 10px;
  }

  .insight-card__title {
    font-size: 15px;
    font-weight: 700;
    color: #18233a;
  }

  .empty-occupancy-card__meta-item {
    padding: 14px 16px;
    border: 1px dashed #cdd8e8;
    border-radius: 14px;
    background: rgb(255 255 255 / 86%);
  }

  .matrix-item {
    padding: 14px 16px;
    background: linear-gradient(180deg, rgb(248 250 252 / 98%), #fff 100%);
  }

  .matrix-item--highlight {
    border-color: #99f6e4;
    background: linear-gradient(180deg, rgb(236 253 245 / 92%), #fff 100%);
  }

  .history-toolbar {
    padding: 16px 16px 0;
  }

  .history-toolbar__groups {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
  }

  .history-toolbar__group {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 10px;
  }

  .history-toolbar__group--range {
    width: 100%;
  }

  .history-toolbar__label {
    font-size: 12px;
    font-weight: 600;
    color: #6f7f99;
  }

  .history-toolbar__range {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 10px;
  }

  .history-toolbar__date-input {
    min-width: 158px;
    height: 32px;
    padding: 0 12px;
    border: 1px solid #d8e0ec;
    border-radius: 10px;
    font-size: 13px;
    color: #18233a;
    background: #fff;
  }

  .history-toolbar__range-divider {
    font-size: 12px;
    font-weight: 600;
    color: #6f7f99;
  }

  .history-toolbar__search {
    width: 320px;
    max-width: 100%;
  }

  .compare-item {
    padding: 14px 16px;
    border: 1px solid #dce5f2;
    border-radius: 14px;
    background: linear-gradient(180deg, rgb(248 250 252 / 94%), #fff 100%);
  }

  .compare-item--changed {
    border-color: #f9c97f;
    background: linear-gradient(180deg, rgb(255 247 237 / 92%), #fff 100%);
    box-shadow: 0 10px 20px rgb(249 115 22 / 8%);
  }

  .compare-item--stable {
    border-color: #cceadb;
    background: linear-gradient(180deg, rgb(236 253 245 / 88%), #fff 100%);
  }

  .compare-item__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 10px;
  }

  .compare-item__values {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .compare-item__row {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 12px;
    font-size: 13px;
    line-height: 1.7;
    color: #5d6b86;
  }

  .compare-item__row strong {
    color: #18233a;
    text-align: right;
    word-break: break-word;
  }

  .record-list {
    padding: 0;
  }

  .record-item {
    padding: 16px;
    box-shadow: 0 10px 24px rgb(15 23 42 / 4%);
  }

  .record-item--active {
    background: linear-gradient(180deg, rgb(236 253 245 / 88%), #fff 100%);
  }

  .record-item--released {
    background: linear-gradient(180deg, rgb(248 250 252 / 94%), #fff 100%);
  }

  @media (width <= 1080px) {
    .occupancy-overview-grid {
      grid-template-columns: 1fr;
    }
  }

  @media (width <= 900px) {
    .current-occupancy-grid,
    .detail-card-grid,
    .record-detail-grid,
    .empty-occupancy-card__meta,
    .insight-card-grid,
    .insight-card__grid {
      grid-template-columns: 1fr;
    }

    .detail-card--wide {
      grid-column: span 1;
    }

    .current-occupancy-card__header,
    .record-item__header,
    .matrix-item__header,
    .history-toolbar,
    .insight-card__header {
      flex-direction: column;
      align-items: flex-start;
    }

    .history-toolbar__search {
      width: 100%;
    }

    .history-toolbar__range,
    .compare-item__header,
    .compare-item__row {
      width: 100%;
    }

    .compare-item__header,
    .compare-item__row {
      flex-direction: column;
      align-items: flex-start;
    }
  }
</style>
