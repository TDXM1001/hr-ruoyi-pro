-- ----------------------------
-- 资产管理一期基础表结构
-- 设计原则：
-- 1. 固定资产按“一物一档”建模，不在主表中维护数量。
-- 2. 主档只承载资产当前态，交接、盘点、处置过程全部独立留痕。
-- 3. 同时保留权属部门、使用部门、责任人，支撑责任分离。
-- 4. 当前不建立数据库外键，沿用若依常见风格，由服务层保证数据一致性。
-- 5. 通过 asset_type 预留未来不动产扩展位，但本期默认只落固定资产。
-- 6. 对低返工、高收益的补强字段优先采用新增字段方式，不重命名既有字段。
-- ----------------------------

-- ----------------------------
-- 1、资产分类表
-- ----------------------------
drop table if exists ast_asset_category;
create table ast_asset_category (
  category_id        bigint(20)      not null auto_increment    comment '分类ID',
  parent_id          bigint(20)      default 0                  comment '父分类ID',
  ancestors          varchar(500)    default ''                 comment '祖级列表',
  category_code      varchar(64)     not null                   comment '分类编码',
  category_name      varchar(100)    not null                   comment '分类名称',
  asset_type         varchar(32)     not null default 'FIXED'   comment '资产类型（FIXED固定资产 REAL_ESTATE不动产）',
  order_num          int(4)          default 0                  comment '显示顺序',
  status             char(1)         default '0'                comment '启停状态（0正常 1停用）',
  del_flag           char(1)         default '0'                comment '删除标志（0存在 2删除）',
  create_by          varchar(64)     default ''                 comment '创建者',
  create_time        datetime                                   comment '创建时间',
  update_by          varchar(64)     default ''                 comment '更新者',
  update_time        datetime                                   comment '更新时间',
  remark             varchar(500)    default null               comment '备注',
  primary key (category_id),
  unique key uk_ast_asset_category_code (asset_type, category_code),
  key idx_ast_asset_category_parent_id (parent_id)
) engine=innodb comment = '资产分类表';

-- ----------------------------
-- 2、资产台账表
-- ----------------------------
drop table if exists ast_asset_ledger;
create table ast_asset_ledger (
  asset_id               bigint(20)      not null auto_increment    comment '资产ID',
  asset_code             varchar(64)     default null               comment '资产编码（草稿可为空，正式建账后必须唯一）',
  asset_name             varchar(120)    not null                   comment '资产名称',
  asset_type             varchar(32)     not null default 'FIXED'   comment '资产类型（FIXED固定资产 REAL_ESTATE不动产）',
  category_id            bigint(20)      default null               comment '资产分类ID',
  spec_model             varchar(255)    default ''                 comment '规格型号',
  serial_no              varchar(100)    default ''                 comment '序列号/出厂编号',
  asset_status           varchar(32)     not null default 'DRAFT'   comment '资产状态（DRAFT草稿 IN_LEDGER在册 IN_USE使用中 IDLE闲置中 INVENTORYING盘点中 PENDING_DISPOSAL待处置 DISPOSED已处置）',
  source_type            varchar(32)     not null default 'MANUAL'  comment '录入来源（MANUAL手工新增 IMPORT存量补录）',
  acquire_type           varchar(32)     default null               comment '取得方式（PURCHASE购置 ALLOCATE_IN调拨转入 DONATION捐赠 SELF_BUILT自建 INVENTORY_PROFIT盘盈 OTHER其他）',
  owner_dept_id          bigint(20)      default null               comment '权属部门ID',
  use_dept_id            bigint(20)      default null               comment '使用部门ID',
  responsible_user_id    bigint(20)      default null               comment '责任人用户ID',
  location_name          varchar(200)    default ''                 comment '当前位置',
  original_value         decimal(18,2)   default null               comment '资产原值',
  acquisition_date       date                                       comment '取得日期',
  enable_date            date                                       comment '启用日期',
  last_inventory_date    date                                       comment '最近盘点日期',
  del_flag               char(1)         default '0'                comment '删除标志（0存在 2删除）',
  create_by              varchar(64)     default ''                 comment '创建者',
  create_time            datetime                                   comment '创建时间',
  update_by              varchar(64)     default ''                 comment '更新者',
  update_time            datetime                                   comment '更新时间',
  remark                 varchar(500)    default null               comment '备注',
  primary key (asset_id),
  unique key uk_ast_asset_ledger_code (asset_code),
  key idx_ast_asset_ledger_status (asset_status),
  key idx_ast_asset_ledger_category_id (category_id),
  key idx_ast_asset_ledger_owner_dept_id (owner_dept_id),
  key idx_ast_asset_ledger_use_dept_id (use_dept_id),
  key idx_ast_asset_ledger_responsible_user_id (responsible_user_id),
  key idx_ast_asset_ledger_last_inventory_date (last_inventory_date)
) engine=innodb comment = '资产台账表';

-- ----------------------------
-- 3、资产变更日志表
-- ----------------------------
drop table if exists ast_asset_change_log;
create table ast_asset_change_log (
  log_id                bigint(20)      not null auto_increment    comment '日志ID',
  asset_id              bigint(20)      not null                   comment '资产ID',
  biz_type              varchar(32)     not null                   comment '业务类型',
  biz_id                bigint(20)      default null               comment '业务单据ID',
  before_status         varchar(32)     default null               comment '变更前状态',
  after_status          varchar(32)     default null               comment '变更后状态',
  operate_by            varchar(64)     default ''                 comment '操作人',
  operate_time          datetime        not null                   comment '操作时间',
  change_desc           varchar(500)    default null               comment '变更说明',
  remark                varchar(500)    default null               comment '备注',
  primary key (log_id),
  key idx_ast_asset_change_log_asset_time (asset_id, operate_time),
  key idx_ast_asset_change_log_biz_type (biz_type)
) engine=innodb comment = '资产变更日志表';

-- ----------------------------
-- 4、资产交接记录表
-- ----------------------------
drop table if exists ast_asset_handover;
create table ast_asset_handover (
  handover_id           bigint(20)      not null auto_increment    comment '交接记录ID',
  handover_no           varchar(64)     not null                   comment '交接单号',
  asset_id              bigint(20)      not null                   comment '资产ID',
  handover_type         varchar(32)     not null                   comment '交接类型（ASSIGN领用 TRANSFER调拨 RETURN退还）',
  from_dept_id          bigint(20)      default null               comment '原部门ID',
  from_user_id          bigint(20)      default null               comment '原责任人用户ID',
  from_location_name    varchar(200)    default ''                 comment '交接前位置',
  to_dept_id            bigint(20)      default null               comment '目标部门ID',
  to_user_id            bigint(20)      default null               comment '目标责任人用户ID',
  handover_status       varchar(32)     not null default 'CONFIRMED' comment '交接状态（PENDING待确认 CONFIRMED已确认 CANCELLED已取消）',
  handover_date         date            not null                   comment '交接日期',
  location_name         varchar(200)    default ''                 comment '交接后位置',
  confirm_by            varchar(64)     default ''                 comment '确认人',
  confirm_time          datetime                                   comment '确认时间',
  create_by             varchar(64)     default ''                 comment '创建者',
  create_time           datetime                                   comment '创建时间',
  update_by             varchar(64)     default ''                 comment '更新者',
  update_time           datetime                                   comment '更新时间',
  remark                varchar(500)    default null               comment '备注',
  primary key (handover_id),
  unique key uk_ast_asset_handover_no (handover_no),
  key idx_ast_asset_handover_asset_id (asset_id),
  key idx_ast_asset_handover_type_date (handover_type, handover_date)
) engine=innodb comment = '资产交接记录表';

-- ----------------------------
-- 5、资产盘点任务表
-- ----------------------------
drop table if exists ast_asset_inventory_task;
create table ast_asset_inventory_task (
  task_id               bigint(20)      not null auto_increment    comment '盘点任务ID',
  task_no               varchar(64)     not null                   comment '盘点任务编号',
  task_name             varchar(120)    not null                   comment '盘点任务名称',
  task_status           varchar(32)     not null default 'DRAFT'   comment '任务状态（DRAFT草稿 IN_PROGRESS进行中 COMPLETED已完成 CANCELLED已取消）',
  scope_type            varchar(32)     not null                   comment '盘点范围类型（ALL全部 CATEGORY按分类 DEPT按部门 ASSET按资产）',
  scope_value           varchar(500)    default ''                 comment '盘点范围值',
  planned_date          date            not null                   comment '计划盘点日期',
  completed_date        date                                       comment '完成日期',
  create_by             varchar(64)     default ''                 comment '创建者',
  create_time           datetime                                   comment '创建时间',
  update_by             varchar(64)     default ''                 comment '更新者',
  update_time           datetime                                   comment '更新时间',
  remark                varchar(500)    default null               comment '备注',
  primary key (task_id),
  unique key uk_ast_asset_inventory_task_no (task_no),
  key idx_ast_asset_inventory_task_status (task_status)
) engine=innodb comment = '资产盘点任务表';

-- ----------------------------
-- 6、资产盘点明细表
-- ----------------------------
drop table if exists ast_asset_inventory_item;
create table ast_asset_inventory_item (
  item_id                   bigint(20)      not null auto_increment    comment '盘点明细ID',
  task_id                   bigint(20)      not null                   comment '盘点任务ID',
  asset_id                  bigint(20)      not null                   comment '资产ID',
  inventory_result          varchar(32)     not null                   comment '盘点结果（NORMAL正常 PROFIT盘盈 LOSS盘亏 DAMAGED毁损 LOCATION_DIFF位置差异 RESPONSIBLE_DIFF责任差异）',
  actual_location_name      varchar(200)    default ''                 comment '盘点确认后位置',
  actual_use_dept_id        bigint(20)      default null               comment '盘点确认后使用部门ID',
  actual_responsible_user_id bigint(20)     default null               comment '盘点确认后责任人用户ID',
  follow_up_action          varchar(32)     not null default 'NONE'    comment '后续动作（NONE无动作 UPDATE_LEDGER修正台账 CREATE_DISPOSAL发起处置）',
  process_status            varchar(32)     not null default 'PENDING' comment '处理状态（PENDING待处理 PROCESSED已处理）',
  process_time              datetime                                   comment '处理时间',
  follow_up_biz_id          bigint(20)      default null               comment '后续业务单据ID',
  checked_by                varchar(64)     default ''                 comment '盘点人',
  checked_time              datetime                                   comment '盘点时间',
  result_desc               varchar(500)    default null               comment '结果说明',
  remark                    varchar(500)    default null               comment '备注',
  primary key (item_id),
  unique key uk_ast_asset_inventory_item_task_asset (task_id, asset_id),
  key idx_ast_asset_inventory_item_asset_id (asset_id)
) engine=innodb comment = '资产盘点明细表';

-- ----------------------------
-- 7、资产处置表
-- ----------------------------
drop table if exists ast_asset_disposal;
create table ast_asset_disposal (
  disposal_id            bigint(20)      not null auto_increment    comment '处置单ID',
  disposal_no            varchar(64)     not null                   comment '处置单号',
  asset_id               bigint(20)      not null                   comment '资产ID',
  disposal_type          varchar(32)     not null                   comment '处置类型（SCRAP报废 SALE出售 TRANSFER_OUT调出）',
  disposal_status        varchar(32)     not null default 'PENDING' comment '处置状态（PENDING待确认 CONFIRMED已处置 CANCELLED已取消）',
  disposal_reason        varchar(255)    not null                   comment '处置原因',
  disposal_date          date            not null                   comment '处置日期',
  disposal_amount        decimal(18,2)   default null               comment '处置金额/回收金额',
  confirmed_by           varchar(64)     default ''                 comment '处置确认人',
  confirmed_time         datetime                                   comment '处置确认时间',
  finance_confirm_flag   char(1)         default '0'                comment '财务确认标志（0未确认 1已确认）',
  finance_confirm_by     varchar(64)     default ''                 comment '财务确认人',
  finance_confirm_time   datetime                                   comment '财务确认时间',
  create_by              varchar(64)     default ''                 comment '创建者',
  create_time            datetime                                   comment '创建时间',
  update_by              varchar(64)     default ''                 comment '更新者',
  update_time            datetime                                   comment '更新时间',
  remark                 varchar(500)    default null               comment '备注',
  primary key (disposal_id),
  unique key uk_ast_asset_disposal_no (disposal_no),
  key idx_ast_asset_disposal_asset_id (asset_id),
  key idx_ast_asset_disposal_status (disposal_status)
) engine=innodb comment = '资产处置表';
