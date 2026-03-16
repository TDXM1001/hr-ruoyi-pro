<!-- 服务监控页面 -->
<template>
  <div class="server-container art-full-height" v-loading="loading">
    <div class="p-4 overflow-auto art-full-height">
      <!-- 仪表盘区域 -->
      <ElRow :gutter="16">
        <ElCol :xs="24" :sm="12" :md="8">
          <ElCard shadow="never" class="metric-card">
            <template #header>
              <div class="card-header">
                <span>CPU 使用率</span>
                <ElTag type="primary" effect="plain">{{ serverData.cpu?.used }}%</ElTag>
              </div>
            </template>
            <div ref="cpuChartRef" class="chart-box"></div>
            <div class="metric-info">
              <div class="info-item">
                <span class="label">核心数：</span>
                <span class="value">{{ serverData.cpu?.cpuNum }} 核心</span>
              </div>
              <div class="info-item">
                <span class="label">当前负载：</span>
                <span class="value"
                  >{{ serverData.cpu?.sys }}% (系统) / {{ serverData.cpu?.used }}% (用户)</span
                >
              </div>
            </div>
          </ElCard>
        </ElCol>

        <ElCol :xs="24" :sm="12" :md="8">
          <ElCard shadow="never" class="metric-card">
            <template #header>
              <div class="card-header">
                <span>内存使用率</span>
                <ElTag type="success" effect="plain">{{ serverData.mem?.usage }}%</ElTag>
              </div>
            </template>
            <div ref="memChartRef" class="chart-box"></div>
            <div class="metric-info">
              <div class="info-item">
                <span class="label">总内存：</span>
                <span class="value">{{ serverData.mem?.total }} GB</span>
              </div>
              <div class="info-item">
                <span class="label">已使用：</span>
                <span class="value">{{ serverData.mem?.used }} GB</span>
              </div>
            </div>
          </ElCard>
        </ElCol>

        <ElCol :xs="24" :sm="12" :md="8">
          <ElCard shadow="never" class="metric-card">
            <template #header>
              <div class="card-header">
                <span>JVM 使用率</span>
                <ElTag type="warning" effect="plain">{{ serverData.jvm?.usage }}%</ElTag>
              </div>
            </template>
            <div ref="jvmChartRef" class="chart-box"></div>
            <div class="metric-info">
              <div class="info-item">
                <span class="label">JVM 总量：</span>
                <span class="value">{{ serverData.jvm?.total }} MB</span>
              </div>
              <div class="info-item">
                <span class="label">JVM 已用：</span>
                <span class="value">{{ serverData.jvm?.used }} MB</span>
              </div>
            </div>
          </ElCard>
        </ElCol>
      </ElRow>

      <!-- 详细信息区域 -->
      <ElRow :gutter="16" class="mt-4">
        <!-- 服务器信息 -->
        <ElCol :span="24">
          <ElCard shadow="never">
            <template #header>
              <span>服务器信息</span>
            </template>
            <ElDescriptions :column="2" border>
              <ElDescriptionsItem label="服务器名称">{{
                serverData.sys?.computerName
              }}</ElDescriptionsItem>
              <ElDescriptionsItem label="操作系统">{{ serverData.sys?.osName }}</ElDescriptionsItem>
              <ElDescriptionsItem label="服务器IP">{{
                serverData.sys?.computerIp
              }}</ElDescriptionsItem>
              <ElDescriptionsItem label="系统架构">{{ serverData.sys?.osArch }}</ElDescriptionsItem>
            </ElDescriptions>
          </ElCard>
        </ElCol>

        <!-- Java虚拟机信息 -->
        <ElCol :span="24" class="mt-4">
          <ElCard shadow="never">
            <template #header>
              <span>Java虚拟机信息</span>
            </template>
            <ElDescriptions :column="2" border>
              <ElDescriptionsItem label="Java名称">{{ serverData.jvm?.name }}</ElDescriptionsItem>
              <ElDescriptionsItem label="Java版本">{{
                serverData.jvm?.version
              }}</ElDescriptionsItem>
              <ElDescriptionsItem label="启动时间">{{
                serverData.jvm?.startTime
              }}</ElDescriptionsItem>
              <ElDescriptionsItem label="运行时长">{{
                serverData.jvm?.runTime
              }}</ElDescriptionsItem>
              <ElDescriptionsItem label="安装路径" :span="2">{{
                serverData.jvm?.home
              }}</ElDescriptionsItem>
              <ElDescriptionsItem label="项目路径" :span="2">{{
                serverData.sys?.userDir
              }}</ElDescriptionsItem>
            </ElDescriptions>
          </ElCard>
        </ElCol>

        <!-- 磁盘状态 -->
        <ElCol :span="24" class="mt-4">
          <ElCard shadow="never">
            <template #header>
              <span>磁盘状态</span>
            </template>
            <ElTable :data="serverData.sysFiles" border style="width: 100%">
              <ElTableColumn prop="dirName" label="盘符路径" />
              <ElTableColumn prop="sysTypeName" label="文件系统" />
              <ElTableColumn prop="typeName" label="盘符类型" />
              <ElTableColumn prop="total" label="总大小" />
              <ElTableColumn prop="free" label="可用大小" />
              <ElTableColumn prop="used" label="已用大小" />
              <ElTableColumn label="已用百分比">
                <template #default="scope">
                  <div class="flex items-center">
                    <ElProgress
                      :percentage="Number(scope.row.usage)"
                      :color="customColors"
                      :stroke-width="12"
                      style="width: 100%"
                    />
                  </div>
                </template>
              </ElTableColumn>
            </ElTable>
          </ElCard>
        </ElCol>
      </ElRow>
    </div>
  </div>
</template>

<script setup lang="ts">
  import * as echarts from 'echarts'
  import { getServer } from '@/api/monitor/server'

  defineOptions({ name: 'Server' })

  // 与后端 Server 结构保持一致，避免监控页继续落回 any / unknown。
  interface ServerCpuInfo {
    cpuNum: number
    total: number
    sys: number
    used: number
    wait: number
    free: number
  }

  interface ServerMemInfo {
    total: number
    used: number
    free: number
    usage: number
  }

  interface ServerJvmInfo {
    total: number
    max: number
    free: number
    used: number
    usage: number
    name: string
    version: string
    home: string
    startTime: string
    runTime: string
    inputArgs: string
  }

  interface ServerSysInfo {
    computerName: string
    computerIp: string
    userDir: string
    osName: string
    osArch: string
  }

  interface ServerSysFile {
    dirName: string
    sysTypeName: string
    typeName: string
    total: string
    free: string
    used: string
    usage: number
  }

  interface ServerInfo {
    cpu: ServerCpuInfo | null
    mem: ServerMemInfo | null
    jvm: ServerJvmInfo | null
    sys: ServerSysInfo | null
    sysFiles: ServerSysFile[]
  }

  type ServerResponse = {
    data: ServerInfo
  }

  const loading = ref(true)
  const serverData = ref<ServerInfo>({
    cpu: null,
    mem: null,
    jvm: null,
    sys: null,
    sysFiles: []
  })

  // 将 DOM 引用与图表实例分离，避免把实例误当成 ref.value 使用。
  const cpuChartRef = ref<HTMLElement | null>(null)
  const memChartRef = ref<HTMLElement | null>(null)
  const jvmChartRef = ref<HTMLElement | null>(null)

  let cpuChart: echarts.ECharts | null = null
  let memChart: echarts.ECharts | null = null
  let jvmChart: echarts.ECharts | null = null

  const customColors = [
    { color: '#5cb87a', percentage: 20 },
    { color: '#1989fa', percentage: 40 },
    { color: '#e6a23c', percentage: 60 },
    { color: '#f56c6c', percentage: 80 },
    { color: '#6f7ad3', percentage: 100 }
  ]

  /**
   * 初始化仪表盘配置
   */
  const getGaugeOption = (name: string, value: number, color: string) => {
    return {
      series: [
        {
          type: 'gauge',
          startAngle: 180,
          endAngle: 0,
          min: 0,
          max: 100,
          radius: '100%',
          center: ['50%', '80%'],
          axisLine: {
            lineStyle: {
              width: 8,
              color: [
                [0.3, '#67C23A'],
                [0.7, '#E6A23C'],
                [1, '#F56C6C']
              ]
            }
          },
          pointer: {
            icon: 'path://M12.8,0.7l12,40.1H0.7L12.8,0.7z',
            length: '12%',
            width: 10,
            offsetCenter: [0, '-60%'],
            itemStyle: {
              color: 'auto'
            }
          },
          axisTick: {
            show: false
          },
          splitLine: {
            show: false
          },
          axisLabel: {
            show: false
          },
          detail: {
            valueAnimation: true,
            formatter: '{value}%',
            color: 'auto',
            fontSize: 20,
            offsetCenter: [0, '-20%']
          },
          data: [
            {
              value: value,
              name: name
            }
          ]
        }
      ]
    }
  }

  /**
   * 获取服务器监控数据
   */
  const fetchServerData = async () => {
    try {
      loading.value = true
      const res = (await getServer()) as ServerResponse
      serverData.value = res.data

      // 数据返回后再更新仪表盘，避免访问空值。
      if (serverData.value.cpu) {
        cpuChart?.setOption(getGaugeOption('CPU', serverData.value.cpu.used, '#409EFF'))
      }
      if (serverData.value.mem) {
        memChart?.setOption(getGaugeOption('内存', serverData.value.mem.usage, '#67C23A'))
      }
      if (serverData.value.jvm) {
        jvmChart?.setOption(getGaugeOption('JVM', serverData.value.jvm.usage, '#E6A23C'))
      }
    } catch (error) {
      console.error('获取监控数据失败', error)
    } finally {
      loading.value = false
    }
  }

  onMounted(() => {
    if (cpuChartRef.value) {
      cpuChart = echarts.init(cpuChartRef.value)
    }
    if (memChartRef.value) {
      memChart = echarts.init(memChartRef.value)
    }
    if (jvmChartRef.value) {
      jvmChart = echarts.init(jvmChartRef.value)
    }

    fetchServerData()
  })

  // 监听窗口缩放
  window.addEventListener('resize', () => {
    cpuChart?.resize()
    memChart?.resize()
    jvmChart?.resize()
  })

  onBeforeUnmount(() => {
    cpuChart?.dispose()
    memChart?.dispose()
    jvmChart?.dispose()
  })
</script>

<style scoped lang="scss">
  .server-container {
    background-color: var(--art-bg-color);
  }

  .metric-card {
    .card-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
    }

    .chart-box {
      height: 180px;
    }

    .metric-info {
      margin-top: 10px;
      padding-top: 10px;
      border-top: 1px solid var(--el-border-color-lighter);

      .info-item {
        display: flex;
        justify-content: space-between;
        margin-bottom: 5px;
        font-size: 13px;

        .label {
          color: var(--el-text-color-secondary);
        }

        .value {
          color: var(--el-text-color-primary);
          font-weight: bold;
        }
      }
    }
  }
</style>
