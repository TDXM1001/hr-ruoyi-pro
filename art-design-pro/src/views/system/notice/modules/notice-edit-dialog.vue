<!-- 通知公告编辑弹窗 (修复版) -->
<template>
  <ElDialog
    v-model="visible"
    :title="dialogType === 'add' ? '新增通知公告' : '修改通知公告'"
    width="800px"
    append-to-body
    destroy-on-close
  >
    <ElForm ref="formRef" :model="form" :rules="rules" label-width="80px">
      <ElRow>
        <ElCol :span="12">
          <ElFormItem label="公告标题" prop="noticeTitle">
            <ElInput v-model="form.noticeTitle" placeholder="请输入公告标题" />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="公告类型" prop="noticeType">
            <ElSelect v-model="form.noticeType" placeholder="请选择公告类型" clearable>
              <ElOption
                v-for="dict in sys_notice_type"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>
        <ElCol :span="24">
          <ElFormItem label="状态" prop="status">
            <ElRadioGroup v-model="form.status">
              <ElRadio
                v-for="dict in sys_notice_status"
                :key="dict.value"
                :label="dict.value"
              >{{ dict.label }}</ElRadio>
            </ElRadioGroup>
          </ElFormItem>
        </ElCol>
        <ElCol :span="24">
          <ElFormItem label="内容" prop="noticeContent">
            <ArtWangEditor
              v-model="form.noticeContent"
              height="400px"
              placeholder="请输入公告内容"
            />
          </ElFormItem>
        </ElCol>
      </ElRow>
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
  import { addNotice, updateNotice } from '@/api/system/notice'
  import { ElMessage, type FormInstance } from 'element-plus'
  import ArtWangEditor from '@/components/core/forms/art-wang-editor/index.vue'
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

  // 修复：字典引入子组件内部，异步响应更安全
  const { sys_notice_type, sys_notice_status } = useDict('sys_notice_type', 'sys_notice_status')

  const visible = ref(false)
  const submitLoading = ref(false)
  const formRef = ref<FormInstance>()

  const initialForm = {
    noticeId: undefined,
    noticeTitle: '',
    noticeType: '',
    noticeContent: '',
    status: '0',
    remark: ''
  }

  const form = reactive({ ...initialForm })

  const rules = {
    noticeTitle: [{ required: true, message: '公告标题不能为空', trigger: 'blur' }],
    noticeType: [{ required: true, message: '公告类型不能为空', trigger: 'change' }]
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
          if (submitData.noticeId !== undefined) {
            await updateNotice(submitData)
            ElMessage.success('修改成功')
          } else {
            await addNotice(submitData)
            ElMessage.success('新增成功')
          }
          visible.value = false
          emit('success')
        } catch (error) {
          console.error('提交公告失败:', error)
        } finally {
          submitLoading.value = false
        }
      }
    })
  }

  defineOptions({ name: 'NoticeEditDialog' })
</script>
