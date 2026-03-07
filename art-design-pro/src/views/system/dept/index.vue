<!-- 部门管理页面 -->
<template>
  <div class="dept-page art-full-height">
    <!-- 搜索栏 -->
    <ArtSearchBar
      v-model="formFilters"
      :items="formItems"
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
          <ElButton v-auth="'system:dept:add'" type="primary" @click="handleAddDept" v-ripple>
            新增部门
          </ElButton>
          <ElButton @click="toggleExpand" v-ripple>
            {{ isExpanded ? '收起' : '展开' }}
          </ElButton>
        </template>
      </ArtTableHeader>

      <ArtTable
        ref="tableRef"
        rowKey="deptId"
        :loading="loading"
        :columns="columns"
        :data="deptList"
        :stripe="false"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        :default-expand-all="isExpanded"
      />
    </ElCard>

    <!-- 部门编辑弹窗 -->
    <DeptEditDialog
      v-model="dialogVisible"
      :dialog-type="dialogType"
      :dept-data="currentData"
      @success="getList"
    />
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, computed, onMounted, nextTick, h } from 'vue'
  import { listDept, delDept } from '@/api/system/dept'
  import { handleTree } from '@/utils/ruoyi'
  import { useTableColumns } from '@/hooks/core/useTableColumns'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import { ElTag, ElMessageBox, ElMessage } from 'element-plus'
  import DeptEditDialog from './modules/dept-edit-dialog.vue'

  defineOptions({ name: 'Dept' })

  // 状态管理
  const loading = ref(false)
  const isExpanded = ref(true)
  const tableRef = ref()
  const deptList = ref<any[]>([])

  // 弹窗相关
  const dialogVisible = ref(false)
  const dialogType = ref<'add' | 'edit'>('add')
  const currentData = ref<any>()

  // 搜索相关
  const initialSearchState = {
    deptName: '',
    status: ''
  }

  const formFilters = reactive({ ...initialSearchState })

  const formItems = computed(() => [
    {
      label: '部门名称',
      key: 'deptName',
      type: 'input',
      props: { placeholder: '请输入部门名称', clearable: true }
    },
    {
      label: '状态',
      key: 'status',
      type: 'select',
      options: [
        { label: '正常', value: '0' },
        { label: '停用', value: '1' }
      ],
      props: { placeholder: '部门状态', clearable: true }
    }
  ])

  // 表格列配置
  const { columnChecks, columns } = useTableColumns(() => [
    {
      prop: 'deptName',
      label: '部门名称',
      minWidth: 150
    },
    {
      prop: 'orderNum',
      label: '排序',
      width: 100,
      align: 'center'
    },
    {
      prop: 'status',
      label: '状态',
      width: 100,
      align: 'center',
      formatter: (row: any) => {
        return h(ElTag, { type: row.status === '0' ? 'success' : 'info' }, () =>
          row.status === '0' ? '正常' : '停用'
        )
      }
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
        const buttonStyle = { style: 'text-align: right' }
        return h('div', buttonStyle, [
          h(ArtButtonTable, {
            type: 'edit',
            onClick: () => handleEditDept(row)
          }),
          h(ArtButtonTable, {
            type: 'add',
            title: '新增',
            onClick: () => handleAddDept(row)
          }),
          h(ArtButtonTable, {
            type: 'delete',
            onClick: () => handleDeleteDept(row)
          })
        ])
      }
    }
  ])

  /**
   * 获取部门列表
   */
  const getList = async () => {
    loading.value = true
    try {
      const response = await listDept(formFilters)
      // 若依接口通常返回 res.data，但 http 拦截器可能已经提取
      // 兼容处理不同返回集
      const data = Array.isArray(response)
        ? response
        : (response as any).data || (response as any).rows || []
      deptList.value = handleTree(data, 'deptId')
    } catch (error) {
      console.error('获取部门列表失败:', error)
    } finally {
      loading.value = false
    }
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
      if (tableRef.value?.elTableRef && deptList.value) {
        const processRows = (rows: any[]) => {
          rows.forEach((row) => {
            if (row.children?.length) {
              tableRef.value.elTableRef.toggleRowExpansion(row, isExpanded.value)
              processRows(row.children)
            }
          })
        }
        processRows(deptList.value)
      }
    })
  }

  /**
   * 新增部门
   */
  const handleAddDept = (row?: any) => {
    dialogType.value = 'add'
    currentData.value = row
    dialogVisible.value = true
  }

  /**
   * 修改部门
   */
  const handleEditDept = (row: any) => {
    dialogType.value = 'edit'
    currentData.value = { ...row }
    dialogVisible.value = true
  }

  /**
   * 删除部门
   */
  const handleDeleteDept = async (row: any) => {
    try {
      await ElMessageBox.confirm(`是否确认删除名称为"${row.deptName}"的数据项？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await delDept(row.deptId)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除部门失败:', error)
      }
    }
  }

  onMounted(() => {
    getList()
  })
</script>

<style lang="scss" scoped>
  .dept-page {
    padding: 0;
  }
</style>
