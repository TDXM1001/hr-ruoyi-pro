<template>
  <ElDialog
    v-model="dialogVisible"
    :title="dialogTitle"
    width="920px"
    destroy-on-close
    append-to-body
    class="handover-dialog"
  >
    <ElAlert :title="dialogDesc" type="info" :closable="false" show-icon class="mb-4" />

    <ElCard shadow="never" class="asset-preview-card mb-4">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="font-600">本次交接资产</span>
          <ElTag type="success" effect="light">共 {{ selectedAssets.length }} 宗</ElTag>
        </div>
      </template>

      <ElTable :data="selectedAssets" border size="small" max-height="220">
        <ElTableColumn prop="assetCode" label="资产编码" min-width="150" />
        <ElTableColumn prop="assetName" label="资产名称" min-width="180" show-overflow-tooltip />
        <ElTableColumn prop="assetStatus" label="当前状态" width="110">
          <template #default="{ row }">
            <ElTag :type="getStatusTagType(row.assetStatus)" effect="light">
              {{ getStatusLabel(row.assetStatus) }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn
          prop="useDeptName"
          label="当前使用部门"
          min-width="140"
          show-overflow-tooltip
        />
        <ElTableColumn
          prop="responsibleUserName"
          label="当前责任人"
          min-width="120"
          show-overflow-tooltip
        />
      </ElTable>
    </ElCard>

    <ElForm ref="formRef" :model="formData" :rules="formRules" label-width="110px">
      <ElRow :gutter="16">
        <ElCol :span="12">
          <ElFormItem label="交接类型">
            <ElInput :model-value="handoverTypeLabel" readonly />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
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
      </ElRow>

      <ElRow :gutter="16">
        <ElCol :span="12">
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
        <ElCol :span="12">
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
      </ElRow>

      <ElFormItem label="目标位置" prop="locationName">
        <ElInput
          v-model="formData.locationName"
          maxlength="200"
          clearable
          placeholder="请输入交接后使用位置，不填则沿用原位置"
        />
      </ElFormItem>

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
    </ElForm>

    <template #footer>
      <div class="dialog-footer">
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">确认提交</ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import type { FormInstance, FormRules } from 'element-plus'
  import { ElMessage } from 'element-plus'
  import type { AssetTreeOption, AssetUserOption } from '@/api/asset/ledger'
  import { getAssetDeptTree, listAssetResponsibleUsers } from '@/api/asset/ledger'
  import { addAssetHandoverOrder } from '@/api/asset/handover'

  defineOptions({ name: 'AssetHandoverDialog' })

  export type HandoverType = 'ASSIGN' | 'TRANSFER' | 'RETURN'

  export interface SelectedAssetItem {
    assetId: number
    assetCode: string
    assetName: string
    assetStatus: string
    useDeptName?: string
    responsibleUserName?: string
  }

  const props = withDefaults(
    defineProps<{
      modelValue: boolean
      handoverType: HandoverType
      selectedAssetIds: number[]
      selectedAssets?: SelectedAssetItem[]
    }>(),
    {
      selectedAssets: () => []
    }
  )

  const emit = defineEmits<{
    (e: 'update:modelValue', value: boolean): void
    (e: 'success'): void
  }>()

  const formRef = ref<FormInstance>()
  const submitLoading = ref(false)
  const responsibleUserLoading = ref(false)
  const deptOptions = ref<AssetTreeOption[]>([])
  const responsibleUserOptions = ref<AssetUserOption[]>([])

  const treeSelectProps = {
    value: 'id',
    label: 'label',
    children: 'children',
    disabled: 'disabled'
  }

  const formData = reactive({
    handoverDate: '',
    toDeptId: undefined as number | undefined,
    toUserId: undefined as number | undefined,
    locationName: '',
    remark: ''
  })

  const dialogVisible = computed({
    get: () => props.modelValue,
    set: (value: boolean) => emit('update:modelValue', value)
  })

  const selectedAssets = computed(() => props.selectedAssets || [])

  const handoverTypeLabel = computed(() => {
    const mapper: Record<HandoverType, string> = {
      ASSIGN: '领用',
      TRANSFER: '调拨',
      RETURN: '退还'
    }
    return mapper[props.handoverType]
  })

  const dialogTitle = computed(() => `发起${handoverTypeLabel.value}交接`)

  const dialogDesc = computed(() => {
    if (props.handoverType === 'RETURN') {
      return '退还场景可不填目标部门，不填时后端会自动回落到资产权属部门。'
    }
    return '本次交接采用“整单提交、整单校验”的事务策略，任一资产校验失败则整单回滚。'
  })

  const deptPlaceholder = computed(() => {
    return props.handoverType === 'RETURN' ? '可选：不填则默认退还到权属部门' : '请选择目标部门'
  })

  // 中文注释：交接类型不同，前端规则与后端规则保持一致，避免产生“前端可提交、后端必失败”的体验问题。
  const formRules = computed<FormRules>(() => ({
    handoverDate: [{ required: true, message: '请选择交接日期', trigger: 'change' }],
    toDeptId: [
      {
        required: props.handoverType !== 'RETURN',
        message: '领用/调拨必须选择目标部门',
        trigger: 'change'
      }
    ],
    toUserId: [
      {
        required: props.handoverType === 'ASSIGN' || props.handoverType === 'TRANSFER',
        message: '领用/调拨必须选择目标责任人',
        trigger: 'change'
      }
    ]
  }))

  const toArrayData = <T,>(response: any): T[] => {
    if (Array.isArray(response)) {
      return response
    }
    return response?.data || []
  }

  const getStatusLabel = (status?: string) => {
    const mapper: Record<string, string> = {
      IN_LEDGER: '在册',
      IN_USE: '使用中',
      IDLE: '闲置中',
      INVENTORYING: '盘点中',
      PENDING_DISPOSAL: '待处置',
      DISPOSED: '已处置'
    }
    return mapper[status || ''] || status || '-'
  }

  const getStatusTagType = (status?: string) => {
    const mapper: Record<string, 'primary' | 'success' | 'warning' | 'danger' | 'info'> = {
      IN_LEDGER: 'primary',
      IN_USE: 'success',
      IDLE: 'info',
      INVENTORYING: 'warning',
      PENDING_DISPOSAL: 'warning',
      DISPOSED: 'danger'
    }
    return mapper[status || ''] || 'info'
  }

  const resetForm = () => {
    formData.handoverDate = new Date().toISOString().slice(0, 10)
    formData.toDeptId = undefined
    formData.toUserId = undefined
    formData.locationName = ''
    formData.remark = ''
    nextTick(() => formRef.value?.clearValidate())
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

  const buildPayload = () => {
    return {
      handoverType: props.handoverType,
      handoverDate: formData.handoverDate,
      assetIds: props.selectedAssetIds,
      toDeptId: formData.toDeptId,
      toUserId: formData.toUserId,
      locationName: formData.locationName?.trim() || undefined,
      remark: formData.remark?.trim() || undefined
    }
  }

  const handleSubmit = async () => {
    if (!props.selectedAssetIds.length) {
      ElMessage.warning('请先选择至少一宗资产')
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
      emit('success')
      dialogVisible.value = false
    } finally {
      submitLoading.value = false
    }
  }

  watch(
    () => props.modelValue,
    async (visible) => {
      if (!visible) {
        return
      }
      resetForm()
      if (!deptOptions.value.length) {
        await loadDeptTree()
      }
      if (!responsibleUserOptions.value.length) {
        await handleResponsibleUserSearch()
      }
    }
  )
</script>

<style scoped lang="scss">
  .handover-dialog {
    :deep(.el-dialog__body) {
      padding-top: 12px;
    }

    .asset-preview-card {
      border: 1px solid #e6ebf5;
    }

    .dialog-footer {
      display: flex;
      justify-content: flex-end;
      gap: 10px;
    }
  }
</style>
