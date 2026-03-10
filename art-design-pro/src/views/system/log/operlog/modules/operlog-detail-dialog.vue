<!-- 操作日志详情弹窗 (路径: src/views/system/log/operlog/modules/operlog-detail-dialog.vue) -->
<template>
  <ElDialog
    v-model="visible"
    title="操作日志详情"
    width="700px"
    append-to-body
    destroy-on-close
  >
    <ElForm :model="form" label-width="100px" class="operlog-detail-form">
      <ElRow>
        <ElCol :span="12">
          <ElFormItem label="操作模块：">{{ form.title }} / {{ typeFormat(form) }}</ElFormItem>
          <ElFormItem label="登录信息：">{{ form.operName }} / {{ form.operIp }} / {{ form.operLocation }}</ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="请求地址：">{{ form.operUrl }}</ElFormItem>
          <ElFormItem label="请求方式：">{{ form.requestMethod }}</ElFormItem>
        </ElCol>
        <ElCol :span="24">
          <ElFormItem label="操作方法：">{{ form.method }}</ElFormItem>
        </ElCol>
        <ElCol :span="24">
          <ElFormItem label="请求参数：">{{ form.operParam }}</ElFormItem>
        </ElCol>
        <ElCol :span="24">
          <ElFormItem label="返回参数：">{{ form.jsonResult }}</ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="操作状态：">
            <div v-if="form.status === 0">正常无异常</div>
            <div v-else-if="form.status === 1">异常报错</div>
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="操作时间：">{{ form.operTime }}</ElFormItem>
        </ElCol>
        <ElCol :span="24" v-if="form.status === 1">
          <ElFormItem label="异常名称：">{{ form.errorMsg }}</ElFormItem>
        </ElCol>
      </ElRow>
    </ElForm>
    <template #footer>
      <div class="dialog-footer">
        <ElButton @click="visible = false">关 闭</ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { ref, watch } from 'vue'

  const props = defineProps({
    modelValue: Boolean,
    data: {
      type: Object,
      default: () => ({})
    },
    typeOptions: {
      type: Array,
      default: () => []
    }
  })

  const emit = defineEmits(['update:modelValue'])

  const visible = ref(false)
  const form = ref<any>({})

  watch(
    () => props.modelValue,
    (val) => {
      visible.value = val
    }
  )

  watch(
    () => visible.value,
    (val) => {
      emit('update:modelValue', val)
    }
  )

  watch(
    () => props.data,
    (val) => {
      form.value = val || {}
    },
    { immediate: true }
  )

  /** 操作类型字典转义 */
  const typeFormat = (row: any) => {
    const option: any = props.typeOptions.find((item: any) => item.value === '' + row.businessType)
    return option ? option.label : row.businessType
  }
</script>

<style lang="scss" scoped>
  .operlog-detail-form {
    :deep(.el-form-item) {
      margin-bottom: 10px;
    }
    :deep(.el-form-item__label) {
      font-weight: bold;
    }
  }
</style>
