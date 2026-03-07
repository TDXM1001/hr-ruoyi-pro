<template>
  <ElDialog
    :title="dialogType === 'add' ? '新增用户' : '修改用户'"
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
        <ElCol :span="12">
          <ElFormItem label="用户昵称" prop="nickName">
            <ElInput v-model="formData.nickName" placeholder="请输入用户昵称" maxlength="30" />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="归属部门" prop="deptId">
            <ElTreeSelect
              v-model="formData.deptId"
              :data="deptOptions"
              :props="{ value: 'id', label: 'label', children: 'children' }"
              value-key="id"
              placeholder="请选择归属部门"
              check-strictly
              class="w-full"
            />
          </ElFormItem>
        </ElCol>
      </ElRow>

      <ElRow>
        <ElCol :span="12">
          <ElFormItem label="手机号码" prop="phonenumber">
            <ElInput v-model="formData.phonenumber" placeholder="请输入手机号码" maxlength="11" />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="邮箱" prop="email">
            <ElInput v-model="formData.email" placeholder="请输入邮箱" maxlength="50" />
          </ElFormItem>
        </ElCol>
      </ElRow>

      <ElRow>
        <ElCol :span="12">
          <ElFormItem label="用户名称" prop="userName" v-if="dialogType === 'add'">
            <ElInput v-model="formData.userName" placeholder="请输入用户名称" maxlength="30" />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="用户密码" prop="password" v-if="dialogType === 'add'">
            <ElInput
              v-model="formData.password"
              placeholder="请输入用户密码"
              type="password"
              maxlength="20"
              show-password
            />
          </ElFormItem>
        </ElCol>
      </ElRow>

      <ElRow>
        <ElCol :span="12">
          <ElFormItem label="用户性别" prop="sex">
            <ElSelect v-model="formData.sex" placeholder="请选择性别" class="w-full">
              <!-- 性别选项由字典 sys_user_sex 动态渲染 -->
              <ElOption
                v-for="item in sys_user_sex"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="状态" prop="status">
            <ElRadioGroup v-model="formData.status">
              <!-- 状态选项由字典 sys_normal_disable 动态渲染 -->
              <ElRadio v-for="item in sys_normal_disable" :key="item.value" :label="item.value">{{
                item.label
              }}</ElRadio>
            </ElRadioGroup>
          </ElFormItem>
        </ElCol>
      </ElRow>

      <ElRow>
        <ElCol :span="12">
          <ElFormItem label="岗位" prop="postIds">
            <ElSelect v-model="formData.postIds" multiple placeholder="请选择岗位" class="w-full">
              <ElOption
                v-for="item in postOptions"
                :key="item.postId"
                :label="item.postName"
                :value="item.postId"
                :disabled="item.status === '1'"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="角色" prop="roleIds">
            <ElSelect v-model="formData.roleIds" multiple placeholder="请选择角色" class="w-full">
              <ElOption
                v-for="item in roleOptions"
                :key="item.roleId"
                :label="item.roleName"
                :value="item.roleId"
                :disabled="item.status === '1'"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>
      </ElRow>

      <ElRow>
        <ElCol :span="24">
          <ElFormItem label="备注" prop="remark">
            <ElInput v-model="formData.remark" type="textarea" placeholder="请输入内容" />
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
  import { getUser, addUser, updateUser, deptTreeSelect } from '@/api/system/user'
  import { useDict } from '@/utils/dict'

  // 性别字典（sys_user_sex）与正常/停用状态字典（sys_normal_disable）
  const { sys_user_sex, sys_normal_disable } = useDict('sys_user_sex', 'sys_normal_disable')

  const props = defineProps<{
    modelValue: boolean
    dialogType: 'add' | 'edit'
    userData?: any
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
  const roleOptions = ref<any[]>([])
  const postOptions = ref<any[]>([])

  const initialFormData = {
    userId: undefined,
    deptId: undefined,
    userName: '',
    nickName: '',
    password: '',
    phonenumber: '',
    email: '',
    sex: '0',
    status: '0',
    postIds: [] as number[],
    roleIds: [] as number[],
    remark: ''
  }

  const formData = reactive({ ...initialFormData })

  const formRules: FormRules = {
    userName: [
      { required: true, message: '用户名称不能为空', trigger: 'blur' },
      { min: 2, max: 20, message: '用户名称长度必须介于 2 和 20 之间', trigger: 'blur' }
    ],
    nickName: [{ required: true, message: '用户昵称不能为空', trigger: 'blur' }],
    password: [
      { required: true, message: '用户密码不能为空', trigger: 'blur' },
      { min: 5, max: 20, message: '用户密码长度必须介于 5 和 20 之间', trigger: 'blur' }
    ],
    email: [{ type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }],
    phonenumber: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }]
  }

  const getTreeOptions = async () => {
    try {
      const res: any = await deptTreeSelect()
      deptOptions.value = Array.isArray(res) ? res : res.data || []
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
        loading.value = true
        try {
          const userId = props.dialogType === 'edit' ? props.userData?.userId : undefined
          const res: any = await getUser(userId)

          postOptions.value = res.posts || []
          roleOptions.value = res.roles || []

          if (props.dialogType === 'edit' && userId) {
            Object.assign(formData, res.data || {})
            formData.postIds = res.postIds || []
            formData.roleIds = res.roleIds || []
          } else {
            Object.assign(formData, initialFormData)
          }
        } finally {
          loading.value = false
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
            await updateUser(formData)
            ElMessage.success('修改成功')
          } else {
            await addUser(formData)
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
    postOptions.value = []
    roleOptions.value = []
  }
</script>

<style scoped></style>
