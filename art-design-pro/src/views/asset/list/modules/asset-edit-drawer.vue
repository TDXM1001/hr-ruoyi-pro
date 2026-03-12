<template>
  <ElDrawer
    :title="dialogType === 'add' ? '新增资产' : '修改资产'"
    v-model="visible"
    size="600px"
    destroy-on-close
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
      <!-- 资产分类插槽 -->
      <template #categoryId>
        <ElTreeSelect
          v-model="formData.categoryId"
          :data="categoryOptions"
          :props="{ value: 'id', label: 'name', children: 'children' }"
          value-key="id"
          placeholder="选择资产分类"
          check-strictly
          class="w-full"
        />
      </template>

      <!-- 归属部门插槽 -->
      <template #deptId>
        <ElTreeSelect
          v-model="formData.deptId"
          :data="deptOptions"
          :props="{ value: 'id', label: 'label', children: 'children' }"
          value-key="id"
          placeholder="选择归属部门"
          check-strictly
          class="w-full"
        />
      </template>

      <!-- 责任人插槽 -->
      <template #userId>
        <ElSelect v-model="formData.userId" placeholder="选择责任人" class="w-full" clearable>
          <ElOption
            v-for="item in userOptions"
            :key="item.userId"
            :label="item.nickName"
            :value="item.userId"
          />
        </ElSelect>
      </template>
    </ArtForm>

    <template #footer>
      <div class="drawer-footer flex justify-end gap-2 p-4 border-t">
        <ElButton @click="visible = false" v-ripple>取消</ElButton>
        <ElButton type="primary" @click="handleSubmit" :loading="submitLoading" v-ripple>
          确定
        </ElButton>
      </div>
    </template>
  </ElDrawer>
</template>

<script setup lang="ts">
  import { ref, reactive, watch, computed } from 'vue'
  import { ElMessage } from 'element-plus'
  import type { FormRules } from 'element-plus'
  import { getInfo, addInfo, updateInfo } from '@/api/asset/info'
  import { listCategory } from '@/api/asset/category'
  import { listUser, deptTreeSelect } from '@/api/system/user'
  import { handleTree } from '@/utils/ruoyi'
  import ArtForm from '@/components/core/forms/art-form/index.vue'
  import type { FormItem } from '@/components/core/forms/art-form/index.vue'
  import { useDict } from '@/utils/dict'

  const props = defineProps<{
    modelValue: boolean
    dialogType: 'add' | 'edit'
    assetData?: any
  }>()

  const emit = defineEmits<{
    (e: 'update:modelValue', value: boolean): void
    (e: 'success'): void
  }>()

  // 状态管理
  const visible = ref(false)
  const loading = ref(false)
  const submitLoading = ref(false)
  const formRef = ref()
  
  // 选项数据
  const categoryOptions = ref<any[]>([])
  const deptOptions = ref<any[]>([])
  const userOptions = ref<any[]>([])

  // 字典数据
  const { asset_type, asset_status } = useDict('asset_type', 'asset_status')

  const initialFormData = {
    assetNo: '',
    assetName: '',
    categoryId: 0,
    assetType: 2,
    deptId: 0,
    userId: 0,
    status: 1,
    remark: ''
  }

  const formData = reactive({ ...initialFormData })

  const formRules: FormRules = {
    assetNo: [{ required: true, message: '资产编号不能为空', trigger: 'blur' }],
    assetName: [{ required: true, message: '资产名称不能为空', trigger: 'blur' }],
    categoryId: [{ required: true, message: '资产分类不能为空', trigger: 'change' }],
    assetType: [{ required: true, message: '资产类型不能为空', trigger: 'change' }],
    status: [{ required: true, message: '资产状态不能为空', trigger: 'change' }]
  }

  // 定义表单项
  const formItems = computed<FormItem[]>(() => [
    {
      label: '资产编号',
      key: 'assetNo',
      type: 'input',
      props: { placeholder: '请输入资产编号', disabled: props.dialogType === 'edit' },
      span: 24
    },
    {
      label: '资产名称',
      key: 'assetName',
      type: 'input',
      props: { placeholder: '请输入资产名称' },
      span: 24
    },
    { label: '资产分类', key: 'categoryId', span: 24 },
    {
      label: '资产类型',
      key: 'assetType',
      type: 'select',
      props: { options: asset_type.value, placeholder: '请选择资产类型' },
      span: 12
    },
    {
      label: '资产状态',
      key: 'status',
      type: 'select',
      props: { options: asset_status.value, placeholder: '请选择状态' },
      span: 12
    },
    { label: '归属部门', key: 'deptId', span: 24 },
    { label: '责任人', key: 'userId', span: 24 },
    {
      label: '备注',
      key: 'remark',
      type: 'input',
      props: { type: 'textarea', placeholder: '请输入备注' },
      span: 24
    }
  ])

  /** 获取下拉选项 */
  const getOptions = async () => {
    try {
      const [catRes, deptRes, userRes] = await Promise.all([
        listCategory(),
        deptTreeSelect(),
        listUser({ pageSize: 100 })
      ])

      // 分类树
      const catList = Array.isArray(catRes) ? catRes : (catRes as any).data || (catRes as any).rows || []
      categoryOptions.value = handleTree(catList, 'id')

      // 部门树
      deptOptions.value = Array.isArray(deptRes) ? deptRes : (deptRes as any).data || []

      // 用户列表
      userOptions.value = (userRes as any).rows || []
    } catch (error) {
      console.error('获取选项失败:', error)
    }
  }

  watch(
    () => props.modelValue,
    async (val) => {
      visible.value = val
      if (val) {
        getOptions()
        if (props.dialogType === 'edit' && props.assetData?.assetNo) {
          loading.value = true
          try {
            const res: any = await getInfo(props.assetData.assetNo)
            Object.assign(formData, res.data || res)
          } finally {
            loading.value = false
          }
        } else if (props.dialogType === 'add') {
          Object.assign(formData, initialFormData)
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
          await updateInfo(formData)
          ElMessage.success('修改成功')
        } else {
          await addInfo(formData)
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
  }
</script>
