# 不动产占用管理状态矩阵第十七批 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 为不动产占用页签补齐治理归档能力，形成治理摘要卡、治理活动流与治理审计包导出的完整前端闭环。

**Architecture:** 保持现有占用页签单组件实现，在治理工具区内部补充治理摘要、活动留痕和统一导出能力。所有状态按资产编码维度持久化到 `localStorage`，不改后端接口、不新增 SQL。

**Tech Stack:** Vue 3、TypeScript、Element Plus、Vitest、Vue Test Utils、localStorage

---

### Task 1: 落盘第十七批文档

**Files:**
- Create: `docs/plans/2026-03-23-asset-phase2-real-estate-occupancy-matrix-seventeenth-batch-design.md`
- Create: `docs/plans/2026-03-23-asset-phase2-real-estate-occupancy-matrix-seventeenth-batch-plan.md`

**Step 1: 写入设计文档**

- 说明治理归档型目标、范围、交互和持久化方案。

**Step 2: 写入实施计划**

- 明确测试优先、最小实现、回归验证与提交口径。

### Task 2: 为治理摘要卡和活动流补失败测试

**Files:**
- Modify: `art-design-pro/tests/views/asset-real-estate-occupancy-panel.test.ts`

**Step 1: 写失败测试**

- 新增“展示治理摘要卡”的测试
- 新增“展示并筛选治理活动流”的测试

**Step 2: 跑测试验证失败**

Run:

```bash
pnpm vitest run art-design-pro/tests/views/asset-real-estate-occupancy-panel.test.ts
```

Expected:

- 新增断言失败，提示缺少摘要卡或活动流节点

### Task 3: 为治理审计包导出补失败测试

**Files:**
- Modify: `art-design-pro/tests/views/asset-real-estate-occupancy-panel.test.ts`

**Step 1: 写失败测试**

- 新增“导出治理审计包”的测试
- 新增“记录最近导出时间”的测试

**Step 2: 跑测试验证失败**

Run:

```bash
pnpm vitest run art-design-pro/tests/views/asset-real-estate-occupancy-panel.test.ts
```

Expected:

- 新增断言失败，提示缺少导出入口或导出元数据

### Task 4: 实现治理摘要卡与活动流

**Files:**
- Modify: `art-design-pro/src/views/asset/real-estate/detail/components/occupancy-panel.vue`

**Step 1: 写最小实现**

- 新增治理活动类型、活动条目与导出元数据状态
- 新增治理摘要卡与活动流 UI
- 为模板、快照、重置动作补活动记录

**Step 2: 跑测试验证通过**

Run:

```bash
pnpm vitest run art-design-pro/tests/views/asset-real-estate-occupancy-panel.test.ts
```

Expected:

- 摘要卡与活动流相关测试通过

### Task 5: 实现治理审计包导出

**Files:**
- Modify: `art-design-pro/src/views/asset/real-estate/detail/components/occupancy-panel.vue`

**Step 1: 写最小实现**

- 新增“导出治理审计包”按钮
- 组装 JSON 内容
- 写入最近导出时间
- 导出动作写入治理活动流

**Step 2: 跑测试验证通过**

Run:

```bash
pnpm vitest run art-design-pro/tests/views/asset-real-estate-occupancy-panel.test.ts
```

Expected:

- 导出与最近导出时间测试通过

### Task 6: 回归验证

**Files:**
- Modify: `art-design-pro/src/views/asset/real-estate/detail/components/occupancy-panel.vue`
- Modify: `art-design-pro/tests/views/asset-real-estate-occupancy-panel.test.ts`

**Step 1: 跑占用页签定向测试**

Run:

```bash
pnpm vitest run art-design-pro/tests/views/asset-real-estate-occupancy-panel.test.ts
```

**Step 2: 跑前端回归**

Run:

```bash
pnpm vitest run art-design-pro/tests/views/asset-real-estate-detail-page.test.ts art-design-pro/tests/views/asset-real-estate-occupancy-panel.test.ts art-design-pro/tests/views/asset-real-estate-inspection-task-page.test.ts art-design-pro/tests/views/asset-real-estate-rectification-form-page.test.ts art-design-pro/tests/views/asset-real-estate-rectification-complete-page.test.ts art-design-pro/tests/views/asset-real-estate-rectification-panel-approval.test.ts art-design-pro/tests/api/asset-real-estate.test.ts
```

Expected:

- 定向测试和回归测试全部通过

### Task 7: 提交

**Files:**
- Modify: `art-design-pro/src/views/asset/real-estate/detail/components/occupancy-panel.vue`
- Modify: `art-design-pro/tests/views/asset-real-estate-occupancy-panel.test.ts`
- Create: `docs/plans/2026-03-23-asset-phase2-real-estate-occupancy-matrix-seventeenth-batch-design.md`
- Create: `docs/plans/2026-03-23-asset-phase2-real-estate-occupancy-matrix-seventeenth-batch-plan.md`

**Step 1: 提交**

```bash
git add docs/plans/2026-03-23-asset-phase2-real-estate-occupancy-matrix-seventeenth-batch-design.md docs/plans/2026-03-23-asset-phase2-real-estate-occupancy-matrix-seventeenth-batch-plan.md art-design-pro/src/views/asset/real-estate/detail/components/occupancy-panel.vue art-design-pro/tests/views/asset-real-estate-occupancy-panel.test.ts
git commit -m "feat: 完善不动产占用状态矩阵第十七批体验"
```
