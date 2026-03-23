# 不动产处置关联最小闭环第三批实施计划

## 1. 实施目标

在不进入统一处置模块内部实现的前提下，把总览与处置页签统一成“责任归口提示”表达。

## 2. 实施步骤

### 步骤 1：补失败测试

在 `asset-real-estate-detail-page.test.ts` 中增加：

1. 总览和处置页签统一展示处置责任归口提示
2. 处置审批中场景统一提示当前责任归口和下一步动作

### 步骤 2：扩展处置摘要解释器

在 `disposal-overview.ts` 中：

1. 增加责任归口字段
2. 增加最近责任人字段
3. 支持从处置生命周期日志中优先识别最近责任人

### 步骤 3：接入详情壳编排层

在 `index.vue` 中把生命周期日志传给处置摘要解释器，确保总览和处置页签共用同一份摘要结果。

### 步骤 4：更新总览与处置页签

1. 总览卡片新增“当前责任归口 / 责任动作 / 最近责任人”
2. 处置页签摘要同步新增相同信息
3. 统一展示责任提示文案

### 步骤 5：执行回归

至少执行：

1. `tests/views/asset-real-estate-detail-page.test.ts`
2. `tests/views/asset-disposal-page.test.ts`
3. `tests/views/asset-real-estate-rectification-panel-approval.test.ts`
4. `tests/views/asset-real-estate-rectification-form-page.test.ts`
5. `tests/views/asset-real-estate-rectification-complete-page.test.ts`
6. `tests/api/asset-real-estate.test.ts`

## 3. 边界说明

1. 不新增 SQL
2. 不修改处置后端
3. 不修改统一资产处置模块脏文件
4. 不触碰 `application-druid.yml`

## 4. 交付结果

本批完成后，资产管理员在总览或处置页签都能直接看到：

1. 当前责任归口
2. 当前责任动作
3. 最近责任人
4. 当前责任提示

这样处置闭环从“只给状态”升级为“状态 + 责任归口”的轻闭环视图。