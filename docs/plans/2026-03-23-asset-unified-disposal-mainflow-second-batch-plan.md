# 统一处置主流程第二批实现计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 把统一处置页的不动产来源入口收成一套清晰的首屏来源视图，减少重复提示并统一当前锁定范围与下一步建议。

**Architecture:** 第二批继续只做统一处置页前端安全接入层。保留来源横幅，增强来源接入卡，收回 tab 内部重复的来源 alert；不改后端、不加 SQL、不碰处置后端脏文件。

**Tech Stack:** Vue 3、Element Plus、Vitest、现有资产处置页

---

### Task 1: 扩展来源上下文失败测试

**Files:**
- Modify: `art-design-pro/tests/views/asset-disposal-page.test.ts`

**Step 1: 写失败测试**

覆盖：

1. `intent=view` 时来源接入卡展示“当前锁定范围”和“下一步建议”
2. `intent=start` 时来源接入卡展示“待处置资产池”语义
3. 有来源参数时 tab 内部重复 alert 不再展示
4. 无来源参数时 tab 内部 alert 仍然存在

**Step 2: 跑测试确认红灯**

Run:
```bash
pnpm vitest run tests/views/asset-disposal-page.test.ts
```

### Task 2: 扩展来源上下文解释层

**Files:**
- Modify: `art-design-pro/src/views/asset/disposal/disposal-source-context.ts`

**Step 1: 增加来源接入卡需要的字段**

至少新增：

1. `scopeTitle`
2. `scopeDescription`
3. `nextStepSuggestion`

**Step 2: 保持现有来源行为不回退**

1. `intent=view` 仍优先落 `record`
2. `intent=start` 仍优先落 `pool`

### Task 3: 改统一处置页首屏来源视图

**Files:**
- Modify: `art-design-pro/src/views/asset/disposal/index.vue`

**Step 1: 在来源接入卡中新增统一信息区**

展示：

1. 当前锁定范围
2. 下一步建议

**Step 2: 收回 tab 内部重复来源 alert**

规则：

1. 有来源参数时隐藏 tab 内来源 alert
2. 无来源参数时保留原逻辑

### Task 4: 跑回归与浏览器点测

**Files:**
- Verify only

**Step 1: 跑前端回归**

Run:
```bash
pnpm vitest run tests/views/asset-disposal-page.test.ts tests/views/asset-real-estate-detail-page.test.ts
```

**Step 2: 做真实浏览器点测**

验证：

1. `intent=view` 首屏来源视图统一
2. `intent=start` 首屏来源视图统一
3. tab 内无重复来源 alert
4. 返回入口正常
5. 控制台无新增告警

### Task 5: 提交

**Step 1: 提交代码**

```bash
git add art-design-pro/src/views/asset/disposal/disposal-source-context.ts art-design-pro/src/views/asset/disposal/index.vue art-design-pro/tests/views/asset-disposal-page.test.ts docs/plans/2026-03-23-asset-unified-disposal-mainflow-second-batch-design.md docs/plans/2026-03-23-asset-unified-disposal-mainflow-second-batch-plan.md
git commit -m "feat: 完善统一处置主流程第二批来源视图"
```
