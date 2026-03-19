<template>
  <div class="asset-use-form-page art-full-height p-3">
    <ElCard class="main-card art-full-height" shadow="never">
      <div class="workflow-shell">
        <header class="page-hero">
          <div class="hero-top">
            <ElButton link type="primary" @click="goBack">返回资产使用</ElButton>
            <div class="hero-tags">
              <ElTag type="success" effect="light">动作：{{ handoverTypeLabel }}</ElTag>
              <ElTag type="warning" effect="light">可选状态：{{ allowedStatusLabelText }}</ElTag>
            </div>
          </div>

          <div class="page-title">发起资产交接单</div>
          <div class="page-desc">
            交接单用于承接领用、调拨、退还流程。页面只负责录入交接信息与勾选资产，提交后统一回写台账并保留交接痕迹。
          </div>

          <ElAlert
            title="当前页面仅支持固定资产批量交接；同一交接单内资产类型必须一致，且不允许跳过交接单直接修改台账使用信息。"
            type="info"
            :closable="false"
            show-icon
            class="rule-alert"
          />
        </header>

        <div class="workflow-scroll-area">
          <section class="workflow-section">
            <div class="section-header">
              <div class="section-title">
                <span class="section-index">01</span>
                <span>交接信息</span>
              </div>
              <ElTag type="info" effect="light">必填完成：{{ requiredFieldProgress }}</ElTag>
            </div>

            <ElForm ref="formRef" :model="formData" :rules="formRules" label-width="110px">
              <ElRow :gutter="16">
                <ElCol :xs="24" :lg="24">
                  <ElFormItem label="交接动作" prop="handoverType">
                    <ElRadioGroup v-model="handoverType" @change="handleHandoverTypeChange">
                      <ElRadioButton label="ASSIGN">领用</ElRadioButton>
                      <ElRadioButton label="TRANSFER">调拨</ElRadioButton>
                      <ElRadioButton label="RETURN">退还</ElRadioButton>
                    </ElRadioGroup>
                  </ElFormItem>
                </ElCol>
              </ElRow>

              <ElRow :gutter="16">
                <ElCol :xs="24" :md="12">
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
                <ElCol :xs="24" :md="12">
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
                <ElCol :xs="24" :md="12">
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
                <ElCol :xs="24" :md="12">
                  <ElFormItem label="目标位置" prop="locationName">
                    <ElInput
                      v-model="formData.locationName"
                      maxlength="200"
                      clearable
                      placeholder="请输入交接后使用位置，不填则沿用原位置"
                    />
                  </ElFormItem>
                </ElCol>
              </ElRow>

              <ElRow :gutter="16">
                <ElCol :xs="24">
                  <ElFormItem label="备注" prop="remark">
                    <ElInput
                      v-model="formData.remark"
                      type="textarea"
                      :rows="3"
                      maxlength="500"
                      show-word-limit
                      placeholder="请输入交接说明（可选）"
                    />
                  </ElFormItem>
                </ElCol>
              </ElRow>
            </ElForm>
          </section>

          <section class="workflow-section asset-section">
            <div class="section-header">
              <div class="section-title">
                <span class="section-index">02</span>
                <span>选择交接资产</span>
              </div>
              <div class="section-actions">
                <ElTag type="info" effect="light">{{ selectedAssetSummary }}</ElTag>
                <ElButton plain :disabled="!selectedAssetRows.length" @click="clearSelection">
                  清空勾选
                </ElButton>
              </div>
            </div>

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
              :height="assetTableHeight"
              @selection-change="handleSelectionChange"
              @pagination:size-change="handleAssetSizeChange"
              @pagination:current-change="handleAssetCurrentChange"
            />
          </section>
        </div>

        <footer class="workflow-footer">
          <div class="footer-hint">
            当前交接将以“{{
              handoverTypeLabel
            }}”方式提交。系统会校验资产状态、目标责任信息，并在提交成功后自动回写台账。
          </div>
          <div class="footer-actions">
            <ElButton @click="goBack">取消</ElButton>
            <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">
              提交交接单
            </ElButton>
          </div>
        </footer>
      </div>
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import type { FormInstance, FormRules } from 'element-plus'
  import { ElMessage } from 'element-plus'
  import { useWindowSize } from '@vueuse/core'
  import type { AssetTreeOption, AssetUserOption } from '@/api/asset/ledger'
  import { listAssetLedger, getAssetDeptTree, listAssetResponsibleUsers } from '@/api/asset/ledger'
  import { addAssetHandoverOrder } from '@/api/asset/handover'
  import DictTag from '@/components/DictTag/index.vue'
  import { useTable } from '@/hooks/core/useTable'
  import { useDict } from '@/utils/dict'

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
  route.meta.activePath = '/asset/use'

  const { height: windowHeight } = useWindowSize()

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

  const selectedAssetSummary = computed(() => {
    return `已选 ${selectedAssetRows.value.length} 宗`
  })

  const deptPlaceholder = computed(() => {
    return handoverType.value === 'RETURN' ? '可选：不填则默认退还到权属部门' : '请选择目标部门'
  })

  const hasValue = (value: unknown) => {
    if (value === undefined || value === null) {
      return false
    }
    if (typeof value === 'string') {
      return value.trim().length > 0
    }
    return true
  }

  // 中文注释：卡片内表格使用独立高度兜底，避免页面高度变化时出现表格区域被裁切。
  const assetTableHeight = computed(() => {
    return Math.min(520, Math.max(300, windowHeight.value - 540))
  })

  const requiredFieldProgress = computed(() => {
    const requiredKeys =
      handoverType.value === 'RETURN' ? ['handoverDate'] : ['handoverDate', 'toDeptId', 'toUserId']
    const completedCount = requiredKeys.filter((key) =>
      hasValue(formData[key as keyof typeof formData])
    ).length
    return `${completedCount}/${requiredKeys.length}`
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
    --asset-accent-soft: rgb(47 102 255 / 10%);
    --asset-border: #e6ebf5;
    --asset-panel-bg: #fff;
    --asset-panel-subtle: #f7faff;
    --asset-text-main: #18233a;
    --asset-text-secondary: #5d6b86;

    background:
      radial-gradient(circle at 0% 0%, rgb(47 102 255 / 8%), transparent 34%),
      radial-gradient(circle at 100% 0%, rgb(32 201 151 / 8%), transparent 36%),
      var(--art-main-bg-color);
  }

  .main-card {
    border: 1px solid var(--asset-border);
    border-radius: 18px;
    background: var(--asset-panel-bg);
    box-shadow: 0 18px 40px rgb(26 39 68 / 6%);

    :deep(.el-card__body) {
      height: 100%;
      padding: 0;
    }
  }

  .workflow-shell {
    display: flex;
    flex-direction: column;
    height: 100%;
    min-height: 0;
  }

  .page-hero {
    padding: 22px 24px 18px;
    border-bottom: 1px solid var(--asset-border);
    background:
      linear-gradient(135deg, rgb(47 102 255 / 6%) 0%, transparent 32%),
      linear-gradient(180deg, #fff 0%, #fbfdff 100%);
  }

  .hero-top {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 12px;
    flex-wrap: wrap;
  }

  .hero-tags {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-wrap: wrap;
  }

  .page-title {
    margin-top: 8px;
    font-size: 30px;
    font-weight: 700;
    line-height: 1.35;
    color: var(--asset-text-main);
  }

  .page-desc {
    margin-top: 8px;
    max-width: 920px;
    font-size: 14px;
    line-height: 1.7;
    color: var(--asset-text-secondary);
  }

  .rule-alert {
    margin-top: 16px;
  }

  .workflow-scroll-area {
    flex: 1;
    min-height: 0;
    overflow-y: auto;
    padding: 24px;
    scroll-behavior: smooth;

    &::-webkit-scrollbar {
      width: 8px;
    }

    &::-webkit-scrollbar-thumb {
      border-radius: 999px;
      background: rgb(124 143 181 / 35%);
    }

    &::-webkit-scrollbar-track {
      background: transparent;
    }
  }

  .workflow-section {
    margin-bottom: 18px;
    padding: 20px 20px 14px;
    border: 1px solid var(--asset-border);
    border-radius: 16px;
    background: linear-gradient(180deg, rgb(247 250 255 / 90%) 0%, #fff 40%), var(--asset-panel-bg);
  }

  .workflow-section:last-child {
    margin-bottom: 0;
  }

  .section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    flex-wrap: wrap;
    margin-bottom: 18px;
  }

  .section-title {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 16px;
    font-weight: 600;
    color: var(--asset-text-main);
  }

  .section-index {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 30px;
    height: 30px;
    border-radius: 999px;
    background: var(--asset-accent-soft);
    color: var(--asset-accent);
    font-size: 13px;
    font-weight: 700;
  }

  .section-actions {
    display: flex;
    align-items: center;
    gap: 10px;
    flex-wrap: wrap;
  }

  .asset-section {
    padding-bottom: 18px;
  }

  .workflow-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px 16px;
    flex-shrink: 0;
    padding: 16px 24px 18px;
    border-top: 1px solid var(--asset-border);
    background: linear-gradient(180deg, rgb(255 255 255 / 96%) 0%, #f8fbff 100%);
  }

  .footer-hint {
    font-size: 13px;
    line-height: 1.7;
    color: var(--asset-text-secondary);
  }

  .footer-actions {
    display: flex;
    align-items: center;
    gap: 10px;
    flex-shrink: 0;

    :deep(.el-button) {
      min-width: 108px;
    }
  }

  :deep(.el-form-item) {
    margin-bottom: 16px;
  }

  :deep(.el-form-item__label) {
    color: #34486a;
    font-weight: 500;
  }

  :deep(.el-input__wrapper),
  :deep(.el-select__wrapper),
  :deep(.el-date-editor.el-input__wrapper),
  :deep(.el-textarea__inner) {
    border-radius: 10px;
    box-shadow: 0 0 0 1px #d9e2f2 inset;
    background-color: #fff;
  }

  :deep(.el-input__wrapper.is-focus),
  :deep(.el-select__wrapper.is-focused),
  :deep(.el-date-editor.el-input__wrapper.is-focus),
  :deep(.el-textarea__inner:focus) {
    box-shadow:
      0 0 0 1px var(--asset-accent) inset,
      0 0 0 3px rgb(47 102 255 / 12%);
  }

  :deep(.el-radio-group) {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
  }

  @media (max-width: 992px) {
    .page-hero,
    .workflow-scroll-area,
    .workflow-footer {
      padding-left: 18px;
      padding-right: 18px;
    }

    .workflow-section {
      padding: 16px 16px 10px;
    }
  }

  @media (max-width: 768px) {
    .asset-use-form-page {
      padding: 8px;
    }

    .page-title {
      font-size: 24px;
    }

    .page-desc,
    .footer-hint {
      font-size: 13px;
    }

    .workflow-scroll-area {
      padding-top: 16px;
      padding-bottom: 16px;
    }

    .workflow-footer {
      flex-direction: column;
      align-items: stretch;
    }

    .footer-actions {
      justify-content: flex-end;
    }
  }
</style>
