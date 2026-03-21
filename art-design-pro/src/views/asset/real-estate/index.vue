<template>
  <div class="asset-real-estate-page art-full-height flex flex-col gap-3 p-3">
    <ElAlert
      title="不动产档案已拆分为列表、详情、新建、编辑四个独立页面，列表只承担查询与跳转入口。"
      type="info"
      :closable="false"
      show-icon
    />

    <ArtSearchBar
      v-model="searchForm"
      :items="searchItems"
      :showExpand="true"
      @search="handleSearch"
      @reset="handleReset"
    />

    <ElCard class="art-table-card flex-1 overflow-hidden" shadow="never">
      <ArtTableHeader
        :showZebra="false"
        :loading="loading"
        v-model:columns="columnChecks"
        @refresh="refreshData"
      >
        <template #left>
          <ElSpace wrap>
            <ElButton v-auth="'asset:realEstate:add'" type="primary" @click="handleAdd" v-ripple>
              新建不动产档案
            </ElButton>
            <ElTag type="success" effect="light">资产类型：不动产</ElTag>
          </ElSpace>
        </template>
      </ArtTableHeader>

      <ArtTable
        rowKey="assetId"
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { computed, h, reactive } from 'vue'
  import { ElButton } from 'element-plus'
  import { getRealEstateList } from '@/api/asset/real-estate'
  import { useTable } from '@/hooks/core/useTable'
  import { useDict } from '@/utils/dict'
  import { useUserStore } from '@/store/modules/user'
  import DictTag from '@/components/DictTag/index.vue'

  defineOptions({ name: 'AssetRealEstate' })

  const router = useRouter()
  const userStore = useUserStore()
  const { ast_asset_status } = useDict('ast_asset_status')

  const initialSearchState = {
    assetCode: '',
    assetName: '',
    ownershipCertNo: '',
    landUseType: '',
    assetStatus: ''
  }

  const searchForm = reactive({ ...initialSearchState })

  const searchItems = computed(() => [
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
    },
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
    },
    {
      label: '资产状态',
      key: 'assetStatus',
      type: 'select',
      props: {
        placeholder: '请选择资产状态',
        clearable: true,
        options: ast_asset_status.value
      }
    }
  ])

  const hasPermission = (permission: string) => {
    return userStore.permissions.includes('*:*:*') || userStore.permissions.includes(permission)
  }

  const canQuery = computed(() => hasPermission('asset:realEstate:query'))
  const canEdit = computed(() => hasPermission('asset:realEstate:edit'))

  const {
    columns,
    columnChecks,
    data,
    loading,
    pagination,
    getData,
    searchParams,
    resetSearchParams,
    handleSizeChange,
    handleCurrentChange,
    refreshData
  } = useTable({
    core: {
      apiFn: getRealEstateList,
      apiParams: {
        pageNum: 1,
        pageSize: 10
      },
      columnsFactory: () => [
        { prop: 'assetCode', label: '资产编码', minWidth: 150 },
        { prop: 'assetName', label: '资产名称', minWidth: 180 },
        { prop: 'ownershipCertNo', label: '权属证号', minWidth: 220 },
        { prop: 'landUseType', label: '土地用途', width: 120 },
        {
          prop: 'buildingArea',
          label: '建筑面积(㎡)',
          width: 130,
          formatter: (row: any) => formatArea(row.buildingArea)
        },
        { prop: 'ownerDeptName', label: '权属部门', minWidth: 130 },
        { prop: 'useDeptName', label: '使用部门', minWidth: 130 },
        {
          prop: 'assetStatus',
          label: '资产状态',
          width: 120,
          formatter: (row: any) => h(DictTag, { options: ast_asset_status.value, value: row.assetStatus })
        },
        {
          prop: 'operation',
          label: '操作',
          width: 150,
          fixed: 'right',
          align: 'right',
          formatter: (row: any) => {
            const actions = []
            if (canQuery.value) {
              actions.push(
                h(
                  ElButton,
                  {
                    link: true,
                    type: 'primary',
                    onClick: () => handleDetail(row)
                  },
                  () => '详情'
                )
              )
            }
            if (canEdit.value) {
              actions.push(
                h(
                  ElButton,
                  {
                    link: true,
                    type: 'primary',
                    onClick: () => handleEdit(row)
                  },
                  () => '编辑'
                )
              )
            }

            return h(
              'div',
              {
                class: 'flex justify-end gap-1'
              },
              actions
            )
          }
        }
      ]
    }
  })

  const formatArea = (value?: number | string) => {
    if (value === null || value === undefined || value === '') {
      return '-'
    }
    return Number(value).toLocaleString('zh-CN', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    })
  }

  // 中文注释：列表页只负责入口检索，避免再次退回抽屉承载详情的旧模式。
  const buildSearchParams = () => {
    return {
      assetCode: searchForm.assetCode?.trim() || undefined,
      assetName: searchForm.assetName?.trim() || undefined,
      ownershipCertNo: searchForm.ownershipCertNo?.trim() || undefined,
      landUseType: searchForm.landUseType?.trim() || undefined,
      assetStatus: searchForm.assetStatus || undefined
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

  const handleAdd = () => {
    router.push('/asset/real-estate/create')
  }

  const handleDetail = (row: any) => {
    router.push(`/asset/real-estate/detail/${row.assetId}`)
  }

  const handleEdit = (row: any) => {
    router.push(`/asset/real-estate/edit/${row.assetId}`)
  }
</script>

<style scoped lang="scss">
  .asset-real-estate-page {
    background:
      radial-gradient(circle at 0% 0%, rgb(31 122 140 / 8%), transparent 34%),
      var(--art-main-bg-color);
  }
</style>