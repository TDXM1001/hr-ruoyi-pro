# 资产模块 SQL 执行说明

## 1. 脚本清单

- `00-asset-schema.sql`
  - 用途：全量建库建表（含 `drop/create`，仅用于全新环境）。
- `01-asset-seed.sql`
  - 用途：全量初始化菜单、权限、字典、分类与联调样例数据。
- `02-asset-upgrade-20260318.sql`
  - 用途：老库增量补齐一期基础字段与索引。
- `03-asset-menu-upgrade-20260318.sql`
  - 用途：老库菜单结构升级（早期菜单修正）。
- `04-asset-handover-order-upgrade-20260318.sql`
  - 用途：交接单从旧单表升级为“主单 + 明细”模型。
- `05-asset-use-menu-upgrade-20260319.sql`
  - 用途：增量补齐“资产使用”菜单与交接新增按钮权限。
- `06-asset-use-route-upgrade-20260319.sql`
  - 用途：增量补齐“资产交接新增页/详情页”隐藏路由。
- `07-asset-inventory-disposal-menu-upgrade-20260319.sql`
  - 用途：增量补齐“资产盘点/资产处置”菜单、按钮权限与台账统计权限。
- `08-asset-inventory-route-upgrade-20260319.sql`
  - 用途：增量补齐“盘点任务发起页/执行页”隐藏动态路由。

## 2. 推荐执行口径

- 全新环境：`00 -> 01`
- 老库且已跑过早期 `00`：`02 -> 04 -> 01 -> 03 -> 05 -> 06 -> 07 -> 08`
- 老库且已跑过 `00~06`：执行 `07 -> 08`
- 老库且已跑过 `00~07`：只需执行 `08`
- 已有业务数据的库：不要重跑 `00`

## 3. 你当前场景如何执行

如果你之前 **0-7 都已经执行过**，本次只需执行：

```sql
source RuoYi-Vue/sql/asset/08-asset-inventory-route-upgrade-20260319.sql;
```

如果你之前只执行到 `06`，建议执行：

```sql
source RuoYi-Vue/sql/asset/07-asset-inventory-disposal-menu-upgrade-20260319.sql;
source RuoYi-Vue/sql/asset/08-asset-inventory-route-upgrade-20260319.sql;
```

## 4. 执行后校验

```sql
select menu_id, menu_name, parent_id, menu_type, visible, path, component, perms
from sys_menu
where menu_id between 2100 and 2129
order by menu_id;
```

至少应看到：

- `2110`：资产使用（可见菜单）
- `2112`：资产交接新增页（隐藏路由）
- `2113`：资产交接详情页（隐藏路由）
- `2120`：资产盘点（可见菜单）
- `2128`：盘点任务发起页（隐藏路由）
- `2129`：盘点任务执行页（隐藏路由）
- `2124`：资产处置（可见菜单）
- `2127`：台账统计总览（按钮权限）

## 5. 注意事项

- 生产库执行前建议先备份 `sys_menu` 与资产模块核心业务表。
- `05/06/07/08` 都是纯增量脚本，支持重复执行（`update + insert not exists`）。
