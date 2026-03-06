<!-- 用户管理页面 -->
<template>
  <div class="user-page art-full-height flex gap-4">
    <!-- 左侧部门树 -->
    <ElCard class="w-64 shrink-0 art-table-card" shadow="never">
      <div class="p-4 border-b font-medium text-gray-700">组织机构</div>
      <div class="p-4">
        <ElInput
          v-model="deptName"
          placeholder="输入部门名称搜索"
          prefix-icon="Search"
          clearable
          class="mb-4"
        />
        <ElTree
          ref="deptTreeRef"
          :data="deptOptions"
          :props="{ label: 'label', children: 'children' }"
          :expand-on-click-node="false"
          :filter-node-method="filterNode"
          highlight-current
          default-expand-all
          node-key="id"
          @node-click="handleNodeClick"
        />
      </div>
    </ElCard>

    <!-- 右侧内容 -->
    <div class="flex-1 min-w-0 flex flex-col gap-4">
      <!-- 搜索栏 -->
      <ArtSearchBar
        v-model="formFilters"
        :items="formItems"
        :showExpand="false"
        @reset="handleReset"
        @search="handleSearch"
      />

      <ElCard class="art-table-card flex-1 flex flex-col overflow-hidden" shadow="never" body-class="flex-1 flex flex-col p-0">
        <!-- 表格头部 -->
        <ArtTableHeader
          :showZebra="false"
          :loading="loading"
          v-model:columns="columnChecks"
          @refresh="handleRefresh"
        >
          <template #left>
            <ElButton v-auth="'add'" type="primary" @click="handleAdd" v-ripple> 新增用户 </ElButton>
            <ElButton v-auth="'remove'" type="danger" plain :disabled="!multiple" @click="handleDelete" v-ripple>
              删除
            </ElButton>
          </template>
        </ArtTableHeader>

        <ArtTable
          ref="tableRef"
          rowKey="userId"
          :loading="loading"
          :columns="columns"
          :data="userList"
          @selection-change="handleSelectionChange"
          class="flex-1"
        />

        <!-- 分页 -->
        <div class="p-4 border-t">
          <ElPagination
            v-show="total > 0"
            :total="total"
            v-model:current-page="queryParams.pageNum"
            v-model:page-size="queryParams.pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="getList"
            @current-change="getList"
          />
        </div>
      </ElCard>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, computed, onMounted, watch, h } from 'vue'
  import { listUser, delUser, deptTreeSelect } from '@/api/system/user'
  import { useTableColumns } from '@/hooks/core/useTableColumns'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import { ElTag, ElMessageBox, ElMessage } from 'element-plus'

  defineOptions({ name: 'User' })

  // 状态管理
  const loading = ref(false)
  const userList = ref<any[]>([])
  const total = ref(0)
  const deptOptions = ref<any[]>([])
  const deptName = ref('')
  const deptTreeRef = ref()
  const ids = ref<number[]>([])
  const multiple = ref(true)

  // 搜索相关
  const initialSearchState = {
    userName: '',
    phonenumber: '',
    status: ''
  }

  const queryParams = reactive({
    pageNum: 1,
    pageSize: 10,
    deptId: undefined
  })

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

  // 表格列配置
  const { columnChecks, columns } = useTableColumns(() => [
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
        return h(
          ElTag,
          { type: row.status === '0' ? 'success' : 'info' },
          () => (row.status === '0' ? '正常' : '停用')
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
        return h('div', { style: 'text-align: right' }, [
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

  /** 监测部门名称筛选 */
  watch(deptName, (val) => {
    deptTreeRef.value?.filter(val)
  })

  /** 筛选节点 */
  const filterNode = (value: string, data: any) => {
    if (!value) return true
    return data.label.includes(value)
  }

  /** 查询部门树结构 */
  const getDeptTree = async () => {
    try {
      const response: any = await deptTreeSelect()
      deptOptions.value = response.data || []
    } catch (error) {
      console.error('获取部门树失败:', error)
    }
  }

  /** 查询用户列表 */
  const getList = async () => {
    loading.value = true
    try {
      const response: any = await listUser({ ...queryParams, ...formFilters })
      userList.value = response.rows || []
      total.value = response.total || 0
    } catch (error) {
      console.error('获取用户列表失败:', error)
    } finally {
      loading.value = false
    }
  }

  /** 节点单击事件 */
  const handleNodeClick = (data: any) => {
    queryParams.deptId = data.id
    handleSearch()
  }

  /** 多选框选中数据 */
  const handleSelectionChange = (selection: any[]) => {
    ids.value = selection.map(item => item.userId)
    multiple.value = !selection.length
  }

  /** 重置按钮操作 */
  const handleReset = () => {
    Object.assign(formFilters, initialSearchState)
    queryParams.deptId = undefined
    handleSearch()
  }

  /** 搜索按钮操作 */
  const handleSearch = () => {
    queryParams.pageNum = 1
    getList()
  }

  /** 刷新按钮操作 */
  const handleRefresh = () => {
    getList()
  }

  /** 新增按钮操作 */
  const handleAdd = () => {
    console.log('新增用户')
  }

  /** 修改按钮操作 */
  const handleUpdate = (row: any) => {
    console.log('修改用户', row)
  }

  /** 删除按钮操作 */
  const handleDelete = async (row?: any) => {
    const userIds = row?.userId || ids.value
    try {
      await ElMessageBox.confirm(`是否确认删除用户编号为"${userIds}"的数据项？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await delUser(userIds)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除用户失败:', error)
      }
    }
  }

  onMounted(() => {
    getDeptTree()
    getList()
  })
</script>

<style lang="scss" scoped>
  .user-page {
    padding: 0;
  }
</style>
