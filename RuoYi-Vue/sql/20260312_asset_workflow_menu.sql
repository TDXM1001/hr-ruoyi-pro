-- 审批流相关字典与菜单脚本
-- 审批流字典类型 wf_status
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
VALUES ('审批流状态', 'wf_status', '0', 'admin', sysdate(), '审批流处理状态');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES (1, '草稿', 'DRAFT', 'wf_status', '', 'default', 'Y', '0', 'admin', sysdate(), '草稿'),
(2, '流转中', 'IN_PROGRESS', 'wf_status', '', 'primary', 'N', '0', 'admin', sysdate(), '流转中'),
(3, '已完成', 'COMPLETED', 'wf_status', '', 'success', 'N', '0', 'admin', sysdate(), '已完成'),
(4, '已驳回', 'REJECTED', 'wf_status', '', 'danger', 'N', '0', 'admin', sysdate(), '已驳回'),
(5, '已取消', 'CANCELLED', 'wf_status', '', 'info', 'N', '0', 'admin', sysdate(), '已取消');

-- 业务类型字典 business_type
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
VALUES ('业务单据类型', 'business_type', '0', 'admin', sysdate(), '关联业务表的类型');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES (1, '资产领用', 'REQUISITION', 'business_type', '', 'default', 'Y', '0', 'admin', sysdate(), '资产领用'),
(2, '资产归还', 'RETURN', 'business_type', '', 'default', 'N', '0', 'admin', sysdate(), '资产归还'),
(3, '资产维修', 'REPAIR', 'business_type', '', 'default', 'N', '0', 'admin', sysdate(), '资产维修'),
(4, '资产报废', 'SCRAP', 'business_type', '', 'danger', 'N', '0', 'admin', sysdate(), '资产报废');

-- 审批中心目录
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('审批中心', 0, 5, 'workflow', '', 1, 0, 'M', '0', '0', '', 'form', 'admin', sysdate(), '审批中心目录');

SET @workflow_id = LAST_INSERT_ID();

-- 待办和已办菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('待办处理', @workflow_id, 1, 'todo', 'asset/workflow/todo/index', 1, 0, 'C', '0', '0', 'workflow:todo:list', 'peoples', 'admin', sysdate(), '待办处理菜单'),
       ('已办列表', @workflow_id, 2, 'done', 'asset/workflow/done/index', 1, 0, 'C', '0', '0', 'workflow:done:list', 'documentation', 'admin', sysdate(), '已办列表菜单');

-- 查询资产管理的父级ID
SELECT menu_id INTO @asset_parent_id FROM sys_menu WHERE menu_name = '资产系统' AND parent_id = 0 LIMIT 1;

-- 业务记录菜单 (领用归还、维保管理、报废处置)
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('领用归还', @asset_parent_id, 3, 'requisition', 'asset/requisition/index', 1, 0, 'C', '0', '0', 'asset:requisition:list', 'guide', 'admin', sysdate(), '领用归还台账'),
       ('维保管理', @asset_parent_id, 4, 'repair', 'asset/repair/index', 1, 0, 'C', '0', '0', 'asset:repair:list', 'tool', 'admin', sysdate(), '维保管理台账'),
       ('报废处置', @asset_parent_id, 5, 'scrap', 'asset/scrap/index', 1, 0, 'C', '0', '0', 'asset:scrap:list', 'validCode', 'admin', sysdate(), '报废处置台账');
