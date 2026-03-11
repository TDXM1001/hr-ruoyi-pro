<!-- 组织管理页面 -->
<template>
  <div class="org-page art-full-height flex !flex-row p-3 overflow-hidden">
    <!-- 左侧组织树 -->
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
            <ElButton v-auth="'hr:organization:add'" type="primary" @click="handleAdd" v-ripple>
              新增组织
            </ElButton>
          </template>
        </ArtTableHeader>

        <!-- 表格 -->
        <ArtTable
          ref="tableRef"
          :loading="loading"
          :data="data"
          :columns="columns"
          rowKey="orgId"
          @selection-change="handleSelectionChange"
        />
      </ElCard>
    </div>

    <!-- 组织编辑弹窗 -->
    <OrgEditDialog
      v-model="dialogVisible"
      :dialog-type="dialogType"
      :org-data="currentData"
      @success="handleSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, h, nextTick } from 'vue';
import { listOrganization, delOrganization, treeselect } from "@/api/hr/organization";
import { useTableColumns } from '@/hooks/core/useTableColumns';
import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue';
import DeptTreeAside from '@/components/business/DeptTreeAside/index.vue';
import { ElMessageBox, ElMessage } from 'element-plus';
import OrgEditDialog from './modules/org-edit-dialog.vue';
import { handleTree } from '@/utils/ruoyi';

defineOptions({ name: 'Organization' });

const deptOptions = ref<any[]>([]);
const currentNodeKey = ref<number | string | null>(null);
const loading = ref(false);
const data = ref<any[]>([]);
const ids = ref<number[]>([]);

// 弹窗相关
const dialogVisible = ref(false);
const dialogType = ref<'add' | 'edit'>('add');
const currentData = ref<any>();

// 搜索渲染项
const initialSearchState = {
  orgName: '',
  orgCode: '',
  status: ''
};
const formFilters = reactive({ ...initialSearchState });

const formItems = computed(() => [
  {
    label: '组织名称',
    key: 'orgName',
    type: 'input',
    props: { placeholder: '请输入组织名称', clearable: true }
  },
  {
    label: '组织编码',
    key: 'orgCode',
    type: 'input',
    props: { placeholder: '请输入组织编码', clearable: true }
  },
  {
    label: '状态',
    key: 'status',
    type: 'select',
    props: {
      placeholder: '选择状态',
      clearable: true,
      options: [
        { label: '正常', value: '0' },
        { label: '停用', value: '1' }
      ]
    }
  }
]);

// 表格列配置
const { columnChecks, columns } = useTableColumns(() => [
  { prop: 'orgName', label: '组织名称', minWidth: 200 },
  { prop: 'orgCode', label: '组织编码', width: 120 },
  { 
    prop: 'orgType', 
    label: '类型', 
    width: 100,
    align: 'center',
    formatter: (row: any) => {
      const map: any = { '1': '集团', '2': '公司', '3': '事业部', '4': '中心', '5': '部门', '6': '团队' };
      return map[row.orgType] || '未知';
    }
  },
  { 
    prop: 'status', 
    label: '状态', 
    width: 80,
    align: 'center',
    formatter: (row: any) => {
      const type = row.status === '0' ? 'success' : 'danger';
      const text = row.status === '0' ? '正常' : '停用';
      return h('span', { class: `el-tag el-tag--${type} el-tag--light` }, text);
    }
  },
  { prop: 'orderNum', label: '排序', width: 80, align: 'center' },
  { 
    prop: 'createTime', 
    label: '创建时间', 
    width: 170, 
    align: 'center' 
  },
  {
    prop: 'operation',
    label: '操作',
    width: 160,
    align: 'right',
    formatter: (row: any) => {
      return h('div', { class: 'flex justify-end gap-2' }, [
        h(ArtButtonTable, {
          type: 'edit',
          onClick: () => handleUpdate(row)
        }),
        h(ArtButtonTable, {
          type: 'add',
          title: '新增',
          onClick: () => handleAdd(row)
        }),
        h(ArtButtonTable, {
          type: 'delete',
          onClick: () => handleDelete(row)
        })
      ]);
    }
  }
]);

/** 查询列表 */
const refreshData = async () => {
  loading.value = true;
  try {
    const response: any = await listOrganization({
      ...formFilters,
      parentId: currentNodeKey.value || undefined
    });
    // 如果是顶级或未选，则处理成树结构展示
    if (!currentNodeKey.value) {
       data.value = handleTree(response.data, 'orgId');
    } else {
       data.value = response.data || [];
    }
  } catch (error) {
    console.error('获取列表失败:', error);
  } finally {
    loading.value = false;
  }
};

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
  refreshData();
};

/** 选中数据 */
const handleSelectionChange = (selection: any[]) => {
  ids.value = selection.map(item => item.orgId);
};

/** 重置 */
const handleReset = () => {
  Object.assign(formFilters, initialSearchState);
  currentNodeKey.value = null;
  refreshData();
};

/** 搜索 */
const handleSearch = () => {
  refreshData();
};

/** 新增 */
const handleAdd = (row?: any) => {
  dialogType.value = 'add';
  currentData.value = row || { orgId: currentNodeKey.value };
  dialogVisible.value = true;
};

/** 修改 */
const handleUpdate = (row: any) => {
  dialogType.value = 'edit';
  currentData.value = { ...row };
  dialogVisible.value = true;
};

/** 成功回调 */
const handleSuccess = () => {
  refreshData();
  getDeptTree();
};

/** 删除 */
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(`是否确认删除名称为"${row.orgName}"的数据项?`, '提示', {
      type: 'warning'
    });
    await delOrganization(row.orgId);
    ElMessage.success('删除成功');
    handleSuccess();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error);
    }
  }
};

onMounted(() => {
  getDeptTree();
  refreshData();
});
</script>

<style lang="scss" scoped>
.org-page {
  background-color: transparent;
  padding: 12px;
  gap: 12px;
}
</style>
