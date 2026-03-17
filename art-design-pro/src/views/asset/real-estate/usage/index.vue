<template>
  <div class="real-estate-usage-page art-full-height flex flex-col p-3 overflow-hidden">
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
            v-auth="'asset:realEstateUsage:add'"
            type="primary"
            :disabled="createGuard.disabled"
            @click="openCreateDialog()"
          >
            发起用途变更
          </ElButton>
        </template>
      </ArtTableHeader>

      <ArtTable
        ref="tableRef"
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
        rowKey="usageChangeNo"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>

    <ElDialog v-model="dialogVisible" title="发起用途变更" width="560px" draggable destroy-on-close>
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
        <ElFormItem label="目标土地用途" prop="targetLandUse">
          <ElInput v-model="formData.targetLandUse" placeholder="请输入目标土地用途" clearable />
        </ElFormItem>
        <ElFormItem label="目标房屋用途" prop="targetBuildingUse">
          <ElInput
            v-model="formData.targetBuildingUse"
            placeholder="请输入目标房屋用途"
            clearable
          />
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
          <ElButton type="primary" :loading="submitting" @click="submitUsageChange">
            提交申请
          </ElButton>
        </span>
      </template>
    </ElDialog>

    <ElDialog v-model="detailVisible" title="用途变更详情" width="620px" destroy-on-close>
      <ElDescriptions v-loading="detailLoading" :column="1" border>
        <ElDescriptionsItem label="变更单号">
          {{ detailData?.usageChangeNo || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="资产编号">
          {{ detailData?.assetNo || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="原土地用途">
          {{ detailData?.oldLandUse || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="目标土地用途">
          {{ detailData?.targetLandUse || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="原房屋用途">
          {{ detailData?.oldBuildingUse || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="目标房屋用途">
          {{ detailData?.targetBuildingUse || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="状态">
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
    createUsageChange,
    getUsageChangeDetail,
    listUsageChange,
    type UsageChangeItem
  } from '@/api/asset/real-estate-usage'
  import {
    buildUsageChangePayload,
    formatRealEstateLatestAction,
    getRealEstateActionGuard,
    parseAssetRouteQuery,
    REAL_ESTATE_DIRECT_STATUS_OPTIONS,
    resolveRealEstateDocumentStatus,
    resolveRealEstateWorkflowStatus,
    shouldOpenCreateDialog,
    type RealEstateRouteAssetContext
  } from '../real-estate-lifecycle.helper'

  defineOptions({ name: 'AssetRealEstateUsage' })

  const route = useRoute()
  const { wf_status } = useDict('wf_status')
  const docStatusOptions = [...ASSET_TIMELINE_DOC_STATUS_OPTIONS]

  const routeAssetContext = ref<RealEstateRouteAssetContext | null>(null)
  const dialogAssetContext = ref<RealEstateRouteAssetContext | null>(null)
  const createGuard = computed(() => getRealEstateActionGuard('realEstateUsage', routeAssetContext.value))
  const routeAlertTitle = computed(() => {
    if (!routeAssetContext.value) return ''
    const latestActionText = formatRealEstateLatestAction(routeAssetContext.value)
    const latestActionPrefix = latestActionText ? `最近动作：${latestActionText}。` : ''
    const actionText = createGuard.value.disabled
      ? createGuard.value.reason || '当前不能直接发起用途变更。'
      : '可直接发起用途变更。'
    return `已从资产台账带入不动产 ${routeAssetContext.value.assetNo}，${latestActionPrefix}${actionText}`
  })

  const initialSearchState = {
    usageChangeNo: '',
    assetNo: '',
    status: undefined as string | undefined
  }

  const formFilters = reactive({ ...initialSearchState })

  const formItems = computed(() => [
    {
      label: '变更单号',
      key: 'usageChangeNo',
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
      apiFn: listUsageChange,
      immediate: false,
      columnsFactory: () => {
        const operationColumn: ColumnOption<UsageChangeItem> = {
          prop: 'operation',
          label: '操作',
          width: 110,
          align: 'right',
          formatter: (row: UsageChangeItem) =>
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
          { prop: 'usageChangeNo', label: '变更单号', width: 180 },
          { prop: 'assetNo', label: '资产编号', minWidth: 140 },
          { prop: 'targetLandUse', label: '目标土地用途', minWidth: 140 },
          { prop: 'targetBuildingUse', label: '目标房屋用途', minWidth: 140 },
          {
            prop: 'status',
            label: '状态',
            width: 110,
            align: 'center',
            formatter: (row: UsageChangeItem) =>
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
            formatter: (row: UsageChangeItem) =>
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
  const detailData = ref<UsageChangeItem>()

  const formData = reactive({
    assetNo: '',
    targetLandUse: '',
    targetBuildingUse: '',
    reason: ''
  })

  const rules: FormRules = {
    assetNo: [{ required: true, message: '请输入资产编号', trigger: 'blur' }],
    targetLandUse: [{ required: true, message: '请输入目标土地用途', trigger: 'blur' }],
    targetBuildingUse: [{ required: true, message: '请输入目标房屋用途', trigger: 'blur' }],
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
    const guard = getRealEstateActionGuard('realEstateUsage', context)
    if (guard.disabled) {
      ElMessage.warning(guard.reason || '当前资产暂不支持用途变更')
      return
    }
    dialogAssetContext.value = context
    formData.assetNo = context?.assetNo || ''
    formData.targetLandUse = ''
    formData.targetBuildingUse = ''
    formData.reason = ''
    dialogVisible.value = true
  }

  const submitUsageChange = async () => {
    try {
      await formRef.value?.validate()

      submitting.value = true
      const payload = buildUsageChangePayload(dialogAssetContext.value, formData)

      await createUsageChange(payload)
      ElMessage.success('用途变更已完成')
      dialogVisible.value = false
      refreshData()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('提交用途变更失败:', error)
      }
    } finally {
      submitting.value = false
    }
  }

  const openDetail = async (row: UsageChangeItem) => {
    try {
      detailVisible.value = true
      detailLoading.value = true
      detailData.value = await getUsageChangeDetail(row.usageChangeNo)
    } catch (error) {
      detailVisible.value = false
      console.error('获取用途变更详情失败:', error)
    } finally {
      detailLoading.value = false
    }
  }

  onMounted(() => {
    routeAssetContext.value = parseAssetRouteQuery(route.query as Record<string, unknown>)
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
  .real-estate-usage-page {
    padding: 12px;
  }
</style>
