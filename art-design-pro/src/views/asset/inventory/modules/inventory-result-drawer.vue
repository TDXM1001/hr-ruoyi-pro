<template>
  <ElDrawer
    :model-value="modelValue"
    :title="drawerTitle"
    size="460px"
    destroy-on-close
    @close="handleClose"
  >
    <template #header>
      <div class="drawer-title">
        <span>{{ drawerTitle }}</span>
        <ElTag v-if="mode === 'batch'" type="info" effect="light">批量 {{ batchCount }} 宗</ElTag>
      </div>
    </template>

    <ElAlert v-if="mode === 'batch'" type="info" :closable="false" show-icon class="mb-3">
      <template #title>
        批量登记仅支持“正常盘点”场景，异常（盘亏/缺失/毁损）请在列表中逐条登记，确保后续动作准确。
      </template>
    </ElAlert>

    <ElDescriptions :column="1" border class="mb-3">
      <ElDescriptionsItem label="任务">{{ taskTitle || '-' }}</ElDescriptionsItem>
      <ElDescriptionsItem :label="mode === 'single' ? '资产' : '资产范围'">
        {{ mode === 'single' ? assetTitle || '-' : `共 ${batchCount} 宗资产` }}
      </ElDescriptionsItem>
    </ElDescriptions>

    <ElForm ref="formRef" :model="formData" :rules="formRules" label-width="110px">
      <ElFormItem v-if="mode === 'single'" label="盘点结果" prop="inventoryResult">
        <ElRadioGroup v-model="formData.inventoryResult">
          <ElRadioButton
            v-for="item in inventoryResultOptions"
            :key="item.value"
            :label="item.value"
            :value="item.value"
          >
            {{ item.label }}
          </ElRadioButton>
        </ElRadioGroup>
      </ElFormItem>

      <ElFormItem v-else label="盘点结果">
        <ElTag type="success" effect="light">正常（批量固定）</ElTag>
      </ElFormItem>

      <ElFormItem v-if="isAbnormalResult" label="后续动作" prop="followUpAction">
        <ElRadioGroup v-model="formData.followUpAction">
          <ElRadioButton
            v-for="item in followUpActionOptions"
            :key="item.value"
            :label="item.value"
            :value="item.value"
          >
            {{ item.label }}
          </ElRadioButton>
        </ElRadioGroup>
      </ElFormItem>

      <ElFormItem v-else label="在用确认">
        <ElSwitch
          v-model="formData.confirmedUse"
          inline-prompt
          active-text="在用"
          inactive-text="闲置"
        />
      </ElFormItem>

      <ElFormItem label="盘点时间" prop="checkedTime">
        <ElDatePicker
          v-model="formData.checkedTime"
          type="datetime"
          value-format="YYYY-MM-DD HH:mm:ss"
          class="w-full"
          placeholder="请选择盘点时间"
        />
      </ElFormItem>

      <ElFormItem label="实际位置">
        <ElInput
          v-model="formData.actualLocationName"
          maxlength="255"
          clearable
          placeholder="请输入实际位置（可选）"
        />
      </ElFormItem>

      <ElFormItem label="结果说明">
        <ElInput
          v-model="formData.resultDesc"
          type="textarea"
          :rows="2"
          maxlength="500"
          show-word-limit
          placeholder="请输入结果说明（可选）"
        />
      </ElFormItem>

      <ElFormItem label="备注">
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
      <div class="drawer-footer">
        <ElButton @click="handleClose">取消</ElButton>
        <ElButton type="primary" :loading="loading" @click="handleSubmit">
          {{ mode === 'single' ? '提交结果' : '批量提交' }}
        </ElButton>
      </div>
    </template>
  </ElDrawer>
</template>

<script setup lang="ts">
  import type { FormInstance, FormRules } from 'element-plus'
  import type { AssetInventoryResultPayload } from '@/api/asset/inventory'

  defineOptions({ name: 'InventoryResultDrawer' })

  type DrawerMode = 'single' | 'batch'

  type DrawerSubmitPayload = Omit<AssetInventoryResultPayload, 'taskId' | 'assetId'>

  const props = withDefaults(
    defineProps<{
      modelValue: boolean
      mode: DrawerMode
      loading?: boolean
      taskTitle?: string
      assetTitle?: string
      batchCount?: number
    }>(),
    {
      loading: false,
      mode: 'single',
      batchCount: 0
    }
  )

  const emit = defineEmits<{
    (event: 'update:modelValue', value: boolean): void
    (event: 'submit', payload: DrawerSubmitPayload): void
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

  const isAbnormalResult = computed(() => {
    if (props.mode === 'batch') {
      return false
    }
    return abnormalResultSet.has(formData.inventoryResult)
  })

  const drawerTitle = computed(() => {
    return props.mode === 'single' ? '登记盘点结果' : '批量登记正常盘点结果'
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
    // 中文注释：批量模式仅允许登记正常结果，前端直接固化字段，避免误传异常结果。
    if (props.mode === 'batch') {
      formData.inventoryResult = 'NORMAL'
      formData.followUpAction = 'NONE'
    }
    formRef.value?.clearValidate()
  }

  watch(
    () => [props.modelValue, props.mode],
    ([visible]) => {
      if (visible) {
        resetFormData()
      }
    }
  )

  watch(
    () => formData.inventoryResult,
    (value) => {
      if (props.mode === 'batch') {
        formData.inventoryResult = 'NORMAL'
        formData.followUpAction = 'NONE'
        return
      }
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
      inventoryResult: props.mode === 'batch' ? 'NORMAL' : formData.inventoryResult,
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
  .drawer-title {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .drawer-footer {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
  }

  :deep(.el-radio-group) {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }
</style>
