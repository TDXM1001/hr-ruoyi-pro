-- =============================================
-- 不动产占用管理前端点测样例
-- 1. 为 asset_id=20001 补 1 条有效占用样例
-- 2. 同步资产主档当前使用快照
-- 3. 脚本可重复执行
-- =============================================

insert into ast_asset_real_estate_occupancy_order (
  occupancy_no,
  asset_id,
  use_dept_id,
  responsible_user_id,
  location_name,
  start_date,
  occupancy_status,
  change_reason,
  create_by,
  create_time,
  del_flag,
  remark
)
select
  'OCC-2026-9001',
  20001,
  103,
  1,
  '深圳南山科技园A座',
  '2026-03-22',
  'ACTIVE',
  '用于占用前端闭环点测',
  'codex',
  now(),
  '0',
  '占用前端闭环点测样例'
from dual
where exists (
  select 1
  from ast_asset_ledger
  where asset_id = 20001
)
and not exists (
  select 1
  from ast_asset_real_estate_occupancy_order
  where occupancy_no = 'OCC-2026-9001'
    and del_flag = '0'
)
and not exists (
  select 1
  from ast_asset_real_estate_occupancy_order
  where asset_id = 20001
    and occupancy_status = 'ACTIVE'
    and del_flag = '0'
);

update ast_asset_ledger
set use_dept_id = (
      select o.use_dept_id
      from ast_asset_real_estate_occupancy_order o
      where o.asset_id = 20001
        and o.occupancy_status = 'ACTIVE'
        and o.del_flag = '0'
      order by o.occupancy_id desc
      limit 1
    ),
    responsible_user_id = (
      select o.responsible_user_id
      from ast_asset_real_estate_occupancy_order o
      where o.asset_id = 20001
        and o.occupancy_status = 'ACTIVE'
        and o.del_flag = '0'
      order by o.occupancy_id desc
      limit 1
    ),
    location_name = (
      select o.location_name
      from ast_asset_real_estate_occupancy_order o
      where o.asset_id = 20001
        and o.occupancy_status = 'ACTIVE'
        and o.del_flag = '0'
      order by o.occupancy_id desc
      limit 1
    ),
    asset_status = 'IN_USE',
    update_by = 'codex',
    update_time = now()
where asset_id = 20001
  and exists (
    select 1
    from ast_asset_real_estate_occupancy_order o
    where o.asset_id = 20001
      and o.occupancy_status = 'ACTIVE'
      and o.del_flag = '0'
  );