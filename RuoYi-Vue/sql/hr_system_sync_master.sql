SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 1. 清理 HR 业务测试数据
TRUNCATE TABLE `hr_employee`;
TRUNCATE TABLE `hr_position`;
TRUNCATE TABLE `hr_position_level`;
TRUNCATE TABLE `hr_position_category`;
TRUNCATE TABLE `hr_organization`;

-- ---------------------------------------------------------
-- 2. 系统管理 - 部门初始化 (依照 ry_20250522.sql 结构 14个字段)
-- ---------------------------------------------------------
DELETE FROM `sys_dept` WHERE `dept_id` IN (200, 201, 202, 203);
INSERT INTO `sys_dept` VALUES (200, 0, '0', '星辰科技集团', 1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
INSERT INTO `sys_dept` VALUES (201, 200, '0,200', '北京星辰科技有限公司', 1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
INSERT INTO `sys_dept` VALUES (202, 201, '0,200,201', '研发中心', 1, '张三', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
INSERT INTO `sys_dept` VALUES (203, 202, '0,200,201,202', '前端开发一组', 1, '王五', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);

-- ---------------------------------------------------------
-- 3. 人事管理 - 组织架构同步 (依照 hr_core_init.sql 结构 18个字段)
-- ---------------------------------------------------------
INSERT INTO `hr_organization` VALUES (1, 0, '0', '星辰科技集团', 'XCKJ', '1', 1, 1, '400-888-9999', '星辰大厦', '2010-01-01', 1, '0', 200, 'admin', sysdate(), '', null);
INSERT INTO `hr_organization` VALUES (2, 1, '0,1', '北京星辰科技有限公司', 'BJXC', '2', 2, 1, '010-12345678', '北京朝阳区星辰大厦', '2010-01-01', 1, '0', 201, 'admin', sysdate(), '', null);
INSERT INTO `hr_organization` VALUES (3, 2, '0,1,2', '研发中心', 'RD', '4', 3, 2, '010-12345678', '北京朝阳区星辰大厦研发部', '2010-01-01', 1, '0', 202, 'admin', sysdate(), '', null);
INSERT INTO `hr_organization` VALUES (4, 3, '0,1,2,3', '前端开发一组', 'F1', '6', 4, 4, '010-12345678', '研发区域', '2010-01-01', 1, '0', 203, 'admin', sysdate(), '', null);

-- ---------------------------------------------------------
-- 4. 系统管理 - 岗位与用户初始化 (依照 ry_20250522.sql 结构 20个字段)
-- ---------------------------------------------------------
DELETE FROM `sys_post` WHERE `post_id` IN (210, 211, 212);
INSERT INTO `sys_post` VALUES (210, 'FED', '前端开发工程师', 1, '0', 'admin', sysdate(), '', null, '');
INSERT INTO `sys_post` VALUES (211, 'BED', 'Java后端开发工程师', 2, '0', 'admin', sysdate(), '', null, '');
INSERT INTO `sys_post` VALUES (212, 'RD_D', '研发总监', 3, '0', 'admin', sysdate(), '', null, '');

-- 用户表 (必须是 20 个字段: ID, DeptID, User, Nick, Type, Email, Phone, Sex, Avat, Pass, Stat, Del, IP, LDate, PDate, CBy, CTime, UBy, UTime, Rem)
DELETE FROM `sys_user` WHERE `user_id` IN (300, 301, 302);
INSERT INTO `sys_user` VALUES (300, 200, 'admin_hr', '人力管理员', '00', 'ry@qq.com', '15888888888', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', sysdate(), sysdate(), 'admin', sysdate(), '', null, '初始管理员');
INSERT INTO `sys_user` VALUES (301, 202, 'zhangsan', '张三', '00', 'zs@qq.com', '13800138000', '0', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', sysdate(), sysdate(), 'admin', sysdate(), '', null, '研发总监');
INSERT INTO `sys_user` VALUES (302, 203, 'wangwu', '王五', '00', 'ww@qq.com', '13800138002', '0', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', sysdate(), sysdate(), 'admin', sysdate(), '', null, '普通员工');

-- 关联岗位和角色
REPLACE INTO `sys_user_post` VALUES (301, 212), (302, 210);
REPLACE INTO `sys_user_role` VALUES (300, 1), (301, 2), (302, 2);

-- ---------------------------------------------------------
-- 5. 人事管理 - 职级岗位与名册同步 (依照 hr_core_init.sql)
-- ---------------------------------------------------------
INSERT INTO `hr_position_category` VALUES (1, '技术族', 'T', '从事技术研发工作', '0', 'admin', sysdate(), '', null);
INSERT INTO `hr_position_level` VALUES (1, 1, '高级工程师 (T3)', 'T3', 3, 25000, 40000, '核心研发', '0', 'admin', sysdate(), '', null);
INSERT INTO `hr_position` VALUES (1, '前端开发工程师', 'FED', 1, 3, 10, 5, 'Web开发', '熟练掌握React', '0', 210, 'admin', sysdate(), '', null);

-- 最终关联员工档案到用户 ID
INSERT INTO `hr_employee` (`employee_id`, `employee_no`, `user_id`, `name`, `gender`, `id_card`, `phone`, `email`, `org_id`, `position_id`, `employee_type`, `employee_status`, `hire_date`, `create_by`, `create_time`) 
VALUES (3, '90004', 302, '王五', '0', '110105199011011234', '13800138002', 'ww@qq.com', 4, 1, '1', '2', '2017-03-01', 'admin', sysdate());

SET FOREIGN_KEY_CHECKS = 1;
