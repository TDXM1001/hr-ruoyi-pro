-- ----------------------------
-- 固定资产一期联调初始化数据
-- 说明：
-- 1. 当前前端运行在 backend 动态路由模式，资产菜单需要由 sys_menu 提供。
-- 2. 本脚本只补联调用的菜单、权限、字典、分类和样例数据，不负责建表。
-- 3. 全新环境请先执行 00-asset-schema.sql，再执行本脚本。
-- 4. 已执行旧版 00 的环境请先执行 02-asset-upgrade-20260318.sql，再执行本脚本。
-- 5. 如果旧环境已经执行过早期版本的 01，请继续执行 03-asset-menu-upgrade-20260318.sql 完成菜单升级。
-- 6. 如果旧环境已经执行过 01 和 03，请补执行 05-asset-use-menu-upgrade-20260319.sql 接入资产使用页面菜单。
-- ----------------------------

-- ----------------------------
-- 1. 资产菜单、隐藏路由与按钮权限
-- ----------------------------
insert into sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  2100, '资产管理', 0, 4, 'asset', null, '', 'Asset',
  1, 0, 'M', '0', '0', '', 'archive',
  'admin', sysdate(), '', null, '固定资产一期菜单目录'
from dual
where not exists (
  select 1 from sys_menu where menu_id = 2100 or (parent_id = 0 and path = 'asset')
);

insert into sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  2101, '资产台账', 2100, 1, 'ledger', 'asset/ledger/index', '', 'AssetLedger',
  1, 0, 'C', '0', '0', 'asset:ledger:list', 'archive-stack',
  'admin', sysdate(), '', null, '资产台账列表页'
from dual
where not exists (
  select 1 from sys_menu where menu_id = 2101 or route_name = 'AssetLedger'
);

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
  select 1 from sys_menu where menu_id = 2102 or route_name = 'AssetLedgerDetail'
);

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
  select 1 from sys_menu where menu_id = 2103 or route_name = 'AssetLedgerCreate'
);

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
  select 1 from sys_menu where menu_id = 2104 or route_name = 'AssetLedgerEdit'
);

insert into sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
select
  2105, '资产台账导出', 2101, 5, '', '', '', '',
  1, 0, 'F', '0', '0', 'asset:ledger:export', '#',
  'admin', sysdate(), '', null, '资产台账导出按钮'
from dual
where not exists (
  select 1 from sys_menu where menu_id = 2105 or perms = 'asset:ledger:export'
);

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
  select 1 from sys_menu where menu_id = 2110 or route_name = 'AssetUse'
);

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
  select 1 from sys_menu where menu_id = 2111 or perms = 'asset:handover:add'
);

-- ----------------------------
-- 2. 资产字典
-- ----------------------------
insert into sys_dict_type (
  dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark
)
select
  200, '资产状态', 'ast_asset_status', '0', 'admin', sysdate(), '', null, '固定资产状态字典'
from dual
where not exists (
  select 1 from sys_dict_type where dict_type = 'ast_asset_status'
);

insert into sys_dict_type (
  dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark
)
select
  201, '资产来源', 'ast_asset_source_type', '0', 'admin', sysdate(), '', null, '固定资产来源字典'
from dual
where not exists (
  select 1 from sys_dict_type where dict_type = 'ast_asset_source_type'
);

insert into sys_dict_type (
  dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark
)
select
  202, '取得方式', 'ast_asset_acquire_type', '0', 'admin', sysdate(), '', null, '固定资产取得方式字典'
from dual
where not exists (
  select 1 from sys_dict_type where dict_type = 'ast_asset_acquire_type'
);

insert into sys_dict_data (
  dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
  is_default, status, create_by, create_time, update_by, update_time, remark
)
select
  2000, 1, '在册', 'IN_LEDGER', 'ast_asset_status', '', 'primary',
  'Y', '0', 'admin', sysdate(), '', null, '资产已建账并在册'
from dual
where not exists (
  select 1 from sys_dict_data where dict_type = 'ast_asset_status' and dict_value = 'IN_LEDGER'
);

insert into sys_dict_data (
  dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
  is_default, status, create_by, create_time, update_by, update_time, remark
)
select
  2001, 2, '使用中', 'IN_USE', 'ast_asset_status', '', 'success',
  'N', '0', 'admin', sysdate(), '', null, '资产处于部门使用中'
from dual
where not exists (
  select 1 from sys_dict_data where dict_type = 'ast_asset_status' and dict_value = 'IN_USE'
);

insert into sys_dict_data (
  dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
  is_default, status, create_by, create_time, update_by, update_time, remark
)
select
  2002, 3, '闲置中', 'IDLE', 'ast_asset_status', '', 'info',
  'N', '0', 'admin', sysdate(), '', null, '资产当前闲置'
from dual
where not exists (
  select 1 from sys_dict_data where dict_type = 'ast_asset_status' and dict_value = 'IDLE'
);

insert into sys_dict_data (
  dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
  is_default, status, create_by, create_time, update_by, update_time, remark
)
select
  2010, 1, '手工新增', 'MANUAL', 'ast_asset_source_type', '', 'primary',
  'Y', '0', 'admin', sysdate(), '', null, '人工录入资产'
from dual
where not exists (
  select 1 from sys_dict_data where dict_type = 'ast_asset_source_type' and dict_value = 'MANUAL'
);

insert into sys_dict_data (
  dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
  is_default, status, create_by, create_time, update_by, update_time, remark
)
select
  2011, 2, '存量补录', 'IMPORT', 'ast_asset_source_type', '', 'warning',
  'N', '0', 'admin', sysdate(), '', null, '历史存量资产导入'
from dual
where not exists (
  select 1 from sys_dict_data where dict_type = 'ast_asset_source_type' and dict_value = 'IMPORT'
);

insert into sys_dict_data (
  dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
  is_default, status, create_by, create_time, update_by, update_time, remark
)
select
  2020, 1, '采购', 'PURCHASE', 'ast_asset_acquire_type', '', 'primary',
  'Y', '0', 'admin', sysdate(), '', null, '采购取得'
from dual
where not exists (
  select 1 from sys_dict_data where dict_type = 'ast_asset_acquire_type' and dict_value = 'PURCHASE'
);

insert into sys_dict_data (
  dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
  is_default, status, create_by, create_time, update_by, update_time, remark
)
select
  2021, 2, '调拨转入', 'ALLOCATE_IN', 'ast_asset_acquire_type', '', 'success',
  'N', '0', 'admin', sysdate(), '', null, '调拨转入取得'
from dual
where not exists (
  select 1 from sys_dict_data where dict_type = 'ast_asset_acquire_type' and dict_value = 'ALLOCATE_IN'
);

insert into sys_dict_data (
  dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
  is_default, status, create_by, create_time, update_by, update_time, remark
)
select
  2022, 3, '捐赠', 'DONATION', 'ast_asset_acquire_type', '', 'warning',
  'N', '0', 'admin', sysdate(), '', null, '捐赠取得'
from dual
where not exists (
  select 1 from sys_dict_data where dict_type = 'ast_asset_acquire_type' and dict_value = 'DONATION'
);

-- ----------------------------
-- 3. 资产分类与台账样例数据
-- ----------------------------
insert into ast_asset_category (
  category_id, parent_id, ancestors, category_code, category_name, asset_type, order_num,
  status, del_flag, create_by, create_time, update_by, update_time, remark
)
select
  1001, 0, '0', 'FIXED_ELECTRONIC', '电子设备', 'FIXED', 1,
  '0', '0', 'admin', sysdate(), '', null, '固定资产一期联调分类'
from dual
where not exists (
  select 1 from ast_asset_category where category_code = 'FIXED_ELECTRONIC'
);

insert into ast_asset_category (
  category_id, parent_id, ancestors, category_code, category_name, asset_type, order_num,
  status, del_flag, create_by, create_time, update_by, update_time, remark
)
select
  1002, 0, '0', 'FIXED_OFFICE', '办公家具', 'FIXED', 2,
  '0', '0', 'admin', sysdate(), '', null, '固定资产一期联调分类'
from dual
where not exists (
  select 1 from ast_asset_category where category_code = 'FIXED_OFFICE'
);

insert into ast_asset_category (
  category_id, parent_id, ancestors, category_code, category_name, asset_type, order_num,
  status, del_flag, create_by, create_time, update_by, update_time, remark
)
select
  1003, 0, '0', 'FIXED_MEETING', '会议设备', 'FIXED', 3,
  '0', '0', 'admin', sysdate(), '', null, '固定资产一期联调分类'
from dual
where not exists (
  select 1 from ast_asset_category where category_code = 'FIXED_MEETING'
);

insert into ast_asset_ledger (
  asset_id, asset_code, asset_name, asset_type, category_id, spec_model, serial_no,
  asset_status, source_type, acquire_type, owner_dept_id, use_dept_id, responsible_user_id,
  location_name, original_value, acquisition_date, enable_date, last_inventory_date,
  del_flag, create_by, create_time, update_by, update_time, remark
)
select
  10001, 'FA-2026-0001', '研发笔记本电脑', 'FIXED', 1001, 'ThinkBook T14 Gen 5', 'SN-T14-20260001',
  'IN_USE', 'MANUAL', 'PURCHASE', 103, 103, 1,
  '研发部门-A区工位', 8999.00, '2026-02-18', '2026-02-20', null,
  '0', 'admin', sysdate(), '', null, '固定资产一期联调样例数据'
from dual
where not exists (
  select 1 from ast_asset_ledger where asset_code = 'FA-2026-0001'
);

insert into ast_asset_ledger (
  asset_id, asset_code, asset_name, asset_type, category_id, spec_model, serial_no,
  asset_status, source_type, acquire_type, owner_dept_id, use_dept_id, responsible_user_id,
  location_name, original_value, acquisition_date, enable_date, last_inventory_date,
  del_flag, create_by, create_time, update_by, update_time, remark
)
select
  10002, 'FA-2026-0002', '财务办公转椅', 'FIXED', 1002, 'Herman Miller Aeron', 'SN-CHAIR-20260002',
  'IDLE', 'IMPORT', 'PURCHASE', 106, 106, 2,
  '财务部门资料室', 5680.00, '2026-01-06', '2026-01-10', null,
  '0', 'admin', sysdate(), '', null, '固定资产一期联调样例数据'
from dual
where not exists (
  select 1 from ast_asset_ledger where asset_code = 'FA-2026-0002'
);

insert into ast_asset_ledger (
  asset_id, asset_code, asset_name, asset_type, category_id, spec_model, serial_no,
  asset_status, source_type, acquire_type, owner_dept_id, use_dept_id, responsible_user_id,
  location_name, original_value, acquisition_date, enable_date, last_inventory_date,
  del_flag, create_by, create_time, update_by, update_time, remark
)
select
  10003, 'FA-2026-0003', '市场部会议投影仪', 'FIXED', 1003, 'Epson CB-FH06', 'SN-PJT-20260003',
  'IN_LEDGER', 'MANUAL', 'PURCHASE', 104, 104, 1,
  '市场部门会议室', 4299.00, '2026-03-01', '2026-03-05', null,
  '0', 'admin', sysdate(), '', null, '固定资产一期联调样例数据'
from dual
where not exists (
  select 1 from ast_asset_ledger where asset_code = 'FA-2026-0003'
);

insert into ast_asset_change_log (
  log_id, asset_id, biz_type, biz_id, before_status, after_status, operate_by,
  operate_time, change_desc, remark
)
select
  10001, 10001, 'LEDGER_CREATE', null, null, 'IN_USE', 'admin',
  sysdate(), '初始化联调样例资产', '固定资产一期联调样例日志'
from dual
where not exists (
  select 1 from ast_asset_change_log where log_id = 10001
);

insert into ast_asset_change_log (
  log_id, asset_id, biz_type, biz_id, before_status, after_status, operate_by,
  operate_time, change_desc, remark
)
select
  10002, 10002, 'LEDGER_CREATE', null, null, 'IDLE', 'admin',
  sysdate(), '初始化联调样例资产', '固定资产一期联调样例日志'
from dual
where not exists (
  select 1 from ast_asset_change_log where log_id = 10002
);

insert into ast_asset_change_log (
  log_id, asset_id, biz_type, biz_id, before_status, after_status, operate_by,
  operate_time, change_desc, remark
)
select
  10003, 10003, 'LEDGER_CREATE', null, null, 'IN_LEDGER', 'admin',
  sysdate(), '初始化联调样例资产', '固定资产一期联调样例日志'
from dual
where not exists (
  select 1 from ast_asset_change_log where log_id = 10003
);
