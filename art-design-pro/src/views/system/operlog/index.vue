<!-- 操作日志管理页面 (路径: src/views/system/operlog/index.vue) -->
<template>
  <div class="operlog-page art-full-height flex flex-col p-3 overflow-hidden">
    <!-- 搜索栏 -->
    <ArtSearchBar
      v-model="formFilters"
      :items="formItems"
      @reset="handleReset"
      @search="handleSearch"
    />

    <ElCard class="art-table-card flex-1 overflow-hidden" shadow="never">
      <!-- 表格头部 -->
      <ArtTableHeader :loading="loading" @refresh="refreshData">
        <template #left>
          <ElButton
            v-auth="'monitor:operlog:remove'"
            type="danger"
            plain
            :disabled="!multiple"
            @click="handleDelete"
            v-ripple
          >
            删除
          </ElButton>
          <ElButton
            v-auth="'monitor:operlog:remove'"
            type="danger"
            plain
            @click="handleClean"
            v-ripple
          >
            清空
          </ElButton>
        </template>
      </ArtTableHeader>

      <!-- 表格 -->
      <ArtTable
        ref="tableRef"
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
        rowKey="operId"
        @selection-change="handleSelectionChange"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>

    <!-- 详情弹窗 -->
    <OperlogDetailDialog
      v-model="detailVisible"
      :data="currentData"
      :type-options="sys_oper_type.value"
    />
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, computed, onMounted, h } from 'vue'
  import { list, delOperlog, cleanOperlog } from '@/api/system/operlog'
  import { useTable } from '@/hooks/core/useTable'
  import { useDict } from '@/utils/dict'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import DictTag from '@/components/DictTag/index.vue'
  import { ElMessageBox, ElMessage } from 'element-plus'
  import OperlogDetailDialog from './modules/operlog-detail-dialog.vue'

  defineOptions({ name: 'Operlog' })

  const dicts = useDict('sys_oper_type', 'sys_common_status')
  const { sys_oper_type, sys_common_status } = dicts

  const ids = ref<number[]>([])
  const multiple = ref(true)
  const detailVisible = ref(false)
  const currentData = ref<any>({})

  const initialSearchState = { title: '', operName: '', businessType: '', status: '' }
  const formFilters = reactive({ ...initialSearchState })

  const formItems = computed(() => [
    { label: '系统模块', key: 'title', type: 'input', props: { placeholder: '请输入系统模块', clearable: true } },
    { label: '操作人员', key: 'operName', type: 'input', props: { placeholder: '请输入操作人员', clearable: true } },
    { label: '类型', key: 'businessType', type: 'select', props: { options: sys_oper_type.value, clearable: true } },
    { label: '状态', key: 'status', type: 'select', props: { options: sys_common_status.value, clearable: true } }
  ])

  const {
    columns,
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
      apiFn: list,
      columnsFactory: () => [
        { type: 'selection', width: 55, align: 'center' },
        { prop: 'operId', label: '日志编号', width: 100 },
        { prop: 'title', label: '系统模块', minWidth: 100 },
        {
          prop: 'businessType',
          label: '操作类型',
          width: 100,
          align: 'center',
          render: (row: any) => h(DictTag, { options: sys_oper_type.value, value: row.businessType })
        },
        { prop: 'operName', label: '人员', width: 100 },
        { prop: 'operIp', label: '地点', width: 130 },
        {
          prop: 'status',
          label: '状态',
          width: 100,
          align: 'center',
          render: (row: any) => h(DictTag, { options: sys_common_status.value, value: row.status })
        },
        { prop: 'operTime', label: '时间', width: 170, align: 'center' },
        {
          prop: 'operation',
          label: '操作',
          width: 120,
          align: 'right',
          render: (row: any) => h('div', { class: 'flex justify-end gap-2' }, [
            h(ArtButtonTable, { type: 'info', onClick: () => handleView(row) }),
            h(ArtButtonTable, { type: 'delete', onClick: () => handleDelete(row) })
          ])
        }
      ]
    }
  })

  const tableRef = ref()

  const handleSelectionChange = (selection: any[]) => {
    ids.value = selection.map((item) => item.operId)
    multiple.value = !selection.length
  }

  const handleReset = () => {
    Object.assign(formFilters, initialSearchState)
    resetSearchParams()
  }

  const handleSearch = () => {
    Object.assign(searchParams, formFilters)
    getData()
  }

  const handleView = (row: any) => {
    currentData.value = row
    detailVisible.value = true
  }

  const handleDelete = async (row?: any) => {
    const operIds = row?.operId || ids.value
    if (!operIds || (Array.isArray(operIds) && operIds.length === 0)) return
    try {
      await ElMessageBox.confirm(`是否确认删除操作日志编号为"${operIds}"？`, '提示', { type: 'warning' })
      await delOperlog(operIds)
      ElMessage.success('删除成功')
      refreshData()
    } catch (error) {}
  }

  const handleClean = async () => {
    try {
      await ElMessageBox.confirm('是否确认清空？', '提示', { type: 'warning' })
      await cleanOperlog()
      ElMessage.success('清空成功')
      refreshData()
    } catch (error) {}
  }

  onMounted(() => {
    void sys_oper_type.value
    void sys_common_status.value
  })
</script>
