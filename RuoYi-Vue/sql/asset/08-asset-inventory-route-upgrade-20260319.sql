-- ----------------------------
-- 固定资产一期盘点隐藏路由增量脚本（2026-03-19）
-- 说明：
-- 1. 用于已执行 00~07 的环境，增量补齐盘点“发起页/执行页”动态路由。
-- 2. 路由设置为隐藏（visible=1），不出现在菜单栏，仅用于业务页面跳转。
-- ----------------------------

-- 2128：盘点任务发起页（隐藏路由）
update sys_menu
set parent_id = 2100,
    order_num = 10,
    path = 'inventory/create',
    component = 'asset/inventory/create/index',
    query = '',
    route_name = 'AssetInventoryCreate',
    menu_type = 'C',
    visible = '1',
    status = '0',
    perms = 'asset:inventory:add',
    icon = '#',
    remark = '资产盘点发起页隐藏路由'
where menu_id = 2128;

insert into sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  2128, '盘点任务发起页', 2100, 10, 'inventory/create', 'asset/inventory/create/index', '', 'AssetInventoryCreate',
  1, 0, 'C', '1', '0', 'asset:inventory:add', '#',
  'admin', sysdate(), '', null, '资产盘点发起页隐藏路由'
from dual
where not exists (
  select 1 from sys_menu where menu_id = 2128
);

-- 2129：盘点任务执行页（隐藏路由）
update sys_menu
set parent_id = 2100,
    order_num = 11,
    path = 'inventory/task/:taskId',
    component = 'asset/inventory/task/index',
    query = '',
    route_name = 'AssetInventoryTaskExecute',
    menu_type = 'C',
    visible = '1',
    status = '0',
    perms = 'asset:inventory:list',
    icon = '#',
    remark = '资产盘点执行页隐藏路由'
where menu_id = 2129;

insert into sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  2129, '盘点任务执行页', 2100, 11, 'inventory/task/:taskId', 'asset/inventory/task/index', '', 'AssetInventoryTaskExecute',
  1, 0, 'C', '1', '0', 'asset:inventory:list', '#',
  'admin', sysdate(), '', null, '资产盘点执行页隐藏路由'
from dual
where not exists (
  select 1 from sys_menu where menu_id = 2129
);

