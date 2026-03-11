SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 清理 HR 业务表（这些是新表，TRUNCATE 是安全的）
-- ----------------------------
TRUNCATE TABLE `hr_employee`;
TRUNCATE TABLE `hr_position`;
TRUNCATE TABLE `hr_position_level`;
TRUNCATE TABLE `hr_position_category`;
TRUNCATE TABLE `hr_organization`;


-- ----------------------------
-- 2. 组织与部门同步 (sys_dept)
-- ----------------------------
-- 使用 INSERT IGNORE + ON DUPLICATE KEY UPDATE 确保数据存在且不报错
INSERT INTO `sys_dept` (`dept_id`, `parent_id`, `ancestors`, `dept_name`, `order_num`, `leader`, `phone`, `email`, `status`, `del_flag`, `create_by`, `create_time`) 
VALUES 
(200, 0, '0', '星辰科技集团', 1, '若依', '400-888-9999', 'admin@xckj.com', '0', '0', 'admin', sysdate()),
(201, 200, '0,200', '北京星辰科技有限公司', 1, '若依', '010-12345678', NULL, '0', '0', 'admin', sysdate())
ON DUPLICATE KEY UPDATE dept_name=VALUES(dept_name), ancestors=VALUES(ancestors);

-- 插入 HR 组织
INSERT INTO `hr_organization` (`org_id`, `parent_id`, `ancestors`, `org_name`, `org_code`, `org_type`, `org_level`, `phone`, `order_num`, `status`, `sys_dept_id`, `create_by`, `create_time`) VALUES 
(1, 0, '0', '星辰科技集团', 'XCKJ', '1', 1, '400-888-9999', 1, '0', 200, 'admin', sysdate()),
(2, 1, '0,1', '北京星辰科技有限公司', 'BJXC', '2', 2, '010-12345678', 1, '0', 201, 'admin', sysdate());


-- ----------------------------
-- 3. 职级与岗位同步 (sys_post)
-- ----------------------------
INSERT INTO `sys_post` (`post_id`, `post_code`, `post_name`, `post_sort`, `status`, `create_by`, `create_time`) 
VALUES (210, 'FED', '前端开发工程师', 1, '0', 'admin', sysdate())
ON DUPLICATE KEY UPDATE post_name=VALUES(post_name);

INSERT INTO `hr_position_category` (`category_id`, `category_name`, `category_code`, `status`, `create_by`, `create_time`) VALUES 
(1, '技术族', 'T', '0', 'admin', sysdate());

INSERT INTO `hr_position` (`position_id`, `position_name`, `position_code`, `category_id`, `org_id`, `status`, `sys_post_id`, `create_by`, `create_time`) VALUES 
(1, '前端开发工程师', 'FED', 1, 2, '0', 210, 'admin', sysdate());


-- ----------------------------
-- 4. 用户与员工同步 (sys_user)
-- ----------------------------
-- 这里包含了 SysUser.java 里的所有关键字段
INSERT INTO `sys_user` (`user_id`, `dept_id`, `user_name`, `nick_name`, `email`, `phonenumber`, `sex`, `password`, `status`, `del_flag`, `create_by`, `create_time`, `remark`, `pwd_update_date`) 
VALUES 
(302, 201, 'wangwu', '王五', 'ww@qq.com', '13800138002', '0', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', 'admin', sysdate(), '初始导入', sysdate())
ON DUPLICATE KEY UPDATE nick_name=VALUES(nick_name), dept_id=VALUES(dept_id);

-- 插入 HR 员工记录并关联 userId
INSERT INTO `hr_employee` (`employee_id`, `employee_no`, `user_id`, `name`, `gender`, `id_card`, `phone`, `email`, `org_id`, `position_id`, `employee_type`, `employee_status`, `hire_date`, `create_by`, `create_time`) VALUES 
(3, '90004', 302, '王五', '0', '110105199011011234', '13800138002', 'ww@qq.com', 2, 1, '1', '2', '2017-03-01', 'admin', sysdate());

SET FOREIGN_KEY_CHECKS = 1;

-- 刷新完成提示
-- 如果执行到这里没有报错，说明同步成功。
