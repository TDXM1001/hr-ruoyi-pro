<template>
  <div class="asset-report-page p-3">
    <ElCard shadow="never" class="mb-3">
      <ElForm :model="filters" inline>
        <ElFormItem label="土地到期预警天数">
          <ElInputNumber v-model="filters.landTermWithinDays" :min="1" :max="3650" />
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" :loading="loading" @click="loadData">刷新报表</ElButton>
          <ElButton @click="handleReset">重置</ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>

    <ElRow :gutter="12" class="mb-3">
      <ElCol :xs="24" :sm="12" :xl="6">
        <ElCard shadow="never" class="metric-card">
          <div class="metric-label">资产总数</div>
          <div class="metric-value">{{ summary.totalAssetCount || 0 }}</div>
          <div class="metric-sub">总原值：{{ formatAmount(summary.totalOriginalValue) }}</div>
        </ElCard>
      </ElCol>
      <ElCol :xs="24" :sm="12" :xl="6">
        <ElCard shadow="never" class="metric-card">
          <div class="metric-label">固定资产</div>
          <div class="metric-value">{{ summary.fixedAssetCount || 0 }}</div>
          <div class="metric-sub">原值：{{ formatAmount(summary.fixedAssetOriginalValue) }}</div>
        </ElCard>
      </ElCol>
      <ElCol :xs="24" :sm="12" :xl="6">
        <ElCard shadow="never" class="metric-card">
          <div class="metric-label">不动产</div>
          <div class="metric-value">{{ summary.realEstateCount || 0 }}</div>
          <div class="metric-sub">原值：{{ formatAmount(summary.realEstateOriginalValue) }}</div>
        </ElCard>
      </ElCol>
      <ElCol :xs="24" :sm="12" :xl="6">
        <ElCard shadow="never" class="metric-card">
          <div class="metric-label">当前预警</div>
          <div class="metric-value">{{ totalWarningCount }}</div>
          <div class="metric-sub">最近口径：{{ filters.landTermWithinDays }} 天内到期</div>
        </ElCard>
      </ElCol>
    </ElRow>

    <ElRow :gutter="12">
      <ElCol :xs="24" :xl="10">
        <ElCard shadow="never" class="mb-3">
          <template #header>
            <div class="card-title">资产类型汇总</div>
          </template>
          <ElTable :data="summary.typeSummaries" v-loading="loading" stripe>
            <ElTableColumn label="资产类型" min-width="120">
              <template #default="{ row }">{{ formatAssetType(row.assetType) }}</template>
            </ElTableColumn>
            <ElTableColumn prop="assetCount" label="数量" width="90" align="center" />
            <ElTableColumn label="原值" min-width="140" align="right">
              <template #default="{ row }">{{ formatAmount(row.originalValueTotal) }}</template>
            </ElTableColumn>
          </ElTable>
        </ElCard>

        <ElCard shadow="never" class="mb-3">
          <template #header>
            <div class="card-title">资产状态分布</div>
          </template>
          <ElTable :data="summary.statusSummaries" v-loading="loading" stripe>
            <ElTableColumn label="资产状态" min-width="120">
              <template #default="{ row }">{{ formatAssetStatus(row.assetStatus) }}</template>
            </ElTableColumn>
            <ElTableColumn prop="assetCount" label="数量" width="90" align="center" />
            <ElTableColumn label="原值" min-width="140" align="right">
              <template #default="{ row }">{{ formatAmount(row.originalValueTotal) }}</template>
            </ElTableColumn>
          </ElTable>
        </ElCard>

        <ElCard shadow="never">
          <template #header>
            <div class="card-title">在用资产部门分布</div>
          </template>
          <ElTable :data="summary.deptSummaries" v-loading="loading" stripe>
            <ElTableColumn prop="deptName" label="部门" min-width="140" show-overflow-tooltip />
            <ElTableColumn prop="assetCount" label="数量" width="90" align="center" />
            <ElTableColumn label="原值" min-width="140" align="right">
              <template #default="{ row }">{{ formatAmount(row.originalValueTotal) }}</template>
            </ElTableColumn>
          </ElTable>
        </ElCard>
      </ElCol>

      <ElCol :xs="24" :xl="14">
        <ElCard shadow="never">
          <template #header>
            <div class="warning-header">
              <div class="card-title">资产预警列表</div>
              <div class="warning-subtitle">覆盖闲置、维修中、待审批、土地到期四类最小治理口径</div>
            </div>
          </template>

          <ElTabs v-model="activeWarningTab" class="warning-tabs">
            <ElTabPane
              v-for="tab in warningTabs"
              :key="tab.key"
              :label="tab.label"
              :name="tab.key"
            />
          </ElTabs>

          <ElTable :data="currentWarningRows" v-loading="loading" stripe empty-text="暂无预警">
            <ElTableColumn prop="warningLabel" label="预警类型" min-width="120" />
            <ElTableColumn prop="assetNo" label="资产编号" min-width="130" show-overflow-tooltip />
            <ElTableColumn prop="assetName" label="资产名称" min-width="150" show-overflow-tooltip />
            <ElTableColumn label="资产类型" width="100" align="center">
              <template #default="{ row }">{{ formatAssetType(row.assetType) }}</template>
            </ElTableColumn>
            <ElTableColumn label="资产状态" width="100" align="center">
              <template #default="{ row }">{{ formatAssetStatus(row.assetStatus) }}</template>
            </ElTableColumn>
            <ElTableColumn prop="deptName" label="部门" min-width="120" show-overflow-tooltip />
            <ElTableColumn prop="businessNo" label="业务单号" min-width="140" show-overflow-tooltip />
            <ElTableColumn prop="handlerName" label="处理人" min-width="100" show-overflow-tooltip />
            <ElTableColumn prop="dueDate" label="到期日" width="110" align="center" />
            <ElTableColumn prop="eventTime" label="触发时间" width="170" align="center" />
            <ElTableColumn prop="detail" label="说明" min-width="220" show-overflow-tooltip />
          </ElTable>
        </ElCard>
      </ElCol>
    </ElRow>
  </div>
</template>

<script setup lang="ts">
  import { computed, onMounted, reactive, ref } from 'vue'
  import { ElMessage } from 'element-plus'
  import { ASSET_STATUS_OPTIONS, ASSET_TYPE_OPTIONS } from '@/types/asset'
  import {
    getAssetReportSummary,
    getAssetWarnings,
    type AssetReportSummary,
    type AssetWarningCollection,
    type AssetWarningItem
  } from '@/api/asset/report'

  defineOptions({ name: 'AssetReport' })

  type WarningTabKey = keyof AssetWarningCollection

  const loading = ref(false)
  const activeWarningTab = ref<WarningTabKey>('idleAssets')
  const filters = reactive({
    landTermWithinDays: 90
  })
  const summary = ref<AssetReportSummary>(createEmptySummary())
  const warnings = ref<AssetWarningCollection>(createEmptyWarnings())

  const warningTabs = computed(() => [
    { key: 'idleAssets' as WarningTabKey, label: `闲置资产（${warnings.value.idleAssets.length}）` },
    {
      key: 'maintenanceAssets' as WarningTabKey,
      label: `维修中未关闭（${warnings.value.maintenanceAssets.length}）`
    },
    {
      key: 'pendingApprovalItems' as WarningTabKey,
      label: `待审批关键动作（${warnings.value.pendingApprovalItems.length}）`
    },
    {
      key: 'landTermExpiringAssets' as WarningTabKey,
      label: `土地到期（${warnings.value.landTermExpiringAssets.length}）`
    }
  ])

  const currentWarningRows = computed<AssetWarningItem[]>(() => warnings.value[activeWarningTab.value] || [])
  const totalWarningCount = computed(
    () =>
      warnings.value.idleAssets.length +
      warnings.value.maintenanceAssets.length +
      warnings.value.pendingApprovalItems.length +
      warnings.value.landTermExpiringAssets.length
  )

  function createEmptySummary(): AssetReportSummary {
    return {
      totalAssetCount: 0,
      totalOriginalValue: 0,
      fixedAssetCount: 0,
      fixedAssetOriginalValue: 0,
      realEstateCount: 0,
      realEstateOriginalValue: 0,
      typeSummaries: [],
      statusSummaries: [],
      deptSummaries: []
    }
  }

  function createEmptyWarnings(): AssetWarningCollection {
    return {
      idleAssets: [],
      maintenanceAssets: [],
      pendingApprovalItems: [],
      landTermExpiringAssets: []
    }
  }

  function formatAmount(value?: number) {
    return Number(value || 0).toLocaleString('zh-CN', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    })
  }

  function formatAssetType(value?: string) {
    return ASSET_TYPE_OPTIONS.find((item) => item.value === value)?.label || '--'
  }

  function formatAssetStatus(value?: string) {
    return ASSET_STATUS_OPTIONS.find((item) => item.value === value)?.label || '--'
  }

  async function loadData() {
    loading.value = true
    try {
      const [summaryResult, warningResult] = await Promise.all([
        getAssetReportSummary(),
        getAssetWarnings({ landTermWithinDays: filters.landTermWithinDays })
      ])

      summary.value = {
        ...createEmptySummary(),
        ...summaryResult
      }
      warnings.value = {
        ...createEmptyWarnings(),
        ...warningResult
      }
    } catch (error) {
      console.error('加载资产报表失败:', error)
      ElMessage.error('加载资产报表失败，请稍后重试')
    } finally {
      loading.value = false
    }
  }

  function handleReset() {
    filters.landTermWithinDays = 90
    void loadData()
  }

  onMounted(() => {
    void loadData()
  })
</script>

<style lang="scss" scoped>
  .metric-card {
    min-height: 120px;
  }

  .metric-label {
    color: var(--art-gray-600);
    font-size: 14px;
    margin-bottom: 12px;
  }

  .metric-value {
    font-size: 30px;
    font-weight: 700;
    color: var(--art-gray-900);
    line-height: 1;
    margin-bottom: 12px;
  }

  .metric-sub {
    color: var(--art-gray-500);
    font-size: 13px;
  }

  .card-title {
    font-size: 15px;
    font-weight: 700;
    color: var(--art-gray-800);
  }

  .warning-header {
    display: flex;
    flex-direction: column;
    gap: 6px;
  }

  .warning-subtitle {
    color: var(--art-gray-500);
    font-size: 12px;
  }

  :deep(.warning-tabs .el-tabs__header) {
    margin-bottom: 12px;
  }
</style>