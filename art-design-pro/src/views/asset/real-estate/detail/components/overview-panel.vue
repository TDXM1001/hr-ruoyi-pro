<template>
  <div class="section-stack">
    <ElCard class="section-card" shadow="never">
      <template #header>
        <div class="card-title">基础台账</div>
      </template>
      <ElDescriptions class="detail-descriptions detail-descriptions--3" :column="3" border>
        <ElDescriptionsItem label="资产编码">{{ detailData.assetCode || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="资产名称">{{ detailData.assetName || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="资产状态">{{ getStatusLabel(detailData.assetStatus) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="录入来源">{{ getSourceTypeLabel(detailData.sourceType) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="取得方式">{{ getAcquireTypeLabel(detailData.acquireType) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="资产分类">{{ detailData.categoryName || '-' }}</ElDescriptionsItem>
      </ElDescriptions>
    </ElCard>

    <ElCard class="section-card" shadow="never">
      <template #header>
        <div class="card-title">权属信息</div>
      </template>
      <ElDescriptions class="detail-descriptions detail-descriptions--3" :column="3" border>
        <ElDescriptionsItem label="权属证号" :span="2">{{ detailData.ownershipCertNo || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="土地用途">{{ detailData.landUseType || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="建筑面积（㎡）">{{ formatArea(detailData.buildingArea) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="所在位置" :span="2">{{ detailData.locationName || '-' }}</ElDescriptionsItem>
      </ElDescriptions>
    </ElCard>

    <ElCard class="section-card" shadow="never">
      <template #header>
        <div class="card-title">整改闭环摘要</div>
      </template>
      <div class="record-wrapper">
        <div class="rectification-summary-grid">
          <div
            class="rectification-summary-item"
            data-testid="overview-rectification-count-pending-rectification"
          >
            <span>待整改</span>
            <strong>{{ rectificationSummary.pendingRectificationCount }}</strong>
          </div>
          <div class="rectification-summary-item" data-testid="overview-rectification-count-pending-submit">
            <span>待提交审批</span>
            <strong>{{ rectificationSummary.pendingSubmitCount }}</strong>
          </div>
          <div class="rectification-summary-item" data-testid="overview-rectification-count-in-review">
            <span>审批中</span>
            <strong>{{ rectificationSummary.inReviewCount }}</strong>
          </div>
          <div
            class="rectification-summary-item"
            data-testid="overview-rectification-count-rejected-resubmit"
          >
            <span>驳回待重提</span>
            <strong>{{ rectificationSummary.rejectedResubmitCount }}</strong>
          </div>
          <div
            class="rectification-summary-item"
            data-testid="overview-rectification-count-approved-closed"
          >
            <span>审批通过</span>
            <strong>{{ rectificationSummary.approvedClosedCount }}</strong>
          </div>
        </div>

        <div class="rectification-focus-card">
          <div class="rectification-focus-card__header">
            <div>
              <div class="rectification-focus-card__label">当前闭环状态</div>
              <div class="rectification-focus-card__title">{{ rectificationSummary.overallLabel }}</div>
            </div>
            <ElTag :type="rectificationSummary.overallTagType" effect="light">
              {{ rectificationSummary.overallLabel }}
            </ElTag>
          </div>
          <div class="rectification-focus-card__meta">
            <span class="rectification-focus-card__label">最近整改动作</span>
            <strong>{{ rectificationSummary.latestActionLabel }}</strong>
            <span>{{ rectificationSummary.latestActionTime || '-' }}</span>
          </div>
          <div class="timeline-desc">{{ rectificationSummary.latestActionDesc }}</div>
          <div class="timeline-meta">{{ rectificationSummary.nextStep }}</div>
        </div>
      </div>
    </ElCard>

    <ElCard class="section-card" shadow="never">
      <template #header>
        <div class="card-title">处置闭环摘要</div>
      </template>
      <div class="record-wrapper">
        <div class="disposal-focus-card" data-testid="overview-disposal-focus-card">
          <div class="rectification-focus-card__header">
            <div>
              <div class="rectification-focus-card__label">当前闭环状态</div>
              <div class="rectification-focus-card__title">{{ disposalSummary.overallLabel }}</div>
            </div>
            <ElTag :type="disposalSummary.overallTagType" effect="light">
              {{ disposalSummary.overallLabel }}
            </ElTag>
          </div>
          <div class="rectification-focus-card__meta">
            <span class="rectification-focus-card__label">最近处置动作</span>
            <strong>{{ disposalSummary.latestActionLabel }}</strong>
            <span>{{ disposalSummary.latestActionTime || '-' }}</span>
          </div>
          <div class="disposal-responsibility-grid">
            <div class="disposal-responsibility-item">
              <span>当前责任归口</span>
              <strong>{{ disposalSummary.responsibilityOwnerLabel }}</strong>
            </div>
            <div class="disposal-responsibility-item">
              <span>责任动作</span>
              <strong>{{ disposalSummary.responsibilityActionLabel }}</strong>
            </div>
            <div class="disposal-responsibility-item">
              <span>最近责任人</span>
              <strong>{{ disposalSummary.latestActionOwner || '-' }}</strong>
            </div>
          </div>
          <div class="timeline-desc">{{ disposalSummary.latestActionDesc }}</div>
          <div class="timeline-desc timeline-desc--emphasis">{{ disposalSummary.responsibilityHint }}</div>
          <div class="timeline-meta">{{ disposalSummary.nextStep }}</div>
        </div>
      </div>
    </ElCard>

    <ElCard class="section-card" shadow="never">
      <template #header>
        <div class="card-title">生命周期轨迹</div>
      </template>
      <div class="record-wrapper">
        <ElTimeline v-if="changeLogs.length">
          <ElTimelineItem
            v-for="record in changeLogs"
            :key="record.logId"
            :timestamp="record.operateTime || '-'"
            placement="top"
          >
            <div class="timeline-title-group">
              <div class="timeline-title">{{ getBizTypeLabel(record.bizType) }}</div>
              <ElTag
                v-if="record.rectificationEventLabel"
                :data-testid="`overview-rectification-event-${record.logId}`"
                :type="record.rectificationEventTagType"
                effect="light"
                size="small"
              >
                {{ record.rectificationEventLabel }}
              </ElTag>
              <ElTag
                v-if="record.disposalEventLabel"
                :data-testid="`overview-disposal-event-${record.logId}`"
                :type="record.disposalEventTagType"
                effect="light"
                size="small"
              >
                {{ record.disposalEventLabel }}
              </ElTag>
            </div>
            <div class="timeline-desc">{{ record.changeDesc || '暂无变更说明' }}</div>
            <div v-if="record.rectificationEventHint" class="timeline-desc timeline-desc--emphasis">
              {{ record.rectificationEventHint }}
            </div>
            <div v-if="record.disposalEventHint" class="timeline-desc timeline-desc--emphasis">
              {{ record.disposalEventHint }}
            </div>
            <div class="timeline-meta">
              操作人：{{ record.operateBy || '-' }}，状态：{{ record.beforeStatus || '-' }} ->
              {{ record.afterStatus || '-' }}
            </div>
          </ElTimelineItem>
        </ElTimeline>
        <ElEmpty v-else description="暂无生命周期记录" :image-size="68" />
      </div>
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import type {
    OverviewLifecycleRecord,
    RectificationOverviewSummary
  } from './rectification-overview'
  import type { DisposalOverviewSummary } from './disposal-overview'

  defineProps<{
    detailData: Record<string, any>
    changeLogs: OverviewLifecycleRecord[]
    rectificationSummary: RectificationOverviewSummary
    disposalSummary: DisposalOverviewSummary
    getStatusLabel: (status?: string) => string
    getSourceTypeLabel: (sourceType?: string) => string
    getAcquireTypeLabel: (acquireType?: string) => string
    getBizTypeLabel: (bizType?: string) => string
    formatArea: (value?: number | string) => string
  }>()
</script>

<style scoped lang="scss">
  .rectification-summary-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(130px, 1fr));
    gap: 10px;
    margin-bottom: 12px;
  }

  .rectification-summary-item {
    display: flex;
    flex-direction: column;
    gap: 8px;
    padding: 12px 14px;
    border: 1px solid #e9edf7;
    border-radius: 12px;
    background: #fbfcff;

    span {
      font-size: 12px;
      color: #6b7b95;
    }

    strong {
      font-size: 22px;
      color: #1d2f4f;
      line-height: 1;
    }
  }

  .rectification-focus-card {
    display: flex;
    flex-direction: column;
    gap: 10px;
    padding: 14px 16px;
    border: 1px solid #dce8f3;
    border-radius: 12px;
    background: linear-gradient(135deg, #fcfeff 0%, #f5fbff 100%);
  }

  .disposal-focus-card {
    @extend .rectification-focus-card;
    background: linear-gradient(135deg, #fffdf8 0%, #fff7ec 100%);
    border-color: #f3e2c8;
  }

  .disposal-responsibility-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(130px, 1fr));
    gap: 10px;
  }

  .disposal-responsibility-item {
    display: flex;
    flex-direction: column;
    gap: 6px;
    padding: 10px 12px;
    border: 1px solid #f3e7d8;
    border-radius: 10px;
    background: rgba(255, 255, 255, 0.75);

    span {
      font-size: 12px;
      color: #7b6d59;
    }

    strong {
      color: #3a2d1b;
      font-size: 14px;
    }
  }

  .rectification-focus-card__header,
  .rectification-focus-card__meta {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    flex-wrap: wrap;
  }

  .rectification-focus-card__label {
    font-size: 12px;
    color: #70839c;
  }

  .rectification-focus-card__title {
    margin-top: 6px;
    font-size: 18px;
    font-weight: 700;
    color: #1d2f4f;
  }

  .timeline-title-group {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-wrap: wrap;
  }

  .timeline-desc--emphasis {
    margin-top: 4px;
    color: #2f5d7c;
  }
</style>
