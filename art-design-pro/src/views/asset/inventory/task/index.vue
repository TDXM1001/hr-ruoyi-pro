<template>
  <div class="asset-inventory-task art-full-height flex flex-col gap-3 overflow-y-auto p-3">
    <ElCard class="head-card" shadow="never">
      <div class="flex flex-wrap items-start justify-between gap-3">
        <div>
          <ElLink type="primary" :underline="false" @click="goBack">返回资产盘点</ElLink>
          <div class="page-title">盘点执行</div>
          <div class="page-desc"
            >按任务逐条登记盘点结果，异常资产必须指定后续动作，系统自动回写台账状态。</div
          >
        </div>
        <ElSpace wrap>
          <ElTag type="info" effect="light">任务：{{ taskMeta.taskNo || '-' }}</ElTag>
          <ElTag :type="isOverdueTask ? 'danger' : 'success'" effect="light">
            {{ isOverdueTask ? '任务逾期' : '任务正常' }}
          </ElTag>
        </ElSpace>
      </div>
      <div class="mt-3 progress-wrapper">
        <div class="progress-meta">
          <span>登记进度：{{ submittedCount }} / {{ scopeAssetCount }}</span>
          <span>异常：{{ abnormalCount }}</span>
        </div>
        <ElProgress :percentage="progressPercent" :stroke-width="10" />
      </div>
    </ElCard>

    <ElCard class="main-card flex-1 min-h-0" shadow="never">
      <template #header>
        <div class="card-header">
          <div class="card-title">任务资产清单</div>
          <ElSpace wrap>
            <ElTag type="info" effect="light">当前页 {{ data.length }} 宗</ElTag>
            <ElTag type="primary" effect="light">批量已选 {{ selectedAssetRows.length }} 宗</ElTag>
            <ElButton type="warning" :disabled="!canEditInventoryResult" @click="openBatchDrawer">
              批量登记正常
            </ElButton>
            <ElButton
              type="primary"
              plain
              icon="ri:refresh-line"
              :loading="loading"
              @click="handleRefresh"
            >
              刷新
            </ElButton>
          </ElSpace>
        </div>
      </template>

      <ArtSearchBar
        v-model="searchForm"
        :items="searchItems"
        :showExpand="true"
        @search="handleSearch"
        @reset="handleReset"
      />

      <ArtTable
        ref="tableRef"
        rowKey="assetId"
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
        :height="560"
        @selection-change="handleSelectionChange"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>

    <InventoryResultDrawer
      v-model="drawerVisible"
      :mode="drawerMode"
      :loading="drawerSubmitting"
      :task-title="drawerTaskTitle"
      :asset-title="drawerAssetTitle"
      :batch-count="selectedAssetRows.length"
      @submit="handleDrawerSubmit"
    />
  </div>
</template>

<script setup lang="ts">
  import type {
    AssetInventoryResultPayload,
    AssetInventoryTaskAssetQuery
  } from '@/api/asset/inventory'
  import { ElButton, ElMessage, ElTag } from 'element-plus'
  import { useRoute, useRouter } from 'vue-router'
  import {
    getInventoryTask,
    listInventoryTask,
    listInventoryTaskAssets,
    submitInventoryResult
  } from '@/api/asset/inventory'
  import DictTag from '@/components/DictTag/index.vue'
  import { useTable } from '@/hooks/core/useTable'
  import { useDict } from '@/utils/dict'
  import { useUserStore } from '@/store/modules/user'
  import InventoryResultDrawer from '../modules/inventory-result-drawer.vue'

  defineOptions({ name: 'AssetInventoryTaskExecute' })

  type InventoryTaskMeta = {
    taskId?: number
    taskNo?: string
    taskName?: string
    taskStatus?: string
    scopeValue?: string
    plannedDate?: string
    scopeAssetCount?: number
    submittedCount?: number
    abnormalCount?: number
  }

  type TaskAssetRow = {
    taskId: number
    assetId: number
    assetCode?: string
    assetName?: string
    assetStatus?: string
    ownerDeptName?: string
    useDeptName?: string
    responsibleUserName?: string
    locationName?: string
    resultRegistered?: number
    inventoryResult?: string
    followUpAction?: string
    processStatus?: string
    checkedBy?: string
    checkedTime?: string
  }

  type DrawerMode = 'single' | 'batch'

  const route = useRoute()
  const router = useRouter()
  const userStore = useUserStore()
  const { ast_asset_status } = useDict('ast_asset_status')

  const taskId = computed(() => Number(route.params.taskId))
  const taskMeta = reactive<InventoryTaskMeta>({})

  const hasPermission = (permission: string) => {
    return userStore.permissions.includes('*:*:*') || userStore.permissions.includes(permission)
  }

  // 中文注释：结果登记属于写动作，执行页可访问但提交能力需要 edit 权限单独控制。
  const canEditInventoryResult = computed(() => hasPermission('asset:inventory:edit'))

  const inventoryResultLabelMap: Record<string, string> = {
    NORMAL: '正常',
    LOSS: '盘亏',
    MISSING: '缺失',
    DAMAGED: '毁损'
  }

  const followUpActionLabelMap: Record<string, string> = {
    NONE: '无',
    UPDATE_LEDGER: '台账修正',
    CREATE_DISPOSAL: '发起处置'
  }

  const scopeAssetCount = computed(() => {
    const scopeCount = Number(taskMeta.scopeAssetCount || 0)
    if (scopeCount > 0) {
      return scopeCount
    }
    const scopeValue = String(taskMeta.scopeValue || '')
    if (!scopeValue) {
      return 0
    }
    return new Set(
      scopeValue
        .split(',')
        .map((item) => item.trim())
        .filter(Boolean)
    ).size
  })

  const submittedCount = computed(() => Number(taskMeta.submittedCount || 0))
  const abnormalCount = computed(() => Number(taskMeta.abnormalCount || 0))

  const progressPercent = computed(() => {
    if (!scopeAssetCount.value) {
      return 0
    }
    return Math.min(100, Math.round((submittedCount.value / scopeAssetCount.value) * 100))
  })

  const isOverdueTask = computed(() => {
    if (String(taskMeta.taskStatus || '').toUpperCase() === 'COMPLETED') {
      return false
    }
    if (!taskMeta.plannedDate) {
      return false
    }
    const plannedDateTime = new Date(taskMeta.plannedDate).getTime()
    if (Number.isNaN(plannedDateTime)) {
      return false
    }
    const today = new Date()
    today.setHours(0, 0, 0, 0)
    return plannedDateTime < today.getTime()
  })

  const searchInitialState = {
    assetCode: '',
    assetName: '',
    assetStatus: '',
    resultType: 'ALL'
  }
  const searchForm = reactive({ ...searchInitialState })

  const resultTypeOptions = [
    { label: '全部', value: 'ALL' },
    { label: '未登记', value: 'PENDING' },
    { label: '异常结果', value: 'ABNORMAL' },
    { label: '已登记', value: 'REGISTERED' }
  ]

  const searchItems = computed(() => [
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
    },
    {
      label: '登记筛选',
      key: 'resultType',
      type: 'select',
      props: {
        placeholder: '请选择登记筛选',
        clearable: false,
        options: resultTypeOptions
      }
    }
  ])

  const {
    columns,
    data,
    loading,
    pagination,
    getData,
    searchParams,
    resetSearchParams,
    handleSizeChange,
    handleCurrentChange,
    refreshData
  } = useTable({
    core: {
      apiFn: (params: AssetInventoryTaskAssetQuery) =>
        listInventoryTaskAssets(taskId.value, params),
      apiParams: {
        pageNum: 1,
        pageSize: 10
      },
      immediate: false,
      columnsFactory: () => [
        {
          type: 'selection',
          width: 48,
          align: 'center',
          selectable: (row: TaskAssetRow) =>
            canEditInventoryResult.value && !Number(row.resultRegistered)
        },
        { prop: 'assetCode', label: '资产编码', minWidth: 140 },
        { prop: 'assetName', label: '资产名称', minWidth: 180, showOverflowTooltip: true },
        {
          prop: 'assetStatus',
          label: '资产状态',
          width: 120,
          formatter: (row: TaskAssetRow) =>
            h(DictTag, { options: ast_asset_status.value, value: row.assetStatus })
        },
        { prop: 'ownerDeptName', label: '权属部门', minWidth: 120 },
        { prop: 'useDeptName', label: '使用部门', minWidth: 120 },
        { prop: 'responsibleUserName', label: '责任人', width: 110 },
        {
          prop: 'inventoryResult',
          label: '盘点结果',
          width: 130,
          formatter: (row: TaskAssetRow) => {
            if (!Number(row.resultRegistered)) {
              return h(ElTag, { type: 'info', effect: 'light' }, () => '未登记')
            }
            const resultCode = String(row.inventoryResult || '').toUpperCase()
            const isAbnormal = ['LOSS', 'MISSING', 'DAMAGED'].includes(resultCode)
            return h(
              ElTag,
              { type: isAbnormal ? 'danger' : 'success', effect: 'light' },
              () => inventoryResultLabelMap[resultCode] || row.inventoryResult || '-'
            )
          }
        },
        {
          prop: 'followUpAction',
          label: '后续动作',
          width: 110,
          formatter: (row: TaskAssetRow) => {
            if (!Number(row.resultRegistered)) {
              return '-'
            }
            const code = String(row.followUpAction || '').toUpperCase()
            return followUpActionLabelMap[code] || row.followUpAction || '-'
          }
        },
        { prop: 'checkedBy', label: '登记人', width: 100 },
        { prop: 'checkedTime', label: '登记时间', minWidth: 160 },
        {
          prop: 'operation',
          label: '操作',
          width: 96,
          fixed: 'right',
          align: 'right',
          formatter: (row: TaskAssetRow) => {
            if (!canEditInventoryResult.value || Number(row.resultRegistered)) {
              return '-'
            }
            return h(
              ElButton,
              {
                link: true,
                type: 'primary',
                onClick: () => openSingleDrawer(row)
              },
              () => '登记结果'
            )
          }
        }
      ]
    }
  })

  const tableRef = ref<any>()
  const selectedAssetMap = ref<Record<number, TaskAssetRow>>({})
  const selectedAssetRows = computed(() => Object.values(selectedAssetMap.value))

  const drawerVisible = ref(false)
  const drawerMode = ref<DrawerMode>('single')
  const drawerSubmitting = ref(false)
  const currentDrawerAsset = ref<TaskAssetRow>()

  const drawerTaskTitle = computed(() => {
    return [taskMeta.taskNo, taskMeta.taskName].filter(Boolean).join(' / ')
  })

  const drawerAssetTitle = computed(() => {
    if (!currentDrawerAsset.value) {
      return ''
    }
    return [currentDrawerAsset.value.assetCode, currentDrawerAsset.value.assetName]
      .filter(Boolean)
      .join(' / ')
  })

  const toRows = <T,>(response: any): T[] => {
    if (Array.isArray(response?.rows)) {
      return response.rows as T[]
    }
    if (Array.isArray(response?.data?.rows)) {
      return response.data.rows as T[]
    }
    return []
  }

  const toRecord = <T,>(response: any): T | undefined => {
    if (response?.data && !Array.isArray(response.data)) {
      return response.data as T
    }
    if (!Array.isArray(response) && typeof response === 'object' && !('rows' in response)) {
      return response as T
    }
    return undefined
  }

  const buildQueryParams = () => {
    return {
      assetCode: searchForm.assetCode?.trim() || undefined,
      assetName: searchForm.assetName?.trim() || undefined,
      assetStatus: searchForm.assetStatus || undefined,
      resultType: searchForm.resultType || 'ALL'
    }
  }

  const syncPageSelection = () => {
    nextTick(() => {
      const pageRows = (data.value || []) as TaskAssetRow[]
      const elTableRef = tableRef.value?.elTableRef
      if (!elTableRef) {
        return
      }
      elTableRef.clearSelection?.()
      pageRows.forEach((row) => {
        if (selectedAssetMap.value[row.assetId] && !Number(row.resultRegistered)) {
          elTableRef.toggleRowSelection?.(row, true)
        }
      })
    })
  }

  // 中文注释：保持跨页勾选，且自动剔除“已登记”资产，防止批量提交重复。
  const handleSelectionChange = (selection: TaskAssetRow[]) => {
    const nextMap = { ...selectedAssetMap.value }
    const pageRows = (data.value || []) as TaskAssetRow[]
    pageRows.forEach((row) => {
      delete nextMap[row.assetId]
    })
    selection
      .filter((row) => !Number(row.resultRegistered))
      .forEach((row) => {
        nextMap[row.assetId] = row
      })
    selectedAssetMap.value = nextMap
  }

  const clearSelection = () => {
    selectedAssetMap.value = {}
    tableRef.value?.elTableRef?.clearSelection?.()
  }

  const handleSearch = () => {
    Object.assign(searchParams, buildQueryParams())
    getData()
  }

  const handleReset = () => {
    Object.assign(searchForm, searchInitialState)
    clearSelection()
    resetSearchParams()
  }

  const loadTaskMeta = async () => {
    const summaryResponse = await listInventoryTask({
      taskId: taskId.value,
      pageNum: 1,
      pageSize: 1
    })
    const summaryRow = toRows<InventoryTaskMeta>(summaryResponse)[0]
    if (summaryRow) {
      Object.assign(taskMeta, summaryRow)
      return
    }
    // 中文注释：兼容历史后端场景，若列表未返回该任务，再回退读取详情基础信息。
    const detailResponse = await getInventoryTask(taskId.value)
    const detailRecord = toRecord<InventoryTaskMeta>(detailResponse)
    if (detailRecord) {
      Object.assign(taskMeta, detailRecord)
    }
  }

  const refreshTaskPage = async () => {
    await Promise.all([refreshData(), loadTaskMeta()])
    syncPageSelection()
  }

  const submitForAsset = async (
    assetId: number,
    payload: Omit<AssetInventoryResultPayload, 'taskId' | 'assetId'>
  ) => {
    try {
      await submitInventoryResult({
        ...payload,
        taskId: taskId.value,
        assetId
      })
      return { success: true as const }
    } catch (error: any) {
      const message = String(error?.message || '')
      if (message.includes('已提交盘点结果')) {
        return { success: true as const, duplicated: true as const }
      }
      return { success: false as const, message: message || '提交失败' }
    }
  }

  const openSingleDrawer = (row: TaskAssetRow) => {
    currentDrawerAsset.value = row
    drawerMode.value = 'single'
    drawerVisible.value = true
  }

  const openBatchDrawer = () => {
    if (!selectedAssetRows.value.length) {
      ElMessage.warning('请先勾选未登记资产后再批量提交')
      return
    }
    drawerMode.value = 'batch'
    currentDrawerAsset.value = undefined
    drawerVisible.value = true
  }

  const handleDrawerSubmit = async (
    payload: Omit<AssetInventoryResultPayload, 'taskId' | 'assetId'>
  ) => {
    drawerSubmitting.value = true
    try {
      if (drawerMode.value === 'single') {
        if (!currentDrawerAsset.value?.assetId) {
          ElMessage.warning('未识别到待登记资产')
          return
        }
        const result = await submitForAsset(currentDrawerAsset.value.assetId, payload)
        if (!result.success) {
          ElMessage.error(result.message)
          return
        }
        ElMessage.success(result.duplicated ? '该资产已登记，已同步刷新列表' : '盘点结果提交成功')
        drawerVisible.value = false
        await refreshTaskPage()
        return
      }

      // 中文注释：批量提交只处理当前已勾选且未登记资产，逐条调用后端以复用既有闭环校验。
      const selectedIds = selectedAssetRows.value.map((item) => item.assetId)
      const settled = await Promise.all(
        selectedIds.map((assetId) => submitForAsset(assetId, payload))
      )
      const successCount = settled.filter((item) => item.success).length
      const failedItems = settled.filter((item) => !item.success)

      if (!failedItems.length) {
        ElMessage.success(`批量登记成功，共 ${successCount} 宗`)
      } else {
        ElMessage.warning(`批量登记完成：成功 ${successCount} 宗，失败 ${failedItems.length} 宗`)
        const firstFailure = failedItems[0] as { success: false; message: string }
        ElMessage.error(`失败原因示例：${firstFailure.message}`)
      }

      drawerVisible.value = false
      clearSelection()
      await refreshTaskPage()
    } finally {
      drawerSubmitting.value = false
    }
  }

  const goBack = () => {
    router.push('/asset/inventory')
  }

  const handleRefresh = async () => {
    await refreshTaskPage()
  }

  watch(
    data,
    () => {
      syncPageSelection()
    },
    { immediate: true }
  )

  watch(
    () => route.params.taskId,
    async () => {
      if (!taskId.value || Number.isNaN(taskId.value)) {
        ElMessage.error('盘点任务ID无效')
        router.replace('/asset/inventory')
        return
      }
      clearSelection()
      Object.assign(searchParams, buildQueryParams())
      await refreshTaskPage()
    },
    { immediate: true }
  )
</script>

<style scoped lang="scss">
  .asset-inventory-task {
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
  .main-card {
    border: 1px solid var(--asset-border);
    border-radius: 12px;
    background: var(--asset-panel-bg);
  }

  .page-title {
    margin-top: 6px;
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

  .progress-wrapper {
    border-top: 1px dashed #e7edf8;
    padding-top: 12px;
  }

  .progress-meta {
    display: flex;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 8px;
    font-size: 13px;
    color: #5d6b86;
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
