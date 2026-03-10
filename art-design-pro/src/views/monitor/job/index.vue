<!-- 定时任务管理页面 -->
<template>
  <div class="art-full-height">
    <JobSearch
      v-show="showSearchBar"
      v-model="searchForm"
      @search="handleSearch"
      @reset="resetSearchParams"
    ></JobSearch>

    <ElCard
      class="art-table-card"
      shadow="never"
      :style="{ 'margin-top': showSearchBar ? '12px' : '0' }"
    >
      <ArtTableHeader
        v-model:columns="columnChecks"
        v-model:showSearchBar="showSearchBar"
        :loading="loading"
        @refresh="refreshData"
      >
        <template #left>
          <ElSpace wrap>
            <ElButton type="primary" plain icon="ri:add-line" @click="handleAdd" v-ripple>新增</ElButton>
            <ElButton type="danger" plain icon="ri:delete-bin-line" :disabled="!multiple" @click="handleDelete" v-ripple>
              删除
            </ElButton>
            <ElButton type="warning" plain icon="ri:download-line" @click="handleExport" v-ripple>导出</ElButton>
            <ElButton type="info" plain icon="ri:file-list-2-line" @click="handleJobLog" v-ripple>任务日志</ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>

      <!-- 表格 -->
      <ArtTable
        v-model:selection="selection"
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      >
      </ArtTable>
    </ElCard>

    <!-- 任务编辑弹窗 -->
    <JobEditDialog
      v-model="dialogVisible"
      :dialog-type="dialogType"
      :job-data="currentJobData"
      @success="refreshData"
    />
  </div>
</template>

<script setup lang="ts">
  import { useTable } from '@/hooks/core/useTable'
  import { listJob, delJob, changeJobStatus, runJob } from '@/api/monitor/job'
  import JobSearch from './modules/job-search.vue'
  import JobEditDialog from './modules/job-edit-dialog.vue'
  import ArtButtonMore from '@/components/core/forms/art-button-more/index.vue'
  import { ElMessageBox, ElMessage, ElSwitch, ElTag } from 'element-plus'

  defineOptions({ name: 'Job' })

  // 搜索表单
  const searchForm = ref({
    jobName: undefined,
    jobGroup: undefined,
    status: undefined
  })

  const showSearchBar = ref(true)
  const dialogVisible = ref(false)
  const dialogType = ref<'add' | 'edit'>('add')
  const currentJobData = ref<any>(undefined)
  const selection = ref<any[]>([])
  const multiple = computed(() => selection.value.length === 0)

  const router = useRouter()

  const {
    columns,
    columnChecks,
    data,
    loading,
    pagination,
    getData,
    searchParams,
    resetSearchParams,
    handleSizeChange,
    handleCurrentChange,
    refreshData
  } = useTable({
    core: {
      apiFn: listJob,
      apiParams: {
        pageNum: 1,
        pageSize: 20
      },
      columnsFactory: () => [
        { type: 'selection', width: 50 },
        { prop: 'jobId', label: '任务编号', width: 80 },
        { prop: 'jobName', label: '任务名称', minWidth: 120, showOverflowTooltip: true },
        { prop: 'jobGroup', label: '任务组名', width: 100 },
        { prop: 'invokeTarget', label: '调用目标', minWidth: 150, showOverflowTooltip: true },
        { prop: 'cronExpression', label: 'Cron 表达式', minWidth: 120 },
        {
          prop: 'status',
          label: '状态',
          width: 80,
          formatter: (row: any) =>
            h(ElSwitch, {
              modelValue: row.status === '0',
              activeValue: true,
              inactiveValue: false,
              onChange: (val: boolean) => handleStatusChange(row, val)
            })
        },
        { prop: 'createTime', label: '创建时间', width: 180 },
        {
          prop: 'operation',
          label: '操作',
          width: 80,
          fixed: 'right',
          formatter: (row: any) =>
            h(ArtButtonMore, {
              list: [
                { key: 'edit', label: '修改', icon: 'ri:edit-line' },
                { key: 'run', label: '执行一次', icon: 'ri:play-line' },
                { key: 'detail', label: '详情', icon: 'ri:contacts-line' },
                { key: 'log', label: '调度日志', icon: 'ri:file-list-line' },
                { key: 'delete', label: '删除', icon: 'ri:delete-bin-line', color: '#f56c6c' }
              ],
              onClick: (item: any) => handleCommand(item, row)
            })
        }
      ]
    }
  })

  /**
   * 搜索处理
   */
  const handleSearch = (params: any) => {
    Object.assign(searchParams, params)
    getData()
  }

  /**
   * 状态修改
   */
  const handleStatusChange = (row: any, val: boolean) => {
    const status = val ? '0' : '1'
    const text = status === '0' ? '启用' : '暂停'
    ElMessageBox.confirm(`确认要"${text}""${row.jobName}"任务吗?`, '系统提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(async () => {
        await changeJobStatus(row.jobId, status)
        ElMessage.success(text + '成功')
        row.status = status
      })
      .catch(() => {
        // 恢复开关状态
      })
  }

  /**
   * 按钮操作
   */
  const handleCommand = (item: any, row: any) => {
    switch (item.key) {
      case 'edit':
        handleUpdate(row)
        break
      case 'run':
        handleRun(row)
        break
      case 'detail':
        handleView(row)
        break
      case 'log':
        handleJobLog(row)
        break
      case 'delete':
        handleDelete(row)
        break
    }
  }

  const handleAdd = () => {
    dialogType.value = 'add'
    currentJobData.value = undefined
    dialogVisible.value = true
  }

  const handleUpdate = (row: any) => {
    dialogType.value = 'edit'
    currentJobData.value = row
    dialogVisible.value = true
  }

  const handleRun = (row: any) => {
    ElMessageBox.confirm(`确认要立即执行一次"${row.jobName}"任务吗?`, '提示', {
      type: 'warning'
    }).then(async () => {
      await runJob(row.jobId, row.jobGroup)
      ElMessage.success('执行成功')
    })
  }

  const handleView = (row: any) => {
    // 详情逻辑
  }

  const handleDelete = (row?: any) => {
    const ids = row?.jobId || selection.value.map((i) => i.jobId).join(',')
    ElMessageBox.confirm('确认删除选中的任务吗?', '警告', {
      type: 'warning'
    }).then(async () => {
      await delJob(ids)
      ElMessage.success('删除成功')
      refreshData()
    })
  }

  const handleExport = () => {
    // 导出逻辑
  }

  const handleJobLog = (row?: any) => {
    // 跳转到日志页面
    const jobId = row?.jobId || 0
    router.push('/monitor/job-log/index/' + jobId)
  }
</script>
