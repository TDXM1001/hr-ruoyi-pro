<template>
  <ElDialog v-model="visible" title="资产财务" width="920px" destroy-on-close>
    <div class="asset-finance-dialog" v-loading="loading">
      <div class="mb-4 flex items-center justify-between gap-3">
        <div class="text-sm text-[var(--art-gray-600)]">
          资产编号：{{ props.assetNo || '--' }}
        </div>
        <ElButton
          type="primary"
          :loading="recalculateLoading"
          :disabled="!props.assetId"
          @click="handleRecalculate"
        >
          财务重算
        </ElButton>
      </div>

      <ElEmpty v-if="!props.assetId" description="未选择可查看财务信息的资产" :image-size="80" />

      <template v-else>
        <ElCard shadow="never" class="mb-4">
          <template #header>
            <span>财务摘要</span>
          </template>

          <ElDescriptions :column="3" border>
            <ElDescriptionsItem
              v-for="item in financeSummaryRows"
              :key="item.label"
              :label="item.label"
            >
              {{ formatMoney(item.value) }}
            </ElDescriptionsItem>
            <ElDescriptionsItem label="累计折旧">
              {{ formatMoney(financeInfo.accumulatedDepreciation) }}
            </ElDescriptionsItem>
            <ElDescriptionsItem label="净值">
              {{ formatMoney(financeInfo.netBookValue) }}
            </ElDescriptionsItem>
            <ElDescriptionsItem label="账面价值">
              {{ formatMoney(financeInfo.bookValue) }}
            </ElDescriptionsItem>
            <ElDescriptionsItem label="最近折旧期间">
              {{ financeInfo.lastDepreciationPeriod || '--' }}
            </ElDescriptionsItem>
            <ElDescriptionsItem label="折旧起始日">
              {{ financeInfo.depreciationStartDate || '--' }}
            </ElDescriptionsItem>
            <ElDescriptionsItem label="财务基础字段">
              {{ financeBaseFieldEditable ? '可编辑' : '只读' }}
            </ElDescriptionsItem>
          </ElDescriptions>
        </ElCard>

        <ElCard shadow="never">
          <template #header>
            <div class="flex items-center justify-between gap-3">
              <span>折旧日志</span>
              <span class="text-xs text-[var(--art-gray-500)]">
                数据来自 /asset/finance/{{ props.assetId }}/depreciation-logs
              </span>
            </div>
          </template>

          <ElEmpty v-if="!depreciationRows.length" description="暂无折旧日志" :image-size="80" />

          <ElTable v-else :data="depreciationRows" border>
            <ElTableColumn prop="periodLabel" label="期间" min-width="120" />
            <ElTableColumn prop="depreciationAmount" label="本期折旧" min-width="120">
              <template #default="{ row }">
                {{ formatMoney(row.depreciationAmount) }}
              </template>
            </ElTableColumn>
            <ElTableColumn prop="accumulatedDepreciation" label="累计折旧" min-width="120">
              <template #default="{ row }">
                {{ formatMoney(row.accumulatedDepreciation) }}
              </template>
            </ElTableColumn>
            <ElTableColumn prop="netBookValue" label="净值" min-width="120">
              <template #default="{ row }">
                {{ formatMoney(row.netBookValue) }}
              </template>
            </ElTableColumn>
            <ElTableColumn prop="bookValue" label="账面价值" min-width="120">
              <template #default="{ row }">
                {{ formatMoney(row.bookValue) }}
              </template>
            </ElTableColumn>
            <ElTableColumn prop="calcType" label="计算方式" min-width="120" />
            <ElTableColumn prop="calcTime" label="计算时间" min-width="180" />
          </ElTable>
        </ElCard>
      </template>
    </div>
  </ElDialog>
</template>

<script setup lang="ts">
  import { computed, ref, watch } from 'vue'
  import { ElMessage } from 'element-plus'
  import { listDepreciationLogs, recalculateFinance } from '@/api/asset/finance'
  import { getInfo } from '@/api/asset/info'
  import type { AssetDepreciationLog, AssetFinanceInfo } from '@/types/asset'
  import {
    buildDepreciationRows,
    buildFinanceSummaryRows,
    canEditFinanceBaseFields
  } from './asset-finance.helper'

  const props = defineProps<{
    modelValue: boolean
    assetId?: number
    assetNo?: string
  }>()

  const emit = defineEmits<{
    (e: 'update:modelValue', value: boolean): void
  }>()

  const loading = ref(false)
  const recalculateLoading = ref(false)
  const financeInfo = ref<Partial<AssetFinanceInfo>>({})
  const depreciationRows = ref<ReturnType<typeof buildDepreciationRows>>([])

  const visible = computed({
    get: () => props.modelValue,
    set: (value: boolean) => emit('update:modelValue', value)
  })

  const financeSummaryRows = computed(() => buildFinanceSummaryRows(financeInfo.value))
  const financeBaseFieldEditable = computed(() => canEditFinanceBaseFields(financeInfo.value))

  /** 兼容 request 直接返回 data，或返回 AjaxResult 包裹对象。 */
  const unwrapList = <T,>(response: any): T[] => {
    if (Array.isArray(response)) {
      return response
    }
    return response?.data || response?.rows || []
  }

  /** 统一加载财务快照和完整折旧日志。 */
  const loadData = async () => {
    if (!props.assetId) {
      financeInfo.value = {}
      depreciationRows.value = []
      return
    }

    loading.value = true
    try {
      const [detailResp, logsResp] = await Promise.all([
        getInfo(props.assetId),
        listDepreciationLogs(props.assetId)
      ])

      const detail = (detailResp as any)?.data || (detailResp as any)
      financeInfo.value = detail?.financeInfo || {}
      depreciationRows.value = buildDepreciationRows(unwrapList<AssetDepreciationLog>(logsResp))
    } finally {
      loading.value = false
    }
  }

  /** 触发财务重算后立即刷新摘要与日志，避免页面停留旧快照。 */
  const handleRecalculate = async () => {
    if (!props.assetId) {
      return
    }

    recalculateLoading.value = true
    try {
      await recalculateFinance(props.assetId)
      ElMessage.success('财务重算成功')
      await loadData()
    } finally {
      recalculateLoading.value = false
    }
  }

  const formatMoney = (value?: number | string) => {
    if (value === undefined || value === null || value === '') {
      return '--'
    }
    if (typeof value === 'number') {
      return value.toFixed(2)
    }
    return value
  }

  watch(
    () => [props.modelValue, props.assetId] as const,
    async ([open]) => {
      if (open) {
        await loadData()
        return
      }

      financeInfo.value = {}
      depreciationRows.value = []
    }
  )
</script>

<style lang="scss" scoped>
  .asset-finance-dialog {
    min-height: 240px;
  }
</style>
