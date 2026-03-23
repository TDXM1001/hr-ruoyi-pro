<template>
  <div class="section-stack" data-testid="disposal-reading-layout">
    <ElAlert
      class="section-alert"
      type="success"
      show-icon
      :closable="false"
      title="处置页签负责识别当前闭环状态和发起入口，正式流程仍由统一资产处置模块承接。"
    />

    <ElCard class="section-card" shadow="never">
      <template #header>
        <div class="card-title">处置闭环摘要</div>
      </template>
      <DisposalClosureCard
        :card="disposalClosureCard"
        testid-prefix="disposal"
      >
        <template #actions>
          <ElButton
            v-if="disposalClosureCard.showInitiateAction"
            data-testid="disposal-initiate-button"
            type="primary"
            @click="$emit('jump-disposal', 'start')"
          >
            {{ disposalClosureCard.initiateActionLabel }}
          </ElButton>
          <ElButton data-testid="disposal-jump-button" plain @click="$emit('jump-disposal', 'view')">
            进入资产处置
          </ElButton>
        </template>
      </DisposalClosureCard>
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
            <div
              :data-testid="`disposal-record-responsibility-${record.disposalId}`"
              class="record-item__desc record-item__desc--responsibility"
            >
              最近责任归口：{{ getRecordResponsibilityView(record).ownerLabel }} · 责任动作：{{
                getRecordResponsibilityView(record).actionLabel
              }} · 最近责任人：{{ getRecordResponsibilityView(record).latestOwner }}
            </div>
            <div class="record-item__desc record-item__desc--emphasis">
              {{ getRecordResponsibilityView(record).hint }}
            </div>
          </div>
        </div>
        <ElEmpty v-else description="暂无处置记录" :image-size="68" />
      </div>
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import type { AssetDisposalRecord } from '@/api/asset/ledger'
  import {
    buildDisposalResponsibilityView,
    resolveDisposalOverviewStage,
    type DisposalClosureCard as DisposalClosureCardView
  } from './disposal-overview'
  import DisposalClosureCard from './disposal-closure-card.vue'

  defineEmits<{
    'jump-disposal': [intent?: 'start' | 'view']
  }>()

  const props = defineProps<{
    detailData: Record<string, any>
    disposalRecords: AssetDisposalRecord[]
    disposalClosureCard: DisposalClosureCardView
  }>()

  const getRecordResponsibilityView = (record: AssetDisposalRecord) => {
    const stage = resolveDisposalOverviewStage([record], props.detailData?.assetStatus)
    return buildDisposalResponsibilityView(stage, record.confirmedBy)
  }
</script>

<style scoped>
  .record-item__desc--emphasis {
    color: var(--el-color-primary);
  }

  .record-item__desc--responsibility {
    color: var(--el-text-color-primary);
    font-weight: 500;
  }
</style>
