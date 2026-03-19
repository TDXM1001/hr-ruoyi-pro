<template>
  <ElDialog
    :model-value="modelValue"
    title="登记盘点结果"
    width="720px"
    destroy-on-close
    @close="handleClose"
  >
    <ElAlert
      :closable="false"
      type="info"
      show-icon
      class="mb-4"
      :title="`任务：${taskTitle}；资产：${assetTitle}`"
    />

    <ElForm ref="formRef" :model="formData" :rules="formRules" label-width="120px">
      <ElFormItem label="盘点结果" prop="inventoryResult">
        <ElRadioGroup v-model="formData.inventoryResult">
          <ElRadioButton
            v-for="item in inventoryResultOptions"
            :key="item.value"
            :label="item.value"
          >
            {{ item.label }}
          </ElRadioButton>
        </ElRadioGroup>
      </ElFormItem>

      <ElFormItem v-if="isAbnormalResult" label="后续动作" prop="followUpAction">
        <ElRadioGroup v-model="formData.followUpAction">
          <ElRadioButton
            v-for="item in followUpActionOptions"
            :key="item.value"
            :label="item.value"
          >
            {{ item.label }}
          </ElRadioButton>
        </ElRadioGroup>
      </ElFormItem>

      <ElFormItem v-if="!isAbnormalResult" label="确认在用" prop="confirmedUse">
        <ElSwitch
          v-model="formData.confirmedUse"
          inline-prompt
          active-text="是"
          inactive-text="否"
        />
      </ElFormItem>

      <ElRow :gutter="16">
        <ElCol :xs="24" :md="12">
          <ElFormItem label="盘点时间" prop="checkedTime">
            <ElDatePicker
              v-model="formData.checkedTime"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              class="w-full"
              placeholder="请选择盘点时间"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :xs="24" :md="12">
          <ElFormItem label="盘点位置" prop="actualLocationName">
            <ElInput
              v-model="formData.actualLocationName"
              maxlength="200"
              clearable
              placeholder="可选：记录实际位置"
            />
          </ElFormItem>
        </ElCol>
      </ElRow>

      <ElFormItem label="结果说明" prop="resultDesc">
        <ElInput
          v-model="formData.resultDesc"
          type="textarea"
          :rows="3"
          maxlength="500"
          show-word-limit
          placeholder="请输入盘点结果说明（可选）"
        />
      </ElFormItem>

      <ElFormItem label="备注" prop="remark">
        <ElInput
          v-model="formData.remark"
          type="textarea"
          :rows="2"
          maxlength="500"
          show-word-limit
          placeholder="请输入备注（可选）"
        />
      </ElFormItem>
    </ElForm>

    <template #footer>
      <ElButton @click="handleClose">取消</ElButton>
      <ElButton type="primary" :loading="loading" @click="handleSubmit">提交结果</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import type { FormInstance, FormRules } from 'element-plus'
  import type { AssetInventoryResultPayload } from '@/api/asset/inventory'

  defineOptions({ name: 'InventoryResultDialog' })

  interface InventoryDialogTask {
    taskNo?: string
    taskName?: string
  }

  interface InventoryDialogAsset {
    assetCode?: string
    assetName?: string
    assetId?: number
  }

  const props = withDefaults(
    defineProps<{
      modelValue: boolean
      loading?: boolean
      task?: InventoryDialogTask
      asset?: InventoryDialogAsset
    }>(),
    {
      loading: false
    }
  )

  const emit = defineEmits<{
    (event: 'update:modelValue', value: boolean): void
    (event: 'submit', payload: Omit<AssetInventoryResultPayload, 'taskId' | 'assetId'>): void
  }>()

  const formRef = ref<FormInstance>()

  const inventoryResultOptions = [
    { label: '正常', value: 'NORMAL' },
    { label: '盘亏', value: 'LOSS' },
    { label: '缺失', value: 'MISSING' },
    { label: '毁损', value: 'DAMAGED' }
  ]

  const followUpActionOptions = [
    { label: '台账修正', value: 'UPDATE_LEDGER' },
    { label: '发起处置', value: 'CREATE_DISPOSAL' }
  ]

  const abnormalResultSet = new Set(['LOSS', 'MISSING', 'DAMAGED'])

  const buildNowDateTime = () => {
    const now = new Date()
    const pad = (value: number) => String(value).padStart(2, '0')
    return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(
      now.getHours()
    )}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`
  }

  const createDefaultFormData = () => ({
    inventoryResult: 'NORMAL',
    followUpAction: 'NONE',
    confirmedUse: true,
    checkedTime: buildNowDateTime(),
    actualLocationName: '',
    resultDesc: '',
    remark: ''
  })

  const formData = reactive(createDefaultFormData())

  const isAbnormalResult = computed(() => abnormalResultSet.has(formData.inventoryResult))

  const taskTitle = computed(() => {
    if (!props.task) {
      return '未选择任务'
    }
    return [props.task.taskNo, props.task.taskName].filter(Boolean).join(' / ') || '未命名任务'
  })

  const assetTitle = computed(() => {
    if (!props.asset) {
      return '未选择资产'
    }
    return [props.asset.assetCode, props.asset.assetName].filter(Boolean).join(' / ') || `ID:${props.asset.assetId}`
  })

  const formRules: FormRules = {
    inventoryResult: [{ required: true, message: '请选择盘点结果', trigger: 'change' }],
    followUpAction: [
      {
        validator: (_rule, value, callback) => {
          if (isAbnormalResult.value && !value) {
            callback(new Error('异常盘点结果必须指定后续动作'))
            return
          }
          callback()
        },
        trigger: 'change'
      }
    ],
    checkedTime: [{ required: true, message: '请选择盘点时间', trigger: 'change' }]
  }

  const resetFormData = () => {
    Object.assign(formData, createDefaultFormData())
    formRef.value?.clearValidate()
  }

  watch(
    () => props.modelValue,
    (visible) => {
      if (visible) {
        resetFormData()
      }
    }
  )

  watch(
    () => formData.inventoryResult,
    (value) => {
      // 中文注释：异常结果默认引导到“台账修正”，正常结果统一回落到 NONE，避免提交脏数据。
      if (abnormalResultSet.has(value)) {
        if (!formData.followUpAction || formData.followUpAction === 'NONE') {
          formData.followUpAction = 'UPDATE_LEDGER'
        }
        return
      }
      formData.followUpAction = 'NONE'
    }
  )

  const handleClose = () => {
    emit('update:modelValue', false)
  }

  const handleSubmit = async () => {
    const valid = await formRef.value?.validate().catch(() => false)
    if (!valid) {
      return
    }

    emit('submit', {
      inventoryResult: formData.inventoryResult,
      followUpAction: isAbnormalResult.value ? formData.followUpAction : 'NONE',
      confirmedUse: isAbnormalResult.value ? undefined : formData.confirmedUse,
      checkedTime: formData.checkedTime,
      actualLocationName: formData.actualLocationName?.trim() || undefined,
      resultDesc: formData.resultDesc?.trim() || undefined,
      remark: formData.remark?.trim() || undefined
    })
  }
</script>

<style scoped lang="scss">
  :deep(.el-radio-group) {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }
</style>
