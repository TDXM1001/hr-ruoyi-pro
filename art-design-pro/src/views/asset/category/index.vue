<template>
  <div class="asset-category-page art-full-height">
    <!-- 搜索栏 -->
    <ArtSearchBar
      v-model="formFilters"
      :items="formItems"
      :showExpand="false"
      @reset="handleReset"
      @search="handleSearch"
    />

    <div class="asset-category-layout">
      <ElCard class="art-table-card category-list-card" shadow="never">
        <!-- 表格头部 -->
        <ArtTableHeader
          :showZebra="false"
          :loading="loading"
          v-model:columns="columnChecks"
          @refresh="handleRefresh"
        >
          <template #left>
            <ElButton v-auth="'asset:category:add'" type="primary" @click="handleAdd" v-ripple>
              新增分类
            </ElButton>
            <ElButton @click="toggleExpand" v-ripple>
              {{ isExpanded ? '收起' : '展开' }}
            </ElButton>
          </template>
        </ArtTableHeader>

        <ArtTable
          ref="tableRef"
          rowKey="id"
          :loading="loading"
          :columns="columns"
          :data="categoryList"
          :stripe="false"
          highlight-current-row
          :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
          :default-expand-all="isExpanded"
          @current-change="handleCurrentChange"
          @row-click="handleCurrentChange"
        />
      </ElCard>

      <CategoryAttrManager
        :category-id="currentCategory?.id"
        :category-name="currentCategory?.name"
      />
    </div>

    <!-- 资产分类编辑弹窗 -->
    <CategoryEditDialog
      v-model="dialogVisible"
      :dialog-type="dialogType"
      :category-data="editingCategory"
      @success="getList"
    />
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, computed, onMounted, nextTick, h } from 'vue'
  import { listCategory, delCategory } from '@/api/asset/category'
  import { handleTree } from '@/utils/ruoyi'
  import { useTableColumns } from '@/hooks/core/useTableColumns'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import { ElMessageBox, ElMessage } from 'element-plus'
  import CategoryEditDialog from './modules/category-edit-dialog.vue'
  import CategoryAttrManager from './modules/category-attr-manager.vue'

  defineOptions({ name: 'AssetCategory' })

  // 状态管理
  const loading = ref(false)
  const isExpanded = ref(true)
  const tableRef = ref()
  const categoryList = ref<any[]>([])
  const currentCategory = ref<any>()

  // 弹窗相关
  const dialogVisible = ref(false)
  const dialogType = ref<'add' | 'edit'>('add')
  const editingCategory = ref<any>()

  // 搜索相关
  const initialSearchState = {
    name: '',
    code: ''
  }

  const formFilters = reactive({ ...initialSearchState })

  const formItems = computed(() => [
    {
      label: '分类名称',
      key: 'name',
      type: 'input',
      props: { placeholder: '请输入分类名称', clearable: true }
    },
    {
      label: '分类编码',
      key: 'code',
      type: 'input',
      props: { placeholder: '请输入分类编码', clearable: true }
    }
  ])

  // 表格列配置
  const { columnChecks, columns } = useTableColumns(() => [
    {
      prop: 'name',
      label: '分类名称',
      minWidth: 200
    },
    {
      prop: 'code',
      label: '分类编码',
      width: 150,
      align: 'center'
    },
    {
      prop: 'createTime',
      label: '创建时间',
      width: 180,
      align: 'center',
      formatter: (row: any) => row.createTime || '-'
    },
    {
      prop: 'operation',
      label: '操作',
      width: 200,
      align: 'right',
      formatter: (row: any) => {
        return h('div', { class: 'flex justify-end' }, [
          h(ArtButtonTable, {
            type: 'edit',
            onClick: () => handleEdit(row)
          }),
          h(ArtButtonTable, {
            type: 'add',
            title: '新增',
            onClick: () => handleAdd(row)
          }),
          h(ArtButtonTable, {
            type: 'delete',
            onClick: () => handleDelete(row)
          })
        ])
      }
    }
  ])

  /**
   * 获取分类列表
   */
  const getList = async () => {
    loading.value = true
    try {
      const response: any = await listCategory(formFilters)
      const data = Array.isArray(response) ? response : response.data || response.rows || []
      categoryList.value = handleTree(data, 'id')
      syncCurrentCategory()
    } catch (error) {
      console.error('获取资产分类列表失败:', error)
    } finally {
      loading.value = false
    }
  }

  /** 刷新分类树后，同步当前高亮行，保证模板面板上下文稳定。 */
  const syncCurrentCategory = () => {
    const currentCategoryId = currentCategory.value?.id
    if (!currentCategoryId) return
    const matchedCategory = findCategoryById(categoryList.value, currentCategoryId)
    currentCategory.value = matchedCategory
    nextTick(() => {
      tableRef.value?.elTableRef?.setCurrentRow?.(matchedCategory || null)
    })
  }

  /** 在树形分类中递归查找指定节点。 */
  const findCategoryById = (rows: any[], categoryId: number): any => {
    for (const row of rows) {
      if (row.id === categoryId) {
        return row
      }
      if (row.children?.length) {
        const childMatch = findCategoryById(row.children, categoryId)
        if (childMatch) {
          return childMatch
        }
      }
    }
    return undefined
  }

  /**
   * 重置搜索
   */
  const handleReset = () => {
    Object.assign(formFilters, initialSearchState)
    getList()
  }

  /**
   * 搜索
   */
  const handleSearch = () => {
    getList()
  }

  /**
   * 刷新
   */
  const handleRefresh = () => {
    getList()
  }

  /**
   * 切换展开/收起
   */
  const toggleExpand = () => {
    isExpanded.value = !isExpanded.value
    nextTick(() => {
      if (tableRef.value?.elTableRef && categoryList.value) {
        const processRows = (rows: any[]) => {
          rows.forEach((row) => {
            if (row.children?.length) {
              tableRef.value.elTableRef.toggleRowExpansion(row, isExpanded.value)
              processRows(row.children)
            }
          })
        }
        processRows(categoryList.value)
      }
    })
  }

  /**
   * 新增分类
   */
  const handleAdd = (row?: any) => {
    dialogType.value = 'add'
    editingCategory.value = row
    dialogVisible.value = true
  }

  /**
   * 修改分类
   */
  const handleEdit = (row: any) => {
    dialogType.value = 'edit'
    editingCategory.value = { ...row }
    dialogVisible.value = true
  }

  /**
   * 切换当前分类，右侧模板管理区跟随刷新。
   */
  const handleCurrentChange = (row?: any) => {
    currentCategory.value = row
  }

  /**
   * 删除分类
   */
  const handleDelete = async (row: any) => {
    try {
      await ElMessageBox.confirm(`是否确认删除名称为"${row.name}"的数据项？`, '提示', {
        type: 'warning'
      })
      await delCategory(row.id)
      ElMessage.success('删除成功')
      if (currentCategory.value?.id === row.id) {
        currentCategory.value = undefined
      }
      getList()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除分类失败:', error)
      }
    }
  }

  onMounted(() => {
    getList()
  })
</script>

<style lang="scss" scoped>
  .asset-category-page {
    padding: 12px;
  }

  .asset-category-layout {
    display: grid;
    grid-template-columns: minmax(0, 1.5fr) minmax(380px, 1fr);
    gap: 12px;
    align-items: start;
  }

  .category-list-card {
    min-width: 0;
  }

  @media (max-width: 1280px) {
    .asset-category-layout {
      grid-template-columns: 1fr;
    }
  }
</style>
