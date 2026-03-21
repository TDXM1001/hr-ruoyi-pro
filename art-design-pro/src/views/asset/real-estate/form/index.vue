<template>
  <div class="asset-real-estate-form-page art-full-height flex flex-col gap-3 overflow-hidden p-3">
    <ElCard class="head-card" shadow="never">
      <div class="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
        <div class="flex flex-col gap-2">
          <ElButton link type="primary" icon="ri:arrow-left-line" @click="goBack">返回不动产档案</ElButton>
          <div>
            <div class="page-title">{{ pageTitle }}</div>
            <div class="page-desc">{{ pageDescription }}</div>
          </div>
        </div>

        <div class="meta-tags flex flex-wrap items-center gap-2">
          <ElTag type="success" effect="light">资产类型：不动产</ElTag>
          <ElTag v-if="isEditMode" type="info" effect="light">当前状态：{{ currentStatusLabel }}</ElTag>
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
            title="不动产编码由后端统一生成并冻结，页面只展示建议编码；编辑页不允许直接修改正式编码。"
            type="info"
            :closable="false"
            show-icon
            class="code-alert mb-4"
          />

          <section class="form-section">
            <header class="section-title">基础信息</header>
            <ElRow :gutter="16">
              <ElCol :xs="24" :md="12">
                <ElFormItem label="资产编码">
                  <div class="asset-code-row">
                    <ElInput v-model="formData.assetCode" maxlength="64" readonly :placeholder="assetCodePlaceholder" />
                    <ElButton v-if="!isEditMode" plain :loading="assetCodeLoading" @click="handleRefreshAssetCode">
                      刷新建议编码
                    </ElButton>
                  </div>
                  <div class="field-tip">{{ assetCodeTip }}</div>
                </ElFormItem>
              </ElCol>
              <ElCol :xs="24" :md="12">
                <ElFormItem label="资产名称" prop="assetName">
                  <ElInput v-model="formData.assetName" maxlength="120" placeholder="请输入资产名称" />
                </ElFormItem>
              </ElCol>
            </ElRow>
            <ElRow :gutter="16">
              <ElCol :xs="24" :md="12">
                <ElFormItem label="资产类型">
                  <ElInput model-value="不动产" readonly />
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
                    placeholder="请选择不动产分类"
                    :render-after-expand="false"
                  />
                </ElFormItem>
              </ElCol>
            </ElRow>
            <ElRow :gutter="16">
              <ElCol :xs="24" :md="12">
                <ElFormItem label="录入来源" prop="sourceType">
                  <ElSelect v-model="formData.sourceType" clearable class="w-full" placeholder="请选择录入来源">
                    <ElOption v-for="item in ast_asset_source_type" :key="item.value" :label="item.label" :value="item.value" />
                  </ElSelect>
                </ElFormItem>
              </ElCol>
              <ElCol :xs="24" :md="12">
                <ElFormItem label="取得方式" prop="acquireType">
                  <ElSelect v-model="formData.acquireType" clearable class="w-full" placeholder="请选择取得方式">
                    <ElOption v-for="item in ast_asset_acquire_type" :key="item.value" :label="item.label" :value="item.value" />
                  </ElSelect>
                </ElFormItem>
              </ElCol>
            </ElRow>
          </section>

          <section class="form-section">
            <header class="section-title">权属信息</header>
            <ElRow :gutter="16">
              <ElCol :xs="24" :md="12">
                <ElFormItem label="权属证号" prop="ownershipCertNo">
                  <ElInput v-model="formData.ownershipCertNo" maxlength="64" placeholder="请输入权属证号" />
                </ElFormItem>
              </ElCol>
              <ElCol :xs="24" :md="12">
                <ElFormItem label="土地用途" prop="landUseType">
                  <ElInput v-model="formData.landUseType" maxlength="64" placeholder="请输入土地用途" />
                </ElFormItem>
              </ElCol>
            </ElRow>
            <ElRow :gutter="16">
              <ElCol :xs="24" :md="12">
                <ElFormItem label="建筑面积(㎡)" prop="buildingArea">
                  <ElInputNumber
                    v-model="formData.buildingArea"
                    :min="0"
                    :precision="2"
                    :step="10"
                    controls-position="right"
                    class="w-full"
                  />
                </ElFormItem>
              </ElCol>
              <ElCol :xs="24" :md="12">
                <ElFormItem label="资产位置" prop="locationName">
                  <ElInput v-model="formData.locationName" maxlength="200" placeholder="请输入资产位置" />
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
                    <ElOption v-for="item in responsibleUserOptions" :key="item.value" :label="item.label" :value="item.value" />
                  </ElSelect>
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

          <section class="form-section">
            <header class="section-title">财务与日期</header>
            <ElRow :gutter="16">
              <ElCol :xs="24" :md="12">
                <ElFormItem label="资产原值(元)" prop="originalValue">
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
                  <ElInput v-model="formData.specModel" maxlength="255" placeholder="请输入规格型号" />
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
            </ElRow>
          </section>
        </div>

        <div class="form-footer">
          <div class="footer-hint">* 必填项请优先补齐，不动产编码由系统统一生成</div>
          <div class="footer-actions">
            <ElButton @click="goBack">取消</ElButton>
            <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">
              {{ isEditMode ? '保存修改' : '提交建档' }}
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
  import type { AssetTreeOption, AssetUserOption } from '@/api/asset/real-estate'
  import {
    addRealEstate,
    getNextRealEstateCode,
    getRealEstateCategoryTree,
    getRealEstateDeptTree,
    getRealEstateDetail,
    listRealEstateResponsibleUsers,
    updateRealEstate
  } from '@/api/asset/real-estate'
  import { useDict } from '@/utils/dict'

  defineOptions({ name: 'AssetRealEstateFormPage' })

  const route = useRoute()
  const router = useRouter()
  route.meta.activePath = '/asset/real-estate'

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

  const pageTitle = computed(() => (isEditMode.value ? '编辑不动产档案' : '新增不动产档案'))

  const pageDescription = computed(() => {
    return isEditMode.value
      ? '编辑页用于维护不动产台账主数据与权属档案，正式编码由系统保留，不允许在表单里直接改写。'
      : '新增页采用页面式建档，权属信息、归属部门和责任人统一在一个表单里补齐。'
  })

  const assetCodePlaceholder = computed(() => {
    return isEditMode.value ? '系统保留原资产编码，不允许在编辑页修改' : '保存后以后端正式生成结果为准'
  })

  const assetCodeTip = computed(() => {
    return isEditMode.value
      ? '资产编码属于正式台账标识，如需重编应走专门流程，而不是复用编辑页。'
      : '当前展示的是后端提供的建议编码，提交建档后以后端正式编码为准。'
  })

  const initialFormData = {
    assetId: undefined as number | undefined,
    assetCode: '',
    assetType: 'REAL_ESTATE',
    assetName: '',
    categoryId: undefined as number | undefined,
    assetStatus: 'IN_USE',
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
    ownershipCertNo: '',
    landUseType: '',
    buildingArea: undefined as number | undefined,
    remark: ''
  }

  const formData = reactive({ ...initialFormData })

  const formRules: FormRules = {
    assetName: [{ required: true, message: '资产名称不能为空', trigger: 'blur' }],
    categoryId: [{ required: true, message: '请选择资产分类', trigger: 'change' }],
    sourceType: [{ required: true, message: '请选择录入来源', trigger: 'change' }],
    ownerDeptId: [{ required: true, message: '请选择权属部门', trigger: 'change' }],
    ownershipCertNo: [{ required: true, message: '请输入权属证号', trigger: 'blur' }],
    landUseType: [{ required: true, message: '请输入土地用途', trigger: 'blur' }],
    buildingArea: [{ required: true, message: '请输入建筑面积', trigger: 'change' }]
  }

  const currentStatusLabel = computed(() => {
    return (
      (ast_asset_status.value as DictOption[]).find((item) => item.value === formData.assetStatus)?.label ||
      '使用中'
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
      formScrollRef.value?.scrollTo?.({ top: 0 })
      const isJsdom = typeof navigator !== 'undefined' && /jsdom/i.test(navigator.userAgent)
      if (!isJsdom && typeof window !== 'undefined' && typeof window.scrollTo === 'function') {
        window.scrollTo({ top: 0, behavior: 'auto' })
      }
    })
  }

  const loadCategoryTree = async () => {
    const response = await getRealEstateCategoryTree()
    categoryOptions.value = toArrayData<AssetTreeOption>(response)
  }

  const loadDeptTree = async () => {
    const response = await getRealEstateDeptTree()
    deptOptions.value = toArrayData<AssetTreeOption>(response)
  }

  const loadNextAssetCode = async () => {
    assetCodeLoading.value = true
    try {
      formData.assetCode = await getNextRealEstateCode()
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
      const response = await listRealEstateResponsibleUsers({ keyword: keyword.trim() })
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
      const response = await getRealEstateDetail(assetId.value)
      Object.assign(formData, { ...initialFormData, ...toObjectData<any>(response) })
    } finally {
      loading.value = false
    }
  }

  const buildSubmitData = () => {
    return {
      assetId: formData.assetId,
      assetType: formData.assetType,
      assetName: normalizeText(formData.assetName),
      categoryId: formData.categoryId,
      assetStatus: formData.assetStatus,
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
      ownershipCertNo: normalizeText(formData.ownershipCertNo),
      landUseType: normalizeText(formData.landUseType),
      buildingArea: formData.buildingArea,
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
    router.push('/asset/real-estate')
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
        await updateRealEstate(submitData)
        ElMessage.success('不动产档案修改成功')
        router.push(`/asset/real-estate/detail/${assetId.value}`)
        return
      }

      const response = await addRealEstate(submitData)
      const createdAssetId = Number(toObjectData<any>(response))
      ElMessage.success('不动产档案新增成功')
      if (Number.isFinite(createdAssetId) && createdAssetId > 0) {
        router.push(`/asset/real-estate/detail/${createdAssetId}`)
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
  .asset-real-estate-form-page {
    --asset-accent: #1f7a8c;
    --asset-border: #e6ebf5;
    --asset-panel-bg: #fff;
    --asset-text-main: #18233a;
    --asset-text-secondary: #5d6b86;

    background:
      radial-gradient(circle at 0% 0%, rgb(31 122 140 / 8%), transparent 34%),
      radial-gradient(circle at 100% 0%, rgb(26 188 156 / 8%), transparent 36%),
      var(--art-main-bg-color);

    .head-card {
      border: 1px solid var(--asset-border);
      border-radius: 12px;
      background: linear-gradient(120deg, #fff 0%, #f7fcfd 100%);

      :deep(.el-card__body) {
        padding: 20px 24px;
      }
    }

    .page-title {
      font-size: 34px;
      font-weight: 700;
      color: var(--asset-text-main);
    }

    .page-desc {
      margin-top: 6px;
      font-size: 14px;
      color: var(--asset-text-secondary);
      line-height: 1.7;
      max-width: 760px;
    }

    .form-card {
      border: 1px solid var(--asset-border);
      border-radius: 12px;

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
    }

    .form-section {
      margin-bottom: 16px;
      padding: 16px 16px 2px;
      border: 1px solid var(--asset-border);
      border-radius: 10px;
      background: linear-gradient(180deg, rgb(247 250 255 / 90%) 0%, #fff 42%), #fff;
    }

    .section-title {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 14px;
      font-size: 14px;
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

      :deep(.el-button) {
        min-width: 98px;
      }
    }
  }
</style>
