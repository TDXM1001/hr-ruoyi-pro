-- e:\my-project\hr-ruoyi-pro\RuoYi-Vue\sql\hr_core_init.sql

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 组织管理
-- ----------------------------

-- 组织架构表
DROP TABLE IF EXISTS `hr_organization`;
CREATE TABLE `hr_organization` (
  `org_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '组织ID',
  `parent_id` bigint(20) DEFAULT NULL COMMENT '上级组织ID',
  `ancestors` varchar(500) DEFAULT '' COMMENT '祖级列表',
  `org_name` varchar(100) DEFAULT '' COMMENT '组织名称',
  `org_code` varchar(50) DEFAULT NULL COMMENT '组织编码',
  `org_type` char(1) DEFAULT NULL COMMENT '组织类型（1集团 2公司 3事业部 4中心 5部门 6团队）',
  `org_level` int(4) DEFAULT NULL COMMENT '组织层级',
  `leader_id` bigint(20) DEFAULT NULL COMMENT '负责人ID',
  `phone` varchar(20) DEFAULT '' COMMENT '联系电话',
  `address` varchar(200) DEFAULT '' COMMENT '办公地址',
  `establish_date` date DEFAULT NULL COMMENT '成立日期',
  `order_num` int(4) DEFAULT '0' COMMENT '排序号',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `sys_dept_id` bigint(20) DEFAULT NULL COMMENT '关联若依 sys_dept_id',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`org_id`),
  UNIQUE KEY `uk_org_code` (`org_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组织架构表';

-- 组织变更记录表
DROP TABLE IF EXISTS `hr_org_change`;
CREATE TABLE `hr_org_change` (
  `change_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '变更记录ID',
  `org_id` bigint(20) NOT NULL COMMENT '组织ID',
  `change_type` char(1) NOT NULL COMMENT '变更类型（1新设 2合并 3拆分 4撤销 5更名）',
  `change_reason` varchar(500) DEFAULT NULL COMMENT '变更原因',
  `effective_date` date DEFAULT NULL COMMENT '生效日期',
  `old_parent_id` bigint(20) DEFAULT NULL COMMENT '原上级组织',
  `new_parent_id` bigint(20) DEFAULT NULL COMMENT '新上级组织',
  `process_id` varchar(64) DEFAULT NULL COMMENT '关联审批流程实例ID',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`change_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组织变更记录表';

-- ----------------------------
-- 2. 岗位职级体系
-- ----------------------------

-- 职族表
DROP TABLE IF EXISTS `hr_position_category`;
CREATE TABLE `hr_position_category` (
  `category_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '职族ID',
  `category_name` varchar(50) NOT NULL COMMENT '职族名称',
  `category_code` varchar(30) NOT NULL COMMENT '职族编码',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`category_id`),
  UNIQUE KEY `uk_category_code` (`category_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职族表';

-- 职级表
DROP TABLE IF EXISTS `hr_position_level`;
CREATE TABLE `hr_position_level` (
  `level_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '职级ID',
  `category_id` bigint(20) NOT NULL COMMENT '所属职族ID',
  `level_name` varchar(50) NOT NULL COMMENT '职级名称',
  `level_code` varchar(30) NOT NULL COMMENT '职级编码',
  `level_rank` int(4) DEFAULT NULL COMMENT '等级序号',
  `salary_min` decimal(10,2) DEFAULT NULL COMMENT '薪资下限',
  `salary_max` decimal(10,2) DEFAULT NULL COMMENT '薪资上限',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`level_id`),
  UNIQUE KEY `uk_level_code` (`level_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职级表';

-- 岗位表
DROP TABLE IF EXISTS `hr_position`;
CREATE TABLE `hr_position` (
  `position_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '岗位ID',
  `position_name` varchar(100) NOT NULL COMMENT '岗位名称',
  `position_code` varchar(50) NOT NULL COMMENT '岗位编码',
  `category_id` bigint(20) NOT NULL COMMENT '所属职族ID',
  `org_id` bigint(20) NOT NULL COMMENT '所属组织ID',
  `headcount` int(4) DEFAULT '0' COMMENT '编制人数',
  `current_count` int(4) DEFAULT '0' COMMENT '在岗人数',
  `responsibilities` text COMMENT '岗位职责',
  `requirements` text COMMENT '任职要求',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `sys_post_id` bigint(20) DEFAULT NULL COMMENT '关联若依 sys_post_id',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`position_id`),
  UNIQUE KEY `uk_position_code` (`position_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岗位表';


-- ----------------------------
-- 3. 员工管理
-- ----------------------------

-- 员工主表
DROP TABLE IF EXISTS `hr_employee`;
CREATE TABLE `hr_employee` (
  `employee_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '员工ID',
  `employee_no` varchar(30) NOT NULL COMMENT '工号',
  `user_id` bigint(20) DEFAULT NULL COMMENT '关联系统用户ID',
  `name` varchar(50) NOT NULL COMMENT '姓名',
  `gender` char(1) DEFAULT NULL COMMENT '性别（0男 1女 2未知）',
  `id_card` varchar(18) DEFAULT NULL COMMENT '身份证号',
  `birthday` date DEFAULT NULL COMMENT '出生日期',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `photo` varchar(200) DEFAULT NULL COMMENT '照片路径',
  `org_id` bigint(20) DEFAULT NULL COMMENT '所属组织ID',
  `position_id` bigint(20) DEFAULT NULL COMMENT '岗位ID',
  `level_id` bigint(20) DEFAULT NULL COMMENT '职级ID',
  `direct_leader_id` bigint(20) DEFAULT NULL COMMENT '直属上级ID',
  `employee_type` char(1) DEFAULT NULL COMMENT '用工类型（1正式 2实习 3劳务派遣 4外包）',
  `employee_status` char(1) DEFAULT NULL COMMENT '状态（0待入职 1试用 2正式 3调岗中 4离职中 5已离职）',
  `hire_date` date DEFAULT NULL COMMENT '入职日期',
  `probation_end` date DEFAULT NULL COMMENT '试用期结束日期',
  `regular_date` date DEFAULT NULL COMMENT '转正日期',
  `leave_date` date DEFAULT NULL COMMENT '离职日期',
  `leave_reason` varchar(500) DEFAULT NULL COMMENT '离职原因',
  `work_location` varchar(200) DEFAULT NULL COMMENT '工作地点',
  `nationality` varchar(20) DEFAULT NULL COMMENT '民族',
  `native_place` varchar(100) DEFAULT NULL COMMENT '籍贯',
  `marital_status` char(1) DEFAULT NULL COMMENT '婚姻状况',
  `political` varchar(20) DEFAULT NULL COMMENT '政治面貌',
  `highest_edu` varchar(20) DEFAULT NULL COMMENT '最高学历',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`employee_id`),
  UNIQUE KEY `uk_employee_no` (`employee_no`),
  UNIQUE KEY `uk_id_card` (`id_card`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工主表';

-- 员工教育经历表
DROP TABLE IF EXISTS `hr_employee_education`;
CREATE TABLE `hr_employee_education` (
  `edu_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '教育经历ID',
  `employee_id` bigint(20) NOT NULL COMMENT '员工ID',
  `school` varchar(100) DEFAULT NULL COMMENT '学校名称',
  `major` varchar(100) DEFAULT NULL COMMENT '专业',
  `degree` varchar(20) DEFAULT NULL COMMENT '学位',
  `education` varchar(20) DEFAULT NULL COMMENT '学历（本科/硕士/博士等）',
  `start_date` date DEFAULT NULL COMMENT '开始日期',
  `end_date` date DEFAULT NULL COMMENT '结束日期',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`edu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工教育经历表';

-- 员工工作经历表
DROP TABLE IF EXISTS `hr_employee_work_history`;
CREATE TABLE `hr_employee_work_history` (
  `history_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '工作经历ID',
  `employee_id` bigint(20) NOT NULL COMMENT '员工ID',
  `company` varchar(100) DEFAULT NULL COMMENT '公司名称',
  `position` varchar(50) DEFAULT NULL COMMENT '职位',
  `start_date` date DEFAULT NULL COMMENT '开始日期',
  `end_date` date DEFAULT NULL COMMENT '结束日期',
  `leave_reason` varchar(200) DEFAULT NULL COMMENT '离职原因',
  `reference_name` varchar(50) DEFAULT NULL COMMENT '证明人',
  `reference_phone` varchar(20) DEFAULT NULL COMMENT '证明人电话',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`history_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工工作经历表';

-- 员工紧急联系人表
DROP TABLE IF EXISTS `hr_employee_contact`;
CREATE TABLE `hr_employee_contact` (
  `contact_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '联系人ID',
  `employee_id` bigint(20) NOT NULL COMMENT '员工ID',
  `name` varchar(50) NOT NULL COMMENT '姓名',
  `relation` varchar(20) DEFAULT NULL COMMENT '关系',
  `phone` varchar(20) NOT NULL COMMENT '电话',
  `address` varchar(200) DEFAULT NULL COMMENT '地址',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`contact_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工紧急联系人表';


-- ----------------------------
-- 4. 合同与异动管理
-- ----------------------------

-- 员工合同表
DROP TABLE IF EXISTS `hr_contract`;
CREATE TABLE `hr_contract` (
  `contract_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '合同ID',
  `employee_id` bigint(20) NOT NULL COMMENT '员工ID',
  `contract_no` varchar(50) NOT NULL COMMENT '合同编号',
  `contract_type` char(1) DEFAULT NULL COMMENT '类型（1固定期限 2无固定期限 3实习协议 4劳务协议）',
  `start_date` date DEFAULT NULL COMMENT '合同开始日期',
  `end_date` date DEFAULT NULL COMMENT '合同结束日期',
  `sign_date` date DEFAULT NULL COMMENT '签订日期',
  `probation_months` int(4) DEFAULT NULL COMMENT '试用期月数',
  `renewal_count` int(4) DEFAULT '0' COMMENT '续签次数',
  `salary_amount` decimal(10,2) DEFAULT NULL COMMENT '合同薪资',
  `status` char(1) DEFAULT '0' COMMENT '状态（0待签 1生效中 2已到期 3已终止）',
  `attachment` varchar(500) DEFAULT NULL COMMENT '合同附件路径',
  `process_id` varchar(64) DEFAULT NULL COMMENT '审批流程实例ID',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`contract_id`),
  UNIQUE KEY `uk_contract_no` (`contract_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工合同表';

-- 员工异动表
DROP TABLE IF EXISTS `hr_employee_change`;
CREATE TABLE `hr_employee_change` (
  `change_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '异动ID',
  `employee_id` bigint(20) NOT NULL COMMENT '员工ID',
  `change_type` char(1) NOT NULL COMMENT '异动类型（1入职 2转正 3调岗 4晋升 5降级 6离职）',
  `old_org_id` bigint(20) DEFAULT NULL COMMENT '原组织',
  `new_org_id` bigint(20) DEFAULT NULL COMMENT '新组织',
  `old_position_id` bigint(20) DEFAULT NULL COMMENT '原岗位',
  `new_position_id` bigint(20) DEFAULT NULL COMMENT '新岗位',
  `old_level_id` bigint(20) DEFAULT NULL COMMENT '原职级',
  `new_level_id` bigint(20) DEFAULT NULL COMMENT '新职级',
  `change_reason` varchar(500) DEFAULT NULL COMMENT '异动原因',
  `effective_date` date DEFAULT NULL COMMENT '生效日期',
  `process_id` varchar(64) DEFAULT NULL COMMENT '关联审批流程实例ID',
  `status` char(1) DEFAULT '0' COMMENT '状态（0审批中 1已生效 2已驳回 3已撤销）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`change_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工异动表';

SET FOREIGN_KEY_CHECKS = 1;
