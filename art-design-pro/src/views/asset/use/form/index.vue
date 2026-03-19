<template>
  <div class="asset-use-form-page art-full-height flex flex-col gap-3 overflow-hidden p-3">
    <ElCard class="head-card" shadow="never">
      <div class="flex flex-wrap items-start justify-between gap-3">
        <div class="flex flex-col gap-2">
          <ElButton link type="primary" @click="goBack">返回资产使用</ElButton>
          <div class="page-title">发起资产交接单</div>
          <div class="page-desc">
            一张交接单支持同类固定资产批量办理，提交后将自动写入交接记录并回写台账使用信息。
          </div>
        </div>
        <ElSpace wrap>
          <ElTag type="success" effect="light">动作：{{ handoverTypeLabel }}</ElTag>
          <ElTag type="warning" effect="light">可选状态：{{ allowedStatusLabelText }}</ElTag>
        </ElSpace>
      </div>
    </ElCard>

    <ElCard class="form-card" shadow="never">
      <template #header>
        <div class="card-title">交接信息</div>
      </template>

      <ElForm ref="formRef" :model="formData" :rules="formRules" label-width="110px">
        <ElRow :gutter="16">
          <ElCol :span="8">
            <ElFormItem label="交接动作" prop="handoverType">
              <ElRadioGroup v-model="handoverType" @change="handleHandoverTypeChange">
                <ElRadioButton label="ASSIGN">领用</ElRadioButton>
                <ElRadioButton label="TRANSFER">调拨</ElRadioButton>
                <ElRadioButton label="RETURN">退还</ElRadioButton>
              </ElRadioGroup>
            </ElFormItem>
          </ElCol>
          <ElCol :span="8">
            <ElFormItem label="交接日期" prop="handoverDate">
              <ElDatePicker
                v-model="formData.handoverDate"
                type="date"
                value-format="YYYY-MM-DD"
                class="w-full"
                placeholder="请选择交接日期"
              />
            </ElFormItem>
          </ElCol>
          <ElCol :span="8">
            <ElFormItem label="目标部门" prop="toDeptId">
              <ElTreeSelect
                v-model="formData.toDeptId"
                :data="deptOptions"
                :props="treeSelectProps"
                value-key="id"
                check-strictly
                filterable
                clearable
                class="w-full"
                :placeholder="deptPlaceholder"
                :render-after-expand="false"
              />
            </ElFormItem>
          </ElCol>
        </ElRow>

        <ElRow :gutter="16">
          <ElCol :span="8">
            <ElFormItem label="目标责任人" prop="toUserId">
              <ElSelect
                v-model="formData.toUserId"
                clearable
                filterable
                remote
                reserve-keyword
                class="w-full"
                placeholder="请输入责任人姓名搜索"
                :remote-method="handleResponsibleUserSearch"
                :loading="responsibleUserLoading"
              >
                <ElOption
                  v-for="item in responsibleUserOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </ElSelect>
            </ElFormItem>
          </ElCol>
          <ElCol :span="8">
            <ElFormItem label="目标位置" prop="locationName">
              <ElInput
                v-model="formData.locationName"
                maxlength="200"
                clearable
                placeholder="请输入交接后使用位置，不填则沿用原位置"
              />
            </ElFormItem>
          </ElCol>
          <ElCol :span="8">
            <ElFormItem label="备注" prop="remark">
              <ElInput
                v-model="formData.remark"
                maxlength="500"
                clearable
                placeholder="请输入交接说明（可选）"
              />
            </ElFormItem>
          </ElCol>
        </ElRow>
      </ElForm>
    </ElCard>

    <ElCard class="asset-card flex-1 min-h-0 overflow-hidden" shadow="never">
      <template #header>
        <div class="card-header">
          <div class="card-title">选择交接资产</div>
          <ElSpace wrap>
            <ElTag type="info" effect="light">已选 {{ selectedAssetRows.length }} 宗</ElTag>
            <ElButton plain :disabled="!selectedAssetRows.length" @click="clearSelection"
              >清空勾选</ElButton
            >
          </ElSpace>
        </div>
      </template>

      <ArtSearchBar
        :key="assetSearchBarKey"
        v-model="assetSearchForm"
        :items="assetSearchItems"
        :showExpand="true"
        @search="handleAssetSearch"
        @reset="handleAssetReset"
      />

      <ArtTable
        ref="assetTableRef"
        rowKey="assetId"
        :loading="assetLoading"
        :data="assetData"
        :columns="assetColumns"
        :pagination="assetPagination"
        @selection-change="handleSelectionChange"
        @pagination:size-change="handleAssetSizeChange"
        @pagination:current-change="handleAssetCurrentChange"
      />
    </ElCard>

    <div class="page-footer">
      <ElButton @click="goBack">取消</ElButton>
      <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">提交交接单</ElButton>
    </div>
  </div>
</template>

<script setup lang="ts">
  import type { FormInstance, FormRules } from 'element-plus'
  import { ElMessage } from 'element-plus'
  import type { AssetTreeOption, AssetUserOption } from '@/api/asset/ledger'
  import { listAssetLedger, getAssetDeptTree, listAssetResponsibleUsers } from '@/api/asset/ledger'
  import { addAssetHandoverOrder } from '@/api/asset/handover'
  import { useDict } from '@/utils/dict'
  import { useTable } from '@/hooks/core/useTable'
  import DictTag from '@/components/DictTag/index.vue'

  defineOptions({ name: 'AssetUseCreate' })

  type HandoverType = 'ASSIGN' | 'TRANSFER' | 'RETURN'

  type DictOption = {
    value: string | number
    label: string
  }

  type AssetRow = {
    assetId: number
    assetCode: string
    assetName: string
    assetStatus: string
    ownerDeptName?: string
    useDeptName?: string
    responsibleUserName?: string
    locationName?: string
  }

  const { ast_asset_status } = useDict('ast_asset_status')
  const router = useRouter()
  const route = useRoute()

  const formRef = ref<FormInstance>()
  const assetTableRef = ref<any>()
  const submitLoading = ref(false)
  const responsibleUserLoading = ref(false)

  const deptOptions = ref<AssetTreeOption[]>([])
  const responsibleUserOptions = ref<AssetUserOption[]>([])
  const selectedAssetRows = ref<AssetRow[]>([])
  const handoverType = ref<HandoverType>('ASSIGN')

  const treeSelectProps = {
    value: 'id',
    label: 'label',
    children: 'children',
    disabled: 'disabled'
  }

  const allowedStatusByType: Record<HandoverType, string[]> = {
    ASSIGN: ['IN_LEDGER', 'IDLE'],
    TRANSFER: ['IN_USE'],
    RETURN: ['IN_USE']
  }

  const formData = reactive({
    handoverDate: new Date().toISOString().slice(0, 10),
    toDeptId: undefined as number | undefined,
    toUserId: undefined as number | undefined,
    locationName: '',
    remark: ''
  })

  const normalizeHandoverType = (type?: string): HandoverType => {
    const value = String(type || '').toUpperCase()
    if (value === 'TRANSFER' || value === 'RETURN') {
      return value
    }
    return 'ASSIGN'
  }

  const handoverTypeLabel = computed(() => {
    const mapper: Record<HandoverType, string> = {
      ASSIGN: '领用',
      TRANSFER: '调拨',
      RETURN: '退还'
    }
    return mapper[handoverType.value]
  })

  const getStatusDictLabel = (status: string) => {
    return (
      (ast_asset_status.value as DictOption[]).find((item: DictOption) => item.value === status)
        ?.label || status
    )
  }

  const allowedStatuses = computed(() => {
    return allowedStatusByType[handoverType.value]
  })

  const allowedStatusLabelText = computed(() => {
    return allowedStatuses.value.map((status) => getStatusDictLabel(status)).join(' / ')
  })

  const deptPlaceholder = computed(() => {
    return handoverType.value === 'RETURN' ? '可选：不填则默认退还到权属部门' : '请选择目标部门'
  })

  // 中文注释：交接动作不同，目标字段的必填策略需要与后端规则保持一致。
  const formRules = computed<FormRules>(() => ({
    handoverDate: [{ required: true, message: '请选择交接日期', trigger: 'change' }],
    toDeptId: [
      {
        required: handoverType.value !== 'RETURN',
        message: '领用/调拨必须选择目标部门',
        trigger: 'change'
      }
    ],
    toUserId: [
      {
        required: handoverType.value === 'ASSIGN' || handoverType.value === 'TRANSFER',
        message: '领用/调拨必须选择目标责任人',
        trigger: 'change'
      }
    ]
  }))

  const assetInitialSearchState = {
    assetCode: '',
    assetName: '',
    assetStatus: '',
    ownerDeptId: undefined as number | undefined,
    useDeptId: undefined as number | undefined,
    responsibleUserId: undefined as number | undefined
  }
  const assetSearchForm = reactive({ ...assetInitialSearchState })

  const assetSearchBarKey = computed(() => {
    return (
      deptOptions.value.length + responsibleUserOptions.value.length + ast_asset_status.value.length
    )
  })

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
    },
    {
      label: '权属部门',
      key: 'ownerDeptId',
      type: 'treeselect',
      props: {
        data: deptOptions.value,
        props: treeSelectProps,
        valueKey: 'id',
        clearable: true,
        checkStrictly: true,
        filterable: true,
        placeholder: '请选择权属部门',
        renderAfterExpand: false
      }
    },
    {
      label: '使用部门',
      key: 'useDeptId',
      type: 'treeselect',
      props: {
        data: deptOptions.value,
        props: treeSelectProps,
        valueKey: 'id',
        clearable: true,
        checkStrictly: true,
        filterable: true,
        placeholder: '请选择使用部门',
        renderAfterExpand: false
      }
    },
    {
      label: '责任人',
      key: 'responsibleUserId',
      type: 'select',
      props: {
        placeholder: '请输入责任人姓名搜索',
        clearable: true,
        filterable: true,
        remote: true,
        reserveKeyword: true,
        loading: responsibleUserLoading.value,
        remoteMethod: handleResponsibleUserSearch,
        options: responsibleUserOptions.value
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
          selectable: (row: AssetRow) => isAssetSelectable(row)
        },
        { prop: 'assetCode', label: '资产编码', minWidth: 140 },
        { prop: 'assetName', label: '资产名称', minWidth: 180 },
        {
          prop: 'assetStatus',
          label: '资产状态',
          width: 120,
          formatter: (row: AssetRow) =>
            h(DictTag, { options: ast_asset_status.value, value: row.assetStatus })
        },
        { prop: 'ownerDeptName', label: '权属部门', minWidth: 130 },
        { prop: 'useDeptName', label: '使用部门', minWidth: 130 },
        { prop: 'responsibleUserName', label: '责任人', width: 120 },
        { prop: 'locationName', label: '资产位置', minWidth: 150, showOverflowTooltip: true }
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

  const handleResponsibleUserSearch = async (keyword = '') => {
    responsibleUserLoading.value = true
    try {
      const response = await listAssetResponsibleUsers({ keyword: keyword.trim() })
      responsibleUserOptions.value = toArrayData<AssetUserOption>(response)
    } finally {
      responsibleUserLoading.value = false
    }
  }

  const buildAssetQueryParams = () => {
    return {
      assetType: 'FIXED',
      assetCode: assetSearchForm.assetCode?.trim() || undefined,
      assetName: assetSearchForm.assetName?.trim() || undefined,
      assetStatus: assetSearchForm.assetStatus || undefined,
      ownerDeptId: assetSearchForm.ownerDeptId,
      useDeptId: assetSearchForm.useDeptId,
      responsibleUserId: assetSearchForm.responsibleUserId
    }
  }

  const handleAssetSearch = () => {
    Object.assign(assetSearchParams, buildAssetQueryParams())
    getAssetData()
  }

  const clearSelection = () => {
    selectedAssetRows.value = []
    assetTableRef.value?.elTableRef?.clearSelection?.()
  }

  const handleAssetReset = () => {
    Object.assign(assetSearchForm, assetInitialSearchState)
    clearSelection()
    resetAssetSearchParams()
  }

  const normalizeStatus = (status?: string) => String(status || '').toUpperCase()

  const isAssetSelectable = (row: AssetRow) => {
    return allowedStatuses.value.includes(normalizeStatus(row.assetStatus))
  }

  const handleSelectionChange = (selection: AssetRow[]) => {
    selectedAssetRows.value = selection
  }

  const showWarning = (message: string) => {
    ElMessage({
      type: 'warning',
      message,
      grouping: true
    })
  }

  const validateSelectedAssets = () => {
    if (!selectedAssetRows.value.length) {
      return { valid: false, message: '请先勾选要交接的资产' }
    }
    const invalidRows = selectedAssetRows.value.filter(
      (row) => !allowedStatuses.value.includes(normalizeStatus(row.assetStatus))
    )
    if (!invalidRows.length) {
      return { valid: true, message: '' }
    }
    const invalidCodes = invalidRows
      .slice(0, 5)
      .map((row) => row.assetCode)
      .join('、')
    return {
      valid: false,
      message: `以下资产状态不满足${handoverTypeLabel.value}规则：${invalidCodes}${invalidRows.length > 5 ? ' 等' : ''}`
    }
  }

  const buildPayload = () => {
    return {
      handoverType: handoverType.value,
      handoverDate: formData.handoverDate,
      assetIds: selectedAssetRows.value.map((item) => item.assetId),
      toDeptId: formData.toDeptId,
      toUserId: formData.toUserId,
      locationName: formData.locationName?.trim() || undefined,
      remark: formData.remark?.trim() || undefined
    }
  }

  const goBack = () => {
    router.push('/asset/use')
  }

  const handleHandoverTypeChange = () => {
    clearSelection()
    formRef.value?.clearValidate()
  }

  // 中文注释：提交流程遵循“前端可解释校验 + 后端权威校验”，确保用户体验与业务严谨性同时成立。
  const handleSubmit = async () => {
    const selectionValidation = validateSelectedAssets()
    if (!selectionValidation.valid) {
      showWarning(selectionValidation.message)
      return
    }

    const valid = await formRef.value?.validate().catch(() => false)
    if (!valid) {
      return
    }

    submitLoading.value = true
    try {
      await addAssetHandoverOrder(buildPayload())
      ElMessage.success(`${handoverTypeLabel.value}交接提交成功`)
      router.push({ path: '/asset/use', query: { refresh: '1' } })
    } finally {
      submitLoading.value = false
    }
  }

  watch(
    () => route.query.type,
    (type) => {
      handoverType.value = normalizeHandoverType(String(type || 'ASSIGN'))
      clearSelection()
    },
    { immediate: true }
  )

  onMounted(async () => {
    await Promise.all([loadDeptTree(), handleResponsibleUserSearch()])
  })
</script>

<style scoped lang="scss">
  .asset-use-form-page {
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
  .form-card,
  .asset-card {
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

  .page-footer {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    padding: 2px 0 8px;
  }
</style>
