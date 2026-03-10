<!-- 在线用户监控页面 -->
<template>
  <div class="art-full-height">
    <OnlineSearch
      v-show="showSearchBar"
      v-model="searchForm"
      @search="handleSearch"
      @reset="resetSearchParams"
    ></OnlineSearch>

    <ElCard
      class="art-table-card"
      shadow="never"
      :style="{ 'margin-top': showSearchBar ? '12px' : '0' }"
    >
      <ArtTableHeader
        v-model:columns="columnChecks"
        v-model:showSearchBar="showSearchBar"
        :loading="loading"
        @refresh="refreshData"
      >
      </ArtTableHeader>

      <!-- 表格 -->
      <ArtTable
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      >
      </ArtTable>
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { useTable } from '@/hooks/core/useTable'
  import { listOnline, forceLogout } from '@/api/monitor/online'
  import OnlineSearch from './modules/online-search.vue'
  import { ElMessageBox, ElMessage, ElButton } from 'element-plus'

  defineOptions({ name: 'Online' })

  // 搜索表单
  const searchForm = ref({
    userName: undefined,
    ipaddr: undefined
  })

  const showSearchBar = ref(true)

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
      apiFn: listOnline,
      apiParams: {
        pageNum: 1,
        pageSize: 20
      },
      columnsFactory: () => [
        {
          prop: 'tokenId',
          label: '会话编号',
          minWidth: 150,
          showOverflowTooltip: true
        },
        {
          prop: 'userName',
          label: '登录名称',
          minWidth: 100
        },
        {
          prop: 'ipaddr',
          label: '登录地址',
          minWidth: 120
        },
        {
          prop: 'loginLocation',
          label: '登录地点',
          minWidth: 120
        },
        {
          prop: 'browser',
          label: '浏览器',
          width: 100
        },
        {
          prop: 'os',
          label: '操作系统',
          width: 100
        },
        {
          prop: 'loginTime',
          label: '登录时间',
          width: 180,
          formatter: (row: any) => {
            return row.loginTime ? new Date(row.loginTime).toLocaleString() : '-'
          }
        },
        {
          prop: 'operation',
          label: '操作',
          width: 100,
          fixed: 'right',
          formatter: (row: any) =>
            h(
              ElButton,
              {
                type: 'danger',
                link: true,
                icon: 'ri:logout-box-r-line',
                onClick: () => handleForceLogout(row)
              },
              () => '强退'
            )
        }
      ]
    }
  })

  /**
   * 搜索处理
   * @param params 搜索参数
   */
  const handleSearch = (params: any) => {
    Object.assign(searchParams, params)
    getData()
  }

  /**
   * 强退处理
   * @param row 行数据
   */
  const handleForceLogout = (row: any) => {
    ElMessageBox.confirm(`是否确认强退用户"${row.userName}"?`, '系统提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(async () => {
        await forceLogout(row.tokenId)
        ElMessage.success('强退成功')
        refreshData()
      })
      .catch(() => {})
  }
</script>
