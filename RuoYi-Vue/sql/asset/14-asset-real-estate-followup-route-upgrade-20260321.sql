-- ----------------------------
-- 不动产详情壳后续页路由补丁（2026-03-21）
-- 说明：
-- 1. 纠正 inspection 页签语义为“巡检”，整改独立为单独页签。
-- 2. 新增巡检任务明细页、整改新增页、整改编辑页隐藏路由。
-- ----------------------------

-- 2136：修正为不动产档案巡检页签
update sys_menu
set menu_name = '不动产档案巡检页',
    remark = '不动产档案巡检隐藏路由'
where menu_id = 2136;

-- 2138：不动产档案整改页签（隐藏路由）
insert into sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  2138, '不动产档案整改页', 2100, 19, 'real-estate/detail/:assetId/rectification', 'asset/real-estate/detail/index', '', 'AssetRealEstateDetailRectification',
  1, 0, 'C', '1', '0', 'asset:realEstate:query', '#',
  'admin', sysdate(), '', null, '不动产档案整改隐藏路由'
from dual
where not exists (
  select 1 from sys_menu where menu_id = 2138 or route_name = 'AssetRealEstateDetailRectification'
);

-- 2139：不动产巡检任务明细页（隐藏路由）
insert into sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  2139, '不动产巡检任务明细页', 2100, 20, 'real-estate/detail/:assetId/inspection-task/:taskId', 'asset/real-estate/inspection-task/index', '', 'AssetRealEstateInspectionTaskDetail',
  1, 0, 'C', '1', '0', 'asset:realEstate:query', '#',
  'admin', sysdate(), '', null, '不动产巡检任务明细隐藏路由'
from dual
where not exists (
  select 1 from sys_menu where menu_id = 2139 or route_name = 'AssetRealEstateInspectionTaskDetail'
);

-- 2140：不动产整改单新增页（隐藏路由）
insert into sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  2140, '不动产整改单新增页', 2100, 21, 'real-estate/detail/:assetId/rectification/create', 'asset/real-estate/rectification/form/index', '', 'AssetRealEstateRectificationCreate',
  1, 0, 'C', '1', '0', 'asset:realEstate:edit', '#',
  'admin', sysdate(), '', null, '不动产整改单新增隐藏路由'
from dual
where not exists (
  select 1 from sys_menu where menu_id = 2140 or route_name = 'AssetRealEstateRectificationCreate'
);

-- 2141：不动产整改单编辑页（隐藏路由）
insert into sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  2141, '不动产整改单编辑页', 2100, 22, 'real-estate/detail/:assetId/rectification/edit/:rectificationId', 'asset/real-estate/rectification/form/index', '', 'AssetRealEstateRectificationEdit',
  1, 0, 'C', '1', '0', 'asset:realEstate:edit', '#',
  'admin', sysdate(), '', null, '不动产整改单编辑隐藏路由'
from dual
where not exists (
  select 1 from sys_menu where menu_id = 2141 or route_name = 'AssetRealEstateRectificationEdit'
);
