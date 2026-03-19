<template>
  <div class="asset-use-detail-page art-full-height flex flex-col gap-3 overflow-hidden p-3">
    <ElCard class="head-card" shadow="never">
      <div class="flex flex-wrap items-start justify-between gap-3">
        <div class="flex flex-col gap-2">
          <ElButton link type="primary" @click="goBack">返回资产使用</ElButton>
          <div class="page-title">交接单详情</div>
          <div class="page-desc"
            >查看主单信息、交接明细与交接前后状态快照，支撑使用流程审计追溯。</div
          >
        </div>
        <ElButton
          type="primary"
          plain
          icon="ri:refresh-line"
          :loading="orderLoading || itemLoading"
          @click="loadDetail"
        >
          刷新详情
        </ElButton>
      </div>
    </ElCard>

    <ElCard class="detail-card" shadow="never">
      <template #header>
        <div class="card-title">交接主单</div>
      </template>

      <ElSkeleton :loading="orderLoading" animated>
        <template #default>
          <ElDescriptions v-if="orderDetail" :column="3" border>
            <ElDescriptionsItem label="交接单号">{{
              orderDetail.handoverNo || '-'
            }}</ElDescriptionsItem>
            <ElDescriptionsItem label="交接类型">
              <ElTag :type="getHandoverTagType(orderDetail.handoverType)" effect="light">
                {{ getHandoverLabel(orderDetail.handoverType) }}
              </ElTag>
            </ElDescriptionsItem>
            <ElDescriptionsItem label="交接状态">{{
              orderDetail.handoverStatus || '-'
            }}</ElDescriptionsItem>
            <ElDescriptionsItem label="交接日期">{{
              orderDetail.handoverDate || '-'
            }}</ElDescriptionsItem>
            <ElDescriptionsItem label="资产数量">{{
              orderDetail.assetCount || 0
            }}</ElDescriptionsItem>
            <ElDescriptionsItem label="资产类型">{{
              orderDetail.assetType || '-'
            }}</ElDescriptionsItem>
            <ElDescriptionsItem label="目标部门">{{
              orderDetail.toDeptName || '-'
            }}</ElDescriptionsItem>
            <ElDescriptionsItem label="目标责任人">{{
              orderDetail.toUserName || '-'
            }}</ElDescriptionsItem>
            <ElDescriptionsItem label="目标位置">{{
              orderDetail.locationName || '-'
            }}</ElDescriptionsItem>
            <ElDescriptionsItem label="确认人">{{
              orderDetail.confirmBy || '-'
            }}</ElDescriptionsItem>
            <ElDescriptionsItem label="确认时间">{{
              orderDetail.confirmTime || '-'
            }}</ElDescriptionsItem>
            <ElDescriptionsItem label="备注">{{ orderDetail.remark || '-' }}</ElDescriptionsItem>
          </ElDescriptions>
          <ElEmpty v-else description="未找到交接单信息" />
        </template>
      </ElSkeleton>
    </ElCard>

    <ElCard class="items-card flex-1 min-h-0 overflow-hidden" shadow="never">
      <template #header>
        <div class="card-title">交接明细</div>
      </template>

      <ElTable v-loading="itemLoading" :data="orderItems" border height="calc(100vh - 450px)">
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
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { ElMessage } from 'element-plus'
  import { getAssetHandoverOrder, listAssetHandoverItems } from '@/api/asset/handover'
  import DictTag from '@/components/DictTag/index.vue'
  import { useDict } from '@/utils/dict'

  defineOptions({ name: 'AssetUseDetail' })

  type HandoverOrderDetail = {
    handoverOrderId: number
    handoverNo: string
    handoverType: string
    handoverStatus: string
    handoverDate: string
    assetCount: number
    assetType?: string
    toDeptName?: string
    toUserName?: string
    locationName?: string
    confirmBy?: string
    confirmTime?: string
    remark?: string
  }

  const route = useRoute()
  const router = useRouter()
  const { ast_asset_status } = useDict('ast_asset_status')

  const orderLoading = ref(false)
  const itemLoading = ref(false)
  const orderDetail = ref<HandoverOrderDetail>()
  const orderItems = ref<any[]>([])

  const handoverOrderId = computed(() => {
    const rawValue = route.params.handoverOrderId
    const value = Number(rawValue)
    return Number.isFinite(value) ? value : 0
  })

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

  const toArrayData = <T,>(response: any): T[] => {
    if (Array.isArray(response)) {
      return response
    }
    return response?.data || []
  }

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

  const goBack = () => {
    router.push('/asset/use')
  }

  // 中文注释：详情页主单与明细分开加载，保障接口失败时可定位具体问题并便于重试。
  const loadDetail = async () => {
    if (!handoverOrderId.value) {
      ElMessage.error('交接单ID非法，无法加载详情')
      return
    }

    orderLoading.value = true
    itemLoading.value = true
    try {
      const [orderResponse, itemResponse] = await Promise.all([
        getAssetHandoverOrder(handoverOrderId.value),
        listAssetHandoverItems(handoverOrderId.value)
      ])
      orderDetail.value = toRecordData<HandoverOrderDetail>(orderResponse)
      orderItems.value = toArrayData<any>(itemResponse)
    } finally {
      orderLoading.value = false
      itemLoading.value = false
    }
  }

  onMounted(() => {
    loadDetail()
  })
</script>

<style scoped lang="scss">
  .asset-use-detail-page {
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
  .detail-card,
  .items-card {
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
