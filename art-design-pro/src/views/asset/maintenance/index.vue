<template>
  <div class="asset-maintenance-page art-full-height flex flex-col p-3 overflow-hidden">
    <ElAlert
      v-if="routeAssetContext"
      class="mb-3"
      type="info"
      :closable="false"
      :title="`已从资产台账带入资产 ${routeAssetContext.assetNo}，可直接发起维修申请。`"
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
          <ElButton v-auth="'asset:maintenance:add'" type="primary" @click="openCreateDialog()">
            发起维修
          </ElButton>
        </template>
      </ArtTableHeader>

      <ArtTable
        ref="tableRef"
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
        rowKey="maintenanceNo"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>

    <ElDialog v-model="dialogVisible" title="发起维修申请" width="520px" draggable destroy-on-close>
      <ElForm :model="formData" :rules="rules" ref="formRef" label-width="100px">
        <ElFormItem label="资产编号" prop="assetNo">
          <ElInput
            v-model="formData.assetNo"
            :disabled="Boolean(dialogAssetContext?.assetNo)"
            placeholder="请输入资产编号"
            clearable
          />
        </ElFormItem>
        <ElFormItem v-if="dialogAssetContext?.assetName" label="资产名称">
          <ElInput :model-value="dialogAssetContext?.assetName" disabled />
        </ElFormItem>
        <ElFormItem v-if="dialogAssetContext?.assetStatus" label="当前状态">
          <ElInput :model-value="dialogAssetContext?.assetStatus" disabled />
        </ElFormItem>
        <ElFormItem label="维修原因" prop="reason">
          <ElInput
            v-model="formData.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入维修原因"
            clearable
          />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <span class="dialog-footer">
          <ElButton @click="dialogVisible = false">取消</ElButton>
          <ElButton type="primary" :loading="submitting" @click="submitMaintenance"
            >提交申请</ElButton
          >
        </span>
      </template>
    </ElDialog>

    <ElDialog v-model="detailVisible" title="维修详情" width="560px" destroy-on-close>
      <ElDescriptions v-loading="detailLoading" :column="1" border>
        <ElDescriptionsItem label="维修单号">
          {{ detailData?.maintenanceNo || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="资产编号">
          {{ detailData?.assetNo || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="申请人ID">
          {{ detailData?.applyUserId ?? '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="状态">
          <DictTag
            :options="wf_status"
            :value="mapMaintenanceStatusToWorkflow(detailData?.status)"
          />
        </ElDescriptionsItem>
        <ElDescriptionsItem label="维修原因">
          {{ detailData?.reason || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="创建时间">
          {{ detailData?.createTime || '--' }}
        </ElDescriptionsItem>
      </ElDescriptions>
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
  import { computed, h, onMounted, reactive, ref } from 'vue'
  import { useRoute } from 'vue-router'
  import { ElButton, ElMessage, ElMessageBox } from 'element-plus'
  import type { FormInstance, FormRules } from 'element-plus'
  import type { ColumnOption } from '@/types/component'
  import DictTag from '@/components/DictTag/index.vue'
  import { useTable } from '@/hooks/core/useTable'
  import { useDict } from '@/utils/dict'
  import {
    applyMaintenance,
    completeMaintenance,
    getMaintenance,
    listMaintenance,
    type ApplyMaintenanceReq,
    type AssetMaintenanceItem
  } from '@/api/asset/maintenance'

  defineOptions({ name: 'AssetMaintenance' })

  interface RouteAssetContext {
    assetId?: number
    assetNo: string
    assetName?: string
    assetStatus?: string
  }

  const route = useRoute()
  const { wf_status } = useDict('wf_status')

  const routeAssetContext = ref<RouteAssetContext | null>(null)
  const dialogAssetContext = ref<RouteAssetContext | null>(null)

  const initialSearchState = {
    maintenanceNo: '',
    assetNo: '',
    status: undefined as number | undefined
  }

  const formFilters = reactive({ ...initialSearchState })

  const formItems = computed(() => [
    {
      label: '维修单号',
      key: 'maintenanceNo',
      type: 'input',
      props: { placeholder: '请输入维修单号', clearable: true }
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
        options: wf_status.value
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
      apiFn: listMaintenance,
      immediate: false,
      columnsFactory: () => {
        const operationColumn: ColumnOption<AssetMaintenanceItem> = {
          prop: 'operation',
          label: '操作',
          width: 180,
          align: 'right',
          formatter: (row: AssetMaintenanceItem) =>
            h('div', { class: 'flex justify-end gap-3' }, buildOperationButtons(row))
        }

        return [
          { type: 'index', label: '序号', width: 60, align: 'center' },
          { prop: 'maintenanceNo', label: '维修单号', width: 160 },
          { prop: 'assetNo', label: '资产编号', minWidth: 140 },
          { prop: 'applyUserId', label: '申请人ID', width: 120, align: 'center' },
          { prop: 'reason', label: '维修原因', minWidth: 220 },
          {
            prop: 'status',
            label: '状态',
            width: 110,
            align: 'center',
            formatter: (row: AssetMaintenanceItem) =>
              h(DictTag, {
                options: wf_status.value,
                value: mapMaintenanceStatusToWorkflow(row.status)
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
  const detailData = ref<AssetMaintenanceItem>()

  const formData = reactive({
    assetNo: '',
    reason: ''
  })

  const rules: FormRules = {
    assetNo: [{ required: true, message: '请输入资产编号', trigger: 'blur' }],
    reason: [{ required: true, message: '请输入维修原因', trigger: 'blur' }]
  }

  /**
   * 把路由查询转换成资产上下文，供页面带入新增表单和默认筛选。
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
      assetStatus: String(route.query.assetStatus || '').trim() || undefined
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

  /**
   * 维修页直接消费工作流状态字典，不再额外维护一套前端枚举。
   */
  const mapMaintenanceStatusToWorkflow = (status?: number) => {
    if (status === 0) return 'IN_PROGRESS'
    if (status === 1 || status === 3) return 'COMPLETED'
    if (status === 2) return 'REJECTED'
    return ''
  }

  const openCreateDialog = (context: RouteAssetContext | null = routeAssetContext.value) => {
    dialogAssetContext.value = context
    formData.assetNo = context?.assetNo || ''
    formData.reason = ''
    dialogVisible.value = true
  }

  const submitMaintenance = async () => {
    try {
      await formRef.value?.validate()

      submitting.value = true
      const payload: ApplyMaintenanceReq = {
        assetId: dialogAssetContext.value?.assetId,
        assetNo: formData.assetNo.trim(),
        reason: formData.reason.trim()
      }

      await applyMaintenance(payload)
      ElMessage.success('维修申请已提交')
      dialogVisible.value = false
      refreshData()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('提交维修申请失败:', error)
      }
    } finally {
      submitting.value = false
    }
  }

  const openDetail = async (row: AssetMaintenanceItem) => {
    try {
      detailVisible.value = true
      detailLoading.value = true
      detailData.value = await getMaintenance(row.maintenanceNo)
    } catch (error) {
      detailVisible.value = false
      console.error('获取维修详情失败:', error)
    } finally {
      detailLoading.value = false
    }
  }

  const handleComplete = async (row: AssetMaintenanceItem) => {
    try {
      await ElMessageBox.confirm(`确认完成维修单“${row.maintenanceNo}”吗？`, '提示', {
        type: 'warning'
      })
      await completeMaintenance(row.maintenanceNo)
      ElMessage.success('维修已完成')
      refreshData()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('完成维修失败:', error)
      }
    }
  }

  const buildOperationButtons = (row: AssetMaintenanceItem) => {
    const buttons = [
      h(
        ElButton,
        {
          type: 'primary',
          link: true,
          onClick: () => openDetail(row)
        },
        () => '详情'
      )
    ]

    if (row.status === 1) {
      buttons.push(
        h(
          ElButton,
          {
            type: 'success',
            link: true,
            onClick: () => handleComplete(row)
          },
          () => '完成维修'
        )
      )
    }

    return buttons
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
  .asset-maintenance-page {
    padding: 12px;
  }
</style>
