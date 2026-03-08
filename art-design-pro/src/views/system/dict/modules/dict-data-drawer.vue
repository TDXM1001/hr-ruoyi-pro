<!-- 字典数据详情抽屉 -->
<template>
  <ElDrawer
    :title="'字典数据管理 - ' + (dictType || '')"
    v-model="visible"
    size="1000px"
    destroy-on-close
    @closed="handleClosed"
  >
    <div class="p-4 h-full flex flex-col overflow-hidden bg-gray-50/50">
      <!-- 列表搜索 -->
      <ArtSearchBar
        v-model="formFilters"
        :items="formItems"
        @reset="handleReset"
        @search="handleSearch"
      />

      <ElCard class="art-table-card mt-3 flex-1 flex flex-col overflow-hidden" shadow="never">
        <!-- 表格头部 -->
        <ArtTableHeader :loading="loading" v-model:columns="columnChecks" @refresh="refreshData">
          <template #left>
            <ElButton v-auth="'system:dict:add'" type="primary" @click="handleAdd" v-ripple>
              新增数据
            </ElButton>
          </template>
        </ArtTableHeader>

        <!-- 列表表格 -->
        <ArtTable
          ref="tableRef"
          class="flex-1"
          :loading="loading"
          :data="data"
          :columns="columns"
          :pagination="pagination"
          rowKey="dictCode"
          @pagination:size-change="handleSizeChange"
          @pagination:current-change="handleCurrentChange"
        />
      </ElCard>

      <!-- 字典数据编辑弹窗 -->
      <DictDataDialog
        v-model="dialogVisible"
        :dialog-type="dialogType"
        :form-data="currentData"
        :default-dict-type="dictType"
        @success="refreshData"
      />
    </div>
  </ElDrawer>
</template>

<script setup lang="ts">
  import { ref, reactive, computed, watch, h, nextTick } from 'vue'
  import { listData, delData } from '@/api/system/dict/data'
  import { useTable } from '@/hooks/core/useTable'
  import { useDict } from '@/utils/dict'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import DictTag from '@/components/DictTag/index.vue'
  import { ElMessageBox, ElMessage } from 'element-plus'
  import DictDataDialog from './dict-data-dialog.vue'

  const props = defineProps<{
    modelValue: boolean
    dictType: string
  }>()

  const emit = defineEmits(['update:modelValue'])

  // 使用完整的 dicts 引用，避免解构失去响应式
  const dicts = useDict('sys_normal_disable')

  const visible = ref(false)
  const dialogVisible = ref(false)
  const dialogType = ref<'add' | 'edit'>('add')
  const currentData = ref<any>()
  const tableRef = ref()

  // 搜索参数
  const formFilters = reactive({
    dictLabel: '',
    status: ''
  })

  // 搜索项配置
  const formItems = computed(() => [
    {
      label: '字典标签',
      key: 'dictLabel',
      type: 'input',
      props: { placeholder: '请输入字典标签', clearable: true }
    },
    {
      label: '状态',
      key: 'status',
      type: 'select',
      props: {
        placeholder: '选择状态',
        clearable: true,
        options: dicts.sys_normal_disable.value
      }
    }
  ])

  // 表格列定义
  const columns = computed(() => [
    { prop: 'dictCode', label: '数据编码', width: 100 },
    { prop: 'dictLabel', label: '字典标签', minWidth: 150 },
    { prop: 'dictValue', label: '字典键值', minWidth: 150 },
    { prop: 'dictSort', label: '字典排序', width: 100, align: 'center' },
    {
      prop: 'status',
      label: '状态',
      width: 100,
      align: 'center',
      formatter: (row: any) =>
        h(DictTag, {
          options: dicts.sys_normal_disable.value,
          value: row.status
        })
    },
    { prop: 'remark', label: '备注', minWidth: 150, showOverflowTooltip: true },
    {
      prop: 'operation',
      label: '操作',
      width: 130,
      align: 'right',
      formatter: (row: any) =>
        h('div', { class: 'flex justify-end' }, [
          h(ArtButtonTable, { type: 'edit', onClick: () => handleUpdate(row) }),
          h(ArtButtonTable, { type: 'delete', onClick: () => handleDelete(row) })
        ])
    }
  ])

  // 表格逻辑
  const {
    data,
    loading,
    pagination,
    searchParams,
    handleSizeChange,
    handleCurrentChange,
    refreshData,
    getData,
    columnChecks
  } = useTable({
    core: {
      apiFn: listData,
      apiParams: { dictType: '' },
      immediate: false // 手动在抽屉打开时触发
    }
  })

  // 监听显示状态
  watch(
    () => props.modelValue,
    async (val) => {
      visible.value = val
      if (val && props.dictType) {
        // 关键逻辑：等待 DOM 渲染或参数同步
        await nextTick()
        searchParams.dictType = props.dictType
        searchParams.pageNum = 1 // 强制切回第一页
        getData()
      }
    }
  )

  watch(
    () => visible.value,
    (val) => emit('update:modelValue', val)
  )

  /** 搜索 */
  function handleSearch() {
    Object.assign(searchParams, formFilters)
    getData()
  }

  /** 重置搜索 */
  function handleReset() {
    formFilters.dictLabel = ''
    formFilters.status = ''
    handleSearch()
  }

  /** 新增数据 */
  function handleAdd() {
    dialogType.value = 'add'
    currentData.value = undefined
    dialogVisible.value = true
  }

  /** 修改数据 */
  function handleUpdate(row: any) {
    dialogType.value = 'edit'
    currentData.value = { ...row }
    dialogVisible.value = true
  }

  /** 删除数据 */
  async function handleDelete(row: any) {
    try {
      await ElMessageBox.confirm(`是否确认删除字典标签为"${row.dictLabel}"的数据项？`, '提示', {
        type: 'warning'
      })
      await delData(row.dictCode)
      ElMessage.success('删除成功')
      refreshData()
    } catch {}
  }

  /** 抽屉关闭回调 */
  function handleClosed() {
    data.value = []
    searchParams.dictLabel = ''
    searchParams.status = ''
    // 隐藏时不重置 dictType，以免下次打开前闪烁，保持为 props.dictType 驱动
  }
</script>
