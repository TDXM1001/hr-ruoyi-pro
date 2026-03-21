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
          <div v-for="record in inspectionRecords" :key="record.itemId || record.taskId" class="record-item">
            <div class="record-item__title">{{ record.taskNo || '-' }} / {{ record.taskName || '-' }}</div>
            <div class="record-item__desc">
              巡检结果：{{ getInventoryResultLabel(record.inventoryResult) }}，后续动作：{{ getFollowUpActionLabel(record.followUpAction) }}
            </div>
            <div class="record-item__desc">
              登记人：{{ record.checkedBy || '-' }}，登记时间：{{ record.checkedTime || '-' }}
            </div>
            <div v-if="record.resultDesc" class="record-item__desc">问题描述：{{ record.resultDesc }}</div>
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
                v-if="canEdit && !record.followUpBizId && isRectifiableRecord(record)"
                :data-testid="`rectification-create-link-${record.taskId}`"
                link
                type="warning"
                @click="$emit('create-rectification', record)"
              >
                发起整改
              </ElButton>
              <ElButton
                v-if="record.followUpBizId"
                :data-testid="`rectification-edit-link-${record.followUpBizId}`"
                link
                type="success"
                @click="$emit('edit-rectification', record.followUpBizId)"
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
  import type { AssetInventoryRecord } from '@/api/asset/ledger'

  defineEmits<{
    'inspection-task': [taskId?: number]
    'create-rectification': [record: AssetInventoryRecord]
    'edit-rectification': [rectificationId?: number]
  }>()

  defineProps<{
    detailData: Record<string, any>
    inspectionRecords: AssetInventoryRecord[]
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

  const isRectifiableRecord = (record: AssetInventoryRecord) => {
    const inventoryResult = String(record.inventoryResult || '').toUpperCase()
    const followUpAction = String(record.followUpAction || '').toUpperCase()
    return followUpAction !== 'CREATE_DISPOSAL' && (inventoryResult !== 'NORMAL' || followUpAction !== 'NONE')
  }
</script>
