-- ----------------------------
-- 不动产档案四页路由与权限补齐脚本（2026-03-21）
-- 说明：
-- 1. 适用于已执行 00~10 脚本的环境，增量补齐不动产档案详情/新建/编辑隐藏路由。
-- 2. 列表菜单保持可见，详情/新建/编辑路由保持隐藏，仅用于页面跳转。
-- 3. 脚本保持幂等，可重复执行。
-- ----------------------------

-- 2132：不动产档案详情页（隐藏路由）
update sys_menu
set parent_id = 2100,
    order_num = 13,
    path = 'real-estate/detail/:assetId',
    component = 'asset/real-estate/detail/index',
    query = '',
    route_name = 'AssetRealEstateDetail',
    menu_type = 'C',
    visible = '1',
    status = '0',
    perms = 'asset:realEstate:query',
    icon = '#',
    remark = '不动产档案详情隐藏路由'
where menu_id = 2132;

insert into sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  2132, '不动产档案详情页', 2100, 13, 'real-estate/detail/:assetId', 'asset/real-estate/detail/index', '', 'AssetRealEstateDetail',
  1, 0, 'C', '1', '0', 'asset:realEstate:query', '#',
  'admin', sysdate(), '', null, '不动产档案详情隐藏路由'
from dual
where not exists (
  select 1 from sys_menu where menu_id = 2132 or route_name = 'AssetRealEstateDetail'
);

-- 2133：不动产档案新建页（隐藏路由）
update sys_menu
set parent_id = 2100,
    order_num = 14,
    path = 'real-estate/create',
    component = 'asset/real-estate/form/index',
    query = '',
    route_name = 'AssetRealEstateCreate',
    menu_type = 'C',
    visible = '1',
    status = '0',
    perms = 'asset:realEstate:add',
    icon = '#',
    remark = '不动产档案新建隐藏路由'
where menu_id = 2133;

insert into sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  2133, '不动产档案新建页', 2100, 14, 'real-estate/create', 'asset/real-estate/form/index', '', 'AssetRealEstateCreate',
  1, 0, 'C', '1', '0', 'asset:realEstate:add', '#',
  'admin', sysdate(), '', null, '不动产档案新建隐藏路由'
from dual
where not exists (
  select 1 from sys_menu where menu_id = 2133 or route_name = 'AssetRealEstateCreate'
);

-- 2134：不动产档案编辑页（隐藏路由）
update sys_menu
set parent_id = 2100,
    order_num = 15,
    path = 'real-estate/edit/:assetId',
    component = 'asset/real-estate/form/index',
    query = '',
    route_name = 'AssetRealEstateEdit',
    menu_type = 'C',
    visible = '1',
    status = '0',
    perms = 'asset:realEstate:edit',
    icon = '#',
    remark = '不动产档案编辑隐藏路由'
where menu_id = 2134;

insert into sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  2134, '不动产档案编辑页', 2100, 15, 'real-estate/edit/:assetId', 'asset/real-estate/form/index', '', 'AssetRealEstateEdit',
  1, 0, 'C', '1', '0', 'asset:realEstate:edit', '#',
  'admin', sysdate(), '', null, '不动产档案编辑隐藏路由'
from dual
where not exists (
  select 1 from sys_menu where menu_id = 2134 or route_name = 'AssetRealEstateEdit'
);