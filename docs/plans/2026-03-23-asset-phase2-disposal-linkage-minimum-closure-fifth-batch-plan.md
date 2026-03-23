# 不动产处置关联最小闭环第五批实施计划

## 1. 实施目标

完成不动产详情壳侧“处置闭环卡统一型”收口：

1. 顶部摘要统一
2. 总览处置摘要统一
3. 处置页签首屏统一

## 2. 改动范围

### 2.1 前端代码

1. `art-design-pro/src/views/asset/real-estate/detail/components/disposal-overview.ts`
2. `art-design-pro/src/views/asset/real-estate/detail/components/disposal-closure-card.vue`
3. `art-design-pro/src/views/asset/real-estate/detail/components/overview-panel.vue`
4. `art-design-pro/src/views/asset/real-estate/detail/components/disposal-panel.vue`
5. `art-design-pro/src/views/asset/real-estate/detail/index.vue`
6. `art-design-pro/tests/views/asset-real-estate-detail-page.test.ts`

### 2.2 文档

1. `docs/plans/2026-03-23-asset-phase2-disposal-linkage-minimum-closure-fifth-batch-design.md`
2. `docs/plans/2026-03-23-asset-phase2-disposal-linkage-minimum-closure-fifth-batch-plan.md`

## 3. 执行步骤

### 步骤1：补测试

1. 为“顶部摘要、总览和处置页签统一闭环卡口径”增加定向测试。
2. 为“未发起处置场景统一发起提示”增加定向测试。
3. 先运行测试，确认红灯。

### 步骤2：统一解释层

1. 在 `disposal-overview.ts` 中新增 `DisposalClosureCard`。
2. 从现有处置摘要派生顶部摘要和首屏卡片所需字段。

### 步骤3：统一组件层

1. 新增共享组件 `disposal-closure-card.vue`。
2. 总览复用共享闭环卡。
3. 处置页签复用共享闭环卡，并合并发起/进入按钮。

### 步骤4：统一顶部摘要

1. 详情壳顶部摘要为处置卡增加紧凑标题。
2. 增加紧凑说明，展示最近动作和最近责任人。

### 步骤5：回归验证

1. 跑第五批定向测试。
2. 跑详情壳与处置相关回归测试。
3. 确认未触碰后端处置脏文件。

## 4. 风险控制

1. 不改后端接口。
2. 不新增 SQL。
3. 不修改统一处置后端相关脏文件。
4. 不触碰 `application-druid.yml`。

## 5. 完成判定

满足以下条件即可判定第五批完成：

1. 定向测试通过。
2. 相关回归通过。
3. 顶部摘要、总览、处置页签三处口径一致。
4. 处置页签入口仍可直接发起和查看统一处置流程。