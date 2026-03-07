<template>
  <div
    class="dept-tree-container relative h-full transition-all duration-300 flex-shrink-0"
    :style="{
      width: isCollapse ? '0px' : width + 'px',
      marginRight: isCollapse ? '0px' : '15px'
    }"
  >
    <!-- 内部卡片容器 -->
    <div
      class="art-table-card !m-0 h-full flex flex-col overflow-hidden border border-solid border-[var(--art-gray-200)]"
      :style="{
        opacity: isCollapse ? 0 : 1,
        visibility: isCollapse ? 'hidden' : 'visible',
        transition: 'opacity 0.2s'
      }"
    >
      <!-- 头部 -->
      <div
        class="aside-header flex-cb px-4 py-3 border-b border-solid border-[var(--art-gray-200)] whitespace-nowrap"
      >
        <span class="text-[15px] font-bold text-g-700">组织机构</span>
        <ArtSvgIcon icon="ri:organization-chart" class="text-g-500 text-lg" />
      </div>

      <!-- 搜索 -->
      <div class="px-4 py-3">
        <ElInput
          v-model="filterText"
          placeholder="搜索部门"
          prefix-icon="Search"
          clearable
          class="aside-search w-full"
        />
      </div>

      <!-- 树结构 - 使用 flex-1 确保填满剩余空间，独立滚动 -->
      <div class="flex-1 overflow-hidden px-2 pb-4">
        <ElScrollbar class="h-full">
          <ElTree
            ref="treeRef"
            :data="data"
            :props="defaultProps"
            highlight-current
            node-key="id"
            default-expand-all
            :expand-on-click-node="false"
            :filter-node-method="filterNode"
            @node-click="handleNodeClick"
            empty-text="暂无数据"
          />
        </ElScrollbar>
      </div>
    </div>

    <!-- 折叠按钮 (独立于组件内容，确保始终可见) -->
    <div
      class="collapse-handle flex-cc cursor-pointer absolute shadow-md border border-solid border-[var(--art-gray-200)]"
      :class="isCollapse ? 'handle-collapsed' : 'handle-expanded'"
      @click="toggleCollapse"
    >
      <ArtSvgIcon
        :icon="isCollapse ? 'ri:arrow-right-s-line' : 'ri:arrow-left-s-line'"
        class="text-lg"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref, watch } from 'vue'

  interface TreeData {
    id: number | string
    label: string
    children?: TreeData[]
  }

  interface Props {
    data: TreeData[]
    width?: number
    currentNodeKey?: number | string | null
  }

  const props = withDefaults(defineProps<Props>(), {
    width: 200,
    currentNodeKey: null
  })

  const emit = defineEmits(['node-click', 'update:currentNodeKey'])

  const filterText = ref('')
  const isCollapse = ref(false)
  const treeRef = ref()

  const defaultProps = {
    children: 'children',
    label: 'label'
  }

  const toggleCollapse = () => {
    isCollapse.value = !isCollapse.value
  }

  watch(filterText, (val) => {
    treeRef.value?.filter(val)
  })

  // 监听 currentNodeKey 变化，同步到树的选中状态
  watch(
    () => props.currentNodeKey,
    (val) => {
      if (val !== null && val !== undefined && treeRef.value) {
        treeRef.value.setCurrentKey(val)
      }
    }
  )

  const filterNode = (value: string, data: any) => {
    if (!value) return true
    return data.label.includes(value)
  }

  const handleNodeClick = (data: any) => {
    // 先设置树的选中状态
    treeRef.value?.setCurrentKey(data.id)
    // 再发射事件
    emit('update:currentNodeKey', data.id)
    emit('node-click', data)
  }
</script>

<style lang="scss" scoped>
  .dept-tree-container {
    background-color: transparent;

    @media (max-width: 768px) {
      display: none;
    }

    .aside-search {
      :deep(.el-input__wrapper) {
        background-color: var(--art-gray-200) !important;
        box-shadow: none !important;
        border: 1px solid transparent;
        border-radius: 8px;

        &.is-focus {
          border-color: var(--theme-color);
          background-color: var(--default-box-color) !important;
        }
      }
    }

    :deep(.el-tree) {
      background: transparent;
      .el-tree-node__content {
        height: 38px;
        border-radius: 6px;
        &:hover {
          background-color: var(--art-gray-200) !important;
        }
      }
      .el-tree-node.is-current > .el-tree-node__content {
        background-color: var(--theme-color-light) !important;
        color: var(--theme-color);
      }
    }

    // 核心：折叠按钮的设计
    .collapse-handle {
      top: 50%;
      transform: translateY(-50%);
      width: 22px;
      height: 50px;
      background-color: var(--default-box-color);
      border-radius: 0 10px 10px 0;
      z-index: 100;
      color: var(--art-gray-500);
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      border-left: none;

      &:hover {
        color: var(--theme-color);
        background-color: var(--art-gray-100);
      }

      &.handle-expanded {
        right: -23px;
      }

      &.handle-collapsed {
        left: -8px; // 稍微露个头在边缘
        border-left: 1px solid var(--art-gray-200);
        border-radius: 6px;
      }
    }
  }

  [data-theme='dark'] {
    .dept-tree-container {
      .aside-search :deep(.el-input__wrapper) {
        background-color: rgba(255, 255, 255, 0.05) !important;
      }
      .collapse-handle {
        background-color: #1a1a1a;
        border-color: #333;
      }
    }
  }
</style>
