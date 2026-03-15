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
      :title="`当前仅维护分类字段模板${props.categoryName ? `，所属分类：${props.categoryName}` : ''}`"
      type="info"
      :closable="false"
      class="mb-4"
    />

    <ElForm ref="formRef" :model="formData" :rules="formRules" label-width="110px">
      <ElRow :gutter="16">
        <ElCol :xs="24" :md="12">
          <ElFormItem label="字段编码" prop="attrCode">
            <ElInput v-model="formData.attrCode" placeholder="请输入字段编码，例如 manufacturer" />
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
            <ElSelect v-model="formData.dataType" class="w-full" placeholder="选择数据类型">
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
        <ElCol :xs="24" :md="12">
          <ElFormItem label="默认值" prop="defaultValue">
            <ElInput v-model="formData.defaultValue" placeholder="可选，未填写时留空" />
          </ElFormItem>
        </ElCol>
        <ElCol :xs="24" :md="12">
          <ElFormItem label="模板状态" prop="status">
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
            <ElSelect v-model="formData.optionSourceType" class="w-full" placeholder="选择选项来源">
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
            <ElInput v-model="formData.validationRule" placeholder="例如 ^[A-Za-z0-9_-]+$" />
          </ElFormItem>
        </ElCol>
      </ElRow>

      <ElFormItem label="选项内容" prop="optionSource">
        <ElInput
          v-model="formData.optionSource"
          type="textarea"
          :rows="4"
          placeholder="手工选项可按“值:名称”逐行填写，例如 1:自有"
        />
      </ElFormItem>

      <ElFormItem label="备注" prop="remark">
        <ElInput
          v-model="formData.remark"
          type="textarea"
          :rows="3"
          placeholder="可选，补充字段使用说明"
        />
      </ElFormItem>
    </ElForm>

    <template #footer>
      <div class="dialog-footer">
        <ElButton @click="visible = false" v-ripple>取消</ElButton>
        <ElButton type="primary" :loading="submitting" @click="handleSubmit" v-ripple>
          保存模板
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
    getReservedCodeMessage,
    isReservedAttrCode
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
    { label: '数值', value: 'number' },
    { label: '日期', value: 'date' },
    { label: 'JSON', value: 'json' },
    { label: '选项', value: 'select' }
  ]

  const optionSourceTypeOptions = [
    { label: '手工维护', value: 'manual' },
    { label: '字典来源', value: 'dict' },
    { label: '远程来源', value: 'remote' }
  ]

  const createInitialFormData = (): CategoryAttrFormData => ({
    categoryId: props.categoryId || 0,
    attrCode: '',
    attrName: '',
    dataType: 'text',
    isRequired: '0',
    defaultValue: '',
    optionSourceType: 'manual',
    optionSource: '',
    validationRule: '',
    status: '0',
    remark: ''
  })

  const formData = reactive<CategoryAttrFormData>(createInitialFormData())

  const dialogTitle = computed(() => (formData.attrId ? '编辑属性模板' : '新增属性模板'))

  const normalizeAttrCode = (value?: string) =>
    String(value || '')
      .trim()
      .toLowerCase()

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
          if (isReservedAttrCode(normalizedValue)) {
            callback(new Error(getReservedCodeMessage(normalizedValue)))
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

  /** 把外部传入的编辑数据与当前分类上下文合并到表单。 */
  const syncFormData = () => {
    const initialValue = createInitialFormData()
    Object.assign(formData, initialValue, props.attrData || {})
    formData.categoryId = props.categoryId || props.attrData?.categoryId || 0
    formData.attrCode = normalizeAttrCode(formData.attrCode)
    formData.attrName = String(formData.attrName || '')
    formData.dataType = formData.dataType || 'text'
    formData.isRequired = formData.isRequired || '0'
    formData.optionSourceType = formData.optionSourceType || 'manual'
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
        ElMessage.success('属性模板修改成功')
      } else {
        await addCategoryAttr(payload)
        ElMessage.success('属性模板新增成功')
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
