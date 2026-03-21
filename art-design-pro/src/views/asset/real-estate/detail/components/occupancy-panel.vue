<template>
  <div class="section-stack">
    <ElAlert
      class="section-alert"
      type="info"
      show-icon
      :closable="false"
      title="占用页签只承载当前在用归属和历史交接轨迹，不在这里直接改资产状态。"
    />

    <ElCard class="section-card" shadow="never">
      <template #header>
        <div class="card-title">当前占用关系</div>
      </template>
      <ElDescriptions class="detail-descriptions detail-descriptions--2" :column="2" border>
        <ElDescriptionsItem label="权属部门">{{ detailData.ownerDeptName || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="使用部门">{{ detailData.useDeptName || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="责任人">{{ detailData.responsibleUserName || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="所在位置">{{ detailData.locationName || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="启用日期">{{ detailData.enableDate || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="最近巡检">{{ detailData.lastInventoryDate || '-' }}</ElDescriptionsItem>
      </ElDescriptions>
    </ElCard>

    <ElCard class="section-card" shadow="never">
      <template #header>
        <div class="card-title">交接轨迹</div>
      </template>
      <div class="record-wrapper">
        <div v-if="handoverRecords.length" class="record-list">
          <div
            v-for="record in handoverRecords"
            :key="`${record.handoverOrderId}-${record.handoverItemId}`"
            class="record-item"
          >
            <div class="record-item__title">
              {{ record.handoverNo || '-' }} / {{ getHandoverTypeLabel(record.handoverType) }}
            </div>
            <div class="record-item__desc">
              交接日期：{{ record.handoverDate || '-' }}，状态：{{ record.beforeStatus || '-' }} ->
              {{ record.afterStatus || '-' }}
            </div>
            <div class="record-item__desc">
              流向：{{ record.toDeptName || '-' }} / {{ record.toUserName || '-' }} /
              {{ record.toLocationName || '-' }}
            </div>
          </div>
        </div>
        <ElEmpty v-else description="暂无交接记录" :image-size="68" />
      </div>
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  defineProps<{
    detailData: Record<string, any>
    handoverRecords: Record<string, any>[]
    getHandoverTypeLabel: (handoverType?: string) => string
  }>()
</script>
