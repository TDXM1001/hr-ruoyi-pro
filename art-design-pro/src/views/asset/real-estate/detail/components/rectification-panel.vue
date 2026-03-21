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
          >
            <div class="record-item__title">
              {{ record.rectificationNo || '待同步整改单号' }} / {{ record.taskNo || '-' }} / {{ record.taskName || '-' }}
            </div>
            <div class="record-item__desc">
              整改状态：{{ getRectificationStatusLabel(record.rectificationStatus) }}，问题类型：{{ record.issueType || '-' }}
            </div>
            <div class="record-item__desc">问题描述：{{ record.issueDesc || '-' }}</div>
            <div class="record-item__desc">
              责任人：{{ record.responsibleDeptName || '-' }} / {{ record.responsibleUserName || '-' }}，期限：{{ record.deadlineDate || '-' }}
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
                v-if="record.rectificationId && String(record.rectificationStatus || '').toUpperCase() === 'PENDING'"
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

  const getRectificationStatusLabel = (status?: string) => {
    const mapper: Record<string, string> = {
      PENDING: '待整改',
      COMPLETED: '已完成'
    }
    return mapper[String(status || '').toUpperCase()] || status || '-'
  }
</script>
