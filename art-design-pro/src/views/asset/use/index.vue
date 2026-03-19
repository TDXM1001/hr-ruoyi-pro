<template>
  <div class="asset-use-page art-full-height flex flex-col gap-3 overflow-hidden p-3">
    <ElCard class="head-card" shadow="never">
      <div class="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
        <div class="flex flex-col gap-2">
          <div class="page-title">资产使用</div>
          <div class="page-desc">
            面向资产管理员提供领用、调拨、退还的一体化页面，严格按“先选资产、再建交接单、最后留痕回写台账”的闭环执行。
          </div>
        </div>
        <div class="meta-tags flex flex-wrap items-center gap-2">
          <ElTag type="success" effect="light">资产类型：固定资产（一期）</ElTag>
          <ElTag type="info" effect="light">单据策略：整单校验 + 整单提交</ElTag>
        </div>
      </div>
    </ElCard>

    <ElCard class="asset-pool-card min-h-0 overflow-hidden" shadow="never">
      <template #header>
        <div class="card-header">
          <div class="card-title">可交接资产池</div>
          <ElSpace wrap>
            <ElTag type="info" effect="light">已选 {{ selectedAssetRows.length }} 宗</ElTag>
            <ElButton
              v-auth="'asset:handover:add'"
              type="primary"
              @click="openHandoverDialog('ASSIGN')"
              v-ripple
            >
              发起领用
            </ElButton>
            <ElButton
              v-auth="'asset:handover:add'"
              type="warning"
              plain
              @click="openHandoverDialog('TRANSFER')"
              v-ripple
            >
              发起调拨
            </ElButton>
            <ElButton
              v-auth="'asset:handover:add'"
              type="danger"
              plain
              @click="openHandoverDialog('RETURN')"
              v-ripple
            >
              发起退还
            </ElButton>
          </ElSpace>
        </div>
      </template>

      <ArtSearchBar
        :key="assetSearchBarKey"
        v-model="assetSearchForm"
        :items="assetSearchItems"
        :showExpand="true"
        @search="handleAssetSearch"
        @reset="handleAssetReset"
      />

      <ArtTable
        v-model:selection="selectedAssetRows"
        rowKey="assetId"
        :loading="assetLoading"
        :data="assetData"
        :columns="assetColumns"
        :pagination="assetPagination"
        @pagination:size-change="handleAssetSizeChange"
        @pagination:current-change="handleAssetCurrentChange"
      />
    </ElCard>

    <ElCard class="order-card flex-1 min-h-0 overflow-hidden" shadow="never">
      <template #header>
        <div class="card-header">
          <div class="card-title">交接单记录</div>
          <ElButton
            type="primary"
            plain
            icon="ri:refresh-line"
            :loading="orderLoading"
            @click="refreshOrderData"
          >
            刷新记录
          </ElButton>
        </div>
      </template>

      <ArtSearchBar
        :key="orderSearchBarKey"
        v-model="orderSearchForm"
        :items="orderSearchItems"
        :showExpand="true"
        @search="handleOrderSearch"
        @reset="handleOrderReset"
      />

      <ArtTable
        rowKey="handoverOrderId"
        :loading="orderLoading"
        :data="orderData"
        :columns="orderColumns"
        :pagination="orderPagination"
        @pagination:size-change="handleOrderSizeChange"
        @pagination:current-change="handleOrderCurrentChange"
      />
    </ElCard>

    <HandoverDialog
      v-model="handoverDialogVisible"
      :handover-type="currentHandoverType"
      :selected-asset-ids="selectedAssetIds"
      :selected-assets="selectedAssetRows"
      @success="handleHandoverSuccess"
    />

    <ElDrawer v-model="detailDrawerVisible" title="交接单明细" size="920px" destroy-on-close>
      <template v-if="currentOrder">
        <ElDescriptions :column="2" border class="mb-4">
          <ElDescriptionsItem label="交接单号">{{
            currentOrder.handoverNo || '-'
          }}</ElDescriptionsItem>
          <ElDescriptionsItem label="交接类型">
            <ElTag :type="getHandoverTagType(currentOrder.handoverType)" effect="light">
              {{ getHandoverLabel(currentOrder.handoverType) }}
            </ElTag>
          </ElDescriptionsItem>
          <ElDescriptionsItem label="交接日期">{{
            currentOrder.handoverDate || '-'
          }}</ElDescriptionsItem>
          <ElDescriptionsItem label="资产数量">{{
            currentOrder.assetCount || 0
          }}</ElDescriptionsItem>
          <ElDescriptionsItem label="目标部门">{{
            currentOrder.toDeptName || '-'
          }}</ElDescriptionsItem>
          <ElDescriptionsItem label="目标责任人">{{
            currentOrder.toUserName || '-'
          }}</ElDescriptionsItem>
        </ElDescriptions>
      </template>

      <ElTable v-loading="itemLoading" :data="orderItems" border height="calc(100vh - 330px)">
        <ElTableColumn prop="assetCode" label="资产编码" min-width="150" />
        <ElTableColumn prop="assetName" label="资产名称" min-width="180" show-overflow-tooltip />
        <ElTableColumn
          prop="fromDeptName"
          label="交接前部门"
          min-width="140"
          show-overflow-tooltip
        />
        <ElTableColumn
          prop="fromUserName"
          label="交接前责任人"
          min-width="120"
          show-overflow-tooltip
        />
        <ElTableColumn prop="toDeptName" label="交接后部门" min-width="140" show-overflow-tooltip />
        <ElTableColumn
          prop="toUserName"
          label="交接后责任人"
          min-width="120"
          show-overflow-tooltip
        />
        <ElTableColumn prop="beforeStatus" label="交接前状态" width="120">
          <template #default="{ row }">
            <DictTag :options="ast_asset_status" :value="row.beforeStatus" />
          </template>
        </ElTableColumn>
        <ElTableColumn prop="afterStatus" label="交接后状态" width="120">
          <template #default="{ row }">
            <DictTag :options="ast_asset_status" :value="row.afterStatus" />
          </template>
        </ElTableColumn>
      </ElTable>
    </ElDrawer>
  </div>
</template>

<script setup lang="ts">
  import { ElButton, ElMessage } from 'element-plus'
  import type { AssetTreeOption, AssetUserOption } from '@/api/asset/ledger'
  import { listAssetLedger, getAssetDeptTree, listAssetResponsibleUsers } from '@/api/asset/ledger'
  import { listAssetHandoverOrder, listAssetHandoverItems } from '@/api/asset/handover'
  import { useTable } from '@/hooks/core/useTable'
  import { useDict } from '@/utils/dict'
  import DictTag from '@/components/DictTag/index.vue'
  import HandoverDialog, {
    type HandoverType,
    type SelectedAssetItem
  } from './modules/handover-dialog.vue'

  defineOptions({ name: 'AssetUse' })

  type DictOption = {
    value: string | number
    label: string
  }

  const { ast_asset_status } = useDict('ast_asset_status')

  const treeSelectProps = {
    value: 'id',
    label: 'label',
    children: 'children',
    disabled: 'disabled'
  }

  const handoverDialogVisible = ref(false)
  const currentHandoverType = ref<HandoverType>('ASSIGN')
  const selectedAssetRows = ref<SelectedAssetItem[]>([])
  const detailDrawerVisible = ref(false)
  const itemLoading = ref(false)
  const currentOrder = ref<any>()
  const orderItems = ref<any[]>([])

  const deptOptions = ref<AssetTreeOption[]>([])
  const userOptions = ref<AssetUserOption[]>([])
  const userLoading = ref(false)

  const assetInitialSearchState = {
    assetCode: '',
    assetName: '',
    assetStatus: '',
    ownerDeptId: undefined as number | undefined,
    useDeptId: undefined as number | undefined,
    responsibleUserId: undefined as number | undefined
  }
  const assetSearchForm = reactive({ ...assetInitialSearchState })

  const orderInitialSearchState = {
    handoverNo: '',
    handoverType: '',
    toDeptId: undefined as number | undefined,
    toUserId: undefined as number | undefined,
    daterange: undefined as string[] | undefined
  }
  const orderSearchForm = reactive({ ...orderInitialSearchState })

  const handoverTypeOptions = [
    { label: '领用', value: 'ASSIGN' },
    { label: '调拨', value: 'TRANSFER' },
    { label: '退还', value: 'RETURN' }
  ]

  const handoverStatusOptions = [{ label: '已确认', value: 'CONFIRMED' }]

  const assetSearchBarKey = computed(() => {
    return deptOptions.value.length + userOptions.value.length + ast_asset_status.value.length
  })

  const orderSearchBarKey = computed(() => {
    return deptOptions.value.length + userOptions.value.length + handoverTypeOptions.length
  })

  const assetSearchItems = computed(() => [
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
        loading: userLoading.value,
        remoteMethod: handleUserSearch,
        options: userOptions.value
      }
    }
  ])

  const orderSearchItems = computed(() => [
    {
      label: '交接单号',
      key: 'handoverNo',
      type: 'input',
      props: { placeholder: '请输入交接单号', clearable: true }
    },
    {
      label: '交接类型',
      key: 'handoverType',
      type: 'select',
      props: {
        placeholder: '请选择交接类型',
        clearable: true,
        options: handoverTypeOptions
      }
    },
    {
      label: '目标部门',
      key: 'toDeptId',
      type: 'treeselect',
      props: {
        data: deptOptions.value,
        props: treeSelectProps,
        valueKey: 'id',
        clearable: true,
        checkStrictly: true,
        filterable: true,
        placeholder: '请选择目标部门',
        renderAfterExpand: false
      }
    },
    {
      label: '目标责任人',
      key: 'toUserId',
      type: 'select',
      props: {
        placeholder: '请输入责任人姓名搜索',
        clearable: true,
        filterable: true,
        remote: true,
        reserveKeyword: true,
        loading: userLoading.value,
        remoteMethod: handleUserSearch,
        options: userOptions.value
      }
    },
    {
      label: '交接日期',
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
    columns: assetColumns,
    data: assetData,
    loading: assetLoading,
    pagination: assetPagination,
    getData: getAssetData,
    searchParams: assetSearchParams,
    resetSearchParams: resetAssetSearchParams,
    handleSizeChange: handleAssetSizeChange,
    handleCurrentChange: handleAssetCurrentChange,
    refreshData: refreshAssetData
  } = useTable({
    core: {
      apiFn: listAssetLedger,
      apiParams: {
        assetType: 'FIXED',
        pageNum: 1,
        pageSize: 10
      },
      columnsFactory: () => [
        { type: 'selection', width: 48, align: 'center' },
        { prop: 'assetCode', label: '资产编码', minWidth: 140 },
        { prop: 'assetName', label: '资产名称', minWidth: 180 },
        {
          prop: 'assetStatus',
          label: '资产状态',
          width: 120,
          formatter: (row: any) =>
            h(DictTag, { options: ast_asset_status.value, value: row.assetStatus })
        },
        { prop: 'ownerDeptName', label: '权属部门', minWidth: 130 },
        { prop: 'useDeptName', label: '使用部门', minWidth: 130 },
        { prop: 'responsibleUserName', label: '责任人', width: 120 },
        { prop: 'locationName', label: '资产位置', minWidth: 150, showOverflowTooltip: true }
      ]
    }
  })

  const {
    columns: orderColumns,
    data: orderData,
    loading: orderLoading,
    pagination: orderPagination,
    getData: getOrderData,
    searchParams: orderSearchParams,
    resetSearchParams: resetOrderSearchParams,
    handleSizeChange: handleOrderSizeChange,
    handleCurrentChange: handleOrderCurrentChange,
    refreshData: refreshOrderData
  } = useTable({
    core: {
      apiFn: listAssetHandoverOrder,
      apiParams: {
        assetType: 'FIXED',
        pageNum: 1,
        pageSize: 10
      },
      columnsFactory: () => [
        { prop: 'handoverNo', label: '交接单号', minWidth: 150 },
        {
          prop: 'handoverType',
          label: '交接类型',
          width: 110,
          formatter: (row: any) =>
            h(
              'span',
              {},
              h(
                'span',
                {
                  class: 'handover-type-tag',
                  style: getTagStyleByType(row.handoverType)
                },
                getHandoverLabel(row.handoverType)
              )
            )
        },
        {
          prop: 'handoverStatus',
          label: '交接状态',
          width: 110,
          formatter: (row: any) =>
            h(DictTag, { options: handoverStatusOptions, value: row.handoverStatus })
        },
        { prop: 'handoverDate', label: '交接日期', width: 120 },
        { prop: 'assetCount', label: '资产数量', width: 96 },
        { prop: 'toDeptName', label: '目标部门', minWidth: 130, showOverflowTooltip: true },
        { prop: 'toUserName', label: '目标责任人', minWidth: 120, showOverflowTooltip: true },
        { prop: 'locationName', label: '目标位置', minWidth: 150, showOverflowTooltip: true },
        { prop: 'confirmBy', label: '确认人', width: 100 },
        { prop: 'confirmTime', label: '确认时间', width: 170 },
        {
          prop: 'operation',
          label: '操作',
          width: 90,
          fixed: 'right',
          align: 'right',
          formatter: (row: any) =>
            h(
              ElButton,
              {
                link: true,
                type: 'primary',
                onClick: () => handleViewOrder(row)
              },
              () => '明细'
            )
        }
      ]
    }
  })

  const selectedAssetIds = computed(() => {
    return selectedAssetRows.value
      .map((item) => item.assetId)
      .filter((assetId) => Number.isFinite(assetId))
  })

  const toArrayData = <T,>(response: any): T[] => {
    if (Array.isArray(response)) {
      return response
    }
    return response?.data || []
  }

  const getHandoverLabel = (type?: string) => {
    const mapper: Record<string, string> = {
      ASSIGN: '领用',
      TRANSFER: '调拨',
      RETURN: '退还'
    }
    return mapper[type || ''] || type || '-'
  }

  const getHandoverTagType = (type?: string): 'success' | 'warning' | 'danger' | 'info' => {
    const mapper: Record<string, 'success' | 'warning' | 'danger' | 'info'> = {
      ASSIGN: 'success',
      TRANSFER: 'warning',
      RETURN: 'info'
    }
    return mapper[type || ''] || 'info'
  }

  const getTagStyleByType = (type?: string) => {
    const mapper: Record<string, { color: string; background: string }> = {
      ASSIGN: { color: '#137a3b', background: '#e9f9ef' },
      TRANSFER: { color: '#9a5d00', background: '#fff3e0' },
      RETURN: { color: '#2f66ff', background: '#eaf0ff' }
    }
    const style = mapper[type || ''] || { color: '#63718b', background: '#f2f4f8' }
    return {
      color: style.color,
      backgroundColor: style.background
    }
  }

  const loadDeptTree = async () => {
    const response = await getAssetDeptTree()
    deptOptions.value = toArrayData<AssetTreeOption>(response)
  }

  const handleUserSearch = async (keyword = '') => {
    userLoading.value = true
    try {
      const response = await listAssetResponsibleUsers({ keyword: keyword.trim() })
      userOptions.value = toArrayData<AssetUserOption>(response)
    } finally {
      userLoading.value = false
    }
  }

  const buildAssetQueryParams = () => {
    return {
      assetType: 'FIXED',
      assetCode: assetSearchForm.assetCode?.trim() || undefined,
      assetName: assetSearchForm.assetName?.trim() || undefined,
      assetStatus: assetSearchForm.assetStatus || undefined,
      ownerDeptId: assetSearchForm.ownerDeptId,
      useDeptId: assetSearchForm.useDeptId,
      responsibleUserId: assetSearchForm.responsibleUserId
    }
  }

  const buildOrderQueryParams = () => {
    const [beginTime, endTime] = Array.isArray(orderSearchForm.daterange)
      ? orderSearchForm.daterange
      : [undefined, undefined]

    return {
      assetType: 'FIXED',
      handoverNo: orderSearchForm.handoverNo?.trim() || undefined,
      handoverType: orderSearchForm.handoverType || undefined,
      toDeptId: orderSearchForm.toDeptId,
      toUserId: orderSearchForm.toUserId,
      'params[beginTime]': beginTime,
      'params[endTime]': endTime
    }
  }

  const syncAssetSearchParams = () => {
    Object.assign(assetSearchParams, buildAssetQueryParams())
  }

  const syncOrderSearchParams = () => {
    Object.assign(orderSearchParams, buildOrderQueryParams())
  }

  const handleAssetSearch = () => {
    syncAssetSearchParams()
    getAssetData()
  }

  const handleOrderSearch = () => {
    syncOrderSearchParams()
    getOrderData()
  }

  const handleAssetReset = () => {
    Object.assign(assetSearchForm, assetInitialSearchState)
    selectedAssetRows.value = []
    resetAssetSearchParams()
  }

  const handleOrderReset = () => {
    Object.assign(orderSearchForm, orderInitialSearchState)
    resetOrderSearchParams()
  }

  const validateSelectedAssets = (handoverType: HandoverType) => {
    const allowedStatusByType: Record<HandoverType, string[]> = {
      ASSIGN: ['IN_LEDGER', 'IDLE'],
      TRANSFER: ['IN_USE'],
      RETURN: ['IN_USE']
    }
    const allowedStatuses = allowedStatusByType[handoverType]
    const invalidRows = selectedAssetRows.value.filter(
      (row) => !allowedStatuses.includes((row.assetStatus || '').toUpperCase())
    )
    if (!invalidRows.length) {
      return { valid: true, message: '' }
    }

    const invalidCodes = invalidRows
      .slice(0, 5)
      .map((row) => row.assetCode)
      .join('、')
    const allowedStatusText = allowedStatuses
      .map(
        (status) =>
          (ast_asset_status.value as DictOption[]).find((item: DictOption) => item.value === status)
            ?.label || status
      )
      .join(' / ')
    return {
      valid: false,
      message: `当前动作仅允许状态为【${allowedStatusText}】的资产，以下资产不符合：${invalidCodes}${
        invalidRows.length > 5 ? ' 等' : ''
      }`
    }
  }

  // 中文注释：交接动作提交前，先做前端状态校验，减少后端报错弹窗，提升批量操作体验。
  const openHandoverDialog = (handoverType: HandoverType) => {
    if (!selectedAssetRows.value.length) {
      ElMessage.warning('请先从资产池勾选需要交接的资产')
      return
    }
    const validation = validateSelectedAssets(handoverType)
    if (!validation.valid) {
      ElMessage.warning(validation.message)
      return
    }
    currentHandoverType.value = handoverType
    handoverDialogVisible.value = true
  }

  const handleHandoverSuccess = async () => {
    selectedAssetRows.value = []
    await Promise.all([refreshAssetData(), refreshOrderData()])
  }

  const handleViewOrder = async (row: any) => {
    currentOrder.value = row
    detailDrawerVisible.value = true
    itemLoading.value = true
    try {
      const response = await listAssetHandoverItems(row.handoverOrderId)
      orderItems.value = toArrayData<any>(response)
    } finally {
      itemLoading.value = false
    }
  }

  onMounted(async () => {
    await Promise.all([loadDeptTree(), handleUserSearch()])
  })
</script>

<style scoped lang="scss">
  .asset-use-page {
    --asset-accent: #2f66ff;
    --asset-border: #e6ebf5;
    --asset-panel-bg: #fff;
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
      font-size: 32px;
      font-weight: 700;
      color: var(--asset-text-main);
      line-height: 1.4;
    }

    .page-desc {
      margin-top: 6px;
      font-size: 14px;
      color: var(--asset-text-secondary);
      line-height: 1.7;
      max-width: 860px;
    }

    .meta-tags {
      :deep(.el-tag) {
        border-radius: 999px;
        padding: 0 12px;
        height: 30px;
        font-weight: 500;
      }
    }

    .asset-pool-card,
    .order-card {
      border: 1px solid var(--asset-border);
      border-radius: 12px;
      background: var(--asset-panel-bg);
    }

    .asset-pool-card {
      min-height: 360px;
      max-height: 46vh;
    }

    .order-card {
      min-height: 320px;
    }

    :deep(.el-card__header) {
      padding: 14px 16px;
      border-bottom: 1px solid #eaf0fb;
      background: linear-gradient(180deg, rgb(247 250 255 / 90%) 0%, #fff 100%);
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

    .handover-type-tag {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      min-width: 52px;
      padding: 2px 8px;
      border-radius: 999px;
      font-size: 12px;
      font-weight: 600;
      line-height: 18px;
    }
  }

  @media (max-width: 992px) {
    .asset-use-page {
      .head-card {
        :deep(.el-card__body) {
          padding: 16px 18px;
        }
      }

      .asset-pool-card {
        max-height: 52vh;
      }
    }
  }

  @media (max-width: 768px) {
    .asset-use-page {
      padding: 8px;
      gap: 8px;

      .page-title {
        font-size: 24px;
      }

      .page-desc {
        font-size: 13px;
      }

      .asset-pool-card,
      .order-card {
        min-height: 280px;
      }
    }
  }
</style>
