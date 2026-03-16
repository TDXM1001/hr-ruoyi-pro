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

-- 五、升级备注
-- 1. 旧菜单若仍使用 repair/scrap 命名，请继续执行 20260315_asset_lifecycle_workflow_menu_patch.sql。
-- 2. 本补丁只统一治理口径，不处理“删除/归档”语义；归档语义将在后续批次单独落地。
