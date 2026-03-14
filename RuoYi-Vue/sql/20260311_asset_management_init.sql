CREATE TABLE `asset_category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `parent_id` bigint(20) DEFAULT '0' COMMENT '父节点ID',
  `name` varchar(50) NOT NULL COMMENT '分类名称',
  `code` varchar(50) DEFAULT NULL COMMENT '分类编码',
  `level` int(11) DEFAULT NULL COMMENT '层级',
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产分类表';

CREATE TABLE `asset_info` (
  `asset_no` varchar(50) NOT NULL COMMENT '资产编号',
  `asset_name` varchar(100) NOT NULL COMMENT '资产名称',
  `category_id` bigint(20) NOT NULL COMMENT '分类ID',
  `asset_type` char(1) NOT NULL COMMENT '类型：1=不动产 2=固定资产',
  `dept_id` bigint(20) DEFAULT NULL COMMENT '归属部门',
  `user_id` bigint(20) DEFAULT NULL COMMENT '责任人',
  `status` char(1) DEFAULT '1' COMMENT '状态：1=正常 2=领用中 3=维修中 4=盘点中 5=已报废',
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`asset_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产主表';
