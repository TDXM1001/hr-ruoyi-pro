-- ----------------------------
-- 不动产整改审批挂载位补丁（2026-03-21）
-- 说明：
-- 1. 本批只补整改域自己的审批挂载位，不接通用审批中心。
-- 2. 审批结果独立挂在整改单上，不改变整改单主状态。
-- ----------------------------

set @approval_status_exists := (
  select count(1)
  from information_schema.columns
  where table_schema = database()
    and table_name = 'ast_asset_rectification_order'
    and column_name = 'approval_status'
);
set @approval_status_sql := if(
  @approval_status_exists = 0,
  'alter table ast_asset_rectification_order add column approval_status varchar(32) not null default ''UNSUBMITTED'' comment ''审批状态'' after acceptance_remark',
  'select ''column approval_status already exists'' '
);
prepare approval_status_stmt from @approval_status_sql;
execute approval_status_stmt;
deallocate prepare approval_status_stmt;

set @approval_submitted_time_exists := (
  select count(1)
  from information_schema.columns
  where table_schema = database()
    and table_name = 'ast_asset_rectification_order'
    and column_name = 'approval_submitted_time'
);
set @approval_submitted_time_sql := if(
  @approval_submitted_time_exists = 0,
  'alter table ast_asset_rectification_order add column approval_submitted_time datetime default null comment ''提交审批时间'' after approval_status',
  'select ''column approval_submitted_time already exists'' '
);
prepare approval_submitted_time_stmt from @approval_submitted_time_sql;
execute approval_submitted_time_stmt;
deallocate prepare approval_submitted_time_stmt;

set @approval_finished_time_exists := (
  select count(1)
  from information_schema.columns
  where table_schema = database()
    and table_name = 'ast_asset_rectification_order'
    and column_name = 'approval_finished_time'
);
set @approval_finished_time_sql := if(
  @approval_finished_time_exists = 0,
  'alter table ast_asset_rectification_order add column approval_finished_time datetime default null comment ''审批完成时间'' after approval_submitted_time',
  'select ''column approval_finished_time already exists'' '
);
prepare approval_finished_time_stmt from @approval_finished_time_sql;
execute approval_finished_time_stmt;
deallocate prepare approval_finished_time_stmt;

create table if not exists ast_asset_rectification_approval_record (
  approval_record_id    bigint(20)   not null auto_increment comment '审批轨迹ID',
  rectification_id      bigint(20)   not null               comment '整改单ID',
  asset_id              bigint(20)   not null               comment '资产ID',
  approval_status       varchar(32)  not null               comment '审批状态',
  opinion               varchar(500) default null           comment '审批意见',
  operate_by            varchar(64)  default ''             comment '操作人',
  operate_time          datetime                              comment '操作时间',
  primary key (approval_record_id),
  key idx_ast_rectification_approval_rectification_id (rectification_id),
  key idx_ast_rectification_approval_asset_id (asset_id),
  key idx_ast_rectification_approval_status (approval_status)
) engine=innodb comment = '不动产整改审批轨迹表';
