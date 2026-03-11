<template>
  <el-dialog :title="title" v-model="visible" width="600px" append-to-body>
    <el-form ref="employeeRef" :model="form" :rules="rules" label-width="100px">
      <el-row>
        <el-col :span="12">
          <el-form-item label="姓名" prop="name">
            <el-input v-model="form.name" placeholder="请输入员工姓名" maxlength="30" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="工号" prop="employeeNo">
            <el-input v-model="form.employeeNo" placeholder="请输入工号(唯一)" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="所属组织" prop="orgId">
            <el-tree-select
              v-model="form.orgId"
              :data="deptOptions"
              :props="{ value: 'id', label: 'label', children: 'children' }"
              value-key="id"
              placeholder="选择所属组织"
              check-strictly
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="性别" prop="gender">
            <el-select v-model="form.gender" placeholder="请选择性别" style="width: 100%">
              <el-option label="男" value="0" />
              <el-option label="女" value="1" />
              <el-option label="未知" value="2" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="手机号码" prop="phone">
            <el-input v-model="form.phone" placeholder="请输入手机号码" maxlength="11" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" placeholder="请输入邮箱" maxlength="50" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="身份证号" prop="idCard">
            <el-input v-model="form.idCard" placeholder="请输入身份证号" maxlength="18" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="用工类型" prop="employeeType">
            <el-select v-model="form.employeeType" placeholder="请选择用工类型" style="width: 100%">
              <el-option label="正式" value="1" />
              <el-option label="实习" value="2" />
              <el-option label="劳务派遣" value="3" />
              <el-option label="外包" value="4" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="员工状态" prop="employeeStatus">
            <el-select v-model="form.employeeStatus" placeholder="请选择员工状态" style="width: 100%">
              <el-option label="待入职" value="0" />
              <el-option label="试用" value="1" />
              <el-option label="正式" value="2" />
              <el-option label="调岗中" value="3" />
              <el-option label="离职中" value="4" />
              <el-option label="已离职" value="5" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="入职日期" prop="hireDate">
            <el-date-picker clearable v-model="form.hireDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择入职日期" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="工作地点" prop="workLocation">
            <el-input v-model="form.workLocation" placeholder="请输入工作地点" maxlength="100" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, reactive, toRefs, onMounted, getCurrentInstance } from 'vue';
import { getEmployee, addEmployee, updateEmployee } from "@/api/hr/employee";
import { treeselect } from "@/api/hr/organization";
import { ElMessage } from 'element-plus';

const props = defineProps({
  modelValue: Boolean,
  dialogType: {
    type: String as () => 'add' | 'edit',
    default: 'add'
  },
  employeeData: Object
});

const emit = defineEmits(['update:modelValue', 'success']);

const { proxy } = getCurrentInstance() as any;
const visible = ref(false);
const title = ref("");
const deptOptions = ref<any[]>([]);

const data = reactive({
  form: {
    employeeId: undefined,
    name: undefined,
    employeeNo: undefined,
    orgId: undefined,
    gender: '0',
    phone: undefined,
    email: undefined,
    idCard: undefined,
    employeeType: '1',
    employeeStatus: '2',
    hireDate: undefined,
    workLocation: undefined
  },
  rules: {
    name: [{ required: true, message: "姓名不能为空", trigger: "blur" }],
    employeeNo: [{ required: true, message: "工号不能为空", trigger: "blur" }],
    orgId: [{ required: true, message: "所属组织不能为空", trigger: "blur" }],
    employeeType: [{ required: true, message: "用工类型不能为空", trigger: "change" }],
    employeeStatus: [{ required: true, message: "员工状态不能为空", trigger: "change" }],
    hireDate: [{ required: true, message: "入职日期不能为空", trigger: "blur" }]
  }
});

const { form, rules } = toRefs(data);

watch(() => props.modelValue, (val) => {
  visible.value = val;
  if (val) {
    getTreeselect();
    if (props.dialogType === 'edit' && props.employeeData) {
      title.value = "修改档案";
      // 如果有数据则回显
      Object.assign(form.value, props.employeeData);
    } else {
      title.value = "新增入职";
      reset();
    }
  }
});

watch(() => visible.value, (val) => {
  emit('update:modelValue', val);
});

/** 查询部门下拉树结构 */
function getTreeselect() {
  treeselect().then(response => {
    deptOptions.value = response.data;
  });
}

// 表单重置
function reset() {
  form.value = {
    employeeId: undefined,
    name: undefined,
    employeeNo: undefined,
    orgId: undefined,
    gender: '0',
    phone: undefined,
    email: undefined,
    idCard: undefined,
    employeeType: '1',
    employeeStatus: '2',
    hireDate: undefined,
    workLocation: undefined
  };
  if (proxy.$refs.employeeRef) {
    proxy.$refs.employeeRef.resetFields();
  }
}

function submitForm() {
  proxy.$refs.employeeRef.validate((valid: boolean) => {
    if (valid) {
      if (form.value.employeeId != undefined) {
        updateEmployee(form.value).then(() => {
          ElMessage.success("修改成功");
          visible.value = false;
          emit('success');
        });
      } else {
        addEmployee(form.value).then(() => {
          ElMessage.success("新增成功");
          visible.value = false;
          emit('success');
        });
      }
    }
  });
}

function cancel() {
  visible.value = false;
  reset();
}
</script>
