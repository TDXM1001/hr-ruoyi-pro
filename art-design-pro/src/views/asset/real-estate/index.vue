<template>
  <div class="asset-real-estate-page art-full-height flex flex-col gap-2 overflow-hidden p-3">
    <ElCard class="flex-1 min-h-0 overflow-hidden" shadow="never">
      <ArtSearchBar
        v-model="searchForm"
        :items="searchItems"
        :showExpand="false"
        @search="handleSearch"
        @reset="handleReset"
      />

      <ArtTable
        rowKey="profileId"
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>

    <ElDrawer v-model="detailVisible" title="不动产权属详情" size="560px">
      <ElDescriptions v-if="detailData" :column="1" border>
        <ElDescriptionsItem label="资产ID">{{ detailData.assetId || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="权属证号">{{
          detailData.ownershipCertNo || '-'
        }}</ElDescriptionsItem>
        <ElDescriptionsItem label="土地用途">{{ detailData.landUseType || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="建筑面积(㎡)">{{ detailData.buildingArea ?? '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="备注">{{ detailData.remark || '-' }}</ElDescriptionsItem>
      </ElDescriptions>
    </ElDrawer>
  </div>
</template>

<script setup lang="ts">
  import { computed, h, reactive, ref } from 'vue'
  import { ElButton } from 'element-plus'
  import { useTable } from '@/hooks/core/useTable'
  import {
    getRealEstateDetail,
    getRealEstateList,
    type AssetRealEstateProfile
  } from '@/api/asset/real-estate'

  defineOptions({ name: 'AssetRealEstate' })

  type RealEstateRow = {
    profileId: number
    assetId?: number
    ownershipCertNo?: string
    landUseType?: string
    buildingArea?: number
  }

  const initialSearchState = {
    ownershipCertNo: '',
    landUseType: ''
  }
  const searchForm = reactive({ ...initialSearchState })

  const searchItems = computed(() => [
    {
      label: '权属证号',
      key: 'ownershipCertNo',
      type: 'input',
      props: { placeholder: '请输入权属证号', clearable: true }
    },
    {
      label: '土地用途',
      key: 'landUseType',
      type: 'input',
      props: { placeholder: '请输入土地用途', clearable: true }
    }
  ])

  const detailVisible = ref(false)
  const detailData = ref<AssetRealEstateProfile>()

  const {
    columns,
    data,
    loading,
    pagination,
    searchParams,
    getData,
    resetSearchParams,
    handleSizeChange,
    handleCurrentChange
  } = useTable({
    core: {
      apiFn: getRealEstateList,
      apiParams: {
        pageNum: 1,
        pageSize: 10
      },
      columnsFactory: () => [
        { prop: 'assetId', label: '资产ID', width: 100 },
        { prop: 'ownershipCertNo', label: '权属证号', minWidth: 180 },
        { prop: 'landUseType', label: '土地用途', width: 120 },
        { prop: 'buildingArea', label: '建筑面积(㎡)', width: 140 },
        {
          prop: 'operation',
          label: '操作',
          width: 100,
          fixed: 'right',
          formatter: (row: RealEstateRow) =>
            h(
              ElButton,
              { link: true, type: 'primary', onClick: () => openDetail(row.assetId) },
              () => '详情'
            )
        }
      ]
    }
  })

  const buildSearchParams = () => {
    return {
      ownershipCertNo: searchForm.ownershipCertNo?.trim() || undefined,
      landUseType: searchForm.landUseType?.trim() || undefined
    }
  }

  const handleSearch = () => {
    Object.assign(searchParams, buildSearchParams())
    getData()
  }

  const handleReset = () => {
    Object.assign(searchForm, initialSearchState)
    resetSearchParams()
  }

  // 中文注释：详情查询始终按资产ID拉取，确保与台账主档一一对应。
  const openDetail = async (assetId?: number) => {
    if (!assetId) {
      return
    }
    const response: any = await getRealEstateDetail(assetId)
    detailData.value = response?.data || response
    detailVisible.value = true
  }
</script>

<style scoped lang="scss">
  .asset-real-estate-page {
    background:
      radial-gradient(circle at 0% 0%, rgb(47 102 255 / 8%), transparent 35%),
      var(--art-main-bg-color);
  }
</style>
