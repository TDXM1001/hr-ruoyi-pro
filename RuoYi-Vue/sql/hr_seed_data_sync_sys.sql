SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 清空 HR 相关表数据，确保重新初始化
TRUNCATE TABLE `hr_employee`;
TRUNCATE TABLE `hr_employee_contact`;
TRUNCATE TABLE `hr_employee_education`;
TRUNCATE TABLE `hr_employee_work_history`;
TRUNCATE TABLE `hr_position`;
TRUNCATE TABLE `hr_position_level`;
TRUNCATE TABLE `hr_position_category`;
TRUNCATE TABLE `hr_organization`;


-- ==========================================
-- 1. 组织架构同频 (HR -> SYS)
-- ==========================================
-- 先在底层 sys_dept 插入部门数据 (使用 REPLACE 防止 ID 冲突)
REPLACE INTO `sys_dept` (`dept_id`, `parent_id`, `ancestors`, `dept_name`, `order_num`, `leader`, `phone`, `email`, `status`, `del_flag`, `create_by`, `create_time`) VALUES 
(200, 0, '0', '星辰科技集团', 1, '若依', '400-888-9999', 'admin@xckj.com', '0', '0', 'admin', sysdate()),
(201, 200, '0,200', '北京星辰科技有限公司', 1, '若依', '010-12345678', NULL, '0', '0', 'admin', sysdate()),
(202, 201, '0,200,201', '研发中心', 1, '张三', '010-12345678', NULL, '0', '0', 'admin', sysdate()),
(203, 202, '0,200,201,202', '前端开发一组', 1, '王五', '010-12345678', NULL, '0', '0', 'admin', sysdate());

-- 插入对应的 HR 组织架构数据
INSERT INTO `hr_organization` (`org_id`, `parent_id`, `ancestors`, `org_name`, `org_code`, `org_type`, `org_level`, `leader_id`, `phone`, `address`, `establish_date`, `order_num`, `status`, `sys_dept_id`, `create_by`, `create_time`) VALUES 
(1, 0, '0', '星辰科技集团', 'XCKJ', '1', 1, 1, '400-888-9999', '星辰大厦', '2010-01-01', 1, '0', 200, 'admin', sysdate()),
(2, 1, '0,1', '北京星辰科技有限公司', 'BJXC', '2', 2, 1, '010-12345678', '北京朝阳区星辰大厦', '2010-01-01', 1, '0', 201, 'admin', sysdate()),
(3, 2, '0,1,2', '研发中心', 'RD', '4', 3, 2, '010-12345678', '北京朝阳区星辰大厦研发部', '2010-01-01', 1, '0', 202, 'admin', sysdate()),
(4, 3, '0,1,2,3', '前端开发一组', 'F1', '6', 4, 4, '010-12345678', '研发区域', '2010-01-01', 1, '0', 203, 'admin', sysdate());


-- ==========================================
-- 2. 岗位同频 (HR -> SYS)
-- ==========================================
REPLACE INTO `sys_post` (`post_id`, `post_code`, `post_name`, `post_sort`, `status`, `create_by`, `create_time`) VALUES 
(210, 'FED', '前端开发工程师', 1, '0', 'admin', sysdate()),
(211, 'BED', 'Java后端开发工程师', 2, '0', 'admin', sysdate()),
(212, 'RD_D', '研发总监', 3, '0', 'admin', sysdate());

INSERT INTO `hr_position_category` (`category_id`, `category_name`, `category_code`, `description`, `status`, `create_by`, `create_time`) VALUES 
(1, '技术族', 'T', '从事技术研发、测试、运维等相关工作', '0', 'admin', sysdate()),
(2, '管理族', 'M', '从事团队管理、项目管理等工作', '0', 'admin', sysdate());

INSERT INTO `hr_position_level` (`level_id`, `category_id`, `level_name`, `level_code`, `level_rank`, `salary_min`, `salary_max`, `description`, `status`, `create_by`, `create_time`) VALUES 
(1, 1, '高级工程师 (T3)', 'T3', 3, 25000, 40000, '系统架构设计及核心研发', '0', 'admin', sysdate()),
(2, 2, '总监 (M3)', 'M3', 3, 40000, 60000, '高级管理', '0', 'admin', sysdate());

INSERT INTO `hr_position` (`position_id`, `position_name`, `position_code`, `category_id`, `org_id`, `headcount`, `current_count`, `status`, `sys_post_id`, `create_by`, `create_time`) VALUES 
(1, '前端开发工程师', 'FED', 1, 3, 10, 5, '0', 210, 'admin', sysdate()),
(2, 'Java后端开发工程师', 'BED', 1, 3, 15, 8, '0', 211, 'admin', sysdate()),
(3, '研发总监', 'RD_D', 2, 3, 1, 1, '0', 212, 'admin', sysdate());


-- ==========================================
-- 3. 用户与员工同频 (HR -> SYS)
-- ==========================================
-- 移除 user_type 字段，修正 sys_user 插入
-- 默认密码: 123456
REPLACE INTO `sys_user` (`user_id`, `dept_id`, `user_name`, `nick_name`, `email`, `phonenumber`, `sex`, `password`, `status`, `del_flag`, `login_ip`, `create_by`, `create_time`, `remark`) VALUES 
(300, 200, 'admin_hr', '人力管理员', 'ry@qq.com', '15888888888', '1', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', 'admin', sysdate(), 'HR管理员'),
(301, 202, 'zhangsan', '张三', 'zs@qq.com', '13800138000', '0', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', 'admin', sysdate(), '研发总监'),
(302, 203, 'wangwu', '王五', 'ww@qq.com', '13800138002', '0', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', 'admin', sysdate(), '普通员工');

REPLACE INTO `sys_user_post` (`user_id`, `post_id`) VALUES (301, 212), (302, 210);
REPLACE INTO `sys_user_role` (`user_id`, `role_id`) VALUES (300, 1), (301, 2), (302, 2);

-- 插入 HR 员工主数据
INSERT INTO `hr_employee` (`employee_id`, `employee_no`, `user_id`, `name`, `gender`, `id_card`, `phone`, `email`, `org_id`, `position_id`, `level_id`, `direct_leader_id`, `employee_type`, `employee_status`, `hire_date`, `create_by`, `create_time`) VALUES 
(1, '90001', 300, '若依HR管理员', '1', '110105199001011234', '15888888888', 'ry@qq.com', 1, 3, 2, NULL, '1', '2', '2015-01-01', 'admin', sysdate()),
(2, '90002', 301, '张三', '0', '110105199201011234', '13800138000', 'zs@qq.com', 3, 3, 2, 1, '1', '2', '2016-05-01', 'admin', sysdate()),
(3, '90004', 302, '王五', '0', '110105199011011234', '13800138002', 'ww@qq.com', 4, 1, 1, 2, '1', '2', '2017-03-01', 'admin', sysdate());

SET FOREIGN_KEY_CHECKS = 1;
