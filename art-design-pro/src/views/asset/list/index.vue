<!-- 资产台账页面 -->
<template>
  <div class="asset-list-page art-full-height flex !flex-row p-3 overflow-hidden">
    <!-- 左侧分类树 (对标 DeptTreeAside 样式与折叠逻辑) -->
    <div
      class="dept-tree-container relative h-full transition-all duration-300 flex-shrink-0"
      :style="{
        width: isTreeCollapse ? '0px' : '200px',
        marginRight: isTreeCollapse ? '0px' : '15px'
      }"
    >
      <div
        class="art-table-card !m-0 h-full flex flex-col overflow-hidden border border-solid border-[var(--art-gray-200)]"
        :style="{
          opacity: isTreeCollapse ? 0 : 1,
          visibility: isTreeCollapse ? 'hidden' : 'visible',
          transition: 'opacity 0.2s'
        }"
      >
        <div
          class="aside-header flex justify-between items-center px-4 py-3 border-b border-solid border-[var(--art-gray-200)] whitespace-nowrap"
        >
          <span class="text-[15px] font-bold text-g-700">资产分类</span>
          <ArtSvgIcon icon="ri:organization-chart" class="text-g-500 text-lg" />
        </div>

        <div class="px-4 py-3">
          <ElInput
            v-model="filterText"
            placeholder="搜索分类"
            prefix-icon="Search"
            clearable
            class="aside-search w-full"
          />
        </div>

        <div class="flex-1 overflow-hidden px-2 pb-4">
          <ElScrollbar class="h-full">
            <ElTree
              ref="treeRef"
              :data="categoryOptions"
              :props="{ label: 'name', children: 'children' }"
              highlight-current
              node-key="id"
              default-expand-all
              :filter-node-method="filterNode"
              @node-click="handleNodeClick"
              empty-text="暂无数据"
            />
          </ElScrollbar>
        </div>
      </div>

      <!-- 折叠按钮 -->
      <div
        class="collapse-handle flex items-center justify-center cursor-pointer absolute shadow-md border border-solid border-[var(--art-gray-200)]"
        :class="isTreeCollapse ? 'handle-collapsed' : 'handle-expanded'"
        @click="isTreeCollapse = !isTreeCollapse"
      >
        <ArtSvgIcon
          :icon="isTreeCollapse ? 'ri:arrow-right-s-line' : 'ri:arrow-left-s-line'"
          class="text-lg"
        />
      </div>
    </div>

    <!-- 右侧主体内容 -->
    <div class="flex-1 min-w-0 flex flex-col h-full overflow-hidden relative">
      <!-- 搜索栏 -->
      <ArtSearchBar
        :key="asset_status.length"
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
              v-auth="'asset:info:remove'"
              type="danger"
              plain
              :disabled="!multiple"
              @click="() => handleDelete()"
              v-ripple
            >
              删除
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

        <!-- 表格 - 使用项目封装的 ArtTable -->
        <ArtTable
          ref="tableRef"
          :loading="loading"
          :data="data"
          :columns="columns"
          :pagination="pagination"
          rowKey="assetId"
          @selection-change="handleSelectionChange"
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

    <AssetFinanceDialog
      v-model="financeVisible"
      :asset-id="currentFinanceAsset?.assetId"
      :asset-no="currentFinanceAsset?.assetNo"
    />

    <!-- 领用申请弹窗 -->
    <ElDialog
      v-model="applyDialogVisible"
      title="资产领用申请"
      width="500px"
      draggable
      destroy-on-close
    >
      <ElForm :model="applyForm" :rules="applyRules" ref="applyFormRef" label-width="100px">
        <ElFormItem label="资产编号">
          <ElInput v-model="applyTask.assetNo" disabled />
        </ElFormItem>
        <ElFormItem label="资产名称">
          <ElInput v-model="applyTask.assetName" disabled />
        </ElFormItem>
        <ElFormItem label="资产状态">
          <ElInput v-model="applyTask.assetStatus" disabled />
        </ElFormItem>
        <ElFormItem label="申请事由" prop="reason">
          <ElInput
            v-model="applyForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入申请事由"
            clearable
          />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <span class="dialog-footer">
          <ElButton @click="applyDialogVisible = false">取消</ElButton>
          <ElButton type="primary" @click="submitApply" :loading="applySubmitting"
            >提交申请</ElButton
          >
        </span>
      </template>
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, computed, onMounted, watch, h } from 'vue'
  import { useRouter } from 'vue-router'
  import { listInfo, delInfo, exportInfo } from '@/api/asset/info'
  import { applyRequisition } from '@/api/asset/requisition'
  import { listCategory } from '@/api/asset/category'
  import type { AssetLifecycleAction, AssetListItem } from '@/types/asset'
  import { useTable } from '@/hooks/core/useTable'
  import { useDict } from '@/utils/dict'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import DictTag from '@/components/DictTag/index.vue'
  import { handleTree } from '@/utils/ruoyi'
  import { ElMessageBox, ElMessage, ElButton } from 'element-plus'
  import AssetEditDrawer from './modules/asset-edit-drawer.vue'
  import AssetFinanceDialog from './modules/asset-finance-dialog.vue'
  import {
    buildAssetListQuery,
    collectAssetIds,
    toApplyAssetContext,
    type AssetApplyContext
  } from './asset-list.helper'
  import { buildApplyRequisitionReq } from '../requisition/requisition.helper'
  import { buildLifecycleActions } from './asset-lifecycle.helper'

  defineOptions({ name: 'AssetList' })
  const router = useRouter()

  // 状态管理
  const filterText = ref('')
  const isTreeCollapse = ref(false)
  const treeRef = ref()
  const categoryOptions = ref<any[]>([])
  const ids = ref<number[]>([])
  const selectedAssetNos = ref<string[]>([])
  const multiple = ref(true)

  // 接入字典
  const { asset_type, asset_status } = useDict('asset_type', 'asset_status')

  // 抽屉相关
  const drawerVisible = ref(false)
  const drawerType = ref<'add' | 'edit'>('add')
  const currentData = ref<AssetListItem>()
  const financeVisible = ref(false)
  const currentFinanceAsset = ref<AssetListItem>()

  // 搜索相关
  const initialSearchState = {
    assetNo: '',
    assetName: '',
    assetStatus: undefined as string | undefined
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
      key: 'assetStatus',
      type: 'select',
      props: {
        placeholder: '资产状态',
        clearable: true,
        options: asset_status.value
      }
    }
  ])

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
        { type: 'selection', width: 55, align: 'center' },
        { prop: 'assetNo', label: '资产编号', width: 120 },
        { prop: 'assetName', label: '资产名称', minWidth: 150 },
        {
          prop: 'assetType',
          label: '资产类型',
          width: 100,
          align: 'center',
          formatter: (row: any) => {
            return h(DictTag, { options: asset_type.value, value: row.assetType })
          }
        },
        { prop: 'specModel', label: '规格型号', minWidth: 140 },
        { prop: 'locationText', label: '位置描述', minWidth: 160 },
        {
          prop: 'assetStatus',
          label: '资产状态',
          width: 110,
          align: 'center',
          formatter: (row: any) => {
            return h(DictTag, { options: asset_status.value, value: row.assetStatus })
          }
        },
        { prop: 'purchaseDate', label: '购置日期', width: 120, align: 'center' },
        { prop: 'capitalizationDate', label: '入账日期', width: 120, align: 'center' },
        {
          prop: 'operation',
          label: '操作',
          width: 420,
          align: 'right',
          formatter: (row: any) => {
            return h(
              'div',
              { class: 'flex flex-wrap justify-end gap-1' },
              buildOperationButtons(row)
            )
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

  /** 多选框选中数据 */
  const handleSelectionChange = (selection: AssetListItem[]) => {
    ids.value = collectAssetIds(selection)
    selectedAssetNos.value = selection.map((item) => item.assetNo)
    multiple.value = !selection.length
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
    Object.assign(
      searchParams,
      buildAssetListQuery({
        ...formFilters,
        categoryId: searchParams.categoryId as number | undefined
      })
    )
    getData()
  }

  /** 新增 */
  const handleAdd = () => {
    drawerType.value = 'add'
    currentData.value = undefined
    drawerVisible.value = true
  }

  /** 修改 */
  const handleUpdate = (row: AssetListItem) => {
    drawerType.value = 'edit'
    currentData.value = { ...row }
    drawerVisible.value = true
  }

  /** 打开单资产财务弹窗，统一查看财务摘要与折旧日志。 */
  const openFinanceDialog = (row: AssetListItem) => {
    currentFinanceAsset.value = row
    financeVisible.value = true
  }

  const handleDelete = async (row?: AssetListItem) => {
    const assetIds = row?.assetId || ids.value
    if (!assetIds || (Array.isArray(assetIds) && assetIds.length === 0)) return

    const displayAssetNos = row?.assetNo || selectedAssetNos.value.join('、')

    try {
      await ElMessageBox.confirm(`是否确认删除资产编号为"${displayAssetNos}"的数据项？`, '提示', {
        type: 'warning'
      })
      await delInfo(Array.isArray(assetIds) ? assetIds.join(',') : assetIds)
      ElMessage.success('删除成功')
      refreshData()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除资产失败:', error)
      }
    }
  }

  /** 占位生命周期动作只给出中文提示，不冒充可成功的业务流程。 */
  const handleLifecyclePlaceholder = (action: AssetLifecycleAction) => {
    ElMessage.info(action.message || `${action.label}入口待规划`)
  }

  /**
   * 把资产上下文带到生命周期台账页。
   *
   * 当前阶段优先保证固定资产动作“能跳、能带参、能直接发起申请”，
   * 复杂的选择器与批量动作留到后续体验优化批次处理。
   */
  const openLifecycleLedger = (path: string, row: AssetListItem) => {
    void router.push({
      path,
      query: {
        assetId: String(row.assetId),
        assetNo: row.assetNo,
        assetName: row.assetName,
        assetStatus: row.assetStatus,
        openCreate: '1'
      }
    })
  }

  const handleMaintenance = (row: AssetListItem) => {
    openLifecycleLedger('/asset/maintenance/index', row)
  }

  const handleDisposal = (row: AssetListItem) => {
    openLifecycleLedger('/asset/disposal/index', row)
  }

  const handleRealEstateOwnership = (row: AssetListItem) => {
    openLifecycleLedger('/asset/real-estate/ownership/index', row)
  }

  const handleRealEstateUsage = (row: AssetListItem) => {
    openLifecycleLedger('/asset/real-estate/usage/index', row)
  }

  const handleRealEstateStatus = (row: AssetListItem) => {
    openLifecycleLedger('/asset/real-estate/status/index', row)
  }

  const handleRealEstateDisposal = (row: AssetListItem) => {
    openLifecycleLedger('/asset/real-estate/disposal/index', row)
  }

  /** 把生命周期动作定义映射为列表页按钮，确保固定资产与不动产入口不混用。 */
  const renderLifecycleActionButton = (row: AssetListItem, action: AssetLifecycleAction) => {
    if (action.mode === 'placeholder') {
      return h(
        ElButton,
        {
          type: action.tone || 'primary',
          link: true,
          onClick: () => handleLifecyclePlaceholder(action)
        },
        () => action.label
      )
    }

    if (action.key === 'delete') {
      return h(ArtButtonTable, {
        type: 'delete',
        onClick: () => handleDelete(row)
      })
    }

    const actionHandlerMap: Partial<Record<AssetLifecycleAction['key'], () => void>> = {
      requisition: () => handleRequisition(row),
      repair: () => handleMaintenance(row),
      disposal: () => handleDisposal(row),
      realEstateOwnership: () => handleRealEstateOwnership(row),
      realEstateUsage: () => handleRealEstateUsage(row),
      realEstateStatus: () => handleRealEstateStatus(row),
      realEstateDisposal: () => handleRealEstateDisposal(row),
      change: () => handleLifecyclePlaceholder(action)
    }

    return h(
      ElButton,
      {
        type: action.tone || 'primary',
        link: true,
        onClick: actionHandlerMap[action.key]
      },
      () => action.label
    )
  }

  /** 统一拼装操作列，保留“编辑”与“变更”的独立语义。 */
  const buildOperationButtons = (row: AssetListItem) => {
    const lifecycleActions = buildLifecycleActions(row)
    const deleteAction = lifecycleActions.find((item) => item.key === 'delete')
    const otherLifecycleActions = lifecycleActions.filter((item) => item.key !== 'delete')

    return [
      ...otherLifecycleActions.map((item) => renderLifecycleActionButton(row, item)),
      h(
        ElButton,
        {
          type: 'primary',
          link: true,
          onClick: () => openFinanceDialog(row)
        },
        () => '财务'
      ),
      h(ArtButtonTable, {
        type: 'edit',
        onClick: () => handleUpdate(row)
      }),
      ...(deleteAction ? [renderLifecycleActionButton(row, deleteAction)] : [])
    ]
  }

  /** 导出 */
  const handleExport = async () => {
    try {
      const response: any = await exportInfo(searchParams)
      const blob = new Blob([response.data || response], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      })
      const link = document.createElement('a')
      link.href = window.URL.createObjectURL(blob)
      link.download = `资产台账_${new Date().getTime()}.xlsx`
      link.click()
    } catch (error) {
      console.error('导出失败:', error)
    }
  }

  // ==== 领用申请相关 ====
  const applyDialogVisible = ref(false)
  const applyTask = ref<AssetApplyContext>({
    assetId: 0,
    assetNo: '',
    assetName: '',
    assetStatus: ''
  })
  const applySubmitting = ref(false)
  const applyFormRef = ref()
  const applyForm = reactive({ reason: '' })
  const applyRules = {
    reason: [{ required: true, message: '请填写原因', trigger: 'blur' }]
  }

  const handleRequisition = (row: AssetListItem) => {
    applyTask.value = toApplyAssetContext(row)
    applyForm.reason = ''
    applyDialogVisible.value = true
  }

  const submitApply = async () => {
    try {
      if (!applyForm.reason.trim()) {
        ElMessage.warning('请填写申请原因')
        return
      }
      await applyFormRef.value?.validate()
      applySubmitting.value = true

      await applyRequisition(buildApplyRequisitionReq(applyTask.value, applyForm.reason))
      ElMessage.success('申请提交成功')
      applyDialogVisible.value = false
      refreshData()
    } catch (e) {
      console.error('提交失败:', e)
    } finally {
      applySubmitting.value = false
    }
  }

  onMounted(() => {
    getCategoryTree()
    // 初始化字典
    void asset_type.value
    void asset_status.value
  })
</script>

<style lang="scss" scoped>
  .asset-list-page {
    padding: 12px;
  }

  // 侧边栏折叠按钮样式 (同步 DeptTreeAside)
  .collapse-handle {
    top: 50%;
    transform: translateY(-50%);
    width: 22px;
    height: 50px;
    background-color: var(--default-box-color);
    border-radius: 0 10px 10px 0;
    z-index: 100;
    color: var(--art-gray-500);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    border-left: none;

    &:hover {
      color: var(--theme-color);
      background-color: var(--art-gray-100);
    }

    &.handle-expanded {
      right: -23px;
    }

    &.handle-collapsed {
      left: -8px;
      border-left: 1px solid var(--art-gray-200);
      border-radius: 6px;
    }
  }

  :deep(.el-tree) {
    background: transparent;
    .el-tree-node__content {
      height: 38px;
      border-radius: 6px;
      &:hover {
        background-color: var(--art-gray-200) !important;
      }
    }
    .el-tree-node.is-current > .el-tree-node__content {
      background-color: var(--theme-color-light) !important;
      color: var(--theme-color);
    }
  }
</style>
