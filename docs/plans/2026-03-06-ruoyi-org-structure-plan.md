# 基础组织架构模块 (Org Structure) Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 实现若依系统核心「基础组织架构」包括「部门管理」、「岗位管理」和「用户管理」这三个大模块的页面展示与接口对接工作。

**Architecture:** 
- 在 API 层采用单一职责原则分离 `dept.ts`、`post.ts`、`user.ts`。
- 部门管理采用 `Element Plus` 的树形表格数据绑定结合 `handleTree` 辅助工具。
- 岗位管理采用基础的分页列表展示机制。
- 所有的主结构引入 `Tailwind CSS` 进行表单的外边距、内外间距等微调。

**Tech Stack:** Vue 3, Vite, TypeScript, Axios, Element Plus, Tailwind CSS

---

### Task 1: 部门管理 API 与数据模型 (Dept API)

**Files:**
- Create: `art-design-pro/src/api/system/dept.ts`
- Create: `art-design-pro/tests/api/dept.test.ts`

**Step 1: Write the failing test**

```typescript
import { listDept } from '../../src/api/system/dept'

describe('Dept API', () => {
  it('should expose listDept method', () => {
    expect(typeof listDept).toBe('function')
  })
})
```

**Step 2: Run test to verify it fails**

Run: `cd e:/my-project/hr-ruoyi-pro/art-design-pro && npx vitest run tests/api/dept.test.ts`
Expected: FAIL, 文件未找到或无对应导出模块。

**Step 3: Write minimal implementation**

```typescript
// art-design-pro/src/api/system/dept.ts
import http from '@/utils/http'

export function listDept(query?: any) {
  return http.request({ url: '/system/dept/list', method: 'get', params: query })
}
export function listDeptExcludeChild(deptId: number | string) {
  return http.request({ url: '/system/dept/list/exclude/' + deptId, method: 'get' })
}
export function getDept(deptId: number | string) {
  return http.request({ url: '/system/dept/' + deptId, method: 'get' })
}
export function addDept(data: any) {
  return http.request({ url: '/system/dept', method: 'post', data: data })
}
export function updateDept(data: any) {
  return http.request({ url: '/system/dept', method: 'put', data: data })
}
export function delDept(deptId: number | string) {
  return http.request({ url: '/system/dept/' + deptId, method: 'delete' })
}
```

**Step 4: Run test to verify it passes**

Run: `cd e:/my-project/hr-ruoyi-pro/art-design-pro && npx vitest run tests/api/dept.test.ts`
Expected: PASS, 暴露接口函数。

**Step 5: Commit**

```bash
cd e:/my-project/hr-ruoyi-pro/art-design-pro
git add src/api/system/dept.ts tests/api/dept.test.ts
git commit -m "feat: 实现部门管理 (Dept) 的全套后端 API 调用接口"
```

### Task 2: 部门管理查询页面 (Dept List UI)

**Files:**
- Create: `art-design-pro/src/views/system/dept/index.vue`

**Step 1: Write the failing test**

暂无专门的 Vue DOM 单元测试，采用编译检查进行兜底。

**Step 2: Run test to verify it fails**

跳过，转为直接实现。

**Step 3: Write minimal implementation**

```html
<!-- art-design-pro/src/views/system/dept/index.vue -->
<template>
  <div class="app-container p-5">
    <!-- 搜索表单 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" class="mb-4">
      <el-form-item label="部门名称" prop="deptName">
        <el-input v-model="queryParams.deptName" placeholder="请输入部门名称" clearable @keyup.enter="getList" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="部门状态" clearable>
          <el-option label="正常" value="0" />
          <el-option label="停用" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="getList">搜索</el-button>
      </el-form-item>
    </el-form>

    <!-- 列表数据 -->
    <el-table
      v-loading="loading"
      :data="deptList"
      row-key="deptId"
      default-expand-all
      :tree-props="{ children: 'children' }"
    >
      <el-table-column prop="deptName" label="部门名称"></el-table-column>
      <el-table-column prop="orderNum" label="排序" width="200"></el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'info'">{{ scope.row.status === '0' ? '正常' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200">
        <template #default="scope">
          <el-button link type="primary" icon="Edit">修改</el-button>
          <el-button link type="primary" icon="Plus">新增</el-button>
          <el-button link type="danger" icon="Delete">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { listDept } from '@/api/system/dept'
import { handleTree } from '@/utils/ruoyi'

const deptList = ref<any[]>([])
const loading = ref(false)
const queryParams = reactive({ deptName: undefined, status: undefined })

function getList() {
  loading.value = true
  listDept(queryParams).then((response: any) => {
    deptList.value = handleTree(response.data, "deptId")
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

onMounted(() => {
  getList()
})
</script>
```

**Step 4: Run test to verify it passes**

Run: `cd e:/my-project/hr-ruoyi-pro/art-design-pro && npm run build`
Expected: 编译通过，不报错。

**Step 5: Commit**

```bash
cd e:/my-project/hr-ruoyi-pro/art-design-pro
git add src/views/system/dept/index.vue
git commit -m "feat: 实现部门管理的树形列表展示功能与查询"
```

### Task 3: 岗位管理 API 与数据模型 (Post API)

**Files:**
- Create: `art-design-pro/src/api/system/post.ts`
- Create: `art-design-pro/tests/api/post.test.ts`

**Step 1: Write the failing test**

```typescript
import { listPost } from '../../src/api/system/post'

describe('Post API', () => {
  it('should expose listPost method', () => {
    expect(typeof listPost).toBe('function')
  })
})
```

**Step 2: Run test to verify it fails**

Run: `cd e:/my-project/hr-ruoyi-pro/art-design-pro && npx vitest run tests/api/post.test.ts`
Expected: FAIL.

**Step 3: Write minimal implementation**

```typescript
// art-design-pro/src/api/system/post.ts
import http from '@/utils/http'

export function listPost(query?: any) {
  return http.request({ url: '/system/post/list', method: 'get', params: query })
}
export function getPost(postId: number | string) {
  return http.request({ url: '/system/post/' + postId, method: 'get' })
}
export function addPost(data: any) {
  return http.request({ url: '/system/post', method: 'post', data: data })
}
export function updatePost(data: any) {
  return http.request({ url: '/system/post', method: 'put', data: data })
}
export function delPost(postId: number | string) {
  return http.request({ url: '/system/post/' + postId, method: 'delete' })
}
```

**Step 4: Run test to verify it passes**

Run: `cd e:/my-project/hr-ruoyi-pro/art-design-pro && npx vitest run tests/api/post.test.ts`
Expected: PASS.

**Step 5: Commit**

```bash
cd e:/my-project/hr-ruoyi-pro/art-design-pro
git add src/api/system/post.ts tests/api/post.test.ts
git commit -m "feat: 实现岗位管理 (Post) 的全套后端 API 调用接口"
```

### Task 4: 用户管理 API 定义 (User API)

**Files:**
- Create: `art-design-pro/src/api/system/user.ts`
- Create: `art-design-pro/tests/api/user.test.ts`

**Step 1: Write the failing test**

```typescript
import { listUser } from '../../src/api/system/user'

describe('User API', () => {
  it('should expose listUser method', () => {
    expect(typeof listUser).toBe('function')
  })
})
```

**Step 2: Run test to verify it fails**

Run: `cd e:/my-project/hr-ruoyi-pro/art-design-pro && npx vitest run tests/api/user.test.ts`
Expected: FAIL.

**Step 3: Write minimal implementation**

```typescript
// art-design-pro/src/api/system/user.ts
import http from '@/utils/http'

export function listUser(query?: any) {
  return http.request({ url: '/system/user/list', method: 'get', params: query })
}
export function getUser(userId: number | string) {
  return http.request({ url: '/system/user/' + userId, method: 'get' })
}
export function addUser(data: any) {
  return http.request({ url: '/system/user', method: 'post', data: data })
}
export function updateUser(data: any) {
  return http.request({ url: '/system/user', method: 'put', data: data })
}
export function delUser(userId: number | string) {
  return http.request({ url: '/system/user/' + userId, method: 'delete' })
}
export function deptTreeSelect() {
  return http.request({ url: '/system/user/deptTree', method: 'get' })
}
```

**Step 4: Run test to verify it passes**

Run: `cd e:/my-project/hr-ruoyi-pro/art-design-pro && npx vitest run tests/api/user.test.ts`
Expected: PASS.

**Step 5: Commit**

```bash
cd e:/my-project/hr-ruoyi-pro/art-design-pro
git add src/api/system/user.ts tests/api/user.test.ts
git commit -m "feat: 实现账号用户管理 (User) 后端 API 获取接口"
```
