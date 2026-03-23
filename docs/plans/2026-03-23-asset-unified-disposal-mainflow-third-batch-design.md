# 统一处置主流程第三批设计

## 1. 目标

第一批解决了来源上下文安全接入，第二批把来源横幅、资产上下文和筛选提示统一成了来源视图。第三批继续只做前端安全接入层，不进入统一处置后端主流程。

本批目标：

> 把来源视图和首屏操作引导收成一张完整的闭环入口卡。

## 2. 当前问题

虽然第二批已经把来源信息收拢了，但首屏仍然是“来源横幅 + 来源接入卡 + tab 视图”三段式结构。用户能理解当前来源，但还不能在首屏一眼知道“下一步该点哪个入口”。

## 3. 最终方案

采用“闭环入口卡”方案：

1. 顶部仍保留来源横幅，负责说明来源与意图。
2. 用一张统一的闭环入口卡承接首屏主要信息。
3. 入口卡固定展示：
   - 当前资产
   - 当前意图
   - 首屏落点
   - 当前锁定范围
   - 下一步建议
   - 主操作入口
   - 返回不动产详情入口
4. `intent=view` 与 `intent=start` 复用同一张卡，但主操作文案不同。

## 4. 交互口径

### 4.1 intent=view

首屏强调：

1. 当前更适合回看处置记录
2. 主操作按钮为“查看处置记录”
3. 点击主操作后，仍落在 `record` tab，并刷新处置记录列表

### 4.2 intent=start

首屏强调：

1. 当前更适合继续办理待处置资产
2. 主操作按钮为“进入待处置资产池”
3. 点击主操作后，切到 `pool` tab，并刷新待处置资产池列表

## 5. 安全边界

### 5.1 可动文件

1. `art-design-pro/src/views/asset/disposal/index.vue`
2. `art-design-pro/src/views/asset/disposal/disposal-source-context.ts`
3. `art-design-pro/src/views/asset/disposal/components/disposal-entry-card.vue`
4. `art-design-pro/tests/views/asset-disposal-page.test.ts`

### 5.2 禁动文件

1. 所有处置后端文件
2. 所有 SQL
3. `RuoYi-Vue/ruoyi-admin/src/main/resources/application-druid.yml`

## 6. 验证口径

1. `intent=view` 首屏展示闭环入口卡，并能触发“查看处置记录”主操作。
2. `intent=start` 首屏展示闭环入口卡，并能触发“进入待处置资产池”主操作。
3. 原第二批的来源横幅、范围说明、返回入口测试不回退。
4. 无来源参数时，统一处置页保持原行为。
