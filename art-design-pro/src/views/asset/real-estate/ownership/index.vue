<template>
  <div class="real-estate-ownership-page art-full-height flex flex-col p-3 overflow-hidden">
    <ElAlert
      v-if="routeAssetContext"
      class="mb-3"
      :type="createGuard.disabled ? 'warning' : 'info'"
      :closable="false"
      :title="routeAlertTitle"
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
          <ElButton
            v-auth="'asset:realEstateOwnership:add'"
            type="primary"
            :disabled="createGuard.disabled"
            @click="openCreateDialog()"
          >
            发起权属变更
          </ElButton>
        </template>
      </ArtTableHeader>

      <ArtTable
        ref="tableRef"
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
        rowKey="ownershipChangeNo"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>

    <ElDialog
      v-model="dialogVisible"
      title="发起权属变更申请"
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
        <ElFormItem label="目标权属人" prop="targetRightsHolder">
          <ElInput v-model="formData.targetRightsHolder" placeholder="请输入目标权属人" clearable />
        </ElFormItem>
        <ElFormItem label="目标产权证号" prop="targetPropertyCertNo">
          <ElInput
            v-model="formData.targetPropertyCertNo"
            placeholder="请输入目标产权证号"
            clearable
          />
        </ElFormItem>
        <ElFormItem label="目标登记日期" prop="targetRegistrationDate">
          <ElDatePicker
            v-model="formData.targetRegistrationDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择目标登记日期"
            class="w-full"
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
          <ElButton type="primary" :loading="submitting" @click="submitOwnershipChange">
            提交申请
          </ElButton>
        </span>
      </template>
    </ElDialog>

    <ElDialog v-model="detailVisible" title="权属变更详情" width="620px" destroy-on-close>
      <ElDescriptions v-loading="detailLoading" :column="1" border>
        <ElDescriptionsItem label="变更单号">
          {{ detailData?.ownershipChangeNo || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="资产编号">
          {{ detailData?.assetNo || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="原权属人">
          {{ detailData?.oldRightsHolder || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="目标权属人">
          {{ detailData?.targetRightsHolder || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="原产权证号">
          {{ detailData?.oldPropertyCertNo || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="目标产权证号">
          {{ detailData?.targetPropertyCertNo || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="目标登记日期">
          {{ detailData?.targetRegistrationDate || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="状态">
          <DictTag
            :options="wf_status"
            :value="mapRealEstateStatusToWorkflow(detailData?.status)"
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
  import { useDict } from '@/utils/dict'
  import {
    createOwnershipChange,
    getOwnershipChangeDetail,
    listOwnershipChange,
    type CreateOwnershipChangeReq,
    type OwnershipChangeItem
  } from '@/api/asset/real-estate-ownership'
  import {
    formatRealEstateLatestAction,
    getRealEstateActionGuard,
    mapRealEstateStatusToWorkflow,
    parseAssetRouteQuery,
    REAL_ESTATE_APPROVAL_STATUS_OPTIONS,
    shouldOpenCreateDialog,
    type RealEstateRouteAssetContext
  } from '../real-estate-lifecycle.helper'

  defineOptions({ name: 'AssetRealEstateOwnership' })

  const route = useRoute()
  const { wf_status } = useDict('wf_status')

  const routeAssetContext = ref<RealEstateRouteAssetContext | null>(null)
  const dialogAssetContext = ref<RealEstateRouteAssetContext | null>(null)
  const createGuard = computed(() =>
    getRealEstateActionGuard('realEstateOwnership', routeAssetContext.value)
  )
  const routeAlertTitle = computed(() => {
    if (!routeAssetContext.value) return ''
    const latestActionText = formatRealEstateLatestAction(routeAssetContext.value)
    const latestActionPrefix = latestActionText ? `最近动作：${latestActionText}。` : ''
    const actionText = createGuard.value.disabled
      ? createGuard.value.reason || '当前不能直接发起权属变更。'
      : '可直接发起权属变更申请。'
    return `已从资产台账带入不动产 ${routeAssetContext.value.assetNo}，${latestActionPrefix}${actionText}`
  })

  const initialSearchState = {
    ownershipChangeNo: '',
    assetNo: '',
    status: undefined as string | undefined
  }

  const formFilters = reactive({ ...initialSearchState })

  const formItems = computed(() => [
    {
      label: '变更单号',
      key: 'ownershipChangeNo',
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
      apiFn: listOwnershipChange,
      immediate: false,
      columnsFactory: () => {
        const operationColumn: ColumnOption<OwnershipChangeItem> = {
          prop: 'operation',
          label: '操作',
          width: 110,
          align: 'right',
          formatter: (row: OwnershipChangeItem) =>
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
          { prop: 'ownershipChangeNo', label: '变更单号', width: 180 },
          { prop: 'assetNo', label: '资产编号', minWidth: 140 },
          { prop: 'targetRightsHolder', label: '目标权属人', minWidth: 140 },
          { prop: 'targetPropertyCertNo', label: '目标产权证号', minWidth: 180 },
          {
            prop: 'status',
            label: '状态',
            width: 110,
            align: 'center',
            formatter: (row: OwnershipChangeItem) =>
              h(DictTag, {
                options: wf_status.value,
                value: mapRealEstateStatusToWorkflow(row.status)
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
  const detailData = ref<OwnershipChangeItem>()

  const formData = reactive({
    assetNo: '',
    targetRightsHolder: '',
    targetPropertyCertNo: '',
    targetRegistrationDate: '',
    reason: ''
  })

  const rules: FormRules = {
    assetNo: [{ required: true, message: '请输入资产编号', trigger: 'blur' }],
    targetRightsHolder: [{ required: true, message: '请输入目标权属人', trigger: 'blur' }],
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

  /** 把路由上下文灌进弹窗，避免从资产台账二次选择。 */
  const openCreateDialog = (
    context: RealEstateRouteAssetContext | null = routeAssetContext.value
  ) => {
    const guard = getRealEstateActionGuard('realEstateOwnership', context)
    if (guard.disabled) {
      ElMessage.warning(guard.reason || '当前资产暂不支持权属变更')
      return
    }
    dialogAssetContext.value = context
    formData.assetNo = context?.assetNo || ''
    formData.targetRightsHolder = ''
    formData.targetPropertyCertNo = ''
    formData.targetRegistrationDate = ''
    formData.reason = ''
    dialogVisible.value = true
  }

  const submitOwnershipChange = async () => {
    try {
      await formRef.value?.validate()

      submitting.value = true
      const payload: CreateOwnershipChangeReq = {
        assetId: dialogAssetContext.value?.assetId,
        assetNo: formData.assetNo.trim() || undefined,
        targetRightsHolder: formData.targetRightsHolder.trim(),
        targetPropertyCertNo: formData.targetPropertyCertNo.trim() || undefined,
        targetRegistrationDate: formData.targetRegistrationDate || undefined,
        reason: formData.reason.trim()
      }

      await createOwnershipChange(payload)
      ElMessage.success('权属变更申请已提交')
      dialogVisible.value = false
      refreshData()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('提交权属变更申请失败:', error)
      }
    } finally {
      submitting.value = false
    }
  }

  const openDetail = async (row: OwnershipChangeItem) => {
    try {
      detailVisible.value = true
      detailLoading.value = true
      detailData.value = await getOwnershipChangeDetail(row.ownershipChangeNo)
    } catch (error) {
      detailVisible.value = false
      console.error('获取权属变更详情失败:', error)
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
  .real-estate-ownership-page {
    padding: 12px;
  }
</style>
