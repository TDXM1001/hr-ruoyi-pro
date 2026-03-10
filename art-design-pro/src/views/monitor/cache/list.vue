<!-- 缓存列表页面 -->
<template>
  <div class="cache-list-container art-full-height p-3">
    <ElRow :gutter="12" class="flex-1 overflow-hidden" style="margin: 0">
      <!-- 缓存名称列表 -->
      <ElCol :span="7" class="h-full overflow-hidden p-1">
        <ElCard shadow="never" class="art-full-height flex-card">
          <template #header>
            <div class="flex items-center justify-between">
              <span class="font-bold">缓存列表</span>
              <ElButton type="primary" link icon="ri:refresh-line" @click="getCacheNames"></ElButton>
            </div>
          </template>
          <ElScrollbar class="list-scrollbar">
            <div
              v-for="(item, index) in cacheNames"
              :key="index"
              class="list-item"
              :class="{ active: activeCacheName === item.cacheName }"
              @click="getCacheKeys(item.cacheName)"
            >
              <div class="flex items-center overflow-hidden">
                <ElIcon class="mr-2"><Collection /></ElIcon>
                <span class="truncate">{{ item.cacheName }}</span>
              </div>
              <span class="remark truncate ml-2">{{ item.remark }}</span>
            </div>
          </ElScrollbar>
        </ElCard>
      </ElCol>

      <!-- 键名列表 -->
      <ElCol :span="8" class="h-full overflow-hidden p-1">
        <ElCard shadow="never" class="art-full-height flex-card">
          <template #header>
            <div class="flex items-center justify-between">
              <span class="font-bold">键名列表</span>
              <ElButton
                type="primary"
                link
                icon="ri:refresh-line"
                @click="getCacheKeys(activeCacheName)"
              ></ElButton>
            </div>
          </template>
          <div class="p-2 border-b border-gray-100">
            <ElInput
              v-model="keySearch"
              placeholder="搜索键名"
              clearable
              prefix-icon="ri:search-line"
              size="small"
            />
          </div>
          <ElScrollbar class="list-scrollbar">
            <div
              v-for="(item, index) in filteredCacheKeys"
              :key="index"
              class="list-item"
              :class="{ active: activeCacheKey === item }"
              @click="getCacheValue(item)"
            >
              <span class="truncate flex-1">{{ item }}</span>
              <ElButton
                type="danger"
                link
                icon="ri:delete-bin-line"
                @click.stop="handleClearCacheKey(item)"
              ></ElButton>
            </div>
            <div v-if="filteredCacheKeys.length === 0" class="empty-text"> 无数据 </div>
          </ElScrollbar>
        </ElCard>
      </ElCol>

      <!-- 键值详情 -->
      <ElCol :span="9" class="h-full overflow-hidden p-1">
        <ElCard shadow="never" class="art-full-height flex-card">
          <template #header>
            <div class="flex items-center justify-between">
              <span class="font-bold">缓存内容</span>
              <ElButton type="danger" link icon="ri:delete-bin-line" @click="handleClearAll"
                >清理全部</ElButton
              >
            </div>
          </template>
          <div v-if="cacheValue.cacheKey" class="p-4 flex-1 overflow-auto">
            <ElForm label-width="80px" label-position="top">
              <ElFormItem label="缓存名称">
                <ElInput :model-value="cacheValue.cacheName" readonly />
              </ElFormItem>
              <ElFormItem label="缓存键名">
                <ElInput :model-value="cacheValue.cacheKey" readonly />
              </ElFormItem>
              <ElFormItem label="缓存内容">
                <div class="code-container">
                  <pre><code>{{ formatJson(cacheValue.cacheValue) || cacheValue.cacheValue }}</code></pre>
                </div>
              </ElFormItem>
            </ElForm>
          </div>
          <div v-else class="flex items-center justify-center flex-1 text-gray-400">
            <div class="text-center">
              <ElIcon size="48" color="#d1d5db"><InfoFilled /></ElIcon>
              <p class="mt-2">请从左侧选择一个缓存项查看详情</p>
            </div>
          </div>
        </ElCard>
      </ElCol>
    </ElRow>
  </div>
</template>

<script setup lang="ts">
  import {
    listCacheName,
    listCacheKey,
    getCacheValue as getCacheValueApi,
    clearCacheKey,
    clearCacheName,
    clearCacheAll
  } from '@/api/monitor/cache'
  import { Collection, InfoFilled } from '@element-plus/icons-vue'
  import { ElMessageBox, ElMessage } from 'element-plus'

  defineOptions({ name: 'CacheList' })

  const cacheNames = ref<any[]>([])
  const cacheKeys = ref<string[]>([])
  const cacheValue = ref<any>({})
  const activeCacheName = ref('')
  const activeCacheKey = ref('')
  const keySearch = ref('')

  const filteredCacheKeys = computed(() => {
    return cacheKeys.value.filter((key) => key.toLowerCase().includes(keySearch.value.toLowerCase()))
  })

  // 格式化输出 JSON
  const formatJson = (val: string) => {
    try {
      if (typeof val === 'string') {
        const obj = JSON.parse(val)
        return JSON.stringify(obj, null, 2)
      }
      return JSON.stringify(val, null, 2)
    } catch (e) {
      return val
    }
  }

  const getCacheNames = async () => {
    try {
      const res = await listCacheName()
      // 如果返回的是 res.data，则取 res.data；否则认为 res 本身就是数组
      cacheNames.value = (res as any).data || res || []
    } catch (e) {
      console.error(e)
    }
  }

  const getCacheKeys = async (name: string) => {
    if (!name) return
    activeCacheName.value = name
    activeCacheKey.value = ''
    cacheValue.value = {}
    try {
      const res = await listCacheKey(name)
      cacheKeys.value = (res as any).data || res || []
    } catch (e) {
      console.error(e)
    }
  }

  const getCacheValue = async (key: string) => {
    activeCacheKey.value = key
    try {
      const res = await getCacheValueApi(activeCacheName.value, key)
      cacheValue.value = (res as any).data || res || {}
    } catch (e) {
      console.error(e)
    }
  }

  const handleClearCacheKey = (key: string) => {
    ElMessageBox.confirm(`确认要清理缓存键名"${key}"吗?`, '提示', {
      type: 'warning'
    }).then(async () => {
      await clearCacheKey(key)
      ElMessage.success('清理成功')
      getCacheKeys(activeCacheName.value)
    })
  }

  const handleClearAll = () => {
    ElMessageBox.confirm('确认要清理全部缓存吗?', '警告', {
      type: 'warning'
    }).then(async () => {
      await clearCacheAll()
      ElMessage.success('清理成功')
      getCacheNames()
      cacheKeys.value = []
      cacheValue.value = {}
    })
  }

  onMounted(() => {
    getCacheNames()
  })
</script>

<style scoped lang="scss">
  .cache-list-container {
    background-color: var(--art-bg-color);

    // 确保 ElRow 填满并在溢出时隐藏
    :deep(.el-row) {
      height: 100%;
      display: flex;
      flex-wrap: nowrap;
    }

    .flex-card {
      display: flex;
      flex-direction: column;

      :deep(.el-card__header) {
        padding: 12px 16px;
      }

      :deep(.el-card__body) {
        flex: 1;
        display: flex;
        flex-direction: column;
        padding: 0;
        overflow: hidden;
      }
    }

    .list-scrollbar {
      flex: 1;
    }

    .list-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 12px 16px;
      font-size: 13px;
      cursor: pointer;
      transition: all 0.2s;
      border-bottom: 1px solid var(--el-border-color-lighter);

      &:hover {
        background-color: var(--el-fill-color-light);
      }

      &.active {
        color: var(--el-color-primary);
        background-color: var(--el-color-primary-light-9);
        border-right: 3px solid var(--el-color-primary);
      }

      .remark {
        font-size: 12px;
        color: var(--el-text-color-secondary);
        max-width: 40%;
      }
    }

    .empty-text {
      padding: 20px;
      text-align: center;
      color: var(--el-text-color-secondary);
      font-size: 12px;
    }

    .code-container {
      padding: 10px;
      background-color: var(--el-fill-color-darker);
      border-radius: 4px;
      margin-top: 8px;

      pre {
        margin: 0;
        white-space: pre-wrap;
        word-wrap: break-word;

        code {
          color: #abb2bf;
          font-family: Consolas, Monaco, monospace;
          font-size: 13px;
        }
      }
    }
  }

  .h-full {
    height: 100%;
  }
</style>
