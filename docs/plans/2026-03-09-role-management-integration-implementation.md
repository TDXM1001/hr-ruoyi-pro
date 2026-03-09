# 角色管理模块与若依后端对接实施计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 实现 `art-design-pro` 中的角色管理模块与若依（RuoYi-Vue）后端的完整对接，涵盖增删改查及菜单权限分配。

**Architecture:** 采用渐进式重构，将前端数据模型与若依后端 `SysRole` 实体对齐，切换 API 调用至标准系统接口，并在现有 UI 框架下适配权限分配逻辑。

**Tech Stack:** Vue 3, TypeScript, Element Plus, RuoYi-Vue Backend.

---

### Task 1: 规范化 API 与类型定义

**Files:**
- Modify: `e:\my-project\hr-ruoyi-pro\art-design-pro\src\api\system\role.ts`
- Modify: `e:\my-project\hr-ruoyi-pro\art-design-pro\src\views\system\role\index.vue` (更新类型引用)

**Step 1: 更新角色相关接口定义**
在 `src/api/system/role.ts` 中完善 `SysRole` 接口定义，并确保请求参数与返回类型正确。

```typescript
// e:\my-project\hr-ruoyi-pro\art-design-pro\src\api\system\role.ts

export interface SysRole {
  roleId?: number
  roleName: string
  roleKey: string
  roleSort: number
  status: string // '0' 正常, '1' 停用
  remark?: string
  menuIds?: number[]
  createTime?: string
  [key: string]: any
}

// 修改 listRole 接口
export function listRole(query?: any) {
  return request.get<{ rows: SysRole[]; total: number }>({ 
    url: '/system/role/list', 
    params: query 
  })
}
```

**Step 2: 验证 API 连通性（伪代码/检查工具）**
检查 `art-design-pro` 的代理配置是否正确指向若依后端。

**Step 3: 提交**
```bash
git add src/api/system/role.ts
git commit -m "feat: 规范化角色管理 API 及类型定义"
```

---

### Task 2: 重构搜索模块 (RoleSearch)

**Files:**
- Modify: `e:\my-project\hr-ruoyi-pro\art-design-pro\src\views\system\role\modules\role-search.vue`

**Step 1: 修改状态选项与搜索字段**
将 `enabled` 替换为 `status`，并将布尔值改为若依标准的 `"0"`/`"1"`。

**Step 2: 实施代码变更**
```vue
// 修改 statusOptions
const statusOptions = ref([
  { label: '正常', value: '0' },
  { label: '停用', value: '1' }
])

// formItems 中 key 改为 status
{
  label: '角色状态',
  key: 'status',
  type: 'select',
  // ...
}
```

**Step 3: 提交**
```bash
git add src/views/system/role/modules/role-search.vue
git commit -m "refactor: 重构角色搜索模块，适配若依状态字段"
```

---

### Task 3: 重构角色列表主页面 (index.vue)

**Files:**
- Modify: `e:\my-project\hr-ruoyi-pro\art-design-pro\src\views\system\role\index.vue`

**Step 1: 切换 API 调用**
将 `fetchGetRoleList` 替换为 `listRole`，并处理分页返回数据的差异。

**Step 2: 更新表格列配置**
更新 `columns` 定义，使用 `status` 字段并调整 `formatter`。

```typescript
// columns 逻辑
{
  prop: 'status',
  label: '角色状态',
  width: 100,
  formatter: (row) => {
    const isNormal = row.status === '0'
    return h(ElTag, { type: isNormal ? 'success' : 'danger' }, () => isNormal ? '正常' : '停用')
  }
}
```

**Step 3: 适配日期查询参数**
在 `handleSearch` 中将 `daterange` 解构并填充到若依所需的 `params[beginTime]` 和 `params[endTime]`。

**Step 4: 提交**
```bash
git add src/views/system/role/index.vue
git commit -m "refactor: 重构角色管理主列表，实现若依后端对接"
```

---

### Task 4: 重构角色编辑弹窗 (RoleEditDialog)

**Files:**
- Modify: `e:\my-project\hr-ruoyi-pro\art-design-pro\src\views\system\role\modules\role-edit-dialog.vue`

**Step 1: 增加角色权限字符与顺序字段**
在模板中添加 `roleKey` 和 `roleSort` 的表单项。

**Step 2: 更新表单初始化与提交逻辑**
确保提交时包含新增字段，并调用 `addRole`/`updateRole` 接口。

**Step 3: 提交**
```bash
git add src/views/system/role/modules/role-edit-dialog.vue
git commit -m "feat: 完善角色编辑弹窗，增加权限字符与排序字段"
```

---

### Task 5: 重构菜单权限分配 (RolePermissionDialog)

**Files:**
- Modify: `e:\my-project\hr-ruoyi-pro\art-design-pro\src\views\system\role\modules\role-permission-dialog.vue`

**Step 1: 集成若依权限接口**
引入 `@/api/system/menu` 中的 `treeselect` 和 `roleMenuTreeselect`。

**Step 2: 实现权限数据加载与勾选逻辑**
- 打开弹窗时，调用上述两接口。
- 将 `treeselect` 的结果赋给 `processedMenuList`。
- 将 `roleMenuTreeselect` 返回的 `checkedKeys` 通过 `treeRef.value.setCheckedKeys` 应用。

**Step 3: 更新保存逻辑**
调用 `updateRole` 传递最新的 `menuIds`。

**Step 4: 提交**
```bash
git add src/views/system/role/modules/role-permission-dialog.vue
git commit -m "feat: 实现角色菜单权限分配功能，对接若依核心权限逻辑"
```
