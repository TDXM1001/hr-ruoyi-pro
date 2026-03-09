# 角色管理模块与若依后端对接设计文档

## 1. 概述
本文档旨在定义 `art-design-pro` 项目中角色管理模块（Role Management）与若依（RuoYi-Vue）后端的对接方案。采用渐进式重构（方案 A），在保留现有 UI 风格的基础上，实现数据结构与接口协议的完全兼容。

## 2. 架构设计
### 2.1 技术栈
- **前端**: Vue 3 (Composition API), TypeScript, Element Plus
- **后端**: RuoYi-Vue (Spring Boot, MyBatis)
- **数据流**: API -> Store/View -> UI Components

### 2.2 核心对接点
- **数据标准**: 统一字段命名为若依 `SysRole` 格式（如 `status` 代替 `enabled`）。
- **权限控制**: 引入 `roleKey`（权限字符）作为角色唯一标识。
- **关联逻辑**: 角色与菜单权限的绑定逻辑，从前端模拟转向调用后端 `roleMenuTreeselect` 接口。

## 3. 详细设计

### 3.1 数据模型 (Data Model)
更新接口类型定义，适配若依后端：

| 字段名 | 类型 | 说明 | 对接规则 |
| :--- | :--- | :--- | :--- |
| `roleId` | number | 角色ID | 保持不变 |
| `roleName` | string | 角色名称 | 保持不变 |
| `roleKey` | string | 权限字符 | **新增**，必填，如 `admin`, `common` |
| `roleSort` | number | 显示顺序 | **新增**，必填 |
| `status` | string | 角色状态 | **替换**，`0`:正常, `1`:停用 |
| `menuIds` | number[] | 菜单ID集合 | 仅在提交阶段使用 |

### 3.2 接口对接 (API Integration)
切换所有调用至 `src/api/system/role.ts`：
- `listRole`: 获取角色分页列表。
- `getRole`: 获取角色详细。
- `addRole`: 新增角色。
- `updateRole`: 修改角色。
- `delRole`: 删除角色。
- `changeRoleStatus`: 修改状态（暂作为备用，交互保持静态）。

### 3.3 组件重构 (Components)

#### 3.3.1 RoleSearch (搜索模块)
- **状态筛选**: 下拉框选项更新为 `[{label: '正常', value: '0'}, {label: '停用', value: '1'}]`。
- **参数适配**: 搜索时，若依后端期望 `beginTime` 和 `endTime` 放在 `params` 属性中，需在 `handleSearch` 中进行映射。

#### 3.3.2 RoleEditDialog (编辑弹窗)
- **表单扩展**: 增加“权限字符”（`roleKey`）和“显示顺序”（`roleSort`）组件。
- **校验增强**: 针对 `roleKey` 增加长度和必填校验。

#### 3.3.3 RolePermissionDialog (权限配置)
- **初始化**: 打开弹窗时，并行调用 `treeselect()` (获取菜单树) 和 `roleMenuTreeselect(roleId)` (获取角色已有权限)。
- **数据绑定**: 
    - `ElTree` 渲染全量菜单。
    - 使用接口返回的 `checkedKeys` 设置已选中状态。
- **提交**: 收集 `tree.getCheckedKeys()` 和 `tree.getHalfCheckedKeys()` 的并集作为 `menuIds` 提交。

### 3.4 列表展示 (ArtTable)
- **状态标签**: 修改 `formatter`，将 `status === '0'` 映射为 `success`，`status === '1'` 映射为 `danger`。
- **操作项**: 保持现有“菜单权限”、“编辑”、“删除”按钮。

## 4. 实施策略
1. 修改 API 定义与类型模型。
2. 重构搜索与角色列表主页面。
3. 重构角色新增/编辑逻辑。
4. 重构菜单权限分配逻辑。
5. 集成测试并验证若依后端权限生效。
