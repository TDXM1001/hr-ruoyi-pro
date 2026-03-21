<template>
  <div class="section-stack">
    <ElAlert
      class="section-alert"
      type="success"
      show-icon
      :closable="false"
      title="整改页签承接巡检异常后的登记与推进，当前阶段不做审批流，只沉淀责任、期限和完成状态。"
    />

    <ElCard class="section-card" shadow="never">
      <template #header>
        <div class="card-title">整改单列表</div>
      </template>
      <div class="record-wrapper">
        <div v-if="rectificationRecords.length" class="record-list">
          <div
            v-for="record in rectificationRecords"
            :key="record.rectificationId || record.inventoryItemId || record.taskId"
            class="record-item"
            :class="getRecordVariantClass(record)"
          >
            <div class="record-item__accent" />
            <div class="record-item__body">
              <div class="record-item__header">
                <div class="record-item__heading">
                  <div class="record-item__title">
                    {{ record.rectificationNo || '待同步整改单号' }} / {{ record.taskNo || '-' }} / {{ record.taskName || '-' }}
                  </div>
                  <div class="record-item__subtitle">
                    {{ isCompletedRecord(record) ? '已完成整改并通过验收，可归档留痕' : '尽快完成整改并提交完成信息' }}
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
                    {{ isCompletedRecord(record) ? '已完成整改并通过验收，可归档留痕' : '尽快完成整改并提交完成信息' }}
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

              <div class="record-item__actions">
                <ElButton
                  v-if="record.rectificationId"
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
        <ElTimeline v-if="rectificationLogs.length">
          <ElTimelineItem
            v-for="record in rectificationLogs"
            :key="record.logId"
            :timestamp="record.operateTime || '-'"
            placement="top"
          >
            <div class="timeline-title">{{ getBizTypeLabel(record.bizType) }}</div>
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

  defineEmits<{
    'edit-rectification': [rectificationId?: number]
    'complete-rectification': [rectificationId?: number]
  }>()

  defineProps<{
    rectificationRecords: AssetRectificationRecord[]
    rectificationLogs: AssetChangeLogRecord[]
    getBizTypeLabel: (bizType?: string) => string
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

  const getRecordVariantClass = (record: AssetRectificationRecord) => {
    return isCompletedRecord(record) ? 'record-item--completed' : 'record-item--pending'
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
</script>

<style scoped lang="scss">
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
    font-size: 16px;
    font-weight: 700;
    line-height: 1.6;
    color: #18233a;
    word-break: break-word;
  }

  .record-item__subtitle {
    font-size: 13px;
    line-height: 1.7;
    color: #5d6b86;
  }

  .record-item__tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    justify-content: flex-end;
  }

  .record-summary-grid,
  .record-detail-grid {
    display: grid;
    gap: 12px;
  }

  .record-summary-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .record-detail-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .summary-card,
  .detail-card {
    display: flex;
    flex-direction: column;
    gap: 8px;
    min-width: 0;
    padding: 14px 16px;
    border: 1px solid #e5edf6;
    border-radius: 14px;
    background: rgb(255 255 255 / 82%);
  }

  .summary-card__label,
  .detail-card__label {
    font-size: 12px;
    font-weight: 600;
    letter-spacing: 0.02em;
    color: #5d6b86;
  }

  .summary-card__value,
  .detail-card__value {
    font-size: 14px;
    font-weight: 600;
    line-height: 1.7;
    color: #18233a;
    word-break: break-word;
    white-space: normal;
  }

  .detail-card--wide {
    grid-column: span 2;
  }

  .record-item__actions {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    justify-content: flex-end;
    padding-top: 4px;
  }

  @media (width <= 960px) {
    .record-item__header {
      flex-direction: column;
    }

    .record-item__tags {
      justify-content: flex-start;
    }

    .record-summary-grid,
    .record-detail-grid {
      grid-template-columns: 1fr;
    }

    .detail-card--wide {
      grid-column: span 1;
    }
  }
</style>
