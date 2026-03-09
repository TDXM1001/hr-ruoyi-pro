<template>
  <ElDialog
    v-model="visible"
    :title="dialogType === 'add' ? '新增角色' : '编辑角色'"
    width="500px"
    align-center
    @close="handleClose"
  >
    <ElForm ref="formRef" :model="form" :rules="rules" label-width="100px">
      <ElFormItem label="角色名称" prop="roleName">
        <ElInput v-model="form.roleName" placeholder="请输入角色名称" />
      </ElFormItem>
      <ElFormItem label="权限字符" prop="roleKey">
        <ElInput v-model="form.roleKey" placeholder="请输入权限字符" />
      </ElFormItem>
      <ElFormItem label="显示顺序" prop="roleSort">
        <ElInputNumber v-model="form.roleSort" :min="0" controls-position="right" />
      </ElFormItem>
      <ElFormItem label="角色状态">
        <ElRadioGroup v-model="form.status">
          <ElRadio value="0">正常</ElRadio>
          <ElRadio value="1">停用</ElRadio>
        </ElRadioGroup>
      </ElFormItem>
      <ElFormItem label="备注" prop="remark">
        <ElInput v-model="form.remark" type="textarea" :rows="3" placeholder="请输入内容" />
      </ElFormItem>
    </ElForm>
    <template #footer>
      <ElButton @click="handleClose">取消</ElButton>
      <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">提交</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import type { FormInstance, FormRules } from 'element-plus'
  import { addRole, updateRole, type SysRole } from '@/api/system/role'

  // 定义组件属性
  interface Props {
    modelValue: boolean
    dialogType: 'add' | 'edit'
    roleData?: SysRole
  }

  // 定义组件事件
  interface Emits {
    (e: 'update:modelValue', value: boolean): void
    (e: 'success'): void // 提交成功后的回调
  }

  const props = withDefaults(defineProps<Props>(), {
    modelValue: false,
    dialogType: 'add',
    roleData: undefined
  })

  const emit = defineEmits<Emits>()

  const formRef = ref<FormInstance>()
  const submitLoading = ref(false) // 提交按钮加载状态

  /**
   * 弹窗显示状态双向绑定
   */
  const visible = computed({
    get: () => props.modelValue,
    set: (value) => emit('update:modelValue', value)
  })

  /**
   * 表单验证规则
   */
  const rules = reactive<FormRules>({
    roleName: [{ required: true, message: '角色名称不能为空', trigger: 'blur' }],
    roleKey: [{ required: true, message: '权限字符不能为空', trigger: 'blur' }],
    roleSort: [{ required: true, message: '显示顺序不能为空', trigger: 'blur' }]
  })

  /**
   * 表单初始数据模板
   */
  const initialForm: SysRole = {
    roleId: undefined,
    roleName: '',
    roleKey: '',
    roleSort: 0,
    status: '0',
    remark: '',
    menuIds: [] // 初始值为空数组，避免后端获取 menuIds 长度时报空指针
  }

  /**
   * 表单响应式数据
   */
  const form = reactive<SysRole>({ ...initialForm })

  /**
   * 监听弹窗打开，初始化表单数据
   */
  watch(
    () => props.modelValue,
    (newVal) => {
      if (newVal) initForm()
    }
  )

  /**
   * 监听角色数据变化，实时同步至表单
   */
  watch(
    () => props.roleData,
    (newData) => {
      if (newData && props.modelValue) {
        initForm()
      }
    },
    { deep: true }
  )

  /**
   * 初始化表单数据逻辑
   */
  const initForm = () => {
    // 编辑状态下填充已有数据
    if (props.dialogType === 'edit' && props.roleData) {
      Object.assign(form, {
        roleId: props.roleData.roleId,
        roleName: props.roleData.roleName,
        roleKey: props.roleData.roleKey,
        roleSort: props.roleData.roleSort,
        status: props.roleData.status,
        remark: props.roleData.remark,
        menuIds: props.roleData.menuIds || []
      })
    } else {
      // 新增状态下重置为初始值
      Object.assign(form, initialForm)
    }
  }

  /**
   * 关闭弹窗并清理表单
   */
  const handleClose = () => {
    visible.value = false
    formRef.value?.resetFields()
  }

  /**
   * 处理表单提交
   */
  const handleSubmit = async () => {
    if (!formRef.value) return

    try {
      // 执行表单校验
      await formRef.value.validate()
      submitLoading.value = true

      if (props.dialogType === 'add') {
        // 【新增模式】构造干净的数据对象
        // 克隆表单数据并显式剔除 roleId 字段，即使它是 undefined，若依后端某些校验也可能对其敏感
        const reqData = { ...form }
        delete (reqData as any).roleId
        
        // 确保包含有效的 menuIds 数组（即使为空），防止后端报 NPE 错误
        if (!reqData.menuIds) {
          reqData.menuIds = []
        }

        // 传递给后端的新增接口
        await addRole(reqData)
        ElMessage.success('新增角色成功')
      } else {
        // 【编辑模式】直接提交整个表单数据
        await updateRole(form)
        ElMessage.success('修改角色成功')
      }

      emit('success') // 提交成功，通知父组件刷新表格
      handleClose() // 关闭当前弹窗
    } catch (error) {
      console.error('提交失败:', error)
      // 若依后端异常一般通过 axios 拦截器统一提示，此处主要处理校验失败逻辑
    } finally {
      submitLoading.value = false
    }
  }
</script>
