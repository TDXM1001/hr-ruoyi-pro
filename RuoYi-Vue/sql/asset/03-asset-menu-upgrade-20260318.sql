-- ----------------------------
-- 固定资产一期菜单升级脚本（2026-03-18）
-- 说明：
-- 1. 用于已经执行过旧版 01-asset-seed.sql 的环境。
-- 2. 将原“弹窗版按钮权限”升级为“页面版隐藏动态路由”。
-- 3. 保留原 menu_id，尽量兼容既有角色授权关系。
-- ----------------------------

-- 2102：资产台账详情
update sys_menu
set parent_id = 2100,
    order_num = 2,
    path = 'ledger/detail/:assetId',
    component = 'asset/ledger/detail/index',
    query = '',
    route_name = 'AssetLedgerDetail',
    menu_type = 'C',
    visible = '1',
    status = '0',
    perms = 'asset:ledger:query',
    icon = '#',
    remark = '资产台账详情隐藏路由'
where menu_id = 2102;

insert into sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  2102, '资产台账详情', 2100, 2, 'ledger/detail/:assetId', 'asset/ledger/detail/index', '', 'AssetLedgerDetail',
  1, 0, 'C', '1', '0', 'asset:ledger:query', '#',
  'admin', sysdate(), '', null, '资产台账详情隐藏路由'
from dual
where not exists (
  select 1 from sys_menu where menu_id = 2102
);

-- 2103：资产台账新增
update sys_menu
set parent_id = 2100,
    order_num = 3,
    path = 'ledger/create',
    component = 'asset/ledger/form/index',
    query = '',
    route_name = 'AssetLedgerCreate',
    menu_type = 'C',
    visible = '1',
    status = '0',
    perms = 'asset:ledger:add',
    icon = '#',
    remark = '资产台账新增隐藏路由'
where menu_id = 2103;

insert into sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  2103, '资产台账新增', 2100, 3, 'ledger/create', 'asset/ledger/form/index', '', 'AssetLedgerCreate',
  1, 0, 'C', '1', '0', 'asset:ledger:add', '#',
  'admin', sysdate(), '', null, '资产台账新增隐藏路由'
from dual
where not exists (
  select 1 from sys_menu where menu_id = 2103
);

-- 2104：资产台账编辑
update sys_menu
set parent_id = 2100,
    order_num = 4,
    path = 'ledger/edit/:assetId',
    component = 'asset/ledger/form/index',
    query = '',
    route_name = 'AssetLedgerEdit',
    menu_type = 'C',
    visible = '1',
    status = '0',
    perms = 'asset:ledger:edit',
    icon = '#',
    remark = '资产台账编辑隐藏路由'
where menu_id = 2104;

insert into sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  2104, '资产台账编辑', 2100, 4, 'ledger/edit/:assetId', 'asset/ledger/form/index', '', 'AssetLedgerEdit',
  1, 0, 'C', '1', '0', 'asset:ledger:edit', '#',
  'admin', sysdate(), '', null, '资产台账编辑隐藏路由'
from dual
where not exists (
  select 1 from sys_menu where menu_id = 2104
);

-- 2105：导出按钮保持为功能按钮，挂在列表页下
update sys_menu
set parent_id = 2101,
    order_num = 5,
    path = '',
    component = '',
    query = '',
    route_name = '',
    menu_type = 'F',
    visible = '0',
    status = '0',
    perms = 'asset:ledger:export',
    icon = '#',
    remark = '资产台账导出按钮'
where menu_id = 2105;
