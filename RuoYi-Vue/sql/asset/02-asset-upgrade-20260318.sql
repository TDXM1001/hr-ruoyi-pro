-- ----------------------------
-- 固定资产一期增量升级脚本（2026-03-18）
-- 说明：
-- 1. 用于已经执行过旧版 00-asset-schema.sql 的数据库。
-- 2. 本脚本只补结构字段和索引，不删表、不清理历史数据。
-- 3. 执行完成后，再执行 01-asset-seed.sql 补菜单、字典和联调样例数据。
-- ----------------------------

drop procedure if exists proc_ast_exec_if_missing_column;
drop procedure if exists proc_ast_exec_if_missing_index;

delimiter $$

create procedure proc_ast_exec_if_missing_column(
    in p_table_name varchar(64),
    in p_column_name varchar(64),
    in p_ddl_sql longtext
)
begin
    if exists (
        select 1
          from information_schema.tables
         where table_schema = database()
           and table_name = p_table_name
    ) and not exists (
        select 1
          from information_schema.columns
         where table_schema = database()
           and table_name = p_table_name
           and column_name = p_column_name
    ) then
        set @asset_upgrade_sql = p_ddl_sql;
        prepare stmt from @asset_upgrade_sql;
        execute stmt;
        deallocate prepare stmt;
    end if;
end$$

create procedure proc_ast_exec_if_missing_index(
    in p_table_name varchar(64),
    in p_index_name varchar(64),
    in p_ddl_sql longtext
)
begin
    if exists (
        select 1
          from information_schema.tables
         where table_schema = database()
           and table_name = p_table_name
    ) and not exists (
        select 1
          from information_schema.statistics
         where table_schema = database()
           and table_name = p_table_name
           and index_name = p_index_name
    ) then
        set @asset_upgrade_sql = p_ddl_sql;
        prepare stmt from @asset_upgrade_sql;
        execute stmt;
        deallocate prepare stmt;
    end if;
end$$

delimiter ;

-- ----------------------------
-- 1. 资产台账表补强
-- ----------------------------
call proc_ast_exec_if_missing_column(
    'ast_asset_ledger',
    'acquire_type',
    "alter table ast_asset_ledger add column acquire_type varchar(32) default null comment '取得方式（PURCHASE购置 ALLOCATE_IN调拨转入 DONATION捐赠 SELF_BUILT自建 INVENTORY_PROFIT盘盈 OTHER其他）' after source_type"
);

call proc_ast_exec_if_missing_index(
    'ast_asset_ledger',
    'idx_ast_asset_ledger_owner_dept_id',
    'alter table ast_asset_ledger add key idx_ast_asset_ledger_owner_dept_id (owner_dept_id)'
);

-- ----------------------------
-- 2. 资产交接表补强
-- ----------------------------
call proc_ast_exec_if_missing_column(
    'ast_asset_handover',
    'from_location_name',
    "alter table ast_asset_handover add column from_location_name varchar(200) default '' comment '交接前位置' after from_user_id"
);

call proc_ast_exec_if_missing_column(
    'ast_asset_handover',
    'confirm_by',
    "alter table ast_asset_handover add column confirm_by varchar(64) default '' comment '确认人' after location_name"
);

call proc_ast_exec_if_missing_column(
    'ast_asset_handover',
    'confirm_time',
    "alter table ast_asset_handover add column confirm_time datetime comment '确认时间' after confirm_by"
);

-- ----------------------------
-- 3. 资产盘点明细表补强
-- ----------------------------
call proc_ast_exec_if_missing_column(
    'ast_asset_inventory_item',
    'follow_up_action',
    "alter table ast_asset_inventory_item add column follow_up_action varchar(32) not null default 'NONE' comment '后续动作（NONE无动作 UPDATE_LEDGER修正台账 CREATE_DISPOSAL发起处置）' after actual_responsible_user_id"
);

call proc_ast_exec_if_missing_column(
    'ast_asset_inventory_item',
    'process_status',
    "alter table ast_asset_inventory_item add column process_status varchar(32) not null default 'PENDING' comment '处理状态（PENDING待处理 PROCESSED已处理）' after follow_up_action"
);

call proc_ast_exec_if_missing_column(
    'ast_asset_inventory_item',
    'process_time',
    "alter table ast_asset_inventory_item add column process_time datetime comment '处理时间' after process_status"
);

call proc_ast_exec_if_missing_column(
    'ast_asset_inventory_item',
    'follow_up_biz_id',
    "alter table ast_asset_inventory_item add column follow_up_biz_id bigint(20) default null comment '后续业务单据ID' after process_time"
);

-- ----------------------------
-- 4. 资产处置表补强
-- ----------------------------
call proc_ast_exec_if_missing_column(
    'ast_asset_disposal',
    'confirmed_by',
    "alter table ast_asset_disposal add column confirmed_by varchar(64) default '' comment '处置确认人' after disposal_amount"
);

call proc_ast_exec_if_missing_column(
    'ast_asset_disposal',
    'confirmed_time',
    "alter table ast_asset_disposal add column confirmed_time datetime comment '处置确认时间' after confirmed_by"
);

call proc_ast_exec_if_missing_column(
    'ast_asset_disposal',
    'finance_confirm_by',
    "alter table ast_asset_disposal add column finance_confirm_by varchar(64) default '' comment '财务确认人' after finance_confirm_flag"
);

call proc_ast_exec_if_missing_column(
    'ast_asset_disposal',
    'finance_confirm_time',
    "alter table ast_asset_disposal add column finance_confirm_time datetime comment '财务确认时间' after finance_confirm_by"
);

drop procedure if exists proc_ast_exec_if_missing_column;
drop procedure if exists proc_ast_exec_if_missing_index;
