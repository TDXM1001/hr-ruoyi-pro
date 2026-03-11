<!-- 参数设置编辑弹窗 (修复版) -->
<template>
  <ElDialog
    v-model="visible"
    :title="dialogType === 'add' ? '新增参数' : '修改参数'"
    width="500px"
    append-to-body
    destroy-on-close
  >
    <ElForm ref="formRef" :model="form" :rules="rules" label-width="80px">
      <ElFormItem label="参数名称" prop="configName">
        <ElInput v-model="form.configName" placeholder="请输入参数名称" />
      </ElFormItem>
      <ElFormItem label="参数键名" prop="configKey">
        <ElInput v-model="form.configKey" placeholder="请输入参数键名" />
      </ElFormItem>
      <ElFormItem label="参数键值" prop="configValue">
        <ElInput v-model="form.configValue" placeholder="请输入参数键值" />
      </ElFormItem>
      <ElFormItem label="系统内置" prop="configType">
        <ElRadioGroup v-model="form.configType">
          <ElRadio
            v-for="dict in sys_yes_no"
            :key="dict.value"
            :label="dict.value"
          >{{ dict.label }}</ElRadio>
        </ElRadioGroup>
      </ElFormItem>
      <ElFormItem label="备注" prop="remark">
        <ElInput v-model="form.remark" type="textarea" placeholder="请输入内容" />
      </ElFormItem>
    </ElForm>
    <template #footer>
      <div class="dialog-footer">
        <ElButton type="primary" @click="submitForm" :loading="submitLoading">确 定</ElButton>
        <ElButton @click="visible = false">取 消</ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { ref, watch, reactive } from 'vue'
  import { addConfig, updateConfig } from '@/api/system/config'
  import { ElMessage, type FormInstance } from 'element-plus'
  import { useDict } from '@/utils/dict'

  const props = defineProps({
    modelValue: Boolean,
    dialogType: {
      type: String,
      default: 'add'
    },
    data: {
      type: Object,
      default: () => ({})
    }
  })

  const emit = defineEmits(['update:modelValue', 'success'])

  // 修复：在组件内部直接获取字典，确保数据响应式
  const { sys_yes_no } = useDict('sys_yes_no')

  const visible = ref(false)
  const submitLoading = ref(false)
  const formRef = ref<FormInstance>()

  const initialForm = {
    configId: undefined,
    configName: '',
    configKey: '',
    configValue: '',
    configType: 'Y',
    remark: ''
  }

  const form = reactive({ ...initialForm })

  const rules = {
    configName: [{ required: true, message: '参数名称不能为空', trigger: 'blur' }],
    configKey: [{ required: true, message: '参数键名不能为空', trigger: 'blur' }],
    configValue: [{ required: true, message: '参数键值不能为空', trigger: 'blur' }]
  }

  watch(
    () => props.modelValue,
    (val) => {
      visible.value = val
      if (val) {
        if (props.dialogType === 'edit' && props.data) {
          Object.assign(form, props.data)
        } else {
          reset()
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

  /** 表单重置 */
  const reset = () => {
    Object.assign(form, initialForm)
    formRef.value?.resetFields()
  }

  /** 提交按钮 */
  const submitForm = async () => {
    if (!formRef.value) return
    
    await formRef.value.validate(async (valid) => {
      if (valid) {
        submitLoading.value = true
        
        // 字段清洗：移除审计字段及冗余参数
        const submitData = { ...form }
        delete (submitData as any).createTime
        delete (submitData as any).updateTime
        delete (submitData as any).params

        try {
          if (submitData.configId !== undefined) {
            await updateConfig(submitData)
            ElMessage.success('修改成功')
          } else {
            await addConfig(submitData)
            ElMessage.success('新增成功')
          }
          visible.value = false
          emit('success')
        } catch (error) {
          console.error('提交参数失败:', error)
        } finally {
          submitLoading.value = false
        }
      }
    })
  }

  defineOptions({ name: 'ConfigEditDialog' })
</script>
