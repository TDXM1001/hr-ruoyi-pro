-- ----------------------------
-- 不动产巡检点测样例修复与待整改入口补丁（2026-03-21）
-- 说明：
-- 1. 修复本地点测巡检样例中已出现的乱码任务名称、结果说明。
-- 2. 额外补 1 条“异常但未发起整改”的巡检样例，方便资产管理员直接看到“发起整改”按钮。
-- 3. 脚本保持幂等，可重复执行。
-- ----------------------------

-- 修复已存在点测样例的任务名称与结果说明
update ast_asset_inventory_task
set task_name = '不动产B座门厅巡检',
    task_status = 'COMPLETED',
    scope_type = 'ASSET',
    scope_value = '20001',
    planned_date = '2026-03-21',
    completed_date = '2026-03-21',
    update_by = 'admin',
    update_time = sysdate(),
    remark = '不动产巡检点测样例：已闭环整改'
where task_no = 'IV-2026-9001';

update ast_asset_inventory_item
set result_desc = '巡检发现门厅门禁异常，需要维修并复核。',
    checked_by = 'admin',
    checked_time = '2026-03-21 11:56:34',
    remark = '不动产巡检点测样例：已闭环整改'
where item_id = 6;

update ast_asset_inventory_task
set task_name = '不动产B座二次巡检',
    task_status = 'COMPLETED',
    scope_type = 'ASSET',
    scope_value = '20001',
    planned_date = '2026-03-21',
    completed_date = '2026-03-21',
    update_by = 'admin',
    update_time = sysdate(),
    remark = '不动产巡检点测样例：整改完成页'
where task_no = 'IV-2026-9002';

update ast_asset_inventory_item
set result_desc = '整改完成页点测：发现门厅门禁异常，需要发起整改并完成验收。',
    checked_by = 'admin',
    checked_time = '2026-03-21 21:43:48',
    remark = '不动产巡检点测样例：整改完成页'
where item_id = 7;

-- 新增一条待发起整改的巡检样例，便于资产管理员直接点测“发起整改”入口
insert into ast_asset_inventory_task (
  task_id, task_no, task_name, task_status, scope_type, scope_value, planned_date, completed_date,
  create_by, create_time, update_by, update_time, remark
)
select
  9, 'IV-2026-9003', '不动产B座三次巡检', 'COMPLETED', 'ASSET', '20001', '2026-03-22', '2026-03-22',
  'admin', sysdate(), 'admin', sysdate(), '不动产巡检点测样例：待发起整改'
from dual
where not exists (
  select 1 from ast_asset_inventory_task where task_id = 9 or task_no = 'IV-2026-9003'
);

insert into ast_asset_inventory_item (
  item_id, task_id, asset_id, inventory_result, actual_location_name, actual_use_dept_id, actual_responsible_user_id,
  follow_up_action, process_status, process_time, follow_up_biz_id, checked_by, checked_time, result_desc, remark
)
select
  8, 9, 20001, 'DAMAGED', '深圳南山研发园区A座', 103, 1,
  'UPDATE_LEDGER', 'PENDING', null, null, 'admin', '2026-03-22 10:30:00',
  '巡检发现门厅门禁再次松动，当前尚未发起整改。', '不动产巡检点测样例：待发起整改'
from dual
where not exists (
  select 1 from ast_asset_inventory_item where item_id = 8
);
