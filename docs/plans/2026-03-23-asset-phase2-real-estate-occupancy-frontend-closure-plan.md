# 2026-03-23 不动产占用前端收口实施计划

## 1. 实施目标

本轮实施只做占用页签的“收口整理”，不扩新功能，不改后端规则。

目标：

1. 保留核心闭环
2. 保留合理摘要和筛选
3. 把高级能力统一收进治理工具区
4. 对前端代码做职责拆分

## 2. 实施范围

### 2.1 包含

1. 占用业务流程文档
2. 占用前端收口设计文档
3. 占用页签前端重构
4. 占用页签测试调整和回归

### 2.2 不包含

1. 新的后端接口
2. 新的占用业务规则
3. 新的 SQL
4. 新的治理增强批次

## 3. 实施顺序

## 3.1 第一步：文档先行

输出：

1. 占用业务流程文档
2. 占用前端收口设计文档
3. 占用前端收口实施计划

验收：

1. 业务边界清楚
2. 前端分层明确
3. 不再把治理能力误认为主流程

## 3.2 第二步：页面分区收口

目标：先调整页面结构，不急于拆很深的逻辑。

实施内容：

1. 重排主视图顺序
2. 让业务操作区前置
3. 摘要区紧跟业务区
4. 历史轨迹区保留在中部
5. 治理工具区统一折叠到底部

验收：

1. 首屏先看到核心业务
2. 治理工具不再抢主视图

## 3.3 第三步：组件拆分

目标：降低 `occupancy-panel.vue` 的职责密度。

建议新增组件：

1. `components/occupancy/OccupancyBusinessSection.vue`
2. `components/occupancy/OccupancySummarySection.vue`
3. `components/occupancy/OccupancyHistorySection.vue`
4. `components/occupancy/OccupancyGovernanceSection.vue`

验收：

1. 主组件只负责组装
2. 业务区、摘要区、历史区、治理区职责清楚

## 3.4 第四步：状态拆分

目标：把主流程状态和治理状态拆开。

建议新增状态模块：

1. `useOccupancyState`
2. `useOccupancyGovernance`

验收：

1. 主流程数据不再和治理配置混杂
2. 后续维护某一层时影响范围更小

## 3.5 第五步：测试收口

需要保留并回归的重点：

1. 当前有效占用显示
2. 发起占用入口
3. 变更占用入口
4. 释放占用入口
5. 主档联动摘要
6. 最近一次变更摘要
7. 历史筛选
8. 治理工具区折叠与展开

验收：

1. 核心业务测试继续通过
2. 治理入口测试继续通过
3. 不因为拆组件导致行为回退

## 4. 目标文件

### 4.1 预计修改

1. `art-design-pro/src/views/asset/real-estate/detail/components/occupancy-panel.vue`
2. `art-design-pro/tests/views/asset-real-estate-occupancy-panel.test.ts`

### 4.2 预计新增

1. `art-design-pro/src/views/asset/real-estate/detail/components/occupancy/OccupancyBusinessSection.vue`
2. `art-design-pro/src/views/asset/real-estate/detail/components/occupancy/OccupancySummarySection.vue`
3. `art-design-pro/src/views/asset/real-estate/detail/components/occupancy/OccupancyHistorySection.vue`
4. `art-design-pro/src/views/asset/real-estate/detail/components/occupancy/OccupancyGovernanceSection.vue`
5. `art-design-pro/src/views/asset/real-estate/detail/components/occupancy/useOccupancyState.ts`
6. `art-design-pro/src/views/asset/real-estate/detail/components/occupancy/useOccupancyGovernance.ts`

## 5. 风险与控制

### 5.1 风险一：拆分后交互回退

控制方式：

1. 先保留现有数据结构
2. 先做视图分层，再做逻辑迁移
3. 每一步都跑定向测试

### 5.2 风险二：治理工具区能力丢失

控制方式：

1. 不删除治理能力
2. 只调整归位方式
3. 保留治理入口测试

### 5.3 风险三：页面虽然拆了，但用户仍觉得乱

控制方式：

1. 先确保首屏只出现业务必需内容
2. 治理工具默认折叠
3. 避免把治理统计继续放进主业务区

## 6. 完成口径

这轮完成的标准不是“占用页签增加了新能力”，而是：

1. 用户第一次进入时能立刻看懂主流程
2. 日常业务操作不需要先理解治理工具
3. 占用主线与治理增强正式分层
4. 后续如果继续扩治理能力，也不会再次污染主视图
