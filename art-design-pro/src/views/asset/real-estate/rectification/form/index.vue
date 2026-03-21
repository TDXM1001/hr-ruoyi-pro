<template>
  <div class="asset-real-estate-rectification-form p-3" data-testid="rectification-processing-page">
    <ElCard class="hero-card" shadow="never">
      <div class="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
        <div class="flex flex-col gap-2">
          <ElButton link type="primary" icon="ri:arrow-left-line" @click="goBack">返回整改页签</ElButton>
          <div>
            <div class="page-title">{{ pageTitle }}</div>
            <div class="page-desc">整改页负责登记、更新和只读回看，不在这里提前塞入审批流，保持处理链路清晰。</div>
          </div>
        </div>

        <div class="header-tags flex flex-wrap gap-2">
          <ElTag type="success" effect="light">{{ detailData.assetCode || '-' }}</ElTag>
          <ElTag effect="light">{{ taskMeta.taskNo || formData.taskId || '未绑定任务' }}</ElTag>
        </div>
      </div>
    </ElCard>

    <div class="processing-layout" data-testid="rectification-processing-layout">
      <div class="processing-main" data-testid="rectification-processing-main">
        <ElCard class="section-card" shadow="never" v-loading="loading">
          <template #header>
            <div class="card-title">{{ isCompletedRectification ? '整改信息' : '整改登记' }}</div>
          </template>

          <div class="section-body">
            <ElAlert
              class="mb-4"
              :type="isCompletedRectification ? 'success' : 'info'"
              :closable="false"
              :title="
                isCompletedRectification
                  ? '该整改单已完成，当前页面仅供查看。'
                  : '先维护问题、责任和期限，再由独立完成页执行整改收口。'
              "
            />

            <ElForm ref="formRef" :model="formData" :rules="rules" label-width="110px">
              <ElFormItem label="问题类型" prop="issueType">
                <ElInput
                  v-model="formData.issueType"
                  maxlength="64"
                  placeholder="请输入问题类型"
                  :readonly="isCompletedRectification"
                />
              </ElFormItem>

              <ElFormItem label="问题描述" prop="issueDesc">
                <ElInput
                  v-model="formData.issueDesc"
                  type="textarea"
                  :rows="5"
                  maxlength="500"
                  show-word-limit
                  placeholder="请输入整改问题描述"
                  :readonly="isCompletedRectification"
                />
              </ElFormItem>

              <div class="form-grid">
                <ElFormItem label="责任部门" prop="responsibleDeptId">
                  <ElTreeSelect
                    v-model="formData.responsibleDeptId"
                    :data="deptTreeOptions"
                    :props="treeSelectProps"
                    value-key="id"
                    check-strictly
                    filterable
                    default-expand-all
                    node-key="id"
                    clearable
                    class="w-full"
                    placeholder="请选择责任部门"
                    :render-after-expand="false"
                    :disabled="isCompletedRectification"
                  />
                </ElFormItem>

                <ElFormItem label="责任人" prop="responsibleUserId">
                  <ElSelect
                    v-model="formData.responsibleUserId"
                    filterable
                    remote
                    clearable
                    :remote-method="loadResponsibleUsers"
                    :loading="userOptionsLoading"
                    placeholder="请输入责任人关键字"
                    :disabled="isCompletedRectification"
                  >
                    <ElOption
                      v-for="item in responsibleUserOptions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </ElSelect>
                </ElFormItem>
              </div>

              <div class="form-grid">
                <ElFormItem label="整改期限" prop="deadlineDate">
                  <ElDatePicker
                    v-model="formData.deadlineDate"
                    type="date"
                    value-format="YYYY-MM-DD"
                    class="w-full"
                    placeholder="请选择整改期限"
                    :disabled="isCompletedRectification"
                  />
                </ElFormItem>

                <ElFormItem label="备注">
                  <ElInput
                    v-model="formData.remark"
                    type="textarea"
                    :rows="3"
                    maxlength="500"
                    show-word-limit
                    placeholder="请输入备注"
                    :readonly="isCompletedRectification"
                  />
                </ElFormItem>
              </div>
            </ElForm>
          </div>

          <div class="action-bar">
            <ElButton @click="goBack">{{ isCompletedRectification ? '返回整改页签' : '取消' }}</ElButton>
            <ElButton
              v-if="!isCompletedRectification"
              type="primary"
              :loading="submitting"
              @click="handleSubmit"
            >
              保存整改单
            </ElButton>
          </div>
        </ElCard>
      </div>

      <div class="processing-side" data-testid="rectification-processing-side">
        <ElCard class="section-card" shadow="never">
          <template #header>
            <div class="card-title">整改上下文</div>
          </template>

          <ElDescriptions class="context-descriptions" :column="1" border>
            <ElDescriptionsItem label="资产编码">{{ detailData.assetCode || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="资产名称">{{ detailData.assetName || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="任务编号">{{ taskMeta.taskNo || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="任务名称">{{ taskMeta.taskName || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="问题类型">{{ formData.issueType || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="结果说明">{{ sourceResultDesc || '-' }}</ElDescriptionsItem>
          </ElDescriptions>
        </ElCard>

        <ElCard class="section-card" shadow="never">
          <template #header>
            <div class="card-title">处理提示</div>
          </template>

          <div class="guide-panel">
            <div class="guide-panel__headline">
              {{ isCompletedRectification ? '当前整改单已收口，页面只做回看。' : '先补齐责任与期限，再推进整改。' }}
            </div>
            <div class="guide-panel__line">整改登记页只维护基础信息，不在这里执行完成动作。</div>
            <div class="guide-panel__line">保存后会回到详情壳整改页签，便于继续处理其他记录。</div>
            <div class="guide-panel__line">如果整改单已完成，基础字段全部切换为只读，避免重复编辑。</div>
          </div>
        </ElCard>

        <ElCard v-if="showCompletionSection" class="section-card" shadow="never">
          <template #header>
            <div class="card-title">整改完成信息</div>
          </template>

          <ElDescriptions class="completion-descriptions" :column="1" border>
            <ElDescriptionsItem label="完成时间">{{ completionInfo.completedTime || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="整改状态">{{ completionStatusLabel }}</ElDescriptionsItem>
            <ElDescriptionsItem label="关闭方式">整改完成收口</ElDescriptionsItem>
            <ElDescriptionsItem label="完成说明">
              {{ completionInfo.completionDesc || '-' }}
            </ElDescriptionsItem>
            <ElDescriptionsItem label="验收备注">
              {{ completionInfo.acceptanceRemark || '-' }}
            </ElDescriptionsItem>
          </ElDescriptions>
        </ElCard>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
  import type { FormInstance, FormRules } from 'element-plus'
  import { ElMessage } from 'element-plus'
  import { getInventoryTask, listInventoryTaskAssets } from '@/api/asset/inventory'
  import {
    addRealEstateRectification,
    getRealEstateDetail,
    getRealEstateDeptTree,
    getRealEstateRectification,
    listRealEstateResponsibleUsers,
    updateRealEstateRectification
  } from '@/api/asset/real-estate'
  import { persistRealEstateDetailTab } from '../../detail/tab-state'

  defineOptions({ name: 'AssetRealEstateRectificationFormPage' })

  type TaskAssetRecord = {
    itemId?: number
    taskId?: number
    inventoryResult?: string
    resultDesc?: string
    followUpBizId?: number
  }

  const route = useRoute()
  const router = useRouter()
  route.meta.activePath = '/asset/real-estate'

  const formRef = ref<FormInstance>()
  const loading = ref(false)
  const submitting = ref(false)
  const userOptionsLoading = ref(false)
  const detailData = reactive<Record<string, any>>({})
  const taskMeta = reactive<Record<string, any>>({})
  const deptTreeOptions = ref<any[]>([])
  const responsibleUserOptions = ref<Array<{ value: number; label: string }>>([])
  const sourceResultDesc = ref('')
  const completionInfo = reactive({
    completedTime: '',
    completionDesc: '',
    acceptanceRemark: ''
  })

  // 中文注释：树选择器统一复用资产域的节点协议，避免不同页面对同一部门树结构解释不一致。
  const treeSelectProps = {
    value: 'id',
    label: 'label',
    children: 'children',
    disabled: 'disabled'
  }

  const formData = reactive({
    rectificationId: undefined as number | undefined,
    taskId: undefined as number | undefined,
    inventoryItemId: undefined as number | undefined,
    rectificationStatus: 'PENDING',
    issueType: '',
    issueDesc: '',
    responsibleDeptId: undefined as number | undefined,
    responsibleUserId: undefined as number | undefined,
    deadlineDate: '',
    remark: ''
  })

  const assetId = computed(() => Number(route.params.assetId))
  const rectificationId = computed(() => {
    const value = route.params.rectificationId
    return value ? Number(value) : undefined
  })
  const createTaskId = computed(() => {
    const value = route.query.taskId
    return value ? Number(value) : undefined
  })
  const isEditMode = computed(() => !!rectificationId.value)
  const isCompletedRectification = computed(() => {
    return isEditMode.value && String(formData.rectificationStatus || '').toUpperCase() === 'COMPLETED'
  })
  const pageTitle = computed(() => {
    if (!isEditMode.value) {
      return '新增整改单'
    }
    return isCompletedRectification.value ? '查看整改单' : '编辑整改单'
  })
  const showCompletionSection = computed(() => isCompletedRectification.value)
  const completionStatusLabel = computed(() => {
    const mapper: Record<string, string> = {
      PENDING: '待整改',
      COMPLETED: '已完成'
    }
    return mapper[String(formData.rectificationStatus || '').toUpperCase()] || formData.rectificationStatus || '-'
  })

  const rules: FormRules = {
    issueType: [{ required: true, message: '请输入问题类型', trigger: 'blur' }],
    issueDesc: [{ required: true, message: '请输入问题描述', trigger: 'blur' }],
    responsibleDeptId: [{ required: true, message: '请选择责任部门', trigger: 'change' }],
    responsibleUserId: [{ required: true, message: '请选择责任人', trigger: 'change' }],
    deadlineDate: [{ required: true, message: '请选择整改期限', trigger: 'change' }],
  }

  const getInventoryResultLabel = (result?: string) => {
    const mapper: Record<string, string> = {
      NORMAL: '正常',
      LOSS: '盘亏',
      MISSING: '缺失',
      DAMAGED: '损坏',
      LOCATION_DIFF: '位置不符',
      RESPONSIBLE_DIFF: '责任人不符'
    }
    return mapper[String(result || '').toUpperCase()] || result || '巡检异常'
  }

  const goBack = () => {
    persistRealEstateDetailTab(assetId.value, 'rectification')
    router.push(`/asset/real-estate/detail/${assetId.value}`)
  }

  const loadResponsibleUsers = async (keyword = '') => {
    userOptionsLoading.value = true
    try {
      const response = await listRealEstateResponsibleUsers({ keyword })
      responsibleUserOptions.value = toArrayData<{ value: number; label: string }>(response)
    } finally {
      userOptionsLoading.value = false
    }
  }

  const toObjectData = <T,>(response: any): T => {
    if (response?.data !== undefined) {
      return response.data
    }
    return response
  }

  const toRows = <T,>(response: any): T[] => {
    if (Array.isArray(response?.rows)) {
      return response.rows
    }
    if (Array.isArray(response?.data?.rows)) {
      return response.data.rows
    }
    return []
  }

  const toArrayData = <T,>(response: any): T[] => {
    if (Array.isArray(response)) {
      return response
    }
    if (Array.isArray(response?.data)) {
      return response.data
    }
    return []
  }

  const loadCreateContext = async () => {
    Object.assign(completionInfo, {
      completedTime: '',
      completionDesc: '',
      acceptanceRemark: ''
    })

    if (!createTaskId.value || Number.isNaN(createTaskId.value)) {
      ElMessage.error('缺少巡检任务参数，无法发起整改')
      goBack()
      return
    }

    const [taskResponse, taskAssetResponse] = await Promise.all([
      getInventoryTask(createTaskId.value),
      listInventoryTaskAssets(createTaskId.value, {
        assetId: assetId.value,
        pageNum: 1,
        pageSize: 10
      })
    ])

    Object.assign(taskMeta, toObjectData<any>(taskResponse))
    const sourceRecord = toRows<TaskAssetRecord>(taskAssetResponse)[0]
    if (!sourceRecord?.itemId) {
      ElMessage.error('未找到对应巡检结果，无法发起整改')
      goBack()
      return
    }

    formData.taskId = createTaskId.value
    formData.inventoryItemId = sourceRecord.itemId
    formData.issueType = getInventoryResultLabel(sourceRecord.inventoryResult)
    formData.issueDesc = sourceRecord.resultDesc || '请结合现场情况补充整改说明'
    sourceResultDesc.value = sourceRecord.resultDesc || ''
  }

  const loadEditContext = async () => {
    const response = await getRealEstateRectification(assetId.value, rectificationId.value as number)
    const detail = toObjectData<any>(response)

    Object.assign(formData, {
      rectificationId: detail.rectificationId,
      taskId: detail.taskId,
      inventoryItemId: detail.inventoryItemId,
      rectificationStatus: detail.rectificationStatus || 'PENDING',
      issueType: detail.issueType || '',
      issueDesc: detail.issueDesc || '',
      responsibleDeptId: detail.responsibleDeptId,
      responsibleUserId: detail.responsibleUserId,
      deadlineDate: detail.deadlineDate || '',
      remark: detail.remark || ''
    })

    Object.assign(taskMeta, {
      taskNo: detail.taskNo,
      taskName: detail.taskName
    })
    Object.assign(completionInfo, {
      completedTime: detail.completedTime || '',
      completionDesc: detail.completionDesc || '',
      acceptanceRemark: detail.acceptanceRemark || ''
    })

    sourceResultDesc.value = detail.issueDesc || ''
  }

  const loadPage = async () => {
    if (!assetId.value || Number.isNaN(assetId.value)) {
      ElMessage.error('不动产资产ID无效')
      goBack()
      return
    }

    loading.value = true
    try {
      const [detailResponse, deptTreeResponse] = await Promise.all([
        getRealEstateDetail(assetId.value),
        getRealEstateDeptTree()
      ])

      Object.assign(detailData, toObjectData<any>(detailResponse))
      deptTreeOptions.value = toArrayData<any>(deptTreeResponse)
      await loadResponsibleUsers('')

      if (isEditMode.value) {
        await loadEditContext()
      } else {
        await loadCreateContext()
      }
    } finally {
      loading.value = false
    }
  }

  const buildPayload = () => {
    return {
      rectificationId: formData.rectificationId,
      // 中文注释：兼容仍按请求体预校验 assetId 的服务端实例，路径参数仍然是主口径。
      assetId: assetId.value,
      taskId: formData.taskId,
      inventoryItemId: formData.inventoryItemId,
      rectificationStatus: formData.rectificationStatus,
      issueType: formData.issueType.trim(),
      issueDesc: formData.issueDesc.trim(),
      responsibleDeptId: formData.responsibleDeptId,
      responsibleUserId: formData.responsibleUserId,
      deadlineDate: formData.deadlineDate,
      remark: formData.remark.trim() || undefined
    }
  }

  const handleSubmit = async () => {
    if (isCompletedRectification.value) {
      ElMessage.warning('已完成整改单不允许再次编辑，请通过新流程发起后续处理')
      return
    }

    const valid = await formRef.value?.validate().catch(() => false)
    if (!valid) {
      return
    }

    submitting.value = true
    try {
      const payload = buildPayload()
      if (isEditMode.value) {
        await updateRealEstateRectification(assetId.value, payload)
      } else {
        await addRealEstateRectification(assetId.value, payload)
      }

      ElMessage.success('整改单保存成功')
      goBack()
    } finally {
      submitting.value = false
    }
  }

  watch(
    () => [route.params.assetId, route.params.rectificationId, route.query.taskId],
    async () => {
      await loadPage()
    },
    { immediate: true }
  )
</script>
<style scoped lang="scss">
  .asset-real-estate-rectification-form {
    --asset-accent: #0f766e;
    --asset-border: #e5edf6;
    --asset-panel-bg: #fff;
    --asset-text-main: #18233a;
    --asset-text-secondary: #5d6b86;

    display: flex;
    flex-direction: column;
    gap: 12px;
    background:
      radial-gradient(circle at 0% 0%, rgb(15 118 110 / 8%), transparent 35%),
      radial-gradient(circle at 100% 0%, rgb(249 115 22 / 8%), transparent 38%),
      var(--art-main-bg-color);
  }

  .hero-card,
  .section-card {
    border: 1px solid var(--asset-border);
    border-radius: 12px;
    background: var(--asset-panel-bg);
  }

  .processing-layout {
    display: grid;
    grid-template-columns: minmax(0, 1.55fr) minmax(320px, 0.9fr);
    gap: 12px;
    align-items: start;
  }

  .processing-main,
  .processing-side {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .page-title {
    font-size: 28px;
    font-weight: 700;
    color: var(--asset-text-main);
  }

  .page-desc {
    margin-top: 6px;
    font-size: 14px;
    line-height: 1.7;
    color: var(--asset-text-secondary);
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

  .section-body {
    padding: 16px 16px 0;
  }

  .form-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 0 16px;
  }

  .action-bar {
    position: sticky;
    bottom: 0;
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    padding: 16px;
    margin-top: 12px;
    border-top: 1px solid #edf2f8;
    background: linear-gradient(180deg, rgb(255 255 255 / 92%), #fff 35%);
    backdrop-filter: blur(8px);
  }

  .context-descriptions :deep(.el-descriptions__body),
  .completion-descriptions :deep(.el-descriptions__body) {
    overflow-x: auto;
  }

  .context-descriptions :deep(.el-descriptions__table),
  .completion-descriptions :deep(.el-descriptions__table) {
    width: 100%;
    min-width: 100%;
  }

  .context-descriptions :deep(.el-descriptions__cell),
  .completion-descriptions :deep(.el-descriptions__cell) {
    line-height: 1.7;
    word-break: break-word;
    white-space: normal;
  }

  .guide-panel {
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding: 16px;
  }

  .guide-panel__headline {
    font-size: 18px;
    font-weight: 700;
    line-height: 1.6;
    color: #18233a;
  }

  .guide-panel__line {
    position: relative;
    padding-left: 14px;
    font-size: 13px;
    line-height: 1.8;
    color: #51627f;

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 10px;
      width: 6px;
      height: 6px;
      border-radius: 999px;
      background: var(--asset-accent);
    }
  }

  @media (width <= 1080px) {
    .processing-layout {
      grid-template-columns: 1fr;
    }
  }

  @media (width <= 900px) {
    .form-grid {
      grid-template-columns: 1fr;
    }

    .action-bar {
      position: static;
      padding-top: 12px;
    }
  }
</style>
