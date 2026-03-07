<template>
  <ElDialog
    :title="dialogType === 'add' ? '新增岗位' : '修改岗位'"
    v-model="visible"
    width="500px"
    destroy-on-close
    append-to-body
    @closed="handleClosed"
  >
    <ElForm
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="80px"
      v-loading="loading"
    >
      <ElFormItem label="岗位名称" prop="postName">
        <ElInput v-model="formData.postName" placeholder="请输入岗位名称" />
      </ElFormItem>
      <ElFormItem label="岗位编码" prop="postCode">
        <ElInput v-model="formData.postCode" placeholder="请输入岗位编码" />
      </ElFormItem>
      <ElFormItem label="岗位顺序" prop="postSort">
        <ElInputNumber
          v-model="formData.postSort"
          :min="0"
          controls-position="right"
          class="w-full"
        />
      </ElFormItem>
      <ElFormItem label="岗位状态" prop="status">
        <ElRadioGroup v-model="formData.status">
          <ElRadio label="0">正常</ElRadio>
          <ElRadio label="1">停用</ElRadio>
        </ElRadioGroup>
      </ElFormItem>
      <ElFormItem label="备注" prop="remark">
        <ElInput v-model="formData.remark" type="textarea" placeholder="请输入备注" />
      </ElFormItem>
    </ElForm>
    <template #footer>
      <div class="dialog-footer">
        <ElButton @click="visible = false">取消</ElButton>
        <ElButton type="primary" @click="handleSubmit" :loading="submitLoading">确定</ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { ref, reactive, watch } from 'vue'
  import { ElMessage } from 'element-plus'
  import { getPost, addPost, updatePost } from '@/api/system/post'

  const props = defineProps<{
    modelValue: boolean
    dialogType: 'add' | 'edit'
    postData?: any
  }>()

  const emit = defineEmits<{
    (e: 'update:modelValue', value: boolean): void
    (e: 'success'): void
  }>()

  const visible = ref(false)
  const loading = ref(false)
  const submitLoading = ref(false)
  const formRef = ref()

  const initialFormData = {
    postId: undefined,
    postName: '',
    postCode: '',
    postSort: 0,
    status: '0',
    remark: ''
  }

  const formData = reactive({ ...initialFormData })

  const formRules = {
    postName: [{ required: true, message: '岗位名称不能为空', trigger: 'blur' }],
    postCode: [{ required: true, message: '岗位编码不能为空', trigger: 'blur' }],
    postSort: [{ required: true, message: '岗位顺序不能为空', trigger: 'blur' }]
  }

  watch(
    () => props.modelValue,
    async (val) => {
      visible.value = val
      if (val && props.dialogType === 'edit' && props.postData?.postId) {
        loading.value = true
        try {
          const res: any = await getPost(props.postData.postId)
          Object.assign(formData, res.data || res)
        } finally {
          loading.value = false
        }
      } else if (val && props.dialogType === 'add') {
        Object.assign(formData, initialFormData)
      }
    }
  )

  watch(
    () => visible.value,
    (val) => {
      emit('update:modelValue', val)
    }
  )

  const handleSubmit = async () => {
    if (!formRef.value) return
    await formRef.value.validate(async (valid: boolean) => {
      if (valid) {
        submitLoading.value = true
        try {
          if (props.dialogType === 'edit') {
            await updatePost(formData)
            ElMessage.success('修改成功')
          } else {
            await addPost(formData)
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

  const handleClosed = () => {
    formRef.value?.resetFields()
    Object.assign(formData, initialFormData)
  }
</script>

<style scoped></style>
