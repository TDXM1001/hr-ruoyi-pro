<template>
  <div class="asset-ledger-page art-full-height flex flex-col gap-3 p-3">
    <ElAlert
      title="当前台账只开放固定资产建账，一期先不开放不动产切换；分类、部门、责任人均通过选择器录入。"
      type="info"
      :closable="false"
      show-icon
    />

    <div class="overview-grid">
      <ElCard v-for="item in overviewCards" :key="item.key" class="overview-card" shadow="never">
        <div class="overview-label">{{ item.label }}</div>
        <div class="overview-value">{{ formatCount(item.value) }}</div>
        <div class="overview-desc">{{ item.desc }}</div>
      </ElCard>
    </div>

    <ArtSearchBar
      :key="searchBarKey"
      v-model="searchForm"
      :items="formItems"
      :showExpand="true"
      @reset="handleReset"
      @search="handleSearch"
    />

    <ElCard class="art-table-card flex-1 overflow-hidden" shadow="never">
      <ArtTableHeader
        :showZebra="false"
        :loading="loading"
        v-model:columns="columnChecks"
        @refresh="handleTableRefresh"
      >
        <template #left>
          <ElSpace wrap>
            <ElButton v-auth="'asset:ledger:add'" type="primary" @click="handleAdd" v-ripple>
              新增资产
            </ElButton>
            <ElTag type="success" effect="light">资产类型：固定资产（一期）</ElTag>
          </ElSpace>
        </template>

        <template #right>
          <ElButton
            v-auth="'asset:ledger:export'"
            type="warning"
            plain
            icon="ri:download-line"
            :loading="exportLoading"
            @click="handleExport"
            v-ripple
          >
            导出台账
          </ElButton>
        </template>
      </ArtTableHeader>

      <ArtTable
        rowKey="assetId"
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
  import FileSaver from 'file-saver'
  import { ElButton, ElMessage } from 'element-plus'
  import type { AssetTreeOption, AssetUserOption } from '@/api/asset/ledger'
  import {
    listAssetLedger,
    exportAssetLedger,
    getAssetCategoryTree,
    getAssetDeptTree,
    listAssetResponsibleUsers
  } from '@/api/asset/ledger'
  import { getAssetOverviewStats } from '@/api/asset/stats'
  import { useTable } from '@/hooks/core/useTable'
  import { useDict } from '@/utils/dict'
  import { useUserStore } from '@/store/modules/user'
  import DictTag from '@/components/DictTag/index.vue'

  defineOptions({ name: 'AssetLedger' })

  const { ast_asset_status, ast_asset_source_type, ast_asset_acquire_type } = useDict(
    'ast_asset_status',
    'ast_asset_source_type',
    'ast_asset_acquire_type'
  )

  const router = useRouter()
  const userStore = useUserStore()

  const exportLoading = ref(false)
  // 中文注释：台账概览卡片数据，来源于后端聚合统计接口，作为管理层看板口径。
  const overview = reactive({
    totalCount: 0,
    inUseCount: 0,
    idleCount: 0,
    inventoryingCount: 0,
    pendingDisposalCount: 0,
    overdueInventoryCount: 0
  })
  const categoryOptions = ref<AssetTreeOption[]>([])
  const deptOptions = ref<AssetTreeOption[]>([])
  const responsibleUserOptions = ref<AssetUserOption[]>([])
  const responsibleUserLoading = ref(false)

  const initialSearchState = {
    assetCode: '',
    assetName: '',
    categoryId: undefined as number | undefined,
    ownerDeptId: undefined as number | undefined,
    useDeptId: undefined as number | undefined,
    responsibleUserId: undefined as number | undefined,
    assetStatus: '',
    sourceType: '',
    acquireType: '',
    daterange: undefined as string[] | undefined
  }

  const searchForm = reactive({ ...initialSearchState })

  const treeSelectProps = {
    value: 'id',
    label: 'label',
    children: 'children',
    disabled: 'disabled'
  }

  // 中文注释：依赖项变化后重建搜索栏，确保远程字典/树数据更新能即时反映到筛选控件。
  const searchBarKey = computed(() => {
    return (
      categoryOptions.value.length +
      deptOptions.value.length +
      responsibleUserOptions.value.length +
      ast_asset_status.value.length +
      ast_asset_source_type.value.length +
      ast_asset_acquire_type.value.length
    )
  })

  const overviewCards = computed(() => [
    {
      key: 'totalCount',
      label: '资产总量',
      value: overview.totalCount,
      desc: '固定资产当前总宗数'
    },
    {
      key: 'inUseCount',
      label: '使用中',
      value: overview.inUseCount,
      desc: '当前正常在用资产'
    },
    {
      key: 'idleCount',
      label: '闲置中',
      value: overview.idleCount,
      desc: '当前未分配使用资产'
    },
    {
      key: 'inventoryingCount',
      label: '盘点中',
      value: overview.inventoryingCount,
      desc: '已纳入盘点流程资产'
    },
    {
      key: 'pendingDisposalCount',
      label: '待处置',
      value: overview.pendingDisposalCount,
      desc: '待确认处置资产数量'
    },
    {
      key: 'overdueInventoryCount',
      label: '逾期待盘点',
      value: overview.overdueInventoryCount,
      desc: '未按期完成盘点任务'
    }
  ])

  const formItems = computed(() => [
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
      label: '资产分类',
      key: 'categoryId',
      type: 'treeselect',
      props: {
        data: categoryOptions.value,
        props: treeSelectProps,
        valueKey: 'id',
        clearable: true,
        checkStrictly: true,
        filterable: true,
        placeholder: '请选择资产分类',
        renderAfterExpand: false
      }
    },
    {
      label: '权属部门',
      key: 'ownerDeptId',
      type: 'treeselect',
      props: {
        data: deptOptions.value,
        props: treeSelectProps,
        valueKey: 'id',
        clearable: true,
        checkStrictly: true,
        filterable: true,
        placeholder: '请选择权属部门',
        renderAfterExpand: false
      }
    },
    {
      label: '使用部门',
      key: 'useDeptId',
      type: 'treeselect',
      props: {
        data: deptOptions.value,
        props: treeSelectProps,
        valueKey: 'id',
        clearable: true,
        checkStrictly: true,
        filterable: true,
        placeholder: '请选择使用部门',
        renderAfterExpand: false
      }
    },
    {
      label: '责任人',
      key: 'responsibleUserId',
      type: 'select',
      props: {
        placeholder: '请输入责任人姓名搜索',
        clearable: true,
        filterable: true,
        remote: true,
        reserveKeyword: true,
        loading: responsibleUserLoading.value,
        remoteMethod: handleResponsibleUserSearch,
        options: responsibleUserOptions.value
      }
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
      label: '录入来源',
      key: 'sourceType',
      type: 'select',
      props: {
        placeholder: '请选择录入来源',
        clearable: true,
        options: ast_asset_source_type.value
      }
    },
    {
      label: '取得方式',
      key: 'acquireType',
      type: 'select',
      props: {
        placeholder: '请选择取得方式',
        clearable: true,
        options: ast_asset_acquire_type.value
      }
    },
    {
      label: '建账日期',
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

  const hasPermission = (permission: string) => {
    return userStore.permissions.includes('*:*:*') || userStore.permissions.includes(permission)
  }

  // 中文注释：详情与编辑按钮分别受查询/编辑权限控制，避免越权访问路由页面。
  const canQuery = computed(() => hasPermission('asset:ledger:query'))
  const canEdit = computed(() => hasPermission('asset:ledger:edit'))

  const {
    columns,
    columnChecks,
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
      apiFn: listAssetLedger,
      apiParams: {
        assetType: 'FIXED',
        pageNum: 1,
        pageSize: 10
      },
      excludeParams: ['daterange'],
      columnsFactory: () => [
        { prop: 'assetCode', label: '资产编码', minWidth: 150 },
        { prop: 'assetName', label: '资产名称', minWidth: 180 },
        {
          prop: 'assetType',
          label: '资产类型',
          width: 120,
          formatter: () => '固定资产'
        },
        { prop: 'categoryName', label: '资产分类', minWidth: 150 },
        {
          prop: 'assetStatus',
          label: '资产状态',
          width: 120,
          formatter: (row: any) => {
            return h(DictTag, { options: ast_asset_status.value, value: row.assetStatus })
          }
        },
        {
          prop: 'sourceType',
          label: '录入来源',
          width: 120,
          formatter: (row: any) => {
            return h(DictTag, { options: ast_asset_source_type.value, value: row.sourceType })
          }
        },
        {
          prop: 'acquireType',
          label: '取得方式',
          width: 120,
          formatter: (row: any) => {
            return h(DictTag, { options: ast_asset_acquire_type.value, value: row.acquireType })
          }
        },
        { prop: 'ownerDeptName', label: '权属部门', minWidth: 130 },
        { prop: 'useDeptName', label: '使用部门', minWidth: 130 },
        { prop: 'responsibleUserName', label: '责任人', width: 120 },
        {
          prop: 'originalValue',
          label: '原值(元)',
          width: 120,
          formatter: (row: any) => formatMoney(row.originalValue)
        },
        { prop: 'enableDate', label: '启用日期', width: 120 },
        { prop: 'createTime', label: '建账时间', width: 170 },
        {
          prop: 'operation',
          label: '操作',
          width: 150,
          fixed: 'right',
          align: 'right',
          formatter: (row: any) => {
            const actions = []
            if (canQuery.value) {
              actions.push(
                h(
                  ElButton,
                  {
                    link: true,
                    type: 'primary',
                    onClick: () => handleDetail(row)
                  },
                  () => '详情'
                )
              )
            }
            if (canEdit.value) {
              actions.push(
                h(
                  ElButton,
                  {
                    link: true,
                    type: 'primary',
                    onClick: () => handleEdit(row)
                  },
                  () => '编辑'
                )
              )
            }

            return h(
              'div',
              {
                class: 'flex justify-end gap-1'
              },
              actions
            )
          }
        }
      ]
    }
  })

  const toArrayData = <T,>(response: any): T[] => {
    if (Array.isArray(response)) {
      return response
    }
    return response?.data || []
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

  const formatCount = (value?: number | string) => {
    if (value === null || value === undefined || value === '') {
      return '0'
    }
    return Number(value).toLocaleString('zh-CN')
  }

  const loadOverview = async () => {
    const response = await getAssetOverviewStats()
    Object.assign(overview, response || {})
  }

  const loadCategoryTree = async () => {
    const response = await getAssetCategoryTree()
    categoryOptions.value = toArrayData<AssetTreeOption>(response)
  }

  const loadDeptTree = async () => {
    const response = await getAssetDeptTree()
    deptOptions.value = toArrayData<AssetTreeOption>(response)
  }

  const handleResponsibleUserSearch = async (keyword = '') => {
    responsibleUserLoading.value = true
    try {
      const response = await listAssetResponsibleUsers({ keyword: keyword.trim() })
      responsibleUserOptions.value = toArrayData<AssetUserOption>(response)
    } finally {
      responsibleUserLoading.value = false
    }
  }

  // 中文注释：统一构造查询参数，保持列表查询、导出、刷新三者口径一致。
  const buildQueryParams = () => {
    const [beginTime, endTime] = Array.isArray(searchForm.daterange)
      ? searchForm.daterange
      : [undefined, undefined]

    return {
      assetType: 'FIXED',
      assetCode: searchForm.assetCode,
      assetName: searchForm.assetName,
      categoryId: searchForm.categoryId,
      ownerDeptId: searchForm.ownerDeptId,
      useDeptId: searchForm.useDeptId,
      responsibleUserId: searchForm.responsibleUserId,
      assetStatus: searchForm.assetStatus,
      sourceType: searchForm.sourceType,
      acquireType: searchForm.acquireType,
      'params[beginTime]': beginTime,
      'params[endTime]': endTime
    }
  }

  // 中文注释：useTable 内部使用 searchParams 触发查询，先同步再拉取数据。
  const syncSearchParams = () => {
    Object.assign(searchParams, buildQueryParams())
  }

  const handleSearch = () => {
    syncSearchParams()
    getData()
  }

  const handleReset = () => {
    Object.assign(searchForm, initialSearchState)
    resetSearchParams()
  }

  const handleAdd = () => {
    router.push('/asset/ledger/create')
  }

  const handleEdit = (row: any) => {
    router.push(`/asset/ledger/edit/${row.assetId}`)
  }

  const handleDetail = (row: any) => {
    router.push(`/asset/ledger/detail/${row.assetId}`)
  }

  // 中文注释：导出沿用当前筛选条件，避免“页面看到什么、导出却不是同一口径”。
  const handleExport = async () => {
    exportLoading.value = true
    try {
      const blob = await exportAssetLedger(buildQueryParams())
      FileSaver.saveAs(blob, `资产台账-${new Date().toISOString().slice(0, 10)}.xlsx`)
      ElMessage.success('资产台账导出成功')
    } catch (error) {
      console.error('导出资产台账失败:', error)
    } finally {
      exportLoading.value = false
    }
  }

  // 中文注释：台账刷新时同步拉取统计卡片，避免表格与概览口径不一致。
  const handleTableRefresh = async () => {
    await Promise.all([refreshData(), loadOverview()])
  }

  onMounted(async () => {
    await Promise.all([
      loadCategoryTree(),
      loadDeptTree(),
      handleResponsibleUserSearch(),
      loadOverview()
    ])
  })
</script>

<style scoped lang="scss">
  .asset-ledger-page {
    background-color: transparent;
  }

  .overview-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
    gap: 12px;
  }

  .overview-card {
    border-radius: 12px;
    border: 1px solid #e8edf7;
    background: linear-gradient(180deg, rgb(247 250 255 / 92%) 0%, #fff 72%);
  }

  .overview-label {
    font-size: 13px;
    color: #6b7892;
    line-height: 1.4;
  }

  .overview-value {
    margin-top: 6px;
    font-size: 28px;
    font-weight: 700;
    color: #1d2f4f;
    line-height: 1.2;
  }

  .overview-desc {
    margin-top: 6px;
    font-size: 12px;
    color: #8a97ad;
  }
</style>
