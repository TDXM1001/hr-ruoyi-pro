-- ----------------------------
-- 不动产整改完成页与完成字段补丁（2026-03-21）
-- 说明：
-- 1. 将“完成整改”从普通整改单编辑页拆出，独立承载完成说明与验收备注。
-- 2. 为整改单补齐 completion_desc、acceptance_remark 字段，沉淀整改完成事实。
-- 3. 新增整改完成页隐藏路由，保持详情壳内页签与独立业务页的边界清晰。
-- ----------------------------

set @completion_desc_exists = (
  select count(*)
  from information_schema.columns
  where table_schema = database()
    and table_name = 'ast_asset_rectification_order'
    and column_name = 'completion_desc'
);
set @completion_desc_sql = if(
  @completion_desc_exists > 0,
  'select 1',
  'alter table ast_asset_rectification_order add column completion_desc varchar(500) default null comment ''完成说明'' after completed_time'
);
prepare stmt from @completion_desc_sql;
execute stmt;
deallocate prepare stmt;

set @acceptance_remark_exists = (
  select count(*)
  from information_schema.columns
  where table_schema = database()
    and table_name = 'ast_asset_rectification_order'
    and column_name = 'acceptance_remark'
);
set @acceptance_remark_sql = if(
  @acceptance_remark_exists > 0,
  'select 1',
  'alter table ast_asset_rectification_order add column acceptance_remark varchar(500) default null comment ''验收备注'' after completion_desc'
);
prepare stmt from @acceptance_remark_sql;
execute stmt;
deallocate prepare stmt;

insert into sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  2142, '不动产整改完成页', 2100, 23, 'real-estate/detail/:assetId/rectification/complete/:rectificationId',
  'asset/real-estate/rectification/complete/index', '', 'AssetRealEstateRectificationComplete',
  1, 0, 'C', '1', '0', 'asset:realEstate:edit', '#',
  'admin', sysdate(), '', null, '不动产整改完成隐藏路由'
from dual
where not exists (
  select 1 from sys_menu where menu_id = 2142 or route_name = 'AssetRealEstateRectificationComplete'
);
