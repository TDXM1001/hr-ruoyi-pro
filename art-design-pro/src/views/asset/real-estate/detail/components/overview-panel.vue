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
            <div class="timeline-title">{{ getBizTypeLabel(record.bizType) }}</div>
            <div class="timeline-desc">{{ record.changeDesc || '暂无变更说明' }}</div>
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
  import type { AssetChangeLogRecord } from '@/api/asset/ledger'

  defineProps<{
    detailData: Record<string, any>
    changeLogs: AssetChangeLogRecord[]
    getStatusLabel: (status?: string) => string
    getSourceTypeLabel: (sourceType?: string) => string
    getAcquireTypeLabel: (acquireType?: string) => string
    getBizTypeLabel: (bizType?: string) => string
    formatArea: (value?: number | string) => string
  }>()
</script>
