<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!-- 组织架构树 -->
      <el-col :span="6" :xs="24">
        <div class="head-container">
          <el-input
            v-model="deptName"
            placeholder="请输入组织名称"
            clearable
            prefix-icon="Search"
            style="margin-bottom: 20px"
          />
        </div>
        <div class="head-container">
          <el-tree
            :data="deptOptions"
            :props="{ label: 'orgName', children: 'children' }"
            :expand-on-click-node="false"
            :filter-node-method="filterNode"
            ref="deptTreeRef"
            node-key="orgId"
            highlight-current
            default-expand-all
            @node-click="handleNodeClick"
          />
        </div>
      </el-col>
      <!-- 组织架构表格 -->
      <el-col :span="18" :xs="24">
        <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
          <el-form-item label="组织名称" prop="orgName">
            <el-input
              v-model="queryParams.orgName"
              placeholder="请输入组织名称"
              clearable
              style="width: 240px"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="组织状态" clearable style="width: 240px">
              <el-option value="0" label="正常" />
              <el-option value="1" label="停用" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button
              type="primary"
              plain
              icon="Plus"
              @click="handleAdd"
              v-hasPermi="['hr:organization:add']"
            >新增子级组织</el-button>
          </el-col>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>

        <el-table v-loading="loading" :data="orgList" row-key="orgId" default-expand-all>
          <el-table-column prop="orgName" label="组织名称" width="260"></el-table-column>
          <el-table-column prop="orgCode" label="组织编码" width="100"></el-table-column>
          <el-table-column prop="orgLevel" label="层级" align="center" width="80"></el-table-column>
          <el-table-column prop="establishDate" label="成立日期" align="center" width="120">
            <template #default="scope">
              <span>{{ parseTime(scope.row.establishDate, '{y}-{m}-{d}') }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
            <template #default="scope">
              <el-button type="text" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hr:organization:edit']">修改</el-button>
              <el-button type="text" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hr:organization:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, toRefs, watch } from 'vue';

const loading = ref(false);
const showSearch = ref(true);
const deptName = ref('');
const deptOptions = ref([
  {
    orgId: 1,
    orgName: '集团总部',
    children: [
      { orgId: 2, orgName: '技术中心' },
      { orgId: 3, orgName: '产品中心' }
    ]
  }
]);

const orgList = ref([
  {
    orgId: 101,
    orgName: '开发部',
    orgCode: 'DEV-001',
    orgLevel: 3,
    establishDate: '2023-01-01'
  }
]);

const data = reactive({
  queryParams: {
    orgName: undefined,
    status: undefined,
    parentId: undefined
  }
});

const { queryParams } = toRefs(data);

function getList() {
  // TODO: mock local get
}

function handleNodeClick(data: any) {
  queryParams.value.parentId = data.orgId;
  getList();
}

function handleQuery() {
  getList();
}

function resetQuery() {
  queryParams.value.orgName = undefined;
  queryParams.value.status = undefined;
  handleQuery();
}

function handleAdd() {}
function handleUpdate(row: any) {}
function handleDelete(row: any) {}

function filterNode(value: string, data: any) {
  if (!value) return true;
  return data.orgName.indexOf(value) !== -1;
}

function parseTime(time: string, format: string) {
  if (!time) return '';
  return time.split('T')[0];
}

watch(deptName, val => {
  // @ts-ignore
  // deptTreeRef.value?.filter(val);
});
</script>
