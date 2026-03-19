<template>
  <div class="asset-use-page art-full-height flex flex-col gap-3 overflow-hidden p-3">
    <ElCard class="head-card" shadow="never">
      <div class="flex flex-wrap items-start justify-between gap-3">
        <div>
          <div class="page-title">资产使用</div>
          <div class="page-desc">
            资产使用页仅负责交接单管理，不直接改台账字段。领用、调拨、退还均通过“发起交接单”页面完成并留痕。
          </div>
        </div>
        <ElSpace wrap>
          <ElButton
            v-auth="'asset:handover:add'"
            type="primary"
            @click="navigateCreate('ASSIGN')"
            v-ripple
          >
            发起领用
          </ElButton>
          <ElButton
            v-auth="'asset:handover:add'"
            type="warning"
            plain
            @click="navigateCreate('TRANSFER')"
            v-ripple
          >
            发起调拨
          </ElButton>
          <ElButton
            v-auth="'asset:handover:add'"
            type="danger"
            plain
            @click="navigateCreate('RETURN')"
            v-ripple
          >
            发起退还
          </ElButton>
        </ElSpace>
      </div>
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
  </div>
</template>

<script setup lang="ts">
  import { ElButton } from 'element-plus'
  import type { AssetTreeOption, AssetUserOption } from '@/api/asset/ledger'
  import { getAssetDeptTree, listAssetResponsibleUsers } from '@/api/asset/ledger'
  import { listAssetHandoverOrder } from '@/api/asset/handover'
  import { useTable } from '@/hooks/core/useTable'

  defineOptions({ name: 'AssetUse' })

  type HandoverType = 'ASSIGN' | 'TRANSFER' | 'RETURN'

  type HandoverOrderRow = {
    handoverOrderId: number
    handoverNo: string
    handoverType: HandoverType
    handoverStatus: string
    handoverDate: string
    assetCount: number
    toDeptName?: string
    toUserName?: string
    locationName?: string
    confirmBy?: string
    confirmTime?: string
  }

  const router = useRouter()
  const route = useRoute()

  const treeSelectProps = {
    value: 'id',
    label: 'label',
    children: 'children',
    disabled: 'disabled'
  }

  const deptOptions = ref<AssetTreeOption[]>([])
  const userOptions = ref<AssetUserOption[]>([])
  const userLoading = ref(false)

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

  const orderSearchBarKey = computed(() => {
    return deptOptions.value.length + userOptions.value.length + handoverTypeOptions.length
  })

  const getHandoverLabel = (type?: string) => {
    const mapper: Record<string, string> = {
      ASSIGN: '领用',
      TRANSFER: '调拨',
      RETURN: '退还'
    }
    return mapper[type || ''] || type || '-'
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
          formatter: (row: HandoverOrderRow) =>
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
          formatter: (row: HandoverOrderRow) => {
            const match = handoverStatusOptions.find((item) => item.value === row.handoverStatus)
            return match?.label || row.handoverStatus || '-'
          }
        },
        { prop: 'handoverDate', label: '交接日期', width: 120 },
        { prop: 'assetCount', label: '资产数量', width: 96 },
        { prop: 'toDeptName', label: '目标部门', minWidth: 140, showOverflowTooltip: true },
        { prop: 'toUserName', label: '目标责任人', minWidth: 130, showOverflowTooltip: true },
        { prop: 'locationName', label: '目标位置', minWidth: 150, showOverflowTooltip: true },
        { prop: 'confirmBy', label: '确认人', width: 100 },
        { prop: 'confirmTime', label: '确认时间', width: 170 },
        {
          prop: 'operation',
          label: '操作',
          width: 90,
          fixed: 'right',
          align: 'right',
          formatter: (row: HandoverOrderRow) =>
            h(
              ElButton,
              {
                link: true,
                type: 'primary',
                onClick: () => navigateDetail(row.handoverOrderId)
              },
              () => '详情'
            )
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

  const syncOrderSearchParams = () => {
    Object.assign(orderSearchParams, buildOrderQueryParams())
  }

  const handleOrderSearch = () => {
    syncOrderSearchParams()
    getOrderData()
  }

  const handleOrderReset = () => {
    Object.assign(orderSearchForm, orderInitialSearchState)
    resetOrderSearchParams()
  }

  // 中文注释：创建页根据动作类型承载不同业务规则，列表页只作为流程入口与追踪页面。
  const navigateCreate = (type: HandoverType) => {
    router.push({ path: '/asset/use/create', query: { type } })
  }

  const navigateDetail = (handoverOrderId: number) => {
    router.push(`/asset/use/detail/${handoverOrderId}`)
  }

  onMounted(async () => {
    await Promise.all([loadDeptTree(), handleUserSearch()])
    if (route.query.refresh === '1') {
      await refreshOrderData()
      router.replace({ path: route.path })
    }
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
  }

  .head-card,
  .order-card {
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
    max-width: 860px;
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
</style>
