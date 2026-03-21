# 不动产占用管理前端闭环 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 为不动产详情页补齐占用管理前端闭环，支持当前占用卡片、历史记录、发起/变更/释放 Drawer，以及本地点测样例 SQL。

**Architecture:** 继续沿用详情壳页内 Tab，不新增独立页面。占用主数据以生命周期接口中的 `occupancyRecords` 为准；写操作调用占用接口，完成后统一刷新详情与生命周期。

**Tech Stack:** Vue3 + Element Plus + Vitest + Playwright + MySQL

---

### Task 1: 补占用样例 SQL 并执行到本地库

**Files:**
- Create: `RuoYi-Vue/sql/asset/20-asset-real-estate-occupancy-sample-upgrade-20260322.sql`

**Steps:**
1. 为 `asset_id=20001` 插入 1 条 `ACTIVE` 占用样例
2. 同步 `ast_asset_ledger` 当前使用快照
3. 保证脚本可重复执行
4. 执行到本地库 `ruoyi-assets`
5. 校验样例已存在

### Task 2: 先写前端失败测试

**Files:**
- Modify: `art-design-pro/tests/api/asset-real-estate.test.ts`
- Create: `art-design-pro/tests/views/asset-real-estate-occupancy-panel.test.ts`
- Modify: `art-design-pro/tests/views/asset-real-estate-detail-page.test.ts`

**Steps:**
1. 先补占用 API 合同测试
2. 再补占用页签组件行为测试
3. 补详情页占用集成测试
4. 运行 Vitest，确认因接口或页面缺失失败

### Task 3: 补占用前端 API 与类型

**Files:**
- Modify: `art-design-pro/src/api/asset/ledger.ts`
- Modify: `art-design-pro/src/api/asset/real-estate.ts`

**Steps:**
1. 给生命周期类型补 `occupancyRecords`
2. 增加占用 VO / Payload 类型
3. 增加占用查询、发起、变更、释放 API

### Task 4: 实现占用 Tab 前端闭环

**Files:**
- Modify: `art-design-pro/src/views/asset/real-estate/detail/index.vue`
- Modify: `art-design-pro/src/views/asset/real-estate/detail/components/occupancy-panel.vue`

**Steps:**
1. 详情壳传入 `assetId / occupancyRecords / canEdit`
2. 占用页签渲染当前占用卡片
3. 占用页签渲染历史记录列表
4. 增加发起占用 Drawer
5. 增加变更占用 Drawer
6. 增加释放占用 Drawer
7. 提交成功后刷新详情与生命周期

### Task 5: 运行前端测试并修到全绿

**Steps:**
1. 运行占用相关 Vitest
2. 修到全部通过

### Task 6: 浏览器点测

**Steps:**
1. 打开不动产详情页占用 Tab
2. 验证样例占用展示
3. 实测发起/变更/释放至少一条链路
4. 记录点测结论

### Task 7: 提交第二批前端闭环

**Steps:**
1. 仅 stage 本批安全文件
2. 使用中文提交信息

```bash
git commit -m "feat: 完成不动产占用管理前端闭环"
```