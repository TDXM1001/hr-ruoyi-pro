<!-- 资产台账页面 -->
<template>
  <div class="asset-list-page art-full-height flex !flex-row p-3 overflow-hidden">
    <!-- 左侧分类树 -->
    <div class="category-tree-wrapper mr-3 transition-all duration-300 flex-shrink-0" :style="{ width: isTreeCollapse ? '0px' : '200px' }">
      <ElCard class="h-full !m-0 overflow-hidden" shadow="never" v-show="!isTreeCollapse">
         <div class="aside-header flex justify-between items-center px-4 py-3 border-b">
            <span class="text-sm font-bold">资产分类</span>
            <ArtSvgIcon icon="ri:organization-chart" />
         </div>
         <div class="p-2">
            <ElInput v-model="filterText" placeholder="搜索分类" size="small" clearable class="mb-2" />
            <ElScrollbar class="h-[calc(100vh-250px)]">
              <ElTree
                ref="treeRef"
                :data="categoryOptions"
                :props="{ label: 'name', children: 'children' }"
                highlight-current
                node-key="id"
                default-expand-all
                :filter-node-method="filterNode"
                @node-click="handleNodeClick"
              />
            </ElScrollbar>
         </div>
      </ElCard>
    </div>

    <!-- 右侧容器 -->
    <div class="flex-1 min-w-0 flex flex-col h-full overflow-hidden">
      <!-- 搜索栏 -->
      <ArtSearchBar
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

        <!-- 表格 -->
        <ArtTable
          ref="tableRef"
          :loading="loading"
          :data="data"
          :columns="columns"
          :pagination="pagination"
          rowKey="assetNo"
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
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import { handleTree } from '@/utils/ruoyi'
  import { ElMessageBox, ElMessage } from 'element-plus'
  import AssetEditDrawer from './modules/asset-edit-drawer.vue'

  defineOptions({ name: 'AssetList' })

  // 状态管理
  const filterText = ref('')
  const isTreeCollapse = ref(false)
  const treeRef = ref()
  const categoryOptions = ref<any[]>([])
  
  // 抽屉相关
  const drawerVisible = ref(false)
  const drawerType = ref<'add' | 'edit'>('add')
  const currentData = ref<any>()

  // 搜索相关
  const initialSearchState = {
    assetNo: '',
    assetName: '',
    categoryId: undefined,
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
        placeholder: '请选择状态',
        clearable: true,
        options: [
          { label: '正常', value: 1 },
          { label: '领用中', value: 2 },
          { label: '维修中', value: 3 },
          { label: '盘点中', value: 4 },
          { label: '已报废', value: 5 }
        ]
      }
    }
  ])

  // 资产类型 & 状态映射
  const typeMap: any = { 1: '不动产', 2: '固定资产' }
  const statusMap: any = {
    1: { label: '正常', type: 'success' },
    2: { label: '领用中', type: 'primary' },
    3: { label: '维修中', type: 'warning' },
    4: { label: '盘点中', type: 'info' },
    5: { label: '已报废', type: 'danger' }
  }

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
        { prop: 'assetNo', label: '资产编号', width: 120 },
        { prop: 'assetName', label: '资产名称', minWidth: 150 },
        { 
          prop: 'assetType', 
          label: '资产类型', 
          width: 100, 
          align: 'center',
          formatter: (row: any) => typeMap[row.assetType] || '-'
        },
        { 
          prop: 'status', 
          label: '状态', 
          width: 100, 
          align: 'center',
          formatter: (row: any) => {
            const status = statusMap[row.status] || { label: '未知', type: 'info' }
            return h('span', { class: `el-tag el-tag--${status.type} el-tag--light` }, status.label)
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
    try {
      await ElMessageBox.confirm(`是否确认删除资产编号为"${row.assetNo}"的数据项？`, '提示', {
        type: 'warning'
      })
      await delInfo(row.assetNo)
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
      const response = await exportInfo(searchParams)
      const blob = new Blob([response as any], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
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
  })
</script>

<style lang="scss" scoped>
  .asset-list-page {
    padding: 12px;
  }
  .category-tree-wrapper {
    overflow: hidden;
  }
</style>
