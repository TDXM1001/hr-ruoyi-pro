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
            <div
              class="insight-card insight-card--interactive"
              data-testid="occupancy-ledger-sync-summary"
              @click="focusActiveHistory"
            >
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
                  class="compare-item compare-item--interactive"
                  :class="item.changed ? 'compare-item--changed' : 'compare-item--stable'"
                  @click.stop="focusActiveHistory"
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

            <div
              class="insight-card"
              :class="latestReleasedRecord ? 'insight-card--interactive' : ''"
              data-testid="occupancy-last-change-summary"
              @click="focusLatestReleasedHistory"
            >
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
                    class="compare-item compare-item--interactive"
                    :class="item.changed ? 'compare-item--changed' : 'compare-item--stable'"
                    @click.stop="focusLatestReleasedHistory"
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

          <div class="occupancy-tab-links">
            <div class="occupancy-tab-links__label">跨页签联动</div>
            <div class="occupancy-tab-links__items">
              <ElButton
                v-for="item in tabLinkOptions"
                :key="item.key"
                :data-testid="`occupancy-tab-link-${item.key}`"
                size="small"
                plain
                @click="emitTabSwitch(item.key)"
              >
                {{ item.label }}
              </ElButton>
            </div>
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

          <div class="occupancy-tab-links">
            <div class="occupancy-tab-links__label">跨页签联动</div>
            <div class="occupancy-tab-links__items">
              <ElButton
                v-for="item in tabLinkOptions"
                :key="item.key"
                :data-testid="`occupancy-tab-link-${item.key}`"
                size="small"
                plain
                @click="emitTabSwitch(item.key)"
              >
                {{ item.label }}
              </ElButton>
            </div>
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
            <div v-if="rule.shortcuts?.length" class="matrix-item__shortcuts">
              <ElButton
                v-for="shortcut in rule.shortcuts"
                :key="shortcut.key"
                :data-testid="shortcut.testId"
                size="small"
                plain
                @click="applyLinkedFilter(shortcut.status)"
              >
                {{ shortcut.label }}
              </ElButton>
            </div>
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

        <div class="history-toolbar__footer">
          <ElInput
            v-model="keyword"
            data-testid="occupancy-keyword-input"
            clearable
            placeholder="搜索占用单号/部门/责任人/位置/原因"
            class="history-toolbar__search"
          />
          <div class="history-toolbar__actions">
            <div class="history-toolbar__filters">
              <ElButton
                data-testid="occupancy-view-list"
                size="small"
                :type="groupViewMode === 'LIST' ? 'primary' : 'default'"
                @click="groupViewMode = 'LIST'"
              >
                平铺视图
              </ElButton>
              <ElButton
                data-testid="occupancy-view-grouped"
                size="small"
                :type="groupViewMode === 'GROUPED' ? 'primary' : 'default'"
                @click="groupViewMode = 'GROUPED'"
              >
                分组视图
              </ElButton>
              <ElButton
                data-testid="occupancy-view-annotation"
                size="small"
                :type="groupViewMode === 'ANNOTATION' ? 'primary' : 'default'"
                @click="groupViewMode = 'ANNOTATION'"
              >
                批注视图
              </ElButton>
            </div>
            <ElButton
              data-testid="occupancy-export-config-toggle"
              size="small"
              @click="exportConfigOpen = !exportConfigOpen"
            >
              导出字段
            </ElButton>
            <ElButton
              data-testid="occupancy-export-link"
              type="primary"
              plain
              :disabled="!filteredRecords.length"
              @click="exportFilteredRecords"
            >
              导出占用轨迹
            </ElButton>
          </div>
        </div>

        <div
          v-if="exportConfigOpen"
          class="export-config-panel"
          data-testid="occupancy-export-config-panel"
        >
          <div class="export-config-panel__header">
            <div class="export-config-panel__title">导出字段配置</div>
            <div class="export-config-panel__desc">
              当前已选择 {{ selectedExportFields.length }} 个字段
            </div>
          </div>
          <div class="export-config-panel__presets">
            <button
              v-for="preset in exportPresetOptions"
              :key="preset.key"
              type="button"
              class="export-preset-chip"
              :data-testid="`occupancy-export-preset-${preset.key}`"
              @click="applyExportPreset(preset.key)"
            >
              {{ preset.label }}
            </button>
          </div>
          <div class="export-config-panel__fields">
            <button
              v-for="field in exportFieldOptions"
              :key="field.key"
              type="button"
              class="export-field-chip"
              :data-testid="`occupancy-export-field-${field.key}`"
              :class="selectedExportFields.includes(field.key) ? 'export-field-chip--active' : ''"
              @click="toggleExportField(field.key)"
            >
              {{ field.label }}
            </button>
          </div>
        </div>
      </div>

      <div ref="historyListRef" class="record-wrapper" data-testid="occupancy-history-list">
        <div v-if="filteredRecords.length" class="record-list">
          <div
            v-if="groupViewMode === 'ANNOTATION'"
            class="annotation-list"
            data-testid="occupancy-annotation-list"
          >
            <div
              v-for="record in filteredRecords"
              :key="`annotation-${getRecordKey(record)}`"
              :data-testid="`occupancy-annotation-card-${getRecordKey(record)}`"
              class="annotation-card"
              :class="[
                record.occupancyStatus === 'ACTIVE'
                  ? 'annotation-card--active'
                  : 'annotation-card--released'
              ]"
            >
              <div class="annotation-card__header">
                <div>
                  <div class="annotation-card__title">
                    {{ record.occupancyNo || '待生成占用单号' }}
                  </div>
                  <div class="annotation-card__meta">
                    {{ getStatusLabel(record.occupancyStatus) }} / {{ record.useDeptName || '-' }} /
                    {{ record.responsibleUserName || '-' }}
                  </div>
                </div>
                <ElTag :type="record.occupancyStatus === 'ACTIVE' ? 'success' : 'warning'" effect="light">
                  轨迹批注
                </ElTag>
              </div>

              <div class="annotation-card__notes">
                <div class="annotation-note">
                  <div class="annotation-note__label">状态说明</div>
                  <div class="annotation-note__value">
                    {{
                      record.occupancyStatus === 'ACTIVE'
                        ? '该轨迹仍是当前有效占用，主档应以这条占用记录为准。'
                        : '该轨迹已经释放，仅保留为历史留痕，不再承接变更或释放动作。'
                    }}
                  </div>
                </div>
                <div class="annotation-note">
                  <div class="annotation-note__label">占用批注</div>
                  <div class="annotation-note__value">{{ record.changeReason || '-' }}</div>
                </div>
                <div class="annotation-note">
                  <div class="annotation-note__label">释放批注</div>
                  <div class="annotation-note__value">{{ record.releaseReason || '-' }}</div>
                </div>
              </div>
            </div>
          </div>

          <div
            v-else
            v-for="group in recordGroups"
            :key="group.key"
            class="record-group"
            :data-testid="groupViewMode === 'GROUPED' ? `occupancy-group-${group.key}` : undefined"
          >
            <div v-if="groupViewMode === 'GROUPED'" class="record-group__header">
              <div class="record-group__title">{{ group.title }}</div>
              <ElTag effect="light">{{ group.records.length }} 条</ElTag>
            </div>

            <div class="record-group__items">
              <div
                v-for="record in group.records"
                :key="record.occupancyId || record.occupancyNo || record.startDate"
                :ref="(element) => setRecordRef(record, element)"
                :data-testid="`occupancy-record-${getRecordKey(record)}`"
                class="record-item"
                :class="[
                  record.occupancyStatus === 'ACTIVE' ? 'record-item--active' : 'record-item--released',
                  focusedRecordKey === getRecordKey(record) ? 'record-item--focused' : ''
                ]"
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
  type StatusFilter = 'ALL' | 'ACTIVE' | 'RELEASED'
  type GroupViewMode = 'LIST' | 'GROUPED' | 'ANNOTATION'
  type LinkedTabName = 'overview' | 'inspection' | 'rectification' | 'disposal'
  type ExportFieldKey =
    | 'occupancyNo'
    | 'occupancyStatus'
    | 'useDeptName'
    | 'responsibleUserName'
    | 'locationName'
    | 'startDate'
    | 'endDate'
    | 'changeReason'
    | 'releaseReason'

  interface OccupancyFilterState {
    statusFilter: StatusFilter
    timeFilter: TimeFilter
    sortDirection: SortDirection
    keyword: string
    customRangeDraftStart: string
    customRangeDraftEnd: string
    customRangeAppliedStart: string
    customRangeAppliedEnd: string
  }

  interface ExportFieldOption {
    key: ExportFieldKey
    label: string
  }

  interface ExportPresetOption {
    key: 'operations' | 'audit' | 'release'
    label: string
    fields: ExportFieldKey[]
  }

  const props = defineProps<{
    detailData: Record<string, any>
    occupancyRecords: AssetRealEstateOccupancyRecord[]
    canEdit?: boolean
  }>()

  const emit = defineEmits<{
    'create-occupancy': []
    'change-occupancy': [record: AssetRealEstateOccupancyRecord]
    'release-occupancy': [record: AssetRealEstateOccupancyRecord]
    'switch-tab': [tab: LinkedTabName]
  }>()

  const historyListRef = ref<HTMLElement>()
  const statusFilter = ref<StatusFilter>('ALL')
  const timeFilter = ref<TimeFilter>('ALL')
  const sortDirection = ref<SortDirection>('DESC')
  const groupViewMode = ref<GroupViewMode>('LIST')
  const exportConfigOpen = ref(false)
  const focusedRecordKey = ref('')
  const filtersReady = ref(false)
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
  const exportFieldOptions: ExportFieldOption[] = [
    { key: 'occupancyNo', label: '占用单号' },
    { key: 'occupancyStatus', label: '占用状态' },
    { key: 'useDeptName', label: '使用部门' },
    { key: 'responsibleUserName', label: '责任人' },
    { key: 'locationName', label: '使用位置' },
    { key: 'startDate', label: '占用起始' },
    { key: 'endDate', label: '释放时间' },
    { key: 'changeReason', label: '发起/变更原因' },
    { key: 'releaseReason', label: '释放原因' }
  ]
  const exportPresetOptions: ExportPresetOption[] = [
    {
      key: 'operations',
      label: '运营摘要',
      fields: ['occupancyNo', 'occupancyStatus', 'useDeptName', 'responsibleUserName', 'locationName']
    },
    {
      key: 'audit',
      label: '审计复盘',
      fields: [...exportFieldOptions.map((item) => item.key)]
    },
    {
      key: 'release',
      label: '释放分析',
      fields: ['occupancyNo', 'occupancyStatus', 'useDeptName', 'locationName', 'endDate', 'changeReason', 'releaseReason']
    }
  ]
  const defaultExportFieldKeys: ExportFieldKey[] = exportFieldOptions.map((item) => item.key)
  const tabLinkOptions: { key: LinkedTabName; label: string }[] = [
    { key: 'overview', label: '回总览核对主档' },
    { key: 'inspection', label: '看巡检联动' },
    { key: 'rectification', label: '看整改进展' },
    { key: 'disposal', label: '看处置关联' }
  ]
  const recordRefs = new Map<string, HTMLElement>()
  const selectedExportFields = ref<ExportFieldKey[]>([...defaultExportFieldKeys])
  const defaultFilterState: OccupancyFilterState = {
    statusFilter: 'ALL',
    timeFilter: 'ALL',
    sortDirection: 'DESC',
    keyword: '',
    customRangeDraftStart: '',
    customRangeDraftEnd: '',
    customRangeAppliedStart: '',
    customRangeAppliedEnd: ''
  }

  const storageKey = computed(() => {
    const assetKey = String(props.detailData.assetCode || props.detailData.assetId || '').trim()
    return assetKey ? `asset-real-estate-occupancy-filters:${assetKey}` : ''
  })
  const exportFieldsStorageKey = 'asset-real-estate-occupancy-export-fields'

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

  const getRecordKey = (record: AssetRealEstateOccupancyRecord) => {
    return String(record.occupancyId || record.occupancyNo || record.startDate || 'unknown')
  }

  const setRecordRef = (record: AssetRealEstateOccupancyRecord, element: Element | null) => {
    const key = getRecordKey(record)
    if (element instanceof HTMLElement) {
      recordRefs.set(key, element)
      return
    }
    recordRefs.delete(key)
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

  const recordGroups = computed(() => {
    if (groupViewMode.value === 'LIST') {
      return [
        {
          key: 'ALL',
          title: '全部轨迹',
          records: filteredRecords.value
        }
      ]
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

  const emitTabSwitch = (tab: LinkedTabName) => {
    emit('switch-tab', tab)
  }

  const focusReleasedHistory = () => {
    statusFilter.value = 'RELEASED'
    resetTimeFilters()
    keyword.value = ''
    nextTick(() => historyListRef.value?.scrollIntoView?.({ behavior: 'smooth', block: 'start' }))
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

  const resetFocusedRecord = () => {
    focusedRecordKey.value = ''
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
    recordRefs.get(focusedRecordKey.value)?.scrollIntoView?.({
      behavior: 'smooth',
      block: 'center'
    })
  }

  const focusActiveHistory = () => focusRecord(activeRecord.value, 'ACTIVE')
  const focusLatestReleasedHistory = () => focusRecord(latestReleasedRecord.value, 'RELEASED')

  const applyLinkedFilter = (status: StatusFilter) => {
    statusFilter.value = status
    sortDirection.value = 'DESC'
    keyword.value = ''
    resetTimeFilters()
    resetFocusedRecord()
    nextTick(() => historyListRef.value?.scrollIntoView?.({ behavior: 'smooth', block: 'start' }))
  }

  const buildPersistedState = (): OccupancyFilterState => {
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

  const restoreExportFields = () => {
    const raw = window.localStorage.getItem(exportFieldsStorageKey)
    if (!raw) {
      selectedExportFields.value = [...defaultExportFieldKeys]
      return
    }

    try {
      const parsed = JSON.parse(raw)
      const nextFields = Array.isArray(parsed)
        ? parsed.filter((item): item is ExportFieldKey =>
            exportFieldOptions.some((field) => field.key === item)
          )
        : []
      selectedExportFields.value = nextFields.length ? nextFields : [...defaultExportFieldKeys]
    } catch {
      selectedExportFields.value = [...defaultExportFieldKeys]
    }
  }

  const toggleExportField = (fieldKey: ExportFieldKey) => {
    if (selectedExportFields.value.includes(fieldKey)) {
      if (selectedExportFields.value.length === 1) {
        return
      }
      selectedExportFields.value = selectedExportFields.value.filter((item) => item !== fieldKey)
      return
    }
    selectedExportFields.value = [...selectedExportFields.value, fieldKey]
  }

  const applyExportPreset = (presetKey: ExportPresetOption['key']) => {
    const preset = exportPresetOptions.find((item) => item.key === presetKey)
    if (!preset) {
      return
    }
    selectedExportFields.value = [...preset.fields]
  }

  const resolveExportValue = (record: AssetRealEstateOccupancyRecord, fieldKey: ExportFieldKey) => {
    if (fieldKey === 'occupancyStatus') {
      return getStatusLabel(record.occupancyStatus)
    }
    return String(record[fieldKey] || '')
  }

  const escapeCsvCell = (value?: string) => {
    const normalized = String(value || '').replace(/"/g, '""')
    return `"${normalized}"`
  }

  const exportFilteredRecords = () => {
    if (!filteredRecords.value.length) {
      return
    }

    const exportFields = exportFieldOptions.filter((item) =>
      selectedExportFields.value.includes(item.key)
    )
    const header = exportFields.map((item) => item.label)
    const rows = filteredRecords.value.map((record) => {
      return exportFields
        .map((field) => resolveExportValue(record, field.key))
        .map((item) => escapeCsvCell(item))
        .join(',')
    })
    const csvContent = `\uFEFF${header.map((item) => escapeCsvCell(item)).join(',')}\n${rows.join('\n')}`
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${props.detailData.assetCode || 'asset'}-occupancy-history.csv`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
  }

  watch(storageKey, () => {
    restorePersistedFilters()
  }, { immediate: true })

  onMounted(() => {
    restoreExportFields()
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
      window.localStorage.setItem(storageKey.value, JSON.stringify(buildPersistedState()))
    }
  )

  watch(
    selectedExportFields,
    (value) => {
      window.localStorage.setItem(exportFieldsStorageKey, JSON.stringify(value))
    },
    { deep: true }
  )

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
  .history-toolbar__filters,
  .occupancy-tab-links__items {
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

  .insight-card--interactive,
  .compare-item--interactive {
    cursor: pointer;
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

  .matrix-item__shortcuts {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    margin-top: 12px;
  }

  .occupancy-tab-links {
    display: flex;
    flex-direction: column;
    gap: 10px;
    padding: 14px 16px;
    border: 1px dashed #cfd9e8;
    border-radius: 14px;
    background: linear-gradient(180deg, rgb(248 250 252 / 96%), #fff 100%);
  }

  .occupancy-tab-links__label {
    font-size: 12px;
    font-weight: 600;
    color: #6f7f99;
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

  .history-toolbar__footer {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
  }

  .history-toolbar__actions {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    justify-content: flex-end;
    gap: 10px;
  }

  .export-config-panel {
    display: flex;
    flex-direction: column;
    gap: 12px;
    margin-top: 12px;
    padding: 14px 16px;
    border: 1px dashed #cfd9e8;
    border-radius: 14px;
    background: linear-gradient(180deg, rgb(248 250 252 / 96%), #fff 100%);
  }

  .export-config-panel__header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 12px;
  }

  .export-config-panel__title {
    font-size: 14px;
    font-weight: 700;
    color: #18233a;
  }

  .export-config-panel__desc {
    font-size: 12px;
    color: #6f7f99;
  }

  .export-config-panel__fields {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
  }

  .export-config-panel__presets {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
  }

  .export-field-chip {
    padding: 6px 12px;
    border: 1px solid #d7e1ee;
    border-radius: 999px;
    font-size: 12px;
    line-height: 1.5;
    color: #4a5a74;
    background: #fff;
    cursor: pointer;
    transition: all 0.2s ease;
  }

  .export-preset-chip {
    padding: 6px 12px;
    border: 1px solid #cfe1ff;
    border-radius: 999px;
    font-size: 12px;
    line-height: 1.5;
    color: #1d4ed8;
    background: rgb(239 246 255 / 92%);
    cursor: pointer;
    transition: all 0.2s ease;
  }

  .export-preset-chip:hover,
  .export-field-chip:hover {
    transform: translateY(-1px);
  }

  .export-field-chip--active {
    border-color: #60a5fa;
    color: #1d4ed8;
    background: rgb(239 246 255 / 92%);
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

  .record-item--focused {
    border-color: #60a5fa;
    box-shadow: 0 0 0 2px rgb(96 165 250 / 18%), 0 12px 26px rgb(37 99 235 / 10%);
  }

  .record-group {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .annotation-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .annotation-card {
    display: flex;
    flex-direction: column;
    gap: 14px;
    padding: 16px;
    border: 1px solid #e7edf6;
    border-radius: 14px;
    background: #fff;
    box-shadow: 0 10px 24px rgb(15 23 42 / 4%);
  }

  .annotation-card--active {
    background: linear-gradient(180deg, rgb(236 253 245 / 88%), #fff 100%);
  }

  .annotation-card--released {
    background: linear-gradient(180deg, rgb(255 247 237 / 92%), #fff 100%);
  }

  .annotation-card__header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 12px;
  }

  .annotation-card__title {
    font-size: 16px;
    font-weight: 700;
    color: #18233a;
  }

  .annotation-card__meta {
    margin-top: 6px;
    font-size: 13px;
    line-height: 1.7;
    color: #5d6b86;
  }

  .annotation-card__notes {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 12px;
  }

  .annotation-note {
    padding: 14px 16px;
    border: 1px solid #dce5f2;
    border-radius: 14px;
    background: rgb(255 255 255 / 88%);
  }

  .annotation-note__label {
    font-size: 12px;
    font-weight: 600;
    color: #6f7f99;
  }

  .annotation-note__value {
    margin-top: 10px;
    font-size: 13px;
    line-height: 1.8;
    color: #18233a;
    white-space: pre-wrap;
    word-break: break-word;
  }

  .record-group__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
  }

  .record-group__title {
    font-size: 14px;
    font-weight: 700;
    color: #18233a;
  }

  .record-group__items {
    display: flex;
    flex-direction: column;
    gap: 16px;
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
    .insight-card__grid,
    .annotation-card__notes {
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

    .history-toolbar__footer {
      align-items: stretch;
    }

    .history-toolbar__actions,
    .export-config-panel__header,
    .record-group__header,
    .annotation-card__header {
      width: 100%;
      justify-content: flex-start;
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
