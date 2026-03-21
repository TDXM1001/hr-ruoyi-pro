-- =============================================
-- 不动产占用管理最小闭环
-- 1. 新增不动产占用单表，承接发起占用/变更占用/释放占用
-- 2. 第一批仅落后端闭环，不含前端页面和样例数据
-- =============================================

create table if not exists ast_asset_real_estate_occupancy_order (
  occupancy_id         bigint(20)      not null auto_increment comment '占用单ID',
  occupancy_no         varchar(64)     not null               comment '占用单号',
  asset_id             bigint(20)      not null               comment '资产ID',
  use_dept_id          bigint(20)      not null               comment '使用部门ID',
  responsible_user_id  bigint(20)      not null               comment '责任人ID',
  location_name        varchar(255)    default null           comment '使用位置',
  start_date           date            not null               comment '占用开始日期',
  end_date             date            default null           comment '占用结束日期',
  occupancy_status     varchar(32)     not null default 'ACTIVE' comment '占用状态（ACTIVE有效 RELEASED已释放）',
  change_reason        varchar(500)    default null           comment '发起/变更原因',
  release_reason       varchar(500)    default null           comment '释放原因',
  create_by            varchar(64)     default ''             comment '创建者',
  create_time          datetime        default current_timestamp comment '创建时间',
  update_by            varchar(64)     default ''             comment '更新者',
  update_time          datetime        default null on update current_timestamp comment '更新时间',
  del_flag             char(1)         not null default '0'   comment '删除标志（0存在 2删除）',
  remark               varchar(500)    default null           comment '备注',
  primary key (occupancy_id),
  unique key uk_ast_re_occupancy_no (occupancy_no),
  key idx_ast_re_occupancy_asset_id (asset_id),
  key idx_ast_re_occupancy_status (occupancy_status),
  key idx_ast_re_occupancy_asset_status (asset_id, occupancy_status)
) engine=innodb default charset=utf8mb4 comment='不动产占用单';
