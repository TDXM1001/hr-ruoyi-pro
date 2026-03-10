<!-- 定时任务编辑弹窗 -->
<template>
  <ElDialog
    v-model="visible"
    :title="dialogType === 'add' ? '新增任务' : '修改任务'"
    width="800px"
    append-to-body
  >
    <ElForm ref="formRef" :model="form" :rules="rules" label-width="120px">
      <ElRow>
        <ElCol :span="12">
          <ElFormItem label="任务名称" prop="jobName">
            <ElInput v-model="form.jobName" placeholder="请输入任务名称" />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="任务组名" prop="jobGroup">
            <ElSelect v-model="form.jobGroup" placeholder="请选择任务组名">
              <ElOption label="默认" value="DEFAULT" />
              <ElOption label="系统" value="SYSTEM" />
            </ElSelect>
          </ElFormItem>
        </ElCol>
        <ElCol :span="24">
          <ElFormItem prop="invokeTarget">
            <template #label>
              <span>
                调用目标
                <ElTooltip placement="top">
                  <template #content>
                    Bean调用示例：ryTask.ryParams('ry')
                    <br />Class类调用示例：com.ruoyi.quartz.task.RyTask.ryParams('ry')
                    <br />参数说明：支持字符串，布尔类型，长整型，浮点型，整型
                  </template>
                  <ElIcon><QuestionFilled /></ElIcon>
                </ElTooltip>
              </span>
            </template>
            <ElInput v-model="form.invokeTarget" placeholder="请输入调用目标字符串" />
          </ElFormItem>
        </ElCol>
        <ElCol :span="24">
          <ElFormItem label="Cron表达式" prop="cronExpression">
            <ElInput v-model="form.cronExpression" placeholder="请输入Cron执行表达式">
              <template #append>
                <ElButton @click="handleShowCron">生成表达式</ElButton>
              </template>
            </ElInput>
          </ElFormItem>
        </ElCol>
        <ElCol :span="24">
          <ElFormItem label="执行策略" prop="misfirePolicy">
            <ElRadioGroup v-model="form.misfirePolicy">
              <ElRadioButton :value="'1'">立即执行</ElRadioButton>
              <ElRadioButton :value="'2'">执行一次</ElRadioButton>
              <ElRadioButton :value="'3'">放弃执行</ElRadioButton>
            </ElRadioGroup>
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="是否并发" prop="concurrent">
            <ElRadioGroup v-model="form.concurrent">
              <ElRadioButton :value="'0'">允许</ElRadioButton>
              <ElRadioButton :value="'1'">禁止</ElRadioButton>
            </ElRadioGroup>
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="状态">
            <ElRadioGroup v-model="form.status">
              <ElRadioButton :value="'0'">正常</ElRadioButton>
              <ElRadioButton :value="'1'">暂停</ElRadioButton>
            </ElRadioGroup>
          </ElFormItem>
        </ElCol>
      </ElRow>
    </ElForm>
    <template #footer>
      <div class="dialog-footer">
        <ElButton type="primary" @click="submitForm">确 定</ElButton>
        <ElButton @click="visible = false">取 消</ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { addJob, updateJob, getJob } from '@/api/monitor/job'
  import { QuestionFilled } from '@element-plus/icons-vue'
  import { type FormInstance, ElMessage } from 'element-plus'

  const props = defineProps<{
    modelValue: boolean
    dialogType: 'add' | 'edit'
    jobData?: any
  }>()

  const emit = defineEmits(['update:modelValue', 'success'])

  const visible = computed({
    get: () => props.modelValue,
    set: (val) => emit('update:modelValue', val)
  })

  const formRef = ref<FormInstance>()
  const form = ref<any>({
    jobId: undefined,
    jobName: undefined,
    jobGroup: 'DEFAULT',
    invokeTarget: undefined,
    cronExpression: undefined,
    misfirePolicy: '1',
    concurrent: '1',
    status: '0'
  })

  const rules = {
    jobName: [{ required: true, message: '任务名称不能为空', trigger: 'blur' }],
    invokeTarget: [{ required: true, message: '调用目标字符串不能为空', trigger: 'blur' }],
    cronExpression: [{ required: true, message: 'Cron执行表达式不能为空', trigger: 'blur' }]
  }

  watch(
    () => props.jobData,
    (val) => {
      if (val) {
        form.value = { ...val }
      } else {
        form.value = {
          jobId: undefined,
          jobName: undefined,
          jobGroup: 'DEFAULT',
          invokeTarget: undefined,
          cronExpression: undefined,
          misfirePolicy: '1',
          concurrent: '1',
          status: '0'
        }
      }
    },
    { immediate: true }
  )

  /**
   * 提交表单
   */
  const submitForm = async () => {
    await formRef.value?.validate()
    if (form.value.jobId !== undefined) {
      await updateJob(form.value)
      ElMessage.success('修改成功')
    } else {
      await addJob(form.value)
      ElMessage.success('新增成功')
    }
    visible.value = false
    emit('success')
  }

  const handleShowCron = () => {
    ElMessage.info('Cron生成器集成开发中...')
  }
</script>
