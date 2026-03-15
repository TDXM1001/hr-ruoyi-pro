-- =========================================================
-- 不动产生命周期动作单据脚本
-- 说明：
-- 1. 本脚本依赖 20260314_asset_data_model_refactor.sql 已执行
-- 2. asset_info 与 asset_real_estate 只保留当前事实，本脚本中的动作单据负责过程留痕
-- 3. 权属变更、注销/处置走审批；用途变更、状态变更先按免审批直达实现
-- =========================================================

-- 一、权属变更单
CREATE TABLE `asset_real_estate_ownership_change` (
  `ownership_change_no` varchar(50) NOT NULL COMMENT '权属变更单号',
  `asset_id` bigint(20) NOT NULL COMMENT '资产ID',
  `asset_no` varchar(64) DEFAULT NULL COMMENT '资产编号',
  `apply_user_id` bigint(20) DEFAULT NULL COMMENT '申请人',
  `apply_dept_id` bigint(20) DEFAULT NULL COMMENT '申请部门',
  `status` varchar(32) NOT NULL COMMENT '单据状态',
  `old_rights_holder` varchar(100) DEFAULT NULL COMMENT '原权属人',
  `target_rights_holder` varchar(100) DEFAULT NULL COMMENT '目标权属人',
  `old_property_cert_no` varchar(100) DEFAULT NULL COMMENT '原不动产权证号',
  `target_property_cert_no` varchar(100) DEFAULT NULL COMMENT '目标不动产权证号',
  `old_registration_date` date DEFAULT NULL COMMENT '原登记日期',
  `target_registration_date` date DEFAULT NULL COMMENT '目标登记日期',
  `reason` varchar(500) DEFAULT NULL COMMENT '变更原因',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`ownership_change_no`),
  KEY `idx_re_ownership_asset_id` (`asset_id`),
  KEY `idx_re_ownership_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='不动产权属变更单';

-- 二、用途变更单
CREATE TABLE `asset_real_estate_usage_change` (
  `usage_change_no` varchar(50) NOT NULL COMMENT '用途变更单号',
  `asset_id` bigint(20) NOT NULL COMMENT '资产ID',
  `asset_no` varchar(64) DEFAULT NULL COMMENT '资产编号',
  `apply_user_id` bigint(20) DEFAULT NULL COMMENT '申请人',
  `apply_dept_id` bigint(20) DEFAULT NULL COMMENT '申请部门',
  `status` varchar(32) NOT NULL COMMENT '单据状态',
  `old_land_use` varchar(50) DEFAULT NULL COMMENT '原土地用途',
  `target_land_use` varchar(50) DEFAULT NULL COMMENT '目标土地用途',
  `old_building_use` varchar(50) DEFAULT NULL COMMENT '原房屋用途',
  `target_building_use` varchar(50) DEFAULT NULL COMMENT '目标房屋用途',
  `reason` varchar(500) DEFAULT NULL COMMENT '变更原因',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`usage_change_no`),
  KEY `idx_re_usage_asset_id` (`asset_id`),
  KEY `idx_re_usage_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='不动产用途变更单';

-- 三、状态变更单
CREATE TABLE `asset_real_estate_status_change` (
  `status_change_no` varchar(50) NOT NULL COMMENT '状态变更单号',
  `asset_id` bigint(20) NOT NULL COMMENT '资产ID',
  `asset_no` varchar(64) DEFAULT NULL COMMENT '资产编号',
  `apply_user_id` bigint(20) DEFAULT NULL COMMENT '申请人',
  `apply_dept_id` bigint(20) DEFAULT NULL COMMENT '申请部门',
  `status` varchar(32) NOT NULL COMMENT '单据状态',
  `old_asset_status` varchar(32) DEFAULT NULL COMMENT '原资产状态',
  `target_asset_status` varchar(32) DEFAULT NULL COMMENT '目标资产状态',
  `reason` varchar(500) DEFAULT NULL COMMENT '变更原因',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`status_change_no`),
  KEY `idx_re_status_asset_id` (`asset_id`),
  KEY `idx_re_status_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='不动产状态变更单';

-- 四、注销/处置单
CREATE TABLE `asset_real_estate_disposal` (
  `disposal_no` varchar(50) NOT NULL COMMENT '注销/处置单号',
  `asset_id` bigint(20) NOT NULL COMMENT '资产ID',
  `asset_no` varchar(64) DEFAULT NULL COMMENT '资产编号',
  `apply_user_id` bigint(20) DEFAULT NULL COMMENT '申请人',
  `apply_dept_id` bigint(20) DEFAULT NULL COMMENT '申请部门',
  `status` varchar(32) NOT NULL COMMENT '单据状态',
  `old_asset_status` varchar(32) DEFAULT NULL COMMENT '原资产状态',
  `target_asset_status` varchar(32) DEFAULT NULL COMMENT '目标资产状态',
  `disposal_type` varchar(32) DEFAULT NULL COMMENT '处置类型',
  `reason` varchar(500) DEFAULT NULL COMMENT '注销/处置原因',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`disposal_no`),
  KEY `idx_re_disposal_asset_id` (`asset_id`),
  KEY `idx_re_disposal_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='不动产注销/处置单';
