<template>
  <div class="workflow-done-page art-full-height flex flex-col p-3 overflow-hidden">
    <ArtSearchBar
      :key="business_type.length + wf_status.length"
      v-model="formFilters"
      :items="formItems"
      :showExpand="false"
      @reset="handleReset"
      @search="handleSearch"
    />

    <ElCard class="art-table-card flex-1 overflow-hidden" shadow="never">
      <ArtTableHeader
        :showZebra="false"
        :loading="loading"
        v-model:columns="columnChecks"
        @refresh="refreshData"
      />

      <ArtTable
        ref="tableRef"
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
        rowKey="instanceId"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { computed, h, onMounted, reactive, ref } from 'vue'
  import { listDone, mergeWorkflowBusinessTypeOptions, type WorkflowTaskItem } from '@/api/workflow/task'
  import DictTag from '@/components/DictTag/index.vue'
  import { useTable } from '@/hooks/core/useTable'
  import { useDict } from '@/utils/dict'

  defineOptions({ name: 'WorkflowDone' })

  const { business_type, wf_status } = useDict('business_type', 'wf_status')
  const workflowBusinessTypeOptions = computed(() =>
    mergeWorkflowBusinessTypeOptions(business_type.value as Array<{ label: string; value: string }>)
  )

  const tableRef = ref()

  const formFilters = reactive<{
    bizNo: string
    bizType?: string
    wfStatus?: string
  }>({
    bizNo: '',
    bizType: undefined,
    wfStatus: undefined
  })

  const formItems = computed(() => [
    {
      label: '业务单号',
      key: 'bizNo',
      type: 'input',
      props: { placeholder: '请输入业务单号', clearable: true }
    },
    {
      label: '业务类型',
      key: 'bizType',
      type: 'select',
      props: {
        placeholder: '请选择业务类型',
        clearable: true,
        options: workflowBusinessTypeOptions.value
      }
    },
    {
      label: '流程状态',
      key: 'wfStatus',
      type: 'select',
      props: {
        placeholder: '请选择流程状态',
        clearable: true,
        options: wf_status.value
      }
    }
  ])

  const resolveBizNo = (task?: Partial<WorkflowTaskItem>) => task?.bizNo || task?.businessId || '--'
  const resolveBizType = (task?: Partial<WorkflowTaskItem>) => task?.bizType || task?.businessType
  const resolveWfStatus = (task?: Partial<WorkflowTaskItem>) => task?.wfStatus || task?.status

  const formatApproverName = (task?: Partial<WorkflowTaskItem>) => {
    if (task?.approverName) {
      return task.approverName
    }
    if (task?.approverId != null) {
      return `用户#${task.approverId}`
    }
    return '--'
  }

  const {
    columns,
    columnChecks,
    data,
    loading,
    pagination,
    searchParams,
    resetSearchParams,
    handleSizeChange,
    handleCurrentChange,
    refreshData,
    getData
  } = useTable({
    core: {
      apiFn: listDone,
      columnsFactory: () => [
        { type: 'index', label: '序号', width: 60, align: 'center' },
        {
          prop: 'bizNo',
          label: '业务单号',
          minWidth: 150,
          formatter: (row: WorkflowTaskItem) => resolveBizNo(row)
        },
        {
          prop: 'bizType',
          label: '业务类型',
          width: 160,
          align: 'center',
          formatter: (row: WorkflowTaskItem) =>
            h(DictTag, { options: workflowBusinessTypeOptions.value, value: resolveBizType(row) })
        },
        {
          prop: 'approverName',
          label: '处理人',
          width: 120,
          align: 'center',
          formatter: (row: WorkflowTaskItem) => formatApproverName(row)
        },
        { prop: 'currentNode', label: '当前节点', width: 140, align: 'center' },
        {
          prop: 'wfStatus',
          label: '流程状态',
          width: 110,
          align: 'center',
          formatter: (row: WorkflowTaskItem) =>
            h(DictTag, { options: wf_status.value, value: resolveWfStatus(row) })
        },
        {
          prop: 'comment',
          label: '审批意见',
          minWidth: 220,
          formatter: (row: WorkflowTaskItem) => row.comment || '--'
        },
        { prop: 'createTime', label: '处理时间', width: 170, align: 'center' }
      ]
    }
  })

  const handleReset = () => {
    Object.assign(formFilters, {
      bizNo: '',
      bizType: undefined,
      wfStatus: undefined
    })
    resetSearchParams()
  }

  const handleSearch = () => {
    Object.assign(searchParams, formFilters)
    getData()
  }

  onMounted(() => {
    void workflowBusinessTypeOptions.value
    void wf_status.value
  })
</script>

<style lang="scss" scoped>
  .workflow-done-page {
    padding: 12px;
  }
</style>
