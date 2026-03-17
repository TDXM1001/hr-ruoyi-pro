<template>
  <div class="asset-requisition-page art-full-height flex flex-col overflow-hidden p-3">
    <ArtSearchBar
      :key="wf_status.length"
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
        rowKey="requisitionNo"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { computed, h, onMounted, reactive, ref } from 'vue'
  import { ElButton, ElMessage, ElMessageBox } from 'element-plus'
  import { listRequisition, returnAsset, type AssetRequisitionItem } from '@/api/asset/requisition'
  import DictTag from '@/components/DictTag/index.vue'
  import { useTable } from '@/hooks/core/useTable'
  import type { ColumnOption } from '@/types/component'
  import { useDict } from '@/utils/dict'
  import {
    canReturnAsset,
    formatFixedAssetBusinessStatus,
    resolveFixedAssetWorkflowStatus
  } from './requisition.helper'

  defineOptions({ name: 'AssetRequisition' })

  const { wf_status } = useDict('wf_status')

  const initialSearchState = {
    requisitionNo: '',
    assetNo: '',
    status: undefined as number | undefined
  }

  const formFilters = reactive({ ...initialSearchState })

  const formItems = computed(() => [
    {
      label: '领用单号',
      key: 'requisitionNo',
      type: 'input',
      props: { placeholder: '请输入领用单号', clearable: true }
    },
    {
      label: '资产编号',
      key: 'assetNo',
      type: 'input',
      props: { placeholder: '请输入资产编号', clearable: true }
    },
    {
      label: '单据状态',
      key: 'status',
      type: 'select',
      props: {
        placeholder: '请选择单据状态',
        clearable: true,
        options: [
          { label: '待审批', value: 0 },
          { label: '已审批', value: 1 },
          { label: '已驳回', value: 2 },
          { label: '已完成', value: 3 }
        ]
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
          { prop: 'assetNo', label: '资产编号', minWidth: 150 },
          { prop: 'assetName', label: '资产名称', minWidth: 150 },
          { prop: 'applyUserId', label: '申请人ID', width: 120, align: 'center' },
          { prop: 'reason', label: '领用事由', minWidth: 200 },
          {
            prop: 'status',
            label: '单据状态',
            width: 100,
            align: 'center',
            formatter: (row: AssetRequisitionItem) => formatFixedAssetBusinessStatus(Number(row.status))
          },
          {
            prop: 'wfStatus',
            label: '审批状态',
            width: 100,
            align: 'center',
            formatter: (row: AssetRequisitionItem) =>
              h(DictTag, {
                options: wf_status.value,
                value: resolveFixedAssetWorkflowStatus(row)
              })
          },
          { prop: 'createTime', label: '申请时间', width: 170, align: 'center' }
        ]

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
                    ElButton,
                    {
                      type: 'primary',
                      link: true,
                      onClick: () => handleReturn(row)
                    },
                    () => '归还资产'
                  )
                : h('span', { class: 'text-[var(--art-gray-400)]' }, '--')
          }
        ]
      }
    }
  })

  const tableRef = ref()

  /** 重置搜索条件并回到默认列表。 */
  const handleReset = () => {
    Object.assign(formFilters, initialSearchState)
    resetSearchParams()
  }

  /** 将搜索栏条件同步到表格查询参数。 */
  const handleSearch = () => {
    Object.assign(searchParams, formFilters)
    getData()
  }

  /**
   * 对审批通过的领用单发起归还，完成后刷新列表。
   */
  const handleReturn = async (row: AssetRequisitionItem) => {
    try {
      await ElMessageBox.confirm(`确认归还领用单 ${row.requisitionNo} 对应的资产吗？`, '提示', {
        type: 'warning'
      })
      await returnAsset(row.requisitionNo)
      ElMessage.success('资产归还成功')
      refreshData()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('归还资产失败:', error)
      }
    }
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
