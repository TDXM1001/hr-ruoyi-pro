-- ----------------------------
-- 不动产档案详情二级页签子路由补齐脚本（2026-03-21）
-- 说明：
-- 1. 适用于已执行 00~11 脚本的环境，增量补齐不动产详情页的占用信息、巡检整改、处置关联子路由。
-- 2. 三个子路由继续复用不动产详情组件，由前端根据路径切换页签内容，减少后续整改返工。
-- 3. 路由保持隐藏，仅用于详情页内跳转。
-- ----------------------------

-- 2135：不动产档案占用信息页签（隐藏路由）
update sys_menu
set parent_id = 2100,
    order_num = 16,
    path = 'real-estate/detail/:assetId/occupancy',
    component = 'asset/real-estate/detail/index',
    query = '',
    route_name = 'AssetRealEstateDetailOccupancy',
    menu_type = 'C',
    visible = '1',
    status = '0',
    perms = 'asset:realEstate:query',
    icon = '#',
    remark = '不动产档案占用信息隐藏路由'
where menu_id = 2135;

insert into sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  2135, '不动产档案占用信息页', 2100, 16, 'real-estate/detail/:assetId/occupancy', 'asset/real-estate/detail/index', '', 'AssetRealEstateDetailOccupancy',
  1, 0, 'C', '1', '0', 'asset:realEstate:query', '#',
  'admin', sysdate(), '', null, '不动产档案占用信息隐藏路由'
from dual
where not exists (
  select 1 from sys_menu where menu_id = 2135 or route_name = 'AssetRealEstateDetailOccupancy'
);

-- 2136：不动产档案巡检整改页签（隐藏路由）
update sys_menu
set parent_id = 2100,
    order_num = 17,
    path = 'real-estate/detail/:assetId/inspection',
    component = 'asset/real-estate/detail/index',
    query = '',
    route_name = 'AssetRealEstateDetailInspection',
    menu_type = 'C',
    visible = '1',
    status = '0',
    perms = 'asset:realEstate:query',
    icon = '#',
    remark = '不动产档案巡检整改隐藏路由'
where menu_id = 2136;

insert into sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  2136, '不动产档案巡检整改页', 2100, 17, 'real-estate/detail/:assetId/inspection', 'asset/real-estate/detail/index', '', 'AssetRealEstateDetailInspection',
  1, 0, 'C', '1', '0', 'asset:realEstate:query', '#',
  'admin', sysdate(), '', null, '不动产档案巡检整改隐藏路由'
from dual
where not exists (
  select 1 from sys_menu where menu_id = 2136 or route_name = 'AssetRealEstateDetailInspection'
);

-- 2137：不动产档案处置关联页签（隐藏路由）
update sys_menu
set parent_id = 2100,
    order_num = 18,
    path = 'real-estate/detail/:assetId/disposal',
    component = 'asset/real-estate/detail/index',
    query = '',
    route_name = 'AssetRealEstateDetailDisposal',
    menu_type = 'C',
    visible = '1',
    status = '0',
    perms = 'asset:realEstate:query',
    icon = '#',
    remark = '不动产档案处置关联隐藏路由'
where menu_id = 2137;

insert into sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  2137, '不动产档案处置关联页', 2100, 18, 'real-estate/detail/:assetId/disposal', 'asset/real-estate/detail/index', '', 'AssetRealEstateDetailDisposal',
  1, 0, 'C', '1', '0', 'asset:realEstate:query', '#',
  'admin', sysdate(), '', null, '不动产档案处置关联隐藏路由'
from dual
where not exists (
  select 1 from sys_menu where menu_id = 2137 or route_name = 'AssetRealEstateDetailDisposal'
);
