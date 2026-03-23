# 不动产占用状态矩阵第十六批实施计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 为不动产占用页签补齐治理审计能力，让策略模板、趋势快照、重置记录都可回看、可对比、可导出。

**Architecture:** 继续只在 `occupancy-panel.vue` 内扩展治理工具区，不新增后端接口和 SQL。实现顺序遵循 TDD，先写失败测试，再做最小实现，最后跑占用页签定向和前端回归测试。

**Tech Stack:** Vue 3、Element Plus、Vitest、localStorage。

---

## Task 1 落盘设计文档
- 新建第十六批设计文档
- 明确治理审计型边界

## Task 2 按 TDD 补失败测试
- 在 `asset-real-estate-occupancy-panel.test.ts` 新增失败测试：
  - 模板详情与最近应用信息展示
  - 两个趋势快照差异对比
  - 重置记录范围筛选与关键字筛选
  - 重置记录按当前筛选结果导出 JSON
- 先运行定向测试确认 RED

## Task 3 实现第十六批能力
- 在 `occupancy-panel.vue` 中新增：
  - 模板审计详情与最近应用结果留痕
  - 快照差异对比面板
  - 重置记录筛选与导出
- 保持治理工具区收敛，不新增新的一级入口

## Task 4 验证

### 4.1 定向验证
```bash
pnpm vitest run tests/views/asset-real-estate-occupancy-panel.test.ts
```

### 4.2 回归验证
```bash
pnpm vitest run tests/views/asset-real-estate-detail-page.test.ts tests/views/asset-real-estate-occupancy-panel.test.ts tests/views/asset-real-estate-inspection-task-page.test.ts tests/views/asset-real-estate-rectification-form-page.test.ts tests/views/asset-real-estate-rectification-complete-page.test.ts tests/views/asset-real-estate-rectification-panel-approval.test.ts tests/api/asset-real-estate.test.ts
```

### 4.3 浏览器点测
- 查看模板详情和最近应用摘要
- 创建两个快照并查看差异对比
- 过滤重置记录并导出 JSON

## Task 5 提交收口
- 仅提交第十六批相关前端文件与文档
- 不带入 disposal/approval 脏文件
- 中文提交信息保持“占用状态矩阵第十六批体验”口径

