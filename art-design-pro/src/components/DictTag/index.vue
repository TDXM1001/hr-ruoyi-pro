<template>
  <div class="inline-flex items-center gap-2">
    <template v-for="(item, index) in options">
      <template v-if="values.includes(item.value)">
        <span
          v-if="
            (item.listClass == 'default' || item.listClass == '') &&
            (item.cssClass == '' || item.cssClass == null)
          "
          :key="item.value"
          :index="index"
          :class="item.cssClass"
          >{{ item.label + ' ' }}</span
        >
        <el-tag
          v-else
          :disable-transitions="true"
          :key="item.value + ''"
          :index="index"
          :type="
            ['success', 'info', 'warning', 'danger'].includes(item.listClass) ? item.listClass : ''
          "
          :class="item.cssClass"
        >
          {{ item.label + ' ' }}
        </el-tag>
      </template>
    </template>
    <template v-if="unmatch && showValue">
      {{ handleArray(unmatchArray) }}
    </template>
  </div>
</template>

<script setup lang="ts">
  import { computed } from 'vue'

  const props = defineProps({
    // 数据
    options: {
      type: Array as () => any[],
      default: () => []
    },
    // 当前的值
    value: {
      type: [Number, String, Array],
      default: ''
    },
    // 当未找到匹配的数据时，显示value
    showValue: {
      type: Boolean,
      default: true
    },
    separator: {
      type: String,
      default: ','
    }
  })

  const values = computed(() => {
    if (props.value === null || typeof props.value === 'undefined' || props.value === '') return []
    return Array.isArray(props.value)
      ? props.value.map((item) => '' + item)
      : String(props.value).split(props.separator)
  })

  const unmatch = computed(() => {
    if (props.options?.length === 0 || values.value.length === 0) return false
    // 传入的值找不到匹配的字典数据
    let unmatch = false
    values.value.forEach((item) => {
      if (!props.options.some((v) => v.value == item)) {
        unmatch = true
      }
    })
    return unmatch
  })

  const unmatchArray = computed(() => {
    // 记录未匹配的项
    if (props.options?.length === 0 || values.value.length === 0) return []
    let array: string[] = []
    values.value.forEach((item) => {
      if (!props.options.some((v) => v.value == item)) {
        array.push(item)
      }
    })
    return array
  })

  const handleArray = (array: string[]) => {
    if (array.length === 0) return ''
    return array.reduce((pre, cur) => pre + ' ' + cur)
  }
</script>
