<template>
  <div class="real-estate-disposal-page art-full-height flex flex-col p-3 overflow-hidden">
    <ElAlert
      v-if="routeAssetContext"
      class="mb-3"
      :type="createGuard.disabled ? 'warning' : 'info'"
      :closable="false"
      :title="routeAlertTitle"
    />

    <ArtSearchBar
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
          <ElButton
            v-auth="'asset:realEstateDisposal:add'"
            type="primary"
            :disabled="createGuard.disabled"
            @click="openCreateDialog()"
          >
            发起注销/处置
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
      title="发起注销/处置申请"
      width="560px"
      draggable
      destroy-on-close
    >
      <ElForm :model="formData" :rules="rules" ref="formRef" label-width="110px">
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
        <ElFormItem label="处置类型" prop="disposalType">
          <ElSelect v-model="formData.disposalType" placeholder="请选择处置类型" class="w-full">
            <ElOption
              v-for="item in disposalTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="目标资产状态" prop="targetAssetStatus">
          <ElSelect
            v-model="formData.targetAssetStatus"
            placeholder="请选择目标资产状态"
            class="w-full"
          >
            <ElOption
              v-for="item in asset_status"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="处置原因" prop="reason">
          <ElInput
            v-model="formData.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入处置原因"
            clearable
          />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <span class="dialog-footer">
          <ElButton @click="dialogVisible = false">取消</ElButton>
          <ElButton type="primary" :loading="submitting" @click="submitDisposal">
            提交申请
          </ElButton>
        </span>
      </template>
    </ElDialog>

    <ElDialog v-model="detailVisible" title="注销/处置详情" width="620px" destroy-on-close>
      <ElDescriptions v-loading="detailLoading" :column="1" border>
        <ElDescriptionsItem label="处置单号">
          {{ detailData?.disposalNo || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="资产编号">
          {{ detailData?.assetNo || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="处置类型">
          {{ formatDisposalType(detailData?.disposalType) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="原资产状态">
          <DictTag :options="asset_status" :value="detailData?.oldAssetStatus" />
        </ElDescriptionsItem>
        <ElDescriptionsItem label="目标资产状态">
          <DictTag :options="asset_status" :value="detailData?.targetAssetStatus" />
        </ElDescriptionsItem>
        <ElDescriptionsItem label="单据状态">
          <DictTag
            :options="docStatusOptions"
            :value="resolveRealEstateDocumentStatus(detailData)"
          />
        </ElDescriptionsItem>
        <ElDescriptionsItem label="流程状态">
          <DictTag
            :options="wf_status"
            :value="resolveRealEstateWorkflowStatus(detailData || {})"
          />
        </ElDescriptionsItem>
        <ElDescriptionsItem label="处置原因">
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
  import type { ColumnOption } from '@/types/component'
  import DictTag from '@/components/DictTag/index.vue'
  import { useTable } from '@/hooks/core/useTable'
  import { ASSET_TIMELINE_DOC_STATUS_OPTIONS } from '@/types/asset'
  import { useDict } from '@/utils/dict'
  import {
    createRealEstateDisposal,
    getRealEstateDisposalDetail,
    listRealEstateDisposal,
    type RealEstateDisposalItem
  } from '@/api/asset/real-estate-disposal'
  import {
    buildRealEstateDisposalPayload,
    formatRealEstateLatestAction,
    getRealEstateActionGuard,
    parseAssetRouteQuery,
    REAL_ESTATE_APPROVAL_STATUS_OPTIONS,
    resolveRealEstateDocumentStatus,
    resolveRealEstateWorkflowStatus,
    shouldOpenCreateDialog,
    type RealEstateRouteAssetContext
  } from '../real-estate-lifecycle.helper'

  defineOptions({ name: 'AssetRealEstateDisposal' })

  const route = useRoute()
  const { asset_status, wf_status } = useDict('asset_status', 'wf_status')
  const docStatusOptions = [...ASSET_TIMELINE_DOC_STATUS_OPTIONS]

  const routeAssetContext = ref<RealEstateRouteAssetContext | null>(null)
  const dialogAssetContext = ref<RealEstateRouteAssetContext | null>(null)
  const createGuard = computed(() =>
    getRealEstateActionGuard('realEstateDisposal', routeAssetContext.value)
  )
  const routeAlertTitle = computed(() => {
    if (!routeAssetContext.value) return ''
    const latestActionText = formatRealEstateLatestAction(routeAssetContext.value)
    const latestActionPrefix = latestActionText ? `最近动作：${latestActionText}。` : ''
    const actionText = createGuard.value.disabled
      ? createGuard.value.reason || '当前不能直接发起注销/处置。'
      : '可直接发起注销/处置申请。'
    return `已从资产台账带入不动产 ${routeAssetContext.value.assetNo}，${latestActionPrefix}${actionText}`
  })

  const disposalTypeOptions = [
    { label: '注销', value: 'cancel' },
    { label: '处置', value: 'dispose' }
  ]

  const initialSearchState = {
    disposalNo: '',
    assetNo: '',
    status: undefined as string | undefined
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
      label: '流转状态',
      key: 'status',
      type: 'select',
      props: {
        placeholder: '请选择流转状态',
        clearable: true,
        options: REAL_ESTATE_APPROVAL_STATUS_OPTIONS
      }
    }
  ])

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
      apiFn: listRealEstateDisposal,
      immediate: false,
      columnsFactory: () => {
        const operationColumn: ColumnOption<RealEstateDisposalItem> = {
          prop: 'operation',
          label: '操作',
          width: 110,
          align: 'right',
          formatter: (row: RealEstateDisposalItem) =>
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
          { prop: 'disposalNo', label: '处置单号', width: 180 },
          { prop: 'assetNo', label: '资产编号', minWidth: 140 },
          {
            prop: 'disposalType',
            label: '处置类型',
            width: 120,
            align: 'center',
            formatter: (row: RealEstateDisposalItem) => formatDisposalType(row.disposalType)
          },
          {
            prop: 'status',
            label: '单据状态',
            width: 110,
            align: 'center',
            formatter: (row: RealEstateDisposalItem) =>
              h(DictTag, {
                options: docStatusOptions,
                value: resolveRealEstateDocumentStatus(row)
              })
          },
          {
            prop: 'wfStatus',
            label: '流程状态',
            width: 110,
            align: 'center',
            formatter: (row: RealEstateDisposalItem) =>
              h(DictTag, {
                options: wf_status.value,
                value: resolveRealEstateWorkflowStatus(row)
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
  const detailData = ref<RealEstateDisposalItem>()

  const formData = reactive({
    assetNo: '',
    disposalType: 'cancel',
    targetAssetStatus: '6',
    reason: ''
  })

  const rules: FormRules = {
    assetNo: [{ required: true, message: '请输入资产编号', trigger: 'blur' }],
    disposalType: [{ required: true, message: '请选择处置类型', trigger: 'change' }],
    targetAssetStatus: [{ required: true, message: '请选择目标资产状态', trigger: 'change' }],
    reason: [{ required: true, message: '请输入处置原因', trigger: 'blur' }]
  }

  const formatDisposalType = (value?: string) => {
    return disposalTypeOptions.find((item) => item.value === value)?.label || '--'
  }

  const handleReset = () => {
    Object.assign(formFilters, initialSearchState)
    resetSearchParams()
  }

  const handleSearch = () => {
    Object.assign(searchParams, formFilters)
    getData()
  }

  const openCreateDialog = (
    context: RealEstateRouteAssetContext | null = routeAssetContext.value
  ) => {
    const guard = getRealEstateActionGuard('realEstateDisposal', context)
    if (guard.disabled) {
      ElMessage.warning(guard.reason || '当前资产暂不支持注销/处置')
      return
    }
    dialogAssetContext.value = context
    formData.assetNo = context?.assetNo || ''
    formData.disposalType = 'cancel'
    formData.targetAssetStatus = '6'
    formData.reason = ''
    dialogVisible.value = true
  }

  const submitDisposal = async () => {
    try {
      await formRef.value?.validate()

      submitting.value = true
      const payload = buildRealEstateDisposalPayload(dialogAssetContext.value, formData)

      await createRealEstateDisposal(payload)
      ElMessage.success('注销/处置申请已提交')
      dialogVisible.value = false
      refreshData()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('提交注销/处置申请失败:', error)
      }
    } finally {
      submitting.value = false
    }
  }

  const openDetail = async (row: RealEstateDisposalItem) => {
    try {
      detailVisible.value = true
      detailLoading.value = true
      detailData.value = await getRealEstateDisposalDetail(row.disposalNo)
    } catch (error) {
      detailVisible.value = false
      console.error('获取注销/处置详情失败:', error)
    } finally {
      detailLoading.value = false
    }
  }

  onMounted(() => {
    routeAssetContext.value = parseAssetRouteQuery(route.query as Record<string, unknown>)
    void asset_status.value
    void wf_status.value

    if (routeAssetContext.value?.assetNo) {
      formFilters.assetNo = routeAssetContext.value.assetNo
      Object.assign(searchParams, { assetNo: routeAssetContext.value.assetNo })
    }

    getData()

    if (shouldOpenCreateDialog(route.query as Record<string, unknown>) && routeAssetContext.value) {
      openCreateDialog(routeAssetContext.value)
    }
  })
</script>

<style lang="scss" scoped>
  .real-estate-disposal-page {
    padding: 12px;
  }
</style>
