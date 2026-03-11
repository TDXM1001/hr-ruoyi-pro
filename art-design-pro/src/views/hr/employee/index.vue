<!-- 员工管理页面 -->
<template>
  <div class="employee-page art-full-height flex !flex-row p-3 overflow-hidden">
    <!-- 左侧组织架构树 -->
    <DeptTreeAside
      :data="deptOptions"
      :width="200"
      :current-node-key="currentNodeKey"
      @node-click="handleNodeClick"
    />

    <!-- 右侧主体内容 -->
    <div class="flex-1 min-w-0 flex flex-col h-full overflow-hidden relative">
      <!-- 搜索栏 -->
      <ArtSearchBar
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
            <ElButton v-auth="'hr:employee:add'" type="primary" @click="handleAdd" v-ripple>
              新增入职
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
          rowKey="employeeId"
          @selection-change="handleSelectionChange"
          @pagination:size-change="handleSizeChange"
          @pagination:current-change="handleCurrentChange"
        />
      </ElCard>
    </div>

    <!-- 员工编辑弹窗 -->
    <EmployeeEditDialog
      v-model="dialogVisible"
      :dialog-type="dialogType"
      :employee-data="currentData"
      @success="refreshData"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, h } from 'vue';
import { listEmployee, delEmployee } from "@/api/hr/employee";
import { treeselect } from "@/api/hr/organization";
import { useTable } from '@/hooks/core/useTable';
import { useRouter } from 'vue-router';
import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue';
import DeptTreeAside from '@/components/business/DeptTreeAside/index.vue';
import { ElMessageBox, ElMessage } from 'element-plus';
import EmployeeEditDialog from './modules/employee-edit-dialog.vue';

defineOptions({ name: 'Employee' });

const router = useRouter();
const deptOptions = ref<any[]>([]);
const currentNodeKey = ref<number | string | null>(null);
const ids = ref<number[]>([]);

// 弹窗相关
const dialogVisible = ref(false);
const dialogType = ref<'add' | 'edit'>('add');
const currentData = ref<any>();

// 搜索渲染项
const initialSearchState = {
  name: '',
  employeeNo: ''
};
const formFilters = reactive({ ...initialSearchState });

const formItems = computed(() => [
  {
    label: '姓名',
    key: 'name',
    type: 'input',
    props: { placeholder: '请输入员工姓名', clearable: true }
  },
  {
    label: '工号',
    key: 'employeeNo',
    type: 'input',
    props: { placeholder: '请输入工号', clearable: true }
  }
]);

// 使用 useTable 钩子
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
    apiFn: listEmployee,
    apiParams: {
      orgId: undefined
    },
    columnsFactory: () => [
      { type: 'selection', width: 55, align: 'center' },
      { prop: 'employeeNo', label: '工号', width: 120 },
      { prop: 'name', label: '姓名', minWidth: 100 },
      { 
        prop: 'gender', 
        label: '性别', 
        width: 80,
        align: 'center',
        formatter: (row: any) => {
          const dict: any = { '0': '男', '1': '女', '2': '未知' };
          return dict[row.gender] || '未知';
        }
      },
      { prop: 'phone', label: '手机号码', width: 120 },
      { 
        prop: 'employeeStatus', 
        label: '状态', 
        width: 100,
        align: 'center',
        formatter: (row: any) => {
          const statusMap: any = {
            '0': { text: '待入职', type: 'info' },
            '1': { text: '试用', type: 'warning' },
            '2': { text: '正式', type: 'success' },
            '3': { text: '调岗中', type: 'primary' },
            '4': { text: '离职中', type: 'danger' },
            '5': { text: '已离职', type: 'info' }
          };
          const status = statusMap[row.employeeStatus] || { text: row.employeeStatus, type: 'info' };
          return h('span', { class: `el-tag el-tag--${status.type} el-tag--light` }, status.text);
        }
      },
      { 
        prop: 'hireDate', 
        label: '入职时间', 
        width: 120,
        formatter: (row: any) => row.hireDate ? row.hireDate.split('T')[0] : ''
      },
      {
        prop: 'operation',
        label: '操作',
        width: 200,
        align: 'right',
        formatter: (row: any) => {
          return h('div', { class: 'flex justify-end gap-2' }, [
            h(ArtButtonTable, {
              type: 'view',
              text: '档案',
              onClick: () => handleDetail(row)
            }),
            h(ArtButtonTable, {
              type: 'edit',
              onClick: () => handleUpdate(row)
            }),
            h(ArtButtonTable, {
              type: 'delete',
              onClick: () => handleDelete(row)
            })
          ]);
        }
      }
    ]
  }
});

/** 查询组织树 */
const getDeptTree = async () => {
  try {
    const response: any = await treeselect();
    deptOptions.value = response.data || [];
  } catch (error) {
    console.error('获取组织树失败:', error);
  }
};

/** 节点点击 */
const handleNodeClick = (data: any) => {
  currentNodeKey.value = data.id;
  searchParams.orgId = data.id;
  refreshData();
};

/** 选中数据 */
const handleSelectionChange = (selection: any[]) => {
  ids.value = selection.map(item => item.employeeId);
};

/** 重置 */
const handleReset = () => {
  Object.assign(formFilters, initialSearchState);
  searchParams.orgId = undefined;
  currentNodeKey.value = null;
  resetSearchParams();
};

/** 搜索 */
const handleSearch = () => {
  Object.assign(searchParams, formFilters);
  getData();
};

/** 新增 */
const handleAdd = () => {
  dialogType.value = 'add';
  currentData.value = undefined;
  dialogVisible.value = true;
};

/** 修改 */
const handleUpdate = (row: any) => {
  dialogType.value = 'edit';
  currentData.value = { ...row };
  dialogVisible.value = true;
};

/** 详情 */
const handleDetail = (row: any) => {
  router.push({ path: '/hr/employee/detail', query: { id: row.employeeId } });
};

/** 删除 */
const handleDelete = async (row?: any) => {
  const employeeIds = row?.employeeId || ids.value;
  if (!employeeIds || (Array.isArray(employeeIds) && employeeIds.length === 0)) return;

  try {
    await ElMessageBox.confirm('是否确认删除选中的员工档案?', '提示', {
      type: 'warning'
    });
    await delEmployee(employeeIds);
    ElMessage.success('删除成功');
    refreshData();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error);
    }
  }
};

onMounted(() => {
  getDeptTree();
});
</script>

<style lang="scss" scoped>
.employee-page {
  background-color: transparent;
  padding: 12px;
  gap: 12px;
}
</style>
