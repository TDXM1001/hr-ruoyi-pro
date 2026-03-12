<template>
  <div class="workflow-todo-page art-full-height flex flex-col p-3 overflow-hidden">
    <ArtSearchBar
      :key="business_type.length"
      v-model="formFilters"
      :items="formItems"
      :showExpand="false"
      @reset="handleReset"
      @search="handleSearch"
    />

    <ElCard class="art-table-card flex-1 overflow-hidden" shadow="never">
      <ArtTableHeader
        :showZebra="false"
        :loading="loading"
        v-model:columns="columnChecks"
        @refresh="refreshData"
      />

      <ArtTable
        ref="tableRef"
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
        rowKey="instanceId"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>

    <!-- 审批弹窗 -->
    <ElDialog v-model="dialogVisible" title="任务审批" width="500px" draggable destroy-on-close>
      <ElForm :model="formData" :rules="rules" ref="formRef" label-width="100px">
        <ElFormItem label="业务编号" prop="businessId">
          <ElInput v-model="currentTask.businessId" disabled />
        </ElFormItem>
        <ElFormItem label="业务类型" prop="businessType">
          <DictTag :options="business_type" :value="currentTask.businessType" />
        </ElFormItem>
        <ElFormItem label="审批意见" prop="comment">
          <ElInput
            v-model="formData.comment"
            type="textarea"
            :rows="4"
            placeholder="请输入审批意见"
            clearable
          />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <span class="dialog-footer">
          <ElButton @click="dialogVisible = false">取消</ElButton>
          <ElButton type="danger" plain @click="submitApproval('reject')" :loading="submitting"
            >驳回</ElButton
          >
          <ElButton type="primary" @click="submitApproval('approve')" :loading="submitting"
            >同意</ElButton
          >
        </span>
      </template>
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, computed, onMounted, h } from 'vue'
  import { ElMessage, ElButton } from 'element-plus'
  import type { FormInstance, FormRules } from 'element-plus'
  import { listTodo, approveTask, type ApproveReq } from '@/api/workflow/task'
  import { useTable } from '@/hooks/core/useTable'
  import { useDict } from '@/utils/dict'
  import DictTag from '@/components/DictTag/index.vue'

  defineOptions({ name: 'WorkflowTodo' })

  const { business_type, wf_status } = useDict('business_type', 'wf_status')

  // 查询参数
  const formFilters = reactive({
    businessId: '',
    businessType: undefined
  })

  const formItems = computed(() => [
    {
      label: '业务编号',
      key: 'businessId',
      type: 'input',
      props: { placeholder: '请输入业务编号', clearable: true }
    },
    {
      label: '业务类型',
      key: 'businessType',
      type: 'select',
      props: {
        placeholder: '业务类型',
        clearable: true,
        options: business_type.value
      }
    }
  ])

  // 表格逻辑
  const {
    columns,
    columnChecks,
    data,
    loading,
    pagination,
    searchParams,
    resetSearchParams,
    handleSizeChange,
    handleCurrentChange,
    refreshData,
    getData
  } = useTable({
    core: {
      apiFn: listTodo,
      columnsFactory: () => [
        { type: 'index', label: '序号', width: 60, align: 'center' },
        { prop: 'businessId', label: '业务单据编号', minWidth: 150 },
        {
          prop: 'businessType',
          label: '业务类型',
          width: 120,
          align: 'center',
          formatter: (row: any) =>
            h(DictTag, { options: business_type.value, value: row.businessType })
        },
        { prop: 'currentNode', label: '当前节点', width: 120, align: 'center' },
        {
          prop: 'status',
          label: '状态',
          width: 100,
          align: 'center',
          formatter: (row: any) => h(DictTag, { options: wf_status.value, value: row.status })
        },
        { prop: 'createTime', label: '到达时间', width: 170, align: 'center' },
        {
          prop: 'operation',
          label: '操作',
          width: 120,
          align: 'right',
          formatter: (row: any) => {
            return h('div', { class: 'flex justify-end' }, [
              h(
                ElButton,
                {
                  type: 'primary',
                  link: true,
                  onClick: () => handleProcess(row)
                },
                () => '处理'
              )
            ])
          }
        }
      ]
    }
  })

  const handleReset = () => {
    Object.assign(formFilters, { businessId: '', businessType: undefined })
    resetSearchParams()
  }

  const handleSearch = () => {
    Object.assign(searchParams, formFilters)
    getData()
  }

  // 审批流程处理
  const dialogVisible = ref(false)
  const formRef = ref<FormInstance>()
  const submitting = ref(false)
  const currentTask = ref<any>({})

  const formData = reactive({
    comment: ''
  })

  // 审批意见规则设定
  const rules: FormRules = {
    comment: [{ max: 200, message: '审批意见不能超过200个字符', trigger: 'blur' }]
  }

  const handleProcess = (row: any) => {
    currentTask.value = row
    formData.comment = ''
    dialogVisible.value = true
  }

  const submitApproval = async (action: 'approve' | 'reject') => {
    try {
      if (action === 'reject' && !formData.comment.trim()) {
        ElMessage.warning('驳回时请填写审批意见')
        return
      }

      await formRef.value?.validate()

      submitting.value = true
      const req: ApproveReq = {
        instanceId: currentTask.value.instanceId,
        action: action,
        comment: formData.comment || undefined
      }

      await approveTask(req)
      const actionText = action === 'approve' ? '同意' : '驳回'
      ElMessage.success(`已${actionText}该任务`)
      dialogVisible.value = false
      refreshData()
    } catch (error) {
      if (error && (error as any).message) {
        ElMessage.error((error as any).message)
      } else if (error !== 'cancel') {
        console.error('审批失败:', error)
      }
    } finally {
      submitting.value = false
    }
  }

  onMounted(() => {
    void business_type.value
    void wf_status.value
  })
</script>

<style lang="scss" scoped>
  .workflow-todo-page {
    padding: 12px;
  }
</style>
