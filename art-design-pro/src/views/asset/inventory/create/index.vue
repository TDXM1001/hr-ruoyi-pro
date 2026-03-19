<template>
  <div class="asset-inventory-create art-full-height flex flex-col gap-3 overflow-y-auto p-3">
    <ElCard class="head-card" shadow="never">
      <div class="flex flex-wrap items-start justify-between gap-3">
        <div>
          <ElLink type="primary" :underline="false" @click="goBack">返回资产盘点</ElLink>
          <div class="page-title">发起盘点任务</div>
          <div class="page-desc">
            按“三步式”建单：先填写任务信息，再选择资产，最后确认提交。提交后进入执行页登记结果。
          </div>
        </div>
        <ElTag type="info" effect="light">步骤 {{ activeStep + 1 }} / 3</ElTag>
      </div>
    </ElCard>

    <ElCard class="main-card flex-1 min-h-0" shadow="never">
      <ElSteps :active="activeStep" finish-status="success" class="mb-4">
        <ElStep title="任务信息" description="填写任务名称与计划盘点日期" />
        <ElStep title="选择资产" description="选择本次盘点范围资产" />
        <ElStep title="确认提交" description="确认摘要并提交任务" />
      </ElSteps>

      <div v-show="activeStep === 0">
        <ElForm
          ref="taskFormRef"
          :model="taskFormData"
          :rules="taskFormRules"
          label-width="110px"
          class="max-w-[920px]"
        >
          <ElRow :gutter="16">
            <ElCol :xs="24" :md="12">
              <ElFormItem label="任务名称" prop="taskName">
                <ElInput
                  v-model="taskFormData.taskName"
                  maxlength="120"
                  clearable
                  placeholder="请输入盘点任务名称"
                />
              </ElFormItem>
            </ElCol>
            <ElCol :xs="24" :md="12">
              <ElFormItem label="计划盘点日" prop="plannedDate">
                <ElDatePicker
                  v-model="taskFormData.plannedDate"
                  type="date"
                  value-format="YYYY-MM-DD"
                  class="w-full"
                  placeholder="请选择计划盘点日期"
                />
              </ElFormItem>
            </ElCol>
          </ElRow>
          <ElFormItem label="备注" prop="remark">
            <ElInput
              v-model="taskFormData.remark"
              type="textarea"
              :rows="3"
              maxlength="500"
              show-word-limit
              placeholder="请输入任务说明（可选）"
            />
          </ElFormItem>
        </ElForm>
      </div>

      <div v-show="activeStep === 1" class="flex flex-col gap-3">
        <ElAlert
          title="仅允许选择在册 / 使用中 / 闲置 / 盘点中的固定资产，待处置与已处置资产不允许加入盘点任务。"
          type="info"
          :closable="false"
          show-icon
        />

        <ArtSearchBar
          v-model="assetSearchForm"
          :items="assetSearchItems"
          :showExpand="true"
          @search="handleAssetSearch"
          @reset="handleAssetReset"
        />

        <div class="flex items-center justify-between gap-3">
          <ElTag type="primary" effect="light">已选 {{ selectedAssetRows.length }} 宗</ElTag>
          <ElButton text type="primary" @click="clearSelection">清空勾选</ElButton>
        </div>

        <ArtTable
          ref="assetTableRef"
          rowKey="assetId"
          :loading="assetLoading"
          :data="assetData"
          :columns="assetColumns"
          :pagination="assetPagination"
          :height="460"
          @selection-change="handleSelectionChange"
          @pagination:size-change="handleAssetSizeChange"
          @pagination:current-change="handleAssetCurrentChange"
        />
      </div>

      <div v-show="activeStep === 2" class="confirm-wrapper">
        <ElDescriptions :column="2" border>
          <ElDescriptionsItem label="任务名称">{{
            taskFormData.taskName || '-'
          }}</ElDescriptionsItem>
          <ElDescriptionsItem label="计划盘点日">{{
            taskFormData.plannedDate || '-'
          }}</ElDescriptionsItem>
          <ElDescriptionsItem label="盘点资产数"
            >{{ selectedAssetRows.length }} 宗</ElDescriptionsItem
          >
          <ElDescriptionsItem label="备注">{{ taskFormData.remark || '-' }}</ElDescriptionsItem>
        </ElDescriptions>

        <ElDivider class="!my-4" content-position="left">已选资产预览</ElDivider>

        <ArtTable
          rowKey="assetId"
          :data="selectedAssetRows"
          :columns="selectedPreviewColumns"
          :height="420"
        />
      </div>

      <div class="footer-actions">
        <ElButton @click="goBack">取消</ElButton>
        <ElButton v-if="activeStep > 0" @click="activeStep -= 1">上一步</ElButton>
        <ElButton v-if="activeStep < 2" type="primary" @click="handleNextStep">下一步</ElButton>
        <ElButton v-else type="primary" :loading="submitting" @click="handleSubmitTask">
          提交任务
        </ElButton>
      </div>
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import type { FormInstance, FormRules } from 'element-plus'
  import { ElMessage } from 'element-plus'
  import { useRouter } from 'vue-router'
  import { addInventoryTask } from '@/api/asset/inventory'
  import { listAssetLedger } from '@/api/asset/ledger'
  import DictTag from '@/components/DictTag/index.vue'
  import { useTable } from '@/hooks/core/useTable'
  import { useDict } from '@/utils/dict'

  defineOptions({ name: 'AssetInventoryCreate' })

  type AssetRow = {
    assetId: number
    assetCode?: string
    assetName?: string
    assetStatus?: string
    ownerDeptName?: string
    useDeptName?: string
  }

  const router = useRouter()
  const { ast_asset_status } = useDict('ast_asset_status')

  const activeStep = ref(0)
  const submitting = ref(false)
  const taskFormRef = ref<FormInstance>()
  const assetTableRef = ref<any>()

  const taskFormData = reactive({
    taskName: '',
    plannedDate: new Date().toISOString().slice(0, 10),
    remark: ''
  })

  const taskFormRules: FormRules = {
    taskName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
    plannedDate: [{ required: true, message: '请选择计划盘点日', trigger: 'change' }]
  }

  const selectableStatuses = new Set(['IN_LEDGER', 'IN_USE', 'IDLE', 'INVENTORYING'])

  const assetSearchInitialState = {
    assetCode: '',
    assetName: '',
    assetStatus: ''
  }
  const assetSearchForm = reactive({ ...assetSearchInitialState })

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
    handleCurrentChange: handleAssetCurrentChange
  } = useTable({
    core: {
      apiFn: listAssetLedger,
      apiParams: {
        assetType: 'FIXED',
        pageNum: 1,
        pageSize: 10
      },
      columnsFactory: () => [
        {
          type: 'selection',
          width: 48,
          align: 'center',
          selectable: (row: AssetRow) =>
            selectableStatuses.has(String(row.assetStatus || '').toUpperCase())
        },
        { prop: 'assetCode', label: '资产编码', minWidth: 140 },
        { prop: 'assetName', label: '资产名称', minWidth: 180, showOverflowTooltip: true },
        {
          prop: 'assetStatus',
          label: '资产状态',
          width: 120,
          formatter: (row: AssetRow) =>
            h(DictTag, { options: ast_asset_status.value, value: row.assetStatus })
        },
        { prop: 'ownerDeptName', label: '权属部门', minWidth: 120 },
        { prop: 'useDeptName', label: '使用部门', minWidth: 120 }
      ]
    }
  })

  const selectedAssetMap = ref<Record<number, AssetRow>>({})

  const selectedPreviewColumns = [
    { prop: 'assetCode', label: '资产编码', minWidth: 140 },
    { prop: 'assetName', label: '资产名称', minWidth: 180, showOverflowTooltip: true },
    {
      prop: 'assetStatus',
      label: '资产状态',
      width: 120,
      formatter: (row: AssetRow) =>
        h(DictTag, { options: ast_asset_status.value, value: row.assetStatus })
    },
    { prop: 'ownerDeptName', label: '权属部门', minWidth: 120 },
    { prop: 'useDeptName', label: '使用部门', minWidth: 120 }
  ]

  const selectedAssetRows = computed(() => Object.values(selectedAssetMap.value))

  const buildAssetQueryParams = () => {
    return {
      assetType: 'FIXED',
      assetCode: assetSearchForm.assetCode?.trim() || undefined,
      assetName: assetSearchForm.assetName?.trim() || undefined,
      assetStatus: assetSearchForm.assetStatus || undefined
    }
  }

  const syncPageSelection = () => {
    nextTick(() => {
      const pageRows = (assetData.value || []) as AssetRow[]
      const tableRef = assetTableRef.value?.elTableRef
      if (!tableRef) {
        return
      }
      tableRef.clearSelection?.()
      pageRows.forEach((row) => {
        if (selectedAssetMap.value[row.assetId]) {
          tableRef.toggleRowSelection?.(row, true)
        }
      })
    })
  }

  // 中文注释：按“当前页覆盖更新 + 跨页保留”策略维护勾选资产，避免翻页后丢失选择。
  const handleSelectionChange = (selection: AssetRow[]) => {
    const nextMap = { ...selectedAssetMap.value }
    const pageRows = (assetData.value || []) as AssetRow[]
    pageRows.forEach((row) => {
      delete nextMap[row.assetId]
    })
    selection.forEach((row) => {
      nextMap[row.assetId] = row
    })
    selectedAssetMap.value = nextMap
  }

  const clearSelection = () => {
    selectedAssetMap.value = {}
    assetTableRef.value?.elTableRef?.clearSelection?.()
  }

  const handleAssetSearch = () => {
    Object.assign(assetSearchParams, buildAssetQueryParams())
    getAssetData()
  }

  const handleAssetReset = () => {
    Object.assign(assetSearchForm, assetSearchInitialState)
    resetAssetSearchParams()
  }

  const handleNextStep = async () => {
    if (activeStep.value === 0) {
      const valid = await taskFormRef.value?.validate().catch(() => false)
      if (!valid) {
        return
      }
      activeStep.value = 1
      return
    }
    if (activeStep.value === 1) {
      if (!selectedAssetRows.value.length) {
        ElMessage.warning('请至少选择一宗资产')
        return
      }
      activeStep.value = 2
    }
  }

  const handleSubmitTask = async () => {
    submitting.value = true
    try {
      const taskId = await addInventoryTask({
        taskName: taskFormData.taskName.trim(),
        plannedDate: taskFormData.plannedDate,
        assetIds: selectedAssetRows.value.map((item) => item.assetId),
        remark: taskFormData.remark?.trim() || undefined
      })
      ElMessage.success('盘点任务创建成功')
      router.replace(`/asset/inventory/task/${taskId}`)
    } finally {
      submitting.value = false
    }
  }

  const goBack = () => {
    router.push('/asset/inventory')
  }

  watch(
    assetData,
    () => {
      syncPageSelection()
    },
    { immediate: true }
  )

  onMounted(() => {
    Object.assign(assetSearchParams, buildAssetQueryParams())
    getAssetData()
  })
</script>

<style scoped lang="scss">
  .asset-inventory-create {
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
  .main-card {
    border: 1px solid var(--asset-border);
    border-radius: 12px;
    background: var(--asset-panel-bg);
  }

  .page-title {
    margin-top: 6px;
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
    max-width: 920px;
  }

  .confirm-wrapper {
    display: flex;
    flex-direction: column;
  }

  .footer-actions {
    margin-top: 16px;
    padding-top: 16px;
    border-top: 1px solid #ebeff7;
    display: flex;
    justify-content: flex-end;
    gap: 10px;
  }
</style>
