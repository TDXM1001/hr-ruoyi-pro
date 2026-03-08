<!-- 岗位管理页面 -->
<template>
  <div class="post-page art-full-height">
    <!-- 搜索栏 -->
    <ArtSearchBar
      :key="dicts.sys_normal_disable.value.length"
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
          <ElButton v-auth="'system:post:add'" type="primary" @click="handleAdd" v-ripple>
            新增岗位
          </ElButton>
          <ElButton
            v-auth="'system:post:remove'"
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

      <ArtTable
        ref="tableRef"
        rowKey="postId"
        :loading="loading"
        :columns="columns"
        :data="postList"
        :pagination="{ current: queryParams.pageNum, size: queryParams.pageSize, total: total }"
        @pagination:size-change="
          (val: number) => {
            queryParams.pageSize = val
            getList()
          }
        "
        @pagination:current-change="
          (val: number) => {
            queryParams.pageNum = val
            getList()
          }
        "
        @selection-change="handleSelectionChange"
      />
    </ElCard>

    <PostEditDialog
      v-model="dialogVisible"
      :dialog-type="dialogType"
      :post-data="currentData"
      @success="getList"
    />
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, computed, onMounted, h } from 'vue'
  import { listPost, delPost } from '@/api/system/post'
  import { useTableColumns } from '@/hooks/core/useTableColumns'
  import { useDict } from '@/utils/dict'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import DictTag from '@/components/DictTag/index.vue'
  import { ElMessageBox, ElMessage } from 'element-plus'
  import PostEditDialog from './modules/post-edit-dialog.vue'

  defineOptions({ name: 'Post' })

  // 接入字典
  const dicts = useDict('sys_normal_disable')

  // 状态管理
  const loading = ref(false)
  const postList = ref<any[]>([])
  const total = ref(0)
  const ids = ref<number[]>([])
  const multiple = ref(true)

  // 弹窗相关
  const dialogVisible = ref(false)
  const dialogType = ref<'add' | 'edit'>('add')
  const currentData = ref<any>()

  // 搜索相关
  const initialSearchState = {
    postCode: '',
    postName: '',
    status: ''
  }

  const queryParams = reactive({
    pageNum: 1,
    pageSize: 10
  })

  const formFilters = reactive({ ...initialSearchState })

  const formItems = computed(() => [
    {
      label: '岗位编码',
      key: 'postCode',
      type: 'input',
      props: { placeholder: '请输入岗位编码', clearable: true }
    },
    {
      label: '岗位名称',
      key: 'postName',
      type: 'input',
      props: { placeholder: '请输入岗位名称', clearable: true }
    },
    {
      label: '状态',
      key: 'status',
      type: 'select',
      props: {
        placeholder: '岗位状态',
        clearable: true,
        options: dicts.sys_normal_disable.value
      }
    }
  ])

  // 表格列配置
  const { columnChecks, columns } = useTableColumns(() => [
    { type: 'selection', width: 55, align: 'center' },
    { prop: 'postCode', label: '岗位编码', minWidth: 100 },
    { prop: 'postName', label: '岗位名称', minWidth: 120 },
    { prop: 'postSort', label: '岗位排序', width: 100, align: 'center' },
    {
      prop: 'status',
      label: '状态',
      width: 100,
      align: 'center',
      formatter: (row: any) => {
        return h(DictTag, { options: dicts.sys_normal_disable.value, value: row.status })
      }
    },
    { prop: 'createTime', label: '创建时间', width: 180, align: 'center' },
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
  ])

  /** 查询岗位列表 */
  const getList = async () => {
    loading.value = true
    try {
      const response: any = await listPost({ ...queryParams, ...formFilters })
      postList.value = response.rows || []
      total.value = response.total || 0
    } catch (error) {
      console.error('获取岗位列表失败:', error)
    } finally {
      loading.value = false
    }
  }

  /** 多选框选中数据 */
  const handleSelectionChange = (selection: any[]) => {
    ids.value = selection.map((item) => item.postId)
    multiple.value = !selection.length
  }

  /** 重置按钮操作 */
  const handleReset = () => {
    Object.assign(formFilters, initialSearchState)
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
    const postIds = row?.postId || ids.value
    try {
      await ElMessageBox.confirm(`是否确认删除岗位编号为"${postIds}"的数据项？`, '提示', {
        type: 'warning'
      })
      await delPost(postIds)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除岗位失败:', error)
      }
    }
  }

  onMounted(() => {
    getList()
    // 触发字典数据初始化
    void dicts.sys_normal_disable.value
  })
</script>

<style lang="scss" scoped>
  .post-page {
    padding: 12px;
  }
</style>
