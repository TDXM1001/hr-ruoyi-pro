-- 人事管理目录
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('人事管理', 0, 10, 'hr', null, 1, 0, 'M', '0', '0', '', 'peoples', 'admin', sysdate(), '', null, '人事管理目录');

-- 组织管理菜单
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('组织架构', (select menu_id from sys_menu where menu_name = '人事管理'), 1, 'organization', 'hr/organization/index', 1, 0, 'C', '0', '0', 'hr:organization:list', 'tree', 'admin', sysdate(), '', null, '组织架构菜单');

-- 员工花名册菜单
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('员工花名册', (select menu_id from sys_menu where menu_name = '人事管理'), 2, 'employee', 'hr/employee/index', 1, 0, 'C', '0', '0', 'hr:employee:list', 'user', 'admin', sysdate(), '', null, '员工花名册菜单');

-- 员工档案详情页面 (隐藏菜单用于详情)
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('员工档案详情', (select menu_id from sys_menu where menu_name = '人事管理'), 3, 'employee/detail', 'hr/employee/detail/index', 1, 0, 'C', '1', '0', 'hr:employee:detail', 'dict', 'admin', sysdate(), '', null, '员工档案详情隐藏菜单');
