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
          <div class="card-title">状态矩阵</div>
        </template>

        <div class="matrix-panel">
          <div
            v-for="rule in matrixRules"
            :key="rule.key"
            class="matrix-item"
            :class="rule.highlight ? 'matrix-item--highlight' : ''"
          >
            <div class="matrix-item__header">
              <div class="matrix-item__title">{{ rule.title }}</div>
              <ElTag :type="rule.tagType" effect="light">{{ rule.tagLabel }}</ElTag>
            </div>
            <div class="matrix-item__desc">{{ rule.desc }}</div>
            <div class="matrix-item__actions">可执行动作：{{ rule.actions }}</div>
          </div>
        </div>
      </ElCard>
    </div>

    <ElCard class="section-card" shadow="never">
      <template #header>
        <div class="card-title">占用历史记录</div>
      </template>

      <div class="history-toolbar">
        <div class="history-toolbar__filters">
          <ElButton
            data-testid="occupancy-filter-all"
            size="small"
            :type="statusFilter === 'ALL' ? 'primary' : 'default'"
            @click="statusFilter = 'ALL'"
          >
            全部
          </ElButton>
          <ElButton
            data-testid="occupancy-filter-active"
            size="small"
            :type="statusFilter === 'ACTIVE' ? 'primary' : 'default'"
            @click="statusFilter = 'ACTIVE'"
          >
            有效占用
          </ElButton>
          <ElButton
            data-testid="occupancy-filter-released"
            size="small"
            :type="statusFilter === 'RELEASED' ? 'primary' : 'default'"
            @click="statusFilter = 'RELEASED'"
          >
            已释放
          </ElButton>
        </div>

        <ElInput
          v-model="keyword"
          data-testid="occupancy-keyword-input"
          clearable
          placeholder="搜索占用单号/部门/责任人/位置/原因"
          class="history-toolbar__search"
        />
      </div>

      <div class="record-wrapper" data-testid="occupancy-history-list">
        <div v-if="filteredRecords.length" class="record-list">
          <div
            v-for="record in filteredRecords"
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

        <ElEmpty v-else description="当前筛选条件下暂无占用轨迹" :image-size="68" />
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

  const statusFilter = ref<'ALL' | 'ACTIVE' | 'RELEASED'>('ALL')
  const keyword = ref('')

  const activeRecord = computed(() => {
    return props.occupancyRecords.find(
      (record) => String(record.occupancyStatus || '').toUpperCase() === 'ACTIVE'
    )
  })

  const matrixRules = computed(() => {
    return [
      {
        key: 'empty',
        title: '无有效占用',
        tagLabel: activeRecord.value ? '待切换' : '当前状态',
        tagType: activeRecord.value ? 'info' : 'primary',
        desc: '当前资产没有有效占用关系，需要先登记归口、责任人与位置。',
        actions: '发起占用',
        highlight: !activeRecord.value
      },
      {
        key: 'active',
        title: '存在有效占用',
        tagLabel: activeRecord.value ? '当前状态' : '待触发',
        tagType: activeRecord.value ? 'success' : 'info',
        desc: '当前资产存在一条有效占用单，后续变更与释放都从当前有效单继续。',
        actions: '变更占用、释放占用',
        highlight: !!activeRecord.value
      },
      {
        key: 'released',
        title: '已释放历史',
        tagLabel: '历史状态',
        tagType: 'warning',
        desc: '已释放记录只保留轨迹，不允许直接对历史单再次执行变更或释放。',
        actions: '查看轨迹',
        highlight: false
      }
    ]
  })

  const filteredRecords = computed(() => {
    const normalizedKeyword = keyword.value.trim().toLowerCase()

    return props.occupancyRecords.filter((record) => {
      const matchesStatus =
        statusFilter.value === 'ALL' ||
        String(record.occupancyStatus || '').toUpperCase() === statusFilter.value

      if (!matchesStatus) {
        return false
      }

      if (!normalizedKeyword) {
        return true
      }

      const searchableText = [
        record.occupancyNo,
        record.useDeptName,
        record.responsibleUserName,
        record.locationName,
        record.changeReason,
        record.releaseReason
      ]
        .filter(Boolean)
        .join(' ')
        .toLowerCase()

      return searchableText.includes(normalizedKeyword)
    })
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
  .matrix-panel,
  .record-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
    padding: 16px;
  }

  .current-occupancy-card__header,
  .record-item__header,
  .matrix-item__header,
  .history-toolbar {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 16px;
  }

  .current-occupancy-card__title,
  .record-item__title,
  .matrix-item__title {
    font-size: 18px;
    font-weight: 700;
    line-height: 1.5;
    color: #18233a;
  }

  .current-occupancy-card__subtitle,
  .record-item__subtitle,
  .matrix-item__desc,
  .matrix-item__actions,
  .empty-occupancy-card__desc {
    font-size: 13px;
    line-height: 1.8;
    color: #5d6b86;
    word-break: break-word;
    white-space: normal;
  }

  .current-occupancy-card__tags,
  .record-item__tags,
  .current-occupancy-card__actions,
  .history-toolbar__filters {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
  }

  .current-occupancy-grid,
  .detail-card-grid,
  .record-detail-grid,
  .empty-occupancy-card__meta {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 12px;
  }

  .summary-card,
  .detail-card,
  .record-item,
  .matrix-item {
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
    grid-template-columns: repeat(2, minmax(0, 1fr));
    width: 100%;
  }

  .empty-occupancy-card__meta-item {
    padding: 14px 16px;
    border: 1px dashed #cdd8e8;
    border-radius: 14px;
    background: rgb(255 255 255 / 86%);
  }

  .matrix-item {
    padding: 14px 16px;
    background: linear-gradient(180deg, rgb(248 250 252 / 98%), #fff 100%);
  }

  .matrix-item--highlight {
    border-color: #99f6e4;
    background: linear-gradient(180deg, rgb(236 253 245 / 92%), #fff 100%);
  }

  .history-toolbar {
    padding: 16px 16px 0;
  }

  .history-toolbar__search {
    width: 320px;
    max-width: 100%;
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
    .record-item__header,
    .matrix-item__header,
    .history-toolbar {
      flex-direction: column;
      align-items: flex-start;
    }

    .history-toolbar__search {
      width: 100%;
    }
  }
</style>
