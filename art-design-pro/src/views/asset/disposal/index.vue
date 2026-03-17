<template>
  <div class="asset-disposal-page art-full-height flex flex-col overflow-hidden p-3">
    <ElAlert
      v-if="routeAssetContext"
      class="mb-3"
      type="info"
      :closable="false"
      :title="`已从资产台账带入资产 ${routeAssetContext.assetNo}，可直接发起${entryActionLabel}申请。`"
    />

    <ArtSearchBar
      :key="wf_status.length"
      v-model="formFilters"
      :items="formItems"
      :showExpand="false"
      @reset="handleReset"
      @search="handleSearch"
    />

    <ElCard class="art-table-card flex-1 overflow-hidden" shadow="never">
      <ArtTableHeader
        :showZebra="false"
        :loading="loading"
        v-model:columns="columnChecks"
        @refresh="refreshData"
      >
        <template #left>
          <ElButton v-auth="'asset:disposal:add'" type="primary" @click="openCreateDialog()">
            新增{{ entryActionLabel }}
          </ElButton>
        </template>
      </ArtTableHeader>

      <ArtTable
        ref="tableRef"
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
        rowKey="disposalNo"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>

    <ElDialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="520px"
      draggable
      destroy-on-close
    >
      <ElForm ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <ElFormItem label="资产编号" prop="assetNo">
          <ElInput
            v-model="formData.assetNo"
            :disabled="Boolean(dialogAssetContext?.assetNo)"
            placeholder="请输入资产编号"
            clearable
          />
        </ElFormItem>
        <ElFormItem v-if="dialogAssetContext?.assetName" label="资产名称">
          <ElInput :model-value="dialogAssetContext.assetName" disabled />
        </ElFormItem>
        <ElFormItem v-if="dialogAssetContext?.assetStatus" label="当前状态">
          <ElInput :model-value="dialogAssetContext.assetStatus" disabled />
        </ElFormItem>
        <ElFormItem label="处置类型" prop="disposalType">
          <ElSelect
            v-model="formData.disposalType"
            placeholder="请选择处置类型"
            class="w-full"
            clearable
          >
            <ElOption
              v-for="item in disposalTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="处置事由" prop="reason">
          <ElInput
            v-model="formData.reason"
            type="textarea"
            :rows="4"
            :placeholder="`请输入${entryActionLabel}事由`"
            clearable
          />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <span class="dialog-footer">
          <ElButton @click="dialogVisible = false">取消</ElButton>
          <ElButton type="primary" :loading="submitting" @click="submitDisposal">提交</ElButton>
        </span>
      </template>
    </ElDialog>

    <ElDialog v-model="detailVisible" title="处置详情" width="560px" destroy-on-close>
      <ElDescriptions v-loading="detailLoading" :column="1" border>
        <ElDescriptionsItem label="处置单号">
          {{ detailData?.disposalNo || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="资产编号">
          {{ detailData?.assetNo || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="资产名称">
          {{ detailData?.assetName || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="单据状态">
          {{ formatFixedAssetBusinessStatus(Number(detailData?.status)) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="审批状态">
          <DictTag
            :options="wf_status"
            :value="resolveFixedAssetWorkflowStatus(detailData || {})"
          />
        </ElDescriptionsItem>
        <ElDescriptionsItem label="处置类型">
          {{ formatDisposalType(detailData?.disposalType) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="申请人ID">
          {{ detailData?.applyUserId ?? '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="处置事由">
          {{ detailData?.reason || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="申请时间">
          {{ detailData?.createTime || '--' }}
        </ElDescriptionsItem>
      </ElDescriptions>
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
  import { computed, h, onMounted, reactive, ref } from 'vue'
  import { useRoute } from 'vue-router'
  import { ElButton, ElMessage } from 'element-plus'
  import type { FormInstance, FormRules } from 'element-plus'
  import DictTag from '@/components/DictTag/index.vue'
  import {
    applyDisposal,
    getDisposal,
    listDisposal,
    type ApplyDisposalReq,
    type AssetDisposalItem
  } from '@/api/asset/disposal'
  import { useTable } from '@/hooks/core/useTable'
  import type { ColumnOption } from '@/types/component'
  import {
    FIXED_ASSET_DISPOSAL_TYPE_OPTIONS,
    formatFixedAssetDisposalType,
    type FixedAssetDisposalType
  } from '@/types/asset'
  import { useDict } from '@/utils/dict'
  import {
    buildFixedAssetBusinessPayload,
    formatFixedAssetBusinessStatus,
    resolveFixedAssetWorkflowStatus
  } from '../requisition/requisition.helper'

  defineOptions({ name: 'AssetDisposal' })

  type DisposalIntent = 'scrap' | 'dispose'

  interface RouteAssetContext {
    assetId?: number
    assetNo: string
    assetName?: string
    assetStatus?: string
    disposalIntent?: DisposalIntent
  }

  const route = useRoute()
  const { wf_status } = useDict('wf_status')

  const routeAssetContext = ref<RouteAssetContext | null>(null)
  const dialogAssetContext = ref<RouteAssetContext | null>(null)
  const disposalTypeOptions = FIXED_ASSET_DISPOSAL_TYPE_OPTIONS

  const initialSearchState = {
    disposalNo: '',
    assetNo: '',
    status: undefined as number | undefined
  }

  const formFilters = reactive({ ...initialSearchState })

  const formItems = computed(() => [
    {
      label: '处置单号',
      key: 'disposalNo',
      type: 'input',
      props: { placeholder: '请输入处置单号', clearable: true }
    },
    {
      label: '资产编号',
      key: 'assetNo',
      type: 'input',
      props: { placeholder: '请输入资产编号', clearable: true }
    },
    {
      label: '单据状态',
      key: 'status',
      type: 'select',
      props: {
        placeholder: '请选择单据状态',
        clearable: true,
        options: [
          { label: '待审批', value: 0 },
          { label: '已审批', value: 1 },
          { label: '已驳回', value: 2 },
          { label: '已完成', value: 3 }
        ]
      }
    }
  ])

  const currentEntryContext = computed(() => dialogAssetContext.value || routeAssetContext.value)

  const entryActionLabel = computed(() => {
    if (currentEntryContext.value?.disposalIntent === 'scrap') {
      return '报废'
    }
    if (currentEntryContext.value?.disposalIntent === 'dispose') {
      return '处置'
    }
    return '报废/处置'
  })

  const dialogTitle = computed(() => `新增${entryActionLabel.value}申请`)

  const {
    columns,
    columnChecks,
    data,
    loading,
    pagination,
    searchParams,
    resetSearchParams,
    handleSizeChange,
    handleCurrentChange,
    refreshData,
    getData
  } = useTable({
    core: {
      apiFn: listDisposal,
      immediate: false,
      columnsFactory: () => {
        const operationColumn: ColumnOption<AssetDisposalItem> = {
          prop: 'operation',
          label: '操作',
          width: 110,
          align: 'right',
          formatter: (row: AssetDisposalItem) =>
            h(
              ElButton,
              {
                type: 'primary',
                link: true,
                onClick: () => openDetail(row)
              },
              () => '详情'
            )
        }

        return [
          { type: 'index', label: '序号', width: 60, align: 'center' },
          { prop: 'disposalNo', label: '处置单号', width: 160 },
          { prop: 'assetNo', label: '资产编号', minWidth: 140 },
          { prop: 'assetName', label: '资产名称', minWidth: 150 },
          {
            prop: 'disposalType',
            label: '处置类型',
            width: 100,
            align: 'center',
            formatter: (row: AssetDisposalItem) => formatDisposalType(row.disposalType)
          },
          { prop: 'applyUserId', label: '申请人ID', width: 120, align: 'center' },
          { prop: 'reason', label: '处置事由', minWidth: 220 },
          {
            prop: 'status',
            label: '单据状态',
            width: 110,
            align: 'center',
            formatter: (row: AssetDisposalItem) => formatFixedAssetBusinessStatus(Number(row.status))
          },
          {
            prop: 'wfStatus',
            label: '审批状态',
            width: 110,
            align: 'center',
            formatter: (row: AssetDisposalItem) =>
              h(DictTag, {
                options: wf_status.value,
                value: resolveFixedAssetWorkflowStatus(row)
              })
          },
          { prop: 'createTime', label: '申请时间', width: 170, align: 'center' },
          operationColumn
        ]
      }
    }
  })

  const tableRef = ref()
  const formRef = ref<FormInstance>()
  const dialogVisible = ref(false)
  const submitting = ref(false)
  const detailVisible = ref(false)
  const detailLoading = ref(false)
  const detailData = ref<AssetDisposalItem>()

  const formData = reactive({
    assetNo: '',
    disposalType: '' as FixedAssetDisposalType | '',
    reason: ''
  })

  const rules: FormRules = {
    assetNo: [{ required: true, message: '请输入资产编号', trigger: 'blur' }],
    disposalType: [{ required: true, message: '请选择处置类型', trigger: 'change' }],
    reason: [{ required: true, message: '请输入处置事由', trigger: 'blur' }]
  }

  /**
   * 读取资产台账传入的处置上下文。
   */
  const readRouteAssetContext = (): RouteAssetContext | null => {
    const assetNo = String(route.query.assetNo || '').trim()
    if (!assetNo) {
      return null
    }

    const assetId = Number(route.query.assetId)
    return {
      assetId: Number.isNaN(assetId) ? undefined : assetId,
      assetNo,
      assetName: String(route.query.assetName || '').trim() || undefined,
      assetStatus: String(route.query.assetStatus || '').trim() || undefined,
      disposalIntent: parseDisposalIntent(route.query.disposalIntent)
    }
  }

  const handleReset = () => {
    Object.assign(formFilters, initialSearchState)
    resetSearchParams()
  }

  const handleSearch = () => {
    Object.assign(searchParams, formFilters)
    getData()
  }

  const openCreateDialog = (context: RouteAssetContext | null = routeAssetContext.value) => {
    if (!context?.assetId) {
      ElMessage.warning('请从资产台账发起报废或处置业务，确保自动带入资产主键')
      return
    }

    dialogAssetContext.value = context
    formData.assetNo = context.assetNo
    formData.disposalType = context.disposalIntent === 'scrap' ? 'scrap' : ''
    formData.reason = ''
    dialogVisible.value = true
  }

  const submitDisposal = async () => {
    try {
      await formRef.value?.validate()

      submitting.value = true
      const basePayload = buildFixedAssetBusinessPayload(dialogAssetContext.value || {}, formData.reason)
      const payload: ApplyDisposalReq = {
        ...basePayload,
        disposalType: formData.disposalType as FixedAssetDisposalType
      }

      await applyDisposal(payload)
      ElMessage.success(`${formatDisposalType(payload.disposalType)}申请提交成功`)
      dialogVisible.value = false
      refreshData()
    } catch (error) {
      if (error !== 'cancel') {
        if (error instanceof Error) {
          ElMessage.warning(error.message)
        }
        console.error('提交处置申请失败:', error)
      }
    } finally {
      submitting.value = false
    }
  }

  const openDetail = async (row: AssetDisposalItem) => {
    try {
      detailVisible.value = true
      detailLoading.value = true
      detailData.value = await getDisposal(row.disposalNo)
    } catch (error) {
      detailVisible.value = false
      console.error('读取处置详情失败:', error)
    } finally {
      detailLoading.value = false
    }
  }

  const formatDisposalType = (value?: string) => formatFixedAssetDisposalType(value)

  /**
   * 解析资产台账路由带入的处置意图。
   */
  const parseDisposalIntent = (value: unknown): DisposalIntent | undefined => {
    const normalized = String(value || '').trim()
    if (normalized === 'scrap' || normalized === 'dispose') {
      return normalized
    }
    return undefined
  }

  onMounted(() => {
    routeAssetContext.value = readRouteAssetContext()
    void wf_status.value

    if (routeAssetContext.value?.assetNo) {
      formFilters.assetNo = routeAssetContext.value.assetNo
      Object.assign(searchParams, { assetNo: routeAssetContext.value.assetNo })
    }

    getData()

    if (route.query.openCreate === '1' && routeAssetContext.value) {
      openCreateDialog(routeAssetContext.value)
    }
  })
</script>

<style lang="scss" scoped>
  .asset-disposal-page {
    padding: 12px;
  }
</style>
