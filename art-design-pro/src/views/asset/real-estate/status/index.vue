<template>
  <div class="real-estate-status-page art-full-height flex flex-col p-3 overflow-hidden">
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
            v-auth="'asset:realEstateStatus:add'"
            type="primary"
            :disabled="createGuard.disabled"
            @click="openCreateDialog()"
          >
            发起状态变更
          </ElButton>
        </template>
      </ArtTableHeader>

      <ArtTable
        ref="tableRef"
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
        rowKey="statusChangeNo"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>

    <ElDialog v-model="dialogVisible" title="发起状态变更" width="560px" draggable destroy-on-close>
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
        <ElFormItem label="变更原因" prop="reason">
          <ElInput
            v-model="formData.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入变更原因"
            clearable
          />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <span class="dialog-footer">
          <ElButton @click="dialogVisible = false">取消</ElButton>
          <ElButton type="primary" :loading="submitting" @click="submitStatusChange">
            提交申请
          </ElButton>
        </span>
      </template>
    </ElDialog>

    <ElDialog v-model="detailVisible" title="状态变更详情" width="620px" destroy-on-close>
      <ElDescriptions v-loading="detailLoading" :column="1" border>
        <ElDescriptionsItem label="变更单号">
          {{ detailData?.statusChangeNo || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="资产编号">
          {{ detailData?.assetNo || '--' }}
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
        <ElDescriptionsItem label="变更原因">
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
    createStatusChange,
    getStatusChangeDetail,
    listStatusChange,
    type StatusChangeItem
  } from '@/api/asset/real-estate-status'
  import {
    buildStatusChangePayload,
    formatRealEstateLatestAction,
    getRealEstateActionGuard,
    parseAssetRouteQuery,
    REAL_ESTATE_DIRECT_STATUS_OPTIONS,
    resolveRealEstateDocumentStatus,
    resolveRealEstateWorkflowStatus,
    shouldOpenCreateDialog,
    type RealEstateRouteAssetContext
  } from '../real-estate-lifecycle.helper'

  defineOptions({ name: 'AssetRealEstateStatus' })

  const route = useRoute()
  const { asset_status, wf_status } = useDict('asset_status', 'wf_status')
  const docStatusOptions = [...ASSET_TIMELINE_DOC_STATUS_OPTIONS]

  const routeAssetContext = ref<RealEstateRouteAssetContext | null>(null)
  const dialogAssetContext = ref<RealEstateRouteAssetContext | null>(null)
  const createGuard = computed(() =>
    getRealEstateActionGuard('realEstateStatus', routeAssetContext.value)
  )
  const routeAlertTitle = computed(() => {
    if (!routeAssetContext.value) return ''
    const latestActionText = formatRealEstateLatestAction(routeAssetContext.value)
    const latestActionPrefix = latestActionText ? `最近动作：${latestActionText}。` : ''
    const actionText = createGuard.value.disabled
      ? createGuard.value.reason || '当前不能直接发起状态变更。'
      : '可直接发起状态变更。'
    return `已从资产台账带入不动产 ${routeAssetContext.value.assetNo}，${latestActionPrefix}${actionText}`
  })

  const initialSearchState = {
    statusChangeNo: '',
    assetNo: '',
    status: undefined as string | undefined
  }

  const formFilters = reactive({ ...initialSearchState })

  const formItems = computed(() => [
    {
      label: '变更单号',
      key: 'statusChangeNo',
      type: 'input',
      props: { placeholder: '请输入变更单号', clearable: true }
    },
    {
      label: '资产编号',
      key: 'assetNo',
      type: 'input',
      props: { placeholder: '请输入资产编号', clearable: true }
    },
    {
      label: '状态',
      key: 'status',
      type: 'select',
      props: {
        placeholder: '请选择状态',
        clearable: true,
        options: REAL_ESTATE_DIRECT_STATUS_OPTIONS
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
      apiFn: listStatusChange,
      immediate: false,
      columnsFactory: () => {
        const operationColumn: ColumnOption<StatusChangeItem> = {
          prop: 'operation',
          label: '操作',
          width: 110,
          align: 'right',
          formatter: (row: StatusChangeItem) =>
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
          { prop: 'statusChangeNo', label: '变更单号', width: 180 },
          { prop: 'assetNo', label: '资产编号', minWidth: 140 },
          {
            prop: 'targetAssetStatus',
            label: '目标资产状态',
            width: 140,
            align: 'center',
            formatter: (row: StatusChangeItem) =>
              h(DictTag, {
                options: asset_status.value,
                value: row.targetAssetStatus
              })
          },
          {
            prop: 'status',
            label: '单据状态',
            width: 110,
            align: 'center',
            formatter: (row: StatusChangeItem) =>
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
            formatter: (row: StatusChangeItem) =>
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
  const detailData = ref<StatusChangeItem>()

  const formData = reactive({
    assetNo: '',
    targetAssetStatus: '',
    reason: ''
  })

  const rules: FormRules = {
    assetNo: [{ required: true, message: '请输入资产编号', trigger: 'blur' }],
    targetAssetStatus: [{ required: true, message: '请选择目标资产状态', trigger: 'change' }],
    reason: [{ required: true, message: '请输入变更原因', trigger: 'blur' }]
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
    const guard = getRealEstateActionGuard('realEstateStatus', context)
    if (guard.disabled) {
      ElMessage.warning(guard.reason || '当前资产暂不支持状态变更')
      return
    }
    dialogAssetContext.value = context
    formData.assetNo = context?.assetNo || ''
    formData.targetAssetStatus = ''
    formData.reason = ''
    dialogVisible.value = true
  }

  const submitStatusChange = async () => {
    try {
      await formRef.value?.validate()

      submitting.value = true
      const payload = buildStatusChangePayload(dialogAssetContext.value, formData)

      await createStatusChange(payload)
      ElMessage.success('状态变更已完成')
      dialogVisible.value = false
      refreshData()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('提交状态变更失败:', error)
      }
    } finally {
      submitting.value = false
    }
  }

  const openDetail = async (row: StatusChangeItem) => {
    try {
      detailVisible.value = true
      detailLoading.value = true
      detailData.value = await getStatusChangeDetail(row.statusChangeNo)
    } catch (error) {
      detailVisible.value = false
      console.error('获取状态变更详情失败:', error)
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
  .real-estate-status-page {
    padding: 12px;
  }
</style>
