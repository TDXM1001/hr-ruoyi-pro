# 统一处置主流程第一批安全实现计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 先把统一处置页对不动产来源上下文的承接做成稳定、安全、可点测的前端接入层，不直接进入后端处置主流程改造。

**Architecture:** 本批只做统一处置页前端安全接入。通过新增来源上下文解释层和来源横幅/资产上下文卡，把 `source + intent + assetId + assetCode + assetName` 这组参数转成稳定的首屏体验；不改后端、不加 SQL、不碰处置后端脏文件。

**Tech Stack:** Vue 3、Element Plus、Vitest、现有资产处置页

---

### Task 1: 明确第一批安全改动边界

**Files:**
- Verify: `docs/plans/2026-03-23-asset-real-estate-mainline-implementation-checkpoint-table.md`
- Verify: `docs/plans/2026-03-23-asset-phase2-disposal-linkage-minimum-closure-isolation-design.md`

**Step 1: 对齐本批目标**

确认本批只做三件事：

1. 来源上下文解析
2. 首屏来源提示与返回入口
3. `intent=start / intent=view` 的前端分流

**Step 2: 明确禁区**

本批不改：

1. `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetDisposalController.java`
2. `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetDisposalServiceImpl.java`
3. `RuoYi-Vue/ruoyi-admin/src/main/resources/application-druid.yml`
4. 任何 SQL

**Step 3: 提交边界确认**

这一步不提交代码，只确认范围。

### Task 2: 写来源上下文解释层的失败测试

**Files:**
- Modify: `art-design-pro/tests/views/asset-disposal-page.test.ts`
- Create: `art-design-pro/src/views/asset/disposal/disposal-source-context.ts`

**Step 1: 增加失败测试**

覆盖至少三种场景：

1. `intent=view` 时展示来源横幅并保留记录页签
2. `intent=start` 时展示发起引导语义
3. 无来源参数时不展示来源增强内容

**Step 2: 运行测试确认红灯**

Run:

```bash
pnpm vitest run tests/views/asset-disposal-page.test.ts
```

Expected:

- 新增来源上下文相关用例失败

**Step 3: 提交**

```bash
git add art-design-pro/tests/views/asset-disposal-page.test.ts
git commit -m "test: 补充统一处置来源接入测试"
```

### Task 3: 实现来源上下文解释层

**Files:**
- Create: `art-design-pro/src/views/asset/disposal/disposal-source-context.ts`
- Modify: `art-design-pro/src/views/asset/disposal/index.vue`

**Step 1: 在解释层中实现最小视图模型**

至少产出：

1. 来源标题
2. 来源说明
3. 当前资产上下文
4. 默认页签建议
5. 返回路径信息
6. 当前筛选锁定说明

**Step 2: 在统一处置页接线**

只基于现有 route query 做前端解释，不新增 API 调用。

**Step 3: 运行测试确认通过**

Run:

```bash
pnpm vitest run tests/views/asset-disposal-page.test.ts
```

Expected:

- 来源上下文相关用例通过

**Step 4: 提交**

```bash
git add art-design-pro/src/views/asset/disposal/disposal-source-context.ts art-design-pro/src/views/asset/disposal/index.vue art-design-pro/tests/views/asset-disposal-page.test.ts
git commit -m "feat: 增加统一处置来源上下文解释层"
```

### Task 4: 补首屏来源横幅和返回入口

**Files:**
- Modify: `art-design-pro/src/views/asset/disposal/index.vue`
- Optional Create: `art-design-pro/src/views/asset/disposal/components/disposal-source-banner.vue`
- Modify: `art-design-pro/tests/views/asset-disposal-page.test.ts`

**Step 1: 写失败测试**

验证：

1. 有来源参数时显示来源横幅
2. 显示当前资产上下文
3. 显示返回不动产详情入口

**Step 2: 最小实现**

只增强 UI 结构，不改业务接口。

**Step 3: 跑测试**

Run:

```bash
pnpm vitest run tests/views/asset-disposal-page.test.ts
```

Expected:

- 首屏来源增强相关用例通过

**Step 4: 提交**

```bash
git add art-design-pro/src/views/asset/disposal/index.vue art-design-pro/tests/views/asset-disposal-page.test.ts
# 如果新增了组件也一起 add
git commit -m "feat: 增强统一处置页来源横幅与返回入口"
```

### Task 5: 完成 `intent` 分流

**Files:**
- Modify: `art-design-pro/src/views/asset/disposal/index.vue`
- Modify: `art-design-pro/tests/views/asset-disposal-page.test.ts`

**Step 1: 写失败测试**

验证：

1. `intent=view` 默认停留记录页签
2. `intent=start` 展示继续发起/继续办理的语义提示
3. 无来源参数时维持原始行为

**Step 2: 最小实现**

只做前端首屏行为控制，不改变处置后端流程。

**Step 3: 跑测试**

Run:

```bash
pnpm vitest run tests/views/asset-disposal-page.test.ts
```

Expected:

- `intent` 分流相关用例通过

**Step 4: 提交**

```bash
git add art-design-pro/src/views/asset/disposal/index.vue art-design-pro/tests/views/asset-disposal-page.test.ts
git commit -m "feat: 完成统一处置页来源意图分流"
```

### Task 6: 跑回归并补浏览器点测

**Files:**
- Verify only

**Step 1: 跑前端相关回归**

Run:

```bash
pnpm vitest run tests/views/asset-disposal-page.test.ts tests/views/asset-real-estate-detail-page.test.ts tests/api/asset-real-estate.test.ts
```

Expected:

- 全部通过

**Step 2: 做浏览器点测**

验证：

1. 从不动产详情壳点击 `发起处置` 进入统一处置页
2. 来源横幅和资产上下文正常显示
3. `intent=start` 语义正确
4. 返回入口可见
5. 无新的 Vue 警告

**Step 3: 提交**

```bash
git add art-design-pro/src/views/asset/disposal/index.vue art-design-pro/tests/views/asset-disposal-page.test.ts
# 如果新增了解释层/组件也一起 add
git commit -m "feat: 完善统一处置主流程第一批安全接入"
```

### Task 7: 文档与收尾

**Files:**
- Create: `docs/plans/2026-03-23-asset-unified-disposal-mainflow-first-batch-safe-implementation-design.md`
- Create: `docs/plans/2026-03-23-asset-unified-disposal-mainflow-first-batch-safe-implementation-plan.md`

**Step 1: 落盘设计与实施计划**

说明：

1. 第一批只做来源上下文安全接入
2. 第二批再考虑正式流程接入

**Step 2: 检查编码**

全部保存为 UTF-8 BOM。

**Step 3: 单独提交文档**

```bash
git add docs/plans/2026-03-23-asset-unified-disposal-mainflow-first-batch-safe-implementation-design.md docs/plans/2026-03-23-asset-unified-disposal-mainflow-first-batch-safe-implementation-plan.md
git commit -m "docs: 梳理统一处置主流程第一批安全实现清单"
```