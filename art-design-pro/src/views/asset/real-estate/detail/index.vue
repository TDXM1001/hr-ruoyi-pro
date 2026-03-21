<template>
  <div class="asset-real-estate-detail-page art-full-height flex flex-col gap-3 overflow-hidden p-3">
    <ElCard class="head-card" shadow="never">
      <div class="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
        <div class="flex flex-col gap-2">
          <ElButton link type="primary" icon="ri:arrow-left-line" @click="goBack">???????</ElButton>
          <div>
            <div class="page-title">???????</div>
            <div class="page-desc">{{ activeTabDesc }}</div>
          </div>
        </div>

        <div class="meta-tags flex flex-wrap items-center gap-2">
          <ElTag type="success" effect="light">????????</ElTag>
          <ElTag effect="light">?????{{ activeTabLabel }}</ElTag>
          <ElButton v-if="canEdit" type="primary" plain @click="handleEdit">??</ElButton>
        </div>
      </div>
    </ElCard>

    <ElCard class="summary-card" shadow="never">
      <div class="summary-grid">
        <div v-for="item in summaryItems" :key="item.label" class="summary-item">
          <div class="summary-item__label">{{ item.label }}</div>
          <div class="summary-item__value">{{ item.value }}</div>
        </div>
      </div>
    </ElCard>

    <ElCard class="tab-card" shadow="never">
      <ElTabs v-model="activeTab" class="detail-tabs" @tab-change="handleTabChange">
        <ElTabPane v-for="item in detailTabs" :key="item.name" :label="item.label" :name="item.name" />
      </ElTabs>
    </ElCard>

    <div class="detail-scroll-area" v-loading="loading">
      <template v-if="activeTab === 'overview'">
        <ElCard class="section-card" shadow="never">
          <template #header>
            <div class="card-title">????</div>
          </template>
          <ElDescriptions class="detail-descriptions detail-descriptions--3" :column="3" border>
            <ElDescriptionsItem label="????">{{ detailData.assetCode || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="????">{{ detailData.assetName || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="????">
              <DictTag :options="ast_asset_status" :value="detailData.assetStatus" />
            </ElDescriptionsItem>
            <ElDescriptionsItem label="????">
              <DictTag :options="ast_asset_source_type" :value="detailData.sourceType" />
            </ElDescriptionsItem>
            <ElDescriptionsItem label="????">
              <DictTag :options="ast_asset_acquire_type" :value="detailData.acquireType" />
            </ElDescriptionsItem>
            <ElDescriptionsItem label="????">{{ detailData.categoryName || '-' }}</ElDescriptionsItem>
          </ElDescriptions>
        </ElCard>

        <ElCard class="section-card" shadow="never">
          <template #header>
            <div class="card-title">????</div>
          </template>
          <ElDescriptions class="detail-descriptions detail-descriptions--3" :column="3" border>
            <ElDescriptionsItem label="????" :span="2">{{ detailData.ownershipCertNo || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="????">{{ detailData.landUseType || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="????(?)">{{ formatArea(detailData.buildingArea) }}</ElDescriptionsItem>
            <ElDescriptionsItem label="????" :span="2">{{ detailData.locationName || '-' }}</ElDescriptionsItem>
          </ElDescriptions>
        </ElCard>

        <ElCard class="section-card" shadow="never">
          <template #header>
            <div class="card-title">?????</div>
          </template>
          <ElDescriptions class="detail-descriptions detail-descriptions--4" :column="4" border>
            <ElDescriptionsItem label="????">{{ formatMoney(detailData.originalValue) }}</ElDescriptionsItem>
            <ElDescriptionsItem label="????">{{ detailData.specModel || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="???">{{ detailData.serialNo || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="????">{{ detailData.lastInventoryDate || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="????">{{ detailData.createTime || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="???">{{ detailData.createBy || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="????">{{ detailData.updateTime || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="???">{{ detailData.updateBy || '-' }}</ElDescriptionsItem>
          </ElDescriptions>
        </ElCard>

        <ElCard class="section-card" shadow="never">
          <template #header>
            <div class="card-title">????</div>
          </template>
          <div class="record-wrapper">
            <ElTimeline v-if="lifecycleData.changeLogs.length">
              <ElTimelineItem
                v-for="record in lifecycleData.changeLogs"
                :key="record.logId"
                :timestamp="record.operateTime || '-'"
                placement="top"
              >
                <div class="timeline-title">{{ getBizTypeLabel(record.bizType) }}</div>
                <div class="timeline-desc">{{ record.changeDesc || '??????' }}</div>
                <div class="timeline-meta">
                  ????{{ record.operateBy || '-' }}????{{ record.beforeStatus || '-' }} ->
                  {{ record.afterStatus || '-' }}
                </div>
              </ElTimelineItem>
            </ElTimeline>
            <ElEmpty v-else description="????????" :image-size="68" />
          </div>
        </ElCard>
      </template>

      <template v-else-if="activeTab === 'occupancy'">
        <ElAlert
          class="section-alert"
          type="info"
          show-icon
          :closable="false"
          title="?????????????????????????????????????????"
        />

        <ElCard class="section-card" shadow="never">
          <template #header>
            <div class="card-title">??????</div>
          </template>
          <ElDescriptions class="detail-descriptions detail-descriptions--2" :column="2" border>
            <ElDescriptionsItem label="????">{{ detailData.ownerDeptName || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="????">{{ detailData.useDeptName || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="???">{{ detailData.responsibleUserName || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="????">{{ detailData.locationName || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="????">{{ detailData.enableDate || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="????">{{ detailData.lastInventoryDate || '-' }}</ElDescriptionsItem>
          </ElDescriptions>
        </ElCard>

        <ElCard class="section-card" shadow="never">
          <template #header>
            <div class="card-title">????</div>
          </template>
          <div class="record-wrapper">
            <div v-if="occupancyRecords.length" class="record-list">
              <div
                v-for="record in occupancyRecords"
                :key="`${record.handoverOrderId}-${record.handoverItemId}`"
                class="record-item"
              >
                <div class="record-item__title">
                  {{ record.handoverNo || '-' }} / {{ getHandoverTypeLabel(record.handoverType) }}
                </div>
                <div class="record-item__desc">
                  ???{{ record.handoverDate || '-' }}????{{ record.beforeStatus || '-' }} ->
                  {{ record.afterStatus || '-' }}
                </div>
                <div class="record-item__desc">
                  ???{{ record.toDeptName || '-' }} / {{ record.toUserName || '-' }} /
                  {{ record.toLocationName || '-' }}
                </div>
              </div>
            </div>
            <ElEmpty v-else description="??????" :image-size="68" />
          </div>
        </ElCard>
      </template>

      <template v-else-if="activeTab === 'inspection'">
        <ElAlert
          class="section-alert"
          type="warning"
          show-icon
          :closable="false"
          title="????????????/??????????????????????????????????"
        />

        <ElCard class="section-card" shadow="never">
          <template #header>
            <div class="card-title">????</div>
          </template>
          <ElDescriptions class="detail-descriptions detail-descriptions--3" :column="3" border>
            <ElDescriptionsItem label="????">{{ detailData.lastInventoryDate || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="?????">{{ inspectionRecords.length }}</ElDescriptionsItem>
            <ElDescriptionsItem label="?????">{{ rectificationItems.length }}</ElDescriptionsItem>
          </ElDescriptions>
        </ElCard>

        <ElCard class="section-card" shadow="never">
          <template #header>
            <div class="card-title">????</div>
          </template>
          <div class="record-wrapper">
            <div v-if="inspectionRecords.length" class="record-list">
              <div v-for="record in inspectionRecords" :key="`${record.taskId}-${record.checkedTime || ''}`" class="record-item">
                <div class="record-item__title">{{ record.taskNo || '-' }} / {{ record.taskName || '-' }}</div>
                <div class="record-item__desc">
                  ???{{ getInventoryResultLabel(record.inventoryResult) }}??????{{ getFollowUpActionLabel(record.followUpAction) }}
                </div>
                <div class="record-item__desc">
                  ????{{ record.checkedBy || '-' }}??????{{ record.checkedTime || '-' }}
                </div>
              </div>
            </div>
            <ElEmpty v-else description="??????" :image-size="68" />
          </div>
        </ElCard>

        <ElCard class="section-card" shadow="never">
          <template #header>
            <div class="card-title">????</div>
          </template>
          <div class="record-wrapper">
            <div v-if="rectificationItems.length" class="record-list">
              <div v-for="item in rectificationItems" :key="item.key" class="record-item">
                <div class="record-item__title">{{ item.title }}</div>
                <div class="record-item__desc">?????{{ item.actionLabel }}</div>
                <div class="record-item__desc">???{{ item.description }}</div>
              </div>
            </div>
            <ElEmpty v-else description="?????????" :image-size="68" />
          </div>
        </ElCard>

        <ElCard class="section-card" shadow="never">
          <template #header>
            <div class="card-title">????</div>
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
                <div class="timeline-desc">{{ record.changeDesc || '??????' }}</div>
                <div class="timeline-meta">????{{ record.operateBy || '-' }}</div>
              </ElTimelineItem>
            </ElTimeline>
            <ElEmpty v-else description="??????" :image-size="68" />
          </div>
        </ElCard>
      </template>

      <template v-else>
        <ElAlert
          class="section-alert"
          type="success"
          show-icon
          :closable="false"
          title="????????????????????????????????????????????????"
        />

        <ElCard class="section-card" shadow="never">
          <template #header>
            <div class="card-title">????</div>
          </template>
          <div class="disposal-link-card">
            <div class="disposal-link-card__meta">
              <div class="disposal-link-card__title">?????????</div>
              <div class="disposal-link-card__desc">
                ?????{{ detailData.assetStatus || '-' }}?????????{{ disposalRecords.length }} ?
              </div>
            </div>
            <ElButton data-testid="disposal-jump-button" type="primary" @click="goToDisposalModule">??????</ElButton>
          </div>
        </ElCard>

        <ElCard class="section-card" shadow="never">
          <template #header>
            <div class="card-title">????</div>
          </template>
          <ElDescriptions class="detail-descriptions detail-descriptions--3" :column="3" border>
            <ElDescriptionsItem label="??????">{{ preferredDisposalTab === 'record' ? '????' : '??????' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="??????">{{ latestDisposalRecord?.disposalDate || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="?????">{{ latestDisposalRecord?.confirmedBy || '-' }}</ElDescriptionsItem>
          </ElDescriptions>
        </ElCard>

        <ElCard class="section-card" shadow="never">
          <template #header>
            <div class="card-title">????</div>
          </template>
          <div class="record-wrapper">
            <div v-if="disposalRecords.length" class="record-list">
              <div v-for="record in disposalRecords" :key="record.disposalId" class="record-item">
                <div class="record-item__title">{{ record.disposalNo || '-' }} / {{ record.disposalType || '-' }}</div>
                <div class="record-item__desc">
                  ???{{ record.disposalStatus || '-' }}??????{{ record.disposalDate || '-' }}
                </div>
                <div class="record-item__desc">
                  ????{{ record.confirmedBy || '-' }}??????{{ record.confirmedTime || '-' }}
                </div>
              </div>
            </div>
            <ElEmpty v-else description="??????" :image-size="68" />
          </div>
        </ElCard>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
  import type { TabPaneName } from 'element-plus'
  import { ElMessage } from 'element-plus'
  import { getRealEstateDetail, getRealEstateLifecycle } from '@/api/asset/real-estate'
  import { useDict } from '@/utils/dict'
  import { useUserStore } from '@/store/modules/user'
  import DictTag from '@/components/DictTag/index.vue'

  defineOptions({ name: 'AssetRealEstateDetailPage' })

  type DetailTabName = 'overview' | 'occupancy' | 'inspection' | 'disposal'

  const route = useRoute()
  const router = useRouter()
  const userStore = useUserStore()
  route.meta.activePath = '/asset/real-estate'

  const detailTabs: Array<{ name: DetailTabName; label: string; desc: string }> = [
    {
      name: 'overview',
      label: '????',
      desc: '???????????????????????????????????????????????'
    },
    {
      name: 'occupancy',
      label: '????',
      desc: '???????????????????????????????????????????????'
    },
    {
      name: 'inspection',
      label: '????',
      desc: '??????????/??????????????????????????????????????'
    },
    {
      name: 'disposal',
      label: '????',
      desc: '?????????????????????????????????????????????'
    }
  ]

  const { ast_asset_status, ast_asset_source_type, ast_asset_acquire_type } = useDict(
    'ast_asset_status',
    'ast_asset_source_type',
    'ast_asset_acquire_type'
  )

  const loading = ref(false)
  const activeTab = ref<DetailTabName>('overview')
  const detailData = reactive<Record<string, any>>({})
  const lifecycleData = reactive({
    handoverRecords: [] as Record<string, any>[],
    inventoryRecords: [] as Record<string, any>[],
    disposalRecords: [] as Record<string, any>[],
    changeLogs: [] as Record<string, any>[]
  })

  const assetId = computed(() => {
    const value = route.params.assetId
    return value ? Number(value) : undefined
  })

  const canEdit = computed(() => {
    return (
      userStore.permissions.includes('*:*:*') || userStore.permissions.includes('asset:realEstate:edit')
    )
  })

  const activeTabLabel = computed(() => {
    return detailTabs.find((item) => item.name === activeTab.value)?.label || '????'
  })

  const activeTabDesc = computed(() => {
    return detailTabs.find((item) => item.name === activeTab.value)?.desc || detailTabs[0].desc
  })

  const summaryItems = computed(() => {
    return [
      { label: '????', value: detailData.assetCode || '-' },
      { label: '????', value: getStatusLabel(detailData.assetStatus) },
      { label: '????', value: detailData.ownerDeptName || '-' },
      { label: '????', value: detailData.lastInventoryDate || '-' }
    ]
  })

  const occupancyRecords = computed(() => lifecycleData.handoverRecords)
  const inspectionRecords = computed(() => lifecycleData.inventoryRecords)
  const disposalRecords = computed(() => lifecycleData.disposalRecords)

  const rectificationItems = computed(() => {
    return inspectionRecords.value
      .filter((record) => record.followUpAction && record.followUpAction !== 'NONE')
      .map((record, index) => ({
        key: `${record.taskId || 'unknown'}-${index}`,
        title: `${record.taskName || record.taskNo || '????'}????`,
        actionLabel: getFollowUpActionLabel(record.followUpAction),
        description: `?????${getInventoryResultLabel(record.inventoryResult)}??????${record.checkedTime || '-'}`
      }))
  })

  const rectificationLogs = computed(() => {
    return lifecycleData.changeLogs.filter((record) => {
      const desc = String(record.changeDesc || '')
      return desc.includes('??') || record.bizType === 'LEDGER_UPDATE' || record.bizType === 'INVENTORY_RESULT'
    })
  })

  const preferredDisposalTab = computed<'pool' | 'record'>(() => {
    if (disposalRecords.value.length) {
      return 'record'
    }
    if (detailData.assetStatus === 'PENDING_DISPOSAL') {
      return 'pool'
    }
    return 'record'
  })

  const latestDisposalRecord = computed(() => {
    return [...disposalRecords.value].sort((left, right) => {
      const leftTime = new Date(left.confirmedTime || left.disposalDate || 0).getTime()
      const rightTime = new Date(right.confirmedTime || right.disposalDate || 0).getTime()
      return rightTime - leftTime
    })[0]
  })

  const toObjectData = <T,>(response: any): T => {
    if (response?.data !== undefined) {
      return response.data
    }
    return response
  }

  const toArrayData = <T,>(value: any): T[] => {
    return Array.isArray(value) ? value : []
  }

  const formatMoney = (value?: number | string) => {
    if (value === null || value === undefined || value === '') {
      return '-'
    }
    return Number(value).toLocaleString('zh-CN', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    })
  }

  const formatArea = (value?: number | string) => {
    if (value === null || value === undefined || value === '') {
      return '-'
    }
    return Number(value).toLocaleString('zh-CN', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    })
  }

  const resolveTabByPath = (path?: string): DetailTabName => {
    if (path?.endsWith('/occupancy')) {
      return 'occupancy'
    }
    if (path?.endsWith('/inspection')) {
      return 'inspection'
    }
    if (path?.endsWith('/disposal')) {
      return 'disposal'
    }
    return 'overview'
  }

  const buildTabPath = (tabName: DetailTabName) => {
    if (!assetId.value) {
      return '/asset/real-estate'
    }
    const basePath = `/asset/real-estate/detail/${assetId.value}`
    return tabName === 'overview' ? basePath : `${basePath}/${tabName}`
  }

  const syncActiveTabFromRoute = () => {
    activeTab.value = resolveTabByPath(route.path)
  }

  const goBack = () => {
    router.push('/asset/real-estate')
  }

  const handleEdit = () => {
    if (!assetId.value) {
      return
    }
    router.push(`/asset/real-estate/edit/${assetId.value}`)
  }

  const handleTabChange = (tabName: TabPaneName) => {
    const nextTab = String(tabName) as DetailTabName
    const nextPath = buildTabPath(nextTab)
    if (nextPath !== route.path) {
      router.push(nextPath)
    }
  }

  const goToDisposalModule = () => {
    if (!assetId.value) {
      return
    }
    router.push({
      path: '/asset/disposal',
      query: {
        tab: preferredDisposalTab.value,
        assetId: String(assetId.value),
        assetCode: detailData.assetCode || ''
      }
    })
  }

  const getStatusLabel = (status?: string) => {
    const option = ast_asset_status.value.find((item: any) => item.value === status)
    return option?.label || status || '-'
  }

  const getBizTypeLabel = (bizType?: string) => {
    const mapper: Record<string, string> = {
      LEDGER_CREATE: '????',
      LEDGER_UPDATE: '????',
      ASSIGN: '????',
      TRANSFER: '????',
      RETURN: '????',
      INVENTORY_CREATE: '????',
      INVENTORY_RESULT: '????',
      DISPOSAL_CONFIRM: '????'
    }
    return mapper[bizType || ''] || bizType || '?????'
  }

  const getHandoverTypeLabel = (handoverType?: string) => {
    const mapper: Record<string, string> = {
      ASSIGN: '??',
      TRANSFER: '??',
      RETURN: '??'
    }
    return mapper[handoverType || ''] || handoverType || '????'
  }

  const getInventoryResultLabel = (result?: string) => {
    const mapper: Record<string, string> = {
      NORMAL: '??',
      LOSS: '??',
      MISSING: '??',
      DAMAGED: '??',
      LOCATION_DIFF: '????',
      RESPONSIBLE_DIFF: '?????'
    }
    return mapper[result || ''] || result || '-'
  }

  const getFollowUpActionLabel = (action?: string) => {
    const mapper: Record<string, string> = {
      NONE: '?',
      UPDATE_LEDGER: '????',
      CREATE_DISPOSAL: '????'
    }
    return mapper[action || ''] || action || '-'
  }

  const loadDetail = async () => {
    if (!assetId.value) {
      ElMessage.error('????ID???????')
      goBack()
      return
    }

    loading.value = true
    try {
      const [detailResponse, lifecycleResponse] = await Promise.all([
        getRealEstateDetail(assetId.value),
        getRealEstateLifecycle(assetId.value)
      ])

      Object.assign(detailData, toObjectData<any>(detailResponse))
      const lifecycle = toObjectData<any>(lifecycleResponse) || {}
      lifecycleData.handoverRecords = toArrayData(lifecycle.handoverRecords)
      lifecycleData.inventoryRecords = toArrayData(lifecycle.inventoryRecords)
      lifecycleData.disposalRecords = toArrayData(lifecycle.disposalRecords)
      lifecycleData.changeLogs = toArrayData(lifecycle.changeLogs)
    } finally {
      loading.value = false
    }
  }

  watch(
    () => route.path,
    () => {
      syncActiveTabFromRoute()
    },
    { immediate: true }
  )

  watch(
    () => assetId.value,
    async (currentAssetId, previousAssetId) => {
      if (!currentAssetId) {
        return
      }
      if (currentAssetId !== previousAssetId || !detailData.assetId) {
        await loadDetail()
      }
    },
    { immediate: true }
  )
</script>

<style scoped lang="scss">
  .asset-real-estate-detail-page {
    --asset-accent: #1f7a8c;
    --asset-border: #e6ebf5;
    --asset-panel-bg: #fff;
    --asset-panel-muted: #f7f9fc;
    --asset-text-main: #18233a;
    --asset-text-secondary: #5d6b86;

    background:
      radial-gradient(circle at 0% 0%, rgb(31 122 140 / 8%), transparent 34%),
      radial-gradient(circle at 100% 0%, rgb(26 188 156 / 8%), transparent 36%),
      var(--art-main-bg-color);
  }

  .head-card,
  .summary-card,
  .tab-card,
  .section-card {
    border: 1px solid var(--asset-border);
    border-radius: 12px;
    background: var(--asset-panel-bg);
  }

  .head-card {
    background: linear-gradient(120deg, #fff 0%, #f7fcfd 100%);

    :deep(.el-card__body) {
      padding: 20px 24px;
    }
  }

  .summary-card :deep(.el-card__body),
  .tab-card :deep(.el-card__body) {
    padding: 16px 20px;
  }

  .page-title {
    font-size: 34px;
    font-weight: 700;
    color: var(--asset-text-main);
  }

  .page-desc {
    margin-top: 6px;
    font-size: 14px;
    color: var(--asset-text-secondary);
    line-height: 1.7;
    max-width: 880px;
  }

  .summary-grid {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 12px;
  }

  .summary-item {
    border: 1px solid #e9eef6;
    border-radius: 12px;
    padding: 14px 16px;
    background: linear-gradient(180deg, #fcfeff 0%, #f7fbfd 100%);
  }

  .summary-item__label {
    font-size: 12px;
    color: #7182a0;
    margin-bottom: 8px;
  }

  .summary-item__value {
    font-size: 18px;
    font-weight: 700;
    color: var(--asset-text-main);
  }

  .detail-tabs :deep(.el-tabs__header) {
    margin-bottom: 0;
  }

  .detail-scroll-area {
    flex: 1;
    min-height: 0;
    overflow-y: auto;
    padding-right: 2px;
  }

  .section-alert {
    margin-bottom: 12px;
  }

  .section-card {
    margin-bottom: 12px;

    &:last-child {
      margin-bottom: 0;
    }

    :deep(.el-card__header) {
      padding: 14px 16px;
      border-bottom: 1px solid #eaf0fb;
    }

    :deep(.el-card__body) {
      padding: 0;
    }
  }

  .card-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 15px;
    font-weight: 600;
    color: var(--asset-text-main);

    &::before {
      content: '';
      width: 4px;
      height: 14px;
      border-radius: 999px;
      background: var(--asset-accent);
    }
  }

  .detail-descriptions {
    width: 100%;

    :deep(.el-descriptions__body) {
      overflow-x: auto;
    }

    :deep(.el-descriptions__table) {
      width: 100%;
      table-layout: fixed;
    }

    :deep(.el-descriptions__cell) {
      padding: 12px 14px;
      font-size: 13px;
      line-height: 1.6;
    }

    :deep(.el-descriptions__label.el-descriptions__cell) {
      color: #5b6f8c;
      font-weight: 500;
      background: var(--asset-panel-muted);
    }
  }

  .record-wrapper {
    padding: 14px 16px;
  }

  .record-list {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  .record-item {
    border: 1px solid #e9edf7;
    border-radius: 10px;
    padding: 10px 12px;
    background: #fbfcff;
  }

  .record-item__title {
    font-size: 14px;
    font-weight: 600;
    color: #1d2f4f;
    margin-bottom: 4px;
  }

  .record-item__desc,
  .timeline-desc,
  .timeline-meta {
    font-size: 12px;
    color: #5f7392;
    line-height: 1.6;
  }

  .timeline-title {
    font-size: 13px;
    font-weight: 600;
    color: #1d2f4f;
  }

  .disposal-link-card {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    padding: 16px;
  }

  .disposal-link-card__title {
    font-size: 14px;
    font-weight: 600;
    color: #1d2f4f;
    margin-bottom: 4px;
  }

  .disposal-link-card__desc {
    font-size: 12px;
    color: #5f7392;
  }

  @media (max-width: 960px) {
    .summary-grid {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }

    .disposal-link-card {
      flex-direction: column;
      align-items: flex-start;
    }
  }

  @media (max-width: 640px) {
    .summary-grid {
      grid-template-columns: 1fr;
    }

    .page-title {
      font-size: 28px;
    }
  }
</style>
