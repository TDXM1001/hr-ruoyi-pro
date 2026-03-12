<template>
  <div class="asset-requisition-page art-full-height flex flex-col p-3 overflow-hidden">
    <!-- 搜索栏 -->
    <ArtSearchBar
      :key="wf_status.length"
      v-model="formFilters"
      :items="formItems"
      :showExpand="false"
      @reset="handleReset"
      @search="handleSearch"
    />

    <ElCard class="art-table-card flex-1 overflow-hidden" shadow="never">
      <!-- 表格头部 -->
      <ArtTableHeader
        :showZebra="false"
        :loading="loading"
        v-model:columns="columnChecks"
        @refresh="refreshData"
      />

      <!-- 表格 -->
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
  import { ref, reactive, computed, onMounted, h } from 'vue'
  import { ElMessageBox, ElMessage, ElButton } from 'element-plus'
  import { listRequisition, returnAsset } from '@/api/asset/requisition'
  import { useTable } from '@/hooks/core/useTable'
  import { useDict } from '@/utils/dict'
  import DictTag from '@/components/DictTag/index.vue'

  defineOptions({ name: 'AssetRequisition' })

  // 状态管理
  const { wf_status } = useDict('wf_status')

  // 搜索相关
  const initialSearchState = {
    requisitionNo: '',
    assetNo: '',
    status: undefined
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
      props: { placeholder: '请输入相关资产编号', clearable: true }
    },
    {
      label: '流转状态',
      key: 'status',
      type: 'select',
      props: {
        placeholder: '流转状态',
        clearable: true,
        options: wf_status.value
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
      apiFn: listRequisition,
      columnsFactory: () => [
        { type: 'index', label: '序号', width: 60, align: 'center' },
        { prop: 'requisitionNo', label: '领用单号', width: 150 },
        { prop: 'assetNo', label: '关联资产', minWidth: 150 },
        { prop: 'applyUserId', label: '申请人ID', width: 120, align: 'center' },
        { prop: 'reason', label: '领用事由', minWidth: 200 },
        {
          prop: 'status',
          label: '状态',
          width: 100,
          align: 'center',
          formatter: (row: any) => {
            // 这里将资产领用表的status映射为对应表现
            // status：0=审批中 1=已通过 2=已驳回 3=已归还
            let val = ''
            if (row.status === 0) val = 'IN_PROGRESS'
            else if (row.status === 1) val = 'COMPLETED'
            else if (row.status === 2) val = 'REJECTED'
            else if (row.status === 3) val = 'COMPLETED' // 类似已归还

            return h(DictTag, { options: wf_status.value, value: val })
          }
        },
        { prop: 'createTime', label: '申请时间', width: 170, align: 'center' },
        {
          prop: 'operation',
          label: '操作',
          width: 120,
          align: 'right',
          formatter: (row: any) => {
            return h('div', { class: 'flex justify-end' }, [
              row.status === 1
                ? h(
                    ElButton,
                    {
                      type: 'primary',
                      link: true,
                      onClick: () => handleReturn(row)
                    },
                    () => '归还资产'
                  )
                : null
            ])
          }
        }
      ]
    }
  })

  const tableRef = ref()

  /** 重置 */
  const handleReset = () => {
    Object.assign(formFilters, initialSearchState)
    resetSearchParams()
  }

  /** 搜索 */
  const handleSearch = () => {
    Object.assign(searchParams, formFilters)
    getData()
  }

  /** 归还操作 */
  const handleReturn = async (row: any) => {
    try {
      await ElMessageBox.confirm(`是否确认归还单号为"${row.requisitionNo}"的资产？`, '归还提示', {
        type: 'warning'
      })
      await returnAsset(row.requisitionNo)
      ElMessage.success('资产归还请求已提交')
      refreshData()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('归还操作失败:', error)
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
