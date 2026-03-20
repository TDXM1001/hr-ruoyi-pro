-- 二期不动产扩展档案表（最小基线）
-- 用途：在统一资产台账之外承载不动产权属补充信息，便于资产管理员核查。

create table if not exists ast_asset_real_estate_profile (
    profile_id         bigint(20)      not null auto_increment comment '档案ID',
    asset_id           bigint(20)      not null comment '资产ID',
    ownership_cert_no  varchar(64)     default null comment '权属证号',
    land_use_type      varchar(64)     default null comment '土地用途',
    building_area      decimal(14,2)   default null comment '建筑面积',
    create_by          varchar(64)     default '' comment '创建者',
    create_time        datetime        default current_timestamp comment '创建时间',
    update_by          varchar(64)     default '' comment '更新者',
    update_time        datetime        default current_timestamp on update current_timestamp comment '更新时间',
    remark             varchar(500)    default null comment '备注',
    del_flag           char(1)         default '0' comment '删除标记（0存在 2删除）',
    primary key (profile_id),
    unique key uk_ast_real_estate_asset_id (asset_id),
    key idx_ast_real_estate_cert_no (ownership_cert_no)
) engine=innodb default charset=utf8mb4 comment='不动产权属扩展档案';

