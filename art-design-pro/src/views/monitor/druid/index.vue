<!-- Druid 数据监控页面 -->
<template>
  <div class="druid-container art-full-height" v-loading="loading">
    <iframe
      :src="url"
      frameborder="0"
      width="100%"
      height="100%"
      scrolling="auto"
      @load="loading = false"
    ></iframe>
  </div>
</template>

<script setup lang="ts">
  defineOptions({ name: 'Druid' })

  const loading = ref(true)
  // 获取 API 基础路径
  const baseUrl = import.meta.env.VITE_API_URL || '/api'
  const url = ref(`${baseUrl}/druid/index.html`)

  /**
   * 页面加载完成后关闭 loading
   */
  onMounted(() => {
    // 延迟 10 秒强制关闭 loading，防止 iframe 加载失败导致一直转圈
    setTimeout(() => {
      loading.value = false
    }, 10000)
  })
</script>

<style scoped lang="scss">
  .druid-container {
    overflow: hidden;
    background-color: var(--art-bg-color);
    border-radius: 8px;
  }
</style>
