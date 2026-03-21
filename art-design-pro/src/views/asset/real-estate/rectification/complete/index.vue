<template>
  <div class="asset-real-estate-rectification-complete p-3" data-testid="rectification-complete-processing-page">
    <ElCard class="hero-card" shadow="never">
      <div class="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
        <div class="flex flex-col gap-2">
          <ElButton link type="primary" icon="ri:arrow-left-line" @click="goBack">返回整改页签</ElButton>
          <div>
            <div class="page-title">{{ pageTitle }}</div>
            <div class="page-desc">{{ pageDesc }}</div>
          </div>
        </div>

        <div class="header-tags flex flex-wrap gap-2">
          <ElTag type="success" effect="light">{{ detailData.assetCode || '-' }}</ElTag>
          <ElTag effect="light">{{ rectificationData.rectificationNo || '未识别整改单' }}</ElTag>
        </div>
      </div>
    </ElCard>

    <div class="complete-layout" data-testid="rectification-complete-layout">
      <div class="complete-main" data-testid="rectification-complete-main">
        <ElCard class="section-card" shadow="never">
          <template #header>
            <div class="card-title">{{ isCompletedRectification ? '完成结果' : '完成登记' }}</div>
          </template>

          <div class="section-body">
            <ElAlert
              class="mb-4"
              :type="isCompletedRectification ? 'success' : 'info'"
              show-icon
              :closable="false"
              :title="
                isCompletedRectification
                  ? '该整改单已完成，当前页面只做结果查看，不再允许重复提交。'
                  : '完成动作只负责收口整改事实，不再修改责任、期限和问题描述等基础信息。'
              "
            />

            <ElDescriptions
              v-if="isCompletedRectification"
              class="result-descriptions"
              :column="1"
              border
            >
              <ElDescriptionsItem label="完成时间">{{ rectificationData.completedTime || '-' }}</ElDescriptionsItem>
              <ElDescriptionsItem label="整改状态">{{ rectificationStatusLabel }}</ElDescriptionsItem>
              <ElDescriptionsItem label="关闭方式">整改完成收口</ElDescriptionsItem>
              <ElDescriptionsItem label="完成说明">
                {{ rectificationData.completionDesc || '-' }}
              </ElDescriptionsItem>
              <ElDescriptionsItem label="验收备注">
                {{ rectificationData.acceptanceRemark || '-' }}
              </ElDescriptionsItem>
            </ElDescriptions>

            <ElForm v-else ref="formRef" :model="formData" :rules="rules" label-width="110px">
              <ElFormItem label="完成说明" prop="completionDesc">
                <ElInput
                  v-model="formData.completionDesc"
                  type="textarea"
                  :rows="6"
                  maxlength="500"
                  show-word-limit
                  placeholder="请输入本次整改已完成的事实说明，例如已修复问题、现场复核结果等"
                />
              </ElFormItem>

              <ElFormItem label="验收备注">
                <ElInput
                  v-model="formData.acceptanceRemark"
                  type="textarea"
                  :rows="5"
                  maxlength="500"
                  show-word-limit
                  placeholder="请输入资产管理员验收备注，可为空"
                />
              </ElFormItem>
            </ElForm>
          </div>

          <div class="action-bar">
            <ElButton @click="goBack">{{ isCompletedRectification ? '返回整改页签' : '取消' }}</ElButton>
            <ElButton v-if="!isCompletedRectification" type="primary" :loading="submitting" @click="handleSubmit">
              确认完成整改
            </ElButton>
          </div>
        </ElCard>
      </div>

      <div class="complete-side" data-testid="rectification-complete-side">
        <ElCard class="section-card" shadow="never" v-loading="loading">
          <template #header>
            <div class="card-title">整改上下文</div>
          </template>

          <ElDescriptions class="context-descriptions" :column="1" border>
            <ElDescriptionsItem label="资产编码">{{ detailData.assetCode || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="资产名称">{{ detailData.assetName || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="整改单号">{{ rectificationData.rectificationNo || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="任务编号">{{ rectificationData.taskNo || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="任务名称">{{ rectificationData.taskName || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="整改状态">{{ rectificationStatusLabel }}</ElDescriptionsItem>
            <ElDescriptionsItem label="问题类型">{{ rectificationData.issueType || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="问题描述">{{ rectificationData.issueDesc || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="责任部门">{{ rectificationData.responsibleDeptName || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="责任人">{{ rectificationData.responsibleUserName || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="整改期限">{{ rectificationData.deadlineDate || '-' }}</ElDescriptionsItem>
          </ElDescriptions>
        </ElCard>

        <ElCard class="section-card" shadow="never">
          <template #header>
            <div class="card-title">收口提示</div>
          </template>

          <div class="guide-panel">
            <div class="guide-panel__headline">
              {{ isCompletedRectification ? '本单已完成收口，可重点回看结果。' : '这里只做收口，不再回头改基础信息。' }}
            </div>
            <div class="guide-panel__line">完成说明必须体现已完成的事实和现场复核结果。</div>
            <div class="guide-panel__line">验收备注可选，但建议写明资产管理员是否完成抽检。</div>
            <div class="guide-panel__line">提交成功后会回到详情壳整改页签，方便继续查看整体整改状态。</div>
          </div>
        </ElCard>

        <ElCard v-if="isCompletedRectification" class="section-card" shadow="never">
          <template #header>
            <div class="card-title">审批挂载信息</div>
          </template>

          <ElDescriptions class="context-descriptions" :column="1" border>
            <ElDescriptionsItem label="审批状态">{{ approvalStatusLabel }}</ElDescriptionsItem>
            <ElDescriptionsItem label="提交时间">{{ rectificationData.approvalSubmittedTime || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="审批完成">{{ rectificationData.approvalFinishedTime || '-' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="最新意见">{{ latestApprovalOpinion }}</ElDescriptionsItem>
          </ElDescriptions>

          <div class="approval-action-bar">
            <ElButton v-if="canSubmitApproval" type="success" plain :loading="approvalSubmitting" @click="handleApprovalAction('submit')">
              提交审批
            </ElButton>
            <ElButton v-if="canApproveOrReject" type="success" plain :loading="approvalSubmitting" @click="handleApprovalAction('approve')">
              审批通过
            </ElButton>
            <ElButton v-if="canApproveOrReject" type="danger" plain :loading="approvalSubmitting" @click="handleApprovalAction('reject')">
              审批驳回
            </ElButton>
          </div>
        </ElCard>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
  import type { FormInstance, FormRules } from 'element-plus'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import {
    approveRealEstateRectification,
    completeRealEstateRectification,
    getRealEstateDetail,
    getRealEstateRectification,
    listRealEstateRectificationApprovalRecords,
    rejectRealEstateRectificationApproval,
    submitRealEstateRectificationApproval
  } from '@/api/asset/real-estate'
  import { persistRealEstateDetailTab } from '../../detail/tab-state'

  defineOptions({ name: 'AssetRealEstateRectificationCompletePage' })

  const route = useRoute()
  const router = useRouter()
  route.meta.activePath = '/asset/real-estate'

  const loading = ref(false)
  const submitting = ref(false)
  const approvalSubmitting = ref(false)
  const formRef = ref<FormInstance>()
  const detailData = reactive<Record<string, any>>({})
  const rectificationData = reactive<Record<string, any>>({})
  const approvalRecords = ref<Array<Record<string, any>>>([])
  const formData = reactive({
    completionDesc: '',
    acceptanceRemark: ''
  })

  const assetId = computed(() => Number(route.params.assetId))
  const rectificationId = computed(() => Number(route.params.rectificationId))
  const isCompletedRectification = computed(() => {
    return String(rectificationData.rectificationStatus || '').toUpperCase() === 'COMPLETED'
  })
  const pageTitle = computed(() => {
    return isCompletedRectification.value ? '整改完成结果' : '整改完成页'
  })
  const pageDesc = computed(() => {
    if (isCompletedRectification.value) {
      return '该整改单已按标准收口，当前页面仅用于查看完成事实、完成时间和验收备注。'
    }
    return '完成页只做状态收口，资产管理员在这里补充完成说明与验收备注，确保“待整改 -> 已完成”有明确事实留痕。'
  })

  const rules: FormRules = {
    completionDesc: [{ required: true, message: '请输入完成说明', trigger: 'blur' }]
  }

  const rectificationStatusLabel = computed(() => {
    const mapper: Record<string, string> = {
      PENDING: '待整改',
      COMPLETED: '已完成'
    }
    return mapper[String(rectificationData.rectificationStatus || '').toUpperCase()] || rectificationData.rectificationStatus || '-'
  })

  const approvalStatusLabel = computed(() => {
    const mapper: Record<string, string> = {
      UNSUBMITTED: '待提交审批',
      SUBMITTED: '审批中',
      APPROVED: '审批通过',
      REJECTED: '审批驳回'
    }
    return mapper[String(rectificationData.approvalStatus || '').toUpperCase()] || '待提交审批'
  })

  const latestApprovalOpinion = computed(() => {
    return approvalRecords.value[0]?.opinion || '-'
  })

  const canSubmitApproval = computed(() => {
    const approvalStatus = String(rectificationData.approvalStatus || '').toUpperCase()
    return isCompletedRectification.value && (!approvalStatus || approvalStatus === 'UNSUBMITTED' || approvalStatus === 'REJECTED')
  })

  const canApproveOrReject = computed(() => {
    return isCompletedRectification.value && String(rectificationData.approvalStatus || '').toUpperCase() === 'SUBMITTED'
  })

  const toObjectData = <T,>(response: any): T => {
    if (response?.data !== undefined) {
      return response.data
    }
    return response
  }

  const goBack = () => {
    persistRealEstateDetailTab(assetId.value, 'rectification')
    router.push(`/asset/real-estate/detail/${assetId.value}`)
  }

  const loadPage = async () => {
    if (!assetId.value || Number.isNaN(assetId.value) || !rectificationId.value || Number.isNaN(rectificationId.value)) {
      ElMessage.error('整改完成页路由参数无效')
      goBack()
      return
    }

    loading.value = true
    try {
      const [detailResponse, rectificationResponse] = await Promise.all([
        getRealEstateDetail(assetId.value),
        getRealEstateRectification(assetId.value, rectificationId.value)
      ])

      Object.assign(detailData, toObjectData<any>(detailResponse))
      Object.assign(rectificationData, toObjectData<any>(rectificationResponse))
      formData.completionDesc = rectificationData.completionDesc || ''
      formData.acceptanceRemark = rectificationData.acceptanceRemark || ''
      const approvalResponse = await listRealEstateRectificationApprovalRecords(assetId.value, rectificationId.value)
      approvalRecords.value = Array.isArray(approvalResponse?.data)
        ? approvalResponse.data
        : Array.isArray(approvalResponse)
          ? approvalResponse
          : []
    } finally {
      loading.value = false
    }
  }

  const askApprovalOpinion = async (title: string, placeholder: string) => {
    const result = await ElMessageBox.prompt('请输入审批意见', title, {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      inputPlaceholder: placeholder,
      inputValidator: (value) => {
        return value?.trim() ? true : '审批意见不能为空'
      }
    })
    return result.value.trim()
  }

  const handleApprovalAction = async (action: 'submit' | 'approve' | 'reject') => {
    if (!assetId.value || !rectificationId.value || !isCompletedRectification.value) {
      return
    }

    const configs = {
      submit: {
        title: '提交整改审批',
        placeholder: '请输入提交审批说明',
        success: '整改审批已提交',
        request: submitRealEstateRectificationApproval
      },
      approve: {
        title: '整改审批通过',
        placeholder: '请输入审批通过意见',
        success: '整改审批已通过',
        request: approveRealEstateRectification
      },
      reject: {
        title: '整改审批驳回',
        placeholder: '请输入审批驳回意见',
        success: '整改审批已驳回',
        request: rejectRealEstateRectificationApproval
      }
    } as const

    const config = configs[action]
    const opinion = await askApprovalOpinion(config.title, config.placeholder).catch(() => '')
    if (!opinion) {
      return
    }

    approvalSubmitting.value = true
    try {
      await config.request(assetId.value, rectificationId.value, { opinion })
      ElMessage.success(config.success)
      await loadPage()
    } finally {
      approvalSubmitting.value = false
    }
  }

  const handleSubmit = async () => {
    if (isCompletedRectification.value) {
      ElMessage.warning('该整改单已完成，请直接查看完成结果')
      return
    }

    const valid = await formRef.value?.validate().catch(() => false)
    if (!valid) {
      return
    }

    submitting.value = true
    try {
      await completeRealEstateRectification(assetId.value, rectificationId.value, {
        completionDesc: formData.completionDesc.trim(),
        acceptanceRemark: formData.acceptanceRemark.trim() || undefined
      })
      ElMessage.success('整改已完成')
      goBack()
    } finally {
      submitting.value = false
    }
  }

  watch(
    () => [route.params.assetId, route.params.rectificationId],
    async () => {
      await loadPage()
    },
    { immediate: true }
  )
</script>
<style scoped lang="scss">
  .asset-real-estate-rectification-complete {
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

  .complete-layout {
    display: grid;
    grid-template-columns: minmax(0, 1.5fr) minmax(320px, 0.9fr);
    gap: 12px;
    align-items: start;
  }

  .complete-main,
  .complete-side {
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
    max-width: 860px;
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
  .result-descriptions :deep(.el-descriptions__body) {
    overflow-x: auto;
  }

  .context-descriptions :deep(.el-descriptions__table),
  .result-descriptions :deep(.el-descriptions__table) {
    width: 100%;
    min-width: 100%;
  }

  .context-descriptions :deep(.el-descriptions__cell),
  .result-descriptions :deep(.el-descriptions__cell) {
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

  .approval-action-bar {
    display: flex;
    flex-wrap: wrap;
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
    .complete-layout {
      grid-template-columns: 1fr;
    }
  }

  @media (width <= 900px) {
    .action-bar {
      position: static;
      padding-top: 12px;
    }
  }
</style>
