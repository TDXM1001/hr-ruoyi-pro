<template>
  <div class="app-container p-5">
    <!-- 搜索表单 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" class="mb-4">
      <el-form-item label="部门名称" prop="deptName">
        <el-input v-model="queryParams.deptName" placeholder="请输入部门名称" clearable @keyup.enter="getList" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="部门状态" clearable>
          <el-option label="正常" value="0" />
          <el-option label="停用" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="getList">搜索</el-button>
      </el-form-item>
    </el-form>

    <!-- 列表数据 -->
    <el-table
      v-loading="loading"
      :data="deptList"
      row-key="deptId"
      default-expand-all
      :tree-props="{ children: 'children' }"
    >
      <el-table-column prop="deptName" label="部门名称"></el-table-column>
      <el-table-column prop="orderNum" label="排序" width="200"></el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'info'">{{ scope.row.status === '0' ? '正常' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200">
        <template #default="scope">
          <el-button link type="primary" icon="Edit">修改</el-button>
          <el-button link type="primary" icon="Plus">新增</el-button>
          <el-button link type="danger" icon="Delete">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { listDept } from '@/api/system/dept'
import { handleTree } from '@/utils/ruoyi'

const deptList = ref<any[]>([])
const loading = ref(false)
const queryParams = reactive({ deptName: undefined, status: undefined })

function getList() {
  loading.value = true
  listDept(queryParams).then((response: any) => {
    deptList.value = handleTree(response.data, "deptId")
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

onMounted(() => {
  getList()
})
</script>
