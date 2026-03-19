# 资产模块 SQL 执行说明

## 1. 脚本说明

- `00-asset-schema.sql`
  - 用途：全新环境建表（含 `drop/create`）。
- `01-asset-seed.sql`
  - 用途：初始化菜单、动态路由、字典、分类与联调样例数据。
- `02-asset-upgrade-20260318.sql`
  - 用途：老库增量补齐一期基础字段与索引。
- `03-asset-menu-upgrade-20260318.sql`
  - 用途：把早期资产台账菜单升级为页面版动态路由。
- `04-asset-handover-order-upgrade-20260318.sql`
  - 用途：把交接从旧单表升级为“主单 + 明细”模型。
- `05-asset-use-menu-upgrade-20260319.sql`
  - 用途：增量补齐资产使用主菜单与交接新增按钮权限。
- `06-asset-use-route-upgrade-20260319.sql`
  - 用途：增量补齐资产使用“新增页/详情页”隐藏路由（不展示菜单，仅用于页面跳转）。

## 2. 推荐执行口径

- 全新环境：`00 -> 01`
- 老库（之前只跑过旧版 `00`）：`02 -> 04 -> 01 -> 05 -> 06`
- 老库（之前跑过旧版 `00` 和旧版 `01`）：`02 -> 04 -> 01 -> 03 -> 05 -> 06`
- 有业务数据的库：不要重跑 `00`

## 3. 你当前场景如何执行

如果你之前已经执行过 `00` 到 `05`，本次只需要执行：

```sql
source RuoYi-Vue/sql/asset/06-asset-use-route-upgrade-20260319.sql;
```

如果你之前执行过 `00` 到 `04`，建议执行：

```sql
source RuoYi-Vue/sql/asset/01-asset-seed.sql;
source RuoYi-Vue/sql/asset/05-asset-use-menu-upgrade-20260319.sql;
source RuoYi-Vue/sql/asset/06-asset-use-route-upgrade-20260319.sql;
```

## 4. 执行后验证

```sql
select menu_id, menu_name, parent_id, menu_type, visible, path, component, perms
from sys_menu
where menu_id between 2100 and 2113
order by menu_id;
```

你至少应看到：

- `2110`：资产使用（可见菜单）
- `2111`：资产交接新增（按钮权限）
- `2112`：资产交接新增页（隐藏路由）
- `2113`：资产交接详情页（隐藏路由）

## 5. 注意事项

- 生产库执行前，建议先备份 `ast_asset_handover_order`、`ast_asset_handover_item` 与 `sys_menu` 相关数据。
- `06` 是纯增量脚本，可重复执行（`update + insert not exists`）。
