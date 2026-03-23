# 不动产处置关联最小闭环第二批实施计划

## 1. 实施目标

在不进入统一处置模块内部实现的前提下，补齐不动产详情壳对处置闭环的第二层表达：

1. 总览正文出现处置闭环摘要。
2. 生命周期轨迹可回看处置关键节点。
3. 处置节点与整改节点解释互不串线。

## 2. 实施步骤

### 步骤 1：补失败测试

在 `asset-real-estate-detail-page.test.ts` 中增加两类失败用例：

1. 总览展示处置闭环摘要和最近处置动作。
2. 总览生命周期轨迹对处置节点展示阶段标签。

### 步骤 2：扩展处置事件解释器

在 `disposal-overview.ts` 中新增处置生命周期节点识别逻辑，支持：

1. 发起处置
2. 提交处置审批
3. 处置审批通过
4. 处置审批驳回
5. 确认处置

### 步骤 3：修正整改事件误判

在 `rectification-overview.ts` 中补充防串线逻辑：

1. 含“处置”语义的日志不再进入整改事件匹配。

### 步骤 4：接入总览页

在 `index.vue` 中：

1. 给 `OverviewPanel` 传入 `disposalSummary`
2. 合并整改和处置生命周期装饰结果

### 步骤 5：更新总览面板

在 `overview-panel.vue` 中：

1. 新增“处置闭环摘要”卡片
2. 生命周期轨迹展示处置阶段标签和提示

### 步骤 6：执行回归

至少执行：

1. `tests/views/asset-real-estate-detail-page.test.ts`
2. `tests/views/asset-disposal-page.test.ts`
3. `tests/views/asset-real-estate-rectification-panel-approval.test.ts`
4. `tests/views/asset-real-estate-rectification-form-page.test.ts`
5. `tests/views/asset-real-estate-rectification-complete-page.test.ts`
6. `tests/api/asset-real-estate.test.ts`

## 3. 交付结果

本批完成后，资产管理员在总览页就能直接看到：

1. 处置闭环当前状态
2. 最近处置动作
3. 生命周期中的关键处置节点
4. 与整改闭环不串线的统一解释

## 4. 边界说明

1. 不新增 SQL
2. 不修改处置后端
3. 不修改统一资产处置模块脏文件
4. 不触碰 `application-druid.yml`