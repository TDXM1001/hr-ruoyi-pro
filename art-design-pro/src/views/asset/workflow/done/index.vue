<template>
  <div class="workflow-done-page art-full-height flex flex-col p-3 overflow-hidden">
    <ArtSearchBar
      :key="business_type.length"
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
  import { ref, reactive, computed, onMounted, h } from 'vue'
  import { ElButton } from 'element-plus'
  import { listDone } from '@/api/workflow/task'
  import { useTable } from '@/hooks/core/useTable'
  import { useDict } from '@/utils/dict'
  import DictTag from '@/components/DictTag/index.vue'

  defineOptions({ name: 'WorkflowDone' })

  const { business_type, wf_status } = useDict('business_type', 'wf_status')

  // 查询参数
  const formFilters = reactive({
    businessId: '',
    businessType: undefined
  })

  const formItems = computed(() => [
    {
      label: '业务编号',
      key: 'businessId',
      type: 'input',
      props: { placeholder: '请输入业务编号', clearable: true }
    },
    {
      label: '业务类型',
      key: 'businessType',
      type: 'select',
      props: {
        placeholder: '业务类型',
        clearable: true,
        options: business_type.value
      }
    }
  ])

  // 表格逻辑
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
        { prop: 'businessId', label: '业务单据编号', minWidth: 150 },
        {
          prop: 'businessType',
          label: '业务类型',
          width: 120,
          align: 'center',
          formatter: (row: any) =>
            h(DictTag, { options: business_type.value, value: row.businessType })
        },
        { prop: 'currentNode', label: '审批节点', width: 120, align: 'center' },
        {
          prop: 'status',
          label: '状态',
          width: 100,
          align: 'center',
          formatter: (row: any) => h(DictTag, { options: wf_status.value, value: row.status })
        },
        { prop: 'createTime', label: '处理时间', width: 170, align: 'center' },
        {
          prop: 'operation',
          label: '操作',
          width: 120,
          align: 'right',
          formatter: (row: any) => {
            return h('div', { class: 'flex justify-end' }, [
              h(
                ElButton,
                {
                  type: 'primary',
                  link: true,
                  onClick: () => handleView(row)
                },
                () => '查看详情'
              )
            ])
          }
        }
      ]
    }
  })

  const tableRef = ref()

  const handleReset = () => {
    Object.assign(formFilters, { businessId: '', businessType: undefined })
    resetSearchParams()
  }

  const handleSearch = () => {
    Object.assign(searchParams, formFilters)
    getData()
  }

  const handleView = (row: any) => {
    // 这里可以后续扩展查看详情逻辑
    console.log('查看详情:', row)
  }

  onMounted(() => {
    void business_type.value
    void wf_status.value
  })
</script>

<style lang="scss" scoped>
  .workflow-done-page {
    padding: 12px;
  }
</style>
