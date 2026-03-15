<template>
  <ElDrawer
    :title="dialogType === 'add' ? '新增资产' : '修改资产'"
    v-model="visible"
    size="860px"
    destroy-on-close
    @closed="handleClosed"
  >
    <div class="asset-edit-drawer h-full" v-loading="loading">
      <ElForm ref="formRef" :model="state" :rules="formRules" label-width="110px">
        <ElTabs v-model="activeTab">
          <ElTabPane label="基础信息" name="basic">
            <ElRow :gutter="16">
              <ElCol :span="12">
                <ElFormItem label="资产编号" prop="basicForm.assetNo">
                  <ElInput
                    v-model="state.basicForm.assetNo"
                    placeholder="请输入资产编号"
                    :disabled="dialogType === 'edit'"
                  />
                </ElFormItem>
              </ElCol>
              <ElCol :span="12">
                <ElFormItem label="资产名称" prop="basicForm.assetName">
                  <ElInput v-model="state.basicForm.assetName" placeholder="请输入资产名称" />
                </ElFormItem>
              </ElCol>
              <ElCol :span="12">
                <ElFormItem label="资产分类" prop="basicForm.categoryId">
                  <ElTreeSelect
                    v-model="state.basicForm.categoryId"
                    :data="categoryOptions"
                    :props="{ value: 'id', label: 'name', children: 'children' }"
                    value-key="id"
                    placeholder="选择资产分类"
                    check-strictly
                    class="w-full"
                  />
                </ElFormItem>
              </ElCol>
              <ElCol :span="12">
                <ElFormItem label="资产类型" prop="basicForm.assetType">
                  <ElSelect
                    v-model="state.basicForm.assetType"
                    placeholder="请选择资产类型"
                    class="w-full"
                  >
                    <ElOption
                      v-for="item in asset_type"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </ElSelect>
                </ElFormItem>
              </ElCol>
              <ElCol :span="12">
                <ElFormItem label="规格型号">
                  <ElInput v-model="state.basicForm.specModel" placeholder="请输入规格型号" />
                </ElFormItem>
              </ElCol>
              <ElCol :span="12">
                <ElFormItem label="计量单位">
                  <ElInput v-model="state.basicForm.unit" placeholder="请输入计量单位" />
                </ElFormItem>
              </ElCol>
              <ElCol :span="12">
                <ElFormItem label="管理部门">
                  <ElTreeSelect
                    v-model="state.basicForm.manageDeptId"
                    :data="deptOptions"
                    :props="{ value: 'id', label: 'label', children: 'children' }"
                    value-key="id"
                    placeholder="选择管理部门"
                    check-strictly
                    class="w-full"
                  />
                </ElFormItem>
              </ElCol>
              <ElCol :span="12">
                <ElFormItem label="使用部门">
                  <ElTreeSelect
                    v-model="state.basicForm.useDeptId"
                    :data="deptOptions"
                    :props="{ value: 'id', label: 'label', children: 'children' }"
                    value-key="id"
                    placeholder="选择使用部门"
                    check-strictly
                    class="w-full"
                  />
                </ElFormItem>
              </ElCol>
              <ElCol :span="12">
                <ElFormItem label="责任人">
                  <ElSelect
                    v-model="state.basicForm.responsibleUserId"
                    placeholder="选择责任人"
                    class="w-full"
                    clearable
                  >
                    <ElOption
                      v-for="item in userOptions"
                      :key="item.userId"
                      :label="item.nickName"
                      :value="item.userId"
                    />
                  </ElSelect>
                </ElFormItem>
              </ElCol>
              <ElCol :span="12">
                <ElFormItem label="当前使用人">
                  <ElSelect
                    v-model="state.basicForm.userId"
                    placeholder="选择当前使用人"
                    class="w-full"
                    clearable
                  >
                    <ElOption
                      v-for="item in userOptions"
                      :key="item.userId"
                      :label="item.nickName"
                      :value="item.userId"
                    />
                  </ElSelect>
                </ElFormItem>
              </ElCol>
              <ElCol :span="12">
                <ElFormItem label="位置描述">
                  <ElInput v-model="state.basicForm.locationText" placeholder="请输入位置描述" />
                </ElFormItem>
              </ElCol>
              <ElCol :span="12">
                <ElFormItem label="资产状态" prop="basicForm.assetStatus">
                  <ElSelect
                    v-model="state.basicForm.assetStatus"
                    placeholder="请选择资产状态"
                    class="w-full"
                  >
                    <ElOption
                      v-for="item in asset_status"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </ElSelect>
                </ElFormItem>
              </ElCol>
            </ElRow>
          </ElTabPane>

          <ElTabPane label="财务信息" name="finance">
            <ElAlert
              v-if="financeBaseReadonly"
              title="该资产已开始折旧，财务基础字段已切换为只读。"
              type="warning"
              class="mb-4"
              :closable="false"
            />
            <ElRow :gutter="16">
              <ElCol :span="12">
                <ElFormItem label="账簿类型" prop="financeForm.bookType">
                  <ElSelect
                    v-model="state.financeForm.bookType"
                    placeholder="请选择账簿类型"
                    class="w-full"
                    :disabled="financeBaseReadonly"
                  >
                    <ElOption
                      v-for="item in bookTypeOptions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </ElSelect>
                </ElFormItem>
              </ElCol>
              <ElCol :span="12">
                <ElFormItem label="币种" prop="financeForm.currencyCode">
                  <ElInput
                    v-model="state.financeForm.currencyCode"
                    placeholder="请输入币种编码"
                    :disabled="financeBaseReadonly"
                  />
                </ElFormItem>
              </ElCol>
              <ElCol :span="12">
                <ElFormItem label="原值" prop="financeForm.originalValue">
                  <ElInputNumber
                    v-model="state.financeForm.originalValue"
                    :min="0"
                    :precision="2"
                    class="w-full"
                    :disabled="financeBaseReadonly"
                  />
                </ElFormItem>
              </ElCol>
              <ElCol :span="12">
                <ElFormItem label="净残值率" prop="financeForm.salvageRate">
                  <ElInputNumber
                    v-model="state.financeForm.salvageRate"
                    :min="0"
                    :max="1"
                    :precision="4"
                    :step="0.01"
                    class="w-full"
                    :disabled="financeBaseReadonly"
                  />
                </ElFormItem>
              </ElCol>
              <ElCol :span="12">
                <ElFormItem label="折旧方法" prop="financeForm.depreciationMethod">
                  <ElSelect
                    v-model="state.financeForm.depreciationMethod"
                    placeholder="请选择折旧方法"
                    class="w-full"
                    :disabled="financeBaseReadonly"
                  >
                    <ElOption
                      v-for="item in depreciationMethodOptions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </ElSelect>
                </ElFormItem>
              </ElCol>
              <ElCol :span="12">
                <ElFormItem label="折旧月数" prop="financeForm.usefulLifeMonth">
                  <ElInputNumber
                    v-model="state.financeForm.usefulLifeMonth"
                    :min="0"
                    class="w-full"
                    :disabled="financeBaseReadonly"
                  />
                </ElFormItem>
              </ElCol>
              <ElCol :span="12">
                <ElFormItem label="折旧起始日" prop="financeForm.depreciationStartDate">
                  <ElDatePicker
                    v-model="state.financeForm.depreciationStartDate"
                    type="date"
                    value-format="YYYY-MM-DD"
                    placeholder="选择折旧起始日"
                    class="w-full"
                    :disabled="financeBaseReadonly"
                  />
                </ElFormItem>
              </ElCol>
              <ElCol :span="12">
                <ElFormItem label="累计折旧">
                  <ElInputNumber
                    v-model="state.financeForm.accumulatedDepreciation"
                    :precision="2"
                    class="w-full"
                    disabled
                  />
                </ElFormItem>
              </ElCol>
            </ElRow>
          </ElTabPane>

          <ElTabPane label="不动产信息" name="realEstate" :disabled="!showRealEstateTab">
            <ElAlert
              v-if="!showRealEstateTab"
              title="当前资产类型不是不动产，提交时不会携带不动产信息。"
              type="info"
              class="mb-4"
              :closable="false"
            />
            <ElRow :gutter="16">
              <ElCol :span="12">
                <ElFormItem label="产权证号">
                  <ElInput
                    v-model="state.realEstateForm.propertyCertNo"
                    placeholder="请输入产权证号"
                    :disabled="!showRealEstateTab"
                  />
                </ElFormItem>
              </ElCol>
              <ElCol :span="12">
                <ElFormItem label="不动产单元号">
                  <ElInput
                    v-model="state.realEstateForm.realEstateUnitNo"
                    placeholder="请输入不动产单元号"
                    :disabled="!showRealEstateTab"
                  />
                </ElFormItem>
              </ElCol>
              <ElCol :span="24">
                <ElFormItem label="坐落地址">
                  <ElInput
                    v-model="state.realEstateForm.addressFull"
                    placeholder="请输入完整坐落地址"
                    :disabled="!showRealEstateTab"
                  />
                </ElFormItem>
              </ElCol>
              <ElCol :span="12">
                <ElFormItem label="土地用途">
                  <ElInput
                    v-model="state.realEstateForm.landUse"
                    placeholder="请输入土地用途"
                    :disabled="!showRealEstateTab"
                  />
                </ElFormItem>
              </ElCol>
              <ElCol :span="12">
                <ElFormItem label="建筑用途">
                  <ElInput
                    v-model="state.realEstateForm.buildingUse"
                    placeholder="请输入建筑用途"
                    :disabled="!showRealEstateTab"
                  />
                </ElFormItem>
              </ElCol>
              <ElCol :span="12">
                <ElFormItem label="建筑面积">
                  <ElInputNumber
                    v-model="state.realEstateForm.buildingArea"
                    :min="0"
                    :precision="2"
                    class="w-full"
                    :disabled="!showRealEstateTab"
                  />
                </ElFormItem>
              </ElCol>
              <ElCol :span="12">
                <ElFormItem label="竣工日期">
                  <ElDatePicker
                    v-model="state.realEstateForm.completionDate"
                    type="date"
                    value-format="YYYY-MM-DD"
                    placeholder="选择竣工日期"
                    class="w-full"
                    :disabled="!showRealEstateTab"
                  />
                </ElFormItem>
              </ElCol>
              <ElCol :span="12">
                <ElFormItem label="权利人">
                  <ElInput
                    v-model="state.realEstateForm.rightsHolder"
                    placeholder="请输入权利人"
                    :disabled="!showRealEstateTab"
                  />
                </ElFormItem>
              </ElCol>
            </ElRow>
          </ElTabPane>

          <ElTabPane label="扩展信息" name="extras">
            <ElAlert
              v-if="dynamicAttrConflictCodes.length"
              :title="`以下动态属性编码与系统保留字段冲突：${dynamicAttrConflictCodes.join('、')}`"
              type="error"
              class="mb-4"
              :closable="false"
            />
            <ElCard shadow="never" class="mb-4">
              <template #header>
                <div class="flex items-center justify-between">
                  <span>动态属性</span>
                  <span class="text-xs text-[var(--art-gray-500)]">切换资产分类后自动加载</span>
                </div>
              </template>

              <ElEmpty
                v-if="!state.dynamicAttrDefinitions.length"
                description="当前分类暂无动态属性定义"
                :image-size="80"
              />

              <ElRow v-else :gutter="16">
                <ElCol
                  v-for="item in state.dynamicAttrDefinitions"
                  :key="item.attrId"
                  :span="item.dataType === 'json' ? 24 : 12"
                >
                  <ElFormItem :label="item.attrName || item.attrCode">
                    <template v-if="getAttrOptions(item).length">
                      <ElSelect
                        v-model="state.dynamicAttrForm[item.attrCode]"
                        class="w-full"
                        clearable
                        :placeholder="`请选择${item.attrName || item.attrCode}`"
                      >
                        <ElOption
                          v-for="option in getAttrOptions(item)"
                          :key="option.value"
                          :label="option.label"
                          :value="option.value"
                        />
                      </ElSelect>
                    </template>
                    <template v-else-if="item.dataType === 'number'">
                      <ElInputNumber
                        v-model="state.dynamicAttrForm[item.attrCode]"
                        class="w-full"
                        :min="0"
                      />
                    </template>
                    <template v-else-if="item.dataType === 'date'">
                      <ElDatePicker
                        v-model="state.dynamicAttrForm[item.attrCode]"
                        type="date"
                        value-format="YYYY-MM-DD"
                        class="w-full"
                        :placeholder="`选择${item.attrName || item.attrCode}`"
                      />
                    </template>
                    <template v-else-if="item.dataType === 'json'">
                      <ElInput
                        v-model="state.dynamicAttrForm[item.attrCode]"
                        type="textarea"
                        :rows="4"
                        :placeholder="`请输入${item.attrName || item.attrCode} JSON 内容`"
                      />
                    </template>
                    <template v-else>
                      <ElInput
                        v-model="state.dynamicAttrForm[item.attrCode]"
                        :placeholder="`请输入${item.attrName || item.attrCode}`"
                      />
                    </template>
                  </ElFormItem>
                </ElCol>
              </ElRow>
            </ElCard>

            <ElCard shadow="never" class="mb-4">
              <template #header>
                <div class="flex items-center justify-between">
                  <span>附件</span>
                  <span class="text-xs text-[var(--art-gray-500)]">上传后会直接回填到附件数组</span>
                </div>
              </template>

              <ElUpload :show-file-list="false" :http-request="handleUploadRequest">
                <ElButton type="primary" plain>上传附件</ElButton>
              </ElUpload>

              <ElEmpty
                v-if="!state.attachments.length"
                description="暂无附件"
                :image-size="80"
                class="mt-4"
              />

              <div v-else class="mt-4 flex flex-col gap-2">
                <div
                  v-for="(item, index) in state.attachments"
                  :key="`${item.fileUrl}-${index}`"
                  class="flex items-center justify-between rounded border border-solid border-[var(--art-gray-200)] px-3 py-2"
                >
                  <div class="min-w-0">
                    <div class="truncate text-sm">{{ item.fileName }}</div>
                    <div class="text-xs text-[var(--art-gray-500)]">
                      {{ formatAttachmentMeta(item) }}
                    </div>
                  </div>
                  <ElButton link type="danger" @click="removeAttachment(index)">删除</ElButton>
                </div>
              </div>
            </ElCard>

            <ElFormItem label="备注">
              <ElInput
                v-model="state.basicForm.remark"
                type="textarea"
                :rows="4"
                placeholder="请输入备注"
              />
            </ElFormItem>
          </ElTabPane>
        </ElTabs>
      </ElForm>
    </div>

    <template #footer>
      <div class="drawer-footer flex justify-end gap-2 p-4 border-t">
        <ElButton @click="visible = false" v-ripple>取消</ElButton>
        <ElButton type="primary" @click="handleSubmit" :loading="submitLoading" v-ripple>
          确定
        </ElButton>
      </div>
    </template>
  </ElDrawer>
</template>

<script setup lang="ts">
  import { ref, reactive, watch, computed } from 'vue'
  import { ElMessage } from 'element-plus'
  import type { FormInstance, FormRules, UploadRequestOptions } from 'element-plus'
  import type {
    AssetAttachment,
    AssetDynamicAttrDefinition,
    AssetDynamicAttrValue
  } from '@/types/asset'
  import { getInfo, addInfo, updateInfo } from '@/api/asset/info'
  import { listCategory } from '@/api/asset/category'
  import { listCategoryAttrs } from '@/api/asset/category-attr'
  import { uploadCommonFile } from '@/api/common/upload'
  import { listUser, deptTreeSelect } from '@/api/system/user'
  import { handleTree } from '@/utils/ruoyi'
  import { useDict } from '@/utils/dict'
  import { findReservedAttrCodes, toDynamicAttrFormRecord } from './asset-dynamic-attr.helper'
  import { canEditFinanceBaseFields } from './asset-finance.helper'
  import {
    buildAggregatePayload,
    createEmptyDrawerState,
    hydrateDrawerState,
    type AssetDrawerState
  } from './asset-form.mapper'

  const props = defineProps<{
    modelValue: boolean
    dialogType: 'add' | 'edit'
    assetData?: any
  }>()

  const emit = defineEmits<{
    (e: 'update:modelValue', value: boolean): void
    (e: 'success'): void
  }>()

  // 状态管理
  const visible = ref(false)
  const loading = ref(false)
  const submitLoading = ref(false)
  const activeTab = ref('basic')
  const formRef = ref<FormInstance>()

  // 选项数据
  const categoryOptions = ref<any[]>([])
  const deptOptions = ref<any[]>([])
  const userOptions = ref<any[]>([])

  // 字典数据
  const { asset_type, asset_status } = useDict('asset_type', 'asset_status')

  const state = reactive<AssetDrawerState>(createEmptyDrawerState())

  const bookTypeOptions = [
    { label: '财务账', value: '1' },
    { label: '预算账', value: '2' }
  ]

  const depreciationMethodOptions = [
    { label: '直线法', value: '1' },
    { label: '双倍余额递减法', value: '2' },
    { label: '年数总和法', value: '3' }
  ]

  const showRealEstateTab = computed(() => state.basicForm.assetType === '2')
  const financeBaseReadonly = computed(() => !canEditFinanceBaseFields(state.financeForm))
  const dynamicAttrConflictCodes = ref<string[]>([])
  const activeBizType = ref('asset')

  const formRules: FormRules = {
    'basicForm.assetNo': [{ required: true, message: '资产编号不能为空', trigger: 'blur' }],
    'basicForm.assetName': [{ required: true, message: '资产名称不能为空', trigger: 'blur' }],
    'basicForm.categoryId': [{ required: true, message: '资产分类不能为空', trigger: 'change' }],
    'basicForm.assetType': [{ required: true, message: '资产类型不能为空', trigger: 'change' }],
    'basicForm.assetStatus': [{ required: true, message: '资产状态不能为空', trigger: 'change' }],
    'financeForm.bookType': [{ required: true, message: '账簿类型不能为空', trigger: 'change' }],
    'financeForm.currencyCode': [{ required: true, message: '币种不能为空', trigger: 'blur' }],
    'financeForm.originalValue': [{ required: true, message: '原值不能为空', trigger: 'change' }],
    'financeForm.salvageRate': [{ required: true, message: '净残值率不能为空', trigger: 'change' }],
    'financeForm.depreciationMethod': [
      { required: true, message: '折旧方法不能为空', trigger: 'change' }
    ],
    'financeForm.usefulLifeMonth': [
      { required: true, message: '折旧月数不能为空', trigger: 'change' }
    ],
    'financeForm.depreciationStartDate': [
      { required: true, message: '折旧起始日不能为空', trigger: 'change' }
    ]
  }

  const resetState = () => {
    Object.assign(state, createEmptyDrawerState())
    dynamicAttrConflictCodes.value = []
  }

  /** 把后端 optionSource 解析成可直接渲染的下拉选项。 */
  const getAttrOptions = (item: AssetDynamicAttrDefinition) => {
    if (!item.optionSourceType || !item.optionSource) {
      return []
    }

    const raw = item.optionSource.trim()
    if (!raw) {
      return []
    }

    try {
      const parsed = JSON.parse(raw)
      if (Array.isArray(parsed)) {
        return parsed.map((option: any) => {
          if (typeof option === 'string') {
            return { label: option, value: option }
          }
          return {
            label: option.label ?? option.name ?? option.value,
            value: option.value ?? option.code ?? option.label
          }
        })
      }
    } catch {
      // 非 JSON 文本继续走分隔符解析。
    }

    return raw
      .split(/[\n,]/)
      .map((entry) => entry.trim())
      .filter(Boolean)
      .map((entry) => {
        const [value, label] = entry.includes(':') ? entry.split(':') : [entry, entry]
        return {
          label: (label || value).trim(),
          value: value.trim()
        }
      })
  }

  /** 合并分类定义与现有动态属性值，保证编辑态可以正确回填。 */
  const mergeCategoryAttrs = (
    definitions: AssetDynamicAttrDefinition[],
    values: AssetDynamicAttrValue[] = []
  ) => {
    const valueMap = new Map(values.map((item) => [item.attrCode, item]))

    return definitions.map((item) => ({
      ...valueMap.get(item.attrCode),
      attrId: item.attrId,
      categoryId: item.categoryId,
      attrCode: item.attrCode,
      dataType: item.dataType
    }))
  }

  /** 根据分类加载动态属性定义，并同步表单回填。 */
  const loadCategoryAttrs = async (categoryId?: number, values: AssetDynamicAttrValue[] = []) => {
    if (!categoryId) {
      state.dynamicAttrDefinitions = []
      state.dynamicAttrs = []
      state.dynamicAttrForm = {}
      dynamicAttrConflictCodes.value = []
      return
    }

    const definitions = await listCategoryAttrs(categoryId)
    const reservedCodes = findReservedAttrCodes(definitions)
    dynamicAttrConflictCodes.value = reservedCodes

    if (reservedCodes.length) {
      state.dynamicAttrDefinitions = []
      state.dynamicAttrs = []
      state.dynamicAttrForm = {}
      ElMessage.warning(`动态属性编码与系统保留字段冲突：${reservedCodes.join('、')}`)
      return
    }

    state.dynamicAttrDefinitions = definitions
    state.dynamicAttrs = mergeCategoryAttrs(definitions, values)
    state.dynamicAttrForm = {
      ...Object.fromEntries(definitions.map((item) => [item.attrCode, item.defaultValue || ''])),
      ...toDynamicAttrFormRecord(values)
    }
  }

  /** 处理附件上传并回填到聚合状态。 */
  const handleUploadRequest = async (options: UploadRequestOptions) => {
    try {
      const resp = await uploadCommonFile(options.file as File)
      state.attachments.push({
        bizType: activeBizType.value,
        fileName: resp.originalFilename || resp.newFileName,
        fileUrl: resp.url,
        fileSize: options.file.size,
        fileSuffix: (options.file.name.split('.').pop() || '').toLowerCase()
      })
      options.onSuccess?.(resp)
      ElMessage.success('附件上传成功')
    } catch (error) {
      options.onError?.(error as any)
      ElMessage.error('附件上传失败')
    }
  }

  /** 删除附件。 */
  const removeAttachment = (index: number) => {
    state.attachments.splice(index, 1)
  }

  /** 格式化附件展示信息。 */
  const formatAttachmentMeta = (item: AssetAttachment) => {
    const sizeText = item.fileSize ? `${(item.fileSize / 1024).toFixed(1)} KB` : '未知大小'
    const suffixText = item.fileSuffix ? `.${item.fileSuffix}` : '未知类型'
    return `${suffixText} / ${sizeText}`
  }

  /** 获取下拉选项 */
  const getOptions = async () => {
    try {
      const [catRes, deptRes, userRes] = await Promise.all([
        listCategory(),
        deptTreeSelect(),
        listUser({ pageSize: 200 })
      ])

      // 分类树
      const catList = Array.isArray(catRes)
        ? catRes
        : (catRes as any).data || (catRes as any).rows || []
      categoryOptions.value = handleTree(catList, 'id')

      // 部门树
      deptOptions.value = Array.isArray(deptRes) ? deptRes : (deptRes as any).data || []

      // 用户下拉
      userOptions.value = (userRes as any).rows || (userRes as any).data || []
    } catch (error) {
      console.error('获取选项失败:', error)
    }
  }

  /** 校验不动产页签的必要字段。 */
  const validateRealEstateTab = () => {
    if (!showRealEstateTab.value) {
      return true
    }
    if (!state.realEstateForm.propertyCertNo || !state.realEstateForm.addressFull) {
      activeTab.value = 'realEstate'
      ElMessage.warning('不动产资产必须填写产权证号和坐落地址')
      return false
    }
    return true
  }

  watch(
    () => props.modelValue,
    async (val) => {
      visible.value = val
      if (val) {
        activeTab.value = 'basic'
        await getOptions()
        if (props.dialogType === 'edit' && props.assetData?.assetId) {
          loading.value = true
          try {
            const res: any = await getInfo(props.assetData.assetId)
            Object.assign(state, hydrateDrawerState(res.data || res))
            await loadCategoryAttrs(state.basicForm.categoryId, state.dynamicAttrs)
          } finally {
            loading.value = false
          }
        } else if (props.dialogType === 'add') {
          resetState()
        }
      }
    }
  )

  watch(
    () => visible.value,
    (val) => {
      emit('update:modelValue', val)
    }
  )

  watch(
    () => state.basicForm.categoryId,
    async (newCategoryId, oldCategoryId) => {
      if (!visible.value || !newCategoryId) {
        if (!newCategoryId) {
          await loadCategoryAttrs(undefined)
        }
        return
      }

      if (newCategoryId === oldCategoryId) {
        return
      }

      // 编辑态首次回填已在打开抽屉时主动加载，这里只处理用户后续切换分类。
      if (oldCategoryId === undefined && props.dialogType === 'edit' && state.dynamicAttrs.length) {
        return
      }

      await loadCategoryAttrs(newCategoryId)
    }
  )

  const handleSubmit = async () => {
    if (!formRef.value) return
    const valid = await formRef.value.validate().catch(() => false)
    if (!valid || !validateRealEstateTab()) {
      return
    }
    if (dynamicAttrConflictCodes.value.length) {
      activeTab.value = 'extras'
      ElMessage.warning('请先处理动态属性编码冲突后再提交')
      return
    }

    submitLoading.value = true
    try {
      const payload = buildAggregatePayload(state, props.dialogType)
      if (props.dialogType === 'edit') {
        await updateInfo(payload)
        ElMessage.success('修改成功')
      } else {
        await addInfo(payload)
        ElMessage.success('新增成功')
      }
      visible.value = false
      emit('success')
    } finally {
      submitLoading.value = false
    }
  }

  const handleClosed = () => {
    formRef.value?.clearValidate()
    resetState()
    activeTab.value = 'basic'
    categoryOptions.value = []
    deptOptions.value = []
    userOptions.value = []
  }
</script>
