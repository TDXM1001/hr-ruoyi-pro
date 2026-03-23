# Rectification Approval Writeback Fourth Batch Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 统一不动产详情壳顶部摘要、总览摘要和整改页签的闭环提示口径，并让整改轨迹展示整改关键阶段标签。

**Architecture:** 继续复用前端共享解释层 `rectification-overview.ts`，由详情壳父层统一计算闭环摘要和阶段化轨迹，再传给整改页签消费。第四批不新增接口、不新增 SQL，只做前端收口和测试补强。

**Tech Stack:** Vue 3、TypeScript、Element Plus、Vitest

---

### Task 1: 落盘第四批设计与实施计划

**Files:**
- Create: `docs/plans/2026-03-23-asset-phase2-rectification-approval-writeback-fourth-batch-design.md`
- Create: `docs/plans/2026-03-23-asset-phase2-rectification-approval-writeback-fourth-batch-plan.md`

**Step 1: 写设计文档**
- 记录第四批目标、方案对比、推荐方案、影响范围和验证口径。

**Step 2: 写实施计划**
- 按 TDD 颗粒度列出测试、实现、验证步骤。

### Task 2: 先写失败测试锁住统一闭环口径

**Files:**
- Modify: `art-design-pro/tests/views/asset-real-estate-detail-page.test.ts`
- Modify: `art-design-pro/tests/views/asset-real-estate-rectification-panel-approval.test.ts`

**Step 1: 写详情壳集成失败测试**
- 断言总览和整改页签面对同一批整改单时，展示同一闭环状态标签和下一步建议。

**Step 2: 写整改页签失败测试**
- 断言整改页签展示共享闭环状态、最近整改动作、下一步建议，以及整改轨迹中的阶段标签。

**Step 3: 运行定向测试确认失败**
- Run: `pnpm vitest run tests/views/asset-real-estate-detail-page.test.ts tests/views/asset-real-estate-rectification-panel-approval.test.ts`
- Expected: 至少新增用例失败，失败点集中在第四批尚未实现的展示逻辑。

### Task 3: 用最小实现打通共享闭环摘要

**Files:**
- Modify: `art-design-pro/src/views/asset/real-estate/detail/index.vue`
- Modify: `art-design-pro/src/views/asset/real-estate/detail/components/rectification-panel.vue`
- Modify: `art-design-pro/src/views/asset/real-estate/detail/components/rectification-overview.ts`

**Step 1: 父层统一传递摘要与阶段化轨迹**
- 让整改页签直接消费 `rectificationOverviewSummary`。
- 让整改页签使用阶段化后的整改轨迹，而不是独立的原始日志展示。

**Step 2: 整改页签改为共享闭环提示**
- 用共享摘要替换本地分支文案。
- 展示当前闭环状态、最近整改动作、下一步建议。

**Step 3: 整改轨迹补齐阶段标签**
- 对整改和审批节点显示阶段标签与提示说明。

**Step 4: 运行定向测试确认通过**
- Run: `pnpm vitest run tests/views/asset-real-estate-detail-page.test.ts tests/views/asset-real-estate-rectification-panel-approval.test.ts`
- Expected: 新增和既有相关测试全部通过。

### Task 4: 做回归验证并提交

**Files:**
- Verify only

**Step 1: 运行相关前端回归**
- Run: `pnpm vitest run tests/views/asset-real-estate-detail-page.test.ts tests/views/asset-real-estate-rectification-panel-approval.test.ts tests/views/asset-real-estate-rectification-form-page.test.ts tests/views/asset-real-estate-rectification-complete-page.test.ts tests/api/asset-real-estate.test.ts`
- Expected: 全部通过。

**Step 2: 检查工作区边界**
- 确认没有改动处置脏文件和 `application-druid.yml`。

**Step 3: 提交**
- Commit: `feat: 完善不动产整改审批回写第四批`
