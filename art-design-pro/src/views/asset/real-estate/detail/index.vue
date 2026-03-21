<template>
  <div class="asset-real-estate-detail-page art-full-height flex flex-col gap-3 overflow-hidden p-3">
    <ElCard class="head-card" shadow="never">
      <div class="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
        <div class="flex flex-col gap-2">
          <ElButton link type="primary" icon="ri:arrow-left-line" @click="goBack">返回不动产档案</ElButton>
          <div>
            <div class="page-title">不动产资产详情壳</div>
            <div class="page-desc">{{ activeTabDesc }}</div>
          </div>
        </div>

        <div class="meta-tags flex flex-wrap items-center gap-2">
          <ElTag type="success" effect="light">不动产档案</ElTag>
          <ElTag effect="light">当前页签：{{ activeTabLabel }}</ElTag>
          <ElButton v-if="canEdit" type="primary" plain @click="handleEdit">编辑档案</ElButton>
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
      </template>

      <template v-else-if="activeTab === 'occupancy'">
        <ElAlert
          class="section-alert"
          type="info"
          show-icon
          :closable="false"
          title="占用页签仅承载当前在用归属和历史交接轨迹，不在这里直接改资产状态。"
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
      </template>

      <template v-else-if="activeTab === 'inspection'">
        <ElAlert
          class="section-alert"
          type="warning"
          show-icon
          :closable="false"
          title="巡检页签负责承接任务事实，整改处理已拆到独立页签和整改单页，避免继续堆在一个内容块里。"
        />

        <ElCard class="section-card" shadow="never">
          <template #header>
            <div class="card-title">巡检摘要</div>
          </template>
          <ElDescriptions class="detail-descriptions detail-descriptions--3" :column="3" border>
            <ElDescriptionsItem label="最近巡检日期">{{ detailData.lastInventoryDate || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="巡检记录数">{{ inspectionRecords.length }}</ElDescriptionsItem>
            <ElDescriptionsItem label="待整改数">{{ pendingRectificationCount }}</ElDescriptionsItem>
          </ElDescriptions>
        </ElCard>

        <ElCard class="section-card" shadow="never">
          <template #header>
            <div class="card-title">巡检任务记录</div>
          </template>
          <div class="record-wrapper">
            <div v-if="inspectionRecords.length" class="record-list">
              <div v-for="record in inspectionRecords" :key="record.itemId || record.taskId" class="record-item">
                <div class="record-item__title">{{ record.taskNo || '-' }} / {{ record.taskName || '-' }}</div>
                <div class="record-item__desc">
                  巡检结果：{{ getInventoryResultLabel(record.inventoryResult) }}，后续动作：{{ getFollowUpActionLabel(record.followUpAction) }}
                </div>
                <div class="record-item__desc">
                  登记人：{{ record.checkedBy || '-' }}，登记时间：{{ record.checkedTime || '-' }}
                </div>
                <div v-if="record.resultDesc" class="record-item__desc">问题描述：{{ record.resultDesc }}</div>
                <div class="record-item__actions">
                  <ElButton
                    :data-testid="`inspection-task-link-${record.taskId}`"
                    link
                    type="primary"
                    @click="goToInspectionTask(record.taskId)"
                  >
                    查看任务明细
                  </ElButton>
                  <ElButton
                    v-if="canEdit && !record.followUpBizId && isRectifiableRecord(record)"
                    :data-testid="`rectification-create-link-${record.taskId}`"
                    link
                    type="warning"
                    @click="goToCreateRectification(record)"
                  >
                    发起整改
                  </ElButton>
                  <ElButton
                    v-if="record.followUpBizId"
                    :data-testid="`rectification-edit-link-${record.followUpBizId}`"
                    link
                    type="success"
                    @click="goToEditRectification(record.followUpBizId)"
                  >
                    查看整改
                  </ElButton>
                </div>
              </div>
            </div>
            <ElEmpty v-else description="暂无巡检记录" :image-size="68" />
          </div>
        </ElCard>
      </template>

      <template v-else-if="activeTab === 'rectification'">
        <ElAlert
          class="section-alert"
          type="success"
          show-icon
          :closable="false"
          title="整改页签承接巡检异常后的登记与推进，当前阶段不做审批流，只沉淀责任、期限和完成状态。"
        />

        <ElCard class="section-card" shadow="never">
          <template #header>
            <div class="card-title">整改单列表</div>
          </template>
          <div class="record-wrapper">
            <div v-if="rectificationRecords.length" class="record-list">
              <div v-for="record in rectificationRecords" :key="record.rectificationId || record.inventoryItemId || record.taskId" class="record-item">
                <div class="record-item__title">
                  {{ record.rectificationNo || '待同步整改单' }} / {{ record.taskNo || '-' }} / {{ record.taskName || '-' }}
                </div>
                <div class="record-item__desc">
                  整改状态：{{ getRectificationStatusLabel(record.rectificationStatus) }}，问题类型：{{ record.issueType || '-' }}
                </div>
                <div class="record-item__desc">问题描述：{{ record.issueDesc || '-' }}</div>
                <div class="record-item__desc">
                  责任人：{{ record.responsibleDeptName || '-' }} / {{ record.responsibleUserName || '-' }}，期限：{{ record.deadlineDate || '-' }}
                </div>
                <div class="record-item__actions">
                  <ElButton
                    v-if="record.rectificationId"
                    link
                    type="primary"
                    @click="goToEditRectification(record.rectificationId)"
                  >
                    查看整改单
                  </ElButton>
                </div>
              </div>
            </div>
            <ElEmpty v-else description="暂无整改记录" :image-size="68" />
          </div>
        </ElCard>

        <ElCard class="section-card" shadow="never">
          <template #header>
            <div class="card-title">整改轨迹</div>
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
                <div class="timeline-desc">{{ record.changeDesc || '暂无整改说明' }}</div>
                <div class="timeline-meta">操作人：{{ record.operateBy || '-' }}</div>
              </ElTimelineItem>
            </ElTimeline>
            <ElEmpty v-else description="暂无整改轨迹" :image-size="68" />
          </div>
        </ElCard>
      </template>

      <template v-else>
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
            <ElButton data-testid="disposal-jump-button" type="primary" @click="goToDisposalModule">进入资产处置</ElButton>
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
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
  import type { TabPaneName } from 'element-plus'
  import { ElMessage } from 'element-plus'
  import type { AssetChangeLogRecord, AssetInventoryRecord, AssetRectificationRecord } from '@/api/asset/ledger'
  import { getRealEstateDetail, getRealEstateLifecycle } from '@/api/asset/real-estate'
  import { useDict } from '@/utils/dict'
  import { useUserStore } from '@/store/modules/user'
  import DictTag from '@/components/DictTag/index.vue'

  defineOptions({ name: 'AssetRealEstateDetailPage' })

  type DetailTabName = 'overview' | 'occupancy' | 'inspection' | 'rectification' | 'disposal'

  const route = useRoute()
  const router = useRouter()
  const userStore = useUserStore()
  route.meta.activePath = '/asset/real-estate'

  const detailTabs: Array<{ name: DetailTabName; label: string; desc: string }> = [
    {
      name: 'overview',
      label: '总览',
      desc: '总览页承接不动产的统一台账和权属视图，资产管理者先在这里确认当前状态和主档口径。'
    },
    {
      name: 'occupancy',
      label: '占用',
      desc: '占用页签只表达当前归属关系和历史交接轨迹，不直接承担流程流转。'
    },
    {
      name: 'inspection',
      label: '巡检',
      desc: '巡检页签聚焦任务事实、异常结果和任务级跳转，整改动作从这里发起。'
    },
    {
      name: 'rectification',
      label: '整改',
      desc: '整改页签沉淀责任、期限和完成状态，作为后续审批流的稳定挂载位。'
    },
    {
      name: 'disposal',
      label: '处置',
      desc: '处置页签只负责联动统一处置模块，避免不动产详情壳自己承载完整处置流程。'
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
    inventoryRecords: [] as AssetInventoryRecord[],
    rectificationOrders: [] as AssetRectificationRecord[],
    disposalRecords: [] as Record<string, any>[],
    changeLogs: [] as AssetChangeLogRecord[]
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
    return detailTabs.find((item) => item.name === activeTab.value)?.label || '总览'
  })

  const activeTabDesc = computed(() => {
    return detailTabs.find((item) => item.name === activeTab.value)?.desc || detailTabs[0].desc
  })

  const summaryItems = computed(() => {
    return [
      { label: '资产编码', value: detailData.assetCode || '-' },
      { label: '资产状态', value: getStatusLabel(detailData.assetStatus) },
      { label: '权属部门', value: detailData.ownerDeptName || '-' },
      { label: '最近巡检', value: detailData.lastInventoryDate || '-' }
    ]
  })

  const handoverRecords = computed(() => lifecycleData.handoverRecords)
  const inspectionRecords = computed(() => lifecycleData.inventoryRecords)
  const disposalRecords = computed(() => lifecycleData.disposalRecords)
  const changeLogs = computed(() => lifecycleData.changeLogs)

  const rectificationRecords = computed<AssetRectificationRecord[]>(() => {
    if (lifecycleData.rectificationOrders.length) {
      return lifecycleData.rectificationOrders
    }

    return inspectionRecords.value
      .filter((record) => !!record.followUpBizId)
      .map((record) => ({
        rectificationId: record.followUpBizId,
        rectificationNo: '',
        assetId: assetId.value,
        taskId: record.taskId,
        taskNo: record.taskNo,
        taskName: record.taskName,
        inventoryItemId: record.itemId,
        rectificationStatus: record.processStatus === 'PROCESSED' ? 'COMPLETED' : 'PENDING',
        issueType: getInventoryResultLabel(record.inventoryResult),
        issueDesc: record.resultDesc,
        deadlineDate: '',
        completedTime: ''
      }))
  })

  const pendingRectificationCount = computed(() => {
    return inspectionRecords.value.filter((record) => isRectifiableRecord(record) && !record.followUpBizId).length
  })

  const rectificationLogs = computed(() => {
    return lifecycleData.changeLogs.filter((record) => String(record.changeDesc || '').includes('整改'))
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

  const toObjectData = <T,>(response: any): T => {
    if (response?.data !== undefined) {
      return response.data
    }
    return response
  }

  const toArrayData = <T,>(value: any): T[] => {
    return Array.isArray(value) ? value : []
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
    if (path?.endsWith('/rectification')) {
      return 'rectification'
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

  const isRectifiableRecord = (record: AssetInventoryRecord) => {
    const inventoryResult = String(record.inventoryResult || '').toUpperCase()
    const followUpAction = String(record.followUpAction || '').toUpperCase()
    return followUpAction !== 'CREATE_DISPOSAL' && (inventoryResult !== 'NORMAL' || followUpAction !== 'NONE')
  }

  const goToInspectionTask = (taskId?: number) => {
    if (!assetId.value || !taskId) {
      return
    }
    router.push(`/asset/real-estate/detail/${assetId.value}/inspection-task/${taskId}`)
  }

  const goToCreateRectification = (record: AssetInventoryRecord) => {
    if (!assetId.value || !record.taskId) {
      return
    }
    router.push({
      path: `/asset/real-estate/detail/${assetId.value}/rectification/create`,
      query: {
        taskId: String(record.taskId)
      }
    })
  }

  const goToEditRectification = (rectificationId?: number) => {
    if (!assetId.value || !rectificationId) {
      return
    }
    router.push(`/asset/real-estate/detail/${assetId.value}/rectification/edit/${rectificationId}`)
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

  const getSourceTypeLabel = (sourceType?: string) => {
    const option = ast_asset_source_type.value.find((item: any) => item.value === sourceType)
    return option?.label || sourceType || '-'
  }

  const getAcquireTypeLabel = (acquireType?: string) => {
    const option = ast_asset_acquire_type.value.find((item: any) => item.value === acquireType)
    return option?.label || acquireType || '-'
  }

  const getBizTypeLabel = (bizType?: string) => {
    const mapper: Record<string, string> = {
      LEDGER_CREATE: '建档',
      LEDGER_UPDATE: '台账更新',
      ASSIGN: '领用',
      TRANSFER: '调拨',
      RETURN: '归还',
      INVENTORY_CREATE: '发起巡检',
      INVENTORY_RESULT: '登记巡检结果',
      DISPOSAL_CONFIRM: '确认处置'
    }
    return mapper[bizType || ''] || bizType || '业务动作'
  }

  const getHandoverTypeLabel = (handoverType?: string) => {
    const mapper: Record<string, string> = {
      ASSIGN: '领用',
      TRANSFER: '调拨',
      RETURN: '归还'
    }
    return mapper[handoverType || ''] || handoverType || '交接'
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

  const getRectificationStatusLabel = (status?: string) => {
    const mapper: Record<string, string> = {
      PENDING: '待整改',
      COMPLETED: '已完成'
    }
    return mapper[String(status || '').toUpperCase()] || status || '-'
  }

  const loadDetail = async () => {
    if (!assetId.value) {
      ElMessage.error('资产ID无效，无法打开不动产详情')
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
      lifecycleData.rectificationOrders = toArrayData(lifecycle.rectificationOrders)
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
    () => route.params.assetId,
    async (currentAssetId, previousAssetId) => {
      const parsedAssetId = currentAssetId ? Number(currentAssetId) : undefined
      const previousParsedAssetId = previousAssetId ? Number(previousAssetId) : undefined

      // 中文注释：首屏直达异步子路由时，route.params 可能晚于组件实例完成注入，直接监听原始参数可以避免漏掉首次加载。
      if (!parsedAssetId || Number.isNaN(parsedAssetId)) {
        return
      }

      if (parsedAssetId !== previousParsedAssetId || !detailData.assetId) {
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

  .record-item__actions {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    margin-top: 8px;
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
