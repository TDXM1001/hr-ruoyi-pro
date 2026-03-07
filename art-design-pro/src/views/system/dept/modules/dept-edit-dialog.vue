<template>
  <ElDialog
    :title="dialogType === 'add' ? '新增部门' : '修改部门'"
    v-model="visible"
    width="600px"
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
      <ElRow>
        <ElCol :span="24" v-if="formData.parentId !== 0">
          <ElFormItem label="上级部门" prop="parentId">
            <ElTreeSelect
              v-model="formData.parentId"
              :data="deptOptions"
              :props="{ value: 'deptId', label: 'deptName', children: 'children' }"
              value-key="deptId"
              placeholder="选择上级部门"
              check-strictly
              class="w-full"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="部门名称" prop="deptName">
            <ElInput v-model="formData.deptName" placeholder="请输入部门名称" />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="显示排序" prop="orderNum">
            <ElInputNumber
              v-model="formData.orderNum"
              :min="0"
              controls-position="right"
              class="w-full"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="负责人" prop="leader">
            <ElInput v-model="formData.leader" placeholder="请输入负责人" maxlength="20" />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="联系电话" prop="phone">
            <ElInput v-model="formData.phone" placeholder="请输入联系电话" maxlength="11" />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="邮箱" prop="email">
            <ElInput v-model="formData.email" placeholder="请输入邮箱" maxlength="50" />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="部门状态" prop="status">
            <ElRadioGroup v-model="formData.status">
              <ElRadio label="0">正常</ElRadio>
              <ElRadio label="1">停用</ElRadio>
            </ElRadioGroup>
          </ElFormItem>
        </ElCol>
      </ElRow>
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
  import { getDept, addDept, updateDept, listDeptExcludeChild, listDept } from '@/api/system/dept'
  import { handleTree } from '@/utils/ruoyi'

  const props = defineProps<{
    modelValue: boolean
    dialogType: 'add' | 'edit'
    deptData?: any
  }>()

  const emit = defineEmits<{
    (e: 'update:modelValue', value: boolean): void
    (e: 'success'): void
  }>()

  const visible = ref(false)
  const loading = ref(false)
  const submitLoading = ref(false)
  const formRef = ref()
  const deptOptions = ref<any[]>([])

  const initialFormData = {
    deptId: undefined,
    parentId: undefined,
    deptName: '',
    orderNum: 0,
    leader: '',
    phone: '',
    email: '',
    status: '0'
  }

  const formData = reactive({ ...initialFormData })

  const formRules: FormRules = {
    parentId: [{ required: true, message: '上级部门不能为空', trigger: 'blur' }],
    deptName: [{ required: true, message: '部门名称不能为空', trigger: 'blur' }],
    orderNum: [{ required: true, message: '显示排序不能为空', trigger: 'blur' }],
    email: [{ type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }],
    phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }]
  }

  const getTreeOptions = async () => {
    try {
      if (props.dialogType === 'edit' && props.deptData?.deptId) {
        const res: any = await listDeptExcludeChild(props.deptData.deptId)
        const data = Array.isArray(res) ? res : res.data || res.rows || []
        deptOptions.value = handleTree(data, 'deptId')
      } else {
        const res: any = await listDept()
        const data = Array.isArray(res) ? res : res.data || res.rows || []
        deptOptions.value = handleTree(data, 'deptId')
      }
    } catch (error) {
      console.error('获取部门树失败:', error)
    }
  }

  watch(
    () => props.modelValue,
    async (val) => {
      visible.value = val
      if (val) {
        getTreeOptions()
        if (props.dialogType === 'edit' && props.deptData?.deptId) {
          loading.value = true
          try {
            const res: any = await getDept(props.deptData.deptId)
            Object.assign(formData, res.data || res)
          } finally {
            loading.value = false
          }
        } else if (props.dialogType === 'add') {
          Object.assign(formData, initialFormData)
          if (props.deptData?.deptId) {
            formData.parentId = props.deptData.deptId
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
            await updateDept(formData)
            ElMessage.success('修改成功')
          } else {
            await addDept(formData)
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
    deptOptions.value = []
  }
</script>

<style scoped></style>
