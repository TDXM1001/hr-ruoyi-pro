# Disposal Linkage Minimum Closure Isolation Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 在不触碰当前处置脏文件的前提下，先明确不动产详情壳与统一处置模块之间的最小闭环分工、状态边界和后续实现顺序。

**Architecture:** 本计划当前阶段只落设计和实施边界，不直接进入代码实现。核心做法是由不动产详情页负责发起与摘要，统一处置模块负责正式流程，并通过最小状态映射层与结果回写建立闭环。

**Tech Stack:** Markdown、现有 Vue 前端结构、现有资产处置模块

---

### Task 1: 落盘隔离设计文档

**Files:**
- Create: `docs/plans/2026-03-23-asset-phase2-disposal-linkage-minimum-closure-isolation-design.md`

**Step 1: 写设计文档**
- 记录背景、目标、方案对比、推荐方案、状态建议和安全边界。

**Step 2: 保存为 UTF-8 BOM**
- 确保中文文档在 IDE 中稳定显示。

### Task 2: 落盘实施计划

**Files:**
- Create: `docs/plans/2026-03-23-asset-phase2-disposal-linkage-minimum-closure-isolation-plan.md`

**Step 1: 写实施计划**
- 先写“设计确认阶段”，再写“后续进入实现时的推荐顺序”。

**Step 2: 说明不立即实现的原因**
- 明确当前风险来自工作区脏文件碰撞，不来自业务设计本身。

### Task 3: 定义后续实现顺序

**Files:**
- Verify only

**Step 1: 先做不动产侧摘要与发起入口设计确认**
- 明确处置页签的轻摘要结构。

**Step 2: 再做统一处置模块接收资产上下文的隔离适配设计**
- 避免直接改现有处置核心文件。

**Step 3: 最后再进入最小实现**
- 包含发起、记录建立、状态回写、详情回看。

### Task 4: 单独提交文档

**Files:**
- Verify only

**Step 1: 检查工作区边界**
- 确认没有触碰处置脏文件和 `application-druid.yml`。

**Step 2: 提交**
- Commit: `docs: 梳理不动产处置关联最小闭环隔离设计`
