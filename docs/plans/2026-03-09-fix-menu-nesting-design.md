# 设计文档 - 修复三级菜单重复渲染问题

## 1. 背景描述
在当前项目中，当用户访问三级菜单（例如：系统管理 -> 日志管理 -> 登录日志）时，页面会在内容区多渲染出一层侧边栏和顶栏组件，导致布局混乱。

## 2. 问题分析
- **核心触发点**：后端的“目录”类型菜单对应的组件名称为 `ParentView`。
- **错误映射**：在 `src/utils/router.ts` 的 `generateRoutes` 转换逻辑中，`ParentView` 被指向了 `src/views/index/index.vue`（即主布局 Layout）。
- **结果**：二级菜单项被当成了一个完整的主页来渲染，从而在主页内部嵌套了一个完整的主页。

## 3. 设计方案
### 3.1 创建通用容器组件
在 `src/views/common/` 路径下创建 `ParentView.vue`。
```vue
<template>
  <router-view />
</template>
```

### 3.2 修正路由生成逻辑
修改 `src/utils/router.ts` 中的 `generateRoutes` 函数：
- 将 `currentRoute.component === 'ParentView'` 的值从 `'/index/index'` 修改为 `'/common/ParentView'`。

## 4. 验证标准
- 访问三级菜单时，页面应当只在主框架的内容区域渲染业务组件。
- 侧边栏层级应当保持正常（一级展开显示二级，二级展开显示三级）。
- 二级目录不应当包含任何 UI 边框。

## 5. 任务列表
1. 创建目录 `src/views/common`。
2. 创建文件 `src/views/common/ParentView.vue`。
3. 修改 `src/utils/router.ts` 中的映射逻辑。
