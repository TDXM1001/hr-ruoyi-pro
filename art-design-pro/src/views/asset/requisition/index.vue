<template>
  <div class="asset-requisition-page art-full-height flex flex-col p-3 overflow-hidden">
    <ArtSearchBar
      :key="wf_status.length"
      v-model="formFilters"
      :items="formItems"
      :showExpand="false"
      @reset="handleReset"
      @search="handleSearch"
    />

    <ElAlert
      class="mb-3"
      title="当前分支对应的本地后端尚未提供资产归还接口，页面暂不展示归还按钮。"
      type="info"
      :closable="false"
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
        rowKey="requisitionNo"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { computed, h, onMounted, reactive, ref } from 'vue'
  import { listRequisition, type AssetRequisitionItem } from '@/api/asset/requisition'
  import { useTable } from '@/hooks/core/useTable'
  import type { ColumnOption } from '@/types/component'
  import { useDict } from '@/utils/dict'
  import DictTag from '@/components/DictTag/index.vue'
  import { canReturnAsset, mapRequisitionStatusToWorkflow } from './requisition.helper'

  defineOptions({ name: 'AssetRequisition' })

  const { wf_status } = useDict('wf_status')

  /** 本地后端未实现归还接口，因此前端不保留必然失败的操作入口。 */
  const returnApiAvailable = false

  const initialSearchState = {
    requisitionNo: '',
    assetNo: '',
    status: undefined as number | undefined
  }

  const formFilters = reactive({ ...initialSearchState })

  const formItems = computed(() => [
    {
      label: '领用业务编号',
      key: 'requisitionNo',
      type: 'input',
      props: { placeholder: '请输入业务单号', clearable: true }
    },
    {
      label: '资产编号',
      key: 'assetNo',
      type: 'input',
      props: { placeholder: '请输入关联资产编号', clearable: true }
    },
    {
      label: '流转状态',
      key: 'status',
      type: 'select',
      props: {
        placeholder: '请选择流转状态',
        clearable: true,
        options: wf_status.value
      }
    }
  ])

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
      apiFn: listRequisition,
      columnsFactory: () => {
        const baseColumns: ColumnOption<AssetRequisitionItem>[] = [
          { type: 'index', label: '序号', width: 60, align: 'center' },
          { prop: 'requisitionNo', label: '领用单号', width: 150 },
          { prop: 'assetNo', label: '关联资产', minWidth: 150 },
          { prop: 'assetName', label: '资产名称', minWidth: 150 },
          { prop: 'applyUserId', label: '申请人ID', width: 120, align: 'center' },
          { prop: 'reason', label: '领用事由', minWidth: 200 },
          {
            prop: 'status',
            label: '状态',
            width: 100,
            align: 'center',
            formatter: (row: AssetRequisitionItem) =>
              h(DictTag, {
                options: wf_status.value,
                value: mapRequisitionStatusToWorkflow(row.status)
              })
          },
          { prop: 'createTime', label: '申请时间', width: 170, align: 'center' }
        ]

        if (!returnApiAvailable) {
          return baseColumns
        }

        return [
          ...baseColumns,
          {
            prop: 'operation',
            label: '操作',
            width: 120,
            align: 'right',
            formatter: (row: AssetRequisitionItem) =>
              canReturnAsset(row)
                ? h(
                    'span',
                    {
                      class: 'text-[var(--art-gray-500)]'
                    },
                    '待补充'
                  )
                : null
          }
        ]
      }
    }
  })

  const tableRef = ref()

  /** 重置查询条件，保持和台账页相同的交互节奏。 */
  const handleReset = () => {
    Object.assign(formFilters, initialSearchState)
    resetSearchParams()
  }

  /** 把筛选条件同步到 useTable 的查询状态中。 */
  const handleSearch = () => {
    Object.assign(searchParams, formFilters)
    getData()
  }

  onMounted(() => {
    void wf_status.value
  })
</script>

<style lang="scss" scoped>
  .asset-requisition-page {
    padding: 12px;
  }
</style>
