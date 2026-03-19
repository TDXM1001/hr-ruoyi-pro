-- ----------------------------
-- 固定资产一期资产使用菜单升级脚本（2026-03-19）
-- 说明：
-- 1. 用于已经执行过 00~04 脚本的环境，增量补齐 Task 6 前端动态路由菜单。
-- 2. 新增“资产使用”页面菜单及“资产交接新增”按钮权限。
-- ----------------------------

-- 2110：资产使用页面
update sys_menu
set parent_id = 2100,
    order_num = 5,
    path = 'use',
    component = 'asset/use/index',
    query = '',
    route_name = 'AssetUse',
    menu_type = 'C',
    visible = '0',
    status = '0',
    perms = 'asset:handover:list',
    icon = 'repeat-2-line',
    remark = '资产领用/调拨/退还页面'
where menu_id = 2110;

insert into sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  2110, '资产使用', 2100, 5, 'use', 'asset/use/index', '', 'AssetUse',
  1, 0, 'C', '0', '0', 'asset:handover:list', 'repeat-2-line',
  'admin', sysdate(), '', null, '资产领用/调拨/退还页面'
from dual
where not exists (
  select 1 from sys_menu where menu_id = 2110
);

-- 2111：资产交接新增按钮
update sys_menu
set parent_id = 2110,
    order_num = 1,
    path = '',
    component = '',
    query = '',
    route_name = '',
    menu_type = 'F',
    visible = '0',
    status = '0',
    perms = 'asset:handover:add',
    icon = '#',
    remark = '资产交接新增按钮'
where menu_id = 2111;

insert into sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  2111, '资产交接新增', 2110, 1, '', '', '', '',
  1, 0, 'F', '0', '0', 'asset:handover:add', '#',
  'admin', sysdate(), '', null, '资产交接新增按钮'
from dual
where not exists (
  select 1 from sys_menu where menu_id = 2111
);

