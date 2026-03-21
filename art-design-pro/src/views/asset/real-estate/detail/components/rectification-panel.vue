<template>
  <div class="section-stack" data-testid="rectification-reading-layout">
    <ElAlert
      class="section-alert"
      type="success"
      show-icon
      :closable="false"
      title="整改页签沉淀责任、期限、完成结果和审批挂载状态，便于资产管理员连续跟踪闭环。"
    />

    <div class="rectification-overview-grid">
      <ElCard class="section-card" shadow="never">
        <template #header>
          <div class="card-title">整改概览</div>
        </template>

        <div class="overview-strip">
          <div class="overview-metric">
            <div class="overview-metric__label">待整改</div>
            <div class="overview-metric__value">{{ pendingCount }}</div>
          </div>
          <div class="overview-metric overview-metric--completed">
            <div class="overview-metric__label">已完成</div>
            <div class="overview-metric__value">{{ completedCount }}</div>
          </div>
          <div class="overview-metric overview-metric--danger">
            <div class="overview-metric__label">已逾期</div>
            <div class="overview-metric__value">{{ overdueCount }}</div>
          </div>
        </div>

        <div class="overview-text">
          整改页签面向资产管理员做持续跟踪。待整改项需要看责任归口、整改期限和完成入口；已完成项除了结果回看，还要确认审批挂载状态是否推进到位。
        </div>
      </ElCard>

      <ElCard class="section-card" shadow="never">
        <template #header>
          <div class="card-title">推进提示</div>
        </template>

        <div class="guide-panel">
          <div class="guide-panel__headline">
            {{ pendingCount ? '先收口待整改记录，再核查审批挂载状态。' : '当前整改记录已收口，可重点核查闭环结果与审批状态。' }}
          </div>
          <div class="guide-panel__line">编辑整改单负责维护责任、期限和说明。</div>
          <div class="guide-panel__line">完成整改只在独立完成页执行，避免基础信息和收口动作混写。</div>
          <div class="guide-panel__line">整改轨迹保留关键节点，便于后续审批流挂接和审计留痕。</div>
        </div>
      </ElCard>
    </div>

    <ElCard class="section-card" shadow="never">
      <template #header>
        <div class="card-title">整改单列表</div>
      </template>

      <div class="record-wrapper" data-testid="rectification-record-list">
        <div v-if="props.rectificationRecords.length" class="record-list">
          <div
            v-for="record in props.rectificationRecords"
            :key="record.rectificationId || record.inventoryItemId || record.taskId"
            class="record-item"
            :class="getRecordVariantClass(record)"
          >
            <div class="record-item__accent" />

            <div class="record-item__body">
              <div class="record-item__header">
                <div class="record-item__heading">
                  <div class="record-item__title">
                    {{ record.rectificationNo || '待同步整改单号' }} / {{ record.taskNo || '-' }} /
                    {{ record.taskName || '-' }}
                  </div>
                  <div class="record-item__subtitle">
                    {{
                      isCompletedRecord(record)
                        ? '已完成整改并通过验收，可归档留痕'
                        : '尽快完成整改并提交完成信息'
                    }}
                  </div>
                </div>

                <div class="record-item__tags">
                  <ElTag :type="getStatusTagType(record.rectificationStatus)" effect="dark">
                    {{ getRectificationStatusLabel(record.rectificationStatus) }}
                  </ElTag>
                  <ElTag
                    v-if="!isCompletedRecord(record)"
                    :type="getPendingTone(record.deadlineDate).type"
                    effect="light"
                  >
                    {{ getPendingTone(record.deadlineDate).label }}
                  </ElTag>
                  <ElTag v-else type="success" effect="light">
                    完成时间 {{ record.completedTime || '-' }}
                  </ElTag>
                </div>
              </div>

              <div class="record-summary-grid">
                <div class="summary-card">
                  <div class="summary-card__label">{{ isCompletedRecord(record) ? '闭环结果' : '当前动作' }}</div>
                  <div class="summary-card__value">
                    {{
                      isCompletedRecord(record)
                        ? '已完成整改并通过验收，可归档留痕'
                        : '尽快完成整改并提交完成信息'
                    }}
                  </div>
                </div>

                <div class="summary-card">
                  <div class="summary-card__label">责任归口</div>
                  <div class="summary-card__value">
                    {{ record.responsibleDeptName || '-' }} / {{ record.responsibleUserName || '-' }}
                  </div>
                </div>

                <div class="summary-card">
                  <div class="summary-card__label">{{ isCompletedRecord(record) ? '完成时间' : '整改期限' }}</div>
                  <div class="summary-card__value">
                    {{ isCompletedRecord(record) ? record.completedTime || '-' : record.deadlineDate || '-' }}
                  </div>
                </div>
              </div>

              <div class="record-detail-grid">
                <div class="detail-card">
                  <div class="detail-card__label">问题类型</div>
                  <div class="detail-card__value">{{ record.issueType || '-' }}</div>
                </div>

                <div class="detail-card detail-card--wide">
                  <div class="detail-card__label">{{ isCompletedRecord(record) ? '完成说明' : '问题描述' }}</div>
                  <div class="detail-card__value">
                    {{ isCompletedRecord(record) ? record.completionDesc || '-' : record.issueDesc || '-' }}
                  </div>
                </div>

                <div class="detail-card" v-if="!isCompletedRecord(record)">
                  <div class="detail-card__label">状态判断</div>
                  <div class="detail-card__value">{{ getPendingTone(record.deadlineDate).label }}</div>
                </div>

                <div class="detail-card detail-card--wide" v-else>
                  <div class="detail-card__label">验收备注</div>
                  <div class="detail-card__value">{{ record.acceptanceRemark || '-' }}</div>
                </div>
              </div>

              <div v-if="isCompletedRecord(record)" class="approval-section">
                <div class="approval-section__header">
                  <div>
                    <div class="approval-section__title">整改审批挂载</div>
                    <div class="approval-section__desc">审批状态与轨迹独立挂载在整改单上，便于后续接正式审批主线。</div>
                  </div>
                  <ElTag :type="getApprovalTagType(record.approvalStatus)" effect="light">
                    {{ getApprovalStatusLabel(record.approvalStatus) }}
                  </ElTag>
                </div>

                <div class="approval-grid">
                  <div class="detail-card">
                    <div class="detail-card__label">提交时间</div>
                    <div class="detail-card__value">{{ record.approvalSubmittedTime || '-' }}</div>
                  </div>

                  <div class="detail-card">
                    <div class="detail-card__label">审批完成</div>
                    <div class="detail-card__value">{{ record.approvalFinishedTime || '-' }}</div>
                  </div>
                </div>
              </div>

              <div class="record-item__actions">
                <ElButton
                  v-if="record.rectificationId"
                  :data-testid="`rectification-edit-link-${record.rectificationId}`"
                  link
                  type="primary"
                  @click="$emit('edit-rectification', record.rectificationId)"
                >
                  查看整改单
                </ElButton>

                <ElButton
                  v-if="record.rectificationId && !isCompletedRecord(record)"
                  :data-testid="`rectification-complete-link-${record.rectificationId}`"
                  link
                  type="warning"
                  @click="$emit('complete-rectification', record.rectificationId)"
                >
                  完成整改
                </ElButton>

                <ElButton
                  v-if="record.rectificationId && isCompletedRecord(record) && props.canEdit && canSubmitApproval(record)"
                  :data-testid="`rectification-submit-approval-link-${record.rectificationId}`"
                  link
                  type="success"
                  @click="$emit('submit-approval', record.rectificationId)"
                >
                  提交审批
                </ElButton>

                <ElButton
                  v-if="record.rectificationId && isCompletedRecord(record) && props.canEdit && canApproveOrReject(record)"
                  :data-testid="`rectification-approve-link-${record.rectificationId}`"
                  link
                  type="success"
                  @click="$emit('approve-approval', record.rectificationId)"
                >
                  审批通过
                </ElButton>

                <ElButton
                  v-if="record.rectificationId && isCompletedRecord(record) && props.canEdit && canApproveOrReject(record)"
                  :data-testid="`rectification-reject-link-${record.rectificationId}`"
                  link
                  type="danger"
                  @click="$emit('reject-approval', record.rectificationId)"
                >
                  审批驳回
                </ElButton>

                <ElButton
                  v-if="record.rectificationId && isCompletedRecord(record)"
                  :data-testid="`rectification-approval-records-link-${record.rectificationId}`"
                  link
                  type="info"
                  @click="$emit('view-approval-records', record.rectificationId)"
                >
                  查看审批轨迹
                </ElButton>
              </div>
            </div>
          </div>
        </div>

        <ElEmpty v-else description="暂无整改记录" :image-size="68" />
      </div>
    </ElCard>

    <ElCard class="section-card" shadow="never">
      <template #header>
        <div class="card-title">整改轨迹</div>
      </template>

      <div class="record-wrapper">
        <ElTimeline v-if="props.rectificationLogs.length">
          <ElTimelineItem
            v-for="record in props.rectificationLogs"
            :key="record.logId"
            :timestamp="record.operateTime || '-'"
            placement="top"
          >
            <div class="timeline-title">{{ props.getBizTypeLabel(record.bizType) }}</div>
            <div class="timeline-desc">{{ record.changeDesc || '暂无整改说明' }}</div>
            <div class="timeline-meta">操作人：{{ record.operateBy || '-' }}</div>
          </ElTimelineItem>
        </ElTimeline>

        <ElEmpty v-else description="暂无整改轨迹" :image-size="68" />
      </div>
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import type { AssetChangeLogRecord, AssetRectificationRecord } from '@/api/asset/ledger'

  const props = defineProps<{
    rectificationRecords: AssetRectificationRecord[]
    rectificationLogs: AssetChangeLogRecord[]
    getBizTypeLabel: (bizType?: string) => string
    canEdit?: boolean
  }>()

  defineEmits<{
    'edit-rectification': [rectificationId?: number]
    'complete-rectification': [rectificationId?: number]
    'submit-approval': [rectificationId?: number]
    'approve-approval': [rectificationId?: number]
    'reject-approval': [rectificationId?: number]
    'view-approval-records': [rectificationId?: number]
  }>()

  const isCompletedRecord = (record: AssetRectificationRecord) => {
    return String(record.rectificationStatus || '').toUpperCase() === 'COMPLETED'
  }

  const getRectificationStatusLabel = (status?: string) => {
    const mapper: Record<string, string> = {
      PENDING: '待整改',
      COMPLETED: '已完成'
    }
    return mapper[String(status || '').toUpperCase()] || status || '-'
  }

  const getStatusTagType = (status?: string) => {
    const mapper: Record<string, 'warning' | 'success' | 'info'> = {
      PENDING: 'warning',
      COMPLETED: 'success'
    }
    return mapper[String(status || '').toUpperCase()] || 'info'
  }

  const getApprovalStatusLabel = (status?: string) => {
    const mapper: Record<string, string> = {
      UNSUBMITTED: '待提交审批',
      SUBMITTED: '审批中',
      APPROVED: '审批通过',
      REJECTED: '审批驳回'
    }
    return mapper[String(status || '').toUpperCase()] || '待提交审批'
  }

  const getApprovalTagType = (status?: string) => {
    const mapper: Record<string, 'info' | 'warning' | 'success' | 'danger'> = {
      UNSUBMITTED: 'info',
      SUBMITTED: 'warning',
      APPROVED: 'success',
      REJECTED: 'danger'
    }
    return mapper[String(status || '').toUpperCase()] || 'info'
  }

  const getRecordVariantClass = (record: AssetRectificationRecord) => {
    return isCompletedRecord(record) ? 'record-item--completed' : 'record-item--pending'
  }

  const canSubmitApproval = (record: AssetRectificationRecord) => {
    const approvalStatus = String(record.approvalStatus || '').toUpperCase()
    return !approvalStatus || approvalStatus === 'UNSUBMITTED' || approvalStatus === 'REJECTED'
  }

  const canApproveOrReject = (record: AssetRectificationRecord) => {
    return String(record.approvalStatus || '').toUpperCase() === 'SUBMITTED'
  }

  const getPendingTone = (deadlineDate?: string) => {
    if (!deadlineDate) {
      return { label: '未设期限', type: 'info' as const }
    }

    const deadline = new Date(`${deadlineDate}T00:00:00`)
    if (Number.isNaN(deadline.getTime())) {
      return { label: '期限待确认', type: 'info' as const }
    }

    const now = new Date()
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
    const diffDays = Math.floor((deadline.getTime() - today.getTime()) / 86400000)

    if (diffDays < 0) {
      return { label: '已逾期', type: 'danger' as const }
    }
    if (diffDays <= 3) {
      return { label: '即将到期', type: 'warning' as const }
    }
    return { label: '按期推进', type: 'success' as const }
  }

  const pendingCount = computed(() => props.rectificationRecords.filter((record) => !isCompletedRecord(record)).length)
  const completedCount = computed(() => props.rectificationRecords.filter((record) => isCompletedRecord(record)).length)
  const overdueCount = computed(() => {
    return props.rectificationRecords.filter((record) => {
      return !isCompletedRecord(record) && getPendingTone(record.deadlineDate).label === '已逾期'
    }).length
  })
</script>

<style scoped lang="scss">
  .rectification-overview-grid {
    display: grid;
    grid-template-columns: minmax(0, 1.4fr) minmax(320px, 0.9fr);
    gap: 12px;
  }

  .overview-strip {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 12px;
    padding: 16px 16px 12px;
  }

  .overview-metric {
    padding: 16px;
    border: 1px solid #fde68a;
    border-radius: 16px;
    background: linear-gradient(180deg, rgb(255 247 237 / 92%), #fff 100%);
  }

  .overview-metric--completed {
    border-color: #99f6e4;
    background: linear-gradient(180deg, rgb(236 253 245 / 92%), #fff 100%);
  }

  .overview-metric--danger {
    border-color: #fdba74;
    background: linear-gradient(180deg, rgb(255 237 213 / 92%), #fff 100%);
  }

  .overview-metric__label {
    font-size: 12px;
    font-weight: 600;
    color: #6c7d97;
  }

  .overview-metric__value {
    margin-top: 10px;
    font-size: 28px;
    font-weight: 800;
    line-height: 1;
    color: #18233a;
  }

  .overview-text {
    padding: 0 16px 16px;
    font-size: 13px;
    line-height: 1.8;
    color: #51627f;
  }

  .guide-panel {
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding: 16px;
  }

  .guide-panel__headline {
    font-size: 18px;
    font-weight: 700;
    line-height: 1.6;
    color: #18233a;
  }

  .guide-panel__line {
    position: relative;
    padding-left: 14px;
    font-size: 13px;
    line-height: 1.8;
    color: #51627f;

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 10px;
      width: 6px;
      height: 6px;
      border-radius: 999px;
      background: #0f766e;
    }
  }

  .record-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .record-item {
    position: relative;
    display: grid;
    grid-template-columns: 8px minmax(0, 1fr);
    overflow: hidden;
    border: 1px solid #e5edf6;
    border-radius: 18px;
    background: #fff;
    box-shadow: 0 10px 24px rgb(15 23 42 / 6%);
  }

  .record-item__accent {
    min-height: 100%;
  }

  .record-item__body {
    display: flex;
    flex-direction: column;
    gap: 16px;
    padding: 18px 20px;
  }

  .record-item--pending {
    background: linear-gradient(180deg, rgb(255 247 237 / 92%), #fff 56%);

    .record-item__accent {
      background: linear-gradient(180deg, #f97316, #fb923c);
    }
  }

  .record-item--completed {
    background: linear-gradient(180deg, rgb(236 253 245 / 92%), #fff 58%);

    .record-item__accent {
      background: linear-gradient(180deg, #0f766e, #14b8a6);
    }
  }

  .record-item__header {
    display: flex;
    justify-content: space-between;
    gap: 16px;
  }

  .record-item__heading {
    display: flex;
    flex-direction: column;
    gap: 6px;
    min-width: 0;
  }

  .record-item__title {
    font-size: 18px;
    font-weight: 700;
    color: #18233a;
    word-break: break-word;
  }

  .record-item__subtitle {
    font-size: 13px;
    color: #5d6b86;
    line-height: 1.7;
  }

  .record-item__tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    justify-content: flex-end;
  }

  .record-summary-grid,
  .record-detail-grid,
  .approval-grid {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 12px;
  }

  .summary-card,
  .detail-card {
    padding: 14px 16px;
    border: 1px solid #e7edf6;
    border-radius: 14px;
    background: rgb(255 255 255 / 88%);
  }

  .summary-card__label,
  .detail-card__label {
    font-size: 12px;
    font-weight: 600;
    color: #6f7f99;
  }

  .summary-card__value,
  .detail-card__value {
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

  .approval-section {
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding: 14px 16px;
    border: 1px dashed #bfd3ea;
    border-radius: 16px;
    background: linear-gradient(180deg, rgb(248 250 252 / 96%), #fff 100%);
  }

  .approval-section__header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 12px;
  }

  .approval-section__title {
    font-size: 14px;
    font-weight: 700;
    color: #18233a;
  }

  .approval-section__desc {
    margin-top: 4px;
    font-size: 12px;
    line-height: 1.7;
    color: #607089;
  }

  .record-item__actions {
    display: flex;
    flex-wrap: wrap;
    justify-content: flex-end;
    gap: 12px;
    padding-top: 4px;
  }

  .timeline-title {
    font-size: 14px;
    font-weight: 700;
    color: #18233a;
  }

  .timeline-desc {
    margin-top: 6px;
    font-size: 13px;
    line-height: 1.8;
    color: #51627f;
    word-break: break-word;
  }

  .timeline-meta {
    margin-top: 6px;
    font-size: 12px;
    color: #7b8aa5;
  }

  @media (width <= 1080px) {
    .rectification-overview-grid {
      grid-template-columns: 1fr;
    }
  }

  @media (width <= 900px) {
    .record-summary-grid,
    .record-detail-grid,
    .approval-grid {
      grid-template-columns: 1fr;
    }

    .detail-card--wide {
      grid-column: span 1;
    }

    .record-item__header,
    .approval-section__header {
      flex-direction: column;
      align-items: flex-start;
    }

    .record-item__tags,
    .record-item__actions {
      justify-content: flex-start;
    }
  }
</style>
