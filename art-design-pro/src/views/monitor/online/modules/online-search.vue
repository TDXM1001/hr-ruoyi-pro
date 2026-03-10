<!-- 在线用户搜索组件 -->
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
      key: 'userName',
      label: '用户名称',
      type: 'input',
      props: {
        placeholder: '请输入用户名称',
        clearable: true
      }
    },
    {
      key: 'ipaddr',
      label: '登录地址',
      type: 'input',
      props: {
        placeholder: '请输入登录地址',
        clearable: true
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
