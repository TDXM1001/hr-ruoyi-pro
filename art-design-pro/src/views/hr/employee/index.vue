<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="姓名" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入员工姓名" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="工号" prop="employeeNo">
        <el-input v-model="queryParams.employeeNo" placeholder="请输入工号" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hr:employee:add']">新增入职</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="employeeList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="工号" align="center" prop="employeeNo" />
      <el-table-column label="姓名" align="center" prop="name" />
      <el-table-column label="性别" align="center" prop="gender">
        <template #default="scope">
          <span v-if="scope.row.gender === '0'">男</span>
          <span v-else-if="scope.row.gender === '1'">女</span>
          <span v-else>未知</span>
        </template>
      </el-table-column>
      <el-table-column label="手机号码" align="center" prop="phone" width="120" />
      <el-table-column label="状态" align="center" prop="employeeStatus">
        <template #default="scope">
          <el-tag v-if="scope.row.employeeStatus === '2'" type="success">正式</el-tag>
          <el-tag v-else-if="scope.row.employeeStatus === '1'" type="warning">试用</el-tag>
          <el-tag v-else type="info">{{ scope.row.employeeStatus }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="入职时间" align="center" prop="hireDate" width="120">
        <template #default="scope">
          <span>{{ parseTime(scope.row.hireDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" fixed="right" width="200" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button type="text" icon="View" @click="handleDetail(scope.row)" v-hasPermi="['hr:employee:query']">档案</el-button>
          <el-button type="text" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hr:employee:edit']">修改</el-button>
          <el-button type="text" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hr:employee:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </div>
</template>

<script setup name="Employee" lang="ts">
import { ref, reactive, toRefs, onMounted } from 'vue';
import { listEmployee, delEmployee } from "@/api/hr/employee";
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';

const router = useRouter();
const loading = ref(true);
const showSearch = ref(true);
const total = ref(0);
const employeeList = ref([]);
const ids = ref<string[] | number[]>([]);

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    name: undefined,
    employeeNo: undefined
  }
});

const { queryParams } = toRefs(data);

function getList() {
  loading.value = true;
  listEmployee(queryParams.value).then(res => {
    employeeList.value = res.rows;
    total.value = res.total;
    loading.value = false;
  }).catch(() => {
    loading.value = false;
  });
}

function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

function resetQuery() {
  queryParams.value.name = undefined;
  queryParams.value.employeeNo = undefined;
  handleQuery();
}

function handleSelectionChange(selection: any[]) {
  ids.value = selection.map(item => item.employeeId);
}

function handleAdd() {
  // TODO 弹窗处理
}

function handleUpdate(row: any) {
  // TODO 弹窗更新信息
}

function handleDetail(row: any) {
  router.push({ path: '/hr/employee/detail', query: { id: row.employeeId } });
}

function handleDelete(row: any) {
  const employeeIds = row.employeeId || ids.value;
  ElMessageBox.confirm('是否确认删除该员工档案?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(function() {
    return delEmployee(employeeIds);
  }).then(() => {
    getList();
    ElMessage.success("删除成功");
  }).catch(() => {});
}

// 帮助函数模拟
function parseTime(time: string, format: string) {
  if (!time) return '';
  return time.split('T')[0];
}

onMounted(() => {
  getList();
});
</script>
