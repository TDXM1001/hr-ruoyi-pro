<template>
  <div class="asset-ledger-detail-page art-full-height flex flex-col gap-3 overflow-hidden p-3">
    <ElCard class="head-card" shadow="never">
      <div class="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
        <div class="flex flex-col gap-2">
          <ElButton link type="primary" icon="ri:arrow-left-line" @click="goBack"
            >返回资产台账</ElButton
          >
          <div>
            <div class="page-title">资产台账详情</div>
            <div class="page-desc">
              当前详情页重点展示台账主数据、归属使用信息和财务字段，后续交接、盘点和处置闭环会继续挂接在这里。
            </div>
          </div>
        </div>

        <div class="meta-tags flex flex-wrap items-center gap-2">
          <ElTag type="success" effect="light">资产类型：固定资产（一期）</ElTag>
          <ElButton v-if="canEdit" type="primary" plain @click="handleEdit">编辑</ElButton>
        </div>
      </div>
    </ElCard>

    <div class="detail-scroll-area" v-loading="loading">
      <ElCard class="section-card section-card--overview" shadow="never">
        <template #header>
          <div class="card-title">状态概览</div>
        </template>
        <ElDescriptions class="detail-descriptions detail-descriptions--3" :column="3" border>
          <ElDescriptionsItem label="资产编码">{{
            detailData.assetCode || '-'
          }}</ElDescriptionsItem>
          <ElDescriptionsItem label="资产名称">{{
            detailData.assetName || '-'
          }}</ElDescriptionsItem>
          <ElDescriptionsItem label="资产状态">
            <DictTag :options="ast_asset_status" :value="detailData.assetStatus" />
          </ElDescriptionsItem>
          <ElDescriptionsItem label="录入来源">
            <DictTag :options="ast_asset_source_type" :value="detailData.sourceType" />
          </ElDescriptionsItem>
          <ElDescriptionsItem label="取得方式">
            <DictTag :options="ast_asset_acquire_type" :value="detailData.acquireType" />
          </ElDescriptionsItem>
          <ElDescriptionsItem label="资产分类">{{
            detailData.categoryName || '-'
          }}</ElDescriptionsItem>
        </ElDescriptions>
      </ElCard>

      <ElCard class="section-card" shadow="never">
        <template #header>
          <div class="card-title">归属与使用</div>
        </template>
        <ElDescriptions class="detail-descriptions detail-descriptions--3" :column="3" border>
          <ElDescriptionsItem label="权属部门">{{
            detailData.ownerDeptName || '-'
          }}</ElDescriptionsItem>
          <ElDescriptionsItem label="使用部门">{{
            detailData.useDeptName || '-'
          }}</ElDescriptionsItem>
          <ElDescriptionsItem label="责任人">{{
            detailData.responsibleUserName || '-'
          }}</ElDescriptionsItem>
          <ElDescriptionsItem label="资产位置" :span="3">
            {{ detailData.locationName || '-' }}
          </ElDescriptionsItem>
        </ElDescriptions>
      </ElCard>

      <ElCard class="section-card" shadow="never">
        <template #header>
          <div class="card-title">财务与日期</div>
        </template>
        <ElDescriptions class="detail-descriptions detail-descriptions--4" :column="4" border>
          <ElDescriptionsItem label="资产原值">{{
            formatMoney(detailData.originalValue)
          }}</ElDescriptionsItem>
          <ElDescriptionsItem label="规格型号">{{
            detailData.specModel || '-'
          }}</ElDescriptionsItem>
          <ElDescriptionsItem label="序列号">{{ detailData.serialNo || '-' }}</ElDescriptionsItem>
          <ElDescriptionsItem label="最近盘点">{{
            detailData.lastInventoryDate || '-'
          }}</ElDescriptionsItem>
          <ElDescriptionsItem label="取得日期">{{
            detailData.acquisitionDate || '-'
          }}</ElDescriptionsItem>
          <ElDescriptionsItem label="启用日期">{{
            detailData.enableDate || '-'
          }}</ElDescriptionsItem>
          <ElDescriptionsItem label="建账时间">{{
            detailData.createTime || '-'
          }}</ElDescriptionsItem>
          <ElDescriptionsItem label="更新时间">{{
            detailData.updateTime || '-'
          }}</ElDescriptionsItem>
        </ElDescriptions>
      </ElCard>

      <ElCard class="section-card" shadow="never">
        <template #header>
          <div class="card-title">备注与留痕</div>
        </template>
        <ElDescriptions class="detail-descriptions detail-descriptions--2" :column="2" border>
          <ElDescriptionsItem label="创建人">{{ detailData.createBy || '-' }}</ElDescriptionsItem>
          <ElDescriptionsItem label="更新人">{{ detailData.updateBy || '-' }}</ElDescriptionsItem>
          <ElDescriptionsItem label="备注" :span="2">{{
            detailData.remark || '-'
          }}</ElDescriptionsItem>
        </ElDescriptions>
      </ElCard>

      <ElCard class="section-card" shadow="never">
        <template #header>
          <div class="card-title">生命周期轨迹</div>
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
              <div class="timeline-desc">{{ record.changeDesc || '无变更说明' }}</div>
              <div class="timeline-meta">
                操作人：{{ record.operateBy || '-' }}，状态：{{ record.beforeStatus || '-' }} ->
                {{ record.afterStatus || '-' }}
              </div>
            </ElTimelineItem>
          </ElTimeline>
          <ElEmpty v-else description="暂无生命周期轨迹" :image-size="68" />
        </div>
      </ElCard>

      <ElCard class="section-card" shadow="never">
        <template #header>
          <div class="card-title">交接记录</div>
        </template>
        <div class="record-wrapper">
          <div v-if="lifecycleData.handoverRecords.length" class="record-list">
            <div
              v-for="record in lifecycleData.handoverRecords"
              :key="`${record.handoverOrderId}-${record.handoverItemId}`"
              class="record-item"
            >
              <div class="record-item__title">
                {{ record.handoverNo || '-' }} · {{ getHandoverTypeLabel(record.handoverType) }}
              </div>
              <div class="record-item__desc">
                日期：{{ record.handoverDate || '-' }}，状态：{{ record.beforeStatus || '-' }} ->
                {{ record.afterStatus || '-' }}
              </div>
              <div class="record-item__desc">
                去向：{{ record.toDeptName || '-' }} / {{ record.toUserName || '-' }} /
                {{ record.toLocationName || '-' }}
              </div>
            </div>
          </div>
          <ElEmpty v-else description="暂无交接记录" :image-size="68" />
        </div>
      </ElCard>

      <ElCard class="section-card" shadow="never">
        <template #header>
          <div class="card-title">盘点记录</div>
        </template>
        <div class="record-wrapper">
          <div v-if="lifecycleData.inventoryRecords.length" class="record-list">
            <div
              v-for="record in lifecycleData.inventoryRecords"
              :key="`${record.taskId}-${record.checkedTime || ''}`"
              class="record-item"
            >
              <div class="record-item__title">{{ record.taskNo || '-' }} · {{ record.taskName || '-' }}</div>
              <div class="record-item__desc">
                结果：{{ getInventoryResultLabel(record.inventoryResult) }}，后续动作：
                {{ getFollowUpActionLabel(record.followUpAction) }}
              </div>
              <div class="record-item__desc">
                盘点人：{{ record.checkedBy || '-' }}，盘点时间：{{ record.checkedTime || '-' }}
              </div>
            </div>
          </div>
          <ElEmpty v-else description="暂无盘点记录" :image-size="68" />
        </div>
      </ElCard>

      <ElCard class="section-card" shadow="never">
        <template #header>
          <div class="card-title">处置记录</div>
        </template>
        <div class="record-wrapper">
          <div v-if="lifecycleData.disposalRecords.length" class="record-list">
            <div
              v-for="record in lifecycleData.disposalRecords"
              :key="record.disposalId"
              class="record-item"
            >
              <div class="record-item__title">{{ record.disposalNo || '-' }} · {{ record.disposalType || '-' }}</div>
              <div class="record-item__desc">
                状态：{{ record.disposalStatus || '-' }}，处置日期：{{ record.disposalDate || '-' }}
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
  </div>
</template>

<script setup lang="ts">
  import { ElMessage } from 'element-plus'
  import { getAssetLedger, getAssetLedgerLifecycle } from '@/api/asset/ledger'
  import { useDict } from '@/utils/dict'
  import { useUserStore } from '@/store/modules/user'
  import DictTag from '@/components/DictTag/index.vue'

  defineOptions({ name: 'AssetLedgerDetailPage' })

  const route = useRoute()
  const router = useRouter()
  const userStore = useUserStore()
  route.meta.activePath = '/asset/ledger'

  const { ast_asset_status, ast_asset_source_type, ast_asset_acquire_type } = useDict(
    'ast_asset_status',
    'ast_asset_source_type',
    'ast_asset_acquire_type'
  )

  const loading = ref(false)
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
      userStore.permissions.includes('*:*:*') || userStore.permissions.includes('asset:ledger:edit')
    )
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

  const goBack = () => {
    router.push('/asset/ledger')
  }

  const handleEdit = () => {
    if (!assetId.value) {
      return
    }
    router.push(`/asset/ledger/edit/${assetId.value}`)
  }

  const getBizTypeLabel = (bizType?: string) => {
    const mapper: Record<string, string> = {
      LEDGER_CREATE: '台账建账',
      LEDGER_UPDATE: '台账更新',
      ASSIGN: '资产领用',
      TRANSFER: '资产调拨',
      RETURN: '资产退还',
      INVENTORY_CREATE: '发起盘点',
      INVENTORY_RESULT: '盘点登记',
      DISPOSAL_CONFIRM: '处置确认'
    }
    return mapper[bizType || ''] || bizType || '未识别动作'
  }

  const getHandoverTypeLabel = (handoverType?: string) => {
    const mapper: Record<string, string> = {
      ASSIGN: '领用',
      TRANSFER: '调拨',
      RETURN: '退还'
    }
    return mapper[handoverType || ''] || handoverType || '未知类型'
  }

  const getInventoryResultLabel = (result?: string) => {
    const mapper: Record<string, string> = {
      NORMAL: '正常',
      LOSS: '盘亏',
      MISSING: '缺失',
      DAMAGED: '毁损',
      LOCATION_DIFF: '位置差异',
      RESPONSIBLE_DIFF: '责任人差异'
    }
    return mapper[result || ''] || result || '-'
  }

  const getFollowUpActionLabel = (action?: string) => {
    const mapper: Record<string, string> = {
      NONE: '无',
      UPDATE_LEDGER: '修正台账',
      CREATE_DISPOSAL: '发起处置'
    }
    return mapper[action || ''] || action || '-'
  }

  const loadDetail = async () => {
    if (!assetId.value) {
      ElMessage.error('缺少资产ID，无法加载详情')
      goBack()
      return
    }

    loading.value = true
    try {
      // 兼容旧接口字段与新生命周期聚合接口，避免一次切换引发详情页空白。
      const [detailResponse, lifecycleResponse] = await Promise.all([
        getAssetLedger(assetId.value),
        getAssetLedgerLifecycle(assetId.value)
      ])

      Object.assign(detailData, toObjectData<any>(detailResponse))
      const lifecycle = toObjectData<any>(lifecycleResponse) || {}
      if (lifecycle.ledger && typeof lifecycle.ledger === 'object') {
        Object.assign(detailData, lifecycle.ledger)
      }
      lifecycleData.handoverRecords = toArrayData(lifecycle.handoverRecords)
      lifecycleData.inventoryRecords = toArrayData(lifecycle.inventoryRecords)
      lifecycleData.disposalRecords = toArrayData(lifecycle.disposalRecords)
      lifecycleData.changeLogs = toArrayData(lifecycle.changeLogs)
    } finally {
      loading.value = false
    }
  }

  onMounted(() => {
    loadDetail()
  })
</script>

<style scoped lang="scss">
  .asset-ledger-detail-page {
    --asset-accent: #2f66ff;
    --asset-accent-soft: rgb(47 102 255 / 10%);
    --asset-border: #e6ebf5;
    --asset-panel-bg: #fff;
    --asset-panel-muted: #f7f9fc;
    --asset-text-main: #18233a;
    --asset-text-secondary: #5d6b86;

    background:
      radial-gradient(circle at 0% 0%, rgb(47 102 255 / 8%), transparent 34%),
      radial-gradient(circle at 100% 0%, rgb(32 201 151 / 8%), transparent 36%),
      var(--art-main-bg-color);

    .head-card {
      border: 1px solid var(--asset-border);
      border-radius: 12px;
      background: linear-gradient(120deg, #fff 0%, #f8fbff 100%);

      :deep(.el-card__body) {
        padding: 20px 24px;
      }
    }

    .page-title {
      font-size: 34px;
      font-weight: 700;
      color: var(--asset-text-main);
      line-height: 1.4;
      letter-spacing: 0.2px;
    }

    .page-desc {
      margin-top: 6px;
      font-size: 14px;
      color: var(--asset-text-secondary);
      line-height: 1.7;
      max-width: 820px;
    }

    .meta-tags {
      :deep(.el-tag) {
        border-radius: 999px;
        padding: 0 12px;
        height: 30px;
        font-weight: 500;
      }

      :deep(.el-button) {
        min-width: 88px;
      }
    }

    .detail-scroll-area {
      flex: 1;
      min-height: 0;
      overflow-y: auto;
      padding-right: 2px;

      &::-webkit-scrollbar {
        width: 8px;
      }

      &::-webkit-scrollbar-thumb {
        border-radius: 999px;
        background: rgb(124 143 181 / 35%);
      }

      &::-webkit-scrollbar-track {
        background: transparent;
      }
    }

    .section-card {
      margin-bottom: 12px;
      border: 1px solid var(--asset-border);
      border-radius: 12px;
      background: var(--asset-panel-bg);

      &:last-child {
        margin-bottom: 0;
      }

      :deep(.el-card__header) {
        padding: 14px 16px;
        border-bottom: 1px solid #eaf0fb;
        background: linear-gradient(180deg, rgb(247 250 255 / 90%) 0%, #fff 100%);
      }

      :deep(.el-card__body) {
        padding: 0;
      }
    }

    .section-card--overview {
      border-color: #d8e4ff;
      box-shadow: 0 8px 24px rgb(31 76 178 / 6%);
    }

    .card-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 15px;
      font-weight: 600;
      color: var(--asset-text-main);
      letter-spacing: 0.2px;

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

      :deep(.el-descriptions__content.el-descriptions__cell) {
        color: #1d2f4f;
        word-break: break-word;
      }
    }

    .detail-descriptions--4 {
      :deep(.el-descriptions__table) {
        min-width: 980px;
      }
    }

    .detail-descriptions--3 {
      :deep(.el-descriptions__table) {
        min-width: 760px;
      }
    }

    .detail-descriptions--2 {
      :deep(.el-descriptions__table) {
        min-width: 640px;
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

    .record-item__desc {
      font-size: 12px;
      color: #5f7392;
      line-height: 1.6;
    }

    .timeline-title {
      font-size: 13px;
      font-weight: 600;
      color: #1d2f4f;
    }

    .timeline-desc {
      margin-top: 2px;
      font-size: 12px;
      color: #4f6487;
      line-height: 1.5;
    }

    .timeline-meta {
      margin-top: 2px;
      font-size: 12px;
      color: #7a8faa;
      line-height: 1.5;
    }
  }

  @media (max-width: 1200px) {
    .asset-ledger-detail-page {
      .page-title {
        font-size: 28px;
      }
    }
  }

  @media (max-width: 992px) {
    .asset-ledger-detail-page {
      .head-card {
        :deep(.el-card__body) {
          padding: 16px 18px;
        }
      }

      .detail-scroll-area {
        padding-right: 0;
      }

      .section-card {
        :deep(.el-card__header) {
          padding: 12px 14px;
        }
      }
    }
  }

  @media (max-width: 768px) {
    .asset-ledger-detail-page {
      padding: 8px;
      gap: 8px;

      .page-title {
        font-size: 24px;
      }

      .page-desc {
        font-size: 13px;
      }

      .card-title {
        font-size: 14px;
      }

      .detail-descriptions {
        :deep(.el-descriptions__cell) {
          padding: 10px 12px;
          font-size: 12px;
        }
      }

      .detail-descriptions--4 {
        :deep(.el-descriptions__table) {
          min-width: 760px;
        }
      }

      .detail-descriptions--3 {
        :deep(.el-descriptions__table) {
          min-width: 620px;
        }
      }
    }
  }
</style>
