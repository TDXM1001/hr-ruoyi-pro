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

      <div class="disposal-summary-card">
        <div class="disposal-summary-card__header">
          <div>
            <div class="disposal-summary-card__label">当前闭环状态</div>
            <div class="disposal-summary-card__title">{{ disposalSummary.overallLabel }}</div>
          </div>
          <ElTag :type="disposalSummary.overallTagType" effect="light">
            {{ disposalSummary.overallLabel }}
          </ElTag>
        </div>

        <div class="disposal-summary-card__meta">
          <span class="disposal-summary-card__label">最近处置动作</span>
          <strong>{{ disposalSummary.latestActionLabel }}</strong>
          <span>{{ disposalSummary.latestActionTime || '-' }}</span>
        </div>

        <div class="disposal-responsibility-grid">
          <div class="disposal-responsibility-item">
            <span>当前责任归口</span>
            <strong>{{ disposalSummary.responsibilityOwnerLabel }}</strong>
          </div>
          <div class="disposal-responsibility-item">
            <span>责任动作</span>
            <strong>{{ disposalSummary.responsibilityActionLabel }}</strong>
          </div>
          <div class="disposal-responsibility-item">
            <span>最近责任人</span>
            <strong>{{ disposalSummary.latestActionOwner || '-' }}</strong>
          </div>
        </div>

        <div class="record-item__desc">{{ disposalSummary.latestActionDesc }}</div>
        <div class="record-item__desc record-item__desc--emphasis">{{ disposalSummary.responsibilityHint }}</div>
        <div class="record-item__desc">{{ disposalSummary.nextStep }}</div>
      </div>
    </ElCard>

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
        <div class="disposal-link-card__actions">
          <ElButton
            v-if="disposalSummary.showInitiateAction"
            data-testid="disposal-initiate-button"
            type="primary"
            @click="$emit('jump-disposal', 'start')"
          >
            {{ disposalSummary.initiateActionLabel }}
          </ElButton>
          <ElButton data-testid="disposal-jump-button" plain @click="$emit('jump-disposal', 'view')">
            进入资产处置
          </ElButton>
        </div>
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
  import type { AssetDisposalRecord } from '@/api/asset/ledger'
  import type { DisposalOverviewSummary } from './disposal-overview'

  defineEmits<{
    'jump-disposal': [intent?: 'start' | 'view']
  }>()

  defineProps<{
    detailData: Record<string, any>
    disposalRecords: AssetDisposalRecord[]
    disposalSummary: DisposalOverviewSummary
  }>()
</script>

<style scoped>
  .disposal-summary-card {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .disposal-summary-card__header,
  .disposal-summary-card__meta,
  .disposal-link-card__actions {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    flex-wrap: wrap;
  }

  .disposal-summary-card__label {
    font-size: 13px;
    color: var(--el-text-color-secondary);
  }

  .disposal-summary-card__title {
    margin-top: 4px;
    font-size: 20px;
    font-weight: 700;
    color: var(--el-text-color-primary);
  }

  .disposal-link-card__actions {
    justify-content: flex-end;
  }

  .disposal-responsibility-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(130px, 1fr));
    gap: 10px;
  }

  .disposal-responsibility-item {
    display: flex;
    flex-direction: column;
    gap: 6px;
    padding: 10px 12px;
    border: 1px solid #e7edf5;
    border-radius: 10px;
    background: #fafcff;

    span {
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }

    strong {
      font-size: 14px;
      color: var(--el-text-color-primary);
    }
  }

  .record-item__desc--emphasis {
    color: var(--el-color-primary);
  }
</style>
