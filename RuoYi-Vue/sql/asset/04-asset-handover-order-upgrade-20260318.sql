-- ----------------------------
-- 资产交接主单明细升级脚本（2026-03-18）
-- 说明：
-- 1. 用于已经执行过旧版 00/02 的数据库。
-- 2. 本脚本把交接模型从“单表单资产”升级为“主单 + 明细”。
-- 3. 不删除旧 ast_asset_handover 表，保留历史表做兼容与审计。
-- 4. 如旧表存在数据，会自动迁移为一单一资产的主单/明细数据。
-- ----------------------------

drop procedure if exists proc_ast_exec_if_missing_table;
drop procedure if exists proc_ast_migrate_handover_data;

delimiter $$

create procedure proc_ast_exec_if_missing_table(
    in p_table_name varchar(64),
    in p_ddl_sql longtext
)
begin
    if not exists (
        select 1
          from information_schema.tables
         where table_schema = database()
           and table_name = p_table_name
    ) then
        set @asset_upgrade_sql = p_ddl_sql;
        prepare stmt from @asset_upgrade_sql;
        execute stmt;
        deallocate prepare stmt;
    end if;
end$$

create procedure proc_ast_migrate_handover_data()
begin
    if exists (
        select 1
          from information_schema.tables
         where table_schema = database()
           and table_name = 'ast_asset_handover'
    ) and exists (
        select 1
          from information_schema.tables
         where table_schema = database()
           and table_name = 'ast_asset_handover_order'
    ) and exists (
        select 1
          from information_schema.tables
         where table_schema = database()
           and table_name = 'ast_asset_handover_item'
    ) then
        insert into ast_asset_handover_order (
            handover_no, asset_type, handover_type, handover_status, handover_date,
            asset_count, to_dept_id, to_user_id, location_name,
            confirm_by, confirm_time, create_by, create_time, update_by, update_time, remark
        )
        select h.handover_no,
               ifnull(nullif(a.asset_type, ''), 'FIXED') as asset_type,
               h.handover_type,
               ifnull(nullif(h.handover_status, ''), 'CONFIRMED') as handover_status,
               h.handover_date,
               1 as asset_count,
               h.to_dept_id,
               h.to_user_id,
               ifnull(h.location_name, '') as location_name,
               ifnull(h.confirm_by, '') as confirm_by,
               h.confirm_time,
               ifnull(h.create_by, '') as create_by,
               h.create_time,
               ifnull(h.update_by, '') as update_by,
               h.update_time,
               h.remark
          from ast_asset_handover h
          left join ast_asset_ledger a on h.asset_id = a.asset_id
         where not exists (
                   select 1
                     from ast_asset_handover_order o
                    where o.handover_no = h.handover_no
               );

        insert into ast_asset_handover_item (
            handover_order_id, asset_id, asset_code, asset_name,
            from_dept_id, from_user_id, from_location_name,
            to_dept_id, to_user_id, to_location_name,
            before_status, after_status, create_by, create_time, update_by, update_time, remark
        )
        select o.handover_order_id,
               h.asset_id,
               ifnull(a.asset_code, '') as asset_code,
               ifnull(a.asset_name, '') as asset_name,
               h.from_dept_id,
               h.from_user_id,
               ifnull(h.from_location_name, '') as from_location_name,
               h.to_dept_id,
               h.to_user_id,
               ifnull(h.location_name, '') as to_location_name,
               case
                   when h.handover_type = 'ASSIGN' then 'IN_LEDGER'
                   when h.handover_type = 'RETURN' then 'IN_USE'
                   else 'IN_USE'
               end as before_status,
               case
                   when h.handover_type = 'RETURN' then 'IDLE'
                   else 'IN_USE'
               end as after_status,
               ifnull(h.create_by, '') as create_by,
               h.create_time,
               ifnull(h.update_by, '') as update_by,
               h.update_time,
               h.remark
          from ast_asset_handover h
          inner join ast_asset_handover_order o on o.handover_no = h.handover_no
          left join ast_asset_ledger a on h.asset_id = a.asset_id
         where not exists (
                   select 1
                     from ast_asset_handover_item i
                    where i.handover_order_id = o.handover_order_id
                      and i.asset_id = h.asset_id
               );
    end if;
end$$

delimiter ;

call proc_ast_exec_if_missing_table(
    'ast_asset_handover_order',
    "create table ast_asset_handover_order (
        handover_order_id     bigint(20)      not null auto_increment    comment '交接主单ID',
        handover_no           varchar(64)     not null                   comment '交接单号',
        asset_type            varchar(32)     not null default 'FIXED'   comment '资产类型（FIXED固定资产 REAL_ESTATE不动产）',
        handover_type         varchar(32)     not null                   comment '交接类型（ASSIGN领用 TRANSFER调拨 RETURN退还）',
        handover_status       varchar(32)     not null default 'CONFIRMED' comment '交接状态（PENDING待确认 CONFIRMED已确认 CANCELLED已取消）',
        handover_date         date            not null                   comment '交接日期',
        asset_count           int(11)         not null default 0         comment '交接资产数量',
        to_dept_id            bigint(20)      default null               comment '目标部门ID',
        to_user_id            bigint(20)      default null               comment '目标责任人用户ID',
        location_name         varchar(200)    default ''                 comment '目标位置',
        confirm_by            varchar(64)     default ''                 comment '确认人',
        confirm_time          datetime                                   comment '确认时间',
        create_by             varchar(64)     default ''                 comment '创建者',
        create_time           datetime                                   comment '创建时间',
        update_by             varchar(64)     default ''                 comment '更新者',
        update_time           datetime                                   comment '更新时间',
        remark                varchar(500)    default null               comment '备注',
        primary key (handover_order_id),
        unique key uk_ast_asset_handover_order_no (handover_no),
        key idx_ast_asset_handover_order_type_date (handover_type, handover_date),
        key idx_ast_asset_handover_order_target_dept (to_dept_id)
    ) engine=innodb comment = '资产交接主单表'"
);

call proc_ast_exec_if_missing_table(
    'ast_asset_handover_item',
    "create table ast_asset_handover_item (
        handover_item_id      bigint(20)      not null auto_increment    comment '交接明细ID',
        handover_order_id     bigint(20)      not null                   comment '交接主单ID',
        asset_id              bigint(20)      not null                   comment '资产ID',
        asset_code            varchar(64)     not null                   comment '资产编码快照',
        asset_name            varchar(120)    not null                   comment '资产名称快照',
        from_dept_id          bigint(20)      default null               comment '交接前使用部门ID',
        from_user_id          bigint(20)      default null               comment '交接前责任人用户ID',
        from_location_name    varchar(200)    default ''                 comment '交接前位置',
        to_dept_id            bigint(20)      default null               comment '交接后使用部门ID',
        to_user_id            bigint(20)      default null               comment '交接后责任人用户ID',
        to_location_name      varchar(200)    default ''                 comment '交接后位置',
        before_status         varchar(32)     not null                   comment '交接前状态',
        after_status          varchar(32)     not null                   comment '交接后状态',
        create_by             varchar(64)     default ''                 comment '创建者',
        create_time           datetime                                   comment '创建时间',
        update_by             varchar(64)     default ''                 comment '更新者',
        update_time           datetime                                   comment '更新时间',
        remark                varchar(500)    default null               comment '备注',
        primary key (handover_item_id),
        unique key uk_ast_asset_handover_item_order_asset (handover_order_id, asset_id),
        key idx_ast_asset_handover_item_order_id (handover_order_id),
        key idx_ast_asset_handover_item_asset_id (asset_id)
    ) engine=innodb comment = '资产交接明细表'"
);

call proc_ast_migrate_handover_data();

drop procedure if exists proc_ast_exec_if_missing_table;
drop procedure if exists proc_ast_migrate_handover_data;
