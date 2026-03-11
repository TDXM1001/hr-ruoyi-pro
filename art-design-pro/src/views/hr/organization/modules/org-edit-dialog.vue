<template>
  <el-dialog :title="title" v-model="visible" width="600px" append-to-body>
    <el-form ref="orgRef" :model="form" :rules="rules" label-width="100px">
      <el-row>
        <el-col :span="24" v-if="form.parentId !== 0">
          <el-form-item label="上级组织" prop="parentId">
            <el-tree-select
              v-model="form.parentId"
              :data="deptOptions"
              :props="{ value: 'id', label: 'label', children: 'children' }"
              value-key="id"
              placeholder="选择上级组织"
              check-strictly
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="组织名称" prop="orgName">
            <el-input v-model="form.orgName" placeholder="请输入组织名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="组织编码" prop="orgCode">
            <el-input v-model="form.orgCode" placeholder="请输入组织编码" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="显示排序" prop="orderNum">
            <el-input-number v-model="form.orderNum" controls-position="right" :min="0" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="类型" prop="orgType">
            <el-select v-model="form.orgType" placeholder="请选择类型" style="width: 100%">
              <el-option label="集团" value="1" />
              <el-option label="公司" value="2" />
              <el-option label="事业部" value="3" />
              <el-option label="中心" value="4" />
              <el-option label="部门" value="5" />
              <el-option label="团队" value="6" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="成立日期" prop="establishDate">
            <el-date-picker clearable v-model="form.establishDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择成立日期" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="状态">
            <el-radio-group v-model="form.status">
              <el-radio label="0">正常</el-radio>
              <el-radio label="1">停用</el-radio>
            </el-radio-group>
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
import { getOrganization, addOrganization, updateOrganization, treeselect } from "@/api/hr/organization";
import { ElMessage } from 'element-plus';

const props = defineProps({
  modelValue: Boolean,
  dialogType: {
    type: String as () => 'add' | 'edit',
    default: 'add'
  },
  orgData: Object
});

const emit = defineEmits(['update:modelValue', 'success']);

const { proxy } = getCurrentInstance() as any;
const visible = ref(false);
const title = ref("");
const deptOptions = ref<any[]>([]);

const data = reactive({
  form: {
    orgId: undefined,
    parentId: undefined,
    orgName: undefined,
    orgCode: undefined,
    orderNum: 0,
    orgType: '5',
    establishDate: undefined,
    status: "0"
  },
  rules: {
    parentId: [{ required: true, message: "上级组织不能为空", trigger: "blur" }],
    orgName: [{ required: true, message: "组织名称不能为空", trigger: "blur" }],
    orderNum: [{ required: true, message: "显示排序不能为空", trigger: "blur" }]
  }
});

const { form, rules } = toRefs(data);

watch(() => props.modelValue, (val) => {
  visible.value = val;
  if (val) {
    getTreeselect();
    if (props.dialogType === 'edit' && props.orgData) {
      title.value = "修改组织架构";
      // 如果是编辑，且 orgData 是从表格行传来的，它可能已经包含了 ID
      // 我们可能需要调用接口获取详情以确保数据最新
      if (props.orgData.orgId) {
        getOrganization(props.orgData.orgId).then(res => {
          Object.assign(form.value, res.data);
          if (form.value.parentId === 0) form.value.parentId = undefined;
        });
      }
    } else {
      title.value = "新增组织架构";
      reset();
      if (props.orgData && props.orgData.orgId) {
        form.value.parentId = props.orgData.orgId;
      }
    }
  }
});

watch(() => visible.value, (val) => {
  emit('update:modelValue', val);
});

function getTreeselect() {
  treeselect().then(response => {
    deptOptions.value = response.data;
  });
}

function reset() {
  form.value = {
    orgId: undefined,
    parentId: undefined,
    orgName: undefined,
    orgCode: undefined,
    orderNum: 0,
    orgType: '5',
    establishDate: undefined,
    status: "0"
  };
  if (proxy.$refs.orgRef) {
    proxy.$refs.orgRef.resetFields();
  }
}

function submitForm() {
  proxy.$refs.orgRef.validate((valid: boolean) => {
    if (valid) {
      const postData = { ...form.value };
      if (postData.parentId === undefined) postData.parentId = 0;
      
      if (postData.orgId != undefined) {
        updateOrganization(postData).then(() => {
          ElMessage.success("修改成功");
          visible.value = false;
          emit('success');
        });
      } else {
        addOrganization(postData).then(() => {
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
