-- =========================================================
-- 不动产生命周期菜单与权限初始化脚本
-- 说明：
-- 1. 依赖资产系统父菜单已存在
-- 2. 四类不动产动作统一挂在“资产系统”下，避免把动作入口塞回资产编辑抽屉
-- 3. 所有 INSERT 均带 NOT EXISTS 判断，可重复执行
-- =========================================================

SELECT menu_id INTO @asset_parent_id
FROM sys_menu
WHERE menu_name = '资产系统'
  AND parent_id = 0
LIMIT 1;

-- 一、建立不动产生命周期四类台账菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '权属变更', @asset_parent_id, 6, 'real-estate-ownership', 'asset/real-estate/ownership/index', 1, 0, 'C', '0', '0', 'asset:realEstateOwnership:list', 'guide', 'admin', sysdate(), '不动产权属变更台账'
WHERE @asset_parent_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu
      WHERE perms = 'asset:realEstateOwnership:list'
  );

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '用途变更', @asset_parent_id, 7, 'real-estate-usage', 'asset/real-estate/usage/index', 1, 0, 'C', '0', '0', 'asset:realEstateUsage:list', 'office-building', 'admin', sysdate(), '不动产用途变更台账'
WHERE @asset_parent_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu
      WHERE perms = 'asset:realEstateUsage:list'
  );

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '状态变更', @asset_parent_id, 8, 'real-estate-status', 'asset/real-estate/status/index', 1, 0, 'C', '0', '0', 'asset:realEstateStatus:list', 'switch-button', 'admin', sysdate(), '不动产状态变更台账'
WHERE @asset_parent_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu
      WHERE perms = 'asset:realEstateStatus:list'
  );

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '注销处置', @asset_parent_id, 9, 'real-estate-disposal', 'asset/real-estate/disposal/index', 1, 0, 'C', '0', '0', 'asset:realEstateDisposal:list', 'delete-location', 'admin', sysdate(), '不动产注销/处置台账'
WHERE @asset_parent_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu
      WHERE perms = 'asset:realEstateDisposal:list'
  );

-- 二、补齐按钮权限，前端台账页只依赖 query/add 两类最小权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '权属变更查询', menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'asset:realEstateOwnership:query', '#', 'admin', sysdate(), '权属变更详情按钮'
FROM sys_menu
WHERE perms = 'asset:realEstateOwnership:list'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu
      WHERE perms = 'asset:realEstateOwnership:query'
  );

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '权属变更新增', menu_id, 2, '#', '', 1, 0, 'F', '0', '0', 'asset:realEstateOwnership:add', '#', 'admin', sysdate(), '权属变更新增按钮'
FROM sys_menu
WHERE perms = 'asset:realEstateOwnership:list'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu
      WHERE perms = 'asset:realEstateOwnership:add'
  );

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '用途变更查询', menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'asset:realEstateUsage:query', '#', 'admin', sysdate(), '用途变更详情按钮'
FROM sys_menu
WHERE perms = 'asset:realEstateUsage:list'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu
      WHERE perms = 'asset:realEstateUsage:query'
  );

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '用途变更新增', menu_id, 2, '#', '', 1, 0, 'F', '0', '0', 'asset:realEstateUsage:add', '#', 'admin', sysdate(), '用途变更新增按钮'
FROM sys_menu
WHERE perms = 'asset:realEstateUsage:list'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu
      WHERE perms = 'asset:realEstateUsage:add'
  );

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '状态变更查询', menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'asset:realEstateStatus:query', '#', 'admin', sysdate(), '状态变更详情按钮'
FROM sys_menu
WHERE perms = 'asset:realEstateStatus:list'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu
      WHERE perms = 'asset:realEstateStatus:query'
  );

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '状态变更新增', menu_id, 2, '#', '', 1, 0, 'F', '0', '0', 'asset:realEstateStatus:add', '#', 'admin', sysdate(), '状态变更新增按钮'
FROM sys_menu
WHERE perms = 'asset:realEstateStatus:list'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu
      WHERE perms = 'asset:realEstateStatus:add'
  );

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '注销处置查询', menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'asset:realEstateDisposal:query', '#', 'admin', sysdate(), '注销/处置详情按钮'
FROM sys_menu
WHERE perms = 'asset:realEstateDisposal:list'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu
      WHERE perms = 'asset:realEstateDisposal:query'
  );

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '注销处置新增', menu_id, 2, '#', '', 1, 0, 'F', '0', '0', 'asset:realEstateDisposal:add', '#', 'admin', sysdate(), '注销/处置新增按钮'
FROM sys_menu
WHERE perms = 'asset:realEstateDisposal:list'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu
      WHERE perms = 'asset:realEstateDisposal:add'
  );

-- 三、输出当前口径，方便执行后核对
SELECT menu_name, path, component, perms, status
FROM sys_menu
WHERE component IN (
    'asset/real-estate/ownership/index',
    'asset/real-estate/usage/index',
    'asset/real-estate/status/index',
    'asset/real-estate/disposal/index'
)
   OR perms IN (
       'asset:realEstateOwnership:query',
       'asset:realEstateOwnership:add',
       'asset:realEstateUsage:query',
       'asset:realEstateUsage:add',
       'asset:realEstateStatus:query',
       'asset:realEstateStatus:add',
       'asset:realEstateDisposal:query',
       'asset:realEstateDisposal:add'
   )
ORDER BY parent_id, order_num, menu_id;
