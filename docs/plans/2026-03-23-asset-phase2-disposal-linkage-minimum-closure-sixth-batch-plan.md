# 不动产处置关联最小闭环第六批实施计划

## 1. 实施目标

完成不动产详情壳侧“最近动作责任链”统一收口：

1. 闭环卡统一责任链
2. 处置记录卡片统一责任链
3. 生命周期节点统一责任链

## 2. 改动范围

### 2.1 前端代码

1. rt-design-pro/src/views/asset/real-estate/detail/components/disposal-overview.ts
2. rt-design-pro/src/views/asset/real-estate/detail/components/disposal-closure-card.vue
3. rt-design-pro/src/views/asset/real-estate/detail/components/disposal-panel.vue
4. rt-design-pro/src/views/asset/real-estate/detail/components/overview-panel.vue
5. rt-design-pro/src/views/asset/real-estate/detail/components/disposal-responsibility-chain.vue
6. rt-design-pro/tests/views/asset-real-estate-detail-page.test.ts

### 2.2 文档

1. docs/plans/2026-03-23-asset-phase2-disposal-linkage-minimum-closure-sixth-batch-design.md
2. docs/plans/2026-03-23-asset-phase2-disposal-linkage-minimum-closure-sixth-batch-plan.md

## 3. 执行步骤

### 步骤1：补测试

1. 为“总览、处置页签、记录卡片和生命周期节点统一展示最近动作责任链”增加定向测试。
2. 为“生命周期处置节点显示统一责任链”补断言。
3. 先运行测试，确认红灯。

### 步骤2：统一解释层

1. 在 disposal-overview.ts 中新增 DisposalResponsibilityChain。
2. 新增闭环卡、记录卡片、生命周期节点各自的责任链构造方法。

### 步骤3：新增共享责任链组件

1. 新建 disposal-responsibility-chain.vue。
2. 支持 card 与 inline 两种展示模式。
3. 支持测试定位标记。

### 步骤4：接入三处视图

1. 闭环卡改为嵌入共享责任链组件。
2. 处置记录卡片改为使用共享责任链组件。
3. 生命周期节点改为使用共享责任链组件。

### 步骤5：回归验证

1. 跑第六批定向测试。
2. 跑详情壳与处置相关回归测试。
3. 确认未触碰处置后端脏文件。

## 4. 风险控制

1. 不改后端接口。
2. 不新增 SQL。
3. 不修改统一处置后端相关脏文件。
4. 不触碰 pplication-druid.yml。

## 5. 完成判定

满足以下条件即可判定第六批完成：

1. 定向测试通过。
2. 相关回归测试通过。
3. 闭环卡、记录卡片、生命周期节点三处责任链口径一致。
4. 本批只落在不动产详情壳前端隔离层。