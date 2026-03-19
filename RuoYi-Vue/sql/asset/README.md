# 资产模块 SQL 执行说明

## 1. 文件说明

当前 `RuoYi-Vue/sql/asset` 目录下主要脚本如下：

- `00-asset-schema.sql`
  - 用途：全新环境首次建表。
  - 特点：包含 `drop table if exists` 和 `create table`，不适合在已有业务数据的库里重复执行。
- `01-asset-seed.sql`
  - 用途：补齐菜单、动态路由、字典、分类和联调样例数据。
- `02-asset-upgrade-20260318.sql`
  - 用途：老库补齐一期基础字段和索引。
  - 范围：资产台账 `acquire_type`、老版交接表补字段、盘点补字段、处置补字段。
- `03-asset-menu-upgrade-20260318.sql`
  - 用途：把早期弹窗式资产台账菜单升级为页面版动态路由菜单。
- `04-asset-handover-order-upgrade-20260318.sql`
  - 用途：把资产交接从“单表单资产”升级为“主单 + 明细”模型。
  - 特点：会保留旧 `ast_asset_handover` 表，并把旧数据迁移到新主单/明细表。
- `05-asset-use-menu-upgrade-20260319.sql`
  - 用途：增量补齐“资产使用”动态路由菜单与交接新增按钮权限（Task 6 前端对接）。

## 2. 推荐执行口径

- 全新环境：
  - 执行顺序：`00 -> 01`
- 老库，之前只执行过旧版 `00`：
  - 执行顺序：`02 -> 04 -> 01 -> 05`
- 老库，之前执行过旧版 `00` 和旧版 `01`：
  - 执行顺序：`02 -> 04 -> 01 -> 03 -> 05`
- 已有业务数据的库：
  - 不要重跑 `00`

## 3. 你当前场景怎么执行

如果你之前已经执行过 `00-asset-schema.sql`，现在要接入新的交接主单/明细后端，执行顺序建议是：

```sql
source RuoYi-Vue/sql/asset/02-asset-upgrade-20260318.sql;
source RuoYi-Vue/sql/asset/04-asset-handover-order-upgrade-20260318.sql;
source RuoYi-Vue/sql/asset/01-asset-seed.sql;
source RuoYi-Vue/sql/asset/05-asset-use-menu-upgrade-20260319.sql;
```

如果你之前连旧版菜单也跑过，并且菜单还是老的弹窗模式，再补执行：

```sql
source RuoYi-Vue/sql/asset/03-asset-menu-upgrade-20260318.sql;
source RuoYi-Vue/sql/asset/05-asset-use-menu-upgrade-20260319.sql;
```

## 4. `04` 脚本做了什么

- 新增 `ast_asset_handover_order`
- 新增 `ast_asset_handover_item`
- 保留旧 `ast_asset_handover`
- 若旧表有数据，则按“一条旧交接记录 = 一张主单 + 一条明细”迁移

说明：

- 旧表没有 `before_status / after_status` 快照字段，所以迁移时会按交接类型推导：
  - `ASSIGN`：`IN_LEDGER -> IN_USE`
  - `TRANSFER`：`IN_USE -> IN_USE`
  - `RETURN`：`IN_USE -> IDLE`
- 这是历史数据兼容方案，不影响新单据按新模型完整留痕。

## 5. 执行后验证

### 5.1 验证新表

```sql
show tables like 'ast_asset_handover%';

show columns from ast_asset_handover_order;
show columns from ast_asset_handover_item;
```

至少应看到：

- `ast_asset_handover_order.handover_order_id`
- `ast_asset_handover_order.asset_count`
- `ast_asset_handover_item.handover_item_id`
- `ast_asset_handover_item.before_status`
- `ast_asset_handover_item.after_status`

### 5.2 验证旧数据是否迁移

```sql
select handover_no, handover_type, asset_count
from ast_asset_handover_order
order by handover_order_id desc
limit 20;

select handover_order_id, asset_id, asset_code, before_status, after_status
from ast_asset_handover_item
order by handover_item_id desc
limit 20;
```

### 5.3 验证台账与交接菜单

```sql
select menu_id, menu_name, parent_id, menu_type, visible, path, component, perms
from sys_menu
where menu_id between 2100 and 2111
order by menu_id;
```

## 6. 注意事项

- `04` 是新增增量脚本，目的是避免你已经执行过的 `02` 被反复改写。
- `00` 可以保持为最新全量建库脚本，供新环境一次性初始化使用。
- 生产库执行前，建议先备份 `ast_asset_handover`、`ast_asset_handover_order`、`ast_asset_handover_item` 三张表相关数据。
