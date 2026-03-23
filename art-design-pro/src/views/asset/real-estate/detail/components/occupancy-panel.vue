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

          <div class="occupancy-link-stats">
            <div class="occupancy-link-stats__header">
              <div class="occupancy-link-stats__meta">
                <div class="occupancy-link-stats__title">来源链路统计</div>
                <div
                  class="occupancy-link-stats__last"
                  data-testid="occupancy-link-stat-last-target"
                >
                  最近一次联动：{{ displayedLinkLastTargetLabel || '暂无' }}
                </div>
              </div>
              <div class="occupancy-link-stats__toolbar">
                <div class="occupancy-link-stats__window-switch">
                  <button
                    v-for="item in linkStatsWindowOptions"
                    :key="item.key"
                    type="button"
                    class="export-preset-chip export-preset-chip--subtle"
                    :class="linkStatsWindow === item.key ? 'export-preset-chip--active' : ''"
                    :data-testid="`occupancy-link-window-${item.key.toLowerCase()}`"
                    @click="setLinkStatsWindow(item.key)"
                  >
                    {{ item.label }}
                  </button>
                </div>
                <div class="occupancy-link-stats__window-switch">
                  <button
                    v-for="item in linkStatsResetScopeOptions"
                    :key="item.key"
                    type="button"
                    class="export-preset-chip export-preset-chip--subtle"
                    :class="linkStatsResetScope === item.key ? 'export-preset-chip--active' : ''"
                    :data-testid="`occupancy-link-stats-reset-scope-${item.key.toLowerCase()}`"
                    @click="linkStatsResetScope = item.key"
                  >
                    {{ item.label }}
                  </button>
                </div>
                <button
                  type="button"
                  class="export-preset-chip export-preset-chip--subtle"
                  data-testid="occupancy-link-stats-reset"
                  @click="resetLinkStatsView"
                >
                  重置统计
                </button>
              </div>
            </div>
            <div class="occupancy-link-stats__grid">
              <div
                v-for="item in linkStatItems"
                :key="item.key"
                class="occupancy-link-stats__item"
                :data-testid="`occupancy-link-stat-${item.key}`"
              >
                <div class="occupancy-link-stats__label">{{ item.label }}</div>
                <div class="occupancy-link-stats__value">{{ item.count }}</div>
              </div>
            </div>
            <div class="occupancy-link-trend" data-testid="occupancy-link-trend-chart">
              <div class="occupancy-link-trend__title">{{ linkTrendTitle }}</div>
              <div class="occupancy-link-trend__grid">
                <div
                  v-for="item in linkTrendItems"
                  :key="item.date"
                  class="occupancy-link-trend__item"
                  :class="trendDrilldown?.date === item.date ? 'occupancy-link-trend__item--active' : ''"
                  :data-testid="`occupancy-link-trend-day-${item.date}`"
                  @click="toggleTrendDrilldown(item)"
                >
                  <div class="occupancy-link-trend__date">{{ item.label }}</div>
                  <div class="occupancy-link-trend__bar-wrap">
                    <div
                      class="occupancy-link-trend__bar"
                      :style="{ height: `${item.barHeight}%` }"
                    />
                  </div>
                  <div class="occupancy-link-trend__count">{{ item.count }}</div>
                  <div class="occupancy-link-trend__target">{{ item.topLabel }}</div>
                </div>
              </div>
              <div
                v-if="trendDrilldown"
                class="occupancy-link-drilldown"
                data-testid="occupancy-link-drilldown-panel"
              >
                <div class="occupancy-link-drilldown__title">
                  趋势钻取：{{ trendDrilldown.date }}
                </div>
                <div class="occupancy-link-drilldown__desc">
                  当前聚焦 {{ trendDrilldown.date }} 的来源联动摘要，主目标为“{{ trendDrilldown.label }}”，共
                  {{ trendDrilldown.count }} 次。
                </div>
                <input
                  v-model="trendSnapshotName"
                  data-testid="occupancy-link-drilldown-snapshot-name"
                  type="text"
                  class="preset-name-field__input occupancy-link-drilldown__input"
                  placeholder="输入快照名称，可选"
                />
                <div class="occupancy-link-drilldown__actions">
                  <ElButton
                    size="small"
                    text
                    type="primary"
                    data-testid="occupancy-link-drilldown-save-snapshot"
                    @click="saveTrendSnapshot"
                  >
                    保存快照
                  </ElButton>
                  <ElButton
                    size="small"
                    text
                    type="primary"
                    data-testid="occupancy-link-drilldown-clear"
                    @click="clearTrendDrilldown"
                  >
                    取消钻取
                  </ElButton>
                </div>
              </div>
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

          <div class="occupancy-link-stats">
            <div class="occupancy-link-stats__header">
              <div class="occupancy-link-stats__meta">
                <div class="occupancy-link-stats__title">来源链路统计</div>
                <div
                  class="occupancy-link-stats__last"
                  data-testid="occupancy-link-stat-last-target"
                >
                  最近一次联动：{{ displayedLinkLastTargetLabel || '暂无' }}
                </div>
              </div>
              <div class="occupancy-link-stats__toolbar">
                <div class="occupancy-link-stats__window-switch">
                  <button
                    v-for="item in linkStatsWindowOptions"
                    :key="item.key"
                    type="button"
                    class="export-preset-chip export-preset-chip--subtle"
                    :class="linkStatsWindow === item.key ? 'export-preset-chip--active' : ''"
                    :data-testid="`occupancy-link-window-${item.key.toLowerCase()}`"
                    @click="setLinkStatsWindow(item.key)"
                  >
                    {{ item.label }}
                  </button>
                </div>
                <button
                  type="button"
                  class="export-preset-chip export-preset-chip--subtle"
                  data-testid="occupancy-link-stats-reset"
                  @click="resetLinkStatsView"
                >
                  重置统计
                </button>
              </div>
            </div>
            <div class="occupancy-link-stats__grid">
              <div
                v-for="item in linkStatItems"
                :key="item.key"
                class="occupancy-link-stats__item"
                :data-testid="`occupancy-link-stat-${item.key}`"
              >
                <div class="occupancy-link-stats__label">{{ item.label }}</div>
                <div class="occupancy-link-stats__value">{{ item.count }}</div>
              </div>
            </div>
            <div class="occupancy-link-trend" data-testid="occupancy-link-trend-chart">
              <div class="occupancy-link-trend__title">{{ linkTrendTitle }}</div>
              <div class="occupancy-link-trend__grid">
                <div
                  v-for="item in linkTrendItems"
                  :key="item.date"
                  class="occupancy-link-trend__item"
                  :class="trendDrilldown?.date === item.date ? 'occupancy-link-trend__item--active' : ''"
                  :data-testid="`occupancy-link-trend-day-${item.date}`"
                  @click="toggleTrendDrilldown(item)"
                >
                  <div class="occupancy-link-trend__date">{{ item.label }}</div>
                  <div class="occupancy-link-trend__bar-wrap">
                    <div
                      class="occupancy-link-trend__bar"
                      :style="{ height: `${item.barHeight}%` }"
                    />
                  </div>
                  <div class="occupancy-link-trend__count">{{ item.count }}</div>
                  <div class="occupancy-link-trend__target">{{ item.topLabel }}</div>
                </div>
              </div>
              <div
                v-if="trendDrilldown"
                class="occupancy-link-drilldown"
                data-testid="occupancy-link-drilldown-panel"
              >
                <div class="occupancy-link-drilldown__title">
                  趋势钻取：{{ trendDrilldown.date }}
                </div>
                <div class="occupancy-link-drilldown__desc">
                  当前聚焦 {{ trendDrilldown.date }} 的来源联动摘要，主目标为“{{ trendDrilldown.label }}”，共
                  {{ trendDrilldown.count }} 次。
                </div>
                <ElButton
                  size="small"
                  text
                  type="primary"
                  data-testid="occupancy-link-drilldown-clear"
                  @click="clearTrendDrilldown"
                >
                  取消钻取
                </ElButton>
              </div>
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
        <div class="history-card-header">
          <div>
            <div class="card-title">占用历史记录</div>
            <div
              v-if="historyDrilldownTip"
              class="history-card-header__tip"
              data-testid="occupancy-history-drilldown-tip"
            >
              {{ historyDrilldownTip }}
            </div>
          </div>
          <div
            v-if="savedTrendSnapshot"
            class="history-card-header__actions"
          >
            <button
              type="button"
              class="export-preset-chip export-preset-chip--subtle"
              data-testid="occupancy-history-restore-snapshot"
              @click="applySavedTrendSnapshot()"
            >
              恢复快照
            </button>
            <button
              type="button"
              class="export-preset-chip export-preset-chip--subtle"
              data-testid="occupancy-history-clear-snapshot"
              @click="clearSavedTrendSnapshot()"
            >
              清空快照
            </button>
          </div>
        </div>
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
              data-testid="occupancy-governance-toggle"
              size="small"
              @click="toggleGovernancePanel"
            >
              {{ governanceOpen ? '收起治理工具' : '治理工具' }}
            </ElButton>
            <ElButton
              v-if="groupViewMode === 'ANNOTATION'"
              data-testid="occupancy-export-annotation-link"
              size="small"
              type="success"
              plain
              :disabled="!filteredRecords.length"
              @click="exportAnnotationRecords"
            >
              导出批注
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
            <div class="export-config-panel__header-actions">
              <div class="export-config-panel__desc">
                当前已选择 {{ selectedExportFields.length }} 个字段
              </div>
              <ElButton
                data-testid="occupancy-preset-name-toggle"
                size="small"
                text
                @click="presetNameEditOpen = !presetNameEditOpen"
              >
                {{ presetNameEditOpen ? '收起命名设置' : '命名设置' }}
              </ElButton>
              <ElButton
                data-testid="occupancy-preset-import-toggle"
                size="small"
                text
                @click="presetImportOpen = !presetImportOpen"
              >
                {{ presetImportOpen ? '收起导入预设' : '导入预设' }}
              </ElButton>
              <ElButton
                data-testid="occupancy-preset-copy-toggle"
                size="small"
                text
                @click="presetCopyOpen = !presetCopyOpen"
              >
                {{ presetCopyOpen ? '收起复制新预设' : '复制新预设' }}
              </ElButton>
              <ElButton
                data-testid="occupancy-preset-export-link"
                size="small"
                text
                :disabled="!customExportPresets.length"
                @click="exportCustomPresets"
              >
                导出预设
              </ElButton>
              <ElButton
                data-testid="occupancy-governance-toggle-inline"
                size="small"
                text
                @click="toggleGovernancePanel"
              >
                {{ governanceOpen ? '收起治理工具' : '治理工具' }}
              </ElButton>
            </div>
          </div>
          <div
            v-if="presetImportOpen"
            class="preset-import-panel"
            data-testid="occupancy-preset-import-panel"
          >
            <div class="preset-import-panel__title">导入自定义预设</div>
            <div class="preset-import-panel__desc">
              粘贴由“导出预设”生成的 JSON 文本，系统会自动过滤非法字段并追加为新的自定义预设。
            </div>
            <textarea
              v-model="presetImportText"
              data-testid="occupancy-preset-import-input"
              class="preset-import-panel__input"
              placeholder="请粘贴自定义预设 JSON"
            />
            <div class="preset-import-panel__actions">
              <ElButton
                data-testid="occupancy-preset-import-preview"
                size="small"
                plain
                @click="previewImportedPresets"
              >
                解析预览
              </ElButton>
              <ElButton
                data-testid="occupancy-preset-import-apply"
                size="small"
                type="primary"
                plain
                :disabled="!presetImportPreviewItems.length"
                @click="applyImportedPresets"
              >
                确认导入
              </ElButton>
            </div>
            <div
              v-if="presetImportPreviewItems.length || presetImportInvalidItems.length"
              class="preset-import-preview"
              data-testid="occupancy-preset-import-preview-panel"
            >
              <div class="preset-import-preview__header">
                <div class="preset-import-preview__title">导入预览</div>
                <div class="preset-import-preview__policies">
                  <button
                    type="button"
                    class="export-preset-chip export-preset-chip--subtle"
                    data-testid="occupancy-preset-import-export-report"
                    @click="exportPresetImportReport"
                  >
                    导出校验报告
                  </button>
                  <button
                    type="button"
                    class="export-preset-chip export-preset-chip--subtle"
                    :disabled="!canReuseLastPolicies"
                    data-testid="occupancy-preset-import-reuse-last-policies"
                    @click="reuseLastImportPolicies"
                  >
                    复用上次策略
                  </button>
                  <button
                    v-for="item in importConflictPolicyOptions"
                    :key="item.key"
                    type="button"
                    class="export-preset-chip export-preset-chip--subtle"
                    :class="presetImportPolicy === item.key ? 'export-preset-chip--active' : ''"
                    :data-testid="`occupancy-preset-import-policy-${item.key.toLowerCase()}`"
                    @click="presetImportPolicy = item.key"
                  >
                    {{ item.label }}
                  </button>
                </div>
              </div>
              <div class="preset-import-preview__summary" data-testid="occupancy-preset-import-summary">
                <span>可导入 {{ presetImportSummary.importableCount }}</span>
                <span>冲突 {{ presetImportSummary.conflictCount }}</span>
                <span>无效 {{ presetImportSummary.invalidCount }}</span>
              </div>
              <div
                v-if="presetImportInvalidItems.length"
                class="preset-import-preview__invalid-list"
                data-testid="occupancy-preset-import-invalid-list"
              >
                <div
                  v-for="item in presetImportInvalidItems"
                  :key="item.key"
                  class="preset-import-preview__invalid-item"
                >
                  <strong>{{ item.label }}</strong>
                  <span>{{ item.reason }}</span>
                  <span v-if="item.invalidFields.length">
                    非法字段：{{ item.invalidFields.join('、') }}
                  </span>
                </div>
              </div>
              <div class="preset-import-preview__list">
                <div
                  v-for="item in presetImportPreviewItems"
                  :key="item.key"
                  class="preset-import-preview__item"
                >
                  <div class="preset-import-preview__item-header">
                    <strong>{{ item.label }}</strong>
                    <span>{{ buildImportConflictLabel(item.conflictType) }}</span>
                  </div>
                  <div class="preset-import-preview__item-desc">
                    字段数：{{ item.fields.length }}，导入后名称：{{ item.resolvedLabel }}
                  </div>
                  <div
                    v-if="item.invalidFields.length"
                    class="preset-import-preview__item-desc"
                  >
                    已过滤非法字段：{{ item.invalidFields.join('、') }}
                  </div>
                  <div
                    v-if="item.conflictType !== 'none'"
                    class="preset-import-preview__item-policies"
                  >
                    <button
                      type="button"
                      class="export-preset-chip export-preset-chip--subtle"
                      :class="item.policyOverride === 'DEFAULT' ? 'export-preset-chip--active' : ''"
                      :data-testid="`occupancy-preset-import-item-policy-default-${item.key}`"
                      @click="setPreviewItemPolicy(item.key, 'DEFAULT')"
                    >
                      跟随全局
                    </button>
                    <button
                      type="button"
                      class="export-preset-chip export-preset-chip--subtle"
                      :class="item.policyOverride === 'SKIP' ? 'export-preset-chip--active' : ''"
                      :data-testid="`occupancy-preset-import-item-policy-skip-${item.key}`"
                      @click="setPreviewItemPolicy(item.key, 'SKIP')"
                    >
                      跳过
                    </button>
                    <button
                      type="button"
                      class="export-preset-chip export-preset-chip--subtle"
                      :class="item.policyOverride === 'RENAME' ? 'export-preset-chip--active' : ''"
                      :data-testid="`occupancy-preset-import-item-policy-rename-${item.key}`"
                      @click="setPreviewItemPolicy(item.key, 'RENAME')"
                    >
                      改名
                    </button>
                    <button
                      v-if="item.conflictType === 'custom'"
                      type="button"
                      class="export-preset-chip export-preset-chip--subtle"
                      :class="item.policyOverride === 'OVERWRITE' ? 'export-preset-chip--active' : ''"
                      :data-testid="`occupancy-preset-import-item-policy-overwrite-${item.key}`"
                      @click="setPreviewItemPolicy(item.key, 'OVERWRITE')"
                    >
                      覆盖
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div
            v-if="lastPresetImportResult"
            class="preset-import-last-result"
            data-testid="occupancy-preset-import-last-result"
          >
            <div class="preset-import-last-result__header">
              <div class="preset-import-last-result__title">最近一次导入处理结果</div>
              <div class="preset-import-last-result__time">
                {{ lastPresetImportResult.executedAt || '-' }}
              </div>
            </div>
            <div class="preset-import-last-result__summary">
              <span>全局策略：{{ buildImportPolicyLabel(lastPresetImportResult.globalPolicy) }}</span>
              <span>{{ lastPresetImportResultSummary }}</span>
            </div>
            <div class="preset-import-last-result__items">
              <div
                v-for="item in lastPresetImportResult.items.slice(0, 4)"
                :key="`${item.label}-${item.resolvedLabel}`"
                class="preset-import-last-result__item"
              >
                <strong>{{ item.label }}</strong>
                <span>{{ buildImportConflictLabel(item.conflictType) }}</span>
                <span>{{ buildImportPolicyLabel(item.effectivePolicy) }}</span>
                <span>结果：{{ item.resolvedLabel }}</span>
              </div>
            </div>
          </div>
          <div
            v-if="governanceOpen"
            class="governance-panel"
            data-testid="occupancy-governance-panel"
          >
            <div
              class="governance-panel__section governance-panel__section--wide governance-panel__summary"
              data-testid="occupancy-governance-summary-card"
            >
              <div class="governance-panel__section-header">
                <div>
                  <div class="governance-panel__section-title">当前治理状态</div>
                  <div class="governance-panel__section-desc">
                    汇总当前模板、快照、最近治理动作和最近导出时间，便于先看全局，再进入细节治理。
                  </div>
                </div>
                <ElButton
                  data-testid="occupancy-governance-export-audit"
                  size="small"
                  type="primary"
                  plain
                  @click="exportGovernanceAuditPackage"
                >
                  导出治理审计包
                </ElButton>
              </div>
              <div class="governance-panel__summary-grid">
                <div class="governance-summary-item">
                  <span class="governance-summary-item__label">策略模板数</span>
                  <strong class="governance-summary-item__value">{{ importPolicyTemplates.length }}</strong>
                  <span class="governance-summary-item__meta">
                    {{ importPolicyTemplates[0]?.name || '暂无策略模板' }}
                  </span>
                </div>
                <div class="governance-summary-item">
                  <span class="governance-summary-item__label">趋势快照数</span>
                  <strong class="governance-summary-item__value">{{ savedTrendSnapshotHistory.length }}</strong>
                  <span class="governance-summary-item__meta">
                    {{ savedTrendSnapshotHistory[0]?.name || '暂无趋势快照' }}
                  </span>
                </div>
                <div class="governance-summary-item">
                  <span class="governance-summary-item__label">最近一次治理动作</span>
                  <strong class="governance-summary-item__value">
                    {{ latestGovernanceActivity?.label || '暂无' }}
                  </strong>
                  <span class="governance-summary-item__meta">
                    {{ latestGovernanceActivity?.executedAt || '暂无治理留痕' }}
                  </span>
                </div>
                <div class="governance-summary-item">
                  <span class="governance-summary-item__label">最近一次导出</span>
                  <strong class="governance-summary-item__value">
                    {{ governanceExportMeta?.exportedAt || '暂无' }}
                  </strong>
                  <span class="governance-summary-item__meta">
                    {{ governanceExportMeta?.fileName || '尚未导出治理审计包' }}
                  </span>
                </div>
              </div>
            </div>

            <div class="governance-panel__section governance-panel__section--wide">
              <div class="governance-panel__section-title">最近治理活动</div>
              <div class="governance-panel__section-desc">
                把模板、快照、重置和导出动作收进统一活动流，支持按类型和关键字回看。
              </div>
              <div class="governance-panel__form">
                <button
                  type="button"
                  class="export-preset-chip export-preset-chip--subtle"
                  :class="governanceActivityFilter === 'ALL' ? 'export-preset-chip--active' : ''"
                  data-testid="occupancy-governance-activity-filter-all"
                  @click="governanceActivityFilter = 'ALL'"
                >
                  全部
                </button>
                <button
                  type="button"
                  class="export-preset-chip export-preset-chip--subtle"
                  :class="governanceActivityFilter === 'TEMPLATE' ? 'export-preset-chip--active' : ''"
                  data-testid="occupancy-governance-activity-filter-template"
                  @click="governanceActivityFilter = 'TEMPLATE'"
                >
                  模板
                </button>
                <button
                  type="button"
                  class="export-preset-chip export-preset-chip--subtle"
                  :class="governanceActivityFilter === 'SNAPSHOT' ? 'export-preset-chip--active' : ''"
                  data-testid="occupancy-governance-activity-filter-snapshot"
                  @click="governanceActivityFilter = 'SNAPSHOT'"
                >
                  快照
                </button>
                <button
                  type="button"
                  class="export-preset-chip export-preset-chip--subtle"
                  :class="governanceActivityFilter === 'RESET' ? 'export-preset-chip--active' : ''"
                  data-testid="occupancy-governance-activity-filter-reset"
                  @click="governanceActivityFilter = 'RESET'"
                >
                  重置
                </button>
                <button
                  type="button"
                  class="export-preset-chip export-preset-chip--subtle"
                  :class="governanceActivityFilter === 'EXPORT' ? 'export-preset-chip--active' : ''"
                  data-testid="occupancy-governance-activity-filter-export"
                  @click="governanceActivityFilter = 'EXPORT'"
                >
                  导出
                </button>
                <input
                  v-model="governanceActivityKeyword"
                  data-testid="occupancy-governance-activity-keyword"
                  type="text"
                  class="preset-name-field__input"
                  placeholder="搜索治理动作或对象"
                />
              </div>
              <div
                v-if="filteredGovernanceActivities.length"
                class="governance-panel__list"
                data-testid="occupancy-governance-activity-list"
              >
                <div
                  v-for="(item, index) in filteredGovernanceActivities.slice(0, 8)"
                  :key="item.key"
                  class="governance-panel__item"
                  :data-testid="`occupancy-governance-activity-item-${index}`"
                >
                  <div class="governance-panel__item-main">
                    <strong>{{ item.label }}</strong>
                    <span>{{ buildGovernanceActivityTypeLabel(item.type) }}</span>
                    <span>{{ item.target }}</span>
                    <span>{{ item.summary }}</span>
                    <span>{{ item.executedAt }}</span>
                  </div>
                </div>
              </div>
            </div>

            <div class="governance-panel__section">
              <div class="governance-panel__section-title">导入策略模板</div>
              <div class="governance-panel__section-desc">
                将当前导入预览的全局策略与逐条覆盖策略沉淀为模板，后续可复用。
              </div>
              <div class="governance-panel__form">
                <input
                  v-model="policyTemplateName"
                  data-testid="occupancy-policy-template-name"
                  type="text"
                  class="preset-name-field__input"
                  placeholder="输入模板名称，可选"
                />
                <ElButton
                  data-testid="occupancy-policy-template-save"
                  size="small"
                  type="primary"
                  plain
                  :disabled="!presetImportPreviewItems.length"
                  @click="saveImportPolicyTemplate"
                >
                  保存当前策略
                </ElButton>
              </div>
              <div
                v-if="importPolicyTemplates.length"
                class="governance-panel__list"
              >
                <div
                  v-for="(item, index) in importPolicyTemplates"
                  :key="item.key"
                  class="governance-panel__item"
                  :data-testid="`occupancy-policy-template-item-${index}`"
                >
                  <div class="governance-panel__item-main">
                    <strong>{{ item.name }}</strong>
                    <span>全局策略：{{ buildImportPolicyLabel(item.globalPolicy) }}</span>
                    <span>逐条策略：{{ item.itemPolicies.length }} 项</span>
                    <span>创建时间：{{ item.createdAt }}</span>
                    <span>最近应用：{{ item.lastAppliedAt || '暂无' }}</span>
                  </div>
                  <div class="governance-panel__item-actions">
                    <button
                      type="button"
                      class="export-preset-chip export-preset-chip--subtle"
                      :data-testid="`occupancy-policy-template-apply-${index}`"
                      @click="applyImportPolicyTemplate(item.key)"
                    >
                      应用
                    </button>
                    <button
                      type="button"
                      class="export-preset-chip export-preset-chip--subtle"
                      :data-testid="`occupancy-policy-template-detail-${index}`"
                      @click="openPolicyTemplateDetail(item.key)"
                    >
                      详情
                    </button>
                    <button
                      type="button"
                      class="export-preset-chip export-preset-chip--subtle"
                      :data-testid="`occupancy-policy-template-delete-${index}`"
                      @click="removeImportPolicyTemplate(item.key)"
                    >
                      删除
                    </button>
                  </div>
                </div>
              </div>
              <div
                v-if="selectedPolicyTemplate"
                class="governance-panel__detail"
                data-testid="occupancy-policy-template-detail-panel"
              >
                <div class="governance-panel__detail-title">
                  {{ selectedPolicyTemplate.name }}
                </div>
                <div class="governance-panel__detail-grid">
                  <span>创建时间：{{ selectedPolicyTemplate.createdAt }}</span>
                  <span>全局策略：{{ buildImportPolicyLabel(selectedPolicyTemplate.globalPolicy) }}</span>
                  <span>最近应用：{{ selectedPolicyTemplate.lastAppliedAt || '暂无' }}</span>
                  <span>最近命中：{{ selectedPolicyTemplate.lastMatchedCount ?? 0 }} 项</span>
                </div>
                <div class="governance-panel__detail-desc">
                  {{ selectedPolicyTemplate.lastAppliedSummary || '暂无最近应用结果摘要。' }}
                </div>
              </div>
            </div>

            <div class="governance-panel__section">
              <div class="governance-panel__section-title">趋势快照管理</div>
              <div class="governance-panel__section-desc">
                管理已保存的趋势钻取快照，支持恢复和删除，顶部快捷入口默认作用于最近一次快照。
              </div>
              <div
                v-if="visibleSavedTrendSnapshots.length"
                class="governance-panel__list"
              >
                <div
                  v-for="(item, index) in visibleSavedTrendSnapshots"
                  :key="item.key"
                  class="governance-panel__item"
                  :data-testid="`occupancy-snapshot-item-${index}`"
                >
                  <div class="governance-panel__item-main">
                    <strong>{{ item.name }}</strong>
                    <span>{{ item.savedAt }}</span>
                  </div>
                  <div class="governance-panel__item-actions">
                    <button
                      type="button"
                      class="export-preset-chip export-preset-chip--subtle"
                      :data-testid="`occupancy-snapshot-compare-left-${index}`"
                      @click="markSnapshotCompareSide('left', item.key)"
                    >
                      对比 A
                    </button>
                    <button
                      type="button"
                      class="export-preset-chip export-preset-chip--subtle"
                      :data-testid="`occupancy-snapshot-compare-right-${index}`"
                      @click="markSnapshotCompareSide('right', item.key)"
                    >
                      对比 B
                    </button>
                    <button
                      type="button"
                      class="export-preset-chip export-preset-chip--subtle"
                      :data-testid="`occupancy-snapshot-restore-${index}`"
                      @click="applySavedTrendSnapshot(item)"
                    >
                      恢复
                    </button>
                    <button
                      type="button"
                      class="export-preset-chip export-preset-chip--subtle"
                      :data-testid="`occupancy-snapshot-delete-${index}`"
                      @click="clearSavedTrendSnapshot(item.key)"
                    >
                      删除
                    </button>
                  </div>
                </div>
              </div>
              <div
                v-if="snapshotCompareItems.length"
                class="governance-panel__detail"
                data-testid="occupancy-snapshot-compare-panel"
              >
                <div class="governance-panel__detail-title">
                  {{ selectedSnapshotCompareLeft?.name }} vs {{ selectedSnapshotCompareRight?.name }}
                </div>
                <div class="governance-panel__detail-desc">{{ snapshotCompareSummary }}</div>
                <div
                  v-for="item in snapshotCompareItems"
                  :key="item.key"
                  class="governance-panel__compare-item"
                >
                  <strong>{{ item.label }}</strong>
                  <span>{{ item.leftValue }} / {{ item.rightValue }}</span>
                  <span>{{ item.changed ? '存在差异' : '一致' }}</span>
                </div>
              </div>
            </div>

            <div class="governance-panel__section">
              <div class="governance-panel__section-title">重置记录</div>
              <div class="governance-panel__section-desc">
                记录趋势、来源和全量重置操作，便于回看治理动作。
              </div>
              <div class="governance-panel__form">
                <button
                  type="button"
                  class="export-preset-chip export-preset-chip--subtle"
                  :class="resetLogFilterScope === 'ALL_RECORDS' ? 'export-preset-chip--active' : ''"
                  data-testid="occupancy-reset-log-filter-all"
                  @click="resetLogFilterScope = 'ALL_RECORDS'"
                >
                  全部记录
                </button>
                <button
                  type="button"
                  class="export-preset-chip export-preset-chip--subtle"
                  :class="resetLogFilterScope === 'EVENTS' ? 'export-preset-chip--active' : ''"
                  data-testid="occupancy-reset-log-filter-events"
                  @click="resetLogFilterScope = 'EVENTS'"
                >
                  只重置趋势
                </button>
                <button
                  type="button"
                  class="export-preset-chip export-preset-chip--subtle"
                  :class="resetLogFilterScope === 'COUNTS' ? 'export-preset-chip--active' : ''"
                  data-testid="occupancy-reset-log-filter-counts"
                  @click="resetLogFilterScope = 'COUNTS'"
                >
                  只重置来源
                </button>
                <input
                  v-model="resetLogKeyword"
                  data-testid="occupancy-reset-log-keyword"
                  type="text"
                  class="preset-name-field__input"
                  placeholder="搜索重置摘要"
                />
                <ElButton
                  data-testid="occupancy-reset-log-export"
                  size="small"
                  type="primary"
                  plain
                  :disabled="!filteredLinkResetLogs.length"
                  @click="exportFilteredResetLogs"
                >
                  导出记录
                </ElButton>
              </div>
              <div
                v-if="filteredLinkResetLogs.length"
                class="governance-panel__list"
              >
                <div
                  v-for="(item, index) in filteredLinkResetLogs.slice(0, 5)"
                  :key="item.key"
                  class="governance-panel__item"
                  :data-testid="`occupancy-reset-log-item-${index}`"
                >
                  <div class="governance-panel__item-main">
                    <strong>{{ buildResetScopeLabel(item.scope) }}</strong>
                    <span>{{ item.summary }}</span>
                    <span>{{ item.executedAt }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div
            v-if="presetNameEditOpen"
            class="preset-name-panel"
            data-testid="occupancy-preset-name-panel"
          >
            <div class="preset-name-panel__grid">
              <label
                v-for="preset in exportPresetOptions"
                :key="preset.key"
                class="preset-name-field"
              >
                <span class="preset-name-field__label">{{ preset.label }}</span>
                <input
                  v-model="exportPresetNameDraft[preset.key]"
                  :data-testid="`occupancy-preset-name-${preset.key}`"
                  type="text"
                  class="preset-name-field__input"
                />
              </label>
            </div>
            <div class="preset-name-panel__actions">
              <ElButton
                data-testid="occupancy-preset-name-apply"
                size="small"
                type="primary"
                plain
                @click="applyPresetNames"
              >
                应用命名
              </ElButton>
            </div>
          </div>
          <div
            v-if="presetCopyOpen"
            class="preset-copy-panel"
            data-testid="occupancy-preset-copy-panel"
          >
            <div class="preset-copy-panel__title">
              {{ presetCopyMode === 'edit' ? '编辑自定义预设' : '复制新预设' }}
            </div>
            <div class="preset-copy-panel__desc">
              {{
                presetCopyMode === 'edit'
                  ? '基于当前已勾选字段覆盖这条自定义预设，同时可以直接修改名称。'
                  : '从系统预设、已保存的自定义预设或当前字段选择快速复制一套新的导出口径。'
              }}
            </div>
            <div class="preset-copy-panel__sources">
              <button
                type="button"
                class="export-preset-chip export-preset-chip--subtle"
                :class="presetCopySourceKey === 'current' ? 'export-preset-chip--active' : ''"
                data-testid="occupancy-preset-copy-source-current"
                @click="presetCopySourceKey = 'current'"
              >
                当前字段
              </button>
              <button
                v-for="(preset, presetIndex) in computedExportPresetOptions"
                :key="`copy-${preset.key}`"
                type="button"
                class="export-preset-chip export-preset-chip--subtle"
                :class="presetCopySourceKey === preset.key ? 'export-preset-chip--active' : ''"
                :data-testid="getPresetCopySourceTestId(preset, presetIndex)"
                @click="presetCopySourceKey = preset.key"
              >
                {{ preset.label }}
              </button>
            </div>
            <div class="preset-copy-panel__form">
              <input
                v-model="presetCopyName"
                data-testid="occupancy-preset-copy-name"
                type="text"
                class="preset-name-field__input"
                placeholder="请输入新预设名称"
              />
              <ElButton
                :data-testid="
                  presetCopyMode === 'edit'
                    ? 'occupancy-preset-copy-save'
                    : 'occupancy-preset-copy-apply'
                "
                size="small"
                type="primary"
                plain
                @click="presetCopyMode === 'edit' ? saveCustomPreset() : createCustomPreset()"
              >
                {{ presetCopyMode === 'edit' ? '保存预设' : '复制创建' }}
              </ElButton>
            </div>
          </div>
          <div class="export-config-panel__presets">
            <button
              v-for="(preset, presetIndex) in computedExportPresetOptions"
              :key="preset.key"
              type="button"
              class="export-preset-chip"
              :data-testid="getExportPresetTestId(preset, presetIndex)"
              @click="applyExportPreset(preset.key)"
            >
              {{ preset.label }}
            </button>
          </div>
          <div
            v-if="customExportPresets.length"
            class="custom-preset-list"
            data-testid="occupancy-custom-preset-list"
          >
            <div
              v-for="(preset, presetIndex) in customExportPresets"
              :key="preset.key"
              class="custom-preset-item"
            >
              <div class="custom-preset-item__meta">
                <div class="custom-preset-item__title">{{ preset.label }}</div>
                <div class="custom-preset-item__desc">
                  当前保存 {{ preset.fields.length }} 个字段，可继续覆盖字段并编辑名称。
                </div>
              </div>
              <div class="custom-preset-item__actions">
                <ElButton
                  :data-testid="`occupancy-custom-preset-edit-${presetIndex}`"
                  size="small"
                  text
                  @click="startEditCustomPreset(preset)"
                >
                  编辑
                </ElButton>
                <ElButton
                  :data-testid="`occupancy-custom-preset-delete-${presetIndex}`"
                  size="small"
                  text
                  type="danger"
                  @click="removeCustomPreset(preset.key)"
                >
                  删除
                </ElButton>
              </div>
            </div>
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
            class="annotation-template-toolbar"
            data-testid="occupancy-annotation-template-toolbar"
          >
            <div class="annotation-template-toolbar__label">批注模板</div>
            <div class="annotation-template-toolbar__items">
              <button
                v-for="template in annotationTemplateOptions"
                :key="template.key"
                type="button"
                class="export-preset-chip export-preset-chip--subtle"
                :class="annotationTemplate === template.key ? 'export-preset-chip--active' : ''"
                :data-testid="`occupancy-annotation-template-${template.key}`"
                @click="annotationTemplate = template.key"
              >
                {{ template.label }}
              </button>
            </div>
          </div>
          <div
            v-if="groupViewMode === 'ANNOTATION'"
            class="annotation-list"
            data-testid="occupancy-annotation-list"
          >
            <div
              v-if="annotationPreviewRecord"
              class="annotation-preview"
              data-testid="occupancy-annotation-preview"
            >
              <div class="annotation-preview__header">
                <div class="annotation-preview__title">模板预览</div>
                <ElTag effect="light">{{ annotationTemplateLabel }}</ElTag>
              </div>
              <div class="annotation-preview__grid">
                <div
                  v-for="item in buildAnnotationPreviewItems(annotationPreviewRecord)"
                  :key="item.label"
                  class="annotation-note annotation-note--preview"
                >
                  <div class="annotation-note__label">{{ item.label }}</div>
                  <div class="annotation-note__value">{{ item.value }}</div>
                </div>
              </div>
            </div>
            <div
              v-if="annotationPreviewRecord"
              class="annotation-compare"
              data-testid="occupancy-annotation-compare"
            >
              <div class="annotation-compare__header">
                <div class="annotation-preview__title">模板差异对比</div>
                <div class="annotation-compare__switches">
                  <button
                    v-for="template in annotationCompareTargetOptions"
                    :key="template.key"
                    type="button"
                    class="export-preset-chip export-preset-chip--subtle"
                    :class="
                      annotationCompareTarget === template.key ? 'export-preset-chip--active' : ''
                    "
                    :data-testid="`occupancy-annotation-compare-target-${template.key}`"
                    @click="annotationCompareTarget = template.key"
                  >
                    {{ template.label }}
                  </button>
                </div>
              </div>
              <div class="annotation-compare__desc">
                当前模板：{{ annotationTemplateLabel }}，对比模板：{{ annotationCompareTargetLabel }}
              </div>
              <div class="annotation-compare__grid">
                <div
                  v-for="item in annotationCompareItems"
                  :key="item.key"
                  class="annotation-compare-item"
                  :class="
                    item.changed ? 'annotation-compare-item--changed' : 'annotation-compare-item--stable'
                  "
                  :data-testid="`occupancy-annotation-compare-item-${item.key}`"
                >
                  <div class="annotation-compare-item__header">
                    <div class="annotation-note__label">{{ item.label }}</div>
                    <ElTag :type="item.changed ? 'warning' : 'success'" effect="light" size="small">
                      {{ item.changed ? '有差异' : '无差异' }}
                    </ElTag>
                  </div>
                  <div class="annotation-compare-item__row">
                    <span>{{ annotationTemplateLabel }}</span>
                    <strong>{{ item.currentValue }}</strong>
                  </div>
                  <div class="annotation-compare-item__row">
                    <span>{{ annotationCompareTargetLabel }}</span>
                    <strong>{{ item.compareValue }}</strong>
                  </div>
                </div>
              </div>
            </div>
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
                  <div class="annotation-note__value">{{ buildAnnotationStatusNote(record) }}</div>
                </div>
                <div class="annotation-note">
                  <div class="annotation-note__label">占用批注</div>
                  <div class="annotation-note__value">{{ buildAnnotationChangeNote(record) }}</div>
                </div>
                <div class="annotation-note">
                  <div class="annotation-note__label">释放批注</div>
                  <div class="annotation-note__value">{{ buildAnnotationReleaseNote(record) }}</div>
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
  import { ElMessageBox } from 'element-plus'

  type TimeFilter = 'ALL' | '7D' | '30D' | '90D' | 'CUSTOM'
  type SortDirection = 'DESC' | 'ASC'
  type CompareFieldKey = 'useDeptName' | 'responsibleUserName' | 'locationName'
  type StatusFilter = 'ALL' | 'ACTIVE' | 'RELEASED'
  type GroupViewMode = 'LIST' | 'GROUPED' | 'ANNOTATION'
  type LinkedTabName = 'overview' | 'inspection' | 'rectification' | 'disposal'
  type LinkStatsWindow = '7D' | '30D'
  type ImportConflictPolicy = 'SKIP' | 'RENAME' | 'OVERWRITE'
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

  interface CustomExportPresetOption {
    key: string
    label: string
    fields: ExportFieldKey[]
    source: 'custom'
  }

  type ExportPresetViewOption = (ExportPresetOption & { source: 'system' }) | CustomExportPresetOption
  type PresetCopySourceKey = 'current' | ExportPresetViewOption['key']
  type AnnotationTemplateKey = 'standard' | 'manager' | 'audit'
  type AnnotationCompareItemKey = 'status' | 'change' | 'release'
  type ImportConflictType = 'none' | 'system' | 'custom'
  type ItemImportConflictPolicy = ImportConflictPolicy | 'DEFAULT'
  type LinkStatsResetScope = 'EVENTS' | 'COUNTS' | 'ALL'

  interface OccupancyLinkStatEvent {
    targetKey: LinkedTabName
    targetLabel: string
    occurredAt: string
  }

  interface OccupancyLinkStatsState {
    counts: Record<LinkedTabName, number>
    lastTargetKey: LinkedTabName | ''
    lastTargetLabel: string
    events: OccupancyLinkStatEvent[]
  }

  interface PresetImportPreviewItem {
    key: string
    label: string
    fields: ExportFieldKey[]
    invalidFields: string[]
    conflictType: ImportConflictType
    resolvedLabel: string
    policyOverride: ItemImportConflictPolicy
  }

  interface PresetImportInvalidItem {
    key: string
    label: string
    reason: string
    invalidFields: string[]
  }

  interface PresetImportResultItem {
    label: string
    conflictType: ImportConflictType
    effectivePolicy: ImportConflictPolicy
    resolvedLabel: string
  }

  interface PresetImportResultState {
    executedAt: string
    globalPolicy: ImportConflictPolicy
    importableCount: number
    invalidCount: number
    skippedCount: number
    renamedCount: number
    overwrittenCount: number
    appliedCount: number
    items: PresetImportResultItem[]
  }

  interface LinkTrendItem {
    date: string
    label: string
    count: number
    topLabel: string
    barHeight: number
  }

  interface TrendDrilldownState {
    date: string
    label: string
    count: number
  }

  interface TrendFilterSnapshot {
    statusFilter: StatusFilter
    timeFilter: TimeFilter
    sortDirection: SortDirection
    keyword: string
    customRangeDraftStart: string
    customRangeDraftEnd: string
    customRangeAppliedStart: string
    customRangeAppliedEnd: string
  }

  interface SavedTrendSnapshotState {
    key: string
    name: string
    savedAt: string
    linkStatsWindow: LinkStatsWindow
    drilldown: TrendDrilldownState | null
    filterState: TrendFilterSnapshot
  }

  interface ImportPolicyTemplateItem {
    label: string
    conflictType: ImportConflictType
    effectivePolicy: ImportConflictPolicy
  }

  interface ImportPolicyTemplateState {
    key: string
    name: string
    createdAt: string
    globalPolicy: ImportConflictPolicy
    itemPolicies: ImportPolicyTemplateItem[]
    lastAppliedAt?: string
    lastAppliedSummary?: string
    lastMatchedCount?: number
  }

  interface LinkResetLogState {
    key: string
    executedAt: string
    scope: LinkStatsResetScope
    summary: string
  }

  type GovernanceActivityType = 'TEMPLATE' | 'SNAPSHOT' | 'RESET' | 'EXPORT'

  interface GovernanceActivityState {
    key: string
    type: GovernanceActivityType
    label: string
    target: string
    summary: string
    executedAt: string
  }

  interface GovernanceExportMetaState {
    exportedAt: string
    fileName: string
  }

  type ResetLogFilterScope = 'ALL_RECORDS' | LinkStatsResetScope

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
  const annotationTemplate = ref<AnnotationTemplateKey>('standard')
  const exportConfigOpen = ref(false)
  const governanceOpen = ref(false)
  const presetNameEditOpen = ref(false)
  const presetCopyOpen = ref(false)
  const presetImportOpen = ref(false)
  const presetCopyMode = ref<'create' | 'edit'>('create')
  const editingCustomPresetKey = ref('')
  const presetCopyName = ref('')
  const presetImportText = ref('')
  const presetImportPolicy = ref<ImportConflictPolicy>('RENAME')
  const policyTemplateName = ref('')
  const trendSnapshotName = ref('')
  const selectedPolicyTemplateKey = ref('')
  const selectedSnapshotCompareLeftKey = ref('')
  const selectedSnapshotCompareRightKey = ref('')
  const resetLogFilterScope = ref<ResetLogFilterScope>('ALL_RECORDS')
  const resetLogKeyword = ref('')
  const governanceActivityFilter = ref<'ALL' | GovernanceActivityType>('ALL')
  const governanceActivityKeyword = ref('')
  const presetCopySourceKey = ref<PresetCopySourceKey>('current')
  const focusedRecordKey = ref('')
  const filtersReady = ref(false)
  const keyword = ref('')
  const customExportPresets = ref<CustomExportPresetOption[]>([])
  const presetImportPreviewItems = ref<PresetImportPreviewItem[]>([])
  const presetImportInvalidItems = ref<PresetImportInvalidItem[]>([])
  const lastPresetImportResult = ref<PresetImportResultState | null>(null)
  const importPolicyTemplates = ref<ImportPolicyTemplateState[]>([])
  const governanceActivities = ref<GovernanceActivityState[]>([])
  const governanceExportMeta = ref<GovernanceExportMetaState | null>(null)
  const annotationCompareTarget = ref<AnnotationTemplateKey>('manager')
  const linkStatsWindow = ref<LinkStatsWindow>('7D')
  const linkStatsResetScope = ref<LinkStatsResetScope>('ALL')
  const trendDrilldown = ref<TrendDrilldownState | null>(null)
  const trendFilterSnapshot = ref<TrendFilterSnapshot | null>(null)
  const savedTrendSnapshot = ref<SavedTrendSnapshotState | null>(null)
  const savedTrendSnapshotHistory = ref<SavedTrendSnapshotState[]>([])
  const linkResetLogs = ref<LinkResetLogState[]>([])
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
  const annotationTemplateOptions: Array<{ key: AnnotationTemplateKey; label: string }> = [
    { key: 'standard', label: '标准模板' },
    { key: 'manager', label: '管理视角' },
    { key: 'audit', label: '审计视角' }
  ]
  const importConflictPolicyOptions: Array<{ key: ImportConflictPolicy; label: string }> = [
    { key: 'SKIP', label: '跳过同名' },
    { key: 'RENAME', label: '自动改名' },
    { key: 'OVERWRITE', label: '覆盖同名' }
  ]
  const exportPresetNameDraft = reactive<Record<ExportPresetOption['key'], string>>({
    operations: '运营摘要',
    audit: '审计复盘',
    release: '释放分析'
  })
  const defaultExportFieldKeys: ExportFieldKey[] = exportFieldOptions.map((item) => item.key)
  const tabLinkOptions: { key: LinkedTabName; label: string }[] = [
    { key: 'overview', label: '回总览核对主档' },
    { key: 'inspection', label: '看巡检联动' },
    { key: 'rectification', label: '看整改进展' },
    { key: 'disposal', label: '看处置关联' }
  ]
  const linkStatsWindowOptions: Array<{ key: LinkStatsWindow; label: string }> = [
    { key: '7D', label: '近 7 天' },
    { key: '30D', label: '近 30 天' }
  ]
  const linkStatsResetScopeOptions: Array<{ key: LinkStatsResetScope; label: string }> = [
    { key: 'EVENTS', label: '只重置趋势' },
    { key: 'COUNTS', label: '只重置来源' },
    { key: 'ALL', label: '全部重置' }
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
  const exportPresetNameStorageKey = 'asset-real-estate-occupancy-export-preset-names'
  const customPresetStorageKey = 'asset-real-estate-occupancy-export-custom-presets'
  const linkStatsStorageKey = computed(() => {
    const assetKey = String(props.detailData.assetCode || props.detailData.assetId || '').trim()
    return assetKey ? `asset-real-estate-occupancy-link-stats:${assetKey}` : ''
  })
  const presetImportResultStorageKey = computed(() => {
    const assetKey = String(props.detailData.assetCode || props.detailData.assetId || '').trim()
    return assetKey ? `asset-real-estate-occupancy-import-result:${assetKey}` : ''
  })
  const trendSnapshotStorageKey = computed(() => {
    const assetKey = String(props.detailData.assetCode || props.detailData.assetId || '').trim()
    return assetKey ? `asset-real-estate-occupancy-trend-snapshot:${assetKey}` : ''
  })
  const importPolicyTemplateStorageKey = computed(() => {
    const assetKey = String(props.detailData.assetCode || props.detailData.assetId || '').trim()
    return assetKey ? `asset-real-estate-occupancy-import-policy-templates:${assetKey}` : ''
  })
  const linkResetLogStorageKey = computed(() => {
    const assetKey = String(props.detailData.assetCode || props.detailData.assetId || '').trim()
    return assetKey ? `asset-real-estate-occupancy-reset-logs:${assetKey}` : ''
  })
  const governanceActivitiesStorageKey = computed(() => {
    const assetKey = String(props.detailData.assetCode || props.detailData.assetId || '').trim()
    return assetKey ? `asset-real-estate-occupancy-governance-activities:${assetKey}` : ''
  })
  const governanceExportMetaStorageKey = computed(() => {
    const assetKey = String(props.detailData.assetCode || props.detailData.assetId || '').trim()
    return assetKey ? `asset-real-estate-occupancy-governance-export-meta:${assetKey}` : ''
  })

  const computedExportPresetOptions = computed(() => {
    const systemPresets: ExportPresetViewOption[] = exportPresetOptions.map((item) => ({
      ...item,
      label: exportPresetNameDraft[item.key] || item.label,
      source: 'system'
    }))
    return [...systemPresets, ...customExportPresets.value]
  })

  const annotationPreviewRecord = computed(() => {
    return activeRecord.value || sortedRecords.value[0]
  })

  const annotationTemplateLabel = computed(() => {
    return annotationTemplateOptions.find((item) => item.key === annotationTemplate.value)?.label || '标准模板'
  })

  const annotationCompareTargetOptions = computed(() => {
    return annotationTemplateOptions.filter((item) => item.key !== annotationTemplate.value)
  })

  const annotationCompareTargetLabel = computed(() => {
    return (
      annotationTemplateOptions.find((item) => item.key === annotationCompareTarget.value)?.label ||
      '对比模板'
    )
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
        {
          overview: 0,
          inspection: 0,
          rectification: 0,
          disposal: 0
        }
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

  const linkTrendTitle = computed(() => {
    return linkStatsWindow.value === '30D' ? '近 30 天联动趋势' : '近 7 天联动趋势'
  })

  const presetImportSummary = computed(() => {
    const previewItems = presetImportPreviewItems.value
    const conflictCount = previewItems.filter((item) => item.conflictType !== 'none').length
    return {
      importableCount: previewItems.length,
      conflictCount,
      invalidCount: presetImportInvalidItems.value.length
    }
  })

  const lastPresetImportResultSummary = computed(() => {
    if (!lastPresetImportResult.value) {
      return ''
    }
    return [
      `导入 ${lastPresetImportResult.value.importableCount}`,
      `无效 ${lastPresetImportResult.value.invalidCount}`,
      `跳过 ${lastPresetImportResult.value.skippedCount}`,
      `改名 ${lastPresetImportResult.value.renamedCount}`,
      `覆盖 ${lastPresetImportResult.value.overwrittenCount}`
    ].join(' / ')
  })

  const canReuseLastPolicies = computed(() => {
    if (!lastPresetImportResult.value || !presetImportPreviewItems.value.length) {
      return false
    }
    return presetImportPreviewItems.value.some((item) =>
      lastPresetImportResult.value?.items.some(
        (resultItem) => resultItem.label === item.label && resultItem.conflictType === item.conflictType
      )
    )
  })

  const visibleSavedTrendSnapshots = computed(() => savedTrendSnapshotHistory.value.slice(0, 5))

  const selectedPolicyTemplate = computed(() => {
    if (!importPolicyTemplates.value.length) {
      return null
    }
    return (
      importPolicyTemplates.value.find((item) => item.key === selectedPolicyTemplateKey.value) ||
      importPolicyTemplates.value[0]
    )
  })

  const selectedSnapshotCompareLeft = computed(() => {
    return visibleSavedTrendSnapshots.value.find(
      (item) => item.key === selectedSnapshotCompareLeftKey.value
    )
  })

  const selectedSnapshotCompareRight = computed(() => {
    return visibleSavedTrendSnapshots.value.find(
      (item) => item.key === selectedSnapshotCompareRightKey.value
    )
  })

  const filteredLinkResetLogs = computed(() => {
    return linkResetLogs.value.filter((item) => {
      if (resetLogFilterScope.value !== 'ALL_RECORDS' && item.scope !== resetLogFilterScope.value) {
        return false
      }
      if (!resetLogKeyword.value.trim()) {
        return true
      }
      const keywordValue = resetLogKeyword.value.trim().toLowerCase()
      return (
        buildResetScopeLabel(item.scope).toLowerCase().includes(keywordValue) ||
        item.summary.toLowerCase().includes(keywordValue) ||
        item.executedAt.toLowerCase().includes(keywordValue)
      )
    })
  })

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

  const latestGovernanceActivity = computed(() => governanceActivities.value[0] || null)

  const historyDrilldownTip = computed(() => {
    if (!trendDrilldown.value) {
      return ''
    }
    return `当前来自趋势钻取：${trendDrilldown.value.date}`
  })

  const snapshotCompareItems = computed(() => {
    const leftSnapshot = selectedSnapshotCompareLeft.value
    const rightSnapshot = selectedSnapshotCompareRight.value
    if (!leftSnapshot || !rightSnapshot || leftSnapshot.key === rightSnapshot.key) {
      return []
    }

    const resolveWindowLabel = (value: LinkStatsWindow) =>
      value === '30D' ? '近 30 天' : '近 7 天'
    const resolveSortLabel = (value: SortDirection) =>
      value === 'ASC' ? '最早在前' : '最新在前'
    const resolveTimeLabel = (state: TrendFilterSnapshot) => {
      if (state.timeFilter === 'CUSTOM') {
        return `${state.customRangeAppliedStart || '-'} ~ ${state.customRangeAppliedEnd || '-'}`
      }
      if (state.timeFilter === '90D') {
        return '近 90 天'
      }
      if (state.timeFilter === '30D') {
        return '近 30 天'
      }
      if (state.timeFilter === '7D') {
        return '近 7 天'
      }
      return '全部时间'
    }

    const compareItems = [
      {
        key: 'window',
        label: '时间窗',
        leftValue: resolveWindowLabel(leftSnapshot.linkStatsWindow),
        rightValue: resolveWindowLabel(rightSnapshot.linkStatsWindow)
      },
      {
        key: 'drilldown',
        label: '钻取日期',
        leftValue: leftSnapshot.drilldown?.date || '-',
        rightValue: rightSnapshot.drilldown?.date || '-'
      },
      {
        key: 'status',
        label: '状态筛选',
        leftValue: leftSnapshot.filterState.statusFilter,
        rightValue: rightSnapshot.filterState.statusFilter
      },
      {
        key: 'time',
        label: '时间筛选',
        leftValue: resolveTimeLabel(leftSnapshot.filterState),
        rightValue: resolveTimeLabel(rightSnapshot.filterState)
      },
      {
        key: 'keyword',
        label: '关键字',
        leftValue: leftSnapshot.filterState.keyword || '-',
        rightValue: rightSnapshot.filterState.keyword || '-'
      },
      {
        key: 'sort',
        label: '排序方式',
        leftValue: resolveSortLabel(leftSnapshot.filterState.sortDirection),
        rightValue: resolveSortLabel(rightSnapshot.filterState.sortDirection)
      }
    ]

    return compareItems.map((item) => ({
      ...item,
      changed: item.leftValue !== item.rightValue
    }))
  })

  const snapshotCompareSummary = computed(() => {
    if (!snapshotCompareItems.value.length) {
      return ''
    }
    const changedCount = snapshotCompareItems.value.filter((item) => item.changed).length
    return changedCount
      ? `核心变化摘要：共 ${changedCount} 项存在差异`
      : '核心变化摘要：两个快照当前没有差异'
  })

  const annotationCompareItems = computed(() => {
    const record = annotationPreviewRecord.value
    if (!record) {
      return []
    }

    const items: Array<{ key: AnnotationCompareItemKey; label: string; currentValue: string; compareValue: string; changed: boolean }> = [
      {
        key: 'status',
        label: '状态说明',
        currentValue: buildAnnotationStatusNote(record, annotationTemplate.value),
        compareValue: buildAnnotationStatusNote(record, annotationCompareTarget.value),
        changed:
          buildAnnotationStatusNote(record, annotationTemplate.value) !==
          buildAnnotationStatusNote(record, annotationCompareTarget.value)
      },
      {
        key: 'change',
        label: '占用批注',
        currentValue: buildAnnotationChangeNote(record, annotationTemplate.value),
        compareValue: buildAnnotationChangeNote(record, annotationCompareTarget.value),
        changed:
          buildAnnotationChangeNote(record, annotationTemplate.value) !==
          buildAnnotationChangeNote(record, annotationCompareTarget.value)
      },
      {
        key: 'release',
        label: '释放批注',
        currentValue: buildAnnotationReleaseNote(record, annotationTemplate.value),
        compareValue: buildAnnotationReleaseNote(record, annotationCompareTarget.value),
        changed:
          buildAnnotationReleaseNote(record, annotationTemplate.value) !==
          buildAnnotationReleaseNote(record, annotationCompareTarget.value)
      }
    ]

    return items
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

      const topEntry =
        Object.entries(targetCounter).sort((left, right) => right[1] - left[1])[0] || undefined

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

  const parseDateValue = (value?: string) => {
    if (!value) {
      return undefined
    }
    const normalized = /^\d{4}-\d{2}-\d{2}$/.test(value) ? `${value}T00:00:00` : value
    const parsed = new Date(normalized)
    return Number.isNaN(parsed.getTime()) ? undefined : parsed
  }

  // 趋势图按浏览器本地自然日分桶，避免 UTC 字符串把当日联动偏移到前一天。
  const formatLocalDateKey = (date: Date) => {
    const year = date.getFullYear()
    const month = `${date.getMonth() + 1}`.padStart(2, '0')
    const day = `${date.getDate()}`.padStart(2, '0')
    return `${year}-${month}-${day}`
  }

  const formatLocalDateTime = (date: Date) => {
    const dateKey = formatLocalDateKey(date)
    const hours = `${date.getHours()}`.padStart(2, '0')
    const minutes = `${date.getMinutes()}`.padStart(2, '0')
    const seconds = `${date.getSeconds()}`.padStart(2, '0')
    return `${dateKey} ${hours}:${minutes}:${seconds}`
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

  const getExportPresetTestId = (preset: ExportPresetViewOption, presetIndex: number) => {
    if (preset.source === 'custom') {
      return `occupancy-export-preset-custom-${presetIndex - exportPresetOptions.length}`
    }
    return `occupancy-export-preset-${preset.key}`
  }

  const getPresetCopySourceTestId = (preset: ExportPresetViewOption, presetIndex: number) => {
    if (preset.source === 'custom') {
      return `occupancy-preset-copy-source-custom-${presetIndex - exportPresetOptions.length}`
    }
    return `occupancy-preset-copy-source-${preset.key}`
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

  const buildAnnotationStatusNote = (
    record: AssetRealEstateOccupancyRecord,
    templateKey: AnnotationTemplateKey = annotationTemplate.value
  ) => {
    const isActive = String(record.occupancyStatus || '').toUpperCase() === 'ACTIVE'
    if (templateKey === 'manager') {
      return isActive
        ? '管理视角：当前占用仍在持续，请优先核对责任归属和主档同步状态。'
        : '管理视角：该条轨迹已经释放，可作为本次占用结束与重新分配的依据。'
    }
    if (templateKey === 'audit') {
      return isActive
        ? '审计视角：当前轨迹仍为有效占用，应作为最近一次占用依据。'
        : '审计视角：该轨迹已释放，应作为历史留痕和释放凭据记录。'
    }
    return isActive
      ? '该轨迹仍是当前有效占用，主档应以这条占用记录为准。'
      : '该轨迹已经释放，仅保留为历史留痕，不再承接变更或释放动作。'
  }

  const buildAnnotationChangeNote = (
    record: AssetRealEstateOccupancyRecord,
    templateKey: AnnotationTemplateKey = annotationTemplate.value
  ) => {
    const reason = record.changeReason || '-'
    if (templateKey === 'manager') {
      return `管理视角：占用依据 ${reason}`
    }
    if (templateKey === 'audit') {
      return `审计视角：占用凭据 ${reason}`
    }
    return reason
  }

  const buildAnnotationReleaseNote = (
    record: AssetRealEstateOccupancyRecord,
    templateKey: AnnotationTemplateKey = annotationTemplate.value
  ) => {
    const reason = record.releaseReason || '-'
    if (templateKey === 'manager') {
      return `管理视角：释放结论 ${reason}`
    }
    if (templateKey === 'audit') {
      return `审计视角：释放凭据 ${reason}`
    }
    return reason
  }

  const buildAnnotationPreviewItems = (record?: AssetRealEstateOccupancyRecord) => {
    if (!record) {
      return []
    }
    return [
      { label: '状态说明样例', value: buildAnnotationStatusNote(record) },
      { label: '占用批注样例', value: buildAnnotationChangeNote(record) },
      { label: '释放批注样例', value: buildAnnotationReleaseNote(record) }
    ]
  }

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

  const persistLastPresetImportResult = () => {
    if (!presetImportResultStorageKey.value) {
      return
    }
    if (!lastPresetImportResult.value) {
      window.localStorage.removeItem(presetImportResultStorageKey.value)
      return
    }
    window.localStorage.setItem(
      presetImportResultStorageKey.value,
      JSON.stringify(lastPresetImportResult.value)
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

  const persistImportPolicyTemplates = () => {
    if (!importPolicyTemplateStorageKey.value) {
      return
    }
    if (!importPolicyTemplates.value.length) {
      window.localStorage.removeItem(importPolicyTemplateStorageKey.value)
      return
    }
    window.localStorage.setItem(
      importPolicyTemplateStorageKey.value,
      JSON.stringify(importPolicyTemplates.value)
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
    window.localStorage.setItem(
      governanceActivitiesStorageKey.value,
      JSON.stringify(governanceActivities.value)
    )
  }

  const persistGovernanceExportMeta = () => {
    if (!governanceExportMetaStorageKey.value) {
      return
    }
    if (!governanceExportMeta.value) {
      window.localStorage.removeItem(governanceExportMetaStorageKey.value)
      return
    }
    window.localStorage.setItem(
      governanceExportMetaStorageKey.value,
      JSON.stringify(governanceExportMeta.value)
    )
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
    emit('switch-tab', tab)
  }

  const setLinkStatsWindow = (window: LinkStatsWindow) => {
    linkStatsWindow.value = window
    if (trendDrilldown.value) {
      clearTrendDrilldown()
    }
  }

  const buildTrendFilterSnapshot = (): TrendFilterSnapshot => {
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

  const applyTrendDrilldownFilters = (date: string) => {
    if (!trendFilterSnapshot.value) {
      trendFilterSnapshot.value = buildTrendFilterSnapshot()
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
      drilldown: trendDrilldown.value
        ? {
            ...trendDrilldown.value
          }
        : null,
      filterState: buildTrendFilterSnapshot()
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
    governanceOpen.value = !governanceOpen.value
    if (governanceOpen.value) {
      exportConfigOpen.value = true
    }
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
    trendDrilldown.value = targetSnapshot.drilldown
      ? {
          ...targetSnapshot.drilldown
        }
      : null
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
    savedTrendSnapshotHistory.value = savedTrendSnapshotHistory.value.filter(
      (item) => item.key !== resolvedSnapshotKey
    )
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
    linkResetLogs.value = [
      {
        key: `reset-${Date.now()}-${linkStatsResetScope.value.toLowerCase()}`,
        executedAt: new Date().toISOString(),
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
      new Date().toISOString()
    )
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

  const restorePresetNames = () => {
    const raw = window.localStorage.getItem(exportPresetNameStorageKey)
    if (!raw) {
      return
    }

    try {
      const parsed = JSON.parse(raw) as Partial<Record<ExportPresetOption['key'], string>>
      ;(['operations', 'audit', 'release'] as const).forEach((key) => {
        const nextLabel = String(parsed[key] || '').trim()
        if (nextLabel) {
          exportPresetNameDraft[key] = nextLabel
        }
      })
    } catch {
      window.localStorage.removeItem(exportPresetNameStorageKey)
    }
  }

  const restoreCustomPresets = () => {
    const raw = window.localStorage.getItem(customPresetStorageKey)
    if (!raw) {
      customExportPresets.value = []
      return
    }

    try {
      const parsed = JSON.parse(raw)
      customExportPresets.value = Array.isArray(parsed)
        ? parsed
            .map((item) => {
              const key = String(item?.key || '').trim()
              const label = String(item?.label || '').trim()
              const fields = Array.isArray(item?.fields)
                ? item.fields.filter((field): field is ExportFieldKey =>
                    exportFieldOptions.some((option) => option.key === field)
                  )
                : []

              if (!key || !label || !fields.length) {
                return undefined
              }

              return {
                key,
                label,
                fields,
                source: 'custom' as const
              }
            })
            .filter((item): item is CustomExportPresetOption => !!item)
        : []
    } catch {
      customExportPresets.value = []
      window.localStorage.removeItem(customPresetStorageKey)
    }
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
      linkStats.lastTargetKey = ['overview', 'inspection', 'rectification', 'disposal'].includes(
        String(parsed.lastTargetKey || '')
      )
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
              return {
                targetKey,
                targetLabel,
                occurredAt
              }
            })
            .filter((item): item is OccupancyLinkStatEvent => !!item)
        : []
    } catch {
      window.localStorage.removeItem(linkStatsStorageKey.value)
    }
  }

  const restoreLastPresetImportResult = () => {
    lastPresetImportResult.value = null
    if (!presetImportResultStorageKey.value) {
      return
    }

    const raw = window.localStorage.getItem(presetImportResultStorageKey.value)
    if (!raw) {
      return
    }

    try {
      const parsed = JSON.parse(raw) as Partial<PresetImportResultState>
      const items = Array.isArray(parsed.items)
        ? parsed.items
            .map((item) => {
              const label = String(item?.label || '').trim()
              const conflictType = String(item?.conflictType || 'none') as ImportConflictType
              const effectivePolicy = String(item?.effectivePolicy || 'RENAME') as ImportConflictPolicy
              const resolvedLabel = String(item?.resolvedLabel || '').trim()
              if (
                !label ||
                !['none', 'system', 'custom'].includes(conflictType) ||
                !['SKIP', 'RENAME', 'OVERWRITE'].includes(effectivePolicy) ||
                !resolvedLabel
              ) {
                return undefined
              }
              return {
                label,
                conflictType,
                effectivePolicy,
                resolvedLabel
              }
            })
            .filter((item): item is PresetImportResultItem => !!item)
        : []

      lastPresetImportResult.value = {
        executedAt: String(parsed.executedAt || ''),
        globalPolicy: ['SKIP', 'RENAME', 'OVERWRITE'].includes(String(parsed.globalPolicy || ''))
          ? (parsed.globalPolicy as ImportConflictPolicy)
          : 'RENAME',
        importableCount: Number(parsed.importableCount || 0),
        invalidCount: Number(parsed.invalidCount || 0),
        skippedCount: Number(parsed.skippedCount || 0),
        renamedCount: Number(parsed.renamedCount || 0),
        overwrittenCount: Number(parsed.overwrittenCount || 0),
        appliedCount: Number(parsed.appliedCount || 0),
        items
      }
    } catch {
      window.localStorage.removeItem(presetImportResultStorageKey.value)
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
      const parsed = JSON.parse(raw) as
        | Partial<SavedTrendSnapshotState>
        | { current?: Partial<SavedTrendSnapshotState>; items?: Partial<SavedTrendSnapshotState>[] }
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
          ? parsed.items
              .map((item) => normalizeSnapshot(item))
              .filter((item): item is SavedTrendSnapshotState => !!item)
          : []
        savedTrendSnapshot.value =
          normalizeSnapshot(parsed.current) || savedTrendSnapshotHistory.value[0] || null
        return
      }

      const legacySnapshot = normalizeSnapshot(parsed as Partial<SavedTrendSnapshotState>)
      savedTrendSnapshot.value = legacySnapshot || null
      savedTrendSnapshotHistory.value = legacySnapshot ? [legacySnapshot] : []
    } catch {
      window.localStorage.removeItem(trendSnapshotStorageKey.value)
    }
  }

  const restoreImportPolicyTemplates = () => {
    importPolicyTemplates.value = []
    if (!importPolicyTemplateStorageKey.value) {
      return
    }
    const raw = window.localStorage.getItem(importPolicyTemplateStorageKey.value)
    if (!raw) {
      return
    }
    try {
      const parsed = JSON.parse(raw)
      importPolicyTemplates.value = Array.isArray(parsed)
        ? parsed
            .map((item) => {
              const name = String(item?.name || '').trim()
              const key = String(item?.key || '').trim()
              const createdAt = String(item?.createdAt || '').trim()
              const globalPolicy = String(item?.globalPolicy || '') as ImportConflictPolicy
              const lastAppliedAt = String(item?.lastAppliedAt || '').trim()
              const lastAppliedSummary = String(item?.lastAppliedSummary || '').trim()
              const lastMatchedCount = Number(item?.lastMatchedCount || 0)
              const itemPolicies = Array.isArray(item?.itemPolicies)
                ? item.itemPolicies
                    .map((policyItem) => {
                      const label = String(policyItem?.label || '').trim()
                      const conflictType = String(policyItem?.conflictType || '') as ImportConflictType
                      const effectivePolicy = String(policyItem?.effectivePolicy || '') as ImportConflictPolicy
                      if (
                        !label ||
                        !['none', 'system', 'custom'].includes(conflictType) ||
                        !['SKIP', 'RENAME', 'OVERWRITE'].includes(effectivePolicy)
                      ) {
                        return undefined
                      }
                      return {
                        label,
                        conflictType,
                        effectivePolicy
                      }
                    })
                    .filter((policyItem): policyItem is ImportPolicyTemplateItem => !!policyItem)
                : []
              if (!name || !key || !createdAt || !['SKIP', 'RENAME', 'OVERWRITE'].includes(globalPolicy)) {
                return undefined
              }
              return {
                key,
                name,
                createdAt,
                globalPolicy,
                itemPolicies,
                lastAppliedAt: lastAppliedAt || undefined,
                lastAppliedSummary: lastAppliedSummary || undefined,
                lastMatchedCount: Number.isFinite(lastMatchedCount) ? lastMatchedCount : undefined
              }
            })
            .filter((item): item is ImportPolicyTemplateState => !!item)
        : []
    } catch {
      window.localStorage.removeItem(importPolicyTemplateStorageKey.value)
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
              return {
                key,
                executedAt,
                scope,
                summary
              }
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
      governanceExportMeta.value = {
        exportedAt,
        fileName
      }
    } catch {
      window.localStorage.removeItem(governanceExportMetaStorageKey.value)
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

  const applyExportPreset = (presetKey: string) => {
    const preset = computedExportPresetOptions.value.find((item) => item.key === presetKey)
    if (!preset) {
      return
    }
    selectedExportFields.value = [...preset.fields]
  }

  const applyPresetNames = () => {
    window.localStorage.setItem(exportPresetNameStorageKey, JSON.stringify(exportPresetNameDraft))
    presetNameEditOpen.value = false
  }

  const buildCustomPresetKey = (seedIndex = 0) => {
    return `custom-${Date.now()}-${customExportPresets.value.length + seedIndex}`
  }

  const buildImportPolicyTemplateKey = () => {
    return `policy-template-${Date.now()}-${importPolicyTemplates.value.length}`
  }

  const openPolicyTemplateDetail = (templateKey: string) => {
    selectedPolicyTemplateKey.value = templateKey
  }

  const saveImportPolicyTemplate = () => {
    if (!presetImportPreviewItems.value.length) {
      return
    }
    const templateName = policyTemplateName.value.trim() || `策略模板 ${importPolicyTemplates.value.length + 1}`
    const nextTemplate: ImportPolicyTemplateState = {
      key: buildImportPolicyTemplateKey(),
      name: templateName,
      createdAt: new Date().toISOString(),
      globalPolicy: presetImportPolicy.value,
      itemPolicies: presetImportPreviewItems.value
        .filter((item) => item.conflictType !== 'none')
        .map((item) => ({
          label: item.label,
          conflictType: item.conflictType,
          effectivePolicy: getEffectiveImportPolicy(item)
        }))
    }
    importPolicyTemplates.value = [
      nextTemplate,
      ...importPolicyTemplates.value
    ].slice(0, 10)
    selectedPolicyTemplateKey.value = nextTemplate.key
    policyTemplateName.value = ''
    recordGovernanceActivity(
      'TEMPLATE',
      '保存策略模板',
      nextTemplate.name,
      `已保存策略模板，默认策略为${buildImportPolicyLabel(nextTemplate.globalPolicy)}`,
      nextTemplate.createdAt
    )
  }

  const buildPolicyTemplateApplySummary = (
    matchedCount: number,
    globalPolicy: ImportConflictPolicy
  ) => {
    if (!matchedCount) {
      return `本次未命中预览冲突项，保留全局策略 ${buildImportPolicyLabel(globalPolicy)}`
    }
    return `本次命中 ${matchedCount} 项冲突，按 ${buildImportPolicyLabel(globalPolicy)} 回填`
  }

  const applyImportPolicyTemplate = (templateKey: string) => {
    const targetTemplate = importPolicyTemplates.value.find((item) => item.key === templateKey)
    if (!targetTemplate) {
      return
    }
    presetImportPolicy.value = targetTemplate.globalPolicy
    let matchedCount = 0
    presetImportPreviewItems.value = presetImportPreviewItems.value.map((item) => {
      const matched = targetTemplate.itemPolicies.find(
        (policyItem) => policyItem.label === item.label && policyItem.conflictType === item.conflictType
      )
      if (!matched) {
        return {
          ...item,
          policyOverride: 'DEFAULT'
        }
      }
      matchedCount += 1
      if (item.conflictType === 'system' && matched.effectivePolicy === 'OVERWRITE') {
        return {
          ...item,
          policyOverride: 'RENAME'
        }
      }
      return {
        ...item,
        policyOverride: matched.effectivePolicy
      }
    })
    importPolicyTemplates.value = importPolicyTemplates.value.map((item) =>
      item.key === templateKey
        ? {
            ...item,
            lastAppliedAt: new Date().toISOString(),
            lastAppliedSummary: buildPolicyTemplateApplySummary(matchedCount, item.globalPolicy),
            lastMatchedCount: matchedCount
          }
        : item
    )
    selectedPolicyTemplateKey.value = templateKey
    recordGovernanceActivity(
      'TEMPLATE',
      '应用策略模板',
      targetTemplate.name,
      buildPolicyTemplateApplySummary(matchedCount, targetTemplate.globalPolicy),
      new Date().toISOString()
    )
  }

  const removeImportPolicyTemplate = (templateKey: string) => {
    const targetTemplate = importPolicyTemplates.value.find((item) => item.key === templateKey)
    importPolicyTemplates.value = importPolicyTemplates.value.filter((item) => item.key !== templateKey)
    if (selectedPolicyTemplateKey.value === templateKey) {
      selectedPolicyTemplateKey.value = importPolicyTemplates.value[0]?.key || ''
    }
    if (targetTemplate) {
      recordGovernanceActivity(
        'TEMPLATE',
        '删除策略模板',
        targetTemplate.name,
        `已删除${targetTemplate.name}`,
        new Date().toISOString()
      )
    }
  }

  const markSnapshotCompareSide = (side: 'left' | 'right', snapshotKey: string) => {
    if (side === 'left') {
      selectedSnapshotCompareLeftKey.value = snapshotKey
      return
    }
    selectedSnapshotCompareRightKey.value = snapshotKey
  }

  const buildImportConflictLabel = (conflictType: ImportConflictType) => {
    const mapper: Record<ImportConflictType, string> = {
      none: '可直接导入',
      system: '系统预设重名',
      custom: '自定义预设重名'
    }
    return mapper[conflictType]
  }

  const buildImportPolicyLabel = (policy: ImportConflictPolicy) => {
    const mapper: Record<ImportConflictPolicy, string> = {
      SKIP: '跳过',
      RENAME: '改名',
      OVERWRITE: '覆盖'
    }
    return mapper[policy]
  }

  const buildResetScopeLabel = (scope: LinkStatsResetScope) => {
    const mapper: Record<LinkStatsResetScope, string> = {
      EVENTS: '只重置趋势',
      COUNTS: '只重置来源',
      ALL: '全部重置'
    }
    return mapper[scope]
  }

  const buildGovernanceActivityTypeLabel = (type: GovernanceActivityType) => {
    const mapper: Record<GovernanceActivityType, string> = {
      TEMPLATE: '模板',
      SNAPSHOT: '快照',
      RESET: '重置',
      EXPORT: '导出'
    }
    return mapper[type]
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
    link.download = `${props.detailData.assetCode || 'asset'}-occupancy-reset-log-audit.json`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
  }

  const exportGovernanceAuditPackage = () => {
    const exportedAt = formatLocalDateTime(new Date())
    const fileName = `${props.detailData.assetCode || 'asset'}-occupancy-governance-audit.json`
    governanceExportMeta.value = {
      exportedAt,
      fileName
    }
    recordGovernanceActivity(
      'EXPORT',
      '导出治理审计包',
      '治理审计包',
      '已导出当前资产治理状态',
      exportedAt
    )

    const content = JSON.stringify(
      {
        version: 1,
        assetCode: props.detailData.assetCode || '',
        exportedAt,
        exportFieldConfig: {
          selectedExportFields: selectedExportFields.value,
          presetNames: { ...exportPresetNameDraft }
        },
        customExportPresets: customExportPresets.value,
        lastPresetImportResult: lastPresetImportResult.value,
        importPolicyTemplates: importPolicyTemplates.value,
        trendSnapshots: {
          current: savedTrendSnapshot.value,
          items: savedTrendSnapshotHistory.value
        },
        linkStats: {
          counts: { ...linkStats.counts },
          lastTargetKey: linkStats.lastTargetKey,
          lastTargetLabel: linkStats.lastTargetLabel,
          window: linkStatsWindow.value,
          events: [...linkStats.events]
        },
        linkResetLogs: linkResetLogs.value,
        governanceActivities: governanceActivities.value,
        governanceExportMeta: governanceExportMeta.value
      },
      null,
      2
    )

    const blob = new Blob([content], { type: 'application/json;charset=utf-8;' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
  }

  const resolveImportConflictType = (label: string): ImportConflictType => {
    const normalized = label.trim()
    const systemLabels = exportPresetOptions.map((item) => exportPresetNameDraft[item.key] || item.label)
    if (systemLabels.includes(normalized)) {
      return 'system'
    }
    if (customExportPresets.value.some((item) => item.label === normalized)) {
      return 'custom'
    }
    return 'none'
  }

  const buildImportedPresetLabel = (baseLabel: string, takenLabels: Set<string>) => {
    let nextLabel = `${baseLabel}（导入）`
    let index = 2
    while (takenLabels.has(nextLabel)) {
      nextLabel = `${baseLabel}（导入${index}）`
      index += 1
    }
    return nextLabel
  }

  const parsePresetImportPreviewItems = () => {
    const raw = presetImportText.value.trim()
    if (!raw) {
      return {
        previewItems: [] as PresetImportPreviewItem[],
        invalidItems: [] as PresetImportInvalidItem[]
      }
    }

    try {
      const parsed = JSON.parse(raw)
      const candidates = Array.isArray(parsed?.presets) ? parsed.presets : []
      const takenLabels = new Set(computedExportPresetOptions.value.map((item) => item.label))
      const invalidItems: PresetImportInvalidItem[] = []

      const previewItems = candidates
        .map((item, index) => {
          const label = String(item?.label || '').trim()
          const rawFields = Array.isArray(item?.fields) ? item.fields.map((field) => String(field)) : []
          const fields = rawFields.filter((field): field is ExportFieldKey =>
            exportFieldOptions.some((option) => option.key === field)
          )
          const invalidFields = rawFields.filter(
            (field) => !exportFieldOptions.some((option) => option.key === field)
          )

          if (!label) {
            invalidItems.push({
              key: `invalid-${index}`,
              label: '未命名预设',
              reason: '名称为空',
              invalidFields
            })
            return undefined
          }

          if (!fields.length) {
            invalidItems.push({
              key: `invalid-${index}`,
              label,
              reason: '无有效字段',
              invalidFields
            })
            return undefined
          }

          const conflictType = resolveImportConflictType(label)
          const resolvedLabel =
            conflictType === 'none' && !takenLabels.has(label)
              ? label
              : buildImportedPresetLabel(label, takenLabels)
          takenLabels.add(resolvedLabel)

          return {
            key: `preview-${index}`,
            label,
            fields,
            invalidFields,
            conflictType,
            resolvedLabel,
            policyOverride: 'DEFAULT'
          }
        })
        .filter((item): item is PresetImportPreviewItem => !!item)
      return {
        previewItems,
        invalidItems
      }
    } catch {
      return {
        previewItems: [] as PresetImportPreviewItem[],
        invalidItems: [
          {
            key: 'invalid-json',
            label: '导入内容',
            reason: 'JSON 解析失败',
            invalidFields: []
          }
        ]
      }
    }
  }

  const previewImportedPresets = () => {
    const { previewItems, invalidItems } = parsePresetImportPreviewItems()
    presetImportPreviewItems.value = previewItems
    presetImportInvalidItems.value = invalidItems
  }

  const setPreviewItemPolicy = (itemKey: string, policy: ItemImportConflictPolicy) => {
    presetImportPreviewItems.value = presetImportPreviewItems.value.map((item) =>
      item.key === itemKey ? { ...item, policyOverride: policy } : item
    )
  }

  const getEffectiveImportPolicy = (item: PresetImportPreviewItem) => {
    const candidate = item.policyOverride === 'DEFAULT' ? presetImportPolicy.value : item.policyOverride
    if (item.conflictType === 'system' && candidate === 'OVERWRITE') {
      return 'RENAME' as ImportConflictPolicy
    }
    return candidate as ImportConflictPolicy
  }

  const exportPresetImportReport = () => {
    if (!presetImportPreviewItems.value.length && !presetImportInvalidItems.value.length) {
      return
    }

    const content = JSON.stringify(
      {
        version: 1,
        summary: {
          importableCount: presetImportSummary.value.importableCount,
          conflictCount: presetImportSummary.value.conflictCount,
          invalidCount: presetImportSummary.value.invalidCount,
          globalPolicy: presetImportPolicy.value
        },
        previewItems: presetImportPreviewItems.value.map((item) => ({
          label: item.label,
          conflictType: item.conflictType,
          effectivePolicy: getEffectiveImportPolicy(item),
          resolvedLabel: item.resolvedLabel,
          invalidFields: item.invalidFields
        })),
        invalidItems: presetImportInvalidItems.value.map((item) => ({
          label: item.label,
          reason: item.reason,
          invalidFields: item.invalidFields
        }))
      },
      null,
      2
    )

    const blob = new Blob([content], { type: 'application/json;charset=utf-8;' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${props.detailData.assetCode || 'asset'}-occupancy-import-report.json`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
  }

  const reuseLastImportPolicies = () => {
    if (!lastPresetImportResult.value) {
      return
    }
    presetImportPreviewItems.value = presetImportPreviewItems.value.map((item) => {
      const matched = lastPresetImportResult.value?.items.find(
        (resultItem) => resultItem.label === item.label && resultItem.conflictType === item.conflictType
      )
      if (!matched) {
        return item
      }
      if (item.conflictType === 'system' && matched.effectivePolicy === 'OVERWRITE') {
        return {
          ...item,
          policyOverride: 'RENAME'
        }
      }
      return {
        ...item,
        policyOverride: matched.effectivePolicy
      }
    })
  }

  const resetPresetEditor = () => {
    presetCopyMode.value = 'create'
    editingCustomPresetKey.value = ''
    presetCopyName.value = ''
    presetCopySourceKey.value = 'current'
  }

  const resolvePresetCopySourceFields = () => {
    if (presetCopySourceKey.value === 'current') {
      return [...selectedExportFields.value]
    }
    const preset = computedExportPresetOptions.value.find((item) => item.key === presetCopySourceKey.value)
    return preset ? [...preset.fields] : []
  }

  const createCustomPreset = () => {
    const nextLabel = presetCopyName.value.trim()
    const nextFields = resolvePresetCopySourceFields()
    if (!nextLabel || !nextFields.length) {
      return
    }

    customExportPresets.value = [
      ...customExportPresets.value,
      {
        key: buildCustomPresetKey(),
        label: nextLabel,
        fields: nextFields,
        source: 'custom'
      }
    ]
    selectedExportFields.value = [...nextFields]
    presetCopyOpen.value = false
    resetPresetEditor()
  }

  const startEditCustomPreset = (preset: CustomExportPresetOption) => {
    presetCopyOpen.value = true
    presetCopyMode.value = 'edit'
    editingCustomPresetKey.value = preset.key
    presetCopyName.value = preset.label
    presetCopySourceKey.value = preset.key
  }

  const saveCustomPreset = () => {
    const nextLabel = presetCopyName.value.trim()
    if (!nextLabel || !editingCustomPresetKey.value) {
      return
    }

    customExportPresets.value = customExportPresets.value.map((preset) =>
      preset.key === editingCustomPresetKey.value
        ? {
            ...preset,
            label: nextLabel,
            fields: [...selectedExportFields.value]
          }
        : preset
    )
    presetCopyOpen.value = false
    resetPresetEditor()
  }

  const removeCustomPreset = (presetKey: string) => {
    customExportPresets.value = customExportPresets.value.filter((preset) => preset.key !== presetKey)
    if (editingCustomPresetKey.value === presetKey) {
      presetCopyOpen.value = false
      resetPresetEditor()
    }
  }

  const exportCustomPresets = () => {
    if (!customExportPresets.value.length) {
      return
    }

    const content = JSON.stringify(
      {
        version: 1,
        presets: customExportPresets.value.map((preset) => ({
          label: preset.label,
          fields: preset.fields
        }))
      },
      null,
      0
    )
    const blob = new Blob([content], { type: 'application/json;charset=utf-8;' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${props.detailData.assetCode || 'asset'}-occupancy-presets.json`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
  }

  const applyImportedPresets = () => {
    if (!presetImportPreviewItems.value.length) {
      return
    }

    const systemLabels = new Set(
      exportPresetOptions.map((item) => exportPresetNameDraft[item.key] || item.label)
    )
    let nextCustomPresets = [...customExportPresets.value]
    const takenLabels = new Set([
      ...systemLabels,
      ...nextCustomPresets.map((item) => item.label)
    ])
    const importResult: PresetImportResultState = {
      executedAt: new Date().toISOString(),
      globalPolicy: presetImportPolicy.value,
      importableCount: presetImportPreviewItems.value.length,
      invalidCount: presetImportInvalidItems.value.length,
      skippedCount: 0,
      renamedCount: 0,
      overwrittenCount: 0,
      appliedCount: 0,
      items: []
    }

    presetImportPreviewItems.value.forEach((item, index) => {
      const effectivePolicy = getEffectiveImportPolicy(item)
      let finalResolvedLabel = item.resolvedLabel

      if (item.conflictType === 'system') {
        if (effectivePolicy !== 'RENAME') {
          importResult.skippedCount += 1
          importResult.items.push({
            label: item.label,
            conflictType: item.conflictType,
            effectivePolicy,
            resolvedLabel: item.label
          })
          return
        }
        const renamedLabel = buildImportedPresetLabel(item.label, takenLabels)
        finalResolvedLabel = renamedLabel
        nextCustomPresets = [
          ...nextCustomPresets,
          {
            key: buildCustomPresetKey(index),
            label: renamedLabel,
            fields: [...item.fields],
            source: 'custom'
          }
        ]
        takenLabels.add(renamedLabel)
        importResult.renamedCount += 1
        importResult.appliedCount += 1
        importResult.items.push({
          label: item.label,
          conflictType: item.conflictType,
          effectivePolicy,
          resolvedLabel: finalResolvedLabel
        })
        return
      }

      if (item.conflictType === 'custom') {
        if (effectivePolicy === 'SKIP') {
          importResult.skippedCount += 1
          importResult.items.push({
            label: item.label,
            conflictType: item.conflictType,
            effectivePolicy,
            resolvedLabel: item.label
          })
          return
        }

        if (effectivePolicy === 'OVERWRITE') {
          nextCustomPresets = nextCustomPresets.map((preset) =>
            preset.label === item.label
              ? {
                  ...preset,
                  fields: [...item.fields]
                }
              : preset
          )
          importResult.overwrittenCount += 1
          importResult.appliedCount += 1
          importResult.items.push({
            label: item.label,
            conflictType: item.conflictType,
            effectivePolicy,
            resolvedLabel: item.label
          })
          return
        }

        const renamedLabel = buildImportedPresetLabel(item.label, takenLabels)
        finalResolvedLabel = renamedLabel
        nextCustomPresets = [
          ...nextCustomPresets,
          {
            key: buildCustomPresetKey(index),
            label: renamedLabel,
            fields: [...item.fields],
            source: 'custom'
          }
        ]
        takenLabels.add(renamedLabel)
        importResult.renamedCount += 1
        importResult.appliedCount += 1
        importResult.items.push({
          label: item.label,
          conflictType: item.conflictType,
          effectivePolicy,
          resolvedLabel: finalResolvedLabel
        })
        return
      }

      const nextLabel = takenLabels.has(item.label) ? buildImportedPresetLabel(item.label, takenLabels) : item.label
      finalResolvedLabel = nextLabel
      nextCustomPresets = [
        ...nextCustomPresets,
        {
          key: buildCustomPresetKey(index),
          label: nextLabel,
          fields: [...item.fields],
          source: 'custom'
        }
      ]
      takenLabels.add(nextLabel)
      if (nextLabel !== item.label) {
        importResult.renamedCount += 1
      }
      importResult.appliedCount += 1
      importResult.items.push({
        label: item.label,
        conflictType: item.conflictType,
        effectivePolicy,
        resolvedLabel: finalResolvedLabel
      })
    })

    customExportPresets.value = nextCustomPresets
    lastPresetImportResult.value = importResult
    presetImportPreviewItems.value = []
    presetImportInvalidItems.value = []
    presetImportText.value = ''
    presetImportOpen.value = false
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

  const exportAnnotationRecords = () => {
    if (!filteredRecords.value.length) {
      return
    }

    const header = ['占用单号', '轨迹状态', '状态说明', '占用批注', '释放批注']
    const rows = filteredRecords.value.map((record) => {
      return [
        record.occupancyNo || '',
        getStatusLabel(record.occupancyStatus),
        buildAnnotationStatusNote(record),
        buildAnnotationChangeNote(record),
        buildAnnotationReleaseNote(record)
      ]
        .map((item) => escapeCsvCell(item))
        .join(',')
    })
    const csvContent = `\uFEFF${header.map((item) => escapeCsvCell(item)).join(',')}\n${rows.join('\n')}`
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${props.detailData.assetCode || 'asset'}-occupancy-annotations.csv`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
  }

  watch(storageKey, () => {
    restorePersistedFilters()
  }, { immediate: true })

  watch(
    annotationTemplate,
    (value) => {
      if (annotationCompareTarget.value === value) {
        annotationCompareTarget.value =
          annotationTemplateOptions.find((item) => item.key !== value)?.key || 'manager'
      }
    },
    { immediate: true }
  )

  watch(
    presetImportText,
    () => {
      presetImportPreviewItems.value = []
      presetImportInvalidItems.value = []
    }
  )

  watch(
    linkStatsStorageKey,
    () => {
      restoreLinkStats()
    },
    { immediate: true }
  )

  watch(
    presetImportResultStorageKey,
    () => {
      restoreLastPresetImportResult()
    },
    { immediate: true }
  )

  watch(
    trendSnapshotStorageKey,
    () => {
      restoreSavedTrendSnapshot()
    },
    { immediate: true }
  )

  watch(
    importPolicyTemplateStorageKey,
    () => {
      restoreImportPolicyTemplates()
    },
    { immediate: true }
  )

  watch(
    linkResetLogStorageKey,
    () => {
      restoreLinkResetLogs()
    },
    { immediate: true }
  )

  watch(
    governanceActivitiesStorageKey,
    () => {
      restoreGovernanceActivities()
    },
    { immediate: true }
  )

  watch(
    governanceExportMetaStorageKey,
    () => {
      restoreGovernanceExportMeta()
    },
    { immediate: true }
  )

  onMounted(() => {
    restoreExportFields()
    restorePresetNames()
    restoreCustomPresets()
    restoreLinkStats()
    restoreLastPresetImportResult()
    restoreSavedTrendSnapshot()
    restoreImportPolicyTemplates()
    restoreLinkResetLogs()
    restoreGovernanceActivities()
    restoreGovernanceExportMeta()
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

  watch(
    customExportPresets,
    (value) => {
      window.localStorage.setItem(customPresetStorageKey, JSON.stringify(value))
    },
    { deep: true }
  )

  watch(
    () => ({
      counts: { ...linkStats.counts },
      lastTargetKey: linkStats.lastTargetKey,
      lastTargetLabel: linkStats.lastTargetLabel,
      events: [...linkStats.events]
    }),
    () => {
      persistLinkStats()
    },
    { deep: true }
  )

  watch(
    lastPresetImportResult,
    () => {
      persistLastPresetImportResult()
    },
    { deep: true }
  )

  watch(
    savedTrendSnapshot,
    () => {
      persistSavedTrendSnapshot()
    },
    { deep: true }
  )

  watch(
    savedTrendSnapshotHistory,
    () => {
      persistSavedTrendSnapshot()
    },
    { deep: true }
  )

  watch(
    importPolicyTemplates,
    () => {
      persistImportPolicyTemplates()
    },
    { deep: true }
  )

  watch(
    linkResetLogs,
    () => {
      persistLinkResetLogs()
    },
    { deep: true }
  )

  watch(
    governanceActivities,
    () => {
      persistGovernanceActivities()
    },
    { deep: true }
  )

  watch(
    governanceExportMeta,
    () => {
      persistGovernanceExportMeta()
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

  .occupancy-link-stats {
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding: 14px 16px;
    border: 1px solid #dce5f2;
    border-radius: 14px;
    background: linear-gradient(180deg, rgb(248 250 252 / 94%), #fff 100%);
  }

  .occupancy-link-stats__header {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
  }

  .occupancy-link-stats__meta {
    display: flex;
    flex-direction: column;
    gap: 6px;
  }

  .occupancy-link-stats__toolbar,
  .occupancy-link-stats__window-switch {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 8px;
  }

  .occupancy-link-stats__title,
  .custom-preset-item__title,
  .annotation-preview__title {
    font-size: 13px;
    font-weight: 700;
    color: #18233a;
  }

  .occupancy-link-stats__last,
  .custom-preset-item__desc {
    font-size: 12px;
    line-height: 1.7;
    color: #6f7f99;
  }

  .occupancy-link-stats__grid {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 12px;
  }

  .occupancy-link-stats__item {
    padding: 12px 14px;
    border: 1px solid #dbe6f5;
    border-radius: 14px;
    background: rgb(255 255 255 / 92%);
  }

  .occupancy-link-stats__label {
    font-size: 12px;
    font-weight: 600;
    color: #6f7f99;
  }

  .occupancy-link-stats__value {
    margin-top: 8px;
    font-size: 18px;
    font-weight: 700;
    color: #1d4ed8;
  }

  .occupancy-link-trend {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .occupancy-link-trend__title {
    font-size: 12px;
    font-weight: 600;
    color: #6f7f99;
  }

  .occupancy-link-trend__grid {
    display: grid;
    grid-template-columns: repeat(7, minmax(0, 1fr));
    gap: 10px;
  }

  .occupancy-link-trend__item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding: 12px 10px;
    border: 1px solid #dbe6f5;
    border-radius: 14px;
    background: rgb(255 255 255 / 92%);
    cursor: pointer;
  }

  .occupancy-link-trend__item--active {
    border-color: #60a5fa;
    box-shadow: 0 0 0 2px rgb(96 165 250 / 16%);
  }

  .occupancy-link-trend__date,
  .occupancy-link-trend__target {
    font-size: 11px;
    line-height: 1.6;
    color: #6f7f99;
    text-align: center;
  }

  .occupancy-link-trend__bar-wrap {
    display: flex;
    align-items: flex-end;
    justify-content: center;
    height: 52px;
    width: 100%;
  }

  .occupancy-link-trend__bar {
    width: 20px;
    min-height: 4px;
    border-radius: 999px;
    background: linear-gradient(180deg, #60a5fa, #1d4ed8);
    transition: height 0.2s ease;
  }

  .occupancy-link-trend__count {
    font-size: 16px;
    font-weight: 700;
    color: #18233a;
  }

  .occupancy-link-drilldown {
    display: flex;
    flex-direction: column;
    gap: 8px;
    padding: 12px 14px;
    border: 1px solid #dbe6f5;
    border-radius: 14px;
    background: linear-gradient(180deg, rgb(239 246 255 / 90%), #fff 100%);
  }

  .occupancy-link-drilldown__title {
    font-size: 13px;
    font-weight: 700;
    color: #18233a;
  }

  .occupancy-link-drilldown__desc {
    font-size: 12px;
    line-height: 1.7;
    color: #6f7f99;
  }

  .occupancy-link-drilldown__actions,
  .history-card-header__actions {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }

  .occupancy-link-drilldown__input {
    max-width: 260px;
  }

  .history-card-header {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    justify-content: space-between;
    gap: 10px;
  }

  .history-card-header__tip {
    padding: 6px 10px;
    border-radius: 999px;
    font-size: 12px;
    font-weight: 600;
    color: #2563eb;
    background: rgb(219 234 254 / 70%);
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

  .export-config-panel__header-actions {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    justify-content: flex-end;
    gap: 10px;
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

  .custom-preset-list {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  .custom-preset-item {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    padding: 12px 14px;
    border: 1px solid #dbe6f5;
    border-radius: 14px;
    background: rgb(255 255 255 / 92%);
  }

  .custom-preset-item__meta,
  .custom-preset-item__actions {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 10px;
  }

  .preset-import-panel {
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding: 14px 16px;
    border: 1px solid #dbe6f5;
    border-radius: 14px;
    background: rgb(255 255 255 / 88%);
  }

  .preset-import-panel__title {
    font-size: 13px;
    font-weight: 700;
    color: #18233a;
  }

  .preset-import-panel__desc {
    font-size: 12px;
    line-height: 1.7;
    color: #6f7f99;
  }

  .preset-import-panel__input {
    min-height: 120px;
    width: 100%;
    padding: 12px;
    border: 1px solid #d8e0ec;
    border-radius: 12px;
    font-size: 13px;
    line-height: 1.7;
    color: #18233a;
    resize: vertical;
    background: #fff;
  }

  .preset-import-panel__actions {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    justify-content: flex-end;
  }

  .preset-import-preview {
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding: 14px 16px;
    border: 1px dashed #d7e3f4;
    border-radius: 14px;
    background: rgb(248 250 252 / 88%);
  }

  .preset-import-preview__header {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
  }

  .preset-import-preview__title {
    font-size: 13px;
    font-weight: 700;
    color: #18233a;
  }

  .preset-import-preview__policies {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }

  .preset-import-preview__summary {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    font-size: 12px;
    font-weight: 600;
    color: #52637d;
  }

  .preset-import-preview__invalid-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .preset-import-preview__invalid-item {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    padding: 10px 12px;
    border: 1px solid #fde2e2;
    border-radius: 12px;
    font-size: 12px;
    color: #9f3f3f;
    background: rgb(254 242 242 / 92%);
  }

  .preset-import-preview__list {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 12px;
  }

  .preset-import-preview__item {
    display: flex;
    flex-direction: column;
    gap: 8px;
    padding: 12px 14px;
    border: 1px solid #dbe6f5;
    border-radius: 12px;
    background: #fff;
  }

  .preset-import-preview__item-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 10px;
    font-size: 12px;
    color: #6f7f99;
  }

  .preset-import-preview__item-header strong {
    font-size: 13px;
    color: #18233a;
  }

  .preset-import-preview__item-desc {
    font-size: 12px;
    line-height: 1.7;
    color: #6f7f99;
    word-break: break-word;
  }

  .preset-import-preview__item-policies {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }

  .preset-import-last-result {
    display: flex;
    flex-direction: column;
    gap: 10px;
    padding: 14px 16px;
    border: 1px solid #dbe6f5;
    border-radius: 14px;
    background: linear-gradient(135deg, rgb(241 245 249 / 96%), rgb(255 255 255 / 96%));
  }

  .preset-import-last-result__header,
  .preset-import-last-result__summary {
    display: flex;
    flex-wrap: wrap;
    justify-content: space-between;
    gap: 10px;
  }

  .preset-import-last-result__title {
    font-size: 13px;
    font-weight: 700;
    color: #18233a;
  }

  .preset-import-last-result__time,
  .preset-import-last-result__summary,
  .preset-import-last-result__item {
    font-size: 12px;
    line-height: 1.7;
    color: #5d6b86;
  }

  .preset-import-last-result__items {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .preset-import-last-result__item {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    padding: 10px 12px;
    border-radius: 10px;
    background: rgb(255 255 255 / 88%);
  }

  .governance-panel {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 12px;
    padding: 14px 16px;
    border: 1px solid #dbe6f5;
    border-radius: 14px;
    background: linear-gradient(135deg, rgb(248 250 252 / 96%), rgb(255 255 255 / 98%));
  }

  .governance-panel__section {
    display: flex;
    flex-direction: column;
    gap: 10px;
    padding: 12px;
    border-radius: 12px;
    background: rgb(255 255 255 / 88%);
    box-shadow: inset 0 0 0 1px rgb(219 230 245 / 80%);
  }

  .governance-panel__section--wide {
    grid-column: 1 / -1;
  }

  .governance-panel__section-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: 12px;
  }

  .governance-panel__summary {
    background: linear-gradient(135deg, rgb(244 248 255 / 96%), rgb(255 255 255 / 98%));
  }

  .governance-panel__summary-grid {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 12px;
  }

  .governance-summary-item {
    display: flex;
    flex-direction: column;
    gap: 6px;
    padding: 12px;
    border-radius: 12px;
    background: rgb(255 255 255 / 88%);
    box-shadow: inset 0 0 0 1px rgb(219 230 245 / 80%);
  }

  .governance-summary-item__label {
    font-size: 12px;
    color: #5d6b86;
  }

  .governance-summary-item__value {
    font-size: 18px;
    line-height: 1.4;
    color: #18233a;
  }

  .governance-summary-item__meta {
    font-size: 12px;
    line-height: 1.7;
    color: #51627d;
    word-break: break-all;
  }

  .governance-panel__section-title {
    font-size: 13px;
    font-weight: 700;
    color: #18233a;
  }

  .governance-panel__section-desc,
  .governance-panel__item-main {
    font-size: 12px;
    line-height: 1.7;
    color: #5d6b86;
  }

  .governance-panel__form,
  .governance-panel__item,
  .governance-panel__item-actions {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }

  .governance-panel__form {
    align-items: center;
  }

  .governance-panel__list {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .governance-panel__item {
    align-items: flex-start;
    justify-content: space-between;
    padding: 10px 12px;
    border-radius: 10px;
    background: rgb(241 245 249 / 72%);
  }

  .governance-panel__item-main {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  .governance-panel__detail {
    display: flex;
    flex-direction: column;
    gap: 8px;
    padding: 10px 12px;
    border-radius: 10px;
    background: rgb(238 244 255 / 72%);
    box-shadow: inset 0 0 0 1px rgb(191 219 254 / 70%);
  }

  .governance-panel__detail-title {
    font-size: 13px;
    font-weight: 700;
    color: #18233a;
  }

  .governance-panel__detail-grid,
  .governance-panel__compare-item {
    display: grid;
    gap: 6px;
    font-size: 12px;
    line-height: 1.7;
    color: #51627d;
  }

  .governance-panel__detail-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .governance-panel__detail-desc {
    font-size: 12px;
    line-height: 1.7;
    color: #51627d;
  }

  .preset-name-panel {
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding: 14px 16px;
    border: 1px solid #dbe6f5;
    border-radius: 14px;
    background: rgb(255 255 255 / 88%);
  }

  .preset-name-panel__grid {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 12px;
  }

  .preset-name-field {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .preset-name-field__label {
    font-size: 12px;
    font-weight: 600;
    color: #6f7f99;
  }

  .preset-name-field__input {
    width: 100%;
    height: 34px;
    padding: 0 12px;
    border: 1px solid #d8e0ec;
    border-radius: 10px;
    font-size: 13px;
    color: #18233a;
    background: #fff;
  }

  .preset-name-panel__actions {
    display: flex;
    justify-content: flex-end;
  }

  .preset-copy-panel {
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding: 14px 16px;
    border: 1px solid #dbe6f5;
    border-radius: 14px;
    background: rgb(255 255 255 / 88%);
  }

  .preset-copy-panel__title {
    font-size: 13px;
    font-weight: 700;
    color: #18233a;
  }

  .preset-copy-panel__desc {
    font-size: 12px;
    line-height: 1.7;
    color: #6f7f99;
  }

  .preset-copy-panel__sources,
  .preset-copy-panel__form {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 10px;
  }

  .preset-copy-panel__form .preset-name-field__input {
    flex: 1;
    min-width: 220px;
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

  .export-preset-chip--subtle {
    color: #375173;
    border-color: #d7e1ee;
    background: #fff;
  }

  .export-preset-chip:hover,
  .export-field-chip:hover {
    transform: translateY(-1px);
  }

  .export-preset-chip--active,
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

  .annotation-template-toolbar {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
  }

  .annotation-template-toolbar__label {
    font-size: 12px;
    font-weight: 600;
    color: #6f7f99;
  }

  .annotation-template-toolbar__items {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
  }

  .annotation-preview {
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding: 14px 16px;
    border: 1px dashed #cfd9e8;
    border-radius: 14px;
    background: linear-gradient(180deg, rgb(248 250 252 / 96%), #fff 100%);
  }

  .annotation-preview__header {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
  }

  .annotation-preview__grid {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 12px;
  }

  .annotation-note--preview {
    background: rgb(255 255 255 / 92%);
  }

  .annotation-compare {
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding: 14px 16px;
    border: 1px solid #dce5f2;
    border-radius: 14px;
    background: linear-gradient(180deg, rgb(248 250 252 / 94%), #fff 100%);
  }

  .annotation-compare__header {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
  }

  .annotation-compare__switches {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
  }

  .annotation-compare__desc {
    font-size: 12px;
    line-height: 1.7;
    color: #6f7f99;
  }

  .annotation-compare__grid {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 12px;
  }

  .annotation-compare-item {
    padding: 14px 16px;
    border: 1px solid #dce5f2;
    border-radius: 14px;
    background: rgb(255 255 255 / 92%);
  }

  .annotation-compare-item--changed {
    border-color: #f9c97f;
    background: linear-gradient(180deg, rgb(255 247 237 / 92%), #fff 100%);
  }

  .annotation-compare-item--stable {
    border-color: #cceadb;
    background: linear-gradient(180deg, rgb(236 253 245 / 88%), #fff 100%);
  }

  .annotation-compare-item__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 10px;
    margin-bottom: 10px;
  }

  .annotation-compare-item__row {
    display: flex;
    flex-direction: column;
    gap: 6px;
    font-size: 12px;
    line-height: 1.7;
    color: #6f7f99;
  }

  .annotation-compare-item__row + .annotation-compare-item__row {
    margin-top: 10px;
  }

  .annotation-compare-item__row strong {
    color: #18233a;
    word-break: break-word;
    white-space: pre-wrap;
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
    .annotation-card__notes,
    .preset-name-panel__grid,
    .annotation-preview__grid,
    .annotation-compare__grid,
    .preset-import-preview__list,
    .governance-panel,
    .governance-panel__summary-grid,
    .occupancy-link-stats__grid,
    .occupancy-link-trend__grid {
      grid-template-columns: 1fr;
    }

    .detail-card--wide {
      grid-column: span 1;
    }

    .current-occupancy-card__header,
    .record-item__header,
    .matrix-item__header,
    .history-toolbar,
    .insight-card__header,
    .governance-panel__section-header {
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
    .export-config-panel__header-actions,
    .record-group__header,
    .annotation-card__header,
    .occupancy-link-stats__header,
    .custom-preset-item,
    .governance-panel__item,
    .annotation-compare__header,
    .annotation-compare-item__header,
    .preset-import-preview__header {
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
