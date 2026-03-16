-- 1. 审批引擎核心表
CREATE TABLE `wf_approval_template` (
  `template_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `business_type` varchar(50) NOT NULL COMMENT '业务类型(如: asset_requisition)',
  `chain_config` json DEFAULT NULL COMMENT '审批链配置JSON',
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批模板表';

CREATE TABLE `wf_approval_instance` (
  `instance_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '实例ID',
  `business_id` varchar(50) NOT NULL COMMENT '业务单据ID',
  `business_type` varchar(50) NOT NULL COMMENT '业务类型',
  `current_node` varchar(50) DEFAULT NULL COMMENT '当前审批节点',
  `status` varchar(20) DEFAULT 'pending' COMMENT '状态: pending/approved/rejected',
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批实例表';

CREATE TABLE `wf_approval_node` (
  `node_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '节点ID',
  `instance_id` bigint(20) NOT NULL COMMENT '实例ID',
  `approver_id` bigint(20) DEFAULT NULL COMMENT '审批人ID',
  `action` varchar(20) DEFAULT NULL COMMENT '操作: approve/reject/transfer',
  `comment` varchar(500) DEFAULT NULL COMMENT '审批意见',
  `process_time` datetime DEFAULT NULL,
  PRIMARY KEY (`node_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批流转记录表';


-- 2. 资产业务流水表
-- 资产领用单表
CREATE TABLE `asset_requisition` (
  `requisition_no` varchar(50) NOT NULL COMMENT '领用单号',
  `asset_id` bigint(20) NOT NULL COMMENT '资产ID',
  `asset_no` varchar(50) NOT NULL COMMENT '资产编号',
  `apply_user_id` bigint(20) NOT NULL COMMENT '申请人',
  `apply_dept_id` bigint(20) NOT NULL COMMENT '申请部门',
  `reason` varchar(500) DEFAULT NULL COMMENT '领用原因',
  `status` tinyint(4) DEFAULT '0' COMMENT '单据状态：0=审批中 1=已通过 2=已驳回 3=已归还',
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`requisition_no`),
  KEY `idx_asset_requisition_asset_id` (`asset_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产领用单表';

-- 资产采购单表
CREATE TABLE `asset_procurement` (
  `procurement_no` varchar(50) NOT NULL COMMENT '采购单号',
  `apply_user_id` bigint(20) NOT NULL COMMENT '申请人',
  `apply_dept_id` bigint(20) NOT NULL COMMENT '申请部门',
  `total_amount` decimal(10,2) DEFAULT NULL COMMENT '采购总金额',
  `reason` varchar(500) DEFAULT NULL COMMENT '采购原因',
  `status` tinyint(4) DEFAULT '0' COMMENT '单据状态：0=审批中 1=已通过 2=已驳回',
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`procurement_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产采购单表';

-- 资产调拨单表
CREATE TABLE `asset_transfer` (
  `transfer_no` varchar(50) NOT NULL COMMENT '调拨单号',
  `asset_no` varchar(50) NOT NULL COMMENT '资产编号',
  `out_dept_id` bigint(20) NOT NULL COMMENT '调出部门ID',
  `in_dept_id` bigint(20) NOT NULL COMMENT '调入部门ID',
  `apply_user_id` bigint(20) NOT NULL COMMENT '申请人',
  `reason` varchar(500) DEFAULT NULL COMMENT '调拨原因',
  `status` tinyint(4) DEFAULT '0' COMMENT '单据状态：0=审批中 1=已通过 2=已驳回',
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`transfer_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产调拨单表';

-- 资产维修单表
CREATE TABLE `asset_maintenance` (
  `maintenance_no` varchar(50) NOT NULL COMMENT '维修单号',
  `asset_id` bigint(20) NOT NULL COMMENT '资产ID',
  `asset_no` varchar(50) NOT NULL COMMENT '资产编号',
  `apply_user_id` bigint(20) NOT NULL COMMENT '报修人',
  `apply_dept_id` bigint(20) NOT NULL COMMENT '报修部门',
  `reason` varchar(500) DEFAULT NULL COMMENT '故障描述/维修原因',
  `cost` decimal(10,2) DEFAULT NULL COMMENT '预计维修费用',
  `status` tinyint(4) DEFAULT '0' COMMENT '单据状态：0=审批中 1=已通过 2=已驳回 3=维修完成',
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`maintenance_no`),
  KEY `idx_asset_maintenance_asset_id` (`asset_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产维修单表';

-- 资产处置/报废单表
-- 当前阶段通过 disposal_type 区分 scrap/sell/transfer/donate 等业务语义。
CREATE TABLE `asset_disposal` (
  `disposal_no` varchar(50) NOT NULL COMMENT '处置单号',
  `asset_id` bigint(20) NOT NULL COMMENT '资产ID',
  `asset_no` varchar(50) NOT NULL COMMENT '资产编号',
  `apply_user_id` bigint(20) NOT NULL COMMENT '申请人',
  `apply_dept_id` bigint(20) NOT NULL COMMENT '申请部门',
  `disposal_type` varchar(20) DEFAULT NULL COMMENT '处置类型（scrap=报废 sell=出售 transfer=划转 donate=捐赠）',
  `reason` varchar(500) DEFAULT NULL COMMENT '处置原因',
  `status` tinyint(4) DEFAULT '0' COMMENT '单据状态：0=审批中 1=已通过 2=已驳回',
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`disposal_no`),
  KEY `idx_asset_disposal_asset_id` (`asset_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产处置单表';
