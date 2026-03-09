<template>
  <ElDialog v-model="visible" title="菜单权限" width="500px" align-center @close="handleClose">
    <div v-loading="loading">
      <div class="mb-4 flex gap-2">
        <ElCheckbox v-model="isExpandAll" @change="handleExpandAll">全部展开/收起</ElCheckbox>
        <ElCheckbox v-model="isSelectAll" @change="handleSelectAll">全部选择/取消</ElCheckbox>
      </div>

      <ElScrollbar height="60vh">
        <ElTree
          ref="treeRef"
          :data="menuOptions"
          show-checkbox
          node-key="id"
          :props="defaultProps"
          empty-text="加载中，请稍候"
        />
      </ElScrollbar>
    </div>
    <template #footer>
      <ElButton @click="handleClose">取消</ElButton>
      <ElButton type="primary" :loading="submitLoading" @click="savePermission">保存</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { treeselect, roleMenuTreeselect } from '@/api/system/menu'
  import { updateRole, type SysRole } from '@/api/system/role'
  import type { ElTree } from 'element-plus'

  interface Props {
    modelValue: boolean
    roleData?: SysRole
  }

  interface Emits {
    (e: 'update:modelValue', value: boolean): void
    (e: 'success'): void
  }

  const props = withDefaults(defineProps<Props>(), {
    modelValue: false,
    roleData: undefined
  })

  const emit = defineEmits<Emits>()

  const visible = computed({
    get: () => props.modelValue,
    set: (value) => emit('update:modelValue', value)
  })

  const treeRef = ref<InstanceType<typeof ElTree>>()
  const loading = ref(false)
  const submitLoading = ref(false)
  const menuOptions = ref<any[]>([])
  const isExpandAll = ref(false)
  const isSelectAll = ref(false)

  const defaultProps = {
    children: 'children',
    label: 'label'
  }

  /**
   * 监听弹窗显示状态切换
   */
  watch(
    () => props.modelValue,
    async (newVal) => {
      // 弹窗打开且角色存在时加载数据
      if (newVal && props.roleData) {
        await loadPermissionData()
      }
    }
  )

  /**
   * 加载菜单树及角色的已勾选权限
   */
  const loadPermissionData = async () => {
    if (!props.roleData?.roleId) return

    loading.value = true
    try {
      // 调用若依标准的角色菜单权限接口
      const res = await roleMenuTreeselect(props.roleData.roleId)
      // 若依返回结构通常为 { menus: [...], checkedKeys: [...] }
      const { menus, checkedKeys } = (res as any) || {}

      // 更新菜单树选项
      menuOptions.value = menus || []

      // 等待 DOM 更新后设置勾选状态
      nextTick(() => {
        if (checkedKeys && treeRef.value) {
          checkedKeys.forEach((key: number) => {
            treeRef.value?.setChecked(key, true, false)
          })
        }
      })
    } catch (error) {
      console.error('加载权限数据失败:', error)
      // 如果报错，尝试加载基础菜单树作为降级方案
      const res = await treeselect()
      menuOptions.value = (res as any) || []
    } finally {
      loading.value = false
    }
  }

  const handleExpandAll = (val: any) => {
    const tree = treeRef.value
    if (!tree) return
    const nodes = (tree as any).store.nodesMap
    for (const i in nodes) {
      nodes[i].expanded = val
    }
  }

  const handleSelectAll = (val: any) => {
    treeRef.value?.setCheckedNodes(val ? menuOptions.value : [])
  }

  const handleClose = () => {
    visible.value = false
    menuOptions.value = []
    isExpandAll.value = false
    isSelectAll.value = false
  }

  /**
   * 保存权限配置
   */
  const savePermission = async () => {
    if (!props.roleData) return
    submitLoading.value = true
    try {
      // 获取目前被选中的节点 ID 数组
      const checkedKeys = treeRef.value?.getCheckedKeys()
      // 获取目前半选中的节点 ID 数组
      const halfCheckedKeys = treeRef.value?.getHalfCheckedKeys()

      const role: SysRole = {
        ...props.roleData,
        menuIds: [...(checkedKeys || []), ...(halfCheckedKeys || [])] as number[]
      }

      await updateRole(role)
      ElMessage.success('权限保存成功')
      emit('success')
      handleClose()
    } catch (error) {
      console.error('保存权限失败:', error)
    } finally {
      submitLoading.value = false
    }
  }
</script>
