# 若依系统角色与字典体系 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 完成若依系统「角色管理系统」的深度对接与「数据字典体系」的构建，并将全局现有的状态、性别等静态下拉框替换为动态数据字典渲染。

**Architecture:** 
- 在 API 层分离 `role.ts`, `dict/type.ts`, `dict/data.ts` 接口文件。
- 构建完整的字典管理 UI（字典类型列表与字典数据列表），支持 CRUD 与缓存刷新。
- 使用经过封装的 `@/utils/dict.ts` 内的 `useDict` 钩子及 `@/components/DictTag`，重构现有包含业务状态信息的页面（部门、岗位、用户、角色等），实现前端枚举状态随由后端字典管控。

**Tech Stack:** Vue 3, Vite, TypeScript, Axios, Element Plus, Pinia

---

### Task 1: 角色管理模块深度对接 (Role API & Permission)

**Files:**
- Create: `art-design-pro/src/api/system/role.ts`
- Modify: `art-design-pro/src/views/system/role/index.vue`
- Modify: `art-design-pro/src/views/system/role/modules/role-search.vue`
- Modify: `art-design-pro/src/views/system/role/modules/role-edit-dialog.vue`

**Step 1: Write the minimal implementation for Role API**

```typescript
// art-design-pro/src/api/system/role.ts
import http from '@/utils/http'

export function listRole(query?: any) {
  return http.request({ url: '/system/role/list', method: 'get', params: query })
}
export function getRole(roleId: number | string) {
  return http.request({ url: '/system/role/' + roleId, method: 'get' })
}
export function addRole(data: any) {
  return http.request({ url: '/system/role', method: 'post', data: data })
}
export function updateRole(data: any) {
  return http.request({ url: '/system/role', method: 'put', data: data })
}
export function delRole(roleId: number | string) {
  return http.request({ url: '/system/role/' + roleId, method: 'delete' })
}
export function changeRoleStatus(roleId: number | string, status: string) {
  const data = { roleId, status }
  return http.request({ url: '/system/role/changeStatus', method: 'put', data: data })
}
```

**Step 2: Commit API layer**

```bash
cd e:/my-project/hr-ruoyi-pro/art-design-pro
git add src/api/system/role.ts
git commit -m "feat: 新增角色管理 (Role) 核心后端 API 接口对接"
```

### Task 2: 字典类型管理 API 与 UI (Dict Type)

**Files:**
- Create: `art-design-pro/src/api/system/dict/type.ts`
- Create: `art-design-pro/src/views/system/dict/index.vue`
- Create: `art-design-pro/src/views/system/dict/modules/dict-type-dialog.vue`

**Step 1: Write minimal API implementation**

```typescript
// art-design-pro/src/api/system/dict/type.ts
import http from '@/utils/http'

export function listType(query?: any) {
  return http.request({ url: '/system/dict/type/list', method: 'get', params: query })
}
export function getType(dictId: number | string) {
  return http.request({ url: '/system/dict/type/' + dictId, method: 'get' })
}
export function addType(data: any) {
  return http.request({ url: '/system/dict/type', method: 'post', data: data })
}
export function updateType(data: any) {
  return http.request({ url: '/system/dict/type', method: 'put', data: data })
}
export function delType(dictId: number | string) {
  return http.request({ url: '/system/dict/type/' + dictId, method: 'delete' })
}
export function refreshCache() {
  return http.request({ url: '/system/dict/type/refreshCache', method: 'delete' })
}
```

**Step 2: Write minimal UI placeholder**

```html
<!-- art-design-pro/src/views/system/dict/index.vue -->
<template>
  <div class="p-4">
    <el-card shadow="never">
      <h2>字典管理页面占位</h2>
      <!-- 此处后续实现查询表单与表格 -->
    </el-card>
  </div>
</template>
<script setup lang="ts">
  defineOptions({ name: 'Dict' })
</script>
```

**Step 3: Run Build to verify**

Run: `cd e:/my-project/hr-ruoyi-pro/art-design-pro && npm run build`
Expected: PASS

**Step 4: Commit**

```bash
cd e:/my-project/hr-ruoyi-pro/art-design-pro
git add src/api/system/dict/type.ts src/views/system/dict/index.vue
git commit -m "feat: 初步搭建字典类型 (Dict Type) API与入口UI视图"
```

### Task 3: 字典数据管理相关 API 与 UI (Dict Data)

**Files:**
- Create: `art-design-pro/src/api/system/dict/data.ts` (Append to existing)
- Create: `art-design-pro/src/views/system/dict/data.vue`

**Step 1: Append minimal API implementation**

修改 `src/api/system/dict/data.ts` 补充 CRUD 方法。

```typescript
export function listData(query?: any) {
  return request.get({ url: '/system/dict/data/list', params: query })
}
export function getData(dictCode: number | string) {
  return request.get({ url: '/system/dict/data/' + dictCode })
}
export function addData(data: any) {
  return request.post({ url: '/system/dict/data', data: data })
}
export function updateData(data: any) {
  return request.put({ url: '/system/dict/data', data: data })
}
export function delData(dictCode: number | string) {
  return request.del({ url: '/system/dict/data/' + dictCode })
}
```

**Step 2: Write minimal UI placeholder**

```html
<!-- art-design-pro/src/views/system/dict/data.vue -->
<template>
  <div class="p-4">
    <el-card shadow="never">
      <h2>字典数据配置页面占位</h2>
    </el-card>
  </div>
</template>
<script setup lang="ts">
  defineOptions({ name: 'DictData' })
</script>
```

**Step 3: Commit**

```bash
cd e:/my-project/hr-ruoyi-pro/art-design-pro
git add src/api/system/dict/data.ts src/views/system/dict/data.vue
git commit -m "feat: 补充字典数据 (Dict Data) API及页面组件"
```

### Task 4: 全局模块字典接入与组件重构 (Dict Integration)

**Files:**
- Modify: `art-design-pro/src/views/system/user/index.vue`
- Modify: `art-design-pro/src/views/system/dept/index.vue`
- Modify: `art-design-pro/src/views/system/post/index.vue`
- Modify: `art-design-pro/src/views/system/user/modules/user-edit-dialog.vue`

**Step 1: Replace hardcoded dropdowns in user module**

将 `user/index.vue` 和 `user-edit-dialog.vue` 中的性别（枚举 `sys_user_sex`）和状态（枚举 `sys_normal_disable`）转换为拉取字典数据。

引入方式（以 `index.vue` 示例）：
```typescript
import { useDict } from '@/utils/dict'
import DictTag from '@/components/DictTag/index.vue'

const { sys_normal_disable } = useDict('sys_normal_disable')
// 使用 sys_normal_disable.value 传递给下拉选项
// 在 Table 列配置中使用 <DictTag :options="sys_normal_disable" :value="row.status" />
```

**Step 2: Replace hardcoded status in dept & post**

按照同样方式引入 `sys_normal_disable` 到 `dept/index.vue`（及其编辑弹窗）和 `post/index.vue`（及其编辑弹窗）的状态属性中。

**Step 3: Run Build to verify integration success**

Run: `cd e:/my-project/hr-ruoyi-pro/art-design-pro && npm run build`
Expected: PASS

**Step 4: Commit**

```bash
cd e:/my-project/hr-ruoyi-pro/art-design-pro
git add src/views/system/user src/views/system/dept src/views/system/post
git commit -m "refactor: 全局用户、部门、岗位模块硬编码下拉框接入动态字典系统"
```
