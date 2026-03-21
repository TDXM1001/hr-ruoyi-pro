# 2026-03-21 资产二期当前实现检查点

## 1. 当前结论

截至 2026-03-21，当前分支的真实推进状态不是“二期三线并行均衡推进”，而是：

> 不动产主线已经形成稳定闭环，并开始向整改审批挂载位延伸；审批主线进入第一批最小闭环；集成主线仍未启动。

从代码和本地数据库实际状态看，当前最稳的业务口径是：

1. 不动产档案建账闭环：已完成
2. 巡检异常到整改完成闭环：已完成
3. 整改审批挂载位第一批：已完成
4. 占用管理：仅展示层，未闭环
5. 处置关联：仅跳转过滤，未闭环
6. 财务 / 采购 / 合同证照集成：未启动

## 2. 本地 SQL 执行状态

### 2.1 已执行到本地库的增量范围

当前本地数据库 `ruoyi-assets` 已执行到以下增量：

1. `09-asset-real-estate-schema-upgrade-20260320.sql`
2. `10-asset-real-estate-menu-data-upgrade-20260321.sql`
3. `11-asset-real-estate-route-upgrade-20260321.sql`
4. `12-asset-real-estate-detail-tabs-upgrade-20260321.sql`
5. `13-asset-real-estate-rectification-schema-upgrade-20260321.sql`
6. `14-asset-real-estate-followup-route-upgrade-20260321.sql`
7. `15-asset-real-estate-detail-tab-route-cleanup-20260321.sql`
8. `16-asset-real-estate-rectification-complete-upgrade-20260321.sql`
9. `17-asset-real-estate-inspection-sample-fix-20260321.sql`
10. `18-asset-real-estate-rectification-approval-hook-upgrade-20260321.sql`

### 2.2 本次新执行的 SQL

本次已实际执行：

- [18-asset-real-estate-rectification-approval-hook-upgrade-20260321.sql](/e:/my-project/hr-ruoyi-pro/RuoYi-Vue/sql/asset/18-asset-real-estate-rectification-approval-hook-upgrade-20260321.sql)

执行结果：

1. `ast_asset_rectification_order` 新增字段：
   - `approval_status`
   - `approval_submitted_time`
   - `approval_finished_time`
2. 新建表：
   - `ast_asset_rectification_approval_record`

### 2.3 当前有效路由口径

当前有效口径仍然是：

> 详情壳页内 Tab + 独立业务页

不是旧的“详情子路由页签”模式。原因：

1. `12` 号 SQL 曾补过详情子路由
2. `15` 号 SQL 已清理旧详情页签子路由 `2135 / 2136 / 2137 / 2138`
3. 当前保留的是独立业务页：
   - 巡检任务明细页
   - 整改单新增页
   - 整改单编辑页
   - 整改完成页

## 3. 代码侧当前闭环

### 3.1 不动产档案主线

当前已经具备：

1. 列表页
2. 详情页壳
3. 新建页
4. 编辑页
5. 生命周期聚合展示
6. 菜单、路由、点测样例

对应代码：

- [AssetRealEstateController.java](/e:/my-project/hr-ruoyi-pro/RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetRealEstateController.java)
- [index.vue](/e:/my-project/hr-ruoyi-pro/art-design-pro/src/views/asset/real-estate/index.vue)
- [detail/index.vue](/e:/my-project/hr-ruoyi-pro/art-design-pro/src/views/asset/real-estate/detail/index.vue)
- [form/index.vue](/e:/my-project/hr-ruoyi-pro/art-design-pro/src/views/asset/real-estate/form/index.vue)

结论：不动产档案主线已闭环。

### 3.2 巡检异常到整改完成主线

当前已经具备：

1. 巡检记录展示
2. 巡检任务明细页
3. 发起整改
4. 编辑整改
5. 完成整改
6. 完成后只读
7. 巡检跟进状态回写
8. 资产变更日志留痕

对应代码：

- [AssetRectificationServiceImpl.java](/e:/my-project/hr-ruoyi-pro/RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetRectificationServiceImpl.java)
- [inspection-task/index.vue](/e:/my-project/hr-ruoyi-pro/art-design-pro/src/views/asset/real-estate/inspection-task/index.vue)
- [rectification/form/index.vue](/e:/my-project/hr-ruoyi-pro/art-design-pro/src/views/asset/real-estate/rectification/form/index.vue)
- [rectification/complete/index.vue](/e:/my-project/hr-ruoyi-pro/art-design-pro/src/views/asset/real-estate/rectification/complete/index.vue)

结论：巡检异常到整改完成已闭环。

### 3.3 整改审批挂载位第一批

当前已经具备的后端能力：

1. 查询整改审批轨迹
2. 提交整改审批
3. 整改审批通过
4. 整改审批驳回
5. 驳回后允许再次提交
6. 审批状态写回整改单
7. 审批轨迹单独落表

对应代码：

- [AssetRectificationApprovalRecord.java](/e:/my-project/hr-ruoyi-pro/RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/AssetRectificationApprovalRecord.java)
- [AssetRectificationApprovalActionBo.java](/e:/my-project/hr-ruoyi-pro/RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/bo/AssetRectificationApprovalActionBo.java)
- [AssetRectificationApprovalMapper.java](/e:/my-project/hr-ruoyi-pro/RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/mapper/AssetRectificationApprovalMapper.java)
- [AssetRectificationApprovalMapper.xml](/e:/my-project/hr-ruoyi-pro/RuoYi-Vue/ruoyi-asset/src/main/resources/mapper/asset/AssetRectificationApprovalMapper.xml)
- [AssetRectificationServiceImpl.java](/e:/my-project/hr-ruoyi-pro/RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetRectificationServiceImpl.java)
- [AssetRealEstateController.java](/e:/my-project/hr-ruoyi-pro/RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetRealEstateController.java)

当前已经具备的前端能力：

1. 整改页签卡片展示审批状态
2. 整改页签支持：
   - 提交审批
   - 审批通过
   - 审批驳回
   - 查看审批轨迹
3. 整改完成页展示审批挂载信息并支持审批动作
4. 整改单编辑页展示审批只读信息
5. 详情壳支持审批轨迹抽屉

对应代码：

- [real-estate.ts](/e:/my-project/hr-ruoyi-pro/art-design-pro/src/api/asset/real-estate.ts)
- [ledger.ts](/e:/my-project/hr-ruoyi-pro/art-design-pro/src/api/asset/ledger.ts)
- [rectification-panel.vue](/e:/my-project/hr-ruoyi-pro/art-design-pro/src/views/asset/real-estate/detail/components/rectification-panel.vue)
- [detail/index.vue](/e:/my-project/hr-ruoyi-pro/art-design-pro/src/views/asset/real-estate/detail/index.vue)
- [rectification/complete/index.vue](/e:/my-project/hr-ruoyi-pro/art-design-pro/src/views/asset/real-estate/rectification/complete/index.vue)
- [rectification/form/index.vue](/e:/my-project/hr-ruoyi-pro/art-design-pro/src/views/asset/real-estate/rectification/form/index.vue)

结论：整改审批挂载位第一批已闭环。

## 4. 当前未闭环线路

### 4.1 占用管理

当前仅有：

1. 占用信息展示
2. 交接轨迹展示

仍缺：

1. 发起占用
2. 占用变更
3. 占用释放
4. 占用状态收口

结论：仅展示层，未形成业务闭环。

### 4.2 处置关联

当前仅有：

1. 不动产详情跳转处置模块
2. 跳转时带 `assetId / assetCode / tab` 过滤上下文

仍缺：

1. 不动产侧发起处置
2. 处置审批链路
3. 处置结果回写资产状态

结论：仅关联跳转闭环，未形成处置业务闭环。

### 4.3 集成主线

当前仍未启动：

1. 财务联动
2. 采购联动
3. 合同证照联动

## 5. 当前阶段定位

用二期分层里程碑口径描述，当前最准确的位置是：

1. `M0`：已完成
2. `M1 不动产主线`：已完成
3. `M1 整改审批挂载位第一批`：已完成
4. `M1 占用管理`：未完成
5. `M1 处置主线`：未完成
6. `M2 正式审批主线`：未完成
7. `M2 集成主线`：未启动

因此，当前分支不应再描述为：

> 三条主线都已经并行推进到同一深度

更准确的说法是：

> 当前分支已经把不动产主线做成稳定闭环，并把整改审批挂载位推进到第一批最小闭环；处置、占用、集成仍需继续补齐。

## 6. 推荐下一条闭环线路

基于当前代码和数据库状态，返工最少、价值最高的下一条闭环线路是：

### 6.1 推荐线路：占用管理最小闭环

建议目标：

1. 发起占用
2. 变更占用部门 / 责任人 / 位置
3. 释放占用
4. 占用状态回写到详情壳
5. 占用轨迹进入生命周期

推荐原因：

1. 不动产详情壳和页签已稳定
2. 占用页签目前仍停留在展示层，最容易形成下一条业务闭环
3. 不需要立即碰通用审批脏文件，风险低于直接进入处置审批主线

### 6.2 备选线路：整改审批正式化

如果要继续沿审批主线深入，下一步应做：

1. 审批角色与权限拆分
2. 审批待办视图
3. 审批结果与资产状态更强联动
4. 与通用审批中心对接

这条线价值高，但因为当前工作区已有通用审批脏文件，推进风险高于占用管理。

## 7. 本次验证证据

### 7.1 后端验证

已通过：

- `mvn -pl ruoyi-asset -am test "-Dtest=AssetRectificationServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false"`
- `mvn -pl ruoyi-admin -am -DskipTests compile`

结果：

1. 整改服务单测通过 `12 tests`
2. 后端编译通过

### 7.2 前端验证

已通过：

- `pnpm vitest run tests/api/asset-real-estate.test.ts tests/views/asset-real-estate-rectification-panel-approval.test.ts tests/views/asset-real-estate-detail-page.test.ts tests/views/asset-real-estate-rectification-form-page.test.ts tests/views/asset-real-estate-rectification-complete-page.test.ts`

结果：

1. `5 files`
2. `21 tests passed`

### 7.3 数据库验证

已通过：

- `18-asset-real-estate-rectification-approval-hook-upgrade-20260321.sql` 本地执行成功

结果：

1. 整改单审批字段已落库
2. 整改审批轨迹表已建成

## 8. 最终判断

当前可以清晰下结论：

> 不动产主线已经从“档案可看”推进到“档案可建、巡检可查、整改可做、整改可收口、整改审批可挂载”的稳定阶段。

但同时也必须明确：

> 当前进展主要集中在不动产与整改线；占用、处置、集成仍未形成等深度闭环。

这份检查点文档可作为后续二期开工、排期和验收的统一口径。
