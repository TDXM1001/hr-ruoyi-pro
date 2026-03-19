<template>
  <div class="asset-inventory-page art-full-height flex flex-col gap-3 overflow-y-auto p-3">
    <ElCard class="head-card" shadow="never">
      <div class="flex flex-wrap items-start justify-between gap-3">
        <div>
          <div class="page-title">资产盘点</div>
          <div class="page-desc">
            盘点任务采用“任务建单 + 结果登记”模式，结果回写台账并保留变更日志，确保资产状态闭环。
          </div>
        </div>
        <ElSpace wrap>
          <ElTag type="success" effect="light">资产类型：固定资产（一期）</ElTag>
          <ElButton v-auth="'asset:inventory:add'" type="primary" @click="openCreateDialog" v-ripple>
            发起盘点任务
          </ElButton>
        </ElSpace>
      </div>
    </ElCard>

    <ElCard class="task-card" shadow="never">
      <template #header>
        <div class="card-header">
          <div class="card-title">盘点任务</div>
          <ElButton
            type="primary"
            plain
            icon="ri:refresh-line"
            :loading="taskLoading"
            @click="handleTaskRefresh"
          >
            刷新任务
          </ElButton>
        </div>
      </template>

      <ArtSearchBar
        v-model="taskSearchForm"
        :items="taskSearchItems"
        :showExpand="true"
        @search="handleTaskSearch"
        @reset="handleTaskReset"
      />

      <ArtTable
        rowKey="taskId"
        :loading="taskLoading"
        :data="taskData"
        :columns="taskColumns"
        :pagination="taskPagination"
        @pagination:size-change="handleTaskSizeChange"
        @pagination:current-change="handleTaskCurrentChange"
      />
    </ElCard>

    <ElCard class="asset-card" shadow="never">
      <template #header>
        <div class="card-header">
          <div class="card-title">
            任务资产
            <ElTag v-if="currentTask" type="info" effect="light">
              {{ currentTask.taskNo }} / {{ currentTask.taskName }}
            </ElTag>
          </div>
          <ElSpace wrap>
            <ElTag type="info" effect="light">共 {{ taskAssets.length }} 宗</ElTag>
            <ElTag type="success" effect="light">已登记 {{ currentTaskSubmittedCount }} 宗</ElTag>
          </ElSpace>
        </div>
      </template>

      <ElEmpty
        v-if="!currentTask"
        description="请先在上方任务列表中点击“查看资产”"
        :image-size="80"
      />
      <ArtTable
        v-else
        rowKey="assetId"
        :loading="taskAssetLoading"
        :data="taskAssets"
        :columns="taskAssetColumns"
      />
    </ElCard>

    <ElDialog
      v-model="createDialogVisible"
      title="发起盘点任务"
      width="1080px"
      destroy-on-close
      @closed="handleCreateDialogClosed"
    >
      <ElForm ref="createFormRef" :model="createFormData" :rules="createFormRules" label-width="110px">
        <ElRow :gutter="16">
          <ElCol :xs="24" :md="12">
            <ElFormItem label="任务名称" prop="taskName">
              <ElInput
                v-model="createFormData.taskName"
                maxlength="120"
                clearable
                placeholder="请输入盘点任务名称"
              />
            </ElFormItem>
          </ElCol>
          <ElCol :xs="24" :md="12">
            <ElFormItem label="计划盘点日" prop="plannedDate">
              <ElDatePicker
                v-model="createFormData.plannedDate"
                type="date"
                value-format="YYYY-MM-DD"
                class="w-full"
                placeholder="请选择计划盘点日期"
              />
            </ElFormItem>
          </ElCol>
        </ElRow>
        <ElFormItem label="备注" prop="remark">
          <ElInput
            v-model="createFormData.remark"
            type="textarea"
            :rows="2"
            maxlength="500"
            show-word-limit
            placeholder="请输入任务说明（可选）"
          />
        </ElFormItem>
      </ElForm>

      <ElDivider class="!my-4" content-position="left">选择盘点资产</ElDivider>

      <ArtSearchBar
        v-model="createAssetSearchForm"
        :items="createAssetSearchItems"
        :showExpand="true"
        @search="handleCreateAssetSearch"
        @reset="handleCreateAssetReset"
      />

      <div class="mb-3 flex items-center justify-between gap-3">
        <ElAlert
          class="flex-1"
          title="仅允许选择在册 / 使用中 / 闲置 / 盘点中的固定资产，待处置与已处置资产不可加入盘点任务。"
          type="info"
          :closable="false"
          show-icon
        />
        <ElTag type="primary" effect="light">已选 {{ selectedCreateAssets.length }} 宗</ElTag>
      </div>

      <ArtTable
        ref="createAssetTableRef"
        rowKey="assetId"
        :loading="createAssetLoading"
        :data="createAssetData"
        :columns="createAssetColumns"
        :pagination="createAssetPagination"
        :height="360"
        @selection-change="handleCreateAssetSelectionChange"
        @pagination:size-change="handleCreateAssetSizeChange"
        @pagination:current-change="handleCreateAssetCurrentChange"
      />

      <template #footer>
        <ElButton @click="createDialogVisible = false">取消</ElButton>
        <ElButton type="primary" :loading="createSubmitting" @click="handleCreateTaskSubmit">
          提交任务
        </ElButton>
      </template>
    </ElDialog>

    <InventoryResultDialog
      v-model="resultDialogVisible"
      :loading="resultSubmitting"
      :task="currentTask"
      :asset="resultDialogAsset"
      @submit="handleInventoryResultSubmit"
    />
  </div>
</template>

<script setup lang="ts">
  import type { FormInstance, FormRules } from 'element-plus'
  import { ElButton, ElMessage, ElTag } from 'element-plus'
  import { getAssetLedger, listAssetLedger } from '@/api/asset/ledger'
  import {
    addInventoryTask,
    listInventoryTask,
    submitInventoryResult,
    type AssetInventoryResultPayload
  } from '@/api/asset/inventory'
  import DictTag from '@/components/DictTag/index.vue'
  import { useTable } from '@/hooks/core/useTable'
  import { useDict } from '@/utils/dict'
  import { useUserStore } from '@/store/modules/user'
  import InventoryResultDialog from './modules/inventory-result-dialog.vue'

  defineOptions({ name: 'AssetInventory' })

  type InventoryTaskRow = {
    taskId: number
    taskNo: string
    taskName: string
    taskStatus: string
    scopeType?: string
    scopeValue?: string
    plannedDate?: string
    completedDate?: string
    createBy?: string
  }

  type InventoryTaskAssetRow = {
    assetId: number
    assetCode?: string
    assetName?: string
    assetStatus?: string
    ownerDeptName?: string
    useDeptName?: string
    responsibleUserName?: string
    locationName?: string
    _loadFailed?: boolean
  }

  const { ast_asset_status } = useDict('ast_asset_status')
  const userStore = useUserStore()

  const taskStatusOptions = [
    { label: '进行中', value: 'IN_PROGRESS' },
    { label: '已完成', value: 'COMPLETED' },
    { label: '草稿', value: 'DRAFT' }
  ]

  const getTaskStatusTagType = (status?: string): 'primary' | 'success' | 'warning' | 'info' => {
    const mapper: Record<string, 'primary' | 'success' | 'warning' | 'info'> = {
      IN_PROGRESS: 'warning',
      COMPLETED: 'success',
      DRAFT: 'info'
    }
    return mapper[String(status || '').toUpperCase()] || 'info'
  }

  const getTaskStatusLabel = (status?: string) => {
    const match = taskStatusOptions.find((item) => item.value === String(status || '').toUpperCase())
    return match?.label || status || '-'
  }

  const hasPermission = (permission: string) => {
    return userStore.permissions.includes('*:*:*') || userStore.permissions.includes(permission)
  }

  const canEditInventoryResult = computed(() => hasPermission('asset:inventory:edit'))

  const taskInitialSearchState = {
    taskNo: '',
    taskName: '',
    taskStatus: '',
    daterange: undefined as string[] | undefined
  }
  const taskSearchForm = reactive({ ...taskInitialSearchState })

  const taskSearchItems = computed(() => [
    {
      label: '任务编号',
      key: 'taskNo',
      type: 'input',
      props: { placeholder: '请输入任务编号', clearable: true }
    },
    {
      label: '任务名称',
      key: 'taskName',
      type: 'input',
      props: { placeholder: '请输入任务名称', clearable: true }
    },
    {
      label: '任务状态',
      key: 'taskStatus',
      type: 'select',
      props: { placeholder: '请选择任务状态', clearable: true, options: taskStatusOptions }
    },
    {
      label: '计划日期',
      key: 'daterange',
      type: 'daterange',
      props: {
        style: { width: '100%' },
        valueFormat: 'YYYY-MM-DD',
        clearable: true,
        rangeSeparator: '至',
        startPlaceholder: '开始日期',
        endPlaceholder: '结束日期'
      }
    }
  ])

  const {
    columns: taskColumns,
    data: taskData,
    loading: taskLoading,
    pagination: taskPagination,
    getData: getTaskData,
    searchParams: taskSearchParams,
    resetSearchParams: resetTaskSearchParams,
    handleSizeChange: handleTaskSizeChange,
    handleCurrentChange: handleTaskCurrentChange,
    refreshData: refreshTaskData
  } = useTable({
    core: {
      apiFn: listInventoryTask,
      apiParams: {
        pageNum: 1,
        pageSize: 10
      },
      excludeParams: ['daterange'],
      columnsFactory: () => [
        { prop: 'taskNo', label: '任务编号', minWidth: 150 },
        { prop: 'taskName', label: '任务名称', minWidth: 180, showOverflowTooltip: true },
        {
          prop: 'taskStatus',
          label: '任务状态',
          width: 110,
          formatter: (row: InventoryTaskRow) =>
            h(
              ElTag,
              { type: getTaskStatusTagType(row.taskStatus), effect: 'light' },
              () => getTaskStatusLabel(row.taskStatus)
            )
        },
        {
          prop: 'scopeType',
          label: '盘点范围',
          width: 110,
          formatter: (row: InventoryTaskRow) =>
            String(row.scopeType || '').toUpperCase() === 'ASSET' ? '资产范围' : row.scopeType || '-'
        },
        { prop: 'plannedDate', label: '计划盘点日', width: 120 },
        { prop: 'completedDate', label: '完成日期', width: 120 },
        { prop: 'createBy', label: '创建人', width: 90 },
        {
          prop: 'operation',
          label: '操作',
          width: 96,
          fixed: 'right',
          align: 'right',
          formatter: (row: InventoryTaskRow) =>
            h(
              ElButton,
              {
                link: true,
                type: 'primary',
                onClick: () => handleSelectTask(row)
              },
              () => '查看资产'
            )
        }
      ]
    }
  })

  const currentTask = ref<InventoryTaskRow>()
  const taskAssets = ref<InventoryTaskAssetRow[]>([])
  const taskAssetLoading = ref(false)
  const submittedResultMap = ref<Record<number, number[]>>({})

  const resultDialogVisible = ref(false)
  const resultSubmitting = ref(false)
  const resultDialogAsset = ref<InventoryTaskAssetRow>()

  const taskSelectableStatuses = new Set(['IN_LEDGER', 'IN_USE', 'IDLE', 'INVENTORYING'])

  const createDialogVisible = ref(false)
  const createSubmitting = ref(false)
  const createFormRef = ref<FormInstance>()
  const createAssetTableRef = ref<any>()
  const selectedCreateAssets = ref<InventoryTaskAssetRow[]>([])

  const createFormData = reactive({
    taskName: '',
    plannedDate: new Date().toISOString().slice(0, 10),
    remark: ''
  })

  const createFormRules: FormRules = {
    taskName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
    plannedDate: [{ required: true, message: '请选择计划盘点日', trigger: 'change' }]
  }

  const createAssetInitialSearchState = {
    assetCode: '',
    assetName: '',
    assetStatus: ''
  }
  const createAssetSearchForm = reactive({ ...createAssetInitialSearchState })

  const createAssetSearchItems = computed(() => [
    {
      label: '资产编码',
      key: 'assetCode',
      type: 'input',
      props: { placeholder: '请输入资产编码', clearable: true }
    },
    {
      label: '资产名称',
      key: 'assetName',
      type: 'input',
      props: { placeholder: '请输入资产名称', clearable: true }
    },
    {
      label: '资产状态',
      key: 'assetStatus',
      type: 'select',
      props: {
        placeholder: '请选择资产状态',
        clearable: true,
        options: ast_asset_status.value
      }
    }
  ])

  const {
    columns: createAssetColumns,
    data: createAssetData,
    loading: createAssetLoading,
    pagination: createAssetPagination,
    getData: getCreateAssetData,
    searchParams: createAssetSearchParams,
    resetSearchParams: resetCreateAssetSearchParams,
    handleSizeChange: handleCreateAssetSizeChange,
    handleCurrentChange: handleCreateAssetCurrentChange
  } = useTable({
    core: {
      apiFn: listAssetLedger,
      apiParams: {
        assetType: 'FIXED',
        pageNum: 1,
        pageSize: 8
      },
      immediate: false,
      columnsFactory: () => [
        {
          type: 'selection',
          width: 48,
          align: 'center',
          selectable: (row: InventoryTaskAssetRow) =>
            taskSelectableStatuses.has(String(row.assetStatus || '').toUpperCase())
        },
        { prop: 'assetCode', label: '资产编码', minWidth: 140 },
        { prop: 'assetName', label: '资产名称', minWidth: 180, showOverflowTooltip: true },
        {
          prop: 'assetStatus',
          label: '资产状态',
          width: 120,
          formatter: (row: InventoryTaskAssetRow) =>
            h(DictTag, { options: ast_asset_status.value, value: row.assetStatus })
        },
        { prop: 'ownerDeptName', label: '权属部门', minWidth: 120 },
        { prop: 'useDeptName', label: '使用部门', minWidth: 120 }
      ]
    }
  })

  const taskAssetColumns = computed(() => [
    { prop: 'assetCode', label: '资产编码', minWidth: 140 },
    { prop: 'assetName', label: '资产名称', minWidth: 180, showOverflowTooltip: true },
    {
      prop: 'assetStatus',
      label: '资产状态',
      width: 120,
      formatter: (row: InventoryTaskAssetRow) => h(DictTag, { options: ast_asset_status.value, value: row.assetStatus })
    },
    { prop: 'ownerDeptName', label: '权属部门', minWidth: 120 },
    { prop: 'useDeptName', label: '使用部门', minWidth: 120 },
    { prop: 'responsibleUserName', label: '责任人', width: 110 },
    { prop: 'locationName', label: '资产位置', minWidth: 150, showOverflowTooltip: true },
    {
      prop: 'operation',
      label: '操作',
      width: 110,
      fixed: 'right' as const,
      align: 'right' as const,
      formatter: (row: InventoryTaskAssetRow) => {
        if (!currentTask.value) {
          return '-'
        }
        if (isAssetResultSubmitted(currentTask.value.taskId, row.assetId)) {
          return h(ElTag, { type: 'success', effect: 'light' }, () => '已登记')
        }
        if (!canEditInventoryResult.value) {
          return '-'
        }
        return h(
          ElButton,
          {
            link: true,
            type: 'primary',
            onClick: () => openResultDialog(row)
          },
          () => '登记结果'
        )
      }
    }
  ])

  const currentTaskSubmittedCount = computed(() => {
    if (!currentTask.value) {
      return 0
    }
    return submittedResultMap.value[currentTask.value.taskId]?.length || 0
  })

  const toRecordData = <T,>(response: any): T | undefined => {
    if (!response) {
      return undefined
    }
    if (response.data && !Array.isArray(response.data)) {
      return response.data as T
    }
    if (!Array.isArray(response) && typeof response === 'object' && !('rows' in response)) {
      return response as T
    }
    return undefined
  }

  const parseScopeAssetIds = (scopeValue?: string) => {
    if (!scopeValue) {
      return []
    }
    const uniqueIdSet = new Set<number>()
    String(scopeValue)
      .split(',')
      .map((item) => Number(String(item).trim()))
      .filter((item) => Number.isFinite(item) && item > 0)
      .forEach((item) => uniqueIdSet.add(item))
    return Array.from(uniqueIdSet)
  }

  const markAssetResultSubmitted = (taskId: number, assetId: number) => {
    const submittedIds = submittedResultMap.value[taskId] || []
    if (!submittedIds.includes(assetId)) {
      submittedResultMap.value[taskId] = [...submittedIds, assetId]
    }
  }

  const isAssetResultSubmitted = (taskId: number, assetId: number) => {
    return submittedResultMap.value[taskId]?.includes(assetId) || false
  }

  const loadTaskAssets = async (task: InventoryTaskRow) => {
    const assetIds = parseScopeAssetIds(task.scopeValue)
    if (!assetIds.length) {
      taskAssets.value = []
      return
    }

    taskAssetLoading.value = true
    try {
      const responseList = await Promise.allSettled(assetIds.map((assetId) => getAssetLedger(assetId)))
      taskAssets.value = responseList.map((result, index) => {
        const assetId = assetIds[index]
        if (result.status === 'fulfilled') {
          const rowData = toRecordData<InventoryTaskAssetRow>(result.value)
          if (rowData) {
            return { ...rowData, assetId }
          }
        }
        // 中文注释：没有台账查询权限或数据异常时，保底展示资产ID，保证盘点登记流程仍可继续。
        return {
          assetId,
          assetCode: `ID-${assetId}`,
          assetName: '资产信息加载失败，可继续登记结果',
          _loadFailed: true
        } as InventoryTaskAssetRow
      })
    } finally {
      taskAssetLoading.value = false
    }
  }

  const buildTaskQueryParams = () => {
    const [beginTime, endTime] = Array.isArray(taskSearchForm.daterange)
      ? taskSearchForm.daterange
      : [undefined, undefined]

    return {
      taskNo: taskSearchForm.taskNo?.trim() || undefined,
      taskName: taskSearchForm.taskName?.trim() || undefined,
      taskStatus: taskSearchForm.taskStatus || undefined,
      'params[beginTime]': beginTime,
      'params[endTime]': endTime
    }
  }

  const handleTaskSearch = () => {
    Object.assign(taskSearchParams, buildTaskQueryParams())
    getTaskData()
  }

  const handleTaskReset = () => {
    Object.assign(taskSearchForm, taskInitialSearchState)
    resetTaskSearchParams()
  }

  const handleSelectTask = async (row: InventoryTaskRow) => {
    currentTask.value = row
    await loadTaskAssets(row)
  }

  const handleTaskRefresh = async () => {
    await refreshTaskData()
    const taskList = taskData.value as InventoryTaskRow[]
    if (!taskList.length) {
      currentTask.value = undefined
      taskAssets.value = []
      return
    }

    const currentTaskId = currentTask.value?.taskId
    const matchedTask = taskList.find((item) => item.taskId === currentTaskId) || taskList[0]
    await handleSelectTask(matchedTask)
  }

  const buildCreateAssetQueryParams = () => {
    return {
      assetType: 'FIXED',
      assetCode: createAssetSearchForm.assetCode?.trim() || undefined,
      assetName: createAssetSearchForm.assetName?.trim() || undefined,
      assetStatus: createAssetSearchForm.assetStatus || undefined
    }
  }

  const clearCreateAssetSelection = () => {
    selectedCreateAssets.value = []
    createAssetTableRef.value?.elTableRef?.clearSelection?.()
  }

  const handleCreateAssetSearch = () => {
    Object.assign(createAssetSearchParams, buildCreateAssetQueryParams())
    getCreateAssetData()
  }

  const handleCreateAssetReset = () => {
    Object.assign(createAssetSearchForm, createAssetInitialSearchState)
    clearCreateAssetSelection()
    resetCreateAssetSearchParams()
  }

  const handleCreateAssetSelectionChange = (selection: InventoryTaskAssetRow[]) => {
    selectedCreateAssets.value = selection
  }

  const openCreateDialog = async () => {
    createDialogVisible.value = true
    await nextTick()
    handleCreateAssetSearch()
  }

  const resetCreateForm = () => {
    Object.assign(createFormData, {
      taskName: '',
      plannedDate: new Date().toISOString().slice(0, 10),
      remark: ''
    })
    createFormRef.value?.clearValidate()
    Object.assign(createAssetSearchForm, createAssetInitialSearchState)
    Object.assign(createAssetSearchParams, buildCreateAssetQueryParams())
    clearCreateAssetSelection()
  }

  const handleCreateDialogClosed = () => {
    resetCreateForm()
  }

  const handleCreateTaskSubmit = async () => {
    const valid = await createFormRef.value?.validate().catch(() => false)
    if (!valid) {
      return
    }
    if (!selectedCreateAssets.value.length) {
      ElMessage.warning('请至少选择一宗资产')
      return
    }

    createSubmitting.value = true
    try {
      const taskId = await addInventoryTask({
        taskName: createFormData.taskName.trim(),
        plannedDate: createFormData.plannedDate,
        assetIds: selectedCreateAssets.value.map((item) => item.assetId),
        remark: createFormData.remark?.trim() || undefined
      })
      ElMessage.success('盘点任务创建成功')
      createDialogVisible.value = false
      await refreshTaskData()
      const taskList = taskData.value as InventoryTaskRow[]
      const createdTask = taskList.find((item) => Number(item.taskId) === Number(taskId))
      if (createdTask) {
        await handleSelectTask(createdTask)
      }
    } finally {
      createSubmitting.value = false
    }
  }

  const openResultDialog = (row: InventoryTaskAssetRow) => {
    resultDialogAsset.value = row
    resultDialogVisible.value = true
  }

  const handleInventoryResultSubmit = async (
    payload: Omit<AssetInventoryResultPayload, 'taskId' | 'assetId'>
  ) => {
    const taskId = currentTask.value?.taskId
    const assetId = resultDialogAsset.value?.assetId
    if (!taskId || !assetId) {
      ElMessage.warning('请选择任务和资产后再提交')
      return
    }

    resultSubmitting.value = true
    try {
      await submitInventoryResult({
        ...payload,
        taskId,
        assetId
      })
      markAssetResultSubmitted(taskId, assetId)
      ElMessage.success('盘点结果提交成功')
      resultDialogVisible.value = false
      await loadTaskAssets(currentTask.value as InventoryTaskRow)
    } catch (error: any) {
      const message = String(error?.message || '')
      if (message.includes('已提交盘点结果')) {
        markAssetResultSubmitted(taskId, assetId)
      }
      throw error
    } finally {
      resultSubmitting.value = false
    }
  }

  watch(
    taskData,
    async (list) => {
      const taskList = (list || []) as InventoryTaskRow[]
      if (!taskList.length) {
        currentTask.value = undefined
        taskAssets.value = []
        return
      }

      const currentTaskId = currentTask.value?.taskId
      if (!currentTaskId) {
        await handleSelectTask(taskList[0])
        return
      }

      const matchedTask = taskList.find((item) => item.taskId === currentTaskId)
      if (!matchedTask) {
        await handleSelectTask(taskList[0])
      }
    },
    { immediate: true }
  )
</script>

<style scoped lang="scss">
  .asset-inventory-page {
    --asset-accent: #2f66ff;
    --asset-border: #e6ebf5;
    --asset-panel-bg: #fff;
    --asset-text-main: #18233a;
    --asset-text-secondary: #5d6b86;

    background:
      radial-gradient(circle at 0% 0%, rgb(47 102 255 / 8%), transparent 34%),
      radial-gradient(circle at 100% 0%, rgb(32 201 151 / 8%), transparent 36%),
      var(--art-main-bg-color);
  }

  .head-card,
  .task-card,
  .asset-card {
    border: 1px solid var(--asset-border);
    border-radius: 12px;
    background: var(--asset-panel-bg);
  }

  .page-title {
    font-size: 28px;
    font-weight: 700;
    color: var(--asset-text-main);
    line-height: 1.4;
  }

  .page-desc {
    margin-top: 6px;
    font-size: 14px;
    color: var(--asset-text-secondary);
    line-height: 1.6;
    max-width: 900px;
  }

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    flex-wrap: wrap;
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
</style>
