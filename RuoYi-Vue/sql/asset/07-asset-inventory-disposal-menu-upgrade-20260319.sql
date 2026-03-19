-- ----------------------------
-- 固定资产一期盘点/处置/统计菜单增量脚本（2026-03-19）
-- 说明：
-- 1. 适用于已执行 00~06 的环境，增量补齐 Task 8 前端动态路由菜单与按钮权限。
-- 2. 本脚本可重复执行（update + insert not exists）。
-- ----------------------------

-- 2120：资产盘点菜单
update sys_menu
set parent_id = 2100,
    order_num = 8,
    path = 'inventory',
    component = 'asset/inventory/index',
    query = '',
    route_name = 'AssetInventory',
    menu_type = 'C',
    visible = '0',
    status = '0',
    perms = 'asset:inventory:list',
    icon = 'survey-line',
    remark = '固定资产盘点任务页面'
where menu_id = 2120;

insert into sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  2120, '资产盘点', 2100, 8, 'inventory', 'asset/inventory/index', '', 'AssetInventory',
  1, 0, 'C', '0', '0', 'asset:inventory:list', 'survey-line',
  'admin', sysdate(), '', null, '固定资产盘点任务页面'
from dual
where not exists (
  select 1 from sys_menu where menu_id = 2120
);

-- 2121：盘点任务新增按钮
update sys_menu
set parent_id = 2120,
    order_num = 1,
    path = '',
    component = '',
    query = '',
    route_name = '',
    menu_type = 'F',
    visible = '0',
    status = '0',
    perms = 'asset:inventory:add',
    icon = '#',
    remark = '盘点任务创建按钮权限'
where menu_id = 2121;

insert into sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  2121, '盘点任务新增', 2120, 1, '', '', '', '',
  1, 0, 'F', '0', '0', 'asset:inventory:add', '#',
  'admin', sysdate(), '', null, '盘点任务创建按钮权限'
from dual
where not exists (
  select 1 from sys_menu where menu_id = 2121
);

-- 2122：盘点结果登记按钮
update sys_menu
set parent_id = 2120,
    order_num = 2,
    path = '',
    component = '',
    query = '',
    route_name = '',
    menu_type = 'F',
    visible = '0',
    status = '0',
    perms = 'asset:inventory:edit',
    icon = '#',
    remark = '盘点结果登记按钮权限'
where menu_id = 2122;

insert into sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  2122, '盘点结果登记', 2120, 2, '', '', '', '',
  1, 0, 'F', '0', '0', 'asset:inventory:edit', '#',
  'admin', sysdate(), '', null, '盘点结果登记按钮权限'
from dual
where not exists (
  select 1 from sys_menu where menu_id = 2122
);

-- 2123：盘点任务查询按钮
update sys_menu
set parent_id = 2120,
    order_num = 3,
    path = '',
    component = '',
    query = '',
    route_name = '',
    menu_type = 'F',
    visible = '0',
    status = '0',
    perms = 'asset:inventory:query',
    icon = '#',
    remark = '盘点任务详情查询权限'
where menu_id = 2123;

insert into sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  2123, '盘点任务查询', 2120, 3, '', '', '', '',
  1, 0, 'F', '0', '0', 'asset:inventory:query', '#',
  'admin', sysdate(), '', null, '盘点任务详情查询权限'
from dual
where not exists (
  select 1 from sys_menu where menu_id = 2123
);

-- 2124：资产处置菜单
update sys_menu
set parent_id = 2100,
    order_num = 9,
    path = 'disposal',
    component = 'asset/disposal/index',
    query = '',
    route_name = 'AssetDisposal',
    menu_type = 'C',
    visible = '0',
    status = '0',
    perms = 'asset:disposal:list',
    icon = 'delete-bin-6-line',
    remark = '固定资产处置确认页面'
where menu_id = 2124;

insert into sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  2124, '资产处置', 2100, 9, 'disposal', 'asset/disposal/index', '', 'AssetDisposal',
  1, 0, 'C', '0', '0', 'asset:disposal:list', 'delete-bin-6-line',
  'admin', sysdate(), '', null, '固定资产处置确认页面'
from dual
where not exists (
  select 1 from sys_menu where menu_id = 2124
);

-- 2125：资产处置确认按钮
update sys_menu
set parent_id = 2124,
    order_num = 1,
    path = '',
    component = '',
    query = '',
    route_name = '',
    menu_type = 'F',
    visible = '0',
    status = '0',
    perms = 'asset:disposal:add',
    icon = '#',
    remark = '资产处置确认按钮权限'
where menu_id = 2125;

insert into sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  2125, '资产处置确认', 2124, 1, '', '', '', '',
  1, 0, 'F', '0', '0', 'asset:disposal:add', '#',
  'admin', sysdate(), '', null, '资产处置确认按钮权限'
from dual
where not exists (
  select 1 from sys_menu where menu_id = 2125
);

-- 2126：资产处置查询按钮
update sys_menu
set parent_id = 2124,
    order_num = 2,
    path = '',
    component = '',
    query = '',
    route_name = '',
    menu_type = 'F',
    visible = '0',
    status = '0',
    perms = 'asset:disposal:query',
    icon = '#',
    remark = '资产处置详情查询权限'
where menu_id = 2126;

insert into sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  2126, '资产处置查询', 2124, 2, '', '', '', '',
  1, 0, 'F', '0', '0', 'asset:disposal:query', '#',
  'admin', sysdate(), '', null, '资产处置详情查询权限'
from dual
where not exists (
  select 1 from sys_menu where menu_id = 2126
);

-- 2127：台账统计总览权限
update sys_menu
set parent_id = 2101,
    order_num = 6,
    path = '',
    component = '',
    query = '',
    route_name = '',
    menu_type = 'F',
    visible = '0',
    status = '0',
    perms = 'asset:stats:overview',
    icon = '#',
    remark = '资产台账统计卡片查询权限'
where menu_id = 2127;

insert into sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  2127, '台账统计总览', 2101, 6, '', '', '', '',
  1, 0, 'F', '0', '0', 'asset:stats:overview', '#',
  'admin', sysdate(), '', null, '资产台账统计卡片查询权限'
from dual
where not exists (
  select 1 from sys_menu where menu_id = 2127
);
