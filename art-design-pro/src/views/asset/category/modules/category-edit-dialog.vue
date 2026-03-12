<template>
  <ElDialog
    :title="dialogType === 'add' ? '新增资产分类' : '修改资产分类'"
    v-model="visible"
    width="500px"
    destroy-on-close
    append-to-body
    @closed="handleClosed"
  >
    <!-- 使用项目封装的 ArtForm 组件 -->
    <ArtForm
      ref="formRef"
      v-model="formData"
      :items="formItems"
      :rules="formRules"
      :span="24"
      label-width="100px"
      v-loading="loading"
      :show-reset="false"
      :show-submit="false"
    >
      <!-- 上级分类插槽 -->
      <template #parentId>
        <ElTreeSelect
          v-model="formData.parentId"
          :data="categoryOptions"
          :props="{ value: 'id', label: 'name', children: 'children' }"
          value-key="id"
          placeholder="选择上级分类"
          check-strictly
          class="w-full"
        />
      </template>
    </ArtForm>

    <template #footer>
      <div class="dialog-footer">
        <ElButton @click="visible = false" v-ripple>取消</ElButton>
        <ElButton type="primary" @click="handleSubmit" :loading="submitLoading" v-ripple
          >确定</ElButton
        >
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { ref, reactive, watch, computed } from 'vue'
  import { ElMessage } from 'element-plus'
  import type { FormRules } from 'element-plus'
  import { getCategory, addCategory, updateCategory, listCategory } from '@/api/asset/category'
  import { handleTree } from '@/utils/ruoyi'
  import ArtForm from '@/components/core/forms/art-form/index.vue'
  import type { FormItem } from '@/components/core/forms/art-form/index.vue'

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

  // 校验规则
  const formRules: FormRules = {
    parentId: [{ required: true, message: '上级分类不能为空', trigger: 'blur' }],
    name: [{ required: true, message: '分类名称不能为空', trigger: 'blur' }]
  }

  // 定义表单项
  const formItems = computed<FormItem[]>(() => [
    {
      label: '上级分类',
      key: 'parentId',
      span: 24
    },
    {
      label: '分类名称',
      key: 'name',
      type: 'input',
      props: { placeholder: '请输入分类名称' },
      span: 24
    },
    {
      label: '分类编码',
      key: 'code',
      type: 'input',
      props: { placeholder: '请输入分类编码' },
      span: 24
    }
  ])

  const getTreeOptions = async () => {
    try {
      const res: any = await listCategory()
      const list = Array.isArray(res) ? res : res.data || res.rows || []
      const tree = handleTree(list, 'id')
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
    const valid = await formRef.value.validate()
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
  }

  const handleClosed = () => {
    formRef.value?.reset()
    Object.assign(formData, initialFormData)
    categoryOptions.value = []
  }
</script>
