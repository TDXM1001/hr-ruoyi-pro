-- 资产管理系统菜单初始化
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('资产系统', 0, 4, 'asset', NULL, 1, 0, 'M', '0', '0', '', 'dict', 'admin', sysdate(), '', null, '资产管理父菜单');

-- 获取刚刚插入的父菜单ID（在MySQL中可以使用 last_insert_id()，但在SQL脚本中通常需要先查询或手动查找）
-- 为了脚本的确定性，这里假设父菜单ID为 2000，或者在执行时由用户根据实际情况调整。
-- 在若依中，通常会先分配一个较大的ID给新模块。
SET @parent_id = LAST_INSERT_ID();

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('资产分类', @parent_id, 1, 'category', 'asset/category/index', 1, 0, 'C', '0', '0', 'asset:category:list', 'tree', 'admin', sysdate(), '', null, '资产分类菜单');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('资产台账', @parent_id, 2, 'list', 'asset/list/index', 1, 0, 'C', '0', '0', 'asset:info:list', 'list', 'admin', sysdate(), '', null, '资产台账菜单');

-- 资产分类按钮权限
SET @category_id = LAST_INSERT_ID() - 1; -- 刚才插入的第一个子菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('分类查询', @category_id, 1, '#', '', 1, 0, 'F', '0', '0', 'asset:category:query', '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('分类新增', @category_id, 2, '#', '', 1, 0, 'F', '0', '0', 'asset:category:add', '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('分类修改', @category_id, 3, '#', '', 1, 0, 'F', '0', '0', 'asset:category:edit', '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('分类删除', @category_id, 4, '#', '', 1, 0, 'F', '0', '0', 'asset:category:remove', '#', 'admin', sysdate(), '', null, '');

-- 资产台账按钮权限
SET @info_id = LAST_INSERT_ID() - 4; -- 刚才插入的第二个子菜单 (由于上面插了4个按钮，所以是 -4)
-- 注意：这里 LAST_INSERT_ID 可能需要更精确的逻辑，但在单次连续执行脚本中通常是按顺序的。
-- 为了保险，重新获取 @info_id 比较好。
SELECT menu_id INTO @info_id FROM sys_menu WHERE menu_name = '资产台账' AND parent_id = @parent_id;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('资产查询', @info_id, 1, '#', '', 1, 0, 'F', '0', '0', 'asset:info:query', '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('资产新增', @info_id, 2, '#', '', 1, 0, 'F', '0', '0', 'asset:info:add', '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('资产修改', @info_id, 3, '#', '', 1, 0, 'F', '0', '0', 'asset:info:edit', '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('资产删除', @info_id, 4, '#', '', 1, 0, 'F', '0', '0', 'asset:info:remove', '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('资产导出', @info_id, 5, '#', '', 1, 0, 'F', '0', '0', 'asset:info:export', '#', 'admin', sysdate(), '', null, '');
