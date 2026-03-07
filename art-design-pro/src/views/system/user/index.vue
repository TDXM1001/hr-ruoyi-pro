<!-- 用户管理页面 -->
<template>
  <div class="user-page art-full-height flex !flex-row p-3 overflow-hidden">
    <!-- 左侧部门树封装组件 -->
    <DeptTreeAside
      :data="deptOptions"
      :width="200"
      :current-node-key="currentNodeKey"
      @node-click="handleNodeClick"
    />

    <!-- 右侧主体内容 -->
    <div class="flex-1 min-w-0 flex flex-col h-full overflow-hidden relative">
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
            <ElButton v-auth="'system:user:add'" type="primary" @click="handleAdd" v-ripple>
              新增用户
            </ElButton>
            <ElButton
              v-auth="'system:user:remove'"
              type="danger"
              plain
              :disabled="!multiple"
              @click="handleDelete"
              v-ripple
            >
              删除
            </ElButton>
          </template>
        </ArtTableHeader>

        <!-- 表格 - 使用 useTable 的数据、分页和事件 -->
        <ArtTable
          ref="tableRef"
          :loading="loading"
          :data="data"
          :columns="columns"
          :pagination="pagination"
          rowKey="userId"
          @selection-change="handleSelectionChange"
          @pagination:size-change="handleSizeChange"
          @pagination:current-change="handleCurrentChange"
        />
      </ElCard>
    </div>

    <!-- 用户编辑弹窗 -->
    <UserEditDialog
      v-model="dialogVisible"
      :dialog-type="dialogType"
      :user-data="currentData"
      @success="refreshData"
    />
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, computed, onMounted, h } from 'vue'
  import { listUser, delUser, deptTreeSelect } from '@/api/system/user'
  import { useTable } from '@/hooks/core/useTable'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import DeptTreeAside from '@/components/business/DeptTreeAside/index.vue'
  import { ElTag, ElMessageBox, ElMessage } from 'element-plus'
  import UserEditDialog from './modules/user-edit-dialog.vue'

  defineOptions({ name: 'User' })

  // 部门树数据
  const deptOptions = ref<any[]>([])
  const currentNodeKey = ref<number | string | null>(null)
  const ids = ref<number[]>([])
  const multiple = ref(true)

  // 弹窗相关
  const dialogVisible = ref(false)
  const dialogType = ref<'add' | 'edit'>('add')
  const currentData = ref<any>()

  // 搜索相关
  const initialSearchState = {
    userName: '',
    phonenumber: '',
    status: ''
  }

  const formFilters = reactive({ ...initialSearchState })

  const formItems = computed(() => [
    {
      label: '用户名称',
      key: 'userName',
      type: 'input',
      props: { placeholder: '请输入用户名称', clearable: true }
    },
    {
      label: '手机号码',
      key: 'phonenumber',
      type: 'input',
      props: { placeholder: '请输入手机号码', clearable: true }
    },
    {
      label: '状态',
      key: 'status',
      type: 'select',
      options: [
        { label: '正常', value: '0' },
        { label: '停用', value: '1' }
      ],
      props: { placeholder: '用户状态', clearable: true }
    }
  ])

  // 使用 useTable 钩子 - 参考 role/index.vue 的模式
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
      apiFn: listUser,
      apiParams: {
        pageNum: 1,
        pageSize: 10,
        deptId: undefined
      },
      columnsFactory: () => [
        { type: 'selection', width: 55, align: 'center' },
        { prop: 'userName', label: '用户名称', minWidth: 100 },
        { prop: 'nickName', label: '用户昵称', minWidth: 100 },
        { prop: 'dept.deptName', label: '部门', minWidth: 120 },
        { prop: 'phonenumber', label: '手机号码', width: 120 },
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
        { prop: 'createTime', label: '创建时间', width: 170, align: 'center' },
        {
          prop: 'operation',
          label: '操作',
          width: 160,
          align: 'right',
          formatter: (row: any) => {
            const buttonStyle = { style: 'text-align: right' }
            return h('div', buttonStyle, [
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

  /** 查询部门树结构 */
  const getDeptTree = async () => {
    try {
      const response: any = await deptTreeSelect()
      deptOptions.value = Array.isArray(response) ? response : response.data || []
    } catch (error) {
      console.error('获取部门树失败:', error)
    }
  }

  /** 节点单击事件 - 部门树点击筛选 */
  const handleNodeClick = (data: any) => {
    currentNodeKey.value = data.id
    searchParams.deptId = data.id
    refreshData()
  }

  /** 多选框选中数据 */
  const handleSelectionChange = (selection: any[]) => {
    ids.value = selection.map((item) => item.userId)
    multiple.value = !selection.length
  }

  /** 重置按钮操作 */
  const handleReset = () => {
    Object.assign(formFilters, initialSearchState)
    searchParams.deptId = undefined
    currentNodeKey.value = null
    resetSearchParams()
  }

  /** 搜索按钮操作 */
  const handleSearch = () => {
    // 合并搜索参数
    Object.assign(searchParams, formFilters)
    getData()
  }

  /** 新增按钮操作 */
  const handleAdd = () => {
    dialogType.value = 'add'
    currentData.value = undefined
    dialogVisible.value = true
  }

  /** 修改按钮操作 */
  const handleUpdate = (row: any) => {
    dialogType.value = 'edit'
    currentData.value = { ...row }
    dialogVisible.value = true
  }

  /** 删除按钮操作 */
  const handleDelete = async (row?: any) => {
    const userIds = row?.userId || ids.value
    if (!userIds || (Array.isArray(userIds) && userIds.length === 0)) return

    try {
      await ElMessageBox.confirm(`是否确认删除用户编号为"${userIds}"的数据项？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await delUser(userIds)
      ElMessage.success('删除成功')
      refreshData()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除用户失败:', error)
      }
    }
  }

  onMounted(() => {
    getDeptTree()
  })
</script>

<style lang="scss" scoped>
  .user-page {
    background-color: transparent;
    padding: 12px;
    gap: 12px;
  }
</style>
