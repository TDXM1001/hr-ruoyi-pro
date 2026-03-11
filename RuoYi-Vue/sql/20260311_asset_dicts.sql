-- 资产管理字典数据初始化
-- 资产类型字典
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
VALUES ('资产类型', 'asset_type', '0', 'admin', sysdate(), '', null, '资产类型列表');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (1, '不动产', '1', 'asset_type', '', 'primary', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (2, '固定资产', '2', 'asset_type', '', 'success', 'Y', '0', 'admin', sysdate(), '', null, '');

-- 资产状态字典
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
VALUES ('资产状态', 'asset_status', '0', 'admin', sysdate(), '', null, '资产状态列表');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (1, '正常', '1', 'asset_status', '', 'success', 'Y', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (2, '领用中', '2', 'asset_status', '', 'primary', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (3, '维修中', '3', 'asset_status', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (4, '盘点中', '4', 'asset_status', '', 'info', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (5, '已报废', '5', 'asset_status', '', 'danger', 'N', '0', 'admin', sysdate(), '', null, '');
