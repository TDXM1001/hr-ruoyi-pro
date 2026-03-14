# 资产数据模型分层设计

## 背景

当前资产模块仅具备基础主表能力，`asset_info` 只承载了资产编号、名称、分类、类型、部门、责任人和状态等少量字段，难以同时支撑固定资产一物一码管理、不动产权属管理、系统内财务核算以及分类扩展字段配置。

本次设计针对以下已确认边界进行收敛：

- 固定资产采用一物一码管理
- 不动产按权属单元管理
- 财务字段在本系统内作为主数据维护和计算
- 分类专属字段由管理员在系统内配置
- 目标同时满足日常运营管理和财务核算审计

## 设计目标

- 建立统一资产主档，承接固定资产和不动产的共同主数据
- 将财务核算字段从主表拆分，形成可计算、可审计的财务表
- 将不动产权属与物理属性拆分为专表，避免主表过宽
- 保留管理员可配置的分类扩展字段能力
- 为附件、折旧记录、后续审批和业务流水留出稳定主键和关联关系

## 设计结论

采用混合分层模型：

- `asset_info`：统一资产主表
- `asset_finance`：资产财务表
- `asset_real_estate`：不动产专表
- `asset_category_attr`：分类字段定义表
- `asset_attr_value`：分类字段值表
- `asset_attachment`：资产附件表
- `asset_depreciation_log`：资产折旧记录表

核心原则如下：

- `asset_id` 作为内部主键，`asset_no` 作为业务唯一编码
- 主表只承接身份、组织归属、使用状态等核心主数据
- 财务核算结果允许落库，但必须由系统计算维护
- 不动产权属字段独立建表，不与固定资产通用字段混存
- 分类专属字段走扩展定义和值表，不侵入核心财务和权属字段

## 表关系

```text
asset_category 1 --- n asset_category_attr
asset_category 1 --- n asset_info
asset_info     1 --- 1 asset_finance
asset_info     1 --- 0..1 asset_real_estate
asset_info     1 --- n asset_attr_value
asset_info     1 --- n asset_attachment
asset_info     1 --- n asset_depreciation_log
```

## 字段分层

### 1. 资产主表 `asset_info`

该表保存资产身份信息、归属和状态，不承接强财务核算字段。

建议字段：

- `asset_id`：主键
- `asset_no`：资产编号，唯一业务编码
- `asset_name`：资产名称
- `asset_type`：资产类型，`1=固定资产`，`2=不动产`
- `category_id`：资产分类 ID
- `spec_model`：规格型号
- `unit`：计量单位
- `ownership_org_id`：权属组织/法人主体
- `manage_dept_id`：归口管理部门
- `use_dept_id`：使用部门
- `responsible_user_id`：责任人
- `user_id`：当前使用人
- `location_id`：位置 ID
- `location_text`：位置描述
- `acquire_method`：取得方式
- `purchase_date`：购置日期
- `capitalization_date`：入账日期
- `enable_date`：启用日期
- `asset_status`：资产状态
- `remark`：备注
- `del_flag`：删除标记
- `create_by/create_time/update_by/update_time`

说明：

- “使用部门”和“归口管理部门”必须拆分
- “使用人”和“责任人”必须拆分
- `location_id` 与 `location_text` 并存，兼顾标准化位置管理和自由描述

### 2. 财务表 `asset_finance`

该表承接系统内核算主数据和计算结果。

建议字段：

- `finance_id`
- `asset_id`
- `book_type`
- `currency_code`
- `original_value`
- `salvage_rate`
- `salvage_value`
- `depreciable_value`
- `depreciation_method`
- `useful_life_month`
- `depreciation_start_date`
- `depreciation_end_date`
- `monthly_depreciation_amount`
- `accumulated_depreciation`
- `net_book_value`
- `impairment_amount`
- `book_value`
- `disposed_value`
- `finance_status`
- `last_depreciation_period`
- `version_no`

其中以下字段定义为系统计算维护字段，不允许前端直接改写：

- `salvage_value`
- `depreciable_value`
- `monthly_depreciation_amount`
- `accumulated_depreciation`
- `net_book_value`
- `book_value`

### 3. 不动产专表 `asset_real_estate`

该表仅保存不动产特有的权属和物理属性。

建议字段：

- `real_estate_id`
- `asset_id`
- `property_cert_no`
- `legacy_cert_no`
- `real_estate_unit_no`
- `address_full`
- `land_nature`
- `land_use`
- `building_use`
- `land_area`
- `shared_land_area`
- `building_area`
- `inner_area`
- `building_structure`
- `building_no`
- `floor_no`
- `room_no`
- `completion_date`
- `built_year`
- `land_term_start_date`
- `land_term_end_date`
- `rights_type`
- `rights_holder`
- `co_ownership_type`
- `mortgage_status`
- `mortgagee`
- `seizure_status`
- `registration_date`

补充说明：

- `real_estate_unit_no` 是不动产台账最关键的权属单元标识之一
- `land_term_start_date` 与 `land_term_end_date` 必须成对存在，便于期限管理和预警

### 4. 分类字段定义表 `asset_category_attr`

该表支持管理员为不同分类动态配置扩展字段。

建议字段：

- `attr_id`
- `category_id`
- `attr_code`
- `attr_name`
- `attr_type`
- `data_type`
- `is_required`
- `is_unique`
- `is_list_display`
- `is_query_condition`
- `default_value`
- `option_source_type`
- `option_source`
- `validation_rule`
- `sort_order`
- `status`

### 5. 分类字段值表 `asset_attr_value`

该表存放资产在扩展字段上的具体值。

建议字段：

- `value_id`
- `asset_id`
- `category_id`
- `attr_id`
- `attr_code`
- `attr_value_text`
- `attr_value_number`
- `attr_value_date`
- `attr_value_json`

说明：

- 不建议只保留单一 `attr_value` 文本列
- 数值、日期、JSON 分列有利于范围检索、排序和数据质量控制

### 6. 附件表 `asset_attachment`

该表统一承接产权证、合同、发票、附图、照片等文件。

建议字段：

- `attachment_id`
- `asset_id`
- `biz_type`
- `file_name`
- `file_url`
- `file_size`
- `file_suffix`
- `sort_order`
- `remark`

### 7. 折旧记录表 `asset_depreciation_log`

该表承接月度计提结果和重算历史，是财务追溯和审计的基础。

建议字段：

- `log_id`
- `asset_id`
- `period`
- `depreciation_amount`
- `accumulated_depreciation`
- `net_book_value`
- `book_value`
- `calc_time`
- `calc_type`

## 原始字段清单的归位建议

### 通用主字段

- 资产编号
- 资产名称
- 资产类别
- 规格型号
- 计量单位
- 使用部门
- 存放地点
- 使用人
- 责任人
- 购置日期
- 入账日期
- 取得方式
- 资产状态
- 备注

### 财务表字段

- 原值
- 净残值率
- 净残值
- 使用年限
- 折旧方法
- 月折旧额
- 累计折旧
- 账面价值

### 不动产专表字段

- 不动产权证号
- 土地证号/房产证号
- 坐落地址
- 土地性质
- 土地用途
- 房屋用途
- 土地面积
- 建筑面积
- 套内面积/使用面积
- 建筑结构
- 建成年份/竣工日期
- 使用终止日期
- 权利性质
- 共有情况
- 抵押情况
- 查封情况
- 权属人
- 登记日期
- 分摊土地面积

### 拆至附件或业务单据

- 供应商
- 发票号
- 合同号
- 不动产附图/附件

说明：

- 供应商、发票号、合同号更适合作为采购来源单据或合同档案信息，不应长期挤压在资产主表
- 附件统一由附件表和文件服务管理，避免主表存在重复 URL 或冗余文件元数据

## 数据规则

- 固定资产必须存在 `asset_finance`
- 不动产必须存在 `asset_finance` 和 `asset_real_estate`
- 资产主档一律使用 `asset_id` 进行内部关联
- `asset_no` 仅承担业务编码职责
- 动态字段不能覆盖系统保留字段，例如 `original_value`、`property_cert_no`
- 已开始折旧的资产，不允许直接修改原值、折旧方法、使用年限，应走财务调整或重算流程

## 后端实现建议

建议在 `ruoyi-asset` 模块中将现有单一资产对象扩展为以下对象体系：

- Domain：`AssetInfo`、`AssetFinance`、`AssetRealEstate`、`AssetCategoryAttr`、`AssetAttrValue`、`AssetAttachment`、`AssetDepreciationLog`
- DTO：`AssetCreateReq`、`AssetUpdateReq`
- VO：`AssetListVO`、`AssetDetailVO`
- Service：以聚合服务 `AssetAggregateService` 统筹多表事务

接口建议分层：

- 主档接口：增删改查资产主档
- 财务接口：重算折旧、查询折旧日志
- 扩展字段接口：分类字段配置和读取
- 附件接口：上传、删除、列表查询

## 风险与边界

- 当前现有表使用 `asset_no` 作为主键，后续扩表前必须先完成主键升级
- 财务字段一旦落入系统内核算，就必须建立折旧日志和重算机制，否则账务口径不可追溯
- 分类动态字段能力应服务于非核心扩展属性，不应承担财务或权属主字段
- 不动产若未来需要管理楼栋、房间、分户关系，可在现有权属单元模型上继续扩展层级关系表

## 结论

资产模型最终采用以 `asset_info` 为核心、以 `asset_finance` 和 `asset_real_estate` 为强约束分表、以 `asset_category_attr` 和 `asset_attr_value` 为弹性扩展的混合分层模型。该模型能够在当前项目中平衡台账管理、财务核算、权属审计和后续扩展能力，是后端数据库和服务设计的推荐基线。
