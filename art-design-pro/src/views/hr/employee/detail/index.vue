<template>
  <div class="app-container">
    <el-card class="box-card employee-detail-header">
      <div class="header-info">
        <el-avatar :size="64" :src="employee.photo || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
        <div class="header-text">
          <h3>{{ employee.name }} <el-tag size="small">{{ formatStatus(employee.employeeStatus) }}</el-tag></h3>
          <p>工号：{{ employee.employeeNo }} | 手机：{{ employee.phone }} </p>
        </div>
      </div>
      <div class="header-action">
        <el-button @click="goBack">返回列表</el-button>
        <el-button type="primary">编辑档案</el-button>
      </div>
    </el-card>

    <div class="employee-tabs">
      <el-tabs v-model="activeName">
        <el-tabPane label="基本信息" name="basic">
          <el-card class="box-card">
            <template #header>基本资料</template>
            <el-descriptions :column="3" border>
              <el-descriptions-item label="姓名">{{ employee.name }}</el-descriptions-item>
              <el-descriptions-item label="性别">{{ formatGender(employee.gender) }}</el-descriptions-item>
              <el-descriptions-item label="出生日期">{{ parseTime(employee.birthday) }}</el-descriptions-item>
              <el-descriptions-item label="身份证号">{{ employee.idCard }}</el-descriptions-item>
              <el-descriptions-item label="户籍">{{ employee.nativePlace }}</el-descriptions-item>
              <el-descriptions-item label="民族">{{ employee.nationality }}</el-descriptions-item>
              <el-descriptions-item label="婚姻状况">{{ employee.maritalStatus }}</el-descriptions-item>
              <el-descriptions-item label="最高学历">{{ employee.highestEdu }}</el-descriptions-item>
              <el-descriptions-item label="邮箱">{{ employee.email }}</el-descriptions-item>
            </el-descriptions>
          </el-card>

          <el-card class="box-card mt-4">
            <template #header>入职与工作信息</template>
            <el-descriptions :column="3" border>
              <el-descriptions-item label="用工类型">{{ formatType(employee.employeeType) }}</el-descriptions-item>
              <el-descriptions-item label="入职日期">{{ parseTime(employee.hireDate) }}</el-descriptions-item>
              <el-descriptions-item label="转正日期">{{ parseTime(employee.regularDate) }}</el-descriptions-item>
              <el-descriptions-item label="工作地点">{{ employee.workLocation }}</el-descriptions-item>
              <!-- 需要联表查询的数据暂留空 -->
              <el-descriptions-item label="所属组织">-</el-descriptions-item>
              <el-descriptions-item label="岗位">-</el-descriptions-item>
              <el-descriptions-item label="职级">-</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-tabPane>
        <el-tabPane label="教育经历" name="edu">
          <el-empty description="暂无教育经历"></el-empty>
        </el-tabPane>
        <el-tabPane label="工作经历" name="work">
          <el-empty description="暂无工作经历"></el-empty>
        </el-tabPane>
        <el-tabPane label="合同信息" name="contract">
          <el-empty description="暂无合同信息"></el-empty>
        </el-tabPane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getEmployee } from "@/api/hr/employee";

const route = useRoute();
const router = useRouter();
const activeName = ref('basic');
const employee = ref<any>({});

function loadEmployee() {
  const id = route.query.id as string;
  if (id) {
    getEmployee(id).then(res => {
      employee.value = res.data;
    });
  }
}

function goBack() {
  router.back();
}

function formatStatus(status: string) {
  const dict: Record<string, string> = { '0': '待入职', '1': '试用', '2': '正式', '3': '调岗中', '4': '离职中', '5': '已离职' };
  return dict[status] || status;
}

function formatGender(gender: string) {
  const dict: Record<string, string> = { '0': '男', '1': '女', '2': '未知' };
  return dict[gender] || gender;
}

function formatType(type: string) {
  const dict: Record<string, string> = { '1': '正式', '2': '实习', '3': '劳务派遣', '4': '外包' };
  return dict[type] || type;
}

function parseTime(time: string) {
  if (!time) return '';
  return time.split('T')[0];
}

onMounted(() => {
  loadEmployee();
});
</script>

<style scoped>
.employee-detail-header {
  margin-bottom: 20px;
}
.header-info {
  display: flex;
  align-items: center;
  float: left;
}
.header-text {
  margin-left: 20px;
}
.header-text h3 {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
}
.header-text p {
  color: #666;
  margin: 5px 0 0 0;
}
.header-action {
  float: right;
  display: flex;
  align-items: center;
  height: 64px;
}
.mt-4 {
  margin-top: 16px;
}
</style>
