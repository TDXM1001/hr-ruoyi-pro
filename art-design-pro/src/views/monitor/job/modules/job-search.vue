<!-- 定时任务搜索组件 -->
<template>
  <ArtSearchBar v-model="model" :items="searchItems" @search="handleSearch" @reset="handleReset">
  </ArtSearchBar>
</template>

<script setup lang="ts">
  import ArtSearchBar, { SearchFormItem } from '@/components/core/forms/art-search-bar/index.vue'

  const props = defineProps<{
    modelValue: any
  }>()

  const emit = defineEmits(['update:modelValue', 'search', 'reset'])

  const model = computed({
    get: () => props.modelValue,
    set: (val) => emit('update:modelValue', val)
  })

  // 搜索项配置
  const searchItems: SearchFormItem[] = [
    {
      key: 'jobName',
      label: '任务名称',
      type: 'input',
      props: {
        placeholder: '请输入任务名称',
        clearable: true
      }
    },
    {
      key: 'jobGroup',
      label: '任务组名',
      type: 'select',
      props: {
        placeholder: '请选择任务组名',
        clearable: true,
        options: [
          { label: '默认', value: 'DEFAULT' },
          { label: '系统', value: 'SYSTEM' }
        ]
      }
    },
    {
      key: 'status',
      label: '任务状态',
      type: 'select',
      props: {
        placeholder: '请选择任务状态',
        clearable: true,
        options: [
          { label: '正常', value: '0' },
          { label: '暂停', value: '1' }
        ]
      }
    }
  ]

  const handleSearch = () => {
    emit('search', model.value)
  }

  const handleReset = () => {
    emit('reset')
  }
</script>
