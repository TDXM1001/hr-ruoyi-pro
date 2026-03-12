<!-- 资产台账页面 -->
<template>
  <div class="asset-list-page art-full-height flex !flex-row p-3 overflow-hidden">
    <!-- 左侧分类树 (对标 DeptTreeAside 样式与折叠逻辑) -->
    <div
      class="dept-tree-container relative h-full transition-all duration-300 flex-shrink-0"
      :style="{
        width: isTreeCollapse ? '0px' : '200px',
        marginRight: isTreeCollapse ? '0px' : '15px'
      }"
    >
      <div
        class="art-table-card !m-0 h-full flex flex-col overflow-hidden border border-solid border-[var(--art-gray-200)]"
        :style="{
          opacity: isTreeCollapse ? 0 : 1,
          visibility: isTreeCollapse ? 'hidden' : 'visible',
          transition: 'opacity 0.2s'
        }"
      >
        <div
          class="aside-header flex justify-between items-center px-4 py-3 border-b border-solid border-[var(--art-gray-200)] whitespace-nowrap"
        >
          <span class="text-[15px] font-bold text-g-700">资产分类</span>
          <ArtSvgIcon icon="ri:organization-chart" class="text-g-500 text-lg" />
        </div>

        <div class="px-4 py-3">
          <ElInput
            v-model="filterText"
            placeholder="搜索分类"
            prefix-icon="Search"
            clearable
            class="aside-search w-full"
          />
        </div>

        <div class="flex-1 overflow-hidden px-2 pb-4">
          <ElScrollbar class="h-full">
            <ElTree
              ref="treeRef"
              :data="categoryOptions"
              :props="{ label: 'name', children: 'children' }"
              highlight-current
              node-key="id"
              default-expand-all
              :filter-node-method="filterNode"
              @node-click="handleNodeClick"
              empty-text="暂无数据"
            />
          </ElScrollbar>
        </div>
      </div>

      <!-- 折叠按钮 -->
      <div
        class="collapse-handle flex items-center justify-center cursor-pointer absolute shadow-md border border-solid border-[var(--art-gray-200)]"
        :class="isTreeCollapse ? 'handle-collapsed' : 'handle-expanded'"
        @click="isTreeCollapse = !isTreeCollapse"
      >
        <ArtSvgIcon
          :icon="isTreeCollapse ? 'ri:arrow-right-s-line' : 'ri:arrow-left-s-line'"
          class="text-lg"
        />
      </div>
    </div>

    <!-- 右侧主体内容 -->
    <div class="flex-1 min-w-0 flex flex-col h-full overflow-hidden relative">
      <!-- 搜索栏 -->
      <ArtSearchBar
        :key="asset_status.length"
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
        >
          <template #left>
            <ElButton v-auth="'asset:info:add'" type="primary" @click="handleAdd" v-ripple>
              新增资产
            </ElButton>
            <ElButton
              v-auth="'asset:info:remove'"
              type="danger"
              plain
              :disabled="!multiple"
              @click="handleDelete"
              v-ripple
            >
              删除
            </ElButton>
            <ElButton
              v-auth="'asset:info:export'"
              type="warning"
              plain
              @click="handleExport"
              v-ripple
            >
              导出
            </ElButton>
          </template>
        </ArtTableHeader>

        <!-- 表格 - 使用项目封装的 ArtTable -->
        <ArtTable
          ref="tableRef"
          :loading="loading"
          :data="data"
          :columns="columns"
          :pagination="pagination"
          rowKey="assetNo"
          @selection-change="handleSelectionChange"
          @pagination:size-change="handleSizeChange"
          @pagination:current-change="handleCurrentChange"
        />
      </ElCard>
    </div>

    <!-- 资产编辑抽屉 -->
    <AssetEditDrawer
      v-model="drawerVisible"
      :dialog-type="drawerType"
      :asset-data="currentData"
      @success="refreshData"
    />
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, computed, onMounted, watch, h } from 'vue'
  import { listInfo, delInfo, exportInfo } from '@/api/asset/info'
  import { listCategory } from '@/api/asset/category'
  import { useTable } from '@/hooks/core/useTable'
  import { useDict } from '@/utils/dict'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import DictTag from '@/components/DictTag/index.vue'
  import { handleTree } from '@/utils/ruoyi'
  import { ElMessageBox, ElMessage } from 'element-plus'
  import AssetEditDrawer from './modules/asset-edit-drawer.vue'

  defineOptions({ name: 'AssetList' })

  // 状态管理
  const filterText = ref('')
  const isTreeCollapse = ref(false)
  const treeRef = ref()
  const categoryOptions = ref<any[]>([])
  const ids = ref<string[]>([])
  const multiple = ref(true)
  
  // 接入字典
  const { asset_type, asset_status } = useDict('asset_type', 'asset_status')

  // 抽屉相关
  const drawerVisible = ref(false)
  const drawerType = ref<'add' | 'edit'>('add')
  const currentData = ref<any>()

  // 搜索相关
  const initialSearchState = {
    assetNo: '',
    assetName: '',
    status: undefined
  }

  const formFilters = reactive({ ...initialSearchState })

  const formItems = computed(() => [
    {
      label: '资产编号',
      key: 'assetNo',
      type: 'input',
      props: { placeholder: '请输入资产编号', clearable: true }
    },
    {
      label: '资产名称',
      key: 'assetName',
      type: 'input',
      props: { placeholder: '请输入资产名称', clearable: true }
    },
    {
      label: '资产状态',
      key: 'status',
      type: 'select',
      props: {
        placeholder: '资产状态',
        clearable: true,
        options: asset_status.value
      }
    }
  ])

  // 表格
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
      apiFn: listInfo,
      apiParams: {
        categoryId: undefined
      },
      columnsFactory: () => [
        { type: 'selection', width: 55, align: 'center' },
        { prop: 'assetNo', label: '资产编号', width: 120 },
        { prop: 'assetName', label: '资产名称', minWidth: 150 },
        { 
          prop: 'assetType', 
          label: '资产类型', 
          width: 100, 
          align: 'center',
          formatter: (row: any) => {
            return h(DictTag, { options: asset_type.value, value: row.assetType })
          }
        },
        { 
          prop: 'status', 
          label: '状态', 
          width: 100, 
          align: 'center',
          formatter: (row: any) => {
            return h(DictTag, { options: asset_status.value, value: row.status })
          }
        },
        { prop: 'createTime', label: '创建时间', width: 170, align: 'center' },
        {
          prop: 'operation',
          label: '操作',
          width: 160,
          align: 'right',
          formatter: (row: any) => {
            return h('div', { class: 'flex justify-end' }, [
              h(ArtButtonTable, {
                type: 'edit',
                onClick: () => handleUpdate(row)
              }),
              h(ArtButtonTable, {
                type: 'delete',
                onClick: () => handleDelete(row)
              })
            ])
          }
        }
      ]
    }
  })

  const tableRef = ref()

  /** 查询分类树 */
  const getCategoryTree = async () => {
    try {
      const res: any = await listCategory()
      const list = Array.isArray(res) ? res : res.data || res.rows || []
      categoryOptions.value = handleTree(list, 'id')
    } catch (error) {
      console.error('获取分类树失败:', error)
    }
  }

  /** 多选框选中数据 */
  const handleSelectionChange = (selection: any[]) => {
    ids.value = selection.map((item) => item.assetNo)
    multiple.value = !selection.length
  }

  /** 分类树过滤 */
  const filterNode = (value: string, data: any) => {
    if (!value) return true
    return data.name.includes(value)
  }

  watch(filterText, (val) => {
    treeRef.value?.filter(val)
  })

  /** 分类点击 */
  const handleNodeClick = (data: any) => {
    searchParams.categoryId = data.id
    refreshData()
  }

  /** 重置 */
  const handleReset = () => {
    Object.assign(formFilters, initialSearchState)
    searchParams.categoryId = undefined
    resetSearchParams()
  }

  /** 搜索 */
  const handleSearch = () => {
    Object.assign(searchParams, formFilters)
    getData()
  }

  /** 新增 */
  const handleAdd = () => {
    drawerType.value = 'add'
    currentData.value = undefined
    drawerVisible.value = true
  }

  /** 修改 */
  const handleUpdate = (row: any) => {
    drawerType.value = 'edit'
    currentData.value = { ...row }
    drawerVisible.value = true
  }

  /** 删除 */
  const handleDelete = async (row: any) => {
    const assetNos = row?.assetNo || ids.value
    if (!assetNos || (Array.isArray(assetNos) && assetNos.length === 0)) return

    try {
      await ElMessageBox.confirm(`是否确认删除资产编号为"${assetNos}"的数据项？`, '提示', {
        type: 'warning'
      })
      await delInfo(assetNos)
      ElMessage.success('删除成功')
      refreshData()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除资产失败:', error)
      }
    }
  }

  /** 导出 */
  const handleExport = async () => {
    try {
      const response: any = await exportInfo(searchParams)
      const blob = new Blob([response.data || response], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
      const link = document.createElement('a')
      link.href = window.URL.createObjectURL(blob)
      link.download = `资产台账_${new Date().getTime()}.xlsx`
      link.click()
    } catch (error) {
      console.error('导出失败:', error)
    }
  }

  onMounted(() => {
    getCategoryTree()
    // 初始化字典
    void asset_type.value
    void asset_status.value
  })
</script>

<style lang="scss" scoped>
  .asset-list-page {
    padding: 12px;
  }
  
  // 侧边栏折叠按钮样式 (同步 DeptTreeAside)
  .collapse-handle {
    top: 50%;
    transform: translateY(-50%);
    width: 22px;
    height: 50px;
    background-color: var(--default-box-color);
    border-radius: 0 10px 10px 0;
    z-index: 100;
    color: var(--art-gray-500);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    border-left: none;

    &:hover {
      color: var(--theme-color);
      background-color: var(--art-gray-100);
    }

    &.handle-expanded {
      right: -23px;
    }

    &.handle-collapsed {
      left: -8px; 
      border-left: 1px solid var(--art-gray-200);
      border-radius: 6px;
    }
  }

  :deep(.el-tree) {
    background: transparent;
    .el-tree-node__content {
      height: 38px;
      border-radius: 6px;
      &:hover {
        background-color: var(--art-gray-200) !important;
      }
    }
    .el-tree-node.is-current > .el-tree-node__content {
      background-color: var(--theme-color-light) !important;
      color: var(--theme-color);
    }
  }
</style>
