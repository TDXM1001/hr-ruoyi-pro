-- =========================================================
-- 资产数据模型重构脚本
-- 说明：
-- 1. 本脚本依赖 20260311_asset_management_init.sql 已执行
-- 2. 本脚本用于将 asset_info 从 asset_no 主键升级为 asset_id 主键
-- 3. 本脚本同时补充财务、不动产、扩展属性、附件和折旧日志相关表
-- =========================================================

-- 一、统一资产类型编码
-- 旧编码：1=不动产 2=固定资产
-- 新编码：1=固定资产 2=不动产
UPDATE asset_info
SET asset_type = CASE asset_type
    WHEN '1' THEN '2'
    WHEN '2' THEN '1'
    ELSE asset_type
END;

-- 二、升级资产主表
ALTER TABLE `asset_info`
    MODIFY COLUMN `asset_no` varchar(64) NOT NULL COMMENT '资产编号',
    MODIFY COLUMN `asset_name` varchar(200) NOT NULL COMMENT '资产名称',
    CHANGE COLUMN `dept_id` `use_dept_id` bigint(20) DEFAULT NULL COMMENT '使用部门',
    CHANGE COLUMN `status` `asset_status` char(2) DEFAULT '1' COMMENT '资产状态：1=在用 2=领用中 3=维修中 4=盘点中 5=已报废 6=已处置 7=闲置',
    MODIFY COLUMN `asset_type` char(1) NOT NULL COMMENT '资产类型：1=固定资产 2=不动产',
    ADD COLUMN `spec_model` varchar(200) DEFAULT NULL COMMENT '规格型号' AFTER `category_id`,
    ADD COLUMN `unit` varchar(30) DEFAULT NULL COMMENT '计量单位' AFTER `spec_model`,
    ADD COLUMN `ownership_org_id` bigint(20) DEFAULT NULL COMMENT '权属组织ID' AFTER `unit`,
    ADD COLUMN `manage_dept_id` bigint(20) DEFAULT NULL COMMENT '归口管理部门ID' AFTER `ownership_org_id`,
    ADD COLUMN `responsible_user_id` bigint(20) DEFAULT NULL COMMENT '责任人ID' AFTER `user_id`,
    ADD COLUMN `location_id` bigint(20) DEFAULT NULL COMMENT '位置ID' AFTER `responsible_user_id`,
    ADD COLUMN `location_text` varchar(255) DEFAULT NULL COMMENT '位置描述' AFTER `location_id`,
    ADD COLUMN `acquire_method` char(2) DEFAULT NULL COMMENT '取得方式' AFTER `location_text`,
    ADD COLUMN `purchase_date` date DEFAULT NULL COMMENT '购置日期' AFTER `acquire_method`,
    ADD COLUMN `capitalization_date` date DEFAULT NULL COMMENT '入账日期' AFTER `purchase_date`,
    ADD COLUMN `enable_date` date DEFAULT NULL COMMENT '启用日期' AFTER `capitalization_date`,
    ADD COLUMN `del_flag` char(1) DEFAULT '0' COMMENT '删除标记（0存在 2删除）' AFTER `asset_status`,
    ADD COLUMN `remark` varchar(500) DEFAULT NULL COMMENT '备注' AFTER `del_flag`,
    ADD COLUMN `update_time` datetime DEFAULT NULL COMMENT '更新时间' AFTER `create_time`;

ALTER TABLE `asset_info`
    ADD COLUMN `asset_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '资产ID' FIRST,
    DROP PRIMARY KEY,
    ADD PRIMARY KEY (`asset_id`),
    ADD UNIQUE KEY `uk_asset_no` (`asset_no`),
    ADD KEY `idx_asset_category` (`category_id`),
    ADD KEY `idx_asset_type_status` (`asset_type`, `asset_status`),
    ADD KEY `idx_asset_use_dept` (`use_dept_id`);

UPDATE `asset_info`
SET `manage_dept_id` = `use_dept_id`
WHERE `manage_dept_id` IS NULL;

UPDATE `asset_info`
SET `responsible_user_id` = `user_id`
WHERE `responsible_user_id` IS NULL;

-- 三、补充业务流水表 asset_id 关联字段
ALTER TABLE `asset_requisition`
    ADD COLUMN `asset_id` bigint(20) DEFAULT NULL COMMENT '资产ID' AFTER `requisition_no`,
    ADD KEY `idx_asset_requisition_asset_id` (`asset_id`);

UPDATE `asset_requisition` req
LEFT JOIN `asset_info` info ON info.`asset_no` = req.`asset_no`
SET req.`asset_id` = info.`asset_id`
WHERE req.`asset_id` IS NULL;

ALTER TABLE `asset_requisition`
    MODIFY COLUMN `asset_id` bigint(20) NOT NULL COMMENT '资产ID';

ALTER TABLE `asset_maintenance`
    ADD COLUMN `asset_id` bigint(20) DEFAULT NULL COMMENT '资产ID' AFTER `maintenance_no`,
    ADD KEY `idx_asset_maintenance_asset_id` (`asset_id`);

UPDATE `asset_maintenance` maintenance
LEFT JOIN `asset_info` info ON info.`asset_no` = maintenance.`asset_no`
SET maintenance.`asset_id` = info.`asset_id`
WHERE maintenance.`asset_id` IS NULL;

ALTER TABLE `asset_maintenance`
    MODIFY COLUMN `asset_id` bigint(20) NOT NULL COMMENT '资产ID';

ALTER TABLE `asset_disposal`
    ADD COLUMN `asset_id` bigint(20) DEFAULT NULL COMMENT '资产ID' AFTER `disposal_no`,
    ADD KEY `idx_asset_disposal_asset_id` (`asset_id`);

UPDATE `asset_disposal` disposal
LEFT JOIN `asset_info` info ON info.`asset_no` = disposal.`asset_no`
SET disposal.`asset_id` = info.`asset_id`
WHERE disposal.`asset_id` IS NULL;

ALTER TABLE `asset_disposal`
    MODIFY COLUMN `asset_id` bigint(20) NOT NULL COMMENT '资产ID';

-- 四、资产财务表
CREATE TABLE `asset_finance` (
  `finance_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '财务记录ID',
  `asset_id` bigint(20) NOT NULL COMMENT '资产ID',
  `book_type` char(1) NOT NULL DEFAULT '1' COMMENT '账簿口径：1=管理账',
  `currency_code` varchar(10) NOT NULL DEFAULT 'CNY' COMMENT '币种',
  `original_value` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '原值',
  `salvage_rate` decimal(8,4) DEFAULT '0.0000' COMMENT '净残值率',
  `salvage_value` decimal(18,2) DEFAULT '0.00' COMMENT '预计净残值',
  `depreciable_value` decimal(18,2) DEFAULT '0.00' COMMENT '可折旧金额',
  `depreciation_method` char(2) DEFAULT NULL COMMENT '折旧方法',
  `useful_life_month` int(11) DEFAULT NULL COMMENT '使用年限（月）',
  `depreciation_start_date` date DEFAULT NULL COMMENT '折旧开始日期',
  `depreciation_end_date` date DEFAULT NULL COMMENT '折旧结束日期',
  `monthly_depreciation_amount` decimal(18,2) DEFAULT '0.00' COMMENT '月折旧额',
  `accumulated_depreciation` decimal(18,2) DEFAULT '0.00' COMMENT '累计折旧',
  `net_book_value` decimal(18,2) DEFAULT '0.00' COMMENT '账面净值',
  `impairment_amount` decimal(18,2) DEFAULT '0.00' COMMENT '减值准备',
  `book_value` decimal(18,2) DEFAULT '0.00' COMMENT '账面价值',
  `disposed_value` decimal(18,2) DEFAULT '0.00' COMMENT '已处置金额',
  `finance_status` char(2) DEFAULT '0' COMMENT '财务状态：0=未计提 1=计提中 2=计提完成 3=已处置',
  `last_depreciation_period` varchar(7) DEFAULT NULL COMMENT '最近计提期间，格式yyyy-MM',
  `version_no` int(11) NOT NULL DEFAULT '0' COMMENT '版本号',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`finance_id`),
  UNIQUE KEY `uk_asset_finance_asset_id` (`asset_id`),
  KEY `idx_asset_finance_status` (`finance_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产财务表';

INSERT INTO `asset_finance` (
  `asset_id`,
  `book_type`,
  `currency_code`,
  `create_time`,
  `update_time`,
  `remark`
)
SELECT
  `asset_id`,
  '1',
  'CNY',
  `create_time`,
  `update_time`,
  '历史资产默认生成财务记录，待补充财务数据'
FROM `asset_info`;

-- 五、不动产专表
CREATE TABLE `asset_real_estate` (
  `real_estate_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '不动产记录ID',
  `asset_id` bigint(20) NOT NULL COMMENT '资产ID',
  `property_cert_no` varchar(100) DEFAULT NULL COMMENT '不动产权证号',
  `legacy_cert_no` varchar(100) DEFAULT NULL COMMENT '老证号',
  `real_estate_unit_no` varchar(100) DEFAULT NULL COMMENT '不动产单元号',
  `address_full` varchar(500) DEFAULT NULL COMMENT '坐落地址',
  `land_nature` varchar(50) DEFAULT NULL COMMENT '土地性质',
  `land_use` varchar(50) DEFAULT NULL COMMENT '土地用途',
  `building_use` varchar(50) DEFAULT NULL COMMENT '房屋用途',
  `land_area` decimal(18,4) DEFAULT NULL COMMENT '土地面积',
  `shared_land_area` decimal(18,4) DEFAULT NULL COMMENT '分摊土地面积',
  `building_area` decimal(18,4) DEFAULT NULL COMMENT '建筑面积',
  `inner_area` decimal(18,4) DEFAULT NULL COMMENT '套内面积/使用面积',
  `building_structure` varchar(50) DEFAULT NULL COMMENT '建筑结构',
  `building_no` varchar(50) DEFAULT NULL COMMENT '楼栋号',
  `floor_no` varchar(50) DEFAULT NULL COMMENT '楼层',
  `room_no` varchar(50) DEFAULT NULL COMMENT '房号',
  `completion_date` date DEFAULT NULL COMMENT '竣工日期',
  `built_year` int(11) DEFAULT NULL COMMENT '建成年份',
  `land_term_start_date` date DEFAULT NULL COMMENT '土地使用起始日',
  `land_term_end_date` date DEFAULT NULL COMMENT '土地使用终止日',
  `rights_type` varchar(50) DEFAULT NULL COMMENT '权利性质',
  `rights_holder` varchar(100) DEFAULT NULL COMMENT '权属人',
  `co_ownership_type` varchar(50) DEFAULT NULL COMMENT '共有情况',
  `mortgage_status` char(1) DEFAULT '0' COMMENT '抵押状态：0=否 1=是',
  `mortgagee` varchar(100) DEFAULT NULL COMMENT '抵押权人',
  `seizure_status` char(1) DEFAULT '0' COMMENT '查封状态：0=否 1=是',
  `registration_date` date DEFAULT NULL COMMENT '登记日期',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`real_estate_id`),
  UNIQUE KEY `uk_asset_real_estate_asset_id` (`asset_id`),
  KEY `idx_asset_real_estate_cert` (`property_cert_no`),
  KEY `idx_asset_real_estate_term_end` (`land_term_end_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='不动产专表';

-- 六、分类扩展字段定义表
CREATE TABLE `asset_category_attr` (
  `attr_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '字段定义ID',
  `category_id` bigint(20) NOT NULL COMMENT '分类ID',
  `attr_code` varchar(64) NOT NULL COMMENT '字段编码',
  `attr_name` varchar(100) NOT NULL COMMENT '字段名称',
  `attr_type` varchar(30) NOT NULL COMMENT '字段组件类型',
  `data_type` varchar(30) NOT NULL COMMENT '字段数据类型',
  `is_required` char(1) NOT NULL DEFAULT '0' COMMENT '是否必填：0=否 1=是',
  `is_unique` char(1) NOT NULL DEFAULT '0' COMMENT '是否唯一：0=否 1=是',
  `is_list_display` char(1) NOT NULL DEFAULT '0' COMMENT '是否列表展示：0=否 1=是',
  `is_query_condition` char(1) NOT NULL DEFAULT '0' COMMENT '是否查询条件：0=否 1=是',
  `default_value` varchar(500) DEFAULT NULL COMMENT '默认值',
  `option_source_type` char(1) DEFAULT '1' COMMENT '选项来源：1=固定选项 2=字典 3=远程',
  `option_source` varchar(1000) DEFAULT NULL COMMENT '选项来源配置',
  `validation_rule` varchar(500) DEFAULT NULL COMMENT '校验规则',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序号',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态：0=启用 1=停用',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`attr_id`),
  UNIQUE KEY `uk_asset_category_attr_code` (`category_id`, `attr_code`),
  KEY `idx_asset_category_attr_sort` (`category_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产分类扩展字段定义表';

-- 七、分类扩展字段值表
CREATE TABLE `asset_attr_value` (
  `value_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '字段值ID',
  `asset_id` bigint(20) NOT NULL COMMENT '资产ID',
  `category_id` bigint(20) NOT NULL COMMENT '分类ID',
  `attr_id` bigint(20) NOT NULL COMMENT '字段定义ID',
  `attr_code` varchar(64) NOT NULL COMMENT '字段编码',
  `attr_value_text` varchar(2000) DEFAULT NULL COMMENT '文本值',
  `attr_value_number` decimal(18,4) DEFAULT NULL COMMENT '数值',
  `attr_value_date` datetime DEFAULT NULL COMMENT '日期值',
  `attr_value_json` text COMMENT '复杂值',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`value_id`),
  KEY `idx_asset_attr_value_asset_attr` (`asset_id`, `attr_id`),
  KEY `idx_asset_attr_value_attr_code` (`attr_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产分类扩展字段值表';

-- 八、附件表
CREATE TABLE `asset_attachment` (
  `attachment_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '附件ID',
  `asset_id` bigint(20) NOT NULL COMMENT '资产ID',
  `biz_type` varchar(30) NOT NULL COMMENT '业务类型',
  `file_name` varchar(255) NOT NULL COMMENT '文件名称',
  `file_url` varchar(500) NOT NULL COMMENT '文件地址',
  `file_size` bigint(20) DEFAULT NULL COMMENT '文件大小',
  `file_suffix` varchar(20) DEFAULT NULL COMMENT '文件后缀',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序号',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`attachment_id`),
  KEY `idx_asset_attachment_asset_id` (`asset_id`),
  KEY `idx_asset_attachment_biz_type` (`biz_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产附件表';

-- 九、折旧日志表
CREATE TABLE `asset_depreciation_log` (
  `log_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '折旧日志ID',
  `asset_id` bigint(20) NOT NULL COMMENT '资产ID',
  `period` varchar(7) NOT NULL COMMENT '折旧期间，格式yyyy-MM',
  `depreciation_amount` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '本期折旧额',
  `accumulated_depreciation` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '累计折旧',
  `net_book_value` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '账面净值',
  `book_value` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '账面价值',
  `calc_time` datetime NOT NULL COMMENT '计算时间',
  `calc_type` char(1) NOT NULL DEFAULT '1' COMMENT '计算类型：1=自动 2=补算 3=重算',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`log_id`),
  UNIQUE KEY `uk_asset_depreciation_asset_period` (`asset_id`, `period`),
  KEY `idx_asset_depreciation_period` (`period`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产折旧日志表';
