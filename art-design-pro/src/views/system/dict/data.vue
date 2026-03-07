<!-- 字典数据管理页面 -->
<template>
  <div class="dict-data-page p-3">
    <!-- 搜索栏 -->
    <ArtSearchBar
      v-model="formFilters"
      :items="formItems"
      @reset="handleReset"
      @search="handleSearch"
    />

    <ElCard class="art-table-card mt-3" shadow="never">
      <!-- 表格头部 -->
      <ArtTableHeader
        :loading="loading"
        v-model:columns="columnChecks"
        @refresh="refreshData"
      >
        <template #left>
          <ElButton v-auth="'system:dict:add'" type="primary" @click="handleAdd" v-ripple>
            新增数据
          </ElButton>
          <ElButton
            v-auth="'system:dict:remove'"
            type="danger"
            plain
            :disabled="!multiple"
            @click="handleDelete()"
            v-ripple
          >
            删除
          </ElButton>
          <ElButton type="warning" plain @click="handleClose" v-ripple>
            关闭
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
        rowKey="dictCode"
        @selection-change="handleSelectionChange"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>

    <!-- 字典数据编辑弹窗 -->
    <DictDataDialog
      v-model="dialogVisible"
      :dialog-type="dialogType"
      :form-data="currentData"
      :default-dict-type="defaultDictType"
      @success="refreshData"
    />
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, computed, onMounted, h } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import { listData, delData } from '@/api/system/dict/data'
  import { getType, listType } from '@/api/system/dict/type'
  import { useTable } from '@/hooks/core/useTable'
  import { useDict } from '@/utils/dict'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import DictTag from '@/components/DictTag/index.vue'
  import { ElMessageBox, ElMessage } from 'element-plus'
  import DictDataDialog from './modules/dict-data-dialog.vue'

  defineOptions({ name: 'DictData' })

  const route = useRoute()
  const router = useRouter()
  const { sys_normal_disable } = useDict('sys_normal_disable')

  // 状态管理
  const ids = ref<number[]>([])
  const multiple = ref(true)
  const dialogVisible = ref(false)
  const dialogType = ref<'add' | 'edit'>('add')
  const currentData = ref<any>()
  const typeOptions = ref<any[]>([])
  const defaultDictType = ref('')

  // 搜索配置
  const initialSearchState = {
    dictType: '',
    dictLabel: '',
    status: ''
  }
  const formFilters = reactive({ ...initialSearchState })

  const formItems = computed(() => [
    {
      label: '字典类型',
      key: 'dictType',
      type: 'select',
      options: typeOptions.value,
      props: { placeholder: '请选择字典类型', clearable: true }
    },
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
      options: sys_normal_disable.value,
      props: { placeholder: '数据状态', clearable: true }
    }
  ])

  // 表格配置
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
      apiFn: listData,
      apiParams: {
        pageNum: 1,
        pageSize: 10,
        dictType: route.params.dictId ? undefined : route.query.dictType
      },
      columnsFactory: () => [
        { type: 'selection', width: 55, align: 'center' },
        { prop: 'dictCode', label: '字典编码', width: 100 },
        { prop: 'dictLabel', label: '字典标签', minWidth: 150 },
        { prop: 'dictValue', label: '字典键值', minWidth: 150 },
        { prop: 'dictSort', label: '字典排序', width: 100, align: 'center' },
        {
          prop: 'status',
          label: '状态',
          width: 100,
          align: 'center',
          formatter: (row: any) => h(DictTag, { options: sys_normal_disable.value, value: row.status })
        },
        { prop: 'remark', label: '备注', minWidth: 150, showOverflowTooltip: true },
        { prop: 'createTime', label: '创建时间', width: 170, align: 'center' },
        {
          prop: 'operation',
          label: '操作',
          width: 160,
          align: 'right',
          formatter: (row: any) => h('div', { class: 'flex justify-end' }, [
            h(ArtButtonTable, { type: 'edit', onClick: () => handleUpdate(row) }),
            h(ArtButtonTable, { type: 'delete', onClick: () => handleDelete(row) })
          ])
        }
      ]
    }
  })

  /** 获取所有字典类型以供下拉筛选 */
  async function getTypeList() {
    try {
      const res: any = await listType()
      const list = Array.isArray(res) ? res : res.rows || []
      typeOptions.value = list.map((item: any) => ({
        label: item.dictName,
        value: item.dictType
      }))
    } catch {}
  }

  /** 初始化查询参数 */
  async function initData() {
    const dictId = route.params.dictId as string
    if (dictId) {
      try {
        const res: any = await getType(dictId)
        const dict = res.data || res
        formFilters.dictType = dict.dictType
        defaultDictType.value = dict.dictType
        searchParams.dictType = dict.dictType
        getData()
      } catch {}
    }
  }

  onMounted(() => {
    getTypeList()
    initData()
  })

  /** 搜索 */
  function handleSearch() {
    Object.assign(searchParams, formFilters)
    getData()
  }

  /** 重置 */
  function handleReset() {
    Object.assign(formFilters, initialSearchState)
    formFilters.dictType = defaultDictType.value
    resetSearchParams()
  }

  /** 多选 */
  function handleSelectionChange(selection: any[]) {
    ids.value = selection.map(item => item.dictCode)
    multiple.value = !selection.length
  }

  /** 关闭/返回上级 */
  function handleClose() {
    router.back()
  }

  /** 新增 */
  function handleAdd() {
    dialogType.value = 'add'
    currentData.value = undefined
    dialogVisible.value = true
  }

  /** 修改 */
  function handleUpdate(row: any) {
    dialogType.value = 'edit'
    currentData.value = { ...row }
    dialogVisible.value = true
  }

  /** 删除 */
  async function handleDelete(row?: any) {
    const dictCodes = row?.dictCode || ids.value
    try {
      await ElMessageBox.confirm(`是否确认删除字典数据编码为"${dictCodes}"的数据项？`, '提示', { type: 'warning' })
      await delData(dictCodes)
      ElMessage.success('删除成功')
      refreshData()
    } catch {}
  }
</script>
