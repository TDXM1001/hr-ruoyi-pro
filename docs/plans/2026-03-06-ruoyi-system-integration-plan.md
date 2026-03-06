# 接入若依核心系统模块 实现计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 将 art-design-pro 模板的底层请求、路由与全局字典机制替换为若依后端兼容规范，并完成核心基建部署。

**Architecture:** 改造基于 Axios 的 HTTP 封装以处理 401/200 响应及 Token 注入；补齐通用的系统树算法等基建：创建全局 Pinia Store 管理字典，引入字典查询 Hook 与字典标签展示组件。后续再进行用户和权限动态加载机制替换。

**Tech Stack:** Vue 3, Vite, TypeScript, Axios, Pinia, Vue Router, Element Plus, Tailwind CSS

---

### Task 1: 改造公共工具类 (Utils)

**Files:**
- Create: `art-design-pro/src/utils/ruoyi.ts`

**Step 1: 编写空的类型定义 (占位预跑)**
```typescript
// src/utils/ruoyi.ts
export function handleTree(data: any, id?: string, parentId?: string, children?: string) { return [] }
export function parseTime(time: any, pattern?: string) { return '' }
export function resetForm(refName: string) {}
```

**Step 2: 运行编译验证**
Run: `cd e:/my-project/hr-ruoyi-pro/art-design-pro && npm run build`
Expected: 构建成功，无语法错误报错。

**Step 3: 编写核心实现代码**
填入 `handleTree` 树形化核心算法、`parseTime` 时期格式化引擎以及表单重置 `resetForm`。

**Step 4: 重新运行编译**
Run: `cd e:/my-project/hr-ruoyi-pro/art-design-pro && npm run build`
Expected: 构建成功。

**Step 5: 提交代码**
```bash
cd e:/my-project/hr-ruoyi-pro/art-design-pro
git add src/utils/ruoyi.ts
git commit -m "feat: 添加 handleTree 等若依基建核心工具方法"
```

### Task 2: 全面改造 HTTP 请求引擎

**Files:**
- Modify: `art-design-pro/src/utils/http/index.ts`
- Modify: `art-design-pro/src/utils/http/error.ts` (按需)

**Step 1: 断言或编写拦截器框架**
将现有的业务状态码处理（如原框架默认的 `code === 0` 等）断开连接，准备抛出错误。

**Step 2: 验证接口破坏**
Run: `cd e:/my-project/hr-ruoyi-pro/art-design-pro && npm run build`
Expected: 可能会抛出对特定接口类型的不兼容。

**Step 3: 写入最新请求拦截与响应结构**
1. 请求拦截增加：`config.headers['Authorization'] = 'Bearer ' + token;` 
2. 响应拦截替换：判断 `res.data.code === 200` 为成功；判断 `code === 401` 调用退出登录 action 并弹窗；500 弹出 `ElMessage.error`。

**Step 4: 验证**
Run: `cd e:/my-project/hr-ruoyi-pro/art-design-pro && npm run build`
Expected: 构建成功，业务层不再处理异常数据。

**Step 5: 提交代码**
```bash
cd e:/my-project/hr-ruoyi-pro/art-design-pro
git add src/utils/http/index.ts
git commit -m "feat: 改造 HTTP 请求引擎适配若依 200/401/500 全局拦截约定"
```

### Task 3: 结合 Pinia 重构字典体系 (Store & Hook)

**Files:**
- Create: `art-design-pro/src/store/modules/dict.ts`
- Create: `art-design-pro/src/utils/dict.ts`

**Step 1: 建立空依赖引用**
创建空的 `useDictStore` 返回 `dict` 数组，以及空的 `export function useDict(...args: string[])`。

**Step 2: 测试编译**
Run: `cd e:/my-project/hr-ruoyi-pro/art-design-pro && npm run build`
Expected: 成功依赖构建。

**Step 3: 实现系统字典挂载与拉取**
1. 编写后端获取字典数据的接口 (例如：`/api/system/dict/data/type/:dictType`)。
2. 完善 `dict.ts` 中的 `useDictStore` 做内存管理。
3. 完善 `utils/dict.ts` 中的 `useDict` hook：如果有缓存直接返回，否则调用 store 中的 actions 拉取接口。

**Step 4: 验证是否通过 TS 检查**
Run: `cd e:/my-project/hr-ruoyi-pro/art-design-pro && npm run build`
Expected: 类型推断与异步数据不报语法错。

**Step 5: 提交代码**
```bash
cd e:/my-project/hr-ruoyi-pro/art-design-pro
git add src/store/modules/dict.ts src/utils/dict.ts
git commit -m "feat: 移植并重构若依数据字典 Pinia 状态树与数据获取 Hook"
```

### Task 4: 实现全局 `<DictTag>` 字典标签组件

**Files:**
- Create: `art-design-pro/src/components/DictTag/index.vue`
- Modify: `art-design-pro/src/main.ts` (如需全局注册)

**Step 1: 编写空的 DOM 结构作为存根**
`<template><span>DictTag</span></template>`

**Step 2: 编译测试组件**
Run: `cd e:/my-project/hr-ruoyi-pro/art-design-pro && npm run build`
Expected: 构建成功。

**Step 3: 实现 Element Plus 和 Tailwind 混合控制**
依据传入的 `options`（字典数据）和 `value` 解析对应的 `dictLabel` 及 `listClass` (样式类别属性)，配合 `<el-tag>` 加 Tailwind 色标展示对应的字典枚举状态。

**Step 4: 编译确认**
Run: `cd e:/my-project/hr-ruoyi-pro/art-design-pro && npm run build`
Expected: 成功。

**Step 5: 提交代码**
```bash
cd e:/my-project/hr-ruoyi-pro/art-design-pro
git add src/components/DictTag/index.vue src/main.ts
git commit -m "feat: 提供全局 DictTag 字典标签组件显示支持"
```

### 后续排期 (Next Phases)
本 Plan 着眼于系统基建。下一阶段 Plan 将覆盖：
- 用户身份信息及全局角色的拉取并落盘 (Auth)
- `Vue-Router` 路由表获取并转化为嵌套路由
- 以及编写 `v-hasPermi` 权限指令完成整体无缝对接。
