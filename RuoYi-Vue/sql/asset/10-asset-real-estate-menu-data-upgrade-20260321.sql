-- ----------------------------
-- 不动产档案菜单与点测样例数据增量脚本（2026-03-21）
-- 说明：
-- 1. 适用于已执行 00~09 脚本的环境，增量补齐不动产档案入口、详情权限与点测样例数据。
-- 2. 脚本保持幂等，可重复执行。
-- 3. 目标是让资产管理员能够从“资产管理”菜单直接进入不动产权属档案查询页。
-- ----------------------------

-- 2130：不动产档案菜单
update sys_menu
set parent_id = 2100,
    order_num = 12,
    path = 'real-estate',
    component = 'asset/real-estate/index',
    query = '',
    route_name = 'AssetRealEstate',
    menu_type = 'C',
    visible = '0',
    status = '0',
    perms = 'asset:realEstate:list',
    icon = 'home-4-line',
    remark = '不动产权属档案查询页面'
where menu_id = 2130;

insert into sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  2130, '不动产档案', 2100, 12, 'real-estate', 'asset/real-estate/index', '', 'AssetRealEstate',
  1, 0, 'C', '0', '0', 'asset:realEstate:list', 'home-4-line',
  'admin', sysdate(), '', null, '不动产权属档案查询页面'
from dual
where not exists (
  select 1 from sys_menu where menu_id = 2130 or route_name = 'AssetRealEstate'
);

-- 2131：不动产档案详情查询权限
update sys_menu
set parent_id = 2130,
    order_num = 1,
    path = '',
    component = '',
    query = '',
    route_name = '',
    menu_type = 'F',
    visible = '0',
    status = '0',
    perms = 'asset:realEstate:query',
    icon = '#',
    remark = '不动产权属档案详情查询权限'
where menu_id = 2131;

insert into sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  2131, '不动产档案详情查询', 2130, 1, '', '', '', '',
  1, 0, 'F', '0', '0', 'asset:realEstate:query', '#',
  'admin', sysdate(), '', null, '不动产权属档案详情查询权限'
from dual
where not exists (
  select 1 from sys_menu where menu_id = 2131 or perms = 'asset:realEstate:query'
);

-- 不动产分类：办公用房
insert into ast_asset_category (
  category_id, parent_id, ancestors, category_code, category_name, asset_type, order_num,
  status, del_flag, create_by, create_time, update_by, update_time, remark
)
select
  1101, 0, '0', 'REAL_ESTATE_OFFICE', '办公用房', 'REAL_ESTATE', 1,
  '0', '0', 'admin', sysdate(), '', null, '不动产档案点测样例分类'
from dual
where not exists (
  select 1 from ast_asset_category where category_code = 'REAL_ESTATE_OFFICE'
);

-- 不动产统一台账样例
insert into ast_asset_ledger (
  asset_id, asset_code, asset_name, asset_type, category_id, spec_model, serial_no,
  asset_status, source_type, acquire_type, owner_dept_id, use_dept_id, responsible_user_id,
  location_name, original_value, acquisition_date, enable_date, last_inventory_date,
  del_flag, create_by, create_time, update_by, update_time, remark
)
select
  20001, 'RE-2026-0001', '深圳研发办公楼A座', 'REAL_ESTATE', 1101, '地上12层/钢混结构', 'SZ-RE-A-2026-0001',
  'IN_USE', 'MANUAL', 'PURCHASE', 103, 103, 1,
  '深圳南山研发园区A座', 12500000.00, '2024-06-01', '2024-07-01', '2026-03-01',
  '0', 'admin', sysdate(), '', null, '不动产档案点测样例台账'
from dual
where not exists (
  select 1 from ast_asset_ledger where asset_id = 20001 or asset_code = 'RE-2026-0001'
);

-- 不动产权属扩展档案样例
insert into ast_asset_real_estate_profile (
  profile_id, asset_id, ownership_cert_no, land_use_type, building_area,
  create_by, create_time, update_by, update_time, remark, del_flag
)
select
  20001, 20001, '粤(2024)深圳市不动产权第A0001号', '科研办公', 18650.50,
  'admin', sysdate(), '', null, '不动产档案点测样例', '0'
from dual
where not exists (
  select 1 from ast_asset_real_estate_profile where asset_id = 20001
);

-- 台账生命周期样例日志
insert into ast_asset_change_log (
  log_id, asset_id, biz_type, biz_id, before_status, after_status, operate_by,
  operate_time, change_desc, remark
)
select
  20001, 20001, 'LEDGER_CREATE', null, null, 'IN_USE', 'admin',
  sysdate(), '初始化不动产档案点测样例资产', '不动产档案点测样例日志'
from dual
where not exists (
  select 1 from ast_asset_change_log where log_id = 20001
);
