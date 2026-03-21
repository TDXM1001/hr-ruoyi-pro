<template>
  <div class="section-stack">
    <ElAlert
      class="section-alert"
      type="warning"
      show-icon
      :closable="false"
      title="巡检页签负责承接任务事实，整改处理已拆到独立页签和整改单页，避免继续堆在一个内容块里。"
    />

    <ElCard class="section-card" shadow="never">
      <template #header>
        <div class="card-title">巡检摘要</div>
      </template>
      <ElDescriptions class="detail-descriptions detail-descriptions--3" :column="3" border>
        <ElDescriptionsItem label="最近巡检日期">{{ detailData.lastInventoryDate || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="巡检记录数">{{ inspectionRecords.length }}</ElDescriptionsItem>
        <ElDescriptionsItem label="待整改数">{{ pendingRectificationCount }}</ElDescriptionsItem>
      </ElDescriptions>
    </ElCard>

    <ElCard class="section-card" shadow="never">
      <template #header>
        <div class="card-title">巡检任务记录</div>
      </template>
      <div class="record-wrapper">
        <div v-if="inspectionRecords.length" class="record-list">
          <div
            v-for="record in inspectionRecords"
            :key="record.itemId || record.taskId"
            class="record-item"
            :class="`record-item--${record.rectificationLinkStatus.toLowerCase()}`"
          >
            <div class="record-item__title">{{ record.taskNo || '-' }} / {{ record.taskName || '-' }}</div>
            <div class="record-item__desc">
              巡检结果：{{ getInventoryResultLabel(record.inventoryResult) }}，后续动作：{{ getFollowUpActionLabel(record.followUpAction) }}
            </div>
            <div class="record-item__desc">
              登记人：{{ record.checkedBy || '-' }}，登记时间：{{ record.checkedTime || '-' }}
            </div>
            <div v-if="record.resultDesc" class="record-item__desc">问题描述：{{ record.resultDesc }}</div>

            <div class="link-summary-card" :class="`link-summary-card--${record.rectificationLinkStatus.toLowerCase()}`">
              <div class="link-summary-card__headline">
                <span class="link-summary-card__title">整改联动：{{ getRectificationLinkLabel(record.rectificationLinkStatus) }}</span>
                <ElTag :type="getRectificationLinkTagType(record.rectificationLinkStatus)" effect="light">
                  {{ getRectificationLinkLabel(record.rectificationLinkStatus) }}
                </ElTag>
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
  .link-summary-card {
    margin-top: 10px;
    padding: 12px 14px;
    border: 1px solid #e5edf6;
    border-radius: 12px;
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

  .link-summary-card__headline {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 8px;
  }

  .link-summary-card__title {
    font-size: 13px;
    font-weight: 700;
    color: #1d2f4f;
  }

  .link-summary-card__summary {
    font-size: 13px;
    font-weight: 600;
    line-height: 1.7;
    color: #18233a;
  }

  .link-summary-card__detail {
    margin-top: 6px;
    font-size: 12px;
    line-height: 1.7;
    color: #5f7392;
    word-break: break-word;
  }

  .record-item--not_created {
    border-color: #fed7aa;
    background: #fffaf5;
  }

  .record-item--pending {
    border-color: #fde68a;
    background: #fffdf4;
  }

  .record-item--completed {
    border-color: #a7f3d0;
    background: #f7fffb;
  }
</style>
