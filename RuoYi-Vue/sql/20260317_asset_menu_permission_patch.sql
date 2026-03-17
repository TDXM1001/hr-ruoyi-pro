-- =========================================================
-- 资产菜单权限补丁（领用申请 + 财务查看）
-- 说明：
-- 1. 适用于已执行 20260311_asset_menu_init.sql、20260312_asset_workflow_menu.sql 的数据库
-- 2. 对齐当前后端控制器权限：asset:requisition:query/add、asset:finance:query
-- 3. 所有 INSERT 均带 NOT EXISTS 判断，可重复执行
-- =========================================================

-- 一、补齐“领用归还”台账按钮权限（AssetRequisitionController）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '领用详情', menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'asset:requisition:query', '#', 'admin', sysdate(), '领用详情按钮'
FROM sys_menu
WHERE component = 'asset/requisition/index'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu
      WHERE perms = 'asset:requisition:query'
  );

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '发起领用', menu_id, 2, '#', '', 1, 0, 'F', '0', '0', 'asset:requisition:add', '#', 'admin', sysdate(), '领用申请按钮'
FROM sys_menu
WHERE component = 'asset/requisition/index'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu
      WHERE perms = 'asset:requisition:add'
  );

-- 二、补齐“资产台账”财务查看权限（AssetFinanceController）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '财务查看', menu_id, 6, '#', '', 1, 0, 'F', '0', '0', 'asset:finance:query', '#', 'admin', sysdate(), '资产财务查询按钮'
FROM sys_menu
WHERE perms = 'asset:info:list'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu
      WHERE perms = 'asset:finance:query'
  );

-- 三、输出补丁结果，方便执行后核对
SELECT menu_name, parent_id, order_num, path, component, perms, status
FROM sys_menu
WHERE perms IN (
    'asset:requisition:list',
    'asset:requisition:query',
    'asset:requisition:add',
    'asset:requisition:return',
    'asset:info:list',
    'asset:finance:query'
)
ORDER BY parent_id, order_num, menu_id;
