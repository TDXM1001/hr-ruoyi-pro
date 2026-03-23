-- =============================================
-- 不动产处置点测样例
-- 1. 为 asset_id=20002 补一条已确认处置记录
-- 2. 同步补齐发起/提交审批/审批通过/确认处置生命周期日志
-- 3. 同步资产主档状态为 DISPOSED，便于详情壳点测回看
-- 4. 脚本可重复执行
-- =============================================

insert into ast_asset_disposal (
  disposal_no,
  asset_id,
  disposal_type,
  disposal_status,
  disposal_reason,
  disposal_date,
  disposal_amount,
  confirmed_by,
  confirmed_time,
  finance_confirm_flag,
  finance_confirm_by,
  finance_confirm_time,
  create_by,
  create_time,
  update_by,
  update_time,
  remark
)
select
  'DIS-2026-9001',
  20002,
  'SCRAP',
  'CONFIRMED',
  '楼宇局部区域调整，样例资产转入处置闭环点测',
  '2026-03-23',
  0.00,
  '资产经理',
  '2026-03-23 11:00:00',
  '1',
  'finance-admin',
  '2026-03-23 10:55:00',
  'codex',
  now(),
  'codex',
  now(),
  '不动产处置最小闭环浏览器点测样例'
from dual
where exists (
  select 1 from ast_asset_ledger where asset_id = 20002 and del_flag = '0'
)
and not exists (
  select 1 from ast_asset_disposal where disposal_no = 'DIS-2026-9001'
);

update ast_asset_disposal
set disposal_status = 'CONFIRMED',
    disposal_type = 'SCRAP',
    disposal_reason = '楼宇局部区域调整，样例资产转入处置闭环点测',
    disposal_date = '2026-03-23',
    disposal_amount = 0.00,
    confirmed_by = '资产经理',
    confirmed_time = '2026-03-23 11:00:00',
    finance_confirm_flag = '1',
    finance_confirm_by = 'finance-admin',
    finance_confirm_time = '2026-03-23 10:55:00',
    update_by = 'codex',
    update_time = now(),
    remark = '不动产处置最小闭环浏览器点测样例'
where disposal_no = 'DIS-2026-9001';

update ast_asset_ledger
set asset_status = 'DISPOSED',
    update_by = 'codex',
    update_time = now(),
    remark = '不动产处置最小闭环浏览器点测样例'
where asset_id = 20002;

insert into ast_asset_change_log (
  log_id,
  asset_id,
  biz_type,
  biz_id,
  before_status,
  after_status,
  operate_by,
  operate_time,
  change_desc,
  remark
)
select
  21001,
  20002,
  'DISPOSAL_APPLY',
  (select disposal_id from ast_asset_disposal where disposal_no = 'DIS-2026-9001' limit 1),
  'IN_USE',
  'PENDING_DISPOSAL',
  'asset-admin',
  '2026-03-23 09:00:00',
  '发起处置：DIS-2026-9001，原因：楼宇局部区域调整，拟转入报废口径',
  '不动产处置最小闭环浏览器点测样例'
from dual
where exists (
  select 1 from ast_asset_disposal where disposal_no = 'DIS-2026-9001'
)
and not exists (
  select 1 from ast_asset_change_log where log_id = 21001
);

insert into ast_asset_change_log (
  log_id,
  asset_id,
  biz_type,
  biz_id,
  before_status,
  after_status,
  operate_by,
  operate_time,
  change_desc,
  remark
)
select
  21002,
  20002,
  'DISPOSAL_APPLY',
  (select disposal_id from ast_asset_disposal where disposal_no = 'DIS-2026-9001' limit 1),
  'PENDING_DISPOSAL',
  'PENDING_DISPOSAL',
  'asset-admin',
  '2026-03-23 09:20:00',
  '提交处置审批：DIS-2026-9001，意见：样例资产已满足处置条件，申请进入审批流程',
  '不动产处置最小闭环浏览器点测样例'
from dual
where exists (
  select 1 from ast_asset_disposal where disposal_no = 'DIS-2026-9001'
)
and not exists (
  select 1 from ast_asset_change_log where log_id = 21002
);

insert into ast_asset_change_log (
  log_id,
  asset_id,
  biz_type,
  biz_id,
  before_status,
  after_status,
  operate_by,
  operate_time,
  change_desc,
  remark
)
select
  21003,
  20002,
  'DISPOSAL_APPLY',
  (select disposal_id from ast_asset_disposal where disposal_no = 'DIS-2026-9001' limit 1),
  'PENDING_DISPOSAL',
  'PENDING_DISPOSAL',
  'auditor',
  '2026-03-23 10:00:00',
  '处置审批通过：DIS-2026-9001，意见：同意按报废口径办理',
  '不动产处置最小闭环浏览器点测样例'
from dual
where exists (
  select 1 from ast_asset_disposal where disposal_no = 'DIS-2026-9001'
)
and not exists (
  select 1 from ast_asset_change_log where log_id = 21003
);

insert into ast_asset_change_log (
  log_id,
  asset_id,
  biz_type,
  biz_id,
  before_status,
  after_status,
  operate_by,
  operate_time,
  change_desc,
  remark
)
select
  21004,
  20002,
  'DISPOSAL_CONFIRM',
  (select disposal_id from ast_asset_disposal where disposal_no = 'DIS-2026-9001' limit 1),
  'PENDING_DISPOSAL',
  'DISPOSED',
  '资产经理',
  '2026-03-23 11:00:00',
  '确认处置：DIS-2026-9001，结果：已完成报废出清并归档留痕',
  '不动产处置最小闭环浏览器点测样例'
from dual
where exists (
  select 1 from ast_asset_disposal where disposal_no = 'DIS-2026-9001'
)
and not exists (
  select 1 from ast_asset_change_log where log_id = 21004
);