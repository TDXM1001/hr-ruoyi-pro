<template>
  <div class="asset-real-estate-detail-page p-3" data-testid="real-estate-detail-reading-page">
    <ElCard class="head-card" shadow="never">
      <div class="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
        <div class="flex flex-col gap-2">
          <ElButton link type="primary" icon="ri:arrow-left-line" @click="goBack"
            >返回不动产档案</ElButton
          >
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
        <ElTabPane
          v-for="item in detailTabs"
          :key="item.name"
          :label="item.label"
          :name="item.name"
        />
      </ElTabs>
    </ElCard>

    <div class="detail-content" v-loading="loading">
      <OverviewPanel
        v-if="activeTab === 'overview'"
        :detail-data="detailData"
        :change-logs="changeLogs"
        :get-status-label="getStatusLabel"
        :get-source-type-label="getSourceTypeLabel"
        :get-acquire-type-label="getAcquireTypeLabel"
        :get-biz-type-label="getBizTypeLabel"
        :format-area="formatArea"
      />

      <OccupancyPanel
        v-else-if="activeTab === 'occupancy'"
        :detail-data="detailData"
        :occupancy-records="occupancyRecords"
        :can-edit="canEdit"
        @create-occupancy="openCreateOccupancyDrawer"
        @change-occupancy="openChangeOccupancyDrawer"
        @release-occupancy="openReleaseOccupancyDrawer"
      />

      <InspectionPanel
        v-else-if="activeTab === 'inspection'"
        :detail-data="detailData"
        :inspection-records="inspectionLinkedRecords"
        :inspection-status-summary="inspectionStatusSummary"
        :pending-rectification-count="pendingRectificationCount"
        :can-edit="canEdit"
        @inspection-task="goToInspectionTask"
        @create-rectification="goToCreateRectification"
        @edit-rectification="goToEditRectification"
      />

      <RectificationPanel
        v-else-if="activeTab === 'rectification'"
        :rectification-records="rectificationRecords"
        :rectification-logs="rectificationLogs"
        :get-biz-type-label="getBizTypeLabel"
        :can-edit="canEdit"
        @edit-rectification="goToEditRectification"
        @complete-rectification="goToCompleteRectification"
        @submit-approval="handleSubmitApproval"
        @approve-approval="handleApproveApproval"
        @reject-approval="handleRejectApproval"
        @view-approval-records="handleViewApprovalRecords"
      />

      <DisposalPanel
        v-else
        :detail-data="detailData"
        :disposal-records="disposalRecords"
        @jump-disposal="goToDisposalModule"
      />
    </div>

    <ElDrawer
      v-model="occupancyDrawerVisible"
      :title="occupancyDrawerTitle"
      size="520px"
      destroy-on-close
      append-to-body
    >
      <div class="occupancy-drawer">
        <div class="occupancy-drawer__headline">{{ occupancyDrawerDesc }}</div>

        <ElForm
          ref="occupancyFormRef"
          :model="occupancyForm"
          :rules="occupancyFormRules"
          label-width="96px"
          class="occupancy-form"
        >
          <template v-if="occupancyDrawerMode !== 'release'">
            <ElFormItem label="使用部门" prop="useDeptId">
              <ElTreeSelect
                v-model="occupancyForm.useDeptId"
                :data="occupancyDeptOptions"
                :props="treeSelectProps"
                value-key="id"
                check-strictly
                filterable
                clearable
                class="w-full"
                placeholder="请选择使用部门"
                :render-after-expand="false"
              />
            </ElFormItem>

            <ElFormItem label="责任人" prop="responsibleUserId">
              <ElSelect
                v-model="occupancyForm.responsibleUserId"
                clearable
                filterable
                remote
                reserve-keyword
                class="w-full"
                placeholder="请输入责任人姓名搜索"
                :remote-method="loadOccupancyResponsibleUsers"
                :loading="occupancyResponsibleUserLoading"
              >
                <ElOption
                  v-for="item in occupancyResponsibleUserOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </ElSelect>
            </ElFormItem>

            <ElFormItem label="使用位置" prop="locationName">
              <ElInput
                v-model="occupancyForm.locationName"
                maxlength="255"
                placeholder="请输入实际使用位置"
              />
            </ElFormItem>

            <ElFormItem label="开始日期" prop="startDate">
              <ElDatePicker
                v-model="occupancyForm.startDate"
                type="date"
                value-format="YYYY-MM-DD"
                class="w-full"
                placeholder="请选择占用开始日期"
              />
            </ElFormItem>

            <ElFormItem label="原因说明" prop="changeReason">
              <ElInput
                v-model="occupancyForm.changeReason"
                type="textarea"
                :rows="4"
                maxlength="500"
                show-word-limit
                placeholder="请输入发起/变更原因"
              />
              <div class="occupancy-template-group">
                <span class="occupancy-template-group__label">原因模板</span>
                <div class="occupancy-template-group__items">
                  <button
                    v-for="(template, index) in occupancyChangeReasonTemplates"
                    :key="template"
                    :data-testid="`occupancy-change-template-${index}`"
                    type="button"
                    class="occupancy-template-chip"
                    @click="applyOccupancyReasonTemplate(template)"
                  >
                    {{ template }}
                  </button>
                </div>
              </div>
            </ElFormItem>
          </template>

          <template v-else>
            <ElAlert
              type="warning"
              show-icon
              :closable="false"
              title="释放后将清空当前占用快照，请确认这条资产已经结束实际使用。"
            />

            <div class="occupancy-drawer__snapshot">
              <div class="snapshot-item">
                <span>当前占用单</span>
                <strong>{{ currentOccupancyTarget?.occupancyNo || '-' }}</strong>
              </div>
              <div class="snapshot-item">
                <span>当前归口</span>
                <strong>{{ currentOccupancyTarget?.useDeptName || '-' }}</strong>
              </div>
            </div>

            <ElFormItem label="释放日期" prop="endDate">
              <ElDatePicker
                v-model="occupancyForm.endDate"
                type="date"
                value-format="YYYY-MM-DD"
                class="w-full"
                placeholder="请选择释放日期"
              />
            </ElFormItem>

            <ElFormItem label="释放原因" prop="releaseReason">
              <ElInput
                v-model="occupancyForm.releaseReason"
                type="textarea"
                :rows="4"
                maxlength="500"
                show-word-limit
                placeholder="请输入释放原因"
              />
              <div class="occupancy-template-group">
                <span class="occupancy-template-group__label">释放模板</span>
                <div class="occupancy-template-group__items">
                  <button
                    v-for="(template, index) in occupancyReleaseReasonTemplates"
                    :key="template"
                    :data-testid="`occupancy-release-template-${index}`"
                    type="button"
                    class="occupancy-template-chip"
                    @click="applyOccupancyReasonTemplate(template)"
                  >
                    {{ template }}
                  </button>
                </div>
              </div>
            </ElFormItem>
          </template>
        </ElForm>

        <div class="occupancy-drawer__footer">
          <ElButton @click="closeOccupancyDrawer">取消</ElButton>
          <ElButton type="primary" :loading="occupancySubmitting" @click="handleOccupancySubmit">
            {{ occupancyDrawerMode === 'release' ? '确认释放' : '确认提交' }}
          </ElButton>
        </div>
      </div>
    </ElDrawer>

    <ElDrawer
      v-model="approvalDrawerVisible"
      title="整改审批轨迹"
      size="420px"
      destroy-on-close
    >
      <div class="approval-drawer">
        <div class="approval-drawer__headline">{{ approvalDrawerTitle }}</div>
        <ElTimeline v-if="approvalRecords.length">
          <ElTimelineItem
            v-for="record in approvalRecords"
            :key="record.approvalRecordId || `${record.operateTime}-${record.approvalStatus}`"
            :timestamp="record.operateTime || '-'"
            placement="top"
          >
            <div class="timeline-title">{{ getApprovalStatusLabel(record.approvalStatus) }}</div>
            <div class="timeline-desc">{{ record.opinion || '暂无审批意见' }}</div>
            <div class="timeline-meta">操作人：{{ record.operateBy || '-' }}</div>
          </ElTimelineItem>
        </ElTimeline>
        <ElEmpty v-else description="暂无审批轨迹" :image-size="72" />
      </div>
    </ElDrawer>
  </div>
</template>

<script setup lang="ts">
  import type { FormInstance, FormRules, TabPaneName } from 'element-plus'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import type {
    AssetChangeLogRecord,
    AssetInventoryRecord,
    AssetRealEstateOccupancyRecord,
    AssetRectificationRecord
  } from '@/api/asset/ledger'
  import type {
    AssetRealEstateRectificationApprovalRecord,
    AssetTreeOption,
    AssetUserOption
  } from '@/api/asset/real-estate'
  import {
    addRealEstateOccupancy,
    approveRealEstateRectification,
    changeRealEstateOccupancy,
    getRealEstateDetail,
    getRealEstateDeptTree,
    getRealEstateLifecycle,
    listRealEstateRectificationApprovalRecords,
    listRealEstateResponsibleUsers,
    rejectRealEstateRectificationApproval,
    releaseRealEstateOccupancy,
    submitRealEstateRectificationApproval
  } from '@/api/asset/real-estate'
  import { useDict } from '@/utils/dict'
  import { useUserStore } from '@/store/modules/user'
  import DisposalPanel from './components/disposal-panel.vue'
  import InspectionPanel from './components/inspection-panel.vue'
  import OccupancyPanel from './components/occupancy-panel.vue'
  import OverviewPanel from './components/overview-panel.vue'
  import RectificationPanel from './components/rectification-panel.vue'
  import {
    type RealEstateDetailTabName,
    REAL_ESTATE_DETAIL_TABS,
    normalizeRealEstateDetailTab,
    persistRealEstateDetailTab,
    readRealEstateDetailTab,
    resolveRealEstateDetailTabByPath
  } from './tab-state'

  defineOptions({ name: 'AssetRealEstateDetailPage' })

  const route = useRoute()
  const router = useRouter()
  const userStore = useUserStore()
  route.meta.activePath = '/asset/real-estate'

  const detailTabs = REAL_ESTATE_DETAIL_TABS
  const treeSelectProps = {
    value: 'id',
    label: 'label',
    children: 'children',
    disabled: 'disabled'
  }

  const { ast_asset_status, ast_asset_source_type, ast_asset_acquire_type } = useDict(
    'ast_asset_status',
    'ast_asset_source_type',
    'ast_asset_acquire_type'
  )

  const loading = ref(false)
  const activeTab = ref<RealEstateDetailTabName>('overview')
  const approvalDrawerVisible = ref(false)
  const approvalRecords = ref<AssetRealEstateRectificationApprovalRecord[]>([])
  const approvalDrawerTitle = ref('')
  const occupancyDrawerVisible = ref(false)
  const occupancySubmitting = ref(false)
  const occupancyDrawerMode = ref<'create' | 'change' | 'release'>('create')
  const occupancyFormRef = ref<FormInstance>()
  const occupancyDeptOptions = ref<AssetTreeOption[]>([])
  const occupancyResponsibleUserOptions = ref<AssetUserOption[]>([])
  const occupancyResponsibleUserLoading = ref(false)
  const currentOccupancyTarget = ref<AssetRealEstateOccupancyRecord>()
  const detailData = reactive<Record<string, any>>({})
  const lifecycleData = reactive({
    occupancyRecords: [] as AssetRealEstateOccupancyRecord[],
    handoverRecords: [] as Record<string, any>[],
    inventoryRecords: [] as AssetInventoryRecord[],
    rectificationOrders: [] as AssetRectificationRecord[],
    disposalRecords: [] as Record<string, any>[],
    changeLogs: [] as AssetChangeLogRecord[]
  })
  const occupancyForm = reactive({
    useDeptId: undefined as number | undefined,
    responsibleUserId: undefined as number | undefined,
    locationName: '',
    startDate: '',
    changeReason: '',
    endDate: '',
    releaseReason: ''
  })
  const occupancyChangeReasonTemplates = [
    '部门调整交接',
    '办公区域调整',
    '项目入驻占用',
    '台账口径校正'
  ]
  const occupancyReleaseReasonTemplates = [
    '部门搬离释放',
    '装修改造临时释放',
    '处置前清场',
    '权属收回'
  ]

  type InspectionLinkStatus = 'NONE_REQUIRED' | 'NOT_CREATED' | 'PENDING' | 'COMPLETED'

  type InspectionRecordView = AssetInventoryRecord & {
    rectificationLinkStatus: InspectionLinkStatus
    linkedRectification?: AssetRectificationRecord
  }

  const assetId = computed(() => {
    const value = route.params.assetId
    return value ? Number(value) : undefined
  })

  const canEdit = computed(() => {
    return (
      userStore.permissions.includes('*:*:*') ||
      userStore.permissions.includes('asset:realEstate:edit')
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

  const occupancyRecords = computed(() => lifecycleData.occupancyRecords)
  const inspectionRecords = computed(() => lifecycleData.inventoryRecords)
  const disposalRecords = computed(() => lifecycleData.disposalRecords)
  const changeLogs = computed(() => lifecycleData.changeLogs)

  const occupancyDrawerTitle = computed(() => {
    const mapper = {
      create: '发起占用',
      change: '变更占用',
      release: '释放占用'
    }
    return mapper[occupancyDrawerMode.value]
  })

  const occupancyDrawerDesc = computed(() => {
    const mapper = {
      create: '发起占用会创建第一条有效占用单，并把使用部门、责任人和位置回写到资产主档。',
      change: '变更占用会关闭当前有效占用，并生成一条新的有效占用单，历史记录继续保留。',
      release: '释放占用后，当前资产会回到“暂无有效占用”的状态，但释放轨迹会完整留痕。'
    }
    return mapper[occupancyDrawerMode.value]
  })

  const occupancyFormRules = computed<FormRules>(() => {
    if (occupancyDrawerMode.value === 'release') {
      return {
        endDate: [{ required: true, message: '请选择释放日期', trigger: 'change' }],
        releaseReason: [{ required: true, message: '请输入释放原因', trigger: 'blur' }]
      }
    }

    return {
      useDeptId: [{ required: true, message: '请选择使用部门', trigger: 'change' }],
      responsibleUserId: [{ required: true, message: '请选择责任人', trigger: 'change' }],
      startDate: [{ required: true, message: '请选择占用开始日期', trigger: 'change' }],
      changeReason: [{ required: true, message: '请输入发起/变更原因', trigger: 'blur' }]
    }
  })

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
    return inspectionLinkedRecords.value.filter((record) =>
      ['NOT_CREATED', 'PENDING'].includes(record.rectificationLinkStatus)
    ).length
  })

  const rectificationLogs = computed(() => {
    return lifecycleData.changeLogs.filter((record) =>
      String(record.changeDesc || '').includes('整改')
    )
  })

  const findLinkedRectification = (record: AssetInventoryRecord) => {
    if (record.followUpBizId) {
      const byRectificationId = rectificationRecords.value.find(
        (item) => item.rectificationId === record.followUpBizId
      )
      if (byRectificationId) {
        return byRectificationId
      }
    }

    if (record.itemId) {
      const byInventoryItemId = rectificationRecords.value.find(
        (item) => item.inventoryItemId === record.itemId
      )
      if (byInventoryItemId) {
        return byInventoryItemId
      }
    }

    if (record.taskId) {
      const byTaskId = rectificationRecords.value.find((item) => item.taskId === record.taskId)
      if (byTaskId) {
        return byTaskId
      }
    }

    return undefined
  }

  const inspectionLinkedRecords = computed<InspectionRecordView[]>(() => {
    return inspectionRecords.value.map((record) => {
      const linkedRectification = findLinkedRectification(record)
      let rectificationLinkStatus: InspectionLinkStatus = 'NONE_REQUIRED'

      if (isRectifiableRecord(record)) {
        if (!linkedRectification) {
          rectificationLinkStatus = 'NOT_CREATED'
        } else if (
          String(linkedRectification.rectificationStatus || '').toUpperCase() === 'COMPLETED'
        ) {
          rectificationLinkStatus = 'COMPLETED'
        } else {
          rectificationLinkStatus = 'PENDING'
        }
      }

      return {
        ...record,
        rectificationLinkStatus,
        linkedRectification
      }
    })
  })

  const inspectionStatusSummary = computed(() => {
    const summary = {
      notCreated: 0,
      pending: 0,
      completed: 0
    }

    inspectionLinkedRecords.value.forEach((record) => {
      if (record.rectificationLinkStatus === 'NOT_CREATED') {
        summary.notCreated += 1
      } else if (record.rectificationLinkStatus === 'PENDING') {
        summary.pending += 1
      } else if (record.rectificationLinkStatus === 'COMPLETED') {
        summary.completed += 1
      }
    })

    return summary
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
    if (Array.isArray(value)) {
      return value
    }
    return Array.isArray(value?.data) ? value.data : []
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

  const getToday = () => new Date().toISOString().slice(0, 10)

  const resetOccupancyForm = () => {
    occupancyForm.useDeptId = undefined
    occupancyForm.responsibleUserId = undefined
    occupancyForm.locationName = ''
    occupancyForm.startDate = getToday()
    occupancyForm.changeReason = ''
    occupancyForm.endDate = getToday()
    occupancyForm.releaseReason = ''
    currentOccupancyTarget.value = undefined
  }

  const ensureResponsibleUserOption = (userId?: number, userName?: string) => {
    if (!userId || !userName) {
      return
    }
    const exists = occupancyResponsibleUserOptions.value.some((item) => item.value === userId)
    if (!exists) {
      occupancyResponsibleUserOptions.value.unshift({
        value: userId,
        label: userName
      })
    }
  }

  const loadOccupancyDeptTree = async () => {
    if (occupancyDeptOptions.value.length) {
      return
    }
    const response = await getRealEstateDeptTree()
    occupancyDeptOptions.value = toArrayData<AssetTreeOption>(response)
  }

  const loadOccupancyResponsibleUsers = async (keyword = '') => {
    occupancyResponsibleUserLoading.value = true
    try {
      const response = await listRealEstateResponsibleUsers({ keyword: keyword.trim() })
      occupancyResponsibleUserOptions.value = toArrayData<AssetUserOption>(response)
    } finally {
      occupancyResponsibleUserLoading.value = false
    }
  }

  const resolveEntryTab = () => {
    const pathTab = resolveRealEstateDetailTabByPath(route.path)
    if (pathTab !== 'overview') {
      return pathTab
    }
    return (
      readRealEstateDetailTab(assetId.value) ||
      normalizeRealEstateDetailTab(String(route.query.tab || ''))
    )
  }

  const syncActiveTabFromContext = () => {
    activeTab.value = resolveEntryTab()
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

  const openOccupancyDrawer = async (
    mode: 'create' | 'change' | 'release',
    record?: AssetRealEstateOccupancyRecord
  ) => {
    occupancyDrawerMode.value = mode
    resetOccupancyForm()
    currentOccupancyTarget.value = record

    if (mode === 'release') {
      occupancyForm.endDate = getToday()
      occupancyForm.releaseReason = ''
    } else {
      occupancyForm.useDeptId = record?.useDeptId || detailData.useDeptId
      occupancyForm.responsibleUserId = record?.responsibleUserId || detailData.responsibleUserId
      occupancyForm.locationName = record?.locationName || detailData.locationName || ''
      occupancyForm.startDate = mode === 'change' ? getToday() : detailData.enableDate || getToday()
      occupancyForm.changeReason = ''
      ensureResponsibleUserOption(
        record?.responsibleUserId || detailData.responsibleUserId,
        record?.responsibleUserName || detailData.responsibleUserName
      )
      await Promise.all([loadOccupancyDeptTree(), loadOccupancyResponsibleUsers()])
    }

    occupancyDrawerVisible.value = true
    nextTick(() => occupancyFormRef.value?.clearValidate())
  }

  const openCreateOccupancyDrawer = () => openOccupancyDrawer('create')

  const openChangeOccupancyDrawer = (record: AssetRealEstateOccupancyRecord) => {
    return openOccupancyDrawer('change', record)
  }

  const openReleaseOccupancyDrawer = (record: AssetRealEstateOccupancyRecord) => {
    return openOccupancyDrawer('release', record)
  }

  const applyOccupancyReasonTemplate = (template: string) => {
    if (occupancyDrawerMode.value === 'release') {
      occupancyForm.releaseReason = template
      return
    }
    occupancyForm.changeReason = template
  }

  const handleTabChange = (tabName: TabPaneName) => {
    // 中文注释：详情页签只在当前页面切换组件，不再推送新路由，避免用户感知上“还在详情里却像换页”。
    activeTab.value = normalizeRealEstateDetailTab(String(tabName))
  }

  const isRectifiableRecord = (record: AssetInventoryRecord) => {
    const inventoryResult = String(record.inventoryResult || '').toUpperCase()
    const followUpAction = String(record.followUpAction || '').toUpperCase()
    return (
      followUpAction !== 'CREATE_DISPOSAL' &&
      (inventoryResult !== 'NORMAL' || followUpAction !== 'NONE')
    )
  }

  const goToInspectionTask = (taskId?: number) => {
    if (!assetId.value || !taskId) {
      return
    }
    persistRealEstateDetailTab(assetId.value, 'inspection')
    router.push(`/asset/real-estate/detail/${assetId.value}/inspection-task/${taskId}`)
  }

  const goToCreateRectification = (record: AssetInventoryRecord) => {
    if (!assetId.value || !record.taskId) {
      return
    }
    persistRealEstateDetailTab(assetId.value, 'rectification')
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
    persistRealEstateDetailTab(assetId.value, 'rectification')
    router.push(`/asset/real-estate/detail/${assetId.value}/rectification/edit/${rectificationId}`)
  }

  const goToCompleteRectification = (rectificationId?: number) => {
    if (!assetId.value || !rectificationId) {
      return
    }
    persistRealEstateDetailTab(assetId.value, 'rectification')
    router.push(
      `/asset/real-estate/detail/${assetId.value}/rectification/complete/${rectificationId}`
    )
  }

  const getApprovalStatusLabel = (status?: string) => {
    const mapper: Record<string, string> = {
      UNSUBMITTED: '待提交审批',
      SUBMITTED: '审批中',
      APPROVED: '审批通过',
      REJECTED: '审批驳回'
    }
    return mapper[String(status || '').toUpperCase()] || '待提交审批'
  }

  const getRectificationTitle = (rectificationId?: number) => {
    const current = rectificationRecords.value.find((item) => item.rectificationId === rectificationId)
    return current?.rectificationNo || '整改审批轨迹'
  }

  const askApprovalOpinion = async (title: string, placeholder: string) => {
    const result = await ElMessageBox.prompt('请输入审批意见', title, {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      inputPlaceholder: placeholder,
      inputValidator: (value) => {
        return value?.trim() ? true : '审批意见不能为空'
      }
    })
    return result.value.trim()
  }

  const reloadLifecycle = async () => {
    if (!assetId.value) {
      return
    }
    const lifecycleResponse = await getRealEstateLifecycle(assetId.value)
    const lifecycle = toObjectData<any>(lifecycleResponse) || {}
    lifecycleData.occupancyRecords = toArrayData(lifecycle.occupancyRecords)
    lifecycleData.handoverRecords = toArrayData(lifecycle.handoverRecords)
    lifecycleData.inventoryRecords = toArrayData(lifecycle.inventoryRecords)
    lifecycleData.rectificationOrders = toArrayData(lifecycle.rectificationOrders)
    lifecycleData.disposalRecords = toArrayData(lifecycle.disposalRecords)
    lifecycleData.changeLogs = toArrayData(lifecycle.changeLogs)
  }

  const handleViewApprovalRecords = async (rectificationId?: number) => {
    if (!assetId.value || !rectificationId) {
      return
    }
    const response = await listRealEstateRectificationApprovalRecords(assetId.value, rectificationId)
    approvalRecords.value = toArrayData<AssetRealEstateRectificationApprovalRecord>(response)
    approvalDrawerTitle.value = getRectificationTitle(rectificationId)
    approvalDrawerVisible.value = true
  }

  const handleSubmitApproval = async (rectificationId?: number) => {
    if (!assetId.value || !rectificationId) {
      return
    }
    const opinion = await askApprovalOpinion('提交整改审批', '请输入提交审批说明').catch(() => '')
    if (!opinion) {
      return
    }
    await submitRealEstateRectificationApproval(assetId.value, rectificationId, { opinion })
    ElMessage.success('整改审批已提交')
    await reloadLifecycle()
    await handleViewApprovalRecords(rectificationId)
  }

  const handleApproveApproval = async (rectificationId?: number) => {
    if (!assetId.value || !rectificationId) {
      return
    }
    const opinion = await askApprovalOpinion('整改审批通过', '请输入审批通过意见').catch(() => '')
    if (!opinion) {
      return
    }
    await approveRealEstateRectification(assetId.value, rectificationId, { opinion })
    ElMessage.success('整改审批已通过')
    await reloadLifecycle()
    await handleViewApprovalRecords(rectificationId)
  }

  const handleRejectApproval = async (rectificationId?: number) => {
    if (!assetId.value || !rectificationId) {
      return
    }
    const opinion = await askApprovalOpinion('整改审批驳回', '请输入审批驳回意见').catch(() => '')
    if (!opinion) {
      return
    }
    await rejectRealEstateRectificationApproval(assetId.value, rectificationId, { opinion })
    ElMessage.success('整改审批已驳回')
    await reloadLifecycle()
    await handleViewApprovalRecords(rectificationId)
  }

  const buildOccupancySubmitData = () => {
    if (occupancyDrawerMode.value === 'release') {
      return {
        endDate: occupancyForm.endDate,
        releaseReason: occupancyForm.releaseReason?.trim()
      }
    }

    return {
      useDeptId: occupancyForm.useDeptId,
      responsibleUserId: occupancyForm.responsibleUserId,
      locationName: occupancyForm.locationName?.trim() || undefined,
      startDate: occupancyForm.startDate,
      changeReason: occupancyForm.changeReason?.trim()
    }
  }

  const closeOccupancyDrawer = () => {
    occupancyDrawerVisible.value = false
  }

  const handleOccupancySubmit = async () => {
    if (!assetId.value) {
      return
    }
    const valid = await occupancyFormRef.value?.validate().catch(() => false)
    if (!valid) {
      return
    }

    occupancySubmitting.value = true
    try {
      if (occupancyDrawerMode.value === 'create') {
        await addRealEstateOccupancy(assetId.value, buildOccupancySubmitData())
        ElMessage.success('占用已发起')
      } else if (occupancyDrawerMode.value === 'change') {
        if (!currentOccupancyTarget.value?.occupancyId) {
          return
        }
        await changeRealEstateOccupancy(
          assetId.value,
          currentOccupancyTarget.value.occupancyId,
          buildOccupancySubmitData()
        )
        ElMessage.success('占用已变更')
      } else {
        if (!currentOccupancyTarget.value?.occupancyId) {
          return
        }
        await releaseRealEstateOccupancy(
          assetId.value,
          currentOccupancyTarget.value.occupancyId,
          buildOccupancySubmitData()
        )
        ElMessage.success('占用已释放')
      }

      closeOccupancyDrawer()
      await loadDetail()
      activeTab.value = 'occupancy'
    } finally {
      occupancySubmitting.value = false
    }
  }

  const goToDisposalModule = () => {
    if (!assetId.value) {
      return
    }
    persistRealEstateDetailTab(assetId.value, 'disposal')
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
      lifecycleData.occupancyRecords = toArrayData(lifecycle.occupancyRecords)
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
    () => [route.params.assetId, route.path, route.query.tab],
    () => {
      syncActiveTabFromContext()
    },
    { immediate: true }
  )

  watch(
    activeTab,
    (currentTab) => {
      persistRealEstateDetailTab(assetId.value, currentTab)
    },
    { immediate: false }
  )

  watch(occupancyDrawerVisible, (visible) => {
    if (!visible) {
      resetOccupancyForm()
      nextTick(() => occupancyFormRef.value?.clearValidate())
    }
  })

  watch(
    () => route.params.assetId,
    async (currentAssetId, previousAssetId) => {
      const parsedAssetId = currentAssetId ? Number(currentAssetId) : undefined
      const previousParsedAssetId = previousAssetId ? Number(previousAssetId) : undefined

      // 中文注释：首屏直达异步详情路由时，直接监听原始参数可以避免首次进入漏拉数据。
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

    display: flex;
    flex-direction: column;
    gap: 12px;
    background:
      radial-gradient(circle at 0% 0%, rgb(31 122 140 / 8%), transparent 34%),
      radial-gradient(circle at 100% 0%, rgb(26 188 156 / 8%), transparent 36%),
      var(--art-main-bg-color);
  }

  .head-card,
  .summary-card,
  .tab-card,
  :deep(.section-card) {
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
    word-break: break-word;
  }

  .detail-tabs :deep(.el-tabs__header) {
    margin-bottom: 0;
  }

  .detail-content {
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding-bottom: 8px;
  }

  .approval-drawer {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .occupancy-drawer {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .occupancy-drawer__headline {
    font-size: 14px;
    line-height: 1.8;
    color: #5d6b86;
  }

  .occupancy-drawer__snapshot {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 12px;
    margin-bottom: 4px;
  }

  .snapshot-item {
    padding: 12px 14px;
    border: 1px solid #e7edf6;
    border-radius: 12px;
    background: #f8fbfd;

    span {
      display: block;
      font-size: 12px;
      color: #6f7f99;
    }

    strong {
      display: block;
      margin-top: 8px;
      font-size: 14px;
      color: #18233a;
      word-break: break-word;
    }
  }

  .occupancy-drawer__footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
  }

  .occupancy-template-group {
    display: flex;
    flex-direction: column;
    gap: 10px;
    margin-top: 12px;
  }

  .occupancy-template-group__label {
    font-size: 12px;
    color: #6f7f99;
  }

  .occupancy-template-group__items {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
  }

  .occupancy-template-chip {
    padding: 0 12px;
    height: 28px;
    border: 1px solid var(--el-border-color);
    border-radius: 999px;
    background: var(--el-fill-color-light);
    color: var(--el-text-color-regular);
    font-size: 12px;
    line-height: 26px;
    cursor: pointer;
    transition:
      border-color 0.2s ease,
      color 0.2s ease,
      background-color 0.2s ease,
      transform 0.2s ease;

    &:hover {
      border-color: var(--el-color-primary);
      color: var(--el-color-primary);
      background: var(--el-color-primary-light-9);
      transform: translateY(-1px);
    }

    &:focus-visible {
      outline: 2px solid var(--el-color-primary-light-5);
      outline-offset: 2px;
    }
  }

  .occupancy-template-tag {
    cursor: pointer;
  }

  .approval-drawer__headline {
    font-size: 16px;
    font-weight: 700;
    color: var(--asset-text-main);
  }

  :deep(.section-stack) {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  :deep(.section-alert) {
    margin-bottom: 0;
  }

  :deep(.section-card .el-card__header) {
    padding: 14px 16px;
    border-bottom: 1px solid #eaf0fb;
  }

  :deep(.section-card .el-card__body) {
    padding: 0;
  }

  :deep(.card-title) {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 15px;
    font-weight: 600;
    color: var(--asset-text-main);
  }

  :deep(.card-title::before) {
    content: '';
    width: 4px;
    height: 14px;
    border-radius: 999px;
    background: var(--asset-accent);
  }

  :deep(.detail-descriptions) {
    width: 100%;
  }

  :deep(.detail-descriptions .el-descriptions__body) {
    overflow-x: auto;
  }

  :deep(.detail-descriptions .el-descriptions__table) {
    width: 100%;
    min-width: 860px;
    table-layout: auto;
  }

  :deep(.detail-descriptions .el-descriptions__cell) {
    padding: 12px 14px;
    font-size: 13px;
    line-height: 1.7;
    word-break: break-word;
    white-space: normal;
  }

  :deep(.detail-descriptions .el-descriptions__content) {
    word-break: break-word;
    white-space: normal;
  }

  :deep(.detail-descriptions .el-descriptions__label.el-descriptions__cell) {
    color: #5b6f8c;
    font-weight: 500;
    background: var(--asset-panel-muted);
  }

  :deep(.record-wrapper) {
    padding: 14px 16px;
  }

  :deep(.record-list) {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  :deep(.record-item) {
    border: 1px solid #e9edf7;
    border-radius: 10px;
    padding: 10px 12px;
    background: #fbfcff;
  }

  :deep(.record-item__title) {
    font-size: 14px;
    font-weight: 600;
    color: #1d2f4f;
    margin-bottom: 4px;
    word-break: break-word;
  }

  :deep(.record-item__desc),
  :deep(.timeline-desc),
  :deep(.timeline-meta) {
    font-size: 12px;
    color: #5f7392;
    line-height: 1.6;
    word-break: break-word;
    white-space: normal;
  }

  :deep(.record-item__actions) {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    margin-top: 8px;
  }

  :deep(.timeline-title) {
    font-size: 13px;
    font-weight: 600;
    color: #1d2f4f;
  }

  :deep(.disposal-link-card) {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    padding: 16px;
  }

  :deep(.disposal-link-card__title) {
    font-size: 14px;
    font-weight: 600;
    color: #1d2f4f;
    margin-bottom: 4px;
  }

  :deep(.disposal-link-card__desc) {
    font-size: 12px;
    color: #5f7392;
    word-break: break-word;
  }

  @media (max-width: 960px) {
    .summary-grid {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }

    :deep(.disposal-link-card) {
      flex-direction: column;
      align-items: flex-start;
    }

    :deep(.detail-descriptions .el-descriptions__table) {
      min-width: 720px;
    }
  }

  @media (max-width: 640px) {
    .summary-grid {
      grid-template-columns: 1fr;
    }

    .page-title {
      font-size: 28px;
    }

    .occupancy-drawer__snapshot {
      grid-template-columns: 1fr;
    }

    :deep(.detail-descriptions .el-descriptions__table) {
      min-width: 640px;
    }
  }
</style>
