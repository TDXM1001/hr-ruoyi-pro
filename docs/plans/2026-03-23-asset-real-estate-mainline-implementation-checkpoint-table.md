# 2026-03-23 不动产主线实现检查点总表

## 1. 文档目的

这份文档用于把当前分支 `main-assets-new` 上，不动产主线相关能力的真实实现状态整理成一张可阅读、可汇报、可继续排期的总表。

重点回答四个问题：

1. 当前不动产主线已经做到哪里
2. 哪些链路已经形成业务闭环
3. 哪些链路仍然只是最小闭环或隔离实现
4. 下一步最值得推进的方向是什么

## 2. 当前总判断

截至 2026-03-23，当前分支在不动产主线上的真实状态可以概括为：

> 不动产档案、巡检整改、整改审批回写、占用管理已经形成稳定可演示闭环；处置关联已推进到详情壳隔离闭环第六批，但仍未进入统一处置主流程的正式深水区；集成主线仍未启动。

换句话说：

1. `不动产主线核心业务`：已经能跑起来
2. `审批深化`：整改线已经进入稳定强化阶段
3. `处置线`：前端隔离层已经成型，但统一处置主流程仍需后续正式接入
4. `集成线`：还没有启动

## 3. 本地 SQL 与数据基线

### 3.1 本地已执行 SQL 范围

当前本地数据库 `ruoyi-assets` 已执行不动产二期相关增量：

| 序号 | SQL | 用途 |
| --- | --- | --- |
| 09 | `09-asset-real-estate-schema-upgrade-20260320.sql` | 不动产主档扩展结构 |
| 10 | `10-asset-real-estate-menu-data-upgrade-20260321.sql` | 不动产菜单与权限 |
| 11 | `11-asset-real-estate-route-upgrade-20260321.sql` | 不动产详情/表单路由 |
| 12 | `12-asset-real-estate-detail-tabs-upgrade-20260321.sql` | 旧详情子路由页签 |
| 13 | `13-asset-real-estate-rectification-schema-upgrade-20260321.sql` | 整改结构 |
| 14 | `14-asset-real-estate-followup-route-upgrade-20260321.sql` | 巡检/整改独立页路由 |
| 15 | `15-asset-real-estate-detail-tab-route-cleanup-20260321.sql` | 清理旧详情子路由页签 |
| 16 | `16-asset-real-estate-rectification-complete-upgrade-20260321.sql` | 整改完成能力 |
| 17 | `17-asset-real-estate-inspection-sample-fix-20260321.sql` | 巡检样例修复 |
| 18 | `18-asset-real-estate-rectification-approval-hook-upgrade-20260321.sql` | 整改审批挂载位 |
| 19 | `19-asset-real-estate-occupancy-minimum-closure-upgrade-20260322.sql` | 占用最小闭环 |
| 20 | `20-asset-real-estate-occupancy-sample-upgrade-20260322.sql` | 占用点测样例 |
| 21 | `21-asset-real-estate-disposal-sample-upgrade-20260323.sql` | 处置点测样例 |

### 3.2 当前有效路由口径

当前不动产详情采用的稳定口径是：

> 详情壳页内 Tab + 独立业务页

也就是：

1. 详情页本身使用页内 Tab 切换
2. 巡检任务明细、整改单、整改完成页等仍保留独立页面
3. 旧的详情子路由页签已经被清理，不再是当前有效方案

## 4. 不动产主线实现检查点总表

| 主线/模块 | 当前目标 | 当前状态 | 当前实现结果 | 闭环判断 | 当前建议 |
| --- | --- | --- | --- | --- | --- |
| 不动产档案主线 | 列表、详情、建档、编辑、生命周期展示 | 已完成 | 列表页、详情壳、新建页、编辑页、生命周期展示均已具备，菜单和样例数据已打通 | 已闭环 | 进入稳定维护 |
| 巡检任务主线 | 巡检记录、任务明细、异常承接整改 | 已完成 | 巡检页签、任务明细页、异常进入整改链路已打通 | 已闭环 | 保持与整改线协同 |
| 整改登记与完成 | 发起整改、编辑整改、完成整改、完成后只读 | 已完成 | 整改单新建/编辑/完成页完整，完成后只读和状态回写已落地 | 已闭环 | 保持现状 |
| 整改审批挂载位 | 提交审批、驳回/通过、轨迹查看 | 已完成 | 审批挂载第一批已完成，并继续做了第二批、第三批、第四批回写强化 | 已闭环（深化中） | 如继续做，属于精修而非主线缺口 |
| 占用管理 | 发起、变更、释放、主档回写、轨迹展示 | 已完成 | 后端闭环、前端闭环、点测样例、前端收口、脚本拆分、治理工具区下沉均已完成 | 已闭环 | 建议进入收口维护，不再继续扩张为主线 |
| 处置关联详情壳 | 详情壳判断状态、发起入口、责任归口、轨迹回看 | 已完成到第六批 | 已完成 1~6 批：闭环卡统一、责任归口统一、最近动作责任链统一、浏览器点测样例已补齐 | 隔离闭环已形成 | 可以继续，但应谨慎接入统一处置主流程 |
| 统一处置主流程 | 处置申请、审批、结果落表、资产状态正式回写 | 未纳入本轮实现 | 当前只通过不动产详情壳做轻发起和上下文联动；统一处置后端仍受现有脏文件影响 | 未闭环 | 后续需单独排期，先处理工作区边界 |
| 财务/采购/合同证照集成 | 与资产主线联动 | 未启动 | 当前没有进入实现阶段 | 未启动 | 后续单独规划 |

## 5. 详细检查点拆解

### 5.1 不动产档案主线

**当前结果**

1. 列表页、详情页壳、新建页、编辑页都已落地
2. 详情页顶部摘要、总览、生命周期信息已稳定
3. 详情页冷启动直链 `404` 问题已修复

**代表实现**

- [AssetRealEstateController.java](e:/my-project/hr-ruoyi-pro/RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetRealEstateController.java)
- [index.vue](e:/my-project/hr-ruoyi-pro/art-design-pro/src/views/asset/real-estate/index.vue)
- [detail/index.vue](e:/my-project/hr-ruoyi-pro/art-design-pro/src/views/asset/real-estate/detail/index.vue)
- [form/index.vue](e:/my-project/hr-ruoyi-pro/art-design-pro/src/views/asset/real-estate/form/index.vue)

**判断**

> 这条线已经不是当前缺口。

### 5.2 巡检整改主线

**当前结果**

1. 巡检页签可展示任务与异常
2. 任务明细页可查看整改上下文
3. 发起整改、编辑整改、完成整改已闭环
4. 已完成整改单只读
5. 巡检结果与整改状态已联动回写

**代表实现**

- [inspection-task/index.vue](e:/my-project/hr-ruoyi-pro/art-design-pro/src/views/asset/real-estate/inspection-task/index.vue)
- [rectification/form/index.vue](e:/my-project/hr-ruoyi-pro/art-design-pro/src/views/asset/real-estate/rectification/form/index.vue)
- [rectification/complete/index.vue](e:/my-project/hr-ruoyi-pro/art-design-pro/src/views/asset/real-estate/rectification/complete/index.vue)
- [AssetRectificationServiceImpl.java](e:/my-project/hr-ruoyi-pro/RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetRectificationServiceImpl.java)

**判断**

> 巡检异常到整改完成已经形成完整闭环。

### 5.3 整改审批回写主线

**当前结果**

当前已经推进到第四批，关键效果是：

1. 整改页签、整改单、整改完成页的审批口径统一
2. 详情壳总览能直接识别整改闭环卡点
3. 生命周期轨迹能识别整改相关关键节点
4. 顶部摘要和整改页签的闭环提示已经统一

**当前定位**

> 这条线已经从“补主流程”进入“精修强化”阶段。

**判断**

如果继续做第五批，属于价值提升，而不是主线缺口修补。

### 5.4 占用管理主线

**当前结果**

1. 发起占用、变更占用、释放占用已具备
2. 主档回写与轨迹展示已具备
3. 占用页签前端已经做过多轮增强，并最终完成收口整理
4. 业务操作区、摘要区、历史区、治理工具区已经分层
5. 脚本层已拆为 `useOccupancyState` 和 `useOccupancyGovernance`

**代表文档**

- [2026-03-23-asset-phase2-real-estate-occupancy-business-flow.md](e:/my-project/hr-ruoyi-pro/docs/plans/2026-03-23-asset-phase2-real-estate-occupancy-business-flow.md)
- [2026-03-23-asset-phase2-real-estate-occupancy-frontend-final-closure-recommendations.md](e:/my-project/hr-ruoyi-pro/docs/plans/2026-03-23-asset-phase2-real-estate-occupancy-frontend-final-closure-recommendations.md)

**判断**

> 占用已经闭环，且已经进入“建议停扩、转维护”的阶段。

### 5.5 处置关联主线

**当前结果**

处置关联已经推进到第六批，当前达成的能力包括：

1. 不动产详情壳顶部摘要可识别处置闭环状态
2. 总览页可展示处置闭环摘要
3. 处置页签首屏已统一为处置闭环卡
4. 处置页签与总览、顶部摘要共享一套解释口径
5. 生命周期处置节点、处置记录卡片、闭环卡已统一为“最近动作责任链”视图
6. 统一处置页的 4 条 Vue 警告已清理
7. 已补真实处置样例数据，并完成浏览器点测

**当前边界**

需要特别注意：

1. 当前完成的是 `不动产详情壳侧隔离闭环`
2. 统一处置主流程后端并没有在本轮被正式推进
3. 当前工作区仍存在一批处置后端相关脏文件，后续若进入统一处置主流程实现，需要先重新确认边界

**判断**

> 处置关联在“不动产详情壳”这一层已经形成稳定最小闭环，但整个统一处置主流程仍未正式闭环。

## 6. 验证与点测情况

### 6.1 自动化验证

当前分支已经多次通过：

1. 后端编译
2. 整改线相关前端回归
3. 处置线相关前端回归
4. 占用线相关前端回归

最近一次与处置第六批直接相关的回归结果：

```bash
pnpm vitest run tests/views/asset-real-estate-detail-page.test.ts tests/views/asset-disposal-page.test.ts tests/views/asset-real-estate-rectification-panel-approval.test.ts tests/views/asset-real-estate-rectification-form-page.test.ts tests/views/asset-real-estate-rectification-complete-page.test.ts tests/api/asset-real-estate.test.ts
```

结果：`47 passed`

### 6.2 真实浏览器点测

最近一次不动产详情壳处置线点测资产为：

- `assetId=20002`
- `assetCode=RE-2026-0002`

已确认：

1. 顶部摘要显示处置闭环状态
2. 总览中的处置闭环卡显示统一责任链
3. 生命周期中的处置节点显示统一责任链
4. 处置页签首屏显示统一闭环卡
5. 处置记录卡片显示统一责任链
6. `进入资产处置` 联动入口正常

## 7. 当前风险与边界

### 7.1 工作区风险

当前工作区仍存在一批未处理的处置/审批相关脏文件，例如：

1. `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetDisposalController.java`
2. `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetDisposalServiceImpl.java`
3. `art-design-pro/src/api/asset/disposal.ts`

这些文件在当前阶段没有被我们纳入统一处置主流程正式实现。

### 7.2 配置文件边界

以下文件仍未触碰：

- [application-druid.yml](e:/my-project/hr-ruoyi-pro/RuoYi-Vue/ruoyi-admin/src/main/resources/application-druid.yml)

## 8. 当前阶段结论

从“资产主线是否已经成形”的角度看，当前最准确的结论是：

1. 不动产档案：已闭环
2. 巡检整改：已闭环
3. 整改审批回写：已推进到第四批，属于稳定强化阶段
4. 占用管理：已闭环并完成前端收口
5. 处置关联：详情壳隔离闭环已推进到第六批，统一处置主流程仍待后续推进
6. 集成主线：未启动

也就是说，当前分支已经具备：

> 一个完整可演示的不动产主线产品骨架。

但仍不应表述为：

> 资产二期所有主线都已经推进到同样深度。

## 9. 下一步建议

### 9.1 推荐优先级

1. 若继续深化不动产主线：
   - 优先进入统一处置主流程的安全接入设计/实现
2. 若先控制风险：
   - 先对当前处置后端脏文件做边界确认，再进入主流程实现
3. 若做阶段收口：
   - 当前即可形成一版“资产二期不动产主线阶段性汇报”

### 9.2 建议口径

如果要对外说明当前进展，建议统一使用以下表述：

> 当前不动产主线已经形成稳定闭环，档案、巡检整改、整改审批回写、占用管理都已可演示；处置关联已在详情壳层完成最小闭环并通过点测，统一处置主流程将作为下一阶段重点推进。