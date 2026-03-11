<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!-- 组织架构树部件 -->
      <el-col :span="6" :xs="24">
        <org-tree 
          :dept-options="deptOptions"
          @node-click="handleNodeClick"
          @node-drop="handleNodeDrop"
        />
      </el-col>
      <!-- 组织架构列表与查询部件 -->
      <el-col :span="18" :xs="24">
        <org-list 
          ref="orgListRef"
          :org-list="orgList"
          :loading="loading"
          @query="handleQuery"
          @add="handleAdd"
          @update="handleUpdate"
          @delete="handleDelete"
        />
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import OrgTree from './components/OrgTree.vue';
import OrgList from './components/OrgList.vue';

const loading = ref(false);
const orgListRef = ref();

// 模拟树形部门结构数据
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

// 模拟列表部门数据
const orgList = ref([
  {
    orgId: 101,
    orgName: '开发部',
    orgCode: 'DEV-001',
    orgLevel: 3,
    establishDate: '2023-01-01'
  }
]);

// 模拟获取数据行为
function getList() {
  console.log('Fetching data with params:', orgListRef.value?.queryParams);
}

// 树节点被点击触发查询
function handleNodeClick(data: any) {
  if (orgListRef.value) {
    orgListRef.value.queryParams.parentId = data.orgId;
    getList();
  }
}

// 组织架构树节点拖拽事件处理
function handleNodeDrop(dropInfo: any) {
  console.log('Node dropped:', dropInfo);
  //后续可在处提交拖拽带来的架构变更逻辑至后端
}

// 查询列表按钮触发
function handleQuery(queryParams: any) {
  getList();
}

// 新增子级组织按钮触发
function handleAdd() {
  console.log('Add node');
}

// 修改按键触发
function handleUpdate(row: any) {
  console.log('Update node', row);
}

// 删除组件按键触发
function handleDelete(row: any) {
  console.log('Delete node', row);
}
</script>
