<template>
  <ElDialog
    :title="dialogType === 'add' ? '新增资产分类' : '修改资产分类'"
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
      label-width="100px"
      v-loading="loading"
    >
      <ElFormItem label="上级分类" prop="parentId">
        <ElTreeSelect
          v-model="formData.parentId"
          :data="categoryOptions"
          :props="{ value: 'id', label: 'name', children: 'children' }"
          value-key="id"
          placeholder="选择上级分类"
          check-strictly
          class="w-full"
        />
      </ElFormItem>
      <ElFormItem label="分类名称" prop="name">
        <ElInput v-model="formData.name" placeholder="请输入分类名称" />
      </ElFormItem>
      <ElFormItem label="分类编码" prop="code">
        <ElInput v-model="formData.code" placeholder="请输入分类编码" />
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
  import type { FormRules } from 'element-plus'
  import { getCategory, addCategory, updateCategory, listCategory } from '@/api/asset/category'
  import { handleTree } from '@/utils/ruoyi'

  const props = defineProps<{
    modelValue: boolean
    dialogType: 'add' | 'edit'
    categoryData?: any
  }>()

  const emit = defineEmits<{
    (e: 'update:modelValue', value: boolean): void
    (e: 'success'): void
  }>()

  const visible = ref(false)
  const loading = ref(false)
  const submitLoading = ref(false)
  const formRef = ref()
  const categoryOptions = ref<any[]>([])

  const initialFormData = {
    id: undefined,
    parentId: 0,
    name: '',
    code: ''
  }

  const formData = reactive({ ...initialFormData })

  const formRules: FormRules = {
    parentId: [{ required: true, message: '上级分类不能为空', trigger: 'blur' }],
    name: [{ required: true, message: '分类名称不能为空', trigger: 'blur' }]
  }

  const getTreeOptions = async () => {
    try {
      const res: any = await listCategory()
      const data = Array.isArray(res) ? res : res.data || res.rows || []
      const tree = handleTree(data, 'id')
      // 添加一个顶级节点
      categoryOptions.value = [{ id: 0, name: '主分类', children: tree }]
    } catch (error) {
      console.error('获取资产分类树失败:', error)
    }
  }

  watch(
    () => props.modelValue,
    async (val) => {
      visible.value = val
      if (val) {
        getTreeOptions()
        if (props.dialogType === 'edit' && props.categoryData?.id) {
          loading.value = true
          try {
            const res: any = await getCategory(props.categoryData.id)
            Object.assign(formData, res.data || res)
          } finally {
            loading.value = false
          }
        } else if (props.dialogType === 'add') {
          Object.assign(formData, initialFormData)
          if (props.categoryData?.id) {
            formData.parentId = props.categoryData.id
          }
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

  const handleSubmit = async () => {
    if (!formRef.value) return
    await formRef.value.validate(async (valid: boolean) => {
      if (valid) {
        submitLoading.value = true
        try {
          if (props.dialogType === 'edit') {
            await updateCategory(formData)
            ElMessage.success('修改成功')
          } else {
            await addCategory(formData)
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
    categoryOptions.value = []
  }
</script>

<style scoped></style>
