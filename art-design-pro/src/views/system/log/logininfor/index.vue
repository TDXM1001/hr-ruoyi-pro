<!-- 登录日志页面 (路径: src/views/system/log/logininfor/index.vue) -->
<template>
  <div class="logininfor-page art-full-height flex flex-col p-3 overflow-hidden">
    <!-- 搜索栏 -->
    <ArtSearchBar
      v-model="formFilters"
      :items="formItems"
      @reset="handleReset"
      @search="handleSearch"
    />

    <ElCard class="art-table-card flex-1 overflow-hidden" shadow="never">
      <!-- 表格头部 -->
      <ArtTableHeader :loading="loading" @refresh="refreshData">
        <template #left>
          <ElButton
            v-auth="'monitor:logininfor:remove'"
            type="danger"
            plain
            :disabled="!multiple"
            @click="handleDelete"
            v-ripple
          >
            删除
          </ElButton>
          <ElButton
            v-auth="'monitor:logininfor:remove'"
            type="danger"
            plain
            @click="handleClean"
            v-ripple
          >
            清空
          </ElButton>
          <ElButton
            v-auth="'monitor:logininfor:unlock'"
            type="primary"
            plain
            :disabled="!single"
            @click="handleUnlock"
            v-ripple
          >
            解锁
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
        rowKey="infoId"
        @selection-change="handleSelectionChange"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, computed, onMounted, h } from 'vue'
  import { list, delLogininfor, cleanLogininfor, unlockLogininfor } from '@/api/system/log/logininfor'
  import { useTable } from '@/hooks/core/useTable'
  import { useDict } from '@/utils/dict'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import DictTag from '@/components/DictTag/index.vue'
  import { ElMessageBox, ElMessage } from 'element-plus'

  defineOptions({ name: 'Logininfor' })

  // 接入字典
  const { sys_common_status } = useDict('sys_common_status')

  // 选中数据
  const ids = ref<number[]>([])
  const selectNames = ref<string[]>([])
  const multiple = ref(true)
  const single = ref(true)

  // 搜索相关
  const initialSearchState = {
    ipaddr: '',
    userName: '',
    status: ''
  }

  const formFilters = reactive({ ...initialSearchState })

  const formItems = computed(() => [
    {
      label: '登录地址',
      key: 'ipaddr',
      type: 'input',
      props: { placeholder: '请输入登录地址', clearable: true }
    },
    {
      label: '用户名称',
      key: 'userName',
      type: 'input',
      props: { placeholder: '请输入用户名称', clearable: true }
    },
    {
      label: '状态',
      key: 'status',
      type: 'select',
      props: {
        placeholder: '登录状态',
        clearable: true,
        options: sys_common_status.value
      }
    }
  ])

  // 表格 Hook
  const {
    columns,
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
      apiFn: list,
      columnsFactory: () => [
        { type: 'selection', width: 55, align: 'center' },
        { prop: 'infoId', label: '访问编号', width: 100 },
        { prop: 'userName', label: '用户名称', minWidth: 100, sortable: 'custom' },
        { prop: 'ipaddr', label: '登录地址', minWidth: 120 },
        { prop: 'loginLocation', label: '登录地点', minWidth: 100 },
        { prop: 'browser', label: '浏览器', width: 120 },
        { prop: 'os', label: '操作系统', width: 120 },
        {
          prop: 'status',
          label: '登录状态',
          width: 100,
          align: 'center',
          render: (row: any) => {
            return h(DictTag, { options: sys_common_status.value, value: row.status })
          }
        },
        { prop: 'msg', label: '提示消息', minWidth: 120 },
        { prop: 'loginTime', label: '访问时间', width: 170, align: 'center', sortable: 'custom' },
        {
          prop: 'operation',
          label: '操作',
          width: 80,
          align: 'right',
          render: (row: any) => {
            return h('div', { class: 'flex justify-end' }, [
              h(ArtButtonTable, {
                type: 'delete',
                auth: 'monitor:logininfor:remove',
                onClick: () => handleDelete(row)
              })
            ])
          }
        }
      ]
    }
  })

  // 修复：确保表格在字典数据到达后能正确重绘渲染
  const tableRef = ref()

  /** 多选框选中数据 */
  const handleSelectionChange = (selection: any[]) => {
    ids.value = selection.map((item) => item.infoId)
    selectNames.value = selection.map((item) => item.userName)
    multiple.value = !selection.length
    single.value = selection.length !== 1
  }

  /** 重置按钮操作 */
  const handleReset = () => {
    Object.assign(formFilters, initialSearchState)
    resetSearchParams()
  }

  /** 搜索按钮操作 */
  const handleSearch = () => {
    Object.assign(searchParams, formFilters)
    getData()
  }

  /** 删除按钮操作 */
  const handleDelete = async (row?: any) => {
    const infoIds = row?.infoId || ids.value
    if (!infoIds || (Array.isArray(infoIds) && infoIds.length === 0)) return

    try {
      await ElMessageBox.confirm(`是否确认删除访问编号为"${infoIds}"的数据项？`, '提示', {
        type: 'warning'
      })
      await delLogininfor(infoIds)
      ElMessage.success('删除成功')
      refreshData()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除登录日志失败:', error)
      }
    }
  }

  /** 清空按钮操作 */
  const handleClean = async () => {
    try {
      await ElMessageBox.confirm('是否确认清空所有登录日志数据项？', '提示', {
        type: 'warning'
      })
      await cleanLogininfor()
      ElMessage.success('清空成功')
      refreshData()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('清空登录日志失败:', error)
      }
    }
  }

  /** 解锁按钮操作 */
  const handleUnlock = async () => {
    const username = selectNames.value[0]
    try {
      await ElMessageBox.confirm(`是否确认解锁用户"${username}"数据项？`, '提示')
      await unlockLogininfor(username)
      ElMessage.success(`用户${username}解锁成功`)
    } catch (error) {
      if (error !== 'cancel') {
        console.error('解锁用户失败:', error)
      }
    }
  }

  onMounted(() => {
    // 强制触发字典刷新以补全 options
    void sys_common_status.value
  })
</script>

<style lang="scss" scoped>
  .logininfor-page {
    background-color: transparent;
    gap: 12px;
  }
</style>
