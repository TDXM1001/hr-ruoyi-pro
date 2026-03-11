<template>
  <div class="head-container">
    <el-input
      v-model="deptName"
      placeholder="请输入组织名称"
      clearable
      prefix-icon="Search"
      style="margin-bottom: 20px"
    />
    <el-tree
      :data="deptOptions"
      :props="{ label: 'orgName', children: 'children' }"
      :expand-on-click-node="false"
      :filter-node-method="filterNode"
      ref="deptTreeRef"
      node-key="orgId"
      highlight-current
      default-expand-all
      draggable
      :allow-drop="allowDrop"
      :allow-drag="allowDrag"
      @node-click="handleNodeClick"
      @node-drop="handleDrop"
    >
      <template #default="{ node, data }">
        <span class="custom-tree-node">
          <span>{{ node.label }}</span>
        </span>
      </template>
    </el-tree>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';

const props = defineProps({
  deptOptions: {
    type: Array,
    required: true
  }
});

const emit = defineEmits(['node-click', 'node-drop']);

const deptName = ref('');
const deptTreeRef = ref();

// 节点点击事件
function handleNodeClick(data: any) {
  emit('node-click', data);
}

// 树节点过滤
function filterNode(value: string, data: any) {
  if (!value) return true;
  return data.orgName.indexOf(value) !== -1;
}

watch(deptName, val => {
  deptTreeRef.value?.filter(val);
});

// 拖拽相关判断：是否允许放入
function allowDrop(draggingNode: any, dropNode: any, type: string) {
  // 暂时允许自由拖拽
  return true;
}

// 是否允许拖拽节点
function allowDrag(draggingNode: any) {
  // 允许所有节点拖拽
  return true;
}

// 拖拽完成事件抛出
function handleDrop(draggingNode: any, dropNode: any, dropType: string, ev: any) {
  emit('node-drop', { draggingNode, dropNode, dropType });
}
</script>

<style scoped>
.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding-right: 8px;
}
</style>
