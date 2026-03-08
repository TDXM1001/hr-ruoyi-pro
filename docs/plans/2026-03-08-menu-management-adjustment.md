# 菜单管理调整实现计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 调整菜单管理页面，使其数据结构、功能和 UI 风格与若依框架保持一致。

**Architecture:** 采用若依后端的 `SysMenu` 字段设计，前端通过 `ArtForm` 和 `ArtTable` 封装组件实现响应式表单和树形表格展示，并确保权限标识（perms）与后端鉴权逻辑闭环。

**Tech Stack:** Vue 3, TypeScript, Element Plus, Art Design Pro 自研组件库。

---

### Task 1: 建立符合若依规范的菜单 API 与类型定义

**Files:**
- Create: `art-design-pro/src/api/system/menu.ts`
- Modify: `art-design-pro/src/api/system-manage.ts` (标记废弃或迁移)

**Step 1: 创建菜单专用 API 文件**

```typescript
import request from '@/utils/http'

/** 菜单实体定义 */
export interface SysMenu {
  menuId: number
  menuName: string
  parentId: number
  orderNum: number
  path: string
  component: string
  query?: string
  isFrame: string // 0 是 1 否
  isCache: string // 0 缓存 1 不缓存
  menuType: 'M' | 'C' | 'F'
  visible: string // 0 显示 1 隐藏
  status: string // 0 正常 1 停用
  perms?: string
  icon?: string
  children?: SysMenu[]
  createTime?: string
}

/** 查询菜单列表 */
export function listMenu(params?: any) {
  return request.get<SysMenu[]>({
    url: '/system/menu/list',
    params
  })
}

/** 查询菜单树结构 */
export function treeselect() {
  return request.get<any[]>({
    url: '/system/menu/treeselect'
  })
}

/** 获取子菜单ID */
export function getMenu(menuId: number) {
  return request.get<SysMenu>({
    url: `/system/menu/${menuId}`
  })
}

/** 新增菜单 */
export function addMenu(data: Partial<SysMenu>) {
  return request.post({
    url: '/system/menu',
    data
  })
}

/** 修改菜单 */
export function updateMenu(data: Partial<SysMenu>) {
  return request.put({
    url: '/system/menu',
    data
  })
}

/** 删除菜单 */
export function delMenu(menuId: number) {
  return request.delete({
    url: `/system/menu/${menuId}`
  })
}
```

**Step 2: 提交**

```bash
git add src/api/system/menu.ts
git commit -m "feat: 按照若依规范添加菜单管理 API 与类型定义"
```

---

### Task 2: 重构菜单编辑弹窗 (MenuDialog)

**Files:**
- Modify: `art-design-pro/src/views/system/menu/modules/menu-dialog.vue`

**Step 1: 更新表单响应式对象与类型定义**

```typescript
// 修改 MenuFormData 定义以匹配 SysMenu
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

const form = reactive<MenuFormData>({
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
```

**Step 2: 根据若依逻辑动态配置 `formItems`**
- 目录(M)：显示上级菜单、图标、名称、顺序、是否外链、路由地址、显示状态、菜单状态。
- 菜单(C)：增加组件路径、权限标识、路由参数、是否缓存。
- 按钮(F)：仅保留上级菜单、名称、顺序、权限标识。

**Step 3: 集成 `treeselect` 获取上级菜单树**

**Step 4: 提交**

```bash
git add src/views/system/menu/modules/menu-dialog.vue
git commit -m "refactor: 重构菜单编辑弹窗，支持 M/C/F 类型动态切换"
```

---

### Task 3: 更新菜单主页面表格与搜索

**Files:**
- Modify: `art-design-pro/src/views/system/menu/index.vue`

**Step 1: 更新 `initialSearchState` 为 `menuName` 和 `status`**

**Step 2: 重定义表格列 `columns`**
- 第一列：菜单名称（渲染图标 + 文本）
- 第二列：图标（单独列或合并）
- 第三列：排序 (orderNum)
- 第四列：权限标识 (perms)
- 第五列：组件路径 (component)
- 第六列：状态 (status，使用 DictTag 或若依风格 Badge)
- 第七列：创建时间

**Step 3: 处理操作列按钮**
- 新增（行内）：点击后自动填充当前行为 `parentId`
- 修改、删除

**Step 4: 移除前端自定义的 `convertAuthListToChildren` (若后端已返回完整树)**

**Step 5: 提交**

```bash
git add src/views/system/menu/index.vue
git commit -m "feat: 更新菜单管理主页面布局，优化表格列展示"
```

---

### Task 4: UI 细节打磨与图标集成

**Files:**
- Modify: `art-design-pro/src/views/system/menu/index.vue`
- Modify: `art-design-pro/src/views/system/menu/modules/menu-dialog.vue`

**Step 1: 渲染图标组件**
实现列表和表单中的图标显示。

**Step 2: 状态列样式对齐**
若依风格：0 正常 (Success), 1 停用 (Danger)。

**Step 3: 提交**

```bash
git add src/views/system/menu/
git commit -m "style: 对齐若依菜单管理 UI 细节"
```
