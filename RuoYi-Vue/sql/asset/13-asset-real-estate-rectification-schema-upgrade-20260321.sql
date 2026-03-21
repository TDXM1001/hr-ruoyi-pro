-- ----------------------------
-- 不动产整改登记最小闭环补丁（2026-03-21）
-- 说明：
-- 1. 面向二期 M1，仅承载巡检异常后的整改登记，不接审批流。
-- 2. 通过 inventory_item_id 与盘点结果明细挂接，复用 ast_asset_inventory_item.follow_up_biz_id。
-- ----------------------------

create table if not exists ast_asset_rectification_order (
  rectification_id      bigint(20)      not null auto_increment comment '整改单ID',
  rectification_no      varchar(64)     not null               comment '整改单号',
  asset_id              bigint(20)      not null               comment '资产ID',
  task_id               bigint(20)      not null               comment '巡检任务ID',
  inventory_item_id     bigint(20)      not null               comment '盘点结果明细ID',
  rectification_status  varchar(32)     not null default 'PENDING' comment '整改状态（PENDING待整改 COMPLETED已完成）',
  issue_type            varchar(64)     not null               comment '问题类型',
  issue_desc            varchar(500)    not null               comment '问题描述',
  responsible_dept_id   bigint(20)      default null           comment '责任部门ID',
  responsible_user_id   bigint(20)      default null           comment '责任人ID',
  deadline_date         date            not null               comment '整改期限',
  completed_time        datetime                                comment '完成时间',
  create_by             varchar(64)     default ''             comment '创建者',
  create_time           datetime                                comment '创建时间',
  update_by             varchar(64)     default ''             comment '更新者',
  update_time           datetime                                comment '更新时间',
  remark                varchar(500)    default null           comment '备注',
  primary key (rectification_id),
  unique key uk_ast_asset_rectification_no (rectification_no),
  unique key uk_ast_asset_rectification_item (inventory_item_id),
  key idx_ast_asset_rectification_asset_id (asset_id),
  key idx_ast_asset_rectification_task_id (task_id),
  key idx_ast_asset_rectification_status (rectification_status)
) engine=innodb comment = '资产整改单表';
