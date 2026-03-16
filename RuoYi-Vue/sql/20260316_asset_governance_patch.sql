-- =========================================================
-- 资产治理口径升级补丁
-- 适用场景：
-- 1. 数据库已执行过旧版 20260311_asset_dicts.sql，资产类型仍为 1=不动产 2=固定资产
-- 2. 资产状态字典仍使用“正常”口径，或缺少 6=已处置、7=闲置
-- 3. 数据库已执行 20260312_asset_workflow_business.sql，需要统一核对 asset_disposal.disposal_type
-- 执行前提：
-- 1. 建议已完成 20260314_asset_data_model_refactor.sql，确保 asset_info 已切换到统一字段口径
-- 2. 如菜单仍保留 repair/scrap 旧命名，请继续执行 20260315_asset_lifecycle_workflow_menu_patch.sql
-- =========================================================

-- 一、纠正资产类型字典口径：1=固定资产 2=不动产
UPDATE sys_dict_type
SET dict_name = '资产类型',
    remark = '资产类型列表（1=固定资产 2=不动产）',
    update_by = 'admin',
    update_time = sysdate()
WHERE dict_type = 'asset_type';

UPDATE sys_dict_data
SET dict_sort = 1,
    dict_label = '固定资产',
    list_class = 'success',
    is_default = 'Y',
    remark = '资产治理口径升级：1=固定资产',
    update_by = 'admin',
    update_time = sysdate()
WHERE dict_type = 'asset_type' AND dict_value = '1';

UPDATE sys_dict_data
SET dict_sort = 2,
    dict_label = '不动产',
    list_class = 'primary',
    is_default = 'N',
    remark = '资产治理口径升级：2=不动产',
    update_by = 'admin',
    update_time = sysdate()
WHERE dict_type = 'asset_type' AND dict_value = '2';

INSERT INTO sys_dict_data (
    dict_sort,
    dict_label,
    dict_value,
    dict_type,
    css_class,
    list_class,
    is_default,
    status,
    create_by,
    create_time,
    update_by,
    update_time,
    remark
)
SELECT 1, '固定资产', '1', 'asset_type', '', 'success', 'Y', '0', 'admin', sysdate(), '', null, '资产治理口径升级：1=固定资产'
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_dict_data
    WHERE dict_type = 'asset_type' AND dict_value = '1'
);

INSERT INTO sys_dict_data (
    dict_sort,
    dict_label,
    dict_value,
    dict_type,
    css_class,
    list_class,
    is_default,
    status,
    create_by,
    create_time,
    update_by,
    update_time,
    remark
)
SELECT 2, '不动产', '2', 'asset_type', '', 'primary', 'N', '0', 'admin', sysdate(), '', null, '资产治理口径升级：2=不动产'
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_dict_data
    WHERE dict_type = 'asset_type' AND dict_value = '2'
);

-- 二、补齐资产状态字典：1=在用 2=领用中 3=维修中 4=盘点中 5=已报废 6=已处置 7=闲置
UPDATE sys_dict_type
SET dict_name = '资产状态',
    remark = '资产状态列表（1=在用 2=领用中 3=维修中 4=盘点中 5=已报废 6=已处置 7=闲置）',
    update_by = 'admin',
    update_time = sysdate()
WHERE dict_type = 'asset_status';

UPDATE sys_dict_data
SET dict_sort = 1,
    dict_label = '在用',
    list_class = 'success',
    is_default = 'Y',
    remark = '资产治理口径升级：1=在用',
    update_by = 'admin',
    update_time = sysdate()
WHERE dict_type = 'asset_status' AND dict_value = '1';

UPDATE sys_dict_data
SET dict_sort = 2,
    dict_label = '领用中',
    list_class = 'primary',
    is_default = 'N',
    remark = '资产治理口径升级：2=领用中',
    update_by = 'admin',
    update_time = sysdate()
WHERE dict_type = 'asset_status' AND dict_value = '2';

UPDATE sys_dict_data
SET dict_sort = 3,
    dict_label = '维修中',
    list_class = 'warning',
    is_default = 'N',
    remark = '资产治理口径升级：3=维修中',
    update_by = 'admin',
    update_time = sysdate()
WHERE dict_type = 'asset_status' AND dict_value = '3';

UPDATE sys_dict_data
SET dict_sort = 4,
    dict_label = '盘点中',
    list_class = 'info',
    is_default = 'N',
    remark = '资产治理口径升级：4=盘点中',
    update_by = 'admin',
    update_time = sysdate()
WHERE dict_type = 'asset_status' AND dict_value = '4';

UPDATE sys_dict_data
SET dict_sort = 5,
    dict_label = '已报废',
    list_class = 'danger',
    is_default = 'N',
    remark = '资产治理口径升级：5=已报废',
    update_by = 'admin',
    update_time = sysdate()
WHERE dict_type = 'asset_status' AND dict_value = '5';

INSERT INTO sys_dict_data (
    dict_sort,
    dict_label,
    dict_value,
    dict_type,
    css_class,
    list_class,
    is_default,
    status,
    create_by,
    create_time,
    update_by,
    update_time,
    remark
)
SELECT 6, '已处置', '6', 'asset_status', '', 'danger', 'N', '0', 'admin', sysdate(), '', null, '资产治理口径升级：6=已处置'
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_dict_data
    WHERE dict_type = 'asset_status' AND dict_value = '6'
);

INSERT INTO sys_dict_data (
    dict_sort,
    dict_label,
    dict_value,
    dict_type,
    css_class,
    list_class,
    is_default,
    status,
    create_by,
    create_time,
    update_by,
    update_time,
    remark
)
SELECT 7, '闲置', '7', 'asset_status', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, '资产治理口径升级：7=闲置'
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_dict_data
    WHERE dict_type = 'asset_status' AND dict_value = '7'
);

-- 三、核对主档治理字段口径
-- 如果历史库还保留旧值，可在业务确认后参考下述规则处理：
-- asset_type：1=固定资产 2=不动产
-- asset_status：1=在用 2=领用中 3=维修中 4=盘点中 5=已报废 6=已处置 7=闲置
-- 注意：主档历史数据的最终纠偏仍以 20260314_asset_data_model_refactor.sql 为准，本补丁不重复执行翻转逻辑。

-- 四、补齐 asset_disposal.disposal_type 历史默认值
-- 旧处置流程长期把“报废/处置”混在同一入口，本补丁先将空值回填为 scrap，保持历史语义与“已报废”兼容。
UPDATE asset_disposal
SET disposal_type = 'scrap'
WHERE disposal_type IS NULL OR disposal_type = '';

-- 处置类型治理说明：
-- 1. scrap = 报废
-- 2. sell = 出售
-- 3. transfer = 划转
-- 4. donate = 捐赠
-- 如历史单据实际属于 sell/transfer/donate，请在业务核对后再二次修正 disposal_type。

-- 五、补齐审批模板与待办分派基线
SET @has_template_name := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'wf_approval_template'
      AND COLUMN_NAME = 'template_name'
);
SET @sql_add_template_name := IF(
    @has_template_name = 0,
    'ALTER TABLE `wf_approval_template` ADD COLUMN `template_name` varchar(100) NOT NULL DEFAULT '''' COMMENT ''模板名称'' AFTER `business_type`',
    'SELECT ''wf_approval_template.template_name 已存在'''
);
PREPARE stmt FROM @sql_add_template_name;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_template_approver := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'wf_approval_template'
      AND COLUMN_NAME = 'approver_id'
);
SET @sql_add_template_approver := IF(
    @has_template_approver = 0,
    'ALTER TABLE `wf_approval_template` ADD COLUMN `approver_id` bigint(20) DEFAULT NULL COMMENT ''默认审批人ID'' AFTER `template_name`',
    'SELECT ''wf_approval_template.approver_id 已存在'''
);
PREPARE stmt FROM @sql_add_template_approver;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_template_status := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'wf_approval_template'
      AND COLUMN_NAME = 'status'
);
SET @sql_add_template_status := IF(
    @has_template_status = 0,
    'ALTER TABLE `wf_approval_template` ADD COLUMN `status` char(1) NOT NULL DEFAULT ''0'' COMMENT ''状态：0=启用 1=停用'' AFTER `approver_id`',
    'SELECT ''wf_approval_template.status 已存在'''
);
PREPARE stmt FROM @sql_add_template_status;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_instance_approver := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'wf_approval_instance'
      AND COLUMN_NAME = 'approver_id'
);
SET @sql_add_instance_approver := IF(
    @has_instance_approver = 0,
    'ALTER TABLE `wf_approval_instance` ADD COLUMN `approver_id` bigint(20) DEFAULT NULL COMMENT ''当前审批人ID'' AFTER `business_type`',
    'SELECT ''wf_approval_instance.approver_id 已存在'''
);
PREPARE stmt FROM @sql_add_instance_approver;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE wf_approval_template
SET template_name = CASE business_type
    WHEN 'asset_requisition' THEN '固定资产领用审批'
    WHEN 'asset_maintenance' THEN '固定资产维修审批'
    WHEN 'asset_disposal' THEN '固定资产处置审批'
    WHEN 'asset_real_estate_ownership_change' THEN '不动产权属变更审批'
    WHEN 'asset_real_estate_disposal' THEN '不动产处置审批'
    ELSE CONCAT('审批模板-', business_type)
END
WHERE template_name IS NULL OR template_name = '';

UPDATE wf_approval_template
SET approver_id = 1
WHERE approver_id IS NULL;

UPDATE wf_approval_template
SET status = '0'
WHERE status IS NULL OR status = '';

INSERT INTO wf_approval_template (`business_type`, `template_name`, `approver_id`, `status`, `create_time`)
SELECT 'asset_requisition', '固定资产领用审批', 1, '0', sysdate()
WHERE NOT EXISTS (
    SELECT 1
    FROM wf_approval_template
    WHERE business_type = 'asset_requisition'
);

INSERT INTO wf_approval_template (`business_type`, `template_name`, `approver_id`, `status`, `create_time`)
SELECT 'asset_maintenance', '固定资产维修审批', 1, '0', sysdate()
WHERE NOT EXISTS (
    SELECT 1
    FROM wf_approval_template
    WHERE business_type = 'asset_maintenance'
);

INSERT INTO wf_approval_template (`business_type`, `template_name`, `approver_id`, `status`, `create_time`)
SELECT 'asset_disposal', '固定资产处置审批', 1, '0', sysdate()
WHERE NOT EXISTS (
    SELECT 1
    FROM wf_approval_template
    WHERE business_type = 'asset_disposal'
);

INSERT INTO wf_approval_template (`business_type`, `template_name`, `approver_id`, `status`, `create_time`)
SELECT 'asset_real_estate_ownership_change', '不动产权属变更审批', 1, '0', sysdate()
WHERE NOT EXISTS (
    SELECT 1
    FROM wf_approval_template
    WHERE business_type = 'asset_real_estate_ownership_change'
);

INSERT INTO wf_approval_template (`business_type`, `template_name`, `approver_id`, `status`, `create_time`)
SELECT 'asset_real_estate_disposal', '不动产处置审批', 1, '0', sysdate()
WHERE NOT EXISTS (
    SELECT 1
    FROM wf_approval_template
    WHERE business_type = 'asset_real_estate_disposal'
);

UPDATE wf_approval_instance i
         left join wf_approval_template t
                   on t.business_type = i.business_type
                  and t.status = '0'
SET i.approver_id = t.approver_id
WHERE i.approver_id IS NULL;

-- 六、升级备注
-- 1. 旧菜单若仍使用 repair/scrap 命名，请继续执行 20260315_asset_lifecycle_workflow_menu_patch.sql。
-- 2. 本补丁补齐审批模板基线后，待办将按 approver_id 收敛到“仅本人可见”。
-- 3. 本补丁只统一治理口径，不处理“删除/归档”语义；归档语义将在后续批次单独落地。
