<template>
  <ElDrawer
    :title="dialogType === 'add' ? '新增资产' : '修改资产'"
    v-model="visible"
    size="600px"
    destroy-on-close
    @closed="handleClosed"
  >
    <ElForm
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="loading"
    >
      <ElFormItem label="资产编号" prop="assetNo">
        <ElInput v-model="formData.assetNo" placeholder="请输入资产编号" :disabled="dialogType === 'edit'" />
      </ElFormItem>
      <ElFormItem label="资产名称" prop="assetName">
        <ElInput v-model="formData.assetName" placeholder="请输入资产名称" />
      </ElFormItem>
      <ElFormItem label="资产分类" prop="categoryId">
        <ElTreeSelect
          v-model="formData.categoryId"
          :data="categoryOptions"
          :props="{ value: 'id', label: 'name', children: 'children' }"
          value-key="id"
          placeholder="选择资产分类"
          check-strictly
          class="w-full"
        />
      </ElFormItem>
      <ElFormItem label="资产类型" prop="assetType">
        <ElSelect v-model="formData.assetType" placeholder="选择资产类型" class="w-full">
          <ElOption label="不动产" :value="1" />
          <ElOption label="固定资产" :value="2" />
        </ElSelect>
      </ElFormItem>
      <ElFormItem label="归属部门" prop="deptId">
        <ElTreeSelect
          v-model="formData.deptId"
          :data="deptOptions"
          :props="{ value: 'deptId', label: 'deptName', children: 'children' }"
          value-key="deptId"
          placeholder="选择归属部门"
          check-strictly
          class="w-full"
        />
      </ElFormItem>
      <ElFormItem label="责任人" prop="userId">
        <ElSelect v-model="formData.userId" placeholder="选择责任人" class="w-full" clearable>
          <ElOption
            v-for="item in userOptions"
            :key="item.userId"
            :label="item.nickName"
            :value="item.userId"
          />
        </ElSelect>
      </ElFormItem>
      <ElFormItem label="资产状态" prop="status">
        <ElSelect v-model="formData.status" placeholder="选择状态" class="w-full">
          <ElOption label="正常" :value="1" />
          <ElOption label="领用中" :value="2" />
          <ElOption label="维修中" :value="3" />
          <ElOption label="盘点中" :value="4" />
          <ElOption label="已报废" :value="5" />
        </ElSelect>
      </ElFormItem>
      <ElFormItem label="备注" prop="remark">
        <ElInput v-model="formData.remark" type="textarea" placeholder="请输入备注" />
      </ElFormItem>
    </ElForm>
    <template #footer>
      <div class="drawer-footer flex justify-end gap-2">
        <ElButton @click="visible = false">取消</ElButton>
        <ElButton type="primary" @click="handleSubmit" :loading="submitLoading">确定</ElButton>
      </div>
    </template>
  </ElDrawer>
</template>

<script setup lang="ts">
  import { ref, reactive, watch } from 'vue'
  import { ElMessage } from 'element-plus'
  import type { FormRules } from 'element-plus'
  import { getInfo, addInfo, updateInfo } from '@/api/asset/info'
  import { listCategory } from '@/api/asset/category'
  import { listDept } from '@/api/system/dept'
  import { listUser } from '@/api/system/user'
  import { handleTree } from '@/utils/ruoyi'

  const props = defineProps<{
    modelValue: boolean
    dialogType: 'add' | 'edit'
    assetData?: any
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
  const deptOptions = ref<any[]>([])
  const userOptions = ref<any[]>([])

  const initialFormData = {
    assetNo: '',
    assetName: '',
    categoryId: undefined,
    assetType: 2,
    deptId: undefined,
    userId: undefined,
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

  /** 获取下拉选项 */
  const getOptions = async () => {
    try {
      // 获取分类
      const catRes: any = await listCategory()
      const catData = Array.isArray(catRes) ? catRes : catRes.data || catRes.rows || []
      categoryOptions.value = handleTree(catData, 'id')

      // 获取部门
      const deptRes: any = await listDept()
      const deptData = Array.isArray(deptRes) ? deptRes : deptRes.data || deptRes.rows || []
      deptOptions.value = handleTree(deptData, 'deptId')

      // 获取用户 (简单起见，这里直接列出)
      const userRes: any = await listUser({ pageSize: 100 })
      userOptions.value = userRes.rows || []
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
    await formRef.value.validate(async (valid: boolean) => {
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
    })
  }

  const handleClosed = () => {
    formRef.value?.resetFields()
    Object.assign(formData, initialFormData)
  }
</script>

<style scoped></style>
