<template>
  <ElCard shadow="never" class="asset-category-attr-card">
    <template #header>
      <div class="flex flex-wrap items-center justify-between gap-3">
        <div>
          <div class="text-base font-semibold">属性模板管理</div>
          <div class="text-xs text-[var(--art-gray-500)]">
            {{ categoryName ? `当前分类：${categoryName}` : '请先在左侧选择资产分类' }}
          </div>
        </div>

        <div class="flex items-center gap-2">
          <ElButton :disabled="!showAttrPanel" @click="loadAttrs" v-ripple>刷新</ElButton>
          <ElButton type="primary" :disabled="!showAttrPanel" @click="handleAdd" v-ripple>
            新增模板
          </ElButton>
        </div>
      </div>
    </template>

    <ElAlert
      title="这里维护的是分类字段模板，资产实例值请在资产台账“扩展信息”页签中填写。"
      type="info"
      :closable="false"
      class="mb-4"
    />

    <ElEmpty
      v-if="!showAttrPanel"
      description="请先选择一个资产分类后再维护属性模板"
      :image-size="88"
    />

    <ElTable
      v-else
      v-loading="loading"
      :data="attrList"
      border
      empty-text="当前分类暂无属性模板"
      row-key="attrId"
    >
      <ElTableColumn prop="attrName" label="字段名称" min-width="140" />
      <ElTableColumn prop="attrCode" label="字段编码" min-width="140" show-overflow-tooltip />
      <ElTableColumn prop="dataType" label="数据类型" width="100" align="center">
        <template #default="{ row }">
          {{ getDataTypeLabel(row.dataType) }}
        </template>
      </ElTableColumn>
      <ElTableColumn prop="isRequired" label="必填" width="90" align="center">
        <template #default="{ row }">
          <ElTag :type="row.isRequired === '1' ? 'danger' : 'info'" effect="plain">
            {{ row.isRequired === '1' ? '是' : '否' }}
          </ElTag>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="status" label="状态" width="90" align="center">
        <template #default="{ row }">
          <ElTag :type="buildAttrStatusTag(row.status).type" effect="light">
            {{ buildAttrStatusTag(row.status).label }}
          </ElTag>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="defaultValue" label="默认值" min-width="140" show-overflow-tooltip />
      <ElTableColumn label="操作" width="220" align="center" fixed="right">
        <template #default="{ row }">
          <div class="flex items-center justify-center gap-2">
            <ElButton type="primary" link @click="handleEdit(row)">编辑</ElButton>
            <ElButton
              type="warning"
              link
              :disabled="row.status === '1'"
              @click="handleDisable(row)"
            >
              停用
            </ElButton>
            <ElButton type="danger" link @click="handleDelete(row)">删除</ElButton>
          </div>
        </template>
      </ElTableColumn>
    </ElTable>

    <CategoryAttrEditDialog
      v-model="dialogVisible"
      :category-id="categoryId"
      :category-name="categoryName"
      :attr-data="currentAttr"
      @success="handleDialogSuccess"
    />
  </ElCard>
</template>

<script setup lang="ts">
  import { computed, ref, watch } from 'vue'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import {
    delCategoryAttr,
    disableCategoryAttr,
    getCategoryAttr,
    listCategoryAttrs
  } from '@/api/asset/category-attr'
  import type { AssetDynamicAttrDefinition } from '@/types/asset'
  import CategoryAttrEditDialog from './category-attr-edit-dialog.vue'
  import { buildAttrStatusTag, shouldShowAttrPanel } from '../category-attr.helper'

  const props = defineProps<{
    categoryId?: number
    categoryName?: string
  }>()

  const loading = ref(false)
  const dialogVisible = ref(false)
  const currentAttr = ref<AssetDynamicAttrDefinition>()
  const attrList = ref<AssetDynamicAttrDefinition[]>([])

  const dataTypeLabelMap: Record<string, string> = {
    text: '文本',
    number: '数值',
    date: '日期',
    json: 'JSON',
    select: '选项'
  }

  const showAttrPanel = computed(() => shouldShowAttrPanel(props.categoryId))

  /** 仅在分类已选中时拉取模板，避免把模板管理职责带回台账侧。 */
  async function loadAttrs() {
    if (!props.categoryId) {
      attrList.value = []
      return
    }

    loading.value = true
    try {
      const response: any = await listCategoryAttrs(props.categoryId)
      attrList.value = Array.isArray(response) ? response : response?.data || response?.rows || []
    } catch (error) {
      attrList.value = []
      console.error('获取分类属性模板失败:', error)
    } finally {
      loading.value = false
    }
  }

  watch(
    () => props.categoryId,
    () => {
      currentAttr.value = undefined
      dialogVisible.value = false
      loadAttrs()
    },
    { immediate: true }
  )

  const getDataTypeLabel = (value?: string) => {
    return dataTypeLabelMap[value || ''] || value || '文本'
  }

  const handleAdd = () => {
    currentAttr.value = undefined
    dialogVisible.value = true
  }

  const handleEdit = async (row: AssetDynamicAttrDefinition) => {
    if (!row.attrId) return
    loading.value = true
    try {
      const response: any = await getCategoryAttr(row.attrId)
      currentAttr.value = response?.data || response || row
      dialogVisible.value = true
    } catch (error) {
      console.error('获取属性模板详情失败:', error)
    } finally {
      loading.value = false
    }
  }

  const handleDisable = async (row: AssetDynamicAttrDefinition) => {
    if (!row.attrId) return
    try {
      await ElMessageBox.confirm(`确认停用字段“${row.attrName || row.attrCode}”吗？`, '提示', {
        type: 'warning'
      })
      await disableCategoryAttr(row.attrId)
      ElMessage.success('属性模板已停用')
      loadAttrs()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('停用属性模板失败:', error)
      }
    }
  }

  const handleDelete = async (row: AssetDynamicAttrDefinition) => {
    if (!row.attrId) return
    try {
      await ElMessageBox.confirm(
        `确认删除字段“${row.attrName || row.attrCode}”吗？删除后无法恢复。`,
        '提示',
        {
          type: 'warning'
        }
      )
      await delCategoryAttr([row.attrId])
      ElMessage.success('属性模板已删除')
      loadAttrs()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除属性模板失败:', error)
      }
    }
  }

  const handleDialogSuccess = () => {
    dialogVisible.value = false
    loadAttrs()
  }
</script>

<style lang="scss" scoped>
  .asset-category-attr-card {
    min-height: 100%;
  }
</style>
