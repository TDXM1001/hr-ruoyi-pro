-- ----------------------------
-- 参考 ry_20250522.sql 重写 HR 模块菜单初始数据
-- sys_menu 共有20个字段
-- ----------------------------

-- 一级菜单: 人事管理
insert into sys_menu values('2000', '人事管理', '0', '10', 'hr', null, '', '', 1, 0, 'M', '0', '0', '', 'peoples', 'admin', sysdate(), '', null, '人事管理目录');

-- 二级菜单
insert into sys_menu values('2001', '组织架构', '2000', '1', 'organization', 'hr/organization/index', '', '', 1, 0, 'C', '0', '0', 'hr:organization:list', 'tree', 'admin', sysdate(), '', null, '组织架构菜单');
insert into sys_menu values('2002', '员工花名册', '2000', '2', 'employee', 'hr/employee/index', '', '', 1, 0, 'C', '0', '0', 'hr:employee:list', 'user', 'admin', sysdate(), '', null, '员工花名册菜单');

-- 隐藏的详细页面
insert into sys_menu values('2003', '员工档案详情', '2000', '3', 'employee/detail', 'hr/employee/detail/index', '', '', 1, 0, 'C', '1', '0', 'hr:employee:detail', 'dict', 'admin', sysdate(), '', null, '员工档案详情隐藏菜单');

-- 三级按钮权限
insert into sys_menu values('2004', '组织新增', '2001', '1',  '', '', '', '', 1, 0, 'F', '0', '0', 'hr:organization:add',    '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2005', '组织修改', '2001', '2',  '', '', '', '', 1, 0, 'F', '0', '0', 'hr:organization:edit',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2006', '组织删除', '2001', '3',  '', '', '', '', 1, 0, 'F', '0', '0', 'hr:organization:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2007', '组织查询', '2001', '4',  '', '', '', '', 1, 0, 'F', '0', '0', 'hr:organization:query',  '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values('2008', '员工新增', '2002', '1',  '', '', '', '', 1, 0, 'F', '0', '0', 'hr:employee:add',    '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2009', '员工修改', '2002', '2',  '', '', '', '', 1, 0, 'F', '0', '0', 'hr:employee:edit',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2010', '员工删除', '2002', '3',  '', '', '', '', 1, 0, 'F', '0', '0', 'hr:employee:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2011', '员工查询', '2002', '4',  '', '', '', '', 1, 0, 'F', '0', '0', 'hr:employee:query',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2012', '员工导出', '2002', '5',  '', '', '', '', 1, 0, 'F', '0', '0', 'hr:employee:export', '#', 'admin', sysdate(), '', null, '');
