-- =========================================================
-- 资产生命周期菜单与审批权限补丁
-- 说明：
-- 1. 适用于已执行 20260312_asset_workflow_menu.sql 的数据库
-- 2. 脚本只修正现有菜单与按钮权限，不改动历史初始化脚本语义
-- 3. 如当前环境暂不开放审批中心，可将 @enable_workflow_center_menu 调整为 0
-- 4. 所有 INSERT 均带 NOT EXISTS 判断，可重复执行
-- =========================================================

SET @enable_workflow_center_menu = 1;
SET @workflow_menu_status = IF(@enable_workflow_center_menu = 1, '0', '1');

-- 一、收敛审批中心菜单权限标识
UPDATE sys_menu
SET perms = 'workflow:task:todo',
    status = @workflow_menu_status,
    remark = '待办处理菜单'
WHERE component = 'asset/workflow/todo/index';

UPDATE sys_menu
SET perms = 'workflow:task:done',
    status = @workflow_menu_status,
    remark = '已办列表菜单'
WHERE component = 'asset/workflow/done/index';

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '审批处理', menu_id, 1, '#', '', 1, 0, 'F', '0', @workflow_menu_status, 'workflow:task:approve', '#', 'admin', sysdate(), '审批动作按钮'
FROM sys_menu
WHERE component = 'asset/workflow/todo/index'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu
      WHERE perms = 'workflow:task:approve'
  );

-- 二、修正固定资产生命周期台账页面路径与菜单权限
UPDATE sys_menu
SET menu_name = '维修管理',
    path = 'maintenance',
    component = 'asset/maintenance/index',
    perms = 'asset:maintenance:list',
    remark = '维修管理台账'
WHERE component = 'asset/repair/index'
   OR path = 'repair'
   OR perms = 'asset:repair:list';

UPDATE sys_menu
SET menu_name = '报废处置',
    path = 'disposal',
    component = 'asset/disposal/index',
    perms = 'asset:disposal:list',
    remark = '报废处置台账'
WHERE component = 'asset/scrap/index'
   OR path = 'scrap'
   OR perms = 'asset:scrap:list';

-- 三、补齐领用、维修、处置按钮权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '资产归还', menu_id, 10, '#', '', 1, 0, 'F', '0', '0', 'asset:requisition:return', '#', 'admin', sysdate(), '领用归还按钮'
FROM sys_menu
WHERE component = 'asset/requisition/index'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu
      WHERE perms = 'asset:requisition:return'
  );

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '维修详情', menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'asset:maintenance:query', '#', 'admin', sysdate(), '维修详情按钮'
FROM sys_menu
WHERE component = 'asset/maintenance/index'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu
      WHERE perms = 'asset:maintenance:query'
  );

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '维修新增', menu_id, 2, '#', '', 1, 0, 'F', '0', '0', 'asset:maintenance:add', '#', 'admin', sysdate(), '维修新增按钮'
FROM sys_menu
WHERE component = 'asset/maintenance/index'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu
      WHERE perms = 'asset:maintenance:add'
  );

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '完成维修', menu_id, 3, '#', '', 1, 0, 'F', '0', '0', 'asset:maintenance:complete', '#', 'admin', sysdate(), '维修完成按钮'
FROM sys_menu
WHERE component = 'asset/maintenance/index'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu
      WHERE perms = 'asset:maintenance:complete'
  );

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '处置详情', menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'asset:disposal:query', '#', 'admin', sysdate(), '处置详情按钮'
FROM sys_menu
WHERE component = 'asset/disposal/index'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu
      WHERE perms = 'asset:disposal:query'
  );

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '处置新增', menu_id, 2, '#', '', 1, 0, 'F', '0', '0', 'asset:disposal:add', '#', 'admin', sysdate(), '处置新增按钮'
FROM sys_menu
WHERE component = 'asset/disposal/index'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu
      WHERE perms = 'asset:disposal:add'
  );

-- 四、输出补丁结果，方便执行后快速核对
SELECT menu_name, path, component, perms, status
FROM sys_menu
WHERE component IN (
    'asset/requisition/index',
    'asset/maintenance/index',
    'asset/disposal/index',
    'asset/workflow/todo/index',
    'asset/workflow/done/index'
)
   OR perms IN (
       'asset:requisition:return',
       'asset:maintenance:query',
       'asset:maintenance:add',
       'asset:maintenance:complete',
       'asset:disposal:query',
       'asset:disposal:add',
       'workflow:task:approve'
   )
ORDER BY parent_id, order_num, menu_id;
