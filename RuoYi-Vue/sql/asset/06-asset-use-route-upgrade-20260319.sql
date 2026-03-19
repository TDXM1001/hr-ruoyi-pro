-- ----------------------------
-- 固定资产一期资产使用隐藏路由增量脚本（2026-03-19）
-- 说明：
-- 1. 用于已经执行过 00~05 脚本的环境，增量补齐资产使用“新增页/详情页”动态路由。
-- 2. 路由设置为隐藏（visible=1），不出现在菜单栏，仅用于页面跳转。
-- ----------------------------

-- 2112：资产交接新增页（隐藏路由）
update sys_menu
set parent_id = 2100,
    order_num = 6,
    path = 'use/create',
    component = 'asset/use/form/index',
    query = '',
    route_name = 'AssetUseCreate',
    menu_type = 'C',
    visible = '1',
    status = '0',
    perms = 'asset:handover:add',
    icon = '#',
    remark = '资产交接新增隐藏路由'
where menu_id = 2112;

insert into sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  2112, '资产交接新增页', 2100, 6, 'use/create', 'asset/use/form/index', '', 'AssetUseCreate',
  1, 0, 'C', '1', '0', 'asset:handover:add', '#',
  'admin', sysdate(), '', null, '资产交接新增隐藏路由'
from dual
where not exists (
  select 1 from sys_menu where menu_id = 2112
);

-- 2113：资产交接详情页（隐藏路由）
update sys_menu
set parent_id = 2100,
    order_num = 7,
    path = 'use/detail/:handoverOrderId',
    component = 'asset/use/detail/index',
    query = '',
    route_name = 'AssetUseDetail',
    menu_type = 'C',
    visible = '1',
    status = '0',
    perms = 'asset:handover:list',
    icon = '#',
    remark = '资产交接详情隐藏路由'
where menu_id = 2113;

insert into sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  2113, '资产交接详情页', 2100, 7, 'use/detail/:handoverOrderId', 'asset/use/detail/index', '', 'AssetUseDetail',
  1, 0, 'C', '1', '0', 'asset:handover:list', '#',
  'admin', sysdate(), '', null, '资产交接详情隐藏路由'
from dual
where not exists (
  select 1 from sys_menu where menu_id = 2113
);
