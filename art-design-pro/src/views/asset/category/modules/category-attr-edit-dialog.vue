<template>
  <ElDialog
    v-model="visible"
    :title="dialogTitle"
    width="760px"
    destroy-on-close
    append-to-body
    @closed="handleClosed"
  >
    <ElAlert
      :title="`动态属性会挂载到当前分类下${props.categoryName ? `：${props.categoryName}` : ''}`"
      type="info"
      :closable="false"
      class="mb-4"
    />

    <ElForm ref="formRef" :model="formData" :rules="formRules" label-width="110px">
      <ElRow :gutter="16">
        <ElCol :xs="24" :md="12">
          <ElFormItem label="字段编码" prop="attrCode">
            <ElInput
              v-model="formData.attrCode"
              placeholder="请输入字段编码，例如 manufacturer_name"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :xs="24" :md="12">
          <ElFormItem label="字段名称" prop="attrName">
            <ElInput v-model="formData.attrName" placeholder="请输入字段名称" />
          </ElFormItem>
        </ElCol>
      </ElRow>

      <ElRow :gutter="16">
        <ElCol :xs="24" :md="12">
          <ElFormItem label="数据类型" prop="dataType">
            <ElSelect v-model="formData.dataType" class="w-full" placeholder="请选择数据类型">
              <ElOption
                v-for="item in dataTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>
        <ElCol :xs="24" :md="12">
          <ElFormItem label="是否必填" prop="isRequired">
            <ElRadioGroup v-model="formData.isRequired">
              <ElRadio value="1">是</ElRadio>
              <ElRadio value="0">否</ElRadio>
            </ElRadioGroup>
          </ElFormItem>
        </ElCol>
      </ElRow>

      <ElRow :gutter="16">
        <ElCol :xs="24" :md="8">
          <ElFormItem label="是否唯一" prop="isUnique">
            <ElRadioGroup v-model="formData.isUnique">
              <ElRadio value="1">是</ElRadio>
              <ElRadio value="0">否</ElRadio>
            </ElRadioGroup>
          </ElFormItem>
        </ElCol>
        <ElCol :xs="24" :md="8">
          <ElFormItem label="列表展示" prop="isListDisplay">
            <ElRadioGroup v-model="formData.isListDisplay">
              <ElRadio value="1">是</ElRadio>
              <ElRadio value="0">否</ElRadio>
            </ElRadioGroup>
          </ElFormItem>
        </ElCol>
        <ElCol :xs="24" :md="8">
          <ElFormItem label="查询条件" prop="isQueryCondition">
            <ElRadioGroup v-model="formData.isQueryCondition">
              <ElRadio value="1">是</ElRadio>
              <ElRadio value="0">否</ElRadio>
            </ElRadioGroup>
          </ElFormItem>
        </ElCol>
      </ElRow>

      <ElRow :gutter="16">
        <ElCol :xs="24" :md="12">
          <ElFormItem label="默认值" prop="defaultValue">
            <ElInput v-model="formData.defaultValue" placeholder="不填写时默认留空" />
          </ElFormItem>
        </ElCol>
        <ElCol :xs="24" :md="12">
          <ElFormItem label="状态" prop="status">
            <ElRadioGroup v-model="formData.status">
              <ElRadio value="0">启用</ElRadio>
              <ElRadio value="1">停用</ElRadio>
            </ElRadioGroup>
          </ElFormItem>
        </ElCol>
      </ElRow>

      <ElRow :gutter="16">
        <ElCol :xs="24" :md="12">
          <ElFormItem label="选项来源" prop="optionSourceType">
            <ElSelect v-model="formData.optionSourceType" class="w-full" placeholder="请选择选项来源">
              <ElOption
                v-for="item in optionSourceTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>
        <ElCol :xs="24" :md="12">
          <ElFormItem label="校验规则" prop="validationRule">
            <ElInput v-model="formData.validationRule" placeholder="例如 ^[A-Za-z0-9_]+$" />
          </ElFormItem>
        </ElCol>
      </ElRow>

      <ElFormItem label="选项内容" prop="optionSource">
        <ElInput
          v-model="formData.optionSource"
          type="textarea"
          :rows="4"
          placeholder="手工维护时可填写枚举项，一行一条或按 1:办公类 形式填写"
        />
      </ElFormItem>

      <ElFormItem label="备注" prop="remark">
        <ElInput
          v-model="formData.remark"
          type="textarea"
          :rows="3"
          placeholder="可填写字段用途、展示规则等补充说明"
        />
      </ElFormItem>
    </ElForm>

    <template #footer>
      <div class="dialog-footer">
        <ElButton @click="visible = false" v-ripple>取消</ElButton>
        <ElButton type="primary" :loading="submitting" @click="handleSubmit" v-ripple>
          保存
        </ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { computed, reactive, ref, watch } from 'vue'
  import { ElMessage } from 'element-plus'
  import type { FormInstance, FormRules } from 'element-plus'
  import { addCategoryAttr, updateCategoryAttr } from '@/api/asset/category-attr'
  import type { AssetDynamicAttrDefinition, AssetDynamicAttrDefinitionReq } from '@/types/asset'
  import {
    buildCategoryAttrSubmitPayload,
    normalizeAttrCode,
    validateReservedAttrCode
  } from '../category-attr.helper'

  interface CategoryAttrFormData extends AssetDynamicAttrDefinitionReq {
    attrId?: number
  }

  const props = defineProps<{
    modelValue: boolean
    categoryId?: number
    categoryName?: string
    attrData?: Partial<AssetDynamicAttrDefinition>
  }>()

  const emit = defineEmits<{
    (e: 'update:modelValue', value: boolean): void
    (e: 'success'): void
  }>()

  const visible = ref(false)
  const submitting = ref(false)
  const formRef = ref<FormInstance>()

  const dataTypeOptions = [
    { label: '文本', value: 'text' },
    { label: '数字', value: 'number' },
    { label: '日期', value: 'date' },
    { label: 'JSON', value: 'json' },
    { label: '选项', value: 'select' }
  ]

  const optionSourceTypeOptions = [
    { label: '手工维护', value: '1' },
    { label: '字典数据', value: '2' },
    { label: '远程接口', value: '3' }
  ]

  const createInitialFormData = (): CategoryAttrFormData => ({
    categoryId: props.categoryId || 0,
    attrCode: '',
    attrName: '',
    dataType: 'text',
    isRequired: '0',
    isUnique: '0',
    isListDisplay: '0',
    isQueryCondition: '0',
    defaultValue: '',
    optionSourceType: '1',
    optionSource: '',
    validationRule: '',
    status: '0',
    remark: ''
  })

  const formData = reactive<CategoryAttrFormData>(createInitialFormData())

  const dialogTitle = computed(() => (formData.attrId ? '编辑动态属性' : '新增动态属性'))

  const formRules: FormRules = {
    attrCode: [
      { required: true, message: '字段编码不能为空', trigger: 'blur' },
      {
        validator: (_rule, value: string, callback) => {
          const normalizedValue = normalizeAttrCode(value)
          if (!normalizedValue) {
            callback(new Error('字段编码不能为空'))
            return
          }
          try {
            validateReservedAttrCode(normalizedValue)
          } catch (error) {
            callback(error instanceof Error ? error : new Error('字段编码校验失败'))
            return
          }
          callback()
        },
        trigger: 'blur'
      }
    ],
    attrName: [{ required: true, message: '字段名称不能为空', trigger: 'blur' }],
    dataType: [{ required: true, message: '数据类型不能为空', trigger: 'change' }]
  }

  /** 弹窗每次打开时都按后端口径回填，避免旧数据污染新建表单。 */
  const syncFormData = () => {
    const initialValue = createInitialFormData()
    Object.assign(formData, initialValue, props.attrData || {})
    formData.categoryId = props.categoryId || props.attrData?.categoryId || 0
    formData.attrCode = normalizeAttrCode(formData.attrCode)
    formData.attrName = String(formData.attrName || '')
    formData.dataType = formData.dataType || 'text'
    formData.isRequired = formData.isRequired || '0'
    formData.isUnique = formData.isUnique || '0'
    formData.isListDisplay = formData.isListDisplay || '0'
    formData.isQueryCondition = formData.isQueryCondition || '0'
    formData.optionSourceType = String(
      buildCategoryAttrSubmitPayload({
        optionSourceType: formData.optionSourceType || '1'
      }).optionSourceType
    )
    formData.status = formData.status || '0'
  }

  watch(
    () => props.modelValue,
    (value) => {
      visible.value = value
      if (value) {
        syncFormData()
      }
    }
  )

  watch(
    () => visible.value,
    (value) => {
      emit('update:modelValue', value)
    }
  )

  const handleSubmit = async () => {
    if (!formRef.value) return
    const valid = await formRef.value.validate().catch(() => false)
    if (!valid) return

    submitting.value = true
    try {
      const payload = buildCategoryAttrSubmitPayload({
        ...formData,
        categoryId: props.categoryId || formData.categoryId,
        attrCode: normalizeAttrCode(formData.attrCode)
      }) as CategoryAttrFormData

      if (payload.attrId) {
        await updateCategoryAttr(payload)
        ElMessage.success('动态属性更新成功')
      } else {
        await addCategoryAttr(payload)
        ElMessage.success('动态属性新增成功')
      }
      visible.value = false
      emit('success')
    } finally {
      submitting.value = false
    }
  }

  const handleClosed = () => {
    formRef.value?.resetFields()
    Object.assign(formData, createInitialFormData())
  }
</script>
