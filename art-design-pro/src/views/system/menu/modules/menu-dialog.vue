<template>
  <ElDialog
    :title="dialogTitle"
    :model-value="visible"
    @update:model-value="handleCancel"
    width="680px"
    align-center
    class="menu-dialog"
    @closed="handleClosed"
  >
    <ArtForm
      ref="formRef"
      v-model="form"
      :items="formItems"
      :rules="rules"
      :span="24"
      :gutter="20"
      label-width="100px"
      :show-reset="false"
      :show-submit="false"
    >
      <!-- 上级菜单插槽 -->
      <template #parentId>
        <ElTreeSelect
          v-model="form.parentId"
          :data="menuOptions"
          :props="{ value: 'menuId', label: 'menuName', children: 'children' }"
          value-key="menuId"
          placeholder="选择上级菜单"
          check-strictly
          :render-after-expand="false"
          style="width: 100%"
        />
      </template>

      <!-- 菜单类型插槽 -->
      <template #menuType>
        <ElRadioGroup v-model="form.menuType">
          <ElRadioButton value="M">目录</ElRadioButton>
          <ElRadioButton value="C">菜单</ElRadioButton>
          <ElRadioButton value="F">按钮</ElRadioButton>
        </ElRadioGroup>
      </template>

      <!-- 图标选择插槽 (暂时使用输入框，后续可扩展图标选择器) -->
      <template #icon>
        <ElInput v-model="form.icon" placeholder="点击选择图标">
          <template #prefix>
            <Icon v-if="form.icon" :icon="form.icon" />
            <ElIcon v-else><Search /></ElIcon>
          </template>
        </ElInput>
      </template>
    </ArtForm>

    <template #footer>
      <span class="dialog-footer">
        <ElButton @click="handleCancel">取 消</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">确 定</ElButton>
      </span>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { reactive, ref, computed, watch } from 'vue'
  import type { FormRules } from 'element-plus'
  import { ElMessage } from 'element-plus'
  import { Search } from '@element-plus/icons-vue'
  import ArtForm from '@/components/core/forms/art-form/index.vue'
  import type { FormItem } from '@/components/core/forms/art-form/index.vue'
  import { useDict } from '@/utils/dict'
  import { listMenu, addMenu, updateMenu } from '@/api/system/menu'
  import type { SysMenu } from '@/api/system/menu'
  import { Icon } from '@iconify/vue'

  /** 表单数据接口 */
  interface MenuFormData {
    menuId?: number
    parentId: number
    menuName: string
    icon: string
    menuType: 'M' | 'C' | 'F'
    orderNum: number
    isFrame: string
    path: string
    component: string
    perms: string
    query: string
    isCache: string
    visible: string
    status: string
  }

  interface Props {
    visible: boolean
    editData?: SysMenu | null
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'success'): void
  }

  const props = withDefaults(defineProps<Props>(), {
    visible: false,
    editData: null
  })

  const emit = defineEmits<Emits>()

  // 接入字典
  const { sys_show_hide, sys_normal_disable } = useDict('sys_show_hide', 'sys_normal_disable')

  const formRef = ref()
  const isEdit = ref(false)
  const submitLoading = ref(false)
  const menuOptions = ref<any[]>([])

  /** 初始表单数据 */
  const form = reactive<MenuFormData>({
    parentId: 0,
    menuName: '',
    icon: '',
    menuType: 'M',
    orderNum: 0,
    isFrame: '1', // 0是 1否
    path: '',
    component: '',
    perms: '',
    query: '',
    isCache: '0', // 0缓存 1不缓存
    visible: '0', // 0显示 1隐藏
    status: '0' // 0正常 1停用
  })

  /** 校验规则 */
  const rules = reactive<FormRules>({
    menuName: [{ required: true, message: '菜单名称不能为空', trigger: 'blur' }],
    orderNum: [{ required: true, message: '菜单顺序不能为空', trigger: 'blur' }],
    path: [{ required: true, message: '路由地址不能为空', trigger: 'blur' }]
  })

  /** 动态配置表单项 */
  const formItems = computed<FormItem[]>(() => {
    const items: FormItem[] = [
      { label: '上级菜单', key: 'parentId', span: 24 },
      { label: '菜单类型', key: 'menuType', span: 24 },
      {
        label: '菜单名称',
        key: 'menuName',
        type: 'input',
        props: { placeholder: '请输入菜单名称' },
        span: 24
      }
    ]

    // 目录和菜单需要图标
    if (form.menuType !== 'F') {
      items.push({ label: '菜单图标', key: 'icon', span: 24 })
    }

    items.push({
      label: '显示排序',
      key: 'orderNum',
      type: 'number',
      props: { min: 0, controlsPosition: 'right' },
      span: 12
    })

    // 目录和菜单特有字段
    if (form.menuType !== 'F') {
      items.push(
        {
          label: '是否外链',
          key: 'isFrame',
          type: 'select',
          props: {
            options: [
              { label: '是', value: '0' },
              { label: '否', value: '1' }
            ]
          },
          span: 12
        },
        {
          label: '路由地址',
          key: 'path',
          type: 'input',
          props: { placeholder: '请输入路由地址' },
          span: 12
        }
      )
    }

    // 菜单特有字段
    if (form.menuType === 'C') {
      items.push(
        {
          label: '组件路径',
          key: 'component',
          type: 'input',
          props: { placeholder: '请输入组件路径' },
          span: 12
        },
        {
          label: '权限标识',
          key: 'perms',
          type: 'input',
          props: { placeholder: '请输入权限标识' },
          span: 12
        },
        {
          label: '路由参数',
          key: 'query',
          type: 'input',
          props: { placeholder: '请输入路由参数' },
          span: 12
        },
        {
          label: '是否缓存',
          key: 'isCache',
          type: 'select',
          props: {
            options: [
              { label: '是', value: '0' },
              { label: '否', value: '1' }
            ]
          },
          span: 12
        }
      )
    }

    // 按钮特有字段
    if (form.menuType === 'F') {
      items.push({
        label: '权限标识',
        key: 'perms',
        type: 'input',
        props: { placeholder: '请输入权限标识' },
        span: 12
      })
    }

    // 公共状态字段 (目录和菜单)
    if (form.menuType !== 'F') {
      items.push(
        {
          label: '显示状态',
          key: 'visible',
          type: 'select',
          props: {
            options: sys_show_hide.value
          },
          span: 12
        },
        {
          label: '菜单状态',
          key: 'status',
          type: 'select',
          props: {
            options: sys_normal_disable.value
          },
          span: 12
        }
      )
    }

    return items
  })

  const dialogTitle = computed(() => (isEdit.value ? '修改菜单' : '添加菜单'))

  /** 查询菜单树 */
  const getTreeselect = async () => {
    try {
      const res = await listMenu()
      menuOptions.value = []
      const menu: any = { menuId: 0, menuName: '主类目', children: [] }
      menu.children = handleTree(res, 'menuId')
      menuOptions.value.push(menu)
    } catch (error) {
      console.error('获取菜单树失败', error)
    }
  }

  /** 构造树型结构 */
  const handleTree = (data: any[], id: string, parentId = 'parentId', children = 'children') => {
    const config = { id, parentId, children }
    const childrenListMap: any = {}
    const nodeIds: any = {}
    const tree = []

    for (const d of data) {
      const pId = d[config.parentId]
      if (childrenListMap[pId] == null) {
        childrenListMap[pId] = []
      }
      nodeIds[d[config.id]] = d
      childrenListMap[pId].push(d)
    }

    for (const d of data) {
      const pId = d[config.parentId]
      if (nodeIds[pId] == null) {
        tree.push(d)
      }
    }

    for (const t of tree) {
      adaptToChildrenList(t)
    }

    function adaptToChildrenList(o: any) {
      if (childrenListMap[o[config.id]] !== null) {
        o[config.children] = childrenListMap[o[config.id]]
      }
      if (o[config.children]) {
        for (const c of o[config.children]) {
          adaptToChildrenList(c)
        }
      }
    }
    return tree
  }

  /** 提交表单 */
  const handleSubmit = async () => {
    await formRef.value?.validate()
    submitLoading.value = true
    try {
      if (form.menuId !== undefined) {
        await updateMenu(form)
        ElMessage.success('修改成功')
      } else {
        await addMenu(form)
        ElMessage.success('新增成功')
      }
      emit('success')
      handleCancel()
    } catch (error) {
      console.error('提交失败', error)
    } finally {
      submitLoading.value = false
    }
  }

  /** 取消操作 */
  const handleCancel = () => {
    emit('update:visible', false)
  }

  /** 弹窗关闭清理 */
  const handleClosed = () => {
    Object.assign(form, {
      menuId: undefined,
      parentId: 0,
      menuName: '',
      icon: '',
      menuType: 'M',
      orderNum: 0,
      isFrame: '1',
      path: '',
      component: '',
      perms: '',
      query: '',
      isCache: '0',
      visible: '0',
      status: '0'
    })
    isEdit.value = false
  }

  /** 监听回显 */
  watch(
    () => props.visible,
    (val) => {
      if (val) {
        getTreeselect()
        if (props.editData) {
          isEdit.value = true
          Object.assign(form, props.editData)
          // 确保 ID 正确映射 (如果是新建子项，外部会传 parentId)
        }
      }
    }
  )
</script>

<style scoped lang="scss">
  .menu-dialog {
    :deep(.el-radio-group) {
      width: 100%;
    }
  }
</style>
