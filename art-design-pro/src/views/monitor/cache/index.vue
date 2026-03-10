<!-- 缓存监控页面 -->
<template>
  <div class="cache-container art-full-height" v-loading="loading">
    <div class="p-4 overflow-auto art-full-height">
      <!-- 统计区域 -->
      <ElRow :gutter="16">
        <ElCol :span="24">
          <ElCard shadow="never">
            <template #header>
              <span>基本信息</span>
            </template>
            <ElDescriptions :column="3" border>
              <ElDescriptionsItem label="Redis版本">{{ cacheData.info?.redis_version }}</ElDescriptionsItem>
              <ElDescriptionsItem label="运行模式">{{
                cacheData.info?.redis_mode === 'standalone' ? '单机' : '集群'
              }}</ElDescriptionsItem>
              <ElDescriptionsItem label="端口">{{ cacheData.info?.tcp_port }}</ElDescriptionsItem>
              <ElDescriptionsItem label="客户端数">{{ cacheData.info?.connected_clients }}</ElDescriptionsItem>
              <ElDescriptionsItem label="运行天数">{{ cacheData.info?.uptime_in_days }} 天</ElDescriptionsItem>
              <ElDescriptionsItem label="使用内存">{{ cacheData.info?.used_memory_human }}</ElDescriptionsItem>
              <ElDescriptionsItem label="使用CPU">{{
                parseFloat(cacheData.info?.used_cpu_user_children || '0').toFixed(2)
              }}%</ElDescriptionsItem>
              <ElDescriptionsItem label="内存配置">{{ cacheData.info?.maxmemory_human }}</ElDescriptionsItem>
              <ElDescriptionsItem label="AOF是否开启">{{
                cacheData.info?.aof_enabled === '0' ? '否' : '是'
              }}</ElDescriptionsItem>
              <ElDescriptionsItem label="RDB是否成功">{{
                cacheData.info?.rdb_last_bgsave_status
              }}</ElDescriptionsItem>
              <ElDescriptionsItem label="Key数量">{{ cacheData.dbSize }}</ElDescriptionsItem>
              <ElDescriptionsItem label="网络入口/出口"
                >{{ cacheData.info?.instantaneous_input_kbps }}kps /
                {{ cacheData.info?.instantaneous_output_kbps }}kps</ElDescriptionsItem
              >
            </ElDescriptions>
          </ElCard>
        </ElCol>
      </ElRow>

      <!-- 图表区域 -->
      <ElRow :gutter="16" class="mt-4">
        <ElCol :xs="24" :md="12">
          <ElCard shadow="never">
            <template #header>
              <span>命令统计</span>
            </template>
            <div ref="commandChartRef" class="chart-box"></div>
          </ElCard>
        </ElCol>

        <ElCol :xs="24" :md="12">
          <ElCard shadow="never">
            <template #header>
              <span>内存消耗</span>
            </template>
            <div ref="memoryChartRef" class="chart-box"></div>
          </ElCard>
        </ElCol>
      </ElRow>
    </div>
  </div>
</template>

<script setup lang="ts">
  import * as echarts from 'echarts'
  import { getCache } from '@/api/monitor/cache'

  defineOptions({ name: 'Cache' })

  const loading = ref(true)
  const cacheData = ref<any>({})

  const commandChartRef = ref<HTMLElement>()
  const memoryChartRef = ref<HTMLElement>()

  let commandChart: echarts.ECharts | null = null
  let memoryChart: echarts.ECharts | null = null

  /**
   * 获取缓存信息
   */
  const fetchCacheData = async () => {
    try {
      loading.value = true
      const res = (await getCache()) as any
      cacheData.value = res.data

      // 设置命令统计图表
      if (commandChart) {
        commandChart.setOption({
          tooltip: {
            trigger: 'item',
            formatter: '{a} <br/>{b} : {c} ({d}%)'
          },
          series: [
            {
              name: '命令',
              type: 'pie',
              roseType: 'radius',
              radius: [15, 95],
              center: ['50%', '38%'],
              data: cacheData.value.commandStats,
              animationEasing: 'cubicInOut',
              animationDuration: 1000
            }
          ]
        })
      }

      // 设置内存图表
      if (memoryChart) {
        memoryChart.setOption({
          tooltip: {
            formatter: '{b} <br/>{a} : ' + cacheData.value.info?.used_memory_human
          },
          series: [
            {
              name: '峰值',
              type: 'gauge',
              min: 0,
              max: 1000,
              progress: {
                show: true
              },
              detail: {
                valueAnimation: true,
                formatter: cacheData.value.info?.used_memory_human
              },
              data: [
                {
                  value: parseFloat(cacheData.value.info?.used_memory_human || '0'),
                  name: '内存使用'
                }
              ]
            }
          ]
        })
      }
    } catch (error) {
      console.error('获取缓存监控数据失败', error)
    } finally {
      loading.value = false
    }
  }

  onMounted(() => {
    if (commandChartRef.value) commandChart = echarts.init(commandChartRef.value)
    if (memoryChartRef.value) memoryChart = echarts.init(memoryChartRef.value)

    fetchCacheData()
  })

  // 监听窗口缩放
  window.addEventListener('resize', () => {
    commandChart?.resize()
    memoryChart?.resize()
  })

  onBeforeUnmount(() => {
    commandChart?.dispose()
    memoryChart?.dispose()
  })
</script>

<style scoped lang="scss">
  .cache-container {
    background-color: var(--art-bg-color);
  }

  .chart-box {
    height: 350px;
  }
</style>
