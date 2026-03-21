<template>
  <div class="section-stack" data-testid="occupancy-reading-layout">
    <ElAlert
      class="section-alert"
      type="info"
      show-icon
      :closable="false"
      title="占用页签聚焦当前实际使用归口、责任人和释放轨迹，资产管理员可以在当前详情页内连续完成发起、变更和释放。"
    />

    <div class="occupancy-overview-grid">
      <ElCard class="section-card" shadow="never">
        <template #header>
          <div class="card-title">当前有效占用</div>
        </template>

        <div v-if="activeRecord" class="current-occupancy-card">
          <div class="current-occupancy-card__header">
            <div>
              <div class="current-occupancy-card__title">
                {{ activeRecord.occupancyNo || '待生成占用单号' }}
              </div>
              <div class="current-occupancy-card__subtitle">
                当前资产已被占用，后续变更和释放都从这条有效占用继续推进。
              </div>
            </div>

            <div class="current-occupancy-card__tags">
              <ElTag type="success" effect="dark">当前有效占用</ElTag>
              <ElTag effect="light">{{ getStatusLabel(activeRecord.occupancyStatus) }}</ElTag>
            </div>
          </div>

          <div class="current-occupancy-grid">
            <div class="summary-card">
              <div class="summary-card__label">使用部门</div>
              <div class="summary-card__value">{{ activeRecord.useDeptName || '-' }}</div>
            </div>
            <div class="summary-card">
              <div class="summary-card__label">责任人</div>
              <div class="summary-card__value">{{ activeRecord.responsibleUserName || '-' }}</div>
            </div>
            <div class="summary-card">
              <div class="summary-card__label">占用起始</div>
              <div class="summary-card__value">{{ activeRecord.startDate || '-' }}</div>
            </div>
          </div>

          <div class="detail-card-grid">
            <div class="detail-card detail-card--wide">
              <div class="detail-card__label">使用位置</div>
              <div class="detail-card__value">{{ activeRecord.locationName || '-' }}</div>
            </div>
            <div class="detail-card detail-card--wide">
              <div class="detail-card__label">发起/变更原因</div>
              <div class="detail-card__value">{{ activeRecord.changeReason || '-' }}</div>
            </div>
          </div>

          <div v-if="props.canEdit" class="current-occupancy-card__actions">
            <ElButton
              :data-testid="`occupancy-change-link-${activeRecord.occupancyId}`"
              link
              type="primary"
              @click="$emit('change-occupancy', activeRecord)"
            >
              变更占用
            </ElButton>
            <ElButton
              :data-testid="`occupancy-release-link-${activeRecord.occupancyId}`"
              link
              type="warning"
              @click="$emit('release-occupancy', activeRecord)"
            >
              释放占用
            </ElButton>
          </div>
        </div>

        <div v-else class="empty-occupancy-card">
          <div>
            <div class="empty-occupancy-card__title">暂无有效占用</div>
            <div class="empty-occupancy-card__desc">
              当前资产没有有效占用关系。可以先登记使用部门、责任人和实际位置，再进入后续变更和释放流程。
            </div>
          </div>

          <div class="empty-occupancy-card__meta">
            <div class="empty-occupancy-card__meta-item">
              <span>资产编码</span>
              <strong>{{ props.detailData.assetCode || '-' }}</strong>
            </div>
            <div class="empty-occupancy-card__meta-item">
              <span>权属部门</span>
              <strong>{{ props.detailData.ownerDeptName || '-' }}</strong>
            </div>
          </div>

          <ElButton
            v-if="props.canEdit"
            data-testid="occupancy-create-link"
            type="primary"
            plain
            @click="$emit('create-occupancy')"
          >
            发起占用
          </ElButton>
        </div>
      </ElCard>

      <ElCard class="section-card" shadow="never">
        <template #header>
          <div class="card-title">操作提示</div>
        </template>

        <div class="guide-panel">
          <div class="guide-panel__headline">
            {{ activeRecord ? '先确认当前占用是否仍然有效，再决定是变更还是释放。' : '当前没有有效占用，优先补齐使用归口后再继续流转。' }}
          </div>
          <div class="guide-panel__line">发起占用会同步回写资产主档的使用部门、责任人和位置。</div>
          <div class="guide-panel__line">变更占用会关闭旧占用，并生成一条新的有效占用单。</div>
          <div class="guide-panel__line">释放占用后，详情页会回到“暂无有效占用”的状态，但释放轨迹会完整留痕。</div>
        </div>
      </ElCard>
    </div>

    <ElCard class="section-card" shadow="never">
      <template #header>
        <div class="card-title">占用历史记录</div>
      </template>

      <div class="record-wrapper" data-testid="occupancy-history-list">
        <div v-if="props.occupancyRecords.length" class="record-list">
          <div
            v-for="record in props.occupancyRecords"
            :key="record.occupancyId || record.occupancyNo || record.startDate"
            class="record-item"
            :class="record.occupancyStatus === 'ACTIVE' ? 'record-item--active' : 'record-item--released'"
          >
            <div class="record-item__header">
              <div>
                <div class="record-item__title">
                  {{ record.occupancyNo || '待生成占用单号' }}
                </div>
                <div class="record-item__subtitle">
                  {{ record.useDeptName || '-' }} / {{ record.responsibleUserName || '-' }} /
                  {{ record.locationName || '-' }}
                </div>
              </div>

              <div class="record-item__tags">
                <ElTag :type="record.occupancyStatus === 'ACTIVE' ? 'success' : 'info'" effect="light">
                  {{ getStatusLabel(record.occupancyStatus) }}
                </ElTag>
                <ElTag v-if="record.occupancyStatus === 'RELEASED'" type="warning" effect="light">
                  已释放
                </ElTag>
              </div>
            </div>

            <div class="record-detail-grid">
              <div class="detail-card">
                <div class="detail-card__label">占用起始</div>
                <div class="detail-card__value">{{ record.startDate || '-' }}</div>
              </div>
              <div class="detail-card">
                <div class="detail-card__label">释放时间</div>
                <div class="detail-card__value">{{ record.endDate || '-' }}</div>
              </div>
              <div class="detail-card detail-card--wide">
                <div class="detail-card__label">发起/变更原因</div>
                <div class="detail-card__value">{{ record.changeReason || '-' }}</div>
              </div>
              <div class="detail-card detail-card--wide">
                <div class="detail-card__label">释放原因</div>
                <div class="detail-card__value">{{ record.releaseReason || '-' }}</div>
              </div>
            </div>
          </div>
        </div>

        <ElEmpty v-else description="暂无占用历史" :image-size="68" />
      </div>
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import type { AssetRealEstateOccupancyRecord } from '@/api/asset/real-estate'

  const props = defineProps<{
    detailData: Record<string, any>
    occupancyRecords: AssetRealEstateOccupancyRecord[]
    canEdit?: boolean
  }>()

  defineEmits<{
    'create-occupancy': []
    'change-occupancy': [record: AssetRealEstateOccupancyRecord]
    'release-occupancy': [record: AssetRealEstateOccupancyRecord]
  }>()

  const activeRecord = computed(() => {
    return props.occupancyRecords.find((record) => String(record.occupancyStatus || '').toUpperCase() === 'ACTIVE')
  })

  const getStatusLabel = (status?: string) => {
    const mapper: Record<string, string> = {
      ACTIVE: '有效占用',
      RELEASED: '已释放'
    }
    return mapper[String(status || '').toUpperCase()] || status || '-'
  }
</script>

<style scoped lang="scss">
  .occupancy-overview-grid {
    display: grid;
    grid-template-columns: minmax(0, 1.4fr) minmax(320px, 0.9fr);
    gap: 12px;
  }

  .current-occupancy-card,
  .empty-occupancy-card,
  .guide-panel,
  .record-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
    padding: 16px;
  }

  .current-occupancy-card__header,
  .record-item__header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 16px;
  }

  .current-occupancy-card__title,
  .record-item__title,
  .guide-panel__headline {
    font-size: 18px;
    font-weight: 700;
    line-height: 1.5;
    color: #18233a;
  }

  .current-occupancy-card__subtitle,
  .record-item__subtitle,
  .guide-panel__line,
  .empty-occupancy-card__desc {
    font-size: 13px;
    line-height: 1.8;
    color: #5d6b86;
    word-break: break-word;
    white-space: normal;
  }

  .current-occupancy-card__tags,
  .record-item__tags,
  .current-occupancy-card__actions {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
  }

  .current-occupancy-grid,
  .detail-card-grid,
  .record-detail-grid {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 12px;
  }

  .summary-card,
  .detail-card,
  .record-item {
    border: 1px solid #e7edf6;
    border-radius: 14px;
    background: rgb(255 255 255 / 90%);
  }

  .summary-card,
  .detail-card {
    padding: 14px 16px;
  }

  .summary-card__label,
  .detail-card__label,
  .empty-occupancy-card__meta-item span {
    font-size: 12px;
    font-weight: 600;
    color: #6f7f99;
  }

  .summary-card__value,
  .detail-card__value,
  .empty-occupancy-card__meta-item strong {
    margin-top: 10px;
    font-size: 14px;
    line-height: 1.8;
    color: #18233a;
    word-break: break-word;
    white-space: pre-wrap;
  }

  .detail-card--wide {
    grid-column: span 2;
  }

  .current-occupancy-card {
    background: linear-gradient(180deg, rgb(236 253 245 / 92%), #fff 100%);
  }

  .empty-occupancy-card {
    align-items: flex-start;
    background: linear-gradient(180deg, rgb(248 250 252 / 96%), #fff 100%);
  }

  .empty-occupancy-card__title {
    font-size: 18px;
    font-weight: 700;
    color: #18233a;
  }

  .empty-occupancy-card__meta {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 12px;
    width: 100%;
  }

  .empty-occupancy-card__meta-item {
    padding: 14px 16px;
    border: 1px dashed #cdd8e8;
    border-radius: 14px;
    background: rgb(255 255 255 / 86%);
  }

  .guide-panel__line {
    position: relative;
    padding-left: 14px;

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
    padding: 0;
  }

  .record-item {
    padding: 16px;
    box-shadow: 0 10px 24px rgb(15 23 42 / 4%);
  }

  .record-item--active {
    background: linear-gradient(180deg, rgb(236 253 245 / 88%), #fff 100%);
  }

  .record-item--released {
    background: linear-gradient(180deg, rgb(248 250 252 / 94%), #fff 100%);
  }

  @media (width <= 1080px) {
    .occupancy-overview-grid {
      grid-template-columns: 1fr;
    }
  }

  @media (width <= 900px) {
    .current-occupancy-grid,
    .detail-card-grid,
    .record-detail-grid,
    .empty-occupancy-card__meta {
      grid-template-columns: 1fr;
    }

    .detail-card--wide {
      grid-column: span 1;
    }

    .current-occupancy-card__header,
    .record-item__header {
      flex-direction: column;
      align-items: flex-start;
    }
  }
</style>
