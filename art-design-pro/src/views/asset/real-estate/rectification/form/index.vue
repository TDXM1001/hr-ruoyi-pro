<template>
  <div class="asset-real-estate-rectification-form art-full-height flex flex-col gap-3 overflow-auto p-3">
    <ElCard class="hero-card" shadow="never">
      <div class="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
        <div class="flex flex-col gap-2">
          <ElButton link type="primary" icon="ri:arrow-left-line" @click="goBack">返回整改页签</ElButton>
          <div>
            <div class="page-title">{{ pageTitle }}</div>
            <div class="page-desc">整改页只做登记与更新，不在这里提前做审批流，保持 M1 最小闭环。</div>
          </div>
        </div>

        <div class="header-tags flex flex-wrap gap-2">
          <ElTag type="success" effect="light">{{ detailData.assetCode || '-' }}</ElTag>
          <ElTag effect="light">{{ taskMeta.taskNo || formData.taskId || '未绑定任务' }}</ElTag>
        </div>
      </div>
    </ElCard>

    <ElCard class="section-card" shadow="never">
      <template #header>
        <div class="card-title">整改上下文</div>
      </template>

      <ElDescriptions :column="3" border>
        <ElDescriptionsItem label="资产编码">{{ detailData.assetCode || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="资产名称">{{ detailData.assetName || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="任务编号">{{ taskMeta.taskNo || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="任务名称">{{ taskMeta.taskName || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="问题类型">{{ formData.issueType || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="结果说明">{{ sourceResultDesc || '-' }}</ElDescriptionsItem>
      </ElDescriptions>
    </ElCard>

    <ElCard class="section-card" shadow="never" v-loading="loading">
      <template #header>
        <div class="card-title">整改登记</div>
      </template>

      <div class="section-body">
        <ElForm ref="formRef" :model="formData" :rules="rules" label-width="110px">
          <ElFormItem label="问题类型" prop="issueType">
            <ElInput v-model="formData.issueType" maxlength="64" placeholder="请输入问题类型" />
          </ElFormItem>

          <ElFormItem label="问题描述" prop="issueDesc">
            <ElInput
              v-model="formData.issueDesc"
              type="textarea"
              :rows="4"
              maxlength="500"
              show-word-limit
              placeholder="请输入整改问题描述"
            />
          </ElFormItem>

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
            >
              <ElOption
                v-for="item in responsibleUserOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </ElSelect>
          </ElFormItem>

          <ElFormItem label="整改期限" prop="deadlineDate">
            <ElDatePicker
              v-model="formData.deadlineDate"
              type="date"
              value-format="YYYY-MM-DD"
              class="w-full"
              placeholder="请选择整改期限"
            />
          </ElFormItem>

          <ElFormItem label="整改状态" prop="rectificationStatus">
            <ElRadioGroup v-model="formData.rectificationStatus">
              <ElRadioButton label="PENDING" value="PENDING">待整改</ElRadioButton>
              <ElRadioButton label="COMPLETED" value="COMPLETED">已完成</ElRadioButton>
            </ElRadioGroup>
          </ElFormItem>

          <ElFormItem label="备注">
            <ElInput
              v-model="formData.remark"
              type="textarea"
              :rows="3"
              maxlength="500"
              show-word-limit
              placeholder="请输入备注"
            />
          </ElFormItem>
        </ElForm>

        <div class="action-bar">
          <ElButton @click="goBack">取消</ElButton>
          <ElButton type="primary" :loading="submitting" @click="handleSubmit">保存整改单</ElButton>
        </div>
      </div>
    </ElCard>
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
  const pageTitle = computed(() => (isEditMode.value ? '编辑整改单' : '新增整改单'))

  const rules: FormRules = {
    issueType: [{ required: true, message: '请输入问题类型', trigger: 'blur' }],
    issueDesc: [{ required: true, message: '请输入问题描述', trigger: 'blur' }],
    responsibleDeptId: [{ required: true, message: '请选择责任部门', trigger: 'change' }],
    responsibleUserId: [{ required: true, message: '请选择责任人', trigger: 'change' }],
    deadlineDate: [{ required: true, message: '请选择整改期限', trigger: 'change' }],
    rectificationStatus: [{ required: true, message: '请选择整改状态', trigger: 'change' }]
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
    padding: 16px;
  }

  .action-bar {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    margin-top: 24px;
  }
</style>
