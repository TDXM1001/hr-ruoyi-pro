<template>
  <ElDialog
    :title="dialogType === 'add' ? '新增字典数据' : '修改字典数据'"
    v-model="visible"
    width="500px"
    destroy-on-close
    append-to-body
    @closed="handleClosed"
  >
    <ElForm ref="formRef" :model="form" :rules="rules" label-width="80px" v-loading="loading">
      <ElFormItem label="字典类型" prop="dictType">
        <ElInput v-model="form.dictType" :disabled="true" />
      </ElFormItem>
      <ElFormItem label="字典标签" prop="dictLabel">
        <ElInput v-model="form.dictLabel" placeholder="请输入字典标签" maxlength="100" />
      </ElFormItem>
      <ElFormItem label="字典键值" prop="dictValue">
        <ElInput v-model="form.dictValue" placeholder="请输入字典键值" maxlength="100" />
      </ElFormItem>
      <ElFormItem label="样式属性" prop="cssClass">
        <ElInput v-model="form.cssClass" placeholder="请输入样式属性" maxlength="100" />
      </ElFormItem>
      <ElFormItem label="字典排序" prop="dictSort">
        <ElInputNumber v-model="form.dictSort" :min="0" />
      </ElFormItem>
      <ElFormItem label="回显样式" prop="listClass">
        <ElSelect v-model="form.listClass" placeholder="请选择回显样式" class="w-full">
          <ElOption
            v-for="item in listClassOptions"
            :key="item.value"
            :label="item.label + '(' + item.value + ')'"
            :value="item.value"
          />
        </ElSelect>
      </ElFormItem>
      <ElFormItem label="状态" prop="status">
        <ElRadioGroup v-model="form.status">
          <ElRadio v-for="dict in sys_normal_disable" :key="dict.value" :label="dict.value">{{
            dict.label
          }}</ElRadio>
        </ElRadioGroup>
      </ElFormItem>
      <ElFormItem label="备注" prop="remark">
        <ElInput v-model="form.remark" type="textarea" placeholder="请输入内容" />
      </ElFormItem>
    </ElForm>

    <template #footer>
      <div class="dialog-footer">
        <ElButton @click="visible = false">取消</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">确定</ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { ref, reactive, watch } from 'vue'
  import { getData, addData, updateData } from '@/api/system/dict/data'
  import { useDict } from '@/utils/dict'
  import { ElMessage, type FormInstance, type FormRules } from 'element-plus'

  const props = defineProps<{
    modelValue: boolean
    dialogType: 'add' | 'edit'
    formData?: any
    defaultDictType?: string
  }>()

  const emit = defineEmits(['update:modelValue', 'success'])

  const { sys_normal_disable } = useDict('sys_normal_disable')

  const visible = ref(false)
  const loading = ref(false)
  const submitLoading = ref(false)
  const formRef = ref<FormInstance>()

  const initialForm = {
    dictCode: undefined,
    dictLabel: '',
    dictValue: '',
    cssClass: '',
    listClass: 'default',
    dictSort: 0,
    status: '0',
    dictType: '',
    remark: ''
  }

  const listClassOptions = [
    { label: '默认', value: 'default' },
    { label: '成功', value: 'success' },
    { label: '信息', value: 'info' },
    { label: '警告', value: 'warning' },
    { label: '危险', value: 'danger' }
  ]

  const form = reactive({ ...initialForm })

  const rules = reactive<FormRules>({
    dictLabel: [{ required: true, message: '字典标签不能为空', trigger: 'blur' }],
    dictValue: [{ required: true, message: '字典键值不能为空', trigger: 'blur' }],
    dictSort: [{ required: true, message: '字典排序不能为空', trigger: 'blur' }]
  })

  watch(
    () => props.modelValue,
    async (val) => {
      visible.value = val
      if (val) {
        if (props.dialogType === 'edit' && props.formData?.dictCode) {
          loading.value = true
          try {
            const res: any = await getData(props.formData.dictCode)
            Object.assign(form, res.data || res)
          } catch (error) {
            console.error('获取字典数据详情失败:', error)
          } finally {
            loading.value = false
          }
        } else {
          Object.assign(form, { ...initialForm, dictType: props.defaultDictType })
        }
      }
    }
  )

  watch(
    () => visible.value,
    (val) => {
      emit('update:modelValue', val)
    }
  )

  function handleClosed() {
    formRef.value?.resetFields()
    Object.assign(form, initialForm)
  }

  async function handleSubmit() {
    if (!formRef.value) return
    await formRef.value.validate(async (valid) => {
      if (valid) {
        submitLoading.value = true
        try {
          if (props.dialogType === 'edit') {
            await updateData(form)
            ElMessage.success('修改成功')
          } else {
            await addData(form)
            ElMessage.success('新增成功')
          }
          visible.value = false
          emit('success')
        } finally {
          submitLoading.value = false
        }
      }
    })
  }
</script>
