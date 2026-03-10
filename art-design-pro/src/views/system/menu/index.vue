<!-- 菜单管理页面 -->
<template>
  <div class="menu-page art-full-height">
    <!-- 搜索栏 -->
    <ArtSearchBar
      v-model="formFilters"
      :items="searchFormItems"
      :showExpand="false"
      @reset="handleReset"
      @search="handleSearch"
    />

    <ElCard class="art-table-card" shadow="never">
      <!-- 表格头部 -->
      <ArtTableHeader
        :showZebra="false"
        :loading="loading"
        v-model:columns="columnChecks"
        @refresh="handleRefresh"
      >
        <template #left>
          <ElButton
            type="primary"
            plain
            icon="Plus"
            @click="handleAdd()"
            v-auth="'system:menu:add'"
            v-ripple
          >
            新增
          </ElButton>
          <ElButton type="info" plain icon="Sort" @click="toggleExpandAll" v-ripple>
            展开/折叠
          </ElButton>
        </template>
      </ArtTableHeader>

      <ArtTable
        ref="tableRef"
        row-key="menuId"
        :loading="loading"
        :columns="columns"
        :data="menuList"
        :stripe="false"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        :default-expand-all="isExpandAll"
      />

      <!-- 菜单弹窗 -->
      <MenuDialog v-model:visible="dialogVisible" :editData="editData" @success="getMenuList" />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, computed, onMounted, h, nextTick } from 'vue'
  import { ElMessageBox, ElMessage } from 'element-plus'
  import ArtSearchBar from '@/components/core/forms/art-search-bar/index.vue'
  import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
  import ArtTable from '@/components/core/tables/art-table/index.vue'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import DictTag from '@/components/DictTag/index.vue'
  import { useTableColumns } from '@/hooks/core/useTableColumns'
  import { useDict } from '@/utils/dict'
  import MenuDialog from './modules/menu-dialog.vue'
  import { listMenu, delMenu } from '@/api/system/menu'
  import type { SysMenu } from '@/api/system/menu'
  // import { Icon } from '@iconify/vue'

  defineOptions({ name: 'MenuManagement' })

  // 接入字典
  const { sys_normal_disable } = useDict('sys_normal_disable')

  // 状态管理
  const loading = ref(false)
  const isExpandAll = ref(false)
  const tableRef = ref()
  const menuList = ref<SysMenu[]>([])

  // 弹窗相关
  const dialogVisible = ref(false)
  const editData = ref<SysMenu | null>(null)

  // 搜索相关
  const initialSearchState = {
    menuName: '',
    status: ''
  }

  const formFilters = reactive({ ...initialSearchState })

  const searchFormItems = computed(() => [
    {
      label: '菜单名称',
      key: 'menuName',
      type: 'input',
      props: { placeholder: '请输入菜单名称', clearable: true }
    },
    {
      label: '状态',
      key: 'status',
      type: 'select',
      props: {
        placeholder: '菜单状态',
        clearable: true,
        options: sys_normal_disable.value
      }
    }
  ])

  // 表格列配置
  const { columnChecks, columns } = useTableColumns(() => [
    {
      prop: 'menuName',
      label: '菜单名称',
      minWidth: 160
    },
    {
      prop: 'orderNum',
      label: '排序',
      width: 80,
      align: 'center'
    },
    {
      prop: 'perms',
      label: '权限标识',
      minWidth: 150,
      showOverflowTooltip: true
    },
    {
      prop: 'component',
      label: '组件路径',
      minWidth: 150,
      showOverflowTooltip: true
    },
    {
      prop: 'status',
      label: '状态',
      width: 80,
      align: 'center',
      formatter: (row: SysMenu) => {
        return h(DictTag, { options: sys_normal_disable.value, value: row.status })
      }
    },
    {
      prop: 'createTime',
      label: '创建时间',
      width: 160,
      align: 'center'
    },
    {
      prop: 'operation',
      label: '操作',
      width: 200,
      align: 'right',
      fixed: 'right',
      formatter: (row: SysMenu) => {
        return h('div', { class: 'flex justify-end gap-2' }, [
          h(ArtButtonTable, {
            type: 'add',
            title: '新增',
            onClick: () => handleAdd(row)
          }),
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
  ])

  /** 查询菜单列表 */
  const getMenuList = async () => {
    loading.value = true
    try {
      const res = await listMenu(formFilters)
      menuList.value = handleTree(res, 'menuId')
    } catch (error) {
      console.error('获取菜单列表失败', error)
    } finally {
      loading.value = false
    }
  }

  /** 构造树型结构 */
  const handleTree = (data: any[], id: string, parentId = 'parentId', children = 'children') => {
    const config = { id, parentId, children }
    const childrenListMap: any = {}
    const nodeIds: any = {}
    const tree = []

    for (const d of data) {
      const pId = d[config.parentId]
      if (childrenListMap[pId] == null) {
        childrenListMap[pId] = []
      }
      nodeIds[d[config.id]] = d
      childrenListMap[pId].push(d)
    }

    for (const d of data) {
      const pId = d[config.parentId]
      if (nodeIds[pId] == null) {
        tree.push(d)
      }
    }

    for (const t of tree) {
      adaptToChildrenList(t)
    }

    function adaptToChildrenList(o: any) {
      if (childrenListMap[o[config.id]] !== null) {
        o[config.children] = childrenListMap[o[config.id]]
      }
      if (o[config.children]) {
        for (const c of o[config.children]) {
          adaptToChildrenList(c)
        }
      }
    }
    return tree
  }

  /** 重置按钮操作 */
  const handleReset = () => {
    Object.assign(formFilters, initialSearchState)
    getMenuList()
  }

  /** 搜索按钮操作 */
  const handleSearch = () => {
    getMenuList()
  }

  /** 刷新操作 */
  const handleRefresh = () => {
    getMenuList()
  }

  /** 新增按钮操作 */
  const handleAdd = (row?: SysMenu) => {
    editData.value = null
    if (row != null && row.menuId) {
      // 如果点击行内新增，作为子菜单
      editData.value = { parentId: row.menuId } as any
    } else {
      editData.value = { parentId: 0 } as any
    }
    dialogVisible.value = true
  }

  /** 修改按钮操作 */
  const handleUpdate = (row: SysMenu) => {
    editData.value = { ...row }
    dialogVisible.value = true
  }

  /** 删除按钮操作 */
  const handleDelete = (row: SysMenu) => {
    ElMessageBox.confirm(`是否确认删除名称为"${row.menuName}"的数据项?`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(() => {
        return delMenu(row.menuId)
      })
      .then(() => {
        getMenuList()
        ElMessage.success('删除成功')
      })
      .catch(() => {})
  }

  /** 展开/折叠操作 */
  const toggleExpandAll = () => {
    isExpandAll.value = !isExpandAll.value
    nextTick(() => {
      const elTable = tableRef.value?.elTableRef
      if (elTable && menuList.value) {
        const toggleRow = (data: any[]) => {
          data.forEach((item) => {
            elTable.toggleRowExpansion(item, isExpandAll.value)
            if (item.children && item.children.length > 0) {
              toggleRow(item.children)
            }
          })
        }
        toggleRow(menuList.value)
      }
    })
  }

  onMounted(() => {
    getMenuList()
  })
</script>

<style scoped lang="scss">
  .menu-page {
    padding: 20px;
    background-color: var(--art-bg-color);
  }
</style>
