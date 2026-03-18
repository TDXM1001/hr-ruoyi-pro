# 资产模块 SQL 执行说明

## 1. 文件说明

当前 `RuoYi-Vue/sql/asset` 目录下共有 4 个脚本：

- `00-asset-schema.sql`
  - 用途：全新环境首次建表。
  - 特点：包含 `drop table if exists` 和 `create table`，不能在已有业务数据的库上重复执行。
- `01-asset-seed.sql`
  - 用途：补齐菜单、动态路由、字典、分类和联调用样例数据。
  - 特点：适用于结构已经满足当前版本要求的环境。
- `02-asset-upgrade-20260318.sql`
  - 用途：把执行过旧版 `00` 的数据库补到当前结构。
  - 特点：只做增量字段和索引升级，不删表、不清历史数据。
- `03-asset-menu-upgrade-20260318.sql`
  - 用途：把执行过旧版 `01` 的菜单结构，从“弹窗版按钮权限”升级为“页面版隐藏动态路由”。
  - 特点：保留原菜单 ID，尽量兼容既有角色授权关系。

## 2. 执行原则

- 新库初始化：执行顺序为 `00 -> 01`
- 旧库结构升级：执行顺序为 `02 -> 01`
- 旧库且已经跑过早期 `01`：执行顺序为 `02 -> 01 -> 03`
- 已有业务数据的库，不能重复执行 `00`

## 3. 全新环境初始化

适用条件：

- 当前数据库还没有资产模块相关表
- 或者这是一个新的开发库 / 测试库

执行顺序：

```sql
source RuoYi-Vue/sql/asset/00-asset-schema.sql;
source RuoYi-Vue/sql/asset/01-asset-seed.sql;
```

说明：

- `00` 负责建表
- `01` 负责补齐动态路由菜单、字典、分类和样例台账数据

## 4. 旧库结构升级

适用条件：

- 之前已经执行过较早版本的 `00-asset-schema.sql`
- 当前库里已经存在资产模块表结构
- 不希望重建表，也不希望影响已有资产数据

执行顺序：

```sql
source RuoYi-Vue/sql/asset/02-asset-upgrade-20260318.sql;
source RuoYi-Vue/sql/asset/01-asset-seed.sql;
```

说明：

- `02` 补当前版本需要的字段和索引
- `01` 补前后端联调需要的菜单、字典和模拟数据

## 5. 已跑过旧版 seed 的菜单升级

适用条件：

- 你已经执行过旧版 `01-asset-seed.sql`
- 当前资产台账菜单还是“列表页 + 按钮弹窗”的老结构
- 现在要切换到“列表页 + 新增/编辑/详情隐藏动态路由”的页面版结构

执行顺序：

```sql
source RuoYi-Vue/sql/asset/03-asset-menu-upgrade-20260318.sql;
```

升级内容：

- `2102` 从查询按钮升级为“资产台账详情”隐藏路由
- `2103` 从新增按钮升级为“资产台账新增”隐藏路由
- `2104` 从编辑按钮升级为“资产台账编辑”隐藏路由
- `2105` 保持为导出按钮，继续挂在列表页下

## 6. 当前菜单结构

执行完最新脚本后，菜单树应为：

- `资产管理`：目录，显示
- `资产台账`：页面菜单，显示，组件 `asset/ledger/index`
- `资产台账详情`：页面菜单，隐藏，组件 `asset/ledger/detail/index`
- `资产台账新增`：页面菜单，隐藏，组件 `asset/ledger/form/index`
- `资产台账编辑`：页面菜单，隐藏，组件 `asset/ledger/form/index`
- `资产台账导出`：功能按钮

说明：

- 新增 / 编辑 / 详情 都走后端动态路由
- 隐藏路由不会出现在左侧菜单栏
- 但会参与动态路由注册和权限控制

## 7. 联调数据说明

执行 `01-asset-seed.sql` 后，会补齐以下内容：

- 菜单目录：`资产管理`
- 台账页面与隐藏动态路由
- 权限标识：
  - `asset:ledger:list`
  - `asset:ledger:query`
  - `asset:ledger:add`
  - `asset:ledger:edit`
  - `asset:ledger:export`
- 字典类型：
  - `ast_asset_status`
  - `ast_asset_source_type`
  - `ast_asset_acquire_type`
- 样例分类：
  - `FIXED_ELECTRONIC`
  - `FIXED_OFFICE`
  - `FIXED_MEETING`
- 样例资产：
  - `FA-2026-0001`
  - `FA-2026-0002`
  - `FA-2026-0003`

## 8. 执行后验证

### 8.1 验证结构升级

```sql
show columns from ast_asset_ledger;
show columns from ast_asset_handover;
show columns from ast_asset_inventory_item;
show columns from ast_asset_disposal;
```

重点确认以下字段存在：

- `ast_asset_ledger.acquire_type`
- `ast_asset_handover.from_location_name`
- `ast_asset_handover.confirm_by`
- `ast_asset_handover.confirm_time`
- `ast_asset_inventory_item.follow_up_action`
- `ast_asset_inventory_item.process_status`
- `ast_asset_inventory_item.process_time`
- `ast_asset_inventory_item.follow_up_biz_id`
- `ast_asset_disposal.confirmed_by`
- `ast_asset_disposal.confirmed_time`
- `ast_asset_disposal.finance_confirm_by`
- `ast_asset_disposal.finance_confirm_time`

### 8.2 验证菜单和动态路由

```sql
select menu_id, menu_name, parent_id, menu_type, visible, path, component, perms
from sys_menu
where menu_id between 2100 and 2105
order by menu_id;
```

重点确认：

- `2101` 为显示菜单，组件是 `asset/ledger/index`
- `2102/2103/2104` 为隐藏菜单，`visible = '1'`
- `2102` 组件是 `asset/ledger/detail/index`
- `2103/2104` 组件是 `asset/ledger/form/index`
- `2105` 仍然是导出按钮

### 8.3 验证字典数据

```sql
select dict_type, dict_name
from sys_dict_type
where dict_type in ('ast_asset_status', 'ast_asset_source_type', 'ast_asset_acquire_type');

select dict_type, dict_label, dict_value
from sys_dict_data
where dict_type in ('ast_asset_status', 'ast_asset_source_type', 'ast_asset_acquire_type')
order by dict_type, dict_sort;
```

### 8.4 验证样例资产

```sql
select asset_id, asset_code, asset_name, asset_status, source_type, acquire_type
from ast_asset_ledger
where asset_code in ('FA-2026-0001', 'FA-2026-0002', 'FA-2026-0003')
order by asset_id;
```

## 9. 前后端联调检查点

1. 用管理员账号重新登录系统，刷新权限缓存。
2. 确认左侧菜单出现“资产管理 / 资产台账”。
3. 打开 `/asset/ledger`，确认列表可以请求 `/asset/ledger/list`。
4. 点击“新增资产”，确认会进入 `/asset/ledger/create` 页面。
5. 点击“详情 / 编辑”，确认会进入隐藏动态路由页面，而不是弹窗。
6. 确认分类、部门、责任人都已经是选择器，不再手填 ID。

## 10. 推荐执行口径

- 新环境：执行 `00`，再执行 `01`
- 老环境但没跑过旧 seed：执行 `02`，再执行 `01`
- 老环境且已经跑过旧 seed：执行 `02`，再执行 `01`，最后执行 `03`
