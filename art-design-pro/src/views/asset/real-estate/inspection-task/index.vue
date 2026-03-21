<template>
  <div class="asset-real-estate-inspection-task p-3" data-testid="inspection-task-reading-page">
    <ElCard class="hero-card" shadow="never">
      <div class="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
        <div class="flex flex-col gap-2">
          <ElButton link type="primary" icon="ri:arrow-left-line" @click="goBack">返回巡检页签</ElButton>
          <div>
            <div class="page-title">巡检任务明细</div>
            <div class="page-desc">面向资产管理者查看当前不动产在指定巡检任务下的登记事实、异常判断和整改入口。</div>
          </div>
        </div>
        <div class="header-tags flex flex-wrap gap-2">
          <ElTag type="success" effect="light">{{ taskMeta.taskNo || '未识别任务' }}</ElTag>
          <ElTag effect="light">{{ detailData.assetCode || '-' }}</ElTag>
        </div>
      </div>
    </ElCard>

    <div class="inspection-task-layout" data-testid="inspection-task-reading-layout">
      <div class="inspection-task-main">
        <ElCard class="section-card" shadow="never">
          <template #header>
            <div class="card-title">任务摘要</div>
          </template>

          <ElDescriptions class="detail-descriptions" :column="3" border>
            <ElDescriptionsItem label="任务名称">{{ taskMeta.taskName || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="计划巡检日期">{{ taskMeta.plannedDate || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="任务状态">{{ taskMeta.taskStatus || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="资产编码">{{ detailData.assetCode || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="资产名称">{{ detailData.assetName || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="最近巡检日期">{{ detailData.lastInventoryDate || '-' }}</ElDescriptionsItem>
          </ElDescriptions>
        </ElCard>

        <ElCard class="section-card" shadow="never" v-loading="loading">
          <template #header>
            <div class="card-title">当前资产巡检结果</div>
          </template>

          <div class="section-body">
            <template v-if="taskAssetRecord">
              <div class="inspection-result-grid">
                <div class="result-chip">
                  <div class="result-chip__label">巡检结果</div>
                  <div class="result-chip__value">
                    {{ getInventoryResultLabel(taskAssetRecord.inventoryResult) }}
                  </div>
                </div>
                <div class="result-chip">
                  <div class="result-chip__label">后续动作</div>
                  <div class="result-chip__value">
                    {{ getFollowUpActionLabel(taskAssetRecord.followUpAction) }}
                  </div>
                </div>
                <div class="result-chip">
                  <div class="result-chip__label">处理状态</div>
                  <div class="result-chip__value">
                    {{ getProcessStatusLabel(taskAssetRecord.processStatus) }}
                  </div>
                </div>
              </div>

              <ElDescriptions class="detail-descriptions" :column="2" border>
                <ElDescriptionsItem label="登记时间">{{ taskAssetRecord.checkedTime || '-' }}</ElDescriptionsItem>
                <ElDescriptionsItem label="登记人">{{ taskAssetRecord.checkedBy || '-' }}</ElDescriptionsItem>
                <ElDescriptionsItem label="位置">{{ taskAssetRecord.locationName || '-' }}</ElDescriptionsItem>
                <ElDescriptionsItem label="整改联动">
                  {{
                    taskAssetRecord.followUpBizId
                      ? '已挂接整改单'
                      : shouldShowCreateRectification
                        ? '待发起整改'
                        : '无需整改'
                  }}
                </ElDescriptionsItem>
              </ElDescriptions>

              <div v-if="taskAssetRecord.resultDesc" class="inspection-note">
                <div class="inspection-note__label">异常说明</div>
                <div class="inspection-note__value">{{ taskAssetRecord.resultDesc }}</div>
              </div>

              <div class="action-bar">
                <ElButton
                  v-if="canEdit && shouldShowCreateRectification"
                  data-testid="inspection-task-create-rectification"
                  type="warning"
                  @click="goToCreateRectification"
                >
                  发起整改
                </ElButton>
                <ElButton
                  v-if="taskAssetRecord.followUpBizId"
                  data-testid="inspection-task-edit-rectification"
                  type="primary"
                  plain
                  @click="goToEditRectification(taskAssetRecord.followUpBizId)"
                >
                  查看整改单
                </ElButton>
              </div>
            </template>

            <ElEmpty v-else description="当前资产在该巡检任务下暂无登记结果" :image-size="72" />
          </div>
        </ElCard>
      </div>

      <div class="inspection-task-side">
        <ElCard class="section-card" shadow="never">
          <template #header>
            <div class="card-title">处理建议</div>
          </template>

          <div class="guide-panel">
            <div class="guide-panel__headline">
              {{
                shouldShowCreateRectification
                  ? '这条巡检记录建议立即发起整改'
                  : taskAssetRecord?.followUpBizId
                    ? '当前记录已进入整改闭环'
                    : '当前记录以事实查看为主'
              }}
            </div>
            <div class="guide-panel__line">先确认巡检结果与异常说明，再决定是否发起整改。</div>
            <div class="guide-panel__line">若已挂接整改单，后续维护动作转到整改单页处理。</div>
            <div class="guide-panel__line">返回详情壳后会自动落在巡检页签，方便继续浏览其他记录。</div>
          </div>
        </ElCard>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ElMessage } from 'element-plus'
  import { getInventoryTask, listInventoryTaskAssets } from '@/api/asset/inventory'
  import { getRealEstateDetail } from '@/api/asset/real-estate'
  import { useUserStore } from '@/store/modules/user'
  import { persistRealEstateDetailTab } from '../detail/tab-state'

  defineOptions({ name: 'AssetRealEstateInspectionTaskPage' })

  type TaskAssetRecord = {
    itemId?: number
    assetId?: number
    assetCode?: string
    assetName?: string
    locationName?: string
    inventoryResult?: string
    followUpAction?: string
    processStatus?: string
    followUpBizId?: number
    checkedBy?: string
    checkedTime?: string
    resultDesc?: string
  }

  const route = useRoute()
  const router = useRouter()
  const userStore = useUserStore()
  route.meta.activePath = '/asset/real-estate'

  const loading = ref(false)
  const detailData = reactive<Record<string, any>>({})
  const taskMeta = reactive<Record<string, any>>({})
  const taskAssetRecord = ref<TaskAssetRecord>()

  const assetId = computed(() => Number(route.params.assetId))
  const taskId = computed(() => Number(route.params.taskId))

  const canEdit = computed(() => {
    return (
      userStore.permissions.includes('*:*:*') ||
      userStore.permissions.includes('asset:realEstate:edit')
    )
  })

  const shouldShowCreateRectification = computed(() => {
    if (!taskAssetRecord.value) {
      return false
    }
    if (taskAssetRecord.value.followUpBizId) {
      return false
    }
    const inventoryResult = String(taskAssetRecord.value.inventoryResult || '').toUpperCase()
    const followUpAction = String(taskAssetRecord.value.followUpAction || '').toUpperCase()
    return followUpAction !== 'CREATE_DISPOSAL' && (inventoryResult !== 'NORMAL' || followUpAction !== 'NONE')
  })

  const toObjectData = <T,>(response: any): T => {
    if (response?.data !== undefined) {
      return response.data
    }
    return response
  }

  const toRows = <T,>(response: any): T[] => {
    if (Array.isArray(response?.rows)) {
      return response.rows
    }
    if (Array.isArray(response?.data?.rows)) {
      return response.data.rows
    }
    return []
  }

  const getInventoryResultLabel = (result?: string) => {
    const mapper: Record<string, string> = {
      NORMAL: '正常',
      LOSS: '盘亏',
      MISSING: '缺失',
      DAMAGED: '损坏',
      LOCATION_DIFF: '位置不符',
      RESPONSIBLE_DIFF: '责任人不符'
    }
    return mapper[String(result || '').toUpperCase()] || result || '-'
  }

  const getFollowUpActionLabel = (action?: string) => {
    const mapper: Record<string, string> = {
      NONE: '无',
      UPDATE_LEDGER: '修正台账',
      CREATE_DISPOSAL: '发起处置'
    }
    return mapper[String(action || '').toUpperCase()] || action || '-'
  }

  const getProcessStatusLabel = (status?: string) => {
    const mapper: Record<string, string> = {
      PENDING: '待处理',
      PROCESSED: '已处理'
    }
    return mapper[String(status || '').toUpperCase()] || status || '-'
  }

  const goBack = () => {
    persistRealEstateDetailTab(assetId.value, 'inspection')
    router.push(`/asset/real-estate/detail/${assetId.value}`)
  }

  const goToCreateRectification = () => {
    if (!assetId.value || !taskId.value) {
      return
    }
    persistRealEstateDetailTab(assetId.value, 'rectification')
    router.push({
      path: `/asset/real-estate/detail/${assetId.value}/rectification/create`,
      query: {
        taskId: String(taskId.value)
      }
    })
  }

  const goToEditRectification = (rectificationId?: number) => {
    if (!assetId.value || !rectificationId) {
      return
    }
    persistRealEstateDetailTab(assetId.value, 'rectification')
    router.push(`/asset/real-estate/detail/${assetId.value}/rectification/edit/${rectificationId}`)
  }

  const loadPage = async () => {
    if (!assetId.value || Number.isNaN(assetId.value) || !taskId.value || Number.isNaN(taskId.value)) {
      ElMessage.error('巡检任务路由参数无效')
      goBack()
      return
    }

    loading.value = true
    try {
      const [detailResponse, taskResponse, taskAssetResponse] = await Promise.all([
        getRealEstateDetail(assetId.value),
        getInventoryTask(taskId.value),
        listInventoryTaskAssets(taskId.value, {
          assetId: assetId.value,
          pageNum: 1,
          pageSize: 10
        })
      ])

      Object.assign(detailData, toObjectData<any>(detailResponse))
      Object.assign(taskMeta, toObjectData<any>(taskResponse))
      taskAssetRecord.value = toRows<TaskAssetRecord>(taskAssetResponse)[0]
    } finally {
      loading.value = false
    }
  }

  watch(
    () => [route.params.assetId, route.params.taskId],
    async () => {
      await loadPage()
    },
    { immediate: true }
  )
</script>

<style scoped lang="scss">
  .asset-real-estate-inspection-task {
    --asset-accent: #0f766e;
    --asset-border: #e5edf6;
    --asset-panel-bg: #fff;
    --asset-text-main: #18233a;
    --asset-text-secondary: #5d6b86;

    display: flex;
    flex-direction: column;
    gap: 12px;
    background:
      radial-gradient(circle at 0% 0%, rgb(15 118 110 / 8%), transparent 35%),
      radial-gradient(circle at 100% 0%, rgb(14 165 233 / 8%), transparent 38%),
      var(--art-main-bg-color);
  }

  .hero-card,
  .section-card {
    border: 1px solid var(--asset-border);
    border-radius: 12px;
    background: var(--asset-panel-bg);
  }

  .inspection-task-layout {
    display: grid;
    grid-template-columns: minmax(0, 1.5fr) minmax(320px, 0.9fr);
    gap: 12px;
  }

  .inspection-task-main,
  .inspection-task-side {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .page-title {
    font-size: 28px;
    font-weight: 700;
    color: var(--asset-text-main);
  }

  .page-desc {
    margin-top: 6px;
    font-size: 14px;
    line-height: 1.7;
    color: var(--asset-text-secondary);
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

  .section-body {
    padding: 16px;
  }

  .inspection-result-grid {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 12px;
    margin-bottom: 14px;
  }

  .result-chip {
    padding: 14px 16px;
    border: 1px solid #e6edf7;
    border-radius: 16px;
    background: linear-gradient(180deg, #fcfeff 0%, #f7fbfd 100%);
  }

  .result-chip__label {
    font-size: 12px;
    font-weight: 600;
    color: #6b7c96;
  }

  .result-chip__value {
    margin-top: 8px;
    font-size: 16px;
    font-weight: 700;
    line-height: 1.6;
    color: #18233a;
    word-break: break-word;
  }

  .inspection-note {
    margin-top: 14px;
    padding: 14px 16px;
    border-radius: 14px;
    background: linear-gradient(180deg, rgb(255 247 237 / 92%), #fff 100%);
    border: 1px solid #fed7aa;
  }

  .inspection-note__label {
    font-size: 12px;
    font-weight: 600;
    color: #9a5b1f;
  }

  .inspection-note__value {
    margin-top: 8px;
    font-size: 14px;
    line-height: 1.8;
    color: #7c4a1c;
    word-break: break-word;
  }

  .detail-descriptions :deep(.el-descriptions__body) {
    overflow-x: auto;
  }

  .detail-descriptions :deep(.el-descriptions__table) {
    min-width: 720px;
  }

  .guide-panel {
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding: 16px;
  }

  .guide-panel__headline {
    font-size: 18px;
    font-weight: 700;
    line-height: 1.6;
    color: #18233a;
  }

  .guide-panel__line {
    position: relative;
    padding-left: 14px;
    font-size: 13px;
    line-height: 1.8;
    color: #51627f;

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 10px;
      width: 6px;
      height: 6px;
      border-radius: 999px;
      background: var(--asset-accent);
    }
  }

  .action-bar {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    margin-top: 16px;
  }

  @media (width <= 1080px) {
    .inspection-task-layout {
      grid-template-columns: 1fr;
    }
  }

  @media (width <= 900px) {
    .inspection-result-grid {
      grid-template-columns: 1fr;
    }
  }
</style>
