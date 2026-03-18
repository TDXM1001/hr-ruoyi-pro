<template>
  <div class="asset-ledger-form-page art-full-height flex flex-col gap-3 overflow-hidden p-3">
    <ElCard class="head-card" shadow="never">
      <div class="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
        <div class="flex flex-col gap-2">
          <ElButton link type="primary" icon="ri:arrow-left-line" @click="goBack"
            >返回资产台账</ElButton
          >
          <div>
            <div class="page-title">{{ pageTitle }}</div>
            <div class="page-desc">
              {{ pageDescription }}
            </div>
          </div>
        </div>

        <div class="meta-tags flex flex-wrap items-center gap-2">
          <ElTag type="success" effect="light">资产类型：固定资产（一期）</ElTag>
          <ElTag v-if="isEditMode" type="info" effect="light"
            >当前状态：{{ currentStatusLabel }}</ElTag
          >
        </div>
      </div>
    </ElCard>

    <ElCard class="form-card flex-1 min-h-0 overflow-hidden" shadow="never">
      <ElForm
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="110px"
        class="asset-form"
        v-loading="loading"
      >
        <div ref="formScrollRef" class="form-scroll-area">
          <ElAlert
            title="资产编号由后端统一生成，当前新增与编辑页面均不开放手工修改；如后续需要历史编号映射，应单独增加“外部编号”字段。"
            type="info"
            :closable="false"
            show-icon
            class="code-alert mb-4"
          />

          <section class="form-section">
            <header class="section-title">基础信息</header>
            <ElRow :gutter="16">
              <ElCol :xs="24" :md="12">
                <ElFormItem label="资产编号">
                  <div class="asset-code-row">
                    <ElInput
                      v-model="formData.assetCode"
                      maxlength="64"
                      readonly
                      :placeholder="assetCodePlaceholder"
                    />
                    <ElButton
                      v-if="!isEditMode"
                      plain
                      :loading="assetCodeLoading"
                      @click="handleRefreshAssetCode"
                    >
                      刷新建议编号
                    </ElButton>
                  </div>
                  <div class="field-tip">{{ assetCodeTip }}</div>
                </ElFormItem>
              </ElCol>
              <ElCol :xs="24" :md="12">
                <ElFormItem label="资产名称" prop="assetName">
                  <ElInput
                    v-model="formData.assetName"
                    maxlength="120"
                    placeholder="请输入资产名称"
                  />
                </ElFormItem>
              </ElCol>
            </ElRow>
            <ElRow :gutter="16">
              <ElCol :xs="24" :md="12">
                <ElFormItem label="资产类型">
                  <ElInput model-value="固定资产（一期）" readonly />
                </ElFormItem>
              </ElCol>
              <ElCol :xs="24" :md="12">
                <ElFormItem label="资产分类" prop="categoryId">
                  <ElTreeSelect
                    v-model="formData.categoryId"
                    :data="categoryOptions"
                    :props="treeSelectProps"
                    value-key="id"
                    check-strictly
                    filterable
                    clearable
                    class="w-full"
                    placeholder="请选择资产分类"
                    :render-after-expand="false"
                  />
                </ElFormItem>
              </ElCol>
            </ElRow>
            <ElRow :gutter="16">
              <ElCol :xs="24" :md="12">
                <ElFormItem label="录入来源" prop="sourceType">
                  <ElSelect
                    v-model="formData.sourceType"
                    clearable
                    class="w-full"
                    placeholder="请选择录入来源"
                  >
                    <ElOption
                      v-for="item in ast_asset_source_type"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </ElSelect>
                </ElFormItem>
              </ElCol>
              <ElCol :xs="24" :md="12">
                <ElFormItem label="取得方式" prop="acquireType">
                  <ElSelect
                    v-model="formData.acquireType"
                    clearable
                    class="w-full"
                    placeholder="请选择取得方式"
                  >
                    <ElOption
                      v-for="item in ast_asset_acquire_type"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </ElSelect>
                </ElFormItem>
              </ElCol>
            </ElRow>
          </section>

          <section class="form-section">
            <header class="section-title">归属与使用</header>
            <ElRow :gutter="16">
              <ElCol :xs="24" :md="12">
                <ElFormItem label="权属部门" prop="ownerDeptId">
                  <ElTreeSelect
                    v-model="formData.ownerDeptId"
                    :data="deptOptions"
                    :props="treeSelectProps"
                    value-key="id"
                    check-strictly
                    filterable
                    clearable
                    class="w-full"
                    placeholder="请选择权属部门"
                    :render-after-expand="false"
                  />
                </ElFormItem>
              </ElCol>
              <ElCol :xs="24" :md="12">
                <ElFormItem label="使用部门" prop="useDeptId">
                  <ElTreeSelect
                    v-model="formData.useDeptId"
                    :data="deptOptions"
                    :props="treeSelectProps"
                    value-key="id"
                    check-strictly
                    filterable
                    clearable
                    class="w-full"
                    placeholder="请选择使用部门"
                    :render-after-expand="false"
                  />
                </ElFormItem>
              </ElCol>
            </ElRow>

            <ElRow :gutter="16">
              <ElCol :xs="24" :md="12">
                <ElFormItem label="责任人" prop="responsibleUserId">
                  <ElSelect
                    v-model="formData.responsibleUserId"
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
                <ElFormItem label="资产位置" prop="locationName">
                  <ElInput
                    v-model="formData.locationName"
                    maxlength="200"
                    placeholder="请输入当前使用位置"
                  />
                </ElFormItem>
              </ElCol>
            </ElRow>
          </section>

          <section class="form-section">
            <header class="section-title">财务与周期</header>
            <ElRow :gutter="16">
              <ElCol :xs="24" :md="12">
                <ElFormItem label="资产原值（元）" prop="originalValue">
                  <ElInputNumber
                    v-model="formData.originalValue"
                    :min="0"
                    :precision="2"
                    :step="100"
                    controls-position="right"
                    class="w-full"
                  />
                </ElFormItem>
              </ElCol>
              <ElCol :xs="24" :md="12">
                <ElFormItem label="规格型号" prop="specModel">
                  <ElInput
                    v-model="formData.specModel"
                    maxlength="255"
                    placeholder="请输入规格型号"
                  />
                </ElFormItem>
              </ElCol>
            </ElRow>

            <ElRow :gutter="16">
              <ElCol :xs="24" :md="12">
                <ElFormItem label="序列号" prop="serialNo">
                  <ElInput v-model="formData.serialNo" maxlength="100" placeholder="请输入序列号" />
                </ElFormItem>
              </ElCol>
              <ElCol :xs="24" :md="12">
                <ElFormItem label="取得日期" prop="acquisitionDate">
                  <ElDatePicker
                    v-model="formData.acquisitionDate"
                    type="date"
                    value-format="YYYY-MM-DD"
                    class="w-full"
                    placeholder="请选择取得日期"
                  />
                </ElFormItem>
              </ElCol>
            </ElRow>

            <ElRow :gutter="16">
              <ElCol :xs="24" :md="12">
                <ElFormItem label="启用日期" prop="enableDate">
                  <ElDatePicker
                    v-model="formData.enableDate"
                    type="date"
                    value-format="YYYY-MM-DD"
                    class="w-full"
                    placeholder="请选择启用日期"
                  />
                </ElFormItem>
              </ElCol>
              <ElCol :xs="24" :md="12">
                <ElFormItem label="备注" prop="remark">
                  <ElInput
                    v-model="formData.remark"
                    type="textarea"
                    :rows="3"
                    maxlength="500"
                    show-word-limit
                    placeholder="请输入备注"
                  />
                </ElFormItem>
              </ElCol>
            </ElRow>
          </section>
        </div>

        <div class="form-footer">
          <div class="footer-hint">* 为必填项，请优先完善基础信息后再提交</div>
          <div class="footer-actions">
            <ElButton @click="goBack">取消</ElButton>
            <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">
              {{ isEditMode ? '保存修改' : '提交建账' }}
            </ElButton>
          </div>
        </div>
      </ElForm>
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import type { FormInstance, FormRules } from 'element-plus'
  import { ElMessage } from 'element-plus'
  import type { AssetTreeOption, AssetUserOption } from '@/api/asset/ledger'
  import {
    addAssetLedger,
    getAssetCategoryTree,
    getAssetDeptTree,
    getAssetLedger,
    getNextAssetCode,
    listAssetResponsibleUsers,
    updateAssetLedger
  } from '@/api/asset/ledger'
  import { useDict } from '@/utils/dict'

  defineOptions({ name: 'AssetLedgerFormPage' })

  const route = useRoute()
  const router = useRouter()
  route.meta.activePath = '/asset/ledger'

  const { ast_asset_status, ast_asset_source_type, ast_asset_acquire_type } = useDict(
    'ast_asset_status',
    'ast_asset_source_type',
    'ast_asset_acquire_type'
  )

  type DictOption = {
    value: string | number
    label: string
  }

  const formRef = ref<FormInstance>()
  const formScrollRef = ref<HTMLElement>()
  const loading = ref(false)
  const submitLoading = ref(false)
  const assetCodeLoading = ref(false)
  const responsibleUserLoading = ref(false)
  const categoryOptions = ref<AssetTreeOption[]>([])
  const deptOptions = ref<AssetTreeOption[]>([])
  const responsibleUserOptions = ref<AssetUserOption[]>([])

  const treeSelectProps = {
    value: 'id',
    label: 'label',
    children: 'children',
    disabled: 'disabled'
  }

  const assetId = computed(() => {
    const value = route.params.assetId
    return value ? Number(value) : undefined
  })

  const isEditMode = computed(() => Number.isFinite(assetId.value))

  const pageTitle = computed(() => (isEditMode.value ? '编辑资产台账' : '新增资产台账'))

  const pageDescription = computed(() => {
    return isEditMode.value
      ? '编辑页只维护台账主数据，资产编号由系统冻结保留，不通过通用编辑页修改编号。'
      : '新增页采用页面式建账，分类、部门、责任人均使用选择器录入，资产编号由后端统一提供。'
  })

  const assetCodePlaceholder = computed(() => {
    return isEditMode.value
      ? '系统保留原资产编号，不允许在编辑页修改'
      : '保存后由后端生成正式资产编号'
  })

  const assetCodeTip = computed(() => {
    return isEditMode.value
      ? '资产编号属于正式台账标识，编辑页只展示不修改；如需重编，应单独走编号调整流程。'
      : '当前展示的是后端提供的建议编号，提交建账时以后端正式生成结果为准。'
  })

  const initialFormData = {
    assetId: undefined as number | undefined,
    assetCode: '',
    assetName: '',
    assetType: 'FIXED',
    categoryId: undefined as number | undefined,
    assetStatus: 'IN_LEDGER',
    sourceType: 'MANUAL',
    acquireType: 'PURCHASE',
    ownerDeptId: undefined as number | undefined,
    useDeptId: undefined as number | undefined,
    responsibleUserId: undefined as number | undefined,
    locationName: '',
    originalValue: undefined as number | undefined,
    specModel: '',
    serialNo: '',
    acquisitionDate: '',
    enableDate: '',
    remark: ''
  }

  const formData = reactive({ ...initialFormData })

  const formRules: FormRules = {
    assetName: [{ required: true, message: '资产名称不能为空', trigger: 'blur' }],
    categoryId: [{ required: true, message: '请选择资产分类', trigger: 'change' }],
    sourceType: [{ required: true, message: '请选择录入来源', trigger: 'change' }],
    acquireType: [{ required: true, message: '请选择取得方式', trigger: 'change' }],
    ownerDeptId: [{ required: true, message: '请选择权属部门', trigger: 'change' }]
  }

  const currentStatusLabel = computed(() => {
    return (
      (ast_asset_status.value as DictOption[]).find((item) => item.value === formData.assetStatus)
        ?.label || '在册'
    )
  })

  const toObjectData = <T,>(response: any): T => {
    if (response?.data !== undefined) {
      return response.data
    }
    return response
  }

  const toArrayData = <T,>(response: any): T[] => {
    if (Array.isArray(response)) {
      return response
    }
    return response?.data || []
  }

  const normalizeText = (value?: string) => {
    return value?.trim() || ''
  }

  const resetFormScrollPosition = () => {
    nextTick(() => {
      formScrollRef.value?.scrollTo({ top: 0 })
      window.scrollTo({ top: 0, behavior: 'auto' })
    })
  }

  const loadCategoryTree = async () => {
    const response = await getAssetCategoryTree()
    categoryOptions.value = toArrayData<AssetTreeOption>(response)
  }

  const loadDeptTree = async () => {
    const response = await getAssetDeptTree()
    deptOptions.value = toArrayData<AssetTreeOption>(response)
  }

  const loadNextAssetCode = async () => {
    assetCodeLoading.value = true
    try {
      formData.assetCode = await getNextAssetCode()
    } finally {
      assetCodeLoading.value = false
    }
  }

  const handleRefreshAssetCode = async () => {
    await loadNextAssetCode()
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

  const loadDetail = async () => {
    if (!isEditMode.value || !assetId.value) {
      Object.assign(formData, initialFormData)
      return
    }

    loading.value = true
    try {
      const response = await getAssetLedger(assetId.value)
      Object.assign(formData, { ...initialFormData, ...toObjectData<any>(response) })
    } finally {
      loading.value = false
    }
  }

  const buildSubmitData = () => {
    return {
      assetId: formData.assetId,
      assetName: normalizeText(formData.assetName),
      assetType: 'FIXED',
      categoryId: formData.categoryId,
      sourceType: formData.sourceType,
      acquireType: formData.acquireType,
      ownerDeptId: formData.ownerDeptId,
      useDeptId: formData.useDeptId,
      responsibleUserId: formData.responsibleUserId,
      locationName: normalizeText(formData.locationName),
      originalValue: formData.originalValue,
      specModel: normalizeText(formData.specModel),
      serialNo: normalizeText(formData.serialNo),
      acquisitionDate: formData.acquisitionDate,
      enableDate: formData.enableDate,
      remark: normalizeText(formData.remark)
    }
  }

  const initializePage = async () => {
    await Promise.all([loadCategoryTree(), loadDeptTree(), handleResponsibleUserSearch()])

    if (isEditMode.value) {
      await loadDetail()
      resetFormScrollPosition()
      return
    }

    Object.assign(formData, initialFormData)
    await loadNextAssetCode()
    resetFormScrollPosition()
  }

  const goBack = () => {
    router.push('/asset/ledger')
  }

  const handleSubmit = async () => {
    const valid = await formRef.value?.validate().catch(() => false)
    if (!valid) {
      return
    }

    submitLoading.value = true
    try {
      const submitData = buildSubmitData()
      if (isEditMode.value && assetId.value) {
        await updateAssetLedger(submitData)
        ElMessage.success('资产台账修改成功')
        router.push(`/asset/ledger/detail/${assetId.value}`)
        return
      }

      const response = await addAssetLedger(submitData)
      const createdAssetId = Number(toObjectData<any>(response))
      ElMessage.success('资产台账新增成功')
      if (Number.isFinite(createdAssetId) && createdAssetId > 0) {
        router.push(`/asset/ledger/detail/${createdAssetId}`)
        return
      }
      goBack()
    } finally {
      submitLoading.value = false
    }
  }

  watch(
    () => route.fullPath,
    async () => {
      await initializePage()
    }
  )

  onMounted(async () => {
    await initializePage()
  })
</script>

<style scoped lang="scss">
  .asset-ledger-form-page {
    --asset-accent: #2f66ff;
    --asset-accent-soft: rgb(47 102 255 / 10%);
    --asset-border: #e6ebf5;
    --asset-panel-bg: #fff;
    --asset-panel-muted: #f7f9fc;
    --asset-text-main: #18233a;
    --asset-text-secondary: #5d6b86;

    background:
      radial-gradient(circle at 0% 0%, rgb(47 102 255 / 8%), transparent 34%),
      radial-gradient(circle at 100% 0%, rgb(32 201 151 / 8%), transparent 36%),
      var(--art-main-bg-color);

    .head-card {
      border: 1px solid var(--asset-border);
      border-radius: 12px;
      background: linear-gradient(120deg, #fff 0%, #f8fbff 100%);

      :deep(.el-card__body) {
        padding: 20px 24px;
      }
    }

    .page-title {
      font-size: 34px;
      font-weight: 700;
      color: var(--asset-text-main);
      line-height: 1.4;
      letter-spacing: 0.2px;
    }

    .page-desc {
      margin-top: 6px;
      font-size: 14px;
      color: var(--asset-text-secondary);
      line-height: 1.7;
      max-width: 760px;
    }

    .meta-tags {
      :deep(.el-tag) {
        border-radius: 999px;
        padding: 0 12px;
        height: 30px;
        font-weight: 500;
      }
    }

    .code-alert {
      :deep(.el-alert) {
        border-radius: 10px;
      }

      :deep(.el-alert__content) {
        padding-right: 2px;
      }

      :deep(.el-alert__title) {
        font-size: 13px;
        font-weight: 500;
        color: #425a7d;
        line-height: 1.6;
      }
    }

    .form-card {
      border: 1px solid var(--asset-border);
      border-radius: 12px;
      background: var(--asset-panel-bg);

      :deep(.el-card__body) {
        display: flex;
        flex-direction: column;
        height: 100%;
        padding: 0;
      }
    }

    .asset-form {
      height: 100%;
      min-height: 0;
      display: flex;
      flex-direction: column;
    }

    .form-scroll-area {
      flex: 1;
      min-height: 0;
      overflow-y: auto;
      padding: 24px 24px 16px;
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

    .form-section {
      margin-bottom: 16px;
      padding: 16px 16px 2px;
      border: 1px solid var(--asset-border);
      border-radius: 10px;
      background:
        linear-gradient(180deg, rgb(247 250 255 / 90%) 0%, #fff 42%), var(--asset-panel-bg);
    }

    .section-title {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 14px;
      font-size: 14px;
      font-weight: 600;
      color: var(--asset-text-main);
      letter-spacing: 0.2px;

      &::before {
        content: '';
        width: 4px;
        height: 14px;
        border-radius: 999px;
        background: var(--asset-accent);
      }
    }

    :deep(.el-form-item) {
      margin-bottom: 14px;
    }

    :deep(.el-form-item__label) {
      color: #34486a;
      font-weight: 500;
    }

    :deep(.el-input__wrapper),
    :deep(.el-textarea__inner),
    :deep(.el-select__wrapper),
    :deep(.el-date-editor.el-input__wrapper) {
      border-radius: 8px;
      transition:
        box-shadow 0.2s ease,
        border-color 0.2s ease,
        background-color 0.2s ease;
    }

    :deep(.el-input__wrapper),
    :deep(.el-select__wrapper),
    :deep(.el-date-editor.el-input__wrapper),
    :deep(.el-textarea__inner) {
      box-shadow: 0 0 0 1px #d9e2f2 inset;
      background-color: #fff;
    }

    :deep(.el-input__wrapper.is-focus),
    :deep(.el-select__wrapper.is-focused),
    :deep(.el-date-editor.el-input__wrapper.is-focus),
    :deep(.el-textarea__inner:focus) {
      box-shadow:
        0 0 0 1px var(--asset-accent) inset,
        0 0 0 3px var(--asset-accent-soft);
    }

    :deep(.el-input.is-disabled .el-input__wrapper) {
      background-color: var(--asset-panel-muted);
    }

    :deep(.el-textarea__inner) {
      min-height: 92px;
      resize: vertical;
    }

    .asset-code-row {
      display: grid;
      grid-template-columns: minmax(0, 1fr) auto;
      gap: 12px;
      width: 100%;
    }

    .field-tip {
      margin-top: 6px;
      font-size: 12px;
      line-height: 1.6;
      color: var(--art-text-gray-500);
    }

    .form-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 10px 14px;
      flex-shrink: 0;
      padding: 14px 24px 16px;
      border-top: 1px solid var(--asset-border);
      background: linear-gradient(180deg, rgb(255 255 255 / 85%) 0%, #f9fbff 100%);
      backdrop-filter: blur(2px);
    }

    .footer-hint {
      font-size: 12px;
      line-height: 1.6;
      color: #6f7f98;
    }

    .footer-actions {
      display: flex;
      align-items: center;
      gap: 12px;
      flex-shrink: 0;

      :deep(.el-button) {
        min-width: 98px;
      }
    }
  }

  @media (max-width: 1200px) {
    .asset-ledger-form-page {
      .page-title {
        font-size: 28px;
      }
    }
  }

  @media (max-width: 992px) {
    .asset-ledger-form-page {
      .head-card {
        :deep(.el-card__body) {
          padding: 16px 18px;
        }
      }

      .form-scroll-area {
        padding: 16px 18px 12px;
      }

      .form-footer {
        padding: 12px 18px 14px;
      }

      .asset-code-row {
        grid-template-columns: minmax(0, 1fr);
      }

      .footer-actions {
        width: 100%;
        justify-content: flex-end;
      }
    }
  }

  @media (max-width: 768px) {
    .asset-ledger-form-page {
      padding: 8px;
      gap: 8px;

      .page-title {
        font-size: 24px;
      }

      .page-desc {
        font-size: 13px;
      }

      .form-section {
        padding: 14px 12px 0;
        margin-bottom: 12px;
      }

      .section-title {
        margin-bottom: 10px;
      }

      .form-footer {
        flex-direction: column;
        align-items: stretch;
      }

      .footer-hint {
        order: 2;
      }

      .footer-actions {
        width: 100%;
        justify-content: flex-end;
      }
    }
  }
</style>
