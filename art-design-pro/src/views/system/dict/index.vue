<!-- 字典类型管理页面 -->
<template>
  <div class="dict-type-page p-3">
    <!-- 搜索栏 -->
    <ArtSearchBar
      v-model="formFilters"
      :items="formItems"
      @reset="handleReset"
      @search="handleSearch"
    />

    <ElCard class="art-table-card mt-3" shadow="never">
      <!-- 表格头部 -->
      <ArtTableHeader :loading="loading" v-model:columns="columnChecks" @refresh="refreshData">
        <template #left>
          <ElButton v-auth="'system:dict:add'" type="primary" @click="handleAdd" v-ripple>
            新增类型
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
          <ElButton type="warning" plain @click="handleRefreshCache" v-ripple> 刷新缓存 </ElButton>
        </template>
      </ArtTableHeader>

      <!-- 表格 -->
      <ArtTable
        ref="tableRef"
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
        rowKey="dictId"
        @selection-change="handleSelectionChange"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>

    <!-- 字典类型编辑弹窗 -->
    <DictTypeDialog
      v-model="dialogVisible"
      :dialog-type="dialogType"
      :form-data="currentData"
      @success="refreshData"
    />

    <!-- 字典数据抽屉（核心修改：点击字典类型后打开此抽屉） -->
    <DictDataDrawer v-model="drawerVisible" :dict-type="selectedDictType" @closed="handleClosed" />
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, computed, h, onMounted } from 'vue'
  import { listType, delType, refreshCache } from '@/api/system/dict/type'
  import { useTable } from '@/hooks/core/useTable'
  import { useDict } from '@/utils/dict'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import DictTag from '@/components/DictTag/index.vue'
  import { ElMessageBox, ElMessage, ElLink } from 'element-plus'
  import DictTypeDialog from './modules/dict-type-dialog.vue'
  import DictDataDrawer from './modules/dict-data-drawer.vue'

  defineOptions({ name: 'Dict' })

  const dicts = useDict('sys_normal_disable')

  // 状态管理
  const ids = ref<number[]>([])
  const multiple = ref(true)
  const dialogVisible = ref(false)
  const dialogType = ref<'add' | 'edit'>('add')
  const currentData = ref<any>()

  // 抽屉管理
  const drawerVisible = ref(false)
  const selectedDictType = ref('')

  // 搜索配置
  const initialSearchState = {
    dictName: '',
    dictType: '',
    status: ''
  }
  const formFilters = reactive({ ...initialSearchState })

  const formItems = computed(() => [
    {
      label: '字典名称',
      key: 'dictName',
      type: 'input',
      props: { placeholder: '请输入字典名称', clearable: true }
    },
    {
      label: '字典类型',
      key: 'dictType',
      type: 'input',
      props: { placeholder: '请输入字典类型', clearable: true }
    },
    {
      label: '状态',
      key: 'status',
      type: 'select',
      props: {
        placeholder: '字典状态',
        clearable: true,
        options: dicts.sys_normal_disable.value
      }
    }
  ])

  // 表格配置
  const columns = computed(() => [
    { type: 'selection' as const, width: 55, align: 'center' },
    { prop: 'dictId', label: '字典主键', width: 100 },
    { prop: 'dictName', label: '字典名称', minWidth: 150 },
    {
      prop: 'dictType',
      label: '字典类型',
      minWidth: 150,
      formatter: (row: any) => {
        return h(
          ElLink,
          {
            type: 'primary',
            underline: false,
            onClick: () => handleOpenDrawer(row)
          },
          () => row.dictType
        )
      }
    },
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
    { prop: 'createTime', label: '创建时间', width: 170, align: 'center' },
    {
      prop: 'operation',
      label: '操作',
      width: 160,
      align: 'right',
      formatter: (row: any) =>
        h('div', { class: 'flex justify-end' }, [
          h(ArtButtonTable, { type: 'edit', onClick: () => handleUpdate(row) }),
          h(ArtButtonTable, { type: 'delete', onClick: () => handleDelete(row) })
        ])
    }
  ])

  /** 状态初始化加载 */
  onMounted(() => {
    // 显式访问一次以触发加载
    void dicts.sys_normal_disable.value
  })

  const {
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
      apiFn: listType,
      apiParams: {
        pageNum: 1,
        pageSize: 10
      }
    }
  })

  /** 搜索 */
  function handleSearch() {
    Object.assign(searchParams, formFilters)
    getData()
  }

  /** 重置 */
  function handleReset() {
    Object.assign(formFilters, initialSearchState)
    resetSearchParams()
  }

  /** 关闭抽屉时重置搜索条件 */
  function handleClosed() {
    // 不在此重置主表格状态
  }

  /** 多选 */
  function handleSelectionChange(selection: any[]) {
    ids.value = selection.map((item) => item.dictId)
    multiple.value = !selection.length
  }

  /** 打开详情抽屉 */
  function handleOpenDrawer(row: any) {
    selectedDictType.value = row.dictType
    drawerVisible.value = true
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
    const dictIds = row?.dictId || ids.value
    if (!dictIds) return
    try {
      await ElMessageBox.confirm(`是否确认删除字典编号为"${dictIds}"的数据项？`, '提示', {
        type: 'warning'
      })
      await delType(dictIds)
      ElMessage.success('删除成功')
      refreshData()
    } catch (error) {
      console.log('取消删除:', error)
    }
  }

  /** 刷新缓存 */
  async function handleRefreshCache() {
    try {
      await refreshCache()
      ElMessage.success('刷新成功')
    } catch (error) {
      console.error('刷新缓存失败:', error)
    }
  }
</script>
