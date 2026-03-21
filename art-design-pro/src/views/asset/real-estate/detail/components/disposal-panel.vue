<template>
  <div class="section-stack">
    <ElAlert
      class="section-alert"
      type="success"
      show-icon
      :closable="false"
      title="处置页签只承接已进入处置口径的结果，仍然跳转到统一处置模块执行正式流程。"
    />

    <ElCard class="section-card" shadow="never">
      <template #header>
        <div class="card-title">处置联动</div>
      </template>
      <div class="disposal-link-card">
        <div class="disposal-link-card__meta">
          <div class="disposal-link-card__title">跳转统一资产处置模块</div>
          <div class="disposal-link-card__desc">
            当前资产状态：{{ detailData.assetStatus || '-' }}，历史处置记录：{{ disposalRecords.length }} 条
          </div>
        </div>
        <ElButton data-testid="disposal-jump-button" type="primary" @click="$emit('jump-disposal')">进入资产处置</ElButton>
      </div>
    </ElCard>

    <ElCard class="section-card" shadow="never">
      <template #header>
        <div class="card-title">处置记录</div>
      </template>
      <div class="record-wrapper">
        <div v-if="disposalRecords.length" class="record-list">
          <div v-for="record in disposalRecords" :key="record.disposalId" class="record-item">
            <div class="record-item__title">{{ record.disposalNo || '-' }} / {{ record.disposalType || '-' }}</div>
            <div class="record-item__desc">
              处置状态：{{ record.disposalStatus || '-' }}，处置日期：{{ record.disposalDate || '-' }}
            </div>
            <div class="record-item__desc">
              确认人：{{ record.confirmedBy || '-' }}，确认时间：{{ record.confirmedTime || '-' }}
            </div>
          </div>
        </div>
        <ElEmpty v-else description="暂无处置记录" :image-size="68" />
      </div>
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  defineEmits<{
    'jump-disposal': []
  }>()

  defineProps<{
    detailData: Record<string, any>
    disposalRecords: Record<string, any>[]
  }>()
</script>
