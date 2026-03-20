<template>
  <div
    class="asset-disposal-page art-full-height flex flex-col gap-2 overflow-y-auto overflow-x-hidden p-3"
  >
    <ElCard class="main-card flex-1 min-h-0 overflow-hidden" shadow="never">
      <ElTabs v-model="activeTab">
        <ElTabPane label="待处置资产池" name="pool">
          <div class="tab-pane-body">
            <ArtSearchBar
              v-model="poolSearchForm"
              :items="poolSearchItems"
              :showExpand="false"
              @search="handlePoolSearch"
              @reset="handlePoolReset"
            />

            <ElCard class="art-table-card flex-1 overflow-hidden inner-table-card" shadow="never">
              <template #header>
                <div class="card-header">
                  <div class="card-title">待处置资产池</div>
                  <ElSpace wrap class="toolbar-actions">
                    <ElTag type="warning" effect="light">处置入口：待处置资产池</ElTag>
                    <ElButton type="primary" plain icon="ri:refresh-line" @click="refreshPoolData">
                      刷新资产池
                    </ElButton>
                  </ElSpace>
                </div>
              </template>
              <ArtTable
                rowKey="assetId"
                :loading="poolLoading"
                :data="poolData"
                :columns="poolColumns"
                :pagination="poolPagination"
                @pagination:size-change="handlePoolSizeChange"
                @pagination:current-change="handlePoolCurrentChange"
              />
            </ElCard>
          </div>
        </ElTabPane>

        <ElTabPane label="处置记录" name="record">
          <div class="tab-pane-body">
            <ArtSearchBar
              v-model="recordSearchForm"
              :items="recordSearchItems"
              :showExpand="false"
              @search="handleRecordSearch"
              @reset="handleRecordReset"
            />

            <ElCard class="art-table-card flex-1 overflow-hidden inner-table-card" shadow="never">
              <template #header>
                <div class="card-header">
                  <div class="card-title">处置记录</div>
                  <ElButton type="primary" plain icon="ri:refresh-line" @click="refreshRecordData">
                    刷新记录
                  </ElButton>
                </div>
              </template>
              <ArtTable
                rowKey="disposalId"
                :loading="recordLoading"
                :data="recordData"
                :columns="recordColumns"
                :pagination="recordPagination"
                @pagination:size-change="handleRecordSizeChange"
                @pagination:current-change="handleRecordCurrentChange"
              />
            </ElCard>
          </div>
        </ElTabPane>
      </ElTabs>
    </ElCard>

    <ElDialog
      v-model="confirmDialogVisible"
      title="确认资产处置"
      width="720px"
      destroy-on-close
      @closed="handleConfirmDialogClosed"
    >
      <ElDescriptions v-if="currentAsset" :column="2" border class="mb-4">
        <ElDescriptionsItem label="资产编码">{{
          currentAsset.assetCode || '-'
        }}</ElDescriptionsItem>
        <ElDescriptionsItem label="资产名称">{{
          currentAsset.assetName || '-'
        }}</ElDescriptionsItem>
        <ElDescriptionsItem label="当前状态">
          <DictTag :options="ast_asset_status" :value="currentAsset.assetStatus" />
        </ElDescriptionsItem>
        <ElDescriptionsItem label="资产位置">{{
          currentAsset.locationName || '-'
        }}</ElDescriptionsItem>
      </ElDescriptions>

      <ElForm
        ref="confirmFormRef"
        :model="confirmFormData"
        :rules="confirmFormRules"
        label-width="110px"
      >
        <ElRow :gutter="16">
          <ElCol :xs="24" :md="12">
            <ElFormItem label="处置类型" prop="disposalType">
              <ElSelect
                v-model="confirmFormData.disposalType"
                class="w-full"
                placeholder="请选择处置类型"
              >
                <ElOption
                  v-for="item in disposalTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </ElSelect>
            </ElFormItem>
          </ElCol>
          <ElCol :xs="24" :md="12">
            <ElFormItem label="处置日期" prop="disposalDate">
              <ElDatePicker
                v-model="confirmFormData.disposalDate"
                type="date"
                value-format="YYYY-MM-DD"
                class="w-full"
                placeholder="请选择处置日期"
              />
            </ElFormItem>
          </ElCol>
        </ElRow>
        <ElRow :gutter="16">
          <ElCol :xs="24" :md="12">
            <ElFormItem label="处置金额" prop="disposalAmount">
              <ElInputNumber
                v-model="confirmFormData.disposalAmount"
                :min="0"
                :precision="2"
                class="!w-full"
                placeholder="可选：录入处置金额"
              />
            </ElFormItem>
          </ElCol>
          <ElCol :xs="24" :md="12">
            <ElFormItem label="财务确认" prop="financeConfirmFlag">
              <ElSwitch
                v-model="confirmFormData.financeConfirmFlag"
                active-value="1"
                inactive-value="0"
                inline-prompt
                active-text="是"
                inactive-text="否"
              />
            </ElFormItem>
          </ElCol>
        </ElRow>
        <ElFormItem label="处置原因" prop="disposalReason">
          <ElInput
            v-model="confirmFormData.disposalReason"
            type="textarea"
            :rows="3"
            maxlength="255"
            show-word-limit
            placeholder="请输入处置原因"
          />
        </ElFormItem>
        <ElFormItem label="备注" prop="remark">
          <ElInput
            v-model="confirmFormData.remark"
            type="textarea"
            :rows="2"
            maxlength="500"
            show-word-limit
            placeholder="请输入备注（可选）"
          />
        </ElFormItem>
      </ElForm>

      <template #footer>
        <ElButton @click="confirmDialogVisible = false">取消</ElButton>
        <ElButton type="primary" :loading="confirmSubmitting" @click="handleConfirmSubmit">
          提交处置
        </ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
  import type { FormInstance, FormRules } from 'element-plus'
  import { ElButton, ElMessage, ElTag } from 'element-plus'
  import { listAssetLedger } from '@/api/asset/ledger'
  import { addAssetDisposal, listAssetDisposal } from '@/api/asset/disposal'
  import DictTag from '@/components/DictTag/index.vue'
  import { useDict } from '@/utils/dict'
  import { useTable } from '@/hooks/core/useTable'
  import { useUserStore } from '@/store/modules/user'

  defineOptions({ name: 'AssetDisposal' })

  type AssetPoolRow = {
    assetId: number
    assetCode?: string
    assetName?: string
    assetStatus?: string
    ownerDeptName?: string
    useDeptName?: string
    responsibleUserName?: string
    locationName?: string
    originalValue?: number
  }

  type DisposalRecordRow = {
    disposalId: number
    disposalNo?: string
    assetCode?: string
    assetName?: string
    disposalType?: string
    disposalStatus?: string
    disposalDate?: string
    disposalAmount?: number
    confirmedBy?: string
    confirmedTime?: string
    financeConfirmFlag?: string
    disposalReason?: string
  }

  const { ast_asset_status } = useDict('ast_asset_status')
  const userStore = useUserStore()
  const hasPermission = (permission: string) => {
    return userStore.permissions.includes('*:*:*') || userStore.permissions.includes(permission)
  }

  // 中文注释：处置确认会改变台账最终状态，仅对具备新增处置权限的用户开放。
  const canConfirmDisposal = computed(() => hasPermission('asset:disposal:add'))

  // 中文注释：默认先展示“待处置资产池”，符合先处理再回看记录的操作习惯。
  const activeTab = ref<'pool' | 'record'>('pool')

  const disposalTypeOptions = [
    { label: '报废', value: 'SCRAP', listClass: 'warning' },
    { label: '变卖', value: 'SALE', listClass: 'primary' },
    { label: '捐赠', value: 'DONATION', listClass: 'success' },
    { label: '毁损', value: 'DAMAGE', listClass: 'danger' }
  ]

  const disposalStatusOptions = [{ label: '已确认', value: 'CONFIRMED', listClass: 'success' }]

  const poolInitialSearchState = {
    assetCode: '',
    assetName: ''
  }
  const poolSearchForm = reactive({ ...poolInitialSearchState })

  const poolSearchItems = computed(() => [
    {
      label: '资产编码',
      key: 'assetCode',
      type: 'input',
      props: { placeholder: '请输入资产编码', clearable: true }
    },
    {
      label: '资产名称',
      key: 'assetName',
      type: 'input',
      props: { placeholder: '请输入资产名称', clearable: true }
    }
  ])

  const {
    columns: poolColumns,
    data: poolData,
    loading: poolLoading,
    pagination: poolPagination,
    getData: getPoolData,
    searchParams: poolSearchParams,
    resetSearchParams: resetPoolSearchParams,
    handleSizeChange: handlePoolSizeChange,
    handleCurrentChange: handlePoolCurrentChange,
    refreshData: refreshPoolData
  } = useTable({
    core: {
      apiFn: listAssetLedger,
      apiParams: {
        assetType: 'FIXED',
        assetStatus: 'PENDING_DISPOSAL',
        pageNum: 1,
        pageSize: 10
      },
      columnsFactory: () => [
        { prop: 'assetCode', label: '资产编码', minWidth: 140 },
        { prop: 'assetName', label: '资产名称', minWidth: 180, showOverflowTooltip: true },
        {
          prop: 'assetStatus',
          label: '资产状态',
          width: 110,
          formatter: (row: AssetPoolRow) =>
            h(DictTag, { options: ast_asset_status.value, value: row.assetStatus })
        },
        { prop: 'ownerDeptName', label: '权属部门', minWidth: 120 },
        { prop: 'useDeptName', label: '使用部门', minWidth: 120 },
        { prop: 'responsibleUserName', label: '责任人', width: 100 },
        { prop: 'locationName', label: '资产位置', minWidth: 150, showOverflowTooltip: true },
        {
          prop: 'originalValue',
          label: '资产原值(元)',
          width: 130,
          formatter: (row: AssetPoolRow) => formatMoney(row.originalValue)
        },
        {
          prop: 'operation',
          label: '操作',
          width: 100,
          fixed: 'right',
          align: 'right',
          formatter: (row: AssetPoolRow) => {
            if (!canConfirmDisposal.value) {
              return '-'
            }
            return h(
              ElButton,
              {
                link: true,
                type: 'danger',
                onClick: () => openConfirmDialog(row)
              },
              () => '确认处置'
            )
          }
        }
      ]
    }
  })

  const recordInitialSearchState = {
    disposalNo: '',
    disposalType: '',
    daterange: undefined as string[] | undefined
  }
  const recordSearchForm = reactive({ ...recordInitialSearchState })

  const recordSearchItems = computed(() => [
    {
      label: '处置单号',
      key: 'disposalNo',
      type: 'input',
      props: { placeholder: '请输入处置单号', clearable: true }
    },
    {
      label: '处置类型',
      key: 'disposalType',
      type: 'select',
      props: {
        placeholder: '请选择处置类型',
        clearable: true,
        options: disposalTypeOptions.map((item) => ({ label: item.label, value: item.value }))
      }
    },
    {
      label: '处置日期',
      key: 'daterange',
      type: 'daterange',
      props: {
        style: { width: '100%' },
        valueFormat: 'YYYY-MM-DD',
        clearable: true,
        rangeSeparator: '至',
        startPlaceholder: '开始日期',
        endPlaceholder: '结束日期'
      }
    }
  ])

  const {
    columns: recordColumns,
    data: recordData,
    loading: recordLoading,
    pagination: recordPagination,
    getData: getRecordData,
    searchParams: recordSearchParams,
    resetSearchParams: resetRecordSearchParams,
    handleSizeChange: handleRecordSizeChange,
    handleCurrentChange: handleRecordCurrentChange,
    refreshData: refreshRecordData
  } = useTable({
    core: {
      apiFn: listAssetDisposal,
      apiParams: {
        pageNum: 1,
        pageSize: 10
      },
      excludeParams: ['daterange'],
      columnsFactory: () => [
        { prop: 'disposalNo', label: '处置单号', minWidth: 150 },
        { prop: 'assetCode', label: '资产编码', minWidth: 130 },
        { prop: 'assetName', label: '资产名称', minWidth: 170, showOverflowTooltip: true },
        {
          prop: 'disposalType',
          label: '处置类型',
          width: 110,
          formatter: (row: DisposalRecordRow) =>
            h(DictTag, {
              options: disposalTypeOptions.map((item) => ({
                label: item.label,
                value: item.value,
                listClass: item.listClass
              })),
              value: row.disposalType
            })
        },
        {
          prop: 'disposalStatus',
          label: '处置状态',
          width: 110,
          formatter: (row: DisposalRecordRow) =>
            h(DictTag, {
              options: disposalStatusOptions,
              value: row.disposalStatus
            })
        },
        { prop: 'disposalDate', label: '处置日期', width: 120 },
        {
          prop: 'disposalAmount',
          label: '处置金额(元)',
          width: 120,
          formatter: (row: DisposalRecordRow) => formatMoney(row.disposalAmount)
        },
        { prop: 'confirmedBy', label: '确认人', width: 100 },
        { prop: 'confirmedTime', label: '确认时间', minWidth: 160 },
        {
          prop: 'financeConfirmFlag',
          label: '财务确认',
          width: 100,
          formatter: (row: DisposalRecordRow) =>
            h(
              ElTag,
              { type: row.financeConfirmFlag === '1' ? 'success' : 'info', effect: 'light' },
              () => (row.financeConfirmFlag === '1' ? '是' : '否')
            )
        }
      ]
    }
  })

  const confirmDialogVisible = ref(false)
  const confirmSubmitting = ref(false)
  const confirmFormRef = ref<FormInstance>()
  const currentAsset = ref<AssetPoolRow>()

  const confirmFormData = reactive({
    disposalType: 'SCRAP',
    disposalDate: new Date().toISOString().slice(0, 10),
    disposalReason: '',
    disposalAmount: undefined as number | undefined,
    financeConfirmFlag: '0',
    remark: ''
  })

  const confirmFormRules: FormRules = {
    disposalType: [{ required: true, message: '请选择处置类型', trigger: 'change' }],
    disposalDate: [{ required: true, message: '请选择处置日期', trigger: 'change' }],
    disposalReason: [{ required: true, message: '请输入处置原因', trigger: 'blur' }]
  }

  const formatMoney = (value?: number) => {
    if (value === null || value === undefined || value === ('' as any)) {
      return '-'
    }
    return Number(value).toLocaleString('zh-CN', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    })
  }

  // 中文注释：资产池固定查询“待处置”状态，确保页面口径与后端闭环规则一致。
  const buildPoolQueryParams = () => {
    return {
      assetType: 'FIXED',
      assetStatus: 'PENDING_DISPOSAL',
      assetCode: poolSearchForm.assetCode?.trim() || undefined,
      assetName: poolSearchForm.assetName?.trim() || undefined
    }
  }

  const handlePoolSearch = () => {
    Object.assign(poolSearchParams, buildPoolQueryParams())
    getPoolData()
  }

  const handlePoolReset = () => {
    Object.assign(poolSearchForm, poolInitialSearchState)
    resetPoolSearchParams()
  }

  const buildRecordQueryParams = () => {
    const [beginTime, endTime] = Array.isArray(recordSearchForm.daterange)
      ? recordSearchForm.daterange
      : [undefined, undefined]

    return {
      disposalNo: recordSearchForm.disposalNo?.trim() || undefined,
      disposalType: recordSearchForm.disposalType || undefined,
      'params[beginTime]': beginTime,
      'params[endTime]': endTime
    }
  }

  const handleRecordSearch = () => {
    Object.assign(recordSearchParams, buildRecordQueryParams())
    getRecordData()
  }

  const handleRecordReset = () => {
    Object.assign(recordSearchForm, recordInitialSearchState)
    resetRecordSearchParams()
  }

  // 中文注释：确认处置前锁定当前资产上下文，提交时据此生成处置单。
  const openConfirmDialog = (row: AssetPoolRow) => {
    currentAsset.value = row
    confirmDialogVisible.value = true
  }

  const resetConfirmForm = () => {
    Object.assign(confirmFormData, {
      disposalType: 'SCRAP',
      disposalDate: new Date().toISOString().slice(0, 10),
      disposalReason: '',
      disposalAmount: undefined,
      financeConfirmFlag: '0',
      remark: ''
    })
    confirmFormRef.value?.clearValidate()
  }

  const handleConfirmDialogClosed = () => {
    resetConfirmForm()
    currentAsset.value = undefined
  }

  // 中文注释：提交成功后同时刷新资产池与处置记录，并切换到记录页便于用户立即核对结果。
  const handleConfirmSubmit = async () => {
    const valid = await confirmFormRef.value?.validate().catch(() => false)
    if (!valid) {
      return
    }
    if (!currentAsset.value?.assetId) {
      ElMessage.warning('未识别到待处置资产')
      return
    }

    confirmSubmitting.value = true
    try {
      await addAssetDisposal({
        assetId: currentAsset.value.assetId,
        disposalType: confirmFormData.disposalType,
        disposalDate: confirmFormData.disposalDate,
        disposalReason: confirmFormData.disposalReason.trim(),
        disposalAmount: confirmFormData.disposalAmount,
        financeConfirmFlag: confirmFormData.financeConfirmFlag,
        remark: confirmFormData.remark?.trim() || undefined
      })
      ElMessage.success('处置确认成功')
      confirmDialogVisible.value = false
      await Promise.all([refreshPoolData(), refreshRecordData()])
      activeTab.value = 'record'
    } finally {
      confirmSubmitting.value = false
    }
  }
</script>

<style scoped lang="scss">
  .asset-disposal-page {
    --asset-accent: #2f66ff;
    --asset-border: #e6ebf5;
    --asset-panel-bg: #fff;
    --asset-text-main: #18233a;
    --asset-text-secondary: #5d6b86;

    background:
      radial-gradient(circle at 0% 0%, rgb(47 102 255 / 8%), transparent 34%),
      radial-gradient(circle at 100% 0%, rgb(32 201 151 / 8%), transparent 36%),
      var(--art-main-bg-color);
    min-height: 0;
    overflow-y: auto;
    overflow-x: hidden;
  }

  .main-card {
    background: var(--asset-panel-bg);
    min-height: 560px;
  }

  .main-card {
    :deep(.el-card) {
      display: flex;
      flex-direction: column;
      height: 100%;
      min-height: 0;
    }

    :deep(.el-card__body) {
      display: flex;
      flex-direction: column;
      flex: 1;
      min-height: 0;
      height: auto;
      padding: 12px;
      overflow: hidden;
    }
  }

  :deep(.el-tabs__header) {
    margin-bottom: 12px;
  }

  :deep(.el-tabs) {
    display: flex;
    flex: 1;
    min-height: 0;
    flex-direction: column;
  }

  :deep(.el-tabs__content) {
    flex: 1;
    min-height: 0;
    overflow: hidden;
  }

  :deep(.el-tab-pane) {
    height: 100%;
  }

  .tab-pane-body {
    display: flex;
    flex-direction: column;
    height: 100%;
    min-height: 0;
    gap: 10px;
    overflow: hidden;
  }

  .inner-table-card {
    margin-top: 0;
    min-height: 440px;

    :deep(.el-card) {
      display: flex;
      flex-direction: column;
      height: 100%;
      min-height: 0;
    }

    :deep(.el-card__body) {
      display: flex;
      flex-direction: column;
      flex: 1;
      min-height: 0;
      height: auto;
      // 中文注释：底部留白保障分页与横向滚动条同时可见。
      padding: 0 16px 56px;
    }

    :deep(.art-table) {
      flex: 1;
      min-height: 0;
    }

    // 中文注释：覆盖表格组件默认固定高度，避免分页区域被底部裁切。
    :deep(.art-table .el-table) {
      margin-top: 0;
    }

    :deep(.el-card__header) {
      padding: 12px 16px;
      border-bottom: 1px solid #eaf0fb;
      background: linear-gradient(180deg, rgb(247 250 255 / 90%) 0%, #fff 100%);
    }
  }

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px 16px;
    flex-wrap: wrap;
  }

  .card-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 15px;
    font-weight: 600;
    color: var(--asset-text-main);

    &::before {
      content: '';
      width: 4px;
      height: 14px;
      border-radius: 999px;
      background: var(--asset-accent);
    }
  }

  .toolbar-actions {
    align-items: center;
  }

  :deep(.art-search-bar) {
    padding: 10px 16px 0;
  }

  :deep(.art-search-bar .el-form-item) {
    margin-bottom: 10px;
  }

  :deep(.art-search-bar .action-column .action-buttons-wrapper) {
    margin-bottom: 10px;
  }

  @media (max-width: 768px) {
    .inner-table-card {
      :deep(.el-card__body) {
        padding: 0 12px 40px;
      }
    }
  }
</style>
