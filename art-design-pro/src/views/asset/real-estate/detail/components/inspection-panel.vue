<template>
  <div class="section-stack" data-testid="inspection-reading-layout">
    <ElAlert
      class="section-alert"
      type="warning"
      show-icon
      :closable="false"
      title="巡检页签聚焦任务事实、异常结果和整改联动，不在这里堆叠过多处理表单。"
    />

    <div class="inspection-overview-grid">
      <ElCard class="section-card" shadow="never">
        <template #header>
          <div class="card-title">巡检概览</div>
        </template>

        <div class="card-body">
          <div class="status-strip">
            <div
              data-testid="inspection-status-not-created"
              class="status-metric-card status-metric-card--not-created"
            >
              <div class="status-metric-card__label">未发起整改</div>
              <div class="status-metric-card__value">{{ inspectionStatusSummary.notCreated }}</div>
              <div class="status-metric-card__hint">需要尽快明确责任人与整改期限</div>
            </div>

            <div
              data-testid="inspection-status-pending"
              class="status-metric-card status-metric-card--pending"
            >
              <div class="status-metric-card__label">待整改</div>
              <div class="status-metric-card__value">{{ inspectionStatusSummary.pending }}</div>
              <div class="status-metric-card__hint">已进入整改流程，需持续跟踪推进</div>
            </div>

            <div
              data-testid="inspection-status-completed"
              class="status-metric-card status-metric-card--completed"
            >
              <div class="status-metric-card__label">已闭环</div>
              <div class="status-metric-card__value">{{ inspectionStatusSummary.completed }}</div>
              <div class="status-metric-card__hint">已完成整改并通过验收，可归档留痕</div>
            </div>
          </div>

          <ElDescriptions class="detail-descriptions detail-descriptions--3" :column="3" border>
            <ElDescriptionsItem label="最近巡检日期">
              {{ detailData.lastInventoryDate || '-' }}
            </ElDescriptionsItem>
            <ElDescriptionsItem label="巡检记录数">
              {{ inspectionRecords.length }}
            </ElDescriptionsItem>
            <ElDescriptionsItem label="待整改数">
              {{ pendingRectificationCount }}
            </ElDescriptionsItem>
          </ElDescriptions>
        </div>
      </ElCard>

      <ElCard class="section-card" shadow="never">
        <template #header>
          <div class="card-title">处理判断</div>
        </template>

        <div class="card-body decision-card">
          <div class="decision-card__headline">
            <div class="decision-card__label">当前关注点</div>
            <div class="decision-card__value">
              {{ pendingRectificationCount ? `还有 ${pendingRectificationCount} 条记录待推进` : '当前无待推进整改' }}
            </div>
          </div>

          <div class="decision-card__summary">
            优先处理“未发起整改”和“待整改”记录；已闭环记录只做事实留痕和回溯查看。
          </div>

          <div class="decision-list">
            <div class="decision-list__item">先看异常结果，再判断是否需要发起整改。</div>
            <div class="decision-list__item">整改动作只从任务记录发起，避免在详情页内混入处理表单。</div>
            <div class="decision-list__item">已闭环记录保留完成时间和验收备注，便于资产管理员复核。</div>
          </div>
        </div>
      </ElCard>
    </div>

    <ElCard class="section-card" shadow="never">
      <template #header>
        <div class="card-title">巡检任务记录</div>
      </template>

      <div class="record-wrapper" data-testid="inspection-record-list">
        <div v-if="inspectionRecords.length" class="record-list">
          <div
            v-for="record in inspectionRecords"
            :key="record.itemId || record.taskId"
            class="record-item"
            :class="`record-item--${record.rectificationLinkStatus.toLowerCase()}`"
          >
            <div class="record-item__header">
              <div class="record-item__heading">
                <div class="record-item__title">
                  {{ record.taskNo || '-' }} / {{ record.taskName || '-' }}
                </div>
                <div class="record-item__subtitle">
                  巡检结果：{{ getInventoryResultLabel(record.inventoryResult) }}，后续动作：{{
                    getFollowUpActionLabel(record.followUpAction)
                  }}
                </div>
              </div>

              <ElTag :type="getRectificationLinkTagType(record.rectificationLinkStatus)" effect="light">
                {{ getRectificationLinkLabel(record.rectificationLinkStatus) }}
              </ElTag>
            </div>

            <div class="record-summary-grid">
              <div class="summary-chip">
                <div class="summary-chip__label">登记人</div>
                <div class="summary-chip__value">{{ record.checkedBy || '-' }}</div>
              </div>
              <div class="summary-chip">
                <div class="summary-chip__label">登记时间</div>
                <div class="summary-chip__value">{{ record.checkedTime || '-' }}</div>
              </div>
              <div class="summary-chip">
                <div class="summary-chip__label">整改联动</div>
                <div class="summary-chip__value">
                  整改联动：{{ getRectificationLinkLabel(record.rectificationLinkStatus) }}
                </div>
              </div>
            </div>

            <div v-if="record.resultDesc" class="record-note">
              <div class="record-note__label">问题描述</div>
              <div class="record-note__value">{{ record.resultDesc }}</div>
            </div>

            <div class="link-summary-card" :class="`link-summary-card--${record.rectificationLinkStatus.toLowerCase()}`">
              <div class="link-summary-card__title">
                整改联动：{{ getRectificationLinkLabel(record.rectificationLinkStatus) }}
              </div>
              <div class="link-summary-card__summary">{{ getRectificationSummary(record) }}</div>
              <div
                v-for="line in getRectificationDetailLines(record)"
                :key="`${record.itemId || record.taskId}-${line}`"
                class="link-summary-card__detail"
              >
                {{ line }}
              </div>
            </div>

            <div class="record-item__actions">
              <ElButton
                :data-testid="`inspection-task-link-${record.taskId}`"
                link
                type="primary"
                @click="$emit('inspection-task', record.taskId)"
              >
                查看任务明细
              </ElButton>

              <ElButton
                v-if="canEdit && record.rectificationLinkStatus === 'NOT_CREATED'"
                :data-testid="`rectification-create-link-${record.taskId}`"
                link
                type="warning"
                @click="$emit('create-rectification', record)"
              >
                发起整改
              </ElButton>

              <ElButton
                v-if="record.linkedRectification?.rectificationId || record.followUpBizId"
                :data-testid="`rectification-edit-link-${record.linkedRectification?.rectificationId || record.followUpBizId}`"
                link
                type="success"
                @click="$emit('edit-rectification', record.linkedRectification?.rectificationId || record.followUpBizId)"
              >
                查看整改
              </ElButton>
            </div>
          </div>
        </div>

        <ElEmpty v-else description="暂无巡检记录" :image-size="68" />
      </div>
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import type { AssetInventoryRecord, AssetRectificationRecord } from '@/api/asset/ledger'

  type InspectionLinkStatus = 'NONE_REQUIRED' | 'NOT_CREATED' | 'PENDING' | 'COMPLETED'

  type InspectionRecordView = AssetInventoryRecord & {
    rectificationLinkStatus: InspectionLinkStatus
    linkedRectification?: AssetRectificationRecord
  }

  defineEmits<{
    'inspection-task': [taskId?: number]
    'create-rectification': [record: AssetInventoryRecord]
    'edit-rectification': [rectificationId?: number]
  }>()

  defineProps<{
    detailData: Record<string, any>
    inspectionRecords: InspectionRecordView[]
    inspectionStatusSummary: {
      notCreated: number
      pending: number
      completed: number
    }
    pendingRectificationCount: number
    canEdit: boolean
  }>()

  const getInventoryResultLabel = (result?: string) => {
    const mapper: Record<string, string> = {
      NORMAL: '正常',
      LOSS: '盘亏',
      MISSING: '缺失',
      DAMAGED: '损坏',
      LOCATION_DIFF: '位置不符',
      RESPONSIBLE_DIFF: '责任人不符'
    }
    return mapper[String(result || '').toUpperCase()] || result || '-'
  }

  const getFollowUpActionLabel = (action?: string) => {
    const mapper: Record<string, string> = {
      NONE: '无',
      UPDATE_LEDGER: '修正台账',
      CREATE_DISPOSAL: '发起处置'
    }
    return mapper[String(action || '').toUpperCase()] || action || '-'
  }

  const getRectificationLinkLabel = (status: InspectionLinkStatus) => {
    const mapper: Record<InspectionLinkStatus, string> = {
      NONE_REQUIRED: '无需整改',
      NOT_CREATED: '未发起整改',
      PENDING: '待整改',
      COMPLETED: '已闭环'
    }
    return mapper[status]
  }

  const getRectificationLinkTagType = (status: InspectionLinkStatus) => {
    const mapper: Record<InspectionLinkStatus, 'info' | 'warning' | 'danger' | 'success'> = {
      NONE_REQUIRED: 'success',
      NOT_CREATED: 'danger',
      PENDING: 'warning',
      COMPLETED: 'success'
    }
    return mapper[status]
  }

  const getRectificationSummary = (record: InspectionRecordView) => {
    const mapper: Record<InspectionLinkStatus, string> = {
      NONE_REQUIRED: '巡检正常，无需进入整改闭环',
      NOT_CREATED: '请尽快发起整改并明确责任人和期限',
      PENDING: '已进入整改流程，请持续跟踪责任人和整改期限',
      COMPLETED: '已完成整改并通过验收，可归档留痕'
    }
    return mapper[record.rectificationLinkStatus]
  }

  const getRectificationDetailLines = (record: InspectionRecordView) => {
    const linkedRectification = record.linkedRectification

    if (record.rectificationLinkStatus === 'NOT_CREATED') {
      return ['请尽快发起整改并明确责任人和期限']
    }

    if (record.rectificationLinkStatus === 'PENDING') {
      return [
        `责任归口：${linkedRectification?.responsibleDeptName || '-'} / ${linkedRectification?.responsibleUserName || '-'}`,
        `整改期限：${linkedRectification?.deadlineDate || '-'}`
      ]
    }

    if (record.rectificationLinkStatus === 'COMPLETED') {
      const details = [`完成时间：${linkedRectification?.completedTime || '-'}`]
      if (linkedRectification?.acceptanceRemark) {
        details.push(`验收备注：${linkedRectification.acceptanceRemark}`)
      } else if (linkedRectification?.completionDesc) {
        details.push(`完成说明：${linkedRectification.completionDesc}`)
      }
      return details
    }

    return ['本条记录无需进入整改闭环']
  }
</script>

<style scoped lang="scss">
  .inspection-overview-grid {
    display: grid;
    grid-template-columns: minmax(0, 1.6fr) minmax(300px, 0.9fr);
    gap: 12px;
  }

  .card-body {
    padding: 16px;
  }

  .status-strip {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 12px;
    margin-bottom: 14px;
  }

  .status-metric-card {
    padding: 16px;
    border: 1px solid #e5edf6;
    border-radius: 16px;
    background: #fff;
  }

  .status-metric-card--not-created {
    border-color: #fdba74;
    background: linear-gradient(180deg, rgb(255 237 213 / 92%), #fff 100%);
  }

  .status-metric-card--pending {
    border-color: #facc15;
    background: linear-gradient(180deg, rgb(254 249 195 / 92%), #fff 100%);
  }

  .status-metric-card--completed {
    border-color: #86efac;
    background: linear-gradient(180deg, rgb(220 252 231 / 92%), #fff 100%);
  }

  .status-metric-card__label {
    font-size: 13px;
    font-weight: 700;
    color: #26415f;
  }

  .status-metric-card__value {
    margin-top: 8px;
    font-size: 30px;
    font-weight: 800;
    line-height: 1;
    color: #112847;
  }

  .status-metric-card__hint {
    margin-top: 10px;
    font-size: 12px;
    line-height: 1.7;
    color: #5f7392;
  }

  .decision-card {
    display: flex;
    flex-direction: column;
    gap: 14px;
    min-height: 100%;
  }

  .decision-card__headline {
    padding: 14px 16px;
    border: 1px solid #dce9f6;
    border-radius: 16px;
    background: linear-gradient(180deg, #f8fbff, #fff);
  }

  .decision-card__label {
    font-size: 12px;
    font-weight: 600;
    color: #6c7d97;
  }

  .decision-card__value {
    margin-top: 8px;
    font-size: 20px;
    font-weight: 700;
    color: #18233a;
    line-height: 1.5;
  }

  .decision-card__summary {
    font-size: 13px;
    line-height: 1.8;
    color: #51627f;
  }

  .decision-list {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  .decision-list__item {
    position: relative;
    padding-left: 14px;
    font-size: 13px;
    line-height: 1.8;
    color: #42526c;

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 10px;
      width: 6px;
      height: 6px;
      border-radius: 999px;
      background: #1f7a8c;
    }
  }

  .record-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .record-item {
    display: flex;
    flex-direction: column;
    gap: 14px;
    padding: 18px;
    border: 1px solid #e7edf6;
    border-radius: 18px;
    background: linear-gradient(180deg, rgb(255 255 255 / 96%), rgb(248 251 255 / 96%));
  }

  .record-item--not_created {
    border-color: #fed7aa;
    background: linear-gradient(180deg, rgb(255 251 245 / 98%), #fff 100%);
  }

  .record-item--pending {
    border-color: #fde68a;
    background: linear-gradient(180deg, rgb(255 253 244 / 98%), #fff 100%);
  }

  .record-item--completed {
    border-color: #a7f3d0;
    background: linear-gradient(180deg, rgb(247 255 251 / 98%), #fff 100%);
  }

  .record-item__header {
    display: flex;
    align-items: flex-start;
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
    line-height: 1.8;
    color: #5d6b86;
    word-break: break-word;
  }

  .record-summary-grid {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 12px;
  }

  .summary-chip {
    padding: 12px 14px;
    border: 1px solid #e8eef6;
    border-radius: 14px;
    background: rgb(255 255 255 / 85%);
  }

  .summary-chip__label {
    font-size: 12px;
    font-weight: 600;
    color: #6a7b95;
  }

  .summary-chip__value {
    margin-top: 8px;
    font-size: 14px;
    font-weight: 600;
    line-height: 1.7;
    color: #18233a;
    word-break: break-word;
  }

  .record-note {
    padding: 14px 16px;
    border-radius: 14px;
    background: rgb(15 23 42 / 3%);
  }

  .record-note__label {
    font-size: 12px;
    font-weight: 600;
    color: #6a7b95;
  }

  .record-note__value {
    margin-top: 8px;
    font-size: 14px;
    line-height: 1.8;
    color: #18233a;
    word-break: break-word;
  }

  .link-summary-card {
    padding: 14px 16px;
    border: 1px solid #e5edf6;
    border-radius: 16px;
    background: #fff;
  }

  .link-summary-card--none_required {
    background: linear-gradient(180deg, rgb(236 253 245 / 85%), #fff 100%);
    border-color: #b7e4d7;
  }

  .link-summary-card--not_created {
    background: linear-gradient(180deg, rgb(255 237 213 / 85%), #fff 100%);
    border-color: #fdba74;
  }

  .link-summary-card--pending {
    background: linear-gradient(180deg, rgb(254 249 195 / 85%), #fff 100%);
    border-color: #facc15;
  }

  .link-summary-card--completed {
    background: linear-gradient(180deg, rgb(220 252 231 / 85%), #fff 100%);
    border-color: #86efac;
  }

  .link-summary-card__title {
    font-size: 14px;
    font-weight: 700;
    color: #1d2f4f;
  }

  .link-summary-card__summary {
    margin-top: 8px;
    font-size: 13px;
    font-weight: 600;
    line-height: 1.7;
    color: #18233a;
  }

  .link-summary-card__detail {
    margin-top: 6px;
    font-size: 12px;
    line-height: 1.8;
    color: #5f7392;
    word-break: break-word;
  }

  .record-item__actions {
    display: flex;
    flex-wrap: wrap;
    gap: 14px;
    padding-top: 4px;
  }

  @media (width <= 1080px) {
    .inspection-overview-grid {
      grid-template-columns: 1fr;
    }
  }

  @media (width <= 900px) {
    .status-strip,
    .record-summary-grid {
      grid-template-columns: 1fr;
    }

    .record-item__header {
      flex-direction: column;
    }
  }
</style>
