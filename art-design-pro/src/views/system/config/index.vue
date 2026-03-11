<!-- 参数设置管理页面 (更新路径适配并优化传参) -->
<template>
  <div class="config-page art-full-height flex flex-col p-3 overflow-hidden">
    <!-- 搜索栏 -->
    <ArtSearchBar
      v-model="formFilters"
      :items="formItems"
      @reset="handleReset"
      @search="handleSearch"
    />

    <ElCard class="art-table-card flex-1 overflow-hidden" shadow="never">
      <!-- 表格头部 -->
      <ArtTableHeader :loading="loading" v-model:columns="columnChecks" @refresh="refreshData">
        <template #left>
          <ElButton v-auth="'system:config:add'" type="primary" @click="handleAdd" v-ripple>
            新增
          </ElButton>
          <ElButton
            v-auth="'system:config:remove'"
            type="danger"
            plain
            :disabled="!multiple"
            @click="handleDelete"
            v-ripple
          >
            删除
          </ElButton>
          <ElButton
            v-auth="'system:config:remove'"
            type="danger"
            plain
            @click="handleRefreshCache"
            v-ripple
          >
            刷新缓存
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
        rowKey="configId"
        @selection-change="handleSelectionChange"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      >
        <!-- 系统内置插槽 -->
        <template #configType="{ row }">
          <DictTag :options="sys_yes_no" :value="row.configType" />
        </template>

        <!-- 操作列插槽 -->
        <template #operation="{ row }">
          <div class="flex justify-end gap-2">
            <ArtButtonTable
              v-if="hasAuth('system:config:edit')"
              type="edit"
              @click="handleUpdate(row)"
            />
            <ArtButtonTable
              v-if="hasAuth('system:config:remove')"
              type="delete"
              @click="handleDelete(row)"
            />
          </div>
        </template>
      </ArtTable>
    </ElCard>

    <!-- 编辑弹窗 -->
    <ConfigEditDialog
      v-model="dialogVisible"
      :dialog-type="dialogType"
      :data="currentData"
      @success="refreshData"
    />
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, computed, onMounted } from 'vue'
  import { listConfig, delConfig, refreshCache, getConfig } from '@/api/system/config'
  import { useTable } from '@/hooks/core/useTable'
  import { useDict } from '@/utils/dict'
  import { useAuth } from '@/hooks/core/useAuth'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import DictTag from '@/components/DictTag/index.vue'
  import { ElMessageBox, ElMessage } from 'element-plus'
  import ConfigEditDialog from './modules/config-edit-dialog.vue'

  defineOptions({ name: 'Config' })

  // 接入字典
  const { sys_yes_no } = useDict('sys_yes_no')

  // 选中数据
  const ids = ref<number[]>([])
  const multiple = ref(true)

  // 弹窗相关
  const dialogVisible = ref(false)
  const dialogType = ref<'add' | 'edit'>('add')
  const currentData = ref<any>(null)

  // 搜索相关
  const initialSearchState = {
    configName: '',
    configKey: '',
    configType: ''
  }

  const formFilters = reactive({ ...initialSearchState })

  const formItems = computed(() => [
    {
      label: '参数名称',
      key: 'configName',
      type: 'input',
      props: { placeholder: '请输入参数名称', clearable: true }
    },
    {
      label: '参数键名',
      key: 'configKey',
      type: 'input',
      props: { placeholder: '请输入参数键名', clearable: true }
    },
    {
      label: '系统内置',
      key: 'configType',
      type: 'select',
      props: {
        placeholder: '系统内置',
        clearable: true,
        options: sys_yes_no.value
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
      apiFn: listConfig,
      columnsFactory: () => [
        { type: 'selection', width: 55, align: 'center' },
        { prop: 'configId', label: '参数主键', width: 100 },
        { prop: 'configName', label: '参数名称', minWidth: 150, showOverflowTooltip: true },
        { prop: 'configKey', label: '参数键名', minWidth: 150, showOverflowTooltip: true },
        { prop: 'configValue', label: '参数键值', minWidth: 150, showOverflowTooltip: true },
        {
          prop: 'configType',
          label: '系统内置',
          width: 100,
          align: 'center',
          useSlot: true
        },
        { prop: 'remark', label: '备注', minWidth: 150, showOverflowTooltip: true },
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
    ids.value = selection.map((item) => item.configId)
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
    const configId = row.configId || ids.value[0]
    try {
      const response: any = await getConfig(configId)
      currentData.value = response.data
      dialogType.value = 'edit'
      dialogVisible.value = true
    } catch (error) {
      console.error('获取参数详情失败:', error)
      ElMessage.error('获取详情失败')
    }
  }

  /** 删除按钮操作 */
  const handleDelete = async (row?: any) => {
    const configIds = row?.configId || ids.value
    if (!configIds || (Array.isArray(configIds) && configIds.length === 0)) return

    try {
      await ElMessageBox.confirm(`是否确认删除参数主键为"${configIds}"的数据项？`, '提示', {
        type: 'warning'
      })
      await delConfig(configIds)
      ElMessage.success('删除成功')
      refreshData()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除参数失败:', error)
      }
    }
  }

  /** 刷新缓存操作 */
  const handleRefreshCache = async () => {
    try {
      await refreshCache()
      ElMessage.success('刷新成功')
    } catch (error) {
      console.error('刷新参数缓存失败:', error)
    }
  }

  onMounted(() => {
    void sys_yes_no.value
  })
</script>

<style lang="scss" scoped>
  .config-page {
    background-color: transparent;
    gap: 12px;
  }
</style>
