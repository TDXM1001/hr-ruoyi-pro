# 2026-03-22 不动产占用管理最小闭环设计

## 1. 背景

截至 2026-03-22，不动产主线已经完成档案、巡检、整改、整改审批挂载位第一批闭环，但“占用管理”仍停留在展示层。当前详情页“占用”页签只能看到当前使用部门、责任人、位置和交接轨迹，无法真正发起占用、变更占用、释放占用。

结合当前代码现状，本轮不进入楼栋、楼层、房间、产权单元等空间粒度，而是以 `assetId` 为主键先做“资产级占用最小闭环”，目标是在不引入空间子系统的前提下，把不动产主线再补完整一段。

## 2. 目标

本轮目标：

1. 支持发起不动产占用
2. 支持对当前有效占用做变更
3. 支持释放当前有效占用
4. 占用动作回写资产主档当前使用信息
5. 占用动作进入生命周期与变更日志

本轮明确不做：

1. 空间粒度管理
2. 占用审批
3. 占用附件
4. 占用批量导入
5. 处置、整改与占用的交叉阻断规则深化

## 3. 方案对比

### 3.1 方案 A：直接改资产主档字段

只更新 `ast_asset_ledger` 的 `use_dept_id / responsible_user_id / location_name / asset_status`。

优点：实现最快。

缺点：

1. 没有独立占用业务单
2. 无法自然承载“变更占用”“释放占用”
3. 历史轨迹只能靠文本日志，后续会变脏

### 3.2 方案 B：独立占用单 + 单资产单有效占用（推荐）

新增不动产占用单表，资产主档只保留当前占用快照；历史轨迹通过占用单保留。

优点：

1. 能闭环发起、变更、释放
2. 历史清晰
3. 后续可以平滑升级到空间级占用
4. 不依赖当前已脏的 disposal / approval 代码线

缺点：

1. 要新增表、Mapper、Service、接口
2. 第一批只落后端，需要第二批再补前端交互

### 3.3 方案 C：复用固定资产交接单

把不动产占用挂到现有 handover 体系。

优点：少一套表。

缺点：

1. 固定资产交接语义和不动产占用语义不同
2. 容易牵动已有固定资产使用流
3. 会碰到用户明确要求避开的脏文件范围

### 3.4 结论

采用方案 B：`独立占用单 + 单资产单有效占用`。

## 4. 数据模型设计

新增表：`ast_asset_real_estate_occupancy_order`

核心字段：

1. `occupancy_id`：占用单 ID
2. `occupancy_no`：占用单号，建议格式 `OCC-2026-0001`
3. `asset_id`：资产 ID
4. `use_dept_id`：使用部门
5. `responsible_user_id`：责任人
6. `location_name`：使用位置
7. `start_date`：占用开始日期
8. `end_date`：占用结束日期
9. `occupancy_status`：`ACTIVE / RELEASED`
10. `change_reason`：变更原因
11. `release_reason`：释放原因
12. `create_by / create_time / update_by / update_time / del_flag`

业务约束：

1. 同一资产同一时刻只允许存在一条 `ACTIVE` 占用
2. 变更占用不直接改旧单，而是：
   - 关闭旧单为 `RELEASED`
   - 新建一条新单为 `ACTIVE`
3. 释放占用后，资产不再存在当前有效占用

## 5. 领域规则

### 5.1 发起占用

输入：`assetId + useDeptId + responsibleUserId + locationName + startDate + changeReason`

规则：

1. 资产必须存在且为 `REAL_ESTATE`
2. 当前不得存在 `ACTIVE` 占用
3. 发起成功后：
   - 写入一条 `ACTIVE` 占用单
   - 资产主档回写 `useDeptId / responsibleUserId / locationName`
   - 资产状态回写为 `IN_USE`
   - 写入 `asset_change_log`

### 5.2 变更占用

输入：`assetId + occupancyId + 新 useDeptId / responsibleUserId / locationName / startDate / changeReason`

规则：

1. 只能对 `ACTIVE` 占用执行
2. 旧占用单更新为 `RELEASED`，写入 `endDate`
3. 新建一条新的 `ACTIVE` 占用单
4. 资产主档回写为最新占用快照
5. 写入变更日志

### 5.3 释放占用

输入：`assetId + occupancyId + endDate + releaseReason`

规则：

1. 只能对 `ACTIVE` 占用执行
2. 释放后该占用单状态变为 `RELEASED`
3. 资产主档清空 `useDeptId / responsibleUserId / locationName`
4. 资产状态回写为 `IDLE`
5. 写入变更日志

## 6. 后端接口设计

第一批只做后端接口，不带前端页面。

### 6.1 查询占用记录

`GET /asset/real-estate/{assetId}/occupancies`

返回：

1. 当前有效占用
2. 历史占用记录

### 6.2 发起占用

`POST /asset/real-estate/{assetId}/occupancies`

### 6.3 变更占用

`POST /asset/real-estate/{assetId}/occupancies/{occupancyId}/change`

### 6.4 释放占用

`POST /asset/real-estate/{assetId}/occupancies/{occupancyId}/release`

## 7. 服务实现设计

为了减少对现有不动产主线的破坏，本轮仍挂在 `IAssetRealEstateService / AssetRealEstateServiceImpl` 之下实现，不新增新的 service 模块边界。

新增内容：

1. 占用单领域对象
2. 占用单 BO / VO
3. 占用单 Mapper / XML
4. `AssetRealEstateServiceImpl` 的占用方法
5. `AssetRealEstateController` 的占用接口
6. `AssetLedgerLifecycleVo` 增加 `occupancyRecords`

## 8. 日志与状态口径

本轮不修改 `AssetBizType`，避免带入已有脏文件。

占用动作写 `asset_change_log` 时：

1. `bizType` 直接使用字符串：`REAL_ESTATE_OCCUPANCY`
2. `changeDesc` 写明确动作：
   - `发起不动产占用`
   - `变更不动产占用`
   - `释放不动产占用`

## 9. 测试策略

第一批只做后端单测。

核心用例：

1. 无有效占用时允许发起占用
2. 已有有效占用时禁止重复发起
3. 变更占用时应关闭旧单并新建新单
4. 释放占用后资产状态应变为 `IDLE`
5. 查询生命周期时应带出占用记录

## 10. 实施边界

第一批实现范围：

1. SQL
2. 后端 Domain / Mapper / Service / Controller
3. 后端单测
4. 本地 SQL 执行与验证

第二批再做：

1. 前端占用页签操作区
2. Drawer 表单
3. 前端交互测试
