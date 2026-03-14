-- =========================================================
-- 资产业务流水表 asset_id 增量修复脚本
-- 说明：
-- 1. 适用于已经执行过旧版 20260312_asset_workflow_business.sql 的存量库
-- 2. 依赖 20260314_asset_data_model_refactor.sql 已完成 asset_info.asset_id 升级
-- 3. 本脚本会为领用、维修、处置单补充 asset_id 字段并回填历史数据
-- 4. 脚本包含字段和索引存在性判断，可重复执行
-- =========================================================

-- 一、资产领用单补充 asset_id
SET @sql = (
    SELECT IF (
        EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'asset_requisition'
              AND column_name = 'asset_id'
        ),
        'SELECT ''asset_requisition.asset_id 已存在，跳过加列''',
        'ALTER TABLE `asset_requisition` ADD COLUMN `asset_id` bigint(20) DEFAULT NULL COMMENT ''资产ID'' AFTER `requisition_no`'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF (
        EXISTS (
            SELECT 1
            FROM information_schema.statistics
            WHERE table_schema = DATABASE()
              AND table_name = 'asset_requisition'
              AND index_name = 'idx_asset_requisition_asset_id'
        ),
        'SELECT ''idx_asset_requisition_asset_id 已存在，跳过建索引''',
        'ALTER TABLE `asset_requisition` ADD KEY `idx_asset_requisition_asset_id` (`asset_id`)'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE `asset_requisition` req
LEFT JOIN `asset_info` info ON info.`asset_no` = req.`asset_no`
SET req.`asset_id` = info.`asset_id`
WHERE req.`asset_id` IS NULL;

ALTER TABLE `asset_requisition`
    MODIFY COLUMN `asset_id` bigint(20) NOT NULL COMMENT '资产ID';

-- 二、资产维修单补充 asset_id
SET @sql = (
    SELECT IF (
        EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'asset_maintenance'
              AND column_name = 'asset_id'
        ),
        'SELECT ''asset_maintenance.asset_id 已存在，跳过加列''',
        'ALTER TABLE `asset_maintenance` ADD COLUMN `asset_id` bigint(20) DEFAULT NULL COMMENT ''资产ID'' AFTER `maintenance_no`'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF (
        EXISTS (
            SELECT 1
            FROM information_schema.statistics
            WHERE table_schema = DATABASE()
              AND table_name = 'asset_maintenance'
              AND index_name = 'idx_asset_maintenance_asset_id'
        ),
        'SELECT ''idx_asset_maintenance_asset_id 已存在，跳过建索引''',
        'ALTER TABLE `asset_maintenance` ADD KEY `idx_asset_maintenance_asset_id` (`asset_id`)'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE `asset_maintenance` maintenance
LEFT JOIN `asset_info` info ON info.`asset_no` = maintenance.`asset_no`
SET maintenance.`asset_id` = info.`asset_id`
WHERE maintenance.`asset_id` IS NULL;

ALTER TABLE `asset_maintenance`
    MODIFY COLUMN `asset_id` bigint(20) NOT NULL COMMENT '资产ID';

-- 三、资产处置单补充 asset_id
SET @sql = (
    SELECT IF (
        EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'asset_disposal'
              AND column_name = 'asset_id'
        ),
        'SELECT ''asset_disposal.asset_id 已存在，跳过加列''',
        'ALTER TABLE `asset_disposal` ADD COLUMN `asset_id` bigint(20) DEFAULT NULL COMMENT ''资产ID'' AFTER `disposal_no`'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF (
        EXISTS (
            SELECT 1
            FROM information_schema.statistics
            WHERE table_schema = DATABASE()
              AND table_name = 'asset_disposal'
              AND index_name = 'idx_asset_disposal_asset_id'
        ),
        'SELECT ''idx_asset_disposal_asset_id 已存在，跳过建索引''',
        'ALTER TABLE `asset_disposal` ADD KEY `idx_asset_disposal_asset_id` (`asset_id`)'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE `asset_disposal` disposal
LEFT JOIN `asset_info` info ON info.`asset_no` = disposal.`asset_no`
SET disposal.`asset_id` = info.`asset_id`
WHERE disposal.`asset_id` IS NULL;

ALTER TABLE `asset_disposal`
    MODIFY COLUMN `asset_id` bigint(20) NOT NULL COMMENT '资产ID';

-- 四、输出修复结果，便于执行后快速核对
SELECT 'asset_requisition' AS table_name, COUNT(*) AS missing_asset_id_rows
FROM `asset_requisition`
WHERE `asset_id` IS NULL
UNION ALL
SELECT 'asset_maintenance' AS table_name, COUNT(*) AS missing_asset_id_rows
FROM `asset_maintenance`
WHERE `asset_id` IS NULL
UNION ALL
SELECT 'asset_disposal' AS table_name, COUNT(*) AS missing_asset_id_rows
FROM `asset_disposal`
WHERE `asset_id` IS NULL;
