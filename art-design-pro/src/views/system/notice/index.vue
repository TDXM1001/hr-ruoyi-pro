<!-- 通知公告管理页面 (修复版) -->
<template>
  <div class="notice-page art-full-height flex flex-col p-3 overflow-hidden">
    <!-- 搜索栏 -->
    <ArtSearchBar
      v-model="formFilters"
      :items="formItems"
      @reset="handleReset"
      @search="handleSearch"
    />

    <ElCard class="art-table-card flex-1 overflow-hidden" shadow="never">
      <!-- 表格头部 -->
      <ArtTableHeader
        :loading="loading"
        v-model:columns="columnChecks"
        @refresh="refreshData"
      >
        <template #left>
          <ElButton
            v-auth="'system:notice:add'"
            type="primary"
            @click="handleAdd"
            v-ripple
          >
            新增
          </ElButton>
          <ElButton
            v-auth="'system:notice:remove'"
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

      <!-- 表格 -->
      <ArtTable
        ref="tableRef"
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
        rowKey="noticeId"
        @selection-change="handleSelectionChange"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      >
        <!-- 公告类型插槽 -->
        <template #noticeType="{ row }">
          <DictTag :options="sys_notice_type" :value="row.noticeType" />
        </template>

        <!-- 状态插槽 -->
        <template #status="{ row }">
          <DictTag :options="sys_notice_status" :value="row.status" />
        </template>

        <!-- 操作列插槽 -->
        <template #operation="{ row }">
          <div class="flex justify-end gap-2">
            <ArtButtonTable
              v-if="hasAuth('system:notice:edit')"
              type="edit"
              @click="handleUpdate(row)"
            />
            <ArtButtonTable
              v-if="hasAuth('system:notice:remove')"
              type="delete"
              @click="handleDelete(row)"
            />
          </div>
        </template>
      </ArtTable>
    </ElCard>

    <!-- 编辑弹窗 -->
    <NoticeEditDialog
      v-model="dialogVisible"
      :dialog-type="dialogType"
      :data="currentData"
      @success="refreshData"
    />
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, computed, onMounted } from 'vue'
  import { listNotice, delNotice, getNotice } from '@/api/system/notice'
  import { useTable } from '@/hooks/core/useTable'
  import { useDict } from '@/utils/dict'
  import { useAuth } from '@/hooks/core/useAuth'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import DictTag from '@/components/DictTag/index.vue'
  import { ElMessageBox, ElMessage } from 'element-plus'
  import NoticeEditDialog from './modules/notice-edit-dialog.vue'

  defineOptions({ name: 'Notice' })

  // 接入字典
  const { sys_notice_type, sys_notice_status } = useDict('sys_notice_type', 'sys_notice_status')

  // 选中数据
  const ids = ref<number[]>([])
  const multiple = ref(true)

  // 弹窗相关
  const dialogVisible = ref(false)
  const dialogType = ref<'add' | 'edit'>('add')
  const currentData = ref<any>(null)

  // 搜索相关
  const initialSearchState = {
    noticeTitle: '',
    createBy: '',
    noticeType: ''
  }

  const formFilters = reactive({ ...initialSearchState })

  const formItems = computed(() => [
    {
      label: '公告标题',
      key: 'noticeTitle',
      type: 'input',
      props: { placeholder: '请输入公告标题', clearable: true }
    },
    {
      label: '操作人员',
      key: 'createBy',
      type: 'input',
      props: { placeholder: '请输入操作人员', clearable: true }
    },
    {
      label: '类型',
      key: 'noticeType',
      type: 'select',
      props: {
        placeholder: '公告类型',
        clearable: true,
        options: sys_notice_type.value
      }
    }
  ])

  // 权限控制
  const { hasAuth } = useAuth()

  // 表格 Hook
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
      apiFn: listNotice,
      columnsFactory: () => [
        { type: 'selection', width: 55, align: 'center' },
        { prop: 'noticeId', label: '序号', width: 60, align: 'center' },
        { prop: 'noticeTitle', label: '公告标题', minWidth: 200, showOverflowTooltip: true },
        {
          prop: 'noticeType',
          label: '公告类型',
          width: 100,
          align: 'center',
          useSlot: true
        },
        {
          prop: 'status',
          label: '状态',
          width: 100,
          align: 'center',
          useSlot: true
        },
        { prop: 'createBy', label: '创建者', width: 100 },
        { prop: 'createTime', label: '创建时间', width: 170, align: 'center' },
        {
          prop: 'operation',
          label: '操作',
          width: 120,
          align: 'right',
          useSlot: true
        }
      ]
    }
  })

  /** 多选框选中数据 */
  const handleSelectionChange = (selection: any[]) => {
    ids.value = selection.map((item) => item.noticeId)
    multiple.value = !selection.length
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

  /** 新增按钮操作 */
  const handleAdd = () => {
    dialogType.value = 'add'
    currentData.value = null
    dialogVisible.value = true
  }

  /** 修改按钮操作 */
  const handleUpdate = async (row: any) => {
    const noticeId = row.noticeId || ids.value[0]
    try {
      const response: any = await getNotice(noticeId)
      currentData.value = response.data
      dialogType.value = 'edit'
      dialogVisible.value = true
    } catch (error) {
      console.error('获取公告详情失败:', error)
      ElMessage.error('获取详情失败')
    }
  }

  /** 删除按钮操作 */
  const handleDelete = async (row?: any) => {
    const noticeIds = row?.noticeId || ids.value
    if (!noticeIds || (Array.isArray(noticeIds) && noticeIds.length === 0)) return

    try {
      await ElMessageBox.confirm(`是否确认删除公告编号为"${noticeIds}"的数据项？`, '提示', {
        type: 'warning'
      })
      await delNotice(noticeIds)
      ElMessage.success('删除成功')
      refreshData()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除通知公告失败:', error)
      }
    }
  }

  onMounted(() => {
    void sys_notice_type.value
    void sys_notice_status.value
  })
</script>

<style lang="scss" scoped>
  .notice-page {
    background-color: transparent;
    gap: 12px;
  }
</style>
