<template>
  <div>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, toRefs } from 'vue';

const props = defineProps({
  orgList: {
    type: Array,
    required: true
  },
  loading: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['query', 'add', 'update', 'delete']);

const showSearch = ref(true);

const data = reactive({
  queryParams: {
    orgName: undefined,
    status: undefined,
    parentId: undefined
  }
});

const { queryParams } = toRefs(data);

// 提取数据获取请求给父组件处理
function getList() {
  emit('query', queryParams.value);
}

// 搜索操作
function handleQuery() {
  getList();
}

// 重置查询条件
function resetQuery() {
  queryParams.value.orgName = undefined;
  queryParams.value.status = undefined;
  handleQuery();
}

// 添加操作触发
function handleAdd() {
  emit('add');
}

// 修改操作触发
function handleUpdate(row: any) {
  emit('update', row);
}

// 删除操作触发
function handleDelete(row: any) {
  emit('delete', row);
}

// 日期格式化辅助函数
function parseTime(time: string, format: string) {
  if (!time) return '';
  return time.split('T')[0];
}

// 暴露 queryParams 给父组件以支持根据节点修改参数
defineExpose({
  queryParams
});
</script>
