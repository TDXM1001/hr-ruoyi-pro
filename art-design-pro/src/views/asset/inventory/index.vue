<template>
  <div
    class="asset-inventory-center art-full-height flex flex-col gap-2 overflow-y-auto overflow-x-hidden p-3"
  >
    <ArtSearchBar
      v-model="searchForm"
      :items="searchItems"
      :showExpand="false"
      @search="handleSearch"
      @reset="handleReset"
    />

    <ElCard class="art-table-card flex-1 overflow-hidden task-card" shadow="never">
      <template #header>
        <div class="card-header">
          <div class="card-title">资产盘点任务</div>
          <ElSpace wrap class="toolbar-actions">
            <ElTag type="success" effect="light">资产类型：固定资产（一期）</ElTag>
            <ElButton
              type="primary"
              plain
              icon="ri:refresh-line"
              :loading="loading"
              @click="handleRefresh"
            >
              刷新任务
            </ElButton>
            <ElButton
              v-auth="'asset:inventory:add'"
              type="primary"
              icon="ri:add-line"
              @click="handleGoCreate"
              v-ripple
            >
              发起盘点任务
            </ElButton>
          </ElSpace>
        </div>
      </template>

      <ArtTable
        rowKey="taskId"
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { ElButton, ElProgress, ElTag } from 'element-plus'
  import { useRouter } from 'vue-router'
  import { listInventoryTask } from '@/api/asset/inventory'
  import { useTable } from '@/hooks/core/useTable'
  import { useUserStore } from '@/store/modules/user'

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
    scopeAssetCount?: number
    submittedCount?: number
    abnormalCount?: number
  }

  const router = useRouter()
  const userStore = useUserStore()

  const taskStatusOptions = [
    { label: '进行中', value: 'IN_PROGRESS' },
    { label: '已完成', value: 'COMPLETED' },
    { label: '草稿', value: 'DRAFT' }
  ]

  const hasPermission = (permission: string) => {
    return userStore.permissions.includes('*:*:*') || userStore.permissions.includes(permission)
  }

  // 中文注释：进入执行页默认要求具备盘点列表权限，登记动作在执行页再做 edit 权限二次校验。
  const canEnterTask = computed(() => hasPermission('asset:inventory:list'))

  const searchInitialState = {
    taskNo: '',
    taskName: '',
    taskStatus: '',
    daterange: undefined as string[] | undefined
  }
  const searchForm = reactive({ ...searchInitialState })

  const searchItems = computed(() => [
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

  const getTaskStatusTagType = (status?: string): 'primary' | 'success' | 'warning' | 'info' => {
    const mapper: Record<string, 'primary' | 'success' | 'warning' | 'info'> = {
      IN_PROGRESS: 'warning',
      COMPLETED: 'success',
      DRAFT: 'info'
    }
    return mapper[String(status || '').toUpperCase()] || 'info'
  }

  const getTaskStatusLabel = (status?: string) => {
    const match = taskStatusOptions.find(
      (item) => item.value === String(status || '').toUpperCase()
    )
    return match?.label || status || '-'
  }

  // 中文注释：兼容旧任务数据（无 scopeAssetCount 字段），回退到 scopeValue 解析数量。
  const getScopeAssetCount = (row: InventoryTaskRow) => {
    if (Number(row.scopeAssetCount) > 0) {
      return Number(row.scopeAssetCount)
    }
    if (!row.scopeValue) {
      return 0
    }
    const uniqueIdSet = new Set(
      String(row.scopeValue)
        .split(',')
        .map((item) => item.trim())
        .filter(Boolean)
    )
    return uniqueIdSet.size
  }

  const getSubmittedCount = (row: InventoryTaskRow) => {
    return Number(row.submittedCount || 0)
  }

  const getProgressPercent = (row: InventoryTaskRow) => {
    const total = getScopeAssetCount(row)
    if (!total) {
      return 0
    }
    return Math.min(100, Math.round((getSubmittedCount(row) / total) * 100))
  }

  const isOverdueTask = (row: InventoryTaskRow) => {
    if (String(row.taskStatus || '').toUpperCase() === 'COMPLETED') {
      return false
    }
    if (!row.plannedDate) {
      return false
    }
    const plannedDateTime = new Date(row.plannedDate).getTime()
    if (Number.isNaN(plannedDateTime)) {
      return false
    }
    const today = new Date()
    today.setHours(0, 0, 0, 0)
    return plannedDateTime < today.getTime()
  }

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
      apiFn: listInventoryTask,
      apiParams: {
        pageNum: 1,
        pageSize: 10
      },
      excludeParams: ['daterange'],
      columnsFactory: () => [
        { prop: 'taskNo', label: '任务编号', minWidth: 150 },
        { prop: 'taskName', label: '任务名称', minWidth: 220, showOverflowTooltip: true },
        {
          prop: 'taskStatus',
          label: '任务状态',
          width: 110,
          formatter: (row: InventoryTaskRow) =>
            h(ElTag, { type: getTaskStatusTagType(row.taskStatus), effect: 'light' }, () =>
              getTaskStatusLabel(row.taskStatus)
            )
        },
        {
          prop: 'progress',
          label: '登记进度',
          minWidth: 210,
          formatter: (row: InventoryTaskRow) => {
            const submitted = getSubmittedCount(row)
            const total = getScopeAssetCount(row)
            const abnormalCount = Number(row.abnormalCount || 0)
            return h('div', { class: 'progress-cell' }, [
              h(
                'div',
                { class: 'progress-text' },
                `已登记 ${submitted} / ${total}（异常 ${abnormalCount}）`
              ),
              h(ElProgress, {
                percentage: getProgressPercent(row),
                strokeWidth: 8,
                showText: false,
                status:
                  String(row.taskStatus || '').toUpperCase() === 'COMPLETED' ? 'success' : undefined
              })
            ])
          }
        },
        { prop: 'plannedDate', label: '计划盘点日', width: 120 },
        { prop: 'completedDate', label: '完成日期', width: 120 },
        {
          prop: 'overdue',
          label: '逾期标识',
          width: 100,
          formatter: (row: InventoryTaskRow) =>
            isOverdueTask(row)
              ? h(ElTag, { type: 'danger', effect: 'light' }, () => '已逾期')
              : h('span', null, '-')
        },
        { prop: 'createBy', label: '创建人', width: 100 },
        {
          prop: 'operation',
          label: '操作',
          width: 110,
          fixed: 'right',
          align: 'right',
          formatter: (row: InventoryTaskRow) => {
            if (!canEnterTask.value) {
              return '-'
            }
            return h(
              ElButton,
              {
                link: true,
                type: 'primary',
                onClick: () => handleGoExecute(row)
              },
              () => '进入执行'
            )
          }
        }
      ]
    }
  })

  const buildQueryParams = () => {
    const [beginTime, endTime] = Array.isArray(searchForm.daterange)
      ? searchForm.daterange
      : [undefined, undefined]

    return {
      taskNo: searchForm.taskNo?.trim() || undefined,
      taskName: searchForm.taskName?.trim() || undefined,
      taskStatus: searchForm.taskStatus || undefined,
      'params[beginTime]': beginTime,
      'params[endTime]': endTime
    }
  }

  const handleSearch = () => {
    Object.assign(searchParams, buildQueryParams())
    getData()
  }

  const handleReset = () => {
    Object.assign(searchForm, searchInitialState)
    resetSearchParams()
  }

  const handleGoCreate = () => {
    router.push('/asset/inventory/create')
  }

  const handleGoExecute = (row: InventoryTaskRow) => {
    router.push(`/asset/inventory/task/${row.taskId}`)
  }

  const handleRefresh = async () => {
    await refreshData()
  }
</script>

<style scoped lang="scss">
  .asset-inventory-center {
    --asset-accent: #2f66ff;
    --asset-border: #e6ebf5;
    --asset-panel-bg: #fff;
    --asset-text-main: #18233a;
    --asset-text-secondary: #5d6b86;

    background:
      radial-gradient(circle at 0% 0%, rgb(47 102 255 / 8%), transparent 34%),
      radial-gradient(circle at 100% 0%, rgb(32 201 151 / 8%), transparent 36%),
      var(--art-main-bg-color);
    min-height: 0;
    overflow-y: auto;
    overflow-x: hidden;
  }

  .task-card {
    background: var(--asset-panel-bg);
    min-height: 520px;
  }

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px 16px;
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

  .toolbar-actions {
    align-items: center;
  }

  :deep(.progress-cell) {
    width: 100%;
  }

  :deep(.progress-text) {
    margin-bottom: 4px;
    color: #5d6b86;
    font-size: 12px;
    line-height: 1.4;
  }

  .task-card {
    margin-top: 0;

    :deep(.el-card) {
      display: flex;
      flex-direction: column;
      height: 100%;
      min-height: 0;
    }

    :deep(.el-card__body) {
      display: flex;
      flex-direction: column;
      flex: 1;
      min-height: 0;
      height: auto;
      // 中文注释：底部预留安全区，避免分页器被工作台底部区域遮挡。
      padding: 0 16px 56px;
    }

    :deep(.art-table) {
      flex: 1;
      min-height: 0;
    }

    // 中文注释：覆盖表格组件默认固定高度，避免分页区域被底部裁切。
    :deep(.art-table .el-table) {
      margin-top: 0;
    }

    :deep(.el-card__header) {
      padding: 12px 16px;
      border-bottom: 1px solid #eaf0fb;
      background: linear-gradient(180deg, rgb(247 250 255 / 90%) 0%, #fff 100%);
    }
  }

  :deep(.art-search-bar) {
    padding: 10px 16px 0;
  }

  :deep(.art-search-bar .el-form-item) {
    margin-bottom: 10px;
  }

  :deep(.art-search-bar .action-column .action-buttons-wrapper) {
    margin-bottom: 10px;
  }

  @media (max-width: 768px) {
    .task-card {
      :deep(.el-card__body) {
        padding: 0 12px 40px;
      }
    }
  }
</style>
